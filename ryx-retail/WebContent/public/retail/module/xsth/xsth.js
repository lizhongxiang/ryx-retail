/**
 * 销售退货功能
 */
HOME.Core.register("plugin-xsth", function(box) {
	
	var parentView = null;
	var returnOrderURL="retail/order/submitReturnOrder"; // 提交退货单
	var saleReturnOrderUrl = "retail/order/searchSaleReturnOrder"; // 查询销售单-----实现退货
	var searchReturnOrderUrl = "retail/order/searchReturnOrder"; // 查询退货单
	var searchReturnOrderDetailUrl = "retail/order/searchReturnOrderDetail"; // 查询退货明细
	var orderDetail=null;
	var orderId=null;
    function stopPP(e) {
         var evt = e|| window.event;
         //IE用cancelBubble=true来阻止而FF下需要用stopPropagation方法
         evt.stopPropagation ?evt.stopPropagation() : (evt.cancelBubble=true);
    }
	
	function onload(view) {
		parentView = view;
		pageIndex = 1;
		pageSize = 20;
		
		showXSTH();//退货列表
	}
	
	//查询退货列表
	function showXSTH(){
		var params = {page_index: pageIndex, page_size: pageSize};
		box.request({
			url : box.getContextPath() + searchReturnOrderUrl,
			data: {params:$.obj2str(params)},
			success:onQueryButtonClick
		});
	}
	
	//退货列表
	function onQueryButtonClick(data) {
			if(data && data.code=="0000") {
				parentView.showPager(data.pageparams,function(param){
					pageIndex=param.page_index;
					pageSize=param.page_size;
					showXSTH();
				});
				var order = data.result;
				for(var i=0;i<order.length;i++){
					order[i].amt_return_total=$.parseMoney(order[i].amt_return_total);
					
					//日期时间格式化为yyyy-MM-dd hh:mm:ss
					var rtnOrderDate=new Date(order[i].return_order_date.substr(0, 4),order[i].return_order_date.substr(4, 2)-1,order[i].return_order_date.substr(6, 2),
							    order[i].return_order_time.substr(0, 2),order[i].return_order_time.substr(2, 2),order[i].return_order_time.substr(4, 2));
					order[i].return_order_date=rtnOrderDate.format("yyyy-MM-dd hh:mm:ss");
					
				}
				parentView.empty().append(box.ich.view_xsth({sale_order: order}));
				parentView.find("#query").unbind("click").click(onDetailQueryClick);//单击查询按钮，查询销售单，用于商品退货
				parentView.find("table tbody tr").unbind("click").click(onDetailTrClick);//查看退货详情
			} else {
				box.showAlert({message: data.msg});
			}
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			});
			parentView.showFooter(true);
	}
	

	//查询退货详情
	function onDetailTrClick(e) {
		var $me = $(e.currentTarget);
//		var orderId = $me.find("#order_id").text();
		orderId = $me.find("#return_order_id").text();
		box.request({
			url:box.getContextPath()+searchReturnOrderDetailUrl,
			data:{params:$.obj2str({return_order_id:orderId})},
			success:searchReturnOrderSuccess
		});
	}
	
	
	//打开退货明细界面
	function searchReturnOrderSuccess(data){
		
		if(data && data.code=="0000") {
			var order = data.result;
			order.amt_return_total = $.parseMoney(order.amt_return_total); 
			order.amt_ret_total = 0;
			order.qty_ret_total = 0;
			for(var index=0; index<order.list.length; index++) {
				var line = order.list[index];
				line.big_pri3=$.parseMoney(line.big_pri3);
				line.amt_ord = $.parseMoney(line.amt_ord);
				line.ret_color = "color:red;";
				order.amt_ret_total += parseFloat(line.amt_ret);
				order.qty_ret_total += parseFloat(line.qty_ret);
			}
			openReturnOrderLineDialog(order);//打开退货界面
		}
	}
	
	//打开退货明细界面
	function openReturnOrderLineDialog(order){
		
		orderDetail =box.ich.view_return_detail({order : order});
		orderDetail = box.showDialog({
			title:"退货明细",
			width:960,
			height:500,
			content: orderDetail
		});
		
		orderDetail.find("[name='checkbox']").unbind("change").change(onCheckboxChange(order.list));
		orderDetail.find("[name='qty_ret']").unbind("keyup").keyup(onQtyRetKeyup);
		orderDetail.find("[name='qty_ret']").unbind("blur").blur(onQtyRetKeyup);
		orderDetail.find("#cancel").unbind("click").click(onCancelButtonClick(orderDetail));
		
		orderDetail.find("tr").select(function(e){
			var target = $(e.target);
			if(target.attr("name")!="qty_ret") {
				$(this).find("#return_money").focus();
			}
		});
		
		orderDetail.find(".table_view").fixedtableheader({
			parent: orderDetail.find(".table_view").parent(),
			win: orderDetail,
			isshow: true 
		});
	}
	
	
	//单击查询按钮，查询销售流水，用于退货
	function onDetailQueryClick(){
//		var orderId = $me.find("#order_id").text();
		orderId = $.trim(parentView.find("#query_return_order_id").val());
		box.request({
			url:box.getContextPath()+saleReturnOrderUrl,
			data:{params:$.obj2str({order_id:orderId,role_id:'1'})},
			success:searchOrderSuccess
		});
	}
	
	//查询销售单
	function searchOrderSuccess(data){
		var order = null;
		if(data && data.code=="0000" && data.result.length>0) {
			if(data.result.length>0) {
				order = data.result[0];
				var returnMsg=null;
				if(order.status=='00'){
					returnMsg="此销售单已经废弃，不可退货";
				}else if(order.status=='01'){
					returnMsg="此销售单正在下单状态，不可退货";
				}else if(order.status=='02'){
					returnMsg="此销售单正在发货状态，不可退货";
				}
				if(order.status!='03'){
					box.showAlert({message:returnMsg});
				}
				order.amtys_ord_total = $.parseMoney(order.amtys_ord_total);
				order.amt_ord_loss = $.parseMoney(order.amt_ord_loss);
				order.amt_ret_total = 0;
				order.qty_ret_total = 0;
				for(var index=0; index<order.list.length; index++) {
					var line = order.list[index];
					line.index = index + 1;
					line.max_qty_ret = $.round(line.qty_ord - line.qty_ret , 2);
					line.default_qty_ret = line.max_qty_ret < 1 ? line.max_qty_ret : 1 ;
//					line.big_pri3 = $.parseMoney(parseFloat(line.amt_ord) / parseFloat(line.qty_ord));
					line.big_pri3 = $.parseMoney(line.big_pri3);
					line.pri3 = $.parseMoney(line.pri3);
					line.amt_ord = $.parseMoney(line.amt_ord);
//					line.return_money = $.parseMoney(line.default_qty_ret*line.big_pri3*line.discount/100);
					box.console.log(line.amt_ord+"--"+line.max_qty_ret+"--"+(line.amt_ord/line.max_qty_ret));
					line.return_money = $.parseMoney(0);
					if(line.max_qty_ret && line.max_qty_ret!=0){
//						var adjusted_amount = $(tr).attr("data-adjusted-amount");
//						var newReturnAmt=(parseFloat(amtOrd) - parseFloat(adjusted_amount)- parseFloat(otherAdjustedAmount))/parseFloat(qtyOrd)*parseFloat(qtyRet);
						line.return_money = $.parseMoney(parseFloat(line.amt_ord ) / parseFloat(line.qty_ord) * parseFloat(line.default_qty_ret));
//						alert("销售金额："+line.amt_ord+"===销售单行让利："+line.other_adjusted_amount+"===销售数量："+line.qty_ord+"==="+line.return_money);
					}
					if(line.default_qty_ret==0) {
						line.style = 'style="display:none;"';
					}
					if(line.qty_ret>0) {
						line.ret_color = "color:red;";
					}
					order.amt_ret_total += parseFloat(line.amt_ret);
					order.qty_ret_total += parseFloat(line.qty_ret);
				}
			}
			openSaleOrderLineDialog(order);//打开销售详情界面--可退货
		}else{
			if(orderId){
				box.showAlert({message:"没有查到此 "+orderId+" 单号的销售单！" });
			}else{
				box.showAlert({message:"请输入销售单号！" });
			}
		}
	}
	
	//打开销售明细界面---可退货
	function openSaleOrderLineDialog(order){
		if(order.qty_ret_total>0) order.return_color = "color:red;";
		orderDetail =box.ich.view_xsth_detail({order : order});
		var orderTbody=orderDetail.find("tbody");
		$(orderTbody).attr("data-consumer-id",order.consumer_id);
		$(orderTbody).attr("data-merch-id",order.merch_id);
		$(orderTbody).attr("data-order-type",order.order_type);
		$(orderTbody).attr("data-status",order.status);
		$(orderTbody).attr("data-order-id",order.order_id);
		orderDetail = box.showDialog({
			title:"销售退货",
			width:960,
			height:500,
			content: orderDetail
		});
		
		if(order.status!='03'){ 
			orderDetail.find("#return").attr("disabled", true);
			orderDetail.find("#return_all").attr("disabled", true);
			orderDetail.find("#return_all").css("background","#666666");
			orderDetail.find("#return").css("background","#666666");
		}
		orderDetail.find("[name='checkbox']").unbind("change").change(onCheckboxChange(order.list));//选择退货商品
		orderDetail.find("[name='qty_ret']").unbind("keyup").keyup(onQtyRetKeyup);///退货数量检测
		orderDetail.find("[name='qty_ret']").unbind("blur").blur(onQtyRetKeyup);//退货数量检测
		orderDetail.find("#return").unbind("click").click(onReturnButtonClick(orderDetail, false));//部分退货
		orderDetail.find("#return_all").unbind("click").click(onReturnButtonClick(orderDetail, true));//全单退货
		orderDetail.find("#cancel").unbind("click").click(onCancelButtonClick(orderDetail));
		
		orderDetail.find("tr").select(function(e){
			var target = $(e.target);
			if(target.attr("name")!="qty_ret") {
				$(this).find("#return_money").focus();
			}
		});
		
//		orderDetail.find(".table_view").fixedtableheader({
//			parent: orderDetail,
//			win: orderDetail,
//			isshow: true 
//		});
	}
	
	//取消商品退货，
	function onCancelButtonClick(orderDetail) {
		return function(e) {
			box.closeDialog({content:orderDetail});
		};
	}
	
	//商品退货时，检测商品退货的复选框是否可选
	function onCheckboxChange(lineList) {
		return function(e) {
			var me = $(e.currentTarget);
			var tr = me.closest("tr");
			if(me.is(':checked')) {
				var maxQtyRet = parseFloat(me.closest("tr").find("#max_qty_ret").text());
				if(maxQtyRet > 0){
					tr.find("#qty_ret").removeAttr("disabled");
					tr.find("#return_money").removeAttr("disabled");
					tr.find("input[name='checkbox_ruku']").removeAttr("disabled");
				}else{
					me.prop("checked",false);
					box.showAlert({message:"对不起，此商品最大可退量为 0 "});
				}
			} else {
				tr.find("#qty_ret").attr("disabled", "disabled");
				tr.find("#return_money").attr("disabled", "disabled");
				tr.find("input[name='checkbox_ruku']").attr("disabled", "disabled");
			}
		};
	}
	
	//商品退货时，检查输入的商品数量
	function onQtyRetKeyup(e) {
		var me = $(e.currentTarget);
		var qtyRet = parseFloat(me.val());
		var tr = me.closest("tr");
		var maxQtyRet =parseFloat( $(tr).find("[name='max_qty_ret']").text());
		if(qtyRet>maxQtyRet) {
			me.val(maxQtyRet);
			box.showAlert({message:"退货数量不能超过最大可退数量"});
		} else {
			var thisTr = me.closest("tr");
//			var pri3 = thisTr.find("#pri3").text();
//			var discount = thisTr.find("#discount").text();
			var amtOrd=thisTr.find("#amt_ord").text();//销售总金额
			var qtyOrd=thisTr.find("#qty_ord").text();//销售总数量
			var otherAdjustedAmount = $(tr).attr("data-other-adjusted-amount");
			var adjustedAmount = $(tr).attr("data-adjusted-amount");
			var newReturnAmt = (parseFloat(amtOrd) /parseFloat(qtyOrd)*parseFloat(qtyRet)).toFixed(3);
//			thisTr.find("#return_money").val($.parseMoney(qtyRet*parseFloat(pri3)*parseFloat(discount)/100));
			thisTr.find("#return_money").val($.parseMoney(newReturnAmt));
		}
	}
	
	
	//退货操作
	function onReturnButtonClick(orderDetail, isAllReturn){
		return function(e) {
			var obj = {};
			var list = [];
			var cs=null;
			if( isAllReturn ) {
				cs=$("input:checkbox").parent().parent();
//				var qty_ret_total = orderDetail.find("#qty_ret_total").text();
				var adjusted_amount = orderDetail.find("#adjusted_amount").text();
				obj.adjusted_amount=adjusted_amount;
				obj.amtys_ord_total = orderDetail.find("#amtys_ord_total").text();
			} else {
				cs=$("input[name='checkbox']:checked").parent().parent();
			}
			if(cs.length>0){
				var tbodycontent= orderDetail.find("#returnTbody");
				var messageList = [] ;
				if(!$.validateForms( orderDetail, messageList)) {
					box.showAlert({message: messageList[0]});
					return;
				}
				obj.order_id = $(tbodycontent).attr("data-order-id");
				obj.consumer_id = $(tbodycontent).attr("data-consumer-id");
				obj.order_type = $(tbodycontent).attr("data-order-type");
				obj.status = $(tbodycontent).attr("data-status");
				
				$.each(cs, function(i, obj) {
					$obj = $(obj);
					var inputQty = parseFloat($obj.find("#qty_ret").val());//输入的数量文本框
					var returnMoney = $obj.find("#return_money").val();//输入的退货金额
					var max_qty_ret = parseFloat($obj.find("#max_qty_ret").attr("data-ret"));//最大可退量
					var yiTuiHuoShuLiang = $obj.find("#qty_returned").text();//已经退货数量
					var amt_ord = $obj.find(".amtord").text();//销售总价
					var qtyOrd=$obj.find("#qty_ord").text();//销售总量
					var item_id=$obj.attr("data-item-id");//id
					var cost = $obj.attr("data-cost");//成本
					var pri3 = $obj.attr("data-pri3"); // pri3
					var big_unit_name = $obj.attr("data-big-unit-name");//当前使用包装
					var unit_ratio = $obj.attr("data-unit-ratio");//转换比
					var unit_name = $obj.attr("data-unit-name");//最小包装
					var line_num = $obj.attr("data-line-num");
					var item_bar = $obj.attr("data-item-bar");//最小包装
					var big_bar = $obj.attr("data-big-bar");//当前使用包装
					var item_name = $obj.find("#item_name").text();
					var big_pri3 = $obj.attr("data-big-pri3");
					var otherAdjustedAmount = $obj.attr("data-other-adjusted-amount");
					var adjusted_amount = $obj.attr("data-adjusted-amount");
					var isResalable = ($obj.find("input[name='checkbox_ruku']")).prop("checked") ? 1 : 0;
					
					if( inputQty > 0 && max_qty_ret > 0 ) {
						if(isAllReturn) {
							inputQty = (qtyOrd - yiTuiHuoShuLiang).toFixed(3);
							returnMoney = (parseFloat(amt_ord)/parseFloat(qtyOrd)*parseFloat(inputQty)).toFixed(3);
							$obj.find("#return_money").val($.parseMoney(returnMoney));
						}
						
						returnAadjustedAmount =( (parseFloat(adjusted_amount) + parseFloat(otherAdjustedAmount)) / parseFloat(qtyOrd) * parseFloat(inputQty)).toFixed(3);
//						var profit = 0;
//						if(isRuKu){
//							profit = ( (parseFloat(returnMoney) / parseFloat(unit_ratio) / parseFloat(inputQty)) - parseFloat(cost)).toFixed(3); 
//						} else {
//							profit = ( (parseFloat(returnMoney) / parseFloat(unit_ratio) / parseFloat(inputQty))).toFixed(3);
//						}
						list.push({big_pri3:big_pri3, unit_ratio:unit_ratio, big_unit_name:big_unit_name,
							item_bar:item_bar, big_bar:big_bar, return_money:returnMoney, item_name:item_name,
							unit_name:unit_name, line_num:line_num, item_id:item_id, pri3:pri3, qty_ord: inputQty,
							cost:cost, adjusted_amount:returnAadjustedAmount, is_resalable:isResalable });
					}
				});
				
				if(list.length) {
					obj.list = list;
					obj.amount_per_point = box.user.amount_per_point;
					
					box.request({
						url: box.getContextPath() + returnOrderURL,
						data: {params: $.obj2str(obj)},
						success: function(json) {
							box.showAlert({message:"商品退换成功"});
							box.closeDialog({content:orderDetail});
							showXSTH();
							if(isAllReturn) {
								orderDetail.find("#return_all").removeAttr("disabled");
							} else {
								orderDetail.find("#return").removeAttr("disabled");
							}
						},
						error: function(e) {
							if(isAllReturn) {
								orderDetail.find("#return_all").removeAttr("disabled");
							} else {
								orderDetail.find("#return").removeAttr("disabled");
							}
						}
					});
				} else {
					box.showAlert({message:"无符合退货条件的商品"});
					stopPP(e);
//					orderDetail.find("#return").removeAttr("disabled");
				}
			}else{
				box.showAlert({message:"请先选择要退货的商品"});
				stopPP(e);
//				orderDetail.find("#return").removeAttr("disabled");
			}
		};
	}
	
	return {
		init: function(){
			box.listen("xsth", onload);
		},
		destroy: function() { }
	};
});