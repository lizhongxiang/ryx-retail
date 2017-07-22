/**
 * 烟草讲堂功能
 */
HOME.Core.register("plugin-ycjt", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/welcome.html?v=1";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_ycjt({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("ycjt", onload);
		},
		destroy: function() { }
	};
});