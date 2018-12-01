package com.zholdak.rbpi.lightscontroller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-09-30 13:18
 */
@Getter
@Setter
@ToString
public class LightsControllerConfigProps {

	private String programsPath;// = "./src/main/groovy/lsprogs";
	private String programsSuffix;// = "lsprog.groovy";
	private String ledDriverFactoryClass;// = "com.zholdak.rbpi.lightscontroller.hardware.DummyLedDriverFactory";

	private static LightsControllerConfigProps _instance;

	private LightsControllerConfigProps() {
		try {
			InputStream input = new FileInputStream("config.properties");
			Properties prop = new Properties();
			prop.load(input);
			this.programsPath = prop.getProperty("programsPath");
			this.programsSuffix = prop.getProperty("programsSuffix");
			this.ledDriverFactoryClass = prop.getProperty("ledDriverFactoryClass");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	};

	public static LightsControllerConfigProps init() {
		if (_instance == null) {
			_instance = new LightsControllerConfigProps();
		}
		return _instance;
	}

	public static LightsControllerConfigProps configProps() {
		return init();
	}
}
