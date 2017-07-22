package com.ryx.social.retail.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
/**
 * 通过字符串获得全拼和简拼
 * @author 徐虎彬
 * @date 2014年3月10日
 */
public class DataUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DataUtil.class);
	/**
	 * 得到 全拼
	 * 
	 * @param src
	 * @return
	 */
	public static List<String> getPinYin(String src) {
		char[] t1 = null;
		t1 = src.toCharArray();
		String[] t2 = new String[t1.length];
		HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
		t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		t3.setVCharType(HanyuPinyinVCharType.WITH_V);
//		String t4 = "";
		int t0 = t1.length;
		List<String> list=new ArrayList();
		try {
			for (int i = 0; i < t0; i++) {
				// 判断是否为汉字字符
				if (java.lang.Character.toString(t1[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
					list.add(t2[0]);
				} else {
					list.add(java.lang.Character.toString(t1[i]));
				}
			}
//			System.out.println("list:"+list.toString());
			return list;
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
//			e1.printStackTrace();
			LOG.error("getPinYin错误",e1);
		}
//		System.out.println("list:"+list.toString());
		return list;
	}

	/**
	 * 得到中文首字母
	 * 
	 * @param str
	 * @return
	 */
	public static String getPinYinHeadChar(String str) {
		StringBuilder convert = new StringBuilder();
		convert.append("");
		for (int j = 0; j < str.length(); j++) {
			char word = str.charAt(j);
			String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
			if (pinyinArray != null) {
//				convert += pinyinArray[0].charAt(0);
				convert.append(pinyinArray[0].charAt(0));
			} else {
				convert.append(word);
//				convert += word;
			}
		}
		return convert.toString();
	}

	/**
	 * 将字符串转移为ASCII码
	 * 
	 * @param cnStr
	 * @return
	 */
	public static String getCnASCII(String cnStr) {
		StringBuffer strBuf = new StringBuffer();
		byte[] bGBK = cnStr.getBytes();
		for (int i = 0; i < bGBK.length; i++) {
			// System.out.println(Integer.toHexString(bGBK[i]&0xff));
			strBuf.append(Integer.toHexString(bGBK[i] & 0xff));
		}
		return strBuf.toString();
	}
	
	public static Map getAllAndHead(String str){
		List<String> list=getPinYin(str);
		Map<String,String> map=new HashMap<String,String>();
		if(list!=null&&list.size()>0){
			StringBuffer sb=new StringBuffer();
			StringBuffer sbHead=new StringBuffer();
			for(String s: list){
				if(s.toLowerCase().equals("zhang")){
					s="chang";
				}
				sb.append(s);
//				System.out.println(s.length());
				sbHead.append(s.substring(0,1));
			}
			map.put("ALL", sb.toString().toUpperCase());
			map.put("HEAD", sbHead.toString().toUpperCase());
		}
		return map;
	}
}
