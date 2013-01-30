/*
 * $Id: LogListener.java 169 2011-03-03 18:45:00Z ahto.truu $
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

/**
 * This is the interface all loggers are expected to implement.
 *
 * @since 0.4
 */
public interface LogListener {

	/**
	 * This method is called whenever the system has generated a message the
	 * logger should be interested in.
	 * <p>
	 * Note: Implementations of this method should never throw exceptions.
	 *
	 * @param level
	 *            the importance level of the message. This is never
	 *            {@code null}.
	 * @param message
	 *            the message. This may be {@code null}.
	 */
	public void log(LogLevel level, Object message);

}
