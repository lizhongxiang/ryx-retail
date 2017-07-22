/**
 * 库存管理功能
 */
HOME.Core.register("plugin-axss", function(box) {
	
	var parentView = null;
	
	var sendtxt = null;
	var submit=null;
	//var sendbutton = null;
	//var full = null;
	//var win = null;
	//var showwin = null;
	var auditing=null;
	var insertUmbrellaUserUrl = "retail/loveForUmbrella/insertUmbrellaUser";
	var selectUmbrellaUserURL="retail/loveForUmbrella/getUmbrellaUser";
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_axss();
		parentView.empty().append(content);
		
		//var subtypevalue = $.getCookie("subtype");
		submit = parentView.find("#submit");
		auditing = parentView.find("#auditing");
		
		selectUmbrellaUserInfo();
		submit.unbind("click").click(submitUmbrellaUser);
		/*if(subtypevalue != ""){
			submit.hide();
 			auditing.show();
		}else{
			submit.click(function(){
				box.showAlert({message:"申请成功！"});
	 			submit.hide();
	 			auditing.show();
	 			
	 			$.setCookie("subtype","true");
			});
		}*/
		
		
		/*
		showwin = parentView.find("#open_button");
		sendtxt = parentView.find("#send_text");
		sendbutton = parentView.find("#send_button");

		full = parentView.find("#fullscreen_button");
		win = parentView.find("#window_button");
		
		sendbutton.click(onSendMessage);
		full.click(function() {
			box.comm.send({type: "fullscreen"});
		});
		win.click(function() {
			box.comm.send({type: "window"});
		});
		
		showwin.click(function() {
			window.open(box.getContextPath() + "public/retail/comm/CommClient.html","newwindow",'height=500,width=800,top=0,left=0,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no');
		});*/
	}
	
	//爱心借伞服务活动
	function submitUmbrellaUser(){
		box.request({
			url : box.getContextPath() + insertUmbrellaUserUrl,
			success : function(json) {
				if(json && json.code=="0000") {
					submit.hide();
					auditing.show();
					selectUmbrellaUserInfo();
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
	}
	function selectUmbrellaUserInfo(){
		
		box.request({
			url : box.getContextPath() + selectUmbrellaUserURL,
			success : function(json) {
				if(json && json.code=="0000") {
					var itemList=json.result;
					if(itemList.length>0) {
						if(itemList[0].status=="01"){
							auditing.val("等待审核");
							auditing.show();
							submit.hide();
							auditing.attr("disabled","disabled");
						}else if(itemList[0].status=="02"){
							auditing.val("等待送伞");
							auditing.show();
							submit.hide();
							auditing.attr("disabled","disabled");
						}else if(itemList[0].status=="03"){
							auditing.val("已完成");
							auditing.show();
							submit.hide();
							auditing.attr("disabled","disabled");
						}else{
							submit.show();
							auditing.hide();
						}
					}else{
						submit.show();
						auditing.hide();
					}
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
	}
	

	//未调用
	/*
	function onSendMessage() {
		var txt = sendtxt.val();
		box.comm.send({type: "showmessage", message: txt});
	}
	*/
	
	
	return {
		init: function(){
			box.listen("axss", onload);
		},
		destroy: function() { }
	};
});