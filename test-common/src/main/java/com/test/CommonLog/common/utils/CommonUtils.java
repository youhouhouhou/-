package com.test.CommonLog.common.utils;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

	/**
	 * 判断是不是空 非空返回原串 空返回默认串
	 * 
	 * @param target
	 * @param defaultStr
	 */
	public static String ifBankReturnDefault(String target, String defaultStr) {
		if (StringUtils.isBlank(target)) {
			return defaultStr;
		}
		return target;
	}

}
