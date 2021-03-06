/**
 * 公告信息功能
 */
HOME.Core.register("plugin-ggxx", function(box) {
	
	//获得系统公告；（notice_type：01、系统公告。02：货源信息。03:1532非烟商品）
	var actListUrl = "retail/infointer/getActivityList";
	//营销活动
	var retailActList = "retail/activity/getRetailActList";
	
	var parentView = null;
	
	var eventInfo = null;
	
	var content = null;
	
	
	var sysList=new Array();
	var actList=new Array();
	var feiyanList=new Array();
	
	function loadGGXX(view) {
		parentView = view;
		parentView.empty().append(box.ich.view_yxhd_ggxx_fix());
		pageIndex = 1;
		pageIndex_act = 1;
		pageSize = 20;
		parentView.showFooter(true);
		onSysInfoClick();
	}
	
	//营销活动-请求列表
	function getMarketingList(){
		box.request({
			url: box.getContextPath() + retailActList,
			success: onGetMarketingList
		});
	}
	
	//营销活动-组织列表数据
	function onGetMarketingList(data) {
		parentView.find("#view_yxhd_ggxx_fix").next().remove();
		if(data && data.code == "0000") {
			
			for(var i=0;i<data.result.length;i++){
				// 暂时屏蔽掉砸金蛋活动
				if(data.result[i].template_path == "zajindan-lsh") {
					data.result.splice(i, 1);
					i--;
					continue;
				}
				
				var readFlag = data.result[i].read_flag;
				if("0"==readFlag){
					data.result[i].flag=true;
				}else{
					data.result[i].flag=false;
				}

				var starttime = data.result[i].start_time;
				var endtime = data.result[i].end_time;
				if(!isNaN(starttime.substr(6))){
					var year = starttime.substr(0,4);
					var month = starttime.substr(4,2);
					var day = starttime.substr(6);
					data.result[i].start_time = year+"年"+month+"月"+day+"日";
				}
				if(!isNaN(endtime.substr(6))){
					var year = endtime.substr(0,4);
					var month = endtime.substr(4,2);
					var day = endtime.substr(6);
					data.result[i].end_time = year+"年"+month+"月"+day+"日";
				}
				data.result[i].imgSrc = box.getResourcePath()+"activities/acts/"+data.result[i].template_path+"/pc-logo.png";
			}
			showMarketingList(data.result);
		}
	}
	
	//营销活动-显示列表
	function showMarketingList(result){
		parentView.find("#marketing_info").unbind("click").click(onMarInfoClick);
		parentView.find("#sys_info").unbind("click").click(onSysInfoClick);//onSysInfoClick
		parentView.find("#act_info").unbind("click").click(onActInfoClick);//onActInfoClick
		parentView.find("#feiyan_info").unbind("click").click(onFeiyanInfoClick);//onFeiyanInfoClick
		eventInfo = result;
		
		var content = box.ich.view_yxhd_yxhd({list: result});
		parentView.find("#notice").empty().append(content);
		parentView.find("#view_yxhd_ggxx_fix").next().remove();
		
		parentView.hidePager();
		
		parentView.find("#view_yxhd_yxhd a").unbind("click", showEventDetail).click(showEventDetail);
		parentView.find("#view_yxhd_yxhd input").unbind("click", showEventDetail).click(showEventDetail);
	}


	//营销活动
	function onMarInfoClick(e){
		parentView.find("#left_view ul li").removeClass("selected");
		parentView.find("#marketing_info").addClass("selected");
		parentView.find("#notice").empty();
		
		params = {page_index: pageIndex, page_size: pageSize};
		
		getMarketingList();
	}
	
	//营销活动--显示详情
	function showEventDetail(e){
		var $target = $(e.currentTarget);
		var act_id = $target.attr("data-id");
		if(eventInfo) {
			for(var i=0; i<eventInfo.length; i++) {
				if(eventInfo[i].act_id == act_id) {//box.getResourcePath()  "http://192.168.0.3:8889/resource/"
					box.controls.showIFrame({title: eventInfo[i].act_title, 
						url: box.getResourcePath()+"activities/acts/"+eventInfo[i].template_path+"/index.html?act_id="+act_id+"&com_id="+eventInfo[i].com_id+"&cust_id=" + box.user.merch_id});
				}
			}
		}
	}

	function showNoticeList(result) {
		parentView.find("#marketing_info").unbind("click").click(onMarInfoClick);
		parentView.find("#sys_info").unbind("click").click(onSysInfoClick);//系统公告
		parentView.find("#act_info").unbind("click").click(onActInfoClick);//货源信息
		parentView.find("#feiyan_info").unbind("click").click(onFeiyanInfoClick);//1532非烟商品专栏
		
		notices = result;
		var content = box.ich.view_yxhd_ggxx({list: result});
		parentView.find("#notice").empty().append(content);
		parentView.find("#view_yxhd_ggxx_fix").next().remove();
		
		parentView.find("table tbody tr").unbind("click", getActDetail).click(getNoticeDetail);

		parentView.find(".table_view").fixedtableheader({
			parent: parentView.find("#notice"),
			win: parentView.parent(),
			isshow: true 
		});
	}
	
	//系统公告
	function onSysInfoClick(event){
		parentView.find("#left_view ul li").removeClass("selected");
		parentView.find("#sys_info").addClass("selected");
		parentView.find("#notice").empty();
		params = {page_index: pageIndex, page_size: pageSize, notice_type:'01'};
		box.request({
			url: box.getContextPath() + actListUrl,
			data:params,
			success: onGetSysList
		});
	}
	
	//显示系统公告
	function onGetSysList(data) {
		sysList = [];
		if(data && data.code == "0000") {
			var j=1;
			for(var i=0;i<data.result.length;i++){
				//日期时间格式化为yyyy-MM-dd
				var crtDate=new Date(data.result[i].crt_date.substr(0, 4),data.result[i].crt_date.substr(4, 2)-1,data.result[i].crt_date.substr(6, 2));
				data.result[i].crt_date=crtDate.format("yyyy-MM-dd");
				
				if(data.result[i].notice_type=="01"){
					data.result[i].index=j;
					j++;
					var readFlag = data.result[i].read_flag;
					if("1"==readFlag){
						data.result[i].flag="已读";//true;
					}else{
						data.result[i].flag="未读";//false
					}
					// 暂时将所有的设为已读
					data.result[i].flag="已读";
					sysList.push(data.result[i]);
				}
			}
			showNoticeList(data.result);
		}
	}
	
	//货源信息
	function onActInfoClick(event){
		parentView.find("#left_view ul li").removeClass("selected");
		parentView.find("#act_info").addClass("selected");
		parentView.find("#notice").empty();
		var params = {notice_type:'02'};
		box.request({
			url: box.getContextPath() + actListUrl,
			data:params,
			success: onGetActList
		});
	}
	
	//显示货源信息
	function onGetActList(data) {
		actList=[];
		if(data && data.code == "0000") {
			var j=1;
			for(var i=0;i<data.result.length;i++){
				
				//日期时间格式化为yyyy-MM-dd
				var crtDate=new Date(data.result[i].crt_date.substr(0, 4),data.result[i].crt_date.substr(4, 2)-1,data.result[i].crt_date.substr(6, 2));
				data.result[i].crt_date=crtDate.format("yyyy-MM-dd");
								
				if(data.result[i].notice_type=="02"){
					data.result[i].index=j;
					j++;
					var readFlag = data.result[i].read_flag;
					if("1"==readFlag){
						data.result[i].flag="已读";//true;
					}else{
						data.result[i].flag="未读";//false
					}
					// 暂时设为已读
					data.result[i].flag = "已读";
					actList.push(data.result[i]);
				}
			}
			
			showActList(data.result);
		}
	}
	
	//1532非烟商品专栏
	function onFeiyanInfoClick(event){
		
		parentView.find("#left_view ul li").removeClass("selected");
		parentView.find("#feiyan_info").addClass("selected");
		parentView.find("#notice").empty();
		var params = {notice_type:'03'};
		box.request({
			url: box.getContextPath() + actListUrl,
			data:params,
			success: onGetFeiyanList
		});
	}
	
	//显示1532非烟
	function onGetFeiyanList(data) {
		feiyanList = [];
		if(data && data.code == "0000") {
			var j=1;
			for(var i=0;i<data.result.length;i++){
				//日期时间格式化为yyyy-MM-dd
				var crtDate=new Date(data.result[i].crt_date.substr(0, 4),data.result[i].crt_date.substr(4, 2)-1,data.result[i].crt_date.substr(6, 2));
				data.result[i].crt_date=crtDate.format("yyyy-MM-dd");
				if(data.result[i].notice_type=="03"){
					data.result[i].index=j;
					j++;
					var readFlag = data.result[i].read_flag;
					if("1"==readFlag){
						data.result[i].flag="已读";//true;
					}else{
						data.result[i].flag="未读";//false
					}
					// 暂时设为已读
					data.result[i].flag = "已读";
					feiyanList.push(data.result[i]);
				}
			}
			
			showFeiyanList(data.result);
		}
	}
	
	function showActList(result) {
		parentView.find("#marketing_info").unbind("click").click(onMarInfoClick);
		parentView.find("#sys_info").unbind("click").click(onSysInfoClick);//onSysInfoClick
		parentView.find("#act_info").unbind("click").click(onActInfoClick);//onActInfoClick
		parentView.find("#feiyan_info").unbind("click").click(onFeiyanInfoClick);//onFeiyanInfoClick
		
		supplyInfo = result;
		var content = box.ich.view_yxhd_ggxx({list: result});
		parentView.find("#notice").empty().append(content);
		parentView.find("#view_yxhd_ggxx_fix").next().remove();
		
		parentView.find("table tbody tr").unbind("click", getActDetail).click(getActDetail);

		parentView.find(".table_view").fixedtableheader({
			parent: parentView.find("#notice"),
			win: parentView.parent(),
			isshow: true 
		});
	}
	
	function getNoticeDetail(e){
		var $target = $(e.currentTarget);
		var noticeId = $target.attr("data-id");
		var indexId = $target.attr("indexId");
		for(var i=0;i<sysList.length;i++){
			if(sysList[i].index==indexId){
				if(sysList.length>1){
					sysList[i].notOnlyOne=function(){return "true";};
				}
				sysList[i].up_id=parseInt(indexId)-1;
				sysList[i].next_id=parseInt(indexId)+1;
				showActDetail(sysList[i]);
				break;
			}
		}
	}
	
	
	function getActDetail(e){
		var $target = $(e.currentTarget);
		var noticeId = $target.attr("data-id");
		var indexId = $target.attr("indexId");
		for(var i=0;i<actList.length;i++){
			if(actList[i].index==indexId){
				if(actList.length>1){
					actList[i].notOnlyOne=function(){return "true";};
				}
				actList[i].up_id=parseInt(indexId)-1;
				actList[i].next_id=parseInt(indexId)+1;
				showActDetail(actList[i]);
				break;
			}
		}
		
	}
	
	
	
	function showFeiyanList(result) {
		parentView.find("#marketing_info").unbind("click").click(onMarInfoClick);
		parentView.find("#sys_info").unbind("click").click(onSysInfoClick);//onSysInfoClick
		parentView.find("#act_info").unbind("click").click(onActInfoClick);//onActInfoClick
		parentView.find("#feiyan_info").unbind("click").click(onFeiyanInfoClick);//onFeiyanInfoClick
		
		feiyanInfo = result;
		var content = box.ich.view_yxhd_ggxx({list: result});
		parentView.find("#notice").empty().append(content);
		parentView.find("#view_yxhd_ggxx_fix").next().remove();
		
		parentView.find("table tbody tr").unbind("click", getActDetail).click(getFeiyanDetail);

		parentView.find(".table_view").fixedtableheader({
			parent: parentView.find("#notice"),
			win: parentView.parent(),
			isshow: true 
		});
	}
	
	
	
	function getFeiyanDetail(e){
		var $target = $(e.currentTarget);
		var noticeId = $target.attr("data-id");
		var indexId = $target.attr("indexId");
		for(var i=0;i<feiyanList.length;i++){
			if(feiyanList[i].index==indexId){
				if(feiyanList.length>1){
					feiyanList[i].notOnlyOne=function(){return "true";};
				}
				feiyanList[i].up_id=parseInt(indexId)-1;
				feiyanList[i].next_id=parseInt(indexId)+1;
				showActDetail(feiyanList[i]);
				break;
			}
		}
		
	}
	
	function thisReadFlag(dataFlag){
		for(var i=0;i<actList.length;i++){
			if(actList[i].notice_id==dataFlag.notice_id){
				actList[i].read_flag=1;
			}
		}
		for(var i=0;i<sysList.length;i++){
			if(sysList[i].notice_id==dataFlag.notice_id){
				sysList[i].read_flag=1;
			}
		}
	}
	
	function showActDetail(data){
		if(content) {
			box.closeDialog({content: content});
		}
		content = box.ich.view_yxhd_hyxx_detail(data);
		box.showDialog({
			title: data.notice_title,
			width: 800,
			height: 580,
            modal: true,
			content: content
		});
		content.find("#upButton").unbind("click", tiaoDetail).click(tiaoDetail);
		content.find("#nextButton").unbind("click", tiaoDetail).click(tiaoDetail);
		content.find("#closeButton").unbind("click", closeContent).click(closeContent);
		
	}
	
	function tiaoDetail(e){
		var $target = $(e.currentTarget);
		var dataType = $target.attr("data-type");
		var dataInid = $target.attr("data-inid");
		var noticeId=null;
		
		
		if(dataType=="02"){
			if(dataInid<1){
				dataInid=actList.length;
			}
			if(dataInid>actList.length){
				dataInid=1;
			}
			for(var i=0;i<actList.length;i++){
				if(actList[i].index==dataInid){
					actList[i].up_id=parseInt(dataInid)-1;
					actList[i].next_id=parseInt(dataInid)+1;
					if(actList.length>1){
						actList[i].notOnlyOne=function(){return "true";};
					}
					showActDetail(actList[i]);
					break;
				}
			}
		}else if(dataType=="03"){
			if(dataInid<1){
				dataInid=feiyanList.length;
			}
			if(dataInid>feiyanList.length){
				dataInid=1;
			}
			for(var i=0;i<feiyanList.length;i++){
				if(feiyanList[i].index==dataInid){
					feiyanList[i].up_id=parseInt(dataInid)-1;
					feiyanList[i].next_id=parseInt(dataInid)+1;
					if(feiyanList.length>1){
						feiyanList[i].notOnlyOne=function(){return "true";};
					}
					showActDetail(feiyanList[i]);
					break;
				}
			}
		}else{
			if(dataInid<1){
				dataInid=sysList.length;
			}
			if(dataInid>sysList.length){
				dataInid=1;
			}
			for(var i=0;i<sysList.length;i++){
				if(sysList[i].index==dataInid){
					sysList[i].up_id=parseInt(dataInid)-1;
					sysList[i].next_id=parseInt(dataInid)+1;
					if(sysList.length>1){
						sysList[i].notOnlyOne=function(){return "true";};
					}
					showActDetail(sysList[i]);
				}
			}
		}
		
	}
	function closeContent(){
		box.closeDialog({content:content});
	}
	
	return {
		init: function(){
			box.listen("ggxx", loadGGXX);
		},
		destroy: function() { }
	};
});