/**
 * 积分规则
 */
HOME.Core.register("plugin-dhls", function(box) {
	
	var searchScoreExchangeUrl = "retail/consumer/searchScoreExchange";//查询商品兑换流水
	
	var startDate = $.GetToday(-6);
	var endDate = $.GetToday();
	var prizeName = "";
	var pageIndex = "1";
	var pageSize = "20";
	
	function onload(view) {
		gradeindex = 0;
		parentView = view;
		pageIndex = "1";
		pageSize = "20";
		startDate = $.GetToday(-6);
		endDate = $.GetToday();
		prizeName = "";
		parentView.showFooter(true);
		getExchangeFlow();	
	}
	
	function getExchangeFlow(){
		var params = {page_size:pageSize, page_index:pageIndex};
		var newStartDate = new Date(startDate.substr(0, 4),startDate.substr(5, 2)-1,startDate.substr(8, 2));
		newStartDate = (newStartDate).format("yyyyMMdd");
		var newEndDate = new Date(endDate.substr(0, 4),endDate.substr(5, 2)-1,endDate.substr(8, 2));
		newEndDate= (newEndDate).format("yyyyMMdd");
		if(newStartDate > newEndDate){
			box.showAlert({message:"对不起，开始时间不可以大于结束时间！"});
			return ;
		}
		params.start_date = newStartDate;
		params.end_date = newEndDate;
		if(prizeName){
			params.prize_name = prizeName;
		}
		box.request({
			url:box.getContextPath()+ searchScoreExchangeUrl,
			data:{params:$.obj2str (params)},
			success: showExchangeFlow
		});
	}
	
	function showExchangeFlow(data){
		if(data && data.code=="0000"){
			var result = data.result;
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getExchangeFlow();
			});
			if(!result || result.length == 0){
				box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			for(var i=0; i < result.length; i++) {
				result[i].index = i + 1;
				
				var date = result[i].exchange_date ; 
				date = new Date(date .substr(0, 4), date.substr(4, 2)-1, date.substr(6, 2));
				date = date.format("yyyy-MM-dd");
				var time = result[i].exchange_time ;
				time = time.substr(0, 2) +":"+ time.substr(2, 2);
				result[i].data_time = date+" "+time;
			}
			var content = box.ich.view_dhls({list:result});
			parentView.empty().append(content);
			$("#beginTime").val(startDate);
			$("#endTime").val(endDate);
			$("#prize_name").val(prizeName);
			$("#query").unbind("click").click(function(){
				startDate = $("#beginTime").val();
				endDate = $("#endTime").val();
				prizeName = $("#prize_name").val();
				
				getExchangeFlow();
			});
		}else{
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		parentView.find("#beginTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find("#endTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		
	}
	
	return {
		init: function(){
			box.listen("dhls", onload);
		},
		destroy: function() { }
	};
});