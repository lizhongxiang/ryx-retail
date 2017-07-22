/**
 * 消费者促销活动功能
 */
HOME.Core.register("plugin-cxfb", function(box) {

	var parentView = null;
	var content = null;
	var updateOrAdd = null;//用于判断新增还是修改
	var showDialogTile = "新增";
	
	function onload(view) {
		parentView = view;
		pageIndex = 1;
		pageSize = 20;
		
		beginTime=$.GetToday(-30);
		endTime=$.GetToday(30);
		
		var beginTimeFormat=new Date(beginTime.substr(0, 4),beginTime.substr(5, 2)-1,beginTime.substr(8, 2));
		beginTimeFormat=beginTimeFormat.format("yyyyMMdd");
		
		var endTimeFormat=new Date(endTime.substr(0, 4),endTime.substr(5, 2)-1,endTime.substr(8, 2));
		endTimeFormat=endTimeFormat.format("yyyyMMdd");
		
		showCXFBList();
	}
	
	function showCXFBList(){
		
		content=box.ich.view_cxfb();
		parentView.empty().append(content);
		
		//初始日期控件
		content.find("#beginTime").val(beginTime); 
		content.find("#endTime").val(endTime);
		content.find("#beginTime").datepicker();
		content.find("#endTime").datepicker();
		//添加tr监听  用作update
		updateOrAdd = "update";
		content.find("table tbody tr").unbind("click").click(onAddCXFB);
		
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
		
		//点击新增发布活动,清空数据
		parentView.find("#add").unbind("click").click(function (){
			updateOrAdd ="add";
			onAddCXFB();
		});

	}
	
	//进入新增/修改促销发布主界面
	function onAddCXFB(data){
		
		if(updateOrAdd =="add" || updateOrAdd ==null){
			updateOrAdd = "add";
			showDialogTile = "新增";
		}else if(updateOrAdd =="update"){
			updateOrAdd ="update";
			showDialogTile = "修改";
		}
		
		content=box.ich.view_cxfb_add();
		//parentView.empty().append(content);
		box.showDialog({
			title: showDialogTile+"促销发布",
			width:780,
			height:550,
			content: content,
			close:function(e) {
				parentView.find("#add").removeAttr("disabled");
			}
		});
		
		
		content.find("#startdate").datepicker({
			minDate: 'd'
		});
		content.find("#enddate").datepicker({
			minDate: 'd' 
		});
		
		content.find(".up_pic").unbind("click").click(clickFile);//点击图片后点击上传文件按钮
		
		var dels = content.find(".img-item-list .icon-img-remove");//选择图片上的删除小按钮
		
		dels.unbind("click").click(function(){//点击按钮删除图片
			$(this).parent(".img-item").remove();
		});
		
		$('.img-item-list').sortable();
	}
	//图片拖拽排序
	 (function($) {
		 var dragging, placeholders = $();
		 $.fn.sortable = function(options) {
		     var method = String(options);
		     options = $.extend({
		         connectWith: false
		     }, options);
		     return this.each(function() {
		         // 参数里有enable或disable或destroy
		         if (/^enable|disable|destroy$/.test(method)) {
		             // 有enable，设置后代元素的draggable属性为true，否则为false
		             var items = $(this).children($(this).data('items')).attr('draggable', method == 'enable');
		             // 有destroy属性，则是移除相关数据和事件
		             if (method == 'destroy') {
		                 items.add(this).removeData('connectWith items')
		                     .off('dragstart.h5s dragend.h5s selectstart.h5s dragover.h5s dragenter.h5s drop.h5s');
		             }
		             return;
		         }
		         // items是后代元素（依据参数的items属性来定）
		         var isHandle, index, items = $(this).children(options.items);
		         // placeholder是，<ul class="sortable-placeholder"></ul>，或<div class="sortable-placeholder"></div>
		         var placeholder = $('<' + (/^ul|ol$/i.test(this.tagName) ? 'li' : 'div') + ' class="sortable-placeholder">');
		         // 依据参数的handler属性,来绑定更后代元素的isHandle
		         items.find(options.handle).mousedown(function() {
		             isHandle = true;
		         }).mouseup(function() {
		             isHandle = false;
		         });
		         // 给元素的数据属性items赋值
		         $(this).data('items', options.items);
		         // placeholders放入placeholder
		         placeholders = placeholders.add(placeholder);
		         if (options.connectWith) {
		             $(options.connectWith).add(this).data('connectWith', options.connectWith);
		         }

		         // 上面代码是做数据环境准备，下面代码开始绑定事件 


		         items.attr('draggable', 'true').on('dragstart.h5s', function(e) {
		             if (options.handle && !isHandle) {
		                 return false;
		             }
		             isHandle = false;
		             // dataTransfer 是 拖拽元素的数据接口
		             var dt = e.originalEvent.dataTransfer;
		             // effectAllowed 拖拽效果
		             dt.effectAllowed = 'move';
		             // 为拖拽元素添加指定数据
		             dt.setData('Text', 'dummy');
		             // dragging是正在拖拽的元素，index是该元素所在数组的位置
		             index = (dragging = $(this)).addClass('sortable-dragging').index();
		         }).on('dragend.h5s', function() {
		             dragging.removeClass('sortable-dragging').show();
		             // 移除 placeholders，但保留事件
		             placeholders.detach();
		             if (index != dragging.index()) {
		                 items.parent().trigger('sortupdate', {item: dragging});
		             }
		             // 释放引用
		             dragging = null;
		         }).not('a[href], img').on('selectstart.h5s', function() {
		             // 当元素选中时，阻止元素背景色边蓝
		             this.dragDrop && this.dragDrop();
		             return false;
		         }).end().add([this, placeholder]).on('dragover.h5s dragenter.h5s drop.h5s', function(e) {
		             // 注意dragenter，dragover，drop的this是目标元素

		             // 拖拽元素，不是items集合内，不给拖拽
		             if (!items.is(dragging) && options.connectWith !== $(dragging).parent().data('connectWith')) {
		                 return true;
		             }
		             if (e.type == 'drop') {
		                 e.stopPropagation();
		                 // 当拖拽的对象，被放下。则在坑后面填入多拽的对象
		                 placeholders.filter(':visible').after(dragging);
		                 return false;
		             }
		             e.preventDefault();
		             e.originalEvent.dataTransfer.dropEffect = 'move';
		             if (items.is(this)) {

		                 if (options.forcePlaceholderSize) {
		                     // 大多数情况下，这一步不会实现
		                     placeholder.height(dragging.outerHeight());
		                 }
		                 // 隐藏被拖动的元素
		                 dragging.hide();
		                 $(this)[placeholder.index() < $(this).index() ? 'after' : 'before'](placeholder);
		                 placeholders.not(placeholder).detach();
		                 console.log(this);

		             } else if (!placeholders.is(this) && !$(this).children(options.items).length) {
		                 placeholders.detach();
		                 $(this).append(placeholder);
		             }
		             return false;
		         });
		     });
		 };
		 })(jQuery);

	
    function clickFile(){
    	content.find("#fileOne").unbind("click").click();
    }
    
	function onClose(){
		beginTime = null; 
		endTime = null;
	}
	return {
		init: function(){
			box.listen("cxfb", onload);
		},
		destroy: function() { }
	};
});