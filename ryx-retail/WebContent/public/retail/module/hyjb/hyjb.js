/**
 * 会员级别管理
 */
HOME.Core.register("plugin-hyjb", function(box) {
	// 接口地址
	var hyjbURL = "/retail/consumer/searchMerchConsumerGradeAndConsumerNumber";
	var addVipGradeURL="/retail/consumer/operationMerchConsumerGrade";
	// 全局数据
	var consumerGradeData = null;
	// 页面元素
	var parentView = null;
	var mainBody = null;
	var addGradeButton = null;
	
	function DataCenter(consumerGrades) {
		
		this.consumerGrades = consumerGrades;
		this.removedConsumerGrade = [];
		this.listenedTr = [];
		
		this.addListener = function(tr) {
			this.listenedTr.push(tr);
		};
		
		this.removeListener = function(tr) {
			var isRemoved = false;
			for(var i=0; i<this.listenedTr.length; i++) {
				if(isRemoved) this.listenedTr[i-1] = this.listenedTr[i];
				if(tr.attr("data-id")==$(this.listenedTr[i]).attr("data-id")) {
					delete(this.listenedTr[i]);
					isRemoved = true;
				}
			}
			if(isRemoved) this.listenedTr.length = this.listenedTr.length - 1;
		};
		
		// 倒序遍历listenedTr, offsize用来改变遍历的元素序号
		this.notify = function(grade, callback) {
			var offsize = null;
			for(var i=this.listenedTr.length; i>=0; i--) {
				callback($(this.listenedTr[i]), this, offsize);
				if(offsize) i = i + offsize;
			}
		};
		
		/*
		this.notify = function(grade, callback) {
			for(var i=0; i<this.listenedTr.length; i++) {
				callback($(this.listenedTr[i]), this);
			}
		};
		*/
		this.findGrade = function(index) {
			return this.consumerGrades[index];
		};
		
		this.appendGrade = function(grade) {
			var newGrade = {grade_id:Math.ceil(Math.random()*100000), operation_type:'add', grade:this.consumerGrades.length+1, grade_name:'', consumer_number:0, discount:100, upgrade_should:{}, upgrade_must:{}};
			if(grade) {
				newGrade.grade_name = grade.grade_name;
				newGrade.consumer_number = grade.consumer_number;
				if(grade.upgrade_must.point) {
					newGrade.upgrade_must.point = grade.upgrade_must.point;
				}
			}
			var newGrades = [newGrade];
			for(var i=0; i<this.consumerGrades.length; i++) {
				newGrades.push(this.consumerGrades[i]);
			}
			this.consumerGrades = newGrades;
			var newTr = box.ich.view_hyjb_row(newGrade);
			var targetTr = mainBody.children(":first");
			if(targetTr && targetTr.length) {
				newTr.insertBefore(targetTr);
			} else {
				mainBody.append(newTr);
			}
			this.addListener(newTr[0]);
		};
		
		this.refreshGrade = function(grade) {
			var index = this.consumerGrades.length - grade.grade;
			this.consumerGrades[index] = {grade_id:grade.grade_id, operation_type:this.consumerGrades[index].operation_type=='add'?'add':'upd', grade:grade.grade, grade_name:grade.grade_name, discount:grade.discount, consumer_number:grade.consumer_number, upgrade_should:{}, upgrade_must:{}};
			if(grade.upgrade_must && grade.upgrade_must.point) {
				this.consumerGrades[index].upgrade_must.point = grade.upgrade_must.point;
			}
			this.notify(grade, function(tr) {
				var gradeLabel = tr.find("#grade");
				var labelText = gradeLabel.text();
				if(labelText==grade.grade) {
					var numberLabel = tr.find("#consumer_number");
					numberLabel.html(grade.consumer_number);
				};
			});
		};
		
		this.removeGrade = function(grade) {
			if(!grade.operation_type || grade.operation_type!='add') {
				this.removedConsumerGrade.push(grade);
			}
			var index = this.consumerGrades.length - grade.grade;
			
			// 更新临近tr的会员数
			if(grade.consumer_number && grade.consumer_number!=0) {
				var closestGrade = null;
				if(index == this.consumerGrades.length-1) {
					if(index-1 >= 0) closestGrade = this.consumerGrades[index-1];
				} else {
					closestGrade = this.consumerGrades[index+1];
				}
				if(closestGrade) {
					closestGrade.consumer_number = parseFloat(closestGrade.consumer_number) + parseFloat(grade.consumer_number);
					this.refreshGrade(closestGrade);
				}
			}
			
			delete(this.consumerGrades[index]);
			for(var j=index+1; j<this.consumerGrades.length; j++) {
				this.consumerGrades[j-1] = this.consumerGrades[j];
			}
			this.consumerGrades.length = this.consumerGrades.length - 1;
			// notify方法中嵌套removeListener是双重循环listenedTr, 
			// 如果内层执行了removeListener, 外层会跳过下一个listenedTr元素, 所以设置offsize将跳过的这个位置补回来
			var offsize = null;
			this.notify(grade, function(tr, obj, offsize) {
				var gradeLabel = tr.find("#grade");
				var labelText = gradeLabel.text();
				if(labelText==grade.grade) {
					tr.remove();
					obj.removeListener(tr);
					offsize = -1;
				} else if(labelText>grade.grade) {
					gradeLabel.text(labelText-1);
				};
			});
			for(var i=0; i<index; i++) {
				this.consumerGrades[i].grade--;
				this.refreshGrade(this.consumerGrades[i]);
			}
		};
		
		this.getData = function(allData) {
			for(var i=0; i<this.removedConsumerGrade.length; i++) {
				this.removedConsumerGrade[i].operation_type = 'del';
			}
			var data = [];
			if(!allData) {
				for(var i=0; i<this.consumerGrades.length; i++) {
					if(this.consumerGrades[i].operation_type=='upd') data.push(this.consumerGrades[i]);
					else if(this.consumerGrades[i].operation_type=='add') data.push(this.consumerGrades[i]);
				}
			} else {
				data = this.consumerGrades;
			}
			return data.concat(this.removedConsumerGrade);
		};
		
	}
	
	function logMe(me) {
		box.console.log(me);
	}
	
	function onload(view) {
		gradeindex = 0;
		parentView = view;
		parentView.showFooter(true);
		prepareParamAndRequireData(hyjbURL, {}, loadFace);
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
	
	function loadFace(result, page) {
		var object = {list: result};
		parentView.empty().append(box.ich.view_hyjb(object));
		consumerGradeData = new DataCenter(result);
		initElementAndBindEvent();
	}
	
	function initElementAndBindEvent() {
		mainBody = parentView.find("#main_body");
		addGradeButton = parentView.find("#add_grade_button");
		submitGradeButton = parentView.find("#submit_grade_button");
		
		$.each(mainBody.children(), function(i, tr) {
			consumerGradeData.addListener(tr);
		});
		mainBody.unbind('click').click(function(e) {
			if($(e.target).attr('name')=='remove_tr_link') {
				onRemoveTrLinkClick($(e.target));
			}
		});
		mainBody.unbind('change').change(function(e) {
			if($(e.target).attr('type')=='text') {
				onInputChange($(e.target));
			}
		});
		addGradeButton.unbind('click').click(function(e) {
			onAddGradeButtionClick($(e.currentTarget));
		});
		submitGradeButton.unbind('click').click(function(e) {
			onSubmitGradeButtionClick($(e.currentTarget));
		});
		
		parentView.find(".table_view").fixedtableheader({
			parent: parentView,
			win: parentView.parent(),
			isshow: true
		});
	}
	
	function onRemoveTrLinkClick(me) {
		var removedTr = me.closest("tr");
		var consumerGrade = consumerGradeData.findGrade(removedTr.index());
		var gradeNumber  = removedTr.find("label[name='grade_number']").text();
		if(gradeNumber > 0) {
			var resultStr = null;
			if(consumerGrade.grade==1) resultStr = "删除级别1，其中的会员将提升到现在的级别2！";
			else resultStr = "删除级别" + consumerGrade.grade + "，其中的会员将降低到现在的级别" + (consumerGrade.grade-1) + "！";
			box.showConfirm({
				message: "该级别下已存在会员，"+resultStr+"是否确定删除？", 
				title: "警告",
				ok: function() {
					consumerGradeData.removeGrade(consumerGrade);
				}
			});
		} else {
			consumerGradeData.removeGrade(consumerGrade);
		}
	}
	
	function onInputChange(me) {
		var changedTr = me.closest("tr");
		var changedGrade = consumerGradeData.findGrade(changedTr.index());
		changedGrade[me.attr("name")] = me.val();
		consumerGradeData.refreshGrade(changedGrade);
	}
	
	function onAddGradeButtionClick(me) {
		consumerGradeData.appendGrade();
	}
	
	function onSubmitGradeButtionClick(me) {
		var messageList = [];
		if(!$.validateForms(mainBody, messageList)) {
			box.showAlert({message: messageList[0]});
			return;
		}
		var grades = {list: consumerGradeData.getData()};
		prepareParamAndRequireData(addVipGradeURL, grades, function() {
			box.showAlert({message: "保存成功!"});
			prepareParamAndRequireData(hyjbURL, {}, loadFace);
		});
	}
	
	return {
		init: function(){
			box.listen("hyjb", onload);
		},
		destroy: function() { }
	};
});