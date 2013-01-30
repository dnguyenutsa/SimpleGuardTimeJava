/*
 * $Id: SignatureInfoTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.SignatureInfo;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link SignatureInfo} tests.
 */
public class SignatureInfoTest
extends TestCase {
	private static final byte[] SIGNATURE_INFO = Base64.decode("MIIBEzANBgkqhkiG9w0BAQsFAASCAQCEMPSumzNjbH+2rlA85au9KGTYZz5aP5FV8uWyunIsZBURo6YhWteKnWmVooQ9QsUAfXae+MJg6MzDOvB5+j2IwEEcsmh90R6K/ZHMmPmEYRCx9FvgY9u0IVsTsggD0Mrc7TMgrsGUgPfQct1Ro53RBX8TlTuBaY8k+HDkK0yDkzEpd0mlRz1UkXvokTSwQFr15G8zvHhE2nRNhjPckXlIGOVhNRDg2zQl2TZO3Retr/5yH3gbhyOIgDT+S1/NwrKEpIH+TfCtqkLfdFaaOU+sTpLxLWGXybG5Dv9ajDdqYa3NDwRz/6Yt8bNbZvpCu/m/mBrWtHJdABn0kf61ld71");
	private static final String SIGNATURE_ALGORITHM = "1.2.840.113549.1.1.11"; // SHA256 with RSA
	private static final byte[] SIGNATURE_VALUE = Base64.decode("hDD0rpszY2x/tq5QPOWrvShk2Gc+Wj+RVfLlsrpyLGQVEaOmIVrXip1plaKEPULFAH12nvjCYOjMwzrwefo9iMBBHLJofdEeiv2RzJj5hGEQsfRb4GPbtCFbE7IIA9DK3O0zIK7BlID30HLdUaOd0QV/E5U7gWmPJPhw5CtMg5MxKXdJpUc9VJF76JE0sEBa9eRvM7x4RNp0TYYz3JF5SBjlYTUQ4Ns0Jdk2Tt0Xra/+ch94G4cjiIA0/ktfzcKyhKSB/k3wrapC33RWmjlPrE6S8S1hl8mxuQ7/Wow3amGtzQ8Ec/+mLfGzW2b6Qrv5v5ga1rRyXQAZ9JH+tZXe9Q==");
	private static final byte[][] PKI_REFERENCES = { "bar".getBytes(), "foo".getBytes(), "xyzzy".getBytes() }; // Strings here should be sorted alphabetically!
	private static final String ENCODED_SIGNATURE = "1.2.840.113549.1.1.11-84:30:F4:AE:9B:33:63:6C:7F:B6:AE:50:3C:E5:AB:BD:28:64:D8:67:3E:5A:3F:91:55:F2:E5:B2:BA:72:2C:64:15:11:A3:A6:21:5A:D7:8A:9D:69:95:A2:84:3D:42:C5:00:7D:76:9E:F8:C2:60:E8:CC:C3:3A:F0:79:FA:3D:88:C0:41:1C:B2:68:7D:D1:1E:8A:FD:91:CC:98:F9:84:61:10:B1:F4:5B:E0:63:DB:B4:21:5B:13:B2:08:03:D0:CA:DC:ED:33:20:AE:C1:94:80:F7:D0:72:DD:51:A3:9D:D1:05:7F:13:95:3B:81:69:8F:24:F8:70:E4:2B:4C:83:93:31:29:77:49:A5:47:3D:54:91:7B:E8:91:34:B0:40:5A:F5:E4:6F:33:BC:78:44:DA:74:4D:86:33:DC:91:79:48:18:E5:61:35:10:E0:DB:34:25:D9:36:4E:DD:17:AD:AF:FE:72:1F:78:1B:87:23:88:80:34:FE:4B:5F:CD:C2:B2:84:A4:81:FE:4D:F0:AD:AA:42:DF:74:56:9A:39:4F:AC:4E:92:F1:2D:61:97:C9:B1:B9:0E:FF:5A:8C:37:6A:61:AD:CD:0F:04:73:FF:A6:2D:F1:B3:5B:66:FA:42:BB:F9:BF:98:1A:D6:B4:72:5D:00:19:F4:91:FE:B5:95:DE:F5";



	/**
	 * Tests {@link SignatureInfo#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			SignatureInfo.getInstance(null);
			fail("null accepted as signature info bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(SIGNATURE_INFO);
			in.skip(1);
			SignatureInfo.getInstance(in);
			fail("rubbish accepted as signature info bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build signature info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(SIGNATURE_INFO);
		SignatureInfo.getInstance(in);

		// Build signature info using valid components
		DERTaggedObject pkiReferences = getDerTagged(false, 0, PKI_REFERENCES);
		in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, pkiReferences);
		SignatureInfo.getInstance(in);
	}

	/**
	 * Tests {@link SignatureInfo#getInstance(InputStream)} method with various
	 * signature algorithms.
	 */
	public void testInitSignatureAlgorithm()
	throws IOException {
		// Make sure empty signature algorithm is NOT accepted
		try {
			DERTaggedObject pkiReferences = getDerTagged(true, 0, PKI_REFERENCES);
			InputStream in = getDerStream(null, SIGNATURE_VALUE, pkiReferences);
			SignatureInfo.getInstance(in);
			fail("null accepted as signature algorithm");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignatureInfo#getInstance(InputStream)} method with various
	 * signature values.
	 */
	public void testInitSignatureValue()
	throws IOException {
		// Make sure empty signature value is NOT accepted
		try {
			DERTaggedObject pkiReferences = getDerTagged(true, 0, PKI_REFERENCES);
			InputStream in = getDerStream(SIGNATURE_ALGORITHM, null, pkiReferences);
			SignatureInfo.getInstance(in);
			fail("null accepted as signature value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignatureInfo#getInstance(InputStream)} method with various
	 * PKI references.
	 */
	public void testInitPkiReferences()
	throws IOException {
		// Make sure empty PKI references are accepted
		try {
			InputStream in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, null);
			SignatureInfo.getInstance(in);
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure explicitly tagged PKI references are NOT accepted
		try {
			DERTaggedObject pkiReferences = getDerTagged(true, 0, PKI_REFERENCES);
			InputStream in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, pkiReferences);
			SignatureInfo.getInstance(in);
			fail("explicitly tagged PKI references accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure PKI references with invalid tag are NOT accepted
		try {
			DERTaggedObject pkiReferences = getDerTagged(true, 1, PKI_REFERENCES);
			InputStream in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, pkiReferences);
			SignatureInfo.getInstance(in);
			fail("PKI references tagged with [1] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link SignatureInfo#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNATURE_INFO);
		SignatureInfo signatureInfo = SignatureInfo.getInstance(in);
		assertTrue(Arrays.equals(SIGNATURE_INFO, signatureInfo.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(SIGNATURE_INFO);
		SignatureInfo signatureInfo = SignatureInfo.getInstance(in);

		assertEquals(SIGNATURE_ALGORITHM, signatureInfo.getSignatureAlgorithm());
		assertTrue(Arrays.equals(SIGNATURE_VALUE, signatureInfo.getSignatureValue()));
		assertNull(signatureInfo.getPkiReferences());

		DERTaggedObject pkiReferences = getDerTagged(false, 0, PKI_REFERENCES);
		in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, pkiReferences);
		signatureInfo = SignatureInfo.getInstance(in);

		assertEquals(SIGNATURE_ALGORITHM, signatureInfo.getSignatureAlgorithm());
		assertTrue(Arrays.equals(SIGNATURE_VALUE, signatureInfo.getSignatureValue()));
		List list = signatureInfo.getPkiReferences();
		assertEquals(PKI_REFERENCES.length, list.size());
		for (int i = 0; i < PKI_REFERENCES.length; i++) {
			assertTrue(Arrays.equals(PKI_REFERENCES[i], (byte[]) list.get(i)));
		}

		assertEquals(ENCODED_SIGNATURE, signatureInfo.getEncodedSignature());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		DERTaggedObject pkiReferences = getDerTagged(false, 0, PKI_REFERENCES);
		InputStream in = getDerStream(SIGNATURE_ALGORITHM, SIGNATURE_VALUE, pkiReferences);
		SignatureInfo signatureInfo = SignatureInfo.getInstance(in);

		assertFalse(signatureInfo.getDerEncoded() == signatureInfo.getDerEncoded());

		assertFalse(signatureInfo.getSignatureValue() == signatureInfo.getSignatureValue());

		try {
			signatureInfo.getPkiReferences().clear();
			fail("Modifiable list returned as PKI references");
		} catch (UnsupportedOperationException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Produces input stream containing ASN.1 representation of signature info.
	 * @throws IOException 
	 */
	private InputStream getDerStream(String signatureAlgorithm, byte[] signatureValue, DERTaggedObject pkiReferences)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (signatureAlgorithm != null) {
			v.add(new AlgorithmIdentifier(new ASN1ObjectIdentifier(signatureAlgorithm)));
		}

		if (signatureValue != null) {
			v.add(new DEROctetString(signatureValue));
		}

		if (pkiReferences != null) {
			v.add(pkiReferences);
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[][] refs)
	throws IOException {
		if (refs == null) {
			return null;
		}

		DEROctetString[] derRefs = new DEROctetString[refs.length];
		for (int i = 0; i < refs.length; i++) {
			derRefs[i] = new DEROctetString(refs[i]);
		}

		// Note: octet-strings will get sorted in this DER-set
		DERSet derSet = new DERSet(derRefs);
		return new DERTaggedObject(isExplicit, tagNumber, derSet);
	}
}
