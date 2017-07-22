/**
 * 便门服务--数字电视缴费
 */
HOME.Core.register("plugin-zzfw", function(box) {
	
	var TVSearchURL = "/retail/pay/search";
	var payTVFee = "/retail/pay/pay";
	var phonePayFee="retail/pay/phone";
	var payPhoneByBind = "retail/pay/phone/bind";
	var merchCardInfoURL="retail/pay/card";
	var phoneSearchURL="http://www.ruishangtong.com:8889/tobacco/pub/mobile/getMobileInfo";
	var paymerchManualUrl ="/retail/pay/merchManual/";
	var queryCreditCardUrl = "/retail/finance/queryCreditCard";
	var repayCreditCardUrl = "/retail/finance/repayCreditCard";
	
	var content = null;
	var parentView = null;
	// 绑定卡信息
	var bindingCards = null;
	
	var detailMSG = "<lable style='color:#ff5828;'>注意事项：</lable><br>1、请确认POS与电脑正确连接。<br>2、请确实POS刷卡充值时处于默认界面。";
	var detailPhoneMSG = "<lable style='color:#ff5828;'>注意事项：</lable><br>1、请确认POS与电脑正确连接。<br>2、请确实POS刷卡充值时处于默认界面。<br><lable style='color:red;'>3、手机充值暂不支持IC卡。</lable>";
	var hasMac = "0";
	
	function onload(view){
		
		parentView = view;
		content = box.ich.view_zzfw();
		parentView.empty().append(content);
		
		getBindingCards();
		
		var panelTitles = content.find("#left_view");
		// 手机充值
		panelTitles.find("#pay_sjcz").unbind("click").click(onPanelTitleClick(showDHCZ()));
		// 电话缴费
		panelTitles.find("#pay_dhjf").unbind("click").click(onPanelTitleClick(dhjf));
		// 用电缴费
		panelTitles.find("#pay_ydjf").unbind("click").click(onPanelTitleClick(ydjf));
		// 有线缴费
		panelTitles.find("#pay_yxjf").unbind("click").click(onPanelTitleClick(yxjf));
		// 燃气缴费
		panelTitles.find("#pay_rqjf").unbind("click").click(onPanelTitleClick(rqjf));
		// 信用卡还款
		panelTitles.find("#xykhk").unbind("click").click(onPanelTitleClick(xykhk));
		// QQ充值
		panelTitles.find("#qqcz").unbind("click").click(onPanelTitleClick(qqcz));
		
		// 默认选中手机充值
		panelTitles.find("#pay_sjcz").click();
		
	}
	
	// 查询此商户绑定卡信息
	function getBindingCards(){
		box.request({
			url: box.getContextPath() + merchCardInfoURL,
			success: function(data) {
				var cards = data.result.cardno;
				if(cards && cards.length) {
					bindingCards = [];
					for(var index=0; index<cards.length; index++) {
						var card = cards[index].card;
						var maskedCardNo = card.substring(0,6) + "********" + card.substring(card.length-4, card.length);
						bindingCards.push({cardSecret: maskedCardNo});
					}
				}
			}
		});
	}
	
	// 在点击标题栏时 改变此标题栏的样式 并恢复其他同级标题栏的样式
	function onPanelTitleClick(callbackFn) {
		return function(e) {
			$(this).siblings("li").removeClass("liselected").removeClass("selected");
			$(this).addClass("liselected").addClass("selected");
			callbackFn(e);
		};
	}
	
	// QQ充值
	function qqcz(e) {
		var qqczPanel = box.ich.view_qqcz();
		qqczPanel.find("#qbcz_btn").unbind("click").click(onQbczClick(qqczPanel));
		qqczPanel.find("#qqby_btn").unbind("click").click(onQqbyClick(qqczPanel));
		content.find("#pay_container").empty().append(qqczPanel);
	}
	
	function onQbczClick(panel) {
		return function(e) {
			$(".qqcz_dialog").addClass("qqcz_hiden_dialog");
			$("#qb_number").removeClass("qqcz_hiden_dialog");
		};
	}
	
	function onQqbyClick(panel) {
		return function(e) {
			$(".qqcz_dialog").addClass("qqcz_hiden_dialog");
			$("#by_number").removeClass("qqcz_hiden_dialog");
		};
	}
	
	// 信用卡还款
	function xykhk(e) {
		var xykhkPanel = box.ich.view_xykhk();
		xykhkPanel.find("#query_button").unbind("click").click(onQueryClick(xykhkPanel));
		xykhkPanel.find("#repay_button").unbind("click").click(onRepayClick(xykhkPanel));
		content.find("#pay_container").empty().append(xykhkPanel);
	}
	
	// 查询信用卡信息
	function onQueryClick(xykhkPanel) {
		return function(e) {
			var me = $(this);
			var cardNumberInput = xykhkPanel.find("#card_num");
			var cellphoneInput = xykhkPanel.find("#cellphone");
			var paymentAmountInput = xykhkPanel.find("#repayment_amount");
			var repayButton = xykhkPanel.find("#repay_button");
			
			var cardNumber = cardNumberInput.val();
			box.request({
				url: box.getContextPath() + queryCreditCardUrl,
				data: {credit_card_number: cardNumber},
				success: function(data) {
					if(data.code="0000") {
						var result = data.result;
						xykhkPanel.find("#bank_name").text(result.issue_bank_name);
						xykhkPanel.find("#card_type").text(result.card_kind=="1" ? "信用卡": "储蓄卡");
						xykhkPanel.find("#charge").text($.parseMoney(result.charge ? result.charge : 0));
						xykhkPanel.find("#is_supported").text(result.repayment=="1" ? "支持" : "不支持");
						me.text("清除");
						me.unbind("click").click(onCleanClick(xykhkPanel));
						cardNumberInput.attr("disabled", "true");
						if(result.repayment=="1") {
							cellphoneInput.removeAttr("disabled");
							paymentAmountInput.removeAttr("disabled");
							repayButton.removeAttr("disabled");
						}
					}
				}
			});
		};
	}
	
	// 清除信用卡信息
	function onCleanClick(xykhkPanel) {
		return function(e) {
			var me = $(this);
			me.text("查询");
			me.unbind("click").click(onQueryClick(xykhkPanel));
			// 置空银行名称
			xykhkPanel.find("#bank_name").text("");
			// 置空卡片类型
			xykhkPanel.find("#card_type").text("");
			// 置空手续费
			xykhkPanel.find("#charge").text("");
			// 置空是否支持还款
			xykhkPanel.find("#is_supported").text("");
			// 置空卡号 并启用输入框
			var cardNumberInput = xykhkPanel.find("#card_num");
			cardNumberInput.removeAttr("disabled");
			cardNumberInput.val("");
			// 置空手机号码 并禁用输入框
			var cellphoneInput = xykhkPanel.find("#cellphone");
			cellphoneInput.attr("disabled", "true");
			cellphoneInput.val("");
			// 置空还款金额 并禁用输入框
			var repaymentAmountInput = xykhkPanel.find("#repayment_amount");
			repaymentAmountInput.attr("disabled", "true");
			repaymentAmountInput.val("");
			// 禁用还款按钮
			var repayButton = xykhkPanel.find("#repay_button");
			repayButton.attr("disabled", "true");
		};
	}
	
	
	function onRepayClick(xykhkPanel) {
		var repaymentAmountInput = xykhkPanel.find("#repayment_amount");
		var creditCardNumberInput = xykhkPanel.find("#card_num");
		var cellphoneNumberInput = xykhkPanel.find("#cellphone");
		return function(e) {
			var repaymentAmount = $.parseMoney(repaymentAmountInput.val());
			var creditCardNumber = creditCardNumberInput.val();
			var cellphoneNumber = cellphoneNumberInput.val();
			onSwip(repaymentAmount, creditCardNumber, cellphoneNumber, xykhkPanel);
		};
	}
	

	function onSwip(repaymentAmount, creditCardNumber, cellphoneNumber, xykhkPanel){
		var amountString = fittingPayFee(repaymentAmount);
		var fieldMAB = {fieldTrancode:"400000005", field3:"740000", field4:amountString, field11:"", field25:"00", field53:"2600000000000000", termMobile:"", ReaderID:""};
		box.showMask({
			message: "请在POS机上刷卡",
			detailMSG: detailMSG,
			hiddenButton:{
				hidden: false,
				val: "重新刷卡",
				callback: function() {
					box.hideMask();
					onSwip(repaymentAmount, creditCardNumber, cellphoneNumber, xykhkPanel);
					box.showAlert({message:"请重新刷卡"});
				}
			},
			cancelButton:{
				callback: function(){
					box.hideMask();
				}
			},
			feeContent:{"hidden":false, val:repaymentAmount}
		});
		box.com.readCardPwd(hasMac, repaymentAmount, fieldMAB, onPOSCallback(repaymentAmount, creditCardNumber, cellphoneNumber, xykhkPanel));
	}
	

	function onPOSCallback(repaymentAmount, creditCardNumber, cellphoneNumber, xykhkPanel) {
		return function(result) {
			var flag = true;
			var message = "";
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
			box.hideMask();
			var reqData = {
				"field2": result.primary_account_number,
				"field52": result.pin_data,
				"field3": "740000", // 原来默认是6个0
				"track_2_data": result.track_2_data,
				"track_3_data": result.track_3_data,
				"fieldMAB": result.fieldMAB,
				"fieldMAC": result.fieldMAC,
				"field41": result.field41,
				"field42": result.field42,
				"field55": result.field55
			};
			reqData.fieldMAB = result.fieldMAB || "";
			reqData.fieldMAC = result.fieldMAC || "";
			// 还款金额
			reqData.repayment_amount = repaymentAmount;
			// 卡号
			reqData.credit_card_number = creditCardNumber;
			// 手机号
			reqData.cellphone_number = cellphoneNumber;
			box.request({
				dataType: "json",
				url: box.getContextPath() + repayCreditCardUrl,
				data: {params: $.obj2str(reqData)},
				success: function(data){
					if(data.code=="0000"){
						if(data.result && data.result.field39 == "00") {
							// 信用卡还款成功
							box.showAlert({message:"信用卡还款成功"});
							try {
								alert(data.result);
								// 清空按钮
								$("#query_button").click();
							} catch (e) {
								box.showAlert({message:"打印小票出错"});
							}
						} else {
							box.showAlert({message:"信用卡还款失败，fieldMessage:"+data.reult.fieldmessage+"，field39:" + data.result.field39});
						}
					} else {
						box.showAlert({message:"信用卡还款失败，" + data.msg});
					}
				},
				error: function(data, reqStatus) {
					if(data && data.code) {
						box.showAlert({message:"信用卡还款失败，" + data.msg});
					} else if(reqStatus){
						box.showAlert({message:"信用卡还款失败，请求状态：" + reqStatus});
					} else {
						box.showAlert({message:"信用卡还款失败：" + data});
					}
				}
			});
		};
	};
	
	
	/**
	 * 燃气缴费
	 */
	function rqjf(){
		var rep = "";
		var field3 = "Ext_QueryRQ";
		var pay_no = "";
		var field4 = "";//金额
		//燃气缴费查询
		var rqSearch = function(result){
			var req = {
				"field3":field3,
				"pay_no":pay_no
				//"field41":result.field41,
				//"field42":result.field42,
				//"fieldMAC":result.fieldMAC,
				//"fieldMAB":result.fieldMAB
			};
			box.request({
				dataType:"json",
				url: box.getContextPath()+TVSearchURL,
				data: req,
				success:function(data){
					$("#rqSearchButton").attr('disabled',false);
					if( data.result && data.result.field39 == "00"){
						rep = JSON.stringify(data.result);
						$("#Num").empty().append(data.result.pay_no);
						$("#UserName").empty().append(data.result.user_name);
						$("#InstallAddr").empty().append(data.result.address);
						$("#PayableTotal").empty().append(data.result.must_pay_sum_amt);
						$("#AccountBalance").empty().append(data.result.last_bal);
					}
					else{
						if(data.result && data.result.fieldmessage){
							box.showAlert({message:data.result.fieldmessage});
						}
						else{
							box.showAlert({message:"未查询到用户信息 ！"});
						}
					}
				
				},
				error:function(data){
					$("#rqSearchButton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
			
		};
		//燃气缴费
		var rqPay = function(result){
			var flag = true;
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
			box.hideMask();
			var cardNo = result.primary_account_number;
			var password = result.pin_data;
			var reqData = {
					"field2":result.primary_account_number,
					"field4":field4,
					"field52":result.pin_data,
					"responseData":rep,
					"field3":field3,
					"track_2_data":result.track_2_data,
					"track_3_data":result.track_3_data,
					"fieldMAB":result.fieldMAB,
					"fieldMAC":result.fieldMAC,
					"field41":result.field41,
					"field42":result.field42,
					"field55":result.field55,
					"responseData":rep
				};
			box.request({
				dataType:"json",
				url: box.getContextPath()+payTVFee,
				data: reqData,
				success:function(data){
					payContent.find("#payRQButton").attr('disabled',false);
					if(data.result.field39 == "00"){
						clear(payContent);
						rep = "";
						payContent.find("#rqNum").val("");
						 $("#payMoney").val("");
						 data.result.issuer="";data.result.acquirer="";
						printTicket_(data.result,"燃气缴费",pay_no);
					}
					payFeeInfo(data);
				},
				error:function(data){
					$("#payRQButton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
		};
		var payContent = box.ich.view_rqjf();
		removeSpecialChar(payContent);
		payContent.find("#rqSearchButton").click(function(){
			pay_no = payContent.find("#rqNum").val();
			if(pay_no == ""){
				box.showAlert({message:"请输入燃气编号"});
				return;
			}
			payContent.find("#rqSearchButton").attr('disabled',true);
			var  fieldMAB={"pay_no":pay_no,"field3":field3,"fieldMAB":"pay_no;field3;field41;field42"};
			rqSearch();
		});
		payContent.find("#payRQButton").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payMoney").val();
			if(field4==""){
				$("#payMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
			}
			payContent.find("#payRQButton").attr('disabled',true);
			var field4_ = field4;
			field4  = fittingPayFee(field4);
			var fieldMAB = {fieldTrancode:"300000002",field3:"Ext_Fee",field4:field4,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
			box.showMask({
				message: "请在POS机上刷卡",
				detailMSG:detailMSG,
				hiddenButton:{"hidden":false,"val":"重新刷卡","callback":function(){
					box.showAlert({message:"请刷卡！"});
					box.com.readCardPwd(hasMac,field4,fieldMAB,rqPay);
				}},
				cancelButton:{"callback":function(){
					payContent.find("#payRQButton").attr('disabled',false);
					box.hideMask();
				}},
				feeContent:{"hidden":false,val:field4_}
			});
			box.com.readCardPwd(hasMac,field4_,fieldMAB,rqPay);
		});
		payContent.find("#payRQButtonByBind").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payMoney").val();
			if(field4==""){
				$("#payMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
			}
			payByMerchBindCard(rep,field4,field3,payContent,pay_no);
		});
		if(bindingCards.length == 0){
			payContent.find("#payRQButtonByBind").attr('disabled',true);
		}
		content.find("#pay_container").empty().append(payContent);
	}
	/**
	 * 有线数字缴费
	 */
	function yxjf(){
		var rep = "";
		var field3 = "Ext_QueryYX";
		var pay_no = "";
		var field4 = "";//金额
		var payMoney__ = "";
		//有线查询
		var yxSearch = function(result){
			var req = {
				"field3":field3,
				"pay_no":pay_no
				//"field41":result.field41,
				//"field42":result.field42,
				//"fieldMAC":result.fieldMAC,
				//"fieldMAB":result.fieldMAB
			};
			box.request({
				dataType:"json",
				url: box.getContextPath()+TVSearchURL,
				data: req,
				success:function(data){
					$("#searchYXbutton").attr('disabled',false);
					if( data.result && data.result.field39 == "00"){
						rep = JSON.stringify(data.result);
						$("#Num").empty().append(data.result.pay_no);
						$("#UserName").empty().append(data.result.user_name);
						$("#InstallAddr").empty().append(data.result.address);
						$("#PayableTotal").empty().append(data.result.must_pay_sum_amt);
						$("#AccountBalance").empty().append(data.result.last_bal);
					}
					else{
						if(data.result && data.result.fieldmessage){
							box.showAlert({message:data.result.fieldmessage});
						}
						else{
							box.showAlert({message:"未查询到用户信息 ！"});
						}
					}
				
				},
				error:function(data){
					$("#searchYXbutton").attr('disabled',false);
					errorInfo(data);
				}
			
			});
			
			
		};
		//有线缴费
		var yxPay = function(result){
			var flag = true;
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
			box.hideMask();
			var cardNo = result.primary_account_number;
			var password = result.pin_data;
			var reqData = {
					"field2":result.primary_account_number,
					"field4":payMoney__,
					"field52":result.pin_data,
					"responseData":rep,
					"field3":field3,
					"track_2_data":result.track_2_data,
					"track_3_data":result.track_3_data,
					"fieldMAB":result.fieldMAB,
					"fieldMAC":result.fieldMAC,
					"field41":result.field41,
					"field55":result.field55,
					"field42":result.field42
				};
			box.request({
				dataType:"json",
				url: box.getContextPath()+payTVFee,
				data: reqData,
				success:function(data){
					payContent.find("#payYXButton").attr('disabled',false);
					if(data.result.field39 == "00"){
						clear(payContent);
						rep = "";
					    $("#payYXMoney").val("");
						payContent.find("#yxNum").val("");
						data.result.issuer="";data.result.acquirer="";
						printTicket_(data.result,"有线缴费",pay_no);
					}
					payFeeInfo(data);
				},
				error:function(data){
					$("#payYXButton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
		};
		var payContent = box.ich.view_yxjf();
		removeSpecialChar(payContent);
		payContent.find("#searchYXbutton").click(function(){
			pay_no = payContent.find("#yxNum").val();
			if(pay_no == ""){
				box.showAlert({message:"请输入有线编号"});
				return;
			}
			payContent.find("#searchYXbutton").attr('disabled',true);
			var  fieldMAB={"pay_no":pay_no,"field3":field3,"fieldMAB":"pay_no;field3;field41;field42"};
			yxSearch();
		});
		payContent.find("#payYXButton").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payYXMoney").val();
			if(field4==""){
				$("#payYXMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			payContent.find("#payYXButton").attr('disabled',true);
			payMoney__ = fittingPayFee(field4);
			var fieldMAB = {fieldTrancode:"300000002",field3:"Ext_Fee",field4:payMoney__,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
			box.showMask({
				message: "请在POS机上刷卡",
				detailMSG:detailMSG,
				hiddenButton:{"hidden":false,"val":"重新刷卡","callback":function(){
					box.showAlert({message:"请刷卡！"});
					box.com.readCardPwd(hasMac,field4,fieldMAB,yxPay);
				}},
				cancelButton:{"callback":function(){
					payContent.find("#payYXButton").attr('disabled',false);
					box.hideMask();
				}},
				feeContent:{"hidden":false,val:field4}
			});
			box.com.readCardPwd(hasMac,field4,fieldMAB,yxPay);
		});
		//输入银行卡密码按钮
		payContent.find("#payYXButtonByBind").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payYXMoney").val();
			if(field4==""){
				$("#payYXMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			payByMerchBindCard(rep,field4,field3,payContent,pay_no);
		});
		if(bindingCards.length == 0){
			payContent.find("#payYXButtonByBind").attr('disabled',true);
		}
		content.find("#pay_container").empty().append(payContent);
	}
	
	
	/**
	 * 用电缴费
	 */
	function ydjf(){
		var rep = "";
		var field3 = "Ext_QueryDF";
		var pay_no = "";
		var field4 = "";//金额
		var oper_type = "";
		//用电查询
		var ydSearch = function(result){
			var req = {
				"field3":field3,
				"pay_no":pay_no,
				"oper_type":oper_type
				//"field41":result.field41,
				//"field42":result.field42,
				//"fieldMAC":result.fieldMAC,
				//"fieldMAB":result.fieldMAB,
				//
			};
			box.request({
				dataType:"json",
				url: box.getContextPath()+TVSearchURL,
				data: req,
				success:function(data){
					$("#searchYDButton").attr('disabled',false);
					if( data.result && data.result.field39 == "00"){
						rep = JSON.stringify(data.result);
						$("#Num").empty().append(data.result.pay_no);
						$("#UserName").empty().append(data.result.user_name);
						$("#InstallAddr").empty().append(data.result.address);
						$("#PayableTotal").empty().append(data.result.must_pay_sum_amt);
						$("#AccountBalance").empty().append(data.result.last_bal);
					}
					else{
						if(data.result && data.result.fieldmessage){
							box.showAlert({message:data.result.fieldmessage});
						}
						else{
							box.showAlert({message:"未查询到用户信息 ！"});
						}
					}
				
				},
				error:function(data){
					$("#searchYDButton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
			
		};
		//用电缴费
		var ydPay = function(result){
			var flag = true;
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
			box.hideMask();
			var reqData = {
					"field2":result.primary_account_number,
					"field4":field4,
					"field52":result.pin_data,
					"responseData":rep,
					"field3":field3,
					"track_2_data":result.track_2_data,
					"track_3_data":result.track_3_data,
					"fieldMAB":result.fieldMAB,
					"fieldMAC":result.fieldMAC,
					"field41":result.field41,
					"field55":result.field55,
					"field42":result.field42
				};
			box.request({
				dataType:"json",
				url: box.getContextPath()+payTVFee,
				data: reqData,
				success:function(data){
					if(data.result.field39 == "00"){
						clear(payContent);
						rep = "";
						$("#payYDMoney").val("");
						payContent.find("#ydNum").val("");
						data.result.issuer="";data.result.acquirer="";
						printTicket_(data.result,"用电缴费",pay_no);
					}
					payContent.find("#payYDbutton").attr('disabled',false);
					payFeeInfo(data);
				},
				error:function(data){
					$("#payYDbutton").attr('disabled',false);
					errorInfo(data);
				}
			
			});
			
		};
		var payContent = box.ich.view_ydjf();
		removeSpecialChar(payContent);
		payContent.find("#searchYDButton").click(function(){
			pay_no = payContent.find("#ydNum").val();
			if(pay_no == ""){
				box.showAlert({message:"请输入用户编号"});
				return;
			}
			oper_type = payContent.find("#oper_type").val();
			payContent.find("#searchYDButton").attr('disabled',true);
			var fieldMAB={"pay_no":pay_no,"field3":field3,"fieldMAB":"pay_no;field3;field41;field42"};
			ydSearch();
		});
		payContent.find("#payYDbutton").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = payContent.find("#payYDMoney").val();
			if(field4==""){
				$("#payYDMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			payContent.find("#payYDbutton").attr('disabled',true);
			var payMoney__  = field4;
			field4 = fittingPayFee(field4);
			var fieldMAB = {fieldTrancode:"300000002",field3:"Ext_Fee",field4:field4,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
			box.showMask({
				message: "请在POS机上刷卡",
				detailMSG:detailMSG,
				hiddenButton:{"hidden":false,"val":"重新刷卡","callback":function(){
					box.showAlert({message:"请刷卡！"});
					box.com.readCardPwd(hasMac,payMoney__,fieldMAB,ydPay);
				}},
				cancelButton:{"callback":function(){
					payContent.find("#payYDbutton").attr('disabled',false);
					box.hideMask();
				}},
				feeContent:{"hidden":false,val:payMoney__}
			});
			box.com.readCardPwd(hasMac,payMoney__,fieldMAB,ydPay);
		});
		payContent.find("#payYDbuttonByBind").click(function(){
			if(rep == ""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = payContent.find("#payYDMoney").val();
			if(field4==""){
				$("#payYDMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			payByMerchBindCard(rep,field4,field3,payContent,pay_no);
		});
		if(bindingCards.length == 0){
			payContent.find("#payYDbuttonByBind").attr('disabled',true);
		}
		content.find("#pay_container").empty().append(payContent);
	}
	/**
	 *电话缴费
	 */
	function dhjf(){
		var rep = "";
		var pay_no = "";
		var field3 = "Ext_QueryWT";
		var field4 = "";//金额
		var oper_type = "GSM";
		var area_no = "0531";
		var payMoney__ = "";
		//电话查询
		var dhSearch = function(result){
			var req = {
				"field3":field3,
				"pay_no":pay_no,
				"oper_type":oper_type,
				"area_no":area_no
				//"field41":result.field41,
				//"field42":result.field42,
				//"fieldMAC":result.fieldMAC,
				//"fieldMAB":result.fieldMAB,
				
			};
			box.request({
				dataType:"json",
				url: box.getContextPath()+TVSearchURL,
				data: req,
				success:function(data){
					$(".searchDHButton").attr('disabled',false);
					if( data.result && data.result.field39 == "00"){
						rep = JSON.stringify(data.result);
						$("#Num").empty().append(data.result.pay_no);
						$("#UserName").empty().append(data.result.user_name);
						$("#InstallAddr").empty().append(data.result.address);
						$("#PayableTotal").empty().append(data.result.must_pay_sum_amt);
						$("#AccountBalance").empty().append(data.result.last_bal);
					}
					else{
						if(data.result && data.result.fieldmessage){
							box.showAlert({message:data.result.fieldmessage});
						}
						else{
							box.showAlert({message:"未查询到用户信息 ！"});
						}
					}
				
				},
				error:function(data){
					$(".searchDHButton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
			
		};
		//电话缴费
		var dhPay = function(result){
			var flag = true;
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
			box.hideMask();
			var cardNo = result.primary_account_number;
			var password = result.pin_data;
			var reqData = {
					"field2":result.primary_account_number,
					"field4":payMoney__,
					"field52":result.pin_data,
					"responseData":rep,
					"field3":field3,
					"track_2_data":result.track_2_data,
					"track_3_data":result.track_3_data,
					"fieldMAB":result.fieldMAB,
					"fieldMAC":result.fieldMAC,
					"field41":result.field41,
					"field42":result.field42,
					"field55":result.field55
				};
			box.request({
				dataType:"json",
				url: box.getContextPath()+payTVFee,
				data: reqData,
				success:function(data){
					if(data.result.field39 == "00"){
						$("#payDHMoney").val("");
						clear(payContent);
						rep = "";
						if(oper_type == "GSM"){
							payContent.find("#dhSJNum").val("");
						}
						else{
							payContent.find("#dhZJNum").val("");
							payContent.find("#area_no").val("0531");
						}
						data.result.issuer="";data.result.acquirer="";
						var area_num = "";
						if(area_no && area_no != "" && pay_no.length != 11){
							area_num = area_no+"-"+pay_no;
						}
						else{
							area_num = pay_no;
						}
						printTicket_(data.result,"电话缴费",area_num);
					}
					payContent.find("#payDHbutton").attr('disabled',false);
					payFeeInfo(data);
				},
				error:function(data){
					payContent.find(".payDHbutton").attr('disabled',false);
					errorInfo(data);
				}
			});
			
		};
		var payContent = box.ich.view_dhjf();
		removeSpecialChar(payContent);
		payContent.find(".searchDHButton").click(function(){
			if(oper_type == "GSM"){
				pay_no = payContent.find("#dhSJNum").val();
				var numFormat=/^(130|131|132|145|155|156|185|186)\d{8}$/;
				if(!numFormat.exec(pay_no)){
					box.showAlert({message:"请输入正确的联通手机号"});
					return;
				}
			}
			else{
				pay_no = payContent.find("#dhZJNum").val();
				area_no =  payContent.find("#area_no").val();
				if(pay_no == "" || area_no==""){
					box.showAlert({message:"请输入正确的座机号"});
					return ;
				}
			}
			payContent.find(".searchDHButton").attr('disabled',true);
			var  fieldMAB={"pay_no":pay_no,"field3":field3,"fieldMAB":"pay_no;field3;field41;field42"};
			//box.com.getMac(fieldMAB,dhSearch);
			dhSearch();
		});
		payContent.find("#payDHbutton").click(function(){
			if(rep==""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payDHMoney").val();
			if(field4==""){
				$("#payDHMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			payContent.find("#payDHbutton").attr('disabled',true);
			payMoney__ = fittingPayFee(field4);
			var fieldMAB = {fieldTrancode:"300000002",field3:"Ext_Fee",field4:payMoney__,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
			box.showMask({
				message: "请在POS机上刷卡",
				detailMSG:detailMSG,
				hiddenButton:{"hidden":false,"val":"重新刷卡","callback":function(){
					box.showAlert({message:"请刷卡！"});
					box.com.readCardPwd(hasMac,field4,fieldMAB,dhPay);
				}},
				cancelButton:{"callback":function(){
					payContent.find("#payDHbutton").attr('disabled',false);
					box.hideMask();
				}},
				feeContent:{"hidden":false,val:field4}
			});
			box.com.readCardPwd(hasMac,field4,fieldMAB,dhPay);
		});
		payContent.find('input:radio[name="oper_type"]').click(function() {
			$("#Num").empty();$("#UserName").empty();$("#InstallAddr").empty();$("#PayableTotal").empty();$("#AccountBalance").empty();
			payContent.find(".searchDHButton").attr('disabled',false);
			rep="";
			var $selectedvalue =  $('input:radio[name="oper_type"]:checked').val();;
			if ($selectedvalue == "1"){
				//联通手机
				payContent.find("#dh_ltphone").find("#dhSJNum").val("");
				payContent.find("#dh_ltphone").show();
				payContent.find("#dh_dhzj").hide();
				oper_type = "GSM";
				field3 = "Ext_QueryWT";
			} else if ($selectedvalue == "2" || $selectedvalue == "3") {
				//网通座机
				payContent.find("#dh_ltphone").hide();
				payContent.find("#dh_dhzj").find("#dhZJNum").val("");
				payContent.find("#dh_dhzj").find("#area_no").val("0531");
				payContent.find("#dh_dhzj").show();
				if($selectedvalue == "2"){
					field3 = "Ext_QueryWT";
				}
				else{
					field3 = "Ext_QueryDX";
				}
				oper_type = "";
			} 
		});
		payContent.find("#payDHbuttonByBind").click(function(){
			if(rep==""){
				box.showAlert({message:"请先查询用户信息！"});
				return;
			}
			field4 = $("#payDHMoney").val();
			if(field4==""){
				$("#payDHMoney").addClass("validate");
				box.showAlert({message:"请输入缴费金额!"});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*(\.[0-9]{1,2})?$/;
				if(!feeFormat.test(field4) || 0==parseFloat(field4)){
					box.showAlert({message:"缴费金额格式不正确！"});
					return ;
				}
				var responseData = JSON.parse(rep);
				if(parseFloat(field4) < parseFloat(responseData.must_pay_sum_amt)){
					box.showAlert({message:"缴费金额必须大于欠费金额！"});
					return ;
				}
			}
			var area_num = "";
			if(area_no && area_no != ""){
				area_num = area_no+"-"+pay_no;
			}
			else{
				area_num = pay_no;
			}
			payByMerchBindCard(rep,field4,field3,payContent,area_num);
		});
		if(bindingCards.length == 0){
			payContent.find("#payDHbuttonByBind").attr('disabled',true);
		}
		content.find("#pay_container").empty().append(payContent);
	}
	/**
	 * 手机充值
	*/
	function showDHCZ(){
		
		var payContent = box.ich.view_sjcz();
		var payFromCard = function(){
			var payMoney = $("#payMoney").text();
			var phoneNum = $("#phoneNum").val();
			var messageList = [];
			if(!$.validateForms(payContent, messageList)) {
				box.showAlert({message: messageList[0]});
				return;
			}
			/*
			//联通 130、131、132、145、155、156、185、186
			var numFormat=/^(130|131|132|145|155|156|185|186)\d{8}$/;
			//电信  133、1349、153、180、181、189
			var dxFormat = /^(133|134|153|180|181|189)\d{8}$/;
			//移动 1340-1348、135、136、137、138、139、147、150、151、152、157、158、159、182、183、184、187、188
			var ydFormat = /^(135|136|137|138|139|147|150|151|152|157|158|159|182|183|184|187|188)\d{8}$/;
			if(!(numFormat.exec(phoneNum) || dxFormat.exec(phoneNum) || ydFormat.exec(phoneNum))){
				box.showAlert({message:"请输入正确的手机号码"});
				return;
			}*/
			var numFormat = /^[1][3-8]\d{9}$/;
			if(!(numFormat.exec(phoneNum))){
				box.showAlert({message:"请输入正确的手机号码"});
				return;
			}

			if(payMoney==""){
				var msg = $("#payMoney").attr("verify-message");
				$("#payMoney").addClass("validate");
				box.showAlert({message:msg});
				return ;
			}
			else{
				var feeFormat = /^[0-9]*$/;
				if(!feeFormat.test(payMoney)){
					box.showAlert({message:"请输入正确的金额！"});
					return ;
				}
			}
			var searchPhoneCallBack = function(data){
				var mobiletype = "无信息";
				var mobilearea = "无信息";
				if(data.code == "0000" && data.result.flag=="1"){
					mobiletype = data.result.mobiletype;
					mobilearea = data.result.mobilearea;
				}
				var list = data.result.list;
				var flag = false;
				var warn = "";
				for(var i=0 ; i<list.length;i++){
					if(payMoney == list[i]){
						flag = true;
						break;
					}
					else{
						warn += list[i];
						if(i+1<list.length){
							warn += "、";
						}
					}
				}
				if(!flag){
					box.showAlert({message:"请输入"+warn+"中一种金额"});
					$("#payPhoneButton").attr('disabled',false);
					return;
				}
				var sjcz_content = box.ich.view_showPayMsg_sjcz({"phoneNum":phoneNum,"payMoney":payMoney,"mobiletype":mobiletype,"mobilearea":mobilearea,"list":bindingCards});
				sjcz_content.find("#phonePayFee").unbind("click").click(function(){
					var payMoney_proto = fittingPayFee(payMoney);
					var fieldMAB = {fieldTrancode:"700000001",field3:"000000",field4:payMoney_proto,field11:"",field25:"00",field53:"2600000000000000",termMobile:"",ReaderID:""};
					var phonePay = function(result){
						var flag = true;
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
						box.closeDialog({content:sjcz_content});
						box.hideMask();
						var reqData = {
								"cardNo": result.primary_account_number,
								"payMoney":payMoney_proto,
								"password":result.pin_data,
								"phoneNum":phoneNum,
								"track_2_data":result.track_2_data,
								"track_3_data":result.track_3_data,
								"fieldMAB":result.fieldMAB,
								"fieldMAC":result.fieldMAC,
								"field41":result.field41,
								"field42":result.field42,
								"field55":result.field55
							};
							box.request({
								dataType:"json",
								url: box.getContextPath()+phonePayFee,
								data:reqData,
								success:function(data){
									payPhoneFeeInfo(data,phoneNum, payMoney);
									if(data.result && data.result.field39 == "00"){
										//充值完成后清除充值信息
										payContent.find("#phoneNum").val("");
										//默认选择充值100元
										payContent.find("#lable_first").unbind("click").click(function(){
											$(this).parent().find("label").removeClass("hoverBor");
											$(this).addClass("hoverBor");
											payContent.find("#other_price").val("");
											payMoney = $(this).text();
											payContent.find("#payMoney").text(payMoney);
										}).trigger("click");

									}
								},
								error:function(data){
									errorInfo(data);
								}
							});
					
					};
					box.showMask({
						message: "请在POS机上刷卡",
						detailMSG:detailPhoneMSG,
						hiddenButton:{"hidden":false,"val":"重新刷卡","callback":function(){
							box.showAlert({message:"请刷卡！"});
							box.com.readCardPwd(hasMac,payMoney,fieldMAB,phonePay);
						}},
						cancelButton:{"callback":function(){
							payContent.find("#payPhoneButton").attr('disabled',false);
							sjcz_content.find("#phonePayFee").attr('disabled',false);
							box.hideMask();
						 }},
						 feeContent:{"hidden":false,val:payMoney}
					});
					box.com.readCardPwd(hasMac,payMoney,fieldMAB,phonePay);
				});
				if(bindingCards.length == 0){
					sjcz_content.find("#phonePayFeeByBind").attr("disabled",true);
					sjcz_content.find("#password").attr("disabled",true);
					sjcz_content.find("#bindCardSpan").hide();
				}
				else{
					sjcz_content.find("#nobindCardSpan").hide();
				}
				sjcz_content.find("#phonePayFeeByBind").click(function(e){
					var password = sjcz_content.find("#password").val();
					var card__ = sjcz_content.find("#bindCardSpanSelect").val();
					if(password == ""){
						sjcz_content.find("#phonePayFeeByBind").removeAttr("disabled");
						sjcz_content.find("#password").focus();
						box.showAlert({message:"请输入密码"});
						box.stopEvent(e);
						return;
					}
					var password_ = desEnc(password);
					var payMoneyBind = fittingPayFee(payMoney);
					box.request({
						dataType:"json",
						url: box.getContextPath()+payPhoneByBind,
						data: {"cardNo":card__,"payMoney":payMoneyBind,"password":password_,"phoneNum":phoneNum},
						success:function(data){
							sjcz_content.find("#phonePayFeeByBind").attr("disabled",false);
							payPhoneFeeInfo(data,phoneNum,payMoney);
							if(data.result && data.result.field39 == "00"){
								box.controls.printPhonePaymentTicket(data.result, phoneNum, payMoney);
								box.closeDialog({content:sjcz_content});
								payContent.find("#phoneNum").val("");
								//默认选择充值100元
								payContent.find("#lable_first").unbind("click").click(function(){
									$(this).parent().find("label").removeClass("hoverBor");
									$(this).addClass("hoverBor");
									payContent.find("#other_price").val("");
									payMoney = $(this).text();
									payContent.find("#payMoney").text(payMoney);
								}).trigger("click");
								//payContent.find("#payMoney").val("");
							}
						},
						error:function(data){
							sjcz_content.find("#phonePayFeeByBind").attr("disabled",false);
							errorInfo(data);
						}
					});
				});
				box.showDialog({
					title:"充值信息",
					width:470,
					height:360,
					content: sjcz_content,
					close:function(){
						$("#payPhoneButton").attr('disabled',false);
					}
				});
			};
			//查询手机信息
			box.request({
				dataType:"jsonp",
				url:phoneSearchURL,
				//type:"jsonp",
				data: {"mobile":phoneNum},
				success:function(data){
					searchPhoneCallBack(data);
				}
			});
		};
		return function(e){
			payContent = box.ich.view_sjcz();
			
			content.find("#pay_container").empty().append(payContent);
			payContent = content.find("#pay_container").find("#view_sjcz");
			
			removeSpecialChar(payContent);
			
			payContent.find("#phoneNum").val("");
			payContent.find("#other_price").val("");
			payContent.find("#payPhoneByCard").unbind("click").click(payFromCard);
			
			//默认选择充值100元
			payContent.find("#lable_first").unbind("click").click(function(){
				$(this).parent().find("label").removeClass("hoverBor");
				$(this).addClass("hoverBor");
				payContent.find("#other_price").val("");
				payMoney = $(this).text();
				payContent.find("#payMoney").text(payMoney);
			}).trigger("click");
			
			//选择100、50、30、20块
			payContent.find(".payje label").unbind("click").click(function(){
				$(this).parent().find("label").removeClass("hoverBor");
				$(this).addClass("hoverBor");
				payContent.find("#other_price").val("");
				var price = $(this).attr("data-val");
				payContent.find("#payMoney").text(price);
			});
			
			//下拉列表选择其他值
			payContent.find("#other_price").unbind("change").change(function(){
				payContent.find(".payje label").removeClass("hoverBor");
				var price = payContent.find("#other_price").val();
				payContent.find("#payMoney").text(price);
			});
		};

	}
	 
	/**
	 * 查询回调数据  原始缴费金额  类型
	 */
	function payByMerchBindCard(searcResponseData,payFee,feeType,content,pay_no){
		var manualCardPasswordDialogContent = box.ich.view_payByManualCardPasswordDialog({payFee:payFee});
		var manualCardPasswordDialog  = box.showDialog({
			title:"缴费信息",
			width:400,
			height:280,
			content: manualCardPasswordDialogContent
		});
		manualCardPasswordDialogContent.find("#manuaPayButton").click(function(e){
			var cardNum = manualCardPasswordDialogContent.find("#manualCardNum").val();
			var password = manualCardPasswordDialogContent.find("#manuaPassword").val();
			if(cardNum == "" || !(/^[0-9]*$/.test(cardNum))){
				box.showAlert({message:"卡号不正确"});
				manualCardPasswordDialogContent.find("#manuaPayButton").attr("disabled",false);
				manualCardPasswordDialogContent.find("#manualCardNum").focus();
				box.stopEvent(e);
				return ;
			}
			if(password == ""){
				manualCardPasswordDialogContent.find("#manuaPayButton").attr("disabled",false);
				manualCardPasswordDialogContent.find("#manuaPassword").focus();
				box.showAlert({message:"请输入密码"});
				box.stopEvent(e);
				return ;
			}
			var field4  = fittingPayFee(payFee);
			password = desEnc(password);
			box.request({
				url: box.getContextPath()+paymerchManualUrl,
				//type:"jsonp",
				data: {"cardNo":cardNum,"payMoney":field4,"password":password,"responseData":searcResponseData,"feeType":feeType},
				success:function(data){
					manualCardPasswordDialogContent.find("#manuaPayButton").attr("disabled",false);
					if(data.result.field39 == "00"){
						clear(content);
						data.result.merch_name = box.user.user_name;
						box.closeDialog({content:manualCardPasswordDialog});
						data.result.issuer="";data.result.acquirer="";
						if(feeType == "Ext_QueryRQ"){
							printTicket_(data.result,"燃气缴费",pay_no);
							
						}
						else if(feeType == "Ext_QueryYX"){
							printTicket_(data.result,"有线缴费",pay_no);
							
						}
						else if(feeType == "Ext_QueryDF"){
							printTicket_(data.result,"用电缴费",pay_no);
							
						}
						else if(feeType == "Ext_QueryDX"){
							printTicket_(data.result,"电话缴费",pay_no);
							
						}
						else if(feeType == "Ext_QueryWT"){
							printTicket_(data.result,"电话缴费",pay_no);
							
						}
						else if(feeType == "Ext_QueryQN"){
							printTicket_(data.result,"暖气缴费",pay_no);
						}
					}
					payFeeInfo(data);
				},
				error:function(data){
					manualCardPasswordDialogContent.find("#manuaPayButton").attr("disabled",false);
					box.stopEvent(e);
					errorInfo(data);
				}
			});
			
		});
		manualCardPasswordDialogContent.find("#manuaPayCancel").click(function(){
			box.closeDialog({content:manualCardPasswordDialog});
		});
		
	}
	function errorInfo(data){
		box.showAlert({message:data.msg});
	}
	/**
	 * 齐鲁银行缴费结果回调
	 */
	function payFeeInfo(data){

		if(data.result && data.result.field39 == "00"){
			var num = data.result.field2.substring(0,6)+"********"+data.result.field2.substring(data.result.field2.length-4,data.result.field2.length);
			data.result.field2 = num;
			var field13 = data.result.field13;
			var field12 = data.result.field12;
			var tranDate=new Date(field13.substr(0, 4),field13.substr(4, 2)-1,field13.substr(6, 2),
					field12.substr(0, 2),field12.substr(2, 2),field12.substr(4, 2));
			 data.result.formDate=tranDate.format("yyyy-MM-dd hh:mm:ss");
			
			var content_show=box.ich.view_showPayMsg_success({result:data.result});
			box.showDialog({
				title:data.result.fieldmessage,
				width:450,
				height:310,
				content: content_show
			});
			content_show.find("#closeDialog").click(function(){
				box.closeDialog({content:content_show});
			});
		}
		else{
			if(data.result && data.result.fieldmessage){
				var content_show=box.ich.view_showPayMsg_error({result:data.result});
				box.showDialog({
					title:"充值失败",
					width:150,
					height:100,
					content: content_show
				});
			}
			else{
				box.showAlert({message:data.msg});
			}
			
		}
	
	}
	/**
	 * 手机充值缴费结果
	 */
	function payPhoneFeeInfo(data,phoneNum,payMoney){
		if(data.result && data.result.field39 == "00"){
			var content_show=box.ich.view_showPayPhoneMsg_success({"phoneNum":phoneNum,"payMoney":payMoney});
			box.showDialog({
				title:"充值成功",
				width:150,
				height:200,
				content: content_show
			});
			try {
				data.result.issuer=data.result.field44.split("   ")[0];data.result.acquirer=data.result.field44.split("   ")[1];
			} catch (e) {
				data.result.issuer="";
				data.result.acquirer="";
			}
			data.result.field4 = parseFloat(data.result.field4)/100;
			data.result.merch_name = box.user.user_name ;
			printTicket_(data.result,"手机充值",phoneNum);
		}
		else{
			if(data.result && data.result.fieldmessage){
				var content_show=box.ich.view_showPayMsg_error({result:data.result});
				box.showDialog({
					title:"缴费结果",
					width:150,
					height:100,
					content: content_show
				});
			}
			else{
				box.showAlert({message:data.msg});
			}
			
		}
	
	}
	/**
	 * 组装金额信息
	 */
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
	function removeSpecialChar(payContent){
		payContent.find(".num").unbind('keyup').keyup(function(){     
			var tmptxt=$(this).val();     
			$(this).val(tmptxt.replace(/[^0-9]/g,''));     
		}).unbind('paste').bind("paste",function(){     
			var tmptxt=$(this).val();     
			$(this).val(tmptxt.replace(/[^0-9]/g,''));     
		}).css("ime-mode", "disabled");

	}
	
	function printTicket_(data,type,payNum){
		//var data = {"acct_bal":"","pay_op_name":"隋长国","field60":"22000074","field62":"","field39":"00","field4":"0.01","field44":"","field3":"S","field2":"6223795310110223389","field42":"409489853319003","field49":"156","merch_name":"历下区军星商贸中心","fieldMAC":"3535414645354444","field41":"90000003","fieldMAB":"fieldTrancode;messtype;field2;field3;field4;field11;field13;field32;field38;field39;field41;field42;field53","fieldTrancode":"300000002","fieldmessage":"联通交费交易成功","gath_person_acctno":"000000723003100004076","messtype":"","pay_voucher_no":"","pay_type_name":"1","field55":"","field53":"2600000000000000","issue_bank_code":"03134510","field32":"","field14":"","field13":"20140730","field15":"","field12":"163129","field38":"","field37":"","field11":"","tran_seq_no":"2014073031656587"};
		//var type = "燃气缴费";
		var num = data.field2.substring(0,6)+"********"+data.field2.substring(data.field2.length-4,data.field2.length);
		var content = "\n\n";
		content += "         "+type+"缴费单\n\n";
		content += "----------------------------- ---\n";
		content += "零售名称:"+data.merch_name+"\n";
		content += "商户编号:"+data.field42+"\n";
		content += "终端编号:"+data.field41+"\n";
		content += "卡号:"+num+"\n";
		content += "发卡行:"+data.issuer+"\n";
		content += "收单行:"+data.acquirer+"\n";
		content += "交易类型:"+type+"\n";
		content += "缴费号码:"+payNum+"\n";
		content += "参 考 号:"+data.field37+"\n";
		content += "交易日期:"+data.field13+" 时间:"+data.field12+"\n";
		content += "交易金额:"+data.field4+" 元\n";
		content += "备注:\n\n";
		try {
			box.com.printTicket(content,2);
		} catch (e) {
			box.console.log("-----------"+$.obj2str(e));
		}
	}
	function clear(content){
		content.find(".num").val("");
		content.find(".pay_fee").val("");
		content.find("#Num").text("");
		content.find("#UserName").text("");
		content.find("#AccountBalance").text("");
		content.find("#PayableTotal").text("");
		content.find("#InstallAddr").text("");
	}
	
	return {
		init:function(){
			box.listen("zzfw", onload);
		},
		destroy: function(){}
	};
});