/**
 * 积分规则
 */
HOME.Core.register("plugin-jfgz", function(box) {
	
	// 接口地址
	var UpdAmountPerPoint="retail/basedata/updateMerchInfo";
	var getAmountPerPoint="retail/basedata/getMerchInfo";
	// 页面元素
	var parentView = null;
	var amountPerPointContent = null;
	
	function logMe(me) {
		box.console.log(me);
	}
	
	function onload(view) {
		gradeindex = 0;
		parentView = view;
		prepareParamAndRequireData(getAmountPerPoint, {}, showGradePointScale);
	}	

	function prepareParamAndRequireData(url, paramObject, callback) {

		box.request({
			url: box.getContextPath() + url,
			data: {params: $.obj2str(paramObject)},
			success: function(data) {
				if(data && data.code=='0000') {
					callback(data.result, null);
				} else {
					logMe(data);
				}
			},
			error: function(data) {
				logMe(data);
			}
		});
	}
	
	//显示积分比例对话框
	function showGradePointScale(result){

		amountPerPointContent = box.ich.view_jfgz({amount_per_point:result.amount_per_point});
		amountPerPointContent.find("#scale_submit_button").unbind('click').click(submitPointScale);
		parentView.empty().append(amountPerPointContent);
	}
	
	//保存积分比例
	function submitPointScale(){

		var amountPerPointText = amountPerPointContent.find("#amount_per_point_text").val();
		prepareParamAndRequireData(UpdAmountPerPoint, {"amount_per_point":amountPerPointText}, function(result) {
			box.showAlert({message: "保存成功!"});
			box.user.amount_per_point = amountPerPointText;
			//box.closeDialog({content: amountPerPointContent});
		});
		
	}
	
	return {
		init: function(){
			box.listen("jfgz", onload);
		},
		destroy: function() { }
	};
});