/*
 * $Id: CertToken.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extensions;

import com.guardtime.util.Util;



/**
 * GuardTime structure {@code CertToken}.
 * <p>
 * Certification token contains data needed to extend a timestamp, that is, to
 * link it to a control publication.
 * <p>
 * Certification token is created by online verification service in response to
 * a certification token request.
 *
 * <pre>
 * CertToken ::= SEQUENCE {
 *    version         INTEGER { v1(1) },
 *    history         OCTET STRING,
 *    publishedData   PublishedData,
 *    pubReference    SET OF OCTET STRING
 *    extensions      [0] Extensions OPTIONAL
 * }
 * </pre>
 *
 * @see PublishedData
 *
 * @since 0.4
 */
public final class CertToken
extends Asn1Wrapper {
	public static final int VERSION = 1;

	private Asn1CertToken certToken;
	private int version;
	private byte[] history;
	private PublishedData publishedData;
	private List pubReferences;
	private byte[] extensions;



	/**
	 * Parses a DER-encoded {@code CertToken} out from the given input stream.
	 *
	 * @param in
	 *            the input stream to read data from.
	 * @return the {@code CertToken} object.
	 * @throws Asn1FormatException
	 *             if the data read from {@code in} does not represent a valid
	 *             {@code CertToken} object.
	 * @throws IOException
	 *             if {@code in} throws one.
	 */
	public static CertToken getInstance(InputStream in)
	throws Asn1FormatException, IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid input stream: null");
		}

		try {
			ASN1Object obj = new ASN1InputStream(in).readObject();
			return new CertToken(obj);
		} catch (IOException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("cert token has invalid format", e);
			} else {
				throw e;
			}
		} catch (IllegalArgumentException e) {
			if (isAsnParserException(e)) {
				throw new Asn1FormatException("cert token has invalid format", e);
			} else {
				throw e;
			}
		}
	}



	/**
	 * Returns the DER representation of the {@code CertToken}.
	 *
	 * @return a DER byte array, or {@code null} on error.
	 */
	public byte[] getDerEncoded() {
		try {
			return certToken.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			return null;
		}
	}



	/**
	 * Returns the version number of the syntax of the {@code CertToken} object.
	 * <p>
	 * The current version is {@link #VERSION}.
	 *
	 * @return the value of the {@code version} field of this {@code CertToken}
	 *         object.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Returns the history hash chain from the {@code CertToken} object.
	 * <p>
	 * This is the hash chain that connects the leaf corresponding to the
	 * {@code historyID} field in the {@link CertTokenRequest} to the root of
	 * the GuardTime calendar tree in the state the tree was at the moment
	 * corresponding to the {@code publicationID} in the {@link PublishedData}.
	 *
	 * @return the contents of the {@code history} field of this
	 *         {@code CertToken} object.
	 */
	public byte[] getHistory() {
		return Util.copyOf(history);
	}

	/**
	 * Returns the control publication data from the {@code CertToken} object.
	 * <p>
	 * This represents the contents of the control publication that can be
	 * used to provide tangible proof of integrity of the timestamp.
	 *
	 * @return the contents of the {@code publishedData} field of this
	 *         {@code CertToken} object.
	 */
	public PublishedData getPublishedData() {
		return publishedData;
	}

	/**
	 * Returns the publication references list from the {@code CertToken} object.
	 * <p>
	 * This list contains bibliographic references to the print media where the
	 * control publication represented by {@code PublishedData} was printed.
	 * <p>
	 * This list is read-only. Any attempts to modify it will result in an
	 * {@code UnsupportedOperationException}.
	 *
	 * @return the contents of the {@code pubReferences} field of this
	 *         {@code CertToken} object.
	 */
	public List getPubReferences() {
		return ((pubReferences == null) ? null : Collections.unmodifiableList(pubReferences));
	}

	/**
	 * Returns the DER representation of {@code CertToken} extensions.
	 * <p>
	 * No extensions are used by the current version of the GuardTime service.
	 *
	 * @return DER-encoded extensions.
	 */
	public byte[] getEncodedExtensions() {
		return Util.copyOf(extensions);
	}



	/**
	 * Class constructor.
	 *
	 * @param obj ASN.1 representation of certification token.
	 */
	CertToken(ASN1Encodable obj)
	throws Asn1FormatException {
		try {
			certToken = Asn1CertToken.getInstance(obj);

			version = certToken.getVersion().getValue().intValue();
			if (version != VERSION) {
				throw new Asn1FormatException("invalid cert token version: " + version);
			}

			history = certToken.getHistory().getOctets();

			publishedData = new PublishedData(certToken.getPublishedData());

			ASN1Set pubRefs = certToken.getPubReference();
			if (pubRefs != null) {
				pubReferences = new ArrayList();
				Enumeration e = pubRefs.getObjects();
				while (e.hasMoreElements()) {
					Object nextElement = e.nextElement();
					if (!(nextElement instanceof DERNull)) {
						pubReferences.add(((ASN1OctetString) nextElement).getOctets());
					}
				}
			}

			Extensions exts = certToken.getExtensions();
			if (exts != null) {
				// check for critical extensions
				Asn1Util.checkExtensions(exts);
				extensions = exts.getEncoded(ASN1Encoding.DER);
			}
		} catch (Asn1FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new Asn1FormatException("cert token has invalid format", e);
		}
	}

	Asn1CertToken getAsn1Token() {
		return certToken;
	}
}



/**
 * Internal implementation class for the ASN.1 representation of
 * {@code CertToken}.
 */
class Asn1CertToken
extends ASN1Object {
	private ASN1Integer version;
	private ASN1OctetString history;
	private Asn1PublishedData publishedData;
	private ASN1Set pubReference;
	private Extensions extensions;



	public static Asn1CertToken getInstance(ASN1TaggedObject obj, boolean explicit) {
		return new Asn1CertToken(ASN1Sequence.getInstance(obj, explicit));
	}

	public static Asn1CertToken getInstance(Object obj) {
		if (obj == null || obj instanceof Asn1CertToken) {
			return (Asn1CertToken) obj;
		} else if (obj instanceof ASN1Sequence) {
			return new Asn1CertToken((ASN1Sequence) obj);
		}

		throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
	}



	public Asn1CertToken(ASN1Sequence seq) {
		Enumeration en = seq.getObjects();

		// Required elements
		version = ASN1Integer.getInstance(en.nextElement());
		history = ASN1OctetString.getInstance(en.nextElement());
		publishedData = Asn1PublishedData.getInstance(en.nextElement());
		pubReference = ASN1Set.getInstance(en.nextElement());

		// Optional elements
		while (en.hasMoreElements()) {
			ASN1TaggedObject obj = ASN1TaggedObject.getInstance(en.nextElement());
			if (obj.getTagNo() == 0 && extensions == null) {
				extensions = Extensions.getInstance(obj, true);
			} else {
				throw new IllegalArgumentException("invalid object in factory: " + obj);
			}
		}
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public ASN1OctetString getHistory() {
		return history;
	}

	public ASN1Set getPubReference() {
		return pubReference;
	}

	public Asn1PublishedData getPublishedData() {
		return publishedData;
	}

	public ASN1Integer getVersion() {
		return version;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(history);
		v.add(publishedData);
		v.add(pubReference);
		if (extensions != null) {
			v.add(new DERTaggedObject(false, 0, extensions));
		}
		return new DERSequence(v);
	}

}
