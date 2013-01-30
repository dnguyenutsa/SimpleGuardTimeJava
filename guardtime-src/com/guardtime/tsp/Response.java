/*
 * $Id: Response.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import com.guardtime.asn1.StatusInfo;



/**
 * Generic service response.
 * <p>
 * All service response handlers should extend this class.
 * <p>
 * Status and fail codes are defined in <a target="_blank"
 * href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>. This class puts no
 * restrictions on status and fail codes, so its child classes should check them
 * if necessary.
 *
 * @since 0.4
 */
abstract class Response {
	private int statusCode;
	private int failCode;



	/**
	 * Class constructor.
	 * <p>
	 * Creates a new instance of service response with the given status and fail
	 * codes.
	 *
	 * @param statusCode status code.
	 * @param failCode fail code.
	 */
	Response(int statusCode, int failCode) {
		this.statusCode = statusCode;
		this.failCode = failCode;
	}



	/**
	 * Returns status code as received from service.
	 *
	 * @return status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns status message corresponding to current status code.
	 *
	 * @return status message.
	 */
	public String getStatusMessage() {
		return StatusInfo.getStatusMessage(statusCode);
	}



	/**
	 * Returns fail code as received from service.
	 *
	 * @return fail code.
	 */
	public int getFailCode() {
		return failCode;
	}

	/**
	 * Returns fail message corresponding to current fail code.
	 *
	 * @return fail message.
	 */
	public String getFailMessage() {
		return StatusInfo.getFailMessage(failCode);
	}
}
