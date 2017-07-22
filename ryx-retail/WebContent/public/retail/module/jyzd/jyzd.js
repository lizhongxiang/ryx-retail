/**
 * 经营指导功能
 */
HOME.Core.register("plugin-jyzd", function(box) {
	
	var jyzdUrl = "retail/statistics/searchGMerchSqGatherRranking";
	var parentView = null;
	var gjList= [];
	var xsList = [];
	var ylList = [];
	var dtList = [];
	var map = {};
	function loadJYZD(view){
		parentView= view;
		gjList= [];
		xsList = [];
		ylList = [];
		dtList = [];
		map = {};
		getRankList();
	}
	
	function getRankList(){
		var param = {};
		box.request({
			url: box.getContextPath() + jyzdUrl,
			data:{params:$.obj2str (param)},
			success: onGetRankList
		});
	}
    
	function onGetRankList(data){
		if(data && data.code=="0000"){
			var result = data.result;
			if(result.list){
				AnalysisData(result);
			}
			var content = box.ich.view_yxhd_jyzd({gj_gather:gjList, xs_gather:xsList, yl_gather:ylList, dt_gather:dtList, my_gather:map, guide_information:result.guide_information });
			parentView.empty().append(content);
		}
	}
	
	//检查数据
	function AnalysisData(result){
		
		var gjSymbol = ComparativeRanking(result.qty_pm, result.qty_pm_last);
		var xsSymbol = ComparativeRanking(result.sale_pm, result.sale_pm_last);
		var dtSymbol = ComparativeRanking(result.avg_pri_pm, result.avg_pri_pm_last);
		var ylSymbol = ComparativeRanking(result.profit_pm, result.profit_pm_last);
		
		map["qty_pm"] = result.qty_pm;
		map["qty_ord"] = result.qty_ord;
		map["sale_pm"] = result.sale_pm;
		map["qty_sale"] = result.qty_sale;
		map["profit_pm"] = result.profit_pm;
		map["profit"] = result.profit;
		map["avg_pri_pm"] = result.avg_pri_pm;
		map["avg_pri"] = result.avg_pri;
		map["gj_pm_text"] = gjSymbol;
		map["xs_pm_text"] = xsSymbol;
		map["dt_pm_text"] = dtSymbol;
		map["yl_pm_text"] = ylSymbol;
		
		var resultList = result.list;
		for ( var i = 0; i < resultList.length; i++) {
			var merchName = "A店";
			if(resultList[i].pm == 1){
				merchName = "A店";
			}
			if(resultList[i].pm == 2){
				merchName = "B店";
			}
			if(resultList[i].pm == 3){
				merchName = "C店";
			}
			if(resultList[i].pm == 4){
				merchName = "D店";
			}
			if(resultList[i].pm == 5){
				merchName = "E店";
			}
			resultList[i]["merch_name"] = merchName;
			resultList[i].pm_text = ComparativeRanking(resultList[i].pm, resultList[i].pm_last);
			if(resultList[i].gather_type == 'DT' ){
				dtList.push(resultList[i]);
			}
			if(resultList[i].gather_type == 'YL' ){
				ylList.push(resultList[i]);
			}
			if(resultList[i].gather_type == 'GJ' ){
				gjList.push(resultList[i]);
			}if(resultList[i].gather_type == 'XS' ){
				xsList.push(resultList[i]);
			}
			if(resultList[i].gather_type == "TJ"){
				map['sum_num'] = resultList[i].pm_last;
			}
		}
	}
	
	//排名比较
	function ComparativeRanking(newPm, lastPm){
		var Symbol = "—";
		if(parseFloat(newPm) < parseFloat(lastPm)){
			Symbol = "↑";
		}
		if(parseFloat(newPm) > parseFloat(lastPm)){
			Symbol = "↓";
		}
		return Symbol;
	}
	
	return {
		init: function(){
			box.listen("jyzd", loadJYZD);
		},
		destroy: function() { }
	};
});