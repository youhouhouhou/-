package com.test.CommonLog.http.intecepter.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.test.CommonLog.http.intecepter.ParametersInterceptor;

public class SimpleParametersInterceptorImpl implements ParametersInterceptor {

	@Override
	public Map<String, Object> getParametersMap(HttpServletRequest request) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("method", request.getMethod());
		parameterMap.put("contentType", request.getContentType());
		parameterMap.put("contentLength", request.getContentLength());
		parameterMap.put("queryString",
				request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
		List<String[]> headerList = new ArrayList<String[]>();
		Enumeration<String> e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			Enumeration<String> n = request.getHeaders(key);
			while (n.hasMoreElements()) {
				String[] header = new String[2];
				header[0] = key;
				header[1] = n.nextElement();
				headerList.add(header);
			}
		}
		parameterMap.put("header", headerList);
		parameterMap.put("parameter", request.getParameterMap());
		return parameterMap;
	}

}
