package com.zholdak.rbpi.lightscontroller.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static com.zholdak.rbpi.lightscontroller.LightsControllerConfigProps.configProps;
import static com.zholdak.rbpi.lightscontroller.hardware.HardwareManager.hardwareManager;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.pmw.tinylog.Logger.debug;
import static org.pmw.tinylog.Logger.error;
import static org.pmw.tinylog.Logger.info;
import static org.pmw.tinylog.Logger.trace;
import static org.pmw.tinylog.Logger.warn;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-09-30 12:36
 */
@SuppressWarnings("WhileLoopReplaceableByForEach")
public final class ProgramsFactory {

	private static ProgramsFactory _instance;

	private ScriptEngine groovy;
	private ScheduledExecutorService scheduledExecutor;

	private Map<String,ProgramListEntry> programsMap = new Hashtable<>();
	private LedStripProgram currPlayingProgram;

	private volatile boolean stopRequested = false;

	private ProgramsFactory() { }

	/**
	 *
	 */
	public static ProgramsFactory programsFactory() {
		return init();
	}

	/**
	 *
	 */
	public static ProgramsFactory init() {
		if (_instance == null) {
			_instance = new ProgramsFactory().build();
		}
		return _instance;
	}

	/**
	 *
	 */
	private ProgramsFactory build() {

		trace("Initializing script engine factory ...");
		ScriptEngineManager manager = new ScriptEngineManager();

		trace("Initializing Groovy engine ...");
		groovy = manager.getEngineByName("groovy");
		if (groovy == null) {
			throw new IllegalStateException("Can't initialize Groovy engine. Missing jsr223 library?");
		}

		trace("Starting filesystem programs directory watcher thread");
		scheduledExecutor = newSingleThreadScheduledExecutor();
		scheduleNextProgramDirWatcher();

		return this;
	}

	/**
	 *
	 */
	private void registerProgram(File programFile) {
		requireNonNull(programFile, "programFile argument must be not null");

		debug("Trying to initialize and register program from file '{}' ...", programFile.getName());

		LedStripProgram program = loadProgram(programFile);
		Object prevEntry = programsMap
				.put(programFile.getName(), new ProgramListEntry(programFile.lastModified(), programFile, program));
		if (prevEntry == null) {
			info("Program script '{}' added into map", programFile.getName());
		} else {
			info("Program script '{}' updated in map", programFile.getName());
		}
	}

	/**
	 * Program loading:<ul>
	 * <li> Load program script from filesystem,</li>
	 * <li> compile it,</li>
	 * <li> create new instance of the program class</li>
	 * <li> init program class with hardware.</li>
	 * </ul>
	 */
	private LedStripProgram loadProgram(File programFile) {
		requireNonNull(programFile, "programFile argument must be not null");
		try {
			info("Loading program class from file '{}' ...", programFile.getName());
			CompiledScript script = ((Compilable) groovy).compile(new FileReader(programFile));
			Class programClass = (Class) script.eval();
			LedStripProgram program = (LedStripProgram) programClass.newInstance();
			program.init(hardwareManager().getLedDriver());
			String reason = program.loadRestrictionReason();
			if (reason != null) {
				info("- program script '{}' not loaded: {}", programFile.getName(), reason);
				return null;
			} else {
				debug("+ loaded and initialized Program script '{}'", program.getName());
				return program;
			}
		} catch (IllegalAccessException | InstantiationException | ScriptException | FileNotFoundException e) {
			error("{} occurred while loading and compiling '{}': {}. See trace logs for detailed stacktrace.",
					e.getClass().getSimpleName(), programFile.getName(), e.getMessage());
			trace(e);
		}
		return null;
	}

	/**
	 * Just helps to schedule next execution of programs directory watcher
	 */
	private void scheduleNextProgramDirWatcher() {
		scheduledExecutor.schedule(new ProgramDirWatcherRunnable(), 1, TimeUnit.SECONDS);
	}

	/**
	 * This class make possible to refresh programs while them source files takes changes on the filesystem
	 */
	private class ProgramDirWatcherRunnable implements Runnable {
		@Override
		public void run() {
			try {
				File[] files = new File(configProps().getProgramsPath())
						.listFiles((dir, name) -> name.endsWith(configProps().getProgramsSuffix()));
				if (files != null) {
					Stream.of(files).forEach(file -> {
						ProgramListEntry progEntry = programsMap.get(file.getName());
						if (progEntry == null || progEntry.getLastModified() != file.lastModified()) {
							registerProgram(file);
						}
						Iterator<Map.Entry<String,ProgramListEntry>> progIterator = programsMap.entrySet().iterator();
						while (progIterator.hasNext()) {
							ProgramListEntry entry = progIterator.next().getValue();
							if (!entry.getProgramScriptFile().exists()) {
								progIterator.remove();
								info("Program script '{}' removed from map", entry.getProgramScriptFile().getName());
							}
						}
						programsMap.values().removeIf(progListEntry -> !progListEntry.getProgramScriptFile().exists());
					});
				} else {
					error("Got null files array when listing programs directory");
				}
			} finally {
				scheduleNextProgramDirWatcher();
			}
		}
	}

	private boolean isStopRequested() {
		return stopRequested;
	}

	private void setStopRequested() {
		this.stopRequested = true;
	}

	/**
	 * Return programs list entries, sorted by program order index
	 */
	private Collection<ProgramListEntry> getPrograms() {
		return programsMap.values()
				.stream()
				.filter(ple -> nonNull(ple.getProgram()))
				.sorted(comparingInt(ple -> ple.getProgram().getOrder()))
				.collect(Collectors.toList());
	}

	/**
	 *
	 */
	public void start() {
		while (true) {
			Collection<ProgramListEntry> programsList = getPrograms();
			if (programsList.isEmpty()) {
				sleep(Duration.ofSeconds(1));
				continue;
			}
			debug("Start iterating through programs");
			ProgramListEntry programListEntry = null;
			try {
				Iterator<ProgramListEntry> programsIterator = programsList.iterator();
				while (programsIterator.hasNext()) {
					(currPlayingProgram = (programListEntry = programsIterator.next()).getProgram()).start();
					if (currPlayingProgram.isTerminated() || isStopRequested()) break;
				}
				if (isStopRequested()) {
					trace("Programs iteration interrupted");
					break;
				} else {
					trace("Programs iteration finished");
				}
			} catch (AlgorithmRealizingException e) {
				error("{}: {}", e.getCause().getClass().getName(), e.getMessage());
				trace(e);
				if (programListEntry != null) {
					warn("Program excluded from programs list. ");
					programListEntry.setProgram(null);
				}
			}
		}
		if (isStopRequested()) {
			info("Programs execution loop has stopped because stop request received");
		}
	}

	private void sleep(Duration timeout) {
		//noinspection CatchMayIgnoreException
		try {
			Thread.sleep(timeout.toMillis());
		} catch (InterruptedException e) { }
	}

	public void shutdown() {
		debug("Shutdowning");
		if (scheduledExecutor != null) {
			debug("Stopping filesystem programs directory watcher");
			scheduledExecutor.shutdownNow();
			//noinspection CatchMayIgnoreException
			try {
				scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) { }
		}
		info("Send request to stop programs execution loop");
		setStopRequested();
		if (currPlayingProgram != null) {
			info("Stopping current playing program and cleanup");
			currPlayingProgram.stop();
		}
	}
}