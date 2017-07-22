/**
 * 进销存明细表
 */
HOME.Core.register("plugin-kcbb", function(box) {
	var getSaleReportTableUrl="retail/whse/getWhseMerchList";
	var getSaleReportTableByLuceUrl = "retail/whse/searchMerchItemJoinWhseMerchByLucene";
	var parentView = null;
	var content = null;
	var beginTime="";
	var endTime="";
	var itemKind="";
	var keyWord = ""; 
	
	function onload(view) {
		parentView = view;
		showTable();
	}
	//显示明细报表
	function showTable(){
		parentView.find("#selewares").attr("disabled","disabled");
		
		var params={"page_index":"-1","page_size":"-1"};
		
		itemKind=parentView.find("#item_kind").val();
		if(!itemKind){
			itemKind = "01";
		}
		if(itemKind && itemKind != "00"){
			params.item_kind_id = itemKind;
		}
		
		keyWord=parentView.find("#keyword").val();
		if(keyWord){
			params.keyword = keyWord;
		}
		
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	
	
	function showTablebyLuce(){
		var params={"page_index":"-1","page_size":"-1"};
		
		itemKind=parentView.find("#item_kind").val();
		if(!itemKind){
			itemKind = "01";
		}
		if(itemKind && itemKind != "00"){
			params.item_kind_id = itemKind;
		}
		keyWord=parentView.find("#keyword").val();
		if(keyWord){
			params.key = keyWord;
		}
		box.request({
			url : box.getContextPath() + getSaleReportTableByLuceUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	
	
	function successJXCReportTable(data){
		parentView.find("#selewares").removeAttr("disabled");
		
		if(data && data.code=="0000") {
			if(data.result.length<=0){
				  box.showAlert({message:"很抱歉，没有查询到数据！"});
			}
			var result = data.result;
			var sum_amt_whse = 0;
			var sum_qty_whse = 0;
			var sum_pri4 = 0;
			var sum_cost = 0;
			if(result[0].status == "0"){
				box.showAlert({message:data.result[0].item_name+"等"+(data.result.length)+"个商品已删除！<br/>请在商品管理中恢复后再进行盘点！"});
				parentView.find("#keyword").val("");
			}else{
				for ( var i = 0; i < result.length; i++) {
					result[i].pri4 = $.parseMoney(result[i].pri4);
					result[i].cost = $.parseMoney(result[i].cost);
					sum_pri4 += parseFloat(result[i].pri4);
					sum_cost += parseFloat(result[i].cost);
//					if(result[i].qty_whse < 0){
//						result[i].amt_whse = $.parseMoney(0);
//						result[i].qty_whse = "0.0";
//						continue;
//					}
					result[i].qty_whse = parseFloat($.parseMoney(result[i].qty_whse)).toFixed(1);
					result[i].amt_whse = $.parseMoney(result[i].cost * result[i].qty_whse);
					sum_amt_whse += parseFloat(result[i].amt_whse);
					sum_qty_whse += parseFloat(result[i].qty_whse);
					
				}
				var whseMap = {};
				whseMap.list = result;
				if(result.length >= 1){
					sum_pri4 = $.parseMoney(sum_pri4/result.length) ;
					sum_cost = $.parseMoney(sum_cost/result.length);
				}
				whseMap.sum_pri4 = $.parseMoney(sum_pri4) ;
				whseMap.sum_cost = $.parseMoney(sum_cost);
				whseMap.sum_amt_whse = $.parseMoney(sum_amt_whse) ;
				whseMap.sum_qty_whse = $.round(sum_qty_whse, 1);
				
				content=box.ich.view_kcbb({result:whseMap, item_kind_list:box.kinds.array});
				if(itemKind) {
					content.find("#item_kind").val(itemKind);
				}
				content.find("#keyword").val(keyWord);
				parentView.empty().append(content);
				parentView.find("#beginTime").val(beginTime);
				parentView.find("#endTime").val(endTime);
				parentView.find("#beginTime").datepicker({
					maxDate: '-1d' 
				});
				parentView.find("#endTime").datepicker({
					maxDate: '-1d' 
				});
				parentView.find("#beginTime").change(function(){
					parentView.find("#selewares").focus();
				});
				parentView.find("#endTime").change(function(){
					parentView.find("#selewares").focus();
				});
				parentView.find("#selewares").unbind("click", showTable).click(showTable);
	
	//			parentView.find("#selewares").unbind("click", showTable).click(function(){
	//				var k = content.find("#keyword").val();
	//				if(k){
	//					showTablebyLuce();
	//				}else{
	//					showTable();
	//				}
	//			});
				
				parentView.find(".table_view").tablesort([
				       		        {col:1,order:"asc",method:"advance",type:"number"},
				       		        {col:3,order:"desc",method:"advance",type:"number"},
				       		        {col:4,order:"desc",method:"advance",type:"number"},
				       		        {col:5,order:"desc",method:"advance",type:"number"},
				       		        {col:6,order:"desc",method:"advance",type:"number"},
				       		        {col:7,order:"desc",method:"advance",type:"number"},
				       		        {col:8,order:"desc",method:"advance",type:"number"},
				       		        {col:9,order:"desc",method:"advance",type:"number"}
				       		]);
				
	
				parentView.showFooter(true);
				parentView.find(".table_view").fixedtableheader({
					parent: parentView,
					win: parentView.parent(),
					isshow: true
				}).fixedtablefooter({
					parent: parentView,
					win: parentView.parent(),
					offset: -1
				});
			}
		}
	}
	function onGetTicket(){
		box.request({
			url: box.getContextPath() + selectTicketUrl,
			success:onGetTicketData
		});
	}
	function onBarcode(data) {
		var itemBar = data.code;
		
		var beginTime=parentView.find("#beginTime").val();
		var endTime=parentView.find("#endTime").val();
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		var params={'start_date':beginTimeFormat,'end_date':endTimeFormat, 'item_bar':itemBar };
		box.request({
			url : box.getContextPath() + getSaleReportTableUrl,
			data : {params: $.obj2str(params)},
			success : successJXCReportTable
		});
	}
	function onclose(){
		parentView = null;
		content = null;
		beginTime="";
		endTime="";
		itemKind="";
		keyWord = "";
	}
	function onKey(data) {
		switch(data.key) {
			case HOME.Keys.BARCODE:
				onBarcode(data);
				break;
		}
	}
	return {
		init: function(){
			box.listen("kcbb", onload);
			box.listen("kcbb_close", onclose);
			box.listen("kcbb_onkey", onKey);
		},
		destroy: function() { }
	};
});