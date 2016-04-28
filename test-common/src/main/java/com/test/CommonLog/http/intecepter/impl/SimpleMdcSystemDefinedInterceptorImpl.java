package com.test.CommonLog.http.intecepter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

import com.test.CommonLog.common.Constants;
import com.test.CommonLog.common.utils.CommonUtils;
import com.test.CommonLog.common.utils.IpUtils;
import com.test.CommonLog.common.utils.UUIDUTils;
import com.test.CommonLog.http.intecepter.MdcSystemDefinedInterceptor;

public class SimpleMdcSystemDefinedInterceptorImpl implements MdcSystemDefinedInterceptor {

	private String systemId;

	@Override
	public void setSystemDefinedBeforeDoFilter(HttpServletRequest request, HttpServletResponse response) {
		copyInfoFromRequest2MDC(request);
	}

	@Override
	public void setSystemDefinedAfterDoFilter(HttpServletRequest request, HttpServletResponse response) {
		MDC.clear();
	}

	/**
	 * 
	 * @param rpcContext
	 * @param b
	 */
	private void copyInfoFromRequest2MDC(HttpServletRequest req) {
		// 流水号
		MDC.put(Constants.OPERATEID,
				CommonUtils.ifBankReturnDefault(req.getHeader(Constants.OPERATEID), UUIDUTils.getUUID()));
		// 客户标示
		MDC.put(Constants.TOKENID, CommonUtils.ifBankReturnDefault(req.getHeader(Constants.TOKENID), "X"));
		// Ip
		MDC.put(Constants.IP, CommonUtils.ifBankReturnDefault(IpUtils.getRealIpWithStaticCache(), "X"));
		// 上游IP
		MDC.put(Constants.UPIP, CommonUtils.ifBankReturnDefault(IpUtils.getHttpClientRemoteIp(req), "X"));
		// 系统标示
		MDC.put(Constants.SYSTEMID, CommonUtils.ifBankReturnDefault(systemId, "X"));
		// 上游系统
		MDC.put(Constants.UPSYSTEMID, CommonUtils.ifBankReturnDefault(req.getHeader(Constants.SYSTEMID), "X"));

		MDC.put(Constants.CHAIN, CommonUtils.ifBankReturnDefault(req.getHeader(Constants.CHAIN), "0"));
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
