/**
 * 今日统计分析功能
 */
HOME.Core.register("plugin-jrtj", function(box) {
	
	var statisticsURL="retail/push/analysisBusiness";
	var content=null;
	var parentView = null;
	function onload(view) {
		parentView = view;
		//var content = box.ich.view_jrtj();
		//parentView.empty().append(content);
		//parentView.showFooter(true);
		selectCurrentStatistics();
	}

	function selectCurrentStatistics(){
		box.request({
			url : box.getContextPath() + statisticsURL,
			success : function(json) {
				if(json && json.code=="0000") {
					var result = json.result;
					if(result) {
						result.profit_today=$.parseMoney(result.profit_today);//当日利润总额
						result.profit_tobacco=$.parseMoney(result.profit_tobacco);//卷烟利润
						result.profit_other=$.parseMoney(result.profit_other); //非烟利润
						result.sale_today=$.parseMoney(result.sale_today);//当日销售总额
						result.sale_online=$.parseMoney(result.sale_online);  //当日线上销售总额
						result.sale_offline=$.parseMoney(result.sale_offline);//当日线下总额
						result.purch_today=$.parseMoney(result.purch_today);//进货总额
						result.whse_tobacco= $.parseMoney(parseFloat(result.whse_tobacco)); // 卷烟库存额
						result.whse_cigar = $.parseMoney(parseFloat(result.whse_cigar)); // 雪茄库存额
						result.whse_other = $.parseMoney(parseFloat(result.whse_other)); // 非烟库存额
						// 在后台统计的库存总数与各分项之和不等, 改为库存总数直接由分项相加
						//result.whse_today = $.parseMoney(tobacooInventoryAmount+cigarInventoryAmount+otherInventoryAmount);
						result.whse_today = $.parseMoney(result.whse_today);
						result.sum_adjusted_amount = $.parseMoney(result.sum_adjusted_amount);//当日利润总额
						result.return_amount = $.parseMoney(result.return_amount);//当日利润总额	
					};
					
					content=box.ich.view_jrtj({currentDailyCountList:result});
					parentView.empty().append(content);
						
					
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
	}
	
	function onSendMessage() {
		var txt = sendtxt.val();
		box.comm.send({type: "showmessage", message: txt});
	}
	return {
		init: function(){
			box.listen("jrtj", onload);
		},
		destroy: function() { }
	};
});