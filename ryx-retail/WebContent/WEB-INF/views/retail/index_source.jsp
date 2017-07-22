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
	
	<script type="text/javascript">
	function chromeCall(command, str) {
		//alert(command + "," + str + "," + window.onReceive);
		if(window.onReceive) {
			window.onReceive(command, str);
		}
	}
	</script>
</head>
<c:if test="${empty cookie.back_url.value}">
<body>
</c:if>
<c:if test="${!empty cookie.back_url.value}">
<body style="background-image: url('${cookie.back_url.value}')">
</c:if>
	<div class="header">
		<div class="contentWrap logocont">
			<!-- <img alt="中国烟草" style="margin-top: 20px;" src="${pageContext.request.contextPath}/public/user/img/zgyc_logo.png"> -->
			<img alt="微商盟现代终端信息系统" style="margin-left: 20px; margin-top: 20px;" 
				src="${pageContext.request.contextPath}/public/img/logo.png?v=3">
		</div>
	</div>
	
	<div id="desktopcontainer">
	
		<div id="desktop" class="navcontainer">
			<ul id="navwrap" class="navwrap">
				<li class="navitem">
					<a class="navlink c1" id="spxs" data-module="spxs">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_spxs"></span>
						<label class="navlink_label">商品销售</label>
						<label class="quick_key">F1</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c2" id="jydh" data-module="jydh">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_jydh"></span>
						<label class="navlink_label">卷烟配货</label>
						<label class="quick_key">F2</label>
						<label id="ordertime" class="ordertime"></label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c3" id="spgl" data-module="spgl">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_spgl"></span>
						<label class="navlink_label">商品管理</label>
						<label class="quick_key">F3</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c4" id="kcgl" data-module="kcgl">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_kcgl"></span>
						<label class="navlink_label">库存管理</label>
						<label class="quick_key">F4</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c5" id="wgdd" data-module="wgdd">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_wgdd"></span>
						<label class="navlink_label">网购订单</label>
						<label class="quick_key">F5</label>
						<label  class="ordertime">未处理的订单量：
							<label id="unreadcount">0</label>
						</label>
						
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c6" id="xsgl" data-module="xsgl">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_xsgl"></span>
						<label class="navlink_label">我的流水</label>
						<label class="quick_key">F6</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c7" id="tjfx" data-module="tjfx">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_tjfx"></span>
						<label class="navlink_label">我的利润</label>
						<label class="quick_key">F7</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c8" id="hygl" data-module="hygl">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_hygl"></span>
						<label class="navlink_label">我的客户</label>
						<label class="quick_key">F8</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c9" id="yxhd" data-module="yxhd" data-width="960">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_yxhd"></span>
						<label class="navlink_label">营销互动</label>
						<label class="quick_key">F9</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c10" id="ycjt" data-module="ycjt">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_ycjt"></span>
						<label class="navlink_label">营销讲堂</label>
						<label class="quick_key">F10</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c11" id="zzfw" data-module="zzfw">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_zzfw"></span>
						<label class="navlink_label">增值服务</label>
						<label class="quick_key">F11</label>
					</a>
				</li>
				<li class="navitem">
					<a class="navlink c12" id="wydh" data-module="wydh">
						<label class="navlink_back">&nbsp;</label>
						<span class="deskIcon deskIcon_wydh"></span>
						<label class="navlink_label">我要订货</label>
						<label class="quick_key">F12</label>
					</a>
				</li>
			</ul>
		</div>
		
	</div>
	
	
	<div id="navbarcontainer">
		<div class="navbaractive">&nbsp;</div>
		<div class="navbarwrap">
			<div class="navbarback">&nbsp;</div>
			<div id="widgetweather" class="navbartop"></div>
			<ul class="navbar">
				<li>
					<a class="navbaritem" id="viewggxx" data-module="ggxx" data-background="#68aede" data-width="960">
						<span class="deskIcon deskIcon_ggxx"></span>
						<label class="navbar-text">系统公告</label>
					</a>
				</li>
				<li>
					<a class="navbaritem" data-module="dpsz" data-background="#68aede" data-width="960">
						<span class="deskIcon deskIcon_dpsz1"></span>
						<label class="navbar-text">店铺设置</label>
					</a>
				</li>
				<li>
					<a class="navbaritem" data-module="bzzx" data-background="#68aede" data-width="960">
						<span class="deskIcon deskIcon_bzzx"></span>
						<label class="navbar-text">帮助中心</label>
					</a>
				</li>
				<li>
					<a class="navbaritem" data-module="zxts" data-background="#68aede" data-width="960">
						<span class="deskIcon deskIcon_zxfk"></span>
						<label class="navbar-text">咨询反馈</label>
					</a>
				</li>
				<li>
					<a class="navbaritem" id="skins">
						<span class="deskIcon deskIcon_pifu"></span>
						<label class="navbar-text">换肤</label>
					</a>
				</li>
				<li style="margin-top: 40px;">
					<a class="navbaritem" id="exit">
						<span class="deskIcon deskIcon_exit"></span>
						<label class="navbar-text">退出</label>
					</a>
				</li>
			</ul>
			<div class="navbaricon">
				<!-- <img title="快捷键" src="../public/retail/img/arrow.png"> -->
				<span class="deskIcon deskIcon_arrow"></span>
			</div>
		</div>
	</div>
	
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
		<div id="confirmViewExtend" style="margin-top: 10px;"></div>
	</div>
	
	<div id="maskView" class="mask_view">
		<div class="mask_back">&nbsp;</div>
		<div class="mask_view_rect" style="padding:20px;height:305px;">
			<img class="window_loading" alt="" src="../public/img/loading.gif" />
			<div id="feeContent" style="margin:5px 0 0 10px;text-align:center;display:none;">
				<span style="font-size:16px;">应付金额：</span><span style="color:#ff9725;font-size:22px;font-weight:bold;" id="posFee"></span><span style="font-size:16px;"> 元</span>
			</div>
			<p id="mask_message" class="mask_message"></p>
			<div id="message_detail" style="margin:30px 10px 10px 10px;line-height:1.8;text-align:left;height:135px;">
			 
			</div>
			<div class="mask_buttons">
			    <input id="mask_hidden" type="button" class="primary_button" style="margin-right:15px;" value=" 已支付完成 " style="display: none">
				<input id="mask_cancel" type="button" class="minor_button" style="width:90px;" value=" 取消操作 ">
			</div>
		</div>
	</div>
	
	<!-- 消息推送：消息提示框 -->
	<div id="msgPushDiv" style="display:none; position:fixed;bottom:0;right:40px;background-color:#fff;width:250px;height:150px; z-index:1000; ">
		<div  style="background-color:rgb(104, 174, 222); padding:5px 8px;">
			<span id="msgPushTitle" style="font-size: 16px; color:#fff;" >新公告</span>
			<span id='msgPushCloseBtn' class="pageIcon pageIcon_close" style="cursor:pointer; float:right; width:20px; margin-top: 3px;"> </span>			
		</div>
		<div style="margin:10px 18px;">
			<div style="line-height:1.6;">
		  		<span id="msgPushTitleBtn" style="cursor:pointer; word-wrap: break-word; word-break: break-all; display: block; width: 212px;" > </span>
			 </div>
<!-- 
			 <div style="border:0px solid #000; bottom: 9px; position: fixed;">
			  	<span id="msgPurchList" style="cursor:pointer;"><查看消息列表></span>
		  	</div>
 -->
		 </div>
	</div>
	
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/des.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-ui-1.10.3.custom.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-tablesort.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-fixedtableheader.js?v=1"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery.easing.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/mustache.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/js/common/jquery-tools.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/homecore.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/framework.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/desktop.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/module.js?v=${version }"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/public/retail/js/plugins/des.js?v=${version }"></script>

<script type="text/javascript">
HOME.Core.startAll({
	root: "${pageContext.request.contextPath}/",
	domain: "${domain}",
	resource: "${resource}",
	master: "${master}",
	version: "${version}",
	msgPushUrl: "${msg_push_url}",
	user: ${user_json}
});
HOME.Core.startApp();

//document.write(encodeURI('http://localhost:8081/t/ipos/postdata/saleorder?params={"ticket_id":"20140407062916","tm":"20140407191323","moling":"0","yf":"7.0","pay_type":"现金","zhaoling":"0.0","sf":"7.0","list":[{"good_name":"牙膏","good_in_price":"6.3","good_unit":"盒","good_sale_amount":"1","good_sale_price":"7","good_code":"16901028153195","good_id":"16901028153201","good_sale_xiaoji":"7.0"}],"zk":"100","card_id":"","cust_id":"370112107467"}'));

</script>
</body>
</html>
