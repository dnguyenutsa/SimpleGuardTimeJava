/*
 * $Id: InsertMode.java 169 2011-03-03 18:45:00Z ahto.truu $
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

package com.guardtime.format;

/**
 * This class follows the "Typesafe Enum" pattern to list the possible modes for
 * inserting timestamps into documents.
 *
 * @since 0.4
 */
public final class InsertMode {

	/**
	 * Indicates the insertion of a timestamp into a document that is previously
	 * not timestamped.
	 * <p>
	 * In this context it is considered an error if the document actually
	 * already contains a timestamp and an {@code IllegalStateException} should
	 * be thrown by the format provider.
	 */
	public static final InsertMode Insert = new InsertMode("Insert");

	/**
	 * Indicates the replacement of a an existing timestamp in a document with
	 * another one.
	 * <p>
	 * In this context it is considered an error if the document does not
	 * contain any timestamps and an {@code IllegalStateException} should be
	 * thrown by the format provider.
	 */
	public static final InsertMode Replace = new InsertMode("Replace");

	/**
	 * Indicates the appending of a new timestamp to a document that may already
	 * have been timestamped.
	 * <p>
	 * If the document does not contain any existing timestamps, the behavior of
	 * the provider in this mode should be the same as if the mode
	 * {@code Insert} had been specified.
	 */
	public static final InsertMode Append = new InsertMode("Append");

	/**
	 * A readable name for the mode.
	 */
	private final String name;

	/**
	 * Constructs a new mode object. The constructor is private to avoid
	 * accidental creation of additional objects outside of this class.
	 *
	 * @param name
	 *            a readable name for the new mode.
	 */
	private InsertMode(String name) {
		this.name = name;
	}

	/**
	 * Converts the object into a human-readable textual representation.
	 *
	 * @return the readable name for the object.
	 */
	public String toString() {
		return this.name;
	}

}
