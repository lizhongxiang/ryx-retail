/**
 * 购物车
 */
HOME.Core.register("plugin-gwc", function(box) {
	var cartListURL = "/retail/supplier/cart/list";
	var orderSubmit = "/retail/supplier/order/submit";
	var updateCartNumURL = "/retail/supplier/cart/update";
	var deleteCartAll = "/retail/supplier/cart/delteAll";
	var content = null;
	function onload(view){
		parentView = view;
		content = box.ich.view_gwc();
		parentView.empty().append(content);
		content.find("#orderSubmit").click(function(e){
			submitOrder(e);
		});
		content.find("#cbxSelectAllCarts").click(function(){
		});
		content.find("#clearAllcarts").click(function(){
			box.request({
				dataType:"json",
				url: box.getContextPath()+deleteCartAll,
				data: {},
				success:function(data){
					if(data.code == "0000"){
						box.showAlert({message:"清除成功"});
						showCartContent();
					}
					else{
						box.showAlert({message:"删除失败"});
					}
				}
			});
		});
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
		showCartContent();
	}
	var cartContent = null;
	function showCartContent(){
		box.request({
			dataType:"json",
			url: box.getContextPath()+cartListURL,
			data: {},
			success:function(data){
				var carts = data.result;
				var totalPrice = 0;
				var totalNum = 0;
				for(var i = 0 ; i < carts.length ; i++){
					var price = 0;
					var num = 0 ;
					for(var j = 0 ; j<carts[i].carts.length ; j++){
						var itemTotal = carts[i].carts[j].num * carts[i].carts[j].whole_prise;
						carts[i].carts[j].total = itemTotal;
						price = price + itemTotal;
						totalNum = parseInt(totalNum)+parseInt(carts[i].carts[j].num);
					}
					totalPrice = totalPrice+price;
					carts[i].totalPrice=price;
				}
				cartContent = box.ich.view_gwc_cartContent({list:carts});
				content.find("#allTotalNum").empty().append(totalNum);
				content.find("#allTotalPrice").empty().append(totalPrice);
				content.find("#payContent").empty().append(cartContent);
				cartContent.find(".downButton").click(function(e){
					var cartID = $(this).attr("cartID");
					var num = parseInt(cartContent.find("#amount_"+cartID).val())-1;
					updateCartNum(cartID,num,e);
					$(this).attr("disabled",true);
					return false;
				});
				cartContent.find(".upButton").click(function(e){
					var cartID = $(this).attr("cartID");
					var num = parseInt(cartContent.find("#amount_"+cartID).val())+1;
					updateCartNum(cartID,num,e);
					$(this).attr("disabled",true);
					return false;
				});
				cartContent.find(".supplierItemCart_").focus(function(e){
					var cartID = $(this).attr("cartID");
					var num = $(this).val();
					$(this).blur(function(){
						var num_ = $(this).val();
						if(num != num_){
							updateCartNum(cartID,num_,e);
						}
					});
				});
				cartContent.find(".supplierItemCart").click(function(e){
					var cartID = $(this).val();
					var itemNum = parseFloat(cartContent.find("#amount_"+cartID).val());
					var oldTotalNum = parseFloat(content.find("#allTotalNum").html());
					var itemPrice = parseFloat(cartContent.find("#cartPrice_"+cartID).html());
					var oldTotalPrice = parseFloat(content.find("#allTotalPrice").html());
					//维持选中不选中状态
					var supplierID = $(this).attr("supplierID");
					if($(this).prop("checked")){
						cartContent.find("#supplier_"+supplierID).prop("checked",true);
						//更新总数量
						content.find("#allTotalNum").html(oldTotalNum+itemNum)
						//更新总金额
						content.find("#allTotalPrice").html(itemPrice+oldTotalPrice)
					}
					else{
						if(cartContent.find(".cart_"+supplierID+":checked").length == 0 ){
							cartContent.find("#supplier_"+supplierID).prop("checked",false);
						}
						//更新总数量
						content.find("#allTotalNum").html(oldTotalNum-itemNum);
						//更新总金额
						content.find("#allTotalPrice").html(oldTotalPrice-itemPrice);
					}
				});
				cartContent.find(".supplier").click(function(e){
					//维持选中不选中状态
					var supplierID = $(this).val();
					if($(this).prop("checked")){
						var newTotalPrice = 0;
						var newTotalNum = 0;
						cartContent.find(".cart_"+supplierID).each(function(){
							if(!$(this).prop("checked")){
								var cartID = $(this).val();
								var itemNum = parseFloat(cartContent.find("#amount_"+cartID).val());
								var itemPrice = parseFloat(cartContent.find("#cartPrice_"+cartID).html());
								newTotalPrice = newTotalPrice+itemPrice;
								newTotalNum = newTotalNum + itemNum;
								$(this).prop("checked",true);
							}
						})
						var oldTotalNum = parseFloat(content.find("#allTotalNum").html());
						var oldTotalPrice = parseFloat(content.find("#allTotalPrice").html());
						content.find("#allTotalNum").html(oldTotalNum+newTotalNum)
						content.find("#allTotalPrice").html(oldTotalPrice+newTotalPrice)
						
					}
					else{
						var newTotalPrice = 0;
						var newTotalNum = 0;
						cartContent.find(".cart_"+supplierID).each(function(){
							if($(this).prop("checked")){
								var cartID = $(this).val();
								var itemNum = parseFloat(cartContent.find("#amount_"+cartID).val());
								var itemPrice = parseFloat(cartContent.find("#cartPrice_"+cartID).html());
								newTotalPrice = newTotalPrice-itemPrice;
								newTotalNum = newTotalNum - itemNum;
								$(this).prop("checked",false);
							}
						})
						var oldTotalNum = parseFloat(content.find("#allTotalNum").html());
						var oldTotalPrice = parseFloat(content.find("#allTotalPrice").html());
						content.find("#allTotalNum").html(oldTotalNum+newTotalNum)
						content.find("#allTotalPrice").html(oldTotalPrice+newTotalPrice)
					}
				});
				cartContent.find(".del_point").click(function(){
					var cartID = $(this).attr("cartID");
					deleteCart(cartID);
					return false;
				});
			}
		});
	}
	function deleteCart(cartID){
		var obj = {};
		obj.cartID = cartID;
		box.request({
			dataType:"json",
			url: box.getContextPath()+deleteCartAll,
			data: {params: $.obj2str(obj)},
			success:function(data){
				if(data.code=="0000"){
					box.showAlert({message:"删除成功"});
					showCartContent();
				}
				else{
					box.showAlert({message:"删除失败"});
				}
			}
		});
	
	}
	function updateCartNum(cartID,num,e){
		if(num<=0){
			cartContent.find(".downButton").attr("disabled",false);
			box.showAlert({message:"值无效"});
			return ;
		}
		var obj = {};
		obj.cartID = cartID;
		obj.num = num;
		box.request({
			dataType:"json",
			url: box.getContextPath()+updateCartNumURL,
			data: {params: $.obj2str(obj)},
			success:function(data){
				if(data.code == "0000"){
					var cartInfo = cartContent.find("#amount_"+cartID);
					//定价
					var wholePrice = parseFloat(cartInfo.attr("wholePrice"));
					//原数量
					var numOld = parseFloat(cartInfo.val());
					//商家编号
					var supplierID = cartInfo.attr("supplierID");
					var cartTotal = parseFloat(cartContent.find("#cartPrice_"+cartID).html());
					cartTotal = cartTotal+(num-numOld)*wholePrice;
					cartContent.find("#cartPrice_"+cartID).html(cartTotal)
					var supplierTolal = parseFloat(cartContent.find("#supplier_price_"+supplierID).html());
					supplierTolal = supplierTolal+(num-numOld)*wholePrice;
					var allTotalPrice = parseFloat(content.find("#allTotalPrice").html());
					allTotalPrice = allTotalPrice+(num-numOld)*wholePrice;
					content.find("#allTotalPrice").html(allTotalPrice);
					var allTotalNum = parseFloat(content.find("#allTotalNum").html());
					allTotalNum = allTotalNum + (num-numOld);
					content.find("#allTotalNum").html(allTotalNum);
					cartContent.find("#supplier_price_"+supplierID).html(supplierTolal);
					cartContent.find("#amount_"+cartID).val(num);
					box.stopEvent(e);
					box.showAlert({message:"更新成功"});
					cartContent.find(".downButton").attr("disabled",false);
					cartContent.find(".upButton").attr("disabled",false);
					
				}
				else{
					box.showAlert({message:"更新失败"});
				}
			}
		});
	}
	
	
	function submitOrder(e) {
		//var cartIDS = content.find("input:radio[name='cdlx']:checked").
		var cartIDS = "";
		content.find('input[name="supplierItemCart"]:checked').each(function(){    
			cartIDS = cartIDS+$(this).val()+",";
		});
		if(cartIDS == "" && content.find('input[name="supplierItemCart"]').length>0){
			box.showAlert({message:"请选择商品"});
			return;
		}
		else if(cartIDS == "" && content.find('input[name="supplierItemCart"]').length==0){
			box.showAlert({message:"购物车中无信息"});
			return;
		}
		box.request({
			dataType:"json",
			url: box.getContextPath()+orderSubmit,
			data: {cartIDS:cartIDS,addressID:"1231232131231"},
			success:function(data){
				var list = data.result.orderlist;
				data.result.orderlist = "";
				var totalPrice = 0;
				var totalNum = 0;
				for(var i = 0 ; i<list.length;i++){
					totalPrice = parseFloat(totalPrice)+parseFloat(list[i].prise_total);
					totalNum = parseInt(totalNum)+parseInt(list[i].num);
				}
				var infoDialog = box.ich.view_checkOrder({list:list,totalPrice:totalPrice,totalNum:totalNum,address:data.result});
				infoDialog = box.showDialog({
					title: "订单信息",
					width: "960",
					height: "540",
					content: infoDialog
				});
			}
		});
	}
	return {
		init:function(){
			box.listen("gwc", onload);
		},
		destroy: function(){}
	};
});