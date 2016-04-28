package com.test.CommonLog.common.utils;

public class UserTokenUtils {
	private static ThreadLocal<String> holder = new ThreadLocal<String>();

	/**
	 * 设置tokenId
	 * 
	 * @param tokenId
	 */
	public static void setToken(String tokenId) {
		holder.set(tokenId);
	}

	/**
	 * 获取token
	 * 
	 * @return
	 */
	public static String getToken() {
		return holder.get();
	}

	/**
	 * 清除
	 */
	public static void clear() {
		holder.remove();
	}

}
