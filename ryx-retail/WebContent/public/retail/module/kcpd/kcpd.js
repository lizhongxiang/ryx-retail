/**
 * 库存盘点功能
 */
HOME.Core.register("plugin-kcpd", function(box) {

	var kcpdUrl = "retail/whse/getWhseMerchList";//查询商品列表
	var searchWhseMerchJoinMerchItemUnitUrl = "retail/whse/searchWhseMerchJoinMerchItemUnit";//扫码
	var searchWhseMerchJoinMerchItemUrl = "retail/whse/searchMerchItemJoinWhseMerchByLucene";//luce搜索
	var upKcpdURL = "retail/whse/submitWhseTurn";//修改
	var itemKindURL = "retail/whse/getItemKindList";//分类的总计信息
	
	var pageIndex=1;
	var pageSize=20;
	var staticItemBar = "";
	var staticItemKind="01";
	var inputMap={};//修改过的库存的tr
	var inputReasonMap={};//损益原因
	var inputs=null;
	var parentView = null;
	var content=null;
	var itemKindData=null;
	function loadKCPD(view){
		parentView= view;
		pageIndex=1;
		pageSize=20;
		onSelectKind();
	}
	function onSelectKind(){
		box.request({
			url:box.getContextPath()+ itemKindURL,
			data:{item_kind_id:staticItemKind},
			success:function(data1){
				itemKindData=data1.result;
				getKCPDDate();
			}
		});
	}
	
	//高级查询
	function onClickHighSearch(){
		var highsearcher1 = $(".window_search");
		if(highsearcher1.is(":visible")){
			//隐藏
			parentView.hideHighSearch();
		}else{
			//显示---morequery为要传入的查询条件
			var morequery = parentView.find("#high");
			parentView.showHighSearch("400px","95px","-100px",morequery);
		}
		
	}
	function onClickTiaoMa(){
		var tiaoma=parentView.find("#tiaoma").val();
		var item_kind_id=parentView.find("#item_kind").val();
		staticItemBar = tiaoma;
		staticItemKind=item_kind_id;
		pageIndex=1;
		pageSize=20;
		
		itemKindData = null;
		
		box.request({
			url:box.getContextPath()+ itemKindURL,
			data:{item_kind_id:staticItemKind},
			success:function(data1){
				itemKindData=data1.result;
				getKCPDDate();
			}
		});
	}
	function getKCPDDate(){
		var params = {page_index:pageIndex,page_size:pageSize,sort_rule:{qty_whse:'desc', item_id:'desc'}};
		var url = kcpdUrl;
		if(staticItemBar){
			url = searchWhseMerchJoinMerchItemUrl;
			params.key = staticItemBar;
		}
		if(staticItemKind){
			params.item_kind_id=staticItemKind;
		}
		box.request({
			url:box.getContextPath()+ url,
			data:{params:$.obj2str(params)},
			success:onGetKCPDDate,
			error:function(e) {
				parentView.find("#selewares").removeAttr("disabled");
			}
		});
	}
	
	function onGetKCPDDate(data){
		if(data && data.code=="0000"){
			if(data.result.length>0 && data.result[0].status == "0"){
				box.showAlert({message:data.result[0].item_name+"等"+(data.result.length)+"个商品已删除！<br/>请在商品管理中恢复后再进行盘点！"});
				parentView.find("#tiaoma").val("");
			}else{
				var pageParam=data.pageparams;
				parentView.showPager(pageParam,function(param){
					pageIndex=param.page_index;
					pageSize=param.page_size;
					checkInventory();
					getKCPDDate();
				});
				for(var i=0;i<data.result.length;i++){
					data.result[i].index = (pageIndex-1)*pageSize+i+1;
					data.result[i].original_qty_whse = data.result[i].qty_whse;
//					data.result[i].qty_whse = $.round(parseFloat(data.result[i].qty_whse<0 ? 0 : data.result[i].qty_whse), 2);
					data.result[i].qty_whse = $.round(parseFloat(data.result[i].qty_whse), 2);
					data.result[i].pri4 = $.parseMoney(data.result[i].pri4);
					data.result[i].qty_whse_warn = $.round(parseFloat(data.result[i].qty_whse_warn), 2);
					if(inputMap[data.result[i].item_id]){
						data.result[i].qty_whse = inputMap[data.result[i].item_id].qty_turn;
						data.result[i].pcl = inputMap[data.result[i].item_id].qty_pl;
						data.result[i].pl_reason = inputMap[data.result[i].item_id].pl_reason;
					}
					data.result[i].whse_remind = checkItemGetMsg(data.result[i].qty_whse );
				}
				showKCPD(data.result);
			}
		}
		parentView.find("#selewares").removeAttr("disabled");
	}
	
	function getKindName(itemId){
		var kindName="卷烟";
		switch(itemId)
		{
		case "01":
			kindName="卷烟";
		  break;
		case "02":
			kindName="酒水饮料";
			  break;
		case "03":
			kindName="食品副食";
			  break;
		case "04":
			kindName="洗涤日化";
			  break;
		case "05":
			kindName="家居百货";
			  break;
		case "06":
			kindName="针纺服饰";
			  break;
		case "07":
			kindName="文体办公";
			  break;
		case "08":
			kindName="五金家电";
			  break;
		case "99":
			kindName="其他";
			  break;
		default:
			kindName="全部";
		}
		return kindName+"商品-";
	}
	
	function showKCPD(result){
		content=box.ich.view_kcgl_kcpd({list:result, static_item_bar:staticItemBar, item_kind_list:box.kinds.array});
		parentView.empty().append(content);
		if(staticItemKind) {
			parentView.find("#item_kind").val(staticItemKind);
		}
		var tbody = parentView.find("table tbody").unbind("change",changInput);
		tbody.change(changInput);
//		inputs = parentView.find("table tbody").unbind("change",changInput);
//		reasoninputs = parentView.find("table tbody").unbind("change",changInput);		
//		inputs.change(changInput);
//		reasoninputs.change(changInput);
		parentView.find("#selewares").unbind("click").click(onClickTiaoMa);
		parentView.find("#submit").unbind("click").click(onSubmitKCPD);
		//高级搜索查询
		parentView.find("#highSearch").unbind("click").click(onClickHighSearch);
		if(itemKindData){
			parentView.find("#maxInfo").show();
			
			//parentView.find("#itemKindName1").text(getKindName(staticItemKind));
			parentView.find("#sum_qty_whse").text(parseFloat(itemKindData.whse_quantity?itemKindData.whse_quantity:0).toFixed(1));
			parentView.find("#sum_money").text($.parseMoney(parseFloat(itemKindData.whse_amount)));
		}
		
		parentView.find(".table_view").tablesort([
		     {col:0,order:"asc",method:"advance",type:"number"},
		     {col:1,order:"asc",method:"advance",type:"string"},
		     {col:3,order:"desc",method:"advance",type:"number"},
		     {col:4,order:"desc",method:"advance",type:"number"}
		     //{col:7,order:"desc",method:"advance",type:"number"}
		]);
		content.find("tr").select(function(e){
			var target = $(e.target);
			if(!target.hasClass("cause")) {
				$(this).find("#qtywhse").focus();
			}
		});
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
		if(result.length==0) {
			box.showAlert({message:"很抱歉，没有此商品（"+staticItemBar+" ）的库存记录！"});
		}
	}
	
	//
	function changInput(e){
		var target = $(e.target);
		var inputClass = $(target).attr("class");
		if(inputClass == 'inventory'){
			onIoChange(e);
			checkItem(e);
		}else if(inputClass == 'cause'){
			onReasonChange(e);
		}
	}
	
	//修改当前库存后，点击提交操作
	function onSubmitKCPD(){
		checkInventory();
		var obj={};
		var list = new Array();
		$.each(inputMap, function(key, values){
			if(!values.unit_ratio || values.unit_ratio == '0'){
				values.unit_ratio = 1;
			}
			var temValues = {};
			temValues.cost = values.cost;
			temValues.item_id = values.item_id;
			temValues.item_kind_id = values.item_kind_id;
			temValues.item_name = values.item_name;
			temValues.pl_reason = values.pl_reason;
			temValues.unit_ratio = values.unit_ratio;
			temValues.qty_pl = values.qty_pl * values.unit_ratio;
			temValues.qty_turn = values.qty_turn * values.unit_ratio;
			temValues.qty_whse = values.qty_whse * values.unit_ratio;
			list.push(temValues);
		});
		obj.list = list;
		if(list.length > 0) {
			box.request({
				url: box.getContextPath() + upKcpdURL,
				data: {params: $.obj2str(obj)},
				success: submitisOk
			});
		}else{
			box.showAlert({message:"请输入盘点库存数量！"});
		}
	}
	function submitisOk(){
		inputMap={};
		staticItemBar="";
		box.showAlert({message:"库存盘点成功"});
//		getKCPDDate();
		onSelectKind();
	}
	
	//检查商品库存，得到提示消息
	function checkItemGetMsg (qty_whse) {
		var whse_remind = "";
		if (qty_whse >=1000) {
			whse_remind = whse_remind + "商品库存过高！";
		}
		if (qty_whse <=0) {
			whse_remind = whse_remind + "商品库存过低！";
		}
		return whse_remind;
	}
	
	//检查商品
	function checkItem(e){
		var $target = $(e.target);
		var val=parseFloat($target.val());
		var iconSpan = $target.closest("tr").find("span[name='whse_remind_icon']");
		var iconDisplay = iconSpan.css('display');
		
		var msg = checkItemGetMsg(val);
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
	
	//监听修改当前库存的change方法
	function onIoChange(e){
		var $target = $(e.target);
		var val=parseFloat($target.val());
		var item_kind_id=$target.attr("data-kind");
		var qtyWhse=parseFloat($target.attr("data-whse"));
		var pancha=$.round(parseFloat(val)-parseFloat(qtyWhse), 2);
		var inventory = $target.closest("tr").find(".qtypl");
		var item_id=$target.attr("data-id");
		var inputreason=$target.closest("tr").find(".cause").val();	
		var item_cost=$target.attr("data-cost");
		var unitRatio = $target.attr("data-ratio");
		var itemName = $target.attr("data-item-name"); 
		var inputInfo={};
		
		if(!unitRatio || unitRatio == '0'){
			unitRatio = 1;
		}
		inputInfo.item_id=item_id;
		inputInfo.qty_turn=val;
		inputInfo.qty_whse=qtyWhse;
		inputInfo.qty_pl=pancha;
		inputInfo.cost=item_cost;//成本		
		inputInfo.pl_reason=inputreason;
		inputInfo.item_kind_id=item_kind_id;
		inputInfo.unit_ratio = unitRatio;
		inputInfo.item_name = itemName;
		inputMap[item_id]=inputInfo; 
		if(inputMap[item_id].qty_turn==qtyWhse){
			delete inputMap[item_id];
		}
		inventory.text(pancha);
	}
	//监听损益原因文本框
	function onReasonChange(e){
		var $target = $(e.target);
		var val=parseInt($target.val());  
		var tr=$target.closest("tr");
		var inventorytd=tr.find(".inventory");		
		var item_id=$(inventorytd).attr("data-id");
		var inputreason=$target.closest("tr").find(".cause").val();		
		var inputInfo={};
		inputInfo.item_id=item_id;
		inputInfo.pl_reason=inputreason;
		if(inputMap[item_id]){		 
			inputMap[item_id].pl_reason=inputreason;//将原因放到inputMap
		}
		inputReasonMap[item_id]=inputInfo; 
	}
	function checkInventory(){
		var messageList = [];
		if(!$.validateForms(content, messageList)) {
			box.showAlert({message:messageList.length>0?messageList[0]:"请检查输入信息正确性整性！"});
			return;
		}
	}
	
	function onBarcode(data) {
		staticItemBar = data.code;
		parentView.find("#tiaoma").val(staticItemBar);
		pageIndex = 1;
		pageSize = 20;
		params = {big_bar:staticItemBar, page_index:pageIndex, page_size:pageSize};
		staticItemKind = "";
		box.request({
			url:box.getContextPath()+ searchWhseMerchJoinMerchItemUnitUrl,
			data:params,
			success:onGetKCPDDate
		});
	}
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.BARCODE:
				onBarcode(data);
				break;
		}
	}
	
	function onClose(param){
		var isChanged = false;
		staticItemBar = "";
		staticItemKind="01";
		$.each(inputMap?inputMap:{}, function(key,values){     
			return !(isChanged = true);
		});
		if(isChanged){
			box.showConfirm({
				message: "库存记录已修改，您是否保存？",
				hasSubmit: true,
				submit: function() {
					parentView.find("#submit").click();
				},
				donotSubmit: function() {
					inputMap={};
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
			box.listen("kcpd", loadKCPD);
			box.listen("kcpd_onkey", onKey);
			box.listen("kcpd_close", onClose);
		},
		destroy: function() { }
	};
});