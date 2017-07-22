/**
 * 用户功能反馈
 */
HOME.Core.register("plugin-yhfk", function(box) {
	

//	var submitUrl = "retail/infointer/submitInfointer";
	var submitUrl = "retail/feedback/submitFeedbackInfo";

	var parentView = null;
	var infos = null;
	
	function onload(view) {
		parentView = view;
		showInfointerList();
	}
	function showInfointerList() {
		var content = box.ich.view_yhfk();
		parentView.empty().append(content);
		parentView.find("#submit").unbind("click", submitInfointer).click(submitInfointer);
	}
	
	
	function submitInfointer(){
		var info_title = $("#info_title").val();
		var info_context =$("#info_context").val();
		var name = $("#name").val();
		var contact = $("#contact").val();
		
		if(info_title==""||info_title==null){
			box.showAlert({message:"标题不能为空！"});
		}else if(info_context==""||info_context==null){
			box.showAlert({message:"内容不能为空！"});
		}else if(name==""||name==null){
			box.showAlert({message:"姓名不能为空！"});
		}else if(contact==""||contact==null){
			box.showAlert({message:"联系方式不能为空！"});
		}else{
			var obj = {info_title:info_title,info_context:info_context,name:name,contact:contact};
			box.request({
				url: box.getContextPath() + submitUrl,
				data: {params: $.obj2str(obj)},
				success: submitisOk
			});
		}
	}
	
	function submitisOk(){
		box.showAlert({message:"提交成功！"});
		showInfointerList();
	}
	
	
	return {
		init: function(){
			box.listen("yhfk", onload);
		},
		destroy: function() { }
	};
});
