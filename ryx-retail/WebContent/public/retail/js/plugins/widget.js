
HOME.Core.register("plugin-widgetshow", function(box) {
	
	var modules = {
			spxs: {bg: "#E8A135"},
			tjfx: {bg: "#8C4F79"},
			jydh: {bg: "#944866"}
	};
	/*
	.c1 { background: #E8A135; }
	.c2 { background: #944866; }
	.c3 { background: #67DDAB; }
	.c4 { background: #BAB387; }
	.c5 { background: #BEA881; }
	.c6 { background: #117AB3; }
	.c7 { background: #8C4F79; }
	.c8 { background: #81A0D7; }
	.c9 { background: #B3CE1D; }
	.c10 { background: #5B53A9; }
	.c11 { background: #6AA71B; }
	.c12 { background: #FAA701; }
	*/
	function onInitComplete() {
		var moduleId = getUrlVar("moduleId");
		
		if(!moduleId) {
			moduleId = box.context.moduleId;
		}
		
		if(moduleId) {
			var param = modules[moduleId];
			if(!param) param = {bg: "#81A0D7"};
			
			var tag = $("<a style='background-color: " + param.bg + "'>&nbsp;</a>");
			var $win = $(window);
			tag.param = {width: $win.width(), height: $win.height()};
			
			box.notify({
				type: HOME.Const.SHOW_MODULE,
				data: {moduleId: moduleId, target: tag}
			});
		}
	}
	
	function getUrlVars() {
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	}
	function getUrlVar(name){
		return getUrlVars()[name];
	}

	
	return {
		init: function(){
			box.listen(HOME.Const.INIT_COMPLETE, onInitComplete);
		},
		destroy: function() { }
	};
});
