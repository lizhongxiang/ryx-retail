/**
 * 问卷调查功能
 */
HOME.Core.register("plugin-wjdc", function(box) {
	
	var listUrl = "retail/activity/getSurveyList";
	var detailUrl = "retail/activity/getSurveyDetail";
	var submitUrl = "retail/activity/submitSurvey";
	var parentView = null;
	var actId = null;
	
	var pageIndex = 1;
	var pageSize = 20;
	
	var currentAct = null;
	var acts = null;
	
	function loadWJDC(view) {
		parentView = view;
		pageIndex = 1;
		pageSize = 20;
		getSurveyList();
	}
	
	function getSurveyList() {
		box.request({
			url: box.getContextPath() + listUrl,
			success: onGetSurveyList
		});
	}
	
	function onGetSurveyList(data) {
		if(data && data.code == "0000") {
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1;
			}
			showSurveyList(data.result);
		}
	}
	function showSurveyList(result) {
		acts = result;
		var content = box.ich.view_yxhd_wjdc({list: result});
		parentView.empty().append(content);
		
		parentView.find("table tbody tr").unbind("click", getSurveyDetail).click(getSurveyDetail);
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
	}
	
	function getSurveyDetail(e){
		var $target = $(e.currentTarget);
		actId = $target.attr("data-id");
		
		for(var i=0;i<acts.length;i++){
			if(acts[i].act_id == actId) {
				currentAct = acts[i];
				break;
			}
		}
		
		var obj = {actId: actId};
		box.request({
			url: box.getContextPath() + detailUrl,
			data: {params: $.obj2str(obj)},
			success: showSurveyDetail
		});
	}
	
	function showSurveyDetail(data) {
		
		if(data && data.code == "0000") {
			for(var i=0;i<data.result.length;i++){
				data.result[i].index=i+1;
				var is_mustbe = data.result[i].is_mustbe;
				if("1"==is_mustbe){
					data.result[i].is_must=true;
				}else{
					data.result[i].is_must=false;
				}
				
				var subject_type = data.result[i].subject_type;
				if("1"==subject_type){
					data.result[i].is_multi=true;
				}else if("2"==subject_type){
					data.result[i].is_answer=true;
				}else{
					data.result[i].is_single=true;
				}
			}
			var result = data.result;
			var content = box.ich.view_yxhd_wjdc_detail({act: currentAct, list: result});
			//parentView.empty().append(content);
			//parentView.find("#submit").unbind("click", submitSurvey).click(submitSurvey);
			
			content = box.showDialog({
				title: currentAct.act_title,
				width: 800,
				height: 560,
	            modal: true,
				content: content
			});
			content.find("#submit").unbind("click", submitSurvey).click(submitSurvey);
		}
	}
	
	function submitSurvey(){
		
		var obj={};
		obj.actId = actId;
		var list = new Array();
		var subject =  $('input:hidden[name="SUBJECT_ID"]');
		for(var i=0;i<subject.length;i++){
			var subjectId = $(subject[i]).val();
			var opt = $('input:radio[name='+subjectId+']:checked').val();
			if(null != opt && "" != opt){
				list.push({option_id:opt,subject_id:subjectId,answer_desc:'',note:''});
			}else{
				var optObj =  $('input:checkbox[name='+subjectId+']:checked');
				if(null != optObj && "" != optObj && optObj.length>0){
					for(var j=0;j<optObj.length;j++){
						opt = $(optObj[j]).val();
						list.push({option_id:opt,subject_id:subjectId,answer_desc:'',note:''});
					}
				}else{
					opt = $('#'+subjectId).val();
					if(null != opt && "" != opt){
						list.push({option_id:'1',subject_id:subjectId,answer_desc:opt,note:''});
					}
				}
			}
			
		}
		obj.lineList=list;
		box.request({
			url: box.getContextPath() + submitUrl,
			data: {params: $.obj2str(obj)},
			success: submitisOk
		});
	}
	
	function submitisOk(){
		box.showAlert({message:"提交成功！"});
		getSurveyList();
	}
	
	return {
		init: function(){
			box.listen("wjdc", loadWJDC);
		},
		destroy: function() { }
	};
});
