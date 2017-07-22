package com.ryx.social.retail.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.social.retail.dao.ISystemDao;
import com.ryx.social.retail.service.ISystemService;
import com.ryx.social.retail.util.DateUtil;

@Service
public class SystemServiceImpl implements ISystemService {
	
	private static final Logger LOG = LoggerFactory.getLogger(SystemServiceImpl.class);
	
	@Resource
	private ISystemDao systemDaoImpl;
	
	@Override
	public void submitTerminalBootupDuration(Map<String, Object> param) throws SQLException {
		LOG.debug("SystemServiceImpl submitTerminalBootupDuration param: " + param);
		String terminalCurrentDateTimeString = MapUtil.getString(param, "current_time");
		long difference = 0;
		// 计算终端和服务器的时间差 用于矫正终端开关机的时间
		if(StringUtils.hasText(terminalCurrentDateTimeString)) {
			Date terminalCurrentDateTime = DateUtil.getCompactDateTime(terminalCurrentDateTimeString);
			if(terminalCurrentDateTime!=null) {
				difference = DateUtil.minus(terminalCurrentDateTime, true);
			}
		}
		// 终端传来的开关机时间列表
		List<Map<String, Object>> onlines = MapUtil.get(param, "onlines", Collections.EMPTY_LIST);
		if(onlines!=null && !onlines.isEmpty()) {
			// 保存到数据表的开关机记录
			List<Map<String, Object>> onlineParams = new ArrayList<Map<String, Object>>();
			Map<String, Object> onlineParam;
			String merchId = MapUtil.getString(param, "merch_id");
			for(Map<String, Object> online : onlines) {
				// 开机时间
				Date terminalStartDateTime = DateUtil.getCompactDateTime(MapUtil.getString(online, "start"));
				// 关机时间
				Date terminalEndDateTime = DateUtil.getCompactDateTime(MapUtil.getString(online, "end"));
				// 如果关机时间比开机时间晚则使用 否则丢弃
				if(terminalStartDateTime!=null && terminalEndDateTime!=null && DateUtil.isLaterThan(terminalEndDateTime, terminalStartDateTime)) {
					// 矫正终端开关机时间
					terminalStartDateTime = DateUtil.plus(terminalStartDateTime, difference);
					terminalEndDateTime = DateUtil.plus(terminalEndDateTime, difference);
					// 终端开关机日期 和标记日期:当开机和关机日期跨天时用来将一条上传记录分割多条记录保存
					Date terminalStartDate = DateUtil.getDay(terminalStartDateTime);
					Date terminalMiddleDate = DateUtil.getDay(terminalStartDateTime);
					Date terminalEndDate = DateUtil.getDay(terminalEndDateTime);
					while(DateUtil.isNotLaterThan(terminalMiddleDate, terminalEndDate)) {
						onlineParam = new HashMap<String, Object>();
						onlineParam.put("merch_id", merchId);
						// 终端上传开关机时间时的token取随机值
						onlineParam.put("user_token", IDUtil.getId());
						onlineParam.put("bootup_date", DateUtil.getCompactDateString(terminalMiddleDate));
						// 标记日期与开始和结束日期都相等 说明不需要分割
						if(terminalMiddleDate.equals(terminalStartDate) && terminalMiddleDate.equals(terminalEndDate)) {
							onlineParam.put("start_datetime", DateUtil.getCompactDateTimeString(terminalStartDateTime));
							onlineParam.put("end_datetime", DateUtil.getCompactDateTimeString(terminalEndDateTime));
							terminalMiddleDate = DateUtil.getTomorrow(terminalMiddleDate); // 相当于break
						} 
						// 标记日期只与开机日期相同 说明需要分割 且是第一个分片 保存开机时间和标记日期的23:59:59 并且标记日期+1
						else if(terminalMiddleDate.equals(terminalStartDate)) {
							onlineParam.put("start_datetime", DateUtil.getCompactDateTimeString(terminalStartDateTime));
							terminalMiddleDate = DateUtil.getTomorrow(terminalMiddleDate);
							onlineParam.put("end_datetime", DateUtil.getCompactDateTimeString(DateUtil.plus(terminalMiddleDate, -1)));
						}
						// 标记日期只与开机日期相同 说明这是最后一个分片 保存标记日期的00:00:00和关机时间
						else if(terminalMiddleDate.equals(terminalEndDate)) {
							onlineParam.put("start_datetime", DateUtil.getCompactDateTimeString(terminalMiddleDate));
							onlineParam.put("end_datetime", DateUtil.getCompactDateTimeString(terminalEndDateTime));
							terminalMiddleDate = DateUtil.getTomorrow(terminalMiddleDate); // 相当于break
						}
						// 标记日期既不与开机时间相同也不与关机时间相同 说明这是中间分片 保存标记日期的00:00:00和标记日期的23:59:59并且标记日期+1
						else {
							onlineParam.put("start_datetime", DateUtil.getCompactDateTimeString(terminalMiddleDate));
							terminalMiddleDate = DateUtil.getTomorrow(terminalMiddleDate);
							onlineParam.put("end_datetime", DateUtil.getCompactDateTimeString(DateUtil.plus(terminalMiddleDate, -1)));
						}
						onlineParams.add(onlineParam);
					}
				}
			}
			// 将终端上传的每个开关机时间(分割后的)保存到数据表
			systemDaoImpl.insertTerminalBootupRecord(onlineParams);
		}
	}
	
	@Override
	public void submitTerminalSignupTime(Map<String, Object> param) throws SQLException {
		LOG.debug("SystemServiceImpl submitTerminalSignupTime param: " + param);
		String merchId = MapUtil.getString(param, "merch_id");
		String userToken = MapUtil.getString(param, "token");
		String bootupDate = DateUtil.getCompactDateString(DateUtil.getToday());
		Map<String, Object> record = systemDaoImpl.selectTerminalBootupRecord(merchId, userToken, bootupDate);
		Date now = DateUtil.getNow(true);
		// 如果根据商户编号 token 开机日期 能查到记录 则更新这条记录的结束时间 否则新生成一条记录
		if(record.isEmpty()) {
			systemDaoImpl.insertTerminalBootupRecord(merchId, userToken, bootupDate, DateUtil.getCompactDateTimeString(now), DateUtil.getCompactDateTimeString(now));
		} else {
			systemDaoImpl.updateTerminalBootupRecord(merchId, userToken, bootupDate, DateUtil.getCompactDateTimeString(now));
		}
	} 
	
}
