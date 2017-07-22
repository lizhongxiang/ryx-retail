<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" pageEncoding="utf-8" buffer="none" contentType="text/html;charset=utf-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">

	<title>零售户进销存</title>
	<script src="${pageContext.request.contextPath}/public/js/common/jquery-1.9.1.js" type="text/javascript"></script>
	
	<script type="text/javascript">
	
	var win = $(window);
	var barstart1 = false;
	var barstart2 = false;
	var barcode = "";
	var time = -1;
	var start;
	
	win.keydown(onKeyDown);
	
	function onKeyDown(e){
		var key = e.keyCode;
		//box.console.log(key);
		$("#container").append(key + " ");
		
		if(key == 9 || key == 13) {
			if(time > 0) {
				barInputEnd();
			}
			return;
		}
			
		
		barcode += String.fromCharCode(key);
		if(time <= 0) {
			start = new Date().getTime();
			time = setTimeout(onTimeout, 100);
		}
	}
	
	function onTimeout() {
		barcode = "";
		time = -1;
	}
	
	function barInputEnd() {
		var b = barcode;
		barInputCancel();
		
		if(b) {
			var span = new Date().getTime() - start;
			$("#container").append("<br />span:" + span);
			//box.console.log(b);
			$("#container").append("<br />" + b + "<br />");
		}
	}
	
	function barInputCancel() {
		barstart1 = false;
		barstart2 = false;
		barcode = "";
		clearTimeout(time);
		time = 0;
	}
	
	</script>
</head>
<body >
	
	<div id="container"></div>
	<!-- 
	<textarea rows="20" cols="20"></textarea>
	 -->
</body>
</html>
