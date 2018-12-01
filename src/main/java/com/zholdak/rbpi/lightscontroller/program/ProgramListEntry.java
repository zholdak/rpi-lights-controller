package com.zholdak.rbpi.lightscontroller.program;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Aleksey Zholdak (aleksey@zholdak.com) 2018-09-30 13:16
 */
@AllArgsConstructor
@Getter
@ToString
public class ProgramListEntry {

	private long lastModified;

	private File programScriptFile;

	@Setter
	private LedStripProgram program;
}
