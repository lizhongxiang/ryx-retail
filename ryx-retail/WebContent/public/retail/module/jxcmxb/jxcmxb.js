/**
 * 进销存明细表
 */
HOME.Core.register("plugin-jxcmxb", function(box) {
	var getJXCReportTableUrl="retail/statistics/jxcreporttable";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var staticItemKind="";
	
	function onload(view) {
		parentView = view;
		showJXCReportTable();
		parentView.showAdvancedSearch();
	}
	//显示明细报表
	function showJXCReportTable(){
		parentView.find("#selewares").attr("disabled","disabled");
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
				 
		if(beginTime && !endTime) {
			endTime=beginTime;
		}
		if(!beginTime && endTime) {
			beginTime=endTime;
		}
		if(!beginTime && !endTime) {
			beginTime=$.GetToday(-7);
			endTime=$.GetToday(-1);
		}
		if(beginTime>endTime){
			box.showAlert({message:"很抱歉，开始日期不能大于结束日期！"});
			parentView.find("#selewares").removeAttr("disabled");
			return;
		}
		var myToday=$.GetToday();
		if(endTime>=myToday){
			box.showAlert({message:"很抱歉，只能查询历史记录！"});
			parentView.find("#selewares").removeAttr("disabled");
			return;
		}
		
		//日期格式化为yyyyMMdd
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		var itemKind=parentView.find("#item_kind").val();
		if(!itemKind){
			itemKind="00";
		}
		staticItemKind=itemKind;
		var params={'end_date':endTimeFormat,'start_date':beginTimeFormat,'item_kind_id':staticItemKind};
		box.request({
			url : box.getContextPath() + getJXCReportTableUrl,
			data : {params: $.obj2str(params)},
			type: "get",
			expires: "d",
			success : successJXCReportTable
		});
	}
	function successJXCReportTable(data){
		if(data.result.length<=0){
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		var grandTotal = {};
		parentView.find("#selewares").removeAttr("disabled");
		if(data && data.code=="0000") {
			result = data.result;
			var sumAllPushAmount = 0.0;
			var sumAllSaleAmount = 0.0;
			var sumAllWhseTurnPlAmount = 0.0;
			var sumAllBeginWhseAmount = 0.0;
			var sumAllEndWhseAmount = 0.0;
			for(var i = 0;i < result.length;i++){
				result[i].sum_purch_amount = $.parseMoney(result[i].sum_purch_amount);
				result[i].sum_profit_amount = $.parseMoney(result[i].sum_profit_amount);
				result[i].sum_sale_amount = $.parseMoney(result[i].sum_sale_amount);
				result[i].sum_whse_turn_pl_amount = $.parseMoney(result[i].sum_whse_turn_pl_amount);
				result[i].end_whse_amount = $.parseMoney(result[i].end_whse_amount);
				result[i].begin_whse_amount = $.parseMoney(result[i].begin_whse_amount);
				sumAllPushAmount = parseFloat(sumAllPushAmount) + parseFloat(result[i].sum_purch_amount);
				sumAllSaleAmount = parseFloat(sumAllSaleAmount) + parseFloat(result[i].sum_sale_amount);
				sumAllWhseTurnPlAmount = parseFloat(sumAllWhseTurnPlAmount) + parseFloat(result[i].sum_whse_turn_pl_amount);
				sumAllBeginWhseAmount = parseFloat(sumAllBeginWhseAmount) + parseFloat(result[i].begin_whse_amount);
				sumAllEndWhseAmount = parseFloat(sumAllEndWhseAmount) + parseFloat(result[i].end_whse_amount);
			}
			grandTotal["sum_all_push_amount"] = $.parseMoney(sumAllPushAmount);
			grandTotal["sum_all_sale_amount"] = $.parseMoney(sumAllSaleAmount);
			grandTotal["sum_all_whse_turn_pl_amount"] = $.parseMoney(sumAllWhseTurnPlAmount);
			grandTotal["sum_all_begin_whse_amount"] = $.parseMoney(sumAllBeginWhseAmount);
			grandTotal["sum_all_end_whse_amount"] = $.parseMoney(sumAllEndWhseAmount);
			content=box.ich.view_jxcmxb({jsclist:result, item_kind_list:box.kinds.array, grand_total:grandTotal});
			if(staticItemKind) {
				content.find("#item_kind").val(staticItemKind);
			}
			parentView.empty().append(content);
			parentView.find("#beginTime").val(beginTime);
			parentView.find("#endTime").val(endTime);
			parentView.find("#beginTime").datepicker({
				maxDate: '-1d' 
			});
			parentView.find("#endTime").datepicker( {
				maxDate: '-1d' 
			});
			parentView.find("#beginTime").change(function(){
				parentView.find("#selewares").focus();
			});
			parentView.find("#endTime").change(function(){
				parentView.find("#selewares").focus();
			});
			
			parentView.find(".table_view").tablesort([
			       		        {col:1,order:"asc",method:"advance",type:"number"},
			       		        {col:3,order:"asc",method:"advance",type:"number"},
			       		        {col:4,order:"asc",method:"advance",type:"number"},
			       		        {col:5,order:"asc",method:"advance",type:"number"},
			       		        {col:6,order:"asc",method:"advance",type:"number"},
			       		        {col:7,order:"asc",method:"advance",type:"number"},
			       		        {col:8,order:"asc",method:"advance",type:"number"},
			       		        {col:9,order:"asc",method:"advance",type:"number"},
			       		        {col:10,order:"asc",method:"advance",type:"number"},
			       		        {col:11,order:"asc",method:"advance",type:"number"},
			       		        {col:12,order:"asc",method:"advance",type:"number"}
			       		]);
			
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			});
			parentView.find("#selewares").unbind("click", showJXCReportTable).click(showJXCReportTable);
			parentView.showFooter(true);
		}
	}
	function onGetTicket(){
		box.request({
			url: box.getContextPath() + selectTicketUrl,
			success:onGetTicketData
		});
	}
	function onclose(){
		parentView.hideAdvancedSearch();
	}

	return {
		init: function(){
			box.listen("jxcmxb", onload);
			box.listen("jxcmxb_close", onclose);
		},
		destroy: function() {}
	};
});