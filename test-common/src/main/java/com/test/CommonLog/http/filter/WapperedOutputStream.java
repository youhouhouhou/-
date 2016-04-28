package com.test.CommonLog.http.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class WapperedOutputStream extends ServletOutputStream {

	private ByteArrayOutputStream bos = null;

	public WapperedOutputStream(ByteArrayOutputStream stream) throws IOException {
		bos = stream;
	}

	@Override
	public void write(int b) throws IOException {
		bos.write(b);
		byte[] byts = new byte[1];
		byts[0] = (byte) b;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] byts = new byte[len];
		System.arraycopy(b, off, byts, 0, len);
		bos.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		bos.write(b);
	}

	@Override
	public void print(boolean b) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(b);
	}

	@Override
	public void print(char c) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(c);
	}

	@Override
	public void print(double d) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(d);
	}

	@Override
	public void print(float f) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(f);
	}

	@Override
	public void print(int i) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(i);
	}

	@Override
	public void print(long l) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(l);
	}

	@Override
	public void print(String string) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.print(string);
	}

	@Override
	public void println() throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println();
	}

	@Override
	public void println(boolean b) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(b);
	}

	@Override
	public void println(char c) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(c);
	}

	@Override
	public void println(double d) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(d);
	}

	@Override
	public void println(float f) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(f);
	}

	@Override
	public void println(int i) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(i);
	}

	@Override
	public void println(long l) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(l);
	}

	@Override
	public void println(String s) throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.println(s);
	}

	@Override
	public void flush() throws IOException {
		PrintWriter print = new PrintWriter(bos);
		print.flush();
	}

	@Override
	public void close() throws IOException {
		bos.close();
		super.close();
	}

	// @Override
	public boolean isReady() {
		return false;
	}

	// @Override
	public void setWriteListener(WriteListener arg0) {
	}

}
