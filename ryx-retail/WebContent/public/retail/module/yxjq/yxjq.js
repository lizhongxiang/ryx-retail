/**
 * 营销技巧功能
 */
HOME.Core.register("plugin-yxjq", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/sale/saleSkill.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_yxjq({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("yxjq", onload);
		},
		destroy: function() { }
	};
});