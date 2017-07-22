/**
 * 投放策略
 */
HOME.Core.register("plugin-tfcl", function(box) {
	var getImportitemQtyUrl = "retail/tobacco/getImportitemQty";
	
	var parentView = null;
	var tabAreaTypeMenu = null;     //商品类型模块
	var tabContent = null;			//tab内容

	var tableList = null;
	var areaTypeMenu = null;		
	
	var item_type = "2";				//商品类型id
	var position_name = "";
	var com_type = "";
	
	function onload(view) {
		
		parentView = view;

		var params = {item_type:'2',position_name:'1',com_type:'1'};
		
		var content = box.ich.view_tfcl();
		parentView.empty().append(content);	

		tabAreaTypeMenu = content.find(".tab_area_type_menu");
		tabContent = content.find(".tab_content");
		
		areaTypeMenu = box.ich.tab_area_type_menu_ul();
		tabAreaTypeMenu.empty().append(areaTypeMenu);
		
		initOrderView(params);
		
		parentView.find(".tab_item_type_menu li").unbind("click").click(onMeClick);
		
		tabAreaTypeMenu.find(".area_type_meun li").unbind("click").click(onAreaLiClick);
		
	}
	
	function initOrderView(params) {
		box.request({
			url: box.getContextPath()+getImportitemQtyUrl,
			data:{params: $.obj2str(params)},
			success: showTFCL
		});
	}
	
	function showTFCL(data){
		if(data){
			var result = data.result;
			tableList = box.ich.view_table_list({list:result});
			tabContent.empty().append(tableList);	
		}
	}

	function onMeClick(){
		var params = {};
		parentView.find(".tab_item_type_menu li").removeClass("on");
		$(this).addClass("on");
		var menuLiId = $(this).attr("id");
		if(menuLiId == "jql"){
			params.item_type = '2';
			areaTypeMenu = box.ich.tab_area_type_menu_ul();				//添加区域tab菜单
			tabAreaTypeMenu.empty().append(areaTypeMenu);
			tabAreaTypeMenu.find(".area_type_meun li").unbind("click").click(onAreaLiClick);
			initOrderView(params);
		}else if(menuLiId == "cgdl"){
			params.item_type = '1';
			tabAreaTypeMenu.empty().append();
			initOrderView(params);
		}
	}
	
	function onAreaLiClick(event){
		tabAreaTypeMenu.find(".area_type_meun li").removeClass("on");
		$(this).addClass("on");
		var areaMenuLiId = $(this).attr("id");
		if(areaMenuLiId=='qcq'){
			position_name = '1';
			com_type = '1';
		}else if(areaMenuLiId=='scq'){
			position_name = '1';
			com_type = '2';
		}else if(areaMenuLiId=='zq'){
			position_name = '2';
		}else if(areaMenuLiId=='hn'){
			position_name = '3';
		}else if(areaMenuLiId=='cn'){
			position_name = '4';
		}
		var params = {item_type:item_type,position_name:position_name,com_type:com_type};

		initOrderView(params);
	}
	return {
		init: function(){
			box.listen("tfcl", onload);
		},
		destroy: function() { }
	};
});