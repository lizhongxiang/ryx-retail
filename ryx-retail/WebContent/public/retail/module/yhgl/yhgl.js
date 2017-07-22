/**
 * 便门服务--数字电视缴费
 */
HOME.Core.register("plugin-yhgl", function(box) {
	var view_ = null;
	var content = null;
	var addRoleURL = "retail/user/role/add";
	var showRoleListURL="retail/user/role/show";
	var logoutUrl="user/logout";//退出
	var isExistUrl = "retail/user/isexist";
	var clerkRole=[{"roleID":"3","roleName":"售货员"}];
	var isUpUser=true;//是否用户修改界面
	var addUserURL = "retail/user/add";//添加销售员
	var updateUserURL = "retail/user/updateUser";//修改销售员信息
	var showUserListURL = "retail/user/show";//查询商户下的销售员
	var updatePwdUrl="retail/basedata/updateMerchPwd";//修改密码
	//添加用户弹出框
	var userDialog = null;
	
	function onload(view){
		view_ = view;
		content = box.ich.view_yhgl();
		view.empty().append(content);
		var selectedObj = content.find(".left_table_view .selected");
		content.find("#left_view li").each(function(){
			$(this).unbind("click").click(showContainer(this));
		});
		$(selectedObj).trigger("click");
	}
	//显示右侧主内容信息
	function showContainer(self){
		return function(){
			content.find("#left_view li").removeClass("liselected").removeClass("selected");
			$(self).addClass("liselected").addClass("selected");
			var id = $(self).attr("id");
			if(id == "jsgl"){
				box.request({
					url:box.getContextPath()+ showRoleListURL,
					data:{},
					success: function(data) {
						var roleList = box.ich.view_yhgl_jsgl({list:data.result});
						content.find("#container").empty().append(roleList);
						roleList.find(".rolelist").unbind("click").click(function(){
							showUpdateRoleRources(this);
						});
					}
				});
				content.find("#add").unbind("click").click(showAddRoleDialog);
			}
			else{
				box.request({
					url:box.getContextPath()+ showUserListURL,
					data:{},
					success: function(data) {
						for(var i = 0 ; i < data.result.length ; i++){
							data.result[i].line_num = i+1;
							var userCode = data.result[i].user_code;
							if(userCode.indexOf("_") != -1){
								data.result[i].clertNum = userCode.split("_")[1];
							}else if (data.result[i].role_id == '3'){
								data.result[i].clertNum = data.result[i].user_code;
							}else{
								data.result[i].clertNum = "无";
							}
						}
						var userList = box.ich.view_yhgl_yhgl({list:data.result});
						content.find("#container").empty().append(userList);
//						var;
						userList.find(".userList").unbind("click").click(function(){
							showUpdateUser(this);//--------showAddUserDialog
						});
						view_.find(".table_view").fixedtableheader({
							parent: view_,
							win: view_.parent(),
							isshow: true
						});
					}
				});
				content.find("#add").unbind("click").click(showAddUserDialog);
			}
		};
	}
	
	//用户管理：修改用户
	function showUpdateUser(self){
		var roleID = $(self).attr("roleID");
		var userCode = $(self).attr("userCode");
		var jobNum=userCode.split("_")[1];
		var userID=$(self).attr("userID");
		var isLocked=$(self).attr("islocked");
		showAddUserDialog(roleID,userCode);
		userDialog.find("#jobNum").val(jobNum);
		userDialog.find("#jobNum").attr("disabled","true");	
		userDialog.find("#userID").val(userID);
		userDialog.find("#addUserButton").val("保 存");
		userDialog.find('input:radio[name="islocked"][value="'+isLocked+'"]').attr("checked",true);
		userDialog.find("#quxiao").unbind("click").click(function(){
			box.closeDialog({content:userDialog});
		});
	}
	
	//显示角色增加窗口
	var roleDialog = null;
	function showAddRoleDialog(){
		roleDialog=box.ich.view_yhgl_add_role();
		var permit = box.user.permit;
		var hhh = fittingResources(permit,null);
		roleDialog.find("#resources").empty().append(hhh);
		//点击父元素选中子元素
		roleDialog.find("#resources .parent").unbind("click").click(function(){
			if($(this).is(':checked')){
				$('#resources input:checkbox[class="children_'+$(this).val()+'"]').attr("checked",true);
			}
			else{
				$('#resources input:checkbox[class="children_'+$(this).val()+'"]').attr("checked",false);
			}
		});
		//点击子元素 选中父元素  $("input[id^='ywxt _']").remove();
		roleDialog.find("#resources input:checkbox[class^='children_']").unbind("click").click(
				function(){
					var parentID  = $(this).attr("class").split("children_")[1];
					$('#resources input:checkbox[value="'+parentID+'"]').attr("checked",true);
			
				}
		);
		//绑定保存按钮事件 addRole
		roleDialog.find("#addRoleButton").unbind("click").click(addRole);
		roleDialog.find("#quxiao").unbind("click").click(
				function(){
					box.closeDialog({content:roleDialog});
				}
		);
		box.showDialog({
			title:"新增角色",
			width:780,
			height:500,
			content: roleDialog
		});
	}
	
	//显示角色修改窗口
	function showUpdateRoleRources(self){
		var roleID = $(self).attr("role_id");
		var resourcesID = $(self).attr("resourcesID");
		var roleName=$(self).attr("roleName");
		var resourcesIDArry = resourcesID.split(",");
		showAddRoleDialog();
		roleDialog.find("#addRoleButton").val("修改");
		roleDialog.find("#rolename").val(roleName);
		roleDialog.find("#roleID").val(roleID);
		for(var i=0;i<resourcesIDArry.length;i++){
			roleDialog.find("#resources input:checkbox[value='"+resourcesIDArry[i]+"']").attr("checked",true);
		}
	}
	
	//组装功能列表结构
	var treeHTHL = "";
	function fittingResources(p,parentID){
		treeHTHL ="<ul>";
		for(var i = 0 ; i < p.length ;i++){
			//构造一级目录
			if(!p[i].parent_id){
				treeHTHL = treeHTHL +"<li>";
				treeHTHL = treeHTHL + "<input type='checkbox' class='parent' value='"+p[i].module_id+"'>&nbsp;&nbsp;"+p[i].title;
				treeHTHL = treeHTHL +"</li>";
				if(p[i].haschildren=="1"){
					treeHTHL = treeHTHL+finttingResourcesChilden(p,p[i].module_id);
				}
			}
		}
		treeHTHL = treeHTHL+"</ul>";
		return treeHTHL;
	}
	
	function finttingResourcesChilden(p,parentID){
		var chlidenHTML = "<ul class='ul_children'>";
		for(var i = 0 ; i<p.length;i++){
			if(p[i].parent_id == parentID){
				chlidenHTML = chlidenHTML +"<li>";
				chlidenHTML = chlidenHTML + "<input type='checkbox' value='"+p[i].module_id+"' class='children_"+parentID+"'>&nbsp;&nbsp;"+ p[i].title;
				chlidenHTML = chlidenHTML +"</li>";
				if(p[i].haschildren=="1"){
					chlidenHTML =+ finttingResourcesChilden(p,p[i].module_id);
				}
			}
		}
		chlidenHTML=chlidenHTML+"</ul>";
		return chlidenHTML;
	}
	
	//addRole
	function addRole(){
		//选中状态的ID
		var resourcesID = "";
		var l = $("#resources input:checkbox:checked").length;
		var i = 0;
		$("#resources input:checkbox:checked").each(
			function(){ 
				i++;
				resourcesID = resourcesID+$(this).val();
				if(i!=l){
					resourcesID = resourcesID+",";
				}
			}
		);
		var rolename = $("#rolename").val();
		var roleID=$("#roleID").val();
		if(resourcesID == ""){
			box.showAlert({message: "请选择功能信息！"});
			return;
		}
		else if(rolename ==""){
			box.showAlert({message: "请填写角色名称！"});
			return;
		}
		box.request({
			url:box.getContextPath()+ addRoleURL,
			data:{"resourcesID":resourcesID,"roleName":rolename,"roleID":roleID},
			success: function(data) {
				box.showAlert({message: data.result});
				box.closeDialog({content:roleDialog});
				$("#jsgl").trigger("click");
			}
		});
		
	}
	
	//添加用户弹出框
	function showAddUserDialog(roleID,userCode){
		if(box.user.lice_id != userCode){
			userDialog=box.ich.view_yhgl_add_user();
			var roleSelect = "";
			var dialogTitle="新增用户";
			for(var i = 0 ;i< clerkRole.length;i++){
				if(roleID == clerkRole[i].roleID){
					dialogTitle="修改用户";
					isUpUser=true;
					roleSelect = roleSelect+"<option value='"+clerkRole[i].roleID+"' selected>"+clerkRole[i].roleName+"</option>";
					userDialog.find("#password").attr("data-empty","true");
					userDialog.find("#rePassword").attr("data-empty","true");
				}else{
					isUpUser=false;
					roleSelect = roleSelect+"<option value='"+clerkRole[i].roleID+"' selected>"+clerkRole[i].roleName+"</option>";
				}
			}
			userDialog.find("#clerkRole").append(roleSelect);
			userDialog.find("#userName_lable").after(box.user.user_code);
			userDialog.find("#addUserButton").unbind("click").click(addUser);
			userDialog.find("#quxiao").unbind("click").click(
				function(){
					box.closeDialog({content:userDialog});
				}
			);
			box.showDialog({
				title:dialogTitle,
				width:500,
				height:400,
				content: userDialog
			});
		}else{
			openPwdDialog();
		}
	}
	
	function addUser(){
		var jobNum = $("#jobNum").val();
		var password = $("#password").val();
		var rePassword = $("#rePassword").val();
		var userID = $("#userID").val();
		var roleID=$("#clerkRole").val();
		var islocked=$('input:radio[name="islocked"]:checked').val();
		var params = new Object();
		var isUpPwd=false;
		var messageList = [];
		if(!$.validateForms(userDialog, messageList)) {
			box.showAlert({message: messageList[0]});
			box.stopEvent(e);
			return;
		}
		if (password!="" && password != rePassword ) {
			box.showAlert({message:"两次输入的密码不一致"});
			return ;
		}else{
			if(password!=""){
				isUpPwd=true;
			}
		}
		if (roleID == "") {
			box.showAlert({message:"请选择角色"});
			return ;
		}
		if (!islocked) {
			box.showAlert({message:"请选择是否启用"});
			return ;
		}
		password = $.MD5(password);
		if(isUpPwd){
			params.password=password;
		}
		var username = box.user.user_code+"_"+jobNum;
		params.userName=username;
		params.roleID=roleID;
		
		params.userID=userID;
		params.islocked=islocked;
		
		if (islocked == 0) {
			params.status = '01';
		} else {
			params.status = '02';
		}
		
		box.request({
			url:box.getContextPath()+ isExistUrl,
			data:{"userCode":username},
			success: function(data) {
				if(isUpUser || data.result== "" || data.result == userID){
					var url = "";
					if(isUpUser){
						url = updateUserURL;
					}else{
						url = addUserURL;
					}
					box.request({
						url:box.getContextPath()+ url,
						data:params,
						success: function(data) {
							box.showAlert({message: data.result});
							box.closeDialog({content:userDialog});
							$("#yhgl").trigger("click");
						}
					});
				}
				else{
					box.showAlert({message:"该工号已存在"});
				}
			}
		});
	}
	
	/*修改密码开始*/
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
//		dialogContent.find("#newPwd1").blur(onPwdValidate);
//		dialogContent.find("#newPwd2").blur(onPwdValidate);
		dialogContent.find("#oldPwd").blur(function(){
			if(!$(this).val()){
//				isValidatePwd=false;
				$(this).css("border-color","red");
				
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
//					isValidatePwd=true;
					return true;
				}else{
					box.showAlert({message:"两次输入的密码不一致!"});
//					isValidatePwd=false;
					$(pwd2).css("border-color","red");
					return false;
				}
			}else{
				box.showAlert({message:"请再次输入新密码!"});
//				isValidatePwd=false;
				$(pwd2).css("border-color","red");
				return false;
			}
		}else{
			box.showAlert({message:"请输入新密码!"});
//			isValidatePwd=false;
			$(pwd1).css("border-color","red");
			return false;
		}
	}
	
	//修改密码提交
	function onUpdatePwd(){
//		if(isValidatePwd==false){
//			box.showAlert({message:"密码信息不正确"});
//			return;
//		}
		if(onPwdValidate()){
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
	
	/*修改密码结束*/
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
	
	return {
		init:function(){
			box.listen("yhgl", onload);
		},
		destroy: function(){}
	};
});