package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.IInfointerDao;

@Repository("infointerDao")
public class InfointerDaoImpl extends BaseDaoImpl implements IInfointerDao {
	
	private Logger LOG = LoggerFactory.getLogger(InfointerDaoImpl.class);

	/**
	 * 查询公告表信息
	 */
	public static final String selectNoticeSql = initSelectNoticeSql();
	private static String initSelectNoticeSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NOTICE.NOTICE_ID, NOTICE.NOTICE_TITLE, NOTICE.NOTICE_DESC, NOTICE.NOTICE_CONTEXT, ");
		sb.append(" NOTICE.NOTICE_TYPE, NOTICE.START_DATE, NOTICE.END_DATE, NOTICE.START_TIME, NOTICE.NOTE,");
		sb.append(" NOTICE.END_TIME, NOTICE.CRT_USER, NOTICE.CRT_DATE, NOTICE.CRT_TIME, NOTICE.STATUS,");
		sb.append(" READ_FLAG.NOTICE_ID MERCH_ID");
		sb.append(" FROM NOTICE LEFT JOIN (SELECT DISTINCT NOTICE_ID FROM NOTICE_READ WHERE MERCH_ID = ?) READ_FLAG ON NOTICE.NOTICE_ID = READ_FLAG.NOTICE_ID  ");
		sb.append(" ORDER BY NOTICE.CRT_DATE");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> getNoticeList(Map<String, String> map)throws Exception {
		LOG.debug("getNoticeList map:"+map);
		
		StringBuffer sqlBuffer = new StringBuffer(selectNoticeSql);
		
		// 分页
		Integer pageIndex = MapUtil.getInt(map, "page_index",1);
		
		Integer pageSize = MapUtil.getInt(map, "page_size" ,20 );
		
		// 查询
		List<Object> paramArray = new ArrayList<Object>();
		String userId = map.get("userId");
		//String comId = map.get("comId");
		//String date1 = map.get("date1");
		
		paramArray.add(userId);
		//paramArray.add(comId);
		//paramArray.add(date1);
		
		Page pageResult = this.searchPaginatedBySql(sqlBuffer.toString(), pageIndex, pageSize, paramArray.toArray());
		Integer pageSum = pageResult.getPageSum();
		Integer total = pageResult.getTotal();
		map.put("page_count", pageSum.toString());
		map.put("count", total.toString());
		return pageResult.getRows();
		
		//return this.selectBySqlQuery(sqlBuffer.toString());
	}
	
	public static final String selectNoticeDetailSql = initSelectNoticeDetailSql();
	private static String initSelectNoticeDetailSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO NOTICE_READ(NOTICE_ID,MERCH_ID,READ_DATE,READ_TIME,NOTE) VALUES(?,?,?,?,?)");
		return sb.toString();
	}
	
	//更新阅读消息状态
	public void updateNoticeDetail(List<String> list) throws Exception {

		this.executeSQL(selectNoticeDetailSql, list.toArray());
	}

}
