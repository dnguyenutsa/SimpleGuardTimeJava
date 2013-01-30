/*
 * $Id: SignedDataTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.PublishedData;
import com.guardtime.asn1.SignedData;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link SignedData} tests.
 */
public class SignedDataTest
extends TestCase {
	private static final byte[] SIGNED_DATA = Base64.decode("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");
	private static final Integer VERSION = new Integer(3);
	private static final String[] DIGEST_ALGS = { "1.3.36.3.2.1" }; // RIPEMD160
	private static final String E_CONTENT_TYPE = "1.2.840.113549.1.9.16.1.4";
	private static final byte[] E_CONTENT = Base64.decode("MGsCAQEGCysGAQQBgdlcAgEBMDEwDQYJYIZIAWUDBAIBBQAEIAAZap90D/GUJFCQZXCqmIwwHist0XoohhX3UitkvZrwAhBLc7pfAAIAAQADAAAAAAglGA8yMDEwMDIxMTA4MDU1MVowAwIBAQ==");
	private static final byte[][] CERTS = { Base64.decode("MIICwDCCAagCAQEwDQYJKoZIhvcNAQELBQAwJjENMAsGA1UEAxMEVFNBMjEVMBMGA1UEChMMR3VhcmRUaW1lIEFTMB4XDTA5MDQwMzExMDU1M1oXDTEwMDUwMzExMDU1M1owJjENMAsGA1UEAxMEVFNBMjEVMBMGA1UEChMMR3VhcmRUaW1lIEFTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArrNiYnYr2fWEhzNUK8AbqwCudYLAf1jSc//s8GR92tFp+SCkL08eqwerlCyMuUz3ivKnM2T0reK/cJPIllKjKK3sVEGpKl0Iab/JER++I9WZQypOoFZ+lDeQKY+gd3ZIN4F6FPCR8qBfXU4C3+tCerEaVYRv+Zj52Yz7c0DcZS4p0meYxUHtkQaPlnRj596I5utq+FLWnhX3nJOFp1h0T9N8xDvJbZHfuEDcmfxNMXkeL7QWgf+A8N/0QagjpTXND1alFQm5Zer+7lV/PFuRq0QyN6x84XI4pP51WwtlkbEYbuiZkzfJXyqR5Idlg6xYh+h6vu1WccYoISjhAauwqQIDAIotMA0GCSqGSIb3DQEBCwUAA4IBAQBfMsSOG0fSYc0Oh2SCQ+YWtL/nL4zTi/Mb06fJWchr9rgdabrJ+CeOZnScvUcH97b4hxb52X7Lcd9LeACLYKgMmRDYj4gtcHeDmY8dvSAnaoAbfSOYvLQfPUCE7YSSCW9/Gb7Gkw24MNkridot6sZ1znLqklTy6UhgsYq6Nn8V+NvLTzi5BpDqTGRs5Rkw9exAS3zkEZ0frx3Zsas78LvYFx8dFrnaV9hWgHcCS/zDKI/1Ys0HFtMQVWpw7YSdy6jVf2p6n9plBNWrYXvwtj0cwawDeztBLhrO6kw2gLP69VP+Aq7qU8gNgCY9azQISQX0Y4mlVczxmQoW5io26qIK") };
	private static final byte[][] SIGNER_INFOS = { Base64.decode("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") };



	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			SignedData.getInstance(null);
			fail("null accepted as signed data bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(SIGNED_DATA);
			in.skip(1);
			SignedData.getInstance(in);
			fail("rubbish accepted as signed data bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build signed data from pre-defined bytes
		InputStream in = new ByteArrayInputStream(SIGNED_DATA);
		SignedData.getInstance(in);

		// Build signed data using valid components
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);
		in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
		SignedData.getInstance(in);
	}

	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method with various
	 * version values.
	 */
	public void testInitVersion()
	throws IOException {
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);

		// Make sure invalid version numbers are not accepted
		Integer[] invalidVersions = { null, Integer.valueOf("-1"), Integer.valueOf("0"), Integer.valueOf("2"), Integer.valueOf("4") };
		for (int i = 0; i < invalidVersions.length; i++) {
			try {
				InputStream in = getDerStream(invalidVersions[i], DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
				SignedData.getInstance(in);
				fail(invalidVersions[i] + " accepted as version");
			} catch (Asn1FormatException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}

	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method with various
	 * digest algorithms.
	 */
	public void testInitDigestAlgorithms()
	throws Asn1FormatException, IOException {
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);

		// Make sure empty algorithm field is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, null, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("null accepted as digest algorithms");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure unsupported algorithms are NOT accepted
		try {
			String[] algorithms = { "1.2.840.113549.2.5" }; // MD5
			InputStream in = getDerStream(VERSION, algorithms, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("unsupported digest algorithm accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure extra algorithms are not causing errors
		String[] algs = { "2.16.840.1.101.3.4.2.1", "1.3.36.3.2.1" };
		InputStream in = getDerStream(VERSION, algs, eContent, certificates, SIGNER_INFOS);
		SignedData.getInstance(in);
	}

	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method with various
	 * encapsulated content info values.
	 */
	public void testInitEContentInfo()
	throws Asn1FormatException, IOException {
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);

		// Make sure empty EContentInfo is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, null, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("null accepted as encapsulated content info");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure EContentInfo with invalid content type is NOT accepted
		try {
			// SignedData OID used instead of EContentType
			DERSequence eContent = getEContent("1.2.840.113549.1.7.2", true, 0, SIGNER_INFOS[0]);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("1.2.840.113549.1.7.2 accepted as encapsulated content type");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure implicitly tagged EContent is NOT accepted
		try {
			// EContent is implicitly tagged here (instead of explicit)
			DERSequence eContent = getEContent(E_CONTENT_TYPE, false, 0, E_CONTENT);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("implicitly tagged encapsulated content accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure EContent with invalid tag is NOT accepted
		try {
			// SEContent tagged with 1 here (instead of 0)
			DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 1, E_CONTENT);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("encapsulated content tagged with 1 accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure EContentInfo with invalid EContent is NOT accepted
		try {
			// SignerInfo used instead of EContentInfo
			DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, SIGNER_INFOS[0]);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("rubbish accepted as encapsulated content info");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method with various
	 * certificates.
	 */
	public void testInitCertificates()
	throws Asn1FormatException, IOException {
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);

		// Make sure explicitly tagged certificates are NOT accepted
		try {
			// Certificates are explicitly tagged here (instead of implicit)
			DERTaggedObject certificates = getDerTagged(true, 0, CERTS);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("explicitly tagged certificates accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure Certificates with invalid tag is NOT accepted
		try {
			// Certificates tagged with 2 here (instead of 0)
			DERTaggedObject certificates = getDerTagged(false, 2, CERTS);
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
			SignedData.getInstance(in);
			fail("certificates tagged with 2 accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure empty certificates field is accepted
		InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, null, SIGNER_INFOS);
		SignedData.getInstance(in);

		// Make sure empty certificates set is accepted
		DERTaggedObject certificates = getDerTagged(false, 0, new byte[0][]);
		in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
		SignedData.getInstance(in);

		// Make sure extra certificates are not causing errors
		byte[][] certs = { CERTS[0], CERTS[0] };
		certificates = getDerTagged(false, 0, certs);
		in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
		SignedData.getInstance(in);
	}

	/**
	 * Tests {@link SignedData#getInstance(InputStream)} method with various
	 * signer infos.
	 */
	public void testInitSignerInfos()
	throws Asn1FormatException, IOException {
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);

		// Make sure empty signer info is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, null);
			SignedData.getInstance(in);
			fail("null accepted as signer info");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure empty signer info set is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, new byte[0][]);
			SignedData.getInstance(in);
			fail("empty signer info set accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
		// Make sure invalid signer infos are NOT accepted
		try {
			// EContent is used insted of SignerInfo
			byte[][] signerInfos = { E_CONTENT };
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, signerInfos);
			SignedData.getInstance(in);
			fail("rubbish accepted as signer infos");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure extra signer infos are NOT accepted
		try {
			byte[][] signerInfos = { SIGNER_INFOS[0], SIGNER_INFOS[0] };
			InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, signerInfos);
			SignedData.getInstance(in);
			fail("extra signer infos accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link PublishedData#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNED_DATA);
		SignedData signedData = SignedData.getInstance(in);
		assertTrue(Arrays.equals(SIGNED_DATA, signedData.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, CertificateEncodingException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNED_DATA);
		SignedData signedData = SignedData.getInstance(in);

		assertEquals(VERSION.intValue(), signedData.getVersion());
		assertEquals(DIGEST_ALGS[0], signedData.getDigestAlgorithms().get(0));
		assertEquals(E_CONTENT_TYPE, signedData.getEContentType());
		assertTrue(Arrays.equals(E_CONTENT, signedData.getEContent().getDerEncoded()));
		assertTrue(Arrays.equals(CERTS[0], signedData.getCertificate().getEncoded()));
		assertTrue(Arrays.equals(SIGNER_INFOS[0], signedData.getSignerInfo().getDerEncoded()));

		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);
		in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
		signedData = SignedData.getInstance(in);

		assertEquals(VERSION.intValue(), signedData.getVersion());
		assertEquals(DIGEST_ALGS[0], signedData.getDigestAlgorithms().get(0));
		assertEquals(E_CONTENT_TYPE, signedData.getEContentType());
		assertTrue(Arrays.equals(E_CONTENT, signedData.getEContent().getDerEncoded()));
		assertTrue(Arrays.equals(CERTS[0], signedData.getCertificate().getEncoded()));
		assertTrue(Arrays.equals(SIGNER_INFOS[0], signedData.getSignerInfo().getDerEncoded()));
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, CertificateEncodingException, IOException {
		DERSequence eContent = getEContent(E_CONTENT_TYPE, true, 0, E_CONTENT);
		DERTaggedObject certificates = getDerTagged(false, 0, CERTS);
		InputStream in = getDerStream(VERSION, DIGEST_ALGS, eContent, certificates, SIGNER_INFOS);
		SignedData signedData = SignedData.getInstance(in);

		assertFalse(signedData.getDerEncoded() == signedData.getDerEncoded());

		try {
			signedData.getDigestAlgorithms().clear();
			fail("Modifiable digest algorithm list returned");
		} catch (UnsupportedOperationException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		assertFalse(signedData.getCertificate().getEncoded() == signedData.getCertificate().getEncoded());
	}

	/**
	 * Produces input stream containing ASN.1 representation of signed data.
	 */
	private InputStream getDerStream(Integer version, String[] digestAlgorithms,
			DERSequence eContent, DERTaggedObject certificates, byte[][] signerInfos)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (version != null) {
			v.add(new ASN1Integer(version.intValue()));
		}

		if (digestAlgorithms != null) {
			AlgorithmIdentifier[] derAlgs = new AlgorithmIdentifier[digestAlgorithms.length];
			for (int i = 0; i < digestAlgorithms.length; i++) {
				derAlgs[i] = new AlgorithmIdentifier(new ASN1ObjectIdentifier(digestAlgorithms[i]));
			}
			v.add(new DERSet(derAlgs));
		}

		if (eContent != null) {
			v.add(eContent);
		}

		if (certificates != null) {
			v.add(certificates);
		}

		// CRLs ignored

		if (signerInfos != null) {
			ASN1Encodable[] derInfos = new ASN1Encodable[signerInfos.length];
			for (int i = 0; i < signerInfos.length; i++) {
				derInfos[i] = new ASN1InputStream(signerInfos[i]).readObject();
			}
			v.add(new DERSet(derInfos));
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 representation of encapsulated content info.
	 */
	private DERSequence getEContent(String eContentType, boolean isExplicit, int tagNumber, byte[] eContent)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (eContentType != null) {
			v.add(new ASN1ObjectIdentifier(eContentType));
		}

		if (eContent != null) {
			ASN1Object derEContent = new ASN1InputStream(eContent).readObject();
			v.add(new DERTaggedObject(isExplicit, tagNumber, new DEROctetString(derEContent)));
		}

		return new DERSequence(v);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[][] certificates)
	throws IOException {
		ASN1Encodable[] derCerts = new ASN1Encodable[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			derCerts[i] = new ASN1InputStream(certificates[i]).readObject();
		}
		return new DERTaggedObject(isExplicit, tagNumber, new DERSet(derCerts));
	}
}
