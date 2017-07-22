
/**
 * 表格排序Jquery插件
 * 插件名称：tablesort
 * 使用方法：$('#myTableId').tablesort([{col:1,order:'desc',method:'advance',type:'string',attr:'class'}])
 * 参数选项说明：
 *	col:	排序的列,从0开始
 *	order:	第一次点击时的排序顺序，升序：asc、降序：desc
 *	method:	简单排序：simply，高级排序：advance
 *	type：	数字排序：number，字符串排序：string
 *	attr:	单元格排序关键字属性名称，若缺省，则使用单元格文字排序
 * 	
 * 注意：表格排序部分必须包含在<tbody></tbody>标签之内.
 *
 */
 
jQuery.fn.extend({
	tablesort : function(options,num){
		var $table = $(this);
		var lastClicked;//标记上一次点击的表头位置
		$.each(options, function(i, option) {
			var $tr = $table.find('thead').find('tr');
			var $td = getTh($tr, option);
			option.order = option.order == "desc" ? "desc" : "asc";
			$td.attr("class", "tableSortA");
			
			$td.attr("data-order", option.order)
				.click(function(e) {
					if(lastClicked){//恢复默认样式
						lastClicked.attr("class", "tableSortA");
					}
					lastClicked = $(this);
					var order = $(this).attr("data-order");
					option.order = order;
					$(this).attr("data-order", order == "asc"? "desc" : "asc");
					$(this).attr("class", "tableSortA datagrid-sort-"+option.order);
					$table.runTableSort(option);
					
					//$table.find("tbody tr:even").removeClass("oddRow");
					//$table.find("tbody tr:odd").addClass("oddRow");
					if(num!=null&& typeof(num)!= "undifined"){
						$table.tableOrdinal(num);
					}
				});
			$td.html("<span>"+$td.text()+"</span>"+"<span class='datagrid-sort-icon'>&nbsp;</span>");
		});
		
		//获得单元格排序关键字
		function getTh($tr, option){
			return $tr.find('th').eq(option.col);
		}
		return this;
	},
	
	runTableSort : function(options){
		
		//默认选项
		var defaults = { 
			col:"0", 			//排序列
			order:"desc",		//升序asc、降序desc
			method:"datasort",	//简单排序：simply，高级排序：advance，数据排序后在设置DOM：datasort
			type:"string",		//数字排序：number，字符串排序：string
			attr:""			//单元格排序关键字属性
		};
		//排序方式标识
		var order;
		//排序表格的Jquery对象
		var $table;
		//选项融合
		var options = $.extend(defaults, options); 
		
		//排序开始
		return this.each(function(){
			$table = $(this);
			
			if(options.order == 'asc'){
				order = -1;
			}
			else{
				order = 1;
			}
			
			
			if(options.method == 'simple'){
				//调用选择排序
				selectSort();
			}
			else if(options.method == 'advance'){
				//调用快速排序
				quickSort();
			}
			else if(options.method == 'datasort') {
				dataQuickSort();
			}
			else{
				//默认快速排序
				quickSort();
			}		
			
		});	
		
		/**
		 * 以下为私有函数 
		 */
		
		//比较大小函数
		function compare(v1,v2){
			if(options.type == 'number'){
				v1 = parseFloat(v1);
				v2 = parseFloat(v2);

				if(isNaN(v1)) {
					return 1;
				} else if(isNaN(v2)) {
					return -1;
				}
			}
			if(v1>v2) return 1;
			else if(v1 == v2) return 0;
			else return -1;
		}
		
		
		//获得单元格排序关键字
		function getTdValue($tr){
			if(options.attr == '')
				return $tr.find('td').eq(options.col).text();
			else 
				return $tr.find('td').eq(options.col).attr(options.attr);	
		}
		
		//选择排序实现函数
		function selectSort(){
			var $tr = $table.find('tbody').find('tr');
			var trLen = $tr.size();
			for(var i=0;i< trLen-1;i++){
				//求极值
				var pos = i;
				for(var j = i+1; j<trLen ; j++){
					if(compare(getTdValue($tr.eq(j)),getTdValue($tr.eq(pos)))*order>0 ){
						pos = j;
					}
				}		
				//交换		
				if(compare(getTdValue($tr.eq(pos)),getTdValue($tr.eq(i)))*order>0 ){
					
					var temp1 = $tr.eq(pos).clone();
					var temp2 = $tr.eq(i).clone();
					$tr.eq(pos).replaceWith(temp2);
					$tr.eq(i).replaceWith(temp1);
					//下面这句很重要
					$tr = $table.find('tbody').find('tr');
				}
			}
		}
	
		//快速排序递归实现	
		function doQuickSort(left,right){
		
			var $tr = $table.find('tbody').find('tr');
			var trLen = $tr.size();
			var i,j,middle;
			i = left;
			j = right;
	
			middle = left;
			do {
				while((compare(getTdValue($tr.eq(i)),getTdValue($tr.eq(middle)))*order>0) && (i<right))
					i++;
				while((compare(getTdValue($tr.eq(j)),getTdValue($tr.eq(middle)))*order<0) && (j>left))
					j--;
				if(i<=j){
					var temp1 = $tr.eq(i).clone();
					var temp2 = $tr.eq(j).clone();
					$tr.eq(i).replaceWith(temp2);
					$tr.eq(j).replaceWith(temp1);
					//下面这句很重要
					$tr = $table.find('tbody').find('tr');
					i++;
					j--;
				}
			}while(i<=j);
			if(left<j)
				doQuickSort(left,j);
			
			if(right>i)
				doQuickSort(i,right);
		}
		
		function quickSort(){
			doQuickSort(0,$table.find('tbody').find('tr').size()-1);
		}
		
		function dataQuickSort() {
			var $tr = $table.find('tbody').find('tr');
			var trLen = $tr.size();
			var data = new Array(trLen);
			for(var i=0; i<trLen; ++i) {
				var obj = {index:i, value:getTdValue($tr.eq(i))};
				data[i] = obj;
			}
			// 排序data中的数据
			doDataQuickSort(data, 0, trLen-1);
			
			var $trclone = $tr.clone(true);
			for(var i=0; i<trLen; ++i) {
				var obj = data[i];
				var tmp = $trclone.eq(obj.index).clone(true);
				$tr.eq(i).replaceWith(tmp);
			}
		}
		function doDataQuickSort(data, left, right) {
			var len = data.length;
			var i,j,middle;
			i = left;
			j = right;
	
			middle = left;
			do {
				while((compare(data[i].value,data[middle].value)*order>0) && (i<right))
					i++;
				while((compare(data[j].value,data[middle].value)*order<0) && (j>left))
					j--;
				if(i<=j){
					// 交互位置
					var tmp = data[i];
					data[i] = data[j];
					data[j] = tmp;
					i++;
					j--;
				}
			}while(i<=j);
			if(left<j)
				doDataQuickSort(data, left,j);
			
			if(right>i)
				doDataQuickSort(data, i,right);
		}
		return this;
	},
	
	tableOrdinal : function(num){//重排表格中序数，输入参数为表格的列数，重0开始数起
		var $table = this;
		var trTotal = $table.find("tbody").find("tr");
		for(var i=0;i<trTotal.size();i++){
			var $tr = trTotal.get(i);
			jQuery($tr).find("td").eq(num).html((i+1));
		}
	}
}); 