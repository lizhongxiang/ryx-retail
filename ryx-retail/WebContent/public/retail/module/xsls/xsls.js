/**
 * 销售流水信息查询
 */
HOME.Core.register("plugin-xsls", function(box) {
	//销售流水列表
	var searchSaleOrderUrl="retail/order/searchSaleOrder";
	//销售单行
	var searchSaleOrderDetailUrl="retail/order/searchSaleOrderDetail";
	//打印销售报表
	var printSaleOrderLine = "retail/order/printSaleOrderLine"; 
	
	var content=null;
	var parentView = null;
	var beginTime="";
	var endTime="";
	var salesStatus="";
	var paymentStatus="";
	var orderType = "";
	var staticConsumerKeyword = "";
	var saleOrderKeyword = "";
	
	function onload(view) {
		parentView= view;
		parentView.showFooter(true);
		pageIndex = 1;
		pageSize = 20;
		salesStatus="";
		paymentStatus="";
		orderType = "";
		staticConsumerKeyword = "";
		saleOrderKeyword = "";
		getXSLSDate();
	}
	function getXSLSDate(){
		params = {page_index: pageIndex, page_size: pageSize};
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		salesStatus=parentView.find("#salesStatus").val();
		paymentStatus=parentView.find("#paymentStatus").val();
		orderType=parentView.find("#orderType").val();
		if(beginTime==undefined || beginTime=="") {
			beginTime=$.GetToday(-6);
//			beginTime=$.GetDateFormat($.GetToday(-6),"yyyymmdd","yyyy-mm-dd");
		}
		if(endTime==undefined || endTime=="") {
			endTime=$.GetToday();
//			endTime=$.GetDateFormat($.GetToday(),"yyyymmdd","yyyy-mm-dd");
		}
		if(beginTime>endTime){
			box.showAlert({message:"开始日期不能大于结束日期！"});
			return;
		}
		
		//日期格式化为yyyyMMdd
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		params.order_date_floor = beginTimeFormat;
		params.order_date_ceiling = endTimeFormat;
//		params.order_date_floor = $.GetDateFormat(beginTime,"yyyy-mm-dd","yyyymmdd");
//		params.order_date_ceiling = $.GetDateFormat(endTime,"yyyy-mm-dd","yyyymmdd");
		if(orderType) {
			params.order_type = orderType;
		}
		if(paymentStatus) {
			params.pmt_status = paymentStatus;
		}
		if(staticConsumerKeyword) {
			params.consumer_keyword = staticConsumerKeyword;
		}
		if(saleOrderKeyword) {
			params.order_id = saleOrderKeyword;
		}
		var messageList = [];
		if(!$.validateForms(parentView, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		box.request({
			url:box.getContextPath()+ searchSaleOrderUrl,
			data:params,
			success:onGetXSLSDate
		});
	}
	
	function onGetXSLSDate(data){
		if(data && data.code=="0000" && data.result.length>0){
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getXSLSDate();
			});
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1+(pageSize*(pageIndex-1));
				data.result[i].salesStatus=box.labels.ordersale[data.result[i].status];
				data.result[i].paymentStatus=box.labels.orderpay[data.result[i].pmt_status];
				data.result[i].salesType=box.labels.ordertype[data.result[i].order_type];
				data.result[i].amt_ord_total = $.parseMoney(data.result[i].amt_ord_total);
				data.result[i].amtys_ord_total = $.parseMoney(data.result[i].amtys_ord_total);
				data.result[i].amt_ord_profit= $.parseMoney(data.result[i].amt_ord_profit);
				if(parseFloat(data.result[i].qty_return_total)>0){
					data.result[i].color="red";
				}
				// pay_type拿出来有空格, 暂时转成数字判断
				data.result[i].pay_type_label= parseFloat(data.result[i].pay_type)==2?"刷卡":"现金";
				// 如果是网络订单, card_id可能没有却有consumer_id
				if(!data.result[i].card_id) {
					data.result[i].card_id = data.result[i].consumer_id;
				}
				
				//日期格式化为yyyy-MM-dd
				var orderDate=new Date(data.result[i].order_date.substr(0, 4),data.result[i].order_date.substr(4, 2)-1,data.result[i].order_date.substr(6, 2),
						data.result[i].order_time.substr(0, 2),data.result[i].order_time.substr(2, 2),data.result[i].order_time.substr(4, 2));
				data.result[i].order_date=orderDate.format("yyyy-MM-dd hh:mm:ss");
				//data.result[i].order_time=orderDate.format("hh:mm:ss");
			}
		}else{
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		showXSLS(data.result);
		parentView.showAdvancedSearch();
	}
	
	function showXSLS(result){
		content=box.ich.view_xsls({list: result});
		parentView.empty().append(content);
		if(result.length!=0){
			parentView.find("table tbody tr").click(onXSLSXiangXi);
		}
		parentView.find("#selewares").unbind("click").click(getXSLSDateAction);
		
//		parentView.find("#printButton").unbind("click").click(printTicket);
		parentView.find("#printButtons").unbind("click").click(printTicket);
		
		parentView.find("#beginTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find("#endTime").datepicker({
			maxDate: 'd' 
		});
		
		parentView.find("#beginTime").val(beginTime);
		parentView.find("#endTime").val(endTime);

		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.find("#salesStatus").val(salesStatus);
		parentView.find("#paymentStatus").val(paymentStatus);
		parentView.find("#orderType").val(orderType);
		if(staticConsumerKeyword) {
			parentView.find("#consumer_keyword").val(staticConsumerKeyword);
		}
		if(saleOrderKeyword) {
			parentView.find("#order_id_keyword").val(saleOrderKeyword);
		}
	}
	function getXSLSDateAction(){
		pageIndex = 1;
		pageSize = 20;
		staticConsumerKeyword =$.trim(parentView.find("#consumer_keyword").val());
		saleOrderKeyword =$.trim(parentView.find("#order_id_keyword").val());
		getXSLSDate();
	}
	
	function printTicket(){
		box.request({
			url:box.getContextPath()+ printSaleOrderLine,
			data:{begintime:beginTime,endtime:endTime},
//			success:thisPrintTicket
			success:showTodayItemSaleDetail
		});
	}
	
	function thisPrintTicket(resultData){
		return function(e) {
			var strarr = new Array();
			var saleItemList =resultData.result.sale_list;
			var returnItemList=resultData.result.return_list;
			var newSaleAme = $.parseMoney(parseFloat(resultData.result.amtys_ord_total) + parseFloat(resultData.result.return_ord_amount));
			var newSaleQty = (parseFloat(resultData.result.qty_ord_total) + parseFloat(resultData.result.return_ord_quantity)).toFixed(1);
			strarr.push("\n" + box.user.merch_name + "日结单\n");
			strarr.push("时间：" + resultData.result.current + "\n");
			strarr.push("--------------------------------\n");
			strarr.push("销售量：" + $.parseLength(newSaleQty, 6, false));
			strarr.push("销售额：" + $.parseLength(newSaleAme, 8, false) + "\n");
			strarr.push("退货量：" + $.parseLength(resultData.result.return_ord_quantity, 6, false));
			strarr.push("退货额：" + $.parseLength($.parseMoney(resultData.result.return_ord_amount), 8, false) + "\n");
			strarr.push("实收额：" + $.parseLength($.parseMoney(resultData.result.amtys_ord_total ), 8, false) + "\n");
			strarr.push("-----------_销售详情-------------\n");
//				strarr.push("\n" + box.user.ticket.welcome_word + "\n");
//				strarr.push("--------------------------------\n");
//				strarr.push("单号：" + order.order_id + "\n");
//				strarr.push("时间：" + order.order_date_label + "\n");
//				strarr.push("--------------------------------\n");
			strarr.push("    商品名      |  数量  |  金额\n");
			strarr.push("　\n");
			
			if(saleItemList && saleItemList.length!=0){
				var saleItem = null;
				for(var i=0; i<saleItemList.length; i++) {
					saleItem = saleItemList[i];
					strarr.push($.parseLength(saleItem.item_name, 15, false, true));
					strarr.push($.parseLength(saleItem.sale_quantity, 5, true));
					strarr.push(" " + $.parseLength($.parseMoney(saleItem.sale_amount), 8, true), "\n");
				}
			}else{
				strarr.push("暂无销售记录");
			}
			strarr.push("　\n");
			strarr.push("---------_--退货详情-------------\n");
			strarr.push("    商品名      |  数量  |  金额\n");
			strarr.push("　\n");
			if(returnItemList){
				var returnItem=null;
				for(var i=0; i<returnItemList.length; i++) {
					returnItem = returnItemList[i];
					strarr.push($.parseLength(returnItem.item_name, 15, false,true));
					strarr.push($.parseLength(returnItem.return_quantity, 5, true));
					strarr.push(" " + $.parseLength($.parseMoney(returnItem.return_amount), 8, true), "\n");
				}
			}else{
				strarr.push("暂无退货记录");
			}
			
			box.console.log(strarr.join(""));
			strarr.push("　\n　\n　\n　\n");
			box.com.print(strarr.join(""));
			setTimeout(function() {
				parentView.find("#printButton").removeAttr("disabled");
			}, 1000);
		}
	}
	
	function onXSLSXiangXi(e){
		var order_id=$(e.currentTarget).attr("data-id");
		showXiangxi(order_id);
	}
	
	function showXiangxi(order_id){
		box.request({
			url:box.getContextPath()+ searchSaleOrderDetailUrl,
			data:{order_id:order_id,begintime:beginTime,endtime:endTime},
			success:onGetXSLS_Detail
		});
	}
	
	function onGetXSLS_Detail(data){
		if(data && data.code=="0000"){
			var result = data.result;
			if(!result){
				box.showAlert({message:"很抱歉，数据异常！"});
				return ;
			}
			var resultList = result.list;
			if(!resultList){
				box.showAlert({message:"很抱歉，数据异常！"});
				return;
			}
			var l = resultList.length;
			
			for(var i=0; i<l; i++){
				var row = resultList[i];
				row.index = i+1;
				row.big_pri3=$.parseMoney(row.big_pri3);
				row.amt_ord=$.parseMoney(row.amt_ord);
				row.amt_ret=$.parseMoney(row.amt_ret);
				row.promotion_price=$.parseMoney(row.amt_ord / row.qty_ord);
				row.new_adjusted_amount=$.parseMoney(parseFloat(row.adjusted_amount) + parseFloat(row.other_adjusted_amount));
			}
			result.adjusted_amount=$.parseMoney(result.adjusted_amount);
			result.amt_ord_total=$.parseMoney(result.amt_ord_total);
			result.amtys_ord_total=$.parseMoney(result.amtys_ord_total);
			result.sum_return_amt=$.parseMoney(result.sum_return_amt);
		
			showXSLS_Detail(result);
		}
	}
	
	//打开“流水明细”窗口
	function showXSLS_Detail(result){
		var order=result.order_id;
	
		content=box.ich.view_xsls_detail({result: result});
		box.showDialog({
			title:"流水明细",
			width:960,
			height:500,
			content: content ,
			close: function(e) {
				//$("#selewares").focus();
			}
		}); 
		content.find(".table_view").fixedtableheader({
			parent: content.find(".table_view").parent(),
			win: content,
			isshow: true 
		});
		setQtyRetColor(result,order);
	}
	
	function setQtyRetColor(result,order){
		var qtyRetTds=content.find("td[name='qtyRetTd']");
		for(var i=0;i<qtyRetTds.length;i++){				
			if(qtyRetTds.eq(i).text()>0){
				qtyRetTds.eq(i).css("color","red");
			}
		}  
		var amtRetTds=content.find("td[name='amtRetTd']");
		for(var i=0;i<amtRetTds.length;i++){				
			if(amtRetTds.eq(i).text()>0){
				amtRetTds.eq(i).css("color","red");
			}
		} 
		var amtRetTotalLabel = content.find("#amt_ret_total");
		var qtyRetTotalLabel = content.find("#qty_ret_total");
		var amtRetTotal = parseFloat(amtRetTotalLabel.text());
		var qtyRetTotal = parseFloat(qtyRetTotalLabel.text());
		if(amtRetTotal>0) amtRetTotalLabel.css("color","red");
		if(qtyRetTotal>0) qtyRetTotalLabel.css("color","red");
		
//		var orderData = new Array();
//		var sumAmt=0;
//		var sumQty=0;
//		var orderLines = result.list;
//		for(var i= 0; i<orderLines.length;i++){
//			var qty = $.round(parseFloat(orderLines[i].qty_ord)-parseFloat(orderLines[i].qty_ret),2);
//			if(qty!=0){
//				orderLines[i].qty_ord=qty;
////			orderLines[i].amt_ord = $.round(parseFloat(orderLines[i].big_pri3)*parseFloat(qty),2);
//				sumAmt=parseFloat(sumAmt)+parseFloat(orderLines[i].amt_ord);
//				sumQty=parseFloat(sumQty)+parseFloat(qty);
//				orderData.push(orderLines[i]);
//			}
//		}
		content.find("#resetPrintButton").click(reprintTicket(result));
	}
	
	//打印小票，调整数据
	function reprintTicket(order) {
		var myOrder = order;
		var orderDate = myOrder.order_date;
		var orderTime = myOrder.order_time;
		myOrder.order_date_label = orderDate.substr(0,4)+"-"+orderDate.substr(4,2)+"-"+orderDate.substr(6,2)
			+" "+orderTime.substr(0,2)+":"+orderTime.substr(2,2)+":"+orderTime.substr(4,2);
		var orderLines = myOrder.list;
		var total = {adjusted_amount:myOrder.adjusted_amount};
		for(var i=0; i<orderLines.length; i++) {
			var line = orderLines[i];
			line.pri = $.parseMoney((line.old_amt_ord - line.adjusted_amount) / line.qty_ord );
			line.amt_ord = $.parseMoney(line.old_amt_ord - line.adjusted_amount);
		}
		myOrder.change = $.parseMoney(myOrder.amt_ord_total - myOrder.amtys_ord_total);
		myOrder.moling= myOrder.amt_ord_loss;
		return function(e) {
			box.controls.print(myOrder, total, myOrder.list, null);
		};
	}
	
	//显示销售员今日销售详情
	function showTodayItemSaleDetail(data){
		var result = data.result;
		
		result.amtys_ord_total=  $.parseMoney(result.amtys_ord_total);
		result.return_ord_amount =  $.parseMoney(result.return_ord_amount );
		result.sale_profit_amount =  $.parseMoney(result.sale_profit_amount );
		
		var index = 0;
		for ( ; index < result.sale_list.length; index++) {
			result.sale_list[index].index = index+1;
			var itemLine = result.sale_list[index];
			var newSaleAmount = parseFloat(itemLine.sale_amount) ;
			itemLine.sale_amount =  $.parseMoney( newSaleAmount );
			itemLine.profit_amount =  $.parseMoney(itemLine.profit_amount);
		}
		for ( var i = 0 ; i < result.return_list.length; i++) {
			result.return_list[i].index = ++index;
			var itemLine = result.return_list[i];
			itemLine.return_amount = $.parseMoney(0 - itemLine.return_amount );
			itemLine.profit_amount = $.parseMoney(0 - itemLine.profit_amount );
			itemLine.return_quantity = 0 - itemLine.return_quantity;
		}
		
		content = box.ich.view_today_xsls_detail( {result:result});
		orderDetail = box.showDialog({
			title:"今日销售商品详情",
			width:960,
			height:500,
			content: content ,
			close: function(e) {
				//$("#selewares").focus();
			}
		}); 
		content.find(".table_view").fixedtableheader({
			parent: content.find(".table_view").parent(),
			win: content,
			isshow: true 
		});
		content.find("#todayXslsDetailButton").unbind("click").click(thisPrintTicket(data));
	}
	
	return {
		init: function(){
			box.listen("xsls", onload);
		},
		destroy: function() { }
	};
});