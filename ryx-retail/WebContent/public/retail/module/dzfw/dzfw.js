/**
 * 便门服务--数字电视缴费
 */
HOME.Core.register("plugin-dzfw", function(box) {
	var phoneurl="retail/bmfw/dzfw/phone";
	var consumeUrl="retail/bmfw/dzfw/consume";
	var gjfUrl="/retail/bmfw/dzfw/gjf";
	var view_ = null;
	var content = null;
	var pageIndex=1;
	var pageSize=20;
	function onload(view){
		view_ = view;
		content = box.ich.view_dzfw();
		view.empty().append(content);
		var selectedObj = content.find(".left_table_view .selected");
		
		content.find("#left_view li").each(function(){
			if($(this).attr("id") == "search_gjfchx"){
				$(this).unbind("click").click(function(){
					 searchGJFCX(this);
				});
			}
			else{
				$(this).unbind("click").click(showBillType(this));
			}
			
		});
		
		$(selectedObj).trigger("click");
		
	}
	var feeTypeID;
    var lastClickObj = null;
    function showBillType(self){
    	return function(e){
			feeTypeID =  $(self).attr("id");
    		var payType = feeTypeID.split("_")[1];
    		//更改左侧导航栏背景
    		//$(self).find("img").attr("src","../public/retail/resource/zzfw/"+payType+"0.png");
			//if(lastClickObj){
			//	$(lastClickObj).find("img").attr("src","../public/retail/resource/zzfw/"+$(lastClickObj).attr("id").split("_")[1]+"1.png")
			//}
			lastClickObj = self;
			content.find("#left_view li").removeClass("liselected").removeClass("selected");
			$(self).addClass("liselected").addClass("selected");
			//隐藏查询条件
			var end = $.GetToday(0);
			var start = $.GetToday(-6);
			if($(self).attr("id") == "search_xfcx"){
				content.find("#view_footer").empty().append(box.ich.view_dzfw_boot({start:start,end:end}));
				$("#consumePhone").hide();
				$("#feeStatus").hide();
				$("#consumeCardNum").show();
			}
			else{
				content.find("#view_footer").empty().append(box.ich.view_dzfw_boot({start:start,end:end}));
				$("#consumeCardNum").hide();
				$("#consumePhone").show();
				$("#feeStatus").show();
			}
			clearForm();
			content.find(".search").focus(function(){
				var lastVal = $(this).val();
				$(this).blur(function(){
					if(lastVal != $(this).val()){
						clearForm();
					}
				});
			});
			//初始日期控件
			content.find("#consumeDateStart").datepicker({
				maxDate: 'd' 
			});
			//初始日期控件
			content.find("#consumeDateEnd").datepicker({
				maxDate: 'd' 
			});
			content.find("#consumeDate").change(function(){
				content.find("#checkBillSearchButton").focus();
			});
			searchContent();
			//绑定查询按钮事件
			$("#checkBillSearchButton").unbind("click").click(searchContent);
			
    	};
    }
    function clearForm(){
    	pageIndex = 1;
    	pageSize = 20;
    }
    function searchContent(){
    	var consumeDateStart = $("#consumeDateStart").val();
    	var consumeDateEnd = $("#consumeDateEnd").val();
    	var consumeCardNum =$("#consumeCardNum").val();
    	var consumePhone = $("#consumePhone").val();
    	var status = $("#feeStatus").val();
    	var url_ = null;
    	//如果电话查询表单是隐藏的表明是销售查询
    	if($("#consumePhone").is(":hidden")){
    		url_ = box.getContextPath()+consumeUrl;
    	}
    	else{
    		url_ = box.getContextPath()+phoneurl;
    	}
		
    	if(consumeDateStart!==""){
    	    //日期格式化为yyyyMMdd
		    var consumeDateFormat=new Date(consumeDateStart.substr(0, 4),consumeDateStart.substr(5, 2)-1,consumeDateStart.substr(8, 2));
		    consumeDateStart=consumeDateFormat.format("yyyyMMdd");
    	}
    	if(consumeDateEnd!==""){
    	    //日期格式化为yyyyMMdd
		    var consumeDateFormat=new Date(consumeDateEnd.substr(0, 4),consumeDateEnd.substr(5, 2)-1,consumeDateEnd.substr(8, 2));
		    consumeDateEnd=consumeDateFormat.format("yyyyMMdd");
    	}
    	
    	if(!consumeDateStart){
			box.showAlert({message: "对不起，开始日期不能为空！"});
			return ;
		}
		if(!consumeDateEnd){
			box.showAlert({message: "对不起，结束日期不能为空！"});
			return ;
		}
		if(consumeDateStart > consumeDateEnd){
			box.showAlert({message: "对不起，开始日期不能大于结束日期！"});
			return ;
		}

    	box.request({
			url: url_,
			data: {"pagenumber":pageIndex,"pagesize":pageSize,"dateStart":consumeDateStart,"dateEnd":consumeDateEnd,"phoneno":consumePhone,"cardno":consumeCardNum,"status":status},
			success:showContent
		});
    	
    }
    function showContent(data){
    	$("#fixedtableheader0").remove();
    	if(data.code=="0000"){
    		if(data.result.msg){
    			box.showAlert({message:"很抱歉，没有查询到数据！"});
    		}
    		else{
    			if(data.result.total == "0"){
					box.showAlert({message:"很抱歉，没有查询到数据！"});
				}
    			var pageAmount = 0.00; 
    			var amount = 0.00;
    			if($("#consumePhone").is(":hidden")){
    				
    				//日期时间格式化为yyyy-MM-dd hh:mm:ss
    				var rows = data.result.rows;
    				for(var i=0 ; i<rows.length;i++){
    					var thisRow = rows[i];
    					var tranDate = thisRow.tran_date;
    					var tranTime = thisRow.tran_time;
    					var cardNo = thisRow.card_no;
					    var tran_date=new Date(tranDate.substr(0, 4),tranDate.substr(4, 2)-1,tranDate.substr(6, 2),
					    		tranTime.substr(0, 2),tranTime.substr(2, 2),tranTime.substr(4, 2));
					    thisRow.tran_date=tran_date.format("yyyy-MM-dd hh:mm:ss");
					    
					    thisRow.card_no = cardNo.substring(0,6)+"********"+ cardNo.substring( cardNo.length-4, cardNo.length);
    					pageAmount =parseFloat(thisRow.tran_amt)+ pageAmount;
    					var code = thisRow.resp_code;
    					var flag = thisRow.tran_flag;
    					if("00"==code) {
    						if("0"==flag) thisRow.resp_msg = "成功";
    						else if("1"==flag) thisRow.resp_msg = "撤销";
    						else if("0"==flag) thisRow.resp_msg = "冲正";
    					} else {
    						thisRow.resp_msg = "失败";
    					}
                        //rows[i].tran_time=tranDate.format("hh:mm:ss");
    				}
    				amount =data.result.footer[0].tran_amt_success;
    				if(amount == "null"){
    					amount = "0.00";
    				}
            		content.find("#container").empty().append(box.ich.view_dzfw_consume({list:data.result.rows,pageAmount:$.parseMoney(pageAmount),amount:amount}));
            		var pageparams = fitingPage(data.result);
            		view_.showPager(pageparams,function(param){
            			pageIndex=param.page_index;
            			searchContent();
            		});
            		view_.find("#consumeContent").tablesort([
            		          			       		        {col:0,order:"asc",method:"advance",type:"number"},
            		          			       		        {col:1,order:"asc",method:"advance",type:"string"},
            		          			       		        {col:2,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:3,order:"desc",method:"advance",type:"number"},
            		                     		        	{col:4,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:5,order:"asc",method:"advance",type:"string"}
            		          			       		        ]);
            		view_.find("#consumeContent").fixedtableheader({
            					parent: view_.find("#consumeContent").parent(),
            					win: view_.parent(),
            					isshow: true
            				});
            	}
            	else{
            		var rows = data.result.rows;
            		var failedTR =new Array();
            		var TRClass=$("#phoneContent").find("tbody tr");
    				for(var i=0 ; i<rows.length;i++){
    					if(rows[i].pay_tran_status == "1"){
    						rows[i].pay_tran_status = "成功";
    						pageAmount = pageAmount + parseFloat(rows[i].tran_amt);
    					}
    					else if(rows[i].pay_tran_status == "2"){
    						rows[i].pay_tran_status = "失败";
    						failedTR.push(i);
    					}
    					else if(rows[i].pay_tran_status == "0"){
    						rows[i].pay_tran_status = "充值中";
    					}
    					else{
    						rows[i].pay_tran_status = "无效订单";
    					}
                        
    					//日期时间格式化为yyyy-MM-dd hh:mm:ss
    					var tranDate=new Date(rows[i].tran_date.substr(0, 4),rows[i].tran_date.substr(4, 2)-1,rows[i].tran_date.substr(6, 2),
    							rows[i].tran_time.substr(0, 2),rows[i].tran_time.substr(2, 2),rows[i].tran_time.substr(4, 2));
    					rows[i].tran_date=tranDate.format("yyyy-MM-dd hh:mm:ss");
    					//var card_no = 
    					var card_no = rows[i].card_no.substring(0,6)+"********"+ rows[i].card_no.substring( rows[i].card_no.length-4, rows[i].card_no.length);
    					rows[i].card_no = card_no;
                        //rows[i].tran_time=tranDate.format("hh:mm:ss");
    					
    				}
    				amount =data.result.footer[0].tran_amt_success; 
    				if(amount == "null"){
    					amount = "0.00";
    				}
            		content.find("#container").empty().append(box.ich.view_dzfw_phone({list:rows,pageAmount:$.parseMoney(pageAmount),amount:amount}));
            		for(var i=0;i<failedTR.length;i++){
            			$("#phoneContent").find("tbody tr:eq("+failedTR[i]+")").addClass("warnRow");
            		}
            		var pageparams = fitingPage(data.result);
            		view_.showPager(pageparams,function(param){
            			pageIndex=param.page_index;
            			searchContent();
            		});
            		view_.find("#phoneContent").tablesort([
            		          			       		        {col:0,order:"asc",method:"advance",type:"number"},
            		          			       		        {col:1,order:"asc",method:"advance",type:"string"},
            		          			       		        {col:2,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:3,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:4,order:"desc",method:"advance",type:"number"},
            		                     		        	{col:5,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:6,order:"asc",method:"advance",type:"string"},
            		                     		        	{col:7,order:"asc",method:"advance",type:"string"}
            		          			       		        ]);
            		view_.find("#phoneContent").fixedtableheader({
            					parent: view_.find("#phoneContent").parent(),
            					win: view_.parent(),
            					isshow: true
            				});
            	}
    		}
    	}
    	else{
    		box.showAlert({message:data.msg});
    	}
    	
    }
    function searchGJFCX(self){
    	content.find("#left_view li").removeClass("liselected").removeClass("selected");
		$(self).addClass("liselected").addClass("selected");
		
		//初始日期控件
    	var gjfContent = box.ich.view_dzfw_gjf();
    	content.find("#container").empty().append(gjfContent);
    	
    	var end = $.GetToday(0);
		var start = $.GetToday(-6);
    	var gjfFooterContent = box.ich.view_dzfw_gjf_footer({start:start,end:end});
    	
    	content.find("#view_footer").empty().append(gjfFooterContent);
    	content.find("#consumeDateStart").datepicker({
			maxDate: 'd' 
		});
    	
		//初始日期控件
		content.find("#consumeDateEnd").datepicker({
			maxDate: 'd' 
		});
		
		content.find("#consumeDate").change(function(){
			content.find("#checkBillSearchButton").focus();
		});
		
		/*view_.showPager(pageparams,function(param){
			pageIndex=param.page_index;
			//searchContent();
			gjf();
		});*/
		content.find("#checkBillSearchSJF").unbind("click").click(function(){
			gjf();
		});
		var gjf=function(){
			var feeType = content.find("#feeType").val();
			var consumeDateStart = content.find("#consumeDateStart").val();
	    	var consumeDateEnd = content.find("#consumeDateEnd").val();
	    	var consumeCardNum = content.find("#consumeCardNum").val();
	    	if(consumeDateStart!==""){
	    	    //日期格式化为yyyyMMdd
			    var consumeDateFormat=new Date(consumeDateStart.substr(0, 4),consumeDateStart.substr(5, 2)-1,consumeDateStart.substr(8, 2));
			    consumeDateStart=consumeDateFormat.format("yyyyMMdd");
	    	}
	    	if(consumeDateEnd!==""){
	    	    //日期格式化为yyyyMMdd
			    var consumeDateFormat=new Date(consumeDateEnd.substr(0, 4),consumeDateEnd.substr(5, 2)-1,consumeDateEnd.substr(8, 2));
			    consumeDateEnd=consumeDateFormat.format("yyyyMMdd");
	    	}
	    	
	    	if(!consumeDateStart){
				box.showAlert({message: "对不起，开始日期不能为空！"});
				return ;
			}
			if(!consumeDateEnd){
				box.showAlert({message: "对不起，结束日期不能为空！"});
				return ;
			}
			if(consumeDateStart > consumeDateEnd){
				box.showAlert({message: "对不起，开始日期不能大于结束日期！"});
				return ;
			}

	    	
	    	var obj = new Object();
	    	obj.page = pageIndex;
	    	obj.rows = pageSize;
	    	obj.messagecode = feeType;
	    	obj.start = consumeDateStart;
	    	obj.end = consumeDateEnd;
	    	obj.cardno = consumeCardNum;
	    	box.request({
				url:  box.getContextPath()+gjfUrl,
				data: {params: $.obj2str(obj)},
				success:function(data){
					var rows = data.result.rows;
					var pageAmount = 0.00;
					var amount = 0.00;
					if(data.result.total == "0"){
						box.showAlert({message:"很抱歉，没有查询到数据！"});
					}
					for(var i = 0 ; i < rows.length;i++){
						//日期时间格式化为yyyy-MM-dd hh:mm:ss
    					var tranDate=new Date(rows[i].tran_date.substr(0, 4),rows[i].tran_date.substr(4, 2)-1,rows[i].tran_date.substr(6, 2),
    							rows[i].tran_timestamp.substr(0, 2),rows[i].tran_timestamp.substr(2, 2),rows[i].tran_timestamp.substr(4, 2));
    					rows[i].tran_date=tranDate.format("yyyy-MM-dd hh:mm:ss");
    					//
    					var acct_no = rows[i].acct_no;
    					var card_no = acct_no.substring(0,6)+"********"+ acct_no.substring( acct_no.length-4, acct_no.length);
    					rows[i].acct_no=card_no;
    					var prd_uid = rows[i].prd_uid;
    					if(prd_uid == "02"){
    						prd_uid = "联通缴费";
    					}
    					else if(prd_uid == "01"){
    						prd_uid = "电信座机";
    					}
    					else if(prd_uid == "03"){
    						prd_uid = "用电缴费";
    					}
    					else if(prd_uid == "04"){
    						prd_uid = "燃气缴费";
    					}
    					else if(prd_uid == "05"){
    						prd_uid = "暖气缴费";
    					}
    					else if(prd_uid == "06"){
    						prd_uid = "有线缴费";
    					}
    					else if(prd_uid == "07"){
    						prd_uid = "社会";
    					}
    					else if(prd_uid == "16"){
    						prd_uid = "历城电力";
    					}
    					else if(prd_uid == "23"){
    						prd_uid = "数字电视";
    					}
    					rows[i].prd_uid = prd_uid;
    					if(rows[i].ret_status  == "S"){
    						pageAmount = pageAmount + parseFloat(rows[i].act_pay_amt);
    						rows[i].ret_msg = "缴费成功";
    					} else if(!rows[i].ret_status || rows[i].ret_status == "") {
    						rows[i].ret_msg = "未知";
    					}
    					else{
    						rows[i].ret_msg = "缴费失败";
    					}
    					
					}
					var pageparams = fitingPage(data.result);
            		view_.showPager(pageparams,function(param){
            			pageIndex=param.page_index;
            			gjf();
            		});
            		amount = data.result.footer[0].tran_amt_success;
            		if(amount == "null"){
    					amount = "0.00";
    				}
					var gjfContent = box.ich.view_dzfw_gjf({list:rows,amount:amount,pageAmount:$.parseMoney(pageAmount)});
			    	content.find("#container").empty().append(gjfContent);
			    	view_.find("#gjfContent").tablesort([
			    	         		       		        {col:0,order:"asc",method:"advance",type:"number"},
			    	         		       		        {col:1,order:"asc",method:"advance",type:"string"},
			    	             		          		{col:2,order:"asc",method:"advance",type:"string"},
			    	             		                {col:3,order:"desc",method:"advance",type:"number"},
			    	             		                {col:4,order:"asc",method:"advance",type:"string"},
			    	             		                {col:5,order:"asc",method:"advance",type:"string"},
			    	             		                {col:6,order:"asc",method:"advance",type:"string"},
			    	             		                {col:7,order:"asc",method:"advance",type:"string"},
			    	             		                {col:8,order:"asc",method:"advance",type:"string"}
			    	             		    ]);
					view_.find("#gjfContent").fixedtableheader({
						parent: view_.find("#gjfContent").parent(),
						win: view_.parent(),
						isshow: true
					});

				}
			});
		};
		gjf();
    }
    
    
    
    
    
    function fitingPage(result){
    	//"page_size":"20","count":"3000","page_index":"1","refid":"","itembar":"","cgtcomid":"","page_count":"2"
    	//{"pagenumber":"1","pagesize":"20","pagesum":"28","total":"548"}
    	var pageparams = {"page_size":result.pagesize,"count":result.total,"page_index":result.pagenumber,"refid":"","itembar":"","cgtcomid":"","page_count":result.pagesum};
    	return pageparams;
    }
	return {
		init:function(){
			box.listen("dzfw", onload);
		},
		destroy: function(){}
	};
});