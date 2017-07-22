/**
 * 非烟入库功能
 */
HOME.Core.register("plugin-fyrk", function(box) {
	
	var itemDetailUrl ="retail/item/searchMerchItemJoinWhseWithIncrease";
	var submitPurchOrderUrl = "retail/purch/submitPurchOrder";
	
	var staticnum = 1;
	
	var currentItem = null;
	var total = null; //quantity商品总量, amount商品总额

	var selectItemContainer = null;
	var selectItemDialog = null;
	var createItemDialog = null;
	
	var itemBarInput = null;
	var currentItemName = null;
	var currentItemPri1 = null;
	var currentItemPri2 = null;
	var currentItemCost = null;
	var currentItemKind = null;
	var currentItemSpec = null;
	var currentItemQtyWhse = null;
	var currentItemQuantity = null;
	var currentItemUnit = null;
	var whseUnit = null;
	var currentItemAmount = null;
	var currentItemType = null;

	var tableContainer = null;
	var table = null;
	var tableBody = null;

	var maxQty = null;
	var maxAmt = null;
	var cashButton = null;
	var clearButton = null;
	
    function stopPP(e) {
         var evt = e|| window.event;
         //IE用cancelBubble=true来阻止而FF下需要用stopPropagation方法
         evt.stopPropagation ?evt.stopPropagation() : (evt.cancelBubble=true);
    }
	
	function loadFYRK(parentView) {
		//初始化所有变量
		parentView.empty();
		originalQuantityValue = 0;
		originalAmountValue = 0.0;
		currentQuantityValue = 0;
		currentAmountValue = 0.0;
		unitMap = {};
		itemList = new Array();
		currentItem = new Object();
		total = {quantity: 0, amount: 0.0};
		parentView.append(box.ich.view_fyrk());
		itemListTableBody = parentView.find("#right_container tbody");
		selectItemContainer = parentView.find("#select_item_container");
		selectItemContainer.find("#order_bar").change(onItemBarInputChange);

		itemBarInput = parentView.find("#order_bar");
		currentItemName = parentView.find("#current_item_name");
		currentItemPri1 = parentView.find("#current_item_pri1");
		currentItemPri2 = parentView.find("#current_item_pri2");
		currentItemCost = parentView.find("#current_item_cost");
		currentItemKind = parentView.find("#current_item_kind");
		currentItemSpec = parentView.find("#current_item_spec");
		currentItemQtyWhse = parentView.find("#current_item_qty_whse");
		currentItemQuantity = parentView.find("#quantity");
		currentItemUnit = parentView.find("#current_item_unit");
		whseUnit = parentView.find("#whse_unit");
		currentItemAmount = parentView.find("#amount");
		
		currentItemType = parentView.find("#order_type");
		

		tableContainer = parentView.find("#right_container");
		table = tableContainer.find("table");
		tableBody = table.find("tbody");
		
		maxQty = parentView.find("#max_ord_qty");
		maxAmt = parentView.find("#max_ord_amt");
		cashButton = parentView.find("#cash_submit");
		clearButton = parentView.find("#clear_all");
		
		itemBarInput.change(onItemBarInputChange);
//		itemBarInput.keydown(function(e) {
//			if(e.keyCode == 13) {
//				currentItemQuantity.focus();
//			}
//		});
		
		currentItemQuantity.change(onQuantityInputChange);//操作数量
		currentItemAmount.change(onAmountInputChange);//操作金额
		
		currentItemType.click(onChoiceItemButtonClick);//类型选择
		
		cashButton.click(onStockButtonClick);//入库
		clearButton.click(onClearAllButtonClick);//清空
		
		tableBody.click(onTrClick);
		tableBody.select(onTrClick);
		
		table.fixedtableheader({
			parent: parentView.find("#right_container"),
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
	}
	
	function onItemBarInputChange(e) {
		var me = $(e.currentTarget);
		//没有输入不走逻辑
		if(!me.val()) {
			return;
		}
		getItem(me.val());
	}
	
	function getItem(itemBar) {
		
		box.request({
			url: box.getContextPath() + itemDetailUrl,
			data: {params:$.obj2str({big_bar: itemBar})},
			success: function(json) {
				if(json && json.code=="0000") {
					itemDetails = json.result;
					if(itemDetails && itemDetails.length>0) {
						if(itemDetails[0].status == "0"){
							box.showAlert({message:itemDetails[0].item_name+"等"+(itemDetails.length)+"个商品已删除！<br/>请在商品管理中恢复后再进行入库！"});
						}else{
							var item = null;
							for(var i=0; i<itemDetails.length; i++) {
								item = itemDetails[i];
								if(item.flag && item.flag=="BASE") {
									box.controls.newItemDialog({big_bar:item.item_bar,item_name:item.item_name}, function(result) {
	//									showItem(result);
										box.hideLoading();
										item=result[0];
										item.unit_name = item.item_unit_name;
										item.quantity = "1";
										item.pri = item.pri1* item.unit_ratio;
										item.amount = item.pri * item.quantity * item.unit_ratio;
										addItem(item);
									},true);
									return;
								}else{
									item.unit_name = item.item_unit_name;
									item.quantity = "1";
									item.pri = item.pri1* item.unit_ratio;
									item.amount = item.pri * item.quantity ;
									
								}
							}
							if(itemDetails.length>1) {
								selectItemDialog = box.ich.view_fyrk_select_item_dialog({list: itemDetails});
								selectItemDialog = box.showDialog({
									title: "商品选择",
									width: "640",
									height: "400",
									model: true,
									content: selectItemDialog
								});
								selectItemDialog.find("tr").click(onChoiceTrClick);
							} else {
								var itemDetail = itemDetails[0];
								addItem(itemDetail);
							}
						}
					} else {
//						box.showAlert({message: "此商品条码无法识别，请移步“基础数据-商品管理”页面新增此条码信息！"});
						box.controls.newItemDialog({big_bar:itemBar,item_name:itemBar}, function(result) {
//							showItem(result);
							box.hideLoading();
							item=result[0];
							item.unit_name = item.item_unit_name;
							item.quantity = item.unit_ratio;
							item.amount = item.pri1;
							item.bpri4 = parseFloat(item.pri4) / parseFloat(item.quantity);
							item.pri = parseFloat(item.amount) / parseFloat(item.quantity);
							addItem(item);
							
						},true);
					}
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
	}
	

	//弹出商品选择框后的点击操作
	function onChoiceTrClick(e) {
		var seqId = $(e.currentTarget).attr("data-id");
		
		for(var i=0; i<itemDetails.length; i++) {
			if(itemDetails[i].seq_id == seqId) {
				box.closeDialog({content: selectItemDialog});
				addItem(itemDetails[i]);
			}
		}
	}
	
	function onNotSplitRadioClick(e) {
		createItemDialog.find("._split").remove();
	}
	//方法为调用
	/*
	function onCanSplitRadioClick(e) {
		var me = $(e.currentTarget);
		var parent = me.parent();
		var splitLi = box.ich.view_fyrk_create_item_split_li();
		var unitId = splitLi.find("#unit_id");
		
		for(var unit in unitMap) {
			unitId.append($("<option value='"+unit+"'>"+unitMap[unit]+"</option>"));
		}
		//var unitMapLength = unitMap.length();
		//for(var i=0;i<=unitMapLength;i++){
		//	unitId.append($("<option value='"+i+"'>"+unitMap[i]+"</option>"));
		//}
		//alert("~~~~~~~");
		parent.after(splitLi);
	}
	*/
	function onNoDiscountRadioClick(e) {
		createItemDialog.find("._discount").remove();
	}
	
	function onHasDiscountRadioClick(e) {
		var me = $(e.currentTarget);
		var parent = me.parent();
		var discountLi = box.ich.view_fyrk_create_item_discount_li();
		parent.after(discountLi);
	}
	//方法为调用
	/*
	function onCreateItemButtonClick(e) {
		return;
//		var is_split = createItemDialog.find("input[name='is_split']:checked").val();
//		var is_discount = createItemDialog.find("input[name='is_discount']:checked").val();
//		var obj = {};
//		obj.big_bar = createItemDialog.find("#big_bar").text();
//		obj.item_name = createItemDialog.find("#item_name").val();
//		obj.pri1 = parseFloat(createItemDialog.find("#pri1").val());
//		obj.big_pri4 = createItemDialog.find("#big_pri4").val();
//		obj.unit_box = createItemDialog.find("#unit_box").val();
//		obj.item_kind_id = createItemDialog.find("#item_kind_id").val();
//		obj.spec = createItemDialog.find("#spec").val();
//		obj.qty_whse = createItemDialog.find("#qty_whse").val();
//		obj.is_split = is_split;
//		if(is_split=="1") {
//			obj.item_bar = createItemDialog.find("#item_bar").val();
//			obj.unit_id = createItemDialog.find("#unit_id").val();
//			obj.pri4 = createItemDialog.find("#pri4").val();
//			obj.unit_ratio = createItemDialog.find("#unit_ratio").val();
//			obj.pri1 = obj.pri1 / parseFloat(obj.unit_ratio);
//		} else {
//			obj.item_bar = createItemDialog.find("#big_bar").text();
//			obj.unit_id = createItemDialog.find("#unit_box").val();
//			obj.pri4 = createItemDialog.find("#big_pri4").val();
//			obj.unit_ratio = "1";
//		}
//		obj.is_discount = is_discount;
//		if(is_discount=="1") {
//			obj.discount = createItemDialog.find("#discount").val();
//			obj.start_date = createItemDialog.find("#start_date").val();
//			obj.end_date = createItemDialog.find("#end_date").val();
//		}
//		box.request({
//			url: box.getContextPath() + createItemUrl,
//			data: {params: $.obj2str(obj)},
//			success: function(json) {
//				if(json && json.code=="0000") {
//					var itemDetail = json.result;
//					addItem(itemDetail);
//					box.closeDialog({content: createItemDialog});
//				} else {
//					box.showAlert({message: json.msg});
//				}
//			}
//		});
	}
	*/
	
	//修改金额
	function onAmountInputChange(e) {
		var value = $(e.currentTarget).val();
		if(!value || isNaN(value)) {
			value = currentItem.amount;
			box.showAlert({message:"请输入正确的采购金额！"});
		}
		chekItem(currentItem, value, 2);
		currentItem.amount = $.parseMoney(value);
		if(parseFloat(currentItem.quantity)>0){
			currentItem.pri =$.parseMoney(parseFloat(currentItem.amount) / parseFloat(currentItem.quantity));
			
			refreshItem();
		}else{
			box.showAlert({message:"请输入正确的采购数量！"});
		}
		
	}
	
	//修改数量
	function onQuantityInputChange(e) {
		var value = $(e.currentTarget).val();
		if(!value || isNaN(value) || value <= 0) {
			value = currentItem.quantity;
			box.showAlert({message:"请输入正确的采购数量！"});
		}
		chekItem(currentItem, value, 1);
		currentItem.quantity = value;
		currentItem.amount =$.parseMoney( parseFloat(currentItem.pri) * parseFloat(currentItem.quantity));
		
		refreshItem();
	}
	
	//在商品列表中的点击操作
	function onTrClick(e) {
		var target = $(e.target);
		var tr = target.closest("tr");
		if(tr.length > 0) {
			if(target.attr("name") == "delete") {
				onDeleteItem(tr.data("item"));
				return;
			}
			
			selectItem(tr.data("item"), true);
		}
	}
	
	function onDeleteItem(data) {
		tableBody.find("tr[data-num="+data.line_num+"]").remove();
		itemBarInput.val("");
		
		if(currentItem) {
			if(currentItem.line_num == data.line_num) {
				selectItem(null);
			}
		}
		
		refreshTotalContainer();
		refreshLineLabel();
	}
	

	function addItem(item) {
		item.line_num = staticnum++;
		item.line_label = tableBody.children().length + 1;
		
		var itemList = new Array();
		$.each(tableBody.children(), function(i, obj) {
			var data = $(obj).data("item");
			itemList.push(data);
		});
		//判断入库单中是否已存在（判断标准big_bar）
		var haveItem=false;
		for(var i=0;i<itemList.length;i++){
			if(itemList[i].big_bar==item.big_bar && itemList[i].item_id==item.item_id ){
				haveItem=true;
				break;
			}
		}
		if(haveItem){
			box.showAlert({message: "入库单中已有该商品！"});
		}
		else{
			if(!item.qty_whse){
				item.qty_whse=0;
			}
			item.pri=$.parseMoney(item.pri);
			item.amount=$.parseMoney(item.amount);
			var tr = box.ich.view_fyrk_item_row(item);
			tableBody.append(tr);
			
			tableBody.find("tr[data-num="+item.line_num+"]").data("item", item);
			selectItem(item);
			
			refreshTotalContainer();
			
		}
	}
	
	function selectItem(data, onselect) {
		currentItem = data;
		if(currentItem) {
			currentItemName.text(currentItem.item_name);
			currentItemPri1.text($.parseMoney(currentItem.pri1*currentItem.unit_ratio));
			currentItemPri2.text($.parseMoney(currentItem.pri));
			currentItemCost.text($.parseMoney(currentItem.cost));
			currentItemKind.text(currentItem.item_kind_id);
			currentItemSpec.text(currentItem.spec);
			currentItemQtyWhse.text(currentItem.qty_whse);
			currentItemQuantity.val(currentItem.quantity);
			currentItemUnit.text(currentItem.big_unit_name);
			whseUnit.text(currentItem.unit_name);
			currentItemAmount.val( $.parseMoney(currentItem.amount));
			
			tableBody.find("tr").removeClass("selectRow");
			if(onselect) {
				tableBody.find("tr[data-num="+currentItem.line_num+"]").addClass("selectRow");
			}
			currentItemQuantity.focus();
			currentItemQuantity.select();
		} else {
			currentItemName.text("");
			currentItemPri1.text("0.0");
			currentItemPri2.text("0.0");
			currentItemKind.text("");
			currentItemSpec.text("");
			currentItemQtyWhse.text("0");
			currentItemQuantity.val("0");
			currentItemUnit.text("");
			whseUnit.text("");
			currentItemAmount.val("0.0");
			
			tableBody.find("tr").removeClass("selectRow");
		}
		
	}
	
	function refreshItem() {
		refreshTrItem(currentItem);
		
		currentItemName.text(currentItem.item_name);
		currentItemPri1.text($.parseMoney(currentItem.pri1*currentItem.unit_ratio));
		currentItemPri2.text($.parseMoney(currentItem.pri));
		currentItemCost.text($.parseMoney(currentItem.cost));
		currentItemKind.text(currentItem.item_kind_id);
		currentItemSpec.text(currentItem.spec);
		currentItemQtyWhse.text(currentItem.qty_whse);
		currentItemQuantity.val(currentItem.quantity);
		currentItemUnit.text(currentItem.big_unit_name);
		whseUnit.text(currentItem.unit_name);
		currentItemAmount.val($.parseMoney(currentItem.amount));
		
		refreshTotalContainer();
	}
	
	function refreshTrItem(item) {
		var tr = tableBody.find("tr[data-num="+item.line_num+"]");
		var view = box.ich.view_fyrk_item_row(item);
		//tr.replaceAll(view);
		tr.empty().append(view.html());
	}
	
	function refreshTotalContainer() {
		total.quantity = 0;
		total.amount = 0.0;
		
		var data = null;
		$.each(tableBody.children(), function(i, obj) {
			data = $(obj).data("item");
			total.quantity +=parseFloat(data.quantity);
			total.amount += parseFloat(data.amount);
		});
		
		maxQty.text(total.quantity);
		maxAmt.text($.parseMoney(total.amount));
	}

	function refreshLineLabel() {
		$.each(tableBody.children(), function(i, obj) {
			$(obj).find(".linelabel").text(i+1);
			$(obj).data("item").line_label = i+1;
		});
	}
	
	//入库操作
	function onStockButtonClick(e) {
		
		var itemList = new Array();
		var isNanItemList = true;
		$.each(tableBody.children(), function(i, obj) {
			isNanItemList = false;
			var data = $(obj).data("item");
			if(parseFloat(data.amount)==0||parseFloat(data.quantity)==0){
				box.showAlert({message: "请输入正确的金额或数量！"});
				box.stopEvent(e);
			}
			itemList.push(data);
		});
		
		if(isNanItemList == false){
			var obj = {status: "09", pmt_status: "03", qty_purch_total: total.quantity, amt_purch_total: total.amount, supplier_id: "0", list: itemList};
			box.request({
				url: box.getContextPath() + submitPurchOrderUrl,
				data: {params: $.obj2str(obj)},
				success: function(json) {
					if(json && json.code=="0000") {
						box.showAlert({message: "入库成功!"});
						clearAll();
					} else {
						box.showAlert({message: json.msg});
					}
					cashButton.removeAttr("disabled");
				},
				error: function(e) {
					cashButton.removeAttr("disabled");
				}
			});
		}else{
			box.showAlert({message: "请先选择要入库的商品！"});
			stopPP(e);
		}
	}
	
//	function onClearAllButtonClick(e) {
//		clearAll();
//	}
	
	function onClearAllButtonClick(e) {
		box.showConfirm({
			message: "确定要清空销售单吗？",
			ok: clearAll
		});
	}
	
	
	function clearAll() {
		tableBody.empty();
		selectItem(null);
		refreshTotalContainer();
		itemBarInput.val("");
	}
	
	
	function onChoiceItemButtonClick(e) {
		box.controls.choiceItemDialog(undefined, function(itemList) {
			showItem(itemList);
		}, currentItemQuantity, currentItemType);
	}

	function showItem(itemList) {
		var item=itemList;
		if(item.length>0) {
			for(var i=0; i<item.length; i++) {
				item[i].quantity = item[i].unit_ratio;
				item[i].pri = item[i].pri1;
				item[i].amount = parseFloat(item[i].quantity) * parseFloat(item[i].pri);
				
			}
			if(item.length==1) {
				addItem(item[0]);
			} else {
				selectItemDialog = box.ich.view_fyrk_select_item_dialog({list: item});
				selectItemDialog = box.showDialog({
					title: "重复商品",
					width: "750",
					height: "400",
					model: true,
					content: selectItemDialog,
					close: function(e) {
						selectItemDialog=null;
					}
				});
				selectItemDialog.find("tr").click(onChoiceTrClick(item));
			}
		} else {
			box.showAlert({message: "所输入的条形码没有对应商品！"});
		}
	}
	
	
	
	function fyrkOnKey(data){
		switch(data.key) {
		case HOME.Keys.DELETE:
			var tr=table.find("tr.selectRow");
			if(tr.length>0){
				onDeleteItem(tr.data("item"));
			}
			break;
		case HOME.Keys.BARCODE:
			onBarcode(data);
			break;
		}
	}
	

	function onBarcode(data) {
		// 反显扫码的结果
		if(data) {
			itemBarInput.val(data.code);
		}
		itemBarInput.change();
//		getItem(data.code);
	}
	
	function onClose(param) {
//		setTimeout(function() {
//			param.callback();
//		}, 3000);
		
		var itemList = new Array();
		var newItemIsEmpty = true;
		$.each(tableBody.children(), function(i, obj) {
			var data = $(obj).data("item");
			itemList.push(data);
		});
		if(itemList==null||itemList.length<1){
			newItemIsEmpty=false;
		}
		if(newItemIsEmpty){
			box.showConfirm({
				message: "入库单中有待入库商品，是否先进行入库操作？",
				hasSubmit:true,
				buttonText:["入 库","直接关闭","取 消"],
				submit:function(){
					cashButton.click();
				},
				donotSubmit:function(){
					param.callback();
				}
			});
		}else{
			return true;
		}
		return false;
		
	}
	
//	检查商品
//	type（1：数量，2：金额
//	a:卷烟单品采购数量大于1000条；
//	b:卷烟单品采购金额为商品信息中采购价3倍时；
	function chekItem(item, val, type){
		
		if (item.item_kind_id != "01") {
			return;
		}
		var itemPri1 = item.big_unit_ratio * item.pri1;
		if (type == 1) {
			if (val >= 1000) {
				box.showAlert({message:"商品入库数量过大！"});
			}
		} else {
			if (val >= itemPri1 * 3) {
				box.showAlert({message:"商品入库金额过大！"});
			}
		}
	}
	
	return {
		init: function(){
			box.listen("fyrk", loadFYRK);
			box.listen("fyrk_close", onClose);
			box.listen("fyrk_onkey", fyrkOnKey);
		},
		destroy: function() { }
	};
});
