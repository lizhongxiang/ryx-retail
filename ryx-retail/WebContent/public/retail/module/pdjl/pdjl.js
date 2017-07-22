HOME.Core.register("plugin-pdjl", function(box) {
	
	var pdjlURL="retail/whse/getTakeStock";
	var xiangXiURL="retail/whse/getTakeStockXiangXi";

	var pageSize=20;
	var pageIndex=1;
	var sList=null;
	var inputs=null;
	var parentView = null;
	var beginTime="";
	var endTime="";
	
	function loadPDJL(view){
		parentView= view;
		pageSize=20;
		pageIndex=1;
		getPDJLDate();
		
	}
	function getPDJLDate(){
	
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		if((beginTime!=null && endTime==null)||(beginTime!="" && endTime=="")){
			endTime=beginTime;
		}
		if((beginTime==null && endTime!=null)||(beginTime=="" && endTime!="")){
			beginTime=endTime;
		}
		if((beginTime=="" && endTime=="")||(beginTime==null && endTime==null)){
			beginTime=$.GetToday(-30);
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

		box.request({
			url:box.getContextPath()+ pdjlURL,
			data:{begintime:beginTimeFormat,endtime:endTimeFormat,page_index:pageIndex,page_size:pageSize},
			success:onGetPDJLDate
		});
	}
	
	function onGetPDJLDate(data){
		 
		if(data && data.code=="0000"){
			var pageParam=data.pageparams;
			parentView.showPager(pageParam,function(param){
				pageIndex=param.page_index;
				pageSize=param.page_size;
				getPDJLDate();
			});
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1+(pageSize*(pageIndex-1));
//				data.result[i].index=(pageIndex-1)*pageSize+i+1;
				data.result[i].amt_profit=$.parseMoney(data.result[i].amt_profit);
				data.result[i].amt_loss=$.parseMoney(data.result[i].amt_loss);
				data.result[i].status='新增';
				if(data.result[i].status=='02'){
					data.result[i].status='记账';
				}
                
				//日期格式化为yyyy-MM-dd
				var crtDate=new Date(data.result[i].crt_date.substr(0, 4),data.result[i].crt_date.substr(4, 2)-1,data.result[i].crt_date.substr(6, 2),
						data.result[i].crt_time.substr(0, 2),data.result[i].crt_time.substr(2, 2),data.result[i].crt_time.substr(4, 2));
				data.result[i].crt_date=crtDate.format("yyyy-MM-dd hh:mm:ss");
				
			}
//			if(data.result.length!=0){
//				data.result[0].aa=begin;
//				data.result[0].bb=end;
//			}
			showPDJL(data.result);
		}
	}
	
	function showPDJL(result){
		if(result.length<=0){
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		var content=box.ich.view_kcgl_pdjl({list: result});
		parentView.empty().append(content);
		if(result.length!=0){
			parentView.find("table tbody tr").click(onPDJLXiangXi);
		}else{
			//box.showAlert({message:"没有盘点商品记录！"});
		}
		parentView.find("#selewares").unbind("click", getPDJLDateAction).click(getPDJLDateAction);
		
		parentView.find("#beginTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find("#endTime").datepicker({
			maxDate: 'd' 
		});
		
		parentView.find("#beginTime").val(beginTime);
		parentView.find("#endTime").val(endTime);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
	}
	function getPDJLDateAction(){
		pageSize=20;
		pageIndex=1;
		getPDJLDate();
	}
	
	function onPDJLXiangXi(e){
		var itemid=$(e.currentTarget).attr("data-id");
		showXiangxi(itemid);
	}
	
	function showXiangxi(firstId){
		box.request({
			url:box.getContextPath()+ xiangXiURL,
			data:{turn_id:firstId},
			success:onGetPDJL_Detail
		});
	}
	
	function onGetPDJL_Detail(data){
		if(data && data.code=="0000"){
			data.result.turn_id=null;//单号
			data.result.amt_profit=null;//增益额
			data.result.qty_pl=null;//增益量
			
			var amtPl=null;//金额
			var qtyPl=null;//数量
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1;
				data.result.turn_id=data.result[i].turn_id;
				qtyPl=data.result[i].qty_pl;
				amtPl=data.result[i].amt_pl;
				
				data.result.amt_profit+=parseFloat(amtPl);
				data.result.qty_pl+=parseFloat(qtyPl);
				
				data.result[i].amt_pl=$.parseMoney(data.result[i].amt_pl);
			}
			data.result.amt_profit=$.parseMoney(data.result.amt_profit);
			data.result.qty_pl=(data.result.qty_pl);
			showPDJL_Detail(data.result);
		}
	}
	
	function showPDJL_Detail(result){
		var content=box.ich.view_kcgl_pdjl_right({result: result});
		box.showDialog({
			title:"盘点明细",
			width:960,
			height:500,
			content: content
		});
		content.find(".table_view").fixedtableheader({
			parent: content.find(".table_view").parent(),
			win: content,
			isshow: true 
		});
	}
	
	return {
		init: function(){
			box.listen("pdjl", loadPDJL);
		},
		destroy: function() { }
	};
});