/**
 * 便门服务--数字电视缴费
 */
HOME.Core.register("plugin-ddgl", function(box) {
	var orderListHis = "/retail/supplier/order/listHis"
	var orderDetailInfo = "/retail/supplier/order/info";
	var beginTime="";
	var endTime="";
	var content = null;
	var pageNum = 1;
	var pageSize = 20;
	function onload(view){
		parentView = view;
		var end = $.GetToday(0);
		var start = $.GetToday(-6);
		content = box.ich.view_ddgl({start:start,end:end});
		//初始日期控件
		content.find("#beginTime").datepicker({
			maxDate: 'd' 
		});
		//初始日期控件
		content.find("#endTime").datepicker({
			maxDate: 'd' 
		});
		content.find("#orderQuery").click(showOrderHis);
		parentView.empty().append(content);
		showOrderHis();
		parentView.showFooter(true);
	}
	function showOrderHis(){
		var startDate = formateDateToDate(content.find("#beginTime").val())+"000000";
		var endDate = (parseInt(formateDateToDate(content.find("#endTime").val()))+1)+"000000";
		var searchParam = content.find("#searchParam").val();
		var obj = new Object();
		obj.startDate = startDate;
		obj.endDate = endDate;
		obj.pageNum = pageNum;
		obj.pageSize = pageSize;
		obj.searchParam = searchParam;
		box.request({
			url: box.getContextPath() + orderListHis,
			data:{params:$.obj2str(obj)},
			success:  function(data){
				var rows = data.result.rows;
				for(var i=0;i<rows.length;i++){
					rows[i].index = i+1;
					if(rows[i].status == "0"){
						rows[i].status = "无效订单";
					}
					else if(rows[i].status == "1"){
						rows[i].status = "订单提交";
						rows[i].action = "取消订单";
					}
					else if(rows[i].status == "2"){
						rows[i].status = "已支付";
					}
					else if(rows[i].status == "3"){
						rows[i].status = "已发货";
					}
					else if(rows[i].status == "4"){
						rows[i].status = "已收货";
					}
					rows[i].create_date=formatDate_(rows[i].create_date);
				}
				var orderListHis = box.ich.view_ddgl_list({list:rows});
				content.find("#orderListHis").empty().append(orderListHis)
				content.find("table tbody tr").click(function(e){
					var orderID = $(this).attr("orderID");
					var supplierID = $(this).attr("supplierID");
					onOrderClick(e,orderID,supplierID);
				});
			}
		});
	}
	function onOrderClick(e,orderID,supplierID) {
		var obj = {};
		obj.orderID = orderID;
		obj.supplierID = supplierID;
		box.request({
			url: box.getContextPath() + orderDetailInfo,
			data:{params:$.obj2str(obj)},
			success:function(data){
				var order = data.result;
				var addressinfo = data.result.addressinfo;
				order.create_date=formatDate_(order.create_date)
				if(order.order_status == "0"){
					order.status = "无效订单";
				}
				else if(order.order_status == "1"){
					order.order_status = "订单提交";
				}
				else if(order.order_status == "2"){
					order.pay_date = formatDate_(order.pay_date);
					order.order_status = "已支付";
				}
				else if(order.order_status == "3"){
					order.pay_date = formatDate_(order.pay_date);
					order.delivery_date = formatDate_(order.delivery_date);
					order.order_status = "已发货";
				}
				else if(order.order_status == "4"){
					order.pay_date = formatDate_(order.pay_date);
					order.delivery_date = formatDate_(order.delivery_date);
					order.accept_date = formatDate_(order.accept_date);
					order.order_status = "已收货";
				}
				dialogView = box.ich.view_ddgl_info({list:order,addressinfo:addressinfo});
				dialogView = box.showDialog({
					title: "订单明细",
					width: 960,
					height: 560,
					content: dialogView
				});
			}
		});
		
		/*dialogView = box.ich.view_ddgl_info();
		dialogView = box.showDialog({
			title: "订单明细",
			width: 960,
			height: 570,
			content: dialogView
		});*/
	}
	function formatDate_(date){
		if(date && date != ""){
			var orderDate=new Date(date.substr(0, 4),date.substr(4, 2)-1,date.substr(6, 2),
					date.substr(8, 2),date.substr(10, 2),date.substr(12, 2));
			return  orderDate.format("yyyy-MM-dd hh:mm:ss");
		}
		else{
			return "";
		}
	}
	
	function formateDateToDate(date_){
	    //日期格式化为yyyyMMdd
	    var consumeDateFormat=new Date(date_.substr(0, 4),date_.substr(5, 2)-1,date_.substr(8, 2));
	    return consumeDateFormat.format("yyyyMMdd");
	}
	return {
		init:function(){
			box.listen("ddgl", onload);
		},
		destroy: function(){}
	};
});