/**
 * 卷烟订货功能
 */
HOME.Core.register("plugin-jydh", function(box) {
	
	var paramsUrl = "retail/tobacco/getOrderParams";
	var orderUrl = "retail/tobacco/getRecurrentOrder";
	var tobaccoUrl = "retail/tobacco/getTobaccoList";
	//支付接口
	var submitOrderPaymentUrl="retail/tobacco/orderPayment";
	var submitOrderUrl = "retail/tobacco/submitOrder";
	var lmtUrl = "retail/tobacco/getTobaccoLmt";
	var itemLmtUrl = "retail/tobacco/getTobaccoItemLmt";
	var payCard = "retail/tobacco/payOrderByCard";
	var bankCardListUrl = "http://124.133.240.131:9002/payonline/payment/bind/getBindList";//银行卡列表
	var smsValidateUrl = "http://124.133.240.131:9002/payonline/payment/bind/phoneCardMassageForConsumption";//短信验证
	var submitOrderPayUrl= "http://124.133.240.131:9002/payonline/payment/bind/consumerTransaction";//消费接口
	
	var userId = "1037010507633";
	var payBankDialog = null;//选择银行卡界面
	var wait=60;
	
	var parentView = null;
	
	var params = null;
	var order = null;
	var tobacco = null;
	var tobaccoMap = null;
	var selectTobacco = [];
	var showTobacco = null;
	var lmts = null;
	var canUpdateOrder = true;
	
	var tobaccoView = null;
	var selectTable = null;
	
	var orderOption = null;
	var ordertag = 1;
	
	var orderStatus = null;
	var maxQty = null;
	var maxAmt = null;
	var submitButton = null;
	var deleteButton = null;
	var clearOrder = null;
	
	var currentItem = null;
	var currentItemImg = null;
	var currentItemName = null;
	var currentItemId = null;
	var currentItemPri = null;
	var currentItemQty = null;
	var currentItemWhse = null;
	var currentItemWhseWarn = null;
	var currentItemDrtl = null;
	var currentItemSuggest = null;
	var currentItemSubmit = null;
	var currentItemDesc = null;
	var currentitemAd = null;
	var currentItemAdImg=null;
	var currentItemImgs = null;
	var tobaccoListScroll = null;

	var searchInput = null;
	var searchSubmit = null;
	var searchOrderby = null;
	var orderbyList = null;
	var orderbyItems = null;
	var searchLabels = null;
	var oldtxt = "";
	var p1 = 0, p2 = 1000;
	
	var searchPris = null;
	var searchSilder = null;
	var searchPrisLabel = null;
	var pritimeout = -1;
	
	var tobaccoTip = null;
	var tobaccoTipImg = null;
	
	var isfirst = true;
	var timeout = -1;
	var onloadindex = 0;
	var firstimgs = null;
	var focustimeout = -1;
	var maxpri = 900;
	
	var selectRow1 = null;
	var selectRow2 = null;
	
	var snychType=null;
	var newItemIsEmpty = true;
	
	function loadJYDH(view) {
		parentView = view;
		
		/* 20141203 青岛，每次打开卷烟配货页面，都需要下载订货参数，因为实时订货户，订货参数会经常变
		if(box.tobacco.params) {
			params = box.tobacco.params;
		}
		if(params) {
			getOrder();
		} else {
			getParams();
		}
		*/
		getParams();
	}
	
	function getParams() {
		box.request({
			url: box.getContextPath() + paramsUrl,
			success: onGetParams,
			error: onGetParamsError
		});
	}
	
	function onGetParams(data) {
		if(data && data.code == "0000") {
			params = data.result;
			box.tobacco.params = params;
			if(params) {
				getOrder();
			} else {
				box.showAlert({message:"未获取到订货参数"});
			}
		}
	}
	function onGetParamsError() {
		getOrder();
	}
	
	function getOrder() {
		if(params) {
			//日期时间格式化为yyyy-MM-dd hh:mm
			var beginOrderDate=new Date(params.order_begintime.substr(0, 4),params.order_begintime.substr(4, 2)-1,params.order_begintime.substr(6, 2),
					                    params.order_begintime.substr(9, 2),params.order_begintime.substr(12, 2));
			var endOrderDate=new Date(params.order_endtime.substr(0, 4),params.order_endtime.substr(4, 2)-1,params.order_endtime.substr(6, 2),
					                  params.order_endtime.substr(9, 2),params.order_endtime.substr(12, 2));
			params.order_begintime_format=beginOrderDate.format("yyyy-MM-dd hh:mm");
			params.order_endtime_format=endOrderDate.format("yyyy-MM-dd hh:mm");
			
			//params.order_begintime_format = params.order_begintime.substr(0, 14);
			//params.order_endtime_format = params.order_endtime.substr(0, 14);
			parentView.setHeaderTip("订货开始时间：" + params.order_begintime_format + "　订货结束时间：" + params.order_endtime_format + "<br />");
		}
		canUpdateOrder = true;
		box.request({
			url: box.getContextPath() + orderUrl,
			success: onGetOrder,
			error: onGetOrderError
		});
		/*
		if(!order) {
			box.request({
				url: box.getContextPath() + orderUrl,
				success: onGetOrder
			});
		} else {
			if(order.flag == "0"){
				initOrderView();
			}else{
				showOrder();
			}
		}
		*/
	}
	
	function onGetOrderError() {
		canUpdateOrder = false;
		onGetOrder({code: "0000", result: {flag: "0", order_id:"", order_date: ""}});
	}
	
	function onGetOrder(data, so) {
		if(data && data.code == "0000") {
			order = data.result;
			if(!order.status) order.status = "00";
			order.status_label = box.labels.tobacco[order.status];
			order.pmt_status_label = box.labels.tobaccopay[order.pmt_status];
			//////////--------------------------------------------------添加卷烟订货状态为：11
			//STATUS  状态(01:计划, 02:订购, 03:确认, 0301:预确认, 04:发货, 08:停止, 09:完成 )  
			//PMT_STATUS  付款状态(01:挂帐, 02:未付款, 03:收款, 04:划帐, 05:待划账 )  
			

			if(params) {
				//params.order_begintime_format = params.order_begintime.substr(0, 14);
				//params.order_endtime_format = params.order_endtime.substr(0, 14);
				
				//日期时间格式化为yyyy-MM-dd hh:mm
				var beginOrderDate=new Date(params.order_begintime.substr(0, 4),params.order_begintime.substr(4, 2)-1,params.order_begintime.substr(6, 2),
						                    params.order_begintime.substr(9, 2),params.order_begintime.substr(12, 2));
				var endOrderDate=new Date(params.order_endtime.substr(0, 4),params.order_endtime.substr(4, 2)-1,params.order_endtime.substr(6, 2),
						                  params.order_endtime.substr(9, 2),params.order_endtime.substr(12, 2));
				params.order_begintime_format=beginOrderDate.format("yyyy-MM-dd hh:mm");
				params.order_endtime_format=endOrderDate.format("yyyy-MM-dd hh:mm");
				
				var date = null;
				var arrformat = null;
				var next = null;
				var day = order.arr_date;
				if(day) {
					date = new Date(day.substr(0, 4), parseInt(day.substr(4, 2)) - 1, parseInt(day.substr(6, 2)));
					arrformat = date.format("yyyy-MM-dd");
				}
				day = order.next_date;
				if(day) {
					date = new Date(day.substr(0, 4), parseInt(day.substr(4, 2)) - 1, parseInt(day.substr(6, 2)));
					next = date.format("yyyy-MM-dd");
				}
				parentView.setHeaderTip("订货开始时间：" + params.order_begintime_format + "　订货结束时间：" + params.order_endtime_format + "<br />" + (arrformat ? "　预计送货日：" + arrformat : "") + (next ? "　下次订货日：" + next : ""));
			}
			
			if(order.flag == "0"){
				initOrderView();
			}else{
				showOrder();
			}
		}
	}
	
	function initOrderView() {
		var content = box.ich.view_jydh({order: order, params: params});
		parentView.empty().append(content);
		
		selectTable = parentView.find("#select_table");
		tobaccoView = parentView.find("#tobacco_container");
		tobaccoListScroll = parentView.find("#tobacco_list_scroll");
		orderStatus = parentView.find("#orderStatus");
		maxQty = parentView.find("#maxQty");
		maxAmt = parentView.find("#maxAmt");
		submitButton = parentView.find("#submitOrder");
		snychLimit = parentView.find("#synchLimit");
		deleteButton = parentView.find("#deleteOrder");
		clearOrder = parentView.find("#clearOrder");
		
		currentItemImg = parentView.find("#current_item_img");
		currentItemName = parentView.find("#current_item_name");
		currentItemId = parentView.find("#current_item_id");
		currentItemPri = parentView.find("#current_item_pri");
		currentItemQty = parentView.find("#current_item_qty");
		currentItemWhse = parentView.find("#current_item_whse");
		currentItemWhseWarn = parentView.find("#current_item_whse_warn");
		currentItemDrtl = parentView.find("#current_item_drtl");
		currentItemSuggest = parentView.find("#current_item_suggest");
		currentItemSubmit = parentView.find("#current_item_submit");
		currentItemDesc = parentView.find("#current_item_desc");
		currentItemAd = parentView.find("#current_item_ad");
		currentItemAdImg = parentView.find("#current_item_ad_img");
		currentItemImgs = parentView.find("#current_item_imgs");
		
		searchInput = parentView.find("#search_input");
		searchSubmit = parentView.find("#search_submit");
		searchOrderby = parentView.find("#search_orderby");
		orderbyList = parentView.find("#orderby_list");
		orderbyItems = parentView.find("#orderby_list_items");
		searchLabels = parentView.find("#search_labels");
		
		searchPris = parentView.find("#search_pris");
		searchSlider = parentView.find("#search_slider");
		searchPrisLabel = parentView.find("#search_pris_label");
		
		tobaccoTip = parentView.find("#tobacco_tip");
		tobaccoTipImg = parentView.find("#tobacco_tip_img");
		
		
		currentItemSubmit.click(onCurrentItemClick);
		/*currentItemQty.keypress(function(e) {
			if(e.keyCode == 13) {
				//currentItemQty.focusout();
				//$("#hiddenFocus").focus();
				currentItemSubmit.click();
			}
		}).change(function() {
			currentItemSubmit.click();
		});*/
		currentItemImg.hover(showImgTip, hideImgTip);
		
		selectTable.click(onSelectTableClick);
		selectTable.select(onSelectTableClick);
		
		searchSubmit.click(onSearchClick);
		
		/*searchInput.keypress(function(e) {
			if(e.keyCode == 13) {
				//$("#hiddenFocus").focus();
				searchSubmit.click();
			}
		}).change(function() {
			searchSubmit.click();
		});.keyup(function() {
			if(oldtxt != searchInput.val()) {
				showTobacco = getSearchItem(searchInput.text());
				showTobaccoList();
			}
		});*/
		searchLabels.find("a").click(onSearchLabelClick);
		
		orderbyList.hover(function() {
			orderbyItems.show();
		}, function() {
			orderbyItems.hide();
		});
		orderbyItems.find("li").click(onOrderItemClick);
		
		if(!canUpdateOrder || !params || params.is_order == "0"){
			//submitButton.disabled=true;
			//deleteButton.disabled=true;
			submitButton.attr("disabled", "disabled");
			deleteButton.attr("disabled", "disabled");
		}
		if(order.flag == "0") {
			deleteButton.attr("disabled", "disabled");
		}
		
		snychLimit.click(handSnychLimit);
		submitButton.click(onSubmitOrderClick);
		deleteButton.click(onDeleteOrderClick);
		clearOrder.click(onClearOrderClick);

		parentView.showFooter(true);
		
		selectTobacco = [];
		box.request({
			url: box.getContextPath() + tobaccoUrl,
			success: onGetTobaccoList
		});
		/*20141022 取消卷烟商品列表的缓存
		if(!tobacco) {
			box.request({
				url: box.getContextPath() + tobaccoUrl,
				success: onGetTobaccoList
			});
		} else {
			//onTobaccoComplete();
			onGetTobaccoList({code: "0000", result: tobacco});
		}*/
	}
	
	function onGetTobaccoList(data) {
		if(data && data.code == "0000") {
			tobacco = data.result;
			tobaccoMap = {};
			
			isfirst = true;
			
			var len = tobacco.length;
			var len2 = 0, len3 = 0, index = 0;
			if(order.purch_order_line)
				len2 = order.purch_order_line.length;
			if(order.purch_order_line_temp)
				len3 = order.purch_order_line_temp.length;
			var item, item2;
			maxpri = 0;
			for(var i=0; i < len; i++) {
				item = tobacco[i];
				tobaccoMap[item.item_id] = item;
				if(parseFloat(item.pri_wsale) > maxpri) maxpri = parseFloat(item.pri_wsale);
				item.index = i;
				item.img_url = box.getResourcePath() + "images/tobacco/m/" + item.item_id + "_m_face.png";
				if(i > 10) {
					item.reload = box.getContextPath() + "public/img/loading-small.gif";
				} else {
					item.reload = item.img_url;
				}
				item.item_class = new Array();
				if(item.is_new == "1") item.item_class.push({name:"t_xin"});
				if(item.is_advise == "1") item.item_class.push({name:"t_jian"});
				if(item.is_promote == "1") item.item_class.push({name:"t_cu"});
				if(item.is_short == "1") item.item_class.push({name:"t_que"});
				item.topflag = "B";
				if(item.is_new == "1" || item.is_advise == "1" || item.is_promote == "1") {
					item.topflag = "A";
				}
				item.qty_whse = parseFloat(item.qty_whse);
				item.qty_whse_warn = parseInt(item.qty_whse_warn);
				item.qty_suggest = parseInt(item.qty_suggest);
				item.qty_lmt = parseInt(item.qty_lmt);
				item.reqlmt = false;
				
				if(isNaN(item.qty_whse)) item.qty_whse = 0.0;
				if(isNaN(item.qty_whse_warn)) item.qty_whse_warn = 0;
				if(isNaN(item.qty_suggest)) item.qty_suggest = 0;
				
				onloadindex = 10;
				for(var j=0; j < len2; j++) {
					item2 = order.purch_order_line[j];
					if(item.item_id == item2.item_id) {
						item.qty_req = item2.qty_req;
						item.qty_ord = item2.qty_ord;
						item.pri_wsale=item2.pri_wsale;
						item.amt_ord = item2.amt_ord;
						order.purch_order_line[j] = item;
						break;
					}
				}
				for(var j=0; j < len3; j++) {
					item2 = order.purch_order_line_temp[j];
					if(item.item_id == item2.item_id) {
						item.qty_ord = item2.qty_ord;
						item.qty_req = item.qty_ord;
						item.qty_whse = item.qty_whse < 0 ? 0 :item.qty_whse ;
						item.amt_ord = item.pri_wsale * item2.qty_ord;
						order.purch_order_line_temp[j] = item;
						break;
					}
				}
			}
			
			if(order.purch_order_line) {
				selectTobacco = selectTobacco.concat(order.purch_order_line);
			} else if(order.purch_order_line_temp) {
				sortItemList(order.purch_order_line_temp);
				selectTobacco = selectTobacco.concat(order.purch_order_line_temp);
			}
			
			onTobaccoComplete();
			
			/*
			 * 2014-07-02 获取卷烟列表时，包含了限量信息，不需要单独在请求限量了
			 * 张礼现
			 */
			//seltMotionSnychLimit();
            
		}
	}
	
	function onTobaccoComplete() {
		showTobacco = tobacco;
		showTobaccoList();
		
		searchSlider.slider({
			range: true,
			min: 0,
			max: maxpri,
			values: [0, maxpri],
			slide: onPriceSlide
		});
		searchPrisLabel.text("￥0-￥" + maxpri);
		
		fillOrderInfo();
	}
	
	function fillOrderInfo() {
		if(selectTobacco) {
			var item = null;
			for(var j=0; j < selectTobacco.length; j++) {
				item = selectTobacco[j];
				if(!item.pri_wsale) {
					selectTobacco.splice(j, 1);
					j--;
					continue;
				}
				item.num = j + 1;
				
				// 20140731 已经提交的订单再修改时，只要不修改数量，则不根据限量砍单
				item.qty_ord_old = item.qty_ord;
				if(order.flag == "0") {
					item.qty_ord_old = 0;
					if(item.qty_ord > item.qty_lmt) {
						item.qty_ord = item.qty_lmt;
						item.amt_ord = item.qty_ord * parseFloat(item.pri_wsale);
					}
				} else {
					item.reqlmt = true;
				}
			}
			var content = box.ich.view_jydh_tobacco_listitem({list:selectTobacco});
			selectTable.find("tbody").append(content);
		}
		refreshTotalNum();
		
		selectTable.tablesort([
				{col:1,order:"asc",method:"advance",type:"string",attr:"data-sort"},
		        {col:2,order:"asc",method:"advance",type:"number"},
		        {col:3,order:"asc",method:"advance",type:"number"},
		        {col:4,order:"desc",method:"advance",type:"number"},
		        {col:5,order:"desc",method:"advance",type:"number"},
		        {col:6,order:"desc",method:"advance",type:"number"},
		        {col:7,order:"desc",method:"advance",type:"number"},
		        {col:8,order:"desc",method:"advance",type:"number"}
		], 0);
		
		selectTable.fixedtableheader({
			parent: selectTable.parent(),
			win: selectTable.parent(),
			isshow: true
		});
        
	}
	
	function sortItemList(list) {
		var tmp1 = null;
		var changeBoth = function(a, b) {
			tmp1 = list[a];
			list[a] = list[b];
			list[b] = tmp1;
		};
		
		for(var j=0; j < list.length; j++) {
			if(typeof list[j].topflag == 'undefined') {
				list.splice(j, 1);
				j--;
			}
		}
		
		for(var k=0; k < list.length; k++) {
			for(var i=list.length - 1; i > k; i--) {
				if(list[i].topflag < list[i - 1].topflag) {
					changeBoth(i, i - 1);
				} else if(list[i].topflag == list[i - 1].topflag) {
					if(list[i].qty_suggest > list[i-1].qty_suggest) {
						changeBoth(i, i - 1);
					}
				}
			}
		}
	}
	
	function seltMotionSnychLimit(){
		snychType="seltMotion";
		getLmt();
	}
	function handSnychLimit(){
		snychType="hand";
		getLmt();
	}
	
	/**
	 * 获取限量
	 */
	function getLmt() {
		box.request({
			url: box.getContextPath() + lmtUrl,
			data:{snychType:snychType},
			success: onGetLmt,
			//showLoading: false,
			error: function() {
				snychLimit.removeAttr("disabled");
			}
		});
	}
	
	function onGetLmt(data) {
		snychLimit.removeAttr("disabled");
		
		if(data && data.code == "0000") {
			lmts = data.result;
			var obj = {};
			for(var i=0; i<lmts.length; i++) {
				obj[lmts[i].item_id] = parseFloat(lmts[i].qty_lmt);
			}
			var item = null;
			for(var i=0; i<tobacco.length; i++) {
				item = tobacco[i];
				if(typeof obj[item.item_id] != "undefined") {
					item.qty_lmt = obj[item.item_id];
					//item.qty_suggest = Math.min(parseFloat(item.qty_lmt), parseFloat(item.qty_suggest));
				}
			}
			
			var trs = selectTable.find("tbody tr");
			$.each(trs, function(i, tr) {
				var $tr = $(tr);
				var trid = $tr.attr("data-id");
				if(typeof obj[trid] != "undefined") {
					$tr.attr("data-lmt", obj[trid]);

					// 20140731 已经提交的订单再修改时，只要不修改数量，则不根据限量砍单
					var item = getTobaccoItem(trid);
					if(item.qty_ord > obj[trid] && item.qty_ord != item.qty_ord_old) {
						item.qty_ord = obj[trid];
						item.amt_ord = item.qty_ord * parseFloat(item.pri_wsale);
						refreshItemList(item, true, true);
					}
					/*
					if(item.qty_ord > obj[trid] || item.qty_suggest > obj[trid]) {
						var b = (item.qty_ord > obj[trid]) ? true : false;
						if(b) {
							item.qty_ord = obj[trid];
							item.amt_ord = item.qty_ord * parseFloat(item.pri_wsale);
						}
						item.qty_suggest = obj[trid];
						refreshItemList(item, true, b);
					}*/
				}
			});
			
			if(currentItem) {
				currentItemQty.val(currentItem.qty_ord);
			}
			
			if(snychType == "hand") {
				box.showAlert({message:"限量同步完成"});
			}
		}
	}
	
	function showOrder() {
		order.statusfn=function(){
			// 计划状态并且未支付
			if(this.status == "01" && this.pmt_status == "02") return true;
			else return false;
		};
//		order.paystatusfn = function() {
//			if(this.pmt_status == "02") return false;
//			else if(this.pmt_status == "03") return true;
//			else return false;
//		};
		
		order.amt_purch_total=$.parseMoney(order.amt_purch_total);
		var orderList=order.purch_order_line;
		for(var i=0;i<orderList.length;i++){
			orderList[i].pri_wsale_n=function(){
				return $.parseMoney(this.pri_wsale);
			};
		}
		order.purch_order_line=orderList;
		var content = box.ich.view_jydh_order_list(order);
		parentView.empty().append(content);
		
		parentView.find(".table_view").fixedtableheader({
			parent:parentView,
			win: parentView.parent(),
			isshow: true
		});
		//////////--------------------------------------------------添加卷烟订货状态为：11
		if(order.pmt_status == "02" && order.status != "11") {
			content.find("#pay_statusfn_li").css("display", "none");
		}
		else if(order.pmt_status == "03") {
			content.find("#paystatus").text("订单已支付");
			content.find("#pay_statusfn_li").css("display", "block");
		}else if(order.status == "11"){
			content.find("#orderStatus").text("支付中");
			content.find("#paystatus").text("订单支付中");
			content.find("#pay_statusfn_li").css("display", "block");
		}
		else {
			content.find("#pay_statusfn_li").css("display", "none");
		}
		
		parentView.showFooter(true);
		parentView.find("#paySubmit").click(function(){
			var fee = $(this).attr("payFee");
			var orderID = $(this).attr("orderID");
			var orderDate = $(this).attr("orderDate");
			var maxQty = $(this).attr("maxQty");
			//choosePayType(fee,orderID,orderDate,maxQty);
			var order = {fee:fee, order_id:orderID, order_date:orderDate, max_qty:maxQty };
			getBankCardList(order);
		});
		//删除订单
		parentView.find("#deleteOrder").click(onDeleteOrderClick);
		//修改订单
		parentView.find("#editOrder").click(initOrderView);
		if(!params || params.is_order == "0") {
			parentView.find("#editOrder").attr("disabled", "disabled");
			parentView.find("#deleteOrder").attr("disabled", "disabled");
		}
		if((!params || params.is_payment == "0") ||  order.pmt_status != "02") {
			parentView.find("#paySubmit").attr("disabled", "disabled");
		}
	}
	var payTypeDialog = null;
	function choosePayType(fee,orderID,orderDate,maxQty){
		payTypeDialog=box.ich.view_jydh_order_payType({"maxQty":maxQty,"fee":fee});
		box.showDialog({
			title:"选择支付方式",
			width:400,
			height:300,
			content: payTypeDialog,
			close:function(){
				parentView.find("#paySubmit").removeAttr("disabled");
			}
		});
		payTypeDialog.find("#payProxy").click(function(){
			payTypeDialog.find("#payProxy").attr("disabled",true);
			payTypeDialog.find("#payCard").attr("disabled",true);
			orderPayment();
		});
		payTypeDialog.find("#payCard").click(function(){
			payTypeDialog.find("#payProxy").attr("disabled",true);
			payTypeDialog.find("#payCard").attr("disabled",true);
			swipCard(fee,orderID,orderDate);
		});
	}
	/**
	 * 订单支付
	 */
	function swipCard(fee,orderID,orderDate){
		var fee_ = "";
		var flag = true;
		var message = "";
		var payOrder = function(result){
			if(result.flag == "01"){
				message = "刷卡支付取消，请再次选择支付方式";
				flag = false;
			}
			else if(result.flag == "02"){
				message = "刷卡支付失败，请再次选择支付方式";
				flag = false;
			}
			else if(result.flag == "03"){
				message = "未连接POS机";
				flag = false;
			}
			else if(result.flag == "04"){
				message = "POS机未响应,请确认POS机状态";
				flag = false;
			}
			if(!flag){
				box.showAlert({message:message});
				return;
			}
			//box.showAlert({message:"请求到卡号：" + result.primary_account_number});
			box.hideMask();
			var reqData = {
							"field2":result.primary_account_number,
							"field52":result.pin_data,
							"field3":"000000",
							"track_2_data":result.track_2_data,
							"track_3_data":result.track_3_data,
							"fieldMAB":result.fieldMAB,
							"fieldMAC":result.fieldMAC,
							"field41":result.field41,
							"field42":result.field42,
							"fieldIssuerCode":box.tobacco.params.issuer_code,
							"field103":box.tobacco.params.payee_account,
							"field55":result.field55
						   };
			if(!result.fieldMAB ){
				reqData.fieldMAB = "";
			}
			else{
				reqData.fieldMAB = result.fieldMAB;
			}
			if(!result.fieldMAC){
				reqData.fieldMAC = "";
			}
			else{
				reqData.fieldMAC = result.fieldMAC;
			}
			box.request({
				dataType:"json",
				url: box.getContextPath()+payCard,
				data:{params: $.obj2str(reqData)},
				success:function(data){
					payTypeDialog.find("#payProxy").attr("disabled",false);
					payTypeDialog.find("#payCard").attr("disabled",false);
					if(data.code=="0000"){
						if(data.result && data.result.field39 == "00") {
							box.closeDialog({content:payTypeDialog});
							// 支付成功
							box.showAlert({message:"订单支付成功"});
							//parentView.find("#paySubmit").removeAttr("disabled");
							getParams();
							try {
								printTicketForSwipCard(data.result);
							} catch (e) {
								box.showAlert({message:"打印小票出错"});
							}
						} else {
							box.showAlert({message:"支付失败，fieldMessage:"+data.reult.fieldmessage+"，field39:" + data.result.field39});
						}
					} else {
						box.showAlert({message:"支付失败，" + data.msg});
					}
				},
				error: function(data, reqStatus) {
					payTypeDialog.find("#payProxy").attr("disabled",false);
					payTypeDialog.find("#payCard").attr("disabled",false);
					if(data && data.code) {
						box.showAlert({message:"支付失败，" + data.msg});
					} else if(reqStatus){
						box.showAlert({message:"支付失败，请求状态：" + reqStatus});
					} else {
						box.showAlert({message:"支付失败：" + data});
					}
				}
			});
		};
		var detailMSG = "<lable style='color:#ff5828;'>注意事项：</lable><br/>1、请确认POS和电脑正确连接。 <br/>";
		fee_ = fittingPayFee(fee);
		var fieldMAB = {fieldTrancode:"2002222",field3:"460000",field4:fee_,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
		box.showMask({
			message: "请在POS机上刷卡",
			detailMSG:detailMSG,
			hiddenButton:{"hidden":false,val:"重新刷卡","callback":function(){
				box.hideMask();
				swipCard(fee,orderID,orderDate);
				box.showAlert({message:"请重新刷卡"});
			}},
			cancelButton:{"callback":function(){
				box.hideMask();
				payTypeDialog.find("#payProxy").attr("disabled",false);
				payTypeDialog.find("#payCard").attr("disabled",false);
			}},
			feeContent:{"hidden":false,val:fee}
		});
		var hasMac = "0";
		box.com.readCardPwd(hasMac,fee,fieldMAB,payOrder);
	}
	
	/**
	 * 订单支付--协议代扣
	 */
	function orderPayment(){
		box.showConfirm({
			message: "您确定要支付订单吗？支付后将不能修改订单！",
			ok: function() {
				box.request({
					url: box.getContextPath() + submitOrderPaymentUrl,
					success: function(data){
						payTypeDialog.find("#payProxy").attr("disabled",false);
						payTypeDialog.find("#payCard").attr("disabled",false);
						if(data.code=="0000"){
							if(data.result && data.result.field39 == "00") {
								//parentView.find("#paySubmit").removeAttr("disabled");
								box.closeDialog({content:payTypeDialog});
								box.showAlert({message:"订单支付成功"});
							} else {
								box.showAlert({message:"支付失败，field39:" + data.result.field39});
							}
							getParams();
						} else {
							payTypeDialog.find("#payProxy").attr("disabled",false);
							payTypeDialog.find("#payCard").attr("disabled",false);
							box.showAlert({message:"支付失败，" + data.msg});
						}
					},
					error: function(data, reqStatus) {
						payTypeDialog.find("#payProxy").attr("disabled",false);
						payTypeDialog.find("#payCard").attr("disabled",false);
						if(data && data.code) {
							box.showAlert({message:"支付失败，" + data.msg});
						} else if(reqStatus){
							box.showAlert({message:"支付失败，请求状态：" + reqStatus});
						} else {
							box.showAlert({message:"支付失败：" + data});
						}
					}
				});
			},
			cancel: function() {
				payTypeDialog.find("#payProxy").attr("disabled",false);
				payTypeDialog.find("#payCard").attr("disabled",false);
			}
		});
	}
	
	function showTobaccoList() {
		var content = box.ich.view_jydh_tobacco_list({list: showTobacco});
		tobaccoView.empty().append(content);
		
		tobaccoView.find(".tobaccolist li").click(onTobaccoClick);
		tobaccoView.find(".tobaccolist li").select(onTobaccoClick);
		tobaccoView.find(".tobaccolist li img").hover(showImgTip, hideImgTip);
		
		if(showTobacco && showTobacco.length > 0) {
			showItem(showTobacco[0]);
		}
		
		if(isfirst) {
			firstimgs = tobaccoView.find(".tobaccolist li img");
			for(var i=0; i< tobacco.length; i++) {
				tobacco[i].reload = tobacco[i].img_url;
			}
			tobaccoListScroll.unbind("scroll").scroll(onTobaccoListScroll);
			isfirst = false;
		} else {
			firstimgs = null;
		}
		
	}
	
	function onTobaccoListScroll(){//拖动滚动条缓加载图片
		clearTimeout(timeout);
		
		timeout = setTimeout(function() {
			if(firstimgs) {
				var $img = null, position = null;
				var len = firstimgs.length;
				for (var i=0; i<len; i++) {
					
					$img = $(firstimgs[i]);
					position = $img.closest("li").position();

					if(position) {
						if(position.top >= 400) break;
						if(position.top >= -50 && position.top < 400) {
							$img.attr("src", $img.attr("data-src"));
						}
					}
				}
			}
		}, 300);
	}

	
	/**
	 * 缓加载卷烟图片
	*/ 
	function loadTobaccoImg() {
		timeout = setTimeout(function() {
			if(firstimgs != null) {
				var imgitem = null;
				if(firstimgs.length > onloadindex) {
					imgitem = $(firstimgs[onloadindex]);
					imgitem.attr("src", imgitem.attr("data-src"));
					onloadindex++;
					
					loadTobaccoImg();
				} else {
					clearTimeout(timeout);
					timeout = -1;
				}
			} else {
				clearTimeout(timeout);
				timeout = -1;
			}
		}, 100);
	}
	
	function showImgTip(e) {
		var c = $(e.currentTarget);
		var src = c.attr("src");
		if(src && src.indexOf("loading-small.gif") == -1) {
			var p = parentView.closest(".dialog_layer").position();
			tobaccoTipImg.attr("src", src);
			tobaccoTip.css("left", e.clientX - p.left + 10).css("top", e.clientY - p.top);
			tobaccoTip.show();
		}
	}
	function hideImgTip(e) {
		tobaccoTipImg.attr("src", "");
		tobaccoTip.hide();
	}
	
	function onTobaccoClick(e) {
		selectTable.removeAttr("data-onkey");
		tobaccoView.find(".tobaccolist").attr("data-onkey", "true");
		
		var row = $(e.currentTarget);
		var t = getTobaccoItem(row.attr("data-id"));
		if(t)
			showItem(t);
	}
	
	function onSelectTableClick(e) {
		tobaccoView.find(".tobaccolist").removeAttr("data-onkey");
		selectTable.attr("data-onkey", "true");
		
		var $tar = $(e.target);
		if($tar.attr("data-option") == "delete") {
			var t = getTobaccoItem($tar.attr("data-id"));
			if(t)
				removeItem(t);
		} else {
			var row = $(e.target).closest("tr");
			var t = getTobaccoItem(row.attr("data-id"));
			if(t)
				showItem(t);
		}
	}
	
	function removeItem(item) {
		var tr = selectTable.find("tbody tr[data-id=" + item.item_id + "]");
		if(tr.length > 0) {
			var nextid = tr.next().attr("data-id");
			tr.remove();
			for(var i=0; i<selectTobacco.length; i++) {
				if(selectTobacco[i].item_id == item.item_id) {
					selectTobacco.splice(i, 1);
					break;
				}
			}
			$.each(selectTable.find("tbody tr"), function(i, obj) {
				$(obj).find("td:first").text(i + 1);
			});
			
			var t = getTobaccoItem(nextid);
			if(t)
				showItem(t);
			
			refreshTotalNum();
		}
	}
	
	function showItem(item) {
		currentItem = item;
		//item.img= "../public/retail/resource/lingxiuad_banner.jpg";
		if(item.have_img&&item.have_img=="1"){
			item.img=box.getResourcePath()+"images/tobacco/ads/"+item.item_id+".jpg";
		} else {
			item.img = null;
		}
		
		currentItemImg.attr("src", item.img_url);
		currentItemName.text(item.item_name);
		currentItemId.text(item.item_id);
		currentItemPri.text(item.pri_wsale);
		if(item.qty_ord == "0") {
			currentItemQty.val("");
		} else {
			currentItemQty.val(item.qty_ord);
		}
		//currentItemImgs.html(box.ich.view_jydh_tobacco_imgs({path: box.getResourcePath(), item_id: item.item_id}));
		
		currentItemWhse.text(item.qty_whse);
		currentItemWhseWarn.text(item.qty_whse_warn);
		currentItemDrtl.text(item.pri_drtl);
		currentItemSuggest.text(item.qty_suggest);
		
		if(item.promote_desc) {
			currentItemDesc.html("<span class=\"t_cu\" style='display:inline-block; position: relative; top: 5px; margin-right: 5px;'></span><label class=\"cu_text\">"+item.promote_desc+"</label>");
			currentItemDesc.show();
			currentItemAd.hide();
			//currentItemImgs.hide();
		} else {
			currentItemDesc.html("");
			currentItemDesc.hide();
			currentItemAd.show();
			parentView.find("#current_item_ad_img").remove();
			if(item.img) {
				currentItemAd.append("<img id='current_item_ad_img' src='"+item.img+"' style='height: 110px;'/>");
			}
			//currentItemImgs.show();
		}
		
		if(selectRow1) selectRow1.removeClass("selectRow");
		if(selectRow2) selectRow2.removeClass("selectRow");
		
		selectRow1 = tobaccoView.find(".tobaccolist li[data-id="+item.item_id+"]").addClass("selectRow");
		selectRow2 = selectTable.find("tbody tr[data-id="+item.item_id+"]")
			.addClass("selectRow")
			.removeClass("warnRow")
			.attr("title", "");
		var sp = selectRow2.position();
		if(sp) {
			if(sp.top < 0) selectTable.parent().scrollTop(selectTable.parent().scrollTop() + sp.top - 40);
			else if(sp.top > 310) selectTable.parent().scrollTop(sp.top - 40);
		}
		
		if(focustimeout > 0)
			clearTimeout(focustimeout);
			focustimeout = setTimeout(function() {
			currentItemQty.focus();
			currentItemQty.select();
		}, 200);
	}
	
	function onCurrentItemClick() {
		newItemIsEmpty = false;
		var qty = parseFloat(currentItemQty.val());
		if(isNaN(qty)) qty = 0;
		var req = qty;
		
		box.request({
			url: box.getContextPath() + itemLmtUrl,
			data:{params: $.obj2str({item_list: [currentItem.item_id]})},
			success: function(data) {
				var lmt = data.result[0];
				if(lmt && lmt.item_id) {
					if(lmt.item_id == currentItem.item_id) {
						currentItem.reqlmt = true;
						currentItem.qty_lmt = lmt.qty_lmt;
						checkSetItemQty(req, qty);
					}
				} else {
					box.showAlert({message: "未获取到商品限量"});
				}
			}
		});
	}
	
	function checkSetItemQty(req, qty) {
		// 20140731 已经提交的订单再修改时，只要不修改数量，则不根据限量砍单
		if(qty != currentItem.qty_ord_old && qty > currentItem.qty_lmt) {
			if(currentItem.qty_ord_old > currentItem.qty_lmt) {
				box.showConfirm({
					message: currentItem.item_name + "限量已调低，修改订购量将减少订购，是否继续修改？",
					ok: function() {
						qty = currentItem.qty_lmt;
						if(qty <= 0) {
							box.showAlert({message:currentItem.item_name + "的限量为0"});
						} else {
							box.showAlert({message:currentItem.item_name + "超出限量，自动调整为限量：" + qty});
						}
						setItemQty(req, qty);
					}
				});
			} else {
				qty = currentItem.qty_lmt;
				if(qty <= 0) {
					box.showAlert({message:currentItem.item_name + "的限量为0"});
				} else {
					box.showAlert({message:currentItem.item_name + "超出限量，自动调整为限量：" + qty});
				}
				setItemQty(req, qty);
			}
		} else {
			setItemQty(req, qty);
		}
	}
	
	function setItemQty(req, qty) {
		currentItem.qty_req = req;
		currentItem.qty_ord = qty;
		currentItem.amt_ord = currentItem.qty_ord * parseFloat(currentItem.pri_wsale);
		currentItem.qty_whse = currentItem.qty_whse < 0 ? 0 :currentItem.qty_whse ;
		refreshItemList(currentItem);
		
		// 显示下一条
		var next = selectTable.find("tbody tr[data-id="+currentItem.item_id+"]").next();
		var t = getTobaccoItem(next.attr("data-id"));
		if(!t) {
			next = tobaccoView.find(".tobaccolist li[data-id="+currentItem.item_id+"]").next();
			t = getTobaccoItem(next.attr("data-id"));
		}
		
		if(t)
			showItem(t);
	}
	
	function onSearchClick() {
		var txt = searchInput.val();
		showTobacco = getSearchItem(txt);
		if(orderOption && showTobacco && showTobacco.length > 0) {
			$.sort(showTobacco, 0, showTobacco.length-1, orderOption);
		}
		showTobaccoList();
	}
	
	function onSearchLabelClick(e) {
		var target = $(e.currentTarget);
		var val = target.attr("data-value");
		searchInput.val("");
		if(val == "is_all") {
			showTobacco = tobacco;
			searchSlider.slider({
				range: true,
				min: 0,
				max: maxpri,
				values: [0, maxpri],
				slide: onPriceSlide
			});
			searchPrisLabel.text("￥0-￥" + maxpri);
			
			searchLabels.find("a").removeClass("labelselected").first().addClass("labelselected");
			oldtxt = "";
			p1 = 0;
			p2 = 1000;
			
		} else {
			searchInput.val("");
			showTobacco = getSearchItem(val);
		}
		
		if(orderOption && showTobacco && showTobacco.length > 0) {
			$.sort(showTobacco, 0, showTobacco.length-1, orderOption);
		}
		showTobaccoList();
	}
	
	function onPriceSlide(e, ui) {
		searchPrisLabel.text("￥" + ui.values[0] + "-￥" + ui.values[1]);
		if(pritimeout > 0) {
			clearTimeout(pritimeout);
		}
		pritimeout = setTimeout(function() {
			pritimeout = -1;
			
			p1 = ui.values[0];
			p2 = ui.values[1];
			
			showTobacco = getSearchItem(oldtxt);
			if(orderOption && showTobacco && showTobacco.length > 0) {
				$.sort(showTobacco, 0, showTobacco.length-1, orderOption);
			}
			showTobaccoList();
		}, 300);
	}
	
	/**
	 * 搜索卷烟
	 */
	function getSearchItem(txt) {
		
		if(!tobacco || tobacco.length == 0) return;
		
		oldtxt = txt;
		//if(!txt)
		//	return tobacco;
		
		var arr = new Array();
		var len = tobacco.length;
		var item = null;
		var isattr = false;
		if(txt == "is_new" || txt == "is_advise" 
			|| txt == "is_promote" || txt == "is_short") {
			isattr = true;
			searchLabels.find("a").removeClass("labelselected");
			searchLabels.find("a[data-value=" + txt + "]").addClass("labelselected");
		} else {
			txt = txt.toUpperCase();
		}
		
		for(var i=0; i<len; i++) {
			item = tobacco[i];
			if(!item) continue;

			if(item.pri_wsale >= p1 && item.pri_wsale <= p2) {
				if(!txt) {
					arr.push(item);
					continue;
				}
				if(isattr) {
					if(item[txt] == "1")
						arr.push(item);
					else
						continue;
				} else if(item.item_name.indexOf(txt) != -1
					|| item.short_name.indexOf(txt) != -1
					|| item.fact_name.indexOf(txt) != -1
					|| item.brand_name.indexOf(txt) != -1) {
					arr.push(item);
				} else if(txt.length > 4 && (item.item_id.indexOf(txt) != -1 
						|| item.short_code.indexOf(txt) != -1
						|| item.item_bar.indexOf(txt) != -1
						|| item.big_bar.indexOf(txt) != -1)) {
					arr.push(item);
				} else if(txt.length <= 4 && item.pri_wsale == parseFloat(txt)) {
					arr.push(item);
				}
			}
		}
		return arr;
	}
	
	/**
	 * 排序
	 */
	function onOrderItemClick(e) {
		$(this).siblings().css("background","");
		$(this).css("background","#C5DAFF");
		var t = $(e.currentTarget);
		var col = t.attr("data-order");
		
		if(orderOption != null && orderOption.attr == col) {
			ordertag = -1*ordertag;
		}
		var o = {attr: col, type: "string", tag: ordertag};
		if(col == "index" || col == "pri_wsale")
			o.type = "number";
		
		orderOption = o;
		
		orderbyItems.hide();
		
		if(showTobacco && showTobacco.length > 0) {
			$.sort(showTobacco, 0, showTobacco.length-1, orderOption);
			showTobaccoList();
		}
	}
	
	function refreshItemList(item, isappend, addwarn) {
		
		if(!item) return;
		
		var tr = selectTable.find("tbody tr[data-id=" + item.item_id + "]");
		if(addwarn) {
			tr.addClass("warnRow");
			tr.attr("title", "您输入的数量超出限量，自动调整为最大量");
		} else {
			tr.removeClass("warnRow");
			tr.attr("title", "");
		}
		item.pri_wsale_n=function(){
			return $.parseMoney(item.pri_wsale);
		};
		item.amt_ord=item.amt_ord;
		//item.pri_drtl=$.parseMoney(parseFloat(item.pri_drtl));
		//if(item.qty_ord > 0 || isappend) {
			if(tr.length == 0) {
				selectTobacco.push(item);
				item.num = selectTobacco.length;
				var view = box.ich.view_jydh_tobacco_listitem({list: [item]});
				selectTable.find("tbody").append(view);
				
				selectTable.parent().scrollTop(9999);
			} else {
				tr.find(".qtyord").text(item.qty_ord);
				tr.find(".amtord").text(item.amt_ord);
				tr.find(".qtysuggest").text(item.qty_suggest);
				tr.find(".qtylmt").text(item.qty_lmt);
				tr.find(".qtyreq").text(item.qty_req);
				tr.attr("data-req", item.qty_req);
			}
		//} else {
		//	tr.remove();
		//}
		
		refreshTotalNum();
	}
	
	function getTobaccoItem(id) {
		var len = tobacco.length;
		for(var i=0; i<len; i++) {
			if(tobacco[i] && tobacco[i].item_id == id) {
				return tobacco[i];
			}
		}
		return null;
	}
	
	function refreshTotalNum() {
		var maxqty = 0;
		var maxamt = 0;
		$.each(selectTable.find("tbody tr"), function(i, tr) {
			$tr = $(tr);
			var qty = parseFloat($tr.find(".qtyord").text());
			var pri = parseFloat($tr.attr("data-pri"));
			if(qty > 0) {
				maxqty += qty;
				maxamt += qty * pri;
			}
		});
		
		maxQty.text(maxqty);
		maxAmt.text($.parseMoney(maxamt));
	}
	
	function onSubmitOrderClick(e) {
		/*
		if(!lmts) {
			box.showAlert({message:"尚未获取到卷烟限量，请稍后提交订单"});
			return;
		}
		*/
		if(!params) {
			box.showAlert({message: "未获取到订货参数"});
			submitButton.removeAttr("disabled");
			box.stopEvent(e);
			return;
		}
		var notreqlmt = new Array();
		$.each(selectTable.find("tbody tr"), function(i, obj) {
			$tr = $(obj);
			var qty = parseFloat($tr.find(".qtyord").text());
			var itemId = $tr.attr("data-id");
			if(qty > 0) {
				if(!tobaccoMap[itemId].reqlmt) {
					notreqlmt.push(itemId);
				}
			}
		});
		
		if(notreqlmt.length > 0) {
			// 未请求限量的商品，需要请求一次限量
			box.request({
				url: box.getContextPath() + itemLmtUrl,
				data:{params: $.obj2str({item_list: notreqlmt})},
				success: function(data) {
					if(data.result) {
						var item = null;
						var itemarr = new Array();
						$.each(data.result, function(i, obj) {
							item = tobaccoMap[obj.item_id];
							item.reqlmt = true;
							item.qty_lmt = parseFloat(obj.qty_lmt);
							
							if(item.qty_ord > item.qty_lmt) {
								// 超限量
								item.qty_ord = item.qty_lmt;
								item.amt_ord = item.qty_ord * parseFloat(item.pri_wsale);
								item.qty_whse = item.qty_whse < 0 ? 0 :item.qty_whse;
								itemarr.push(item.item_name);
								
								refreshItemList(item);
							}
						});
						
						if(itemarr.length == 0) {
							onSubmitOrder(e);
						} else {
							var msg = itemarr.join(",") + "的限量已调低，当前订购数量：<span style='color: red; font-size: 18px;'>" 
									+ maxQty.text() + "</span>条，订购金额：<span style='color: red; font-size: 18px;'>" + maxAmt.text() + "</span>元，是否继续提交？";
							
							box.showConfirm({
								message: msg,
								ok: function() {
									onSubmitOrder(e);
								}, 
								cancel: function() {
									submitButton.removeAttr("disabled");
								}
							});
						}
					} else {
						box.showAlert({message: "未获取到部分商品的限量"});
						submitButton.removeAttr("disabled");
					}
				},
				error: function() {
					submitButton.removeAttr("disabled");
				}
			});
		} else {
			onSubmitOrder(e);
		}
	}
	
	function onSubmitOrder(e) {
		var list = new Array();
		var max = 0;
		$.each(selectTable.find("tbody tr"), function(i, obj) {
			$tr = $(obj);
			var qty = parseFloat($tr.find(".qtyord").text());
			var req = parseFloat($tr.attr("data-req"));
			var itemId = $tr.attr("data-id");
			if(qty > 0 || req > 0) {
				list.push({item_id: itemId, qty_req: req, qty_ord: qty});
			}
			max += qty;
		});
		
		if(max > parseFloat(params.order_limit)) {
			box.showAlert({message:"所选数量超出总限量(" + params.order_limit + "条)，请调整您的订单"});
			submitButton.removeAttr("disabled");
			box.stopEvent(e);
			return;
		}
		if(max < parseFloat(params.order_min_qty)) {
			box.showAlert({message:"所选数量低于最小限量(" + params.order_min_qty + "条)，请调整您的订单"});
			submitButton.removeAttr("disabled");
			box.stopEvent(e);
			return;
		}
		
		var obj = {flag: order.flag, order_id: order.order_id, order_date: order.order_date};
		if(order.order_id) {
			obj.flag = "1";
		} else {
			obj.flag = "0";
		}
		obj.list = list;
		
		if(list.length > 0) {
			box.request({
				url: box.getContextPath() + submitOrderUrl,
				data: {params: $.obj2str(obj)},
				success: function(thisData){
					if(thisData && thisData.code == "0000") {
						thisOrder = thisData.result;
						box.console.log("提交订单：code=" + thisData.code + ", msg=" + thisOrder.submitmsg);
						if(thisOrder.flag == "0"){
							box.showAlert({message: "订单提交失败，请稍后重试！"});
						}else{
							var day = params.order_begintime;
							var date = new Date(day.substr(0, 4), parseInt(day.substr(4, 2)) - 1, parseInt(day.substr(6, 2)) + 5, day.substr(9, 2), day.substr(12, 2));
							var nextstart = date.format("yyyy-MM-dd hh:mm");
							day = params.order_endtime;
							date = new Date(day.substr(0, 4), parseInt(day.substr(4, 2)) - 1, parseInt(day.substr(6, 2)) + 5, day.substr(9, 2), day.substr(12, 2));
							var nextend = date.format("yyyy-MM-dd hh:mm");
							
							var msg = "订单提交成功！";
							if(thisOrder.submitmsg) {
								msg = thisOrder.submitmsg;
							}
							box.showAlert({message: msg + "<br />您的下次订货时间是：<br />开始：" + nextstart + "<br />结束：" + nextend, time: 8000});
							
							newItemIsEmpty = true;
						}
					}else{
						box.showAlert({message: "订单提交失败，请稍后重试！"});
					}
					
					submitButton.removeAttr("disabled");
					
					// 提交订单之后用来显示
					onGetOrder(thisData);
				},
				error: function() {
					submitButton.removeAttr("disabled");
				}
			});
		} else {
			submitButton.removeAttr("disabled");
			box.showAlert({message: "没有选择商品，请先选择商品"});
			box.stopEvent(e);
		}
	}
	
	function onDeleteOrderClick(e) {
		box.showConfirm({
			message: "您确定要删除本周期的订单吗？",
			ok: function() {
				var obj = {flag: 2, order_id: order.order_id, order_date: order.order_date, list: []};
				
				box.request({
					url: box.getContextPath() + submitOrderUrl,
					data: {params: $.obj2str(obj)},
					success: onDeleteOrderComplete
				});
			}
		});
	}
	
	function onClearOrderClick(e) {
		box.showConfirm({
			message: "您确定要清空此订单吗？清空后需重新选择",
			ok: function() {
				var list = selectTable.find("tbody tr");
				$.each(list, function(i, obj){
					var itemid = $(obj).attr("data-id");
					var item = getTobaccoItem(itemid);
					if(item) {
						item.qty_ord = 0;
						item.amt_ord = 0;
					}
				});
				list.remove();
				
				selectTobacco = [];
				
				refreshTotalNum();
			}
		});
	}
	
	function onDeleteOrderComplete(data) {
		box.showAlert({message: "订单删除成功"});
		onGetOrder(data, "showOrder");
	}
	
	function onEditOrder() {
		getTobaccoList();
	}
	
	function onBarcode(data) {
		p1 = 0;
		p2 = 1000;
		var arr = getSearchItem(data.code);
		if(arr && arr[0]) {
			showItem(arr[0]);
		}
	}
	function onClose(param) {
		if(!newItemIsEmpty){
			newItemIsEmpty = true;
			box.showConfirm({
				message: "订货数量已修改，您是否关闭？",
				ok: function(){
					if(timeout != -1)
						clearTimeout(timeout);
					param.callback();
				}
			});
		}else{
			if(timeout != -1)
				clearTimeout(timeout);
			return true;
		}
		return false;
	}
	
	function onkey(data) {
		switch(data.key) {
			case HOME.Keys.DELETE:
				var tr = selectTable.find("tr.selectRow");
				var t = getTobaccoItem(tr.attr("data-id"));
				if(t)
					removeItem(t);
				break;
			case HOME.Keys.BARCODE:
				onBarcode(data);
				break;
		}
	}
	
	function printTicketForSwipCard(data){
		var field2 = data.field2.substring(0,6)+"********"+data.field2.substring(data.field2.length-4,data.field2.length);
		var field103 = data.field103.substring(0,6)+"********"+data.field103.substring(data.field103.length-4,data.field103.length);
		var field4 = parseFloat(data.field4)/100;
		var field7 = data.field7;
		var orderDate=new Date(field7.substr(0, 4),field7.substr(4, 2)-1,field7.substr(6, 2),
				field7.substr(8, 2),field7.substr(10, 2),field7.substr(12, 2));
		var field7  = orderDate.format("yyyy-MM-dd hh:mm:ss");
		var content  = "         卷烟订单支付\n\n";
			content += "--------------------------------\n";
			content += "零售名称:"+box.user.user_name +"\n";
			content += "商户编号:"+data.field42+"\n";
			content += "终端编号:"+data.field41+"\n";
			content += "付款卡号:"+field2+"\n";
			content += "付款账号开户行:\n"+data.fieldinbankname+"\n";
			content += "收款账号:"+field103+"\n";
			content += "收款账号开户行:\n"+data.fieldoutbankname+"\n";
			content += "交易类型:山东烟草资金归集\n";
			//content += "凭证号:"+data.field11+"\n";  返回的为固定字段 取消小时
			content += "参考号:"+data.field37+"\n";
			content += "交易时间:"+field7+"\n";
			content += "交易金额:"+field4+" 元\n";
			content += "备注:\n\n";
		try {
			box.com.printTicket(content,2);
		} catch (e) {
		}
	}
	
	function fittingPayFee(payMoney){
		$("#payPhoneButton").attr('disabled',false);
		if(payMoney.split(".").length > 1){
			var m = payMoney.split(".");
			var yuan = m[0];
			var jiao = m[1];
			if(jiao.length > 2){
				jiao = jiao.substring(0,2);
			}
			else{
				if(jiao.length==1){
					jiao = jiao+"0";
				}
			}
			var l = yuan.length;
			for(var i = 0 ; i < 10 - l ; i++){
				yuan = "0"+yuan;
			}
			payMoney = yuan+jiao;
		}
		else{
			var l = payMoney.length;
			for(var i = 0 ; i < 10-l; i++ ){
				payMoney = "0"+payMoney;
			}
			payMoney = payMoney+"00";
		}
		return payMoney;
	}
	
	//////////////////////////////////
	
	//获得银行卡列表
	function getBankCardList(orderInfo){
		var params = {userId:userId};
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: bankCardListUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				showBankCardList(data, orderInfo);
			},
			error:function(data){
				showBankCardList(null);
			}
		});
	}
	
	//展示银行卡列表
	function showBankCardList(data, orderInfo){
	    wait = 0;
		var bankCardList = [];
		if (data && data.code == "0000") {
			bankCardList = data.result;
		}
		if (bankCardList.length < 1) {
			box.showAlert({message: "对不起，您没有绑定的银行卡！"});
		}
		for ( var i = 0; i < bankCardList.length; i++) {
			bankCardList[i].bank_imp = box.getBankImage(bankCardList[i].issinscode, "s", "face");
			
			var cardId = bankCardList[i].accno;
			bankCardList[i].newaccno =cardId.substring(0, 6) +" ******* "+ cardId.substring(cardId.length-6 ,cardId.length);
			
			var phoneno = bankCardList[i].phoneno;
			bankCardList[i].newphoneno =phoneno.substring(0, 3) +" **** "+ phoneno.substring(phoneno.length-4 ,phoneno.length);
			
			var binddate = bankCardList[i].binddate;
			var newBinddate=new Date(binddate.substr(0, 4), binddate.substr(4, 2)-1, binddate.substr(6, 2));
			data.result[i].new_bind_date = newBinddate.format("yyyy-MM-dd");
			if(bankCardList[i].paycardtype == '01'){
				bankCardList[i].paycard_type_name = "借记卡";
			}else{
				bankCardList[i].paycard_type_name = "信用卡";
			} 
		}
		
		payBankDialog=box.ich.view_jydh_order_pay_bank({bank_card_list:bankCardList, order_info:orderInfo});
		box.showDialog({
			title:"选择绑定银行卡",
			width:710,
			height:480,
			content: payBankDialog,
			close:function(){
				parentView.find("#paySubmit").removeAttr("disabled");
			}
		});
		payBankDialog.find("#send_sms_code_btn").unbind("click").click(sendVerificationCode(orderInfo));
		payBankDialog.find("#submit_pay").unbind("click").click(submitOrderPay(orderInfo));
		payBankDialog.find(".bank-block").unbind("click").click(chekedBankCardDiv);
		
	}
	
	//单击银行的时候
	function chekedBankCardDiv(e){
		var target = $(e.target);
		var chekDiv = $(target).closest(".bank-block");
		payBankDialog.find(".bank-block").css("background-color", ""); 
		var chekRadio = chekDiv.find("input[name='user_bank_card_radio']");
		chekRadio.prop("checked","checked");
		
		var bankCardInfo = payBankDialog.find("input[name='user_bank_card_radio']:checked");
		if (bankCardInfo.length > 0) {
			$(bankCardInfo).closest(".bank-block").css("background-color", "#C6E2FF"); 
			chekDiv.css("background-color", "#C6E2FF"); 
			payBankDialog.find("#content_div").css("bottom", "105px");
			payBankDialog.find("#sms_div").css("display", "block");
		}
	}
	
	//发送短息验证码
	function sendVerificationCode(orderInfo){
		return function(){
			var bankCardInfo = payBankDialog.find("input[name='user_bank_card_radio']:checked");
			if (bankCardInfo.length<=0) {
				box.showAlert({message: "请选择支付银行卡！"});
				return ;
			}
			
			var userBindBankCard = $(bankCardInfo).closest(".bank-block");
			var bankId = $(userBindBankCard).attr("data-bank-id");//卡号
			var orderAmount = $.parseMoney(orderInfo.fee);
			var orderId = orderInfo.order_id;
			
			//订单金额要求以分为单位，订单金额  * 100
			var params={accNo:bankId, userId:userId, txnAmt:parseFloat(orderAmount)*100, orderId:orderId };
			box.request({
				type : "post",
				dataType : 'jsonp',
				url: smsValidateUrl,
				data:{params:$.obj2str (params)},
				success:function(data) {
					time();
					userSmsCodeResult = data;
					////////////////////////////////////------------------------------------------需要删除
					payBankDialog.find("#sms_code").val(data.pageparams);
					///////////////////////////////////userSmsCodeResult.pageparams;
				},
				error:function(data){
					userSmsCodeResult = null;
					box.showAlert({massger : "短信验证失败: "+data.msg });
				}
			});
			return false;
		};
	}
	
	
	function time(o) {
		var sendSmsCodeBtn = payBankDialog.find("#send_sms_code_btn");
        if (wait == 0) {
        	sendSmsCodeBtn.attr("disabled",false);
            sendSmsCodeBtn.val("获取验证码");
            wait = 60;
        } else { // www.jbxue.com
        	sendSmsCodeBtn.attr("disabled",true);
            sendSmsCodeBtn.val("重新发送(" + wait + ")");
            wait--;
            setTimeout(function() {
                time(sendSmsCodeBtn);
            },
            1000);
        }
    }
	
	
	//支付订单
	function submitOrderPay(orderInfo){
		return function(){
			var bankCardInfo = payBankDialog.find("input[name='user_bank_card_radio']:checked");
			if (bankCardInfo.length<=0) {
				box.showAlert({message:"请选择支付银行卡！"});
				return ;
			}
			
			var userBindBankCard = $(bankCardInfo).closest(".bank-block");
			var bankId = $(userBindBankCard).attr("data-bank-id");//卡号
			var orderAmount = $.parseMoney(orderInfo.fee);
			var orderId = orderInfo.order_id;
			var smsCode = payBankDialog.find("#sms_code").val();
			if( !smsCode ){
				box.showAlert({message: "请输入短信验证码！"});
				return ;
			}
			
			//金额精确到分，所以订单金额 * 100
			var params = {userId:userId, accNo:bankId, txnAmt:parseFloat(orderAmount)*100, orderId:orderId, vfCode:smsCode};
			
			box.request({
				type : "post",
				dataType : 'jsonp',
				url: submitOrderPayUrl,
				data:{params:$.obj2str (params)},
				success: function(data){
					if(data.code=="0000"){
//						if(data.result && data.result.field39 == "00") {
						if(data.result ) {
							box.showAlert({message:"订单支付成功"});
						} else {
							box.showAlert({message:"对不起，支付失败！" });
						}
						getParams();
					} else {
						box.showAlert({message:"支付失败，" + data.msg});
					}
				},
				error: function(data, reqStatus) {
					if(data && data.code) {
						box.showAlert({message:"支付失败，" + data.msg});
					} else if(reqStatus){
						box.showAlert({message:"支付失败，请求状态：" + reqStatus});
					} else {
						box.showAlert({message:"支付失败：" + data});
					}
				}
			});
			
			box.closeDialog({content:payBankDialog});
			$("#operation_order_div").css("display","none");
			$("#orderStatus").text("支付中");
			$("#paystatus").text("订单支付中");
			
			$("#pay_statusfn_li").css("display", "block");
			
			return false;
		};
	}
	
	
	
	
	
	return {
		init: function(){
			box.listen("jydh", loadJYDH);
			box.listen("jydh_onkey", onkey);
			box.listen("jydh_close", onClose);
		},
		destroy: function() { }
	};
});
