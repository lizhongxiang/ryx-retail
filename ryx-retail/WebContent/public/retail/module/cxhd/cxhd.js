/**
 * 促销活动功能
 */
HOME.Core.register("plugin-cxhd", function(box) {
	var cxhdURL="retail/promotion/searchMerchPromotion";
	//正式会员等级URL  retail/consumer/searchMerchConsumerGradeAndConsumerNumber
	//测试会员等级URL  retail/consumer/searchMerchConsumerGrade
	var vipgradeURL="retail/consumer/searchMerchConsumerGradeAndConsumerNumber";
	var updateCXHD="retail/promotion/updateMerchPromotion";
	var parentView=null;
	var content=null;
	//商品查询
	var merchItemUrl="retail/item/searchMerchItem";
	var searchUnitJoinMerchItemUrl = "retail/item/getItemDetail";
	var baseItemUrl="retail/item/searchItem";
	var insertPromotionUrl = "retail/promotion/insertMerchPromotion";//保存促销信息
	var pageIndex = 1;
	var pageSize = 20;
	//通过商品类别查询商品
	var currentItemType = null;
	//定义窗口
	var promotionAddDialog = null;
	var fullReductionAddDialog = null;
	var vippromotionAddDialog = null;
	var makeSureSubmitDialog = null;
	var integralAddDialog = null;
	var orderIntegralDialog = null;
	//重码商品选择界面
	var selectItemDialog = null;
	//定义next窗口
	var backDialogs = null;
	//定义促销活动属性
	//促销活动信息
	var merch_promotion = null;
	var stradd = null;
	var consumerType = new Object();//会员等级
	var newConsumerGradeResult = [];//新的会员等级
	var myConsumerPromotion = {};//用户自定义会员折扣-----
	var myOraderIntegralPromotion = {};//销售单会员积分
	var myItemPromotion = {};//用户自定义商品（折扣、金额、积分） ---
	
	var itemInfo = {};//保存商品数据实现上下页保存
	var myCeilingReduction = {};//用户自定义满减
	var updateOrAdd = null;//用于判断新增还是修改
	var showDialogTile = "新增";
	
	var beginTime = null; 
	var endTime = null;
	/*function stopPP(e) {
		var evt = e || window.event;
		//IE用cancelBubble=true来阻止而FF下需要用stopPropagation方法
		evt.stopPropagation ?evt.stopPropagation() : (evt.cancelBubble=true);
	}*/
	
	function onload(view) {
		pageIndex = 1;
		pageSize = 20;
		parentView = view;
		parentView.closeMessage();
		parentView.showFooter(true);
		
		beginTime=$.GetToday(-30);
		endTime=$.GetToday(30);
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		var params = {page_index: pageIndex, page_size: pageSize};
		params.start_date = beginTimeFormat ;
		params.end_date = endTimeFormat;
		params.is_clash = "1";
		getPromotionJson(params);
	}
	
	//促销信息json
	function getPromotionJson(params){
		box.request({
			url:box.getContextPath()+ cxhdURL,
			data:{params:$.obj2str (params)},
			success:showCXHDList
		});
	}
	
	//显示促销列表
	function showCXHDList (data){
		merch_promotion = {};
		var promotionList = [];
		if(data && data.code=="0000"){
			var pageParam = data.pageparams;
			if(pageParam.count <= 0){
				box.showAlert({message: "没有查询到促销活动！"});
			}
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				var params = {page_index: pageIndex, page_size: pageSize};
				getPromotionJson(params);
			});
			for ( var i = 0; i < data.result.length; i++) {
				data.result[i].promotion_content_all = data.result[i].promotion_content;
				data.result[i].promotion_content = data.result[i].promotion_content.length>20 ? data.result[i].promotion_content = data.result[i].promotion_content.substring(0,15)+"···" : data.result[i].promotion_content ;
				data.result[i].promotion_desc_all = data.result[i].promotion_desc;
				data.result[i].promotion_desc = data.result[i].promotion_desc.length >10 ? data.result[i].promotion_desc.substring(0,8)+"···" : data.result[i].promotion_desc;
				var newPromotionMap = analyzePromotion(data.result[i]);
				newPromotionMap.index = i+1;
				promotionList.push( newPromotionMap);
			}
		}
		var content=box.ich.view_cxhd({list:promotionList});
		parentView.empty().append(content);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		content.find("#beginTime").val(beginTime); 
		content.find("#endTime").val(endTime);
		content.find("#beginTime").datepicker();
		content.find("#endTime").datepicker();
		content.find("#query").unbind("click").click(function (){
			beginTime = content.find("#beginTime").val(); 
			endTime = content.find("#endTime").val();
			var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
			beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
			var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
			endTimeFormat=endTimeFormat.format("yyyyMMdd");
			if(!beginTimeFormat){
				box.showAlert({message: "对不起，开始时间不能为空！"});
				return ;
			}
			if(!endTimeFormat){
				box.showAlert({message: "对不起，结束时间不能为空！"});
				return ;
			}
			if(beginTimeFormat > endTimeFormat){
				box.showAlert({message: "对不起，结束时间不能大于开始时间！"});
				return ;
			}
			
			var params = {page_index: pageIndex, page_size: pageSize, is_clash:'1' };
			
			
			params.start_date = beginTimeFormat ;
			params.end_date = endTimeFormat;
			
			box.request({
				url:box.getContextPath()+ cxhdURL,
				data:{params:$.obj2str (params)},
				success:showCXHDList
			});
		});
		//点击新增促销活动,清空数据
		parentView.find("#add").unbind("click").click(function (){
			updateOrAdd ="add";
			getConsumerGrade();
		});
		//添加tr监听  用作update
		content.find("table tbody tr").unbind("click").click(searchCUHD);
		/*parentView.find("#startdate").datepicker();
		parentView.find("#enddate").datepicker();*/
		content.find("#startdate").datepicker({
			minDate: 'd'
		});
		content.find("#enddate").datepicker({
			minDate: 'd' 
		});
	}
	//通过商品类型选择商品
	function onChoiceItemButtonClick(e) {
		box.controls.choiceItemDialog(undefined, function(itemList) {
			addItem(itemList[0]);
		});
	}
	//点击tr,通过promotion_id,查出对应的单挑数据
	function searchCUHD(e){
		//page_index: pageIndex, page_size: pageSize
		params = {};
		params.promotion_id = $(e.currentTarget).attr("data-id");
		box.request({
			url:box.getContextPath()+ cxhdURL,
			data:{params:$.obj2str (params)},
			success: function(data) {
				if(data && data.code=="0000"){
					itemInfo = {};
					myItemPromotion = {};
					var promotionList = data.result;
					merch_promotion = promotionList[0];
					myConsumerPromotion = merch_promotion.promotion_action.discount;
					myOraderIntegralPromotion = merch_promotion.promotion_action.point;
					merch_promotion.not_promotion_id = params.promotion_id;
					updateOrAdd = "update";
					//判断促销活动类型
					getConsumerGrade();
				}
			}
		});
	}
	
	//获得会员等级
	function getConsumerGrade(){
		if(updateOrAdd =="add" || updateOrAdd ==null){
			updateOrAdd = "add";
			showDialogTile = "新增";
			merch_promotion = {};
		}else if(updateOrAdd =="update"){
			updateOrAdd ="update";
			showDialogTile = "修改";
		}
		params = {};
		box.request({
			url:box.getContextPath()+ vipgradeURL,
			data:{params:$.obj2str (params)},
			success: function(data){
				consumerType = data;
				if(updateOrAdd == "add"){
					myCeilingReduction = {};//用户自定义满减
					myConsumerPromotion = {};//用户自定义会员折扣-----
					myOraderIntegralPromotion = {};
					itemInfo = {};//保存商品数据实现上下页保存
					myItemPromotion = {};//用户自定义商品（折扣、金额、积分） ---
				}else{
					if(merch_promotion.promotion_type == "10" || merch_promotion.promotion_type == "30"){
						getItemInfo();
					}
				}
				onAddCXHD(data);
			}
		});
	}
	//进入新增促销主界面
	function onAddCXHD(data){
		if(data && data.code=="0000"){
			var result = {};
			//促销活动时间
			if(merch_promotion.start_time){
				merch_promotion = analyzePromotion(merch_promotion);
				result.start_time_text =  merch_promotion.start_time_text;
			}else{
				result.start_time_text = "00:00";
			}
			if(merch_promotion.end_time){
				result.end_time_text =  merch_promotion.end_time_text;
			}else{
				result.end_time_text = "23:59";
			}
			if(merch_promotion.start_date){
				result.start_date_text = merch_promotion.start_date_text;
			}else{
				result.start_date_text = $.GetToday();
			}
			if(merch_promotion.end_date){
				result.end_date_text = merch_promotion.end_date_text;
			}else{
				result.end_date_text = $.GetToday(30);
			}
			if(merch_promotion.promotion_desc){
				result.promotion_desc = merch_promotion.promotion_desc;
			}
			if(merch_promotion.promotion_content){
				result.promotion_content = merch_promotion.promotion_content;
			}
			
			content=box.ich.view_cxhd_add({result:result});
			box.showDialog({
				title: showDialogTile+"促销活动",
				//780,600, 
				width:780,
				height:550,
				content: content,
				close:function(e) {
					parentView.find("#add").removeAttr("disabled");
				}
			});
			//判断促销活动是否有效
			if(merch_promotion.status){
				if(merch_promotion.status =="0"){
					$("#status1").removeAttr("checked");
					$("#status0").attr("checked","checked");
				}else{
					$("#status0").removeAttr("checked");
					$("#status1").attr("checked","checked");
				}
			}else{
				merch_promotion.status = $('input[name="status"]:checked').val();
			}
			//弹出日历控件
			/*content.find("#startdate").datepicker({
				minDate: 'd' 
			});*/
			content.find("#startdate").datepicker({
				minDate: 'd'
			});
			content.find("#enddate").datepicker({
				minDate: 'd' 
			});
			//content.find("#enddate").datepicker();
			content.find("#vipcheckboxid").append("&nbsp;&nbsp;<input type='checkbox' name='consumerGrade' value='0'/>全部&nbsp;&nbsp;");
			$.each(data.result, function(i, vip_grade) {
				content.find("#vipcheckboxid").append("&nbsp;&nbsp;<input type='checkbox' name='consumerGrade' value='"
				+vip_grade.grade+"'/>"+vip_grade.grade_name+"&nbsp;&nbsp;");
			});
			//选择促销类型
			if(merch_promotion.promotion_type){
				content.find("#promotion_type").val(merch_promotion.promotion_type);
			}
			//选择促销针对人群
			if(merch_promotion.promotion_must){
				if(merch_promotion.promotion_must.target.all == "true"){
					content.find("#consumer_type").val("all");//针对人群选择为-所有顾客
				}else{
					content.find("#consumer_type").val("consumer");//针对人员选择为-会员
					content.find("#vipcheckboxtr").show();//现在会员复选框
					if(merch_promotion.promotion_must.target.all_member == "true"){//全部会员
						//选择全部会员
						content.find('input[type="checkbox"][name="consumerGrade"]').slice(0,10).prop("checked",true);
					}else{//部分会员
						var consumerGradeArr = merch_promotion.promotion_must.target.member_grade;//得到选择的会员
						for ( var int = 0; int < consumerGradeArr.length; int++) {
							//并在复选框中选中
							$("input[type='checkbox'][name='consumerGrade'][value='"+consumerGradeArr[int]+"']").prop("checked",true);
						}
					}
				}
				//设置修改的类型和针对人群不可修改
				if(updateOrAdd == "update"){
					$("#promotion_type").prop("disabled","disabled");
					$("#promotion_type").css("cursor","default");
					$("#consumer_type").prop("disabled","disabled");
					$("#consumer_type").css("cursor","default");
				}
			}
			content.find("#nextDialog").unbind("click").click(operateConsumer);//下一步
			//监听活动类型选择  如果选择会员折扣 设置会员打折
			content.find("#promotion_type").change(function(e){
				itemInfo = {};
				myItemPromotion = {};
				$('[type=checkbox]:checkbox').prop("checked",false);
				if(content.find("#promotion_type").val()==13 || content.find("#promotion_type").val()==30 || content.find("#promotion_type").val()==33){
					content.find("#promotion_checked").remove();
					content.find("#null_promotion_checked").remove();
					content.find("#vipcheckboxtr").show();
					//添加会员打折需要打折的会员等级列表
				}else{
					if($("#null_promotion_checked").val()==undefined && $("#consumer_type").val() != "consumer"){
						content.find("#consumer_type").prepend("<option value='all' id='promotion_checked'>所有顾客</option>");
						content.find("#consumer_type").prepend("<option value='' id='null_promotion_checked'>请选择针对群体</option>");
						content.find("#vipcheckboxtr").hide();
					}else if($("#null_promotion_checked").val()==undefined && $("#consumer_type").val() == "consumer"){
						content.find("#consumer_type").prepend("<option value='all' id='promotion_checked'>所有顾客</option>");
						content.find("#consumer_type").prepend("<option value='' id='null_promotion_checked'>请选择针对群体</option>");
					}
					//每次选择活动类型,重置针对群体
					content.find("#consumer_type").val("");
					content.find("#vipcheckboxtr").hide();
				}
			});
			//监听活动针对群体选择会员事件
			content.find("#consumer_type").change(function(e){
				if($("#consumer_type").val()=="consumer" && $("#promotion_type").val()!="13"){
					content.find("#vipcheckboxtr").show();
					//添加会员打折需要打折的会员等级列表
				}else if($("#consumer_type").val()=="consumer" && $("#promotion_type").val()=="13"){
					//会员折扣,不做修改
				}else{
					content.find("#vipcheckboxtr").hide();
				}
			});
			content.find("input[type='checkbox'][name='consumerGrade']").unbind("change").change(function(e){
				var boxes = document.getElementsByName('consumerGrade');
				var checkedCount=0;
				if($(this).val()=="0"){//全选或取消
					for(var i=0; i<boxes.length; i++) {
						boxes[i].checked = boxes[0].checked;
					}
				}else{
					if($(this).val()!="0" && content.find("input[type='checkbox'][name='consumerGrade']:first").is(':checked')){
						content.find("input[type='checkbox'][name='consumerGrade']:first").removeAttr("checked");
					}else{
						$("input[type='checkbox'][name='consumerGrade']").each(function (index, domEle) {
							if($(this).is(':checked') && $(this).val()!=="0"){
								checkedCount++;
							}						
						});
						if(checkedCount==9){
							content.find("input[type='checkbox'][name='consumerGrade']:first").prop("checked",true);
						}
					}
				}
			});
			//判断选择会员列表,针对群体清空
			if($("#promotion_type").val() == 13 || $("#promotion_type").val() == 30 || $("#promotion_type").val() == 33){
				content.find("#promotion_checked").remove();
				content.find("#null_promotion_checked").remove();
			}
		}
	}
	
	function operateConsumer(event) {
		if($("#startdate").val()==""){
			box.showAlert({message: "对不起，请选择促销活动开始时间！"});
			return ;
		}
		if($("#enddate").val()==""){
			box.showAlert({message: "对不起，请选择促销活动结束时间！"});
			return ;
		}
		if($("#startdate").val() > $("#enddate").val()){
			box.showAlert({message:"活动开始日期不能大于活动结束日期！"});
			return ;
		}
		if($("#promotion_type").val()==0){
			box.showAlert({message: "对不起，请选择活动类型！"});
			return;
		}
		if($("#consumer_type").val()==""){
			box.showAlert({message: "对不起，请选择活动针对群体！"});
			return ;
		}
		var messageList = [];
		if(!$.validateForms(content, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		//判断会员是否选中
		if($("#promotion_type").val() =="13" || $("#consumer_type").val() == "consumer"){
			if($('input[type="checkbox"][name="consumerGrade"]:checked').length==0){
				box.showAlert({message: "对不起，请选择要优惠的会员级别！"});
				return ;
			}
		}
		//促销活动是否有效
		merch_promotion.status = $('input[name="status"]:checked').val();
		//赋值 获取新增促销活动表单中的数据
		if($("#startdate")){
			var startDate = $("#startdate").val();
			merch_promotion.start_date = startDate ;
		}
		if($("#enddate")){
			var endDate = $("#enddate").val();
			merch_promotion.end_date = endDate;
		}
		if($("#starttime")){
			merch_promotion.start_time = $("#starttime").val();
		}
		if($("#endtime")){
			merch_promotion.end_time = $("#endtime").val();
		}
		if($("#promotion_type")){
			merch_promotion.promotion_type = $("#promotion_type option:selected").val();
		}
		var target = {};
		if($("#consumer_type").val() == "all"){//全部人群
			target.all = "true";
		}else{
			if($('input[type="checkbox"][name="consumerGrade"]:first').is(':checked')){//所有会员
				target.all_member = "true";
				newConsumerGradeResult = consumerType.result;
			}else{
				var obj =[];
				newConsumerGradeResult = [];
				$('input[type="checkbox"][name="consumerGrade"]:checked').each(function(){
					obj.push( $(this).val());
					for(var i = 0;i < consumerType.result.length;i++){
						if($(this).val() == consumerType.result[i].grade ){
							var newConsumerGradeMap = {};
							newConsumerGradeMap["grade"] = consumerType.result[i].grade;
							newConsumerGradeMap["grade_name"] = consumerType.result[i].grade_name;
							newConsumerGradeResult.push(newConsumerGradeMap);
						}
					}
				});
				target.member_grade = obj;
			}
		}
		var promotion_must = new Object();
		promotion_must.target = target ;
		merch_promotion.promotion_must = promotion_must;
		if($("#promotion_desc")){
			merch_promotion.promotion_desc = $("#promotion_desc").val();
		}
		if($("#promotion_content")){
			merch_promotion.promotion_content = $("#promotion_content").val();
		}
		merch_promotion = analyzePromotion(merch_promotion);
		//判断类型 跳转到不同的function
		if($("#promotion_type").val()==0){
			box.showAlert({message: "对不起，请选择活动类型！"});
			return;
		}else if($("#promotion_type").val()==10){
			//折扣促销
			promotionAdd();
			box.closeDialog({content: content});
		}else if($("#promotion_type").val()==13){
			//会员折扣
			vipPromotionAdd(newConsumerGradeResult);
			box.closeDialog({content: content});
		}else if($("#promotion_type").val()==30){
			//积分
			integralAdd();
			box.closeDialog({content: content});
		}else if($("#promotion_type").val()==33){
			//销售单积分
			showOrderIntegralDialog(newConsumerGradeResult);
			box.closeDialog({content: content});
		}else if($("#promotion_type").val()==40){
			//满减促销
			fullReductionAdd();
			box.closeDialog({content: content});
		}else{
			box.showAlert({message:"对不起，请选择活动类型！"});
		}
	}
	
	//积分
	function integralAdd(){
		stradd = "integral";
		integralAddDialog = box.ich.view_cxhd_integral();
		integralAddDialog = box.showDialog({
			title:showDialogTile+"积分促销活动",
			/*width:760,
			height:500,*/
			width:780,
			height:550,
			content:integralAddDialog,
			close:function(e) {},
			onkey: function(data) {
				switch(data.key) {
					case HOME.Keys.DELETE:
						break;
					case HOME.Keys.BARCODE:
						stradd = "integral";
						searchMerchItem(data.code);
				}
			}
		});
		$.each(itemInfo, function(key, value) {
			value.cost = $.parseMoney(parseFloat(value.cost) * parseFloat(value.unit_ratio));
			value.big_pri4 = $.parseMoney(value.big_pri4);
			$("#adding").append(box.ich.view_integral_item_row(value));
			var myItemInfo = {};
			if(value.discount){
				myItemInfo.discount = value.discount;
			}
			if(value.discount_price){
				myItemInfo.discount_price = value.discount_price;
			}
			if(value.point){
				myItemInfo.point = value.point;
			}
			myItemPromotion[key] = myItemInfo;
		});
		//监听商品类别
		currentItemType = $("#order_type");
		currentItemType.unbind("click").click(onChoiceItemButtonClick);
		
		//删除商品
		integralAddDialog.find("a[name='removeTr']").click(function(){
			var myTr = $(this).closest("tr");
			myTr.remove();
			delete myItemPromotion[$(this).attr("value")];
		});
		//单选框监听
		$("input[name^='radio']").change(checkradiozk);
		$("#repeat_query").click(searchRepeatQuery);
		//上一步
		$("#back").click(function(e) {
			box.closeDialog({content: integralAddDialog});
//			onAddCXHD(consumerType);
			checkItemPromotin(integralAddDialog, true);
		});
		
		//点击复选框,设置默认折扣比例
//		$("#zkcxcheckbox").change(defaultmorenbili);//修改默认积分
		
		$("#jfcxcheckbox").change(function(){
			var jfCheckbox =  $("#jfcxcheckbox");
			if(jfCheckbox .is(":checked")){
				$("#jfcxmrzkbl").removeAttr("disabled");
			}else{
				$("#jfcxmrzkbl").attr("disabled","disabled");
			}
		});
		
		//下一步监听事件
		$("#integralsubmit").click(function(e){
			//判断是否有数据
			if($("input[name='promotionPoint']").length == 0){
				box.showAlert({message: "请添加积分商品！"});
				return ;
			}
			
			if(!$("#jfcxcheckbox").is(":checked") || !$("#jfcxmrzkbl").val()){
				var messageList = [];
				if(!$.validateForms(integralAddDialog, messageList)) {
					box.showAlert({message: messageList[0]});
					return;
				}
			}
			
//			box.closeDialog({content: integralAddDialog});
			backDialogs = "4";
			checkItemPromotin(integralAddDialog);
		});
	}
	
	//折扣促销
	function promotionAdd(){
		stradd ="promotion";
		
		promotionAddDialog = box.ich.view_cxhd_add_zkcx();
		promotionAddDialog = box.showDialog({
			title:showDialogTile+"折扣促销活动",
			//960,
			width:780,
			height:550,
			content:promotionAddDialog,
			close:function(e) {
				parentView.find("#add").removeAttr("disabled");
			},
			onkey: function(data) {
				switch(data.key) {
					case HOME.Keys.DELETE:
						break;
					case HOME.Keys.BARCODE:
						stradd ="promotion";
						searchMerchItem(data.code);
				}
			}
		});
		//将数据库读出来的折扣数据放到页面并放到map中（判断是否重复）
		$.each(itemInfo, function(key, value) {
			if($.parseMoney(parseFloat(value.cost) * parseFloat(value.unit_ratio)) != value.cost){
				value.cost = $.parseMoney(parseFloat(value.cost) * parseFloat(value.unit_ratio));
			}
			value.big_pri4 = $.parseMoney(value.big_pri4);
			$("#adding").append(box.ich.view_cxhd_item_row(value));
			var myItemInfo = {};
			if(value.discount){
				myItemInfo.discount = value.discount;
			}
			if(value.discount_price){
				myItemInfo.discount_price = value.discount_price;
			}
			if(value.point){
				myItemInfo.point = value.point;
			}
			myItemPromotion[key] = myItemInfo;
		});
		
		//删除商品
		promotionAddDialog.find("a[name='removeTr']").click(function(){
			var myTr = $(this).closest("tr");
			myTr.remove();
			delete myItemPromotion[$(this).attr("value")];
		});
		//判断单选框中是否优惠价格是否存在
		var pricelist = promotionAddDialog.find("input[name='promotionPrice']");
		$.each(pricelist, function(i,price) {
			if(pricelist[i].value.length != 0){
				var radioname = "radio"+pricelist[i].id.substr(2,pricelist[i].id.length);
				var inputzkblid = "bl"+pricelist[i].id.substr(2,pricelist[i].id.length);
				$("input[name^='"+radioname+"']:eq(1)").attr("checked","checked");
				$("#"+pricelist[i].id).removeAttr("disabled");
				$("#"+inputzkblid).attr("disabled","disabled");
			}
		});
		
		//单选框监听
		$("input[name^='radio']").change(checkradiozk);
		
//		$("#zkcxcheckbox").change(defaultmorenbili);//修改默认折扣
		
		$("#zkcxcheckbox").change(function(){
			var zkCheckbox =  $("#zkcxcheckbox");
			if(zkCheckbox.is(":checked")){
				$("#zkcxmrzkbl").removeAttr("disabled");
				$("#zkcxmrzkbl").attr("data-empty","false");
				$("input[name='promotionDesc']").attr("data-empty","true");
			}else{
				$("#zkcxmrzkbl").attr("disabled","disabled");
				$("#zkcxmrzkbl").attr("data-empty","true");
				$("input[name='promotionDesc']").attr("data-empty","false");
			}
		});
		
		//查询
		$("#repeat_query").click(searchRepeatQuery);
		//上一步按钮事件
		$("#back").click(function(e) {
			box.closeDialog({content: promotionAddDialog});
			checkItemPromotin(promotionAddDialog, true);
		});
		//监听商品类别
		currentItemType = $("#order_type");
		currentItemType.unbind("click").click(onChoiceItemButtonClick);
		//通过商品类别添加商品
		//$("#order_type").click(onChoiceItemButtonClick);
		//下一步监听事件
		$("#zkcxsubmit").click(function(e){
			//判断数据是否为空
			var trname = $("input[name='promotionDesc']").val();
			if(trname == undefined){
				box.showAlert({message: "请输入要打折的商品！"});
				return ;
			}
			if($("#zkcxcheckbox").is(":checked")){
				$("#zkcxmrzkbl").removeAttr("disabled");
				$("#zkcxmrzkbl").attr("data-empty","false");
				$("input[name='promotionDesc']").attr("data-empty","true");
			}
			var messageList = [];
			if(!$.validateForms(promotionAddDialog, messageList)) {
				box.showAlert({message: messageList[0]});
				return;
			}
			backDialogs = "1";
			checkItemPromotin(promotionAddDialog);
			
		});
	}
	//点击单选框
	function checkradiozk(e){
		var radiovalue = $(e.target).attr("value");
		var radioname = $(e.target).attr("name");
		var radiozkblid = "bl"+radioname.substr(5,radioname.length);
		var radiozkjgid = "jg"+radioname.substr(5,radioname.length);
		if(radiovalue == "0"){
			$("#"+radiozkblid).removeAttr("disabled");
			$("#"+radiozkblid).attr("data-empty","false");
			$("#"+radiozkjgid).attr("data-empty","true");
			$("#"+radiozkjgid).attr("disabled","disabled");
		}else{
			$("#"+radiozkjgid).removeAttr("disabled");
			$("#"+radiozkblid).attr("data-empty","true");
			$("#"+radiozkjgid).attr("data-empty","false");
			$("#"+radiozkblid).attr("disabled","disabled");
		}
	}
	
	//默认比例
	function defaultmorenbili(e){
		if($("#zkcxcheckbox").is(":checked")){
			$("#zkcxmrzkbl").removeAttr("disabled");
			//设置默认折扣文本框的监听
			$("#zkcxmrzkbl").blur(function (e){
				if($("#zkcxmrzkbl").val() != "" && $("#zkcxmrzkbl").val()>0){
					if(stradd == "promotion"){
						//折扣促销
//						$.each($("input[name='promotionDesc']"), function(i, zkbl) {
//							var radio0id = "radio0"+zkbl.id.substr(2,zkbl.id.length);
//							var radio1id = "radio1"+zkbl.id.substr(2,zkbl.id.length);
//							$("#"+radio0id).prop("checked","checked");
//							$("#"+radio1id).removeAttr("checked");
//							$("#"+zkbl.id).removeAttr("disabled");
//							var zkjgid = "jg"+zkbl.id.substr(2,zkbl.id.length);
//							$("#"+zkjgid).attr("disabled","disabled");
//							zkbl.value = $("#zkcxmrzkbl").val();
//						});
					}
					if(stradd == "integral"){
						//商品积分促销 
						$.each($("input[name='promotionPoint']"), function(i, zkbl) {
							zkbl.value = $("#zkcxmrzkbl").val();
						});
					}
				}
			});
		}else{
			$("#zkcxmrzkbl").attr("disabled","disabled");
		}
	}
	//查询商品
	function searchRepeatQuery(){
		var repeartItemBar = null;
		if(merch_promotion.promotion_type == "10"){
			repeartItemBar=promotionAddDialog.find("#repeat_item_bar");
		}else if(merch_promotion.promotion_type == "30"){
			repeartItemBar=integralAddDialog.find("#repeat_item_bar");
		}
		/*if(promotionAddDialog != null){
		repeartItemBar=promotionAddDialog.find("#repeat_item_bar");
		}else if(integralAddDialog != null){
			repeartItemBar=integralAddDialog.find("#repeat_item_bar");
		}*/
		if(repeartItemBar.val()){
			searchMerchItem(repeartItemBar.val());
		}else{
			box.showAlert({message: "请输入条码！"});
			return;
		}
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
	
	//销售单积分对话框
	function showOrderIntegralDialog(consumerGrderList){
		for ( var i = 0; i < consumerGrderList.length; i++) {
			var myConsumerText = myOraderIntegralPromotion[consumerGrderList[i].grade];
			if(!myConsumerText){
				myConsumerText = myOraderIntegralPromotion.all;
			}
			consumerGrderList[i].discount = myConsumerText;
		}
		orderIntegralDialog = box.ich.view_cxhd_order_integral({list:consumerGrderList});
		orderIntegralDialog = box.showDialog({
			title:showDialogTile+"销售单促销活动",
			width:780,
			height:550,
			content:orderIntegralDialog,
			close:function(e) {
			}
		});
//		上一步事件
		$("#orderIntegralBack").click(function(e) {
			box.closeDialog({content: orderIntegralDialog});
			onAddCXHD(consumerType);
		});
//		下一步监听事件
		$("#orderIntegralNext").click(checkConsumerOrderIntegral);
		
		$("#orderIntegralDefSwitch").change(function(){//默认框复选框
			if($("#orderIntegralDefSwitch").is(':checked')){//选中
				$("#orderIntegralDefTest").attr("disabled",false);
			}else{
				$("#orderIntegralDefTest").attr("disabled",true);
			}
		});
	}
	
	//检查会员销售单积分
	function checkConsumerOrderIntegral(){
		//myOraderIntegralPromotion
		if(!$("#orderIntegralDefSwitch").is(":checked") || !$("#orderIntegralDefTest").val()){
			var messageList = [];
			if(!$.validateForms(orderIntegralDialog, messageList)) {
				box.showAlert({message: messageList[0]});
				return;
			}
		}
		myOraderIntegralPromotion = {};
		var isDiscountAll = true;
		var firstConsumerPromoton = "";
		$("input[type='text'][name='order_integral']").each(function(){
			var consumerIntegralText = $(this).val(); 
			var defOrderIntegralText = $("#orderIntegralDefTest").val();
			if(!consumerIntegralText && $("#orderIntegralDefSwitch").is(':checked') && defOrderIntegralText ){
				consumerIntegralText = defOrderIntegralText;
			}
//			var ;
			if(!firstConsumerPromoton){
				firstConsumerPromoton = consumerIntegralText;
			}
			if(consumerIntegralText != firstConsumerPromoton){
				isDiscountAll = false;
			}
			var gradeId = $(this).attr("data-id");//级别
			myOraderIntegralPromotion[gradeId] = consumerIntegralText;
		});
		if(isDiscountAll){
			myOraderIntegralPromotion = {};
			myOraderIntegralPromotion["all"] = firstConsumerPromoton;
		}
		
//		backDialogs = "2";
		searchClashPromotion();
	}
	
	//会员折扣
	function vipPromotionAdd(consumerGrderList){
		for ( var i = 0; i < consumerGrderList.length; i++) {
			var myConsumerText = myConsumerPromotion[consumerGrderList[i].grade];
			if(!myConsumerText){
				myConsumerText = myConsumerPromotion.all;
			}
			consumerGrderList[i].discount = myConsumerText;
		}
		vippromotionAddDialog = box.ich.view_cxhd_vippromotion({list:consumerGrderList});
		vippromotionAddDialog = box.showDialog({
			title:showDialogTile+"会员折扣活动",
			width:780,
			height:550,
			content:vippromotionAddDialog,
			close:function(e) {
			}
		});
		//上一步事件
		$("#lastback").click(function(e) {
			box.closeDialog({content: vippromotionAddDialog});
			onAddCXHD(consumerType);
		});
		//下一步监听事件
		$("#hyzksubmit").click(checkConsumerPromotin);
		
		$("#consumerDefSwitch").change(function(){//默认框复选框
			if($("#consumerDefSwitch").is(':checked')){//选中
//				$("input[type='text'][name='hyzkvalue']").attr("disabled",true);
				$("#consumerDefPromotion").attr("disabled",false);
			}else{
//				$("input[type='text'][name='hyzkvalue']").attr("disabled",false);
				$("#consumerDefPromotion").attr("disabled",true);
			}
		});
//		$("#consumerDefPromotion").change(function(){//默认框
//			var defPromotion = $("#consumerDefPromotion").val();
//			if(defPromotion){
//				$("input[type='text'][name='hyzkvalue']").val(defPromotion);
//				myConsumerPromotion["all"] = defPromotion ; 
//			}
//		});
	}
	
	//会员折扣检查
	function checkConsumerPromotin(e){
		if(!$("#consumerDefSwitch").is(":checked") || !$("#consumerDefPromotion").val()){
			var messageList = [];
			if(!$.validateForms(vippromotionAddDialog, messageList)) {
				box.showAlert({message: messageList[0]});
				return;
			}
		}
		myConsumerPromotion = {};
		var isDiscountAll = true;
		var firstConsumerPromoton = "";
		$("input[type='text'][name='hyzkvalue']").each(function(){
			var everyConsumerPromotion = $(this).val(); 
			var defaultConsumerPromotion = $("#consumerDefPromotion").val();
			if(!everyConsumerPromotion && $("#consumerDefSwitch").is(':checked') && defaultConsumerPromotion ){
				everyConsumerPromotion = defaultConsumerPromotion;
			}
			
			if(!firstConsumerPromoton){
				firstConsumerPromoton = everyConsumerPromotion;
			}
			if(everyConsumerPromotion != firstConsumerPromoton){
				isDiscountAll = false;
			}
			var gradeId = $(this).attr("data-id");//级别
			myConsumerPromotion[gradeId] = everyConsumerPromotion;
		});
		if(isDiscountAll){
			myConsumerPromotion = {};
			myConsumerPromotion["all"] = firstConsumerPromoton;
		}
		
		backDialogs = "2";
		searchClashPromotion();
	}
	
	//通过seq_id得到商品信息
	function getItemInfo(){
		var newSeqId = "";
		for (key in merch_promotion.promotion_should.seq_id){
			newSeqId = key+","+newSeqId;
	    }
		params = {page_index:-1, page_size:-1, seq_id:newSeqId};
		box.request({
			url : box.getContextPath() + searchUnitJoinMerchItemUrl,
			data : {params:$.obj2str(params)},
			success :function(data){
				if(data && data.code == "0000"){
					for ( var i = 0; i < data.result.length; i++) {
						var seqId = data.result[i].seq_id;						
						if(merch_promotion.promotion_should.seq_id[seqId]){
							data.result[i].discount = merch_promotion.promotion_should.seq_id[seqId]["discount"];
							data.result[i].discount_price = merch_promotion.promotion_should.seq_id[seqId]["discount_price"];
							data.result[i].point = merch_promotion.promotion_should.seq_id[seqId]["point"];
							itemInfo[seqId] = data.result[i];
						}
					}
				}
			}
		});
	}
	
	//检查促销商品
	function checkItemPromotin(myDialog, isFallback){
		itemInfo = {};
		var itemTr = myDialog.find("tr[name='repeat_item_tr']");
		var defItemDiscount = myDialog.find("#zkcxmrzkbl").val();//默认商品折扣
		var defItemIntegral = myDialog.find("#jfcxmrzkbl").val();//默认商品积分
		
		$(itemTr).each(function(){
			var myItemInfo = {};
			var seqId = $(this).attr("data-seq-id");
			var itemId = $(this).attr("data-id");
			var itemBar = $(this).attr("data-big-bar");
			var itemName = $(this).attr("data-item-name");
			var unitName = $(this).attr("data-unit-name");
			var cost = $(this).attr("data-cost");
			var pri4 = $(this).attr("data-big-pri4");
			var unitRatio = $(this).attr("data-unit-ratio");
			var discount = $(this).find("input[type='text'][name='promotionDesc']").val();
			var promotionPrice = $(this).find("input[type='text'][name='promotionPrice']").val();
			var promotionPoint = $(this).find("input[type='text'][name='promotionPoint']").val();
			myItemInfo["item_id"] = itemId;
			myItemInfo["big_bar"] = itemBar;
			myItemInfo["item_name"] = itemName;
			myItemInfo["seq_id"] = seqId;
			myItemInfo["big_unit_name"] = unitName;
			myItemInfo["unit_ratio"] = unitRatio;
			myItemInfo["cost"] = cost;
			myItemInfo["big_pri4"] = pri4;
			if(!discount && !$(this).find("input[type='text'][name='promotionDesc']").attr("disabled")){
				discount = defItemDiscount;
			}else if(!promotionPrice && !$(this).find("input[type='text'][name='promotionPrice']").attr("disabled")){
				box.showAlert({message: "请完善商品折后价"});
			}
			if(discount && $(this).find("input[type='text'][name='promotionDesc']").attr("disabled")){
				discount = "";
			}
			if(promotionPrice && $(this).find("input[type='text'][name='promotionPrice']").attr("disabled")){
				promotionPrice = "";
			}
			
			if(!promotionPoint){
				promotionPoint = defItemIntegral;
			}
			myItemInfo["discount"] = discount;
			myItemInfo["discount_price"] = promotionPrice;
			myItemInfo["point"] = promotionPoint;
			itemInfo[seqId] = myItemInfo;
			var myItemInfo = {};
			//商品折扣（折扣、金额）取值
			if(promotionPrice && !$(this).find("input[type='text'][name='promotionPrice']").attr("disabled")){
				myItemInfo.discount_price = promotionPrice;
			}else if(discount && !$(this).find("input[type='text'][name='promotionDesc']").attr("disabled") ){
				myItemInfo.discount = discount;
			}else if(defItemDiscount ){
				myItemInfo.discount = defItemDiscount;
			}else if(merch_promotion.promotion_type == "10"){			
				box.showAlert({message: "请完善商品折扣"});
//				return;
			}
			
			//商品积分   取值
			if(promotionPoint){
				myItemInfo.point = promotionPoint;
			}else if(defItemIntegral){
				myItemInfo.point = defItemIntegral;
			}else if(merch_promotion.promotion_type == "30"){
				box.showAlert({message: "请完善商品积分"});
				return;
			}
			
			myItemPromotion[seqId] = myItemInfo;
		});
		
		if(isFallback){
			onAddCXHD(consumerType);
		}else{
			searchClashPromotion();
		}
	}
	//满减促销
	function fullReductionAdd(){
		var ceilingReductionResult = {};
		if(merch_promotion.promotion_action){
			if(merch_promotion.promotion_action.ceiling_reduction.ceiling && merch_promotion.promotion_action.ceiling_reduction.reduction){
				ceilingReductionResult.ceiling = merch_promotion.promotion_action.ceiling_reduction.ceiling;
				ceilingReductionResult.reduction = merch_promotion.promotion_action.ceiling_reduction.reduction;
			}
		}else if(myCeilingReduction["ceiling"] && myCeilingReduction["reduction"]){
			ceilingReductionResult.ceiling = myCeilingReduction["ceiling"];
			ceilingReductionResult.reduction = myCeilingReduction["reduction"];
		}
		fullReductionAddDialog = box.ich.view_cxhd_add_mjcx({crResult:ceilingReductionResult});
		fullReductionAddDialog = box.showDialog({
			title:showDialogTile+"满减促销活动",
			width:780,
			height:550,
			/*width:780,
			height:550,*/
			content:fullReductionAddDialog,
			close:function(e) {
			}
		});
		$("#back").click(function(e) {
			box.closeDialog({content: fullReductionAddDialog});
			onAddCXHD(consumerType);
		});
		//下一步
		$("#fullreductionsubmit").click(checkCeilingReduction);
	}
	//满减检查
	function checkCeilingReduction(e){
		var messageList = [];
		if(!$.validateForms(fullReductionAddDialog, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		myCeilingReduction = {};
		if( $("#xsdm").val() && $("#xsdj").val()){
			myCeilingReduction["ceiling"] = $("#xsdm").val();
			myCeilingReduction["reduction"] = $("#xsdj").val();
		}else{
			box.showAlert({message: "对不起，请检查满减数据！"});
			return;
		}
//		box.closeDialog({content: fullReductionAddDialog});
		backDialogs = "3";
		searchClashPromotion();
	}
	//显示冲突的促销
	function searchClashPromotion(){
		params.start_date = merch_promotion.start_date;
		params.end_date = merch_promotion.end_date;
		params.status = "1";
		if(merch_promotion.not_promotion_id){
			params.not_promotion_id = merch_promotion.not_promotion_id;
		}
		params.is_clash = "1";
		box.request({
			url:box.getContextPath()+ cxhdURL,
			data:{params:$.obj2str (params)},
			success:makeSureSubmit
		});
	}
	//处理促销信息
	function analyzePromotion(promotionMap){
		if(promotionMap){
			if(promotionMap.start_time.indexOf(":")<0){
				var startTime = promotionMap.start_time;
				promotionMap.start_time_text = startTime.substr(0, 2) +":"+ startTime.substr(2, 2);
				var endTime = promotionMap.end_time;
				promotionMap.end_time_text = endTime.substr(0, 2) +":"+ endTime.substr(2, 2);
			}else{
				var startTime = promotionMap.start_time;
				promotionMap.start_time_text = startTime;
				promotionMap.start_time = startTime.substr(0, 2) +""+ startTime.substr(3, 2);
				
				var endTime = promotionMap.end_time;
				promotionMap.end_time_text = endTime;
				promotionMap.end_time = endTime.substr(0, 2) +""+ endTime.substr(3, 2);
			}
			if(promotionMap.start_date.length==8){
				var startDate = promotionMap.start_date;
				startDate = new Date(startDate .substr(0, 4),startDate .substr(4, 2)-1,startDate .substr(6, 2));
				promotionMap.start_date_text = startDate.format("yyyy-MM-dd");
				var endDate = promotionMap.end_date;
				endDate = new Date(endDate.substr(0, 4),endDate.substr(4, 2)-1,endDate.substr(6, 2));
				promotionMap.end_date_text = endDate.format("yyyy-MM-dd");
			}
			else{
				var startDate = promotionMap.start_date;
				promotionMap.start_date_text = startDate ;
				startDate = new Date(startDate.substr(0, 4),startDate.substr(5, 2)-1,startDate.substr(8, 2));
				promotionMap.start_date = (startDate).format("yyyyMMdd");
				var endDate = promotionMap.end_date;
				promotionMap.end_date_text = endDate;
				endDate = new Date(endDate.substr(0, 4),endDate.substr(5, 2)-1,endDate.substr(8, 2));
				promotionMap.end_date = (endDate).format("yyyyMMdd");
			}
			
			//判断是否有效
			if(promotionMap.status==1){
//				var d = new Date();
//				var vYear = d.getFullYear();
//				var vMon = d.getMonth() + 1;
//				var vDay = d.getDate();
//				var h = d.getHours();
//				var m = d.getMinutes();
//				var s=vYear+""+(vMon<10 ? "0" + vMon : vMon) + (vDay<10 ? "0"+ vDay : vDay) + (h < 10 ? "0" + h : h) + (m < 10 ? "0" + m : m);
//				
				var s = $.GetDateFormat($.GetToday(),"yyyy-mm-dd", "yyyymmdd" )+""+ $.GetCurrentTime();
				if(s > promotionMap.end_date + "" + promotionMap.end_time + "00"){
					promotionMap.status_text = "过期";
				}else{
					promotionMap.status_text = "有效";
				}
			}else{
				promotionMap.status_text = "无效";
			}
			//判断是否共存
			if(promotionMap.is_coexistent==1){
				promotionMap.is_coexistent_text = "是";
			}else{
				promotionMap.is_coexistent_text = "否";
			}
			promotionMap.promotion_type_text = box.labels.promotiontype[promotionMap.promotion_type];
			if (promotionMap.is_insistent && promotionMap.is_insistent.length>=12) {
				promotionMap.is_insistent = promotionMap.is_insistent.substring(0,1);
			}
			//判断冲突后是否退让
			if(promotionMap.is_insistent==1){
				//与其他活动冲突取消其他活动
				promotionMap.is_insistent_text = "是";
			}else{
				//与其他活动冲突同时生效
				promotionMap.is_insistent_text = "否";
			}
			//判断针对群体
			if(promotionMap.promotion_must.target.all == "true"){
					promotionMap.promotion_must_text = "所有顾客";
			}else if(promotionMap.promotion_must.target.all_member == "true"){
				promotionMap.promotion_must_text = "全部会员";
			}else{
				promotionMap.promotion_must_text = "部分会员";
			}
		}
		return promotionMap;
	}
	
	//确认发布促销
	function makeSureSubmit(data){
		//发送url,列出冲突的页面
		var promotionList = [];
		if(data && data.code=="0000"){
			for(var i=0; i<data.result.length; i++) {
				var s = $.GetDateFormat($.GetToday(),"yyyy-mm-dd", "yyyymmdd" )+""+ $.GetCurrentTime();
				if(s <= data.result[i].end_date + "" + data.result[i].end_time + "00"){
					promotionList.push(analyzePromotion(data.result[i]))  ;
				}
			}
		}
		//设置标题长度
		if(merch_promotion.promotion_desc.length>=5){
			merch_promotion.promotion_desc_text = merch_promotion.promotion_desc.substring(0,12)+"...";
		}else{
			merch_promotion.promotion_desc_text = merch_promotion.promotion_desc;
		}
		for(var j=0;j<promotionList.length;j++){
			if(promotionList[j].promotion_desc.length>=5){
				promotionList[j].promotion_desc_text = promotionList[j].promotion_desc.substring(0,12)+"...";
			}else{
				promotionList[j].promotion_desc_text = promotionList[j].promotion_desc;
			}
		}
		//画确认发布促销界面
		merch_promotion.promotion_type_text = box.labels.promotiontype[merch_promotion.promotion_type];
		makeSureSubmitDialog = box.ich.view_cxhd_makeSureSubmit({clash_promotion:promotionList,my_promotion:merch_promotion});
		makeSureSubmitDialog = box.showDialog({
			title:"确认发布促销活动",
			width:780,
			height:550,
			content:makeSureSubmitDialog,
			close:function(e) {
				parentView.find("#add").removeAttr("disabled");
			}
		});
		var tableViewContainer = makeSureSubmitDialog.find("#consumer_container1");
		makeSureSubmitDialog.find("#consumer_container1 .table_view").fixedtableheader({
			parent: tableViewContainer,
			win: tableViewContainer,
			isshow: true,
			offset: tableViewContainer.offset
		});
		
		if(!merch_promotion.is_coexistent ){
			merch_promotion.is_coexistent = "0";
		}
		if(!merch_promotion.is_insistent ){
			merch_promotion.is_insistent = "1";
		}
		if(merch_promotion.promotion_type == "40"){
			//满减
			box.closeDialog({content: fullReductionAddDialog});
		}else if(merch_promotion.promotion_type == "10"){
			//折扣
			box.closeDialog({content: promotionAddDialog});
		}else if(merch_promotion.promotion_type == "13") {
			//会员
			box.closeDialog({content: vippromotionAddDialog});
		}else if(merch_promotion.promotion_type == "30"){
			//积分
			box.closeDialog({content: integralAddDialog});
		}else if(merch_promotion.promotion_type == "33"){
			box.closeDialog({content: orderIntegralDialog});
		}
		
		if(updateOrAdd == "update"){
			$("#lastsubmit").val("修 改");
		}
		//判断is_coexistent是否选中
		if(merch_promotion.is_coexistent == "1"){
			$("#is_coexistent").attr("checked","checked");
		}
		if(merch_promotion.is_insistent == "1"){
			$("#is_insistent").attr("checked","checked");
		}else{
			$("#is_insistent").attr("checked",false);
		}
		//策略帮助提示
		$(".helpImg").mousemove(function(){
            $(".helpContent").show();
		});
		$(".helpImg").mouseleave(function(e){
	          $(".helpContent").hide();
		});
		
		makeSureSubmitDialog.find("#lastsubmit").unbind("click").click(submitPromotion);
		//添加上一步监听
		$("#lastback").click(function(e){
			box.closeDialog({content: makeSureSubmitDialog});
			if(backDialogs == "1"){
				//折扣促销
				promotionAdd();
			}else if(backDialogs == "2"){
				//会员促销
				vipPromotionAdd(newConsumerGradeResult);
			}else if(backDialogs == "3"){
				//满减促销
				fullReductionAdd();
			}else if(backDialogs == "4"){
				//积分促销
				integralAdd();
			}else if(merch_promotion.promotion_type == "33"){
				showOrderIntegralDialog(newConsumerGradeResult);
			}
			else{
				pageIndex = param.page_index;
				pageSize = param.page_size;
				var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
				beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
				var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
				endTimeFormat=endTimeFormat.format("yyyyMMdd");
				var params = {page_index: pageIndex, page_size: pageSize};
				params.start_date = beginTimeFormat ;
				params.end_date = endTimeFormat;
				getPromotionJson(params);
			}
		});
		//当前促销是否与其他促销共存     0：不共存   1：共存"chek"
		$("#is_coexistent").change(function(){
			if($(this).is(':checked')){
				merch_promotion.is_coexistent = "1";
			}else{
				merch_promotion.is_coexistent = "0";
			}
		});
		//冲突后是否取消其他促销   0:坚持"chek"  1：退让
		$("#is_insistent").change(function(){
			if($(this).is(':checked')){
				merch_promotion.is_insistent= "1";
			}else{
				merch_promotion.is_insistent = "0";
			}
		});
	}
	//提交
	function submitPromotion(){
		//关闭窗口
		box.closeDialog({content:makeSureSubmitDialog});
		if(merch_promotion.promotion_type == "40"){
			//满减
			var promotion_action = {};
			promotion_action.ceiling_reduction = myCeilingReduction;
			merch_promotion.promotion_action = promotion_action;
			merch_promotion.promotion_should = {};
		}else if(merch_promotion.promotion_type == "10" || merch_promotion.promotion_type == "30"){
			//折扣
			var promotion_should = {};
			promotion_should.seq_id = myItemPromotion;
			merch_promotion.promotion_should = promotion_should;
			merch_promotion.promotion_action = {};
		}else if(merch_promotion.promotion_type == "13") {
			//会员
			var promotion_action = {} ;
			promotion_action.discount = myConsumerPromotion;
			merch_promotion.promotion_action = promotion_action;
			merch_promotion.promotion_should = {};
		}else if(merch_promotion.promotion_type == "33") {
			//销售单会员积分
			var promotion_action = {} ;
			promotion_action.point = myOraderIntegralPromotion;
			merch_promotion.promotion_action = promotion_action;
			merch_promotion.promotion_should = {};
		}
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		var params = {page_index: pageIndex, page_size: pageSize};
		params.start_date = beginTimeFormat ;
		params.end_date = endTimeFormat;
		params.is_clash = "1";
		var finalURL = "";
		if(updateOrAdd == "update"){
			finalURL = updateCXHD;
		}else{
			finalURL = insertPromotionUrl;
		}
		box.request({
			url:box.getContextPath()+ finalURL,
			data:{params:$.obj2str (merch_promotion)},
			success:function(){
				getPromotionJson(params);
			}
		});
	}
	//扫码
	function searchMerchItem(itemBar) {
		params = {page_index:-1, page_size:-1, big_bar:itemBar};
		var textItemBar = null;
		/*if(promotionAddDialog != null){
			textItemBar=promotionAddDialog.find("#repeat_item_bar");
		}else if(integralAddDialog != null){
			textItemBar=integralAddDialog.find("#repeat_item_bar");
		}*/
		if(merch_promotion.promotion_type == "10"){
			textItemBar=promotionAddDialog.find("#repeat_item_bar");
		}else if(merch_promotion.promotion_type == "30"){
			textItemBar=integralAddDialog.find("#repeat_item_bar");
		}
		textItemBar.val(itemBar);
		textItemBar.blur();
			box.request({
				url : box.getContextPath() + searchUnitJoinMerchItemUrl,
				data : {params:$.obj2str(params)},
				success : function(json) {
					if(json && json.code=="0000") {
						var itemList = json.result;
						//$.unique(itemList)
						$.each(itemList, function(i, item) {
							if(box.kinds[item.item_kind_id]) {
								item.item_kind_name = box.kinds[item.item_kind_id];
							} else {
								item.item_kind_name = item.item_kind_id;
							}
						});
						//判断是否重码
						if(itemList.length==1) {
							addItem(itemList[0]);
						} else if(itemList.length == 0){
							//没有此商品,请在商品管理中添加该商品！
							box.showAlert({message: "查无此商品"});
							return ;
						}else {
							for ( var i = 0; i < itemList.length; i++) {
								itemList[i].big_pri4 = $.parseMoney(itemList[i].big_pri4 );
							}
							selectItemDialog = box.ich.view_cxhd_select_item_dialog({list: itemList});
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
							selectItemDialog.find("tr").click(onChoiceTrClick(itemList));
							
						}
					} else {
						box.showAlert({message: json.msg});
					}
				}
			});
	    	//删除监听
			if(stradd == "promotion"){
				promotionAddDialog.click(function (e){
					var source = $(e.target);
					if(source.attr("name")=="removeTr") {
						var sourceTr = source.closest("tr");
						delete myItemPromotion[source.attr("value")];
						sourceTr.remove();
//						item_barList.splice($.inArray(source.attr("value"),item_barList),1);
					}
				});
			}else if(stradd == "integral"){
				//integralAddDialog != null
				integralAddDialog.click(function (e){
					var source = $(e.target);
					if(source.attr("name")=="removeTr") {
						var sourceTr = source.closest("tr");
						delete myItemPromotion[source.attr("value")];
						sourceTr.remove();
					}
				});
			}
	    	
	}
	function onTrClick(e){
		var source = $(e.target);
		if(source.attr("name")=="removeTr") {
			var sourceTr = source.closest("tr");
			delete myItemPromotion[source.attr("value")];
			sourceTr.remove();
		}
	}
	//重复商品选择
	function onChoiceTrClick(itemList){
		if(itemList == undefined||itemList.length == 0){
			return ;
		}
		return function(e) {
			var seqId = $(e.currentTarget).attr("data-id");
			for(var i=0; i<itemList.length; i++) {
				if(itemList[i].seq_id == seqId) {
					selectItemDialog.find("tr").unbind("click");
					box.closeDialog({content:selectItemDialog});
					selectItemDialog = null;
					addItem(itemList[i]);
				}
			}
		};
	}
	//添加商品
	function addItem(item) {
		if(item.discount){
			item.discount = "";
		}
		if(item.status != "1"){
			box.showAlert({message: "此商品已经下架！"});
			return ;
		}
		if(stradd=="" || stradd==null){
			return ;
		}
		item.cost = $.parseMoney(parseFloat(item.cost) * parseFloat(item.unit_ratio));
		item.big_pri4 = $.parseMoney(item.big_pri4);
		if(myItemPromotion[item.seq_id]){
			box.showAlert({message: "对不起，已有此商品！"});
			return;
		}else{
			var myItemInfo = {};
			myItemPromotion[item.seq_id] = myItemInfo;
			itemInfo[item.seq_id] = item;
		}
		if(stradd=="promotion"){
			$("#adding").append(box.ich.view_cxhd_item_row(item));
			//单选框监听
			$("input[name^='radio']").change(checkradiozk);
			$("input[type='text'][name='promotionDesc'").change(function(){
				var trSeqId = $(this).attr("data-seq-id");
				var myItemInfo = {};
				myItemInfo.discount = $(this).val();
				myItemPromotion[trSeqId] = myItemInfo;
			});
			$("input[type='text'][name='promotionPrice'").change(function(){
				var trSeqId = $(this).attr("data-seq-id");
				var myItemInfo = {};
				myItemInfo.discount_price = $(this).val();
				myItemPromotion[trSeqId] = myItemInfo;
			});
		}else if(stradd=="integral"){
			$("#adding").append(box.ich.view_integral_item_row(item));
			$("input[type='text'][name='promotionPoint'").change(function(){
				var trSeqId = $(this).attr("data-seq-id");
				var myItemInfo = {};
				myItemInfo.point = $(this).val();
				myItemPromotion[trSeqId] = myItemInfo;
			});
		}
		//添加删除监听
		$(".table_view tbody tr").unbind("click").click(onTrClick);
	}
	
	function onClose(){
		beginTime = null; 
		endTime = null;
	}
	return {
		init: function(){
			box.listen("cxhd", onload);
			box.listen("cxhd_onKey",onKey);
			box.listen("cxhd_close", onClose);
		},
		destroy: function() { }
	};
});