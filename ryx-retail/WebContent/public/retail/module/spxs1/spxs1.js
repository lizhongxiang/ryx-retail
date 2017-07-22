/**
 * 商品销售功能
 */
HOME.Core.register("plugin-spxs1", function(box) {
	// 接口地址
	var itemDetailUrl = "retail/item/searchMerchItemWithIncrease";
	var searchMerchPromotionUrl = "retail/promotion/searchMerchPromotionForSale";//请求促销信息
	var submitSaleOrderUrl = "retail/order/submitPCSaleOrder";//现金结算
	var submiCardtSaleOrderUrl="retail/order/submitPCCardSaleOrder";// "刷卡结算"按钮触发的请求
	var submitHangingSaleOrderUrl="retail/order/submitPCHangingSaleOrder";// 挂账
	var submitSaleOrderWithCardUrl = "retail/order/submitPCSaleOrderWithCard";
	var cashPaySaleOrderUrl = "retail/order/cashPaySaleOrder"; // 刷卡销售之后转现金支付, 传order_id
	var completeCardPayUrl = "retail/order/completeCardPay"; // 刷卡销售之后完成支付, 传order_id
//	var searchMerchConsumer = "retail/consumer/getMerchConsumer";
	var searchMerchConsumer = "retail/consumer/searchMerchConsumerInfo";//会员
//	var searchMerchConsumer = "retail/consumer/searchMerchConsumerInfo"; // 提交14
	var exitUrl = "user/logout";//退出登录
	var allsubmitSaleOrderUrl = "retail/basedata/dutyShift";//交班结算
	var addsubmitSaleOrderUrl = "retail/basedata/insertDutyShift";//插入交班结算
	// 全局数据
	var staticnum = 1;
	var currentItem = null;
	var total = null; //quantity商品总量, amount商品总额, amt_ss实收金额
	var currentConsumer = {};
	var proInf=null;  //促销信息列表
	var proInfMap = {};  //促销信息列表
	var orderInfoCloseTimeout = -1;
	var orderInfo = null;
	var promotionItem = null;
	var consumerPoint = null;
	// 页面元素
	var parentView = null;
	var submitDialog = null;
	var selectItemDialog = null;
	var consumerContainer = null;
	var selectAllSubmitSaleOrder = null;//交班弹出框
	var consumerInput = null;
	var itemBarInput = null;
	var choiceItemButton = null;
	var currentItemName = null;
	var currentItemPri = null;
	var currentItemSalePri = null;
	var currentItemQty = null;
	var currentItemUnit = null;
	var currentItemDiscount = null;
	var currentItemAmt = null;
	var table = null;
	var tableBody = null;
	var orderInfoDialog= null;
	var cardConfirmDialog = null;
	var maxQty = null;
	var maxAmt = null;
	var orderSubmitButton = null;
	var clearButton = null;
	var allOrderSubmitButton = null;//交班结算
	var actualInput = null;
	var cashSubmitButton = null;
	var cardSubmitButton = null;
	var keXianPinObj = {};//客显屏数据
	keXianPinObj.list = [];//客显屏单行数据
	
	function logMe(me) {
		box.console.log(me);
	}
	
	function alertMe(me) {
		box.showAlert({message: me});
	}
	
	function prepareParamAndRequestData(url, paramObject, callback, errorback) {
		var param = {
			url: box.getContextPath() + url,
			data: {params: $.obj2str(paramObject)},
			success: function(data) {
				if(data && data.code=='0000') {
					callback(data.result, null);
				} else {
					logMe(data);
				}
			},
			error: function(data) {
				logMe(data);
			}
		};
		if(errorback) param.error = errorback;
		box.request(param);
	}
	
	function loadSPXS(view) {
		parentView = view;
		currentItem = {};
		currentConsumer = {};
		keXianPinObj.consumer = "";
		keXianPinObj = {};//
		keXianPinObj.list = [];
		total = {quantity: 0, amount: 0, count: 0, oriAmount:0};
		
		parentView.empty().append(box.ich.view_spxs1());
		
		parentView.find("#create_date_text").datepicker();

		itemBarInput = parentView.find("#input_item_bar");
		choiceItemButton = parentView.find("#choice_item");
		
		consumerContainer = parentView.find("#consumer_container");
		resetConsumer();
		
		currentItemName = parentView.find("#current_item_name");
		currentItemPri = parentView.find("#current_item_pri");
		currentItemSalePri = parentView.find("#current_item_salepri");
		currentUnitPrice = parentView.find("#current_unitPrice");
		currentUnitSalePrice = parentView.find("#current_unitSalePrice");
		currentItemUnit = parentView.find("#current_item_unit");
		currentItemQty = parentView.find("#current_item_quantity");
		currentItemDiscount = parentView.find("#current_item_discount");
		currentItemAmt = parentView.find("#current_item_amtord");
		currentItemImg = parentView.find("#currentImg");
		
		table = parentView.find("#item_list_table");
		tableBody = table.find("tbody");
		
		maxQty = parentView.find("#max_ord_qty");
		maxAmt = parentView.find("#max_ord_amt");
		maxDiscount = parentView.find("#max_ord_discount");
		orderSubmitButton = parentView.find("#order_submit");
		clearButton = parentView.find("#clear_all");
		//交班结算赋值
		allOrderSubmitButton = parentView.find("#all_order_submit");
		
		itemBarInput.change(onItemBarInputChange);
		choiceItemButton.click(onChoiceItemButtonClick);
		currentItemQty.change(onQuantityInputChange);
		currentItemAmt.change(onAmtInputChange);

		orderSubmitButton.click(onOrderSubmitButtonClick);
		clearButton.click(onClearAllButtonClick);
		//添加交班结算事件
		//allOrderSubmitButton.click(onAllOrderSubmitButtonClick);
		allOrderSubmitButton.click(function(e) {
			prepareParamAndRequestData(allsubmitSaleOrderUrl, {}, onSearchDutyShiftSuccess);
		});
		
		tableBody.click(onTrClick);
		tableBody.select(onTrClick);
		
		table.fixedtableheader({
			parent: parentView.find("#right_container"),
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
		/*  ↖↗↘↙↓↑←→ */
		//parentView.setHeaderTip("F5 选择会员　　F6 选择商品　　F9 结算　　F12 清空销售单　　↓↑ 选择销售单行　　Delete 删除选中行");
		//快捷键链接动作
		parentView.setHeaderTip("<ul id='view_spxs1_headertip'>"+
				"<li id='onkey_f5'><a>F5 选择会员</a></li>" +
				"<li id='onkey_f6'><a>F6 选择商品</a></li>" +
				"<li id='onkey_f9'><a>F9 结算</a></li>" +
				"<li id='onkey_f12'><a>F12 清空销售单</a></li>" +
				"<li>↓↑ 选择销售单行</li>" +
				"<li id='delete'><a>Delete 删除选中行</a></li>" +
				"</ul>");	
		
		parentView.closest(".window").find("#view_spxs1_headertip").on("click","li",function(){			
			var id = $(this).attr("id");
			if(id=="onkey_f5"){
				consumerInput.focus();
				consumerInput.select();
			}
			if(id=="onkey_f6"){
				itemBarInput.focus();
				itemBarInput.select();
			}
			if(id=="onkey_f9"){
				 orderSubmitButton.click();
			}
			if(id=="onkey_f12"){
				 clearButton.click();
			}
			if(id=="delete"){
				 if(table && table.jquery) {
					var tr=table.find("tr.selectRow");
					if(tr.length > 0) { 
						onDeleteItem(tr.data("item"));
						return; 
//						selectItem(tr.data("item"), true);
					}
				}
			 }
			  
		});
		
		parentView.find("#onlottery").click(function() {
			var lh = box.ich.view_spxs1_lottery();
			lh = box.showDialog({
				content: lh,
				width: 300,
				height: 200,
				buttons: []
			});
			lh.find("#lottery_submit").click(function() {
				var url = lh.find("#lottery_url").val();
				box.com.commSend({
					type: "showlottery",
					message: {
						domain: box.context.domain,
						url: url
					}
				});
//				box.console.log("-204--------------"+box.context.domain);
				box.closeDialog({content: lh});
			});
		});
		// 获取促销活动
		prepareParamAndRequestData(searchMerchPromotionUrl, {}, function(result) {
			proInf = result;
			if(proInf){
				proInfMap = {};
				for ( var i = 0; i < result.length; i++) {
					proInfMap[result[i].promotion_id] = result[i];
				}
			}
		});
		// 20140820 提示searchMerchPromotion未找到，该方法可能已删除
		//searchMerchPromotion();
	}
	
	//展示促销商品列表
	function getAllLineItem(){
		var itemInLine = null;
		var orderList = [];
		var order = {qty_ord_total: 0, amtys_ord_total: 0, qty_ord_count: 0};
		$.each(tableBody.children(), function(index, line) {
			itemInLine = $(line).data("item");
			orderList.push(itemInLine);
			order.qty_ord_count++;
			order.qty_ord_total += parseFloat(itemInLine.qty_ord);
			order.amtys_ord_total += parseFloat(itemInLine.notPromote?itemInLine.amt_ord:itemInLine.big_pri4*itemInLine.qty_ord);
		});
		order.list = orderList;
		if(currentConsumer && currentConsumer.consumer_id) {
			order.consumer_id = currentConsumer.consumer_id;
//			order.consumer_grade = currentConsumer.grade;
		}
		
		var executor = getPromotionExecutor(proInf);
		promotionItem = executor.execute(order);
		adjustedAmount = order.adjusted_amount;
		promotionIds = order.promotion_ids;
		onGetPromotionItem(promotionItem);
	}
	
	function insertPromotionIcon(itemTr, itemInLine) {
		if(itemInLine.promotion_ids && proInf) {
			for(var i=0; i<itemInLine.promotion_ids.length; i++) {
				for(var j=0; j<proInf.length; j++) {
					var promotionId = proInf[j].promotion_id;
					var promotionType = proInf[j].promotion_type;
					if(itemInLine.promotion_ids[i]==promotionId) {
						if(promotionType=="30") {
							//if(!itemTr.find("td:eq(1)").children().hasClass("projifen")){
								itemTr.find("td:eq(1)").append("<img class='salepro_img projifen' alt='jifen' src='../public/retail/img/jifen_icon.png'>");
								itemTr.find("td:eq(1)").append("<div class='salepro_text jifen imgTips'><b>促销：</b><lable></lable></div>");
							//}
							itemTr.find("td:eq(1)").find("div.jifen lable").text(proInf[j].promotion_desc);
						} else if(promotionType=="10") {
							if(!itemTr.find("td:eq(1)").children().hasClass("prozhe")){
								itemTr.find("td:eq(1)").append("<img class='salepro_img prozhe' alt='zhe' src='../public/retail/img/zhe_icon.png'>");
								itemTr.find("td:eq(1)").append("<div class='salepro_text zhe imgTips'><b>促销：</b><lable></lable></div>");
							}
							itemTr.find("td:eq(1)").find("div.zhe lable").text(proInf[j].promotion_desc);
						}
						break;
					}
				}
			}
		}else{
			itemTr.find(".prozhe").remove();
		}
	}
	
	// 促销完成后更新当前商品
	function updateCurrentItem(itemTr) {
		var selectedItem=itemTr.data("item");
		if(currentItem && currentItem.line_label==selectedItem.line_label) {
			currentItemName.text(selectedItem.item_name);
			currentItemPri.text($.parseMoney(selectedItem.big_pri4));
			currentItemSalePri.text($.parseMoney(selectedItem.amt_ord/selectedItem.qty_ord));
			currentItemUnit.text(selectedItem.big_unit_name);
			currentItemAmt.val(selectedItem.amt_ord);
			currentItemQty.val(selectedItem.qty_ord);
		}
	}
	
	function onGetPromotionItem(order) {
		$(".jian_img").hide();
		//$(".vip_img").hide();
		$(".projifen").remove();
		
		var orderLines = order.list;
		if(orderLines && orderLines.length) {
			for(var index=0; index<orderLines.length; index++) {
				var itemInLine = orderLines[index];
				var itemTr=$("#item_list_table tbody").find("tr:eq("+index+")");
				insertPromotionIcon(itemTr, itemInLine);
				var qtyOrd = itemInLine.qty_ord;
				var amount = itemInLine.notPromote?itemInLine.amt_ord:itemInLine.big_pri4*qtyOrd;
				var adjustedAmount = itemInLine.adjusted_amount ? itemInLine.adjusted_amount : 0;
				var proAmtOrd = $.parseMoney(amount-adjustedAmount);
				var pri = (amount-adjustedAmount) / qtyOrd;
				itemTr.find("td:eq(2)").find("label.pri").text($.parseMoney(parseFloat(pri)));
				itemTr.find("td:eq(4)").find("label.qtyord").text(qtyOrd);
				itemTr.find("td:eq(5)").find("label.amtord").text(proAmtOrd);
				var selectedItem=itemTr.data("item");
				selectedItem.pri=$.parseMoney(pri);
				selectedItem.newPri=$.parseMoney(pri);
				selectedItem.qty_ord=qtyOrd;
				selectedItem.amt_ord=proAmtOrd;
				updateCurrentItem(itemTr);
				// 赋值给tr保存以备后用, 调用selectItem更新"当前商品"区域数据 #by 梁凯 2014-7-28
//				itemTr.data("item", selectedItem);
			}
		}
		totalAmt=$.parseMoney(parseFloat(order.amtys_ord_total));
		disVal=$.parseMoney(parseFloat(order.adjusted_amount));
		amtReceivable=$.parseMoney(parseFloat(order.amtys_ord_total-order.adjusted_amount));
		if(order.promotion_ids){
			for(var n=0;n<order.promotion_ids.length;n++){
				for(var m=0;m<proInf.length;m++){
					if(order.promotion_ids[n]==proInf[m].promotion_id){
						if(proInf[m].promotion_type=="40"){
							$(".footer_ul").find("li:eq(3)").find(".jian_img").show();
							$(".footer_ul").find("li:eq(3)").find("div.jian lable").text(proInf[m].promotion_desc);
							$("#submit_info li").find(".jian_content").text("促销："+proInf[m].promotion_desc);
						}
						if(proInf[m].promotion_type=="13"){
							$(".footer_ul").find("li:eq(3)").find(".vip_img").show();
							$(".footer_ul").find("li:eq(3)").find("div.vip lable").text(proInf[m].promotion_desc);
						}
					}
				}
			}
		}
		
		$(".salepro_img").mousemove(function(){
			var altImg=$(this).attr("alt");
			var osTop=$(this).offset().top-30;
			var osLeft=$(this).offset().left+20;
			$(this).parent().find("."+altImg).offset({top:osTop,left:osLeft});
			$(this).parent().find("."+altImg).show(50);
		});
		$(".salepro_img").mouseleave(function(e){
			var altImg=$(this).attr("alt");
			$(this).parent().find("."+altImg).hide();
		});
	}
	
	// 处理交接班数据
	function onSearchDutyShiftSuccess(result) {//.unbind("click")
		//日期时间格式化为yyyy-MM-dd hh:mm:ss
		var lastShiftYear = result.last_shift_date.substr(0, 4);
		var lastShiftMonth = result.last_shift_date.substr(4, 2);
		var lastShiftDay = result.last_shift_date.substr(6, 2);
		var lastShiftHour = result.last_shift_time.substr(0, 2);
		var lastShiftMinute = result.last_shift_time.substr(2, 2);
		var lastShiftSecond = result.last_shift_time.substr(4, 2);
		var lastShiftDate = new Date(lastShiftYear, lastShiftMonth, lastShiftDay, lastShiftHour, lastShiftMinute, lastShiftSecond); 
		result.last_shift_date = lastShiftDate.format("yyyy-MM-dd hh:mm:ss");
		var shiftYear = result.shift_date.substr(0, 4);
		var shiftMonth = result.shift_date.substr(4, 2);
		var shiftDay = result.shift_date.substr(6, 2);
		var shiftHour = result.shift_time.substr(0, 2);
		var shiftMinute = result.shift_time.substr(2, 2);
		var shiftSecond = result.shift_time.substr(4, 2);
		var shiftDate=new Date(shiftYear, shiftMonth, shiftDay, shiftHour, shiftMinute, shiftSecond);
		result.shift_date=shiftDate.format("yyyy-MM-dd hh:mm:ss");
		
		//显示数据
		selectAllSubmitSaleOrder = box.ich.view_spxs1_allOrderSubmit({result:result});
		selectAllSubmitSaleOrder = box.showDialog({
			title: "交班结算",
			width: "440",
			height: "370",
			model: true,
			content: selectAllSubmitSaleOrder,
			close: function(e) {
				selectAllSubmitSaleOrder=null;
			}
		});
		$("#closeallordersubmit").click(function(e) {
			clearAll();
			prepareParamAndRequestData(addsubmitSaleOrderUrl, {
				amt_sale_card: result.amt_sale_card,
				amt_sale_cash: result.amt_sale_cash,
				amt_return: result.amt_return,
				last_shift_time: result.last_shift_time,
				last_shift_date: lastShiftYear + lastShiftMonth + lastShiftDay
			}, function(result) {
				prepareParamAndRequestData(exitUrl, {}, function(result) {
					alertMe("已注销，将会转向到登录页面！");
					setTimeout(function() {
						window.location.href = result;
					}, 1000);
				});
			});
		});
		
		$("#backordersubmit").click(function(e) {
			isCancelClose = true;
			box.closeDialog({content: selectAllSubmitSaleOrder});
			isCancelClose = false;
		});
	}
	
	//查看完结算返回
	function backordersubmit(selectAllSubmitSaleOrder){
		isCancelClose = true;
		box.closeDialog({content: selectAllSubmitSaleOrder});
		isCancelClose = false;
	}
	function onChoiceItemButtonClick(e) {
		box.controls.choiceItemDialog(undefined, function(itemList) {
			showItem(itemList);
		}, currentItemQty, choiceItemButton);
	}
	
	function onConsumerInputChange(e) {
		var keyword = $(e.currentTarget).val();
		if(keyword) {
			box.request({
				url: box.getContextPath() + searchMerchConsumer,
//				data: {keyword: keyword},
				data: {params: $.obj2str({keyword: keyword})},
				success: function(data) {
					if(data && data.code=='0000') {
						onSearchConsumerSuccess(data.result);
					}
				}
			});
//			prepareParamAndRequestData(searchMerchConsumer, {telephone: keyword, page_index: -1, page_size: -1}, onSearchConsumerSuccess);
		}
	}
	
	//使用会员
	function onSearchConsumerSuccess(result) {
		if(result.length>0) {
			currentConsumer = result[0];
			var kxpConsumer = {consumer_id:currentConsumer.consumer_id, card_id:currentConsumer.card_id,
					consumer_name:currentConsumer.consumer_name, telephone:currentConsumer.telephone, 
					curscore:currentConsumer.curscore, grade_id:currentConsumer.grade_id,
					grade_name:currentConsumer.grade_name, topscore:currentConsumer.topscore,
					grade:currentConsumer.grade, discount:currentConsumer.discount
			};
			if(currentConsumer.status=="2") {
				consumerContainer.find("#input_consumer").val("");
				alertMe("此会员已无效！");
				return;
			}
			if(currentConsumer.consumer_id && currentConsumer.grade_id && currentConsumer.discount ){
				$(".footer_ul").find("li:eq(3)").find(".vip_img").show();
				$(".footer_ul").find("li:eq(3)").find("div.vip lable").text(currentConsumer.discount +"%");
			}
			var consumerUl = box.ich.view_spxs1_consumer_ul({consumer:currentConsumer});
			consumerContainer.empty().append(consumerUl);
			getAllLineItem();
			refreshTotalContainer();
			keXianPinObj.consumer = kxpConsumer;
			box.com.commSend({type: "showconsumer", message: {consumer_grade:currentConsumer.grade, consumer_point:currentConsumer.curscore}});
			box.com.commSend({type: "showall", message: (keXianPinObj)});
//			box.console.log("--添加会员----------------keXianPinObj:"+$.obj2str(keXianPinObj));
			consumerContainer.find("#clean_consumer").unbind("click").click(onClearConsumerButtonClick);
		} else {
			consumerContainer.find("#input_consumer").val("");
			alertMe("卡号/手机号没有对应的会员信息！");
		}
	}
	
	function resetConsumer() {
		currentConsumer = {};
		$(".vip_img").hide();
		consumerContainer.empty().append(box.ich.view_spxs1_consumer_input());
		consumerInput = consumerContainer.find("#input_consumer");
		consumerInput.change(onConsumerInputChange);
	}
	
	function onItemBarInputChange(e) {
		var itemBar = $(e.currentTarget).val();
		if(itemBar) {
			getItem(itemBar);
		}
	}
	
	//取消会员
	function onClearConsumerButtonClick(e) {
		resetConsumer();
		currentConsumer.consumer_id="";
		getAllLineItem();
		refreshTotalContainer();
		keXianPinObj.consumer="";
		box.com.commSend({type: "showall", message: (keXianPinObj)});
//		box.console.log("--取消会员----------------keXianPinObj:"+$.obj2str(keXianPinObj));
	}
	
	function getItem(itemBar) {
		if(selectItemDialog!=null) {
			selectItemDialog.find("tr").unbind("click");
			box.closeDialog({content:selectItemDialog});
			selectItemDialog = null;
		}
		itemBarInput.val(itemBar);
		/**
		 * 20140723 修改扫码获取商品采用异步方式
		 */
		box.com.getItemAsync({bigbar: itemBar}, function(items) {
			if(items && items.length > 0) {
				onGetItem(items, itemBar);
			} else {
				prepareParamAndRequestData(itemDetailUrl, {big_bar:itemBar}, function(result) {
					onGetItem(result, itemBar);
				});
			}
		});
	}
	
	function onGetItem(result, itemBar){
		if(result && result.length>0) {
			itemList = [];
			isBase = false;
			$.each(result, function(index, item) {
				if(item) {
					if(item.flag=="BASE") {
						box.controls.newItemDialog({item_id:item.item_id,big_bar:item.item_bar,item_name:item.item_name}, function(result) {
							showItem(result);
							currentItemQty.focus();
							currentItemQty.select();
						}, true);
						isBase = true;
						return false;
					} else if(item.status=="1"){
						itemList.push(item);
					} 
				}
			});
			if(!isBase) {
				if(itemList.length>0) {
					showItem(itemList);
				} else {
					box.showAlert({message:result[0].item_name+"等"+(result.length-itemList.length)+"个商品不在售！<br/>请在商品管理中上架后再进行销售！"});
					itemBarInput.val("");
				}
			}
		} else {
			box.controls.newItemDialog({big_bar:itemBar,item_name:itemBar}, function(result) {
				showItem(result);
			},true);
		}
	}
	
	function showItem(itemList) {
		if(itemList.length>0) {
			if(itemList.length==1) {
				addItem(itemList[0]);				
			} else {
				selectItemDialog = box.ich.view_spxs1_select_item_dialog({list: itemList});
				selectItemDialog = box.showDialog({
					title: "重复商品",
					width: "750",
					height: "400",
					model: true,
					content: selectItemDialog,
					close: function(e) {
						selectItemDialog=null;
					}
				});
				selectItemDialog.find("tr").click(onChoiceTrClick(itemList));
			}
			currentItemQty.focus();
			currentItemQty.select();
		} else {
			box.showAlert({message: "所输入的条形码没有对应商品！"});
		}
	}
	
	
	//弹出商品选择框后的点击操作
	function onChoiceTrClick(itemList) {
		return function(e) {
			var seqId = $(e.currentTarget).attr("data-id");
			
			for(var i=0; i<itemList.length; i++) {
				if(itemList[i].seq_id == seqId) {
					selectItemDialog.find("tr").unbind("click");
					box.closeDialog({content:selectItemDialog});
					selectItemDialog = null;
					addItem(itemList[i]);
				}
			}
		};
	}
	
	
	function onQuantityInputChange(e) {
		if(currentItem) {
			var value = parseFloat($(e.currentTarget).val());
			if(isNaN(value)) {
				value = currentItem.qty_ord;
			}
			currentItem.qty_ord = value;
			
			var amt = 0.0;
			if(currentItem.newPri) {
				amt=currentItem.newPri*currentItem.qty_ord;
			} else {
				amt=currentItem.big_pri4*currentItem.qty_ord;
			}
			// 2014-8-21 修改销售数量后单价不重置, 且不再重新应用促销
			if(currentItem.modifiedPriceCausedByAmountChanged || currentItem.modifiedPriceCausedByAmountChanged==0) {
				amt=currentItem.modifiedPriceCausedByAmountChanged*currentItem.qty_ord;
			}
			if(isNaN(amt)) amt = 0.0;
			currentItem.amt_ord = amt;
			
			currentItemSalePri.text($.parseMoney(currentItem.amt_ord/currentItem.qty_ord));
			
			refreshItem();
		}
	}
	
	function onAmtInputChange(e) {
		if(currentItem) {
			var value = parseFloat($(e.currentTarget).val());
			if(isNaN(value)) {
				value = currentItem.amt_ord;
			}
			// 检查价格预警
			checkItemAmtOrd(currentItem, value, function() {
				currentItem.amt_ord = value;
				currentItem.notPromote = true;
				if(currentItem && currentItem.pri && currentItem.qty_ord) {
					currentItemSalePri.text($.parseMoney(currentItem.amt_ord/currentItem.qty_ord));
					currentItem.modifiedPriceCausedByAmountChanged = $.round(currentItem.amt_ord/currentItem.qty_ord, 2);
				}
				var tr = tableBody.find("tr[data-num="+currentItem.line_num+"]");
				tr.data("item",currentItem);
				tr.find(".amtord").text($.parseMoney(currentItem.amt_ord));
//				refreshTotalContainer();
				refreshItem(true);
			}, function() {
				// 还原成原价格
				$(e.currentTarget).val(currentItem.amt_ord);
			});
		}
	}
	
	//在商品列表中的点击操作
	function onTrClick(e) {
		var target = $(e.target);
		var tr = target.closest("tr");
		if(tr.length > 0) {
			if(target.attr("name") == "delete") {
				onDeleteItem(tr.data("item"));
				return;
			}
						
			selectItem(tr.data("item"), true);
		}
	}
	
	//删除商品的时候
	function onDeleteItem(data) {
		tableBody.find("tr[data-num="+data.line_num+"]").remove();
		
		if(currentItem) {
			if(currentItem.line_num == data.line_num) {
				selectItem(null);
			}
		}
		
		itemBarInput.val("");
		getAllLineItem();
		refreshTotalContainer();
		
		keXianPinObj.operate = "del";
		keXianPinObj.operate_line = data.line_num;
		box.com.commSend({type: "showall", message: (keXianPinObj)});
//		alert("删除"+data.line_num);
//		box.console.log("--删除商品(1)---keXianPinObj: "+$.obj2str(keXianPinObj));
//		box.console.log("--删除商品(2)---total: "+$.obj2str(total));
//		box.console.log("--679商品(3)---promotionItem: "+$.obj2str(promotionItem));
		
		refreshLineLabel();
	}
	
	//添加商品
	function addItem(item) {
		item.line_num = staticnum++;
		item.line_label = tableBody.children().length + 1;
		item.qty_ord = 1;
		item.amt_ord = $.parseMoney(parseFloat(item.big_pri4)*parseFloat(item.discount)/100.0);
		item.pri = $.parseMoney(parseFloat(item.big_pri4));
		item.newPri = item.pri;
		var tr = box.ich.view_spxs1_item_row(item);
		tableBody.append(tr);
		parentView.find("#right_container").scrollTop(99999);
		tableBody.find("tr[data-num="+item.line_num+"]").data("item", item);
		getAllLineItem();
		selectItem(item);
		refreshTotalContainer();
		keXianPinObj.operate = "add";
		keXianPinObj.operate_line = item.line_num;
		box.com.commSend({type: "showall", message: (keXianPinObj)});
//		box.console.log("--添加商品(1)---keXianPinObj:"+$.obj2str(keXianPinObj));
//		alert("添加"+item.line_num);
	}
	
	//单击商品（tr）
	function selectItem(data, onselect) {
		currentItem = data;
		if(currentItem) {
			currentItemName.text(currentItem.item_name);
			currentItemPri.text($.parseMoney(currentItem.big_pri4));
			currentItemSalePri.text($.parseMoney(currentItem.amt_ord/currentItem.qty_ord));
			currentItemUnit.text(currentItem.big_unit_name);
			currentItemQty.val(currentItem.qty_ord);
			currentItemDiscount.text(currentItem.discount);
			currentItemAmt.val($.parseMoney(currentItem.amt_ord));
			
			var item_url = function() {
				if(box.isTobacco(currentItem.item_kind_id)) {
					return box.getTobaccoImage(currentItem.item_id);
				} else {
					return box.getItemImage(currentItem.item_bar);
				}
			};
			currentItemImg.attr("src",item_url);
			
			tableBody.find("tr").removeClass("selectRow");
			if(onselect) {
				tableBody.find("tr[data-num="+currentItem.line_num+"]").addClass("selectRow");

				keXianPinObj.operate = "sel";
				keXianPinObj.operate_line = currentItem.line_num;
				box.com.commSend({type: "showall", message: (keXianPinObj)});
//				alert("选择"+currentItem.line_num);
//				box.console.log("--选择商品(1)---keXianPinObj:"+$.obj2str(keXianPinObj));
			}
			
			currentItemQty.focus();
			currentItemQty.select();
		} else {
			currentItemName.text("暂无信息");
			currentItemPri.text("0.00");
			currentItemSalePri.text("0.00");
			currentItemUnit.text("");
			currentUnitPrice.text("元");
			currentUnitSalePrice.text("元");
			currentItemQty.val("0");
			currentItemDiscount.text("100");
			currentItemAmt.val("0.00");
			currentItemImg.attr("src","");
			
			tableBody.find("tr").removeClass("selectRow");
		}
	}
	
	//修改商品（单价或金额）
	function refreshItem() {
		if(currentItem.pri) currentItem.pri = $.parseMoney(currentItem.pri);
		else currentItem.pri = "0.00";
		if(currentItem.amt_ord) currentItem.amt_ord = $.parseMoney(currentItem.amt_ord);
		else currentItem.amt_ord = "0.00";
		if(!currentItem.qty_ord) currentItem.qty_ord="0";
		currentItemName.text(currentItem.item_name);
		currentItemPri.text($.parseMoney(currentItem.big_pri4));
		if(currentItem.qty_ord != 0){
			currentItemSalePri.text($.parseMoney(currentItem.amt_ord/currentItem.qty_ord));
		}else{
			currentItemSalePri.text($.parseMoney(currentItem.amt_ord/1));
		}
		currentItemUnit.text(currentItem.big_unit_name);
		currentItemAmt.val(currentItem.amt_ord);
		currentItemQty.val(currentItem.qty_ord);
		getAllLineItem();
		refreshTotalContainer();
		
		keXianPinObj.operate = "upd";
		keXianPinObj.operate_line = currentItem.line_num;
		box.com.commSend({type: "showall", message: (keXianPinObj)});
//		box.console.log("--修改商品(1)---keXianPinObj: "+$.obj2str(keXianPinObj));
//		alert("修改"+currentItem.line_num);
//		//refreshTrItem(currentItem);
	}
	
	function refreshTrItem(item) {
		var tr = tableBody.find("tr[data-num="+item.line_num+"]");
		var view = box.ich.view_spxs1_item_row(item);
		tr.empty().append(view.html());
	}
	
	//客显屏商品赋值
	function setKxpItemList(data){
		var kxpItemMap = {}; 
		kxpItemMap.item_name = data.item_name;
		kxpItemMap.item_kind_id = data.item_kind_id;
		kxpItemMap.item_bar = data.item_bar;
		kxpItemMap.big_bar = data.big_bar;
		kxpItemMap.pir4 = $.parseMoney(data.newPri);
		kxpItemMap.unit_name = data.big_unit_name;
		kxpItemMap.qty = data.qty_ord;
		kxpItemMap.amt = $.parseMoney(data.amt_ord);
		kxpItemMap.line_num = data.line_num;
		kxpItemMap.item_id = data.item_id;
		if(data.promotion_ids){
			var promotion_descs = [];
			for ( var i = 0; i < data.promotion_ids.length; i++) {
				promotion_descs.push(proInfMap[data.promotion_ids[i]].promotion_desc);
			}
			kxpItemMap.promotion_descs = promotion_descs ;
			kxpItemMap.promotion_ids = data.promotion_ids ;
		}else{
			kxpItemMap.promotion_descs = "" ;
			kxpItemMap.promotion_ids = "";
		}
		
		return kxpItemMap;
	}
	
	//客显屏单赋值
	function setKxpObj(){
		keXianPinObj.line_num_count = promotionItem.qty_ord_count;//-----------------------总行数
		keXianPinObj.amount  = $.parseMoney(total.amount);//-------------------------------销售单应收金额
		keXianPinObj.qty_count = promotionItem.qty_ord_total;//----------------------------商品总数量
		keXianPinObj.ord_amts = $.parseMoney(promotionItem.amtys_ord_total);//-------------销售单总金额（未打折）
		if(promotionItem.promotion_ids){
			var promotion_descs = [];
			for ( var i = 0; i < promotionItem.promotion_ids.length; i++) {
				promotion_descs.push(proInfMap[promotionItem.promotion_ids[i]].promotion_desc);
			}
			keXianPinObj.promotion_descs = promotion_descs ;
			keXianPinObj.promotion_ids = promotionItem.promotion_ids ;//----------------促销id
		}else{
			keXianPinObj.promotion_descs = "" ;
			keXianPinObj.promotion_ids = "";//----------------促销id
		}
		
		keXianPinObj.adjusted_amt = $.parseMoney(promotionItem.amtys_ord_total - total.amount);//------------------------------
		
	}
	
	//页面变动时触发，（商品金额，数量，会员，添加取消会员）
	function refreshTotalContainer() {
		total.quantity = promotionItem.qty_ord_total;
		total.count = promotionItem.qty_ord_count;
		total.amount = promotionItem.amtys_ord_total - (promotionItem.adjusted_amount ? promotionItem.adjusted_amount : 0);
		total.oriAmount = promotionItem.amtys_ord_total;
		total.amount = 0.0;
		var data = null;
		keXianPinObj.list = [];
		$.each(tableBody.children(), function(i, obj) {
			data = $(obj).data("item");
			if(data.adjusted_amount){
				total.amount += parseFloat(data.amt_ord)+data.adjusted_amount;
			}else{
				total.amount += parseFloat(data.amt_ord);
			}
			keXianPinObj.list.push(setKxpItemList(data));
		});
		total.amount = total.amount - (promotionItem.adjusted_amount ? promotionItem.adjusted_amount : 0);
		if(currentConsumer.consumer_id && currentConsumer.discount){
			total.amount = $.parseMoney(parseFloat(total.amount) * parseFloat(currentConsumer.discount) / 100);
		}
		var qty = total.quantity;
		maxQty.text(qty);
		maxAmt.text($.parseMoney(total.amount));
		maxDiscount.text($.parseMoney(total.oriAmount-total.amount));
		if(submitDialog) {
			submitDialog.find("#totalAmount").text($.parseMoney(total.oriAmount));
			submitDialog.find("#receivable").text($.parseMoney(total.amount));
			submitDialog.find("#actual_input").val($.parseMoney(total.amount));
			submitDialog.find("#change").text($.parseMoney(0));
		}
		setKxpObj();//向客显屏单赋值
		if(currentItem && currentItem.qty_ord) {
			box.com.commSend({type: "showitem", message: {current_item:currentItem.item_name, current_qty:currentItem.qty_ord, current_amt:$.parseMoney(currentItem.amt_ord), total_qty:qty, total_amt:$.parseMoney(total.amount)}});
		}
	}
	
	function refreshLineLabel() {
		$.each(tableBody.children(), function(i, obj) {
			$(obj).find(".linelabel").text(i+1);
			$(obj).data("item").line_label = i+1;
		});
	}
	
	///单击结算
	function onOrderSubmitButtonClick(e) {
		var height = 365;
		var consumerId = null;
		var consumerDis = "100%";
		consumerPoint = "0";
		if(!total.amount || !total.quantity || total.quantity <= 0) {
			box.showAlert({message: "没有选择任何商品！"});
			box.stopEvent(e);
			return;
		}
		//店铺优惠金额
		box.com.commSend({type: "showorder", message: {total_qty:total.quantity, total_amt:$.parseMoney(total.amount), amt_ss:$.parseMoney(total.amount), amt_left:$.parseMoney(0)}});
//		box.console.log("--875(1)-------------单击结算---"+$.obj2str(total).toString());
//		box.console.log("--875(2)-------------"+total.quantity+"|-|"+$.parseMoney(total.amount)+"|-|"+$.parseMoney(total.oriAmount)+"|-|"+$.parseMoney(0));
		var promotionMoney = $.round( parseFloat(total.oriAmount) - parseFloat(total.amount), 2);
		if(currentConsumer && currentConsumer.consumer_id) {
			height=365;
			consumerId = currentConsumer.consumer_id;
			if(currentConsumer.discount){
				consumerDis = currentConsumer.discount+"%";
			}else{
				consumerDis = "无";
			}
			var amountPerPoint = box.user.amount_per_point;
			//会员积分
			if(!amountPerPoint || amountPerPoint == 0){
				consumerPoint = "0";
			}else{
				consumerPoint = amountPerPoint ? parseInt(total.amount / amountPerPoint) : 0;
			}
		}
		
		submitDialog = box.ich.view_spxs1_cash_submit_dialog({consumer_id:consumerId, consumer_point: consumerPoint, consumer_grade_discount:consumerDis, totalAmount: $.parseMoney(total.oriAmount), discountVal:$.parseMoney(total.oriAmount-total.amount), receivable:$.parseMoney(total.amount), change: $.parseMoney(0)});
		
		var width = 350;
		var len = (""+total.amount).length;
		if(len>7) {
			width += (len-7)*15;
		}
		submitDialog = box.showDialog({
			title: "结算",
			width: 420,
			height: height,
			model: true,
			content: submitDialog,
			close: function(e) {
				orderInfo = null;
				choiceItemButton.removeAttr("disabled");
				orderSubmitButton.removeAttr("disabled");
			}
		});
		actualInput = submitDialog.find("#actual_input");
		actualInput.select();
		cashSubmitButton = submitDialog.find("#cash_submit_button");//查找现金支付按钮
		cardSubmitButton = submitDialog.find("#card_submit_button");//查找刷卡按钮
		hangingSubmitButton = submitDialog.find("#hanging_submit_button"); // 挂账按钮
		actualInput.select();
		actualInput.keyup(onActualInputKeyUp);
		//刷卡消费按钮 
		cashSubmitButton.unbind("click").click(onSubmitButtonClick(true,1));//单击现金支付
		cardSubmitButton.unbind("click").click(onSubmitButtonClick(false,2));//单击刷卡
//		hangingSubmitButton.unbind("click").click(onSubmitButtonClick(true,3)); // 挂账
	}
	
	function onClearAllButtonClick(e) {
		box.showConfirm({
			message: "确定要清空销售单吗？",
			ok: clearAll
		});
	}
	
	function onActualInputKeyUp(e) {
		var change = submitDialog.find("#change");
		// 应收从total中取, 实收接收键盘输入
		total.amt_ss = submitDialog.find("#actual_input").val();
		var c = parseFloat(total.amt_ss) - parseFloat(total.amount); //找零
		if(isNaN(c)) c = 0;
		total.change = $.parseMoney(c);
		change.text(total.change);
		box.com.commSend({type: "showorder", message: {total_qty:total.quantity, total_amt:$.parseMoney(total.amount), amt_ss:$.parseMoney(total.amt_ss), amt_left:$.parseMoney(total.change)}});
//		box.console.log("--864------------total_qty:"+$.obj2str(total));
//		box.console.log("--864(2)-------------"+total.quantity+"|-|"+$.parseMoney(total.amount)+"|-|"+$.parseMoney(total.amt_ss)+"|-|"+$.parseMoney(total.change));
	}
	
	function onSubmitButtonClick(isCash,payType) {
		var itemList = new Array();
		if(!payType){
			payType="1";
		}
		function submitCashOrder(orderInfo) {
			orderInfoDialog = box.ich.view_spxs1_cash_confirm_dialog({order:orderInfo});
			orderInfoDialog = box.showDialog({
				title: "结算",
				width: 525,
				height: 400,
				model: true,
				content: orderInfoDialog,
				onkey: function(data) {
					if(data.key == HOME.Keys.ENTER) {
						box.closeDialog({content: orderInfoDialog});
					}
				},
				close: function() {
					clearAll();
				}
			});
			box.closeDialog({content: submitDialog});
			openCashBox();
			
			box.controls.print(orderInfo, total, itemList, currentConsumer);
			$("#order_submit").blur();
			
			orderInfoDialog.find("#printing").empty().append($('<input id="close_button" type="button" class="minor_button" style="width:100px; height:35px; font-size:20px" value="关 闭"/>'));
			$("#close_button").focus();
			orderInfoDialog.find("#close_button").unbind("click").click(closeOrderInfoDialog);
			
			orderInfoCloseTimeout = setTimeout(closeOrderInfoDialog, 5000);
		}
		function submitCardOrder(order, amt_ss){
			//刷卡消费或刷卡转现金支付 请求后台更改订单状态后的 回调事件
			var consumeSuccessByCardOrCardToCash = function(result){
				var title="";
				if(orderInfo.pay_type == 1){
					title = "现金结算";
					cardConfirmDialog = box.ich.view_spxs1_cash_confirm_dialog({order:orderInfo});
				}
				else{
					title = "刷卡结算";
					cardConfirmDialog = box.ich.view_spxs1_card_confirm_dialog({order:orderInfo});
				}
				if(orderInfo.pmt_status == "02") {
					title = "挂账";
					cardConfirmDialog = box.ich.view_spxs1_card_confirm_dialog({order:orderInfo});
				}
				cardConfirmDialog = box.showDialog({
					title: title,
					width: 525,
					height: 420,
					model: true,
					content: cardConfirmDialog,
					onkey: function(data) {
						if(data.key == HOME.Keys.ENTER) {
							box.closeDialog({content: cardConfirmDialog});
						}
					},
					close: function() {
						clearAll();
					}
				});
				box.closeDialog({content: submitDialog});
				$("#order_submit").blur();
				box.controls.print(order, total, itemList, currentConsumer);
				cardConfirmDialog.find("#printing").empty().append($('<input id="close_button" type="button" class="minor_button" style="width:100px; height:35px; font-size:20px" value="关 闭"/>'));
				$("#close_button").focus();
				cardConfirmDialog.find("#close_button").unbind("click").click(closeOrderInfoCardDialog);
				orderInfoCloseTimeout = setTimeout(closeOrderInfoCardDialog, 5000);
			};
			//刷卡转现金支付   ----点击现金支付时执行
			var submitCardToCash = function(){
				var amt_ord_total = submitDialog.find("#actual_input").val();
				var amtys_ord_total = total.amount; 
				var amt_ord_change = parseFloat(amt_ord_total) - parseFloat(amtys_ord_total); //找零AMT_ORD_CHANGE
				orderInfo.amtys_ord_total=amtys_ord_total;
				orderInfo.amt_ord_total=amt_ord_total;
				orderInfo.change=amt_ord_change;
				orderInfo.pay_type="1";
				prepareParamAndRequestData(cashPaySaleOrderUrl, {
					order_id: orderInfo.order_id,
					amt_ord_total: $.parseMoney(amt_ord_total),
					amtys_ord_total: $.parseMoney(amtys_ord_total),
					amt_ord_change: $.parseMoney(amt_ord_change)
				}, consumeSuccessByCardOrCardToCash);
			};
			//刷卡之后回调函数
			var onsumeUseCardCallback = function(data) {
				box.hideMask();
				// 凭证号, 卡号, 金额, 日期, 时间
				if(data){
					if(data.flag == "00"){
						var amt_ord_total = submitDialog.find("#actual_input").val();
						var amtys_ord_total = total.amount; 
						var amt_ord_change = parseFloat(amt_ord_total) - parseFloat(amtys_ord_total); //找零AMT_ORD_CHANGE
						orderInfo.amtys_ord_total=amtys_ord_total;
						orderInfo.amt_ord_total=amt_ord_total;
						orderInfo.change=amt_ord_change;
						var trade_info = data.pos_serial_number +"-"+ data.shop_number +"-"+ data.pos_number +"-"+ data.card_number;
						var certificate_id = data.bank_serial_number;
						prepareParamAndRequestData(completeCardPayUrl, {
							order_id: orderInfo.order_id,
							certificate_id: certificate_id,
							trade_info: trade_info,
							amt_ord_total: $.parseMoney(amt_ord_total),
							amtys_ord_total: $.parseMoney(amtys_ord_total),
							amt_ord_change: $.parseMoney(amt_ord_change)
						}, consumeSuccessByCardOrCardToCash);
					}
					else {
						var message = "";
						if(data.flag == "01"){
							message = "刷卡支付取消，请再次选择支付方式";
						}
						else if(data.flag == "02"){
							message = "刷卡支付失败，请再次选择支付方式";
						}
						else if(data.flag == "03"){
							message = "未连接POS机";
						}
						else if(data.flag == "04"){
							message = "POS机未响应,请确认POS机状态";
						}
						box.showAlert({message: message});
					}
				}
				else{
					var amt_ord_total = submitDialog.find("#actual_input").val();
					var amtys_ord_total = total.amount; 
					var amt_ord_change = parseFloat(amt_ord_total) - parseFloat(amtys_ord_total); //找零AMT_ORD_CHANGE
					orderInfo.amtys_ord_total=amtys_ord_total;
					orderInfo.amt_ord_total=amt_ord_total;
					orderInfo.change=amt_ord_change;
					prepareParamAndRequestData(completeCardPayUrl, {
						order_id: orderInfo.order_id,
						certificate_id: "-",
						trade_info: "-",
						amt_ord_total: $.parseMoney(amt_ord_total),
						amtys_ord_total: $.parseMoney(amtys_ord_total),
						amt_ord_change: $.parseMoney(amt_ord_change)
					}, consumeSuccessByCardOrCardToCash);
				}
			};
			//点击刷卡消费之后  就需要将将现金消费改为  刷卡转现金状态。
			cashSubmitButton.unbind("click").click(submitCardToCash);
			cardSubmitButton.attr("disabled",false);
			var detailMSG = "<lable style='color:#ff5828;'>注意事项：</lable> <br/>1、请确认POS机与电脑正常连接。 <br/>2、确认POS上的缴费金额。";
			// 调用刷卡结算接口, 回调
			box.showMask({
				message: "请在POS机上刷卡",
				detailMSG:detailMSG,
				hiddenButton:{"hidden":false,"val":" 已完成支付 ",callback:onsumeUseCardCallback},
				cancelButton:{},
				feeContent:{"hidden":false,val:amt_ss}
			});
			//box.showMask({message: "请在POS机上刷卡",isShowHiddenButton:true,buttonVal:"已支付完成",callBack:onsumeUseCardCallback});
			box.stopEvent();
			box.com.consume({num:2,pri:amt_ss},onsumeUseCardCallback);
		}
		
		function onSubmitClick() {
			itemList.length=0;
			$.each(tableBody.children(), function(i, obj) {
				var data = $(obj).data("item");
				if(!data.cost) {
					delete(data.cost);
				}
				itemList.push(data);
			});
			var obj = {qty_ord_total: total.quantity, qty_ord_count: total.count, amt_ord_total: total.amt_ss, amtys_ord_total: total.amount, adjusted_amount: total.oriAmount-total.amount, order_type: "01", operator: "1", list: itemList};
			var orderDate = parentView.find("#create_date_text").val();
			if(orderDate){
				var newOrderDate = new Date(orderDate.substr(0, 4), orderDate.substr(5, 2)-1, orderDate.substr(8, 2));
				newOrderDate = (newOrderDate).format("yyyyMMdd");
				obj.order_date = newOrderDate;
			}
			if(currentConsumer && currentConsumer.consumer_id) {
				obj.consumer_id = currentConsumer.consumer_id;
//				obj.consumer_grade = currentConsumer.grade;
			}
			if(consumerPoint) {
				obj.consumer_point = consumerPoint;
			}
			obj.adjusted_amount = total.oriAmount-total.amount;
			if(promotionIds) obj.promotion_ids = promotionIds;
			obj.amt_ord_change=parseFloat(total.amt_ss) - parseFloat(total.amount);//找零
			var url = "";
			if(isCash) {
				if(payType==1){
					url = submitSaleOrderUrl;
				}else if(payType==2){
					url=submiCardtSaleOrderUrl;
				}else if(payType==3){
					url=submitHangingSaleOrderUrl;
				}
			} else {
				//点击刷卡结算未成功再次点击刷卡结算，此订单不再入库
				if(orderInfo){
					submitCardOrder(orderInfo, submitDialog.find("#actual_input").val());
					return ;
				}
				else{
					url = submitSaleOrderWithCardUrl;
				}
			}
			box.request({
				url: box.getContextPath() + url,
				data: {params: $.obj2str(obj)},
				success: function(json) {
					if(json && json.code=="0000") {
						orderInfo = json.result;
						// 挂账的找零和实收都为0, 在后台还会处理抹零
						if(payType==3) {
							orderInfo.change = $.parseMoney(0);
							orderInfo.amt_ord_total = $.parseMoney(0);
						} else {
							orderInfo.change = $.parseMoney(orderInfo.amt_ord_total-orderInfo.amtys_ord_total);
							orderInfo.amt_ord_total = $.parseMoney(orderInfo.amt_ord_total);
						}
						orderInfo.amtys_ord_total = $.parseMoney(orderInfo.amtys_ord_total);
						orderInfo.card_id = currentConsumer.card_id;
						//日期时间格式化为yyyy-MM-dd hh:mm:ss
						var orderDate=new Date(orderInfo.order_date.substr(0, 4),orderInfo.order_date.substr(4, 2)-1,orderInfo.order_date.substr(6, 2),
								orderInfo.order_time.substr(0, 2),orderInfo.order_time.substr(2, 2),orderInfo.order_time.substr(4, 2));
						orderInfo.order_date_label=orderDate.format("yyyy-MM-dd hh:mm:ss");
						
						if(isCash) {
							submitCashOrder(orderInfo);
							cashSubmitButton.removeAttr("disabled");
						} else {
							submitCardOrder(orderInfo, total.amt_ss);
							//cardSubmitButton.removeAttr("disabled");
						}
						orderSubmitButton.removeAttr("disabled");
					} else {
						box.showAlert({message: json.msg});
						if(isCash) {
							cashSubmitButton.removeAttr("disabled");
						} else {
							cardSubmitButton.removeAttr("disabled");
						}
						orderSubmitButton.removeAttr("disabled");
					}
				},
				error: function(json) {
					if(isCash) {
						cashSubmitButton.removeAttr("disabled");
					} else {
						cardSubmitButton.removeAttr("disabled");
					}
				}
			});
		}
		
		return function(e) {
			total.amt_ss = submitDialog.find("#actual_input").val();
			if(!total.amt_ss) {
				box.showAlert({message: "请输入实收金额！"});
				return;
			}
			checkAmtOrd(total.amt_ss, total.amount, function() {
				onSubmitClick();
			}, function() {
				// 点击取消，不做动作
				cashSubmitButton.removeAttr("disabled");
				cardSubmitButton.removeAttr("disabled");
			});
		};
	}
	
	function closeOrderInfoDialog() {
		clearTimeout(orderInfoCloseTimeout);
		box.closeDialog({content: orderInfoDialog});
		box.showAlert({message: "销售结算完成"});
	}
	function closeOrderInfoCardDialog(){
		clearTimeout(orderInfoCloseTimeout);
		box.closeDialog({content: cardConfirmDialog});
		box.showAlert({message: "销售结算完成"});
	}
	function openCashBox() {
		box.com.openCashBox();
	}
	
	function clearAll() {
		keXianPinObj = {};
		keXianPinObj.list = [];
		keXianPinObj.consumer="";
		staticnum = 1;
		box.com.commSend({type: "clearorder"});
//		box.console.log("-1151------------清除销售单");
		resetConsumer();
		tableBody.empty();
		selectItem(null);
		consumerPoint = null;
		
		getAllLineItem();
		
		refreshTotalContainer();
		itemBarInput.val("");
	}
	
	function onKey(data){
		switch(data.key) {
		case HOME.Keys.BARCODE:
			getItem(data.code);
			break;
		case HOME.Keys.DELETE:
			if(table && table.jquery) {
				var tr=table.find("tr.selectRow");
				if(tr.length > 0) { 
					onDeleteItem(tr.data("item"));
					return; 
//					selectItem(tr.data("item"), true);
				}
			}
			break;
		case HOME.Keys.F5:
			consumerInput.focus();
			consumerInput.select();
			break;
		case HOME.Keys.F6:
			itemBarInput.focus();
			itemBarInput.select();
			break;
		case HOME.Keys.F9:
			orderSubmitButton.click();
			break;
		case HOME.Keys.F12:
			clearButton.click();
			break;
			
		}
	}

	// 窗口关闭，资源释放
	function onClose(param) {
		var newItemIsEmpty = true;
		if(total.amount == 0 && total.quantity == 0) {
			newItemIsEmpty=false;
		}
		if(newItemIsEmpty){
			box.showConfirm({
				message: "商品列表中有未结算商品，是否结算？",
				hasSubmit:true,
				buttonText:["结 算","直接关闭","取 消"],
				submit: function() {
					orderSubmitButton.click();
				},
				donotSubmit: function() {
					clearAll();
					param.callback();
				}
			});
		}else{
			return true;
		}
		return false;
	}
	
	var adjustedAmount = 0;
	var promotionIds = [];
	var executorHistory = [];
	
	//商品促销逻辑
	function isIncludes(collection, key) {
		if(collection) {
			for(var i=0; i<collection.length; i++) {
				if(collection[i]==key) return true;
			}
		}
		return false;
	}
	
	function getShouldElementFromLine(promotion, line) {
		var shouldElement=null;
		if(promotion.promotion_should) {
			var shouldPromotion = promotion.promotion_should;
			if(shouldPromotion.seq_id && shouldPromotion.seq_id[line.seq_id]) shouldElement = shouldPromotion.seq_id[line.seq_id];
			else if(shouldPromotion.item_kind_id && shouldPromotion.item_kind_id[line.item_kind_id]) shouldElement = shouldPromotion.item_kind_id[line.item_kind_id];
			else if(shouldPromotion.all_item) shouldElement = shouldPromotion.all_item;
		}
		return shouldElement;
	}
	
	function getDiscountFromOrder(promotion, order) {
		if(promotion.promotion_action && promotion.promotion_action.discount && order.consumer_grade) {
			var consumerDiscount = promotion.promotion_action.discount[order.consumer_grade];
			return consumerDiscount ? consumerDiscount : promotion.promotion_action.discount['all'];
		}
	}
	
	function getReductionFromOrder(promotion, order) {
		if(promotion.promotion_action) {
			var actionPromotion = promotion.promotion_action;
			if(actionPromotion.ceiling_reduction) {
				var ceiling = actionPromotion.ceiling_reduction.ceiling;
				var reduction = actionPromotion.ceiling_reduction.reduction;
				if(!order.adjusted_amount) order.adjusted_amount = 0;
				if(order.amtys_ord_total-order.adjusted_amount>=ceiling) {
					return reduction;
				}
			}
		}
		return 0;
	}
	
	function caculateAdjustedPrice(line, should) {
		if(!line.adjusted_price) line.adjusted_price = 0;
		if(should.discount_price) {
			line.adjusted_price = line.big_pri4 - should.discount_price;
		} else if(should.discount) {
			//line.adjusted_price += (line.big_pri4 - line.adjusted_price) * (1.0 - should.discount / 100);
			line.adjusted_price = line.big_pri4 * (1.0 - should.discount / 100);
		}
	}
	
	function caculateAdjustedPoint(line, should) {
		if(!line.adjusted_point) line.adjusted_point = 0;
		line.adjusted_point += should.point ? parseInt(should.point) : 0;
	}
	
	function caculateAdjustedAmount(order, discount) {
		if(!order.adjusted_amount) order.adjusted_amount = 0;
		if(discount) {
			order.adjusted_amount += (order.amtys_ord_total - order.adjusted_amount) * (1.0 - discount / 100);
		}
	}
	
	function getPromotionExecutor(promotions) {
		var executor=null;
		for(var i=0; i<promotions.length; i++) {
			var promotion = promotions[i];
			switch(promotion.promotion_type) {
			case "10":
				if(executor) executor.appendExecutor(new ItemDiscountPromotionExecutor(promotion));
				else executor = new ItemDiscountPromotionExecutor(promotion);
				break;
			case "13":
				if(executor) executor.appendExecutor(new DiscountPromotionExecutor(promotion));
				else executor = new DiscountPromotionExecutor(promotion);
				break;
			case "30":
				if(executor) executor.appendExecutor(new ItemPointPromotionExecutor(promotion));
				else executor = new ItemPointPromotionExecutor(promotion);
				break;
			case "40":
				if(executor) executor.appendExecutor(new CeilingReductionPromotionExecutor(promotion));
				else executor = new CeilingReductionPromotionExecutor(promotion);
				break;
			}
		}
		return executor ? executor : new function() {
			this.execute = function(order) {
				return order;
			};
		}();
	}
	
	function PromotionExecutor(promotion) {
		
		this.promotionId = promotion.promotion_id;
		this.type = promotion.promotion_type;
		this.must = promotion.promotion_must;
		this.target = this.must ? this.must.target : {};
		this.dimensionId = promotion.dimension_id;
		this.isCoexistent = promotion.is_coexistent;
		this.isInsistent = promotion.is_insistent;
		
		this.loadStrategy = function(strategy) {
			this.promotionStrategy = strategy;
		};
		
		this.appendExecutor = function(executor) {
			if(this.nextExecutor) {
				this.nextExecutor.appendExecutor(executor);
			} else {
				this.nextExecutor = executor;
			}
		};
		
		this.execute = function(order) {
			if(!executorHistory.length) this.clearOrder(order);
			if(this.matchTime() && this.matchConsumer(order) && this.isActive()) {
				this.promotionStrategy.promote(order);
				if(this.isUsed) executorHistory.push(this);
			}
			if(this.nextExecutor) this.nextExecutor.execute(order);
			else executorHistory.length=0;
			
			return order;
		};
		
		this.clearOrder = function(order) {
			delete(order.adjusted_amount);
			delete(order.adjusted_point);
			delete(order.promotion_ids);
			var list = order.list;
			if(list) {
				for(var i=0; i<list.length; i++) {
					var line = list[i];
					delete(line.adjusted_point);
					delete(line.adjusted_amount);
					delete(line.adjusted_price);
					delete(line.promotion_ids);
				}
			}
		};
		
		this.matchTime = function() {
			var now = new Date().format('yyyyMMddhhmmss');
			var start = promotion.start_date + promotion.start_time;
			var end = promotion.end_date + promotion.end_time;
			return now >= start && now <= end;
		};
		
		this.matchConsumer = function(order) {
			return this.target.all 
						|| order.consumer_id && (this.target.all_member 
								|| isIncludes(this.target.member, order.consumer_id)) 
						|| order.consumer_grade && isIncludes(this.target.member_grade, order.consumer_grade);
		};
		
		this.isActive = function() {
			for(var i=0; i<executorHistory.length; i++) {
				var lastExecutor = executorHistory[i];
				// 此活动不生效的判断标准: 1. 同维度下类型相同, 2. 同维度下不共存且对方坚持, 3.对方不坚持自己也不坚持
				// 其他判断标准: 1. 会员因素, 2. 促销过期
				// 前提条件: 活动按照共存正序,坚持倒序,创建时间倒序排列
				if(this.dimensionId==lastExecutor.dimensionId && (this.type==lastExecutor.type 
						|| (this.isCoexistent=='1'&&lastExecutor.isCoexistent=='1'?false:lastExecutor.isInsistent=='1'?true:this.isInsistent=='1'?false:true))) return false;
			}
			return true;
		};
		
	}
	
	function ItemDiscountPromotionExecutor(promotion) {
		PromotionExecutor.call(this, promotion);
		this.loadStrategy(new LinePromotionStrategy(this));
		this.matchLine = function(line) {
			var shouldElement = getShouldElementFromLine(promotion, line);
			if(shouldElement) {
				if(!line.notPromote) caculateAdjustedPrice(line, shouldElement);
				if(!line.promotion_ids) line.promotion_ids = [];
				line.promotion_ids.push(this.promotionId);
				return true;
			}
			return false;
		};
	}
	
	function ItemPointPromotionExecutor(promotion) {
		PromotionExecutor.call(this, promotion);
		this.loadStrategy(new LinePointPromotionStrategy(this));
		this.matchLine = function(line) {
			var shouldElement = getShouldElementFromLine(promotion, line);
			if(shouldElement) {
				if(!line.notPromote) caculateAdjustedPoint(line, shouldElement);
				if(!line.promotion_ids) line.promotion_ids = [];
				line.promotion_ids.push(this.promotionId);
				return true;
			}
			return false;
		};
	}
	
	function DiscountPromotionExecutor(promotion) {
		PromotionExecutor.call(this, promotion);
		this.loadStrategy(new OrderPromotionStrategy(this));
		this.matchOrder = function(order) {
			var discount = getDiscountFromOrder(promotion, order);
			if(discount) {
				caculateAdjustedAmount(order, discount);
				if(!order.promotion_ids) order.promotion_ids = [];
				order.promotion_ids.push(this.promotionId);
				return true;
			}
			return false;
		};
	}
	
	function CeilingReductionPromotionExecutor(promotion) {
		PromotionExecutor.call(this, promotion);
		this.loadStrategy(new OrderPromotionStrategy(this));
		this.matchOrder = function(order) {
			var reduction = getReductionFromOrder(promotion, order);
			if(reduction) {
				order.adjusted_amount += parseFloat(reduction);
				if(!order.promotion_ids) order.promotion_ids = [];
				order.promotion_ids.push(this.promotionId);
				return true;
			}
			return false;
		};
	}
	
	function LinePromotionStrategy(executor) {
		this.promote = function(order) {
			for(var i=0; i<order.list.length; i++) {
				var line = order.list[i];
				var isMatch = executor.matchLine(line);
				executor.isUsed = isMatch || executor.isUsed;
				if(isMatch) {
					line.adjusted_amount = line.qty_ord * (line.adjusted_price ? line.adjusted_price : 0);
					if(!order.adjusted_amount) order.adjusted_amount = 0;
					order.adjusted_amount += line.adjusted_amount;
				}
			}
		};
	}
	
	function LinePointPromotionStrategy(executor) {
		this.promote = function(order) {
			for(var i=0; i<order.list.length; i++) {
				var line = order.list[i];
				var isMatch = executor.matchLine(line);
				executor.isUsed = isMatch || executor.isUsed;
				if(isMatch) {
					if(!order.adjusted_point) order.adjusted_point = 0;
					order.adjusted_point += line.adjusted_point;
				}
			}
		};
	}
	
	function OrderPromotionStrategy(executor) {
		this.promote = function(order) {
			executor.isUsed = executor.matchOrder(order) || executor.isUsed;
		};
	}
	

	/**
	 * 检查价格预警
	 * 卷烟商品销售价格上浮1.5倍或降低1.5倍时弹出预警
	 * 非烟商品销售价格上浮2倍或降低1/2时弹出预警
	 */
	function checkItemAmtOrd(item, val, ok, cancel) {
		
		if(!box.user.witems && box.user.wlist) {
			// 初始化不再提醒列表
			box.user.witems = {};
			for ( var index in box.user.wlist) {
				box.user.witems[box.user.wlist[index]] = true;
			}
		}
		if(box.user.witems && box.user.witems[item.item_id]) {
			// 已经加入到不在提醒列表
			if(ok) ok();
			return false;
		}
		
		var amt = item.big_pri4 * item.qty_ord;
		if(amt) {
			var isWarn = false;
			if(item.item_kind_id == "01") {
				if(val > amt * 1.5 || val < amt / 1.5) {
					// 卷烟销售异常
					isWarn = true;
				}
			} else {
				if(val > amt * 2 || val < amt / 2 ) {
					// 非烟销售异常
					isWarn = true;
				}
			}
			if(isWarn) {
				var ht = '<input type="checkbox" id="spxsYcyjCheckbox">&nbsp;&nbsp;<label for="spxsYcyjCheckbox">不再提示该商品</label>';
				box.showConfirm({message: "您输入的商品金额超出异常预警，是否确定修改？", 
					title: "警告",
					extendView: ht,
					ok: function(confirm) {
						var checkbox = confirm.find("#spxsYcyjCheckbox");
						if(checkbox[0] && checkbox[0].checked) {
							// 将商品记录到不再提醒库中
							box.request({
								url: box.getContextPath() + "retail/item/insertMerchWarningItem",
								data: {params: $.obj2str([item.item_id])},
								showLoading: false
							});
							if(!box.user.witems) box.user.witems = {};
							box.user.witems[item.item_id] = true;
						}
						if(ok) ok();
					},
					cancel: cancel
				});
				return isWarn;
			}
		}
		if(ok) ok();
		return false;
	}
	
	/**
	 * 检查实收金额预警
	 * 实收金额少于应收金额超过1元钱（含1元）时预警
	 * 实收金额超出应收金额100元（含100元）时预警
	 */
	function checkAmtOrd(ss, ys, ok, cancel) {
		var isWarn = false;
		if(ys - ss >= 1 || ss - ys >= 100 ) {
			isWarn = true;
		}
		if(isWarn) {
			box.showConfirm({message: "您输入实收金额超出异常预警，是否确定修改？", 
				title: "警告",
				ok: ok,
				cancel: cancel
			});
			return isWarn;
		}
		if(ok) ok();
		return false;
	}
	
	
	return {
		init: function(){
			box.listen("spxs1", loadSPXS);
			box.listen("spxs1_close", onClose);
			box.listen("spxs1_onkey",onKey);
		},
		destroy: function() { }
	};
});
