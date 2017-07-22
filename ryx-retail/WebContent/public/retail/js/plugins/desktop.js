


/**
 * 消息推送
 */
HOME.Core.register("plugin-msgPush", function(box) {
	
	//消息推送
	var socketJs = "public/js/common/socket.io.js";
	var socket = null;
	var msgContent = null;
	
	function loadSocketJs(){
		box.notify({
			type: HOME.Const.LOAD_JSPLUGINS,
			data: {
				url: socketJs, 
				success: socketConn
			}
		});
	}
	
	function socketConn(e){
		var lastMsg = {};
	 	socket = io.connect(box.context.msgPushUrl,{"force new connection":true});
	    socket.on("connect",iconnected);//建立连接
	    socket.on("messages",imessages);//得到未读消息
		socket.on("feedOver",ifeedOver);//得到修改后的json
//		socket.on("disconnect",idisconnect);//断开连接
	    socket.on("error",ierror);//连接错误
	    
	    //关闭按钮
	    $("#msgPushCloseBtn").click(function (){
	    	$("#msgPushDiv").hide();
	    });
	    
	    //查看列表
	    $("#msgPurchList").click(function(){
	    	$("#viewggxx").click();
	    	$("#msgPushDiv").hide();
	    });
	    
	    //读取最后一条数据
	    $("#msgPushTitleBtn").click(function (){
	    	if (msgContent && msgContent.length > 0) {
	    		box.closeDialog({content:msgContent});
	    	}
	    	if (lastMsg.type == '1001' || lastMsg.type == '1002' || lastMsg.type == '1003' ) {
	    		msgContent = box.ich.msg_purch_detail_xxgg(lastMsg.msg);
		    	msgContent = box.showDialog({
					title: "系统消息",
					content: msgContent,
					width: 780,
					height: 570
				});
			} else if (lastMsg.type == '1004') {//处理咨询反馈
				msgContent = box.ich.msg_purch_detail_zxfk(lastMsg.msg);
				msgContent = box.showDialog({
					title: "咨询/投诉",
					content: msgContent,
					width: 780,
					height: 570
				});
			}
			
	    	$("#msgPushDiv").hide();
	    	msgContent.find("#msgPushInfoCloseBtn").click(function(){
	    	box.closeDialog({content:msgContent});
	    	});
	    	
	    });
	    
	    //建立连接
		function iconnected(){
			var userId = 'liceid:'+box.user.user_code;
	        socket.emit("identify",{userId:userId, product:'pcpos'});
	    }
		
		//得到未读的消息
	    function imessages(data){
	    	var newRefDesc = "";
	    	if (data && data.length > 0) {
	    		for ( var i = 0; i < data.length; i++) {
	    			if (data[i].type == "message") {
	    				change(data[i], 1);//修改消息状态
	    				var msg = jQuery.parseJSON(data[i].content);
	    				//处理系统公告
	    				if (msg.type == '1001' || msg.type == '1002' || msg.type == '1003' ) {
	    					lastMsg = msg;
	    					newRefDesc = disposeXXGG();
	    					$("#msgPushDiv").find("#msgPushTitle").text("新公告");
	    				} else if (msg.type == '1004') {//处理咨询反馈
	    					$("#msgPushDiv").find("#msgPushTitle").text("新回复");
	    					lastMsg = msg;
	    					newRefDesc = disposeZXFK();
	    				}
	    			}
				}
	    		
	    		$("#msgPushDiv").find("#msgPushTitleBtn").text(newRefDesc);
	    		$("#msgPushDiv").show();
	    	}
	    	
	    	box.console.log("messages:"+$.obj2str(data));
	    }
	    
	    //处理系统公告
	    function disposeXXGG() {
	    	var newRefDesc = "";
	    	if (lastMsg && lastMsg.msg.ref_desc ) {
    			newRefDesc = lastMsg.msg.ref_desc;
    			if (newRefDesc.length >40){
	    			newRefDesc = newRefDesc.substring(0, 35);
	    			newRefDesc = newRefDesc+"....";
	    		}
    		}
	    	return newRefDesc;
	    }
	    
	    //处理咨询反馈
	    function disposeZXFK() {
	    	var newRefDesc = "";
	    	if (lastMsg && lastMsg.msg.info_desc ) {
    			newRefDesc = lastMsg.msg.info_desc;
    			if (newRefDesc.length >40){
	    			newRefDesc = newRefDesc.substring(0, 35);
	    			newRefDesc = newRefDesc+"....";
	    		}
    		}
	    	return newRefDesc;
	    }
		
	    //得到修改后result
	    function ifeedOver(data){
	    	box.console.log("feedOver:"+JSON.stringify(data));
	    }
		
	  	//修改消息状态-已收到
	    function change(msgData, state){
	    	socket.emit("feed",{stateId:msgData.stateId, state:state});
	    }
	    
	    //错误
	    function ierror(e){
	        box.console.log("消息推送 连接错误:"+e);
	    }
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.INIT_COMPLETE, loadSocketJs);
		},
		destroy: function() { }
	};
});

/**
 * 零售户主页面
 */
HOME.Core.register("plugin-desktop", function(box) {

	var exitUrl = "user/logout";
	var testUrl = "retail/test";
	var weatherUrl = "pub/weather/getWeather";
	var getUnreadWGDDCountUrl="retail/order/searchUnreadWGDDCount";//获得网购订单未读条数
	var context = $("#navwrap");
	var items = context.find(".navlink");
	var navbar = $("#navbarcontainer");
	var navbaritems = navbar.find(".navbaritem");
	var navbarindex = -1;
	
	var exit = $("#exit");
	var timeout = -1;
	
	window.consolelog = function(str) {
		box.console.log(str);
	};
	
	function listener() {
		items.click(itemClickHandler);
		navbaritems.click(itemClickHandler);
		
		exit.click(exitClickHandler);
		
		navbar.hover(showNavbar, hideNavbar);
		navbar.find(".navbar").hover(function() {
			navbaritems.removeClass("active");
			navbarindex = -1;
		});
		
		box.keys.push("desktop_onkey");
		
		startTest();
		
		$(document).on("click","input[type='text']",function(e){
			e.preventDefault();
			$(this).select();
		});
	}
	
	function startTest() {
		// 10分钟与服务器连接一次
		timeout = setTimeout(function() {
			box.request({
				url: box.getContextPath() + testUrl,
				success: onTestComplete,
				showLoading: false
			});
			startTest();
		}, 600000);
	}
	
	function onTestComplete(data) {
		box.console.log("test message:" + data.msg);
		putActivity();
		getWeather();
		getUnreadWGDDCount();//获得网购订单未读条数
	}
	
	function showNavbar() {
		navbar.stop();
		navbar.animate({
			right: "0px"
		}, {
			easing: 'easeOutQuad',
			duration: 300 
		});
		navbar.find(".navbartop").show();
	}
	function hideNavbar() {
		navbar.stop();
		navbar.animate({
			right: "-82px"
		}, 300, 'easeOutQuad', function() {
			navbar.find(".navbartop").hide();
		});
	}
	
	function itemClickHandler(e) {
		var $target = $(e.currentTarget);
		var module = $target.attr("data-module");
		if(module) {
			box.notify({
				type: HOME.Const.SHOW_MODULE,
				data: {moduleId: module, target: $target}
			});
		}
	}
	
	function exitClickHandler() {
		box.request({
			url: box.getContextPath() + exitUrl,
			success: onExitHandler
		});
	}
	
	function onExitHandler(data) {
		box.showAlert({message: "已注销，将会转向到登录页面！"});
		setTimeout(function() {
			window.location.href = data["result"];
		}, 1000);
	}
	
	function onInitComplete() {
		$("#viewggxx").click();
		putActivity();
		setTimeout(function() {
			putActivity();
			getWeather();
			getUnreadWGDDCount();//获得网购订单未读条数
		}, 200);
	}
	
	function putActivity() {
		var data = {path: box.getContextFullPath(), url: 'consumer/promotion/searchMerchPromotion?params={"merch_id":"' + box.user.merch_id + '"}'};
		box.com.commSend({
			type: "showactivity",
			message: data
		});
	}
	
	function getWeather() {
		box.request({
			url: box.getContextPath() + weatherUrl,
			success: onGetWeather,
			showLoading: false,
			error: function() {
				return false;
			}
		});
	}
	//获得未读的网购订单
	function getUnreadWGDDCount() {
		box.request({
			url: box.getContextPath() + getUnreadWGDDCountUrl,
			success: onGetUnreadWGDDCount
		});
	}
	function onGetUnreadWGDDCount(data){
		if(data.code == "0000" && data.result) {
			$("#unreadcount").html(data.result);
		}else{
			$("#unreadcount").html("0" );
		}
	}
	function onGetWeather(data) {
		if(data.code == "0000" && data.result) {
			box.context.weather = data.result;
			
			var whtml = box.ich.widget_weather(box.context.weather);
			$("#widgetweather").empty().append(whtml.html());
			
			box.com.commSend({
				type: "showweather",
				message: box.context.weather
			});
		}
	}
	
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.F1: $("#spxs").click(); break;
			case HOME.Keys.F2: $("#jydh").click(); break;
			case HOME.Keys.F3: $("#spgl").click(); break;
			case HOME.Keys.F4: $("#kcgl").click(); break;
			case HOME.Keys.F5: $("#wgdd").click(); break;
			case HOME.Keys.F6: $("#xsgl").click(); break;
			case HOME.Keys.F7: $("#tjfx").click(); break;
			case HOME.Keys.F8: $("#hygl").click(); break;
			case HOME.Keys.F9: $("#yxhd").click(); break;
			case HOME.Keys.F10: $("#ycjt").click(); break;
			case HOME.Keys.F11: $("#zzfw").click(); break;
			case HOME.Keys.F12: $("#dpsz").click(); break;
			case HOME.Keys.LEFT: showNavbar(); break;
			case HOME.Keys.RIGHT: hideNavbar(); break;
			case HOME.Keys.UP:
				navbarindex--;
				if(navbarindex < 0) navbarindex = navbaritems.length - 1;
				navbaritems.removeClass("active");
				navbaritems.eq(navbarindex).addClass("active");
				break;
			case HOME.Keys.DOWN:
				navbarindex++;
				if(navbarindex >= navbaritems.length) navbarindex = 0;
				navbaritems.removeClass("active");
				navbaritems.eq(navbarindex).addClass("active");
				break;
			case HOME.Keys.ENTER:
				if(navbaritems[navbarindex]) {
					navbaritems.eq(navbarindex).click();
				}
				break;
			case HOME.Keys.TAB:
				box.notify({
					type: HOME.Const.SHOW_MODULE,
					data: {moduleId: "spxs1", target: $("#spxs")}
				});
				break;
		}
	}
	
	return {
		init: function(){
			listener();
			box.listen(HOME.Const.INIT_COMPLETE, onInitComplete);
			box.listen("desktop_onkey", onKey);
		},
		destroy: function() { }
	};
});

/**
 * 桌面图标滚动
 */
HOME.Core.register("plugin-autoroll", function(box) {
	
	var list = new Array();
	var currentOver = null;
	var timeout = -1;
	
	function addRollItem(data) {
		var len = list.length;
		var b = false;
		for(var i=0; i<len; i++) {
			if(list[i].id == data.id) {
				itemAddRollItem(list[i], data);
				b = true;
				break;
			}
		}
		if(!b) {
			var item = {
					id: data.id,
					count: 0,
					length: 1,
					index: 0,
					trigger: 8,
					ori: "up"
				};
			itemAddRollItem(item, data);
			list.push(item);
		}
	}
	
	function removeRollItem(data) {
		var len = list.length;
		for(var i=0; i<len; i++) {
			if(list[i].id == data.id) {
				itemRemoveRollItem(list[i], data);
				break;
			}
		}
	}
	
	function itemAddRollItem(item, data) {
		var ele = $("#" + item.id);
		
		var ul = ele.find("ul");
		if(ul.length > 0) {
			if(data.position == "before") {
				ul.prepend(data.tags);
				item.index++;
			} else {
				ul.append(data.tags);
			}
		} else {
			ul = $("<ul class='navlinkul'><li>" + ele.html() + "</li></ul>");
			if(data.position == "before") {
				ul.prepend(data.tags);
				item.index++;
			} else {
				ul.append(data.tags);
			}
			ele.empty().append(ul);
			if(data.count) item.count = data.count;
		}
		item.length++;
		
		refreshPosition(ul, item);
	}
	
	function itemRemoveRollItem(item, data) {
		var ele = $("#" + item.id);
		
		var ul = ele.find("ul");
		var ls = ul.find("li[data-tag=" + data.tag + "]").remove();
		item.length -= ls.length;
	}
	
	function refreshPosition(ul, item) {
		var top = -(item.index * 120);
		ul.css("top", top);
	}
	
	function startTimer() {
		timeout = setTimeout(onTimer, 1000);
		
		var context = $("#navwrap");
		var items = context.find(".navlink");
		items.hover(function(e) {
			currentOver = $(e.currentTarget);
		}, function() {
			currentOver = null;
		});
	}
	
	function onTimer() {
		var len = list.length;
		for(var i=0; i<len; i++) {
			list[i].count++;
			
			if(list[i].count >= list[i].trigger) {
				rollItem(list[i]);
				list[i].count = 0;
			}
		}
		timeout = setTimeout(onTimer, 1000);
	}
	
	function rollItem(item) {
		
		if(currentOver && currentOver.attr("id") == item.id) {
			// 鼠标在当前区域上，不做滚动
			return;
		}
		
		var ele = $("#" + item.id).find("ul");
		
		if(item.ori == "down" && item.index == 0) {
			item.ori = "up";
		} else if(item.ori == "up" && item.index == item.length-1) {
			item.ori = "down";
		}
		if(item.count > 1) {
			var offset = null;
			if(item.ori == "up") {
				offset = "-=120px";
				item.index++;
			}
			else if(item.ori == "down") {
				offset = "+=120px";
				item.index--;
			}
			
			if(offset) {
				ele.animate({
					top: offset
				}, {
					easing: 'easeOutQuad',
					duration: 500 
				});
			}
		}
	}
	
	function onPause() {
		clearTimeout(timeout);
	}
	function onRestart() {
		timeout = setTimeout(onTimer, 1000);
	}
	
	return {
		init: function(){
			/** 20140803 屏蔽桌面的滑动广告 */
			//box.listen(HOME.Const.INIT_COMPLETE, startTimer);
			box.listen("addRollItem", addRollItem);
			box.listen("removeRollItem", removeRollItem);
			box.listen("pauseRoll", onPause);
			box.listen("restartRoll", onRestart);
		},
		destroy: function() { }
	};
});

/**
 * 加载系统参数
 */
HOME.Core.register("plugin-loadads", function(box) {
	
	function tobaccoComplete(data) {
		/*
		var rollItem = {
				id: "jydh",
				count: 4,
				tags: box.ich.roll_jydh_item1(box.tobacco.params)
		};
		box.notify({
			type: "addRollItem",
			data: rollItem
		});
*/
		/*
		rollItem = {
				id: "jydh",
				tags: box.ich.roll_ad_item({adimg: box.getContextPath() + "public/retail/resource/taishanxinpinad.jpg"})
		};
		box.notify({
			type: "addRollItem",
			data: rollItem
		});
		rollItem = {
				id: "jydh",
				position: "before",
				tags: box.ich.roll_ad_item({adimg: box.getContextPath() + "public/retail/resource/boguangad.jpg"})
		};
		box.notify({
			type: "addRollItem",
			data: rollItem
		});
		*/
		if(box.tobacco.params) {
			//var begin = box.tobacco.params.order_begintime.substring(0, 14);
			//var end = box.tobacco.params.order_endtime.substring(0, 14);
			//box.tobacco.params.order_begintime_format = begin;
			//box.tobacco.params.order_endtime_format = end;
			
			//日期时间格式化为yyyy-MM-dd hh:mm
			var beginDate=new Date(box.tobacco.params.order_begintime.substr(0, 4),box.tobacco.params.order_begintime.substr(4, 2)-1,box.tobacco.params.order_begintime.substr(6, 2),
					               box.tobacco.params.order_begintime.substr(9, 2),box.tobacco.params.order_begintime.substr(12, 2));
            var endDate=new Date(box.tobacco.params.order_endtime.substr(0, 4),box.tobacco.params.order_endtime.substr(4, 2)-1,box.tobacco.params.order_endtime.substr(6, 2),
            		             box.tobacco.params.order_endtime.substr(9, 2),box.tobacco.params.order_endtime.substr(12, 2));
            box.tobacco.params.order_begintime_format=beginDate.format("yyyy-MM-dd hh:mm");
            box.tobacco.params.order_endtime_format=endDate.format("yyyy-MM-dd hh:mm");
			
			$("#ordertime").html("开始：" + box.tobacco.params.order_begintime_format + "<br />结束：" + box.tobacco.params.order_endtime_format);
		}
	}
	
	function initComplete() {
		/** 20140803 去掉桌面上的滑动广告
		var rollItem = {
				id: "yxhd",
				tags: box.ich.roll_ad_item({adimg: box.getContextPath() + "public/retail/resource/jiangjunad.jpg"})
		};
		box.notify({
			type: "addRollItem",
			data: rollItem
		});
		
		var rollItem = {
				id: "tjfx",
				count: 3,
				tags: box.ich.roll_ad_item({adimg: box.getContextPath() + "public/retail/resource/hongbaxiad.jpg"})
		};
		box.notify({
			type: "addRollItem",
			data: rollItem
		});
		*/
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.INIT_COMPLETE, initComplete);
			box.listen(HOME.Const.TOBACCO_COMPLETE, tobaccoComplete);
		},
		destroy: function() { }
	};
});

HOME.Core.register("plugin-skins", function(box) {

	var skins = $("#skins");
	var skinlist = null;
	var datas = [
	    {title:"1", tag:"beijing1"},
	    {title:"2", tag:"beijing2"},
	    {title:"3", tag:"beijing3"},
	    {title:"4", tag:"beijing4"},
	    {title:"5", tag:"beijing5"},
	    {title:"6", tag:"beijing6"},
	    {title:"7", tag:"beijing7"},
	    {title:"8", tag:"beijing8"},
	    {title:"9", tag:"beijing9"}
	];
	var timeout = -1;
	
	function listener() {
		var item = null;
		for(var i=0; i<datas.length; i++) {
			item = datas[i];
			item.url = box.getContextPath() + "public/retail/themes/bg/" + item.tag + ".jpg";
			//item.small_url = box.getContextPath() + "public/retail/themes/bg-min/" + item.tag;
			item.small_url = "bgIcon bgIcon_" + item.tag;
		}
		skinlist = box.ich.back_skin({list: datas});
		skins.append(skinlist);
		skinlist = skins.find(".back_skin");
		
		skins.hover(function() {
			timeout = setTimeout(function() {
				skinlist.show();
			}, 300);
		}, function() {
			clearTimeout(timeout);
			timeout = -1;
			skinlist.hide();
		});
		
		skinlist.find("li a").click(onThemeClick);
	}
	
	function onThemeClick(e) {
		var $tag = $(e.currentTarget);
		var url = $tag.attr("data-url");
		
		$.setCookie("back_url", url);
		//alert(url);
		$("body").css("background-image", "url('" + url + "')");
		$.checkInShell(function(b) {
			if(b) {
				setTimeout(function() {
					$("body").css("background-image", "url('" + url + "')");
				}, 100);
			}
		});
		//alert("success:" + url);
		//skinlist.hide();
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.INIT_COMPLETE, listener);
		},
		destroy: function() {
			
		}
	};
});
