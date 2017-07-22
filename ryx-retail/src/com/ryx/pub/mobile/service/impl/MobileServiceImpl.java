package com.ryx.pub.mobile.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ryx.pub.mobile.dao.MobileDao;
import com.ryx.pub.mobile.service.IMobileService;

@Service
public class MobileServiceImpl implements IMobileService {

	@Resource
	private MobileDao mobileDao;
	@Override
	public Map<String, Object> getMobileInfo(String mobilePhone)
			throws Exception {
		//初始化数据
		Map<String, Object> mobileInfo=new HashMap<String,Object>();
		mobileInfo.put("FLAG", "0");
		mobileInfo.put("MOBILENUMBER", "");
		mobileInfo.put("MOBILEAREA", "");
		mobileInfo.put("MOBILETYPE", "");
		mobileInfo.put("AREATYPE", "");
		mobileInfo.put("POSTTYPE", "");
		mobileInfo.put("LIST", new ArrayList<String>());
		
		if(mobilePhone!=null&&mobilePhone.length()>=7){
			String mobileNumber=mobilePhone.substring(0,7);
			List<Map<String, Object>> mobileInfoList=mobileDao.selectMobileInfoByMobileArea(mobileNumber);
			if(mobileInfoList!=null&&mobileInfoList.size()>0){
				mobileInfo.putAll(mobileInfoList.get(0));
				mobileInfo.put("FLAG", "1");
				List moneyList=getMobileMoneyList(mobileInfo);
				mobileInfo.put("LIST", moneyList);
			}
		}
		mobileInfo.put("MOBILENUMBER", mobilePhone);
		
		return mobileInfo;
	}
	/**
	 * 查询指定地区及电话类型的可充值金额
	 * @author 徐虎彬
	 * @date 2014年3月27日
	 * @param mobileInfo
	 * @return
	 * @throws Exception
	 */
	public List<String> getMobileMoneyList(Map<String, Object> mobileInfo)throws Exception{
		
		Map<String, Object> mobileAreaAndType=new HashMap<String,Object>();
		if(mobileInfo.containsKey("MOBILETYPE")){
			if(mobileInfo.get("MOBILETYPE")!=null){
				String type=mobileInfo.get("MOBILETYPE").toString();
				
				if(type.indexOf("移动")>-1){
					type="移动";
				}
				if(type.indexOf("联通")>-1){
					type="联通";				
				}
				if(type.indexOf("电信")>-1){
					type="电信";
				}
				mobileAreaAndType.put("mobiletype", type);
			}
			if(mobileInfo.get("MOBILEAREA")!=null){
				String area=mobileInfo.get("MOBILEAREA").toString();
				area=area.substring(0,area.indexOf(" "));
				mobileAreaAndType.put("provinces", area);
			}
		}
		List<Map<String, Object>> mobileMoneyList=mobileDao.selectMobileMoneyByProvincesAndMobileType(mobileAreaAndType);
		List<String> momeyList=new ArrayList<String>();
		if(mobileMoneyList!=null){
			for(Map<String, Object> map:mobileMoneyList){
				momeyList.add(map.get("MONEY").toString());
			}
		}
		
		return momeyList;
	}

}
