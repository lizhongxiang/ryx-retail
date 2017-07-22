jQuery.fn.extend({
	val: function(sval) { // 重写JQuery的val方法，增加placeholder的判断
		return function() {
			var ret, place;
			ret = sval.apply( this, arguments );
			place = this.attr("placeholder");
			if(place && ret == place) ret = "";
			return ret;
		};
	}($.fn.val),
	empty: function(sempty) {
		return function() {
			var ret = sempty.apply(this, arguments);
			this.trigger("empty");
			return ret;
		};
	}($.fn.empty),
	autocomplete: function(ac) {
		return function() {
			this.attr("data-keys", "38,40");
			return ac.apply(this, arguments);
		};
	}($.fn.autocomplete)
});

jQuery.extend( $.ui.autocomplete, {		// 扩展JQuery AutoComplete组件的过滤方法，增加search属性
	filter: function(array, term) {
		var matcher = new RegExp( $.ui.autocomplete.escapeRegex(term), "i" );
		return $.grep( array, function(value) {
			//return matcher.test( value.label || value.value || value);
			return matcher.test(value.label || value.value || value) || matcher.test(value.search);
		});
	}
});

jQuery.ui.dialog.prototype._focusTabbable = function() {
	// Set focus to the first match:
	// 1. First element inside the dialog matching [autofocus]
	// 2. Tabbable element inside the content element
	// 3. Tabbable element inside the buttonpane
	// 4. The close button
	// 5. The dialog itself
	var hasFocus = this.element.find("[autofocus]");

	/* 
	 * 2014-06-09
	 * 去掉打开或关闭窗口时，焦点自动定位到文本框或按钮上的问题
	 * 
	if ( !hasFocus.length ) {
		hasFocus = this.element.find(":tabbable");
	}
	if ( !hasFocus.length ) {
		hasFocus = this.uiDialogButtonPane.find(":tabbable");
	}
	if ( !hasFocus.length ) {
		hasFocus = this.uiDialogTitlebarClose.filter(":tabbable");
	}
	*/
	
	if ( !hasFocus.length ) {
		hasFocus = this.uiDialog;
	}
	hasFocus.eq( 0 ).focus();
};

jQuery.extend ({
	obj2str: function(o) {
        if (typeof o == "undefined" || o == undefined) {
            return "";
        }
        var r = [];
        if (typeof o == "number" || typeof o == "boolean") return "\"" + o + "\"";
        if (typeof o == "string") return "\"" + o.replace(/([\"\\])/g, "\\$1").replace(/(\n)/g, "\\n").replace(/(\r)/g, "\\r").replace(/(\t)/g, "\\t") + "\"";
        if (typeof o == "object") {
            if (!o.sort) {
                for (var i in o)
                    r.push("\"" + i + "\":" + $.obj2str(o[i]));
                if (!!document.all && !/^\n?function\s*toString\(\)\s*\{\n?\s*\[native code\]\n?\s*\}\n?\s*$/.test(o.toString)) {
                    r.push("toString:" + o.toString.toString());
                }
                r = "{" + r.join() + "}";
            } else {
                for (var i = 0; i < o.length; i++)
                    r.push($.obj2str(o[i]));
                r = "[" + r.join() + "]";
            }
            return r;
        }
        return o.toString().replace(/\"\:/g, '":""');
    },
    str2obj: function(str) {
    	return jQuery.parseJSON(str);
    },

	/**
	 * 比较大小函数
	 */
	compare: function(v1, v2, option){
		var val1 = v1[option.attr];
		var val2 = v2[option.attr];
		
		if(option.type == 'number'){
			val1 = parseFloat(val1);
			val2 = parseFloat(val2);
		}
		if(val1 > val2) return 1;
		else if(val1 == val2) return 0;
		else return -1;
	},
	
	/**
	 * 排序函数
	 */
	sort: function(data, left, right, option) {
		var i,j,middle;
		i = left;
		j = right;

		middle = left;
		do {
			while((this.compare(data[i],data[middle], option)*option.tag>0) && (i<right))
				i++;
			while((this.compare(data[j],data[middle], option)*option.tag<0) && (j>left))
				j--;
			if(i<=j){
				// 交互位置
				var tmp = data[i];
				data[i] = data[j];
				data[j] = tmp;
				i++;
				j--;
			}
		} while(i<=j);
		if(left<j)
			this.sort(data, left,j, option);
		
		if(right>i)
			this.sort(data, i,right, option);

	},
	round: function(num, digits) {
		var dev = 1;
		if(digits) {
			if(digits == 1) dev = 10;
			else if(digits == 2) dev = 100;
			else if(digits == 3) dev = 1000;
			else if(digits == 4) dev = 10000;
		}
		var num2 = Math.round(num * dev) / dev;
		return num2;
	},
	parseMoney: function(str, def) {
		if(typeof def == "undefined") def = 0;
		var m = parseFloat(str);
		if(isNaN(m) || m == "") m = def;
		if(typeof m == "number") m = m.toFixed(2);
		return m;
	},
	parseQuantity: function(str, def) {
		/*if(typeof def == "undefined") def = 0;
		var m = parseFloat(str);
		if(isNaN(m) || m == "") m = def;
		if(typeof m == "number") m = m.toFixed(2);*/
		var m = $.parseMoney(str, def);
		if(m == parseInt(m)) m = parseInt(m);
		return m;
	},
	parseLength: function(str, len, prep, clip) {
		var size = 0;
		for(var i=0;i<str.length;i++){
			if(str.substring(i, i + 1).match(/[^\x00-\xff]/ig) != null)
				size+=2;
			else
				size+=1;
			if(size >= len) {
				if(clip) {
					if(size == len) {
						return str.substring(0, i+1);
					} else {
						return str.substring(0, i) + " ";
					}
				} else {
					return str;
				}
			}
		}
		for( ;size<len;size++){
			if(!prep)
				str = str + " ";
			else
				str = " " + str;
		}
		return str;
	},
	
	setCookie: function(name, value, path, domain) {
		var expires = new Date();
		expires.setMonth(expires.getMonth() + 1);
		var str = name + "=" + value + "; expires=" + expires.toGMTString();
		if(path) {
			str += "; path=" + path;
		}
		if(domain) {
			str += "; domain="+domain;
		}
		document.cookie = str;
	},
	getCookie: function(name) {
		var re = new RegExp("\\b"+name+"=([^;]*)\\b");
		var arr = re.exec(document.cookie);
		return arr ? arr[1] : "";
	},
	isPlaceholder: function(){
		var input = document.createElement('input');
		return 'placeholder' in input;
	},
	isInput: function(tag) {
		var name = tag.nodeName;
		if(name == "INPUT") {
			if(tag.type == "text" || tag.type == "password") {
				return true;
			}
		} else if(name == "TEXTAREA") {
			return true;
		}
		return false;
	},
	isComponent: function(tag) {
		var name = tag.nodeName;
		if(name == "INPUT" || name == "TEXTAREA" || name == "SELECT"
			|| name == "A" || name == "BUTTON" || name == "IMG") {
			return true;
		}
		return false;
	},
	validate: function(val, verify) {
		if(!val) return false;
		
		var b = true;
		switch(verify) {
			case "int":		// 整数
				b = /^-?\d+$/.test(val);
				break;
			case "pint":	// 正整数+0
				b = /^\d+$/.test(val);
				break;
			case "number":	// 浮点数
				b = /^(-?\d+)(\.\d+)?$/.test(val);
				break;
			case "pnumber":	// 正浮点数+0
				b = /^\d+(\.\d+)?$/.test(val);
				break;
			case "phone":	// 电话
				b = /^\d+$/.test(val);
				break;
			case "email":	// 邮箱
				b = /^\w+((-\w+)|(\.\w+)|(_\w+))*\@[A-Za-z0-9]+((\.|-|_)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/.test(val);
				break;
			case "word": // 数字字母
				b = /^\w*$/.test(val);
				break;
			case "time":	// 24小时制的时间
				b = /^(([0-1]\d)|(2[0-4])):[0-5]\d$/.test(val);
				break;
			case "date":
//				b=/^((((1[8-9]\d{2})|([2-9]\d{3}))([-\/\._])(10|12|0?[13578])([-\/\._])(3[01]|[12][0-9]|0?[1-9])$)|(^((1[8-9]\d{2})|([2-9]\d{3}))([-\/\._])(11|0?[469])([-\/\._])(30|[12][0-9]|0?[1-9])$)|(^((1[8-9]\d{2})|([2-9]\d{3}))([-\/\._])(0?2)([-\/\._])(2[0-8]|1[0-9]|0?[1-9])$)|(^([2468][048]00)([-\/\._])(0?2)([-\/\._])(29)$)|(^([3579][26]00)([-\/\._])(0?2)([-\/\._])(29)$)|(^([1][89][0][48])([-\/\._])(0?2)([-\/\._])(29)$)|(^([2-9][0-9][0][48])([-\/\._])(0?2)([-\/\._])(29)$)|(^([1][89][2468][048])([-\/\._])(0?2)([-\/\._])(29)$)|(^([2-9][0-9][2468][048])([-\/\._])(0?2)([-\/\._])(29)$)|(^([1][89][13579][26])([-\/\._])(0?2)([-\/\._])(29)$)|(^([2-9][0-9][13579][26])([-\/\._])(0?2)([-\/\._])(29)))$/.test(val);
				b=/^(\d{4})-([0-9]{1,2})-([0-9]{1,2})$/.test(val);
				break;
			case "card": // 身份证号
				b = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(val);
				break;
		}
		
		return b;
	},
	// 校验重复
	checkuniq: function(uniqList, messageList) {
		if(messageList) {
			messageList.length=0;
		}
		var b = true;
		if(uniqList && uniqList.length>0) {
			for(var i=0; i<uniqList.length-1; i++) {
				for(var j=i+1; j<uniqList.length; j++) {
					if(uniqList[i].val()==uniqList[j].val()) {
						b = false;
						if(!uniqList[i].hasClass("validate")) uniqList[i].addClass("validate");
						if(!uniqList[j].hasClass("validate")) uniqList[j].addClass("validate");
					}
				}
			}
		}
		if(!b) {
			for(var k=0; k<uniqList.length; k++) {
				if(uniqList[k].attr("uniq-message")) {
					messageList.push(uniqList[k].attr("uniq-message"));
					break;
				}
			}
		}
		return b;
	},
	validateForms: function(form, messageList) {		// 验证容器内的元素
		if(messageList) {
			messageList.length=0;
		}
		var f = form;
		if(typeof f == "string") {
			f = $(f);
		}
		var result = true;
		var emptys = f.find("[data-empty]:enabled");
		for(var i=0; i<emptys.length; i++) {
			var $obj = $(emptys[i]);
			var empty = $obj.attr("data-empty");
			if(empty == "false") {
				if(!$obj.val()) {
					$obj.addClass("validate");
					var m = $obj.attr("verify-message");
					if(messageList && m) {
						messageList.push(m);
					}
					result = false;
				} else {
					$obj.removeClass("validate");
				}
			}
		};
		
		var verifies = f.find("[data-verify]");
		for(var i=0; i<verifies.length; i++) {
			var $obj = $(verifies[i]);
			var verify = $obj.attr("data-verify");
			var val = $obj.val();
			if(val) {
				var b = $.validate(val, verify);
				if(!b) {
					$obj.addClass("validate");
					var m = $obj.attr("verify-message");
					if(messageList && m) {
						messageList.push(m);
					}
					result = false;
				} else {
					$obj.removeClass("validate");
				}
			}
		};
		
		var uniqz = f.find("[data-uniq]");
		var list = [];
		for(var i=0; i<uniqz.length; i++)  {
			var $obj = $(uniqz[i]);
			var uniq = $obj.attr("data-uniq");
			var val = $obj.val();
			if(uniq && val) {
				list.push($obj);
			}
		}
		if(list.length>1) {
			result = $.checkuniq(list, messageList);
		}
		return result;
	},
	getSearchParams: function() {
		var url = location.search;
		var theRequest = new Object();
		if (url.indexOf("?") != -1) {
			var str = url.substr(1);
			strs = str.split("&");
			for(var i = 0; i < strs.length; i ++) {
				theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
			}
		}
		return theRequest;
	},
	inShell: function() {
		if(typeof window.inShell != "undefined") return window.inShell;
		try {
			window.inShell = window.external.test();
		} catch(e) {
			window.inShell = false;
		}
		return window.inShell;
	},
	checkInShell: function(callback) {
		if(typeof window.inShell != "undefined") {
			if(callback) {
				callback(window.inShell);
			}
		} else {
			try {
				window.inShell = window.external.test();
				if(callback) {
					callback(window.inShell);
				}
			} catch(e) {
				//window.inShell = false;
				$.ajax({
					url: "http://pcshell.ruishangtong.com/",
					data: {command: "test", paramstr: ""},
					type: "post",
					success: function() {
						window.inShell = true;
						window.chromeShell = true;
						if(callback) {
							callback(window.inShell);
						}
					},
					error: function() {
						window.inShell = false;
						if(callback) {
							callback(window.inShell);
						}
					}
				});
			}
		}
	},
	ByteMD5: function(arr,Type) {
		return binl2hex(coreMD5(arr2binl(arr)));
		function safe_add(x, y)
		{
			var lsw = (x & 0xFFFF) + (y & 0xFFFF);
			var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
			return (msw << 16) | (lsw & 0xFFFF);
		}
		function rol(num, cnt)
		{
			return (num << cnt) | (num >>> (32 - cnt));
		}
		function cmn(q, a, b, x, s, t)
		{
			return safe_add(rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b);
		}
		function ff(a, b, c, d, x, s, t)
		{
			return cmn((b & c) | ((~b) & d), a, b, x, s, t);
		}
		function gg(a, b, c, d, x, s, t)
		{
			return cmn((b & d) | (c & (~d)), a, b, x, s, t);
		}
		function hh(a, b, c, d, x, s, t)
		{
			return cmn(b ^ c ^ d, a, b, x, s, t);
		}
		function ii(a, b, c, d, x, s, t)
		{
			return cmn(c ^ (b | (~d)), a, b, x, s, t);
		}
		function coreMD5(x)
		{
			var a =  1732584193;
			var b = -271733879;
			var c = -1732584194;
			var d =  271733878;
			for(var i = 0; i < x.length; i += 16)
			{
				var olda = a;
				var oldb = b;
				var oldc = c;
				var oldd = d;
				a = ff(a, b, c, d, x[i+ 0], 7 , -680876936);
				d = ff(d, a, b, c, x[i+ 1], 12, -389564586);
				c = ff(c, d, a, b, x[i+ 2], 17,  606105819);
				b = ff(b, c, d, a, x[i+ 3], 22, -1044525330);
				a = ff(a, b, c, d, x[i+ 4], 7 , -176418897);
				d = ff(d, a, b, c, x[i+ 5], 12,  1200080426);
				c = ff(c, d, a, b, x[i+ 6], 17, -1473231341);
				b = ff(b, c, d, a, x[i+ 7], 22, -45705983);
				a = ff(a, b, c, d, x[i+ 8], 7 ,  1770035416);
				d = ff(d, a, b, c, x[i+ 9], 12, -1958414417);
				c = ff(c, d, a, b, x[i+10], 17, -42063);
				b = ff(b, c, d, a, x[i+11], 22, -1990404162);
				a = ff(a, b, c, d, x[i+12], 7 ,  1804603682);
				d = ff(d, a, b, c, x[i+13], 12, -40341101);
				c = ff(c, d, a, b, x[i+14], 17, -1502002290);
				b = ff(b, c, d, a, x[i+15], 22,  1236535329);
				a = gg(a, b, c, d, x[i+ 1], 5 , -165796510);
				d = gg(d, a, b, c, x[i+ 6], 9 , -1069501632);
				c = gg(c, d, a, b, x[i+11], 14,  643717713);
				b = gg(b, c, d, a, x[i+ 0], 20, -373897302);
				a = gg(a, b, c, d, x[i+ 5], 5 , -701558691);
				d = gg(d, a, b, c, x[i+10], 9 ,  38016083);
				c = gg(c, d, a, b, x[i+15], 14, -660478335);
				b = gg(b, c, d, a, x[i+ 4], 20, -405537848);
				a = gg(a, b, c, d, x[i+ 9], 5 ,  568446438);
				d = gg(d, a, b, c, x[i+14], 9 , -1019803690);
				c = gg(c, d, a, b, x[i+ 3], 14, -187363961);
				b = gg(b, c, d, a, x[i+ 8], 20,  1163531501);
				a = gg(a, b, c, d, x[i+13], 5 , -1444681467);
				d = gg(d, a, b, c, x[i+ 2], 9 , -51403784);
				c = gg(c, d, a, b, x[i+ 7], 14,  1735328473);
				b = gg(b, c, d, a, x[i+12], 20, -1926607734);
				a = hh(a, b, c, d, x[i+ 5], 4 , -378558);
				d = hh(d, a, b, c, x[i+ 8], 11, -2022574463);
				c = hh(c, d, a, b, x[i+11], 16,  1839030562);
				b = hh(b, c, d, a, x[i+14], 23, -35309556);
				a = hh(a, b, c, d, x[i+ 1], 4 , -1530992060);
				d = hh(d, a, b, c, x[i+ 4], 11,  1272893353);
				c = hh(c, d, a, b, x[i+ 7], 16, -155497632);
				b = hh(b, c, d, a, x[i+10], 23, -1094730640);
				a = hh(a, b, c, d, x[i+13], 4 ,  681279174);
				d = hh(d, a, b, c, x[i+ 0], 11, -358537222);
				c = hh(c, d, a, b, x[i+ 3], 16, -722521979);
				b = hh(b, c, d, a, x[i+ 6], 23,  76029189);
				a = hh(a, b, c, d, x[i+ 9], 4 , -640364487);
				d = hh(d, a, b, c, x[i+12], 11, -421815835);
				c = hh(c, d, a, b, x[i+15], 16,  530742520);
				b = hh(b, c, d, a, x[i+ 2], 23, -995338651);
				a = ii(a, b, c, d, x[i+ 0], 6 , -198630844);
				d = ii(d, a, b, c, x[i+ 7], 10,  1126891415);
				c = ii(c, d, a, b, x[i+14], 15, -1416354905);
				b = ii(b, c, d, a, x[i+ 5], 21, -57434055);
				a = ii(a, b, c, d, x[i+12], 6 ,  1700485571);
				d = ii(d, a, b, c, x[i+ 3], 10, -1894986606);
				c = ii(c, d, a, b, x[i+10], 15, -1051523);
				b = ii(b, c, d, a, x[i+ 1], 21, -2054922799);
				a = ii(a, b, c, d, x[i+ 8], 6 ,  1873313359);
				d = ii(d, a, b, c, x[i+15], 10, -30611744);
				c = ii(c, d, a, b, x[i+ 6], 15, -1560198380);
				b = ii(b, c, d, a, x[i+13], 21,  1309151649);
				a = ii(a, b, c, d, x[i+ 4], 6 , -145523070);
				d = ii(d, a, b, c, x[i+11], 10, -1120210379);
				c = ii(c, d, a, b, x[i+ 2], 15,  718787259);
				b = ii(b, c, d, a, x[i+ 9], 21, -343485551);
				a = safe_add(a, olda);
				b = safe_add(b, oldb);
				c = safe_add(c, oldc);
				d = safe_add(d, oldd);
			}
			if (Type == 32)
			{
				return [a, b, c, d];
			}		
			else
			{
				return [b, c]; 
			}
		}
		function binl2hex(binarray)
		{
			var hex_tab = "0123456789abcdef";
			var str = "";
			for(var i = 0; i < binarray.length * 4; i++)
			{
				str += hex_tab.charAt((binarray[i>>2] >> ((i%4)*8+4)) & 0xF) +
				hex_tab.charAt((binarray[i>>2] >> ((i%4)*8)) & 0xF);
			}
			return str;
		}
		function arr2binl(arr)
		{
			var nblk = ((arr.length + 8) >> 6) + 1 ;
			var blks = new Array(nblk * 16);
			for(var i = 0; i < nblk * 16; i++) blks[i] = 0;
				for(var i = 0; i < arr.length; i++)
				blks[i>>2] |= (arr[i] & 0xFF) << ((i%4) * 8);
			blks[i>>2] |= 0x80 << ((i%4) * 8);
			blks[nblk*16-2] = arr.length * 8;
			return blks;
		}
	},
	MD5: function(s)
	{
		s = s+"{ryx}";
		var len=s.length;
		var arr=new Array(len);
		for(var i=0;i<len;i++)
		{
			var cc=s.charCodeAt(i);
			arr[i]=cc&0xFF;
		}
		return $.ByteMD5(arr,32);	
	},
	//日期格式转换
	//formatStr:yyyy-mm-dd,yyyymmdd
	GetDateFormat:function(myDate,oldFormatStr,newFormatStr){
		oldFormatStr=oldFormatStr.toLowerCase();
		newFormatStr=newFormatStr.toLowerCase();
		var newDate=null;//返回的时间
		var dateArr= new Array(); //定义yyyy,mm,dd一数组
		var yIndex=null;
		var mIndex=null;
		var dIndex=null;
		//盘点格式不是yyyymmdd
		
		yIndex=oldFormatStr.indexOf("yyyy");
		mIndex=oldFormatStr.indexOf("mm");
		dIndex=oldFormatStr.indexOf("dd");
		
		dateArr.push(myDate.substring(yIndex,yIndex+4));
		dateArr.push(myDate.substring(mIndex,mIndex+2));
		dateArr.push(myDate.substring(dIndex,dIndex+2));
		switch(newFormatStr) 
		{
			case "yyyy-mm-dd":
				newDate=dateArr[0]+"-"+dateArr[1]+"-"+dateArr[2];
				break;
			case "yyyymmdd":
				newDate=dateArr[0]+""+dateArr[1]+""+dateArr[2];
				break;
			case "yyyy/mm/dd":
				
				break;
			case "yyyy年mm月dd日":
				newDate=dateArr[0]+"年"+dateArr[1]+"月"+dateArr[2]+"日";
				break;
			case "mmddyyyy":
				newDate=dateArr[1]+""+dateArr[2]+""+dateArr[0];
				break;
			case "yymmdd":
				newDate=(dateArr[0].substring(2,4))+""+dateArr[1]+""+dateArr[2];
				break;
			default:
				newDate=dateArr[0]+""+dateArr[1]+""+dateArr[2];
		}
		return newDate;	
	},
	GetToday: function(pastDay) {
		//获得日期，传入数字，负数表示：前几天的日期；正数：表示未来日期
		var d = new Date();
		if(pastDay){
			d.setTime(d.getTime() + (pastDay * 24 * 60 * 60 * 1000));
		}
		var year = d.getFullYear();
		var month = d.getMonth() + 1;
		var date = d.getDate();
		var curDateTime = "" + year+"-";
		if (month > 9){
			curDateTime = curDateTime + month+"-";
		}
		else{
			curDateTime = curDateTime + "0" + month+"-";
		}
		if (date > 9){
			curDateTime = curDateTime + date;
		}
		else{
			date="0"+date;
			if(date==00){
				date="01";
			}
			curDateTime = curDateTime + date;
		}
		return curDateTime;	
	},
	GetCurrentTime:function (){
		var today=new Date();                           
		var h=today.getHours();
		var m=today.getMinutes();
		var s=today.getSeconds();
		if(h<10){
			h="0" + h;
		}
		if (m<10){
			m="0" + m;
		}
		if (s<10){
			s="0" + s;
		}
		return h+""+m+""+s;
	}
});


String.prototype.trim = function(){
	var exp = /(^\s*)|(\s*$)/g;
	return this.replace(exp, '');
};

String.prototype.isEmpty = function(){
    if (this != null && !/^\s*$/.test(this)) {
        return false;
    } else {
        return true;
    }
};
Date.prototype.format =function(format)
{
	var o = {
		"M+" : this.getMonth()+1, //month
		"d+" : this.getDate(), //day
		"h+" : this.getHours(), //hour
		"m+" : this.getMinutes(), //minute
		"s+" : this.getSeconds(), //second
		"q+" : Math.floor((this.getMonth()+3)/3), //quarter
		"S" : this.getMilliseconds() //millisecond
	};
	if(/(y+)/.test(format)) 
		format=format.replace(RegExp.$1, (this.getFullYear()+"").substr(4- RegExp.$1.length));
	for(var k in o)
		if(new RegExp("("+ k +")").test(format))
			format = format.replace(RegExp.$1, RegExp.$1.length==1? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
	return format;
};