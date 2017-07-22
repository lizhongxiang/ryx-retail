package com.ryx.social.retail.dao.impl;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.ISystemDao;

@Repository
public class SystemDaoImpl extends BaseDaoImpl implements ISystemDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(SystemDaoImpl.class);
	
	@Override
	public void insertTerminalBootupRecord(List<Map<String, Object>> params) throws SQLException {
		LOG.debug("SystemDaoImpl insertTerminalBootupRecord params: " + params);
		
		String sql = "insert into terminal_bootup_record(merch_id, user_token, bootup_date, start_datetime, end_datetime) values (?, ?, ?, ?, ?)";
		
		List<Object[]> args = new ArrayList<Object[]>();
		
		for(Map<String, Object> param : params) {
			args.add(new Object[] {
					param.get("merch_id"), 
					param.get("user_token"), 
					param.get("bootup_date"), 
					param.get("start_datetime"), 
					param.get("end_datetime")});
		}
		
		this.executeBatchSQL(sql, args);
	}
	
	@Override
	public Map<String, Object> selectTerminalBootupRecord(String merchId, String userToken, String bootupDate) throws SQLException {
		LOG.debug("SystemDaoImpl selectTerminalBootupRecord merchId: " + merchId + " userToken: " + userToken + " bootupDate: " + bootupDate);
		String sql = "select start_datetime, end_datetime from terminal_bootup_record where merch_id=? and user_token=? and bootup_date=?";
		List<Map<String, Object>> records = this.selectBySqlQuery(sql, new Object[] {merchId, userToken, bootupDate});
		if(records!=null && !records.isEmpty()) {
			return records.get(0);
		} else {
			return new HashMap<String, Object>();
		}
	}
	
	@Override
	public void updateTerminalBootupRecord(String merchId, String userToken, String bootupDate, String endDatetime) throws SQLException {
		LOG.debug("SystemDaoImpl updateTerminalBootupRecord merchId: " + merchId + " userToken: " + userToken + " bootupDate: " + bootupDate + " endDatetime: " + endDatetime);
		String sql = "update terminal_bootup_record set end_datetime=? where merch_id=? and user_token=? and bootup_date=?";
		this.executeSQL(sql, new Object[] {endDatetime, merchId, userToken, bootupDate});
	}
	
	@Override
	public void insertTerminalBootupRecord(String merchId, String userToken, String bootupDate, String startDatetime, String endDatetime) throws SQLException {
		LOG.debug("SystemDaoImpl insertTerminalBootupRecord merchId: " + merchId + " userToken: " + userToken + " bootupDate: " + bootupDate + " startDatetime: " + startDatetime + " endDatetime: " + endDatetime);
		String sql = "insert into terminal_bootup_record(merch_id, user_token, bootup_date, start_datetime, end_datetime) values (?, ?, ?, ?, ?)";
		this.executeSQL(sql, new Object[] {merchId, userToken, bootupDate, startDatetime, endDatetime});
	}
	
}
