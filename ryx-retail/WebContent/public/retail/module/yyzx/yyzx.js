/**
 * 应用中心
 */
HOME.Core.register("plugin-yyzx", function(box) {

	var parentView = null;
	var content = null;
	var contentTitle = null;
	var yyzxUrl = box.getResourcePath()+"htmls/yyzx/";
	
	function onload(view) {
		parentView = view;
		content = box.ich.view_yyzx({url: yyzxUrl});
		parentView.empty().append(content);
		
		parentView.find("#jsqa").unbind("click").click(panduan);
		parentView.find("#tqcxa").unbind("click").click(panduan);
		parentView.find("#wnla").unbind("click").click(panduan);
		parentView.find("#lyja").unbind("click").click(panduan);
		parentView.find("#hjkga").unbind("click").click(panduan);
		parentView.find("#ewma").unbind("click").click(panduan);
		parentView.find("#jplxa").unbind("click").click(panduan);
		parentView.find("#xhzda").unbind("click").click(panduan);
	}
	
	function panduan(e) {
		if ($(e.currentTarget).attr("data-name") == "jsq") {
			content = box.ich.view_yyzx_jsq({url: yyzxUrl});
			contentTitle = "计算器";
			jisuanqi();
		}
		
		if ($(e.currentTarget).attr("data-name") == "tqcx") {
			content = box.ich.view_yyzx_tqcx({url: yyzxUrl});
			contentTitle = "天气查询";
			tianqichaxun();
		}

		if ($(e.currentTarget).attr("data-name") == "wnl") {
			content = box.ich.view_yyzx_wnl({url: yyzxUrl});
			contentTitle = "万年历";
			wannianli();
		}
		
		if ($(e.currentTarget).attr("data-name") == "lyj") {
			content = box.ich.view_yyzx_lyj({url: yyzxUrl});
			contentTitle = "录音机";
			luyinji();
		}
		
		if ($(e.currentTarget).attr("data-name") == "xhzd") {
			content = box.ich.view_yyzx_xhzd({url: yyzxUrl});
			contentTitle = "字典成语大全";
			xinhuazidian();
		}
		
		if ($(e.currentTarget).attr("data-name") == "hjkg") {
			content = box.ich.view_yyzx_hjkg();
			contentTitle = "黄金矿工";
			huangjinkuanggong();
		}
		
		if ($(e.currentTarget).attr("data-name") == "ewm") {
			content = box.ich.view_yyzx_ewm({url: yyzxUrl});
			contentTitle = "二维码生成";
			erweima();
		}
		
		if ($(e.currentTarget).attr("data-name") == "jplx") {
			content = box.ich.view_yyzx_jplx({url: yyzxUrl});
			contentTitle = "键盘练习";
			jianpanlianxi();
		}
	}
	
	function jisuanqi() {
		box.showDialog({
			title : contentTitle,
			width : "535",
			height : "615",
			content : content
		});
	}
	
	function tianqichaxun() {
		box.showDialog({
			title : contentTitle,
			width : "600",
			height : "487",
			content : content
		});
	}
	
	function wannianli() {
		box.showDialog({
			title : contentTitle,
			width : "540",
			height : "436",
			content : content
		});
	}
	
	function luyinji() {
		box.showDialog({
			title : contentTitle,
			width : "540",
			height : "296",
			content : content
		});
	}
	
	function huangjinkuanggong() {
		box.showDialog({
			title : contentTitle,
			width : "550",
			height : "436",
			content : content
		});
	}
	
	function erweima() {
		box.showDialog({
			title : contentTitle,
			width : "540",
			height : "596",
			content : content
		});
	}
	
	function jianpanlianxi() {
		box.showDialog({
			title : contentTitle,
			width : "700",
			height : "536",
			content : content
		});
	}
	
	function xinhuazidian() {
		box.showDialog({
			title : contentTitle,
			width : "780",
			height : "560",
			content : content
		});
	}
	
	
	
	return {
		init : function() {
			box.listen("yyzx", onload);
		},
		destroy : function() {
		}
	};
});