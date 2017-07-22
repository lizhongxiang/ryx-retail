/**
 * 库存管理功能
 */
HOME.Core.register("plugin-sjcz", function(box) {
	
	var parentView = null;
	
	var sendtxt = null;
	var sendbutton = null;
	var full = null;
	var win = null;
	var showwin = null;
	
	function onload(view) {
		parentView = view;
		
		var content = box.ich.view_sjcz();
		parentView.empty().append(content);
		
		showwin = parentView.find("#open_button");
		sendtxt = parentView.find("#send_text");
		sendbutton = parentView.find("#send_button");

		full = parentView.find("#fullscreen_button");
		win = parentView.find("#window_button");
		
		sendbutton.click(onSendMessage);
		full.click(function() {
			box.comm.send({type: "fullscreen"});
		});
		win.click(function() {
			box.comm.send({type: "window"});
		});
		
		showwin.click(function() {
			window.open(box.getContextPath() + "public/retail/comm/CommClient.html","newwindow",'height=500,width=800,top=0,left=0,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no');
		});
	}
	
	function onSendMessage() {
		var txt = sendtxt.val();
		box.comm.send({type: "showmessage", message: txt});
	}
	
	
	return {
		init: function(){
			box.listen("sjcz", onload);
		},
		destroy: function() { }
	};
});