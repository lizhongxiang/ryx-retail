<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" pageEncoding="utf-8" buffer="none" contentType="text/html;charset=utf-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	 <link rel="shortcut icon" href="${pageContext.request.contextPath}/public/img/favicon.ico" type="image/x-icon" />
	<c:set var="version" value="1.5.4"></c:set>

	<title>微商盟现代终端信息系统</title>
    
    
	<link href="${pageContext.request.contextPath}/public/css/common/base.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/public/css/common/jquery-ui-1.10.3.custom.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/public/css/common/widgets.css?v=${version }" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/public/retail/css/retail.css?v=${version }" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/public/retail/css/widget.css?v=${version }" rel="stylesheet">
	
</head>
<body style="background: none;">
	<div id="loadingView" class="loading_view">
		<div class="loading_view_rect">
			<img class='window_loading' alt='' src='../public/img/loading.gif' />
		</div>
	</div>
	
	<div id="alertView" class="alert_view">
		<label id="alertViewText"></label>
	</div>
	
	<div id="confirmView" class="confirm_view">
		<label id="confimViewText"></label>
	</div>
	
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-ui-1.10.3.custom.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-tablesort.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-fixedtableheader.js?v=1"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery.easing.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/mustache.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-tools.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/homecore.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/framework.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/widget.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/module.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/des.js?v=${version }"></script>

<script type="text/javascript">
HOME.Core.startAll({
	root: "${pageContext.request.contextPath}/",
	domain: "${domain}",
	resource: "${resource}",
	version: "${version}",
	user: ${user_json},
	moduleId: "${moduleId}"
});
HOME.Core.startApp();

</script>
</body>
</html>
