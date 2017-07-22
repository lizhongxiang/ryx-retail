package com.ryx.social.retail.service;

import java.sql.SQLException;
import java.util.Map;

public interface ISystemService {
	
	public void submitTerminalBootupDuration(Map<String, Object> param) throws SQLException;

	public void submitTerminalSignupTime(Map<String, Object> param) throws SQLException;
	
}
