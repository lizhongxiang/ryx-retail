/**
 * 青岛 银行卡管理
 */
HOME.Core.register("plugin-hygl", function(box) {
	
	var bindBankCardUrl = "http://124.133.240.131:9002/payonline/payment/bind/bindCard";//银行卡绑定URL
	var smsValidateUrl =  "http://124.133.240.131:9002/payonline/payment/bind/phoneCardMassageForBinding";//短信验证
	var bankCardListUrl = "http://124.133.240.131:9002/payonline/payment/bind/getBindList";//银行卡列表
	var getUnbundBankCardUrl = "http://124.133.240.131:9002/payonline/payment/bind/unbundlingCard";//银行卡解绑
	var getBinkInfoUrl = "http://192.168.0.3:8818/master/public/bank/getCardInfo";//获得银行卡的信息
	
	var parentView=null;
	var content=null;
	var userId = "1037010507633";
	var userSmsCodeResult = null;
	var wait=60;
	
	function onload(view) {
		parentView = view;
		parentView.closeMessage();
		parentView.showFooter(true);
		userSmsCodeResult = null;
		getBankCardList();
	}
	
	//获得银行卡列表
	function getBankCardList(){
		var params = {userId:userId};
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: bankCardListUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				showBankCardList(data);
			},
			error:function(data){
				showBankCardList(null);
			}
		});
	}
	
	//展示银行卡列表
	function showBankCardList(data){
		
		var bankCardList = [];
		if (data && data.code == "0000") {
			bankCardList = data.result;
		}
		if (bankCardList.length < 1) {
			box.showAlert({message: "对不起，没有查询已绑定的银行卡！"});
		}
		for ( var i = 0; i < bankCardList.length; i++) {
			bankCardList[i].bank_imp = box.getBankImage(bankCardList[i].issinscode);//获得银行对应的图片地址
			
			//卡号处理
			var cardId = bankCardList[i].accno;
			bankCardList[i].newaccno =cardId.substring(0, 6) +"*******"+ cardId.substring(cardId.length-6 ,cardId.length);
			
			//手机号处理
			var phoneNo  = bankCardList[i].phoneno;
			bankCardList[i].newphoneno =phoneNo.substring(0, 3) +"*******"+ phoneNo.substring(phoneNo.length-4 ,phoneNo.length);
			
			//日期处理
			var binddate = bankCardList[i].binddate;
			var newBinddate=new Date(binddate.substr(0, 4), binddate.substr(4, 2)-1, binddate.substr(6, 2));
			data.result[i].new_bind_date = newBinddate.format("yyyy-MM-dd");
			
			if(bankCardList[i].paycardtype == '01'){
				bankCardList[i].paycard_type_name = "借记卡";
			}else{
				bankCardList[i].paycard_type_name = "信用卡";
			} 
		}
		content=box.ich.view_yhklb({bank_card_list:bankCardList});
		parentView.empty().append(content);		
		parentView.find(".unbund_div a").click(WarningUnbundling);//解绑
		parentView.find("#bind_bank_card_btn").unbind("click").click(showBindBankCard);//绑定
	}
	
	
	//解绑警告
	function  WarningUnbundling (e){
		var aTag = $(e.target);
		var bankCardId = aTag.closest("li").attr("data-bank-id");
		var cardIdTail = bankCardId.substring(0, 6) +"*******"+ bankCardId.substring(bankCardId.length-6 ,bankCardId.length);
		
		box.showConfirm({
			message: "确定要解绑此银行卡("+cardIdTail+")？", 
			title: "警告",
			ok: function() {
				UnbundlingBankCard(bankCardId);
			}
		});
	}
	
	//银行卡解绑
	function UnbundlingBankCard(bankCardId){
//		var aTag = $(e.target);
//		var bankCardId = aTag.closest("li").attr("data-bank-id");
		var params = {userId:userId, accNo:bankCardId};
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: getUnbundBankCardUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				getBankCardList();
			},
			error:function(data){
				box.showAlert({message: "对不起，解绑失败！"});
			}
		});
	}
	
	//显示绑定银行卡界面
	function showBindBankCard (){
		wait = 60;//用于发生验证码--计时
		userSmsCodeResult = null;
		content=box.ich.view_bind_bank_card();
		box.showDialog({
			title:"绑定银行卡",
			width:480,
			height:390,
			content: content,
			close: function(e){
//				parentView.find("#addconsumer").removeAttr("disabled");
			}
		});
		content.find("#submit").unbind("click").click(submitBindBankCard);//绑定
		content.find("input[name='bank_card_type']").change(changeBankCardType);//选择银行卡类型
		content.find("#send_sms_code_btn").unbind("click").click(sendVerificationCode);//发送短信验证码
		content.find("#bank_card_id").keyup(keyupCardId);//银行卡号输入时改变样式
		content.find("#bank_card_id").blur(blurCardId);//银行卡号输入时改变样式
		content.find("#bank_card_id").focus(focusCardId);//银行卡号输入时改变样式
		content.find("#bank_card_id").unbind("change").change(changeCardId);//银行卡号输入时改变样式
		content.find("#user_agreement").unbind("change").change(JudgeUserAreement);//勾选用户协议
		
		content.find("#cancel").unbind("click").click(function(e){
			box.closeDialog({content: content});
		});//绑定
	}
	
	//用户协议选择
	function JudgeUserAreement(){
		var userAreement = content.find("#user_agreement");
		var submit = content.find("#submit");
		if(userAreement.is(':checked')){
			submit.attr("disabled", false);
		}else{
			submit.attr("disabled", true);
		}
	}
	
	//通过银行卡号，获得银行的信息，
	function changeCardId(e){
		var cardId = content.find("#bank_card_id").val();
		var bankCardId = cardId.replace(/\s+/g,"");
		if(!bankCardId){
			content.find("#bank_icon").css("display", "none");
			content.find("#new_bank_icon").attr("src", "");
			content.find("#new_bank_icon").attr("title", "");
			
			content.find("#bank_adddiv>table").attr("data-bank-name", "");
			content.find("#bank_adddiv>table").attr("data-bank-code", "");
			return ;
		}
		var params = {card_no:bankCardId};
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: getBinkInfoUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				if(data && data.code == '0000'){
					content.find("#new_bank_icon").attr("src", box.getBankImage(data.result.issue_bank_code, "s", "icon"));
					content.find("#new_bank_icon").attr("title", data.result.issue_bank_name);
					content.find("#bank_icon").css("display", "block");
					content.find("#bank_adddiv>table").attr("data-bank-name",data.result.issue_bank_name);
					content.find("#bank_adddiv>table").attr("data-bank-code",data.result.issue_bank_code);
				}
			},error:function(data){
				box.showAlert({message: data.msg});
				content.find("#bank_icon").css("display", "none");
				content.find("#new_bank_icon").attr("src", "");
				content.find("#new_bank_icon").attr("title", "");
				content.find("#bank_adddiv>table").attr("data-bank-name", "");
				content.find("#bank_adddiv>table").attr("data-bank-code", "");
			}
		});
	}
	
	function focusCardId(){
		var formatDiv = content.find("#format_card_div");
		
		var cardId = content.find("#bank_card_id");
		if(cardId.val()){
			formatDiv.css("display", "none");
			var formatId = content.find("#format_bank_card_id");
			formatId.text(cardId.val().replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1 "));
		}
		
	}
	
	function blurCardId(){
		var formatDiv = content.find("#format_card_div");
		formatDiv.css("display", "none");
	}
	
	
	//银行卡号，卡号没四位自动隔开
	function keyupCardId(e){
		var formatDiv = content.find("#format_card_div");
		formatDiv.css("display", "block");
		
		var cardId = content.find("#bank_card_id");
		var formatId = content.find("#format_bank_card_id");
		formatId.text(cardId.val().replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1 "));
	}
	
	//发送短息验证码
	function sendVerificationCode(){
		var userTelephone = content.find("#user_telephone").val();//用户手机号
		var params={phoneNo:userTelephone, userId:userId };
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: smsValidateUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				time();
				userSmsCodeResult = data;
				////////////////////////////////////-----------------------------------------需要删除
				content.find("#sms_code").val(userSmsCodeResult.pageparams);
				////////////////////////userSmsCodeResult.pageparams;
			},
			error:function(data){
				userSmsCodeResult = null;
				box.showAlert({message: "短信验证失败: "+data.msg });
			}
		});
	}
	
	//信用卡不需要短信验证
	function changeBankCardType (e){
		var creditCard = content.find("#credit_card");//是否选择信用卡
		if($(creditCard).is(':checked')){
			//content.find("#validate_sms_tr").show();
			content.find("#validate_sms_tr").css("visibility", "visible");
			content.find("#sms_code").attr("data-empty", "false");
		}else{
			//content.find("#validate_sms_tr").hide();
			content.find("#validate_sms_tr").css("visibility", "hidden");
			content.find("#sms_code").attr("data-empty", "true");
		}
	}
	
	//绑定银行卡
	function submitBindBankCard (){
		
		var messageList = [];
		if(!$.validateForms(content, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		var bankCardType = content.find("input[name='bank_card_type']:checked").val();//银行卡类型
		var bankCardId = content.find("#bank_card_id").val();//银行卡号
		var userName = content.find("#user_name").val();//用户名称
		var identityCardId = content.find("#identity_card_id").val();//用户身份证号
		var userTelephone = content.find("#user_telephone").val();//用户手机号
		var smsCode = content.find("#sms_code").val();//短信验证码
		
		var bankName = $("div>table").attr("data-bank-name");
		var bankCode = $("div>table").attr("data-bank-code");
		
		var vfOrderId = " ";//后台短信验证码
		bankCardId = bankCardId.replace(/\s+/g,"");
		if(bankCardType == '01' && (!userSmsCodeResult || userSmsCodeResult.code != '0000')){
			box.showAlert({message: "验证码获取失败，请重新获取！"});
			return ;
		}
		
		var params={userId:userId, payCardType:bankCardType, accNo:bankCardId, userName:userName, 
				cardId:identityCardId, phoneNo:userTelephone, vfCode:smsCode, 
				issInsCode:bankCode, issInsName:bankName
			};
			
		if(userSmsCodeResult){
			vfOrderId = userSmsCodeResult.result;
			params.vfOrderId = vfOrderId;
		}
		if(bankCardType == "02"){
			var newBindBankCardUrl = bindBankCardUrl+"?params={userId='"+userId+"', payCardType='"+bankCardType+"', accNo='"+bankCardId+"', userName='"+userName+"', cardId='"+identityCardId+"', phoneNo='"+userTelephone+"', issInsCode='"+bankCode+"', issInsName='"+bankName +"' }";
			window.open(newBindBankCardUrl);
			showBindDetermine(params);
		}else{
			box.request({
				type : "post",
				dataType : 'jsonp',
				url: bindBankCardUrl,
				data:{params:$.obj2str (params)},
				success:function(data) {
					if(data && data.code == '0000'){
						box.showAlert({message: "绑定银行卡成功"});
						box.closeDialog({content: content});
						getBankCardList();
					}
				},
				error:function(data){
					box.showAlert({message: "绑定银行卡失败"});
				}
			});
		}
		
	}
	
	
	
	//显示绑定银行卡确定页面
	function showBindDetermine (bindBankCardParams){
		determineContent=box.ich.view_bind_determine();
		box.showDialog({
			title:"提示",
			width:380,
			height:220,
			content: determineContent,
			close: function(e){
				//parentView.find("#addconsumer").removeAttr("disabled");
			}
		});
		determineContent.find("#submit_determine").unbind("click").click(function(e){//支付完成
			validateBindBankCardStaty(bindBankCardParams);
			
		});
	}
	
	
	//判断银行卡状态
	function validateBindBankCardStaty(bindBankCardParams){
		var params = {userId:userId};
		box.request({
			type : "post",
			dataType : 'jsonp',
			url: bankCardListUrl,
			data:{params:$.obj2str (params)},
			success:function(data) {
				var bankCardList = [];
				if (data && data.code == "0000") {
					bankCardList = data.result;
				}
				var isBindScs = false;//是否绑定成功
				for ( var i = 0; i < bankCardList.length; i++) {
					if(bankCardList[i].accno == bindBankCardParams.accNo ){
						isBindScs = true;
						break;
					}
				}
				if(isBindScs){//绑定成功
					box.showAlert({message: data.msg});
					box.closeDialog({content: determineContent});
					box.closeDialog({content: content});
					getBankCardList();
					box.showAlert({message: "绑定银行卡成功"});
				}else{
					box.closeDialog({content: determineContent});
					box.showAlert({message: "绑定银行卡失败"});
				}
			}
		});
	}
	
	function time(o) {
		var sendSmsCodeBtn = content.find("#send_sms_code_btn");
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
	
	
	
	//关闭此功能模块
	function onClose(){
		pageIndex = 1;
		pageSize = 20;
		userSmsCodeResult = null;
	}
	
	return {
		init: function(){
			box.listen("yhkgl", onload);
			box.listen("yhkgl_close", onClose);
		},
		destroy: function() { }
	};
});