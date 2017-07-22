var t = n = 0, count,sh;
var btl=["#btl1","#btl2", "#btl3"];
var img_i=["#i1","#i2","#i3"];
function chengeImg(id){
		 $(btl[n]).fadeOut("500");
		 $(btl[id]).fadeIn("1500");
		 $(img_i[n]).removeClass("num");
		 $(img_i[id]).addClass("num");
		 n=id;
		 timer();
}
function chengeImgTime(){
	if(n<btl.length-1){
		$(btl[n]).fadeOut("500");
		 $(btl[n+1]).fadeIn("1500");
		 $(img_i[n]).removeClass("num");
		 $(img_i[n+1]).addClass("num");
		 n+=1;	
	}else if(n==btl.length-1){
		$(btl[n]).fadeOut("500");
		$(btl[0]).fadeIn("1500");
		$(img_i[n]).removeClass("num");
		$(img_i[0]).addClass("num");
		n=0;
	}
	
}
function timer(){
	 clearInterval(sh);
	 sh=setInterval(chengeImgTime,8000);
}

$(document).ready(function() {
	
	var username = $("#username");
	var password = $("#password");
	var code = $("#code");
	var validatecode = $("#validatecode");
	var submit = $("#submit");
	var loginform = $("#loginform");
	var unprompt = $("#unprompt");
	var pwprompt = $("#pwprompt");
	var vcprompt = $("#vcprompt");
	var message = $("#message");
	
	var hascode = false;
	
	username.val(getCookie("username"));
	
	if(username.val()) {
		password.focus();
	} else {
		username.focus();
	}
	
	for(var i=1;i<btl.length;i++){
		 $(btl[i]).hide();
	}
	 $(img_i[0]).addClass("num");
	 n=0;//当前显示的
	 sh=setInterval(chengeImgTime,10000);
	
	function setUnPrompt() {
		if(username.val()) {
			unprompt.css("display", "none");
		} else {
			unprompt.css("display", "block");
		}
	}
	function setPwPrompt() {
		if(password.val()) {
			pwprompt.css("display", "none");
		} else {
			pwprompt.css("display", "block");
		}
	}
	function setVcPrompt() {
		if(code.val()) {
			vcprompt.css("display", "none");
		} else {
			vcprompt.css("display", "block");
		}
	}
	setUnPrompt();
	setPwPrompt();
	setVcPrompt();
	
	
	unprompt.click(function(e) {
		username.focus();
		if (e.stopPropagation) e.stopPropagation();
		else e.cancelBubble = true;
	});
	pwprompt.click(function(e) {
		password.focus();
		if (e.stopPropagation) e.stopPropagation();
		else e.cancelBubble = true;
	});
	vcprompt.click(function(e) {
		code.focus();
		if (e.stopPropagation) e.stopPropagation();
		else e.cancelBubble = true;
	});
	
	username.focusin(function() {
		unprompt.css("display", "none");
	}).focusout(function() {
		setUnPrompt();
	}).keypress(function() {
		username.removeClass("validate");
		unprompt.css("display", "none");
		
		loadCode();
	});

	password.focusin(function() {
		pwprompt.css("display", "none");
	}).focusout(function(){
		setPwPrompt();
	}).keypress(function() {
		password.removeClass("validate");
		pwprompt.css("display", "none");
		
		loadCode();
	});
	
	code.focusin(function() {
		vcprompt.css("display", "none");
	}).focusout(function(){
		setVcPrompt();
	}).keypress(function() {
		code.removeClass("validate");
		vcprompt.css("display", "none");
	});
	
	code.focus(function() {
		loadCode();
	});
	
	validatecode.click(function() {
		validatecode.empty();
		hascode = false;
		loadCode();
	});
	
	function loadCode() {
		validatecode.show();
		if(!hascode) {
			hascode = true;
			getValidateCode();
		}
	}
	
	function getValidateCode() {
		var url = validatecode.attr("data-path");
		url = url + "?t=" + new Date().getTime();
		validatecode.empty().append("<img alt='点击更换验证码' src='" + url + "' />");
	}
	
	loginform.submit(function() {
		var un = username.val().trim();
		var pw = password.val().trim();
		var vc = code.val().trim();
		
		var b = true;
		if(!un) {
			b = false;
			username.focus();
			username.addClass("validate");
		}
		if(!pw) {
			b = false;
			if(un) {
				password.focus();
			}
			password.addClass("validate");
		}
		if(!vc) {
			b = false;
			if(un && pw) {
				code.focus();
			}
			code.addClass("validate");
		}
		if(b) {
			setCookie("username", un);
			
			vc = vc.toUpperCase();
			var params = {username: un, password: MD5(MD5(pw)+vc), challenge: vc, requestType: "ajax"};
			
			submit.attr("disabled", true);
			var action = loginform.attr("action");
			$.ajax({
				url: action,
				data: params,
				method: "post",
				dataType: "json",
				success: onLoginSuccess,
				error: onLoginError
			});
		}
		return false;
	});
	
	function onLoginSuccess(data) {
		if(data && data.code == "0000") {
			var url = data.result.redirecturl;
			window.location.href = url;
		} else {
			message.text(data.result);
			message.show();
			
			if(data.result == "验证码错误！") {
				code.focus();
				code.val("");
			} else if(data.result == "用户名或密码错误！") {
				password.focus();
				code.val("");
				password.select();
			} else {
				username.focus();
			}
			
			getValidateCode();
		}
		submit.attr("disabled", false);
	}
	function onLoginError() {
		submit.attr("disabled", false);
		message.text("登录失败：" + arguments[1]);
		message.show();
		
		getValidateCode();
	}
	
});


function setCookie(name, value) {
	var expires = new Date();
	expires.setMonth(expires.getMonth() + 1);
	var str = name + "=" + value + "; path=/;expires=" + expires.toGMTString();
	document.cookie = str;
}
function getCookie(name) {
	var re = new RegExp("\\b"+name+"=([^;]*)\\b");
	var arr = re.exec(document.cookie);
	return arr ? arr[1] : "";
}

String.prototype.trim = function(){
	var exp = /(^\s*)|(\s*$)/g;
	return this.replace(exp, '');
};


function ByteMD5(arr,Type)
{
	return binl2hex(coreMD5(arr2binl(arr)))
	function safe_add(x, y)
	{
		var lsw = (x & 0xFFFF) + (y & 0xFFFF)
		var msw = (x >> 16) + (y >> 16) + (lsw >> 16)
		return (msw << 16) | (lsw & 0xFFFF)
	}
	function rol(num, cnt)
	{
		return (num << cnt) | (num >>> (32 - cnt))
	}
	function cmn(q, a, b, x, s, t)
	{
		return safe_add(rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b)
	}
	function ff(a, b, c, d, x, s, t)
	{
		return cmn((b & c) | ((~b) & d), a, b, x, s, t)
	}
	function gg(a, b, c, d, x, s, t)
	{
		return cmn((b & d) | (c & (~d)), a, b, x, s, t)
	}
	function hh(a, b, c, d, x, s, t)
	{
		return cmn(b ^ c ^ d, a, b, x, s, t)
	}
	function ii(a, b, c, d, x, s, t)
	{
		return cmn(c ^ (b | (~d)), a, b, x, s, t)
	}
	function coreMD5(x)
	{
		var a =  1732584193
		var b = -271733879
		var c = -1732584194
		var d =  271733878
		for(var i = 0; i < x.length; i += 16)
		{
			var olda = a
			var oldb = b
			var oldc = c
			var oldd = d
			a = ff(a, b, c, d, x[i+ 0], 7 , -680876936)
			d = ff(d, a, b, c, x[i+ 1], 12, -389564586)
			c = ff(c, d, a, b, x[i+ 2], 17,  606105819)
			b = ff(b, c, d, a, x[i+ 3], 22, -1044525330)
			a = ff(a, b, c, d, x[i+ 4], 7 , -176418897)
			d = ff(d, a, b, c, x[i+ 5], 12,  1200080426)
			c = ff(c, d, a, b, x[i+ 6], 17, -1473231341)
			b = ff(b, c, d, a, x[i+ 7], 22, -45705983)
			a = ff(a, b, c, d, x[i+ 8], 7 ,  1770035416)
			d = ff(d, a, b, c, x[i+ 9], 12, -1958414417)
			c = ff(c, d, a, b, x[i+10], 17, -42063)
			b = ff(b, c, d, a, x[i+11], 22, -1990404162)
			a = ff(a, b, c, d, x[i+12], 7 ,  1804603682)
			d = ff(d, a, b, c, x[i+13], 12, -40341101)
			c = ff(c, d, a, b, x[i+14], 17, -1502002290)
			b = ff(b, c, d, a, x[i+15], 22,  1236535329)
			a = gg(a, b, c, d, x[i+ 1], 5 , -165796510)
			d = gg(d, a, b, c, x[i+ 6], 9 , -1069501632)
			c = gg(c, d, a, b, x[i+11], 14,  643717713)
			b = gg(b, c, d, a, x[i+ 0], 20, -373897302)
			a = gg(a, b, c, d, x[i+ 5], 5 , -701558691)
			d = gg(d, a, b, c, x[i+10], 9 ,  38016083)
			c = gg(c, d, a, b, x[i+15], 14, -660478335)
			b = gg(b, c, d, a, x[i+ 4], 20, -405537848)
			a = gg(a, b, c, d, x[i+ 9], 5 ,  568446438)
			d = gg(d, a, b, c, x[i+14], 9 , -1019803690)
			c = gg(c, d, a, b, x[i+ 3], 14, -187363961)
			b = gg(b, c, d, a, x[i+ 8], 20,  1163531501)
			a = gg(a, b, c, d, x[i+13], 5 , -1444681467)
			d = gg(d, a, b, c, x[i+ 2], 9 , -51403784)
			c = gg(c, d, a, b, x[i+ 7], 14,  1735328473)
			b = gg(b, c, d, a, x[i+12], 20, -1926607734)
			a = hh(a, b, c, d, x[i+ 5], 4 , -378558)
			d = hh(d, a, b, c, x[i+ 8], 11, -2022574463)
			c = hh(c, d, a, b, x[i+11], 16,  1839030562)
			b = hh(b, c, d, a, x[i+14], 23, -35309556)
			a = hh(a, b, c, d, x[i+ 1], 4 , -1530992060)
			d = hh(d, a, b, c, x[i+ 4], 11,  1272893353)
			c = hh(c, d, a, b, x[i+ 7], 16, -155497632)
			b = hh(b, c, d, a, x[i+10], 23, -1094730640)
			a = hh(a, b, c, d, x[i+13], 4 ,  681279174)
			d = hh(d, a, b, c, x[i+ 0], 11, -358537222)
			c = hh(c, d, a, b, x[i+ 3], 16, -722521979)
			b = hh(b, c, d, a, x[i+ 6], 23,  76029189)
			a = hh(a, b, c, d, x[i+ 9], 4 , -640364487)
			d = hh(d, a, b, c, x[i+12], 11, -421815835)
			c = hh(c, d, a, b, x[i+15], 16,  530742520)
			b = hh(b, c, d, a, x[i+ 2], 23, -995338651)
			a = ii(a, b, c, d, x[i+ 0], 6 , -198630844)
			d = ii(d, a, b, c, x[i+ 7], 10,  1126891415)
			c = ii(c, d, a, b, x[i+14], 15, -1416354905)
			b = ii(b, c, d, a, x[i+ 5], 21, -57434055)
			a = ii(a, b, c, d, x[i+12], 6 ,  1700485571)
			d = ii(d, a, b, c, x[i+ 3], 10, -1894986606)
			c = ii(c, d, a, b, x[i+10], 15, -1051523)
			b = ii(b, c, d, a, x[i+ 1], 21, -2054922799)
			a = ii(a, b, c, d, x[i+ 8], 6 ,  1873313359)
			d = ii(d, a, b, c, x[i+15], 10, -30611744)
			c = ii(c, d, a, b, x[i+ 6], 15, -1560198380)
			b = ii(b, c, d, a, x[i+13], 21,  1309151649)
			a = ii(a, b, c, d, x[i+ 4], 6 , -145523070)
			d = ii(d, a, b, c, x[i+11], 10, -1120210379)
			c = ii(c, d, a, b, x[i+ 2], 15,  718787259)
			b = ii(b, c, d, a, x[i+ 9], 21, -343485551)
			a = safe_add(a, olda)
			b = safe_add(b, oldb)
			c = safe_add(c, oldc)
			d = safe_add(d, oldd)
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
		var hex_tab = "0123456789abcdef"
		var str = ""
		for(var i = 0; i < binarray.length * 4; i++)
		{
			str += hex_tab.charAt((binarray[i>>2] >> ((i%4)*8+4)) & 0xF) +
			hex_tab.charAt((binarray[i>>2] >> ((i%4)*8)) & 0xF)
		}
		return str
	}
	function arr2binl(arr)
	{
		var nblk = ((arr.length + 8) >> 6) + 1 
		var blks = new Array(nblk * 16)
		for(var i = 0; i < nblk * 16; i++) blks[i] = 0
			for(var i = 0; i < arr.length; i++)
			blks[i>>2] |= (arr[i] & 0xFF) << ((i%4) * 8)
		blks[i>>2] |= 0x80 << ((i%4) * 8)
		blks[nblk*16-2] = arr.length * 8
		return blks
	}
}

function MD5(s)
{
	s = s+"{ryx}";
	var len=s.length;
	var arr=new Array(len);
	for(var i=0;i<len;i++)
	{
		var cc=s.charCodeAt(i);
		arr[i]=cc&0xFF;
	}
	return ByteMD5(arr,32);	
}