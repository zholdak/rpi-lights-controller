package com.zholdak.rbpi.lightscontroller;

import org.pmw.tinylog.Level;

import com.zholdak.rbpi.lightscontroller.hardware.HardwareManager;
import com.zholdak.rbpi.lightscontroller.program.ProgramsFactory;

import static com.zholdak.rbpi.lightscontroller.hardware.HardwareManager.hardwareManager;
import static com.zholdak.rbpi.lightscontroller.program.ProgramsFactory.programsFactory;
import static java.lang.Runtime.getRuntime;
import static org.pmw.tinylog.Configurator.defaultConfig;
import static org.pmw.tinylog.Logger.info;
import static org.pmw.tinylog.Logger.trace;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-09-30 11:42
 */
public class LightsControllerApplication {

	static {
		defaultConfig()
				.level(Level.DEBUG)
				.formatPattern("{date:HH:mm:ss.SSS} {level} [{file}:{line}] {message}")
				.activate();
	}

	public static void main(String[] args) {
		info("Application starting ...");
		new LightsControllerApplication().go();
	}

	private LightsControllerApplication() {
		LightsControllerConfigProps.init();
		HardwareManager.init();
		ProgramsFactory.init();
	}

	private void go() {
		trace("Adding shutdown hook for correct application finalization");
		getRuntime().addShutdownHook(new LightsControllerApplicationShutdownHook());

		programsFactory().start();
	}

	/**
	 * This class helps gracefully finalize this factory
	 */
	private class LightsControllerApplicationShutdownHook extends Thread {
		@Override
		public void run() {
			info("Shutdown requested");
			programsFactory().shutdown();
			//hardwareManager().shutdown();
		}
	}
}
