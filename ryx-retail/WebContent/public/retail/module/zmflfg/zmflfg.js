/**
 * 专卖法律法规功能
 */
HOME.Core.register("plugin-zmflfg", function(box) {
	
	var ycjtUrl = box.getResourcePath() + "htmls/ycjt/jsp/school/law/lawRegulations.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_ycjt({url: ycjtUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("zmflfg", onload);
		},
		destroy: function() { }
	};
});