package com.zholdak.rbpi.lightscontroller.hardware;

import java.io.Closeable;
import java.io.IOException;

import com.diozero.ws281xj.LedDriverInterface;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-02 22:23
 */
public abstract class LedDriverFactory implements Closeable {

	private LedDriverInterface ledDriver;

	protected abstract LedDriverInterface createLedDriver();

	public LedDriverInterface init() {
		ledDriver = createLedDriver();
		if (ledDriver == null) {
			throw new IllegalStateException("null ledDriver created =(");
		}
		return ledDriver;
	}

	public LedDriverInterface getLedDriver() {
		if (ledDriver == null) {
			throw new IllegalStateException("ledDriver not yet initialized");
		}
		return ledDriver;
	}

	@Override
	public void close() throws IOException {
		if (ledDriver != null) {
			ledDriver.close();
		}
	}
}
