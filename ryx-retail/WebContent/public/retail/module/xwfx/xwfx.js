/**
 * 会员分析
 */
HOME.Core.register("plugin-xwfx", function(box) {
	
	var searchHYFX = "retail/consumer/searchConsumeBehaviorAnalysis";
	var searchHYFXLine = "retail/consumer/searchConsumeBehaviorAnalysisLine";
	var parentView = null;
	var consumerResult = {};
	var operateResult = {};
	var pageIndex = 1;
	var pageSize = 20;
	var beginDate = "";
	var endDate = "";
	var itemKindId = "";
	var beginTime = "";
	var endTime = "";
	var diaEndTime = "";
	var diaBeginTime = "";
	var diaBeginDate = "";
	var diaEndDate = "";
	
	var currentResult = null;
	var currentContent = null;
	
	function onload(view) {
		parentView = view;
		parentView.showFooter(true);
		pageIndex = 1;
		pageSize = 20;
		beginDate = "";
		endDate = "";
		itemKindId = "";
		beginTime = "";
		endTime = "";
		consumerResult = {};
		operateResult = {};
		getCsmBehaviorAnalysis();
	}
	
	//获得会员分析
	function getCsmBehaviorAnalysis(){
		var params = {page_index: pageIndex, page_size: pageSize};
		beginDate = parentView.find("#begin_date").val();
		endDate = parentView.find("#end_date").val();
		beginTime = parentView.find("#begin_time").val();
		endTime = parentView.find("#end_time").val();
//		itemKindId = parentView.find("#item_kind").val();
//		if(itemKindId){
//			params.item_kind_id = itemKindId;
//		}
		if(!endDate) {
			endDate=$.GetToday();
		}
		if(!beginDate ) {
			beginDate=$.GetToday(-60);
		}
		if(!beginTime){
			beginTime = "00:00";
		}
		if(!endTime){
			endTime = "23:59";
		}
		params.start_date = dateFormat(beginDate) + timeFormat(beginTime);
		params.end_date = dateFormat(endDate) + timeFormat(endTime);
//		params.start_date = dateFormat(beginDate)+"000000";
//		params.end_date = dateFormat(endDate)+"240000";
		box.request({
			url:box.getContextPath()+ searchHYFX,
			data:{params: $.obj2str(params)},
			success:dataAnalytical
		});
	}
	
	//数据解析
	function dataAnalytical(data){
		if(data && data.code=="0000"){
			var result = data.result;
			var pageParam = data.pageparams;
			if(pageParam) {
				parentView.showPager(pageParam, function(param) {
					pageIndex = param.page_index;
					pageSize = param.page_size;
					getCsmBehaviorAnalysis();
				});
			}
			if(pageParam.count == 0){
				box.showAlert({message: "对不起，没有查询到数据！"});
			}
			for(var i=0;i < result.length;i++){
				var consuerId = result[i].consumer_id;
				result[i].index = i+1+(pageSize*(pageIndex-1));
				result[i].sum_qty_ord_total = $.round(result[i].sum_qty_ord_total, 1);
				result[i].sum_amtys_ord_total = $.parseMoney(result[i].sum_amtys_ord_total);
				result[i].amt_ord_profit = $.parseMoney(result[i].amt_ord_profit);
				var grossMargin = "100%"; 
				if(result[i].sum_amtys_ord_total == 0){
					grossMargin = "0%";
				}else{
					grossMargin = $.parseMoney(parseFloat(result[i].amt_ord_profit) / parseFloat(result[i].sum_amtys_ord_total));
					grossMargin = $.round(grossMargin * 100, 0)+"%";
				}
				result[i].gross_margin = grossMargin;
				consumerResult[consuerId] = result[i];
			}
			showXWFX(result);
		}
	}
	
	function showXWFX(result){
		var content=box.ich.view_xwfx({list: result, item_kind_list:box.kinds.array});
		parentView.empty().append(content);
		$("#begin_date").val(beginDate);
		$("#end_date").val(endDate);
		//$("#item_kind").val(itemKindId);
//		datetimepicker
		$("#begin_date").datepicker({
			maxDate: 'd' 
		});
		$("#end_date").datepicker({
			maxDate: 'd' 
		});
		
		$("#begin_time").timepicker();
		$("#end_time").timepicker();
		$("#begin_time").val(beginTime);
		$("#end_time").val(endTime);
		parentView.find("#all_list").tablesort([
	        {col:0,order:"asc",method:"advance",type:"number"},
	        {col:1,order:"asc",method:"advance",type:"string"},
	        {col:2,order:"asc",method:"advance",type:"string"},
	        {col:3,order:"asc",method:"advance",type:"string"},
	        {col:4,order:"desc",method:"advance",type:"number"},
	        {col:5,order:"desc",method:"advance",type:"number"},
	        {col:6,order:"desc",method:"advance",type:"number"},
	        {col:7,order:"desc",method:"advance",type:"number"},
	        {col:8,order:"asc",method:"advance",type:"number"}
   		]);
		
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		content.find("table tbody").unbind("click").click(getCsmBehaviorAnalysisDetails);
		content.find("#query").unbind("click").click(function(){
			pageIndex = 1;
			pageSize = 20;
			getCsmBehaviorAnalysis();
		});
	}
	
	//获得行为单行
	function getCsmBehaviorAnalysisDetails(e){
		var target = $(e.target);
		var consumerId = $(target).attr("data-consumer-id");
		if(!consumerId){
			var tr = $(target).closest("tr");
			consumerId = $(tr).attr("data-consumer-id");
		}
		operateResult = consumerResult[consumerId];
		var params = {consumer_id:consumerId};
		var dbd = $("#diabegin_date").val();;
		var ded = $("#diaend_date").val();;
		var dbt = $("#diabegin_time").val();
		var det = $("#diaend_time").val();
		
		diaBeginDate = !dbd ?  beginDate : dbd;
		diaEndDate = !ded ? endDate : ded;
		diaBeginTime = !dbt ?  beginTime : dbt;
		diaEndTime = !det ? endTime : det;
		if(!diaEndDate) {
			diaEndDate=$.GetToday();
		}
		if(!diaBeginDate ) {
			diaBeginDate=$.GetToday(-60);
		}
		if(!diaBeginTime){
			diaBeginTime = "00:00";
		}
		if(!diaEndTime){
			diaEndTime = "23:59";
		}
		itemKindId = $("#item_kind").val();
		if(itemKindId){
			params.item_kind_id = itemKindId;
		}
		params.start_date = dateFormat(diaBeginDate) + timeFormat(diaBeginTime);
		params.end_date = dateFormat(diaEndDate) + timeFormat(diaEndTime);
		box.request({
			url:box.getContextPath()+ searchHYFXLine,
			data:{params: $.obj2str(params)},
			success:showCsmBehaviorAnalysisDetails
		});
	}
	
	//显示行为分析详情
	function showCsmBehaviorAnalysisDetails (data){
		if(data && data.code=="0000"){
			currentResult = data.result;
			showAnalysisDetails(filterDetails(currentResult));
		}
	}
	
	function filterDetails(list, starttime, endtime, itemkind) {
		//if(!starttime && !itemkind) return list;
		var result = new Array();
		var ordersum = {sum_qty_ord_total: 0, sum_amtys_ord_total: 0, amt_ord_profit: 0};
		for ( var i = 0; i < list.length; i++) {
			var obj = {};
			$.extend(true, obj, list[i]);
			
			if(obj.order_time < starttime || obj.order_time > endtime) {
				continue;
			}
			
			obj.amtys_ord_total = $.parseMoney(obj.amtys_ord_total);
			var date = dateFormat(obj.order_date);
			var time = obj.order_time ;
			time = time.substr(0, 2) +":"+ time.substr(2, 2);
			obj.data_time = date+" "+time;
			var line = obj.line;
			var sum = {item_name: "合计", qty_ord: 0, amt_ord: 0, profit: 0, adjusted_amount: 0};
			for ( var j = 0; j < line.length; j++) {
				if(!itemkind || line[j].item_kind_id == itemkind) {
//					line[j].big_pri3 = $.parseMoney(line[j].big_pri3);
					line[j].big_pri3 = $.parseMoney( parseFloat(line[j].amt_ord) / parseFloat(line[j].qty_ord));
					line[j].amt_ord = $.parseMoney(line[j].amt_ord);
					line[j].profit = $.parseMoney(line[j].profit * line[j].qty_ord * line[j].unit_ratio);
					line[j].adjusted_amount = $.parseMoney(line[j].adjusted_amount);
					
					sum.qty_ord += parseFloat(line[j].qty_ord);
					if(line[j].unit_ratio == "1" && (line[j].item_kind_id == '01' || line[j].item_kind_id == '0102') ){
						ordersum.sum_qty_ord_total += parseFloat(line[j].qty_ord ) / parseFloat(line[j].big_unit_ratio);
					}else{
						ordersum.sum_qty_ord_total += parseFloat(line[j].qty_ord );
					}
					
					sum.amt_ord += parseFloat(line[j].amt_ord);
					sum.profit += parseFloat(line[j].profit);
					sum.adjusted_amount += parseFloat(line[j].adjusted_amount);
				} else {
					line.splice(j, 1);
					j--;
				}
			}
			sum.amt_ord = $.parseMoney(sum.amt_ord);
			sum.profit = $.parseMoney(sum.profit);
			sum.adjusted_amount = $.parseMoney(sum.adjusted_amount);
			
			if(line.length) {
				line.push(sum);
				result.push(obj);
				
				ordersum.sum_amtys_ord_total += parseFloat(sum.amt_ord);
				ordersum.amt_ord_profit += parseFloat(sum.profit);
			}
		}
		
		if(currentContent) {
			currentContent.find("#qty_ord_total").text($.round(ordersum.sum_qty_ord_total,1));
			currentContent.find("#amt_ord_total").text($.parseMoney(ordersum.sum_amtys_ord_total));
			currentContent.find("#amt_profit_total").text($.parseMoney(ordersum.amt_ord_profit));
		}
		return result;
	}
	
	function showAnalysisDetails(result) {
		if(result.length <= 0 ){
			box.showAlert({message: "对不起，没有查询到数据！"});
		}
		if(!currentContent) {
			currentContent=box.ich.view_xwfx_detail({result:result,operate_result:operateResult, item_kind_list:box.kinds.array});
			currentContent=box.showDialog({
				title:operateResult.consumer_name + "的消费流水",
				width:850,
				height:520,
				content: currentContent,
				close: function() {
					currentContent = null;
				}
			});
			$("#diabegin_date").datepicker({
				maxDate: 'd' 
			});
			$("#diaend_date").datepicker({
				maxDate: 'd' 
			});
			currentContent.find("#diabegin_time").timepicker();
			currentContent.find("#diaend_time").timepicker();
			currentContent.find("#diabegin_time").val(diaBeginTime);
			currentContent.find("#diaend_time").val(diaEndTime);
			currentContent.find("#diabegin_date").val(diaBeginDate);
			currentContent.find("#diaend_date").val(diaEndDate);
			currentContent.find("#consumerFilter").unbind("click").click(getCsmBehaviorAnalysisDetails);
		} else {
			currentContent.find("#diabegin_time").val(diaBeginTime);
			currentContent.find("#diaend_time").val(diaEndTime);
			currentContent.find("#diabegin_date").val(diaBeginDate);
			currentContent.find("#diaend_date").val(diaEndDate);
			var tableview = box.ich.view_xwfx_detail_table({result:result});
			currentContent.find("#view_xwfx_detail_table").replaceWith(tableview);
		}
	}
	
	function onFilter() {
		var start = currentContent.find("#beginTime").val();
		var end = currentContent.find("#endTime").val();
		var kindid = currentContent.find("#item_kind").val();
		
		start = timeFormat(start);
		end = timeFormat(end);
		showAnalysisDetails(filterDetails(currentResult, start, end, kindid));
	}
	
	//
	function timeFormat(myTime){
		var newTimeArr = myTime.split(":");
		var newTime = "";
		for ( var i = 0; i < newTimeArr.length; i++) {
			if(newTimeArr[i].length == 1){
				newTimeArr[i] = "0"+newTimeArr[i];
			}
			newTime += newTimeArr[i];
		}
		return newTime;
	}
	
	function dateFormat(myDate){
		var newDate = "";
		if(myDate.length==8){
			newDate = new Date(myDate .substr(0, 4), myDate.substr(4, 2)-1, myDate.substr(6, 2));
			newDate = newDate.format("yyyy-MM-dd");
		}else{
			newDate = new Date(myDate.substr(0, 4), myDate.substr(5, 2)-1, myDate.substr(8, 2));
			newDate = (newDate).format("yyyyMMdd");
		}
		return newDate;
	}

	return {
		init: function(){
			box.listen("xwfx", onload);
		},
		destroy: function() { }
	};
});