/*
 * $Id: CertTokenResponse.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;



/**
 * GuardTime structure {@code CertTokenResponse}.
 * <p>
 * Certification token response is a response from online verification service
 * containing status info and (if the processing succeeded) a certification
 * token.
 *
 * <pre>
 * CertTokenResponse ::= SEQUENCE {
 *    status      PKIStatusInfo,
 *    certToken   [0] CertToken OPTIONAL
 * }
 * </pre>
 *
 * @see StatusInfo
 * @see CertToken
 *
 * @since 0.4
 */
public final class CertTokenResponse
extends Asn1Wrapper {
	private Asn1CertTokenResponse response;
	private StatusInfo statusInfo;
	private CertToken token;



	/**
	 * Parses a DER-encoded {@code CertTokenResponse} out from the given input
	 * stream.
	 *
	 * @param in
	 *            the input stream to read data from.
	 * @return the {@code CertTokenResponse} object.
	 * @throws Asn1FormatException
	 *             if the data read from {@code in} does not represent a valid
	 *             {@code CertTokenResponse} object.
	 * @throws IOException
	 *             if {@code in} throws one.
	 */
	public static CertTokenResponse getInstance(InputStream in)
	throws Asn1FormatException, IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid input stream: null");
		}

		try {
			ASN1Object obj = new ASN1InputStream(in).readObject();
			return new CertTokenResponse(obj);
		} catch (IOException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("cert token response has invalid format", e);
			} else {
				throw e;
			}
		} catch (IllegalArgumentException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("cert token response has invalid format", e);
			} else {
				throw e;
			}
		}
	}



	/**
	 * Returns the DER representation of the {@code CertTokenResponse}.
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
	 * Returns the {@code CertToken} object contained in this response.
	 *
	 * @return the {@code CertToken} object embedding a certification token,
	 *         or {@code null}.
	 */
	public CertToken getToken() {
		return token;
	}



	/**
	 * Class constructor.
	 *
	 * @param obj ASN.1 representation of certification token response.
	 */
	CertTokenResponse(ASN1Encodable obj)
	throws Asn1FormatException {
		try {
			response = Asn1CertTokenResponse.getInstance(obj);

			statusInfo = new StatusInfo(response.getStatus());

			int statusCode = statusInfo.getStatusCode();
			ASN1Encodable ct = response.getCertToken();
			// RFC 3161 on TimeStampToken:
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
			//
			// GuardTime:
			//
			// Here we apply the same rule for CertToken.
			if (statusCode == 0 || statusCode == 1) {
				if (ct == null) {
					throw new Asn1FormatException("cert token is missing in cert token response");
				}
				token = new CertToken(ct);
			} else if (statusCode >= 2 && statusCode <= 5) {
				if (ct != null) {
					throw new Asn1FormatException("unexpected cert token in cert token response");
				}
				token = null;
			} else {
				throw new Asn1FormatException("invalid status code: " + statusCode);
			}
		} catch (Asn1FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new Asn1FormatException("cert token response has invalid format", e);
		}
	}
}



/**
 * Internal implementation class for the ASN.1 representation of
 * {@code CertTokenResponse}.
 */
class Asn1CertTokenResponse
extends ASN1Object {
	private PKIStatusInfo status;
	private Asn1CertToken certToken;



	public static Asn1CertTokenResponse getInstance(Object obj) {
		if (obj == null || obj instanceof Asn1CertTokenResponse) {
			return (Asn1CertTokenResponse) obj;
		} else if (obj instanceof ASN1Sequence) {
			return new Asn1CertTokenResponse((ASN1Sequence) obj);
		}

		throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
	}



	public Asn1CertTokenResponse(ASN1Sequence seq) {
		Enumeration en = seq.getObjects();

		// Required elements
		status = PKIStatusInfo.getInstance(en.nextElement());

		// Optional elements
		while (en.hasMoreElements()) {
			ASN1TaggedObject obj = ASN1TaggedObject.getInstance(en.nextElement());
			if (obj.getTagNo() == 0 && certToken == null) {
				certToken = Asn1CertToken.getInstance(obj, false);
			} else {
				throw new IllegalArgumentException("invalid object in factory: " + obj);
			}
		}
	}

	public Asn1CertToken getCertToken() {
		return certToken;
	}

	public PKIStatusInfo getStatus() {
		return status;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(status);
		if (certToken != null) {
			v.add(new DERTaggedObject(0, certToken));
		}
		return new DERSequence(v);
	}
}

