
/**
 * 加载模块插件
 */
HOME.Core.register("plugin-module", function(box) {
	
	var modules = null;
	
	var currentRootModule = null;
	var currentModule = null;
	var currentWindow = null;
	var isShow = false;
	var showfooter = false;
	
	function initModule() {
		modules = box.user.permit;
		
		var list = new Array();
		var objtmp = new Object();
		if(modules && modules.length > 0) {
			for(var i in modules) {
				setModuleUrl(modules[i]);
				if(!modules[i].parent_id) {
					modules[i].children = [];
					list.push(modules[i]);
					objtmp[modules[i].module_id] = modules[i];
					if(modules[i].haspage == "1") {
						modules[i].quick_key = "F1";
						modules[i].children = [modules[i]];
					}
				}
			}
			for(var i in modules) {
				var pid = modules[i].parent_id;
				if(pid) {
					if(!objtmp[pid].children) objtmp[pid].children = [];
					objtmp[pid].children.push(modules[i]);
					modules[i].quick_key = "F" + objtmp[pid].children.length;
				}
			}
			for(var index in list) {
				if(list[index].children.length == 1) {
					list[index].children[0].quick_key = "";
				}
			}
			modules = list;
			
		} else {
			box.showAlert({message: "未获取到模块权限"});
		}
	}
	
	function setModuleUrl(m) {
		// 20141123 模块支持多地市个性化
		if(m.multi == "1") {
			m.jsurl = "public/retail/multi/" + box.user.com_id + "/module/" + m.module_id + "/" + m.module_id + ".js";
			m.templateurl = "public/retail/multi/" + box.user.com_id + "/module/" + m.module_id + "/" + m.module_id + ".html"; 
			//m.icon = box.getContextPath() + "public/retail/img/" + m.module_id + ".png";
			m.icon = "navbarIcon navbarIcon_" + m.module_id;
		} else {
			m.jsurl = "public/retail/module/" + m.module_id + "/" + m.module_id + ".js";
			m.templateurl = "public/retail/module/" + m.module_id + "/" + m.module_id + ".html"; 
			//m.icon = box.getContextPath() + "public/retail/img/" + m.module_id + ".png";
			m.icon = "navbarIcon navbarIcon_" + m.module_id;
		}
	}
	
	function checkModule(data) {
		var rootModule = null;
		var module = null;
		var m, m2;
		for(var i in modules) {
			m = modules[i];
			if(m.module_id == data.moduleId) {

				rootModule = m;
				if(m.haspage == "1") {
					module = m;
				} else {
					if(m.children) {
						module = m.children[0];
					} else {
						box.showAlert({message: "您没有权限访问该功能！"});
					}
				}
				break;
			}
			for(var j in m.children) {
				m2 = m.children[j];
				if(m2.module_id == data.moduleId) {
					module = m2;
					rootModule = m;
					break;
				}
			}
			if(module) break;
		}
		
		if(module) {
			var back = data.target.attr("data-background");
			if(!back) back = data.target.css("background-color");
			module.style = {background: back};
			var w = data.target.attr("data-width");
			if(w) {
				module.style.width = w;
			}
			
			if(data.target.param) {
				module.param = data.target.param;
			}
			//loadModule(rootModule, module);
			showModule(rootModule, module);
		} else {
			box.showAlert({message: "您没有权限访问该功能！"});
		}
	}
	
	function loadModule(rootModule, module, windowContent) {
		if(module.isload) {
			startModule(rootModule, module, windowContent);
		} else {
			box.notify({
				type: HOME.Const.LOAD_TEMPLATE,
				data: {url: module.templateurl, success: onLoadTemplateSuccess(rootModule, module, windowContent)}
			});
		}
	}
	
	function onLoadTemplateSuccess(rootModule, module, windowContent) {
		return function() {
			box.notify({
				type: HOME.Const.LOAD_JSPLUGINS,
				data: {url: module.jsurl, success: onLoadJSSuccess(rootModule, module, windowContent)}
			});
		};
	}
	
	function onLoadJSSuccess(rootModule, module, windowContent) {
		return function() {
			module.isload = true;
			startModule(rootModule, module, windowContent);
		};
	}
	
	function startModule(rootModule, module, windowContent) {
		box.notify({
			type: module.module_id,
			data: windowContent
		});
	}
	
	function showModule(rootModule, module) {
		if(currentModule && currentModule.module_id == module.module_id) {
			return;
		}
		box.console.log("打开模块：" + module.module_id);
		if(module.module_id == "sjkd") {
			box.com.openSJKD();
			return;
		}
		
		if(currentRootModule && currentRootModule.module_id != rootModule.module_id) {
			// 已打开一个根模块，暂时不能打开
			return;
		}
		
		if(currentModule && currentModule.module_id != module.module_id) {
			var b = box.notify({
				type: currentModule.module_id + "_close",
				data: {
					content: currentWindow, 
					callback: function() {
						createModule(rootModule, module);
					}
				}
			});
			if(b) {
				createModule(rootModule, module);
			}
		} else {
			createModule(rootModule, module);
		}
	}
	
	function createModule(rootModule, module) {
		
		if(!currentRootModule || currentRootModule.module_id != rootModule.module_id) {
			currentRootModule = rootModule;
			var windowHeader = box.ich.r_window_header(rootModule);
			currentWindow = box.ich.r_window({header: windowHeader[0].outerHTML});
		}
		if(!isShow) {
			isShow = true;
			var dataobj = {
				title: module.title, 
				content: currentWindow,
				close: onCloseModule
			};
			
			if(module.style && module.style.width) {
				dataobj.width = module.style.width;
			}
			if(module.param) {
				$.extend(dataobj, module.param);
			}
			
			currentWindow = box.showLayer(dataobj);
			if(module.style) {
				currentWindow.find(".window_header").css("background-color", module.style.background);
			}
			currentWindow.closest(".ui-resizable").resize(function() {
				resizeWindow(currentWindow);
			});
			currentWindow.find(".window_close").click(closeModule);
			listenerWindowHeader(currentWindow);
		}
		
		if(currentRootModule.children.length > 1) {
			currentWindow.removeClass("single");
		} else {
			currentWindow.addClass("single");
		}
		currentWindow.removeClass("haspager");
		currentWindow.removeClass("hasadsearch");
		//移除高级查询
		var highsearcher1 = currentWindow.find(".window_search");
		if(highsearcher1.is(":visible")){
			highsearcher1.hide();
		}
		currentWindow.find(".window_header_item").removeClass("selected");
		currentWindow.find(".window_header_item[data-module="+module.module_id+"]").addClass("selected");
		
		if(!currentModule || currentModule.module_id != module.module_id) {
			
			currentModule = module;
			var windowContent = currentWindow.find(".window_content");
			var windowHeaderTip = currentWindow.find(".window_header_tip");
			var windowMessage = currentWindow.find(".window_message");
			var windowPager = currentWindow.find(".window_pager");
			//高级查询弹出层
			var highsearcher = currentWindow.find(".window_search");
			
			windowContent.empty().append(getLoadingTag());
			showfooter = false;
			//弹出层初始为隐藏
			windowContent.showFooter = function(b) {
				showfooter = b;
				resizeWindow(currentWindow);
			};
			windowContent.setHeaderTip = function(d) {
				windowHeaderTip.empty().append(d);
			};
			windowContent.showMessage = function(param) {
				if(param.content) {
					var parentTop = param.content.offset().top;
					var parentLeft = param.content.offset().left;
					var parentWidth = param.content.width();
					var parentHeight = param.content.height();
					var width = windowMessage.width();
					var height = windowMessage.height();
					if(param.message) {
						windowMessage.html(param.message);
					} else {
						windowMessage.html("");
					}
					windowMessage.show();
					windowMessage.offset({top:parentTop+(parentHeight/2-height/2)<300?300:(parentHeight/2-height/2), left:parentLeft+(parentWidth/2-width/2)<450?450:(parentWidth/2-width/2)});
				}
			};
			windowContent.closeMessage = function() {
				windowMessage.hide();
			};
			/**
			 * 高级查询隐藏
			 * */
			windowContent.hideHighSearch = function(){
				//判断高级查询弹出层是否显示,显示则隐藏
				if(highsearcher.is(":visible")){
					//highsearcher.hide();
					highsearcher.fadeOut();
				}
			};
			/**
			 * 高级查询,弹出层
			 * */
			windowContent.showHighSearch =  function(width,height,margintop,context){
				highsearcher.fadeIn();
				highsearcher.attr("id","window_search"); 
				highsearcher.css("width",width);
				highsearcher.css("height",height);
				highsearcher.css("margin-top",margintop);
				if(highsearcher.children().size()==0){
					highsearcher.append(context);
				}
				//添加监听高级查询弹出框事件
				listenHighSearch(windowContent);
			};
			
			windowContent.unbind("empty").bind("empty", function(e) {
				if(e.target == e.currentTarget) {
					// 定位滚动条
					currentWindow.find(".window_content_cont").scrollTop(0);
				}
			});
			
			/**
			 * 分页
			 */
			windowContent.showPager = function(pager, showpage) {
				if(!currentWindow) return;
				
				var prev = windowPager.find(".window_pager_prev");
				var next = windowPager.find(".window_pager_next");
				var start = windowPager.find(".window_pager_start");
				var count = windowPager.find(".window_pager_count");
				var pageIndex=windowPager.find(".window_pager_index");
				var pageUpdate=windowPager.find(".window_pager_update");
				var pageFirst=windowPager.find(".window_pager_first");
				var pageLast=windowPager.find(".window_pager_last");
				var pageCustom=windowPager.find(".window_pager_custom");
				
				
				prev.unbind("click").attr("disabled", false).removeClass("disabled");
				next.unbind("click").attr("disabled", false).removeClass("disabled");
				
				pager.page_index = parseInt(pager.page_index);
				pager.page_size = parseInt(pager.page_size);
				pager.page_count = parseInt(pager.page_count);
				pager.count = parseInt(pager.count);
				
				if(pager.page_count <= 0) pager.page_count = 1;
				
				start.text(pager.page_index);
				count.text(pager.page_count);
				
				currentWindow.addClass("haspager");
				if(pager && pager.page_index <= 1) {
					prev.attr("disabled", true).addClass("disabled");
				}
				if(pager && pager.page_index >= pager.page_count) {
					next.attr("disabled", true).addClass("disabled");
				}
				
				prev.unbind("click").click(function() {
					pageUpdate.hide();
					var i = pager.page_index - 1;
					if(i < 1) return;
					
					if(showpage) {
						var obj = {page_index: i, page_size: pager.page_size};
						showpage(obj);
					}
					currentWindow.find(".window_footer").height(60);
					resizeWindow(currentWindow);
				});
				next.unbind("click").click(function() {
					pageUpdate.hide();
					var i = pager.page_index + 1;
					if(i > pager.page_count) return;
					
					if(showpage) {
						var obj = {page_index: i, page_size: pager.page_size};
						showpage(obj);
					}
					currentWindow.find(".window_footer").height(60);
					resizeWindow(currentWindow);
				});
				pageIndex.unbind("click").click(function(){
					pageUpdate.show();
					pageCustom.val("");
					pageCustom.focus().select();
				});
				pageFirst.unbind("click").click(function(){
					pageCustom.val("");
					var i = 1;
					if(i > pager.page_count) return;
					
					if(showpage) {
						var obj = {page_index: i, page_size: pager.page_size};
						showpage(obj);
					}
					currentWindow.find(".window_footer").height(60);
					resizeWindow(currentWindow);
					pageUpdate.hide();
				});
				pageLast.unbind("click").click(function(){
					pageCustom.val("");
					var i = pager.page_count;
					if(i > pager.page_count) return;
					
					if(showpage) {
						var obj = {page_index: i, page_size: pager.page_size};
						showpage(obj);
					}
					currentWindow.find(".window_footer").height(60);
					resizeWindow(currentWindow);
					pageUpdate.hide();
				});
				pageCustom.unbind("change").change(function(){
					var i=parseInt(pageCustom.val());
					if(i > pager.page_count || i==0){
						box.showAlert({message: "很抱歉，所输入的页码不能小于1大于"+pager.page_count+"！"});
					}else{					
					  if(showpage) {
						  var obj = {page_index: i, page_size: pager.page_size};
						  showpage(obj);
					  }
					  currentWindow.find(".window_footer").height(60);
					  resizeWindow(currentWindow);
					}
					pageUpdate.hide();
				});
				$(document).click(function(e){
					var tar=e.target;
					if(!($(tar).hasClass("window_pager") || $(tar).parents().hasClass("window_pager"))){
						pageUpdate.hide();
					}
				});
			};
			windowContent.hidePager = function() {
				if(!currentWindow) return;
				currentWindow.removeClass("haspager");
			};
			/**
			 * 高级查询
			 */
			windowContent.showAdvancedSearch = function(){
				if(!currentWindow) return;
				
				currentWindow.addClass("hasadsearch");
				
				var search = currentWindow.find("#footer_adSearch");
				var state = search.attr("data-state");
				if(state == "down") {
					windowContent.advancedOpen(search);
				} else {
					windowContent.advancedClose(search);
				}
				
				search.unbind("click").click(function(){
					if(search.attr("data-state")=="up"){
						windowContent.advancedOpen(search);
					}else{
						windowContent.advancedClose(search);
					}
				});
			};
			
			windowContent.advancedOpen = function(search) {
				search.find("span").removeClass("pageIcon_upArrrow").addClass("pageIcon_downArrrow");
				search.attr("data-state","down");
				search.attr("title","合并");
				currentWindow.find(".view_footer").css("height","100px");
				currentWindow.find(".view_adSearch").show();
				currentWindow.find(".window_footer").height(100);
				resizeWindow(currentWindow);
			};
			windowContent.advancedClose = function(search) {
				search.find("span").removeClass("pageIcon_downArrrow").addClass("pageIcon_upArrrow");
				search.attr("data-state","up");
				search.attr("title","展开");
				currentWindow.find(".view_footer").css("height","60px");
				currentWindow.find(".view_adSearch").hide();
				currentWindow.find(".window_footer").height(60);
				resizeWindow(currentWindow);
			};
			windowContent.hideAdvancedSearch = function(){
				if(!currentWindow) return;

				currentWindow.find(".window_footer").height(60);
				resizeWindow(currentWindow);
				currentWindow.removeClass("hasadsearch");
			};
			
			loadModule(rootModule, module, windowContent);
		}
		resizeWindow(currentWindow);
	}
	//监听高级查询弹出层事件
	function listenHighSearch(windowContent){
		$(document).bind("click",function(e){
			if($(e.target).closest("#window_search").length == 0 && e.target.id != "highSearch"){
				windowContent.hideHighSearch();
			}
		});
	}
	function listenerWindowHeader(context) {
		context.find(".subnavlink")
			.unbind("click", itemClickHandler)
			.click(itemClickHandler);
		box.keys.push("layer_onkey");
	}
	function itemClickHandler(e) {
		var $target = $(e.currentTarget);
		var module = $target.attr("data-module");
		box.notify({
			type: HOME.Const.SHOW_MODULE,
			data: {moduleId: module, target: $target}
		});
	}
	
	function resizeWindow(win) {
		if(win) {
			var header = win.find(".window_header");
			var footer = win.find(".window_footer");
			var content = win.find(".window_content_cont");
			//计算内容高度 
			content.height(win.height() - header.height() - (showfooter ? footer.height() : 0));
			content.trigger("footer_resize");
		}
	}
	
	function closeModule() {
		box.console.log("关闭模块：" + currentModule.module_id);
		
		var b = box.notify({
			type: currentModule.module_id + "_close",
			data: {
				content: currentWindow, 
				callback: function() {
					box.notify({
						type: HOME.Const.CLOSE_LAYER,
						data: {content: currentWindow}
					});
				}
			}
		});
		if(b) {
			box.notify({
				type: HOME.Const.CLOSE_LAYER,
				data: {content: currentWindow}
			});
		}
	}
	
	function onCloseModule() {
		isShow = false;
		currentModule = null;
		currentRootModule = null;
		if(currentWindow) {
			currentWindow.find(".window_close").unbind("click", closeModule);
			currentWindow = null;
		}
		box.keys.popToFirst();
	}
	
	function getLoadingTag() {
		//return "<img class='window_loading' alt='' src='" + box.getContextPath() + "public/img/loading.gif' />";
		return "<div style='margin:0 auto; padding-top: 180px; text-align:center; font-size: 18px'>正在加载...</div>";
	}
	
	function desktopOnKey(data) {
		switch(data.key) {
			case HOME.Keys.BARCODE:
				if(!currentModule) {
					// 当前没有打开的模块，默认打开销售
					$("#spxs").click();
				}
				break;
		}
	}
	
	function onKey(data) {
		var widgets = null;
		if(currentWindow) {
			widgets = currentWindow.find("[data-onkey=true]");
		}
		var b = false;
		switch(data.key) {
			case HOME.Keys.ESC:
				closeModule();
				break;
			case HOME.Keys.PAGEUP:
				if(currentWindow && currentWindow.length)
					currentWindow.find(".window_pager_prev").click();
				break;
			case HOME.Keys.PAGEDOWN:
				if(currentWindow && currentWindow.length)
					currentWindow.find(".window_pager_next").click();
				break;
			case HOME.Keys.F1: b = showChildModule(0); if(b) break;
			case HOME.Keys.F2: b = showChildModule(1); if(b) break;
			case HOME.Keys.F3: b = showChildModule(2); if(b) break;
			case HOME.Keys.F4: b = showChildModule(3); if(b) break;
			case HOME.Keys.F5: b = showChildModule(4); if(b) break;
			case HOME.Keys.F6: b = showChildModule(5); if(b) break;
			case HOME.Keys.F7: b = showChildModule(6); if(b) break;
			case HOME.Keys.F8: b = showChildModule(7); if(b) break;
			case HOME.Keys.F9: b = showChildModule(8); if(b) break;
			case HOME.Keys.F10: b = showChildModule(9); if(b) break;
			case HOME.Keys.F11: b = showChildModule(10); if(b) break;
			case HOME.Keys.F12: b = showChildModule(11); if(b) break;
			default:
				if(widgets && widgets.length) {
					if(typeof box.keys.widgetOnkey != "undefined") {
						b = box.keys.widgetOnkey(widgets, data);
					}
				}
				if(!b && currentModule) {
					box.notify({
						type: currentModule.module_id + "_onkey",
						data: data
					});
				}
				break;
		}
	}
	
	function showChildModule(index) {
		/*if(currentRootModule && currentRootModule.children && currentRootModule.children[index]) {
			var m = currentRootModule.children[index];
			if(m != currentModule) {
				
			}
		}*/
		
		var b = false;
		if(currentWindow) {
			var list = currentWindow.find(".subnavlink");
			if(list && list[index]) {
				b = true;
				$(list[index]).click();
			}
		}
		return b;
	}
	
	return {
		init: function(){
			box.listen(HOME.Const.SHOW_MODULE, checkModule);
			box.listen(HOME.Const.CLOSE_MODULE, closeModule);
			box.listen("desktop_onkey", desktopOnKey);
			box.listen("layer_onkey", onKey);
			
			initModule();
		},
		destroy: function() { }
	};
});


HOME.Core.register("plugin-widgetonkey", function(box) {
	
	var lastView = null, parentView = null, theadView = null;;
	var parentHeight = 0, viewHeight = 0, rowHeight = 0, parentOffset = 0;
	
	function load() {
		box.keys.widgetOnkey = widgetOnkey;
	}
	function widgetOnkey(widgets, data) {
		var b = false;
		var w = null;
		for(var i=0; i<widgets.length; i++) {
			w = widgets.eq(i);
			if(w.hasClass("table_view")) {
				if(w[0].id && w[0].id.indexOf("fixedtable") != -1) {
					continue;
				}
				b |= tableviewOnkey(w, data);
			} else if(w.hasClass("list_view")) {
				b |= listviewOnkey(w, data);
			}
		}
		return b;
	}
	
	function tableviewOnkey(view, data) {
		var row = view.find(".selectRow");
		switch(data.key) {
			case HOME.Keys.UP:
				row.removeClass("selectRow");
				if(row.length) row = row.prev(); 
				else row = view.find("tbody tr:last");
				row.addClass("selectRow");
				moveScroll(view, row, false);
				row.select();
				if(row.find("input").length!==0){
					row.find("input:first").focus().select();
				}
				return true;
			case HOME.Keys.DOWN:
				row.removeClass("selectRow");
				if(row.length) row = row.next();
				else row = view.find("tbody tr:first");
				row.addClass("selectRow");
				moveScroll(view, row, true);
				row.select();
				if(row.find("input").length!==0){
					row.find("input:first").focus().select();
				}
				return true;
			case HOME.Keys.ENTER:
				row.click();
				return true;
		}
	}
	
	function listviewOnkey(view, data) {
		var row = view.find(".selectRow");
		switch(data.key) {
			case HOME.Keys.UP:
				row.removeClass("selectRow");
				if(row.length) row = row.prev(); 
				else row = view.find("li:last");
				row.addClass("selectRow");
				moveScroll(view, row, false);
				row.select();
				return true;
			case HOME.Keys.DOWN:
				row.removeClass("selectRow");
				if(row.length) row = row.next();
				else row = view.find("li:first");
				row.addClass("selectRow");
				moveScroll(view, row, true);
				row.select();
				return true;
			case HOME.Keys.ENTER:
				row.click();
				return true;
		}
	}
	
	/**
	 * 按上下箭头时根据内容移动滚动条
	 */
	function moveScroll(view, row, down) {
		if(!row.length) return false;
		
		if(lastView != view) {
			lastView = view;
			viewHeight = lastView.outerHeight();
			theadView = lastView.find("thead");
			parentView = view.parent();
			parentHeight = parentView.height();
			if(parentHeight == viewHeight) {
				parentView = view.parent().parent();
				parentHeight = parentView.height();
				if(parentHeight == viewHeight) {
					parentView = lastView.closest(".window_content_cont");
					parentHeight = parentView.height();
				}
			}
			
			var pp = parentView.css("position");
			if(pp != "absolute" && pp != "relative") {
				parentOffset = parentView.position().top;
			} else {
				parentOffset = 0;
			}
		}
		rowHeight = row.height();
		var position = row.position();
		
		if(parentHeight < viewHeight) {
			if(down) {
				if(position.top + rowHeight - parentOffset > parentHeight) {
					parentView.scrollTop(position.top + rowHeight - parentHeight - parentOffset + parentView.scrollTop());
				} else if(position.top - parentOffset < parentView.scrollTop()) {
					parentView.scrollTop(position.top - parentOffset + parentView.scrollTop() - rowHeight);
				}
			} else {
				rowHeight = theadView.height();
				if(position.top - rowHeight - parentOffset < 0) {
					parentView.scrollTop(parentView.scrollTop() + position.top - parentOffset - rowHeight);
				} else if(position.top - parentOffset > parentHeight) {
					parentView.scrollTop(position.top + rowHeight - parentHeight - parentOffset + parentView.scrollTop());
				}
			}
		}
	}
	
	return {
		init: function(){
			load();
		},
		destroy: function() {
			
		}
	};
});

