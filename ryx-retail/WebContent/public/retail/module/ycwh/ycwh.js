/**
 * 烟草文化功能
 */
HOME.Core.register("plugin-ycwh", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/culture/tobaCulture.html?v=1";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		var content = box.ich.view_ycwh({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("ycwh", onload);
		},
		destroy: function() { }
	};
});