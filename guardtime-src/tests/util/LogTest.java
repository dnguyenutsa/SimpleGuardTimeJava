/*
 * $Id: LogTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

package tests.util;

import com.guardtime.util.Log;
import com.guardtime.util.LogLevel;
import com.guardtime.util.LogListener;

import junit.framework.TestCase;

/**
 * JUnit test cases for the logging framework.
 */
public class LogTest extends TestCase {

	/**
	 * Tests adding and removing of loggers.
	 */
	public void testAddRemove() {
		LogCounter lc = new LogCounter();
		int count = lc.count;
		Log.fatal("Logging test message, ignore");
		assertEquals(count, lc.count);
		Log.addListener(lc, LogLevel.All);
		Log.fatal("Logging test message, ignore");
		count += 1;
		assertEquals(count, lc.count);
		Log.removeListener(lc);
		Log.fatal("Logging test message, ignore");
		assertEquals(count, lc.count);
	}

	/**
	 * Tests filtering by logger level.
	 */
	public void testLoggerLevel() {
		LogLevel[] levels = LogLevel.getLevels();
		LogCounter lc = new LogCounter();
		int count = lc.count;
		for (int i = 0; i < levels.length; ++i) {
			Log.addListener(lc, levels[i]);
		}
		for (int i = 0; i < levels.length; ++i) {
			Log.log(levels[i], "Logging test message, ignore");
			count += (levels.length - i);
			assertEquals("Message level " + levels[i], count, lc.count);
		}
		Log.removeListener(lc);
	}

	/**
	 * Tests filtering by global level.
	 */
	public void testGlobalLevel() {
		LogLevel[] levels = LogLevel.getLevels(true, true);
		LogCounter lc = new LogCounter();
		Log.addListener(lc, LogLevel.All);
		int count = lc.count;
		for (int i = 0; i < levels.length; ++i) {
			Log.setLevel(levels[i]);
			for (int j = 1; j < levels.length - 1; ++j) {
				Log.log(levels[j], "Logging test message, ignore");
				count += (i >= j ? 1 : 0);
				assertEquals("Global level " + levels[i] + ", message level "
						+ levels[j], count, lc.count);
			}
		}
		Log.removeListener(lc);
	}

	/**
	 * Internal helper to count the messages passed on by the logging system.
	 */
	private static class LogCounter implements LogListener {

		public int count = 0;

		public void log(LogLevel level, Object message) {
			++count;
		}

	}

}
