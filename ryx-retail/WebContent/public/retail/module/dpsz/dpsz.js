/**
 * 店铺设置
 */
HOME.Core.register("plugin-dpsz", function(box) {
	var mapUrl="public/js/common/map.js";
//	var mapUrl="http://api.map.baidu.com/api?v=1.3";
	var merchUrl = "retail/basedata/getMerchInfo";
	var updateUrl = "retail/basedata/updateMerchInfo";//没用使用
	var updatePwdUrl="retail/basedata/updateMerchPwd";//修改密码
	//修改基本信息，（经纬度，范围，时间）    merchAddr:修改经纬度，merchTime：修改营业时间，merchScope:修改经营范围,merchDelivery：修改送货方式
	var updateMerchInfoUrl="retail/basedata/updateMerchBasicInfo";
	var logoutUrl="user/logout";//退出
	
	var parentView = null;
	var merchInfo = null;
	var content=null;
	var map=null;//map对象
	var marker = null;
	var lng=null;
	var lat=null;
	var updateMerchInfo={};
	var isValidatePwd=false;//是否修改密码
	var isValidateAddr = false;//是否修改地理位置
	var dialogContent=null;//修改密码
	function loadDPSZ(view) {
		parentView = view;
		getMerchInfo();
	}
	function getMerchInfo() {
		box.request({
			url: box.getContextPath() + merchUrl,
			success: onMerchInfo
		});
	}
	function onMerchInfo(data) {
		merchInfo = data.result;
//		merchInfo.time=merchInfo.busi_time;
		merchInfo.time="";
		if(merchInfo.open_time&&merchInfo.close_time){
			merchInfo.time=merchInfo.open_time+"-"+merchInfo.close_time ;
		}
		if(merchInfo.delivery_type&&merchInfo.delivery_type=='01'){
			merchInfo.delivery_type_str="到店自取";
		}else if(merchInfo.delivery_type&&merchInfo.delivery_type=='02'){
			merchInfo.delivery_type_str="送货上门";
		}
		merchInfo.order_type_string = merchInfo.order_type && merchInfo.order_type=='0' ? '关闭' : '启用';
		content = box.ich.view_dpsz(merchInfo);
		lng=data.result.longitude;
		lat=data.result.latitude;
		parentView.empty().append(content);
		mapLoatJs();
		parentView.find("#updatePwd").click(openPwdDialog);
		content.find("#wherebtn").click(sear);//位置查找
		content.find("#latLonMerchBtn").click(onSaveMerchInfo);//保存地理位置
		parentView.find("#selectscope").click(openScopeDialog);//营业范围
		parentView.find("#selecttime").click(openTimeDialog);//营业时间
		parentView.find("#selectdelivery").click(openDelivery);//送货方式
		parentView.find("#choice_order").click(showChoiceOrderDialog);//自动卷烟配货
	}
	//送货方式
	function openDelivery(){
		dialogContent=box.ich.view_dpsz_delivery();
		box.showDialog({
			title:"修改送货方式",
			width:500,
			height:300,
			content: dialogContent,
			close: function(e) {
			}
		});
		var deliveryStr=parentView.find("#mydelivery").text();
		if(deliveryStr=='送货上门'){
			$("input:radio[name='deliveryType']").attr("checked",'02');
		}
		dialogContent.find("#deliverySubmit").click(onUpdateDelivery);
	}
	function showChoiceOrderDialog(){
		dialogContent=box.ich.view_order_type();
		box.showDialog({
			title:"修改卷烟配货方式",
			width:500,
			height:300,
			content: dialogContent,
			close: function(e) {
			}
		});
		var orderTypeString = parentView.find("#order_type_string").text();
		if(orderTypeString=='启用'){
			$("input:radio[name='order_type']").eq(0).attr("checked",true);
		} else {
			$("input:radio[name='order_type']").eq(1).attr("checked",true);
		}
		dialogContent.find("#submit_order_type").click(function(e) {
			var orderType = $('input:radio[name="order_type"]:checked').val();
			updateMerchInfo.type = 'order_type';
			updateMerchInfo.order_type = orderType;
			box.request({
				url: box.getContextPath() + updateMerchInfoUrl,
				data:{params:$.obj2str(updateMerchInfo)},
				success:  function(data){
					if(data && data.code=="0000"){
						box.showAlert({message: data.msg});
						parentView.find("#order_type_string").text(orderType=='0'?'关闭':'启用');
						box.closeDialog({content:dialogContent});
					}
				}
			});
		});
	}
	function onUpdateDelivery(){
		var deliveryCheck = $('input:radio[name="deliveryType"]:checked');
		updateMerchInfo.type='merchDelivery';
		updateMerchInfo.delivery_type=deliveryCheck.val();
		box.request({
			url: box.getContextPath() + updateMerchInfoUrl,
			data:{params:$.obj2str(updateMerchInfo)},
			success:  function(data){
				var msg=data.msg;
				if(data && data.code=="0000"){
					box.showAlert({message: msg});
					var deliveryStr="到店自取";
					if(deliveryCheck.val()=='02'){
						deliveryStr="送货上门";
					}
					parentView.find("#mydelivery").text(deliveryStr);
					box.closeDialog({content:dialogContent});
				}
			}
		});
	}
	//打开修改营业时间
	function openTimeDialog(){
		dialogContent=box.ich.view_dpsz_time();
		box.showDialog({
			title:"修改营业时间",
			width:500,
			height:300,
			content: dialogContent,
			close: function(e) {
			}
		});
		var busiTimeText=parentView.find("#myselecttime").text();
		if(busiTimeText){
			busiTime=busiTimeText.split("-");
			dialogContent.find("#start_time").val(busiTime[0]);
			dialogContent.find("#end_time").val(busiTime[1]);
		}else{
			dialogContent.find("#start_time").val("08:30");
			dialogContent.find("#end_time").val("17:30");
		}
		dialogContent.find("#all_day").click("click", function () {
			var inputCheck= dialogContent.find("input:checked");
			var startTime=dialogContent.find("#start_time");
			var endTime=dialogContent.find("#end_time");
			if(inputCheck.length==1){
				dialogContent.find("#start_time").val("00:00");
				dialogContent.find("#end_time").val("24:00");
				$(startTime).attr("disabled","disabled");//设为不可用
				$(endTime).attr("disabled","disabled");//设为不可用
			}else{
				$(startTime).removeAttr("disabled");   //取消不可用的设置
				$(endTime).removeAttr("disabled");   //取消不可用的设置
			}
         });
		dialogContent.find("#timeSubmit").click(onUpdateTime);
	}
	function onUpdateTime(){
		var messageList = [];
		if(!$.validateForms(dialogContent, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		var startTime=dialogContent.find("#start_time").val();
		var endTime=dialogContent.find("#end_time").val();
		updateMerchInfo.type='merchTime';
		updateMerchInfo.start_time=startTime;
		updateMerchInfo.end_time=endTime;
		box.request({
			url: box.getContextPath() + updateMerchInfoUrl,
			data:{params:$.obj2str(updateMerchInfo)},
			success:  function(data){
				var msg=data.msg;
				if(data && data.code=="0000"){
					box.showAlert({message: msg});
					parentView.find("#myselecttime").text(startTime+"-"+endTime);
					box.closeDialog({content:dialogContent});
				}
			}
		});
	}

	//经营范围
	function openScopeDialog(){
		dialogContent=box.ich.view_dpsz_scope();
		box.showDialog({
			title:"修改经营范围",
			width:500,
			height:300,
			content: dialogContent,
			close: function(e) {
			}
		});
		var mySelectScope= parentView.find("#myselectscope").text();
		var mySelectScopeType=mySelectScope.split(",");
		var chekbox= dialogContent.find("[name = scopetype]:checkbox");//经营范围
		for(var j=0;j<chekbox.length;j++){
			for(var i=0;i<mySelectScopeType.length;i++){
				if(mySelectScopeType[i]==$(chekbox[j]).val()){
					$(chekbox[j]).attr("checked", true);
				}
			}
		}
		dialogContent.find("#scopeSubmit").click(onUpdateScope);//修改范围
	}
	//经营范围提交
	function onUpdateScope(){
		var inputCheck= dialogContent.find("input:checked");//经营范围
		var busiScope="";//经营范围
		inputCheck.each(function(i){
			busiScope += $(this).val()+','; 
		});
		updateMerchInfo.type='merchScope';
		updateMerchInfo.busi_scope=busiScope;
		box.request({
			url: box.getContextPath() + updateMerchInfoUrl,
			data:{params:$.obj2str(updateMerchInfo)},
			success: function(data){
				var msg=data.msg;
				if(data && data.code=="0000"){
					box.showAlert({message: msg});
					parentView.find("#myselectscope").text(busiScope);
					box.closeDialog({content:dialogContent});
				}
			}
		});
	}
	function openPwdDialog(){ 
		dialogContent=box.ich.view_dpsz_pwd();
		box.showDialog({
			title:"修改密码",
			width:500,
			height:300,
			content: dialogContent,
			close: function(e) {
			}
		});
		dialogContent.find("#newPwd1").blur(onPwdValidate);
		dialogContent.find("#newPwd2").blur(onPwdValidate);
		dialogContent.find("#oldPwd").blur(function(){
			if(!$(this).val()){
				isValidatePwd=false;
			}
		});
		dialogContent.find("#pwdSubmit").click(onUpdatePwd);//修改密码
	}
	//密码验证
	function onPwdValidate(){
		var pwd2=dialogContent.find("#newPwd2");
		var pwd1=dialogContent.find("#newPwd1");
		var oldPwd=dialogContent.find("#oldPwd");
		if(pwd1.val()){
			if(pwd2.val()){
				if(pwd1.val()==pwd2.val()&&oldPwd.val()){
					isValidatePwd=true;
				}else{
					isValidatePwd=false;
					$(pwd2).css("border-color","red");
				}
			}else{
				isValidatePwd=false;
				$(pwd2).css("border-color","red");
			}
		}else{
			isValidatePwd=false;
			$(pwd1).css("border-color","red");
		}
	}
	//修改密码提交
	function onUpdatePwd(){
		if(isValidatePwd==false){
			box.showAlert({message:"密码信息不正确"});
			return;
		}
		if(isValidatePwd==true){
			var oldPwd=dialogContent.find("#oldPwd").val();
			var newPwd=dialogContent.find("#newPwd2").val();
			var merchPwdObject={};
			merchPwdObject.oldPassword=$.MD5(oldPwd);
			merchPwdObject.newPassword=$.MD5(newPwd);
			box.request({
				url: box.getContextPath() + updatePwdUrl,
				data:{params: $.obj2str(merchPwdObject)},
				success: updatePwdResult
			});
		}
	}
	//退出
	function onExitHandler(data) {
		box.showAlert({message: "密码已修改，请重新登录！"});
		setTimeout(function() {
			window.location.href = data["result"];
		}, 2000);
	}
	function updatePwdResult(data){
		var msg=data.msg;
		if(data && data.code=="0000"){
			box.request({
				url:box.getContextPath()+ logoutUrl,
				success:onExitHandler
			});
		}
		box.showAlert({message:msg});
	}
	function openMap(){
		content=box.ich.view_dpsz_map();
		box.showDialog({
			title:"我的经纬度",
			width:900,
			height:550,
			content: content,
			close: function(e) {
				parentView.find("#lng").val(lng);
				parentView.find("#lat").val(lat);
			}
		});
		content.find("#wherebtn").click(sear);
		mapLoatJs();
	}
	function onSaveMerchInfo() {
		merchInfo.merch_name = parentView.find("#merch_name").val();
		merchInfo.manager = parentView.find("#manager").val();
		merchInfo.telephone = parentView.find("#telephone").val();
		merchInfo.address = parentView.find("#address").val();
		var startTime = parentView.find("#start_time").val();//开始时间
		var endTime = parentView.find("#end_time").val();//结束时间
		var busiTime=startTime+"-"+endTime;
		merchInfo.busi_time =busiTime;//开始时间//input[name='newgoodbar']
		var inputCheck= parentView.find("input:checked");//经营范围
		var busiScope="";//经营范围
		inputCheck.each(function(i){
			busiScope += $(this).val()+','; 
		});
		merchInfo.busi_scope =busiScope;
		merchInfo.latitude = parentView.find("#lat").val();
		merchInfo.longitude = parentView.find("#lng").val();
		merchInfo.lice_id = parentView.find("#lice_id").val();
		// merchAddr:修改经纬度，merchTime：修改营业时间，merchScope:经营范围
		box.request({
			url: box.getContextPath() + updateMerchInfoUrl,
			data:{params:$.obj2str(updateMerchInfo)},
			success: onUpdateMerchInfo
		});
	}
	function onUpdateMerchInfo(data) {
		if(data.code == "0000") {
			box.showAlert({message: "店铺信息修改成功"});
			isValidateAddr = false;
			content.find("#latLonMerchBtn").hide();
			if(marker) {
				marker.setPosition(new BMap.Point(lng,lat));
			}
		}
	}
	function mapLoatJs(){
		box.notify({
			type: HOME.Const.LOAD_JSPLUGINS,
			data: {
				url: mapUrl, 
				success: mapLoat
			}
		});
	}
	function mapLoat(){
		map = new BMap.Map("container");//在指定的容器内创建地图实例
		var point = new BMap.Point(lng,lat);
		map.setDefaultCursor("crosshair");//设置地图默认的鼠标指针样式
		map.enableScrollWheelZoom();//启用滚轮放大缩小，默认禁用。
		map.centerAndZoom(point, 14);
		
		if(lng!=""||lat!=""){
			marker = new BMap.Marker(point);  // 创建标注
			map.addOverlay(marker);              // 将标注添加到地图中
			//marker.setAnimation(BMAP_ANIMATION_BOUNCE); //跳动的动画
		}
		map.addControl(new BMap.NavigationControl()); 
		
		map.addEventListener("click", function(e){//地图单击事件
			var maplng=e.point.lng;
			var maplat=e.point.lat;
			content.find("#maplng").val(maplng);
			content.find("#maplat").val(maplat);
			content.find("#lonlab").text(maplng);
			content.find("#latlab").text(maplat);
			isValidateAddr = true;
			content.find("#latLonMerchBtn").show();
			lng=maplng;
			lat=maplat;
			updateMerchInfo.lng=lng;
			updateMerchInfo.lat=lat;
			updateMerchInfo.type="merchAddr";
		});
		var myCity = new BMap.LocalCity();
		
		if(!lng || !lat){
			myCity.get(iploac);
			
			setTimeout(function() {
				content.find("#where").val(merchInfo.address);
				var local = new BMap.LocalSearch(map, {
			  		renderOptions:{map: map}
				});
				local.search(merchInfo.address);
			}, 100);
		}
	}
	function sear(result){//地图搜索	
		var where=content.find("#where").val();
		var local = new BMap.LocalSearch(map, {
	  		renderOptions:{map: map}
		});
		local.search(where);
	}
	function iploac(result){//根据IP设置地图中心
	    var cityName = result.name;
	    map.setCenter(cityName);
	}
	function onClose(param){
		if(isValidateAddr){
			box.showConfirm({
				message: "地理位置已修改，是否保存？",
				hasSubmit: true,
				submit: function() {
					onSaveMerchInfo();
					updateMerchInfo = {};
					param.callback();
				},
				donotSubmit: function() {
					updateMerchInfo = {};
					isValidateAddr = false;
					param.callback();
				}
			});
		}else{
			lng=null;
			lat=null;
			return true;
		}
		return false;
	}
	return {
		init: function(){
			box.listen("dpsz", loadDPSZ);
			box.listen("dpsz_close", onClose);
		},
		destroy: function() { }
	};
});
