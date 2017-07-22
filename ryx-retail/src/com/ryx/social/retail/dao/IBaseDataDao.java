package com.ryx.social.retail.dao;

import java.util.List;
import java.util.Map;

public interface IBaseDataDao {
	public List<Map<String, Object>> selectPubUser(Map<String, Object> userParam) throws Exception;

	public List<Map<String, Object>> selectBaseMerch(Map<String, Object> merchParam) throws Exception;
}
