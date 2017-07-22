package com.ryx.social.consumer.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.social.consumer.dao.IConsumerWhseDao;
import com.ryx.social.consumer.dao.impl.ConsumerWhseDaoImpl;
import com.ryx.social.consumer.service.IConsumerWhseService; 

@Service
public class ConsumersWhseServiceImpl implements IConsumerWhseService {
	private static final Logger logger=LoggerFactory.getLogger(ConsumerWhseDaoImpl.class);
	@Resource
	private IConsumerWhseDao consumerWhseDao;
	/**
	 * 更新whse_merch表, 支持批量
	 */
	@Override
	public void modifyConsumerWhseMerch(Map<String, Object> paramMap)throws Exception {
		logger.debug("WhseServiceImpl modifyWhseMerch paramMap: "+ paramMap);
		consumerWhseDao.updateWhseMerch(paramMap);
	}
}
