package com.test.CommonLog.http.intecepter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author zhuzhujier
 *
 */
public interface ParametersInterceptor {

	Map<String, Object> getParametersMap(HttpServletRequest request);
}
