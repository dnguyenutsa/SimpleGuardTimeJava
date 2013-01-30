/*
 * $Id: StatusInfo.java 268 2012-08-27 18:31:08Z ahto.truu $
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
package com.guardtime.asn1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.cmp.PKIFreeText;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;



/**
 * <a target="_blank" href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * structure {@code PKIStatusInfo}.
 * <p>
 * Represents service status in service response; contains status and error
 * codes.
 *
 * <pre>
 * PKIStatusInfo ::= SEQUENCE {
 *    status        PKIStatus,
 *    statusString  PKIFreeText OPTIONAL,
 *    failInfo      PKIFailureInfo OPTIONAL
 * }
 * </pre>
 *
 * @since 0.4
 */
public final class StatusInfo
extends Asn1Wrapper {
	private static final int[] allowedFailCodes = { -1, 0, 2, 5, 14, 15, 16, 17, 25, 100, 101 };

	private PKIStatusInfo statusInfo;
	private int statusCode;
	private List statusText;
	private int failCode;



	/**
	 * Returns description of the given status code.
	 *
	 * @param statusCode status code.
	 *
	 * @return status message describing the status code.
	 *
	 * @see #getStatusCode()
	 */
	public static String getStatusMessage(int statusCode) {
		if (statusCode == 0) {
			return "granted";
		} else if (statusCode == 1) {
			return "granted with modifications";
		} else if (statusCode == 2) {
			return "rejected";
		} else if (statusCode == 3) {
			return "waiting";
		} else if (statusCode == 4) {
			return "revocation warning";
		} else if (statusCode == 5) {
			return "revocation notification";
		} else {
			return "invalid status code: " + statusCode;
		}
	}

	/**
	 * Returns description of the given fail code.
	 *
	 * @param failCode fail code.
	 *
	 * @return fail message describing the fail code.
	 *
	 * @see #getFailCode()
	 */
	public static String getFailMessage(int failCode) {
		if (failCode == -1) {
			return "";
		} else if (failCode == 0) {
			return "bad algorithm";
		} else if (failCode == 2) {
			return "bad request";
		} else if (failCode == 5) {
			return "bad data format";
		} else if (failCode == 14) {
			return "time not available";
		} else if (failCode == 15) {
			return "unaccepted policy";
		} else if (failCode == 16) {
			return "unaccepted extension";
		} else if (failCode == 17) {
			return "additional information not available";
		} else if (failCode == 25) {
			return "system failure";
		} else if (failCode == 100) { // Defined by GT
			return "timestamp too new to be extended";
		} else if (failCode == 101) { // Defined by GT
			return "timestamp too old to be extended";
		} else {
			return "invalid fail code: " + failCode;
		}
	}



	/**
	 * Returns the DER representation of the {@code StatusInfo}.
	 *
	 * @return a DER byte array, or {@code null} on error.
	 */
	public byte[] getDerEncoded() {
		try {
			return statusInfo.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			return null;
		}
	}



	/**
	 * Returns status code as defined in
	 * <a target="_blank" href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>.
	 *
	 * <pre>
	 * PKIStatus ::= INTEGER {
	 *    granted                (0),
	 *    -- when the PKIStatus contains the value zero a TimeStampToken, as
	 *    -- requested, is present.
	 *    grantedWithMods        (1),
	 *    -- when the PKIStatus contains the value one a TimeStampToken,
	 *    -- with modifications, is present.
	 *    rejection              (2),
	 *    waiting                (3),
	 *    revocationWarning      (4),
	 *    -- this message contains a warning that a revocation is
	 *    -- imminent
	 *    revocationNotification (5)
	 *    -- notification that a revocation has occurred
	 * }
	 * </pre>
	 *
	 * If status code is neither {@code 0} nor {@code 1}, fail code will
	 * describe the failure more precisely.
	 *
	 * @return status code.
	 *
	 * @see #getStatusMessage(int)
	 * @see #getFailCode()
	 */
	public int getStatusCode() {
		return statusCode;
	}

	public List getStatusText() {
		return ((statusText == null) ? null : Collections.unmodifiableList(statusText));
	}

	/**
	 * Returns error code as defined in
	 * <a target="_blank" href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>.
	 *
	 * <pre>
	 * PKIFailureInfo ::= BIT STRING {
	 *    badAlg               (0),
	 *    -- unrecognized or unsupported Algorithm Identifier
	 *    badRequest           (2),
	 *    -- transaction not permitted or supported
	 *    badDataFormat        (5),
	 *    -- the data submitted has the wrong format
	 *    timeNotAvailable    (14),
	 *    -- the TSA's time source is not available
	 *    unacceptedPolicy    (15),
	 *    -- the requested TSA policy is not supported by the TSA.
	 *    unacceptedExtension (16),
	 *    -- the requested extension is not supported by the TSA.
	 *    addInfoNotAvailable (17),
	 *    -- the additional information requested could not be understood
	 *    -- or is not available
	 *    systemFailure       (25),
	 *    -- the request cannot be handled due to system failure
	 *    timestampTooNew    (100),
	 *    -- (Defined by GT) timestamp too new to be extended
	 *    timestampTooOld    (101)
	 *    -- (Defined by GT) timestamp too old to be extended
	 * }
	 * </pre>
	 *
	 * If status code is either {@code 0} or {@code 1}, fail code {@code -1}
	 * will be returned.
	 *
	 * @return fail code.
	 *
	 * @see #getFailMessage(int)
	 * @see #getStatusCode()
	 *
	 */
	public int getFailCode() {
		return failCode;
	}



	/**
	 * Class constructor.
	 *
	 * @param obj DER-encoded status info object.
	 *
	 * @throws Asn1FormatException if status info object has invalid format.
	 */
	StatusInfo(ASN1Encodable obj)
	throws Asn1FormatException {
		try {
			statusInfo = PKIStatusInfo.getInstance(obj);

			statusCode = statusInfo.getStatus().intValue();
			// RFC 3161:
			//
			// Compliant servers SHOULD NOT produce any other (than 0..5)
			// values. Compliant clients MUST generate an error
			// if values it does not understand are present.
			if (statusCode < 0 || statusCode > 5) {
				throw new Asn1FormatException("invalid status: " + statusCode);
			}

			PKIFreeText freeText = statusInfo.getStatusString();
			if (freeText != null) {
				int freeTextSize = freeText.size();
				// PKIFreeText ::= SEQUENCE SIZE (1..MAX) OF UTF8String
				if (freeTextSize < 1) {
					throw new Asn1FormatException("zero-length status string not allowed");
				}
				statusText = new ArrayList();
				for (int i = 0; i < freeTextSize; i++) {
					statusText.add(freeText.getStringAt(i).getString());
				}
			}

			// -1 means that status code is not set
			failCode = -1;
			DERBitString bitString = statusInfo.getFailInfo();
			if (bitString != null) {
				byte[] failBytes = bitString.getBytes();
				int len = failBytes.length * 8;
				for (int i = 0; i < len; i++) {
					// return only the first error encountered
					if ((failBytes[i >> 3] & (0x80 >> (i & 7))) != 0) {
						failCode = i;
						break;
					}
				}

				// Check that received fail code is valid
				boolean isValidFailCode = false;
				for (int i = 0; i < allowedFailCodes.length; i++) {
					if (failCode == allowedFailCodes[i]) {
						isValidFailCode = true;
						break;
					}
				}
				if (!isValidFailCode) {
					throw new Asn1FormatException("invalid fail info: " + failCode);
				}
			}
		} catch (Asn1FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new Asn1FormatException("status info has invalid format", e);
		}
	}
}
