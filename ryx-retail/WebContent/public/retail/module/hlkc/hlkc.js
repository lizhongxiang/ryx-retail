/**
 * 库存管理功能
 */
HOME.Core.register("plugin-hlkc", function(box) {
	
	var hlkcUrl = "retail/whse/getWhseMerchList";
	var searchWhseMerchJoinMerchItemUrl = "retail/whse/searchMerchItemJoinAdvWhseByLucene";
	var uphlkcUrl="retail/whse/updateWarnWhseList";

	var pageIndex=1;
	var pageSize=20;
	var staticItemBar = "";
	var parentView = null;
	var upHLCKList={};//修改的合理库存
	var unitRatioMap = {}; // 修改过合理库存的商品转换比
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
		var params = {page_index:pageIndex,page_size:pageSize,item_kind_id:"01",sort_rule:{qty_whse:'desc', item_id:'desc'}};
		var url = hlkcUrl;
		if(staticItemBar) {
			url = searchWhseMerchJoinMerchItemUrl;
			params.key = staticItemBar;
		} else {
			params.itemBar = staticItemBar;
		}
		box.request({
			url: box.getContextPath() + url,
			data:{params:$.obj2str(params)},
			success: onGetHLKCData
		});
	}
	
	function onGetHLKCData(data) {
		if(data && data.code == "0000") {
			if(data.result.length>0 && data.result[0].status == "0"){
				box.showAlert({message:data.result[0].item_name+"等"+(data.result.length)+"个商品已删除！<br/>请在商品管理中恢复后再进行盘点！"});
				parentView.find("#tiaoma").val("");
			}else{
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
					data.result[i].qty_whse=parseFloat(data.result[i].qty_whse).toFixed(1);
					data.result[i].qty_whse_warn=parseFloat(data.result[i].qty_whse_warn).toFixed(1);
					data.result[i].adv_whse=parseFloat(data.result[i].adv_whse).toFixed(1);
					data.result[i].whse_remind = checkItemGetMsg(data.result[i].qty_whse_warn);
					
					if(upHLCKList[newItemId]){
						data.result[i].qty_whse_warn=upHLCKList[newItemId];
					}
				}
				showHLKC(data.result);
			}
		}
	}
	
	//检查商品合理库存，得到提示消息
	function checkItemGetMsg (qty) {
		var whse_remind = "";
		if (qty >=1000) {
			whse_remind = whse_remind + "商品合理库存过高！";
		}
		if (qty <0) {
			whse_remind = whse_remind + "商品合理库存过低！";
		}
		return whse_remind;
	}
	
	function showHLKC(result) {
		
		var content = box.ich.view_kcgl_hlkc({list: result, static_item_bar:staticItemBar});
		parentView.empty().append(content);
		inputs = parentView.find(".qtyord");
		parentView.find("#submit").unbind("click").click(onSubmitHLKC);
		parentView.find("#selewares").unbind("click").click(clickSelewares);
		parentView.find("tr").select(function(){
			$(this).find(".qtyord").focus();
		});
		qtyWhseWarn = parentView.find("table tbody").unbind("change",onQtyChange);
		parentView.find(".table_view").tablesort([
   		        {col:0,order:"asc",method:"advance",type:"number"},
   		        {col:1,order:"asc",method:"advance",type:"string"},
   		        {col:2,order:"asc",method:"advance",type:"string"},
   		        {col:3,order:"desc",method:"advance",type:"number"},
   		        {col:5,order:"desc",method:"advance",type:"number"},
   		        {col:6,order:"desc",method:"advance",type:"number"},
   		        {col:7,order:"desc",method:"advance",type:"number"},
   		        {col:8,order:"desc",method:"advance",type:"number"}
   		]);
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
		for(var i in upHLCKList) {
			list.push({item_id: i, qty_whse_warn:parseFloat(upHLCKList[i])*unitRatioMap[i]});
		}
		obj.list = list;
		if(list.length) {
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
//			url: box.getContextPath() + searchWhseMerchJoinMerchItemUnitUrl,
			url: box.getContextPath() + hlkcUrl,
//			data:{big_bar:staticItemBar, page_index: pageIndex, page_size:pageSize,item_kind_id:"01"},
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
		var $target = $(e.target);
		var itemId = $target.attr("data-id");
		var vals = $target.val();
		var unitRatio = $target.attr("data-ratio");
		
		upHLCKList[itemId] = vals;
		unitRatioMap[itemId] = parseFloat(unitRatio);
		
		var iconSpan = $target.closest("tr").find("span[name='whse_remind_icon']");
		var iconDisplay = iconSpan.css('display');
		var msg = checkItemGetMsg(vals);
		if (iconDisplay == 'none' && msg) {
//			box.showAlert({message:msg});
			$(iconSpan).css('display','block'); 
			$(iconSpan).attr("title", msg);
		} else if (iconDisplay == 'block' ) {
			if (msg) {
				$(iconSpan).attr("title", msg);
			} else {
				$(iconSpan).css('display','none'); 
			}
		}
		
	}
	
	function onClose(param){
		var isChanged = false;
		for (var i in upHLCKList){
			isChanged = true;
			break;
		}
		if(isChanged){
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