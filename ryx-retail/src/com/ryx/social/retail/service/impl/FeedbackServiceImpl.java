package com.ryx.social.retail.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.social.retail.dao.IFeedbackDao;
import com.ryx.social.retail.service.IFeedbackService;

@Service
public class FeedbackServiceImpl implements IFeedbackService {
	private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);
	@Resource
	private IFeedbackDao feedbackDao;
	/**
	 * 提交反馈信息
	 */
	@Override
	public void submitFeedbackInfo(Map<String, Object> paramMap) throws Exception {
		logger.debug("FeedbackServiceImpl submitFeedbackInfo paramMap:" + paramMap);
		feedbackDao.submitFeedbackInfo(paramMap);
	}
}
