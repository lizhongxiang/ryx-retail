<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<%@ page language="java" pageEncoding="utf-8" buffer="none"
	contentType="text/html;charset=utf-8"%>
<%
String path = request.getContextPath(); 
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/"; 
%>

<html>
<head>
<title>Home</title>
<script type="text/javascript">
function loadXMLDoc(){
	var Url=document.getElementById("postUrl").value;
	var parames=document.getElementById("postParames").value;
	parames=decodeURI(parames); 
	var xmlhttp;
	if (window.XMLHttpRequest){
	  xmlhttp=new XMLHttpRequest();
	}else{
	  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange=function(){
	    document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
	    document.getElementById("result").style.display="";
	  }
	xmlhttp.open("POST",Url,true);
	xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	xmlhttp.send(parames);
}
</script>
</head>
<body>
	<h1>测试用页</h1>
	<div>
		<form action="/tobacco/file/backupDB" method="post" enctype="multipart/form-data">
			<input type="text" name="merch_id" id="merch_id"/>
			<input type="file" name="file" />
			<input type="submit" value="submit" />
		</form>
	</div>
	<div style="magin-left:150px">
		<h3>模拟POST请求</h3>
		<div>
			URL:<input type="text" id="postUrl" style="width:500px" value="<%=basePath%>"><br>
			<br>
			参数:<input type="text" id="postParames" style="width:500px">(需要分页的需要手动添加分页信息"page_index=1&page_size=20">)<br><br>
			参数格式：1、params={"list":[{"item_id":"6901028153898","qty_whse_warn":"10"}]}<br>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			2、begin_date=20140429&end_date=20140529<br>
			<br>
			<button type="button" onclick="loadXMLDoc()">请求数据</button>
		</div>
		<h5 id="result" style="display:none">请求返回结果</h5>
		<div id="myDiv"></div>
	</div>
</body>
</html>
