/*
 * $Id: TstInfoTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.GeneralName;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.TstInfo;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link TstInfo} tests.
 */
public class TstInfoTest
extends TestCase {
	private static final byte[] TST_INFO = Base64.decode("MGsCAQEGCysGAQQBgdlcAgEBMDEwDQYJYIZIAWUDBAIBBQAEIAAZap90D/GUJFCQZXCqmIwwHist0XoohhX3UitkvZrwAhBLc7pfAAIAAQADAAAAAAglGA8yMDEwMDIxMTA4MDU1MVowAwIBAQ==");
	private static final Integer VERSION = new Integer(1);
	private static final String POLICY = "1.3.6.1.4.1.27868.2.1.1";
	private static final byte[] MESSAGE_IMPRINT = Base64.decode("MDEwDQYJYIZIAWUDBAIBBQAEIAAZap90D/GUJFCQZXCqmIwwHist0XoohhX3UitkvZrw");
	private static final BigInteger SERIAL_NUMBER = new BigInteger("100292993877464331580964279931289733157");
	private static final String GEN_TIME = "20100211080551Z";
	private static final byte[] ACCURACY = Base64.decode("MAMCAQE=");



	/**
	 * Tests {@link TstInfo} constant field values.
	 */
	public void testConstants() {
		assertEquals(VERSION.intValue(), TstInfo.VERSION);
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			TstInfo.getInstance(null);
			fail("null accepted as TST info bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(TST_INFO);
			in.skip(1);
			TstInfo.getInstance(in);
			fail("rubbish accepted as TST info bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build signer info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(TST_INFO);
		TstInfo.getInstance(in);

		// Build signature info using valid components
		in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME);
		TstInfo.getInstance(in);
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * version numbers.
	 */
	public void testInitVersion()
	throws IOException {
		// Make sure invalid version numbers are NOT accepted
		Integer[] invalidVersions = { null, Integer.valueOf("0"), Integer.valueOf("2") };
		for (int i = 0; i < invalidVersions.length; i++) {
			try {
				InputStream in = getDerStream(invalidVersions[i], POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME);
				TstInfo.getInstance(in);
				fail(invalidVersions[i] + " accepted as version");
			} catch (Asn1FormatException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * policy identifiers.
	 */
	public void testInitPolicy()
	throws Asn1FormatException, IOException {
		// Make sure empty policy ID is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, null, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME);
			TstInfo.getInstance(in);
			fail("null accepted as policy ID");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * message imprints.
	 */
	public void testInitMessageImprint()
	throws IOException {
		// Make sure empty message imprint is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, POLICY, null, SERIAL_NUMBER, GEN_TIME);
			TstInfo.getInstance(in);
			fail("null accepted as message imprint");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * serial numbers.
	 */
	public void testInitSerialNumber()
	throws Asn1FormatException, IOException {
		// Make sure empty serial number is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, null, GEN_TIME);
			TstInfo.getInstance(in);
			fail("null accepted as serial number");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure (some) valid serial numbers are accepted
		// 1461501637330902918203684832716283019655932542976 is 2^160
		// Negative values are allowed (at least not prohibited) by RFC 3161
		String[] validSerialNumbers = { "-1", "0", "1461501637330902918203684832716283019655932542975" };
		for (int i = 0; i < validSerialNumbers.length; i++) {
			BigInteger serialNumber = new BigInteger(validSerialNumbers[i]);
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, serialNumber, GEN_TIME);
			TstInfo.getInstance(in);
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * genTime values.
	 */
	public void testInitGenTime()
	throws IOException {
		// Make sure empty request time is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, null);
			TstInfo.getInstance(in);
			fail("null accepted as genTime");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with no optional
	 * fields set.
	 */
	public void testInitNoOptional()
	throws Asn1FormatException, IOException {
		// Make sure empty accuracy, ordering, nonce and tsa are accepted
		InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
				null, null, null, null);
		TstInfo.getInstance(in);
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * accuracy values.
	 */
	public void testInitAccuracy()
	throws Asn1FormatException, IOException {
		// Make sure invalid accuracy is not accepted
		try {
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
					MESSAGE_IMPRINT, null, null, null);
			TstInfo.getInstance(in);
			// TODO: check disabled as BouncyCastle type would allow this
			//fail("rubbish accepted as accuracy");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * ordering values.
	 */
	public void testInitOrdering()
	throws Asn1FormatException, IOException {
		// Make sure all valid ordering values are accepted
		Boolean[] validOrd = { null, Boolean.FALSE, Boolean.TRUE };
		for (int i = 0; i < validOrd.length; ++i) {
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
					null, validOrd[i], null, null);
			TstInfo tstInfo = TstInfo.getInstance(in);
			assertEquals(tstInfo.getOrdering(), validOrd[i] == Boolean.TRUE);
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * nonce values.
	 */
	public void testInitNonce()
	throws Asn1FormatException, IOException {
		// Make sure (some) valid nonce values are accepted
		// 18446744073709551616 is 2^64
		// Negative values are allowed (at least not prohibited) by RFC 3161
		String[] validNonces = { "-1", "0", "18446744073709551615" };
		for (int i = 0; i < validNonces.length; i++) {
			BigInteger nonce = new BigInteger(validNonces[i]);
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
					null, null, nonce, null);
			TstInfo.getInstance(in);
		}
	}

	/**
	 * Tests {@link TstInfo#getInstance(InputStream)} method with various
	 * TSA names.
	 */
	public void testInitTsa()
	throws Asn1FormatException, IOException {
		// Make sure implicitly tagged TSA name is NOT accepted
		try {
			DERTaggedObject tsa = getDerTagged(false, 0, 6, "TSA");
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
					null, null, null, tsa);
			TstInfo.getInstance(in);
			// TODO: check disabled as BouncyCastle type would allow this
			//fail("implicitly tagged TSA name accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure TSA name with invalid tag is NOT accepted
		try {
			DERTaggedObject tsa = getDerTagged(false, /* should be 0 */ 4, 6, "TSA");
			InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
					null, null, null, tsa);
			TstInfo.getInstance(in);
			fail("TSA name tagged with [4] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TstInfo#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(TST_INFO);
		TstInfo tstInfo = TstInfo.getInstance(in);
		assertTrue(Arrays.equals(TST_INFO, tstInfo.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		InputStream in = new ByteArrayInputStream(TST_INFO);
		TstInfo tstInfo = TstInfo.getInstance(in);

		assertEquals(VERSION.intValue(), tstInfo.getVersion());
		assertEquals(POLICY, tstInfo.getPolicy());
		assertTrue(Arrays.equals(MESSAGE_IMPRINT, tstInfo.getMessageImprint().getDerEncoded()));
		assertEquals(0, SERIAL_NUMBER.compareTo(tstInfo.getSerialNumber()));
		assertEquals(GEN_TIME, dateFormat.format(tstInfo.getGenTime()));
		assertTrue(Arrays.equals(ACCURACY, tstInfo.getAccuracy().getDerEncoded()));
		assertFalse(tstInfo.getOrdering());
		assertNull(tstInfo.getNonce());
		assertNull(tstInfo.getTsa());
		assertNull(tstInfo.getEncodedExtensions());

		DERTaggedObject tsa = getDerTagged(true, 0, 6, "TSA");
		in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
				ACCURACY, new Boolean(true), new BigInteger("7"), tsa);
		tstInfo = TstInfo.getInstance(in);

		assertEquals(VERSION.intValue(), tstInfo.getVersion());
		assertEquals(POLICY, tstInfo.getPolicy());
		assertTrue(Arrays.equals(MESSAGE_IMPRINT, tstInfo.getMessageImprint().getDerEncoded()));
		assertEquals(0, SERIAL_NUMBER.compareTo(tstInfo.getSerialNumber()));
		assertEquals(GEN_TIME, dateFormat.format(tstInfo.getGenTime()));
		assertTrue(Arrays.equals(ACCURACY, tstInfo.getAccuracy().getDerEncoded()));
		assertTrue(tstInfo.getOrdering());
		assertEquals(7, tstInfo.getNonce().intValue());
		assertEquals("6: TSA", tstInfo.getTsa());
		assertNull(tstInfo.getEncodedExtensions());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		Boolean ordering = Boolean.FALSE;
		DERTaggedObject tsa = getDerTagged(false, 0, 6, "TSA");
		BigInteger nonce = BigInteger.valueOf(100500);
		InputStream in = getDerStream(VERSION, POLICY, MESSAGE_IMPRINT, SERIAL_NUMBER, GEN_TIME,
				ACCURACY, ordering, nonce, tsa);
		TstInfo tstInfo = TstInfo.getInstance(in);

		assertFalse(tstInfo.getDerEncoded() == tstInfo.getDerEncoded());

		tstInfo.getGenTime().setTime(new Date().getTime());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals(GEN_TIME, dateFormat.format(tstInfo.getGenTime()));

		// TODO: check disabled as GuardTime is not currently using this field.
		//try {
		//	tstInfo.getExtensions().clear();
		//	fail("Modifiable extension list returned");
		//} catch (UnsupportedOperationException e) {
		//	Log.debug("[DBG] (OK) " + e.getMessage());
		//}
	}

	/**
	 * Produces input stream containing ASN.1 representation of TST info.
	 */
	private InputStream getDerStream(
			Integer version, String policy, byte[] messageImprint, BigInteger serialNumber, String genTime)
	throws Asn1FormatException, IOException {
		return getDerStream(version, policy, messageImprint, serialNumber, genTime, null, null, null, null);
	}

	/**
	 * Produces input stream containing ASN.1 representation of TST info.
	 */
	private InputStream getDerStream(
			Integer version, String policy, byte[] messageImprint, BigInteger serialNumber, String genTime,
			byte[] accuracy, Boolean ordering, BigInteger nonce, DERTaggedObject tsa)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (version != null) {
			v.add(new ASN1Integer(version.intValue()));
		}

		if (policy != null) {
			v.add(new ASN1ObjectIdentifier(policy));
		}

		if (messageImprint != null) {
			v.add(new ASN1InputStream(messageImprint).readObject());
		}

		if (serialNumber != null) {
			v.add(new ASN1Integer(serialNumber));
		}

		if (genTime != null) {
			v.add(new ASN1GeneralizedTime(genTime));
		}

		if (accuracy != null) {
			v.add(new ASN1InputStream(accuracy).readObject());
		}

		if (ordering != null) {
			v.add(new ASN1Boolean(ordering.booleanValue()));
		}

		if (nonce != null) {
			v.add(new ASN1Integer(nonce.intValue()));
		}

		if (tsa != null) {
			v.add(tsa);
		}

		// Extensions skipped -- see TstInfo code for comments

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, int nameTagNumber, String name)
	throws IOException {
		GeneralName generalName = new GeneralName(nameTagNumber, name);
		return new DERTaggedObject(isExplicit, tagNumber, generalName);
	}
}
