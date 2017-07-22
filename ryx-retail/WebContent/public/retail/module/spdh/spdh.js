/**
 * 积分兑换商品
 */
HOME.Core.register("plugin-spdh", function(box) {
	
	var searchExchangePrizeUrl = "retail/consumer/searchExchangePrize";//查询兑换商品
	var insertExchangePrizeUrl = "retail/consumer/insertExchangePrize";//插入兑换商品
	var updExchangePrizeUrl = "retail/consumer/updateExchangePrize";//修改兑换商品
	var insertScoreExchangeUrl = "retail/consumer/insertScoreExchange";//兑换商品
	var searchConsumerUrl = "retail/consumer/searchMerchConsumerInfo";//查询会员
	
	var exchangePrizeDetails = null;
	var jfdhDetails = null;
	var currentConsumer = {};
	var consumerInput = null;
	var consumerDiv = null;
	var parentView = null;
	var prizeName = "";
	var pageIndex = "1";
	var pageSize = "20";
	function onload(view) {
		parentView = view;
		prizeName = "";
		parentView.showFooter(true);
		getExchangePrize();
	}
	
	function getExchangePrize(){
		var params = {page_size:pageSize, page_index:pageIndex};
		prizeName = $("#prize_name").val();
		if(prizeName ){
			params.prize_name = prizeName ; 
		}
		
		box.request({
			url:box.getContextPath()+ searchExchangePrizeUrl,
			data:{params:$.obj2str (params)},
			success: showExchangePrize
		});
	}
	
	function showExchangePrize(data){
		if(data && data.code=="0000"){
			var result = data.result;
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getExchangePrize();
			});
			for ( var i = 0; i < result.length; i++) {
				result[i].index = i + 1;
				
				var date = result[i].create_date ; 
				date = new Date(date .substr(0, 4), date.substr(4, 2)-1, date.substr(6, 2));
				date = date.format("yyyy-MM-dd");
				var time = result[i].create_time ;
				time = time.substr(0, 2) +":"+ time.substr(2, 2);
				result[i].data_time = date+" "+time;
				
				if(result[i].status == '1'){
					result[i].status_test = "有效"; 
				}else{
					result[i].status_test = "无效";
				}
			}
			var content = box.ich.view_spdh({list:result});
			parentView.empty().append(content);
			
			$("#exchange").unbind("click").click(showExchangePrizeDetails);//添加
			$("#add_exchange_prize").unbind("click").click(showExchangePrizeDetails);//添加
			$("#query").unbind("click").click(getExchangePrize);//查询
			$("#prize_name").val(prizeName);
			
			content.find("table tbody tr").unbind("click").click(getExchangePrizeById);
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			});
		}
	}
	
	//显示商品兑换界面
	function showExchangDialog(data){
		if(data && data.code=="0000"){
			jfdhDetails = box.ich.view_spdh_jfdh({result:data.result[0]});			
			jfdhDetails=box.showDialog({
				title:"兑换",
				width:470,
				height:430,
				content: jfdhDetails
			});
			consumerDiv = jfdhDetails.find("#consumer_container");
			resetConsumerInput();
			//$("#search_consumer_btn").unbind("click").click(getConsumer);
			$("#submit_exchang_btn").unbind("click").click(insertScoreExchange);
			//$("#consumer_text").unbind("change").change(getConsumer);
			$("#quxiao").unbind("click").click(function(){
				box.closeDialog({content: jfdhDetails});
			});//取消
		}
		
	}
	
	function resetConsumerInput() {
		currentConsumer = {};
		consumerDiv.empty().append(box.ich.view_spdh_jfdh_consumer_input());
		consumerInput = consumerDiv.find("#consumer_text");
		consumerInput.change(getConsumer);
	}
	
	//通过id得到兑换商品
	function getExchangePrizeById(e){
		var target = $(e.target);
		var tr = $(target).closest("tr");
		var prize_id = $.trim($(tr).attr("data-prize-id"));
		var is_valid = $(this).find("td:eq(4)").text();
		var params = {};
		params.prize_id = prize_id;
		if(target.attr("name")=="jfdh") {
			if(is_valid == "有效"){
				box.request({
					url:box.getContextPath()+ searchExchangePrizeUrl,
					data:{params:$.obj2str (params)},
					success: showExchangDialog
				});
			}else if(is_valid == "无效"){
				box.showAlert({message:"无效商品，不能兑换！"});
				return;
			}
		}else{
			box.request({
				url:box.getContextPath()+ searchExchangePrizeUrl,
				data:{params:$.obj2str (params)},
				success: showExchangePrizeDetails
			});
		}
		
		
	}
	
	//获得会员
	function getConsumer(e){
		var target = $(e.target);
		var div = $(target).closest("div");
		var keyword = $(div).find("#consumer_text").val();
		if(keyword) {
			box.request({
				url: box.getContextPath() + searchConsumerUrl,
				data: {params: $.obj2str({keyword: keyword})},
				success: showConsumer
			});
		}
	}
	
	function showConsumer(data){
		if(data && data.code=='0000' && data.result.length >= 1) {
			var consumerUl = box.ich.view_spdh_jfdh_consumer_ul({card_id:data.result[0].card_id});
			consumerDiv.empty().append(consumerUl);
			consumerDiv.find("#clean_consumer").unbind("click").click(onClearConsumerButtonClick);
			$("#myconsumer_name").text(data.result[0].consumer_name);
			$("#myconsumer_card").text(data.result[0].card_id);
			$("#myconsumer_phone").text(data.result[0].telephone);
			$("#myconsumer_score").text(data.result[0].curscore);
			$("#submit_exchang_btn").attr("data-consumer-id",data.result[0].consumer_id);
		}else{
			box.showAlert({message: "会员信息获取失败！！"});
			return;
		}
	}
	
	//取消会员
	function onClearConsumerButtonClick(e) {
		resetConsumerInput();
		$("#myconsumer_name").text("");
		$("#myconsumer_card").text("");
		$("#myconsumer_phone").text("");
		$("#myconsumer_score").text("");
		$("#submit_exchang_btn").attr("data-consumer-id","");

	}
	
	//兑换商品
	function insertScoreExchange (e){
		var consumerId = $(e.target).attr("data-consumer-id");
		var dataId = $(e.target).attr("data-id");
		if(!consumerId){
			box.showAlert({message:"请输入查询会员！"});
			return;
		}
//		var consumerCard = $("#myconsumer_card").text();
//		var consumerPhone = $("#myconsumer_phone").text();
//		var consumerName = $("#myconsumer_name").text();
		var itemName = $("#exchang_name").text();
		var consumeScore = $("#myconsumer_score").text();
		var itemScore = $("#exchang_score").text();
		if( parseFloat(itemScore) > parseFloat(consumeScore)){
			box.showAlert({message:"对不起，积分数量不足！"});
			return ;
		}
		var params={consumer_id:consumerId, score:itemScore, prize_name:itemName, prize_id:dataId};
		box.request({
			url: box.getContextPath() + insertScoreExchangeUrl,
			data: {params: $.obj2str(params)},
			success: function(data){
				if(data && data.code=="0000"){
					box.showAlert({message:"兑换成功！"});
					getExchangePrize();
					box.closeDialog({content: jfdhDetails});
				}
			}
		});
	}
	
	//显示兑换商品详情页
	function showExchangePrizeDetails(data){
		var title = "新增兑换商品";
		var result = null;
		if(data && data.code=="0000"){
			result = data.result;
			if(result && result.length){
				result = result[0];
			}
			title = "修改兑换商品";
		}
		exchangePrizeDetails = box.ich.view_spdh_details({result:result});			
		exchangePrizeDetails=box.showDialog({
			title:title,
			width:470,
			height:280,
			content: exchangePrizeDetails
		});
		
		if(result && result.status == 1){
			$("input[name=status]:eq(0)").prop("checked",true);
		}else if(result && result.status == 0){
			$("input[name=status]:eq(1)").prop("checked",true);
		}
		
		$("#submit").unbind("click").click(updOrInsExchangePrize);//保存
		$("#quxiao").unbind("click").click(function(){
			box.closeDialog({content: exchangePrizeDetails});
		});//取消
	}
	
	//插入或修改兑换商品
	function updOrInsExchangePrize(e){
		var messageList = [];
		if(!$.validateForms(exchangePrizeDetails, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		var url = insertExchangePrizeUrl;
		var itemName = $("#item_name").val();
		var score = $("#score").val();
		var prizeId = $(e.target).attr("data-prize-id");
		var status = $("input[name='status']:checked").val();
		var params={score:score, prize_name:itemName, status:status};
		
		if(prizeId && prizeId != "/" ){
			url = updExchangePrizeUrl;
			params.prize_id = prizeId;
		}
		box.request({
			url:box.getContextPath()+ url,
			data:{params:$.obj2str (params)},
			success: function(data){
				if(data && data.code=="0000"){
					box.showAlert({message:"积分商品保存成功！"});
					$("#prize_name").val("");
					getExchangePrize();
					box.closeDialog({content: exchangePrizeDetails});
				}
			}
		});
	}
	
	return {
		init: function(){
			box.listen("spdh", onload);
		},
		destroy: function() { }
	};
});