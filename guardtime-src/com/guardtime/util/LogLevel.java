/*
 * $Id: LogLevel.java 169 2011-03-03 18:45:00Z ahto.truu $
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
 * This class follows the "Typesafe Enum" pattern to list the importance levels
 * of log messages. The levels, ordered from the most important to the least
 * important, are {@link #Fatal}, {@link #Error}, {@link #Warning}, and
 * {@link #Debug}. In case you need, this list is available from
 * {@link #getLevels()}.
 * <p>
 * The same levels are used by loggers to ask to be notified only about messages
 * that are "important enough". A logger that signs up for a particular level
 * will only get the messages whose importance level is equal to or exceeds the
 * level for which the logger signed up. Additionally, {@link #All} is available
 * to specify that a logger wants all the messages. Technically, the result is
 * the same as if the logger had signed up for {@code Debug}, but it should be
 * more obvious to the reader and also will continue to work in case another
 * level below {@code Debug} should be added in the future.
 * <p>
 * The same levels are used for global filtering. Additionally, {@link #None} is
 * available to specify that all logging should be turned off.
 * <p>
 * In case you need, the list with one or both of the special values is
 * available from {@link #getLevels(boolean, boolean)}.
 *
 * @since 0.4
 */
public final class LogLevel implements Comparable {

	/**
	 * Indicates a level above all others. Should only be used with
	 * {@link Log#setLevel} to turn off all logging.
	 */
	public static final LogLevel None = new LogLevel(5, "None");

	/**
	 * Indicates a critical error that will cause the system to abort.
	 */
	public static final LogLevel Fatal = new LogLevel(4, "Fatal");

	/**
	 * Indicates an error that will cause the current operation to abort, but
	 * the system in general remain operational.
	 */
	public static final LogLevel Error = new LogLevel(3, "Error");

	/**
	 * Indicates an unexpected condition that the system is able to cope with
	 * and proceed with the current operation.
	 */
	public static final LogLevel Warning = new LogLevel(2, "Warning");

	/**
	 * Indicates a debug or trace message that is most likely to be filtered out
	 * in a normal production operation of the system.
	 */
	public static final LogLevel Debug = new LogLevel(1, "Debug");

	/**
	 * Indicates a level below all others. Should only be used with
	 * {@link Log#setLevel} to turn off all filtering and with
	 * {@link Log#addListener} to sign up for all messages.
	 */
	public static final LogLevel All = new LogLevel(0, "All");

	/**
	 * Returns all the message levels, in the order from the most important to
	 * the least important.
	 *
	 * @return a newly allocated and populated array.
	 */
	public static LogLevel[] getLevels() {
		return getLevels(false, false);
	}

	/**
	 * Returns all the message levels, in the order from the most important to
	 * the least important.
	 *
	 * @param withNone
	 *            if {@code true}, the list will include {@code None} in
	 *            addition to the "normal" levels.
	 * @param withAll
	 *            if {@code true}, the list will include {@code All} in
	 *            addition to the "normal" levels.
	 * @return a newly allocated and populated array.
	 */
	public static LogLevel[] getLevels(boolean withNone, boolean withAll) {
		if (withNone) {
			if (withAll) {
				return new LogLevel[] { None, Fatal, Error, Warning, Debug, All };
			} else {
				return new LogLevel[] { None, Fatal, Error, Warning, Debug };
			}
		} else {
			if (withAll) {
				return new LogLevel[] { Fatal, Error, Warning, Debug, All };
			} else {
				return new LogLevel[] { Fatal, Error, Warning, Debug };
			}
		}
	}

	/**
	 * A numeric value for the level.
	 */
	private final int level;

	/**
	 * A readable name for the level.
	 */
	private final String name;

	/**
	 * Constructs a new level object. The constructor is private to avoid
	 * accidental creation of additional objects outside of this class.
	 *
	 * @param level
	 *            a numeric value for the new level. Larger values mean more
	 *            critical errors.
	 * @param name
	 *            a readable name for the new level.
	 */
	private LogLevel(int level, String name) {
		this.level = level;
		this.name = name;
	}

	/**
	 * Compares this level to the other level.
	 *
	 * @param that
	 *            the other level to compare to.
	 * @return a negative integer, zero, or a positive integer as this level is
	 *         lower than, same, or higher than the other level.
	 * @throws NullPointerException
	 *             when {@code that} is {@code null}.
	 * @throws ClassCastException
	 *             when {@code that} is not a {@code LogLevel}.
	 */
	public int compareTo(Object that) {
		return level - ((LogLevel) that).level;
	}

	/**
	 * Compares this level to the other level.
	 *
	 * @param that
	 *            the other level to compare to.
	 * @return {@code true} if {@code this} is equal to {@code that};
	 *         {@code false} otherwise.
	 */
	public boolean equals(Object that) {
		return (that instanceof LogLevel) && (level == ((LogLevel) that).level);
	}

	/**
	 * Returns a hash code value for the level object. This method is overridden
	 * to honor the contract of the {@link #equals} method.
	 *
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return level;
	}

	/**
	 * Converts the object into a human-readable textual representation.
	 *
	 * @return the readable name for the object.
	 */
	public String toString() {
		return name;
	}

}
