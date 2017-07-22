/**
 * 会员管理功能
 */
HOME.Core.register("plugin-hygl", function(box) {
	var hyglURL="retail/consumer/searchMerchConsumerInfo";//获得会员总信息，包括会员等级
	var xzhyURL="retail/consumer/insertMerchConsumer";//插入会员
	var upURL="/retail/consumer/updateMerchConsumerForHygl";//修改会员
	var getGrade = "retail/consumer/selectMerchConsumerGrade";//获得会员等级
	var getConsumerById = "retail/consumer/selectMerchConsumerById";//通过consumer_查询会员信息
	
	var mrhyjbURL="retail/consumer/insertDefaultGrade";
	
	var hyglContent = null;
	var parentView=null;
	var content=null;
	var pageIndex = 1;
	var pageSize = 20;
	var gradeList = [];//会员级别列表
	
	var mainBody = null;
	var addGradeButton = null;
	//定义一个会员等级列表
	var keyWord = null;
	//定义会员列表
	var isUpgradable = null;
	var searchGrade = null;
	function onload(view) {
		pageIndex = 1;
		pageSize = 20;
		
		parentView = view;
		parentView.closeMessage();
		parentView.showFooter(true);
		getGradeList();
	}


	
	function getGradeList(){
		var params = {};
		box.request({
			url:box.getContextPath()+ getGrade,
			data:{params:$.obj2str (params)},
			success: function(data){
				if(data && data.code=="0000" && data.result){				
					gradeList = data.result || [];
					if(gradeList.length == 0){
						ifHaveHyjb();
					}
					getConsumerList();
				}
			}
		});
	}

	//获得会员
	function getConsumerList(){
		var params = {page_index: pageIndex, page_size: pageSize};
		keyWord = parentView.find("#keyword").val();//keyword
		searchGrade = parentView.find("#grade_list").val();//级别
		
		if(keyWord){
			params.keyword = keyWord;
		}
		if(searchGrade && searchGrade !="0" ){
			params.grade = searchGrade;
		}
		if(parentView.find("#is_upgradable").is(':checked')){
			isUpgradable = "1";
			params.is_upgradable = isUpgradable;
		}else{
			isUpgradable = "0";
		}
		box.request({
			url:box.getContextPath()+ hyglURL,
			data:{params:$.obj2str (params)},
			success: showConsumer
		});
	}
	
	//显示会员列表
	function showConsumer(data){
		if(data && data.code=="0000"){
			var pageParam = data.pageparams;
			parentView.showPager(pageParam, function(param) {
				pageIndex = param.page_index;
				pageSize = param.page_size;
				getConsumerList();
			});
			//获得会员列表
			var consumerList = data.result;
			if(consumerList.length==0){
				box.showAlert({message: "对不起，没有查询到会员信息 ！"});			
			} else {
				for(var i=0; i<consumerList.length; i++) {
					var consumer = consumerList[i];
					consumer.index = (pageIndex-1)*pageSize+i+1;
					consumer.status_info = consumer.status == 1 ? "有效" : "无效";
					if(consumer.address){
						consumer.address = consumer.address.length>18 ? consumer.address.substring(0, 15)+"···" : consumer.address;
					}
				}
			}
			var content=box.ich.view_hygl({consumer_list:consumerList,grade_list:gradeList});
			parentView.empty().append(content);
			
			parentView.find(".table_view").tablesort([
	  		        {col:0,order:"asc",method:"advance",type:"number"},
	  		        {col:1,order:"asc",method:"advance",type:"string"},
	  		        {col:2,order:"asc",method:"advance",type:"string"},
	  		        {col:3,order:"asc",method:"advance",type:"string"},
	  		        {col:4,order:"desc",method:"advance",type:"number"},
	  		        {col:5,order:"desc",method:"advance",type:"number"},
	  		        {col:6,order:"asc",method:"advance",type:"string"},
	  		        {col:7,order:"asc",method:"advance",type:"string"}
			]);
			
			parentView.find(".table_view").fixedtableheader({
				parent: parentView,
				win: parentView.parent(),
				isshow: true
			});
			if(searchGrade && searchGrade != "0"){
				$("#grade_list").val(searchGrade);//选择默认的级别
			}
			if(isUpgradable && isUpgradable == "1"){
				parentView.find("#is_upgradable").prop("checked",true);
			}
			if(keyWord) {
				parentView.find("#keyword").val(keyWord);
			}
			//添加会员按钮监听事件
			parentView.find("#addconsumer").unbind("click").click(onAddHYGL);
			parentView.find("#queryKeywordOrGrade").unbind("click").click(onQueryButtonClick);
			parentView.find("table tbody tr a").unbind("click").click(upgradeConsumer);
			content.find("table tbody").unbind("click").click(getUpdConsumerInfo);
			
		}
	}

	//升级会员
	function upgradeConsumer(e){
		var target = $(e.target);
		var tr = $(target).closest("tr");
		var consumerId = $(tr).attr("data-consumer-id");
		var isUpgradable = $(tr).attr("data-is-upgradable");
		var grade = $(tr).attr("data-grade");
		grade = parseInt(grade) + 1 ;
		if(isUpgradable == "0"){
			box.showAlert({message:"对不起，该会员没有达到升级要求！"});
			return false ;
		}else if(grade >= gradeList.length){
			box.showAlert({message:"对不起，该会员已经是最高会员！"});
			return false ;
		}	
		
		var params = {consumer_id:consumerId, is_upgradable:"0", grade:grade};		
		box.request({
			url:box.getContextPath()+upURL,
			data:{params:$.obj2str (params)},
			success:function(data) {
				if(data && data.code=="0000"){
					var gradeMap = gradeList[gradeList.length-parseInt(grade)];
					$(tr).find("td[name='grade_name']").text(gradeMap.grade_name);
					$(tr).attr("data-is-upgradable","0");
					$(tr).find("a[name='upgrade']").css("visibility","hidden");
					$(tr).attr("data-grade", grade);
					box.showAlert({message:"升级会员信息成功！"});
					box.closeDialog({content: content});
					
				}else{
					box.showAlert({message:"升级会员信息失败！"});
				}
			}
		});
		return false;
	}
	
	function onQueryButtonClick() {
		pageIndex = 1;
		pageSize = 20;
		getConsumerList();
	}
	
	//通过id获得会员信息
	function getUpdConsumerInfo(e){
		var params = {};
		params.consumer_id = $(e.target).closest("tr").attr("data-consumer-id");
		if (!params.consumer_id ) {
			box.showAlert({message: "对不起，选择会员无效！"});
			return ;
		}
//		params.consumer_id = $(e.currentTarget).attr("data-consumer-id");
		box.request({
			url:box.getContextPath()+ getConsumerById,
			data:{params:$.obj2str (params)},
			success: showUpdConsumer
		});
	}
	
	//显示修改会员
	function showUpdConsumer(data){
		if(data && data.code=="0000") {
			var myConsumer =  null;
			if(data.result) {
				myConsumer = data.result;
				if(myConsumer.birthday){
					var birthday = myConsumer.birthday;
					birthday = new Date(birthday.substr(0, 4),birthday.substr(4, 2)-1, birthday.substr(6, 2));
					myConsumer.birthday = birthday.format("yyyy-MM-dd");
				}
				content=box.ich.view_hygl_add1({consumer:myConsumer,grade_list:gradeList});
				content=box.showDialog({
					title:"修改会员",
					width:680,
					height:480,
					content: content
				});
				content.find("#birth_day").datepicker();
				$("#upd_grade_list").val(myConsumer.grade);//选择默认的级别
				$("#cert_type").val(myConsumer.cert_type);//选择默认的级别
				$("#month_salary").val(myConsumer.month_salary);//选择默认月收入
				$("#degree").val(myConsumer.degree);//选择默认学历
				if(myConsumer.gender == 0){
					$("input[name=radio_sex]:eq(0)").prop("checked",true);
				}else{
					$("input[name=radio_sex]:eq(1)").prop("checked",true);
				}
				//status    2：无效   1：有效
				if(myConsumer.status == 2){
					$("input[name=status_radio]:eq(1)").prop("checked",true);
				}else{
					$("input[name=status_radio]:eq(0)").prop("checked",true);
				}
				
				content.find("#tr_curscore").show();//默认积分
				content.find("#submit").val("保 存");
				isAddConsumer=false;
				content.find("#submit").unbind("click").click(operateConsumer);
				content.find("#quxiao").click(function(e) {
					box.closeDialog({content: content});
				});
//				//新增会员编号
//				content.find("#merchIdTitleTd").show();
//				content.find("#merchIdTd").show();
			} else {
				box.showAlert({message: "获取会员信息错误！"});
			}
		} else {
			box.showAlert({message: result.msg});
		}
	}
	
	//显示添加会员
	function onAddHYGL(){
		content=box.ich.view_hygl_add1({grade_list:gradeList});
		box.showDialog({
			title:"新增会员",
			width:680,
			height:480,
			content: content,
			close: function(e){
				parentView.find("#addconsumer").removeAttr("disabled");
			}
		});
		isAddConsumer=true;
		content.find("#birth_day").datepicker();
		//保存
		content.find("#submit").click(operateConsumer);
		//取消按钮监听
		content.find("#quxiao").click(function(e) {
			box.closeDialog({content: content});
		});
	}
	
	//保存会员
	function operateConsumer(event) {
		//判断手机号是否重复
		var consumerId = ($(this).attr("data-consumer-id"));
		var telephone = content.find("#telephone").val();//手机号
		var consumerName = content.find("#consumer_name").val();//名称
		var cardId = content.find("#card_id").val();
		var certType = content.find("#cert_type").val();
		var radioSex = content.find("input[name='radio_sex']:checked").val();
		var birthDay = content.find("#birth_day").val();
		var zipCode = content.find("#zip_code").val();
		var email = content.find("#email").val();
		var certId = content.find("#cert_id").val();
		var status = content.find("input[name='status_radio']:checked").val();
		
		var monthSalary = content.find("#month_salary").val();//月收入
		var degree = content.find("#degree").val();//学历
		
		var messageList = [];
		if(!$.validateForms(content, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		
		var url = null;
		var backMessage = null;
		var grade = content.find("#upd_grade_list").val();//级别
		if(!grade && gradeList.length != 0){
			box.showAlert({message:"对不起，请选择会员级别！"});
			return;
		}
		var address = content.find("#address").val();//备注
		var params = {telephone:telephone, consumer_name:consumerName, grade:grade, card_id:cardId, gender:radioSex};
		
		if(status){
			params.status = status;
		}
		params.email = email;

		params.month_salary = monthSalary;
		
		params.zipcode = zipCode;
		
		if(birthDay){
			if(isdate(birthDay)){
				birthDay = new Date(birthDay.substr(0, 4), birthDay.substr(5, 2)-1, birthDay.substr(8, 2));
				birthDay = (birthDay).format("yyyyMMdd");
				content.find("#birth_day").css("border","1px solid #ddd");
				params.birthday = birthDay;
			}else{
				content.find("#birth_day").css("border","1px red solid");
				return;
			}
		}else{
			params.birthday = "";
		}
		params.degree = degree;
		params.cert_id = certId;
		
		if( certType){
			params.cert_type = certType;
		}
		
		params.address = address;

		if(consumerId && consumerId != "/" ){
			url = upURL;
			params.consumer_id = consumerId;
			backMessage = "修改";
		}else{
			params.curscore = 0;
			url = xzhyURL;
			backMessage = "新增";
		}
		box.request({
			url:box.getContextPath()+url,
			data:{params:$.obj2str (params)},
			success:function(data) {
				if(data && data.code=="0000"){
					box.showAlert({message:backMessage+"会员信息成功！"});
					box.closeDialog({content: content});
					getConsumerList();
				}else{
					box.showAlert({message:backMessage+"会员信息失败:"+data.msg});
				}
			}
		});
	}
	
	function isdate(strDate){
	   var strSeparator = "-"; //日期分隔符
	   var strDateArray;
	   var intYear;
	   var intMonth;
	   var intDay;
	   var boolLeapYear;
	   
	   strDateArray = strDate.split(strSeparator);
	   
	   if(strDateArray.length!=3) {
		   box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
		   return false;
	   }
	   
	   intYear = parseInt(strDateArray[0],10);
	   intMonth = parseInt(strDateArray[1],10);
	   intDay = parseInt(strDateArray[2],10);
	   
	   if(isNaN(intYear)||isNaN(intMonth)||isNaN(intDay)) {
		   box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
		   return false;
	   }
	   
	   if(intMonth>12||intMonth<1) {
		   box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
		   return false;
	   }
	   
	   if((intMonth==1||intMonth==3||intMonth==5||intMonth==7||intMonth==8||intMonth==10||intMonth==12)&&(intDay>31||intDay<1)) {
		   box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
		   return false;
	   }
	   
	   if((intMonth==4||intMonth==6||intMonth==9||intMonth==11)&&(intDay>30||intDay<1)) {
		   box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
		   return false;
	   }
	   
	   if(intMonth==2){
	      if(intDay<1) {
	    	  box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
	    	  return false;
	      }
	      
	      boolLeapYear = false;
	      if((intYear%100)==0){
	         if((intYear%400)==0) boolLeapYear = true;
	      }
	      else{
	         if((intYear%4)==0) boolLeapYear = true;
	      }
	      
	      if(boolLeapYear){
	    	  if(intDay>29) {
	    		  box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
	    		  return false;
	    	  }
	      }
	      else{
	         if(intDay>28) {
	        	 box.showAlert({message: "日期格式不正确或者输入的日期不合法！"});
	        	 return false;
	         }
	      }
	   }
	  // alert("ddd");
	   return true;
	}
	
	function DataCenter(consumerGrades) {
		this.consumerGrades = consumerGrades;	
		this.appendGrade = function() {
			var newGrade = {is_default_grade:'',grade_name:'', discount:100};
			var newGrades = [newGrade];
			for(var i=0; i<this.consumerGrades.length; i++) {
				newGrades.push(this.consumerGrades[i]);
			}
			this.consumerGrades = newGrades;
			var newTr = box.ich.view_default_hyjb_row(newGrade);
			var targetTr = mainBody.children(":first");
			if(targetTr && targetTr.length) {
				newTr.insertBefore(targetTr);
			} else {
				mainBody.append(newTr);
			}
			this.addListener(newTr[1]);
		};
		
		this.getData = function() {
			var data = [];

			data = this.consumerGrades;
			return data;
		};
		
	}
		
	// 判读是否有未设置会员级别
	function ifHaveHyjb() {
		box.showConfirm({
			message: "还没有会员级别列表，是否现在设置？", 
			title: "警告",
			ok: function() {
				loadFace();
			}
		});

	}
	
	function loadFace(){
		
		hyglContent = box.ich.view_default_hyjb();
		hyglContent = box.showDialog({
			title:"设置默认会员级别",
			width:630,
			height:400,
			content: hyglContent
		});
		
		mainBody = hyglContent.find("#main_body");
		consumerGrades = mainBody.children();
		addGradeButton = hyglContent.find("#add_grade_button");
		submitGradeButton = hyglContent.find("#submit_grade_button");

		addGradeButton.unbind('click').click(function(e) {
			onAddGradeButtionClick($(e.currentTarget));
		});
		
		submitGradeButton.unbind('click').click(function(e) {

			onSubmitGradeButtionClick($(e.currentTarget));
		});
		
		mainBody.unbind('click').click(function(e) {
			if($(e.target).attr('name')=='remove_tr_link') {
				onRemoveTrLinkClick($(e.target));
			}
		});
	}
	
	function onAddGradeButtionClick(me) {
		
		consumerGrades = mainBody.children();
		
		var newGrade = {is_default_grade:'',grade_name:'', discount:100};
		var newTr = box.ich.view_default_hyjb_row(newGrade);
		var targetTr = mainBody.children(":first");
		if(targetTr && targetTr.length) {
			newTr.insertBefore(targetTr);
		} else {
			mainBody.append(newTr);
		}
	}

	function onRemoveTrLinkClick(me) {
		me.parents("tr").remove();
	}
	
	function onSubmitGradeButtionClick(me) {
		var messageList = [];
		if(!$.validateForms(mainBody, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		consumerGrades = mainBody.children();
		var defConsumerGrade = null;
		var data = [];
		$.each(consumerGrades, function(i, tr) {
			var isDefaultGrade = $(this).find('input:radio[name="is_default_grade"]:checked').val();
			if(isDefaultGrade){
				defConsumerGrade = {is_default_grade:"1",grade_name:$(this).find("#grade_name").val(),discount:$(this).find("#discount").val()};
			}else{
				defConsumerGrade = {grade_name:$(this).find("#grade_name").val(),discount:$(this).find("#discount").val()};
			}
			data.push(defConsumerGrade);
			
		});
		
		prepareParamAndRequireData(mrhyjbURL, data, function() {
			box.showAlert({message: "保存成功!"});
			box.closeDialog({content:hyglContent});
			getGradeList();
		});
	}
	
	function prepareParamAndRequireData(url, paramObject, callback) {
		
		box.request({
			url: box.getContextPath() + url,
			data: {params: $.obj2str(paramObject)},
			success: function(data) {
				if(data && data.code=='0000') {
					callback(data.result, null);
				} else {
					logMe(data);
				}
			},
			error: function(data) {
				logMe(data);
			}
		});
	}
	
	function onClose(){
		keyWord = null;
		isUpgradable = null;
		searchGrade = null;
		pageIndex = 1;
		pageSize = 20;
	}
	
	return {
		init: function(){
			box.listen("hygl", onload);
			box.listen("hygl_close", onClose);
		},
		destroy: function() { }
	};
});