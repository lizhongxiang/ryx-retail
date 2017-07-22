/**
 * 手机看店
 */
HOME.Core.register("plugin-sjkd", function(box) {
	
	//var sjkdUrl = "http://view.homca.com/default.aspx";
	var sjkdUrl = "http://192.168.0.114/web/mainpage.html";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_sjkd({url: sjkdUrl});
		parentView.empty().append(content);
	}
	
	return {
		init: function(){
			box.listen("sjkd", onload);
		},
		destroy: function() { }
	};
});