/**
 * 库存管理功能
 */
HOME.Core.register("plugin-dysz", function(box) {
	
	var selectTicketUrl = "/retail/selectTicket";
	var updateOrInsertTicketUrl = "/retail/updateOrInsertTicket";
	
	var parentView = null;
	var content = null;
	
	function onload(view) {
		parentView = view;
		onGetTicket();
	}
	
	function onGetTicket(){
		box.request({
			url: box.getContextPath() + selectTicketUrl,
			success:onGetTicketData
		});
	}
	
	function onGetTicketData(data) {
		if(data && data.code =="0000") {
			content = box.ich.view_dysz(data.result);
			
			parentView.empty().append(content);
			
			parentView.find("#ShowDiscourse").text(data.result.welcome_word);
			parentView.find("#showPhone").text(data.result.phone);
			parentView.find("#showRemork").text(data.result.note);
			
			parentView.find("#saveButton").click(onSubmitBtn);
			parentView.find("#printButton").click(print);
			
			parentView.find("#downpd").attr("href", box.getResourcePath() + "download/files/XPrinter.rar");
		}else{
			box.showAlert({message:msg});
		}
	}
	
	function onSubmitBtn(){
		var discourse=parentView.find("#welcomeDiscourse").val();
		var remark=parentView.find("#welcomeRemark").val();
		var phone=parentView.find("#welcomePhone").val();
		var number=parentView.find("#welcomeNUm").val();
		
		//"ticket":{"ticket_type":"01","num":"1","phone":"15562499800","ticket_date":"","note":"","welcome_word":"济南高新开发区全信酒水批发部欢迎您！"}
		
		box.user.ticket.welcome_word = discourse;
		box.user.ticket.note = remark;
		box.user.ticket.phone = phone;
		box.user.ticket.num = number;
		
//		parentView.find("#ShowDiscourse").text(number); 
		
		box.request({
			url: box.getContextPath() + updateOrInsertTicketUrl,
			data:{welcome_word:discourse,note:remark,phone:phone,num:number},
			success:onGetTicket
		});
		
	}
	
	
	function print(){
		var strarr = new Array();
		strarr.push("\n打印测试页\n　\n");
		strarr.push(box.user.ticket.welcome_word + "\n");
		strarr.push("--------------------------------\n");
		strarr.push("单号：13963398888XXXXX\n");
		strarr.push("时间：2014-01-01 12:00:00\n");
		strarr.push("--------------------------------\n");
		strarr.push("  商品名    | 数量 | 售价 | 金额\n");
		strarr.push("娇子(软阳光)    1   9.50    9.50\n");
		strarr.push("双喜(硬经典)    1  11.00  110.00\n");
		strarr.push("--------------------------------\n");
		strarr.push("数量：" + $.parseLength(2,6, true) + "     ");
		strarr.push("合计：" + $.parseLength("119.50",7, true) + "\n");
		strarr.push("实收：" + $.parseLength("119.50",7, true) + "     ");
		strarr.push("找零：" + $.parseLength("0.00", 7, true) +   "\n");
		
		strarr.push("--------------------------------\n");
		
		strarr.push("会员卡号：10000100\n");
		if(box.user.ticket.phone) {
			strarr.push("本店电话：" + box.user.ticket.phone + "\n");
		}
		if(box.user.ticket.note) {
			strarr.push("" + box.user.ticket.note + "\n");
		}
		strarr.push("--------------------------------\n");
		strarr.push("　\n　\n　\n　\n");
		box.com.print(strarr.join(""));
		
		box.console.log(strarr.join(""));
	}
	
	
	
	return {
		init: function(){
			box.listen("dysz", onload);
		},
		destroy: function() { }
	};
});