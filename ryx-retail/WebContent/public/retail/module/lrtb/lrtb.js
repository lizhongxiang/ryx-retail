/**
 * 利润图表
 */
HOME.Core.register("plugin-lrtb", function(box) {
	
	var highchartsUrl = "public/js/common/highcharts-all1.js";
	
	var getChartsUrl = "retail/statistics/profitReportForms";
	
	var parentView = null;
	var container = null;
	var top_type = null;
	var selectType=null;
	function onload(view) {
		parentView = view;
		parentView.showFooter(true);
		top_type = null;
		loadChartsJs();
	}
	
	function loadChartsJs() {
		box.notify({
			type: HOME.Const.LOAD_JSPLUGINS,
			data: {
				url: highchartsUrl, 
				success: onGetChart
			}
		});
	}
	
	function onGetChart(e){
		params = {'time_interval': 'day', 'number': '15'};
		top_type = '最近15天';
		selectType = '最近15天';
		if(e != undefined){
			var $target = $(e.currentTarget);
			var type = $target.attr("data-id");
			var option_selected = $target.find("option:selected").text();
			
			if(type == "halfMonth"){
				params = {'time_interval': 'day', 'number': '15'};
				top_type = '最近半个月';
				selectType = '最近15天';
			}else if(type == 'oneMonth'){
				params = {'time_interval': 'day', 'number': '30'};
				top_type = '最近一个月';
				selectType = '最近30天';
			}else if(type == 'threeMonth'){
				params = {'time_interval': 'week', 'number': '13'};
				top_type = '最近三个月';
				selectType = '最近13周';
			}else if(type == 'sixMonth'){
				params = {'time_interval': 'monthly', 'number': '6'};
				top_type = '最近六个月';
				selectType = '最近6个月';
			}
			
			if(option_selected == "最近5天"){
				params = {"time_interval": "day", "number": "5"};
				top_type = '最近5天';
				selectType = '最近5天';
			}else if(option_selected == "最近10天"){
				params = {"time_interval": "day", "number": "10"};
				top_type = '最近10天';
				selectType = '最近10天';
			}else if(option_selected == "最近15天"){
				params = {"time_interval": "day", "number": "15"};
				top_type = '最近15天';
				selectType = '最近15天';
			}else if(option_selected == "最近20天"){
				params = {"time_interval": "day", "number": "20"};
				top_type = '最近20天';
				selectType = '最近20天';
			}else if(option_selected == "最近30天"){
				params = {"time_interval": "day", "number": "30"};
				top_type = '最近30天';
				selectType = '最近30天';
			}else if(option_selected == "最近5周"){
				params = {"time_interval": "week", "number": "5"};
				top_type = '最近5周';
				selectType = '最近5周';
			}else if(option_selected == "最近10周"){
				params = {"time_interval": "week", "number": "10"};
				top_type = '最近10周';
				selectType = '最近10周';
			}else if(option_selected == "最近13周"){
				params = {"time_interval": "week", "number": "13"};
				top_type = '最近13周';
				selectType = '最近13周';
			}else if(option_selected == "最近15周"){
				params = {"time_interval": "week", "number": "15"};
				top_type = '最近15周';
				selectType = '最近15周';
			}else if(option_selected == "最近20周"){
				params = {"time_interval": "week", "number": "20"};
				top_type = '最近20周';
				selectType = '最近20周';
			}else if(option_selected == "最近6个月"){
				params = {"time_interval": "monthly", "number": "6"};
				top_type = '最近6个月';
				selectType = '最近6个月';
			}else if(option_selected == "最近12个月"){
				params = {"time_interval": "monthly", "number": "12"};
				top_type = '最近12个月';
				selectType = '最近12个月';
			}else if(option_selected == "最近18个月"){
				params = {"time_interval": "monthly", "number": "18"};
				top_type = '最近18个月';
				selectType = '最近18个月';
			}else if(option_selected == "最近24个月"){
				params = {"time_interval": "monthly", "number": "24"};
				top_type = '最近24个月';
				selectType = '最近24个月';
			}
		}
		
		params.mid = box.user.merch_id;
		box.request({
			url: box.getContextPath() + getChartsUrl,
			data:{params:$.obj2str(params)},
			type: "get",
			expires: "d",
			success: showChartView
		});
	}
	
	function showChartView(data) {
		
		if(data && data.code == "0000") {
			var results = data.result;
			
			var settlement_date = [];//时间
			var profitAmount = [];//全部
			var tobaccoProfit=[];//卷烟
			var cigarProfit=[];//雪茄
			var otherProfit=[];//非烟
//{other_profit=16.8, total_profit=144.1, tobacco_profit=127.3, settlement_date=20140510}
			for(var i = 0; i < results.length; i ++){
				settlement_date[i] = results[i].settlement_date;
				if(results[i].total_profit){
					profitAmount[i] = parseFloat(results[i].total_profit);
				}else{
					profitAmount[i] = 0;
				}
				if(results[i].tobacco_profit){
					tobaccoProfit[i]=parseFloat(results[i].tobacco_profit);
				}else{
					tobaccoProfit[i]=0;
				}
				if(results[i].cigar_profit){
					cigarProfit[i]=parseFloat(results[i].cigar_profit);
				}else{
					cigarProfit[i]=0;
				}
				if(results[i].other_profit){
					otherProfit[i]=parseFloat(results[i].other_profit);
				}else{
					otherProfit[i]=0;
				}
			}			
			var content = box.ich.view_lrtb();
			parentView.empty().append(content);
			
			parentView.find(".view_footer input").unbind("click").click(onGetChart);
			parentView.find(".view_footer select").unbind("change").change(onGetChart);
			container = parentView.find("#chartContainer");
			parentView.find(".view_footer select").find("option[data-id='"+selectType+"']").attr("selected",true);
			
			container.highcharts({
	            chart: {
	                type: 'line'
	            },
	            credits: {
	            	enabled: false
	            },
	            title: {
	                text: '利润统计报表'+'('+top_type+',不含今日)'
	            },
	            subtitle: {
	                text: ''
	            },
	            xAxis: {
	                categories: settlement_date,
	                labels: {
	                    rotation: -45,
	                    align: 'right',
	                    style: {
	                        fontSize: '12px',
	                        fontFamily: 'Verdana, sans-serif'
	                    }
	                }
	            },
	            yAxis: {
	                title: {
	                    text: '利润总额'
	                }
	            },
	            tooltip: {
	            	enabled: true,
	            	crosshairs: true,
	            	shared: true
//	                formatter: function() {
//	                	 return '时间 : <b>'+ this.x +'</b><br/>'+
//	                         '总利润额 : <b>'+Highcharts.numberFormat( this.y,2) +'</b>';
////	                         '卷烟利润额 : <b>'+Highcharts.numberFormat( this.y,2) +'</b>'+
////	                         '非烟利润额 : <b>'+Highcharts.numberFormat( this.y,2) +'</b>';
//	                	 // Highcharts.numberFormat(this.y, 1);
//	                }
	            },
	            plotOptions: {
	            	line: {
	                    dataLabels: {
	                        enabled: true,
	                        formatter:function(){
	                        	return Highcharts.numberFormat( this.y,2) ;
	                        }
	                    },
	                    enableMouseTracking: true
	                }
	            },
	            marker:{
	            	enabled:false,//是否显示点
	            	radius:3,//点的半径
	            	states:{  
		            	hover:{  
		            		enabled:true//鼠标放上去点是否放大  
		            	},
		            	select:{  
			            	enabled:false//控制鼠标选中点时候的状态  
		            	}
	            	}
	            },

	            states:{  
		            hover:{  
			            enabled:true,//鼠标放上去线的状态控制  
			            lineWidth:100
		            }  
	            },
	            series: [
	                     {
	                    	 name: '<lable style="font-size:15px;">全部利润额</lable>',
	                    	 data: profitAmount
	                     },
	                     {
	                    	 name: '<lable style="font-size:15px;">卷烟利润额</lable>',
	                    	 data: tobaccoProfit
	                     },
	                     {
	                    	 name: '<lable style="font-size:15px;">雪茄利润额</lable>',
	                    	 data: cigarProfit
	                     },
	                     {
	                    	 name: '<lable style="font-size:15px;">非烟利润额</lable>',
	                    	 data: otherProfit
	                     }
	            ]
	        });
		}
	}
	function onclose(){
		parentView = null;
		container = null;
		top_type = null;
		selectType=null;
	}
	return {
		init: function(){
			box.listen("lrtb", onload);
			box.listen("lrtb_close",onclose);
		},
		destroy: function() { }
	};
});