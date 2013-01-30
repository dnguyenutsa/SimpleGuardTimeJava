/*
 * $Id: CertTokenRequest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.math.BigInteger;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extensions;



/**
 * GuardTime structure {@code CertTokenRequest}.
 * <p>
 * Certification token request is sent to online verification service to create
 * extension data (certification token) for timestamp history identifier
 * contained in the request.
 *
 * <pre>
 * CertTokenRequest ::= SEQUENCE {
 *    version             INTEGER { v1(1) },
 *    historyIdentifier   INTEGER,
 *    extensions          [0] Extensions OPTIONAL
 * }
 * </pre>
 *
 * @since 0.4
 */
public final class CertTokenRequest
extends Asn1Wrapper {
	private static final int VERSION = 1;

	private Asn1CertTokenRequest request;



	/**
	 * Composes a new {@code CertTokenRequest} structure containing the given
	 * history identifier and no extensions.
	 *
	 * @param historyId
	 *            identifier of the second for which the certification token in
	 *            requested.
	 * @return a new certification token request.
	 */
	public static CertTokenRequest compose(BigInteger historyId) {
		if (historyId == null) {
			throw new IllegalArgumentException("invalid history ID: null");
		}

		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(new ASN1Integer(VERSION));
		v.add(new ASN1Integer(historyId));
		ASN1Sequence seq = new DERSequence(v);
		Asn1CertTokenRequest req = new Asn1CertTokenRequest(seq);
		return new CertTokenRequest(req);
	}



	/**
	 * Returns the DER representation of the {@code CertTokenRequest}.
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
	 * @param request ASN.1 representation of certification token request.
	 */
	CertTokenRequest(Asn1CertTokenRequest request) {
		this.request = request;
	}
}



/**
 * Internal implementation class for the ASN.1 representation of
 * {@code CertTokenRequest}.
 */
class Asn1CertTokenRequest
extends ASN1Object {
	private ASN1Integer version;
	private ASN1Integer historyIdentifier;
	private Extensions extensions;



	public static Asn1CertTokenRequest getInstance(Object obj) {
		if (obj == null || obj instanceof Asn1CertTokenRequest) {
			return (Asn1CertTokenRequest) obj;
		} else if (obj instanceof ASN1Sequence) {
			return new Asn1CertTokenRequest((ASN1Sequence) obj);
		}

		throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
	}



	public Asn1CertTokenRequest(ASN1Sequence seq) {
		Enumeration en = seq.getObjects();

		// Required elements
		version = ASN1Integer.getInstance(en.nextElement());
		historyIdentifier = ASN1Integer.getInstance(en.nextElement());

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

	public ASN1Integer getHistoryIdentifier() {
		return historyIdentifier;
	}

	public ASN1Integer getVersion() {
		return version;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(historyIdentifier);
		if (extensions != null) {
			v.add(new DERTaggedObject(false, 0, extensions));
		}
		return new DERSequence(v);
	}
}
