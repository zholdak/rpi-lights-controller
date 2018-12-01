package com.zholdak.rbpi.lightscontroller.hardware;

import com.diozero.ws281xj.LedDriverInterface;

import static org.pmw.tinylog.Logger.trace;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-02 21:49
 */
public class DummyLedDriver implements LedDriverInterface {

	private int numPixels;
	private int[] colour;

	private DummyLedDriver() { }

	public DummyLedDriver(int numPixels) {
		this.numPixels = numPixels;
		this.colour = new int[numPixels];
	}

	@Override
	public void close() {
		//trace("close()");
	}

	@Override
	public int getNumPixels() {
		return numPixels;
	}

	@Override
	public void render() {
		//trace("render()");
	}

	@Override
	public void allOff() {
		for (int i = 0; i < numPixels; i++) {
			setPixelColour(i, 0);
		}
		render();
	}

	@Override
	public int getPixelColour(int pixel) {
		validatePixel(pixel);
		return colour[pixel];
	}

	@Override
	public void setPixelColour(int pixel, int colour) {
		validatePixel(pixel);
		this.colour[pixel] = colour;
		//trace("setPixelColour(pixel={}, color={})", pixel, colour);
	}

	private void validatePixel(int pixel) {
		if (pixel < 0 || pixel >= numPixels) {
			throw new IllegalArgumentException("pixel must be 0.." + (numPixels - 1));
		}
	}
}
