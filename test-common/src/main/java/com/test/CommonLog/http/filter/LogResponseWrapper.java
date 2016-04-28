package com.test.CommonLog.http.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class LogResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream buffer = null;
	private ServletOutputStream out = null;
	private PrintWriter print = null;
	private int stauts = 200;
	private String stautsError = null;
	private Map<String, String> header = new HashMap<String, String>();

	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}

	public LogResponseWrapper(HttpServletResponse response) throws IOException {
		super(response);
		buffer = new ByteArrayOutputStream(); // 真正存储数据的流
		out = new WapperedOutputStream(buffer);
		print = new PrintWriter(new OutputStreamWriter(out, this.getCharacterEncoding()));
	}

	// 重载父类获取outputstream的方法
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	// 重载父类获取writer的方法
	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {
		return print;
	}

	// 重载父类获取flushBuffer的方法
	@Override
	public void flushBuffer() throws IOException {
		if (print != null) {
			print.flush();
		}
		if (out != null) {
			out.flush();
		}
	}

	@Override
	public void reset() {
		buffer.reset();
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		stauts = sc;
		stautsError = msg;
		super.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		stauts = sc;
		super.sendError(sc);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setStatus(int sc, String sm) {
		stauts = sc;
		stautsError = sm;
		super.setStatus(sc, sm);
	}

	@Override
	public void setStatus(int sc) {
		stauts = sc;
		super.setStatus(sc);
	}

	@Override
	public void addDateHeader(String name, long date) {
		header.put(name, date + "");
		super.addDateHeader(name, date);
	}

	@Override
	public void addHeader(String name, String value) {
		header.put(name, value);
		super.addHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		header.put(name, value + "");
		super.addIntHeader(name, value);
	}

	@Override
	public void setDateHeader(String name, long date) {
		header.put(name, date + "");
		super.setDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		header.put(name, value);
		super.setHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		header.put(name, value + "");
		super.setIntHeader(name, value);
	}

	public int getStatus() {
		return this.stauts;
	}

	public String getStatusError() {
		return this.stautsError;
	}

	public byte[] getResponseData() throws IOException {
		flushBuffer(); // 将out、writer中的数据强制输出到WapperedResponse的buffer里面，否则取不到数据
		return buffer.toByteArray();
	}

	public Map<String, String> getHeader() {
		return this.header;
	}

}
