<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" pageEncoding="utf-8" buffer="none"	contentType="text/html;charset=utf-8"%>
<!DOCTYPE html>
<html>
<head>


<link rel="shortcut icon" href="${pageContext.request.contextPath}/views/ycls/img/favicon.ico" type="image/x-icon" />

<title>微商盟现代终端信息系统-用户登录</title>
<link	href="${pageContext.request.contextPath}/views/ycls/css/base.css"		rel="stylesheet">
<link	href="${pageContext.request.contextPath}/views/ycls/css/widgets.css"	rel="stylesheet">
<link	href="${pageContext.request.contextPath}/views/ycls/css/login.css?v=2"	rel="stylesheet">
</head>
<body>
	<input type="hidden" value="${serviceFlag }" id="serviceFlag" name="serviceFlag">
	<input type="hidden" value="${callbackURL}" id="callbackURL" name="callbackURL">
	<input type="hidden" value="${onceFlag}"	 id="onceFlag" name="onceFlag">
	<!--[if lte IE 6]>	
	<div id="ie6-warning"  style="background-color:#F8EFB4;width:100%;height:30px;position:absolute;top:0px;left:0px;border-bottom:solid 1px #FCE003">
			<ul  class="footer_ul" style="text-align:center; margin-left:20%; margin-top:5px">
				<li style="float:left;"><img src="${pageContext.request.contextPath}/public/user/img/jinggao.png" height="22" width="22"/></li>
				<li style="float:left;margin-left: 15px;font-weight:600;">您的浏览器版本过低，可能会影响页面显示效果！建议您升级</li>
				<li style="float:left;margin-left: 15px;"><a style="text-decoration:none" href="http://www.microsoft.com/china/windows/internet-explorer/" target="_blank"><img src="${pageContext.request.contextPath}/public/user/img/ielogo.png" height="20" width="20"/></a></li>
				<li style="float:left;margin-left: 5px;"><a style="text-decoration:none" href="http://www.microsoft.com/china/windows/internet-explorer/" target="_blank">&nbsp;IE8</a> </li>
				<li style="float:left;margin-left: 15px;"><a style="text-decoration:none" href="http://www.microsoft.com/china/windows/internet-explorer/" target="_blank"><img src="${pageContext.request.contextPath}/public/user/img/Chromelogo.png" height="20" width="20"/></a></li>
				<li style="float:left;margin-left: 5px;"><a style="text-decoration:none" href="http://www.google.com/chrome/?hl=zh-CN" target="_blank">&nbsp;Chrome</a> </li>
				<li style="float:left;margin-left: 15px;"><a style="text-decoration:none" href="http://www.microsoft.com/china/windows/internet-explorer/" target="_blank"><img src="${pageContext.request.contextPath}/public/user/img/360logo.png" height="20" width="20"/></a></li>
				<li style="float:left;margin-left: 5px;"><a style="text-decoration:none" href="http://se.360.cn/" target="_blank">&nbsp;360安全浏览器</a></li>
			</ul>
	</div>
	<![endif]-->
	<div class="header">
		<div class="contentWrap logocont">	
			<a href="/resource/download/package/terminal.exe" style="float: right; margin-right: 24px; margin-top: 24px; cursor:pointer;">下载客户端</a>
			
			<!-- <img alt="中国烟草"
				src="${pageContext.request.contextPath}/views/ycls/img /zgyc_logo.png"> -->
			<img alt="微商盟现代终端信息系统" style="margin-left:20px;"
				src="${pageContext.request.contextPath}/views/ycls/img/logo.png?v=4">
			
		</div>
	</div>

	<div class="banner">
		<div class="bannerimgs">
			<ul>
				<li id="btl1" class="bannerimg1"></li>
				<li id="btl2" class="bannerimg2"></li>
				<li id="btl3" class="bannerimg3"></li>
			</ul>
		</div>
		<div class="contentWrap">
			<div class="login-IconBg loginbody">
				<form id="loginform" action="${pageContext.request.contextPath}/doLogin"	method="post">
					<h3 class="logintitle">欢迎登录</h3>
					<div class="login-IconBg loginuser">
						<input type="text" id="username" name="username" />
						<label id="unprompt">用户名</label>
					</div>
					<div class="login-IconBg loginclert">
						<input type="text" id="clertNum" name="clertNum" />
						<label id="cnprompt">工号，可为空</label>
					</div>
					<div class="login-IconBg loginpwd">
						<input type="password" id="password" name="password" />
						<label id="pwprompt">密码</label>
					</div>
					<div class="login-IconBg logincode">
						<input type="text" id="code" name="challenge" /> 
						<a	id="validatecode"	data-path="${pageContext.request.contextPath}/challenge" class="validatecode" title="点击更换"></a>
						 <label id="vcprompt">验证码</label>
					</div>
					<input id="submit" type="submit" class="primary_button loginbutton"
						value="登录" />
					<div class="loginbottom">
						<!-- <a class="register" href="javascript:void(0)" title="用户注册">没有账号？</a> -->
						<label id="message"></label>
					</div>
				</form>
			</div>
		</div>
	</div>
	<div class="bannerindex">
		<ul>
			<li id="i1" onclick="chengeImg(0);" class="login-IconBg"></li>
			<li id="i2" onclick="chengeImg(1);" class="login-IconBg"></li>
			<li id="i3" onclick="chengeImg(2);" class="login-IconBg"></li>
		</ul>
	</div>

	<div class="content">
		<div class="contentWrap">

			<ul class="contentws">
				<!-- 
					<li>
						<a href="javascript:void(0);">
							<label>微商助手</label>
							<img alt="" src="${pageContext.request.contextPath}/lsimg/content1.png">
						</a>
					</li>
					<li>
						<a href="javascript:void(0);">
							<label>微商服务</label>
							<img alt="" src="${pageContext.request.contextPath}/views/ycls/img/content2.png">
						</a>
					</li>
					<li>
						<a href="javascript:void(0);">
							<label>数字便民</label>
							<img alt="" src="${pageContext.request.contextPath}/views/ycls/img/content3.png">
						</a>
					</li>
					 -->
				<li class="l1" style="margin-top:10px;">
					<a href="/resource/download/package/guandianbao.apk" title="点击下载管店宝">
						<img
							style="width: 96px; height: 96px;"
							src="${pageContext.request.contextPath}/views/ycls/img/gdbewm.png?v=1" />
					</a>
				</li>
				<li style="border-right: 1px solid #CCC;  margin-right:15px;"><%--<a href="javascript:void(0);"> --%>
					<div class="contenttitle" style="line-height:2em;color:#565656;">管店宝
						<a id="sjkdlabel" style="font-size: 14px; margin-left: 50px; margin-top: 12px; position: relative;"
							href="/resource/download/package/shoujikandian.apk">手机看店
							
							<div id="sjkdewm" style="display:none; position: absolute;top: -100px; left: -25px; padding: 4px; border: 1px solid #CCC; background: #FFF;">
								<img
									style="width: 96px; height: 96px;"
									src="${pageContext.request.contextPath}/views/ycls/img/sjkdewm.png?v=1" />
							</div>
						</a>
					</div>
					
					<p>
						随时随地掌握店铺动态<br/> 库存、销量、毛利一目了然 <br /> 帮您降低市场营销费用、人力成本
					</p> <%--</a> --%>
				</li>
				<li class="l1" style="margin-top:10px;">
					<a href="/resource/download/package/louxiadian.apk" title="点击下载楼下店">
						<img
							style="width: 96px; height: 96px;"
							src="${pageContext.request.contextPath}/views/ycls/img/lxdewm.png?v=1">
					</a>
				</li>

				<li style="border-right: 1px solid #CCC; margin-right:15px;"><%--<a
					href="javascript:void(0);"> --%><label class="contenttitle"
						style="line-height:2em;color:#565656;">楼下店</label>
						<p>
							促销活动信息免费推送<br />把您的门店开到客户手机里<br /> 小门店大营销，为您带来无尽商机
				</p> <%--</a>--%></li>
						
						
				<li style="width:170px;"><%--<a href="javascript:void(0);"style="width:170px;"> --%><label
						class="contenttitle" style="line-height:2em;color:#565656;">数字便民</label>
						<p>
							生活缴费，不再排队<br />便民服务，温暖你我他<br /> 网上付款，让生活简单一点<br />
						</p> <%--</a>--%></li>
		
			</ul>
		</div>
	</div>
	<div class="clearfix"></div>
	<div class="footer">
		<div class="contentWrap">
			<p>技术支持：瑞银信&nbsp;&nbsp;&nbsp;&nbsp;备案号：鲁ICP备14026018</p>
			<p style="color: #FF0000;">本网站含有烟草内容，未成年人谢绝访问</p>
		</div>
	</div>
	<script	src="${pageContext.request.contextPath}/public/js/common/jquery-1.9.1.min.js"type="text/javascript"></script>
	<script	src="${pageContext.request.contextPath}/views/ycls/js/login.js"			type="text/javascript"></script>
	<script type="text/javascript">
	$(document).ready(function() {
		$("#sjkdlabel").hover(function() {
			$("#sjkdewm").show();
		}, function() {
			$("#sjkdewm").hide();
		});
	});
	</script>
</body>
</html>
