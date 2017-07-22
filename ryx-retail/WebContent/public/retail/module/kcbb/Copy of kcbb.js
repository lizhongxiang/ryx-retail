/**
 * 进销存明细表
 */
HOME.Core.register("plugin-kcbb", function(box) {
	var getSaleReportTableUrl="retail/statistics/searchWhseRecords";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var itemKind="";
	var keyWord = ""; 
	
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
		
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	function successJXCReportTable(data){
		
		parentView.find("#selewares").removeAttr("disabled");
		
		if(data && data.code=="0000") {
			if(data.result.whse_list.length<=0){
				  box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			var result = data.result;
			for ( var i = 0; i < result.whse_list.length; i++) {
				if(parseFloat(result.whse_list[i].begining_whse_quantity) <= 0 ){
					result.whse_list[i].begining_whse_quantity = 0.0;
					result.whse_list[i].begining_whse_amount = 0.00;
				}
				if(parseFloat(result.whse_list[i].ending_whse_quantity) <= 0){
					result.whse_list[i].ending_whse_quantity = 0.0;
					result.whse_list[i].ending_whse_amount = 0.00;
				}
			}
			content=box.ich.view_kcbb({result:result, item_kind_list:box.kinds.array});
			if(itemKind) {
				content.find("#item_kind").val(itemKind);
			}
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
			       		        {col:3,order:"asc",method:"advance",type:"number"},
			       		        {col:4,order:"asc",method:"advance",type:"number"},
			       		        {col:5,order:"asc",method:"advance",type:"number"},
			       		        {col:6,order:"asc",method:"advance",type:"number"},
			       		        {col:7,order:"asc",method:"advance",type:"number"},
			       		        {col:8,order:"asc",method:"advance",type:"number"},
			       		        {col:9,order:"asc",method:"advance",type:"number"}
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
		itemKind="";
		keyWord = "";
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
			box.listen("kcbb", onload);
			box.listen("kcbb_close", onclose);
			box.listen("kcbb_onkey", onKey);
		},
		destroy: function() { }
	};
});