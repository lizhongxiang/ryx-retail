/**
 * 利润报表
 */
HOME.Core.register("plugin-lrbb", function(box) {
	var getSaleReportTableUrl="retail/statistics/searchProfitRecords";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var itemKind="00";
	var keyWord = "";
	var minCost = "";
	var maxCost = "";
	
	function onload(view) {
		parentView = view;
		showTable();
	}
	//显示明细报表
	function showTable(){
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
		
		var params={'start_date':beginTimeFormat,'end_date':endTimeFormat};
		
		itemKind=parentView.find("#item_kind").val();
		
		if(!itemKind){
			itemKind = "01";
		}
		if(itemKind && itemKind != "00"){
			params.item_kind_id = itemKind;
		}
		keyWord=parentView.find("#keyword").val();
		if(keyWord){
			params.keyword = keyWord;
		}
		minCost=parentView.find("#min_cost").val();
		maxCost=parentView.find("#max_cost").val();
		if(minCost){
			params.min_cost = minCost;
		}
		if(maxCost){
			params.max_cost = maxCost;
		}
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	function successJXCReportTable(data){
		parentView.find("#selewares").removeAttr("disabled");
		
		if(data && data.code=="0000") {
			if(data.result.profit_list.length<=0){
				  box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			content=box.ich.view_lrbb({result:data.result,item_kind_list:box.kinds.array});
			if(itemKind) {
				content.find("#item_kind").val(itemKind);
			}
			content.find("#min_cost").val(minCost);
			content.find("#max_cost").val(maxCost);
			content.find("#keyword").val(keyWord);
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
			parentView.find("#selewares").unbind("click", showTable).click(showTable);

			parentView.find(".table_view").tablesort([
			       		        {col:1,order:"asc",method:"advance",type:"number"},
			       		        {col:3,order:"desc",method:"advance",type:"number"},
			       		        {col:4,order:"desc",method:"advance",type:"number"},
			       		        {col:5,order:"desc",method:"advance",type:"number"},
			       		        {col:6,order:"desc",method:"advance",type:"number"},
			       		        {col:7,order:"desc",method:"advance",type:"number"},
			       		        {col:8,order:"desc",method:"advance",type:"number"},
			       		        {col:9,order:"desc",method:"advance",type:"number"}
			       		]);
			

			parentView.showFooter(true);
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			}).fixedtablefooter({
				parent: parentView,
				win: parentView.parent(),
				offset: -1
			});
			parentView.showAdvancedSearch();
		}
	}
	function onGetTicket(){
		box.request({
			url: box.getContextPath() + selectTicketUrl,
			success:onGetTicketData
		});
	}
	function onBarcode(data) {
		var itemBar = data.code;
		
		var beginTime=parentView.find("#beginTime").val();
		var endTime=parentView.find("#endTime").val();
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		var params={'start_date':beginTimeFormat,'end_date':endTimeFormat, 'item_bar':itemBar };
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	function onclose(){
		parentView = null;
		content = null;
		beginTime="";
		endTime="";
		itemKind="00";
		keyWord = "";
		minCost = "";
		maxCost = "";
	}
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.BARCODE:
				onBarcode(data);
				break;
		}
	}
	return {
		init: function(){
			box.listen("lrbb", onload);
			box.listen("lrbb_close", onclose);
			box.listen("lrbb_onkey", onKey);
		},
		destroy: function() { }
	};
});