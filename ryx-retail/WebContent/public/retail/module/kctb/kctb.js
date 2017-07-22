/**
 * 统计分析功能
 */
HOME.Core.register("plugin-kctb", function(box) {
	
	var highchartsUrl = "public/js/common/highcharts-all1.js";
	
	var getChartsUrl = "retail/statistics/stockReport";
	
	var parentView = null;
	var container = null;
	var selectType=null;
	function onload(view) {
		parentView = view;
		parentView.showFooter(true);
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
		
		box.request({
			url: box.getContextPath() + getChartsUrl,
			success: showChartView
		});
	}
	
	function showChartView(data) { 
		if(data && data.code == "0000") {
			var results = data.result;
			
			var period = [];
			var salerooms = [];
			var ckNum=[];
			var sumMoney=null;
			var sumQtyWhse=null;
			for(var i = 0; i < results.length; i ++){	
				period[i] = results[i].item_kind_name;
				sumMoney=results[i].sum_money;
				sumQtyWhse=results[i].sum_qty_whse;
				if(!sumMoney||sumMoney==0){
					sumMoney="0.00";
				}
				if(!sumQtyWhse||sumQtyWhse==0){
					sumQtyWhse="0.00";
				}
				salerooms[i] = parseFloat(parseFloat(sumMoney).toFixed(2));
				ckNum[i]=parseFloat(parseFloat(sumQtyWhse).toFixed(2));
			}
			var content = box.ich.view_kcbb();
			parentView.empty().append(content);
			container = parentView.find("#chartContainer");			
			container.highcharts({
	            chart: {
	                type: 'column'
	            },
	            credits: {
	            	enabled: false
	            },
	            title: {
	                text: '库存统计报表'
	            },
	            subtitle: {
	                text: ''
	            },
	            xAxis: {
	                categories: period,
	                labels: {
	                    rotation: -45,
	                    align: 'right',
	                    style: {
	                        fontSize: '12px',
	                        fontFamily: 'Verdana, sans-serif'
	                    },
	                },	                
	            },
	            yAxis: [ 
	                     { //设置Y轴-第一个（增幅） 
			                labels: { 
			                    formatter: function() { //格式化标签名称 
			                        return this.value + ''; 
			                    }
			                }, 
			                title: {text: '库存情况'}//Y轴标题设为空		
			               
		        		},	 
						{ //设置Y轴-第一个（增幅） 
							labels: { 
							    formatter: function() { //格式化标签名称 
							        return this.value + ''; 
							    }
							}, 
							title: {text: '数量情况'},//Y轴标题设为空 	
							 opposite: true
						}
	            	], 	               
	            tooltip: {
	            	enabled: true,
	                formatter: function() {
	                	var info; 
	                	if(this.series.name=='库存额'){
	                		 info='类型 : <b>'+ this.x +'</b><br/>'+
	                         ' <b>金额：'+Highcharts.numberFormat( this.y,2) +'</b><br/>';
	                	}else{
	                		 info= '类型 : <b>'+ this.x +'</b><br/>'+
	                         ' <b>数量：'+Highcharts.numberFormat( this.y,2) +'</b><br/>';
	                	}
	                	return info;                 
	                	 // Highcharts.numberFormat(this.y, 1);
	                }
	            },
	            legend : {//设置图例
	            	layout : 'horizontal',//显示形式，支持水平horizontal(默认)和垂直vertical
	            	align : 'center',// left/right/center/
	            	verticalAlign : 'bottom',//bottom  top
	            	y : 10,//纵轴方向的偏移
	            	borderWidth : 1
	            	//图例的边框宽度
	            },
	            plotOptions: {
	            	column:{ 
	            		dataLabels:{ 
	            			enabled:true //是否显示数据标签 
	            		}
	            	},
	            		
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
	            series:[
		            {
		            	name: '库存额',
//		                yAxis: 2,//坐标轴序号
		                data: salerooms
		            },
		            {
		            	 name: '数量值',
//		            	 yAxis:1,
		            	data:ckNum,
		            	center: [100, 80],                                            
		                size: 100,                                                    
		                showInLegend: true,                                          
		                dataLabels: {                                                 
		                    enabled: true                                            
		                }          
		            }
		        ]
	        });
		}
		
	}
	function onclose(){
		 parentView = null;
		 container = null;
		 selectType=null;
	}
	return {
		init: function(){
			box.listen("kctb", onload);
			box.listen("kctb_close", onclose);
		},
		destroy: function() { }
	};
});