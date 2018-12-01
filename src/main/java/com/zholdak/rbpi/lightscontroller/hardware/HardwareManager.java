package com.zholdak.rbpi.lightscontroller.hardware;

import java.io.IOException;

import com.diozero.ws281xj.LedDriverInterface;

import static com.zholdak.rbpi.lightscontroller.LightsControllerConfigProps.configProps;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static org.pmw.tinylog.Logger.debug;
import static org.pmw.tinylog.Logger.trace;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-10-02 22:55
 */
public class HardwareManager {

	private static HardwareManager _instance;

	private LedDriverFactory ledDriverFactory;

	private HardwareManager() {}

	public static HardwareManager hardwareManager() {
		return init();
	}

	public static HardwareManager init() {
		if (_instance == null) {
			_instance = new HardwareManager().build();
		}
		return _instance;
	}

	private HardwareManager build() {

		String ledDriverFactoryClassName = configProps().getLedDriverFactoryClass();
		trace("Creating led driver factory '{}'", ledDriverFactoryClassName);
		try {
			ledDriverFactory = (LedDriverFactory)Class.forName(ledDriverFactoryClassName).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(
					format("%s catched while trying to create led driver factory '%s'", e.getClass().getSimpleName(),
							ledDriverFactoryClassName));
		}
		trace("Initializing led driver factory");
		ledDriverFactory.init();

		return this;
	}

	public LedDriverInterface getLedDriver() {
		return ledDriverFactory.getLedDriver();
	}

	public void shutdown() {
		debug("Shutdown hardware manager");
		if (ledDriverFactory != null) {
			debug("Closing led driver factory");
			try {
				ledDriverFactory.close();
			} catch (IOException e) { }
		}
	}
}
