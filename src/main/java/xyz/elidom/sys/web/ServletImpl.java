package xyz.elidom.sys.web;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

/**
 * ServletInputStream 확장
 * 
 * @author shortstop
 */
public class ServletImpl extends ServletInputStream {
	private InputStream inputStream;

	public ServletImpl(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setReadListener(ReadListener listener) {
	}

	@Override
	public int read() throws IOException {
		return this.inputStream.read();
	}
}