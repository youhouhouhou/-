package com.test.CommonLog.http.intecepter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MdcSystemDefinedInterceptor {

	void setSystemDefinedBeforeDoFilter(HttpServletRequest request, HttpServletResponse response);

	void setSystemDefinedAfterDoFilter(HttpServletRequest request, HttpServletResponse response);
}
