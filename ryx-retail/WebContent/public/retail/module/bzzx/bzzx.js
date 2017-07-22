/**
 * 帮助中心功能
 */
HOME.Core.register("plugin-bzzx", function(box) {
	
//	var ycjtUrl = box.getContextPath() + "public/retail/html/bzzx/jsp/index.html";
	var bzzxUrl = box.getResourcePath() + "htmls/bzzx/jsp/";
//	var bzzxUrl = "http://192.168.0.3:8889/resource/htmls/bzzx/jsp/";
	var parentView = null;
	
	function onload(view) {
		parentView = view;
		var content = box.ich.view_bzzx({url: bzzxUrl});
		parentView.empty().append(content);
		$(".ulu").find("a").each(function(){
			$(this).click(function(){
				$(".ulu").find("a").css("background","#f9f9f9").css("color","#484848");
				$(this).css("background","#f0f0f0");
			});
		});
//		parentView = view;
//		parentView.empty().append(box.ich.view_bzzx());
//		parentView.empty().append(box.ich.view_bzzx()).css("background-image","url(../public/retail/resource/zzjs.jpg)");
		
	}
	
	return {
		init: function(){
			box.listen("bzzx", onload);
		},
		destroy: function() { }
	};
	

	
	
	
});
