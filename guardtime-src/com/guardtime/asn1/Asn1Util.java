/*
 * $Id: Asn1Util.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;



/**
 * ASN.1 related utility functions.
 */
abstract class Asn1Util {
	private static String[] supportedDigestAlgorithms = {
		TeleTrusTObjectIdentifiers.ripemd160.getId(),
		OIWObjectIdentifiers.idSHA1.getId(),
		NISTObjectIdentifiers.id_sha224.getId(),
		NISTObjectIdentifiers.id_sha256.getId(),
		NISTObjectIdentifiers.id_sha384.getId(),
		NISTObjectIdentifiers.id_sha512.getId()
	};

	/**
	 * Checks that the given digest algorithm is supported.
	 *
	 * @param algOid algorithm OID.
	 *
	 * @throws Asn1FormatException if algorithm is not supported.
	 */
	static void checkDigestAlgorithm(String algOid)
	throws Asn1FormatException {
		for (int i = 0; i < supportedDigestAlgorithms.length; i++) {
			if (algOid.equals(supportedDigestAlgorithms[i])) {
				return;
			}
		}

		throw new Asn1FormatException("digest algorithm not supported: " + algOid);
	}

	/**
	 * Verifies that the given extensions list does not contain any critical
	 * extensions.
	 *
	 * @param exts
	 *            the extensions list to check.
	 * @throws Asn1FormatException
	 *             if the lists is not properly formatted or contains critical
	 *             extensions.
	 */
	static void checkExtensions(Extensions exts)
	throws Asn1FormatException {
		if (exts == null) {
			// no extensions, nothing to check
			return;
		}
		Enumeration e = exts.oids();
		if (!e.hasMoreElements()) {
			// empty extensions lists are not allowed per X.509 specifications
			throw new Asn1FormatException("empty extensions list");
		}
		while (e.hasMoreElements()) {
			ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier((String) e.nextElement());
			Extension ext = exts.getExtension(oid);
			if (ext == null) {
				// should never happen, but...
				throw new Asn1FormatException("empty extension " + oid.getId());
			}
			if (ext.isCritical()) {
				throw new Asn1FormatException("unknown critical extension " + oid.getId());
			}
		}
	}

	/**
	 * Extends the given content info with data from the given certification
	 * token.
	 *
	 * @param contentInfo
	 *            the original timestamp encoded in a CMS {@code ContentInfo}
	 *            structure.
	 * @param certToken
	 *            the {@code CertToken} from the GuardTime online verification
	 *            service.
	 * @return updated (extended) timestamp encoded in a new CMS
	 *         {@code ContentInfo} structure.
	 */
	static org.bouncycastle.asn1.cms.ContentInfo extend(
			org.bouncycastle.asn1.cms.ContentInfo contentInfo,
			Asn1CertToken certToken)
	throws Asn1FormatException {
		ASN1EncodableVector v;

		// Extract signed data
		ASN1Encodable asn1SignedData = contentInfo.getContent();
		org.bouncycastle.asn1.cms.SignedData content =
			org.bouncycastle.asn1.cms.SignedData.getInstance(asn1SignedData);

		// Extract signer info
		ASN1Encodable asn1SignerInfo = content.getSignerInfos().getObjectAt(0);
		org.bouncycastle.asn1.cms.SignerInfo signerInfo =
			org.bouncycastle.asn1.cms.SignerInfo.getInstance(asn1SignerInfo);

		// Extract time signature
		ASN1Primitive asn1TimeSignature = null;
		try {
			asn1TimeSignature = ASN1Primitive.fromByteArray(signerInfo.getEncryptedDigest().getOctets());
		} catch (IOException e) {
			throw new Asn1FormatException("time signature has invalid format");
		}
		Asn1TimeSignature timeSignature = Asn1TimeSignature.getInstance(asn1TimeSignature);

		// Extend TimeSignature
		v = new ASN1EncodableVector();
		v.add(timeSignature.getLocation());
		v.add(certToken.getHistory());
		v.add(certToken.getPublishedData());
		// Skip PK signature <- updated
		v.add(new DERTaggedObject(false, 1, certToken.getPubReference()));
		timeSignature = Asn1TimeSignature.getInstance(new DERSequence(v));

		// Extend SignerInfo
		v = new ASN1EncodableVector();
		v.add(signerInfo.getVersion());
		v.add(signerInfo.getSID());
		v.add(signerInfo.getDigestAlgorithm());

		ASN1Set signedAttrs = signerInfo.getAuthenticatedAttributes();
		if (signedAttrs != null) {
			v.add(new DERTaggedObject(false, 0, signedAttrs));
		}

		v.add(signerInfo.getDigestEncryptionAlgorithm());
		try {
			v.add(new DEROctetString(timeSignature)); // <- updated
		} catch (IOException e) {
			throw new Asn1FormatException(e);
		}

		ASN1Set unsignedAttrs = signerInfo.getUnauthenticatedAttributes();
		if (unsignedAttrs != null) {
			v.add(new DERTaggedObject(false, 1, unsignedAttrs));
		}

		signerInfo = org.bouncycastle.asn1.cms.SignerInfo.getInstance(new DERSequence(v));

		// Extend SignedData
		v = new ASN1EncodableVector();
		v.add(content.getVersion());
		v.add(content.getDigestAlgorithms());
		v.add(content.getEncapContentInfo());
		// Skipping certificates <- updated
		// Skipping CRLs <- updated
		v.add(new DERSet(signerInfo)); // <- updated
		content = org.bouncycastle.asn1.cms.SignedData.getInstance(new DERSequence(v));

		// Extend ContentInfo
		v = new ASN1EncodableVector();
		v.add(contentInfo.getContentType());
		v.add(new DERTaggedObject(true, 0, content)); // <- updated
		contentInfo = org.bouncycastle.asn1.cms.ContentInfo.getInstance(new DERSequence(v));

		return contentInfo;
	}

	/**
	 * Extracts the value of the specified attribute from the given attribute
	 * set.
	 *
	 * @param attrs
	 *            the attribute set to search; this must not be {@code null}.
	 * @param oid
	 *            the OID of the attribute to look for.
	 * @return the value of the attribute.
	 * @throw Asn1FormatException if the attribute does not have exactly one
	 *        single value in the set.
	 */
	static ASN1Encodable getAttributeValue(ASN1Set attrs, String oid)
	throws Asn1FormatException {
		ASN1ObjectIdentifier asnOid = new ASN1ObjectIdentifier(oid);
		ASN1Encodable val = null;
		int count = 0;
		for (int i = 0; i < attrs.size(); ++i) {
			Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
			if (attr.getAttrType().equals(asnOid)) {
				ASN1Set set = attr.getAttrValues();
				if (set.size() < 1) {
					throw new Asn1FormatException("empty attribute " + oid);
				}
				if (set.size() > 1) {
					throw new Asn1FormatException("multi-valued attribute " + oid);
				}
				val = set.getObjectAt(0);
				++count;
			}
		}
		if (count < 1) {
			throw new Asn1FormatException("no attribute " + oid);
		}
		if (count > 1) {
			throw new Asn1FormatException("multiple instances of attribute " + oid);
		}
		return val;
	}
}



/**
 * ASN.1 object wrapper.
 */
abstract class Asn1Wrapper {
	abstract public byte[] getDerEncoded();

	/**
	 * Starting with version 1.47, the ASN.1 parser in BC throws generic
	 * IOExceptions in several cases where some sort of ASN.1 format exception
	 * would be more appropriate (for example, when an unknown ASN.1 tag or
	 * unsupported length encoding is encountered).
	 * <p>
	 * This method analyzes the stack trace from an exception and tries to
	 * detect such a situation with the goal that the exception could then be
	 * wrapped into a more appropriate exception type by the calling code in the
	 * GT API for the benefit of error handling in client code.
	 * <p>
	 * For consistency of results, we additionally apply the same logic to some
	 * runtime exception types that have also been used to signal conditions
	 * that really are ASN.1 format errors.
	 * 
	 * @param e
	 *            the exception to be analyzed.
	 * @return {@code true}, if {@code e} appears to be caused by an ASN.1
	 *         syntax error.
	 */
	protected static boolean isAsnParserException(Exception e) {
		StackTraceElement trace[] = e.getStackTrace();
		return trace.length > 0
			&& trace[0] != null
			&& trace[0].getClassName() != null
			&& (trace[0].getClassName().startsWith("org.bouncycastle.asn1.")
				|| trace[0].getClassName().startsWith("com.guardtime.asn1."));
	}
}
