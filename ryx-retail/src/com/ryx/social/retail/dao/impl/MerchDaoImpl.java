package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.jdbc.data.Page;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IMerchDao;
import com.ryx.social.retail.util.SQLFlag;
import com.ryx.social.retail.util.SQLUtil;

@Repository("merchDao")
public class MerchDaoImpl extends BaseDaoImpl implements IMerchDao {

	private static final Logger LOG = LoggerFactory.getLogger(MerchDaoImpl.class);
	
	public static final String selectMerchSql = getSelectMerchSql();
	private static String getSelectMerchSql() {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT a.*, open_time||'-'||close_time busi_time FROM BASE_MERCH a WHERE 1=1");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerch(Map<String, Object> merchParam) throws Exception {
		StringBuilder sqlBuilder = new StringBuilder(selectMerchSql);
		List<Object> paramsList = new ArrayList<Object>();
		SQLUtil.initSQLIn(merchParam, sqlBuilder, paramsList, "merch_id");
		SQLUtil.initSQLIn(merchParam, sqlBuilder, paramsList, "lice_id");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramsList.toArray());
	}
	
	@Override
	public Map getMerchInfo(String merchId) throws Exception {
		List<Object> params=new ArrayList<Object>();
		
		StringBuffer sqlBuffer=new StringBuffer(selectMerchSql);
		sqlBuffer.append("  and merch_id=? ");
		params.add(merchId);
		
		List<Map> list = this.selectBySqlQuery(sqlBuffer.toString(),params.toArray());
		
		if(list.size() > 0) return list.get(0);
		else return null;
	}
	
	
	public static final String selectUserSql = getSelectUserSql();
	private static String getSelectUserSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT USER_ID, USER_CODE, USER_NAME, PASSWORD, USER_TYPE, EMAIL, PHONE, REF_ID, IS_LOCKED, LOGIN_FAIL_NUM, IS_MRB,");
		sb.append(" NOTE, LAST_LOGIN_IP, LAST_LOGIN_TIME FROM PUB_USER WHERE USER_CODE = ?");
		return sb.toString();
	}
	@Override
	public Map getUserInfo(String custId) throws Exception {
		
		List<Map> list = this.selectBySqlQuery(selectUserSql, new Object[] {custId});
		
		if(list.size() > 0) return list.get(0);
		else return null;
	}
	
	//修改基本信息   merchAddr:修改经纬度，merchTime：修改营业时间，merchScope:经营范围,merchDelivery：修改送货方式
	@Override
	public void updateMerchBasicInfo(Map<String,Object> merchInfo) throws Exception {
		StringBuffer sql=new StringBuffer();
		sql.append("update BASE_MERCH SET  ");
		String merchId=(String) merchInfo.get("merch_id");
		String busiScope=(String) merchInfo.get("busi_scope");//经营范围
		String lng=(String) merchInfo.get("lng");//经度
		String lat=(String) merchInfo.get("lat");//纬度
		String type=(String) merchInfo.get("type");
		String startTime=(String) merchInfo.get("start_time");//营业开始时间
		String endTime=(String) merchInfo.get("end_time");//营业结束时间
		String deliveryType=(String) merchInfo.get("delivery_type");//送货方式（01：到店自取、02：送货上门
		String orderType = MapUtil.getString(merchInfo, "order_type", null);//自动配合（0：关闭、1启动
		List<Object> params=new ArrayList<Object>();
		sql.append(" merch_id=? ");
		params.add(merchId);
		if(busiScope!=null && type.equals("merchScope")){
			sql.append(" ,busi_scope=? ");
			params.add(busiScope);
		}
		if(lng!=null&& lat!=null && type.equals("merchAddr")){
			sql.append(" ,longitude=? ");
			params.add(lng);
			sql.append(" ,latitude=? ");
			params.add(lat);
		}
		if(startTime!=null && endTime!=null && type.equals("merchTime")){
			sql.append(" , open_time =? ");
			sql.append(" , close_time= ? ");
			sql.append(" , busi_time=? ");
			params.add(startTime);
			params.add(endTime);
			params.add(startTime+"-"+endTime);
		}
		if(deliveryType!=null && type.equals("merchDelivery")){
			sql.append(" , delivery_type= ? ");
			params.add(deliveryType);
		}
		if(orderType!=null && type.equals("order_type")){
			sql.append(" , order_type= ? ");
			params.add(orderType);
		}
		sql.append(" where merch_id=? ");
		params.add(merchId);
		this.executeSQL(sql.toString(), params.toArray());
		
	}
	
	@Override
	public void updateMerchInfo(Map merchInfo) throws Exception {
		List<Object> mer=new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
			sql.append("UPDATE BASE_MERCH SET MERCH_ID=MERCH_ID");
			if(merchInfo.containsKey("merch_name")){
				sql.append(",MERCH_NAME = ?");
				mer.add(merchInfo.get("merch_name"));
			}
			if(merchInfo.containsKey("manager")){
				sql.append(", MANAGER = ?");
				mer.add(merchInfo.get("manager"));
			}
			if(merchInfo.containsKey("amount_per_point")){
				sql.append(", AMOUNT_PER_POINT = ? ");
				mer.add(merchInfo.get("amount_per_point"));
			}
			if(merchInfo.containsKey("telephone")){
				sql.append(", TELEPHONE = ?");
				mer.add(merchInfo.get("telephone"));
			}
			if(merchInfo.containsKey("address")){
				sql.append(", ADDRESS = ?");
				mer.add(merchInfo.get("address"));
			}
			if(merchInfo.containsKey("open_time")){
				sql.append(", open_time = ?");
				mer.add(merchInfo.get("open_time"));
			}
			if(merchInfo.containsKey("close_time")){
				sql.append(" , close_time= ?  ");
				mer.add(merchInfo.get("close_time"));
			}
			if(merchInfo.containsKey("open_time")&&merchInfo.containsKey("close_time")){
				sql.append(" , BUSI_TIME= ?  ");
				StringBuffer busiTime=new StringBuffer();
				busiTime.append(merchInfo.get("open_time"));
				busiTime.append("-");
				busiTime.append(merchInfo.get("close_time"));
				mer.add(busiTime.toString());
			}
			if(merchInfo.containsKey("busi_scope")){
				sql.append(", BUSI_SCOPE = ?");
				mer.add(merchInfo.get("busi_scope"));
			}
			if(merchInfo.containsKey("longitude")){
				sql.append(", LONGITUDE = ?");
				mer.add(merchInfo.get("longitude"));
			}
			if(merchInfo.containsKey("latitude")){
				sql.append(", LATITUDE = ?");
				mer.add(merchInfo.get("latitude"));
			}
			if(merchInfo.containsKey("delivery_type")){
				sql.append(", delivery_type = ?");
				mer.add(merchInfo.get("delivery_type"));
			}
			if(merchInfo.containsKey("file_id")){
				sql.append(" , file_id= ? ");
				mer.add(merchInfo.get("file_id"));
			}
			if(merchInfo.containsKey("is_init")){
				sql.append(" , is_init= ? ");
				mer.add(merchInfo.get("is_init"));
			}
			if(merchInfo.containsKey("init_date")){
				sql.append(" , init_date= ? ");
				mer.add(merchInfo.get("init_date"));
			}
			if(merchInfo.containsKey("init_time")){
				sql.append(" , init_time= ? ");
				mer.add(merchInfo.get("init_time"));
			}
			mer.add(merchInfo.get("merch_id"));
			sql.append(" WHERE MERCH_ID = ?");
			Object[] obj=new Object[mer.size()];
			for(int i=0;i<obj.length;i++){
				obj[i]=mer.get(i);
			}
		this.executeSQL(sql.toString(), mer.toArray());
	}
	//2.搜索附近店铺（根据距离查看）
	public static final String searchShopSql = initSearchShopSql();
	private static String initSearchShopSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT merch_id ShopID, merch_name ShopName,");
		sb.append(" sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2)) distance,");
		sb.append(" telephone ShopTel, open_time||'-'||close_time ShoppingTime,FILE_ID,close_time closeTime,open_time openTime, ADDRESS ShopAddress, 'Y' Songhuo");
		sb.append(" FROM BASE_MERCH WHERE sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2)) <= ?");
		return sb.toString();
	}
	public List<Map<String, Object>> searchMerch(Map<String, Object> merchParam) throws Exception {
		StringBuffer sqlBuffer = new StringBuffer(searchShopSql);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(merchParam.get("lng"));
		paramObject.add(merchParam.get("lat"));
		paramObject.add(merchParam.get("lng"));
		paramObject.add(merchParam.get("lat"));
		paramObject.add(merchParam.get("disitence"));
		sqlBuffer.append(" ORDER BY sqrt(power((to_number(LONGITUDE)-?)*1117000,2)+power((to_number(LATITUDE)-?)*1117000,2))");
		paramObject.add(merchParam.get("lng"));
		paramObject.add(merchParam.get("lat"));

		int pageIndex = Integer.parseInt(merchParam.get("page_index").toString());
		int pageSize = Integer.parseInt(merchParam.get("page_size").toString());
		Page pageResult=this.searchPaginatedBySql(sqlBuffer.toString(), pageIndex, pageSize, paramObject.toArray());
		Integer pageSum=pageResult.getPageSum();
		Integer total=pageResult.getTotal();
		merchParam.put("page_count", pageSum);
		merchParam.put("count", total);
		return pageResult.getRows();
	}
	
	
	@Override
	public void insertMertchTicket(Map<String, Object> merchTicket)
			throws Exception {
		String sql="INSERT INTO MERCH_TICKET("
				+ "MERCH_ID,TICKET_TYPE,WELCOME_WORD,NOTE,PHONE,NUM,TICKET_DATE) "
				+ "VALUES(?,?,?,?,?,?,TO_CHAR(SYSDATE,'yyyyMMdd'))";
		Object[] obj=new Object[6];
		obj[0]=merchTicket.get("merch_id");
		obj[1]=merchTicket.get("ticket_type");
		obj[2]=merchTicket.get("welcome_word");
		obj[3]=merchTicket.get("note");
		obj[4]=merchTicket.get("phone");
		obj[5]=merchTicket.get("num");
		this.executeSQL(sql, obj);
	}
	@Override
	public void updateMertchTicket(Map<String, Object> merchTicket)
			throws Exception {
		String sql="UPDATE  MERCH_TICKET "
				+ "SET WELCOME_WORD=?,NOTE=?,PHONE=?,NUM=?,TICKET_DATE=TO_CHAR(SYSDATE,'yyyyMMdd') "
				+ "WHERE MERCH_ID=? AND TICKET_TYPE=?";
		Object[] obj=new Object[6];
		obj[0]=merchTicket.get("welcome_word");
		obj[1]=merchTicket.get("note");
		obj[2]=merchTicket.get("phone");
		obj[3]=merchTicket.get("num");
		obj[4]=merchTicket.get("merch_id");
		obj[5]=merchTicket.get("ticket_type");
		this.executeSQL(sql, obj);
	}
	@Override
	public List<Map<String, Object>> selectMerchTicket(
			Map<String, Object> merchTicket) throws Exception {
		String sql="SELECT MERCH_ID,TICKET_TYPE,WELCOME_WORD,NOTE,PHONE,NUM,TICKET_DATE FROM MERCH_TICKET WHERE MERCH_ID=? AND TICKET_TYPE=?";
		List<Map<String,Object>> ticketList=this.selectBySqlQuery(sql, new Object[]{merchTicket.get("merch_id"),merchTicket.get("ticket_type")});
		return ticketList;
	}
	
	

	//查询交接班
	public static final String searchDutyShiftSql = initSearchDutyShiftSql();
	private static String initSearchDutyShiftSql() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select MERCH_ID, USER_CODE, AMT_SALE_CARD, AMT_SALE_CASH, AMT_RETURN, LAST_SHIFT_DATE, ");
		sql.append(" LAST_SHIFT_TIME, SHIFT_DATE, SHIFT_TIME ");
		sql.append(" from DUTY_SHIFT ");
		sql.append(" where 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> searchDutyShift(Map<String, Object> paramsMap) throws Exception {
		LOG.debug("searchDutyShift paramsMap:"+paramsMap);
		StringBuilder sql=new StringBuilder(searchDutyShiftSql);
		List<Object> list=new ArrayList<Object>();
		SQLUtil.initSQLEqual(paramsMap,sql, list, "merch_id");
		SQLUtil.initSQLBetweenAnd(paramsMap,sql, list, "shift_date", true,true, "shift_date");
		SQLUtil.initSQLBetweenAnd(paramsMap,sql, list, "shift_time", true, true,"shift_time");
		String startDate = MapUtil.getString(paramsMap, "start_date", null);
		String endDate = MapUtil.getString(paramsMap, "end_date", null);
		String startTime = MapUtil.getString(paramsMap, "start_time", null);
		String endTime = MapUtil.getString(paramsMap, "end_time", null);
		String roleIds =MapUtil.getString(paramsMap, "role_id", "1");
		if (roleIds.equals("3")) {
			String userCode = MapUtil.getString(paramsMap, "user_code");
			sql.append(" AND USER_CODE = ? ");
			list.add(userCode);
		}
		if(!StringUtil.isBlank(startDate)){
			sql.append(" and last_shift_date >= ? ");
			list.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append(" and last_shift_date <= ? ");
			list.add(endDate);
		}
		if(!StringUtil.isBlank(startTime)){
			sql.append(" and last_shift_time >= ? ");
			list.add(startTime);
		}
		if(!StringUtil.isBlank(endTime)){
			sql.append(" and last_shift_time <= ? ");
			list.add(endTime);
		}
		
		SQLUtil.initSQLOrder(sql, "shift_date", "2", "shift_time","2");
		
		int pageIndex =MapUtil.getInt(paramsMap, "page_index",1); 
		int pageSize = MapUtil.getInt(paramsMap, "page_size",20); 
		
		Page pageResult=this.searchPaginatedBySql(sql.toString(), pageIndex, pageSize, list.toArray());
		Integer pageSum=pageResult.getPageSum();
		Integer total=pageResult.getTotal();
		paramsMap.put("page_count", pageSum);
		paramsMap.put("count", total);
		return pageResult.getRows();
	}
	
	
	//新增交接班
	public static final String insertDutyShiftSql = initInsertDutyShiftSql();
	private static String initInsertDutyShiftSql() {
		StringBuffer sql = new StringBuffer();
		sql.append(" insert into DUTY_SHIFT( ");
		sql.append(" MERCH_ID, USER_CODE, AMT_SALE_CARD, AMT_SALE_CASH, AMT_RETURN, ");
		sql.append(" LAST_SHIFT_DATE, LAST_SHIFT_TIME, SHIFT_DATE, SHIFT_TIME ");
		sql.append(" ) ");
		sql.append(" values( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ");
		sql.append("  ");
		return sql.toString();
	}
	@Override
	public void insertDutyShift(Map<String, Object> paramsMap) throws Exception {
		LOG.debug("searchDutyShift paramsMap:"+paramsMap);
		StringBuilder sql=new StringBuilder(insertDutyShiftSql);
		List<Object> list=new ArrayList<Object>();
		
		list.add(MapUtil.get(paramsMap, "merch_id", null));
		list.add(MapUtil.get(paramsMap, "user_code", null));
		list.add(MapUtil.getBigDecimal(paramsMap, "amt_sale_card"));
		list.add(MapUtil.getBigDecimal(paramsMap, "amt_sale_cash"));
		list.add(MapUtil.getBigDecimal(paramsMap, "amt_return"));
		list.add(MapUtil.get(paramsMap, "last_shift_date", null));
		list.add(MapUtil.get(paramsMap, "last_shift_time", null));
		list.add(MapUtil.get(paramsMap, "shift_date", DateUtil.getToday()));
		list.add(MapUtil.get(paramsMap, "shift_time", DateUtil.getCurrentTime().substring(8)));
		
		this.executeSQL(sql.toString(),list.toArray());
	}
	
	@Override
	public void updatePassword(List<String[]> passwordList) throws Exception {
		LOG.debug("MerchDaoImpl updatePassword passwordList: " + passwordList);
		this.executeBatchSQL("update pub_user set password=? where user_code=?", passwordList);
	}
	
	
	
	//查询迁移数据，权限用户
	@Override
	public List<Map<String, Object>> selectTransferMerch(Map<String, Object> paramMap) throws Exception {
		LOG.debug("merchDaoImpl selectTransferMerch paramMap: " + paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		sql.append("SELECT MERCH_ID, FIRST_DATE, FIRST_TIME, LAST_DATE, LAST_TIME ");
		sql.append("FROM TRANSFER_MERCH ");
		sql.append("WHERE MERCH_ID = ? ");
		list.add(merchId);
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//修改迁移数据，权限用户
	@Override
	public void updateTransferMerch(Map<String, Object> paramMap) throws Exception {
		LOG.debug("merchDaoImpl updateTransferMerch paramMap: " + paramMap);
		StringBuilder sql = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		sql.append("UPDATE TRANSFER_MERCH  SET ");
		sql.append(SQLUtil.initSQLEqual(paramMap, list, SQLFlag.COMMA, "first_date", "first_time", "last_date", "last_time"));
		sql.append(" WHERE MERCH_ID = ? ");
		list.add(merchId);
		this.executeSQL(sql.toString(),list.toArray());
	}	
	
}
