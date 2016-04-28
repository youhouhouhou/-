package com.test.CommonLog.http.intecepter.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.test.CommonLog.http.intecepter.UrlIntecepter;

/**
 * 先查match 再從match中找不match的
 * 
 * @author zhuzhujier
 *
 */
public class SimpleUrlIntecepterImpl implements UrlIntecepter {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private String regex = ","; // 以逗號分割

	private Set<String> uriConditionsSet = new ConcurrentHashSet<String>();
	private Set<String> notMatchUriConditionsSet = new ConcurrentHashSet<String>();

	@Override
	public void setURiConditions(String uriConditions) {
		if (StringUtils.isNotBlank(uriConditions)) {
			String[] array = uriConditions.split(regex);
			uriConditionsSet.addAll(Arrays.asList(array));
		}
	}

	@Override
	public void setNotMatchUriConditions(String uriConditions) {
		if (StringUtils.isNotBlank(uriConditions)) {
			String[] array = uriConditions.split(regex);
			notMatchUriConditionsSet.addAll(Arrays.asList(array));
		}
	}

	@Override
	public boolean canPass(String url) {

		return isMatch(url, uriConditionsSet) && !isMatch(url, notMatchUriConditionsSet);
	}

	@Override
	public boolean canPass(String url, String uriConditions) {
		if (StringUtils.isNoneBlank(uriConditions)) {
			String[] array = uriConditions.split(regex);
			Set<String> set = new HashSet<String>(Arrays.asList(array));
			return isMatch(url, set);
		}
		return true;
	}

	@Override
	public boolean canPass(String url, String uriConditions, String notMatchUriConditions) {

		boolean match = false;
		if (StringUtils.isBlank(uriConditions)) {
			return false;
		}
		String[] array = uriConditions.split(regex);
		Set<String> set = new HashSet<String>(Arrays.asList(array));
		match = isMatch(url, set);
		if (StringUtils.isBlank(notMatchUriConditions)) {
			return match;
		}
		array = notMatchUriConditions.split(regex);
		set = new HashSet<String>(Arrays.asList(array));

		return match && !isMatch(url, set);
	}

	/**
	 * 
	 * @param url
	 * @param set
	 * @return
	 */
	public boolean isMatch(String url, Set<String> set) {

		for (String s : set) {
			if (pathMatcher.match(s, url)) {
				return true;
			}
		}
		return false;
	}

}
