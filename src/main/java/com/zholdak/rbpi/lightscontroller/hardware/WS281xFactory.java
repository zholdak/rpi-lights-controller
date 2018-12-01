package com.zholdak.rbpi.lightscontroller.hardware;

import com.diozero.ws281xj.LedDriverInterface;
import com.diozero.ws281xj.rpiws281x.WS281x;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-02 22:24
 */
public class WS281xFactory extends LedDriverFactory {

	@Override
	public LedDriverInterface createLedDriver() {

		int gpioNum = 12;
		int brightness = 254;	// 0..255
		int numPixels = 100;

		return new WS281x(gpioNum, brightness, numPixels);
	}
}
