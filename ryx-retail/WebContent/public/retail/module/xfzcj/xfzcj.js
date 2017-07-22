/**
 * 消费者抽奖功能
 */
HOME.Core.register("plugin-xfzcj", function(box) {
	
	var listUrl = "retail/activity/getConsumerRaffleList";
	var detailUrl = "retail/activity/getConsumerRaffleDetail";
	
	var parentView = null;
	
	var acts = null;
	
	function loadXFZCJ(view) {
		parentView = view;
		
		getConsumerRaffleList();
	}
	
	function getConsumerRaffleList() {
		box.request({
			url: box.getContextPath() + listUrl,
			success: onGetConsumerRaffleList
		});
	}
	
	function onGetConsumerRaffleList(data) {
		if(data && data.code == "0000") {
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1;	
			}
			showConsumerRaffleList(data.result);
		}
	}	
	function showConsumerRaffleList(result) {
		acts = result;
		var content = box.ich.view_yxhd_xfzcj({list: result});
		parentView.empty().append(content);
		parentView.find("table tbody tr").unbind("click", getConsumerRaffleDetail).click(getConsumerRaffleDetail);
		
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
	}
	
	
	function getConsumerRaffleDetail(e){
		var $target = $(e.currentTarget);
		var actId = $target.attr("data-id");
		
		var obj = {actId: actId};
		box.request({
			url: box.getContextPath() + detailUrl,
			data: {params: $.obj2str(obj)},
			success: showConsumerRaffleDetail
		});
	}
	
	function showConsumerRaffleDetail(data){
		var map = data.result;
		var content = box.ich.view_yxhd_xfzcj_detail(map);
		//parentView.empty().append(content);
		box.showDialog({
			title: map.act_title,
			width: 800,
			height: 560,
            modal: true,
			content: content
		});
	}
	
	return {
		init: function(){
			box.listen("xfzcj", loadXFZCJ);
		},
		destroy: function() { }
	};
});
