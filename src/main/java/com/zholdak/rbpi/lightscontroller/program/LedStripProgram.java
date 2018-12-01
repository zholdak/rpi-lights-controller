package com.zholdak.rbpi.lightscontroller.program;

import org.pmw.tinylog.Logger;

import com.diozero.ws281xj.LedDriverInterface;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.pmw.tinylog.Logger.debug;
import static org.pmw.tinylog.Logger.info;
import static org.pmw.tinylog.Logger.trace;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-09-30 12:02
 */
public abstract class LedStripProgram {

	private LedDriverInterface ledDriver;
	private volatile Boolean running = false;
	private volatile boolean terminateRequested = false;

	public LedStripProgram init(LedDriverInterface ledDriver) {
		trace("Initializing program class '{}' with name '{}'", this.getClass().getName(), getName());
		this.ledDriver = ledDriver;
		return this;
	}

	public LedStripProgram start() throws RuntimeException {
		if (isRunning()) {
			Logger.warn("Program '{}' (Class '{}') already running", getName(), getClass().getSimpleName());
			return this;
		}
		try {
			setRunning(true);

			trace("Running program '{}'", getName());

			if (ledDriver == null) {
				throw new IllegalStateException("ledDriver not initialized yet?");
			}
			try {

				debug("Invoking programmed algorithm '{}'", getName());
				synchronized (this) {
					algorithm();
				}
				if (isTerminated()) {
					trace("Programmed algorithm invocation terminated");
				} else {
					trace("Programmed algorithm invocation finished");
				}

			} catch (Exception e) {
				throw new AlgorithmRealizingException(format("%s catched when realizing algorithm of '%s' program (%s): %s",
						e.getClass().getSimpleName(), getName(), getClass().getName(), e.getMessage()), e);
			}
			return this;

		} finally {
			setRunning(false);
			trace("Program '{}' finished", getName());
		}
	}

	public void stop() {
		info("Stopping program {}", getName());
		try {
			terminate();
			cleanIt();
		} catch (Exception e) {
			throw new AlgorithmRealizingException(format("%s catched when trying to clean program %s: %s",
					e.getClass().getSimpleName(), getName(), e.getMessage()), e);
		}

	}

	protected String name() {
		return getClass().getSimpleName();
	};

	public String getName() {
		requireNonNull(name(), "Name of the program must be not null");
		return name();
	}

	protected int order() {
		return 0;
	}

	public Integer getOrder() {
		return order();
	}

	protected String loadPredicate() {
		return null;
	}

	public String loadRestrictionReason() {
		return loadPredicate();
	}

	protected abstract void algorithm() throws Exception;

	private void cleanIt() throws Exception {
		trace("Waiting for cleanup program {} ...", getName());
		synchronized (this) {
			trace("Cleaning up program {}", getName());
			clean();
		}
	}

	public abstract void clean() throws Exception;

	protected LedDriverInterface getLedDriver() {
		return ledDriver;
	}

	protected LedDriverInterface getLeds() {
		return getLedDriver();
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		synchronized (this.running) {
			this.running = running;
		}
	}

	protected boolean isTerminateRequested() {
		return terminateRequested;
	}

	public boolean isTerminated() {
		return terminateRequested;
	}

	public void terminate() {
		this.terminateRequested = true;
	}
}
