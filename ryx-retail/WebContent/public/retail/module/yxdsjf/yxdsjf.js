/**
 * 便门服务--数字电视缴费
 */
HOME.Core.register("plugin-yxdsjf", function(box) {
	var TVSearchURL = "retail/pay/tv/search";
	var payTVFee = "retail/pay/tv/pay";
	var content = null;
	function onload(view) {
		var parentView = view;
		var content = box.ich.view_yxdsjf();
		parentView.empty().append(content);
		//点击查询按钮
		$("#TVNumCheck").click(checkTVUserInfo)
		//点击缴费按钮
		$("#payTVbutton").click(payTVFee_);
		
	}
	//点击查询事件
	function checkTVUserInfo(){
		var TVNum = $("#TVNumInput").val();
		if(TVNum && TVNum!=""){
			box.request({
				dataType:"json",
				url: box.getContextPath()+TVSearchURL,
				data: {TVNum:TVNum},
				success:showTVUserInfo
			});
		}
		else{
			var msg = $("#TVNumInput").attr("verify-message");
			$("#TVNumInput").addClass("validate");
			box.showAlert({message:msg});
		}
		
	}
	//查询结果回调事件
	function showTVUserInfo(data){
		if(data.result.field39 == "00"){
			var hiddenData = JSON.stringify(data.result);
			$("#hiddenData").val(hiddenData);
			$("#TVNum").empty().append(data.result.pay_no);
			$("#TVUserName").empty().append(data.result.user_name);
			$("#TVInstallAddr").empty().append(data.result.address);
			$("#TVPayableTotal").empty().append(data.result.must_pay_sum_amt);
			$("#AccountBalance").empty().append(data.result.last_bal);
		}
		else{
			box.showAlert({message:"没有查询到对应的有线编号信息！"});
		}
	}
	//点击缴费执行
	function payTVFee_(){
		var payMoney = $("#payMoney").val();
		var cardNo = $("#cardNo").val();
		var password= $("#password").val();
		var responseData = $("#hiddenData").val();
		if(responseData == ""){
			box.showAlert({message:"请先查询有线电视信息！"});
			return;
		}
		if(payMoney==""){
			var msg = $("#payMoney").attr("verify-message");
			$("#payMoney").addClass("validate");
			box.showAlert({message:msg});
			return ;
		}
		if(cardNo == ""){
			var msg = $("#cardNo").attr("verify-message");
			$("#cardNo").addClass("validate");
			box.showAlert({message:msg});
			return ;
		}
		if(password==""){
			var msg = $("#password").attr("verify-message");
			$("#password").addClass("validate");
			box.showAlert({message:msg});
			return ;
		}
		box.request({
			dataType:"json",
			url: box.getContextPath()+payTVFee,
			data: {"cardNo":cardNo,"payMoney":payMoney,"password":password,"responseData":responseData},
			success:payFeeInfo
		});
	}
	//缴费回调函数
	function payFeeInfo(data){
		if(data.result.field39 == "00"){
			var content_show=box.ich.view_showPayMsg_success({result:data.result});
			box.showDialog({
				title:"充值成功",
				width:300,
				height:400,
				content: content_show
			});
		}
		else{
			var content_show=box.ich.view_showPayMsg_error();
			box.showDialog({
				title:"充值失败",
				width:300,
				height:400,
				content: content_show
			});
		}
	}
	
	return {
		init: function(){
			box.listen("yxdsjf", onload);
		},
		destroy: function() { }
	};
});