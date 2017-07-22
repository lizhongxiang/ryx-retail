HOME.Core.register("plugin-netcall", function(box) {
	
	var com = box.com;
	var index = 0;
	var callbacks = new Array(100);
	
	function ieCallShell(command, params, statusCallback) {
		try {
			window.external.callShell(command, params);
			if(statusCallback) statusCallback(true);
		} catch(e) {
			box.console.log("ieCallShell出现错误：" + e.message);
			if(statusCallback) statusCallback(false);
		}
	}
	
	function chromeCallShell(command, params, statusCallback) {
		$.ajax({
			url: "http://pcshell.ruishangtong.com/",
			data: {command: command, paramstr: params},
			type: "post",
			success: function() {
				if(statusCallback) statusCallback(true);
			},
			error: function(arg) {
				box.console.log("chromeCallShell出现错误：" + arg.statusText);
				if(statusCallback) statusCallback(false);
			}
		});
	}
	
	function callShell(command, params, statusCallback) {
		//alert(window.chromeShell);
		if(typeof window.chromeShell != "undefined" && window.chromeShell) {
			chromeCallShell(command, params, statusCallback);
		} else {
			ieCallShell(command, params, statusCallback);
		}
	}
	
	function callShellData(command, params) {
		try {
			var result = window.external.callShellData(command, params);
			if(result) {
				return $.str2obj(result);
			} else {
				return null;
			}
		} catch(e) {
			box.console.log("callShellData：" + e.message);
			return null;
		}
	}
	
	function getIndex() {
		index++;
		if(index > 100) {
			index = 0;
		}
		return index;
	}
	
	function arradd(obj) {
		//callbacks.unshift(obj);
		callbacks[obj.index] = obj;
	}
	function arrget(ind) {
		/*
		for ( var i = index; i >= 0; i--) {
			if(callbacks[i].index == ind) {
				return callbacks[i];
			}
		}*/
		return callbacks[ind];
	}
	
	function initCom() {
		com.initParams = function(str, fullparams) {
			callShell("init", str);
			callShell("initFullParams", fullparams);
		};
		com.showScreen = function(obj) {
			callShell("showScreen", "");
		};
		com.commSend = function(obj) {
			callShell("commSend", $.obj2str(obj));
		};
		com.print = function(str) {
			callShell("print", str);
		};
		com.formFeed = function() {
			callShell("formFeed", "");
		};
		com.printQr = function(str) {
			callShell("printQr", str);
		};
		com.openCashBox = function() {
			callShell("openCashBox", "");
		};
		com.openSJKD = function() {
			callShell("openSJKD", "");
		};
		com.printTicket = function(content,num) {
			var obj = {content:content,num:num};
			callShell("ticket", $.obj2str(obj));
		};
		com.consume = function(obj, callback) {
			obj.index = getIndex();
			arradd({index: obj.index, callback: callback});
			callShell("consume", $.obj2str(obj));
		};
		com.readCardPwd = function(hasMac,pri,fieldMAB,callback) {
			var obj = {index: getIndex(), fieldMAB:fieldMAB};
			obj.pri = pri;
			obj.hasMac = hasMac;
			arradd({index: obj.index, callback: callback});
			callShell("readCardPwd", $.obj2str(obj));
		};
		com.getMac = function(fieldMAB,callback){
			index++;
			var obj = {index: index,fieldMAB:fieldMAB};
			arradd({index: index, callback: callback});
			callShell("getMac", $.obj2str(obj));
		};
		com.log = function(str) {
			callShell("log", str); 
		};
		
		com.getItem = function(data) {
			return callShellData("getItem", $.obj2str(data));
		};
		com.getItemAsync = function(obj, callback) {
			obj.index = getIndex();
			arradd({index: obj.index, callback: callback});
			callShell("getItem", $.obj2str(obj), function(b) {
				if(!b && callback) {
					callback(null);
				}
			});
		};
		
		var parentReceive = com.onReceive;
		com.onReceive = function(command, str) {
			var callobj = null;
			if(command == "getItem") {
				var params = $.str2obj(str);
				callobj = arrget(params.index);
				if(callobj && callobj.callback) {
					callobj.callback(params.items);
				}
			} else if(command == "consume") {
				var params = $.str2obj(str);
				callobj = arrget(params.index);
				if(callobj && callobj.callback) {
					callobj.callback(params);
				}
			} else if(command == "readCardPwd") {
				var params = $.str2obj(str);
				callobj = arrget(params.index);
				if(callobj && callobj.callback) {
					callobj.callback(params);
				}
			}
			else if(command == "getMac"){
				var params = $.str2obj(str);
				callobj = arrget(params.index);
				if(callobj && callobj.callback) {
					callobj.callback(params);
				}
			
			}
			return parentReceive(command, str);
		};
		window.onReceive = com.onReceive;
		
		var arr = new Array();
		arr.push(box.user.user_code);
		arr.push(box.user.merch_id);
		arr.push(box.user.merch_name);
		arr.push(box.user.lice_id);
		arr.push(box.getContextFullPath());
		arr.push(box.getResourcePath());
		
		var obj = $.extend({}, box.user);
		delete obj.permit;
		
		var params = {
				user: obj, 
				weather: box.context.weather, 
				path: {domain_url: box.getContextFullPath(), resource_url: box.getResourcePath()}
		};
		
		com.initParams(arr.join("^"), $.obj2str(params));
	}
	
	return {
		init: function(){
			initCom();
		},
		destroy: function() { }
	};
});
