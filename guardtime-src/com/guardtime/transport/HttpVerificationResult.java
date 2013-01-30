/*
 * $Id: HttpVerificationResult.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package com.guardtime.transport;

import com.guardtime.tsp.GTVerificationResult;
import com.guardtime.tsp.VerificationResult;



/**
 * HTTP verification result.
 * <p>
 * Contains timestamp verification result (see {@link #getGtResult()}) as well
 * as service status and error codes.
 * <p>
 * HTTP verification consists of multiple steps involving parsing timestamp
 * structure, connecting to verification service for possible extension
 * attempts, etc. To distinguish timestamp-related verification errors from
 * service-related ones, actual timestamp verification result is wrapped with
 * this HTTP verification result.
 *
 * @see com.guardtime.tsp.VerificationResult
 * @see com.guardtime.tsp.GTVerificationResult
 *
 * @since 0.4
 */
public class HttpVerificationResult
extends VerificationResult {
	/*
	 * Status codes
	 */



	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = granted (0)}.
	 */
	public static final int TIMESTAMP_GRANTED = 1;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = grantedWithMods (1)}.
	 */
	public static final int TIMESTAMP_GRANTED_WITH_MODS = 2;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = rejection (2)}.
	 */
	public static final int TIMESTAMP_REJECTED = 4;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = waiting (3)}.
	 */
	public static final int TIMESTAMP_WAITING = 8;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = revocationWarning (4)}.
	 */
	public static final int REVOCATION_WARNING = 16;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo status = revocationNotification (5)}.
	 */
	public static final int REVOCATION_NOTIFICATION = 32;

	/**
	 * Timestamp extension was attempted during verification.
	 */
	public static final int EXTENSION_ATTEMPTED = 128;

	/**
	 * Timestamp was successfully extended during verification.
	 */
	public static final int TIMESTAMP_EXTENDED = 256;



	/*
	 * Error codes
	 */



	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = badAlg (0)}.
	 */
	public static final int INVALID_ALGORITHM_FAILURE = 1;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = badRequest (2)}.
	 */
	public static final int INVALID_REQUEST_FAILURE = 2;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = badDataFormat (5)}.
	 */
	public static final int INVALID_DATA_FORMAT_FAILURE = 4;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = timeNotAvailalble (14)}.
	 */
	public static final int TIME_NOT_AVAILBLE_FAILURE = 8;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = unacceptedPolicy (15)}.
	 */
	public static final int UNACCEPTED_POLICY_FAILURE = 16;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = unacceptedExtension (16)}.
	 */
	public static final int UNACCEPTED_EXTENSION_FAILURE = 32;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = addInfoNotAvailalble (17)}.
	 */
	public static final int ADDITIONAL_INFO_NOT_AVAILABLE_FAILURE = 64;

	/**
	 * Reserved as alias for
	 * <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
	 * {@code PKIStatusInfo fail = systemFailure (25)}.
	 */
	public static final int SYSTEM_FAILURE = 128;

	/**
	 * Timestamp is too new to be extended.
	 */
	public static final int TIMESTAMP_TOO_NEW_FAILURE = 256;

	/**
	 * Timestamp is too old to be extended.
	 */
	public static final int TIMESTAMP_TOO_OLD_FAILURE = 512;

	/**
	 * Service response has invalid format.
	 */
	public static final int RESPONSE_FORMAT_FAILURE = 2048;

	/**
	 * Service is unreachable, possibly network error or malformed URL.
	 */
	public static final int SERVICE_UNREACHABLE_FAILURE = 8192;



	private GTVerificationResult gtResult;



	/**
	 * Returns timestamp verification result.
	 * <p>
	 * Will be {@code null} if no timestamp verification was performed.
	 *
	 * @return timestamp verification result.
	 */
	public GTVerificationResult getGtResult() {
		return gtResult;
	}

	/**
	 * Sets timestamp verification result.
	 *
	 * @param gtResult
	 */
	void setGtResult(GTVerificationResult gtResult) {
		this.gtResult = gtResult;
	}

	/**
	 * Checks if this verification result is valid.
	 * <p>
	 * HTTP verification result is valid only if no critical errors occurred
	 * during both HTTP verification and timestamp verification.
	 * <p>
	 * {@link #TIMESTAMP_TOO_NEW_FAILURE}, {@link #TIMESTAMP_TOO_OLD_FAILURE}
	 * and {@value #SERVICE_UNREACHABLE_FAILURE} are not considered critical.
	 * If any of them occurs, verification will just go on ignoring the HTTP
	 * verification.
	 */
	public boolean isValid() {
		int errorCode = super.getErrorCode();
		if (errorCode == NO_FAILURES ||
				errorCode == TIMESTAMP_TOO_NEW_FAILURE ||
				errorCode == TIMESTAMP_TOO_OLD_FAILURE ||
				errorCode == SERVICE_UNREACHABLE_FAILURE) {
			return ((gtResult == null) ? false : gtResult.isValid());
		} else {
			return false;
		}
	}



	/**
	 * Class constructor.
	 * <p>
	 * Creates a new instance of HTTP verification result with default status
	 * and error codes.
	 */
	HttpVerificationResult() {
		super();
		gtResult = null;
	}

	/**
	 * Class constructor.
	 * <p>
	 * Creates a new instance of HTTP verification result with these status and
	 * error codes set.
	 *
	 * @param statusCode status code.
	 * @param errroCode error code.
	 */
	HttpVerificationResult(int statusCode, int errorCode) {
		super(statusCode, errorCode);
		gtResult = null;
	}
}
