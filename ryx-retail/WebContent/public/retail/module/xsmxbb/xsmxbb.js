/**
 * 进销存明细表
 */
HOME.Core.register("plugin-xsmxbb", function(box) {
	var getSaleReportTableUrl="retail/statistics/salereporttable";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var staticItemKind="";
	
	function onload(view) {
		parentView = view;
		showJXCReportTable();
	}
	//显示明细报表
	function showJXCReportTable(){
		parentView.find("#selewares").attr("disabled","disabled");
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		//if((beginTime!=null && endTime==null)||(beginTime!="" && endTime=="")){
		//	endTime=beginTime;
		//}
		//if((beginTime==null && endTime!=null)||(beginTime=="" && endTime!="")){
		//	beginTime=endTime;
		//}
		//if((beginTime=="" && endTime=="")||(beginTime==null && endTime==null)){
		//	beginTime=$.GetToday(-7);
		//	endTime=$.GetToday(-1);
		//}
		//if(beginTime.length!=8|| endTime.length!=8){
		//	box.showAlert({message:"很抱歉，时间格式错误！"});
		//	parentView.find("#selewares").removeAttr("disabled");
		//	return;
		//} 
		
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
		
		//var mydate = new Date();
		//var newYear=mydate.getFullYear(); //获取完整的年份(4位,1970-????)
		//var newMonth=mydate.getMonth()+1; //获取当前月份(0-11,0代表1月)
		//var newDate=mydate.getDate(); //获取当前日(1-31)
		//if(newMonth<=9){
		//	newMonth="0"+newMonth;
		//}
		//if(newDate<=9){
		//	newDate="0"+newDate;
		//}
		//var myToday=newYear+newMonth+newDate;
		
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
		
		//var newBeginTime="";
		//newBeginTime+=beginTime.substring(0,4);
		//newBeginTime+="-";
		//newBeginTime+=beginTime.substring(4,6);
		//newBeginTime+="-";
		//newBeginTime+=beginTime.substring(6,8);
		var itemKind=parentView.find("#item_kind").val();
		if(!itemKind){
			itemKind="00";
		}
		staticItemKind=itemKind;
		var params={'start_date':beginTimeFormat,'end_date':endTimeFormat,'item_kind_id':staticItemKind};
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
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
		firstOpen=false;
		parentView.find("#selewares").removeAttr("disabled");
		
		if(data && data.code=="0000") {
			var grandTotal = {};
			var result = data.result;
			var sumAllProfitAmount = 0.0;
			var sumAllSaleAmount = 0.0;
			var sumAllSaleQuantion = 0.0;
			
			for(var i=0;i<result.length;i++){
				result[i].sum_profit_amount = $.parseMoney(result[i].sum_profit_amount);
				result[i].sum_sale_amount = $.parseMoney(result[i].sum_sale_amount );
				result[i].avg_sale_profit = $.parseMoney(result[i].avg_sale_profit );
				result[i].avg_sale_amount = $.parseMoney(result[i].avg_sale_amount );
				result[i].last_jnjw = $.parseMoney(result[i].last_jnjw );
				result[i].cost = $.parseMoney(result[i].cost );
				sumAllProfitAmount = parseFloat(sumAllProfitAmount) + parseFloat(result[i].sum_profit_amount);
				sumAllSaleAmount = parseFloat(sumAllSaleAmount) + parseFloat(result[i].sum_sale_amount);
				sumAllSaleQuantion = parseFloat(sumAllSaleQuantion) + parseFloat(result[i].sum_sale_quantion);
			}
			grandTotal["sum_all_profit_amount"] = $.parseMoney(sumAllProfitAmount);
			grandTotal["sum_all_sale_amount"] = $.parseMoney(sumAllSaleAmount);
			grandTotal["sum_all_sale_quantion"] = $.parseMoney(sumAllSaleQuantion);
			
			content=box.ich.view_xsmxbb({jsclist:result,item_kind_list:box.kinds.array,grand_total:grandTotal});
			if(staticItemKind) {
				content.find("#item_kind").val(staticItemKind);
			}
			parentView.empty().append(content);
			parentView.find("#beginTime").val(beginTime);
			parentView.find("#endTime").val(endTime);
			parentView.find("#beginTime").datepicker({
				maxDate: '-1d' 
			});
			parentView.find("#endTime").datepicker({
				maxDate: '-1d' 
			});
			parentView.find("#beginTime").change(function(){
				parentView.find("#selewares").focus();
			});
			parentView.find("#endTime").change(function(){
				parentView.find("#selewares").focus();
			});
			parentView.find("#selewares").unbind("click", showJXCReportTable).click(showJXCReportTable);

			parentView.find(".table_view").tablesort([
			       		        {col:1,order:"asc",method:"advance",type:"number"},
			       		        {col:3,order:"asc",method:"advance",type:"number"},
			       		        {col:4,order:"asc",method:"advance",type:"number"},
			       		        {col:5,order:"asc",method:"advance",type:"number"},
			       		        {col:6,order:"asc",method:"advance",type:"number"},
			       		        {col:7,order:"asc",method:"advance",type:"number"},
			       		        {col:8,order:"asc",method:"advance",type:"number"},
			       		        {col:9,order:"asc",method:"advance",type:"number"}
			       		]);
			
			
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			});
			parentView.showFooter(true);
		}
	}
	function onGetTicket(){
		box.request({
			url: box.getContextPath() + selectTicketUrl,
			success:onGetTicketData
		});
	}

	return {
		init: function(){
			box.listen("xsmxbb", onload);
		},
		destroy: function() { }
	};
});