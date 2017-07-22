/**
 *入库订单信息查询
 */
HOME.Core.register("plugin-rkcx", function(box) {
	var getPurchOrderListUrl="retail/purch/getSomePurchOrderList";
	var getPurchOrderDetailUrl="retail/purch/getPurchOrderDetail";
	var content=null;
	var parentView = null;
	var beginTime="";
	var endTime="";
	
	function onload(view) {
		parentView= view;
		parentView.showFooter(true);
		pageIndex = 1;
		pageSize = 20;
		getRKCXDate();
	}
	function getRKCXDate(){
		params = {page_index: pageIndex, page_size: pageSize};
		beginTime=parentView.find("#beginTime").val();
		endTime=parentView.find("#endTime").val();
		if(beginTime==undefined || beginTime=="") {
			beginTime=$.GetToday(-30);
		}
		if(endTime==undefined || endTime=="") {
			endTime=$.GetToday();
		}
		if(beginTime>endTime){
			box.showAlert({message:"开始日期不能大于结束日期！"});
			return;
		}
		
		//日期格式化为yyyyMMdd
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");

		params.date_begin = beginTimeFormat;
		params.date_end = endTimeFormat;		
		
		box.request({
			url:box.getContextPath()+ getPurchOrderListUrl,
			data:params,
			success:onGetRKCXDate
		});
	}
	
	function onGetRKCXDate(data){
		if(data && data.code=="0000"){
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getRKCXDate();
			});
			for(var i=0;i<data.result.length;i++){
                
				//日期格式化为yyyy-MM-dd
				var voucherDate=new Date(data.result[i].voucher_date.substr(0, 4),data.result[i].voucher_date.substr(4, 2)-1,data.result[i].voucher_date.substr(6, 2));
				data.result[i].voucher_date=voucherDate.format("yyyy-MM-dd");
				
				data.result[i].index=i+1+(pageSize*(pageIndex-1));
				data.result[i].amt_purch_total = $.parseMoney(data.result[i].amt_purch_total);
			}
			showRKCX(data.result);
		}
	}
	
	function showRKCX(result){
		if(result.length<=0){
			box.showAlert({message:"很抱歉，没有查询到数据！"});
		}
		var content=box.ich.view_rkcx({list: result});
		parentView.empty().append(content);
		if(result.length!=0){
			parentView.find("table tbody tr").click(onRKCXXiangXi);
		}
		parentView.find("#selewares").unbind("click", getRKCXDateAction).click(getRKCXDateAction);
		
		parentView.find("#beginTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find("#endTime").datepicker({
			maxDate: 'd' 
		});
		parentView.find("#beginTime").val(beginTime);
		parentView.find("#endTime").val(endTime);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
	}
	function getRKCXDateAction(){
		pageIndex = 1;
		pageSize = 20;
		getRKCXDate();
	}
	
	function onRKCXXiangXi(e){
		var order_id=$(e.currentTarget).attr("data-id");
		showXiangxi(order_id);
	}
	
	function showXiangxi(order_id){
		box.request({
			url:box.getContextPath()+ getPurchOrderDetailUrl,
			data:{order_id:order_id},
			success:onGetRKCX_Detail
		});
	}
	
	function onGetRKCX_Detail(data){
		if(data && data.code=="0000"){
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1;
			}
				showRKCX_Detail(data.result);
		}
	}
	function setQtyRetColor(){	  
			var qtyRetTds=content.find("td[name='qtyRetTd']");
			for(var i=0;i<qtyRetTds.length;i++){				
				if(qtyRetTds.eq(i).text()>0){
					qtyRetTds.eq(i).css("color","red");
				}
			} 
		
	}
	function showRKCX_Detail(result){
		var order=result[0].order_id;
		var sumAmt=parseFloat(0);
		var sumQty=parseFloat(0);
		for(var i=0;i<result.length;i++){
			sumAmt=parseFloat(sumAmt)+parseFloat(result[i].amt_ord);
			sumQty=parseFloat(sumQty)+parseFloat(result[i].qty_ord);
		}
		
		result.sort(sortFunction);
		
		for(var i=0;i<result.length;i++){
			result[i].index=i+1;
			result[i].amt_ord=$.parseMoney(result[i].amt_ord);
			result[i].pri=$.parseMoney(result[i].pri);
			result[i].big_pri=$.parseMoney(result[i].big_pri);
		}
		
		content=box.ich.view_rkcx_detail({list: result,orderid:order,sumamt:$.parseMoney(sumAmt),sumqty:sumQty});
		box.showDialog({
			title:"入库明细",
			width:960,
			height:500,
			content: content ,close: function(e) {
				//$("#selewares").focus();
			}
		}); 
		content.find(".table_view").fixedtableheader({
			parent: content.find(".table_view").parent(),
			win: content,
			isshow: true 
		});
		setQtyRetColor();
	}
	return {
		init: function(){
			box.listen("rkcx", onload);
		},
		destroy: function() { }
	};
	
	function sortFunction(x,y){
		return -1*(x.qty_ord-y.qty_ord);
	}
});