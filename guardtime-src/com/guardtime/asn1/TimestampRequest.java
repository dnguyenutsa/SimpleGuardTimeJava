/*
 * $Id: TimestampRequest.java 268 2012-08-27 18:31:08Z ahto.truu $
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

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;



/**
 * <a target="_blank" href="http://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * structure {@code TimeStampReq}.
 * <p>
 * Timestamp request is sent to timestamping service to create timestamp for
 * data hash contained in this request.
 *
 * <pre>
 * TimeStampReq ::= SEQUENCE  {
 *    version          INTEGER  { v1(1) },
 *    messageImprint   MessageImprint,
 *    reqPolicy        TSAPolicyId OPTIONAL,
 *    nonce            INTEGER OPTIONAL,
 *    certReq          BOOLEAN DEFAULT FALSE,
 *    extensions       [0] IMPLICIT Extensions OPTIONAL
 * }
 * </pre>
 *
 * @see MessageImprint
 *
 * @since 0.4
 */
public final class TimestampRequest
extends Asn1Wrapper {
	private TimeStampReq request;


	/**
	 * Composes a new {@code TimeStampReq} structure containing the given
	 * hash value and no optional fields.
	 *
	 * @param algOid
	 *            identifier of the hash algorithm that was used to hash the
	 *            data.
	 * @param hashedMessage
	 *            hash value of the data to be timestamped.
	 * @return a new timestamp request.
	 */
	public static TimestampRequest compose(String algOid, byte[] hashedMessage) {
		if (algOid == null) {
			throw new IllegalArgumentException("invalid hash algorithm: null");
		} else if (hashedMessage == null) {
			throw new IllegalArgumentException("invalid hashed message: null");
		}

		AlgorithmIdentifier algId = new AlgorithmIdentifier(new ASN1ObjectIdentifier(algOid));
		MessageImprint mi = new MessageImprint(algId, hashedMessage);
		TimeStampReq req = new TimeStampReq(mi, null, null, null, null);
		return new TimestampRequest(req);
	}



	/**
	 * Returns the DER representation of the {@code TimeStampReq}.
	 *
	 * @return a DER byte array, or {@code null} on error.
	 */
	public byte[] getDerEncoded() {
		try {
			return request.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			return null;
		}
	}



	/**
	 * Class constructor.
	 *
	 * @param request ASN.1 representation of timestamp request.
	 */
	TimestampRequest(TimeStampReq request) {
		this.request = request;
	}
}
