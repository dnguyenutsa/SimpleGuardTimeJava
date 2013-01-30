/*
 * $Id: Log.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This abstract class manages a simple logging framework.
 *
 * @since 0.4
 */
public abstract class Log {

	/**
	 * The current global logging level. Any messages whose importance level is
	 * below this threshold are ignored.
	 */
	private static LogLevel globalLevel = LogLevel.All;

	/**
	 * Retrieves the current global logging level. Any messages whose importance
	 * level is below this threshold are ignored.
	 *
	 * @return the current global logging level.
	 */
	public static synchronized LogLevel getLevel() {
		return globalLevel;
	}

	/**
	 * Sets the global logging level. Any future messages whose importance level
	 * is below this threshold will be ignored. This is the "master volume" knob
	 * to adjust the total amount of noise the system produces.
	 *
	 * @param level
	 *            the new global logging level.
	 * @throws NullPointerException
	 *             when {@code level} is {@code null}.
	 */
	public static synchronized void setLevel(LogLevel level) {
		if (level == null) {
			throw new NullPointerException();
		}
		globalLevel = level;
	}

	/**
	 * The list of currently registered loggers.
	 * <p>
	 * TODO: Replace the raw type with {@code ArrayList<ListenerEntry>} when we
	 * upgrade the platform requirements to JDK 1.5+.
	 */
	private static List listeners = new ArrayList();

	/**
	 * Adds the given logger at the given importance level. Only messages whose
	 * importance level is equal to or higher than the given threshold will be
	 * passed on to the logger.
	 *
	 * @param listener
	 *            the logger to add.
	 * @param level
	 *            the threshold for importance of messages.
	 * @throws NullPointerException
	 *             when {@code listener} or {@code level} is {@code null}.
	 */
	public static synchronized void addListener(LogListener listener,
			LogLevel level) {
		if (listener == null || level == null) {
			throw new NullPointerException();
		}
		listeners.add(new ListenerEntry(listener, level));
	}

	/**
	 * Removes the given logger. No further messages will be passed on to the
	 * logger.
	 *
	 * @param listener
	 *            the logger to remove.
	 */
	public static synchronized void removeListener(LogListener listener) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			ListenerEntry e = (ListenerEntry) i.next();
			if (e.listener == listener) {
				i.remove();
			}
		}
	}

	/**
	 * Logs the given message at the given importance level. This means passing
	 * the message on to all currently registered listeners whose threshold is
	 * not higher than the importance level of the message.
	 *
	 * @param level
	 *            the importance level of the message.
	 * @param message
	 *            the message.
	 * @throws NullPointerException
	 *             when {@code level} is {@code null}.
	 */
	public static synchronized void log(LogLevel level, Object message) {
		if (level == null) {
			throw new NullPointerException();
		}
		if (globalLevel.compareTo(level) <= 0) {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				ListenerEntry e = (ListenerEntry) i.next();
				if (e.level.compareTo(level) <= 0) {
					e.listener.log(level, message);
				}
			}
		}
	}

	/**
	 * Logs the given message at the {@link LogLevel#Fatal} level.
	 *
	 * @param message
	 *            the message.
	 */
	public static void fatal(Object message) {
		log(LogLevel.Fatal, message);
	}

	/**
	 * Logs the given message at the {@link LogLevel#Error} level.
	 *
	 * @param message
	 *            the message.
	 */
	public static void error(Object message) {
		log(LogLevel.Error, message);
	}

	/**
	 * Logs the given message at the {@link LogLevel#Warning} level.
	 *
	 * @param message
	 *            the message.
	 */
	public static void warning(Object message) {
		log(LogLevel.Warning, message);
	}

	/**
	 * Logs the given message at the {@link LogLevel#Debug} level.
	 *
	 * @param message
	 *            the message.
	 */
	public static void debug(Object message) {
		log(LogLevel.Debug, message);
	}

	/*
	 * This is here just to clean up the public JavaDoc.
	 */
	private Log() {
	}

	/**
	 * An internal helper to keep the loggers paired to their levels.
	 */
	private static class ListenerEntry {

		/**
		 * The logger.
		 */
		public final LogListener listener;

		/**
		 * The level of messages the logger is interested in.
		 */
		public final LogLevel level;

		/**
		 * Constructs a new logger/level pair.
		 *
		 * @param listener
		 *            the logger.
		 * @param level
		 *            the level.
		 */
		public ListenerEntry(LogListener listener, LogLevel level) {
			this.listener = listener;
			this.level = level;
		}

	}

}
