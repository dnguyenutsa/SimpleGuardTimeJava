/*
 * $Id: LogPrinter.java 169 2011-03-03 18:45:00Z ahto.truu $
 *
 *
 *
 * Copyright 2008-2011 GuardTime AS
 *
 * This file is part of the GuardTime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guardtime.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * A logger that dumps the messages to a {@code PrintWriter} object.
 *
 * @since 0.4
 */
public class LogPrinter implements LogListener {

	/**
	 * The {@code PrintWriter} where the messages are written.
	 */
	protected PrintWriter pw;

	/**
	 * Constructs a new object to write messages to the given
	 * {@code PrintWriter}.
	 *
	 * @param pw
	 *            the object to write the messages to.
	 */
	public LogPrinter(PrintWriter pw) {
		this.pw = pw;
	}

	/**
	 * Constructs a new object to write messages to the given
	 * {@code OutputStream}.
	 *
	 * @param out
	 *            the stream to write the messages to.
	 */
	public LogPrinter(OutputStream out) {
		this(new PrintWriter(out));
	}

	/**
	 * Constructs a new object to write messages to the given {@code Writer}.
	 *
	 * @param out
	 *            the writer to write the messages to.
	 */
	public LogPrinter(Writer out) {
		this(new PrintWriter(out));
	}

	/**
	 * Constructs a new object to write messages to the given file.
	 *
	 * @param file
	 *            the file to write the messages to.
	 * @throws IOException
	 *             when the file can't be opened for writing.
	 */
	public LogPrinter(File file) throws IOException {
		this(new PrintWriter(new FileWriter(file)));
	}

	/**
	 * Constructs a new object to write messages to the file with the given
	 * name.
	 *
	 * @param name
	 *            the name of the file to write the messages to.
	 * @throws IOException
	 *             when the file can't be opened for writing.
	 */
	public LogPrinter(String name) throws IOException {
		this(new PrintWriter(new FileWriter(name)));
	}

	/**
	 * Logs the given level and message to the embedded writer.
	 *
	 * @param level
	 *            the importance level of the message.
	 * @param message
	 *            the message.
	 */
	public void log(LogLevel level, Object message) {
		pw.println(level + ": " + message);
		pw.flush();
	}

}
