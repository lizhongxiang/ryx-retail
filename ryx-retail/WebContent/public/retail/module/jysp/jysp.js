/**
 * 烟草讲堂功能
 */
HOME.Core.register("plugin-jysp", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/video/videoPlay.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_ycjt({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("jysp", onload);
		},
		destroy: function() { }
	};
});