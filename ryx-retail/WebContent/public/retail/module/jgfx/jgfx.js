/**
 * 结构分析
 */
HOME.Core.register("plugin-jgfx", function(box) {
	
	
	var highchartsUrl = "public/js/common/highcharts-all1.js";
	
	var getChartsUrl = "retail/statistics/profitReportForms";
	
	var ageChartsUrl="retail/customer/searchCustomerByAgeGender";//按年龄分组url
	var moneyChartsUrl="retail/customer/searchCustomerByMoney";//按收入分组url
	var eduChartsUrl="retail/customer/searchCustomerByEdu";//按学历分组url
	
	var parentView = null;
	var container = null;
	var top_type = null;
	var selectType=null;
	
	var salaryContent=box.ich.view_jgfx_salary();
	var degreeContent=box.ich.view_jgfx_salary();
	
	
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
				success: onGetChartAge
			}
		});
	}

	//点击事件
	function onClickRadio(){
		parentView.find(".salary").unbind("click").click(function(){
			onGetChartSalary();
			$(".salary").attr('checked','checked');
			
		});
		parentView.find(".age").unbind("click").click(function(){
			onGetChartAge();
			$(".age").attr('checked','checked');
			
		});
		parentView.find(".degree").unbind("click").click(function(){
			onGetChartDegree();
			$(".degree").attr('checked','checked');
			
		});
	}
	
	//按年龄性别
	function onGetChartAge(e){
		var JgfxAgeContent = box.ich.view_jgfx_age();
		parentView.empty().append(JgfxAgeContent);
		parentView.showFooter(true);
		onClickRadio();
		box.request({
			url:box.getContextPath()+ageChartsUrl,
			success:onGetAgeChart
		});
		
	}
	//年龄图表
	function onGetAgeChart(data){
		var numMale=[0,0,0,0,0,0,0,0,0,0];
		var numFemale=[0,0,0,0,0,0,0,0,0,0];
		if(data && data.code=='0000'){
			//alert(JSON.stringify(data.result));
			var result=data.result;
			
			for(var i=0;i<result.length;i++){
				var map=result[i];
				map.age=parseFloat(map.age);//年龄进行类型转换
				if(map.gender=='0'){//性别为男
					if(map.age>=0 && map.age<=25){	   //年龄18-25岁
						numMale[0]+=parseFloat(map.customer_number);
					}else if(map.age>25 && map.age<=30){//年龄26-30岁
						numMale[1]+=parseFloat(map.customer_number);
					}else if(map.age>30 && map.age<=35){//年龄31-35岁
						numMale[2]+=parseFloat(map.customer_number);
					}else if(map.age>35 && map.age<=40){ //年龄36-40岁
						numMale[3]+=parseFloat(map.customer_number);
					}else if(map.age>40 && map.age<=45){ //年龄41-45岁
						numMale[4]+=parseFloat(map.customer_number);
					}else if(map.age>45 && map.age<=50){//年龄46-50岁
						numMale[5]+=parseFloat(map.customer_number);
					}else if(map.age>50 && map.age<=55){//年龄51-55岁
						numMale[6]+=parseFloat(map.customer_number);
					}else if(map.age>55 && map.age<=60){//年龄56-60岁
						numMale[7]+=parseFloat(map.customer_number);
					}else if(map.age>60){				//年龄60岁以上
						numMale[8]+=parseFloat(map.customer_number);
					}else{												//其他情况
						numMale[9]+=parseFloat(map.customer_number);
					}
				}else if(map.gender=='1'){//性别为女
					if(map.age>=0 && map.age<=25){	   //年龄18-25岁
						numFemale[0]+=parseFloat(map.customer_number);
					}else if(map.age>25 && map.age<=30){//年龄26-30岁
						numFemale[1]+=parseFloat(map.customer_number);
					}else if(map.age>30 && map.age<=35){//年龄31-35岁
						numFemale[2]+=parseFloat(map.customer_number);
					}else if(map.age>35 && map.age<=40){ //年龄36-40岁
						numFemale[3]+=parseFloat(map.customer_number);
					}else if(map.age>40 && map.age<=45){ //年龄41-45岁
						numFemale[4]+=parseFloat(map.customer_number);
					}else if(map.age>45 && map.age<=50){//年龄46-50岁
						numFemale[5]+=parseFloat(map.customer_number);
					}else if(map.age>50 && map.age<=55){//年龄51-55岁
						numFemale[6]+=parseFloat(map.customer_number);
					}else if(map.age>55 && map.age<=60){//年龄56-60岁
						numFemale[7]+=parseFloat(map.customer_number);
					}else if(map.age>60){				//年龄60岁以上
						numFemale[8]+=parseFloat(map.customer_number);
					}else{								//其他情况
						numFemale[9]+=parseFloat(map.customer_number);
					}
				}
			}
		
		$('#chartContaineAger').highcharts({
            chart: {
                type: 'column'
            },
            credits: { 
                enabled: false   //右下角不显示LOGO 
            }, 
            title: {
                text: '消费者年龄性别分析表',
                style: { "color": "#333333", "fontSize": "18px" }
                
            },
            xAxis: {
                categories: ['18-25岁', '26-30岁', '31-35岁', '36-40岁', '41-45岁', '46-50岁', '51-55岁', '56-60岁', '60岁以上', '年龄不详'], 
                lineWidth: 1,  //基线宽度 
                labels:{
                	style: { 
                    fontSize: '12px', 
                    marginTop: 5 }
            	}  
            },
            yAxis: {
               	allowDecimals:false,//是否允许刻度有小数
                title: {
                    text: ''
                },
	            stackLabels: {
	                enabled: true,
	                style: {
	                    fontWeight: 'bold',
	                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
	                }
	            }
            },
            legend: {
                align: 'right',
                x: -70,
                verticalAlign: 'top',
                y: 20,
                floating: true,
                backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
                borderColor: '#CCC',
                borderWidth: 1,
                shadow: false
            },
            tooltip: {
            	formatter: function() {
                    return '<b>'+ this.x +'</b><br/>'+
                        this.series.name +': '+ this.y +'<br/>'+
                        '总数: '+ this.point.stackTotal;
                }
            },
            plotOptions: {
            	 column: {
                    stacking: 'normal',
                    dataLabels: {
                        enabled: true,
                        color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                        style: {
                            textShadow: '0 0 3px black, 0 0 3px black'
                        }
                    }
                 }
            },
            series: [ {
                name: '男',
                data: numMale,
	            dataLabels: {
	                enabled: true,
	                color: '#FFFFFF',
	                align: 'center',
	                x: 2,
	                y: -2,
	                style: {
	                    fontSize: '13px',
	                    fontFamily: 'Verdana, sans-serif',
	                    textShadow: '1px 1px 2px black'
	                }
	            }
            },
            {
                name: '女',
                data: numFemale,
                dataLabels: {
	                enabled: true,
	                color: '#FFFFFF',
	                align: 'center',
	                x: 2,
	                y: -2,
	                style: {
	                    fontSize: '13px',
	                    fontFamily: 'Verdana, sans-serif',
	                    textShadow: '1px 1px 2px black'
	                }
	            }
            }]
        });
		}
	}
	
	//按收入
	function onGetChartSalary(e){
		var JgfxSalaryContent = box.ich.view_jgfx_salary();
		parentView.empty().append(JgfxSalaryContent);
		parentView.showFooter(true);
		onClickRadio();
		box.request({
			url:box.getContextPath()+moneyChartsUrl,
			success:onGetSalaryChart
		});
	}
	function onGetSalaryChart(data){
		var salaryNum=[0,0,0,0,0,0,0];
		if(data && data.code=="0000"){
			var result=data.result;
			for(var i=0;i<result.length;i++){
				var map=result[i];
				if(map.month_salary=='01'){//1000元以下
					salaryNum[0]=parseFloat(map.customer_number);
				}else if(map.month_salary=='02'){//1000-2000
					salaryNum[1]=parseFloat(map.customer_number);
				}else if(map.month_salary=='03'){//2000-3000
					salaryNum[2]=parseFloat(map.customer_number);
				}else if(map.month_salary=='04'){//3000-4000
					salaryNum[3]=parseFloat(map.customer_number);
				}else if(map.month_salary=='05'){//4000-5000
					salaryNum[4]=parseFloat(map.customer_number);
				}else if(map.month_salary=='06'){//5000以上
					salaryNum[5]=parseFloat(map.customer_number);
				}else{													//其他
					salaryNum[6]+=parseFloat(map.customer_number);//异常情况有多种,要进行累加
				}
			}
        $('#chartContainerSalary').highcharts({
            chart: {
                type: 'column'
            },
            credits: { 
                enabled: false   //右下角不显示LOGO 
            }, 
            title: {
                text: '消费者收入分析表'
            },
            xAxis: {
            	title:'收入水平',
                categories: [
                    '1000元以下',
                    '1000元-2000元',
                    '2000元-3000元',
                    '3000元-4000元',
                    '4000元-5000元',
                    '5000元以上',
                    '其他'
                ],
                labels: {
                	enabled: true,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            },
            yAxis: {
               	allowDecimals:false,//是否允许刻度有小数
                title: {
                    text: ''
                },
	            stackLabels: {
	                enabled: true,
	                style: {
	                    fontWeight: 'bold',
	                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
	                }
	            }
            },
            legend: {
                align: 'right',
                x: -70,
                verticalAlign: 'top',
                y: 5,
                floating: true,
                backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColorSolid) || 'white',
                borderColor: '#CCC',
                borderWidth: 1,
                shadow: false
            },
            tooltip: {
                pointFormat: '<b>{point.y:.1f}</b>',
            },
            plotOptions: {
            	 column: {
                    stacking: 'normal',
                    dataLabels: {
                        enabled: true,
                        color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                        style: {
                            textShadow: '0 0 3px black, 0 0 3px black'
                        }
                    }
                 }
            },
            series: [{
                name: '收入水平',
                data: salaryNum,
                dataLabels: {
                    enabled: false,
                    color: '#FFFFFF',
                    align: 'right',
                    x: 4,
                    y: -2,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif',
                        textShadow: '1px 1px 2px black'
                    }
                }
            }]
        });
      }
	}

	//按学历
	function onGetChartDegree(e){
		var JgfxSalaryContent = box.ich.view_jgfx_salary();
		parentView.empty().append(JgfxSalaryContent);
		parentView.showFooter(true);
		onClickRadio();
		box.request({
			url:box.getContextPath()+eduChartsUrl,
			success:onGetDegreeChart
		});
	}
	function onGetDegreeChart(data){
		var degreeNum=[0,0,0,0,0,0,0,0];
		if(data && data.code=='0000'){
			var result=data.result;
			for(var i=0;i<result.length;i++){
				var map=result[i];
				if(map.degree=='01'){//小学
					degreeNum[0]=parseFloat(map.customer_number);
				}else if(map.degree=='02'){//初中
					degreeNum[1]=parseFloat(map.customer_number);
				}else if(map.degree=='03'){//高中
					degreeNum[2]=parseFloat(map.customer_number);
				}else if(map.degree=='04'){//专科
					degreeNum[3]=parseFloat(map.customer_number);
				}else if(map.degree=='05'){//本科
					degreeNum[4]=parseFloat(map.customer_number);
				}else if(map.degree=='06'){//硕士
					degreeNum[5]=parseFloat(map.customer_number);
				}else if(map.degree=='07'){//博士
					degreeNum[6]=parseFloat(map.customer_number);
				}else{										//其他
					degreeNum[7]+=parseFloat(map.customer_number);//异常情况可能有多种,要进行累加
				}
			}
			$('#chartContainerSalary').highcharts({
            chart: {
                type: 'column'
            },
            credits: { 
                enabled: false   //右下角不显示LOGO 
            }, 
            title: {
                text: '消费者职业与文化特征分析表'
            },
            xAxis: {
            	title:'文化程度',
                categories: [
                	'小学',
                    '初中',
                    '高中',
                    '专科',
                    '本科',
                    '硕士',
                    '博士',
                    '其他'
                ],
                labels: {
                	enabled: true,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif'
                    }
                }
            },
            yAxis: {
               	allowDecimals:false,//是否允许刻度有小数
                title: {
                    text: ''
                },
	            stackLabels: {
	                enabled: true,
	                style: {
	                    fontWeight: 'bold',
	                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
	                }
	            }
            },
            legend: {
                align: 'right',
                x: -70,
                verticalAlign: 'top',
                y: 5,
                floating: true,
                borderColor: '#CCC',
                borderWidth: 1,
                shadow: false
            },
            tooltip: {
                pointFormat: '<b>{point.y:.1f}</b>',
            },
       		plotOptions: {
            	 column: {
                    stacking: 'normal',
                    dataLabels: {
                        enabled: true
                    }
                 }
            },
            series: [{
                name: '文化程度',
                data: degreeNum,
                dataLabels: {
                    enabled: false,
                    color: '#FFFFFF',
                    align: 'right',
                    x: 4,
                    y: -2,
                    style: {
                        fontSize: '13px',
                        fontFamily: 'Verdana, sans-serif',
                        textShadow: '1px 1px 2px black'
                    }
                }
            }]
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
			box.listen("jgfx", onload);
			box.listen("jgfx_close",onclose);
		},
		destroy: function() { }
	};
});