package com.ryx.social.retail.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ryx.framework.util.JsonUtil;

public class CommonVerifier {
	
	private static final Logger LOG = LoggerFactory.getLogger(CommonVerifier.class);
	
	SAXReader reader = null;
	String path = null;
	
	private static Map<String, Element> urlMap = null;

	public CommonVerifier() {
		reader = new SAXReader();
	}
	
	public CommonVerifier(String path) {
		reader = new SAXReader();
		this.path = path;
	}
	
	private synchronized Map<String, Element> getUrlMap() {
		if(urlMap == null) {
			urlMap = construct();
		}
		return urlMap;
	}
	
	private Map<String, Element> construct() {
		Map<String, Element> tmpMap = new HashMap<String, Element>();
		Document document = null;
		try {
			document = reader.read(new File(path));
			Element root = document.getRootElement();
			String baseUrl = root.elementText("base-url");
			Iterator<Element> mappingIt = root.elementIterator("mapping");
			while(mappingIt.hasNext()) {
				Element mapping = mappingIt.next();
				String url = mapping.elementText("url");
				if(baseUrl!=null) {
					url = baseUrl + url;
				}
				Element params = mapping.element("params");
				tmpMap.put(url, params);
			}
		} catch (Exception e) {
			LOG.error("construct错误",e);
		}
		return tmpMap;
	}
	
	protected Map<String, Object> init(HttpServletRequest request) {
		return null;
	}
	
	// object是参数, element是配置
	private VerifiedResult validate(Object parameter, Element configuration) {
		String configName = configuration.attributeValue("name");
		String configRequired = configuration.attributeValue("required");
		String configType = configuration.attributeValue("type");
		String configVerify = configuration.attributeValue("verify");
		String configDefault = configuration.attributeValue("default");
		// 判断xml配置是否有错: 1. 有没有配置name属性, 2. default类型能不能转为type类型
		// 配置不能为空的是不是为空
		// 配置类型是不是不符合
		// 配置校验是否成功
		if(configName==null) {
			return new VerifiedResult("配置错误：某param元素没有设置name属性");
		}
		if("true".equals(configRequired) && parameter==null) {
			if(configDefault==null) {
				return new VerifiedResult("参数错误："+configName+"不能为空");
			}
		}
		if("List".equals(configType)) {
			List<Map<String, Object>> listParam = null;
			try {
				if(parameter instanceof String) {
					listParam = JsonUtil.json2List((String) parameter);
				} else {
					listParam = (List<Map<String, Object>>) parameter;
				}
			} catch (Exception ex) {
				return new VerifiedResult("参数错误："+configName+"不能转换为"+configType+"类型");
			}
			// 如果listParam为空,list类型是不支持default
			if("true".equals(configRequired) && listParam==null) {
				return new VerifiedResult("参数错误："+configName+"不能为空");
			}
			// 如果listParam==null,说明可以为空,什么都不判断,直接返回true
			if(listParam!=null) {
				// 如果设置了notempty,则参数的长度不能为0
				if("notempty".equals(configVerify) && listParam.size()==0) {
					return new VerifiedResult("参数错误："+configName+"长度不能为0");
				}
				// 如果list中有配置则校验list中的参数
				List<Map<String, Object>> newListParam = new ArrayList<Map<String, Object>>();
				Map<String, Object> mapParam = null;
				if(configuration.elements("param").size()>0) {
					// 控制list中的每个map
					for(int index=listParam.size()-1; index>=0; index--) {
						try {
							mapParam = (Map<String, Object>) listParam.get(index);
						} catch (Exception e) {
							return new VerifiedResult("参数错误："+configName+"中的元素不能转换为Map类型");
						}
						Map<String, Object> newMapParam = new HashMap<String, Object>();
						Iterator<Element> paramIt = configuration.elementIterator("param");
						// 每个设置对应map参数中的每个元素
						while(paramIt.hasNext()) {
							Element paramElement = paramIt.next();
							String paramElementName = paramElement.attributeValue("name");
							if(paramElementName==null) {
								return new VerifiedResult("配置错误："+configName+"中某param元素没有设置name属性");
							}
							VerifiedResult mapResult = validate(listParam.get(index).get(paramElementName), paramElement);
							if(!mapResult.isFlag()) {
								return mapResult;
							}
							// 把校验后的元素放入newmap中
							newMapParam.put(paramElementName, mapResult.getData());
						}
						newMapParam.putAll(mapParam);
						newListParam.add(newMapParam);
					}
					return new VerifiedResult(newListParam);
				}
			}
			// 返回null
			return new VerifiedResult(listParam);
		} else if("Map".equals(configType)) {
			Map<String, Object> mapParam = null;
			try {
				if(parameter instanceof String) {
					mapParam = JsonUtil.json2Map((String) parameter);
				} else {
					mapParam = (Map<String, Object>) parameter;
				}
			} catch (Exception ex) {
				return new VerifiedResult("参数错误："+configName+"不能转换为"+configType+"类型");
			}
			// 如果mapParam为空,map类型是不支持default
			if("true".equals(configRequired) && mapParam==null) {
				return new VerifiedResult("参数错误："+configName+"不能为空");
			}
			// 如果mapParam为空,说明可以为空,什么都不判断,直接返回true
			if(mapParam!=null) {
				if("notempty".equals(configVerify) && mapParam.size()==0) {
					return new VerifiedResult("参数错误："+configName+"长度不能为0");
				}
				Map<String, Object> newMapParam = new HashMap<String, Object>();
				if(configuration.elements("param").size()>0) {
					Iterator<Element> paramIt = configuration.elementIterator("param");
					while(paramIt.hasNext()) {
						Element paramElement = paramIt.next();
						String paramElementName = paramElement.attributeValue("name");
						if(paramElementName==null) {
							return new VerifiedResult("配置错误："+configName+"中某param元素没有设置name属性");
						}
						VerifiedResult mapResult = validate(mapParam.get(paramElementName), paramElement);
						if(!mapResult.isFlag()) {
							return mapResult;
						}
						newMapParam.put(paramElementName, mapResult.getData());
					}
				}
				// 控制校验的结果是添加还是替换原数据
				newMapParam.putAll(mapParam);
				return new VerifiedResult(newMapParam);
			}
			// 返回的是null
			return new VerifiedResult(mapParam);
		} else if("BigDecimal".equals(configType)) {
			BigDecimal bigParam = null;
			String add = "";
			if(parameter!=null) {
				try {
					bigParam = new BigDecimal(parameter.toString());
				} catch (Exception ex) {
					return new VerifiedResult("参数错误："+configName+add+"不能转换为"+configType+"类型");
				}
			}
			return new VerifiedResult(bigParam);
		} else if("Integer".equals(configType)) {
			Integer intParam = null;
			String add = "";
			try {
				if(configDefault==null) {
					intParam = Integer.parseInt(parameter.toString());
				} else {
					add = "的default参数"+configDefault;
					intParam = Integer.parseInt(configDefault);
				}
			} catch (Exception ex) {
				return new VerifiedResult("参数错误："+configName+add+"不能转换为"+configType+"类型");
			}
			return new VerifiedResult(intParam);
		} else if("String".equals(configType) || configType==null) {
			String stringParam = null;
			if(parameter!=null) {
				try {
					stringParam = parameter.toString();
				} catch (Exception ex) {
					return new VerifiedResult("参数错误："+configName+"不能转换为String类型");
				}
				if(configVerify!=null) {
					if("notempty".equals(configVerify) && "".equals(stringParam)) {
						return new VerifiedResult("参数错误："+configName+"长度不能为0");
					} else if("number".equals(configVerify) && !Pattern.matches("-?\\d+", stringParam)) {
						return new VerifiedResult("参数错误："+configName+"只能包含数字");
					} else if("email".equals(configVerify) && !Pattern.matches("\\w+@\\w+\\..+", stringParam)) {
						return new VerifiedResult("参数错误："+configName+"只能形如aa@bb.cc");
					}
				}
			}
			return new VerifiedResult().setData(stringParam);
		} else {
			return new VerifiedResult("配置错误："+configName+"的类型"+configType+"不合法");
		}
	}
	
	public VerifiedResult validateParameter(HttpServletRequest request) {
		String url = request.getRequestURI();
		if(getUrlMap().containsKey(url)) {
			Element params = getUrlMap().get(url);
			Map<String, String[]> requestMap = request.getParameterMap();
			if(requestMap.size()==0) {
				return validate(null, params);
			}
			Map<String, Object> newRequestMap = new HashMap<String, Object>();
			for(Entry<String, String[]> requestEntry : requestMap.entrySet()) {
				String entryKey = requestEntry.getKey();
				String[] entryValueArray = requestEntry.getValue();
				// 原来是先判断个数,如果参数个数是1个再判断是不是params;现在先判断params,如果是则直接取第一个
				if("params".equals(entryKey)) {
					try {
						newRequestMap.put(entryKey, JsonUtil.json2Map(entryValueArray[0]));
					} catch (Exception e) {
						return new VerifiedResult("参数错误：params不能转换为Map类型");
					}
				} else {
					if(entryValueArray.length==1) {
						newRequestMap.put(entryKey, entryValueArray[0]);
					} else if(entryValueArray.length==0) {
						newRequestMap.put(entryKey, null);
					} else {
						newRequestMap.put(entryKey, entryValueArray);
					}
				}
			}
			return validate(newRequestMap, params);
		} else { // 如果没有在xml中配置这个链接,直接跳过
			return new VerifiedResult();
		}
	}
	
	protected Object automateService() {
		return null;
	}
	
}


