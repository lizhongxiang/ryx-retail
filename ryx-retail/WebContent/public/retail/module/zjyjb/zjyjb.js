/**
 * 真假烟鉴别功能
 */
HOME.Core.register("plugin-zjyjb", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/identify/identifyTobacco.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_zjyjb({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("zjyjb", onload);
		},
		destroy: function() { }
	};
});