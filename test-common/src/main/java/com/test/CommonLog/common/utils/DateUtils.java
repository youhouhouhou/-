package com.test.CommonLog.common.utils;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * 时间工具类
 * 
 * @author zhuzhujier
 *
 */
public class DateUtils {

	/**
	 * yyyyMMdd
	 */
	public static final String PATTERN_SHORT = "yyyyMMdd";
	/**
	 * yyyy-MM-dd
	 */
	public static final String PATTERN_LONG = "yyyy-MM-dd";
	/**
	 * yyyy年MM月dd日
	 */
	public static final String PATTERN_SHORT_CN = "yyyy年MM月dd日";
	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static final String PATTERN_LONGTIME = "yyyy-MM-dd HH:mm:ss";
	/**
	 * yyyy-MM-dd HH:mm:ss.SSS
	 */
	public static final String PATTERN_LONGTIMEPLUS = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * 
	 * @param date
	 * 
	 * @param pattern
	 *            {@link PATTERN_SHORT}
	 * @return
	 */
	public static String format(Date date, String pattern) {

		return FastDateFormat.getInstance(pattern).format(date);
	}
}
