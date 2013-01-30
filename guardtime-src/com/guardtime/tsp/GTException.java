/*
 * $Id: GTException.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package com.guardtime.tsp;



/**
 * Exception thrown by timestamp handlers.
 *
 * @since 0.2
 */
public class GTException
extends Exception {
	private static final long serialVersionUID = 1L;



	/**
	 * Constructs a new exception with no message and no cause.
	 */
	public GTException() {
		super();
	}

	/**
	 * Constructs a new exception with the given message and no cause.
	 *
	 * @param message
	 *            explains the reason for the exception to be thrown.
	 */
	public GTException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the given message and given cause.
	 *
	 * @param message
	 *            explains the reason for the exception to be thrown.
	 * @param cause
	 *            specifies the underlying cause for the exception to be thrown.
	 */
	public GTException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with no message and the given cause.
	 *
	 * @param cause
	 *            specifies the underlying cause for the exception to be thrown.
	 */
	public GTException(Throwable cause) {
		super(cause);
	}
}
