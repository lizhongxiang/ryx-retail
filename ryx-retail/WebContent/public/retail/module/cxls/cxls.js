/**
 * 促销流水功能
 */
HOME.Core.register("plugin-cxls", function(box) {
	// 接口地址
	var searchRecordUrl="retail/promotion/searchMerchPromotionRecord";
	
	// 页面元素
	var parentView = null;
	var beginDate="";
	var endDate="";
	var orderId = "";
	var promotionType = "";
	var content = null;
	var pageIndex = "1";
	var pageSize = "20";
	
	function onload(view) {
		parentView = view;
		pageIndex = 1;
		pageSize = 20;
		beginDate="";
		endDate="";
		orderId = "";
		promotionType = "";
		parentView.showFooter(true);
		getCXLS();
	}
	
	function getCXLS(){
		orderId = $("#order_id").val();
		beginDate = $("#begin_date").val();
		endDate = $("#end_date").val();
		promotionType = $("#promotion_type").val();
		
		if (!beginDate) {
			beginDate=$.GetToday(-6);
		}
		if (!endDate) {
			endDate=$.GetToday();
		}
		
		var params = {page_size:pageSize, page_index:pageIndex};
		if(beginDate ){
			var bd = new Date(beginDate.substr(0, 4),beginDate.substr(5, 2)-1,beginDate.substr(8, 2));
			params.start_date = (bd).format("yyyyMMdd");
		}
		if(endDate){
			var ed = new Date(endDate.substr(0, 4),endDate.substr(5, 2)-1,endDate.substr(8, 2));
			params.end_date = (ed).format("yyyyMMdd");
		}
		if(orderId){
			params.order_id = orderId;
		}
		if(promotionType){
			params.promotion_type = promotionType;
		}
		box.request({
			url:box.getContextPath()+ searchRecordUrl,
			data:{params:$.obj2str (params)},
			success: showCXLS
		});
	}
	
	function showCXLS(data){
		
		if(data && data.code=="0000"){
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getCXLS();
			});
			var result = data.result;
			if(!result || result.length == 0){
				box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			for ( var i = 0; i < result.length; i++) {
//				result[i].index = i+1;
				result[i].index = i+1+((pageIndex-1)*pageSize);
				var date = result[i].record_date ; 
				date = new Date(date .substr(0, 4), date.substr(4, 2)-1, date.substr(6, 2));
				date = date.format("yyyy-MM-dd");
				var time = result[i].record_time ;
				time = time.substr(0, 2) +":"+ time.substr(2, 2);
				result[i].data_time = date+" "+time;
				
				result[i].promotion_key = $.parseMoney(result[i].promotion_key);
				result[i].amtys_ord_total = $.parseMoney(result[i].amtys_ord_total);
				result[i].amt_ord_profit = $.parseMoney(result[i].amt_ord_profit);
				result[i].promotion_type_text = box.labels.promotiontype[result[i].promotion_type];
			}
			content = box.ich.view_cxls({result:result});
			parentView.empty().append(content);
		}else{
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		
		if(beginDate==undefined || beginDate=="") {
			beginDate=$.GetToday(-6);
		}
		if(endDate==undefined || endDate=="") {
			endDate=$.GetToday();
		}
		if(beginDate>endDate){
			box.showAlert({message:"开始日期不能大于结束日期！"});
			return;
		}
		$("#order_id").val(orderId);
		$("#begin_date").val(beginDate);
		$("#end_date").val(endDate);
		$("#promotion_type").val(promotionType);
		//初始日期控件
		content.find("#begin_date").datepicker({
			maxDate: 'd' 
		});
		//初始日期控件
		content.find("#end_date").datepicker({
			maxDate: 'd' 
		});
		$("#query").unbind("click").click(getCXLS);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		
	}
	
	return {
		init: function(){
			box.listen("cxls", onload);
		},
		destroy: function() { }
	};
	
});