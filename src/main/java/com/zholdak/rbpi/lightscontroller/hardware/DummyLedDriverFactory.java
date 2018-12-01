package com.zholdak.rbpi.lightscontroller.hardware;

import com.diozero.ws281xj.LedDriverInterface;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-02 22:14
 */
public class DummyLedDriverFactory extends LedDriverFactory {

	@Override
	public LedDriverInterface createLedDriver() {

		return new DummyLedDriver(100);
	}
}
