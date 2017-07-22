package com.ryx.social.retail.dao.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IFeedbackDao;

@Repository
public class FeedbackDaoImpl extends BaseDaoImpl implements IFeedbackDao {
	private static final Logger logger = LoggerFactory.getLogger(FeedbackDaoImpl.class);
	/**
	 * 提交反馈信息
	 */
	private static final String selectFeedbackSql = getSelectFeedbackSql();
	private static String getSelectFeedbackSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO FEEDBACK_INFO (INFO_ID, INFO_TITLE, INFO_CONTEXT, INFO_DATE, INFO_TIME, REFERENCE,");
		sb.append(" USER_CODE, USER_NAME, NAME, CONTACT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		return sb.toString();
	}
	@Override
	public void submitFeedbackInfo(Map<String, Object> feedbackParam) throws Exception {
		logger.debug("FeedbackDaoImpl submitFeedbackInfo feedbackParam: " + feedbackParam);
		String infoId = (String) feedbackParam.get("info_id");
		String infoTitle = (String) feedbackParam.get("info_title");
		String infoContext = (String) feedbackParam.get("info_context");
		String infoDate = (String) feedbackParam.get("info_date");
		String infoTime = (String) feedbackParam.get("info_time");
		String reference = (String) feedbackParam.get("reference");
		String userCode = (String) feedbackParam.get("user_code");
		String userName = (String) feedbackParam.get("user_name");
		String name = (String) feedbackParam.get("name");
		String contact = (String) feedbackParam.get("contact");
		this.executeSQL(selectFeedbackSql, 
				new Object[] {infoId, infoTitle, infoContext, infoDate, infoTime, reference, userCode, userName, name, contact});
	}
}
