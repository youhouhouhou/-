package com.test.CommonLog.http.filter;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.test.CommonLog.common.utils.DateUtils;
import com.test.CommonLog.http.intecepter.MdcSystemDefinedInterceptor;
import com.test.CommonLog.http.intecepter.ParametersInterceptor;
import com.test.CommonLog.http.intecepter.UrlIntecepter;

/**
 * HTTPFilter
 * 
 * @author zhuzhujier
 *
 */
public class HttpFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpFilter.class);

	public static final String URICONDITIONS = "uriConditions";
	public static final String URINOTMATCHCONDITIONS = "uriNotMatchConditions";

	@Resource
	private UrlIntecepter urlIntecepter;

	@Resource
	private ParametersInterceptor parametersInterceptor;

	@Resource
	private MdcSystemDefinedInterceptor mdcSystemDefinedInterceptor;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			throw new ServletException("SessionUserFilter only supports HTTP requests");
		}

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = req.getRequestURI();
		if (req.getCharacterEncoding() == null || req.getCharacterEncoding().equalsIgnoreCase("ISO-8859-1")) {
			req.setCharacterEncoding("utf-8");
		}
		if (res.getCharacterEncoding() == null || res.getCharacterEncoding().equalsIgnoreCase("ISO-8859-1")) {
			res.setCharacterEncoding("utf-8");
		}

		if (urlIntecepter.canPass(uri)) {
			Date start = new Date();
			try {
				String startStr = DateUtils.format(start, DateUtils.PATTERN_LONGTIMEPLUS);
				//
				mdcSystemDefinedInterceptor.setSystemDefinedBeforeDoFilter(req, res);
				// 开始日志
				LOGGER.info("==HttpFilter开始==,flag:[start],hasEx:[false],url:[" + req.getRequestURL() + "],开始时间为："
						+ startStr);
				// 打印参数
				Map<String, Object> parameterMap = this.parametersInterceptor.getParametersMap(req);
				LOGGER.info("请求的URI为：" + uri + "," + req.getMethod() + "请求的请求详情为：" + JSON.toJSONString(parameterMap));
			} catch (Exception e) {
				LOGGER.error("Httpfilter 记录参数等信息时出现异常");
			}

			LogResponseWrapper wapperRes = null;
			try {
				wapperRes = new LogResponseWrapper(res);
				chain.doFilter(req, wapperRes);
			} catch (Exception e) {
				// 异常结束日志
				long endTime = System.currentTimeMillis();
				LOGGER.info("==HttpFilter发生异常并结束==,flag:[end],hasEx:[true],url:[" + req.getRequestURL() + "],结束时间为："
						+ DateUtils.format(new Date(endTime), DateUtils.PATTERN_LONGTIMEPLUS) + "，用时 "
						+ (endTime - start.getTime()) + " ms");
			}

			// 缓存的输出信息写入output
			wapperRes.flushBuffer();
			wapperRes.setHeader("Content-Length", wapperRes.getBuffer().toByteArray().length + "");
			res.getOutputStream().write(wapperRes.getBuffer().toByteArray());
			res.getOutputStream().flush();

			// 正常结束日志
			long endTime = System.currentTimeMillis();
			LOGGER.info("==HttpFilter结束==,flag:[end],hasEx:[false],url:[" + req.getRequestURL() + "],结束时间为："
					+ DateUtils.format(new Date(endTime), DateUtils.PATTERN_LONGTIMEPLUS) + "，用时 "
					+ (endTime - start.getTime()) + " ms");

			// 攔截器
			mdcSystemDefinedInterceptor.setSystemDefinedAfterDoFilter(req, wapperRes);
		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		urlIntecepter.setURiConditions(config.getInitParameter(URICONDITIONS));
		urlIntecepter.setNotMatchUriConditions(config.getInitParameter(URINOTMATCHCONDITIONS));
	}

}
