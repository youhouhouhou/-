package com.test.CommonLog.http.intecepter;

/**
 * 
 * @author zhuzhujier
 *
 */
public interface UrlIntecepter {

	/**
	 * 要攔截處理的 以，分隔
	 * 
	 * @param uriConditions
	 */
	void setURiConditions(String uriConditions);

	/**
	 * 不需要攔截處理的 以，分隔
	 * 
	 * @param uriConditions
	 */
	void setNotMatchUriConditions(String uriConditions);

	/**
	 * 判斷是否處理 直接傳來要攔截的串 以，分隔
	 * 
	 * @param url
	 * @return
	 */
	boolean canPass(String url, String uriConditions);

	/**
	 * 判斷是否處理 直接傳來要攔截的串 和不攔截的串 以，分隔
	 * 
	 * @param url
	 * @return
	 */
	boolean canPass(String url, String uriConditions, String notMatchUriConditions);

	/**
	 * 判斷是否處理
	 * 
	 * @param url
	 * @return
	 */
	boolean canPass(String url);

}
