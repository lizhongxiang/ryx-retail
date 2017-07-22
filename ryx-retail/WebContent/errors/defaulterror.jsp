<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" pageEncoding="utf-8" buffer="none" contentType="text/html;charset=utf-8"%>

<html>
<head>
	<title>errMsg</title>
</head>
<body>
<h1>
	这是默认错误页面
</h1>

<P>  错误信息：</P>
<P>${errMsg} </P>
</body>
</html>
