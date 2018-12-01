package com.zholdak.rbpi.lightscontroller.hardware;

import com.diozero.ws281xj.LedDriverInterface;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-09 20:24
 */
public class ConsoleLedDriverFactory extends LedDriverFactory {

	@Override
	public LedDriverInterface createLedDriver() {

		return new ConsoleLedDriver(100);
	}
}
