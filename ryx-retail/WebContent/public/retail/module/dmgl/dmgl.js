/**
 * 店面管理功能
 */
HOME.Core.register("plugin-dmgl", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/shopManage/shopManage.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_dmgl({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("dmgl", onload);
		},
		destroy: function() { }
	};
});