/*
 * $Id: TimestampResponse.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.io.InputStream;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.tsp.TimeStampResp;



/**
 * <a target="_blank" href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * structure {@code TimeStampResp}.
 * <p>
 * Timestamp response is a response from timestamping service containing status info
 * and (if the processing succeeded) a timestamp.
 * <p>
 * Timestamp is internally a {@code ContentInfo} structure.
 *
 * <pre>
 * TimeStampResp ::= SEQUENCE {
 *    status          PKIStatusInfo,
 *    timeStampToken  TimeStampToken OPTIONAL
 * }
 * </pre>
 *
 * @see StatusInfo
 * @see ContentInfo
 *
 * @since 0.4
 */
public final class TimestampResponse
extends Asn1Wrapper {
	private TimeStampResp response;
	private StatusInfo statusInfo;
	private ContentInfo token;



	/**
	 * Parses a DER-encoded {@code TimeStampResp} out from the given input
	 * stream.
	 *
	 * @param in
	 *            the input stream to read data from.
	 * @return the {@code TimeStampResp} object.
	 * @throws Asn1FormatException
	 *             if the data read from {@code in} does not represent a valid
	 *             {@code TimeStampResp} object.
	 * @throws IOException
	 *             if {@code in} throws one.
	 */
	public static TimestampResponse getInstance(InputStream in)
	throws Asn1FormatException, IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid input stream: null");
		}

		try {
			ASN1Object obj = new ASN1InputStream(in).readObject();
			return new TimestampResponse(obj);
		} catch (IOException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("timestamp response has invalid format", e);
			} else {
				throw e;
			}
		} catch (IllegalArgumentException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("timestamp response has invalid format", e);
			} else {
				throw e;
			}
		}
	}



	/**
	 * Returns the DER representation of the {@code TimeStampResp}.
	 *
	 * @return a DER byte array, or {@code null} on error.
	 */
	public byte[] getDerEncoded() {
		try {
			return response.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			return null;
		}
	}



	/**
	 * Returns the {@code StatusInfo} object contained in this response.
	 *
	 * @return the {@code StatusInfo} object.
	 */
	public StatusInfo getStatusInfo() {
		return statusInfo;
	}

	/**
	 * Returns the {@code ContentInfo} object contained in this response.
	 *
	 * @return the {@code ContentInfo} object embedding a timestamp, or {@code null}.
	 */
	public ContentInfo getToken() {
		return token;
	}



	/**
	 * Class constructor.
	 *
	 * @param obj ASN.1 representation of timestamp response.
	 */
	TimestampResponse(ASN1Encodable obj)
	throws Asn1FormatException {
		try {
			response = TimeStampResp.getInstance(obj);

			statusInfo = new StatusInfo(response.getStatus());

			int statusCode = statusInfo.getStatusCode();
			org.bouncycastle.asn1.cms.ContentInfo ci = response.getTimeStampToken();
			// RFC 3161:
			//
			// When the status contains the value zero or one, a TimeStampToken
			// MUST be present.  When status contains a value other than zero
			// or one, a TimeStampToken MUST NOT be present.
			//
			// When the PKIStatus contains the value zero a TimeStampToken, as
			// requested, is present.
			//
			// When the PKIStatus contains the value one a TimeStampToken,
			// with modifications, is present.
			if (statusCode == 0 || statusCode == 1) {
				if (ci == null) {
					throw new Asn1FormatException("timestamp token is missing in timestamp response");
				}
				token = new ContentInfo(ci);
			} else if (statusCode >= 2 && statusCode <= 5) {
				if (ci != null) {
					throw new Asn1FormatException("unexpected timestamp token in timestamp response");
				}
				token = null;
			} else {
				throw new Asn1FormatException("invalid status code: " + statusCode);
			}
		} catch (Asn1FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new Asn1FormatException("timestamp response has invalid format", e);
		}
	}
}
