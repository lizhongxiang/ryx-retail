package com.ryx.social.retail.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLUtil {
	
	public static String initSQLIn(Map<String, Object> paramMap, String paramEntry) {
		return initSQLInORNotIn(paramMap, null, null, paramEntry, 1000, SQLFlag.IN);
	}
	
	public static String initSQLIn(Map<String, Object> paramMap, List<Object> paramList, String paramEntry) {
		return initSQLInORNotIn(paramMap, null, paramList, paramEntry, 1000, SQLFlag.IN);
	}
	
	public static String initSQLIn(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String paramEntry) {
		return initSQLInORNotIn(paramMap, sqlBuilder, paramList, paramEntry, 1000, SQLFlag.IN);
	}
	
	public static String initSQLNotIn(Map<String, Object> paramMap, String paramEntry) {
		return initSQLInORNotIn(paramMap, null, null, paramEntry, 1000, SQLFlag.NOT_IN);
	}
	
	public static String initSQLNotIn(Map<String, Object> paramMap, List<Object> paramList, String paramEntry) {
		return initSQLInORNotIn(paramMap, null, paramList, paramEntry, 1000, SQLFlag.NOT_IN);
	}
	
	public static String initSQLNotIn(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String paramEntry) {
		return initSQLInORNotIn(paramMap, sqlBuilder, paramList, paramEntry, 1000, SQLFlag.NOT_IN);
	}
	
	private static String initSQLInORNotIn(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String paramEntry, int splitSize, SQLFlag operator) {
		if(paramMap==null || paramEntry==null) return "";
		
		SQLParam param = null;
		String fieldName = null;
		Object defaultValue = null;
		if(SQLParam.shouldParse(paramEntry)) {
			param = new SQLParam(paramEntry);
			fieldName = param.getName();
			defaultValue = param.getDefault();
		} else {
			fieldName = paramEntry;
		}
		
	    splitSize = Math.min(splitSize, 1000);
	    
		if(!paramMap.containsKey(fieldName) && defaultValue==null) return "";

		Object paramValue = paramMap.get(fieldName);
	    StringBuilder tempBuilder = new StringBuilder();
		String[] paramValueArray = paramValue==null ? (defaultValue==null ? new String[]{null} : defaultValue.toString().split(",")) : 
			paramValue.toString().split(",");
		if(sqlBuilder!=null) {
	    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
	    	int sqlLength = sqlBuilder.length();
	    	if(lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(" AND");
	    	tempBuilder.append(" ");
		}
	    int paramLength = paramValueArray.length;
	    if(paramLength==1) {
	    	if(paramList!=null) paramList.add(paramValueArray[0]);
    		tempBuilder.append(param!=null ? param.getFullName() : fieldName);
    		tempBuilder.append(operator==SQLFlag.IN ? " = ?" : " != ?");
	    } else {
		    int splitCount = paramLength%splitSize==0 ? paramLength/splitSize : (paramLength/splitSize) + 1;
		    for (int i = 0; i < splitCount; i++) {
		        int fromIndex = i * splitSize;
		        int toIndex = Math.min(fromIndex + splitSize, paramLength);
		        if (i != 0) tempBuilder.append(" OR ");
		        tempBuilder.append(param!=null ? param.getFullName() : fieldName).append(" " + (operator==SQLFlag.IN ? "IN" : "NOT IN") + " (");
		        for(int j = fromIndex; j < toIndex; j++) {
		        	if(paramList!=null) paramList.add(paramValueArray[j]);
		        	if(j==fromIndex) tempBuilder.append("?");
		        	else tempBuilder.append(", ?");
		        }
		        tempBuilder.append(")");
		    }
	    }
	    if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
	    return tempBuilder.toString();
	}
	
	public static String initSQLOrder(Map<String, Object> orderMap) {
		return initSQLOrder(null, orderMap);
	}
	
	public static String initSQLOrder(StringBuilder sqlBuilder, Map<String, Object> paramMap, String... paramEntries) {
		if(paramMap==null || paramMap.isEmpty() || paramEntries==null || paramEntries.length==0) return "";
		StringBuilder tempBuilder = new StringBuilder();
		int index = 0;
		for(String paramEntry : paramEntries) {
			SQLParam param = null;
			String fieldName = null;
			Object fieldValue = null;
			String defaultValue = null;
			if(SQLParam.shouldParse(paramEntry)) {
				param = new SQLParam(paramEntry);
				fieldName = param.getName();
				defaultValue = param.getDefault();
			} else {
				param = null;
				fieldName = paramEntry;
			}
			if((fieldValue=paramMap.get(fieldName))!=null || defaultValue!=null) {
				if(index++ != 0) tempBuilder.append(",");
				tempBuilder.append(" ");
				tempBuilder.append(param.getFullName());
				String sortRulePattern = fieldValue==null ? defaultValue : fieldValue.toString();
				if("desc".equalsIgnoreCase(sortRulePattern) || "2".equalsIgnoreCase(sortRulePattern)) tempBuilder.append(" DESC");
				else tempBuilder.append(" ASC");
			}
		}
		if(tempBuilder.length()!=0) tempBuilder.insert(0, " ORDER BY");
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	
	public static String initSQLOrder(String fieldName, String orderValue, String... others) {
		return initSQLOrder(null, fieldName, orderValue, others);
	}
	
	public static String initSQLOrder(StringBuilder sqlBuilder, String fieldName, String orderValue, String... others) {
		if(fieldName==null || orderValue==null) return "";
		StringBuilder tempBuilder = new StringBuilder();
		orderValue = orderValue.trim();
		if("2".equals(orderValue) || "DESC".equals(orderValue.toUpperCase())) {
			tempBuilder.append(" " + fieldName + " DESC,");
		} else {
			tempBuilder.append(" " + fieldName + " ASC,");
		}
		if(others!=null) {
			int othersLength = others.length;
			if(othersLength/2>0 && othersLength%2==0) {
				for(int i=0; i<othersLength; i=i+2) {
					fieldName = others[i];
					orderValue = others[i+1];
					if("2".equals(orderValue) || "DESC".equals(orderValue.toUpperCase())) {
						tempBuilder.append(" " + fieldName + " DESC,");
					} else {
						tempBuilder.append(" " + fieldName + " ASC,");
					}
				}
			}
		}
		String tempSql = tempBuilder.insert(0, " ORDER BY").substring(0, tempBuilder.length()-1);
		if(sqlBuilder!=null) sqlBuilder.append(tempSql);
		return tempSql;
	}
	
	/**
	 * 不支持null为key的参数
	 * @param paramMap 用来拼接SQL的参数, key为字段名, value为操作数据表的值
	 * @param paramList 存放paramMap中的value, 用来替代SQL中的占位符
	 * @param paramEntries 需要拼接到SQL的字段名
	 * @return
	 */
	public static String initSQLEqual(Map<String, Object> paramMap, String... paramEntries) {
		return initSQLEqual(paramMap, null, null, null, paramEntries);
	}

	public static String initSQLEqual(Map<String, Object> paramMap, SQLFlag prefix, String... paramEntries) {
		return initSQLEqual(paramMap, null, null, prefix, paramEntries);
	}
	
	public static String initSQLEqual(Map<String, Object> paramMap, List<Object> paramList, String... paramEntries) {
		return initSQLEqual(paramMap, null, paramList, null, paramEntries);
	}
	
	public static String initSQLEqual(Map<String, Object> paramMap, List<Object> paramList, SQLFlag prefix, String... paramEntries) {
		return initSQLEqual(paramMap, null, paramList, prefix, paramEntries);
	}
	
	public static String initSQLEqual(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String... paramEntries) {
		return initSQLEqual(paramMap, sqlBuilder, paramList, null, paramEntries);
	}

	public static String initSQLEqual(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, SQLFlag prefix, String... paramEntries) {
		if(paramMap==null || paramMap.isEmpty() || paramEntries==null || paramEntries.length==0) return "";
		StringBuilder tempBuilder = new StringBuilder();
		String prefixPattern = prefix==null ? " AND": prefix==SQLFlag.AND ? " AND" : ",";
		int index = 0;
		for(String paramEntry : paramEntries) {
			SQLParam param = null;
			String fieldName = null;
			Object defaultValue = null;
			if(SQLParam.shouldParse(paramEntry)) {
				param = new SQLParam(paramEntry);
				fieldName = param.getName();
				defaultValue = param.getDefault();
			} else {
				param = null;
				fieldName = paramEntry;
			}
			if(fieldName!=null && paramMap.containsKey(fieldName) || defaultValue!=null) {
				if(index++==0) {
					if(sqlBuilder!=null) {
				    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
				    	int sqlLength = sqlBuilder.length();
						if(lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(prefixPattern);
					}
				} else {
					tempBuilder.append(prefixPattern);
				}
				tempBuilder.append(" ");
				tempBuilder.append(param!=null ? param.getFullName() : fieldName);
				tempBuilder.append(" = ?");
				if(paramList!=null) {
					Object paramValue = paramMap.get(fieldName); // 之前需要判断paramValue是否为null决定是否拼接sql, 现在不需要这个条件所以在这里声明变量
					if(paramValue!=null && !"".equals(paramValue)) paramList.add(paramValue);
					else paramList.add(defaultValue);
				}
			}
		}
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	
	public static String initSQLLike(Map<String, Object> paramMap, List<Object> paramList, String paramEntry) {
		return initSQLLike(paramMap, null, paramList, paramEntry);
	}
	
	public static String initSQLLike(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String paramEntry) {
		if(paramMap==null || paramMap.isEmpty()) return "";
		StringBuilder tempBuilder = new StringBuilder();
		if(!paramMap.containsKey(paramEntry)) return "";
		if(sqlBuilder!=null) {
	    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
	    	int sqlLength = sqlBuilder.length();
	    	if(sqlBuilder!=null && lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(" AND");
		}
    	if(paramList!=null) paramList.add((paramMap.get(paramEntry)==null?"":paramMap.get(paramEntry)) + "%");
    	tempBuilder.append(" ");
		tempBuilder.append(paramEntry);
		tempBuilder.append(" LIKE ?");
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	/*
	public static String initSQLOriginal(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String originalSql, String paramEntry) {
		StringBuilder tempBuilder = new StringBuilder();
		if(sqlBuilder!=null) {
	    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
	    	int sqlLength = sqlBuilder.length();
	    	if(sqlBuilder!=null && lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(" AND");
		}
    	Object paramValue = null;
    	if(paramMap!=null) paramValue = paramMap.get(paramEntry);
    	if(paramList!=null && paramValue!=null) paramList.add(paramValue);
    	tempBuilder.append(" ");
		tempBuilder.append(originalSql);
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	*/
	public static String initSQLOriginal(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String originalSql, String... paramEntries) {
		StringBuilder tempBuilder = new StringBuilder();
		if(sqlBuilder!=null) {
	    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
	    	int sqlLength = sqlBuilder.length();
	    	if(sqlBuilder!=null && lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(" AND");
		}
    	if(paramMap!=null && paramList!=null) {
        	for(String paramEntry : paramEntries) {
        		paramList.add(paramMap.get(paramEntry));
        	}
    	}
    	tempBuilder.append(" ");
		tempBuilder.append(originalSql);
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, String fieldName, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, null, null, fieldName, true, true, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, String fieldName, boolean isLeftClosed, boolean isRightClosed, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, null, null, fieldName, isLeftClosed, isRightClosed, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, StringBuilder sqlBuilder, String fieldName, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, sqlBuilder, null, fieldName, true, true, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, StringBuilder sqlBuilder, String fieldName, boolean isLeftClosed, boolean isRightClosed, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, sqlBuilder, null, fieldName, isLeftClosed, isRightClosed, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, List<Object> paramList, String fieldName, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, null, paramList, fieldName, true, true, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, List<Object> paramList, String fieldName, boolean isLeftClosed, boolean isRightClosed, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, null, paramList, fieldName, isLeftClosed, isRightClosed, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String fieldName, String... paramKeys) {
		return initSQLBetweenAnd(paramMap, sqlBuilder, paramList, fieldName, true, true, paramKeys);
	}
	
	public static String initSQLBetweenAnd(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String fieldName, boolean isLeftClosed, boolean isRightClosed, String... paramKeys) {
		int paramLength = 0;
		if(paramMap==null || paramMap.isEmpty() || paramKeys==null || (paramLength=paramKeys.length)==0) return "";
		StringBuilder tempBuilder = new StringBuilder();
		String floorKey = null;
		Object floorValue = null;
		if(paramLength>0) {
			floorKey = paramKeys[0];
			if(floorKey!=null && paramMap.containsKey(floorKey) && (floorValue=paramMap.get(floorKey))!=null) {
				if(sqlBuilder!=null) {
			    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
			    	int sqlLength = sqlBuilder.length();
					if(lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) tempBuilder.append(" AND");
				}
				tempBuilder.append(" ");
				tempBuilder.append(fieldName);
				tempBuilder.append(isLeftClosed ? " >= ?" : " > ?");
				if(paramList!=null) paramList.add(floorValue);
			}
		}
		if(paramLength>1) {
			String ceilingKey = paramKeys[1];
			Object ceilingValue = null;
			if(ceilingKey!=null && paramMap.containsKey(ceilingKey) && (ceilingValue=paramMap.get(ceilingKey))!=null) {
				if(floorKey!=null && paramMap.containsKey(floorKey) && (floorValue=paramMap.get(floorKey))!=null) {
					tempBuilder.append(" AND");
				}
				tempBuilder.append(" ");
				tempBuilder.append(fieldName);
				tempBuilder.append(isRightClosed ? " <= ?" : " < ?");
				if(paramList!=null) paramList.add(ceilingValue);
			}
		}
		if(sqlBuilder!=null) sqlBuilder.append(tempBuilder);
		return tempBuilder.toString();
	}
	
	public static String initSQLInsertValues(Map<String, Object> paramMap, String... paramEntries) {
		return initSQLInsertValues(paramMap, null, null, paramEntries);
	}
	
	public static String initSQLInsertValues(Map<String, Object> paramMap, StringBuilder sqlBuilder, String... paramEntries) {
		return initSQLInsertValues(paramMap, sqlBuilder, null, paramEntries);
	}
	
	public static String initSQLInsertValues(Map<String, Object> paramMap, List<Object> paramList, String... paramEntries) {
		return initSQLInsertValues(paramMap, null, paramList, paramEntries);
	}
	
	public static String initSQLInsertValues(Map<String, Object> paramMap, StringBuilder sqlBuilder, List<Object> paramList, String... paramEntries) {
		if(paramMap==null || paramMap.isEmpty() || paramEntries==null || paramEntries.length==0) return "";
		StringBuilder fieldBuilder = new StringBuilder();
		StringBuilder placeholderBuilder = new StringBuilder();
		int index = 0;
		SQLParam param = null;
		for(String paramEntry : paramEntries) {
			String fieldName = null;
			Object defaultValue = null;
			if(SQLParam.shouldParse(paramEntry)) {
				param = new SQLParam(paramEntry);
				fieldName = param.getName();
				defaultValue = param.getDefault();
			} else {
				param = null;
				fieldName = paramEntry;
			}
			if(fieldName!=null) {
				if(paramMap.containsKey(fieldName) || defaultValue!=null) {
					if(index++==0) {
						fieldBuilder.append(" (");
						fieldBuilder.append(param!=null ? param.getFullName() : fieldName);
						placeholderBuilder.append(" (?");
					} else {
						fieldBuilder.append(", ");
						fieldBuilder.append(param!=null ? param.getFullName() : fieldName);
						placeholderBuilder.append(", ?");
					}
					if(paramList!=null) {
						Object paramValue = paramMap.get(fieldName);
						if(paramValue!=null && !"".equals(paramValue)) paramList.add(paramValue);
						else paramList.add(defaultValue);
					}
				}
			}
		}
		fieldBuilder.append(")");
		placeholderBuilder.append(")");
		String tempSql = fieldBuilder + " VALUES" + placeholderBuilder;
		if(sqlBuilder!=null) sqlBuilder.append(tempSql);
		return tempSql;
	}
	
	public static void main(String[] args) {
		// 测试SQLParam
		/*
		SQLParam param = new SQLParam("a.:order_id:");
		System.out.println(param.getAlias());
		System.out.println(param.getName());
		System.out.println(param.getDefault());
		*/
		// 测试 in / not in / = / !=
//		/*
		Map params = new HashMap<String, Object>();
		params.put("order_id", null);
//		params.put("order_date", "20140506");
		StringBuilder sb = new StringBuilder("select * from sale_order a, sale_order_line b where 1=1");
		List list = new ArrayList();
		System.out.println(initSQLIn(params, sb, list, "A.order_id"));
//		System.out.println(initSQLIn(params, sb, list, "order_date:20140506,20140507"));
		System.out.println(list);
//		*/
		// 测试order by
		/*
		StringBuilder s = new StringBuilder("select * from a where 1=1");
		System.out.println(initSQLOrder(s, "abc","1","bcd","2"));
		*/
		// 测试order by
		/*
		Map b = new HashMap<String, Object>();
		StringBuilder s = new StringBuilder("select * from a where 1=1");
		b.put("abc", "1");
		b.put("eee", "2");
		System.out.println(initSQLOrder(s, b));
		*/
		// 测试 =
		/*
		StringBuilder s = new StringBuilder("select * from a where merch_id=?");
		Map c = new HashMap<String, Object>();
		List d = new ArrayList();
		
		c.put("order_id", null);
		c.put("order_date", "20140506");
		System.out.println(initSQLEqual(c, s, d, "order_id", "order_date", "order_time"));
		System.out.println(s);
		System.out.println(d);
		*/
		// 测试between and
		/*
		Map saleOrderParam = new HashMap<String, Object>();
		saleOrderParam.put("START_DATE", "20140501");
		saleOrderParam.put("END_DATE", "20140601");
		saleOrderParam.put("START_TIME", "060800");
		saleOrderParam.put("END_TIME", "061606");
		List f = new ArrayList();
		StringBuilder s = new StringBuilder("select * from sale_order a where 1=1");
		String sql1 = initSQLBetweenAnd(saleOrderParam, s, f, "a.ORDER_DATE", "START_DATE", "END_DATE");
//		String sql2 = initSQLBetweenAnd(saleOrderParam, s, f, "a.ORDER_TIME", false, false, "START_TIME", "END_TIME");
		System.out.println(s);
		System.out.println(sql1);
		System.out.println(f);
		*/
		
		/*
		Map insertParam = new HashMap<String, Object>();
		insertParam.put("order_id", "20140501");
		insertParam.put("order_date", "20140601");
		List list = new ArrayList();
		StringBuilder sb = new StringBuilder("insert into sale_order");
		String sql = initSQLInsertValues(insertParam, "order_id");
		System.out.println(sb);
		System.out.println(list);
		System.out.println(sql);
		*/
		
		/*
		List<Map<String, Object>> itemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("item_bar", null);
		m1.put("item_id", "itemid1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("item_bar", "itembar2");
		m2.put("item_id", "itemid2");
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO BASE_MERCH_ITEM ");
		List<Object> paramArray = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(m2, sqlBuilder, paramArray, "item_id", "item_bar");
		System.out.println(paramArray);
		System.out.println("-------");
		System.out.println(sqlBuilder.toString());
		*/
/*		
		Map<String, Object> itemParam = new HashMap<String, Object>();
		itemParam.put("item_id", "itemid123");
		itemParam.put("item_bar", "itembar456");
		StringBuilder sqlBuilder = new StringBuilder("UPDATE BASE_MERCH_ITEM SET");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "item_bar", "item_id");
		sqlBuilder.append(" WHERE");
		SQLUtil.initSQLEqual(itemParam, sqlBuilder, paramList, "item_bar", "item_id");
		System.out.println(sqlBuilder.toString());
		System.out.println(paramList);
		*/
		
		/*
		List<Map<String, Object>> unitList = new ArrayList<Map<String,Object>>();
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("item_id", "itemid1");
		m1.put("item_bar", "itembar1");
		
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("item_id", "itemid2");
		m2.put("item_bar", "itembar2");
		unitList.add(m1);
		unitList.add(m2);
		
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO BASE_MERCH_ITEM_UNIT");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> unitMap : unitList) {
			paramArray = new ArrayList<Object>();
			if(index++==0) {
				sqlBuilder.append(SQLUtil.initSQLInsertValues(unitMap, paramArray
						, "item_id", "item_bar"));
			} else {
				SQLUtil.initSQLInsertValues(unitMap, paramArray
						, "item_id", "item_bar");
			}
			paramArrayList.add(paramArray.toArray());
			System.out.println(paramArray);
		}
		System.out.println("--------");
		System.out.println(sqlBuilder.toString());
		*/
		/*
		List<Map<String, Object>> itemList = new ArrayList<Map<String,Object>>();
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("item_id", "itemid1");
		m1.put("item_bar", "itembar1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("item_id", "itemid2");
		m2.put("item_bar", "itembar2");
		Map<String, Object> m3 = new HashMap<String, Object>();
		m3.put("item_id", "itemid3");
		m3.put("item_bar", "itembar3");
		itemList.add(m1);
		itemList.add(m2);
		itemList.add(m3);
		
		StringBuilder sqlBuilder = new StringBuilder("UPDATE BASE_MERCH_ITEM SET");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		List<Object> paramArray = null;
		int index = 0;
		for(Map<String, Object> itemMap : itemList) {
			paramArray = new ArrayList<Object>();
			if(index++==0) {
				sqlBuilder.append(SQLUtil.initSQLEqual(itemMap, paramArray, SQLFlag.COMMA
						, "item_bar", "item_id"));
				sqlBuilder.append(" WHERE");
			} else {
				SQLUtil.initSQLEqual(itemMap, sqlBuilder, paramArray
						, "item_bar", "item_id");
			}
			System.out.println(paramArray);
			paramArrayList.add(paramArray.toArray());
		}
		System.out.println(sqlBuilder.toString());
		*/

		/*
		StringBuilder sql = new StringBuilder("where ");
		List<Object> list = new ArrayList<Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("merch_id", "merchid23");
		SQLUtil.initSQLEqual(paramsMap,sql, list, "A.merch_id","consumer_id:hhh", "status", "GRADE", "is_upgradable");
		
		System.out.println(sql.toString());
		System.out.println(list);
		*/
		
		
	}
	
}

class SQLBody {
	
	StringBuilder sqlBuilder;
	StringBuilder tempBuilder;
	
	SQLBody(StringBuilder sqlBuilder) {
		this.sqlBuilder = sqlBuilder;
		this.tempBuilder = new StringBuilder();
	}
	
	void append(StringBuilder sqlPart) {
		append(sqlPart.toString());
	}
	
	void append(String sqlPart) {
		if(sqlBuilder!=null) {
	    	int lastIndex = sqlBuilder.lastIndexOf("WHERE")==-1 ? sqlBuilder.lastIndexOf("where") : sqlBuilder.lastIndexOf("WHERE");
	    	int sqlLength = sqlBuilder.length();
	    	if(lastIndex!=-1 && lastIndex!=sqlLength-5 && lastIndex!=sqlLength-6) {
	    		tempBuilder.append(" AND");
	    	}
		}
		tempBuilder.append(sqlPart);
	}
	
	void appendToBuilder() {
		sqlBuilder.append(tempBuilder);
	}
	
	String getFullSql() {
		return sqlBuilder==null ? "" : sqlBuilder.toString()+tempBuilder.toString();
	}
	
	String getPartSql() {
		return sqlBuilder==null ? "" : tempBuilder.toString();
	}
	
}

class SQLParam {
	
	private String paramEntry;
	private String tableAlias;
	private String paramName;
	private String defaultValue;
	
	SQLParam(Object param) {
		if(param!=null) {
			paramEntry = param.toString();
			int index = paramEntry.lastIndexOf(":");
			int stringLength = paramEntry.length();
			if(index > -1 && index < stringLength) {
				paramName = paramEntry.substring(0, index);
				defaultValue = paramEntry.substring(index+1);
			}
			if(paramName!=null) {
				index = paramName.indexOf(".");
				stringLength = paramName.length();
				if(index > -1 && index < stringLength) {
					tableAlias = paramName.substring(0, index);
					paramName = paramName.substring(index+1);
				}
			} else {
				index = paramEntry.indexOf(".");
				stringLength = paramEntry.length();
				if(index > -1 && index < stringLength) {
					tableAlias = paramEntry.substring(0, index);
					paramName = paramEntry.substring(index+1);
				}
			}
		}
	}
	
	private int getColonIndex() {
		return paramEntry.lastIndexOf(":");
	}
	
	String getAlias() {
		return this.tableAlias;
	}
	
	String getName() {
		return this.paramName;
	}
	
	String getFullName() {
		if(paramName==null) return "";
		return tableAlias==null ? paramName : tableAlias + "." + paramName;
	}
	
	String getDefault() {
		return this.defaultValue;
	}
	
	public static boolean shouldParse(Object paramKey) {
		if(paramKey!=null) {
			String param = paramKey.toString();
			return param.indexOf(".")+param.lastIndexOf(":") > -2;
		}
		return false;
	}
	
}
