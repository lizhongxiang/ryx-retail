/**
 * 商品管理功能
 */
HOME.Core.register("plugin-spgl", function(box) {
	
	var parentView = null;
	var currentDialog = null;
	var staticItemBar = "";
	var staticItemKindId = "";
	var floor_big_pri4 = "";
	var ceiling_big_pri4 = "";
	var pageIndex = 1;
	var pageSize = 20;
	var searchUnitJoinMerchItemUrl = "retail/item/searchItemDetail";
	var merchItemUrl="retail/item/searchMerchItem";
	var baseItemUrl="retail/item/searchItem";
	var merchItemDetailUrl="retail/item/searchMerchItemJoinMerchItemUnit";
	var updateMerchItemsUrl="retail/item/updateMerchItems";//修改商品信息
	var onsaleItemUrl = "retail/item/onsaleItem";
	var unsaleItemUrl = "retail/item/unsaleItem";
	var deleteItemUrl = "retail/item/deleteItem";
	var submitItemListUrl="retail/item/createMerchTobaccoItemList";//多个商品添加
	var luceneUrl = "retail/item/searchMerchItemByLucene"; // 从lucene上查询商品
	var isSubmitClose = false;
	var isQueryByLucene = false; // 是否模糊搜索, 默认不是
	var staticCost = undefined;
	var recoverItemUrl = "retail/item/recoverItem";//恢复商品
	var isDeleteUnit = false; // 是否点击了删除包装按钮
	var selectTr=null;
	var isRecoverItem = true;//标识符
	var isSaveItem = false;//判断是否是从新增商品页面执行的查询
	var isRepeatItemSubmit = false;
	var isModifyItemSubmit = false;
	var repeatItemDialog = null;
	
//	var repeatItemNum=0;//条形码扫描shuliang
	
	function loadSPGL(view) {
		pageIndex = 1;
		pageSize = 20;
		parentView = view;
		staticItemBar = "";
		staticItemKindId = "";
		parentView.showFooter(true);
		parentView.closeMessage();
		showMerchItem();
	}
	
	var newItemDialog = undefined;
	var newItemMap = {};
	var updateItemMap=false;
	var emptyItemTr = undefined;
	var tabindex = 100;
	var focusedInput= undefined;
	
	var isrepeat=false;//是否打开新增商品(可重复)页面
	var addTrIndex=0;//新增商品index
	
	function submitItemList(e){
		var itemObjectMap =  {};
		var itemObjectList = []; 
		var messageList = [];
		var newItemIsEmpty = true;
		$.each(newItemMap, function(key, value) {
			newItemIsEmpty = false;
			// 重新赋值, 不依赖onchange
			var tr = $(value);
			var data = $(value).data("item");
			var newItem = {};
			var itemName = tr.find("input[name='newgoodname']").val();
			if(itemName) {
				newItem.item_name = itemName;
			}
			var itemKindId = tr.find("input[name='newgoodkind']").val();
			itemKindId = $.trim(itemKindId);
			if(itemKindId) {
				if(box.kinds.map[itemKindId]) {
					itemKindId = box.kinds.map[itemKindId];
				}
				newItem.item_kind_id = itemKindId;
			}
			var unitName = tr.find("input[name='newgoodunitname']").val();
			if(unitName) {
				newItem.unit_name = unitName;
			}
			var pri1 = tr.find("input[name='newgoodpri1']").val();
			if(pri1) {
				newItem.pri1 = pri1;
			}				
			var whse = tr.find("input[name='newgoodwhse']").val();
			if(whse) {
				newItem.qty_whse= whse;
			}
			var pri4 = tr.find("input[name='newgoodpri4']").val();
			if(pri4) {
				newItem.pri4 = pri4;
			}
			var discount = tr.find("input[name='newgooddiscount']").val();
			if(discount) {
				newItem.discount = discount;
			}
			if(tr.attr("data-id")) {
				newItem.item_id = tr.attr("data-id");
			}
			$.extend(data, newItem);
			itemObjectList.push(data);
		});
		if(newItemIsEmpty) {
			box.showAlert({message: "请输入商品信息后保存！"});
			box.stopEvent(e);
			return;
		}
		if(!$.validateForms(newItemDialog, messageList)) {
			box.showAlert({message: messageList[0]});
			box.stopEvent(e);
			return;
		}
		itemObjectMap["item_list"] = itemObjectList;
		box.request({
			url : box.getContextPath() + submitItemListUrl,
			data : {params:$.obj2str(itemObjectMap)},
			success : function(json) {
				newItemDialog.find("#submit").removeAttr("disabled");
				if(json && json.code=="0000") {
					box.showAlert({message: "商品信息添加成功！"});
					isSubmitClose = true;
					box.closeDialog({content:newItemDialog});
					isSubmitClose = false;
					showMerchItem(undefined, staticItemBar);
					focusedInput = undefined;
				} else {
					box.showAlert({message: json.msg});
				}
			},
			error: function(e) {
				newItemDialog.find("#submit").removeAttr("disabled");
			}
		});
	}
	
	function createNewItemDialog(item) {
		isSaveItem = true;
		newItemDialog = box.ich.view_spge_addGoods();
		newItemDialog = box.showDialog({
			title:"新增商品",
			width:960,
			height:500,
			content:newItemDialog,
			beforeClose: function(param) {
				if(isSubmitClose) return true;
				var newItemIsEmpty = true;
				$.each(newItemMap, function(key, value) {
					newItemIsEmpty = false;
				});
				if(!newItemIsEmpty){
					box.showConfirm({
						message: "商品列表中有未保存商品，您是否需要保存？",
						hasSubmit:true,
						submit: function() {
							newItemDialog.find("#submit").click();
						},
						donotSubmit: function() {
							param.callback();
						}
					});
				}else{
					return true;
				}
				return false;
			},
			close: function(e) {
				newItemMap = {};
				newItemDialog = undefined;
				emptyItemTr = undefined;
				parentView.find("#newGoods").removeAttr("disabled");
			},
			onkey: function(data) {
				if(data.key==HOME.Keys.DELETE) {
					newItemDialog.find("input:focus").closest("tr").find("[name='removeTr']").click();
				} else if(data.key==HOME.Keys.BARCODE) {
					showMerchItem(undefined, data.code);
				}
			}
		});		
		newItemDialog.unbind("select").select(onNewItemDialogTrSelect);
		newItemDialog.find("#submit").unbind("click").click(submitItemList);
		newItemDialog.find("#quxiao").unbind("click").click(cancelItemList);
		appendEmptyItemTr();
		if(item) {
			addNewItem(item);
		}
	}
	
	function cancelItemList(e) {
		isCancelClose = true;
		box.closeDialog({content: newItemDialog});
		isCancelClose = false;
	}
	
	
	function addNewItem(item) {
		item.index1 = ++tabindex;
		item.index2 = ++tabindex;
		item.index3 = ++tabindex;
		item.index4 = ++tabindex;
		item.index5 = ++tabindex;
		item.index6 = ++tabindex;
		item.index7 = ++tabindex;
		item.indexs=addTrIndex++;
		if(newItemMap[item.item_bar]&&isrepeat==false) {
			box.showAlert({message:"列表中已有此条码："+item.item_bar+" 的商品信息！"});
			tempTr = box.ich.view_spge_addEmptyTr({item:item});
			emptyItemTr.empty().append(tempTr.html());
			emptyItemTr.find("input:not(:disabled)").first().select();
			listenNewItemDialogEvent();
			return;
		}
		if(box.kinds[item.item_kind_id]) {
			item.item_kind_name = box.kinds[item.item_kind_id];
		} else if(item.item_kind_id) {
			item.item_kind_name = item.item_kind_id;
		}
		var itemTr = box.ich.view_spge_addGoodsTr({item:item});
		emptyItemTr.empty().append(itemTr.html());
		emptyItemTr.data("item", item);
		emptyItemTr.find("input:not(:disabled)").first().select();
		emptyItemTr.attr("data-id", item.item_id);
		emptyItemTr.attr("data-bar", item.item_bar);
		emptyItemTr.attr("data-index", item.indexs);
		if(isrepeat) {
			newItemMap[item.item_bar+item.indexs] = emptyItemTr;
		} else {
			newItemMap[item.item_bar] = emptyItemTr;
		}
		focusedInput = emptyItemTr.find("input[name='newgoodname']");
		appendEmptyItemTr();
	}
	
	function appendEmptyItemTr() {
		var item = {};
		item.index1 = ++tabindex;
		item.index2 = ++tabindex;
		item.index3 = ++tabindex;
		item.index4 = ++tabindex;
		item.index5 = ++tabindex;
		item.index6 = ++tabindex;
		item.index7 = ++tabindex;
		emptyItemTr = box.ich.view_spge_addEmptyTr({item:item});
		newItemDialog.find(".table_view tbody").append(emptyItemTr);
		newItemDialog.find(".table_view").closest("div").scrollTop(99999);
		listenNewItemDialogEvent();
		if(focusedInput) {
			focusedInput.focus();
		} else {
			emptyItemTr.find("input").first().focus();
		}
	}
	
	function listenNewItemDialogEvent() {
		newItemDialog.find(".table_view tbody").unbind("click").click(function(e) {
			var target = $(e.target);
			if(target.attr("name")=="removeTr") {
				var itemBar = target.closest("tr").attr("data-bar");
				var itemIndex=target.closest("tr").attr("data-index");
				if(isrepeat==true){
					itemBar=itemBar+itemIndex;
				}
				if(newItemMap[itemBar]) {
					focusInput = newItemMap[itemBar].next().find("input[name='newgoodbar']");
					newItemMap[itemBar].remove();
					delete(newItemMap[itemBar]);
					if(focusInput) {
						focusInput.focus();
					}
				}
			}
		});
		newItemDialog.find("input").unbind("change").change(function(e) {
			var me = $(e.currentTarget);
			var tr = me.closest("tr");
			var itemBar = tr.find("input[name='newgoodbar']").val();
			if(itemBar) {
				var newItem = {};
				newItem.item_bar = itemBar;
				if(isrepeat==true){
					itemBar=itemBar+($(tr).attr("data-index"));
				}
				var itemName = tr.find("input[name='newgoodname']").val();
				if(itemName) {
					newItem.item_name = itemName;
				}
				var itemKindId = tr.find("input[name='newgoodkind']").val();
				itemKindId = $.trim(itemKindId);
				if(itemKindId) {
					if(box.kinds.map[itemKindId]) {
						itemKindId = box.kinds.map[itemKindId];
					}
					newItem.item_kind_id = itemKindId;
				}
				var unitName = tr.find("input[name='newgoodunitname']").val();
				if(unitName) {
					newItem.unit_name = unitName;
				}
				var pri1 = tr.find("input[name='newgoodpri1']").val();
				if(pri1) {
					newItem.pri1 = pri1;
				}				
				var whse = tr.find("input[name='newgoodwhse']").val();
				if(whse) {
					newItem.qty_whse= whse;
				}
				var pri4 = tr.find("input[name='newgoodpri4']").val();
				if(pri4) {
					newItem.pri4 = pri4;
				}
				var discount = tr.find("input[name='newgooddiscount']").val();
				if(discount) {
					newItem.discount = discount;
				}
				if(tr.attr("data-id")) {
					newItem.item_id = tr.attr("data-id");
				}
				if(newItemMap[itemBar]) {
					$.extend(newItemMap[itemBar].data("item"), newItem);
				} else {
					tr.data("item", newItem);
					newItemMap[itemBar] = tr;
				}
			}
		});
		newItemDialog.find("input[name='newgoodbar']").unbind("change").change(function(e) {
			var me = $(e.currentTarget);
			var tr = me.closest("tr");
			focusedInput = tr.find("input[name='newgoodname']");
			var bar = me.val();
			if(bar) {
				showMerchItem(undefined, bar);
			}
		});
		newItemDialog.find("input[name='newgoodunitname']").autocomplete({
			source: box.unit.array
		});
		newItemDialog.find("input[name='newgoodkind']").autocomplete({
			source: box.kinds.array
		});
	}
	
	function onNewItemDialogTrSelect(e) {
		var target = $(e.target);
		if(target.attr("name")=="new_item_tr"){
			target.find("input:not(:disabled)").first().select();
		}
	}
	
	function showMerchItem(itemId, itemBar) {
		isQueryByLucene = false;
		var params = {page_index: pageIndex, page_size: pageSize};
		var url = merchItemUrl;// item_id或者没有参数从merch_item中查
		if(itemId) {
			params.item_id = itemId;
		}
		if(itemBar) {
			params.big_bar = itemBar; // 用big_bar来查询3表
			params.item_bar = itemBar;
			params.status = "0,1,2";
			url = baseItemUrl;
			isSaveItem = true;
		}
		if(staticItemKindId) {
			params.item_kind_id = staticItemKindId;
		}
		box.request({
			url : box.getContextPath() + url,
			data : params,
			success : function(json) {
				if(json && json.code=="0000") {
					for(var j=0;j<json.result.length;j++){
						json.result[j].index=j+1+((pageIndex-1)*pageSize);
					}
					var itemList = json.result;
					var pageParam = json.pageparams;
					if(itemList.length>0&&isrepeat==false) {
						box.console.log(itemList);
						if(itemList[0].status=="0" && isSaveItem){
							var confirmInfo = "商品条码："+itemList[0].item_bar+"，名称："+itemList[0].item_name+" 此商品已删除，是否恢复？";
							var backInfo = "恢复成功！";
							showConfirmDialog(itemList[0], confirmInfo, backInfo, recoverItemUrl, "recover");
						}else{
							if(itemList[0].flag=="MERCH") { // 查出来的是商户表数据		
								if(!newItemDialog){
									parentView.showPager(pageParam, function(param) {
										pageIndex = param.page_index;						
										pageSize = param.page_size;
										showMerchItem(itemId, itemBar);
									});
								}
							if(newItemDialog) {
								parentView.find("#item_bar").val("");
								staticItemBar = "";
								staticItemKindId = "";
	//							box.showAlert({message:"您的系统中已有此的商品信息！<br/>条码："+itemList[0].item_bar+" 品名："+itemList[0].item_name});
								box.showAlert({message:"商品条码："+itemList[0].item_bar+"，名称："+itemList[0].item_name+" <br/>已有此的商品信息！"});
								tempTr = box.ich.view_spge_addEmptyTr({item:item});
								emptyItemTr.empty().append(tempTr.html());
								emptyItemTr.find("input:not(:disabled)").first().select();
								emptyItemTr.attr("data-id", item.item_id);
								emptyItemTr.attr("data-bar", item.item_bar);
								listenNewItemDialogEvent();
							} else {
								var tempItemList = [];
								$.each(itemList, function(index, item) {
									if(box.kinds[item.item_kind_id]) {
										item.item_kind_name = box.kinds[item.item_kind_id];
									} else {
										item.item_kind_name = item.item_kind_id;
									}
									if(item.status=="1") {
										item.status_text = "下架";
										item.status_title = "../public/retail/img/soldout.png";
									} else if(item.status=="2") {
										item.status_text = "上架";
										item.status_title = "../public/retail/img/putaway.png";
									}
									if(item.status != "0"){
										item.delete_status_text = "删除";
										item.delete_status_title = "../public/retail/img/delete.png";
									}
									if(item.pri1) {
										item.pri1 = $.parseMoney(item.pri1*item.unit_ratio);
									}
									if(item.pri4) {
										item.pri4 = $.parseMoney(item.pri4);
									}
									if(item.big_pri4) {
										item.big_pri4 = $.parseMoney(item.big_pri4);
									}
									if(!staticItemKindId || (staticItemKindId && item.item_kind_id==staticItemKindId)) {
										tempItemList.push(item);
									}
								});
								parentView.empty().append(box.ich.view_spgl({merch_item_list:tempItemList, item_bar:staticItemBar, item_kind_list:box.kinds.array}));
								parentView.find("#all_item_list").tablesort([
	                      	        {col:0,order:"asc",method:"advance",type:"number"},
	                      	        {col:1,order:"asc",method:"advance",type:"string"},
	                      	        {col:2,order:"asc",method:"advance",type:"string", attr:"data-short_name"},
	                      	        {col:3,order:"asc",method:"advance",type:"string"},
	                      	        {col:4,order:"asc",method:"advance",type:"string"},
	                      	        {col:5,order:"desc",method:"advance",type:"number"},
	                      	        {col:6,order:"desc",method:"advance",type:"number"}
	                         		]);
								parentView.find(".table_view").fixedtableheader({
									parent: parentView,
									win: parentView.parent(),
									isshow: true
								});
								if(tempItemList.length==0) {
									if(itemBar) {
										box.showAlert({message:"商品条码："+itemBar+"，商品类型："+box.kinds[staticItemKindId]+" <br/>没有查询到结果！"});
									} else if(staticItemKindId) {
										box.showAlert({message:"商品类型："+box.kinds[staticItemKindId]+"<br/> 没有查询到结果！"});
									}
								}
								if(itemBar!=undefined) { // 输入空字符串也能进
									parentView.find("#item_bar").val(itemBar);
								}
								if(staticItemKindId) {
									parentView.find("#item_kind").val(staticItemKindId);
								}
								parentView.find(".table_view tbody").unbind("click").click(onTrClick);
								parentView.find("#new").unbind("click").click(onNewButtonClick);
								parentView.find("#newGoods").unbind("click").click(function() {createNewItemDialog();});
								parentView.find("#repeatGoods").unbind("click").click(function() {createRepeatItemDialog();});
								parentView.find("#query").unbind("click").click(onQueryButtonClick);
							}
						} else { // base或者tobacco商品则弹窗
							if(itemList[0].flag=="TOBACCO") {
								itemList[0].item_kind_name = "卷烟";
								//判断商品是否为一个包装的卷烟
								if (!itemList[0].item_bar || itemList[0].item_bar == itemList[0].item_id ) {
									itemList[0].unit_name = itemList[0].big_unit_name;
									itemList[0].pri1 = itemList[0].pri_wsale;
									itemList[0].pri4 = itemList[0].pri_drtl;
								}else{
									itemList[0].unit_name = itemList[0].item_unit_name;
									itemList[0].pri1 = itemList[0].pri_wsale/itemList[0].unit_ratio;
									itemList[0].pri4 = itemList[0].pri_drtl/itemList[0].unit_ratio;
								}
								
							}
							parentView.find("#item_bar").val("");
							staticItemBar = "";
							staticItemKindId = "";
							if(newItemDialog) { // 如果有窗则新增
								addNewItem(itemList[0]);
							} else { // 没窗需要弹窗再新增
								createNewItemDialog(itemList[0]);
								box.showAlert({message:"在系统中查找不到此商品，请完善此商品信息！"});
							}
						}
					}
				} else { // 没有商品, 需要替换空行然后新增空行
						if(url==baseItemUrl) { // 输入条件但是没有查出来
//							box.showAlert({message:"商品条码："+itemBar+" 商品类型："+box.kinds[staticItemKindId]+" 没有查询到结果！"});
							parentView.find("#item_bar").val("");
							staticItemBar = "";
							staticItemKindId = "";
							if(newItemDialog) { // 如果有窗则新增
								addNewItem({item_bar:itemBar});
							} else { // 没窗需要弹窗再新增
								createNewItemDialog({item_bar:itemBar});
								box.showAlert({message:"在系统中查找不到此商品，请完善此商品信息！"});
							}
						} else if(url==merchItemUrl) { // 没有输入条件也没有查出数据					
							parentView.showPager(pageParam, function(param) {
								pageIndex = param.page_index;						
								pageSize = param.page_size;
								showMerchItem(itemId, itemBar);
							});
							if(itemBar) {
								box.showAlert({message:"商品条码："+itemBar+"，商品类型："+box.kinds[staticItemKindId]+"<br/> 没有查询到结果！"});
							} else if(staticItemKindId) {
								box.showAlert({message:"商品类型："+box.kinds[staticItemKindId]+"<br/> 没有查询到结果！"});
							} else {
								box.showAlert({message:"在系统中查找不到任何商品！"});
							}
							parentView.empty().append(box.ich.view_spgl({merch_item_list:itemList, item_bar:staticItemBar, item_kind_list:box.kinds.array}));
							parentView.find("#all_item_list").tablesort([
                  		        {col:0,order:"asc",method:"advance",type:"number"},
                  		        {col:1,order:"asc",method:"advance",type:"string"},
                  		        {col:2,order:"asc",method:"advance",type:"string", attr:"data-short_name"},
                  		        {col:3,order:"asc",method:"advance",type:"string"},
                  		        {col:4,order:"asc",method:"advance",type:"string"},
                  		        {col:5,order:"desc",method:"advance",type:"number"},
                  		        {col:6,order:"desc",method:"advance",type:"number"}
                      		]);
							parentView.find(".table_view").fixedtableheader({
								parent: parentView,
								win: parentView.parent(),
								isshow: true
							});
							if(itemBar!=undefined) {
								parentView.find("#item_bar").val(itemBar);
							}
							if(staticItemKindId) {
								parentView.find("#item_kind").val(staticItemKindId);
							}
//							parentView.find("a[name='remove']").unbind("click").click(onRemoveButtonClick);
							parentView.find(".table_view tbody").unbind("click").click(onTrClick);
							parentView.find("#new").unbind("click").click(onNewButtonClick);
							parentView.find("#newGoods").unbind("click").click(function() {createNewItemDialog();});
							parentView.find("#repeatGoods").unbind("click").click(function() {createRepeatItemDialog();});
							parentView.find("#query").unbind("click").click(onQueryButtonClick);
							parentView.showAdvancedSearch();
						}
					}
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
		parentView.showAdvancedSearch();
	}
	
	function showConfirmDialog(merchItemObject, confirmInfo, backInfo, url, isRecore) {
		var confirmDialog = box.ich.view_spgl_confirm_dialog({context:confirmInfo});
		confirmDialog = box.showDialog({
			title: "提示",
			width: "350",
			height: "180",
			model: true,
			content: confirmDialog,
			buttons: [{
	            id: "yes",
	            text: "确 定",
	            "class": "primary_button",
	            click: function(){
	            	box.closeDialog({content:confirmDialog}); // 关闭弹窗
	            	box.request({
						url : box.getContextPath() + url,
						data : {params: $.obj2str(merchItemObject)},
						success : function(json) {
							if(json && json.code=="0000") {
								box.showAlert({message: backInfo});
								staticItemBar = "";
								staticItemKindId = "";
								if(isRecore=="recover" && !isSaveItem){
									showMerchItem(merchItemObject.item_id, merchItemObject.item_bar);
								}else if(isRecore=="recover" && isSaveItem){
									onQueryButtonClick();
								}else{
									showMerchItem(undefined, staticItemBar);
								}
								isRecoverItem = true;
							} else {
								box.showAlert({message: json.msg});
							}
						}
	        		});
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
	

	function showEditorDialog(merchItemObject) {
		staticCost = merchItemObject.cost;
		if(merchItemObject.cost) {
			merchItemObject.cost = $.parseMoney(merchItemObject.cost);
		}
		if(merchItemObject.pri1) {
			merchItemObject.pri1 = $.parseMoney(merchItemObject.pri1);
		}
		if(merchItemObject.pri2) {
			merchItemObject.pri2 = $.parseMoney(merchItemObject.pri2);
		}
		if(merchItemObject.pri4) {
			merchItemObject.pri4 = $.parseMoney(merchItemObject.pri4);
		}
		if(box.kinds[merchItemObject.item_kind_id]) {
			merchItemObject.item_kind_name = box.kinds[merchItemObject.item_kind_id];
		} else if(merchItemObject.item_kind_id) {
			merchItemObject.item_kind_name = merchItemObject.item_kind_id;
		}
		merchItemObject.pri1 = $.parseMoney(merchItemObject.pri1);
		if(merchItemObject.list) {
			$.each(merchItemObject.list, function(index, item_unit) {
				item_unit.pri4 = $.parseMoney(item_unit.pri4);
			});
		}
		var editorDialog = box.ich.view_spgl_editor({merchItem:merchItemObject});
		
		editorDialog = box.showDialog({
			title: "修改商品",
			width: "720",
			height: "550",
			model: true,
			content: editorDialog,
			beforeClose: function(param) {
				var cost = editorDialog.find("#cost").val();
				if(!isModifyItemSubmit && (updateItemMap || isDeleteUnit || parseFloat(cost)!=parseFloat(staticCost))){
					box.showConfirm({
						message: "商品信息已修改，您是否需要保存？",
						hasSubmit:true,
						submit: function() {
							updateItemMap=false;
							isDeleteUnit = false;
							staticCost = cost;
							editorDialog.find("#submit").click();
						},
						donotSubmit: function() {
							updateItemMap=false;
							isDeleteUnit = false;
							staticCost = cost;
							param.callback();
						}
					});
				}else{
					return true;
				}
				return false;
			}
		});
		
		editorDialog.unbind("change").change(updateItemData);
		
		editorDialog.find("#unit_name").val(merchItemObject.unit_name);
	
		editorDialog.find("#item_kind_id").keydown(function(){
			$(this).autocomplete({
				source: box.kinds.array
			});
		});		

//		editorDialog.find("#item_kind_id").autocomplete({
//			source: box.kinds.array
//		});
		
		editorDialog.find("#unit_name").keydown(function(){
			$(this).autocomplete({
				source: box.unit.array
			});
		});
//		editorDialog.find("#unit_name").autocomplete({
//			source: box.unit.array
//		});

		editorDialog.find("input[name='big_unit_name']").keydown(function(){
			$(this).autocomplete({
				source: box.unit.array
			});
		});
		
//		editorDialog.find("input[name='big_unit_name']").autocomplete({
//			source: box.unit.array
//		});
		editorDialog.find("#modify_cost").unbind("click").click(function(e) {
			var modifyCostDialog = box.ich.view_spgl_modify_cost_dialog({cost:merchItemObject.cost});
			modifyCostDialog = box.showDialog({
				title: "修改成本价",
				width: "340",
				height: "260",
				model: true,
				content: modifyCostDialog
			});
			var confirmModifyCheckBox = modifyCostDialog.find("#confirm_modify");
			var modifyDiv = modifyCostDialog.find("#modify_div");
			var confirmModifyButton = modifyCostDialog.find("#confirm_modify_button");
			var cancelModifyButton = modifyCostDialog.find("#cancel_modify_button");
			var modifiedCostInput = modifyCostDialog.find("#modified_cost");
			
			var showModifyDiv = false;
			confirmModifyCheckBox.unbind("click").click(function(e) {
				showModifyDiv = !showModifyDiv;
				if(showModifyDiv) {
					modifyDiv.removeAttr("style");
				} else {
					modifyDiv.attr("style", "display:none;");
				}
			});
			confirmModifyButton.unbind("click").click(function(e) {
				var modifiedCost = modifiedCostInput.val();
				if(modifiedCost && !isNaN(modifiedCost)) {
					editorDialog.find("#cost").val($.parseMoney(modifiedCost));
				}else{
					editorDialog.find("#cost").val($.parseMoney(0));
				}
				box.closeDialog({content:modifyCostDialog});
				modifyCostDialog = undefined;
			});
			cancelModifyButton.unbind("click").click(function(e) {
				box.closeDialog({content:modifyCostDialog});
				modifyCostDialog = undefined;
			});
		});
		if(merchItemObject.list!=undefined) {
			for(var i in merchItemObject.list) {
				var seqId = merchItemObject.list[i].seq_id;
				editorDialog.find("#"+seqId).find("#big_unit_name").val(merchItemObject.list[i].big_unit_name);
			}
		}
		currentDialog = editorDialog;
		editorDialog.find("#submit").unbind("click").click(onSubmitButtonClick(merchItemObject));
		editorDialog.find("#cancel").unbind("click").click(onCancelButtonClick);
		editorDialog.find("#add_unit").unbind("click").click(onAddUnitButtonClick);
		editorDialog.find("a[name='delete_unit']").unbind("click").click(onDeleteUnitButtonClick);
	}
	function updateItemData(e){
		if($.isInput(e.target)) {
			updateItemMap=true;
		}
	}
	// 修改为点击行弹出修改页面
	function onTrClick(event) {
		var target = $(event.target);
		selectTr=target.closest("tr");
		if(target.attr("name")=="unsale") {
			var itemId = $(selectTr).closest("tr").attr("data-id");
			var itemName = $(selectTr).closest("tr").attr("data-name");
			var status = $(selectTr).closest("tr").attr("data-status");
			modifyMerchItemStatus({item_id:itemId, item_name:itemName, status:status});
		} else if(target.attr("name")=="delete"){
			var itemId = $(selectTr).closest("tr").attr("data-id");
			var itemName = $(selectTr).closest("tr").attr("data-name");
			var status = $(selectTr).closest("tr").attr("data-status");
			deleteMerchItemStatus({item_id:itemId, item_name:itemName, status:status});
		} else {
			var itemId = $(selectTr).attr("data-id");
			if(itemId) {
		    	box.request({
					url : box.getContextPath() + merchItemDetailUrl,
					data : {item_id:itemId},
					success : function(json) {
						if(json && json.code=="0000") {
							var merchItemObject = json.result;
							merchItemObject.type = "U";
							showEditorDialog(merchItemObject);
						} else {
							box.showAlert({message: json.msg});
						}
					}
				});
			}
		}
	}
	
	function modifyMerchItemStatus(merchItemObject) {
		var confirmInfo = "";
		var backInfo = "";
		var url = "";
		if(merchItemObject.status=="2") {
			confirmInfo = "确定要上架此商品: "+merchItemObject.item_name+" ?";
			backInfo = merchItemObject.item_name+" 上架成功!";
			url = onsaleItemUrl;
			merchItemObject.status="1";
		} else if(merchItemObject.status=="1") {
			confirmInfo = "确定要下架此商品: "+merchItemObject.item_name+" ?";
			backInfo = merchItemObject.item_name+" 下架成功!";
			url = unsaleItemUrl;
			merchItemObject.status="2";
		}
		showConfirmDialog(merchItemObject, confirmInfo, backInfo, url, null);
	}
	
	function deleteMerchItemStatus(merchItemObject) {
		var confirmInfo = "";
		var backInfo = "";
		var url = "";
		confirmInfo = "确定要删除此商品: "+merchItemObject.item_name+" ?";
		backInfo = merchItemObject.item_name+" 删除成功!";
		url = deleteItemUrl;
		merchItemObject.status="0";
		showConfirmDialog(merchItemObject, confirmInfo, backInfo, url, null);
	}
	
	function onNewButtonClick(event) {
		var merchItemObject = new Object();
		merchItemObject.type="C";
		showEditorDialog(merchItemObject);
	}

	function onQueryButtonClick(event) {
		pageIndex = 1;
		pageSize = 20;
		staticItemBar = parentView.find("#item_bar").val();
		staticItemKindId = parentView.find("#item_kind").val();
		floor_big_pri4 = parentView.find("#floor_big_pri4").val();
		ceiling_big_pri4 = parentView.find("#ceiling_big_pri4").val();
		// 点击查询走lucene
		lucenQuery(staticItemBar, staticItemKindId, floor_big_pri4, ceiling_big_pri4);
	}
	
	function lucenQuery(key, itemKindId, floor_big_pri4, ceiling_big_pri4) {
		isQueryByLucene = true;
		var params = {page_index: pageIndex, page_size: pageSize, status: "1,2"};
		var url = merchItemUrl;// item_id或者没有参数从merch_item中查
		if(key) {
			url = luceneUrl;
			params.key = key;
		}
		if(itemKindId) {
			params.item_kind_id = itemKindId;
		}
		if(floor_big_pri4) {
			params.floor_big_pri4 = floor_big_pri4;
		}
		if(ceiling_big_pri4) {
			params.ceiling_big_pri4 = ceiling_big_pri4;
		}
		box.request({
			url : box.getContextPath() + url,
			data : {params:$.obj2str(params)},
			success : function(json) {
				if(json && json.code=="0000") {
					for(var j=0;j<json.result.length;j++){
						json.result[j].index=j+1+((pageIndex-1)*pageSize);
					}
					var itemList = json.result;
					var pageParam = json.pageparams;
					parentView.showPager(pageParam, function(param) {
						pageIndex = param.page_index;						
						pageSize = param.page_size;
						
						lucenQuery(key, itemKindId, floor_big_pri4, ceiling_big_pri4);
						
					});
					var tempItemList = [];
					$.each(itemList, function(index, item) {
						if(box.kinds[item.item_kind_id]) {
							item.item_kind_name = box.kinds[item.item_kind_id];
						} else {
							item.item_kind_name = item.item_kind_id;
						}
						if(item.status=="1") {
							item.status_text = "下架";
							item.status_title = "../public/retail/img/soldout.png";
						} else if(item.status=="2") {
							item.status_text = "上架";
							item.status_title = "../public/retail/img/putaway.png";
						}
						if(item.status != "0"){
							item.delete_status_text = "删除";
							item.delete_status_title = "../public/retail/img/delete.png";
						}
						if(item.pri1) item.pri1 = $.parseMoney(item.pri1*item.unit_ratio);
						if(item.pri4) item.pri4 = $.parseMoney(item.pri4);
						if(item.big_pri4) item.big_pri4 = $.parseMoney(item.big_pri4);
						if(!staticItemKindId || (staticItemKindId && item.item_kind_id==staticItemKindId)) {
							tempItemList.push(item);
						} 
						else if (staticItemKindId && staticItemKindId == '99') {
							//查询“其他”类型的商品，包含用户手动输入的类型
							tempItemList.push(item);
						}
					});
					parentView.empty().append(box.ich.view_spgl({merch_item_list:tempItemList, item_bar:key, item_kind_list:box.kinds.array}));
					parentView.find("#all_item_list").tablesort([
          		        {col:0,order:"asc",method:"advance",type:"number"},
          		        {col:1,order:"asc",method:"advance",type:"string"},
          		        {col:2,order:"asc",method:"advance",type:"string",attr:"data-short_name"},
          		        {col:3,order:"asc",method:"advance",type:"string"},
          		        {col:4,order:"asc",method:"advance",type:"string"},
          		        {col:5,order:"desc",method:"advance",type:"number"},
          		        {col:6,order:"desc",method:"advance",type:"number"}
					]);
					parentView.find(".table_view").fixedtableheader({
						parent: parentView,
						win: parentView.parent(),
						isshow: true
					});
					if(itemList.length==0) {
						if(key) {
							box.showAlert({message:"没有查询到"+(itemKindId==""?"全部商品":box.kinds[itemKindId])+"类型中的"+key+"商品！"});
						} else {
							box.showAlert({message:"没有查询到"+(itemKindId==""?"全部商品":box.kinds[itemKindId])+"类型中的商品！"});
						}
					}
					if(key!=undefined) { // 输入空字符串也能进
						parentView.find("#item_bar").val(key);
					}
					if(staticItemKindId) {
						parentView.find("#item_kind").val(staticItemKindId);
					}
//					parentView.find("a[name='remove']").unbind("click").click(onRemoveButtonClick);
					parentView.find(".table_view tbody").unbind("click").click(onTrClick);
					parentView.find("#new").unbind("click").click(onNewButtonClick);
					parentView.find("#newGoods").unbind("click").click(function() {createNewItemDialog();});
					parentView.find("#repeatGoods").unbind("click").click(function() {createRepeatItemDialog();});
					parentView.find("#query").unbind("click").click(onQueryButtonClick);
					parentView.showAdvancedSearch();
				} else {
					box.showAlert({message: json.msg});
				}
				parentView.find("#floor_big_pri4").val(floor_big_pri4);
				parentView.find("#ceiling_big_pri4").val(ceiling_big_pri4);
			}
		});
	}
	
	function onSubmitButtonClick(merchItemObject) {	
		return function(event) {
			var messageList = [];
			if(!$.validateForms(currentDialog, messageList)) {
				box.showAlert({message: messageList[0]});
				return;
			}
			var newBigPri4 = merchItemObject.big_pri4;
			var itemContainer = currentDialog.find("#item_container");
//			merchItemObject = new Object();
			merchItemObject.seq_id = itemContainer.find("#seq_id").val();
			merchItemObject.item_name = itemContainer.find("#item_name").val();
			merchItemObject.item_bar = itemContainer.find("#item_bar").val();
			merchItemObject.short_name = itemContainer.find("#short_name").val();
			merchItemObject.short_code = itemContainer.find("#short_code").val();
			var kindId = itemContainer.find("#item_kind_id").val();
			merchItemObject.item_kind_name = kindId;
			if(kindId && box.kinds.map[kindId]) kindId = box.kinds.map[kindId];
			merchItemObject.item_kind_id = kindId;
			
			merchItemObject.unit_name = itemContainer.find("#unit_name").val();
			merchItemObject.spec = itemContainer.find("#spec").val();
			if(itemContainer.find("#cost").val()) {
				merchItemObject.cost = itemContainer.find("#cost").val();
			} else {
				delete(merchItemObject.cost);
			}
			if(itemContainer.find("#pri1").val()) {
				merchItemObject.pri1 = itemContainer.find("#pri1").val();
			} else {
				delete(merchItemObject.pri1);
			}
			if(itemContainer.find("#pri2").val()) {
				merchItemObject.pri2 = itemContainer.find("#pri2").val();
			} else {
				delete(merchItemObject.pri2);
			}
			if(itemContainer.find("#pri4").val()) {
				merchItemObject.pri4 = itemContainer.find("#pri4").val();
			} else {
				delete(merchItemObject.pri4);
			}
			
			var unitContainer = currentDialog.find("#unit_container");
			var unitList = unitContainer.find("div");
			if(merchItemObject.list==undefined) {
				merchItemObject.list = [];
			}
			var unitRatioContainer = unitContainer.find(".unit_ratio");
			var unitRatio = parseFloat(unitRatioContainer.val());
			if(unitRatio == 1 || unitRatio == 1.0 || unitRatio == 0){
				unitRatioContainer.focus();
				unitRatioContainer.addClass("errorCls");
				box.showAlert({message : "转化系数不可为0或1!"});
				return;
			}
			newBigPri4 =  merchItemObject.big_bar == merchItemObject.item_bar ? merchItemObject.pri4 : newBigPri4;
			
			for(var i=0; i<merchItemObject.list.length; i++) {
				var alreadyfound = false;
				for(var j=0; j<unitList.length; j++) {
					var unit = $(unitList[j]);
					var seqId = unit.attr("id");
					var oldBar = $(unit).attr("data-big-bar");//原包装条码，防止修改包装条码，
					var bigBar = unit.find("#big_bar").val();
					var bigUnitName = unit.find("#big_unit_name").val();
					var unitRatio = unit.find("#unit_ratio").val();
					var pri4 = unit.find("#pri4").val();
					
					newBigPri4 =  merchItemObject.big_bar == bigBar ? pri4 : newBigPri4;
					
					if(seqId == merchItemObject.list[i].seq_id) {
						merchItemObject.list[i].type = "U";
						merchItemObject.list[i].big_bar = bigBar;
						merchItemObject.list[i].big_unit_name = bigUnitName;
						merchItemObject.list[i].unit_ratio = unitRatio;
						merchItemObject.list[i].pri4 = pri4;
						
						// 为了在后面不再添加
						unitList[j].type = "U";
//						用于修改管理包装。。。暂时不用（当前管理包装是最后一个包装）
//						if (oldBar == merchItemObject.big_bar) {
//							merchItemObject.big_pri4 = pri4;
//							merchItemObject.big_bar = bigBar;
//							merchItemObject.big_unit_name = bigUnitName;
//						}
						alreadyfound = true;
						break;
					}
				}
				if(!alreadyfound) {
					merchItemObject.list[i].type = "D";
				}
			}
			// 判断完重复包装再添加
			for(var k=0; k<unitList.length; k++) {
				if(!unitList[k].type || unitList[k].type!="U") { // 不是U且存在的就是新增, 新增的商品属性不需要seq_id
					var unit = $(unitList[k]);
					var bigBar = unit.find("#big_bar").val();
					var bigUnitName = unit.find("#big_unit_name").val();
					var unitRatio = unit.find("#unit_ratio").val();
					var pri4 = unit.find("#pri4").val();
					merchItemObject.list.push({type:"C", big_bar:bigBar, big_unit_name:bigUnitName, unit_ratio:unitRatio, pri4:pri4});
				}
			}
			//检查商品是否正确
			chekItem(merchItemObject);
		};
	}
	
	//修改商品
	function updateItem(merchItemObject){
		box.request({
    		url : box.getContextPath() + updateMerchItemsUrl,
			data : {params:$.obj2str(merchItemObject)},
			success : function(json) {
				updateItemMap=false;
				if(json && json.code=="0000") {
					box.showAlert({message: "商品信息修改成功"});
					isModifyItemSubmit = true;
					box.closeDialog({content:currentDialog});//关闭修改商品窗口
					showMerchItem();
//					isModifyItemSubmit = false;
////					showMerchItem(undefined, staticItemBar);//查询商品列表
//					if(selectTr){
//						selectTr.find("td").eq(1).text(merchItemObject.big_bar);
//						selectTr.find("td").eq(2).text(merchItemObject.item_name);
//						selectTr.find("td").eq(3).text(merchItemObject.item_kind_name);
//						selectTr.find("td").eq(4).text(merchItemObject.big_unit_name);
//						selectTr.find("td").eq(5).text($.parseMoney(parseFloat(merchItemObject.pri1) * parseFloat(merchItemObject.unit_ratio)));
//						selectTr.find("td").eq(6).text($.parseMoney(newBigPri4));
//						selectTr.find("td").eq(7).text(merchItemObject.discount);
//					}
					parentView.find("#item_bar").val(staticItemBar);
					if(!updateItemMap) {
						currentDialog = undefined;
					}
				} else {
					box.showAlert({message: json.msg});
				}
			}
		});
	}
	
	function onCancelButtonClick(event) {
		box.closeDialog({content:currentDialog});
	}
	
	function onAddUnitButtonClick(event) {
		var unitContainer = currentDialog.find("#unit_container");
//		if(showPack==false){
//			showPack=true;
//			currentDialog.css("height",showPackHight);
//			unitContainer.show();//view_spgl_editor
//		}
		
		unitContainer.append(box.ich.view_spgl_unit());
		unitContainer.find("a[name='delete_unit']").unbind("click").click(onDeleteUnitButtonClick);
		unitContainer.find("input[name='big_unit_name']").autocomplete({
			source: box.unit.array
		});
	}	
	function onDeleteUnitButtonClick(event) {
		isDeleteUnit = true;
		var selfDiv = $(event.currentTarget).closest("div");
		selfDiv.remove();
	}
	
	
	function createRepeatItemDialog() {
		repeatItemDialog = box.ich.view_spge_add_repeat_item();
		repeatItemDialog=box.showDialog({
			title:"新增重码商品",
			width:960,
			height:500,
			content:repeatItemDialog,
			beforeClose: function(param) {
				var repeatItemList = repeatItemDialog.find("#adding").find("tr");
				if(repeatItemList.length>1 && !isRepeatItemSubmit) {
					box.showConfirm({
						message: "商品列表中有未保存商品，您是否需要保存？",
						hasSubmit:true,
						submit: function() {
							repeatItemDialog.find("#save_repeat_item").click();
						},
						donotSubmit: function() {
							param.callback();
						}
					});
				}else{
					return true;
				}
				return false;
			},
			close: function(e) {
				repeatItemDialog = undefined;
				isrepeat=false;
			},
			onkey: function(data) {
				switch(data.key) {
					case HOME.Keys.DELETE:
						break;
					case HOME.Keys.BARCODE:
						searchMerchItem(data.code);
				}
			}
		});
		isrepeat=true;
		repeatItemDialog.find("#save_repeat_item").unbind("click").click(submitRepeatItemList);
		repeatItemDialog.find("#repeat_query").unbind("click").click(searchRepeatQuery);
		repeatItemDialog.find("#cancel_save_repeat_item").click(cancelAllItemList);
	}
	//查询重码商品
	function searchRepeatQuery(){
		var repeartItemBar=repeatItemDialog.find("#repeat_item_bar");
		searchMerchItem(repeartItemBar.val());
	}
	function cancelAllItemList(e) {
		isCancelClose = true;
		box.closeDialog({content: repeatItemDialog});
		isCancelClose = false;
	}
	
	
	
	function submitRepeatItemList(e) {
		var repeatItemList = repeatItemDialog.find("#adding").find("tr");
		itemObjectList = [];
		var hasRepeat = false;
		$.each(repeatItemList, function(i, itemTr){
			var itemName = $(itemTr).find("input[name='repeat_item_name']").val();
			//  data-verify="number" data-empty="false" value="{{pri1}}" verify-message="进货价不能为空且必须为数字！"
			if(itemName) {
				var itemBarInput = repeatItemDialog.find("#repeat_item_bar");
				var itemKindInput = $(itemTr).find("input[name='repeat_item_kind']");
				var itemUnitInput = $(itemTr).find("input[name='repeat_item_unit']");
				var pri1Input = $(itemTr).find("input[name='repeat_item_pri1']");
				pri1Input.attr("data-verify", "number");
				pri1Input.attr("data-empty", "false");
				pri1Input.attr("verify-message", "采购价不能为空且必须为数字！");
				var pri4Input = $(itemTr).find("input[name='repeat_item_pri4']");
				pri4Input.attr("data-verify", "number");
				pri4Input.attr("data-empty", "false");
				pri4Input.attr("verify-message", "零售价不能为空且必须为数字！");
				var discountInput = $(itemTr).find("input[name='repeat_item_discount']");
				discountInput.attr("data-verify", "number");
				discountInput.attr("verify-message", "折扣必须为数字！");
				hasRepeat = true;
				itemObject = {item_name:itemName};
				var itemBar = itemBarInput.val();
				if(itemBar) {
					itemObject.item_bar= itemBar;
				}
				var itemKindName = itemKindInput.val();
				if(itemKindName && box.kinds.map[itemKindName]){
					itemObject.item_kind_id = box.kinds.map[itemKindName];
				}
				var unit = itemUnitInput.val();
				if(unit) {
					itemObject.unit_name = unit;
				}
				var pri1 = pri1Input.val();
				if(pri1) {
					itemObject.pri1 = pri1;
				}
				var pri4 = pri4Input.val();
				if(pri4) {
					itemObject.pri4 = pri4;
				}
				var discount = discountInput.val();
				if(discount) {
					itemObject.discount = discount;
				}else{
					discount="100";
					itemObject.discount = discount;
				}
				itemObjectList.push(itemObject);
			}
		});
		var messageList = [];
		if(!$.validateForms(repeatItemDialog, messageList)) {
			box.showAlert({message: messageList[0]});
			box.stopEvent(e);
			hasRepeat = false;
			return;
		}
		itemObjectMap = {item_list:itemObjectList}; // item_list
		if(hasRepeat) {
			box.request({
				url : box.getContextPath() + submitItemListUrl,
				data : {params:$.obj2str(itemObjectMap)},
				success : function(json) {
//					newItemDialog.find("#submit").removeAttr("disabled");
					if(json && json.code=="0000") {
						box.showAlert({message: "商品信息添加成功！"});
						isSubmitClose = true;
						isRepeatItemSubmit = true;
						box.closeDialog({content:repeatItemDialog});
						isRepeatItemSubmit = false;
						isSubmitClose = false;
						if(isQueryByLucene) {
							lucenQuery(staticItemBar, staticItemKindId, null, null);
						} else {
							showMerchItem(undefined, staticItemBar);
						}
						newItemDialog = undefined;
						focusedInput = undefined;
					} else {
						box.showAlert({message: json.msg});
						repeatItemDialog.find("#submit").removeAttr("disabled");
					}
				},
				error: function(e) {
					repeatItemDialog.find("#submit").removeAttr("disabled");
				}
			});
		} else {
			box.showAlert({message:"请输入商品信息后保存！"});
			box.stopEvent(e);
		}
	}
	
	function searchMerchItem(itemBar) {
//		repeatItemNum=0;
		params = {page_index:-1, page_size:-1, status:"1,2", big_bar:itemBar};
		var textItemBar=repeatItemDialog.find("#repeat_item_bar");
		textItemBar.val(itemBar);
		textItemBar.blur();
    	box.request({
			url : box.getContextPath() + searchUnitJoinMerchItemUrl,
			data : {params:$.obj2str(params)},
			success : function(json) {
				if(json && json.code=="0000") {
					var itemList = json.result;
//					repeatItemNum=itemList.length;
					$.each(itemList, function(i, item) {
						item.item_kind_name = box.kinds[item.item_kind_id];
					});
					repeatItemDialog.find("#add_repeat_item_div").empty()
						.append(box.ich.view_spge_repeat_item_table({list:itemList}));
					repeatItemDialog.find("#adding").empty()
						.append(box.ich.view_spgl_repeat_item_tr());
					repeatItemDialog.unbind("change").change(function(e) {
						var source = $(e.target);
						var sourceTr = source.closest("tr");
						var sourceBody = sourceTr.closest("tbody");
						if(source.attr("name")=="repeat_item_name") {
							source.attr("disabled", true);
							sourceTr.find("a[name='removeTr']").removeAttr("style");
							if(sourceBody.children().length==sourceTr.index()+1) {
								sourceBody.append(box.ich.view_spgl_repeat_item_tr());
							}
						}
						repeatItemDialog.find("input[name='repeat_item_kind']").autocomplete({
							source: box.kinds.array
						});
						repeatItemDialog.find("input[name='repeat_item_unit']").autocomplete({
							source: box.unit.array
						});
					});
					repeatItemDialog.unbind("click").click(function(e) {
						var source = $(e.target);
						var sourceTr = source.closest("tr");
						var sourceBody = sourceTr.closest("tbody");
						
						if(source.attr("name")=="removeTr") {
							sourceTr.remove();
							if(sourceBody.children().length==0) {
								sourceBody.append(box.ich.view_spgl_repeat_item_tr());
							}
						}
					});
					repeatItemDialog.find("input[name='repeat_item_kind']").autocomplete({
						source: box.kinds.array
					});
					repeatItemDialog.find("input[name='repeat_item_unit']").autocomplete({
						source: box.unit.array
					});
				} else {
					box.showAlert({message: json.msg});
				}
				repeatItemDialog.find("#adding").find("input[name='repeat_item_name']").focus();
			}
		});
	}
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.DELETE:
				parentView.find("tr.selectRow [name='remove']").click();
				break;
			case HOME.Keys.BARCODE:
				staticItemKindId = parentView.find("#item_kind").val();
				pageIndex = 1;
				pageSize = 20;
				showMerchItem(undefined, data.code);
				break;
		}
	}
	
	//检查商品正确性
	function chekItem(item) {
		
		var checkItemMsg = "";
		var marking = false;
		if (!parseFloat(item.cost) || parseFloat(item.cost) <=0 ) {
			checkItemMsg = checkItemMsg+"商品成本价为零或小于零！<br/>";
			marking = true;
		}
		
		if (!parseFloat(item.pri1) || parseFloat(item.pri1) <=0 ) {
			checkItemMsg = checkItemMsg+"商品采购价为零或小于零！<br/>";
			marking = true;
		}
		
		if (!parseFloat(item.pri4) || parseFloat(item.pri4) <=0 ) {
			checkItemMsg = checkItemMsg+"商品销售价为零或小于零！<br/>";
			marking = true;
		} 
		
		if (parseFloat(item.pri4) < parseFloat(item.cost) ) {
			checkItemMsg = checkItemMsg+"商品销售价小于商品成本价！<br/>";
			marking = true;
		} 
		
		if (parseFloat(item.pri4) !=0 && parseFloat(item.cost) != 0 && parseFloat(item.pri4) >= parseFloat(item.cost) *5) {
			checkItemMsg = checkItemMsg+"商品销售价远远大于商品成本价！<br/>";
			marking = true;
		}
		
		checkItemMsg = checkItemMsg+"是否确定修改？";
		
		if (marking ) {
			box.showConfirm({
				message: checkItemMsg, 
				title: "警告",
				ok: function () {
					updateItem(item);
				}
			});
			
		} else {
			updateItem(item);
		}
	}
	
	return {
		init: function(){
			box.listen("spgl", loadSPGL);
			box.listen("spgl_onkey", onKey);
		},
		destroy: function() { }
	};
});