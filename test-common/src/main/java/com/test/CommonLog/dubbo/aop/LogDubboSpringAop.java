package com.test.CommonLog.dubbo.aop;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.test.CommonLog.common.Constants;
import com.test.CommonLog.common.utils.CommonUtils;
import com.test.CommonLog.common.utils.DateUtils;
import com.test.CommonLog.common.utils.IpUtils;
import com.test.CommonLog.common.utils.UUIDUTils;
import com.test.CommonLog.common.utils.UserTokenUtils;

/**
 * dubbo方法记录日志
 *
 */
public class LogDubboSpringAop {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogDubboSpringAop.class);

	/**
	 * 系统id 配置
	 */
	private String systemId = "noneSet";

	public Object aroundDubbo(ProceedingJoinPoint joinPoint) throws Exception {

		// url参数，用于区分接口
		String url = "";
		// 结果
		Object result = null;
		Object[] args = joinPoint.getArgs();
		RpcContext rpcContext = RpcContext.getContext();
		url = joinPoint.getSignature().toString();

		// 记录开始时间
		Date start = new Date();
		String startStr = DateUtils.format(start, DateUtils.PATTERN_LONGTIMEPLUS);
		try {
			boolean isProvider = isProviderSide(rpcContext, joinPoint.getSignature());

			boolean isCreateMDC = false;
			try {
				// 非提供者 已經有MDC即是 web調用dubbo 集成web的MDC变量继续传递
				if (!isProvider && StringUtils.isNotBlank(MDC.get(Constants.OPERATEID))) {

					LOGGER.info(
							"*==LogDubboSpringAop开始==,此次调用为web客户端或(DUBBO A->B->C(B->C))调用*,flag:[start],hasEx:[false],url:["
									+ url + "],开始时间为：" + startStr);

					// 从MDC复制信息到RpcContext
					copyInfoFromMDC2RpcContext(rpcContext, 0);
				}

				// 非提供者 没有有MDC即是 dubbo刚开始被调用 应该生成MDC
				if (!isProvider && StringUtils.isBlank(MDC.get(Constants.OPERATEID))) {
					// 生成MDC
					createMdcInfo();
					isCreateMDC = true;
					LOGGER.info("*==LogDubboSpringAop开始==,此次调用为dubbo客户端调用*,flag:[start],hasEx:[false],url:[" + url
							+ "],开始时间为：" + startStr);
					// 从MDC复制信息到RpcContext
					copyInfoFromMDC2RpcContext(rpcContext, 0);
				}

				// 是服务端，并且可以接收客户端的参数，生成并传递 MDC
				if (isProvider && StringUtils.isNotBlank(rpcContext.getAttachment(Constants.OPERATEID))) {
					// 从RpcContext复制信息到MDC
					copyInfoFromRpcContext2MDC(rpcContext, 1);

					LOGGER.info("==LogDubboSpringAop开始==,此次调用为服务端传递调用,flag:[start],hasEx:[false],url:[" + url
							+ "],开始时间为：" + startStr);
				}

				// 是服务端，并且可以接收客户端的参数，生成并传递 MDC
				if (isProvider && StringUtils.isBlank(rpcContext.getAttachment(Constants.OPERATEID))) {
					// 生成MDC
					createMdcInfo();
					isCreateMDC = true;
					LOGGER.info("==LogDubboSpringAop开始==,此次调用为服务端非传递调用,flag:[start],hasEx:[false],url:[" + url
							+ "],开始时间为：" + startStr);
					// 从MDC复制信息到RpcContext
					copyInfoFromMDC2RpcContext(rpcContext, 0);
				}

			} catch (Exception e) {
				LOGGER.error("dubbo前置处理异常", e);
			}

			boolean hasException = false;
			try {
				LOGGER.info("调用参数为：" + JSON.toJSONString(args));
				result = joinPoint.proceed(args);
				LOGGER.info("本次调用正常结果为:{}", JSON.toJSON(result));
				if (isProvider) {
					// TODO 服务端统计
				} else {
					// TODO 客户端统计
				}
			} catch (Throwable e) {
				hasException = true;
				LOGGER.info("本次调用异常异常消息为:{}", e.getMessage());
				if (isProvider) {
					// TODO 服务端统计
				} else {
					// TODO 客户端统计
				}
				LOGGER.info("本次调用异常", e);
			} finally {
				Date end = new Date();
				String endStr = DateUtils.format(end, DateUtils.PATTERN_LONGTIMEPLUS);
				long usedTime = end.getTime() - start.getTime();
				if (isProvider) {
					if (hasException) {
						LOGGER.info("*==LogDubboSpringAop服务端发生异常并结束==*,flag:[end],hasEx:[true],url:[" + url + "],结束时间为："
								+ endStr + ",用时 " + usedTime + "ms");
					} else {
						LOGGER.info("*==LogDubboSpringAop正常结束==*,flag:[end],hasEx:[false],url:[" + url + "],结束时间为："
								+ endStr + ",用时 " + usedTime + "ms");
					}
				} else {
					if (hasException) {
						LOGGER.info("*==LogDubboSpringAop发生异常并结束==*,flag:[end],hasEx:[true],url:[" + url + "],结束时间为："
								+ endStr + ",用时 " + usedTime + "ms");
					} else {
						LOGGER.info("*==LogDubboSpringAop正常结束==*,flag:[end],hasEx:[false],url:[" + url + "],结束时间为："
								+ endStr + ",用时 " + usedTime + "ms");
					}
				}

				// 生成MDC了的話 要清除掉
				if (isCreateMDC) {
					MDC.clear();
				}
			}

		} catch (Exception e) {
			LOGGER.error("LogDubboSpringAop异常", e);
			throw e;
		}

		return result;
	}

	/**
	 * 判斷是不是服務提供者
	 * 
	 * @param rpcContext
	 * @param signature
	 * @return
	 */
	private boolean isProviderSide(RpcContext rpcContext, Signature signature) {
		boolean ret = rpcContext.isProviderSide();
		if (ret && rpcContext.getUrl() != null) {
			if (!rpcContext.getUrl().getServiceInterface().equals(signature.getDeclaringTypeName())) {
				ret = false;
			}
		}
		return ret;
	}

	/**
	 * 生成MDC信息
	 */
	private void createMdcInfo() {
		MDC.put(Constants.OPERATEID, UUIDUTils.getUUID());
		MDC.put(Constants.TOKENID, CommonUtils.ifBankReturnDefault(UserTokenUtils.getToken(), "X"));
		MDC.put(Constants.IP, IpUtils.getRealIpWithStaticCache());
		MDC.put(Constants.UPIP, "X");
		MDC.put(Constants.SYSTEMID, systemId);
		MDC.put(Constants.UPSYSTEMID, "X");
		MDC.put(Constants.CHAIN, "0");
	}

	/**
	 * 从MDC复制信息到RpcContext
	 */
	private void copyInfoFromMDC2RpcContext(RpcContext rpcContext, int chainadd) {
		// 流水号
		rpcContext.setAttachment(Constants.OPERATEID,
				CommonUtils.ifBankReturnDefault(MDC.get(Constants.OPERATEID), "X"));
		// 客户标示
		rpcContext.setAttachment(Constants.TOKENID, CommonUtils.ifBankReturnDefault(MDC.get(Constants.TOKENID), "X"));
		// Ip
		rpcContext.setAttachment(Constants.IP, CommonUtils.ifBankReturnDefault(MDC.get(Constants.IP), "X"));
		// 上游IP
		rpcContext.setAttachment(Constants.UPIP, CommonUtils.ifBankReturnDefault(MDC.get(Constants.UPIP), "X"));
		// 系统标示
		rpcContext.setAttachment(Constants.SYSTEMID, CommonUtils.ifBankReturnDefault(MDC.get(Constants.SYSTEMID), "X"));
		// 上游系统
		rpcContext.setAttachment(Constants.UPSYSTEMID,
				CommonUtils.ifBankReturnDefault(MDC.get(Constants.UPSYSTEMID), "X"));

		rpcContext.setAttachment(Constants.CHAIN,
				(Integer.valueOf(CommonUtils.ifBankReturnDefault(MDC.get(Constants.CHAIN), -chainadd + "")) + chainadd)
						+ "");
	}

	/**
	 * 
	 * @param rpcContext
	 * @param b
	 */
	private void copyInfoFromRpcContext2MDC(RpcContext rc, int chainadd) {
		// 流水号
		MDC.put(Constants.OPERATEID, CommonUtils.ifBankReturnDefault(rc.getAttachment(Constants.OPERATEID), "X"));
		// 客户标示
		MDC.put(Constants.TOKENID, CommonUtils.ifBankReturnDefault(rc.getAttachment(Constants.TOKENID), "X"));
		// Ip
		MDC.put(Constants.IP, CommonUtils.ifBankReturnDefault(IpUtils.getRealIpWithStaticCache(), "X"));
		// 上游IP
		MDC.put(Constants.UPIP, CommonUtils.ifBankReturnDefault(rc.getAttachment(Constants.IP), "X"));
		rc.getAttachments().put(Constants.IP, MDC.get(Constants.IP));
		// 系统标示
		MDC.put(Constants.SYSTEMID, CommonUtils.ifBankReturnDefault(systemId, "X"));
		// 上游系统
		MDC.put(Constants.UPSYSTEMID, CommonUtils.ifBankReturnDefault(rc.getAttachment(Constants.SYSTEMID), "X"));
		rc.getAttachments().put(Constants.SYSTEMID, MDC.get(Constants.SYSTEMID));
		MDC.put(Constants.CHAIN,
				(Integer.valueOf(CommonUtils.ifBankReturnDefault(rc.getAttachment(Constants.CHAIN), -chainadd + ""))
						+ chainadd) + "");
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
