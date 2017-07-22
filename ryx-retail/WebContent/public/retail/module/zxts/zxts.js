/**
 * 咨询投诉功能
 */
HOME.Core.register("plugin-zxts", function(box) {
	
	var listUrl = "retail/infointer/getInfointerList";
	var detailUrl = "retail/infointer/getInfointerDetail";
	var submitUrl = "retail/infointer/submitInfointer";
	
	var parentView = null;
	var infos = null;
	
	var content = null;
	
	function loadZXTS(view) {
		parentView = view;
		getInfointerList();
	}
	
	function getInfointerList() {
		box.request({
			url: box.getContextPath() + listUrl,
			success: onGetInfointerList
		});
	}

	function onGetInfointerList(data) {
		if(data && data.code == "0000") {
			for(var i=0;i<data.result.length;i++){
				//日期格式化为yyyy-MM-dd
				var subDate=new Date(data.result[i].submit_date.substr(0, 4),data.result[i].submit_date.substr(4, 2)-1,data.result[i].submit_date.substr(6, 2));
				data.result[i].submit_date=subDate.format("yyyy-MM-dd");
				data.result[i].index=i+1;
				var yhf = "";
				if(data.result[i].status == '1' && data.result[i].vfy_desc){
					yhf="[已回复]";
				}
				data.result[i].yhf = yhf;
			}
			showInfointerList(data.result);
		}
	}	
	function showInfointerList(result) {
		infos = result;
		
		var content = box.ich.view_yxhd_zxts({list: result});
		parentView.empty().append(content);
		//for(var i=0;i<yhfImg.length;i++){
		//	$("."+yhfImg[i]).show();
			//$("."+yhfImg[i]).attr("src","../public/retail/img/yhy.png");
		//}
		parentView.find("table tbody tr").unbind("click", getInfointerDetail).click(getInfointerDetail);
		
		parentView.find("#forInsert").unbind("click", forInsert).click(forInsert);
		
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		parentView.showFooter(true);
	}
	
	function forInsert(){
		content = box.ich.view_yxhd_zxts_insert();
		//parentView.empty().append(content);
		//parentView.find("#submit").unbind("click", submitInfointer).click(submitInfointer);
		content = box.showDialog({
			title: "我要咨询",
			width: 560,
			height: 450,
            modal: true,
			content: content
		});
		content.find("#submit").unbind("click", submitInfointer).click(submitInfointer);

		$("#telephone").val(box.user.telephone);
		$("#userName").val(box.user.manager);
	}
	
	function submitInfointer(){
		var infoTitle = $("#infoTitle").val();
		var infoType =$('input:radio[name="infoType"]:checked').val();
		var infoDesc = $("#infoDesc").val();
		var telephone = $("#telephone").val();
		var userName = $("#userName").val();
		
		if(infoTitle=="请输入标题"||infoTitle==""||infoTitle==null||telephone=="请输入手机号或邮箱"||telephone==""||telephone==null||userName=="请输入您的姓名"||userName==""||userName==null){
			box.showAlert({message:"提交失败，请完整填写各项内容！"});
		}else {
			var obj = {infoTitle: infoTitle,infoType:infoType,infoDesc:infoDesc,telephone:telephone,userName:userName};
			box.request({
				url: box.getContextPath() + submitUrl,
				data: {params: $.obj2str(obj)},
				success: submitisOk
			});
		}
	}
	
	function submitisOk(){
		box.showAlert({message:"提交成功！"});
		box.closeDialog({content:content});
		getInfointerList();
	}
	
	function getInfointerDetail(e){
		var $target = $(e.currentTarget);
		var infoId = $target.attr("data-id");
		
		if(infos) {
			for(var i=0; i<infos.length; i++) {
				if(infos[i].info_id == infoId) {
					showInfointerDetail(infos[i]);
					return;
				}
			}
		}
		
		var obj = {infoId: infoId};
		box.request({
			url: box.getContextPath() + detailUrl,
			data: {params: $.obj2str(obj)},
			success: function(data) {
				showInfointerDetail(data.result);
			}
		});
	}
	
	function showInfointerDetail(data){
		var content = box.ich.view_yxhd_zxts_detail(data);
		//parentView.empty().append(content);
		content = box.showDialog({
			title: "咨询/投诉",
			width: 800,
			height: 560,
            modal: true,
			content: content
		});
	}
	
	return {
		init: function(){
			box.listen("zxts", loadZXTS);
		},
		destroy: function() { }
	};
});
