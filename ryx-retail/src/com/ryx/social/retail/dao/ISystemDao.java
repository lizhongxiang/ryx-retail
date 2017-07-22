package com.ryx.social.retail.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface ISystemDao {
	
	public void insertTerminalBootupRecord(List<Map<String,Object>> params) throws SQLException;

	public Map<String, Object> selectTerminalBootupRecord(String merchId, String userToken, String bootupDate)
			throws SQLException;

	public void updateTerminalBootupRecord(String merchId, String userToken, String bootupDate, String endDatetime)
			throws SQLException;

	public void insertTerminalBootupRecord(String merchId, String userToken, String bootupDate, String startDatetime, String endDatetime) 
			throws SQLException;
	
}
