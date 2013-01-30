/*
 * $Id: MessageImprintTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.MessageImprint;
import com.guardtime.util.Base16;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link MessageImprint} tests.
 */
public class MessageImprintTest
extends TestCase {
	private static final byte[] MESSAGE_IMPRINT = Base64.decode("MC8wCwYJYIZIAWUDBAIBBCAAGWqfdA/xlCRQkGVwqpiMMB4rLdF6KIYV91IrZL2a8A==");
	private static final String HASH_ALGORITHM = "2.16.840.1.101.3.4.2.1"; // SHA256
	private static final byte[] HASHED_MESSAGE = Base16.decode("00196a9f740ff1942450906570aa988c301e2b2dd17a288615f7522b64bd9af0"); // sha256("Tere\n")



	/**
	 * Tests {@link MessageImprint#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			MessageImprint.getInstance(null);
			fail("null accepted as message imprint bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(MESSAGE_IMPRINT);
			in.skip(1);
			MessageImprint.getInstance(in);
			fail("rubbish accepted as message imprint bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build message imprint from pre-defined bytes
		InputStream in = new ByteArrayInputStream(MESSAGE_IMPRINT);
		MessageImprint.getInstance(in);

		// Build message imprint using valid components
		in = getDerStream(HASH_ALGORITHM, HASHED_MESSAGE);
		MessageImprint.getInstance(in);
	}

	/**
	 * Tests {@link MessageImprint#getInstance(InputStream)} method with various
	 * hash algorithms.
	 */
	public void testInitHashAlgorithm()
	throws IOException {
		// Make sure empty hash algorithm is NOT accepted
		try {
			InputStream in = getDerStream(null, HASHED_MESSAGE);
			MessageImprint.getInstance(in);
			fail("null accepted as hash algorithm");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link MessageImprint#getInstance(InputStream)} method with various
	 * hashed messages.
	 */
	public void testInitHasedMessage()
	throws IOException {
		// Make sure empty hashed message is NOT accepted
		try {
			InputStream in = getDerStream(HASH_ALGORITHM, null);
			MessageImprint.getInstance(in);
			fail("null accepted as hashed message");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link MessageImprint#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(MESSAGE_IMPRINT);
		MessageImprint messageImprint = MessageImprint.getInstance(in);
		assertTrue(Arrays.equals(MESSAGE_IMPRINT, messageImprint.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(MESSAGE_IMPRINT);
		MessageImprint messageImprint = MessageImprint.getInstance(in);

		assertEquals(HASH_ALGORITHM, messageImprint.getHashAlgorithm());
		assertTrue(Arrays.equals(HASHED_MESSAGE, messageImprint.getHashedMessage()));
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		InputStream in = getDerStream(HASH_ALGORITHM, HASHED_MESSAGE);
		MessageImprint messageImprint = MessageImprint.getInstance(in);

		assertFalse(messageImprint.getDerEncoded() == messageImprint.getDerEncoded());

		assertFalse(messageImprint.getHashedMessage() == messageImprint.getHashedMessage());
	}

	/**
	 * Produces input stream containing ASN.1 representation of message imprint.
	 */
	private InputStream getDerStream(String hashAlgorithm, byte[] hashedMessage)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (hashAlgorithm != null) {
			v.add(new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashAlgorithm)));
		}

		if (hashedMessage != null) {
			v.add(new DEROctetString(hashedMessage));
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}
}
