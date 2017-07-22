/**
 */
jQuery.fn.extend({
	fixedtableheader : function(options) {
		var settings = jQuery.extend({
			headerrowsize : 1,
			offset: 0
		}, options);
		var parent = options.parent;
		var win = options.win;
		if(!parent) parent = $("body");
		if(!win) win = $(window);
		this
				.each(function(i) {
					var $tbl = $(this);
					
					var $tblhfixed = $tbl.find("tr:lt(" + settings.headerrowsize + ")");
					var headerelement = "th";
					if ($tblhfixed.find(headerelement).length == 0)
						headerelement = "td";
					if ($tblhfixed.find(headerelement).length > 0) {
						$tblhfixed.find(headerelement).each(function() {
							//$(this).css("width", $(this).width());
						});
						var $clonedTable = $tbl.clone().empty();
						$clonedTable.removeClass("clickable");
						
						var tblwidth = GetTblWidth($tbl);
						$clonedTable.attr("id", "fixedtableheader" + i).css({
							"position" : "fixed"
						}).append($tblhfixed.clone(true)).width(tblwidth).prependTo(parent);
						
						if(options.isshow) {
							$clonedTable.show();
							var ptop = win.offset().top;
							
							$clonedTable.css({
								"position" : "fixed"
							});
						} else {
							$clonedTable.hide();
						}
						
						var onScroll = function(e) {
							var ptop = win.offset().top;
							
							$clonedTable.css({
								"position" : "fixed"
							});
							
							var sctop = win.scrollTop();
							var elmtop = $tblhfixed.offset().top - ptop;
							if (sctop + settings.offset > elmtop
									&& sctop <= (ptop + $tbl.height() - $tblhfixed.height())) {
								$clonedTable.show();
								$clonedTable.width(GetTblWidth($tbl));
							} else {
								if(!options.isshow) {
									$clonedTable.hide();
								}
							}
						};

						if(!options.isshow) {
							win.scroll(onScroll);
						}
							
						/*
						$tbl.parent().resize(function(e) {
							if(e.currentTarget == e.target) {
								var ptop = win.offset().top;
								
								$clonedTable.css({
									"position" : "fixed",
									"top" : ptop + settings.offset,
									"left" : $tbl.offset().left - win.scrollLeft()
								});
								
								$clonedTable.width(GetTblWidth($tbl));
							}
						});
						*/
						var w = win.closest(".ui-resizable");
						if(!w.length) {
							w = win;
						}
						
						var onResize = function(e) {
							if(e.currentTarget == e.target) {
								if ($clonedTable.outerWidth() != $tbl
										.outerWidth()) {
									$tblhfixed.find(headerelement).each(
											function(index) {
												var w = $(this).width();
												//$(this).css("width", w);
												$clonedTable
														.find(headerelement)
														.eq(index).css("width", w);
											});
									$clonedTable.width($tbl.outerWidth());
								}
								var os = $tbl.offset();
								$clonedTable.css("left", os.left);
								$clonedTable.css("top", os.top + win.scrollTop());
							}
						};
						w.resize(onResize);
					}
				});
		
		function GetTblWidth($tbl) {
			var tblwidth = $tbl.outerWidth();
			return tblwidth;
		}
		return this;
	},
	fixedtablefooter : function(options) {
		var settings = jQuery.extend({
			footerrowsize : 1,
			offset: 0
		}, options);
		var parent = options.parent;
		var win = options.win;
		if(!parent) parent = $("body");
		if(!win) win = $(window);
		this
				.each(function(i) {
					var $tbl = $(this);
					
					var $tblhfixed = $tbl.find(".tfoot tr");
					var headerelement = "td";
					if ($tblhfixed.find(headerelement).length > 0) {
						$tblhfixed.find(headerelement).each(function() {
							$(this).css("width", $(this).width());
						});
						var $clonedTable = $tbl.clone().empty();
						$clonedTable.removeClass("clickable");
						
						var tblwidth = GetTblWidth($tbl);
						$clonedTable.attr("id", "fixedtablefooter" + i).css({
							"position" : "fixed",
							"bottom" : settings.offset,
							"left" : $tbl.offset().left
						}).append($tblhfixed.clone(true)).width(tblwidth).appendTo(parent);
						
						$clonedTable.find("tbody").addClass("tfoot");
						
						if(options.isshow) {
							$clonedTable.show();
							var ptop = win.offset().top;
							var pheight = win.height();
							
							$clonedTable.css({
								"position" : "fixed",
								"top" : ptop + pheight + settings.offset - $clonedTable.height(),
								"left" : $tbl.offset().left - win.scrollLeft()
							});
						} else {
							$clonedTable.hide();
						}
						
						var onScroll = function(e) {
							var ptop = win.offset().top;
							var pheight = win.height();
							
							$clonedTable.css({
								"position" : "fixed",
								"top" : ptop + pheight + settings.offset - $clonedTable.height(),
								"left" : $tbl.offset().left - win.scrollLeft()
							});
							
							//ptop + pheight + settings.offset - $clonedTable.height(),
							var tfoot = $(this).find(".table_view .tfoot");
							if(!tfoot[0]) tfoot = $tbl.find(".tfoot");
							if(tfoot[0]) {
								var sctop = win.scrollTop();
								var elmtop = tfoot.offset().top + tfoot.height() + 2;
								if(elmtop > ptop + pheight) {
									$clonedTable.show();
									$clonedTable.width(GetTblWidth($tbl));
								} else {
									if(!options.isshow) {
										$clonedTable.hide();
									}
								}
							}
						};

						if(!options.isshow) {
							win.scroll(onScroll);
						}
						onScroll();
						/*
						$tbl.closest(".window_content_cont").resize(function(e) {
							if(e.currentTarget == e.target) {
								var ptop = win.offset().top;
								var pheight = win.height();
								
								$clonedTable.css({
									"position" : "fixed",
									"top" : ptop + pheight + settings.offset - $clonedTable.height(),
									"left" : $tbl.offset().left - win.scrollLeft()
								});
								
								$clonedTable.width(GetTblWidth($tbl));
							}
						});
						*/
						var w = win.closest(".window_content_cont");
						if(!w.length) {
							w = win;
						}
						
						var onResize = function(e) {
							if(e.currentTarget == e.target) {
								if ($clonedTable.outerWidth() != $tbl
										.outerWidth()) {
									$tblhfixed.find(headerelement).each(
											function(index) {
												var w = $(this).width();
												//$(this).css("width", w);
												$clonedTable
														.find(headerelement)
														.eq(index).css("width", w);
											});
									$clonedTable.width($tbl.outerWidth());
								}
								
								var ptop = win.offset().top;
								var pheight = win.height();
								$clonedTable.css("left", $tbl.offset().left - win.scrollLeft());
								$clonedTable.css("top", ptop + pheight + settings.offset - $clonedTable.height());
							}
						};
						w.bind("footer_resize", onResize);
						//footer_resize
					}
				});
		
		function GetTblWidth($tbl) {
			var tblwidth = $tbl.outerWidth();
			return tblwidth;
		}
		return this;
	}
});