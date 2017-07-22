/**
 * 交接班查询功能
 */
HOME.Core.register("plugin-jjcx", function(box) {
	//交接班查询
	var jjcxURL ="retail/basedata/searchDutyShift";
	var parentView=null;
	var content=null;
	var isAddConsumer=false;
	var beginTime="";
	var endTime="";
	var pageIndex = 1;
	var pageSize = 20;

	function stopPP(e) {
		var evt = e || window.event;
		//IE用cancelBubble=true来阻止而FF下需要用stopPropagation方法
		evt.stopPropagation ?evt.stopPropagation() : (evt.cancelBubble=true);
	}
	
	function onload(view) {
		pageIndex = 1;
		pageSize = 20;
		parentView = view;
		parentView.closeMessage();
		parentView.showFooter(true);
		getJJCXDate();
	}
	
	function getJJCXDate(isok){
		params = {page_index: pageIndex, page_size: pageSize};
		
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		if((beginTime!=null && endTime==null)||(beginTime!="" && endTime=="")){
			endTime=beginTime;
		}
		if((beginTime==null && endTime!=null)||(beginTime=="" && endTime!="")){
			beginTime=endTime;
		}
		if((beginTime=="" && endTime=="")||(beginTime==null && endTime==null)){
			beginTime=$.GetToday(-6);
			endTime=$.GetToday();
		}
		if(beginTime>endTime){
			box.showAlert({message:"开始日期不能大于结束日期！"});
			return;
		}
		//日期格式化为yyyyMMdd
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");

		params.start_date = beginTimeFormat;
		params.end_date = endTimeFormat;
		
		box.request({
			url:box.getContextPath()+ jjcxURL,
			data:{params:$.obj2str(params)},
			success: function(json) {
				if(json.result.length<=0){
					box.showAlert({message:"很抱歉，没有查询到数据！"});
				}
				if(json && json.code=="0000"){
					var pageParam = json.pageparams;
					parentView.showPager(pageParam, function(param) {
						pageIndex = param.page_index;
						pageSize = param.page_size;
						getJJCXDate();
					});
					var consumerList = json.result;
					
					if(consumerList.length==0&&isok){
						box.showAlert({message: "很抱歉，没有查询到数据！"});			
					}
					for(var i=0; i<consumerList.length; i++) {
						consumerList[i].index = (pageIndex-1)*pageSize+i+1;
						//遍历金额
						consumerList[i].amt_sale_card = $.parseMoney(consumerList[i].amt_sale_card);
						consumerList[i].amt_sale_cash = $.parseMoney(consumerList[i].amt_sale_cash);
						consumerList[i].amt_return = $.parseMoney(consumerList[i].amt_return);
						//遍历时间
						if(consumerList[i].last_shift_date != null){
							//日期时间格式化为yyyy-MM-dd hh:mm:ss
							var lastShiftDate=new Date(consumerList[i].last_shift_date.substr(0, 4),consumerList[i].last_shift_date.substr(4, 2)-1,consumerList[i].last_shift_date.substr(6, 2),
									          consumerList[i].last_shift_time.substr(0, 2),consumerList[i].last_shift_time.substr(2, 2),consumerList[i].last_shift_time.substr(4, 2));
							consumerList[i].last_shift_date=lastShiftDate.format("yyyy-MM-dd hh:mm:ss");
						}
						if(consumerList[i].shift_date != null){
							//日期时间格式化为yyyy-MM-dd hh:mm:ss
							var shiftDate=new Date(consumerList[i].shift_date.substr(0, 4),consumerList[i].shift_date.substr(4, 2)-1,consumerList[i].shift_date.substr(6, 2),
							          consumerList[i].shift_time.substr(0, 2),consumerList[i].shift_time.substr(2, 2),consumerList[i].shift_time.substr(4, 2));
					        consumerList[i].shift_date=shiftDate.format("yyyy-MM-dd hh:mm:ss");
						}
					}
					var content=box.ich.view_jjcx({list:consumerList});
					parentView.empty().append(content);
					parentView.find(".table_view").fixedtableheader({
						parent: parentView,
						win: parentView.parent(),
						isshow: true
					});
					
					parentView.find("#beginTime").datepicker({
						maxDate: 'd' 
					});
					parentView.find("#endTime").datepicker({
						maxDate: 'd' 
					});
					parentView.find("#beginTime").val(beginTime);
					parentView.find("#endTime").val(endTime);
					
					parentView.find("#selewares").unbind("click").click(onQueryButtonClick);
					//content.find("table tbody tr").unbind("click").click(update);
				}
			}
		});
	}
	
	function onQueryButtonClick(e) {
		pageIndex = 1;
		pageSize = 20;
		var isOk=true;
		getJJCXDate(isOk);
	}
	return {
		init: function(){
			box.listen("jjcx", onload);
		},
		destroy: function() { }
	};
});