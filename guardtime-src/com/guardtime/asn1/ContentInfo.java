/*
 * $Id: ContentInfo.java 268 2012-08-27 18:31:08Z ahto.truu $
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



/**
 * <a target="_blank" href="http://www.ietf.org/rfc/rfc2630.txt">RFC 2630</a> structure
 * {@code ContentInfo}.
 * <p>
 * Timestamp token created by timestamping service is internally a content info
 * structure.
 *
 * <pre>
 * ContentInfo ::= SEQUENCE {
 *    contentType     OBJECT IDENTIFIER,
 *    content [0]     EXPLICIT ANY DEFINED BY contentType
 * }
 * </pre>
 *
 * @since 0.4
 */
public final class ContentInfo
extends Asn1Wrapper {
	public static final String CONTENT_TYPE = "1.2.840.113549.1.7.2";

	private org.bouncycastle.asn1.cms.ContentInfo contentInfo;
	private String contentType;
	private SignedData content;



	/**
	 * Parses a DER-encoded {@code ContentInfo} out from the given input stream.
	 *
	 * @param in
	 *            the input stream to read data from.
	 * @return the {@code ContentInfo} object.
	 * @throws Asn1FormatException
	 *             if the data read from {@code in} does not represent a valid
	 *             {@code ContentInfo} object.
	 * @throws IOException
	 *             if {@code in} throws one.
	 */
	public static ContentInfo getInstance(InputStream in)
	throws Asn1FormatException, IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid input stream: null");
		}

		try {
			ASN1Object obj = new ASN1InputStream(in).readObject();
			return new ContentInfo(obj);
		} catch (IOException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("content info has invalid format", e);
			} else {
				throw e;
			}
		} catch (IllegalArgumentException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("content info has invalid format", e);
			} else {
				throw e;
			}
		}
	}



	/**
	 * Returns the DER representation of the {@code ContentInfo}.
	 *
	 * @return a DER byte array, or {@code null} on error.
	 */
	public byte[] getDerEncoded() {
		try {
			return contentInfo.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			return null;
		}
	}



	/**
	 * Returns the identifier of the type of the embedded content.
	 * <p>
	 * It must be equal to {@link #CONTENT_TYPE} for timestamps.
	 *
	 * @return the content type OID.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the actual content embedded in this object.
	 * <p>
	 * This must be a {@code SignedData} object for timestamps.
	 *
	 * @return the content.
	 */
	public SignedData getContent() {
		return content;
	}



	/**
	 * Creates extended timestamp from this timestamp using data from the
	 * given certification token.
	 *
	 * @see CertToken
	 *
	 * @param certToken
	 *            the {@code CertToken} from the GuardTime online verification
	 *            service.
	 * @return a new updated (extended) timestamp.
	 */
	public ContentInfo extend(CertToken certToken)
	throws Asn1FormatException {
		if (certToken == null) {
			throw new IllegalArgumentException("invalid cert token: null");
		}

		return new ContentInfo(Asn1Util.extend(contentInfo, certToken.getAsn1Token()));
	}

	/**
	 * Checks whether the timestamp is extended.
	 * <p>
	 * An extended timestamp is traceable to a control publication without any
	 * extra information. An unextended timestamp needs additional information
	 * from the online verification service.
	 *
	 * @see CertToken
	 *
	 * @return {@code true} if the timestamp is extended, {@code false}
	 *         otherwise.
	 */
	public boolean isExtended() {
		return content.isExtended();
	}



	/**
	 * Class constructor.
	 *
	 * @param obj ASN.1 representation of content info.
	 *
	 * @throws Asn1FormatException if provided ASN.1 object has invalid format.
	 */
	ContentInfo(ASN1Encodable obj)
	throws Asn1FormatException {
		try {
			contentInfo = org.bouncycastle.asn1.cms.ContentInfo.getInstance(obj);

			contentType = contentInfo.getContentType().toString();
			if (!contentType.equals(CONTENT_TYPE)) {
				throw new Asn1FormatException("invalid content type: " + contentType);
			}

			content = new SignedData(contentInfo.getContent());
		} catch (Asn1FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new Asn1FormatException("content info has invalid format", e);
		}
	}
}
