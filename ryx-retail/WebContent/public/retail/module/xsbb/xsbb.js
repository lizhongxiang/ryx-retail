/**
 * 进销存明细表
 */
HOME.Core.register("plugin-xsbb", function(box) {
	var getSaleReportTableUrl="retail/statistics/searchSalesRecords";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var itemKind = "";
	var keyWord = "";
	var minSaleAmount = "";
	var maxSaleAmount = "";
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
		minSaleAmount=parentView.find("#min_sale_amount").val();
		maxSaleAmount=parentView.find("#max_sale_amount").val();
		if(minSaleAmount){
			params.min_sale_amount = minSaleAmount;
		}
		if(maxSaleAmount){
			params.max_sale_amount = maxSaleAmount;
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
			if(data.result.sale_list.length<=0){
				  box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			content=box.ich.view_xsbb({result:data.result,item_kind_list:box.kinds.array});
			if(itemKind) {
				content.find("#item_kind").val(itemKind);
			}
			content.find("#keyword").val(keyWord);
			content.find("#item_kind").val();
			content.find("#min_sale_amount").val(minSaleAmount);
			content.find("#max_sale_amount").val(maxSaleAmount);
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
		itemKind = "";
		keyWord = "";
		minSaleAmount = "";
		maxSaleAmount = "";
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
			box.listen("xsbb", onload);
			box.listen("xsbb_close", onclose);
			box.listen("xsbb_onkey", onKey);
		},
		destroy: function() { }
	};
});