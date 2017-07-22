/**
 * 历史订单功能
 */
HOME.Core.register("plugin-lsdd", function(box) {
	
	var orderListUrl = "retail/tobacco/getOrderList";
	var orderDetailUrl = "retail/tobacco/getOrderDetail";
	var putOrder = "retail/tobacco/putOrder";
	
	var parentView = null;
	
	var beginTime="";
	var endTime="";
	
	var dialogView = null;
	
	function onload(view) {
		parentView = view;
		
		getOrderList();
	}
	
	function getOrderList() {
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		if(beginTime && !endTime) {
			endTime=beginTime;
		}
		if(!beginTime && endTime) {
			beginTime=endTime;
		}
		if(!beginTime && !endTime) {
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
			url: box.getContextPath() + orderListUrl,
			data:{begin_date:beginTimeFormat, end_date:endTimeFormat},
			success:onGetOrderList
		});
	}
	
	function onGetOrderList(data) {
		if(data && data.code =="0000") {
			var item = null;
			var str = "订单";
			var isAuto = false;
			for(var i=0;i<data.result.length;i++){
				item = data.result[i];
				
				//日期格式化为yyyy-MM-dd
				var itemDate=new Date(item.order_date.substr(0, 4),item.order_date.substr(4, 2)-1,item.order_date.substr(6, 2));
				item.order_date=itemDate.format("yyyy-MM-dd");
				
				item.index=i+1;
				item.status_label = box.labels.tobacco[item.status];
				item.pmt_label = box.labels.tobaccopay[item.pmt_status];

				if(item.is_auto) {
					isAuto = true;
					str += item.order_id + ",";
				}
			}
			str += "已自动入库";
			if(isAuto) {
				box.showAlert({message:str});
			}
			
			showOrderList(data.result);
		}else{
			box.showAlert({message:msg});
		}
	}
	
	function showOrderList(result){
		if(result.length<=0){
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		for(var i=0;i<result.length;i++){
			result[i].amt_purch_total=$.parseMoney(result[i].amt_purch_total);
			result[i].number=i+1;
		}
		box.hideLoading();
		var content=box.ich.view_lsdd({list: result});
		parentView.empty().append(content);
		if(result.length!=0){
			parentView.find("table tbody tr").click(onOrderClick);
			parentView.find("table tbody tr a").click(ruku);
		}
		
		parentView.find("#selewares").unbind("click", getOrderList).click(getOrderList);
		
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
	/**
	 * 卷烟订单入库
	 */
	function ruku(e){
		var id=$(e.currentTarget).attr("data-id");
		if(id!="0"){
			box.showConfirm({
				message: "您确定将卷烟订单 "+id+" 中的卷烟全部加入库存吗？",
				ok: function() {
					putOrderLocal(id);
				}
			});
		}
		return false;
	}
	/**
	 * 卷烟订单入库
	 */
	function putOrderLocal(id){
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		if(beginTime && !endTime) {
			endTime=beginTime;
		}
		if(!beginTime && endTime) {
			beginTime=endTime;
		}
		if(!beginTime && !endTime) {
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
		
		box.showLoading();
		box.request({
			url:box.getContextPath()+ putOrder,
			data:{params:$.obj2str({order_id:id,begin_date:beginTimeFormat, end_date:endTimeFormat})},
			success:function (data){
				if(data.code=="0000"){
					box.showAlert({message:"入库成功！"});
					onGetOrderList(data);
					box.closeDialog({content:dialogView});
				}
				
			}
		});
	}
	
	function onOrderClick(e) {
		var target = $(e.currentTarget);
		var id = target.attr("data-id");
		if(id) {
			box.request({
				url:box.getContextPath()+ orderDetailUrl,
				data:{order_id: id},
				success:onGetDetail
			});
		}
	}
	
	function onGetDetail(data) {
		if(data && data.code == "0000" && data.result) {
			dataSort(data);
			deleteData(data);
			
			//日期格式化为yyyy-MM-dd
			var orderDate=new Date(data.result.order_date.substr(0, 4),data.result.order_date.substr(4, 2)-1,data.result.order_date.substr(6, 2));
			data.result.order_date=orderDate.format("yyyy-MM-dd");
			for ( var i = 0; i < data.result.purch_order_line.length; i++) {
				data.result.purch_order_line[i].amt_ord=$.parseMoney(data.result.purch_order_line[i].amt_ord);
			}
			data.result.amt_purch_total=$.parseMoney(data.result.amt_purch_total);
			dialogView = box.ich.view_lsdd_detail(data.result);
			dialogView = box.showDialog({
				title: "订单明细",
				width: 800,
				height: 560,
				content: dialogView,
				minWidth: 700,
				minHeight: 300,
				close: function(e) {
					//$("#selewares").focus();
					//$("#beginTime").blur();
				}
			});
			dialogView.find("#detailPut").click(ruku);
			dialogView.find(".table_view").fixedtableheader({
				parent: dialogView,
				win: dialogView,
				isshow: true 
			});
			
		}
	}
	/**
	 * 排序
	 */
	function dataSort(data){
		var testArray = data.result.purch_order_line;
		testArray.sort(sortFunction);
		for(var i=0;i<testArray.length;i++){
			testArray[i].number=i+1;
		}
		data.result.purch_order_line=testArray;
	}
	/**
	 * 删除订购量为零的数据
	 */
	function deleteData(data){
		var testArray = data.result.purch_order_line;
		var n=0;
		for(var i=0;i<testArray.length;i++){
			if(testArray[i].qty_ord==0){
				n++;
			}
		}
		for(var i=0;i<n;i++){
			testArray.pop();
		}
		data.result.purch_order_line=testArray;
	}
	
	function sortFunction(x,y){
		return -1*(x.qty_ord-y.qty_ord);
	}
	
	function lsddOnKey(data){
		switch(data.key) {
		case HOME.Keys.INSERT:
			var tr=table.find("tr.selectRow");
			if(tr.length>0){
				ruku(tr.data("item"));
			}
			break;
		}
	}
	
	
	
	return {
		init: function(){
			box.listen("lsdd", onload);
			box.listen("lsdd_onkey", lsddOnKey);
		},
		destroy: function() { }
	};
});