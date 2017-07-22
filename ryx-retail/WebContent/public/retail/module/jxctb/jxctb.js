/**
 * 统计分析功能
 */
HOME.Core.register("plugin-jxctb", function(box) {
	
	var highchartsUrl = "public/js/common/highcharts-all1.js";
	
	var getChartsUrl = "retail/statistics/jxcreportforms";
	
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
			var purch_amount=[];//进
			var sale_amount=[];//销
			var whse_amount=[];//存
			var purchPie=0.0;//饼图 进
			var salePie=0.0;//销
			var whsePie=0.0;//存
			for(var i = 0; i < results.length; i ++){
				settlement_date[i] = results[i].settlement_date;
				purchPie+=parseFloat(results[i].purch_amount);
				salePie+= parseFloat(results[i].sale_amount);
				whsePie+=parseFloat(results[i].whse_amount);
				purch_amount[i] = parseFloat(results[i].purch_amount);//进
				sale_amount[i] = parseFloat(results[i].sale_amount);//销
				whse_amount[i] = parseFloat(results[i].whse_amount);//存
			}
//			var lastWhseAmount = 0;
//			var secondLastWhseAmount = 0;
//			var gap = 0;
//			var whseAmountLastIndex = whse_amount.length-1;
//			for (var j=whseAmountLastIndex; j>=0; j--) {
//				if (j==whseAmountLastIndex) {
//					lastWhseAmount = whse_amount[j];
//				} else if (j==whseAmountLastIndex-1) {
//					secondLastWhseAmount = whse_amount[j];
//					gap = secondLastWhseAmount-lastWhseAmount;
//				}
//				if(gap>10000) {
//					whse_amount[j] = parseFloat((whse_amount[j]-gap+500).toFixed(2));
//				} else if(j!=whseAmountLastIndex) {
//					break;
//				}
//			}
			var content = box.ich.view_jxctb();
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
	                text: '进销存统计报表'+'('+top_type+',不含今日)'
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
	            yAxis: [
			            {
			                title: {
			                    text: '进销存额'
			                }
			            }
	            ],
	            tooltip: {
	            	 crosshairs: true,
	            	 shared: true
//	            	 formatter: function() {
//	                     var s;
//	                     if (this.point.name) { // the pie chart
//	                    	 s = ''+ Highcharts.numberFormat( this.y,2);
//	                     } else {
//	                         s = '时间 : <b>'+ this.x +'</b><br/>'+
//	                         '金额 : <b>'+Highcharts.numberFormat( this.y,2) +'</b>';
//	                     }
//	                     return s;
//	                 }
	            },
	            plotOptions: {
	            	line: {
	                    dataLabels: {
	                        enabled: true
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
//						{
//						    type: 'pie',                                                  
//						    name: 'fewrfefsadfadsfadf',                                    
//						    data: [
//						       {                                                      
//							        name: '进货总额',                                             
//							        y: purchPie,
//							        color: Highcharts.getOptions().colors[0] // Jane's color  
//							    }, {                                                          
//							        name: '销售总额',                                             
//							        y: salePie, 
//							        color: Highcharts.getOptions().colors[1] // John's color  
//							    }, {                                                          
//							        name: '入库总额',                                              
//							        y: whsePie,
//							        color: Highcharts.getOptions().colors[2] // Joe's color   
//							    }
//							],
//							center: [100, 80],                                            
//				            size: 100,                                                    
//				            showInLegend: true,                                          
//				            dataLabels: {                                                 
//				                enabled: false                                            
//				            }
//						},
	                    {
		                	name: '<lable style="font-size:15px;">进货额</lable>',
		                	data: purch_amount
		            	},
		            	{
			                name: '<lable style="font-size:15px;">销售额</lable>',
			                data: sale_amount
			            },
			            {
			                name: '<lable style="font-size:15px;">库存额</lable>',
			                data: whse_amount
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
			box.listen("jxctb", onload);
			box.listen("jxctb_close",onclose);
		},
		destroy: function() { }
	};
});