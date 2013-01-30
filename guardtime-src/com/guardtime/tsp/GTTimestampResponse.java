/*
 * $Id: GTTimestampResponse.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.TimestampResponse;



/**
 * Timestamping service response wrapper.
 * <p>
 * Timestamping service response is what the <acronym title="Timestamping
 * Authority">TSA</acronym> sends in response to timestamping request. This
 * response contains status and error codes, and may contain timestamp if that
 * was created.
 * <p>
 * To retrieve created timestamp, if any, use {@link #getTimestamp()} method.
 *
 * @since 0.4
 */
public class GTTimestampResponse
extends Response {
	private GTTimestamp timestamp;



	/**
	 * Creates a new timestamp response object from the given byte array containing
	 * DER-encoded timestamping service response.
	 *
	 * @param b DER-encoded timestamping service response.
	 *
	 * @return timestamp response object.
	 *
	 * @throws GTException if service response has invalid format.
	 */
	public static GTTimestampResponse getInstance(byte[] b)
	throws GTException {
		if (b == null) {
			throw new IllegalArgumentException("invalid response data: null");
		}

		return getInstance(b, 0, b.length);
	}

	/**
	 * Creates a new timestamp response object from part of the given byte array
	 * containing DER-encoded timestamping service response.
	 *
	 * @param b byte array containing DER-encoded timestamping service response.
	 * @param offset offset to start reading service response from.
	 * @param length service response length.
	 *
	 * @return timestamp response object.
	 *
	 * @throws GTException if service response has invalid format.
	 */
	public static GTTimestampResponse getInstance(byte[] b, int offset, int length)
	throws GTException {
		if (b == null) {
			throw new IllegalArgumentException("invalid response data: null");
		}

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b, offset, length);
			return getInstance(in);
		} catch (IOException e) {
			throw new GTException("response has invalid format", e);
		}
	}

	/**
	 * Reads a new timestamp response object from the given input stream.
	 *
	 * @param in input stream containing DER-encoded timestamping service
	 * 			response.
	 *
	 * @return timestamp response object.
	 *
	 * @throws GTException if service response has invalid format.
	 * @throws IOException if stream reading error occurs.
	 */
	public static GTTimestampResponse getInstance(InputStream in)
	throws IOException, GTException {
		if (in == null) {
			throw new IllegalArgumentException("invalid response stream: null");
		}

		try {
			TimestampResponse response = TimestampResponse.getInstance(in);
			return new GTTimestampResponse(response);
		} catch (Asn1FormatException e) {
			throw new GTException("response has invalid format", e);
		}
	}



	/**
	 * Returns timestamp, if any, contained in this reponse.
	 * <p>
	 * As stated in <a href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>,
	 * timestamping service response will contain the created timestamp only if
	 * response status is either {@code 0} or {@code 1}.
	 * <p>
	 * If response contains no timestamp, this method will return {@code null}.
	 *
	 * @return created timestamp, or {@code null}.
	 */
	public GTTimestamp getTimestamp() {
		return timestamp;
	}



	/**
	 * Class constructor.
	 * <p>
	 * Called by {@code getInstance} methods.
	 *
	 * @throws GTException if response internal structure has invalid format.
	 */
	private GTTimestampResponse(TimestampResponse response)
	throws GTException {
		super(response.getStatusInfo().getStatusCode(), response.getStatusInfo().getFailCode());

		int statusCode = getStatusCode();
		if (statusCode == 0 || statusCode == 1) {
			timestamp = new GTTimestamp(response.getToken());
		}
	}
}
