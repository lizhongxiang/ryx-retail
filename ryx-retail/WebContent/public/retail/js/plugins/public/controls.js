HOME.Core.register("plugin-framepage", function(box) {
	
	var controls = {};
	
	var $window = $(window);
	
	var ActivityParameter = null;//活动参数
	var unitObject=null; 
	var pageIndex = -1; // 按类型选择商品分页参数
	var pageSize = -1; // 按类型选择商品分页参数
	
	var timeout = -1;
	var onloadindex = -1;
	var firstimgs = null;
	
	controls.showIFrame = function(data) {
		var w = $window.width() - 80;
		var h = $window.height();
		h = Math.min(800, Math.max(560, h));

		var content = box.ich.r_iframedialog(data);
		box.showDialog({
			title: data.title,
			content: content,
			minWidth: 960, minHeight: 600,
			maxWidth: 1200, maxHeight: 800,
			closeText: "关闭(Esc)",
			width: w, height: h
		});

		$.checkInShell(function(b) {
			if(b) {
				content.find("iframe").attr("src", data.url);
			}
		});
	};

	var content = undefined;
	controls.newItemDialog = function(data, callback, flag) { // flag为true是商品销售, 其他为商品入库
		if(content) {
			box.closeDialog({content:content});
			content = undefined;
		}
		box.showAlert({message:"请在完善此商品信息后继续操作！"});
		if(flag) {
			content = box.ich.new_item_dialog2(data);
			content = box.showDialog({
				title: "新增商品",
				content: content,
				width: 675,
				height: 183
			});
		} else {
			content = box.ich.new_item_dialog(data);
			content = box.showDialog({
				title: "新增商品",
				content: content,
				width: 575,
				height: 183
			});
		}
		content.find("#new_item_dialog_cancel").unbind("click").click(function() {
			box.closeDialog({content:content});
		});
		
		content.find("#new_item_dialog_item_kind").keydown(function(){
			$(this).autocomplete({
				source: box.kinds.array
			});
		});	
		
		var itemKindInput = content.find("#new_item_dialog_item_kind");
		var pri4Input = content.find("#new_item_dialog_pri4");
		var unitNameInput = content.find("#new_item_dialog_unit_name");
		var submitButton = content.find("#new_item_dialog_submit");
		submitButton.unbind("click").click(function() {
			var messageList = [];
			if(!$.validateForms(content, messageList)) {
				box.showAlert({message: messageList[0]});
				content.find("input.validate").eq(0).focus();
				return;
			}
			var pri4 = pri4Input.val();
			var item_kind_id = itemKindInput.val();
			var unitName = unitNameInput.val();
			var itemName = content.find("#new_item_dialog_item_name").val();
			if(flag) {
				var pri1Input = content.find("#new_item_dialog_pri1");
				data.pri1 = pri1Input.val();
			}
			data.pri4 = pri4;
			data.item_bar = data.big_bar;
			data.item_kind_id = item_kind_id;
			data.item_name = itemName;
			data.unit_name = unitName;
			box.request({
				url:box.getContextPath() + "retail/item/createMerchItem",
				data:{params:$.obj2str(data)},
				success:function(json) {
					if(json && json.code=="0000") {
						box.showAlert({message:"新增商品成功！"});
						box.closeDialog({content:content});
						content = undefined;
						var itemBar = json.result.item_bar;
						if(callback) {
							box.request({
								url:box.getContextPath() + "retail/item/getItemDetail",
								data:{big_bar:itemBar},
								success:function(result) {
									if(result && result.code=="0000") {
										callback(result.result);
									} else {
										box.showAlert({message:json.msg});
									}
								}
							});
						}
					} else {
						box.showAlert({message:json.msg});
					}
				}
			});
		});
		unitNameInput.autocomplete({
			source: box.unit.array
		});
		if(flag) {
			var pri1Input = content.find("#new_item_dialog_pri1");
			pri1Input.focus();
		} else {
			pri4Input.focus();
		}
	};
	
	var choiceItemContent = undefined;
	// 按类型选择商品
	controls.choiceItemDialog = function(data, callback, focus, me, flag) {
		if(choiceItemContent) {
			box.closeDialog({content:choiceItemContent});
			choiceItemContent = undefined;
		}
		if(!data) {
			data = {};
		}
		data.item_kind_list = box.kinds.array;
		choiceItemContent = box.ich.choice_item_dialog(data);
		choiceItemContent = box.showDialog({
			title: "选择商品",
			content: choiceItemContent,
			width: 890,
			height: 540,
			beforeClose: function(param) {
				/*setTimeout(function() {
					param.callback();
				}, 2000);
				return false;*/
			},
			close: function(e) {
				choiceItemContent = undefined;
				if(focus) $(focus).select();
				if(me) me.removeAttr("disabled");
			}
		});
		choiceItemContent.find("#key").focus();
		choiceItemContent.unbind("click").click(function(e) {
			var target = $(e.target);
			
			var tmp = target.closest("a[name=item_button]");
			if(tmp[0]) target = tmp;
			
			var id = target.attr("data-id");
			var bigBar = target.attr("data-bar");
			if(target.attr("name")=="item_kind_a") {
				choiceItemContent.find("[name='item_kind_a']").removeClass("unselected_a").removeClass("selected_a");
				target.addClass("selected_a");
				target.closest("li").siblings().find("a").addClass("unselected_a");
				var key = choiceItemContent.find("#key").val();
				seachMerchItem(choiceItemContent, key, id, flag);
			} else if(target.attr("name")=="item_button") {
				if(callback) {
					box.request({
						url:box.getContextPath() + "retail/whse/getItemDetailWithWhse",
						data:{big_bar:bigBar, item_id:id},
						success:function(result) {
							if(result && result.code=="0000") {
								callback(result.result);
							} else {
								box.showAlert({message:json.msg});
							}
						}
					});
				}
			} else if(target.attr("name")=="query_by_key") {
				var itemKindId = choiceItemContent.find(".selected_a").attr("data-id");
				var key = choiceItemContent.find("#key").val();
				seachMerchItem(choiceItemContent, key, itemKindId, flag);
			} else if(target.attr("name")=="choice_item_dialog_cancel") {
				box.closeDialog({content:choiceItemContent});
				choiceItemContent = undefined;
			}
		});
		choiceItemContent.find("[name='item_kind_a']").eq(1).click(); // 默认取卷烟商品
	};
	
	function seachMerchItem(choiceItemContent, key, itemKindId, flag) {
		params = {page_index:pageIndex, page_size:pageSize};
		
		var url = "retail/item/searchMerchItem";
		if(key) {
			params.key = key;
			if(flag){
				params.status = 1;
			}
			url = "retail/item/searchMerchItemByLucene";
		}
		if(itemKindId) {
			params.item_kind_id = itemKindId;
		}
		firstimgs = null;
		onloadindex = -1;
		clearTimeout(timeout);
		
		box.request({
			url:box.getContextPath() + url,
			data:params,
			success:function(json) {
				if(json.result.length<=0){
					box.showAlert({message:"很抱歉，没有查询到数据！"});
				}
				if(json && json.code=="0000") {

					var itemListContent = choiceItemContent.find("#item_list_content");
					var list = json.result;
					var item_url = function() {
						if(box.isTobacco(this.item_kind_id)) {
							return box.getTobaccoImage(this.item_id);
						} else {
							return box.getItemImage(this.item_bar);
						}
					};
					onloadindex = 24;
					for ( var i in list) {
						list[i].item_url = item_url;
						if(i < 24)
							list[i].reload = list[i].item_url;
						else
							list[i].reload = box.getContextPath() + "public/img/loading-small.gif";
					}
					itemListContent.empty().append(box.ich.choice_item_dialog_li({item_list:json.result}));
					
					firstimgs = itemListContent.find(".cat_item img");

					itemListContent.unbind("scroll").scroll(onItemListScroll);
				} else {
					box.showAlert({message:json.msg});
				}
			}
		});
	}
	
	function onGetMerchItem() {
		
	}

	/**
	 * 缓加载卷烟图片
	 */
	function onItemListScroll(){
		clearTimeout(timeout);
		
		timeout = setTimeout(function() {
			if(firstimgs) {
				var $img = null, position = null;
				
				var len = firstimgs.length;
				for (var i=0; i<len; i++) {
					
					$img = $(firstimgs[i]);
					position = $img.position();
					
					if(position) {
						if(position.top >= 450) break;
						if(position.top >= -100 && position.top < 450) {
							$img.attr("src", $img.attr("data-src"));
						}
					}
				}
			}
		}, 300);
	}

	/**
	 * 缓加载卷烟图片
	 */
    /*
	function loadTobaccoImg() {
		timeout = setTimeout(function() {
			if(firstimgs != null) {
				var imgitem = null;
				if(firstimgs.length > onloadindex) {
					imgitem = $(firstimgs[onloadindex]);
					imgitem.attr("src", imgitem.attr("data-src"));
					onloadindex++;
					
					loadTobaccoImg();
				} else {
					clearTimeout(timeout);
				}
			} else {
				clearTimeout(timeout);
			}
		}, 100);
	}
	*/
	
	controls.printPhonePaymentTicket = function(data, phoneNum, payMoney) {
		var num = data.field2.substring(0,6)+"********"+data.field2.substring(data.field2.length-4,data.field2.length);
		var content = "\n\n";
		content += "         公共事业缴费单\n\n";
		content += "----------------------------- ---\n";
		content += "零售名称:"+data.merch_name+"\n";
		content += "商户编号:"+data.field42+"\n";
		content += "终端编号:"+data.field41+"\n";
		content += "卡号:"+num+"\n";
		content += "发卡行:"+data.issuer+"\n";
		content += "收单行:"+data.acquirer+"\n";
		content += "交易类型:手机充值\n";
		content += "缴费号码:"+phoneNum+"\n";
		content += "参 考 号:"+data.field37+"\n";
		content += "交易日期:"+data.field13+" 时间:"+data.field12+"\n";
//		content += "交易金额:"+data.field4+" 元\n";
		content += "交易金额:"+$.parseMoney(payMoney)+" 元\n";
		content += "备注:\n\n";
		box.console.log(content);
		box.com.print(content);
	};
	
	controls.print = function(order, total, itemList, card) {
		var tobaccoItemList=new Array();
		$.each(itemList,function(index,tobaccoitem){
			if(box.isTobacco(tobaccoitem.item_kind_id)){
				tobaccoItemList.push(tobaccoitem);
			}
		});
		var QrCode = null;
		if(tobaccoItemList.length) {
//			QrCode = activity(order,tobaccoItemList);
			QrCode=order.act_keys;
		}
		
		onPrint(order, total, itemList, card, QrCode, 1);
	};
	
	function onPrint(order, total, itemList, card, QrCode, count) {
		var newPromotionMoney = 0;
		if(total.adjusted_amount){
			newPromotionMoney = total.adjusted_amount;
		} else if (total.oriAmount && total.input_amount) {
			newPromotionMoney = total.oriAmount-total.input_amount;
		} else if (total.oriAmount && total.amount) {
			newPromotionMoney = total.oriAmount-total.amount;
		}
		if(count > box.user.ticket.num) return;
		
		var strarr = new Array();
		strarr.push("\n" + box.user.ticket.welcome_word + "\n");
		strarr.push("--------------------------------\n");
		strarr.push("单号：" + order.order_id + "\n");
		strarr.push("时间：" + order.order_date_label + "\n");
		strarr.push("--------------------------------\n");
		strarr.push("  商品名    | 数量 | 售价 | 金额\n");
		strarr.push("　\n");
		var item = null;
		for(var i=0; i<itemList.length; i++) {
			item = itemList[i];
			strarr.push($.parseLength(item.item_name, 13, false, true));
			strarr.push($.parseLength(""+item.qty_ord, 5, true));
			strarr.push(" " + $.parseLength($.parseMoney(item.pri), 6, true));
			strarr.push(" " + $.parseLength($.parseMoney(item.amt_ord), 6, true), "\n");
		}
		strarr.push("--------------------------------\n");
		strarr.push("数量：" + $.parseLength(order.qty_ord_total, 7, true) + "     ");
		strarr.push("合计：" + $.parseLength($.parseMoney(order.amtys_ord_total), 7, true) + "\n");
		strarr.push("实收：" + $.parseLength($.parseMoney(order.amt_ord_total), 7, true) + "     ");
		strarr.push("找零：" + $.parseLength($.parseMoney(order.change), 7, true) + "\n");
		strarr.push("优惠：" + $.parseLength($.parseMoney(newPromotionMoney), 7, true) +  "\n");
//		strarr.push("抹零：" + $.parseLength($.parseMoney(order.moling), 7, true) +  "\n");
		// 因为挂账算作现金支付, 所以需要根据pmt_status来判断挂账
		if(order.pmt_status=="02") {
			strarr.push("结算方式：挂账\n");
			//strarr.push("此销售单未付款（顾客签字）\n　\n　\n");
		} else { // 真正的刷卡和现金结算
			if(order.pay_type==1){
				strarr.push("结算方式：现金结算\n");
			}
			if(order.pay_type==2){
				strarr.push("结算方式：刷卡结算\n");
			}
		}
		strarr.push("--------------------------------\n");
		var hasc = false;
		if(card && card.card_id) {
			hasc = true;
			strarr.push("会员卡号：" + card.card_id + "\n");
		}
		if(box.user.ticket.phone) {
			hasc = true;
			strarr.push("本店电话：" + box.user.ticket.phone + "\n");
		}
		if(box.user.ticket.note) {
			hasc = true;
			strarr.push(box.user.ticket.note + "\n");
		}
		if(hasc) {
			strarr.push("--------------------------------\n");
		}
		
		box.console.log(strarr.join(""));
		if(QrCode && QrCode.length>0) {
			box.com.print(strarr.join(""));

			setTimeout(function() {
				for(var i=0;i<QrCode.length;i++){
					var url=box.getContextFullPath()+"consumer/lottery?key="+QrCode[i];
					box.com.printQr(url);
					box.console.log("第"+i+"个url:"+url);
				}
				
				strarr = new Array();
				strarr.push("    扫描二维码,大奖等你拿\n");
				strarr.push("--------------------------------\n");
				strarr.push("　\n　\n　\n　\n");
				box.com.print(strarr.join(""));
				
				if(count < box.user.ticket.num) {
					setTimeout(function() {
						onPrint(order, total, itemList, card, QrCode, count+1);
					}, 200);
				}
			}, 100);
			
		} else {
			strarr.push("　\n　\n　\n　\n");
			box.com.print(strarr.join(""));
			
			if(count < box.user.ticket.num) {
				setTimeout(function() {
					onPrint(order, total, itemList, card, QrCode, count+1);
				}, 200);
			}
		}
	}
	
	function activity(order,itemList){
		var QrCode = new Array();
		var havCard = "1";
		if(!order.card_id){
			havCard = "0";
		}
		var payType = "1";
		for(var i=0;i<ActivityParameter.length;i++){
			var param = ActivityParameter[i];
			if((param.member_lmt == havCard||param.member_lmt=="0") && (param.pay_type == "0" || param.pay_type == payType)){
				if(ActivityParameter[i].is_item_lmt==0){
					QrCode.push(ActivityParameter[i].act_id);
				}else{
					var item=null;
					for(var j=0; j<itemList.length; j++) {
						item=itemList[j];
						if(ActivityParameter[i].unitObject[item.big_bar]){
							QrCode.push(ActivityParameter[i].act_id);
							continue;
						}
					}
				}
			}
		}
		return QrCode;
	}
	
	function initUnits(unitInfo) {
		unitObject.array = unitInfo;
		$.each(unitInfo, function(index, unit) {
			unitObject[unit.item_id] = "1";
		});
	}
	
	function getActivity(){
		box.request({
			url: box.getContextPath() + "retail/lottery/getActivityParameter",
			success:function(data){
				ActivityParameter=data.result;
				for(var i=0;i<ActivityParameter.length;i++){
					unitObject = {array: []};
					initUnits(ActivityParameter[i].item_list);
					ActivityParameter[i].unitObject=unitObject;
				}
			},
			showLoading: false
		});
	}
	
	return {
		init: function(){
			box.register("controls", controls);
			getActivity();
		},
		destroy: function() { }
	};
});