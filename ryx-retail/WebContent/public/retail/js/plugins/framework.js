
/**
 * 弹出窗口插件
 */
HOME.Core.register("plugin-dialog", function(box) {
	
	var alertView = $("#alertView");
	var alertFlag = 0;
	var confirmView = $("#confirmView");
	var $window = $(window);
	
	var dialogs = new Array();
	
	function showDialog(data) {
		var w = parseFloat(data.width);
		w = Math.min(960, Math.max(300, w));
		var h = parseFloat(data.height);
		h = Math.min(600, Math.max(240, h));

		var def = {width: w, height: h, modal: true,
				closeOnEscape: false, resizable:false,
				minWidth: 300, minHeight: 240,
				maxWidth: 960, maxHeight: 600,
				closeText: "关闭(Esc)",
				dialogClass: "dialog_middle"};
		
		$.extend(def, data);
		
		def.open = function() {
			if(box.keys.array[box.keys.array.length-1] != "dialog_onkey") {
				box.keys.push("dialog_onkey");
			}
			dialogs.push(data);
			if(data.open) data.open(arguments);
		};
		def.beforeClose = function() {
			if(!data.forceClose) {
				var b = true;
				if(data.beforeClose) b = data.beforeClose({
					callback: function() {
						data.forceClose = true;
						closeDialog(data);
					}
				});
				return b;
			}
		};
		def.close = function(e) {
			for(var i=0; i<dialogs.length;i++) {
				if(dialogs[i] == data) {
					dialogs.splice(i, 1);
					break;
				}
			}
			if(dialogs.length == 0) {
				box.keys.pop("dialog_onkey");
			}
			if(data.close) data.close(arguments);
			data.content.remove();
		};
		
		data.content.dialog(def);
	}

	function closeDialog(data) {
		if(data.content) {
			try {
				data.content.dialog("close");
			} catch(e) {
				box.console.log("close窗口出现异常：" + e.message);
				var closest = data.content.closest("ui-dialog");
				if(closest.length > 0) {
					closest.prev("ui-widget-overlay").remove();
					closest.remove();
				}
			}
		}
	}
	
	function showLayer(data) {
		var w = $window.width() - 80;
		var h = $window.height() - 80;
		h = Math.min(720, Math.max(600, h));

		var def = {width: w, height: h, modal: true,
				closeOnEscape: false, //resizable:false,
				minWidth: 960, minHeight: 600,
				maxWidth: 1200, maxHeight: 720,
				closeText: "关闭(Esc)",
				dialogClass: "dialog_layer"};
		
		$.extend(def, data);
		
		def.open = function() {
			if(data.open) data.open(arguments);
		};
		def.close = function() {
			data.content.remove();
			if(data.close) data.close(arguments);
		};
		
		data.content.dialog(def);
		
		box.notify({type: "pauseRoll", data: null});
	}
	
	function closeLayer(data) {
		if(data.content) {
			try {
				data.content.dialog("close");
			} catch(e) {
				box.console.log("关闭窗口出现异常：" + e.message);
				var closest = data.content.closest("ui-dialog");
				if(closest.length > 0) {
					closest.prev("ui-widget-overlay").remove();
					closest.remove();
				}
			}
		}
		box.notify({type: "restartRoll", data: null});
	}

	/**
	 * 显示一个提示框，默认3秒后自动消失
	 */
	function showAlert(data) {
		// 如果没有传message属性, 直接返回
		if(!data || !data.message) {
			return;
		}
		var label = alertView.find("#alertViewText");
		label.html(data.message);
		
		if(!data.time) data.time = 4000;
		
		if(alertFlag) {
			clearTimeout(alertFlag);
		}
		alertView.stop();
		
		var y = $window.height() * 0.85;
		var x = ($window.width() - alertView.width() - 20) / 2;
		alertView.css({top: y, left: x}).hide();
		alertView.animate({
			top: y-50, opacity: 'show'
		}, 500);
		alertFlag = setTimeout(function() {
			alertView.animate({
				top: y-100, opacity: 'hide'
			}, 500);
		}, data.time);
	}
	
	function showConfirm(data) {
		var label = confirmView.find("#confimViewText");
		label.html(data.message);
		data.content = confirmView;
		
		var def = {width: 360, modal: true,
				closeOnEscape: false, resizable:false,
				title: "提示",
				closeText: "关闭(Esc)",
				dialogClass: "dialog_middle"};
		
		$.extend(def, data);
		
		confirmView.find("#confirmViewExtend").empty();
		if(def.extendView) {
			confirmView.find("#confirmViewExtend").append(def.extendView);
		}
		
		var isCancel = true;
		var onOk = function() {
			isCancel = false;
			confirmView.dialog("close");
			if(data.ok) {
				data.ok(confirmView);
			}
		};
		var onCancel = function() {
			confirmView.dialog("close");
		};
		var onSubmit = function() { 
			isCancel = false;
			confirmView.dialog("close");
			if(data.submit) {
				data.submit();
			}
		};
		var onDonotSubmit = function() {
			isCancel = false;
			confirmView.dialog("close");
			if(data.donotSubmit) {
				data.donotSubmit();
			}
		};
		if(def.hasSubmit) {
			if(!def.buttons) {
				if(!def.buttonText){
					def.buttons = [{
		            text: "保 存",
		            "class": "primary_button",
		            click: onSubmit
					},{
			            text: "不保存",
			            "class": "closebtn_button",
			            click: onDonotSubmit
					}, {
			            text: "取 消",
			            "class": "closebtn_button",
			            click:onCancel
			        }];
				}else{
					def.buttons = [{
			            text: def.buttonText[0],
			            "class": "primary_button",
			            click: onSubmit
						},{
				            text: def.buttonText[1],
				            "class": "closebtn_button",
				            click: onDonotSubmit
						}, {
				            text: def.buttonText[2],
				            "class": "closebtn_button",
				            click:onCancel
				        }];
				}
			}
		} else {
			if(!def.buttons) def.buttons = [{
	            text: "确 定",
	            "class": "primary_button",
	            click: onOk
			}, {
	            text: "取 消",
	            "class": "closebtn_button",
	            click:onCancel
	        }];
		}
		def.open = function() {
			if(box.keys.array[box.keys.array.length-1] != "dialog_onkey") {
				box.keys.push("dialog_onkey");
			}
			dialogs.push(data);
		};
		def.close = function() {
			for(var i=0; i<dialogs.length;i++) {
				if(dialogs[i] == data) {
					dialogs.splice(i, 1);
					break;
				}
			}
			if(dialogs.length == 0) {
				box.keys.pop("dialog_onkey");
			}
			data.content.remove();
			if(isCancel && data.cancel) {
				data.cancel(confirmView);
			}
		};
		
		confirmView.dialog(def);
		setTimeout(function() {
			confirmView.focus();
		}, 100);
	}
	
	function onKey(data) {
		if(!dialogs.length) return;
		
		var dial = dialogs[dialogs.length-1];
		
		var widgets = null;
		if(dial && dial.content) {
			widgets = dial.content.find("[data-onkey=true]");
		}
		
		switch(data.key) {
			case HOME.Keys.ESC:
				closeDialog(dial);
				break;
			default:
				var b = false;
				if(widgets && widgets.length) {
					if(typeof box.keys.widgetOnkey != "undefined") {
						b = box.keys.widgetOnkey(widgets, data);
					}
				}
				if(!b && dial.onkey) {
					dial.onkey(data);
				}
				break;
		}
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.SHOW_ALERT, showAlert);
			box.listen(HOME.Const.SHOW_DIALOG, showDialog);
			box.listen(HOME.Const.SHOW_LAYER, showLayer);
			box.listen(HOME.Const.SHOW_CONFIRM, showConfirm);
			box.listen(HOME.Const.CLOSE_DIALOG, closeDialog);
			box.listen(HOME.Const.CLOSE_LAYER, closeLayer);
			box.listen("dialog_onkey", onKey);
		},
		destroy: function() { }
	};
});

/**
 * 显示Loading插件
 */
HOME.Core.register("plugin-loading", function(box) {
	
	var loading = $("#loadingView");
	var mask = $("#maskView");
	
	function load() {}
	
	function showLoading() {
		loading.show();
	}
	function hideLoading() {
		loading.hide();
	}
	
	function showMask(data) {
		mask.find("#mask_message").text(data.message);
		mask.find("#message_detail").html(data.detailMSG);
		mask.find("#mask_hidden").hide();
		mask.find("#feeContent").hide();
		if(data.hiddenButton && !data.hiddenButton.hidden){
			mask.find("#mask_hidden").val(data.hiddenButton.val).unbind("click").click(function(){
				var callback = data.hiddenButton.callback;
				callback();
			}).show();
		}
		if(data.feeContent && !data.feeContent.hidden){
			mask.find("#posFee").html(data.feeContent.val);
			mask.find("#feeContent").show();
		}
		if(data.cancelButton && data.cancelButton.callback){
			mask.find("#mask_cancel").unbind("click").click(function(){
				var callback = data.cancelButton.callback;
				callback();
			});
		}
		else{
			mask.find("#mask_cancel").unbind("click").click(function(){
				mask.hide();
			});
		}
		mask.show();
	}
	function hideMask() {
		mask.hide();
	}
	
	return {
		init: function(){
			load();
			box.listen(HOME.Const.SHOW_LOADING, showLoading);
			box.listen(HOME.Const.HIDE_LOADING, hideLoading);
			box.listen(HOME.Const.SHOW_MASK, showMask);
			box.listen(HOME.Const.HIDE_MASK, hideMask);
		},
		destroy: function() { }
	};
});

/**
 * 过滤请求
 */
HOME.Core.register("plugin-message", function(box) {
	
	function requestError(args) {
		var p0 = args[0];
		var p2 = args[1];
		
		if(p0) {
			if(p0.status == 200) {
				box.showAlert({message: "请求服务器出错：" + p2});
			} else if(p0.status == 0) {
				box.showAlert({message: "请求服务器出错：网络错误"});
			} else {
				box.showAlert({message: "请求服务器出错：" + p0.status});
			}
		} else {
			box.showAlert({message: "请求服务器出错：无状态"});
		}
	}
	
	function dataError(data) {
		if(!data) {
			box.showAlert({message: "服务器返回的数据为空"});
		} else {
			if(data.code == "1000") {
				box.showAlert({message: data.msg});
			} else if(data.code == "1001") {
				box.showAlert({message: "出现服务器端错误！"});
			} else if(data.code == "1002") {
				box.showAlert({message: "无权限访问接口！"});
			} else if(data.code == "1003") {
				box.showAlert({message: "您的身份已失效，将会转向到登录页面！"});
				setTimeout(function() {
					var u = box.user.login_url + "?serviceFlag=" + box.user.service_flag
						+ "&callbackURL=" + box.user.callback_url + "&onceFlag=";
					window.location.href = u;
				}, 1000);
			} else if(data.code == "1004") {
				box.showAlert({message: "访问接口不存在！"});
			} else if(data.code == "1005") {
				box.showAlert({message: "缺少必要的参数或参数格式不正确！"});
			} else if(data.code == "1033") {
				box.showAlert({message: data.msg});
			} else {
				box.showAlert({message: "系统忙，请稍后再试！"});
			}
		}
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.REQUEST_ERROR, requestError);
			box.listen(HOME.Const.DATA_ERROR, dataError);
		},
		destroy: function() { }
	};
});

/**
 * 加载模版文件插件
 */
HOME.Core.register("plugin-loadtemplate", function(box) {
	
	function trim(stuff) {
        if (''.trim) return stuff.trim();
        else return stuff.replace(/^\s+/, '').replace(/\s+$/, '');
    }
	
	// 检测html，是否存在需要统一处理的功能
	function renderData(html) {
		if(html) {
			if(!$.isPlaceholder()) {
				var list = html.find("input[placeholder],textarea[placeholder]");
				if(html.is("input[placeholder],textarea[placeholder]")) {
					list = html;
				}
				$.each(list, function(i, obj) {
					var $obj = $(obj);
					if(!$obj.val()) {
						$obj.addClass("prompt_label");
						$obj.attr("value", $obj.attr("placeholder"));
					}
				});
			}
			var onError = function(e) {
				var target = e.target;
				target.src = box.getContextPath() + "public/img/zwtp.png";
				//$(target).unbind("error");
			};
			html.find("img").error(onError);
		}
	}
	
	var ich = {
			
		templates: {},
		htmls: {},
		
		render: function(template, view, partials) {
			return Mustache.render(template, view, partials);
		},
		
		// grab jquery or zepto if it's there
        $: (typeof window !== 'undefined') ? window.jQuery || window.Zepto || null : null,
        
		// public function for adding templates
		// can take a name and template string arguments
		// or can take an object with name/template pairs
		// We're enforcing uniqueness to avoid accidental template overwrites.
		// If you want a different template, it should have a different name.
		addTemplate: function (name, templateString) {
		    if (typeof name === 'object') {
		    for (var template in name) {
		        this.addTemplate(template, name[template]);
		    }
		    return;
		}
		if (ich[name]) {
		    box.console.error("Invalid name: " + name + ".");
		} else if (ich.templates[name]) {
		    box.console.error("Template \"" + name + "  \" exists");
		    } else {
		        ich.templates[name] = templateString;
		        ich[name] = function (data, raw) {
		            data = data || {};
		            var result = ich.render(ich.templates[name], data, ich.templates);
		            var r = (ich.$ && !raw) ? ich.$(trim(result)) : result;
		            if(r.jquery && !r.attr("id")) {
		            	r.attr("id", name);
		            }
		            renderData(r);
		            return r;
		        };
		    }
		},
		
		// clears all retrieval functions and empties cache
		clearAll: function () {
		    for (var key in ich.templates) {
		        delete ich[key];
		    }
		    ich.templates = {};
		},
		
		// clears/grabs
		refresh: function () {
		    ich.clearAll();
		    ich.grabTemplates();
		},
		
		// grabs templates from the DOM and caches them.
		// Loop through and add templates.
		// Whitespace at beginning and end of all templates inside <script> tags will
		// be trimmed. If you want whitespace around a partial, add it in the parent,
		// not the partial. Or do it explicitly using <br/> or &nbsp;
		grabTemplates: function (doc) {
		    var i,
		        l,
		        scripts = doc.find('script'),
		    script,
		    trash = [];
		    for (i = 0, l = scripts.length; i < l; i++) {
		    script = scripts[i];
		    if (script && script.innerHTML && script.id && (script.type === "text/html" || script.type === "text/mustache")) {
		            ich.addTemplate(script.id, trim(script.innerHTML));
		            trash.unshift(script);
		        }
		    }
		    for (i = 0, l = trash.length; i < l; i++) {
		        trash[i].parentNode.removeChild(trash[i]);
		    }
		}
	};
	
	function onLoadTemplate(data) {
		if(box.ich.htmls[data.url]) {
			if(data.success) {
				data.success(box.ich.htmls[data.url]);
			}
		} else {
			//$.get(box.getContextPath() + data.url + "?t=" + new Date().getTime(), data.data, onLoadCallback(data), "html");
			$.ajax({
				url: box.getContextPath() + data.url + "?t=" + new Date().getTime(),
				data: data.data,
				dataType: "html",
				success: onLoadCallback(data),
				error: onLoadError(data)
			});
		}
	}
	
	function onLoadCallback(data) {
		return function(html) {
			
			var dom = document.createElement("div");
			dom.innerHTML = html;
			
			if(!dom.innerHTML) {
				$html = $("<div>" + html + "</div>");
			} else {
				$html = $(dom);
			}
			
			box.ich.htmls[data.url] = $html;
			box.ich.grabTemplates($html);
			if(data.success) {
				data.success($html);
			}
		};
	}
	
	function onLoadError(data) {
		return function() {
			box.showAlert({message: "加载模版文件失败：" + data.url + (arguments[1] ? "，" + arguments[1] : "")});
		};
	}
	
	return {
		init: function(){
			box.register("ich", ich);
			box.listen(HOME.Const.LOAD_TEMPLATE, onLoadTemplate);
			
			box.ich.grabTemplates($);
		},
		destroy: function() {
			
		}
	};
});

/**
 * 加载模块插件
 */
HOME.Core.register("plugin-loadjs", function(box) {
	
	var jss = {};
	
	function onLoadJSPlugins(data) {
		//$.getScript(box.getContextPath() + data.url + "?t=" + new Date().getTime(), onLoadCallback(data));
		if(!jss[data.url]) {
			$.ajax({
				url: box.getContextPath() + data.url + "?t=" + new Date().getTime(),
				dataType: "script",
				success: onLoadCallback(data),
				error: onLoadError(data)
			});
		} else {
			if(data.success) {
				data.success();
			}
		}
	}
	
	function onLoadCallback(data) {
		return function() {
			jss[data.url] = true;
			if(data.success) {
				data.success();
			}
		};
	}
	function onLoadError(data) {
		return function() {
			box.showAlert({message: "加载操作文件失败：" + data.url + (arguments[1] ? "，" + arguments[1] : "")});
		};
	}
	
	
	return {
		init: function(){
			box.listen(HOME.Const.LOAD_JSPLUGINS, onLoadJSPlugins);
		},
		destroy: function() {
			
		}
	};
});


/**
 * 加载系统组件
 */
HOME.Core.register("plugin-components", function(box) {
	var netcallJs = "public/retail/js/plugins/public/netcall.js";
	
	var com = {};
	var success = false;
	
	function initCom() {
		com.initParams = function() { };
		com.showScreen = function() { };
		com.print = function() { };
		com.formFeed = function() { };
		com.printQr = function() { };
		com.openCashBox = function() { };
		com.consume = function(obj, callback){};
		com.readCardPwd = function() {};
		com.openSJKD = function() {
			box.showAlert({message: "网页中无法启动手机看店程序，请安装客户端查看"});
		};
		com.commSend = function(obj) {
			if(success) {
				swfObj.send(obj);
			}
		};
		com.log = function() { };
		com.printTicket = function() { };
		
		com.getItem = function() { return null; };
		com.getItemAsync = function(data, callback) { if(callback) callback(null); };
		
		com.onReceive = function(command, params) {
			
			if(command == "barcode") {
				var act = $(document.activeElement);
				var respond = act.attr("data-respond");
				// 判断是否响应条形码事件
				if(respond != "false") {
					if(params.length == 13 || params.length == 12 || params.length == 8) {
						//box.showAlert({message: "读到条码：" + params});
						setTimeout(function() {
							box.keys.onkey({key:HOME.Keys.BARCODE, code: params});
						}, 10);
					} else {
						box.showAlert({message: "读到的条码不正确，请重新扫码"});
					}
				} else {
					if($.isInput(act[0])) {
						act.val(params);
						act.change();
					}
				}
			} else if(command == "onbarinput") {
				if(params == "0") {
					setTimeout(function() {
						window.onBarInput = params;
					}, 100);
				} else {
					window.onBarInput = params;
				}
			} else if(command == "onAward") {
				var obj = $.str2obj(params);
				if(obj.raffle_opt == "0") {
					box.showAlert({message: "顾客未中奖"});
				} else {
					box.showAlert({message: "顾客中了" + obj.opt_name + obj.item_name, time: 6000});
				}
			}
				
			return command;
		};
		
		window.onBarInput = "0";
		window.onReceive = com.onReceive;
		
		box.register("com", com);
		
		loadNetcall();
	}
	
	function loadNetcall() {
		// 异步方式检测是否在外壳中
		$.checkInShell(function(b) {
			if(b) {
				box.notify({
					type: HOME.Const.LOAD_JSPLUGINS,
					data: {url: netcallJs, success: loadNetJSSuccess}
				});
			}
		});
	}
	function loadNetJSSuccess() {
		box.console.log("通信JS加载成功");
	}
	
	
	return {
		init: function(){
			initCom();
		},
		destroy: function() { }
	};
});

HOME.Core.register("plugin-shortcuts", function(box) {
	
	var keys = {};
	keys.array = new Array();
	
	function load() {
		box.register("keys", keys);
		keys.onkey = function(data) {
			if(keys.array.length > 0) {
				var l = keys.array[keys.array.length - 1];
				box.notify({
					type: l,
					data: data
				});
			}
		};
		keys.push = function(tag) {
			keys.array.push(tag);
		};
		keys.pop = function(str) {
			if(keys.array.length > 0) {
				if(typeof str == "undefined") {
					keys.array.length = keys.array.length - 1;
				} else {
					if(keys.array[keys.array.length-1] == str) {
						keys.array.length = keys.array.length - 1;
					}
				}
			}
		};
		keys.popToFirst = function() {
			if(keys.array.length > 0) {
				keys.array.length = 1;
			}
		};
	}
	
	return {
		init: function(){
			load();
		},
		destroy: function() { }
	};
});


/**
 * 主页面监听条码扫描
 * 
 * 条码器设置
 * 输出模式：键盘模式
 * 条码字符的末端字符设定：加后缀-TAB
 * 在条码前添加“STX”标识符
 * 在条码后添加“ETX”标识符
 * 
 */
HOME.Core.register("plugin-barcode", function(box) {
	
	var win = $(document);
	
	var keysObj = {};
	//keysObj.array = [27(ESC), 112-123(F1-F12), 38-41(左上右下)];
	
	var barcode = "";
	var time = -1;
	var start = 0;
	var len = 0;
	var hasprevent = false;
	
	var current = null;
	var verify = null;
	var empty = null;
	var prompt = null;
	
	var oldArr = [{t:0, str: "", ch: ""}, {t:0, str: "", ch: ""}];
	
	var t = 0;
	
	function listener() {
		win.keydown(onKeyDown);
		win.focusin(onFocusin);
		
		for(var i in HOME.Keys){  
			keysObj["_" + HOME.Keys[i]] = true;
		}
	}
	
	function onKeyDown(e) {
		var eve = e;
		if(window.event) eve = window.event;
		
		var key = window.event ? e.keyCode : e.which;
		var respond = $(e.target).attr("data-respond");
		
//		box.console.log("keycode=" + key);

		if(window.onBarInput == "1") {
			if(respond != "false") {
				return false;
			}
		}

		// 检测扫码枪输入结束
		if(key == 9 || key == 13) {
			if(time > 0) {
				barInputEnd();
				return false;
			}
		}
		if(key >= 48 && key <= 57) {
			// 判断是否响应条形码事件
			if(respond != "false") {
				var ch = String.fromCharCode(key);
				if(time > 0) {
					barcode += ch;
					return false;
				} else {
					var t2 = new Date().getTime();
					var span = t2 - t;
					t = t2;
					box.console.log(span);
					if(span < 30) {
						if(t2 - oldArr[0].t < 100) {
							if($.isInput(e.target)) {
								e.target.value = oldArr[0].str;
							}
							barcode = oldArr[0].ch + oldArr[1].ch + ch;
						} else {
							//box.console.log("t2span=" + (t2 - oldArr[0].t));
							if($.isInput(e.target)) {
								e.target.value = oldArr[1].str;
							}
							barcode = oldArr[1].ch + ch;
						}
						time = setTimeout(barInputEnd, 200);
						return false;
					} else {
						putOldKey(t, e.target.value, ch);
					}
				}
			}
		}

		if($.isInput(e.target)) {
			// 在输入框中允许以下功能键
			if(key == 8 || key == 9 || key == 13 || key == 32 
				|| key == 35 || key == 36 || key == 37
				|| key == 39 || key == 127) {

				var $inp = $("input:not(:disabled)");
				
				if(key == 13) {
					e.preventDefault();
				    var nxtIdx = $inp.index(e.target) + 1;
				    $("input:not(:disabled):eq(" + nxtIdx + ")").focus().select();
				    
					eve.keyCode = 9;
					var enter = $(e.target).attr("data-enter");
					if(enter) {
						$("#" + enter).click();
					}else{
						$(e.target).blur();
					}
				}
				return;
			}
		} else if($.isComponent(e.target)) {
			if(key == 9 || key == 13)
				return;
		}

		var quickkey = $(e.target).attr("data-keys");
		if(quickkey) {
			quickkey = "," + quickkey + ",";
			if(quickkey.indexOf("," + key + ",") != -1) {
				return;
			}
		}
		
		if(keysObj["_" + key]) {
			// 检测到功能键
			box.keys.onkey({key:key});
			if(e && e.stopPropagation){
				e.stopPropagation();
			} else {
				window.event.cancelBubble = true;
			}
			
			var browser=navigator.appName ;
			var b_version=navigator.appVersion ;
			var version=b_version.split(";"); 
			var trim_Version=version[1].replace(/[ ]/g,"");
			if(browser=="Microsoft Internet Explorer" && trim_Version=="MSIE7.0") {
				window.event.keyCode = 0;
			}
			
			return false;
		}
	}
	
	function putOldKey(t, str, ch) {
		var item = oldArr[0];
		item.t = t;
		item.str = str;
		item.ch = ch;
		oldArr[0] = oldArr[1];
		oldArr[1] = item;
	}
	
	function barTimeout() {
		// 条码输入超时，直接丢弃
		barcode = "";
		len = 0;
		clearTimeout(time);
		time = -1;
		oldArr = [{t:0, str: "", ch: ""}, {t:0, str: "", ch: ""}];
		
		box.showAlert("读到的条码不正确，请重新扫码");
	}
	
	function barInputEnd() {
		var b = barcode;
		// 根据长度8位或者13位来标示是一个正确的条形码
		if(b) {
			if(b.length == 13 || b.length == 12 || b.length == 8) {
				//var span = new Date().getTime() - start;
				//box.console.log("span:" + span);
				box.console.log(b);
				/*box.notify({
					type: HOME.Const.BAR_CODE,
					data: {code: b}
				});*/
				box.keys.onkey({key:HOME.Keys.BARCODE, code: b});
			} else {
				box.showAlert("读到的条码不正确，请重新扫码");
			}
		}
		
		barcode = "";
		len = 0;
		clearTimeout(time);
		time = -1;
		oldArr = [{t:0, str: "", ch: ""}, {t:0, str: "", ch: ""}];
	}
	
	//verify
	function onFocusin(e) {
		releaseInput();
		
		if(!$.isComponent(e.target)) {
			return;
		}
		
		hasprevent = false;
		
		current = $(e.target);
		verify = current.attr("data-verify");
		empty = current.attr("data-empty");
		prompt = current.attr("placeholder");
		
		current.keypress(onFocusKeyDown);
		/*if(verify || empty || prompt) {
			current.focusout(onFocusOut);
		}*/
		current.focusout(onFocusOut);
		
		if(prompt && !$.isPlaceholder()) {
			if(!current.val() || current.val() === prompt) {
				current.val("");
			}
			current.removeClass("prompt_label");
		}
		
		if(current[0].nodeName == "INPUT" && !current.attr("maxlength")) {
			current.attr("maxlength", 16);
		}
	}
	
	function onFocusKeyDown(e) {
		var key = window.event ? e.keyCode : e.which;
		var target = $(e.currentTarget);
		
		if(key < 32) { return; }
		if(time > 0) { return; }
		
		var val = target.val();
		var keychar = e["char"];
		if(!keychar) {
			keychar = String.fromCharCode(key);
		}
		
		verify = target.attr("data-verify");
	    
		var b = true;
		
		switch(verify) {
			case "int":		// 整数
				b = /[-\d]/.test(keychar);
				if(b) {
					if(keychar == "-" && !val) {
						b = false;
					}
				}
				break;
			case "pint":	// 正整数
				b = /\d/.test(keychar);
				break;
			case "number":	// 浮点数
				b = /[-\.\d]/.test(keychar);
				if(b) {
					if(keychar == "-" && !val) {
						b = false;
					} else if(keychar == "." && val.indexOf(".") != -1) {
						b = false;
					}
				}
				break;
			case "pnumber":	// 正浮点数
				b = /[\.\d]/.test(keychar);
				if(b) {
					if(keychar == "." && val.indexOf(".") != -1) {
						b = false;
					}
				}
				break;
			case "phone":	// 电话
				b = /\d/.test(keychar);
				break;
			case "email":	// 邮箱
				b = /[A-Za-z0-9\.@-_]/.test(keychar);
				if(b) {
					if(keychar == "@" && val.indexOf("@") != -1) {
						b = false;
					}
				}
				break;
			case "time":	// 时间
				b = /[:\d]/.test(keychar);
				break;
			case "date":	// 日期
				b = /[-\d]/.test(keychar);
				break;
			case "card":	// 身份证号
				b = /[Xx\d]/.test(keychar);
				break;
		}
		return b;
	}
	
	function onFocusOut(e) {
		var target = $(e.currentTarget);
		var val = target.val();
		
		verify = target.attr("data-verify");
		empty = target.attr("data-empty");
		prompt = target.attr("placeholder");
		
		if(empty == "false") {
			if(!val) {
				target.addClass("validate");
			}
		}
		
		if(val) {
			var b = $.validate(val, verify);
			
			if(!b) {
				target.addClass("validate");
			} else {
				target.removeClass("validate");
			}
		} else {
			if(prompt && !$.isPlaceholder()) {
				target.addClass("prompt_label");
				target.val(prompt);
			}
		}
		
		if(hasprevent) {
			target.change();
		}
	}
	
	function releaseInput() {
		if(current) {
			current.unbind("keypress", onFocusKeyDown);
			current.unbind("focusout", onFocusOut);
			current = null;
		}
	}
	
	return {
		init: function(){
			listener();
		},
		destroy: function() {
			win.unbind("keydown", onKeyDown);
		}
	};
});



HOME.Core.register("plugin-winevent", function(box) {
	
	var win = $(document);
	
	function listener() {
		win.click(onDocumentClick);
	}
	
	function onDocumentClick(e) {
		
		var target = $(e.target);
		var dc = target.attr("data-doubleclick");
		
		if(dc === "false") {
			target.attr("disabled", "disabled");
		}
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.INIT_COMPLETE, listener);
		},
		destroy: function() {
			
		}
	};
});


/**
 * 加载系统参数
 */
HOME.Core.register("plugin-loadparams", function(box) {

	var tobaccoParamsUrl = "retail/tobacco/getOrderParams";
//	var unitUrl = "retail/item/searchItemUnit";
//	var itemKindUrl = "retail/item/searchItemKind";
	var controlHtml = "public/retail/js/plugins/public/controls.html";
	var controlJs = "public/retail/js/plugins/public/controls.js";
	var merchParams = "retail/getMerchParams";
	
//	var params = {};
	var labels = {};
	var unitObject = {array: []};
	var kindObject = {array: [], map: {}};
	var tobaccoObject = {};
	
	var tobaccoStatus = {"00":"未提交", "01": "计划", "02":"订购", "03":"确认", "0301":"预确认", "04":"发货", "08": "停止", "09": "完成", "05": "未知", "06": "未知", "07": "未知", "10": "未知", "99": "未知", "11":"正在支付"};
	var tobaccoPayStatus = {"01":"挂帐", "02":"未付款", "03": "收款", "04": "划帐", "05": "待划账"};
	var saleStatus = {"00":"已废弃", "01":"已下单", "02":"已审核", "03":"已完成", "04": "已取消"};
	var orderType = {"01":"电脑", "02":"POS", "03":"管店宝","04":"楼下店"};
	var promotionType = {"10":"商品折扣", "13":"会员折扣", "20":"搭配促销", "30":"商品积分", "33":"销售单积分", "40":"满减促销", "50":"抽奖"};
	var orderPay = {"01":"未收款", "02":"挂账", "03":"已收款"};

	function load() {
		labels.promotiontype = promotionType;
		labels.tobacco = tobaccoStatus;
		labels.tobaccopay = tobaccoPayStatus;
		labels.ordersale = saleStatus;
		labels.ordertype = orderType;
		labels.orderpay = orderPay;
		box.register("labels", labels);
		box.register("unit", unitObject);
		box.register("kinds", kindObject);
		box.register("tobacco", tobaccoObject);
		
		loadControls();
		//loadUnits();
		//loadKinds();
	}
	
	function loadControls() {
		box.notify({
			type: HOME.Const.LOAD_TEMPLATE,
			data: {url: controlHtml, success: loadTemplateSuccess}
		});
	}
	function loadTemplateSuccess() {
		box.notify({
			type: HOME.Const.LOAD_JSPLUGINS,
			data: {url: controlJs, success: loadJSSuccess}
		});
	}
	function loadJSSuccess() {
		/*// 初始化结束
		box.notify({
			type: HOME.Const.INIT_COMPLETE
		});

		loadTobaccoParams();
		*/
		loadParams();
	}
	
	function loadParams() {
		box.request({
			url: box.getContextPath() + merchParams,
			success: onGetParams
		});
	}
	
	function onGetParams(data) {
		if(data && data.code == "0000") {
			initUnits(data.result.units);
			initKinds(data.result.kinds);
		}
		
		// 初始化结束
		box.notify({
			type: HOME.Const.INIT_COMPLETE
		});
		
		loadTobaccoParams();
	}
	
	function initUnits(unitInfo) {
		unitObject.array = unitInfo;
		$.each(unitInfo, function(index, unit) {
			unit.label = unit.unit_name;
			unit.search = unit.unit_code + " " + unit.unit_spell;
			unitObject[unit.unit_id] = unit.unit_name;
		});
	}
	function initKinds(kindInfo) {
		var kindarr = [];
		var kindmap = {"":"全部商品",undefined:"全部商品"};
		$.each(kindInfo, function(index, kind) {
			kindObject[kind.item_kind_id] = kind.item_kind_name;
			kindmap[kind.item_kind_name] = kind.item_kind_id;
			kindarr[index] = { id: kind.item_kind_id, 
					pId: kind.item_kind_parent, 
					name: kind.item_kind_name, 
					isParent: kind.item_kind_parent=="0",
					label: kind.item_kind_name,
					search: kind.item_kind_id + " " + kind.item_kind_short_name + " " + kind.item_kind_code
				};
		});
		kindObject.array = kindarr;
		kindObject.map = kindmap;
	}
	
	function loadTobaccoParams() {
		box.request({
			url: box.getContextPath() + tobaccoParamsUrl,
			success: onGetTobaccoParams,
			error: onGetTobaccoParamsError
		});
	}
	function onGetTobaccoParams(data) {
		if(data && data.code == "0000") {
			tobaccoObject.params = data.result;
			
			// 订货参数获取
			box.notify({
				type: HOME.Const.TOBACCO_COMPLETE
			});
		}
	}
	function onGetTobaccoParamsError(data) {
		// 潍坊暂时不能订烟，点击配货直接打开潍坊的订货页面
		/*if(box.user.com_id == "10370701") {
			$("#jydh").attr("data-module", "")
				.attr("href", "http://www.1532mall.net/tobacco/login/wf")
				.attr("target", "_blank");
		}*/
		return false;
	}
	
	return {
		init: function(){
			load();
		},
		destroy: function() { }
	};
});


