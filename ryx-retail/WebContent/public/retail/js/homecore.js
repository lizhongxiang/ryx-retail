
var HOME = {};

HOME.Const = {
	WINDOW_RESIZE:"window_resize",
	SHOW_LOADING:"show_loading",
	HIDE_LOADING:"hide_loading",
	SHOW_MASK:"show_mask",
	HIDE_MASK:"hide_mask",
	
	SHOW_ALERT:"show_alert",
	SHOW_DIALOG:"show_dialog",
	SHOW_LAYER:"show_layer",
	SHOW_CONFIRM:"show_confirm",
	CLOSE_DIALOG:"close_dialog",
	CLOSE_LAYER:"close_layer",
	
	LOAD_TEMPLATE:"load_html",
	LOAD_JSPLUGINS: "load_js",
	
	SHOW_MODULE: "show_module",
	CLOSE_MODULE: "close_module",
	
	REQUEST_ERROR: "request_error",
	DATA_ERROR: "data_error",
	
	BAR_CODE: "bar_code",
	INIT_COMPLETE: "init_complete",
	TOBACCO_COMPLETE: "tobacco_complete"
};

HOME.Keys = {
	BACK: 8,
	TAB: 9,
	ENTER: 13,
	ESC: 27,
	SPACE: 32,
	PAGEUP: 33,
	PAGEDOWN: 34,
	END: 35,
	HOME: 36,
	LEFT: 37,
	UP: 38,
	RIGHT: 39,
	DOWN: 40,
	DELETE: 46,
	MULTIPLY: 106,
	ADD: 107,
	SUBTRACT: 109,
	DIVIDE: 111,
	F1: 112,
	F2: 113,
	F3: 114,
	F4: 115,
	F5: 116,
	F6: 117,
	F7: 118,
	F8: 119,
	F9: 120,
	F10: 121,
	F11: 122,
	F12: 123,
	DEL: 127,
	BARCODE: 999
};

HOME.Sandbox = function($) {
	var result = function(coreObj, moduleId, context) {
		this.coreObj = coreObj;
		this.moduleId = moduleId;
		this.context = context;
		
		if(typeof console == "undefined") {
			this.console = {
				log: function() { },
				error: function() { },
				wran: function() { },
				info: function() { }
			};
		} else {
			this.console = console;
		}
		
		this.user = this.context.user;
	};
	result.fn = result.prototype;
	result.fn.register = function(name, value) {
		result.fn[name] = value;
	};
	result.fn.listen = function(eventNames, callback, module) {
		this.coreObj.listen(this.moduleId, eventNames, callback, module);
		return this;
	};
	result.fn.notify = function(event) {
		return this.coreObj.broadcast(event);
		//return this;
	};
	/*
	result.fn.request = function(url, type, dataType, data, success, error) {
		this.coreObj.request(url, type, dataType, data, success, error);
		return this;
	};
	*/
	result.fn.request = function(url, type, dataType, data, success, error, showLoading) {
		var obj = url;
		if(typeof(obj) == "string") {
			obj = {
                url: url,
                type: type,
                dataType: dataType,
                data: data,
                success: success,
                error: error,
                showLoading: showLoading
            };
		}
		if(!obj.type) obj.type="post";
		if(!obj.dataType) obj.dataType="json";
		if(obj.dataType == "jsonp") {
			obj.jsonp = "jsonp";
		}
		if(obj.data) obj.data.requestType = "ajax";
		else obj.data = {requestType: "ajax"};
		
		if(obj.expires) {
			// url的缓存失效设置
			var now = new Date();
			switch(obj.expires) {
				case "h":
					obj.data.t = now.format("yyyyMMddhh");
					break;
				case "d":
					obj.data.t = now.format("yyyyMMdd");
					break;
				case "m":
					obj.data.t = now.format("yyyyMM");
					break;
			}
		}
		
		if(typeof obj.showLoading == "undefined") obj.showLoading = true;
		if(!obj.timeout) obj.timeout = 60000;	// 25秒超时改为60秒 2014年10月12日
		
		var scb = obj.success;
		var ecb = obj.error;
		var $this = this;
		var timeout = -1;
		obj.success = function(data) {
			clearTimeout(timeout);
			$this.hideLoading();
			//try {
				if(obj.dataType == "json" || obj.dataType == "jsonp") {
					if(data && data.code == "0000") {
						if(scb) {
							scb(data);
						}
					} else {
						var b = true;
						if(ecb) {
							b = ecb(data);
						}
						if(b !== false) {
							$this.notify({
								type: HOME.Const.DATA_ERROR,
								data: data
							});
						}
					}
				} else {
					if(scb) {
						scb(data);
					}
				}
			/*} catch(e) {
				$this.console.error("URL请求回调出现异常：" + e.message);
			}*/
		};
		obj.error = function() {
			clearTimeout(timeout);
			$this.hideLoading();
			$this.notify({
				type: HOME.Const.REQUEST_ERROR,
				data: arguments
			});
			if(ecb) {
				ecb(arguments);
			}
		};
		
		if(obj.showLoading) {
			timeout = setTimeout(function() {
				$this.showLoading();
			}, 100);
		}
		$.ajax(obj);
		
		return this;
	};
	result.fn.showLoading = function() {
		this.notify({
			type: HOME.Const.SHOW_LOADING,
			data: ""
		});
		return this;
	};
	result.fn.hideLoading = function() {
		this.notify({
			type: HOME.Const.HIDE_LOADING,
			data: ""
		});
		return this;
	};
	result.fn.showMask = function(data) {
		this.notify({
			type: HOME.Const.SHOW_MASK,
			data: data
		});
		return this;
	};
	result.fn.hideMask = function() {
		this.notify({
			type: HOME.Const.HIDE_MASK,
			data: ""
		});
		return this;
	};
	result.fn.showLayer = function(data) {
		var id = null;
		if(data.content && data.content.jquery) {
			id = data.content.attr("id");
		}
		this.notify({
			type: HOME.Const.SHOW_LAYER,
			data: data
		});
		
		if(id) {
			return $("#" + id);
		}
	};
	result.fn.closeLayer = function(data) {
		this.notify({
			type: HOME.Const.CLOSE_LAYER,
			data: data
		});
	};
	result.fn.showDialog = function(data) {
		var id = null;
		if(data.content && data.content.jquery) {
			id = data.content.attr("id");
		}
		this.notify({
			type: HOME.Const.SHOW_DIALOG,
			data: data
		});
		if(id) {
			return $("#" + id);
		}
	};
	result.fn.closeDialog = function(data) {
		this.notify({
			type: HOME.Const.CLOSE_DIALOG,
			data: data
		});
	};
	result.fn.createDialog = function(data) {
		
	};
	result.fn.showAlert = function(data) {
		this.notify({
			type: HOME.Const.SHOW_ALERT,
			data: data
		});
	};
	result.fn.showConfirm = function(data) {
		this.notify({
			type: HOME.Const.SHOW_CONFIRM,
			data: data
		});
	};
	result.fn.getContextPath = function() {
		return this.context.root;
	};
	result.fn.getContextFullPath = function() {
		return this.context.domain;
	};
	result.fn.getResourcePath = function() {
		return this.context.resource;
	};
	result.fn.getMasterPath = function() {
		return this.context.master;
	};
	result.fn.getTobaccoImage = function(bar, size, position) {
		if(!size) size = "m";
		if(!position) position = "face";
		return this.getResourcePath() + "images/tobacco/" + size + "/" + bar + "_" + size + "_" + position + ".png";
	};
	result.fn.getItemImage = function(bar, size, position) {
		if(!size) size = "m";
		if(!position) position = "face";
		return this.getResourcePath() + "images/item/" + size + "/" + bar + "_" + size + "_" + position + ".png";
	};
	result.fn.getBankImage = function(id, size, position) {
		if(!size) size = "s";
		if(!position) position = "face";
		// 0313 开头的银行，id用8位取图片
		if(!(id.indexOf("0313") == 0)) {
			id = id.substr(0, 4);
		}
		return this.getResourcePath() + "images/bank/" + size + "/" + id + "_" + size + "_" + position + ".png";
	};
	result.fn.stopEvent = function(e) {
		var evt = e || window.event;
		//IE用cancelBubble=true来阻止而FF下需要用stopPropagation方法
		evt.stopPropagation ? evt.stopPropagation() : (evt.cancelBubble=true);
	};
	result.fn.getHashcode = function() {
		return location.hash;
	};
	result.fn.setHashcode = function(hash) {
		location.hash = hash;
	};
	result.fn.getDatas = function(key) {
		return this.coreObj.getDatas(key);
	};
	result.fn.setDatas = function(key, value) {
		this.coreObj.setDatas(key, value);
	};
	result.fn.isTobacco = function(type) {
		if(type) {
			return type.indexOf("01") == 0;
		} else {
			return false;
		}
	};
	result.fn.loadTemplate = function(url, callback) {
		this.notify({
			type: HOME.Const.LOAD_TEMPLATE,
			data: {
				url: url,
				callback: callback
			}
		});
	};
	result.fn.getVersion = function() {
		return this.context.version;
	};
	return result;
}(jQuery);

HOME.Core = function($, library) {
	
	var modules = {};
	var listens = {};
	
	var context = {};
	
	var isStart = false;
	var box = null;
	
	function triggerEvent(obj) {
		var type = obj.type;
		var data = obj.data;
		var lisarr = listens[type];
		b = true;
		if (lisarr) {
			for (var i = 0, j = lisarr.length; i < j; i++) {
				if(lisarr[i] && lisarr[i]["method"]) {
					//try {
						var ret = lisarr[i]["method"](data, type);
						if(ret === false) {
							b = false;
							break;
						}
					/*} catch(e) {
						//alert("出现客户端错误，请联系我们的服务人员，谢谢！问题描述：类型=" + type + ", 错误信息=" + e.message + ", 数据=" + (data.jquery ? data[0] : data) + ", " + lisarr[i]["method"]);
						if(box && box.com && box.com.log) {
							box.com.log("JS调用方法出错：type=" + type + ", message=" + e.message + ", data=" + data);
						}
					}*/
				}
			}
		}
		return b;
	}
	
	var ret = {
		startApp: function() {
			//$(window).hashchange();
		},
		register: function(name, module) {
			modules[name] = {
				createor: module,
				instance: null
			};
			if(isStart) {
				this.start(name);
			}
		},
		start: function(name) {
			modules[name].instance = modules[name].createor(new HOME.Sandbox(this, name, context));
			modules[name].instance.init();
		},
		stop: function(name) {
			var module = modules[name];
			if (module.instance) {
				module.instance.destroy();
				module.instance = null;
			}
		},
		startAll: function(c) {
			if(c) {
				$.extend(context, c);
			}
			for (var m in modules) {
				if (modules.hasOwnProperty(m)) {
					this.start(m);
				}
			}
			isStart = true;
		},
		stopAll: function() {
			for (var m in modules) {
				if (modules.hasOwnProperty(m)) {
					this.stop(m);
				}
			}
			isStart = false;
		},
		listen: function(moduleId, eventNames, callback, module) {
			if (!$.isArray(eventNames)) {
				eventNames = [eventNames];
			}
			$.each(eventNames, function(i, name) {
				if (listens[name]) {
					listens[name].push({
						moduleId: moduleId,
						method: callback,
						scope: module
					});
				} else {
					listens[name] = [{
						moduleId: moduleId,
						method: callback,
						scope: module
					}];
				}
			});
		},
		broadcast: function(event) {
			return triggerEvent(event);
		}
	};
	box = new HOME.Sandbox(ret, "core", context);
	return ret;
}(jQuery);
