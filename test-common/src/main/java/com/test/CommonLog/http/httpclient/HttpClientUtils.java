package com.test.CommonLog.http.httpclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.test.CommonLog.common.Constants;
import com.test.CommonLog.common.utils.CommonUtils;
import com.test.CommonLog.common.utils.IpUtils;
import com.test.CommonLog.common.utils.UUIDUTils;
import com.test.CommonLog.common.utils.UserTokenUtils;

/**
 * http工具类
 * 
 * @author zhuzhujier
 *
 */
public class HttpClientUtils {

	private static final String UTF_8 = "utf-8";
	private static PoolingHttpClientConnectionManager cm = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);
	// 请求重试处理
	private static HttpRequestRetryHandler httpRequestRetryHandler = null;

	static {
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", plainsf).register("https", sslsf).build();
		cm = new PoolingHttpClientConnectionManager(registry);
		httpRequestRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount >= 5) {// 如果已经重试了5次，就放弃
					return false;
				}
				if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
					return true;
				}
				if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
					return false;
				}
				if (exception instanceof InterruptedIOException) {// 超时
					return false;
				}
				if (exception instanceof UnknownHostException) {// 目标服务器不可达
					return false;
				}
				if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
					return false;
				}
				if (exception instanceof SSLException) {// ssl握手异常
					return false;
				}

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();
				// 如果请求是幂等的，就再次尝试
				if (!(request instanceof HttpEntityEnclosingRequest)) {
					return true;
				}
				return false;

			}
		};
	}

	/**
	 * 
	 * @return
	 */
	public static HttpClient getHttpClient() {
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
				.setRetryHandler(httpRequestRetryHandler).build();
		return httpClient;
	}

	/**
	 * get
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String httpGetRequest(String url) throws Exception {
		return httpGetRequest(url, null, null);
	}

	/**
	 * get
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpGetRequest(String url, Map<String, String> params) throws Exception {

		return httpGetRequest(url, null, params);
	}

	/**
	 * get 最底层get
	 * 
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpGetRequest(String url, Map<String, String> headers, Map<String, String> params)
			throws Exception {
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);

		if (!CollectionUtils.isEmpty(params)) {
			ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
			ub.setParameters(pairs);
		}

		HttpGet httpGet = new HttpGet(ub.build());
		if (!CollectionUtils.isEmpty(headers)) {
			for (Map.Entry<String, String> param : headers.entrySet()) {
				httpGet.addHeader(param.getKey(), param.getValue());
			}
		}
		return getResult(httpGet);
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String httpPostRequest(String url) throws Exception {

		return httpPostRequest(url, null, null);
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPostRequest(String url, Map<String, String> params) throws Exception {

		return httpPostRequest(url, null, params);
	}

	/**
	 * post 最底层post
	 * 
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPostRequest(String url, Map<String, String> headers, Map<String, String> params)
			throws Exception {
		HttpPost httpPost = new HttpPost(url);
		if (!CollectionUtils.isEmpty(headers)) {
			for (Entry<String, String> param : headers.entrySet()) {
				httpPost.addHeader(param.getKey(), param.getValue());
			}
		}
		if (!CollectionUtils.isEmpty(params)) {
			ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
		}

		return getResult(httpPost);
	}

	private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, String> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> param : params.entrySet()) {
			pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
		}
		return pairs;
	}

	/**
	 * 处理Http请求
	 * 
	 * @param request
	 * @return
	 */
	private static String getResult(HttpRequestBase request) throws Exception {
		boolean createMdc = false;
		try {
			createMdc = copyMdcInfo2Headers(request);

			HttpClient httpClient = getHttpClient();
			if (request instanceof HttpGet) {
				LOGGER.info("http get请求url[" + request.getURI() + "]headers["
						+ JSON.toJSONString(request.getAllHeaders()) + "]");
			} else if (request instanceof HttpPost) {
				HttpEntity entity = ((HttpPost) request).getEntity();
				LOGGER.info(
						"http post请求url[" + request.getURI() + "]headers[" + JSON.toJSONString(request.getAllHeaders())
								+ "]entity[" + (entity == null ? "" : EntityUtils.toString(entity)) + "]");
			} else {
				LOGGER.info("http 其他请求url[" + request.getURI() + "]headers["
						+ JSON.toJSONString(request.getAllHeaders()) + "]");
			}

			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			if ("200".equals(response.getStatusLine().getStatusCode() + "")) {
				if (entity != null) {
					String ret = EntityUtils.toString(entity);
					LOGGER.info("http 请求正常结果为" + ret);
					return ret;
				}
				return null;
			} else {
				throw new RuntimeException(
						"http接口请求" + request.getURI() + "返回结果码[" + response.getStatusLine().getStatusCode() + "]");
			}

		} catch (Exception e) {
			LOGGER.error("http接口调用异常", e);
			throw e;
		} finally {
			if (createMdc) {
				MDC.clear();
			}
		}

	}

	/**
	 * 复制mdc中的信息到headers
	 * 
	 * @param request
	 * @return 是否新建了mdc信息
	 */
	private static boolean copyMdcInfo2Headers(HttpRequestBase request) {

		boolean credteMdc = false;

		if (StringUtils.isBlank(MDC.get(Constants.OPERATEID))) {
			credteMdc = true;
			MDC.put(Constants.OPERATEID, UUIDUTils.getUUID());
			MDC.put(Constants.TOKENID, CommonUtils.ifBankReturnDefault(UserTokenUtils.getToken(), "X"));
			MDC.put(Constants.IP, CommonUtils.ifBankReturnDefault(IpUtils.getRealIpWithStaticCache(), "X"));
			MDC.put(Constants.UPIP, "X");
			MDC.put(Constants.SYSTEMID, "X");
			MDC.put(Constants.UPSYSTEMID, "X");
			MDC.put(Constants.CHAIN, "0");

		}

		request.addHeader(Constants.OPERATEID, MDC.get(Constants.OPERATEID));
		request.addHeader(Constants.TOKENID, MDC.get(Constants.TOKENID));
		request.addHeader(Constants.IP, MDC.get(Constants.IP));
		request.addHeader(Constants.UPIP, MDC.get(Constants.UPIP));
		request.addHeader(Constants.SYSTEMID, MDC.get(Constants.SYSTEMID));
		request.addHeader(Constants.UPSYSTEMID, MDC.get(Constants.UPSYSTEMID));
		request.addHeader(Constants.CHAIN, MDC.get(Constants.CHAIN));

		return credteMdc;
	}

}