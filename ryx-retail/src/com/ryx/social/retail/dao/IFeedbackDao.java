package com.ryx.social.retail.dao;

import java.util.Map;

public interface IFeedbackDao {
	public void submitFeedbackInfo(Map<String, Object> feedbackParam) throws Exception;
}
