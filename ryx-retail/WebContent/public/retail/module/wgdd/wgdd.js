/**
 * 网购订单功能
 */
HOME.Core.register("plugin-wgdd", function(box) {
	
	var WGDDURL = "retail/order/searchOnlineSaleOrder";
	var xiangXiURL="retail/order/searchSaleOrderDetail";
	var removeSaleOrder = "retail/order/removeSaleOrder";
	var deliverSaleOrder = "retail/order/deliverSaleOrder";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		pageIndex = 1;
		pageSize = 20;
		parentView.showFooter(true);
		getWGDDDate();
	}
	
	function getWGDDDate(){
		params = {order_type:'04', page_index:pageIndex, page_size:pageSize};
		
//////////////////////
		
//		beginTime=parentView.find("#beginTime").val();
//		endTime=parentView.find("#endTime").val();
//		if(!beginTime) {
//			beginTime = $.GetToday(-30);
//		}
//		
//		if(!endTime) {
//			endTime = $.GetToday();
//		}
//		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
//		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
//		params.order_date_floor = beginTimeFormat;
//		
//		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
//		endTimeFormat=endTimeFormat.format("yyyyMMdd");
//		params.order_date_ceiling = endTimeFormat;
//		
//		if(beginTime>endTime){
//			box.showAlert({message:"开始日期不能大于结束日期！"});
//			return;
//		}

////////////////////////////
		box.request({
			url:box.getContextPath()+ WGDDURL,
			data:{params: $.obj2str(params)},
			success:onGetWGDDDate
		});
	}
	
	function onGetWGDDDate(data){
		if(data && data.code=="0000"){
			var wclOrderCount = 0;
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getWGDDDate();
			});
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1+(pageSize*(pageIndex-1));
				if(data.result[i].status != "01"){
					data.result[i].display = "display: none;";
				}else{
					wclOrderCount++;
				}
			}
			
			if(wclOrderCount){
				$("#unreadcount").html(wclOrderCount);
			}else{
				$("#unreadcount").html("0");
			}
			
			showWGDD(data.result);
		}
	}
	
	function showWGDD(result){
		
		for(var i=0;i<result.length;i++){
//			result[i].status_type = "计划";
			result[i].salesStatus=box.labels.ordersale[result[i].status];
			result[i].paymentStatus=box.labels.orderpay[result[i].pmt_status];
			result[i].salesType=box.labels.ordertype[result[i].order_type];
			result[i].amtys_ord_total = $.parseMoney(result[i].amtys_ord_total);
			result[i].amt_ord_total = $.parseMoney(result[i].amt_ord_total);
			
			//日期格式化为yyyy-MM-dd
			var orderDate=new Date(result[i].order_date.substr(0, 4),result[i].order_date.substr(4, 2)-1,result[i].order_date.substr(6, 2),
					result[i].order_time.substr(0, 2),result[i].order_time.substr(2, 2),result[i].order_time.substr(4, 2));
			result[i].order_date=orderDate.format("yyyy-MM-dd hh:mm:ss");
		}
		
		
		var content=box.ich.view_wgdd({list: result});
		parentView.empty().append(content);
		
/////////////////////
		
//		parentView.find("#beginTime").datepicker();
//		parentView.find("#endTime").datepicker();
//		
//		parentView.find("#beginTime").val(beginTime);
//		parentView.find("#endTime").val(endTime);
//		
//		parentView.find("#query").unbind("click").click(function(){
//			pageIndex = 1;
//			pageSize = 20;
//			getWGDDDate();
//		});

//////////
		
		if(result.length!=0){
			parentView.find("table tbody tr").click(onWGDDXiangXi);
			parentView.find("table tbody tr a").click(fahuo);
			
		}
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
	}
	
	function getWGDDDateAction(){
		getWGDDDate();
	}
	
	/**
	 * 订单发货、订单取消
	 */
	function fahuo(e){
		var order_id=$(e.currentTarget).attr("data-id");
		var order_type=$(e.currentTarget).attr("data-name");
		var confirmInfo = "是否确定废弃此订单："+order_id;
		var backInfo = "订单废弃成功！";
		if(order_type=="03") {
			confirmInfo = "是否发货？订单号："+order_id;
			backInfo = "发货成功！";
		}
		if(order_type=="04") {
			confirmInfo = "确定要取消订单？订单号："+order_id;
			backInfo = "取消订单成功！";
		}
		showConfirmDialog(order_id, order_type, confirmInfo, backInfo);
		return false;
	}
	
	/**
	 * 取消/发货确认窗口
	 */
	function showConfirmDialog(orderId, orderType, confirmInfo, backInfo) {
		var confirmDialog = box.ich.view_wgdd_confirm_dialog({context:confirmInfo});
		confirmDialog = box.showDialog({
			title: "提示",
			width: "360",
			height: "160",
			model: true,
			content: confirmDialog,
			buttons: [{
	            id: "yes",
	            text: "确 定",
	            "class": "primary_button",
	            click: function(){
	        		saleOperate(orderId,orderType,backInfo);
	            	box.closeDialog({content:confirmDialog}); // 关闭弹窗
	            }
	        },
	        {
	            text: "取 消",
	            "class": "closebtn_button",
	            click: function() {
	            	box.closeDialog({content:confirmDialog}); // 关闭弹窗
	            }
	        }]
		});
	}
	
	/**
	 * 网购订单状态处理
	 * order_id:需要操作的订单ID
	 * operate_type:处理类型03：完成、04：废弃
	 */
	function saleOperate(order_id,operate_type,backInfo){
		var url = "";
		if(operate_type=="03") {
			url = deliverSaleOrder;
		} else if(operate_type=="04") {
			url = removeSaleOrder;
		}
		box.request({
			url:box.getContextPath()+ url,
			data:{order_id:order_id},
			success: function(json) {
				if(json && json.code=="0000") {
					box.showAlert({message:backInfo});
					$("#unreadcount").html(parseFloat($("#unreadcount").html())-1);
				} else {
					box.showAlert({message:json.msg});
				}
				getWGDDDate();
			}
		});
	}
	
	function onWGDDXiangXi(e){
		var order_id=$(e.currentTarget).attr("data-id");
		showXiangxi(order_id);
	}
	
	function showXiangxi(order_id){
		box.request({
			url:box.getContextPath()+ xiangXiURL,
			data:{order_id:order_id},
			success:onGetWGDD_Detail
		});
	}
	
	function onGetWGDD_Detail(data){
		if(data && data.code=="0000"){
			data.result.amtys_ord_total = $.parseMoney(data.result.amtys_ord_total);
			data.result.amt_ord_total = $.parseMoney(data.result.amt_ord_total);
			
			for(var i=0;i<data.result.list.length;i++){
				data.result.list[i].amt_ord=$.parseMoney(data.result.list[i].amt_ord);
				data.result.list[i].big_pri3=$.parseMoney(data.result.list[i].big_pri3);
			}
				showWGDD_Detail(data.result);
		}
	}
	
	function showWGDD_Detail(result){
		
//		for(var i=0;i<result.list.length;i++){
//			var unit_id=result.list[i].unit_id;
//			//var unit_name=box.unit[unit_id];
//			//result[i].unit_name = unit_name;
////			result.list[i].pri3 = $.parseMoney(result.list[i].pri3);
////			data.result[i].big_pri3=$.parseMoney(data.result[i].big_pri3);
//			result.list[i].amt_ord = $.parseMoney(result.list[i].amt_ord);
//		}
		
		var content=box.ich.view_wgdd_detail({wgddDetail: result});
		box.showDialog({
			title:"流水明细",
			width:1000,
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
			box.listen("wgdd", onload);
		},
		destroy: function() { }
	};
	
	//获取当前时间
	function curDateTime() {
		var d = new Date();
		var year = d.getFullYear();
		var month = d.getMonth() + 1;
		var date = d.getDate();
		var day = d.getDay();
		var curDateTime = year;
		   if (month > 9)
		curDateTime = curDateTime + month;
		else
		curDateTime = curDateTime + "0" + month;
		if (date > 9)
		curDateTime = curDateTime + date;
		else
		curDateTime = curDateTime + "0" + date;
		   
		return curDateTime;
	}
});