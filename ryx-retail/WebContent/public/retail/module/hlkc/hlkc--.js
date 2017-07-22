/**
 * 库存管理功能
 */
HOME.Core.register("plugin-hlkc", function(box) {
	
	var hlkcUrl = "retail/whse/getWhseMerchList";
//	var searchWhseMerchJoinMerchItemUnitUrl = "retail/whse/searchWhseMerchJoinMerchItemUnit";
	var searchWhseMerchJoinMerchItemUrl = "retail/whse/searchMerchItemJoinAdvWhseByLucene";
	var uphlkcUrl="retail/whse/updateWarnWhseList";

	var pageIndex=1;
	var pageSize=20;
	var inputs=null;
	var staticItemBar = "";
	var parentView = null;
	var upHLCKList={};//修改的合理库存
	function loadHLKC(view) {
		parentView = view;
		staticItemBar = "";
		pageIndex=1;
		pageSize=20;
		getHLKCData();		
	}
	function clickSelewares(){
		staticItemBar=parentView.find("#tiaoma").val();
		pageIndex=1;
		pageSize=20;		
		getHLKCData();
	}
	function getHLKCData() {
		if(staticItemBar=="请输入条码/品名/拼音") staticItemBar = "";
		var params = {page_index:pageIndex,page_size:pageSize,item_kind_id:"01"};
		var url = hlkcUrl;
		if(staticItemBar) {
			url = searchWhseMerchJoinMerchItemUrl;
			params.key = staticItemBar;
			params.item_bar = staticItemBar;
		}
		box.request({
			url: box.getContextPath() + url,
			data:params,
			success: onGetHLKCData
		});
	}
	
	function onGetHLKCData(data) {
		if(data && data.code == "0000") {
			var pageParam=data.pageparams;
			parentView.showPager(pageParam,function(param){
				pageIndex=param.page_index;
				pageSize=param.page_size;
				getHLKCData();
			});
			if(data.result.length<=0 && staticItemBar){
				box.showAlert({message:"很抱歉，没有此商品（"+staticItemBar+" ）记录<br/>或此商品不是卷烟商品"});
			}
			var newItemId = null;
			for(var i=0;i<data.result.length;i++){
//				data.result[i].index=i+1;
				data.result[i].index=(pageIndex-1)*pageSize+i+1;
				data.result[i].pri1=$.parseMoney(data.result[i].pri1);
				data.result[i].pri4=$.parseMoney(data.result[i].pri4);
				newItemId = data.result[i].item_id;
				var qtyWhse = data.result[i].qty_whse;
				if(qtyWhse <= 0){
					qtyWhse = 0;
				}
				data.result[i].qty_whse=((parseFloat(qtyWhse))).toFixed(1);
				data.result[i].qty_whse_warn=((parseFloat(data.result[i].qty_whse_warn))).toFixed(1);
				data.result[i].adv_whse=((parseFloat(data.result[i].adv_whse))).toFixed(1);
				
				if(upHLCKList[newItemId]){
					data.result[i].qty_whse_warn=upHLCKList[newItemId];
				}
			}
			showHLKC(data.result);
		}
	}
	
	function showHLKC(result) {
		
		var content = box.ich.view_kcgl_hlkc({list: result, static_item_bar:staticItemBar});
		parentView.empty().append(content);
		inputs = parentView.find(".qtyord");
		parentView.find("#submit").unbind("click", onSubmitHLKC).click(onSubmitHLKC);
		parentView.find("#selewares").unbind("click", clickSelewares).click(clickSelewares);
		parentView.find("tr").select(function(){
			$(this).find(".qtyord").focus();
		});
		qtyWhseWarn = parentView.find(".qtyord").unbind("change",onQtyChange);
		qtyWhseWarn.change(onQtyChange);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
	}
	
	function onSubmitHLKC(){
		var obj={};
		var list = new Array();
//		$.each(inputs, function(i, obj) {
//			$obj = $(obj);
//			var qty = parseInt($obj.val());
//			var itemId = $obj.attr("data-id");
//			var pri = parseInt($obj.attr("data-pri"));
//			if(qty!=pri){
//				list.push({item_id: itemId, qty_whse_warn: qty});
//			}
//		});
		for (var i in upHLCKList){
			list.push({item_id: i, qty_whse_warn:parseFloat(upHLCKList[i])});
		}
		obj.list = list;
		if(list.length > 0) {
			box.request({
				url: box.getContextPath() + uphlkcUrl,
				data: {params: $.obj2str(obj)},
				success: submitisOk
			});
		}else{
			box.showAlert({message:"没有录入合理库存，请先修改合理库存！"});
		}
	}
	function submitisOk(){
		box.showAlert({message:"录入合理库存成功"});
		upHLCKList={};
		getHLKCData();
	}

	function onBarcode(data){
		staticItemBar=data.code;
		parentView.find("#tiaoma").val(staticItemBar);
		box.request({
			url: box.getContextPath() + hlkcUrl,
			data:{item_bar:staticItemBar, page_index: pageIndex, page_size:pageSize,item_kind_id:"01"},
			
			success: onGetHLKCData
		});
	}
	
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.BARCODE:
				onBarcode(data);
				break;
		}
	}
	
	function onQtyChange(e){
		var $target = $(e.currentTarget);
		var itemId = $target.attr("data-id");
		var vals = $target.val();
		upHLCKList[itemId] = vals;
	}
	
	function onClose(param){
		var newItemIsEmpty = true;
		for (var i in upHLCKList){
			newItemIsEmpty = false;
		}
		
//		var list = new Array();
//		if(inputs) {
//			$.each(inputs, function(i, obj) {
//				$obj = $(obj);
//				var qty = parseInt($obj.val());
//				var itemId = $obj.attr("data-id");
//				var pri = parseInt($obj.attr("data-pri"));
//				if(qty!=pri){
//					list.push({item_id: itemId, qty_whse_warn: qty});
//				}
//			});
//		}
//		if(list.length > 0) {
//			newItemIsEmpty=false;
//		}
		if(!newItemIsEmpty){
			box.showConfirm({
				message: "合理库存已修改，您是否保存？",
				hasSubmit: true,
				submit: function() {
					parentView.find("#submit").click();
				},
				donotSubmit: function() {
					upHLCKList={};
					param.callback();
				}
			});
		}else{
			return true;
		}
		return false;
	}
	
	return {
		init: function(){
			box.listen("hlkc", loadHLKC);
			box.listen("hlkc_close", onClose);
			box.listen("hlkc_onkey", onKey);
		},
		destroy: function() { }
	};
});



	function getKindName(KindId){
		if(KindId=="100") return "卷烟";
		else if(KindId=="200") return "副食";
		else if(KindId=="300") return "百货";
		else if(KindId=="400") return "酒水";
		else if(KindId=="999") return "其他";
		else return "";
	}