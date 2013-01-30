/*
 * $Id: SignerInfoTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
package tests.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.SignerInfo;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;
import com.guardtime.util.Util;



/**
 * {@link SignerInfo} tests.
 */
public class SignerInfoTest
extends TestCase {
	private static final byte[] SIGNER_INFO = Base64.decode("MIIK1AIBATArMCYxDTALBgNVBAMTBFRTQTIxFTATBgNVBAoTDEd1YXJkVGltZSBBUwIBATAJBgUrJAMCAQUAoEEwGgYJKoZIhvcNAQkDMQ0GCyqGSIb3DQEJEAEEMCMGCSqGSIb3DQEJBDEWBBR0QI8IDFiMPNKwhSezN/BouPTDpjAOBgorBgEEAYHZXAQBBQAEggpCMIIKPgSCBUwCAAICAgICAgICAgICAgICAgICAgICAgICAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAMCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAQCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAUCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAYCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAcCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAgCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAkCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAoCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABMBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABQBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABUBAAG8xgQ4DeBjkN2/y3dXyjKvhcVgO/y9ME1l5+FRCTzz5xYBAAF0DpPkRRSEikIEcewmZ7dmsvKzcZH5wmJnTe87cX2NrxcBAQHcIaqiVALli9vsxKRB2gd+lsyr7h5ibWqa9XxYA1okLRgBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABkBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABoBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB0BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAR4BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACcBAAHXlxNYS0BUgQwICnM6K39SSXpIxYtR79DNlwY0nmBTTygBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACkBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACoBAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC0BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC4BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC8BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBATMBAAGCBwRgX9EbEel7AanF5FGuFXZjoHxqJxbH4My5wIE9pzwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD0BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD4BAAHlpRia05um4btFCZ+ES68Hq1Rc1WqCvZfEGY9dCOqnYT8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEABAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEEBAAE2YyKHjz3f9DKTwKSSWb5eLKTMaBE1uYBQ7tsvnIGo+EIBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEMBAAECy4HBTmkgo8oVg6HHOo2cE1bphcq8Ryvgeld35S+Nqf8EggOoAQABs3oP8CjvYkjE1AVBYuVNxiFXmIyRqLOqnvzQnfMTnij/AQABfrrLclB5s/B+Z6uFhdvFAHSPPg6ShNc1PTA1LH745Zn/AQAB2WphXffcpgRN63Ka/zjyJ85gt4KFPdVWKJc4qR9h9Zr/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABpqnsC8pxBFiu2lQ4PYODHOoIn3nr3O1suylqWwDdHlb/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABa34e3rx8xwkwULN7K0f8N+/aIXbjqeZxcQCwEvrRb7T/AQABLL7IpYAOHGWZxGLPznET7LxvXFGHdVC6wkWY6z8t2KX/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQAB2JaKdodLcPfjefAccJozquHoeVwbu/TvyxTnjvvl4X//AQABdgTDojjrv8k8yuVoaLT14EqyaG1+d4kqFfDh+qk146P/AQABGf/07pUKZHFQP3T8mFFvvpJk+RMq0efqBuGE6CbXuIH/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABGeX9zdGy9XH03N62fQgZE+L36er6oECzgj9oHNkEcr7/AQABfIfIdjOoihaFkqH13DKTGpIx3fSEizgxWXHNLNNYvOf/AQABKV4lLgIwqfTrpiSKzlO1Ue51kk8lavxIx5HD6hDZXw//AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABG8JJVaLoqrqfkEky5OjUeUJS6xZt22LC0Jsi6C1JUDz/AQABsXaQ3QyISMeCoTdrNZ5fmbewaQgz4DI7Vsf6zeKdjm3/AQABFsLUXys9fMUU//9iQwMbTGwrUL4AmZRDOrGBWWIFFW//AQABZ2LCFin0ZnYb/Le2FAB9dQZa1iufx/NypKsKn4jMX+3/AQAByAF8RzeggmB3ya3Udtr4Ov4fShYo2l7LLaZZoq6h42n/AQABCZE/ez9vAEHYgXbeiO1RdGJUeT97j0HnwjFq6ZuGRw7/AQABu0T9NqXzze57XG3zpgmKCeNTM1tgKfFHdQJYin43vgD/MCkCBEt0mgAEIQHyTOwdn+JEv4hg4kgjkHufRhOfCEz/mAyAybGRVXaZyqCCARMwDQYJKoZIhvcNAQELBQAEggEAhDD0rpszY2x/tq5QPOWrvShk2Gc+Wj+RVfLlsrpyLGQVEaOmIVrXip1plaKEPULFAH12nvjCYOjMwzrwefo9iMBBHLJofdEeiv2RzJj5hGEQsfRb4GPbtCFbE7IIA9DK3O0zIK7BlID30HLdUaOd0QV/E5U7gWmPJPhw5CtMg5MxKXdJpUc9VJF76JE0sEBa9eRvM7x4RNp0TYYz3JF5SBjlYTUQ4Ns0Jdk2Tt0Xra/+ch94G4cjiIA0/ktfzcKyhKSB/k3wrapC33RWmjlPrE6S8S1hl8mxuQ7/Wow3amGtzQ8Ec/+mLfGzW2b6Qrv5v5ga1rRyXQAZ9JH+tZXe9Q==");
	private static final byte[] EXTENDED_SIGNER_INFO = Base64.decode("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");
	private static final Integer VERSION = new Integer(1);
	private static final byte[] SIGNER_ID = Base64.decode("MCswJjENMAsGA1UEAxMEVFNBMjEVMBMGA1UEChMMR3VhcmRUaW1lIEFTAgEB");
	private static final String SIGNER_ID_ISSUER_NAME = "CN=TSA2,O=GuardTime AS";
	private static final BigInteger SIGNER_ID_SERIAL_NUMBER = BigInteger.valueOf(1);
	private static final String DIGEST_ALG = "1.3.36.3.2.1"; // RIPEMD160
	private static final byte[] SIGNED_ATTRS = Base64.decode("MUEwGgYJKoZIhvcNAQkDMQ0GCyqGSIb3DQEJEAEEMCMGCSqGSIb3DQEJBDEWBBR0QI8IDFiMPNKwhSezN/BouPTDpg==");
	private static final String SIGNATURE_ALG = "1.3.6.1.4.1.27868.4.1"; // id-gt-TimeSignatureAlg
	private static final byte[] SIGNATURE = Base64.decode("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");
	private static final byte[] EXTENDED_SIGNATURE = Base64.decode("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");
	private static final byte[] UNSIGNED_ATTRS = Util.copyOf(SIGNED_ATTRS); // TODO



	/**
	 * Tests {@link SignerInfo} constant field values.
	 */
	public void testConstants() {
		assertEquals(VERSION.intValue(), SignerInfo.VERSION);
		assertEquals(SIGNATURE_ALG, SignerInfo.SIGNATURE_ALGORITHM);
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			SignerInfo.getInstance(null);
			fail("null accepted as signer info bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(SIGNER_INFO);
			in.skip(1);
			SignerInfo.getInstance(in);
			fail("rubbish accepted as signer info bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build signer info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(SIGNER_INFO);
		SignerInfo.getInstance(in);

		in = new ByteArrayInputStream(EXTENDED_SIGNER_INFO);
		SignerInfo.getInstance(in);

		// Build signer info using valid components
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);
		in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
		SignerInfo.getInstance(in);
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * version numbers.
	 */
	public void testInitVersion()
	throws IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure invalid version numbers are NOT accepted
		Integer[] invalidVersions = { null, Integer.valueOf("0"), Integer.valueOf("2") };
		for (int i = 0; i < invalidVersions.length; i++) {
			try {
				InputStream in = getDerStream(invalidVersions[i], SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
				SignerInfo.getInstance(in);
				fail(invalidVersions[i] + " accepted as version");
			} catch (Asn1FormatException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * signer identifiers.
	 */
	public void testInitSignerIdentifier()
	throws IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure empty signer identifier is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, null, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
			SignerInfo.getInstance(in);
			fail("null accepted as signer identifier");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * digest algorithms.
	 */
	public void testInitDigestAlgorithm()
	throws IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure empty digest algorithm is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, SIGNER_ID, null, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
			SignerInfo.getInstance(in);
			fail("null accepted as digest algorithm");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * signed attributes.
	 */
	public void testInitSignedAttrs()
	throws Asn1FormatException, IOException {
		// Make sure empty signed attributes are NOT accepted
		try {
			InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, null, SIGNATURE_ALG, SIGNATURE, null);
			SignerInfo.getInstance(in);
			fail("null accepted as signed attrs ");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure explicitly tagged signed attributes are NOT accepted
		try {
			DERTaggedObject signedAttrs = getDerTagged(true, 0, SIGNED_ATTRS);
			InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
			SignerInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("explicitly tagged signed attrs accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure signed attributes with invalid tag are NOT accepted
		try {
			DERTaggedObject signedAttrs = getDerTagged(false, 2, SIGNED_ATTRS);
			InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
			SignerInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("signed attributes tagged with [2] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * signature algorithms.
	 */
	public void testInitSignatureAlgorithm()
	throws IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure invalid signature algorithm is NOT accepted
		String[] invalidAlg = { null, DIGEST_ALG };
		for (int i = 0; i < invalidAlg.length; ++i) {
			try {
				InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, invalidAlg[i], SIGNATURE, null);
				SignerInfo.getInstance(in);
				fail(invalidAlg[i] + " accepted as signature algorithm");
			} catch (Asn1FormatException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * signature values.
	 */
	public void testInitSignature()
	throws IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure empty signature is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, null, null);
			SignerInfo.getInstance(in);
			fail("null accepted as signature value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignerInfo#getInstance(InputStream)} method with various
	 * unsigned attributes.
	 */
	public void testInitUnsignedAttrs()
	throws Asn1FormatException, IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);

		// Make sure empty unsigned attributes are accepted
		InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
		SignerInfo.getInstance(in);

		// Make sure explicitly tagged unsigned attributes are NOT accepted
		try {
			DERTaggedObject unsignedAttrs = getDerTagged(true, 1, UNSIGNED_ATTRS);
			in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, unsignedAttrs);
			SignerInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("explicitly tagged unsigned attrs accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure unsigned attributes with conflicting tag are NOT accepted
		try {
			DERTaggedObject unsignedAttrs = getDerTagged(false, 0, UNSIGNED_ATTRS);
			in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, unsignedAttrs);
			SignerInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("unsigned attributes tagged with [0] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignerInfo#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNER_INFO);
		SignerInfo signerInfo = SignerInfo.getInstance(in);
		assertTrue(Arrays.equals(SIGNER_INFO, signerInfo.getDerEncoded()));

		in = new ByteArrayInputStream(EXTENDED_SIGNER_INFO);
		signerInfo = SignerInfo.getInstance(in);
		assertTrue(Arrays.equals(EXTENDED_SIGNER_INFO, signerInfo.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNER_INFO);
		SignerInfo signerInfo = SignerInfo.getInstance(in);

		assertEquals(VERSION.intValue(), signerInfo.getVersion());
		assertEquals(SIGNER_ID_ISSUER_NAME, signerInfo.getIssuerName());
		assertEquals(0, SIGNER_ID_SERIAL_NUMBER.compareTo(signerInfo.getSerialNumber()));
		assertEquals(DIGEST_ALG, signerInfo.getDigestAlgorithm());
		assertTrue(Arrays.equals(SIGNED_ATTRS, signerInfo.getEncodedSignedAttrs()));
		assertEquals(SIGNATURE_ALG, signerInfo.getSignatureAlgorithm());
		assertTrue(Arrays.equals(SIGNATURE, signerInfo.getSignature().getDerEncoded()));
		assertNull(signerInfo.getEncodedUnsignedAttrs());

		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);
		DERTaggedObject unsignedAttrs = getDerTagged(false, 1, UNSIGNED_ATTRS);
		in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, EXTENDED_SIGNATURE, unsignedAttrs);
		signerInfo = SignerInfo.getInstance(in);

		assertEquals(VERSION.intValue(), signerInfo.getVersion());
		assertEquals(SIGNER_ID_ISSUER_NAME, signerInfo.getIssuerName());
		assertEquals(0, SIGNER_ID_SERIAL_NUMBER.compareTo(signerInfo.getSerialNumber()));
		assertEquals(DIGEST_ALG, signerInfo.getDigestAlgorithm());
		assertTrue(Arrays.equals(SIGNED_ATTRS, signerInfo.getEncodedSignedAttrs()));
		assertEquals(SIGNATURE_ALG, signerInfo.getSignatureAlgorithm());
		assertTrue(Arrays.equals(EXTENDED_SIGNATURE, signerInfo.getSignature().getDerEncoded()));
		assertTrue(Arrays.equals(UNSIGNED_ATTRS, signerInfo.getEncodedSignedAttrs()));
	}

	/**
	 * Tests {@link SignerInfo#isExtended()} method.
	 */
	public void testExtend()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNER_INFO);
		SignerInfo signerInfo = SignerInfo.getInstance(in);
		assertFalse(signerInfo.isExtended());

		in = new ByteArrayInputStream(EXTENDED_SIGNER_INFO);
		signerInfo = SignerInfo.getInstance(in);
		assertTrue(signerInfo.isExtended());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		DERTaggedObject signedAttrs = getDerTagged(false, 0, SIGNED_ATTRS);
		InputStream in = getDerStream(VERSION, SIGNER_ID, DIGEST_ALG, signedAttrs, SIGNATURE_ALG, SIGNATURE, null);
		SignerInfo signerInfo = SignerInfo.getInstance(in);

		assertFalse(signerInfo.getDerEncoded() == signerInfo.getDerEncoded());

		assertFalse(signerInfo.getEncodedSignedAttrs() == signerInfo.getEncodedSignedAttrs());

		assertFalse(signerInfo.getEncodedSignedAttrs() == signerInfo.getEncodedSignedAttrs());
	}

	/**
	 * Produces input stream containing ASN.1 representation of signer info.
	 */
	private InputStream getDerStream(
			Integer version, byte[] signerId, String digestAlgorithm, DERTaggedObject signedAttrs,
			String signatureAlgorithm, byte[] signature, DERTaggedObject unsignedAttrs)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (version != null) {
			v.add(new ASN1Integer(version.intValue()));
		}

		if (signerId != null) {
			v.add(new ASN1InputStream(signerId).readObject());
		}

		if (digestAlgorithm != null) {
			v.add(new AlgorithmIdentifier(new ASN1ObjectIdentifier(digestAlgorithm)));
		}

		if (signedAttrs != null) {
			v.add(signedAttrs);
		}

		if (signatureAlgorithm != null) {
			v.add(new AlgorithmIdentifier(new ASN1ObjectIdentifier(signatureAlgorithm)));
		}

		if (signature != null) {
			v.add(new DEROctetString(signature));
		}

		if (unsignedAttrs != null) {
			v.add(unsignedAttrs);
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[] data)
	throws IOException {
		DLSet attrs = (DLSet) new ASN1InputStream(data).readObject();
		return new DERTaggedObject(isExplicit, tagNumber, attrs);
	}
}
