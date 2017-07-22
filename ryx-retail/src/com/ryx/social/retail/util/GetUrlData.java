package com.ryx.social.retail.util;

import java.util.HashMap;
import java.util.Map;

import com.ryx.framework.util.StringUtil;

/**
 * 从新商盟跨越登录直接访问信息
 * @author 徐虎彬
 * @date 2014年3月3日
 */
public class GetUrlData {
	/**
	 * 通过URL获取返回数据 
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @return
	 */
	public String getDataFromUrl(String id){
//		String id="6901028180580";
		//获取卷烟系统服务地址
		String cgtItemUrl = "http://sd.xinshangmeng.com/ecweb/order/cgtDetailNew.htm?cgtCode="+id;
		Map map=null;
		//从卷烟系统服务上获取数据
		String json = MyHttpUtil.post(cgtItemUrl, map);
		
		return json;

	}
	/**
	 * 通过给定字符串数据截获所需数据
	 * @author 徐虎彬
	 * @date 2014年3月3日
	 * @return
	 */
	public Map<String,Object> cutOutData(String id){
		String data=getDataFromUrl(id);
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("describe","");
		map.put("name","");
		map.put("subjectColor","");
		map.put("tipColor","");
		map.put("tipType","");
		map.put("tarContent","");
		map.put("co","");
		map.put("pack","");
//		if(data!=null && data!="" && data.trim().length()!=-1 ){
		if(!StringUtil.isBlank(data)){
			String str="<p style=\"margin-left:10px;margin-right:10px;\">";
			data=data.substring(data.indexOf(str)+str.length());
			if(!StringUtil.isBlank(data) ){
				if(data.indexOf("</p>")!=-1){
					map.put("describe",data.substring(0,data.indexOf("</p>")).trim());
				}
				if(data.indexOf("卷烟名称：")!=-1){
					data=data.substring(data.indexOf("卷烟名称：")+5);
					if(!StringUtil.isBlank(data)){
						if(data.indexOf("</p>")!=-1){
							map.put("name",data.substring(0,data.indexOf("</p>")).trim());
						}
						if(data.indexOf("主体颜色：")!=-1){
							data=data.substring(data.indexOf("主体颜色：")+5);
							if(!StringUtil.isBlank(data)){
								if(data.indexOf("</p>")!=-1){
									map.put("subjectColor",data.substring(0,data.indexOf("</p>")).trim());
								}
								if(data.indexOf("滤嘴颜色：")!=-1){
									data=data.substring(data.indexOf("滤嘴颜色：")+5);
									if(!StringUtil.isBlank(data)){
										if(data.indexOf("</p>")!=-1){
											map.put("tipColor",data.substring(0,data.indexOf("</p>")).trim());
										}
										if(data.indexOf("卷烟类型：")!=-1){
											data=data.substring(data.indexOf("卷烟类型：")+5);
											if(!StringUtil.isBlank(data)){
												if(data.indexOf("</p>")!=-1){
													map.put("tipType",data.substring(0,data.indexOf("</p>")).trim());
												}
												if(data.indexOf("焦油含量：")!=-1){
													data=data.substring(data.indexOf("焦油含量：")+5);
													if(!StringUtil.isBlank(data)){
														if(data.indexOf("</p>")!=-1){
															map.put("tarContent",data.substring(0,data.indexOf("</p>")).trim());
														}
														if(data.indexOf("一氧化碳：")!=-1){
														data=data.substring(data.indexOf("一氧化碳：")+5);
															if(!StringUtil.isBlank(data)){
																if(data.indexOf("</p>")!=-1){
																	map.put("co",data.substring(0,data.indexOf("</p>")).trim());
																}
																if(data.indexOf("包装形式：")!=-1){
																	data=data.substring(data.indexOf("包装形式：")+5);
																	if(!StringUtil.isBlank(data)){
																		if(data.indexOf("</p>")!=-1){
																			map.put("pack",data.substring(0,data.indexOf("</p>")).trim());
																		}
																	}
																}
															}
														}	
													}
												}	
											}
									    }
									}
								}
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	public static void main(String[] args) {
		GetUrlData getData = new GetUrlData();
		String a = " <pp>";
		System.out.println(a.indexOf("<p>"));
//		getData.getDataFromUrl();
//		getData.cutOutData();
	}
	
	
}
