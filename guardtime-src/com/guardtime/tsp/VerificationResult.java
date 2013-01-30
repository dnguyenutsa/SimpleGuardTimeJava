/*
 * $Id: VerificationResult.java 169 2011-03-03 18:45:00Z ahto.truu $
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
 * Generic verification result.
 * <p>
 * All verification result handlers should extend this class. All numerical code
 * handling is implemented in this class, so the child classes should only
 * define their own status and error codes. Bit flag method is used for both
 * status and error codes, all defined values should be powers of 2 in the range
 * 1..1073741824 (0 is used already).
 * <p>
 * Verification result has two main properties: status code and error code.
 * Status code is ought to show verification status, e.g. which checks were
 * run and which were not. Error code represents errors occurred while
 * performing verification.
 * <p>
 * To get current status or error bitmap, use {@link #getErrorCode()} or
 * {@link #getStatusCode()} methods, respectively. To simply check if this
 * verification result contains any errors, use {@link #isValid()} method.
 * <p>
 * Verification result can contain multiple status and error codes. To check
 * if some particular error or status flag was raised, use
 * {@link #hasStatus(int)} or {@link #hasError(int)} methods.
 * <p>
 * To update current result with some status or error code, use
 * {@link #updateStatus(int)} or {@link #updateErrors(int)} methods.
 *
 * @since 0.4
 */
public abstract class VerificationResult {
	/**
	 * No checks were run.
	 */
	public static final int NO_CHECKS = 0;

	/**
	 * Verification completed successfully (no errors occurred).
	 */
	public static final int NO_FAILURES = 0;



	private int errorCode;
	private int statusCode;



	/**
	 * Class constructor.
	 * <p>
	 * Creates a new instance of verification result with default status
	 * ({@link #NO_CHECKS}) and error ({@link #NO_FAILURES}) codes.
	 */
	public VerificationResult() {
		this(NO_CHECKS, NO_FAILURES);
	}

	/**
	 * Class constructor.
	 * <p>
	 * Creates a new instance of verification result with these status and
	 * error codes set.
	 *
	 * @param statusCode status code.
	 * @param errroCode error code.
	 */
	public VerificationResult(int statusCode, int errroCode) {
		this.statusCode = statusCode;
		this.errorCode = errroCode;
	}



	/**
	 * Returns current status code.
	 *
	 * @return status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns current error code.
	 *
	 * @return error code.
	 */
	public int getErrorCode() {
		return errorCode;
	}



	/**
	 * Checks if the given status flag was raised during verification.
	 *
	 * @param statusCode status code.
	 *
	 * @return {@code true} if check was run, {@code false} otherwise.
	 */
	public boolean hasStatus(int statusCode) {
		return ((statusCode & this.statusCode) > 0);
	}

	/**
	 * Checks if the given error occurred during verification.
	 *
	 * @param errorCode error code.
	 *
	 * @return {@code true} if error occurred, {@code false} otherwise.
	 */
	public boolean hasError(int errorCode) {
		return ((errorCode & this.errorCode) > 0);
	}

	/**
	 * Checks if this verification result is valid.
	 * <p>
	 * Verification result is valid only if no errors occurred during
	 * verification.
	 *
	 * @return {@code true} if verification result is valid, {@code false}
	 * 			otherwise.
	 */
	public boolean isValid() {
		return (errorCode == NO_FAILURES);
	}



	/**
	 * Updates status of this verification result with the given status code.
	 *
	 * @param statusCode status code.
	 */
	public void updateStatus(int statusCode) {
		this.statusCode |= statusCode;
	}

	/**
	 * Updates errors in this verification result with the given error code.
	 *
	 * @param errorCode error code.
	 */
	public void updateErrors(int errorCode) {
		this.errorCode |= errorCode;
	}
}
