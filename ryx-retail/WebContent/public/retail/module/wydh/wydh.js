/**
 * 非烟我要订货功能
 */
HOME.Core.register("plugin-wydh", function(box) {
	var supplierItemListURL = "/retail/supplier/item/list";
	var supplierMerchShopingCartAdd = "/retail/supplier/cart/add";
	var parentView = null;
	var content = null;
	var pageNum = 1;
	var pageSize = 20;
	var itemListData = null;
	var itemList = null;
	function onload(view){
		parentView = view;
		content = box.ich.view_wydh();
		content.find("#good_submit").click(goToCart);
		parentView.empty().append(content);
		content.find("#itemQuery").unbind("click").click(showItem);
		content.find("#itemQuery").trigger("click");
		parentView.showFooter(true);
		
	}
	function showItem(){
		var param = content.find("#goods_bar").val();
		var obj = new Object();
		obj.param = param;
		obj.pageNum = pageNum;
		obj.pageSize = pageSize;
		box.request({
			dataType:"json",
			url: box.getContextPath()+supplierItemListURL,
			data: {params: $.obj2str(obj)},
			success:function(data){
				itemListData = data.result.itemlist.rows;
				var totalInfo = data.result.totalinfo;
				content.find("#itemNumTotal").empty().html(totalInfo.item_total);
				content.find("#itemPriceTotal").empty().html(totalInfo.total_price);
				itemList =  box.ich.view_wydh_item_list({list:itemListData});
				itemList.find(".img img").click(function(){
					var itemID = $(this).attr("itemID");
					onGoodClick(itemID);
				});
				itemList.find(".title").click(function(){
					var itemID = $(this).attr("itemID");
					onGoodClick(itemID);
				});
				itemList.find(".addCartButton").click(function(e){
					var itemID = $(this).attr("itemID");
					var itemNum = itemList.find("#itemNum_"+itemID).val();
					var wholePrice =  $(this).attr("wholePrice");
					addMerchCart(itemID,itemNum,wholePrice,"1",e);
				});
				itemList.find(".downButton").click(function(){
					var itemID = $(this).attr("itemID");
					var num = itemList.find("#itemNum_"+itemID).val();
					if(num-1<0){
						num = 0;
					}
					else{
						num = num -1;
					}
					itemList.find("#itemNum_"+itemID).val(num);
					return false;
				});
				itemList.find(".upButton").click(function(){
					var itemID = $(this).attr("itemID");
					var num = itemList.find("#itemNum_"+itemID).val();
					num++;
					itemList.find("#itemNum_"+itemID).val(num);
					return false;
				});
				content.find("#item_goods_content").empty().append(itemList);
			}
		});
	}
	var itemDialog = null;
	/*弹出商品详情窗口*/
	function onGoodClick(itemID) {
		var choseItem = null;
		for(var i = 0 ; i< itemListData.length; i++){
			var item = itemListData[i];
			if(item.id == itemID){
				choseItem = item;
				break;
			}
		}
		itemDialog = box.ich.view_wydh_goodsinfo({item:choseItem});
		infoDialog = box.showDialog({
			title: "商品详情",
			width: "800",
			height: "430",
			model: true,
			content: itemDialog
		});
		itemDialog.find("#addCartButton").click(function(e){
			var itemID = $(this).attr("itemID");
			var itemNum = infoDialog.find("#itemNum").val();
			var wholePrice =  $(this).attr("wholePrice");
			addMerchCart(itemID,itemNum,wholePrice,"2",e);
		});
		itemDialog.find("#downButton").click(function(){
			var num = itemDialog.find("#itemNum").val();
			if(num-1<0){
				num = 0;
			}
			else{
				num = num -1;
			}
			itemDialog.find("#itemNum").val(num);
			return false;
		});
		itemDialog.find("#upButton").click(function(){
			var num = itemDialog.find("#itemNum").val();
			num++;
			itemDialog.find("#itemNum").val(num);
			return false;
		});
	}
	/**
	 * 添加购物车
	 */
	function addMerchCart(itemID,itemNum,wholePrice,type,e){
		if(0>=parseFloat(itemNum)){
			box.showAlert({message:"请选择商品数量！"});
			if(type == "1"){
				content.find("#itemButton_"+itemID).attr("disabled",false);
			}
			else if(type == "2"){
				itemDialog.find("#addCartButton").attr("disabled",false);
			}
			box.stopEvent(e);
			return ;
		}
		var choseItem = null;
		for(var i = 0 ; i< itemListData.length; i++){
			var item = itemListData[i];
			if(item.id == itemID){
				choseItem = item;
				break;
			}
		}
		var obj = new Object();
		obj.supplierID = choseItem.supplier_id;
		obj.itemID = choseItem.id;
		obj.num = itemNum;
		obj.wholePrice = choseItem.whole_prise;
		obj.itemNum = itemNum;
		box.request({
			dataType:"json",
			url: box.getContextPath()+supplierMerchShopingCartAdd,
			data: {params: $.obj2str(obj)},
			success:function(data){
				if(type == "1"){
					content.find("#itemButton_"+itemID).attr("disabled",false);
				}
				else if(type == "2"){
					itemDialog.find("#addCartButton").attr("disabled",false);
				}
				if(data.code == "0000"){
					box.showAlert({message:"购物车添加成功！"});
					var numTotal = parseFloat(content.find("#itemNumTotal").html());
					numTotal = numTotal + parseFloat(itemNum);
					content.find("#itemNumTotal").html(numTotal);
					var priceTotal = parseFloat(content.find("#itemPriceTotal").html());
					priceTotal = priceTotal + parseFloat(itemNum)*parseFloat(wholePrice);
					content.find("#itemPriceTotal").html(priceTotal);
				}
				else{
					box.showAlert({message:"购物车添加失败！"});
				}
			}
		});
	}
	
	/*弹出购物车窗口*/
	function goToCart(){
		var cartDialog = box.ich.view_wydh_cart();
		infoDialog = box.showDialog({
			title: "我的购物车",
			width:960,
			height:500,
			model: true,
			content: cartDialog
		});
	}
	return {
		init:function(){
			box.listen("wydh", onload);
		},
		destroy: function(){}
	};
	
	
});