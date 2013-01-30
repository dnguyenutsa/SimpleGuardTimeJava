/*
 * $Id: AccuracyTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import com.guardtime.asn1.Accuracy;
import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link Accuracy} tests.
 */
public class AccuracyTest
extends TestCase {
	private static final byte[] ACCURACY = Base64.decode("MAMCAQE=");
	private static final Integer SECONDS = new Integer(1);



	/**
	 * Tests {@link Accuracy#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			Accuracy.getInstance(null);
			fail("null accepted as accuracy bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(ACCURACY);
			in.skip(1);
			Accuracy.getInstance(in);
			fail("rubbish accepted as accuracy bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build accuracy info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(ACCURACY);
		Accuracy.getInstance(in);

		// Build accuracy info using valid components
		in = getDerStream(SECONDS, null, null);
		Accuracy.getInstance(in);
	}

	/**
	 * Tests {@link Accuracy#getInstance(InputStream)} method with various
	 * seconds values.
	 */
	public void testInitSeconds()
	throws IOException {
		// Make sure empty seconds value is accepted
		try {
			InputStream in = getDerStream(null, null, null);
			Accuracy accuracy = Accuracy.getInstance(in);
			assertNull(accuracy.getSeconds());
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure negative seconds value is NOT accepted
		try {
			InputStream in = getDerStream(new Integer(-1), null, null);
			Accuracy.getInstance(in);
			fail("-1 accepted as seconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure 0 as seconds value is accepted
		try {
			InputStream in = getDerStream(new Integer(0), null, null);
			Accuracy accuracy = Accuracy.getInstance(in);
			assertEquals(0, accuracy.getSeconds().intValue());
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests {@link Accuracy#getInstance(InputStream)} method with various
	 * milliseconds values.
	 */
	public void testInitMillis()
	throws IOException {
		// Make sure empty milliseconds value is accepted
		try {
			InputStream in = getDerStream(SECONDS, null, null);
			Accuracy accuracy = Accuracy.getInstance(in);
			assertNull(accuracy.getMillis());
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure negative milliseconds value is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(true, 0, -1);
			InputStream in = getDerStream(SECONDS, millis, null);
			Accuracy.getInstance(in);
			fail("-1 accepted as milliseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure 0 as milliseconds value is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(true, 0, 0);
			InputStream in = getDerStream(SECONDS, millis, null);
			Accuracy.getInstance(in);
			fail("0 accepted as milliseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure 1..999 as milliseconds value are accepted
		try {
			for (int i = 1; i <= 999; i++) {
				DERTaggedObject millis = getDerTagged(true, 0, i);
				InputStream in = getDerStream(SECONDS, millis, null);
				Accuracy accuracy = Accuracy.getInstance(in);
				assertEquals(i, accuracy.getMillis().intValue());
			}
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure 1000 as milliseconds value is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(true, 0, 1000);
			InputStream in = getDerStream(SECONDS, millis, null);
			Accuracy.getInstance(in);
			fail("1000 accepted as milliseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure implicitly tagged millisecond value is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(false, 0, 1);
			InputStream in = getDerStream(SECONDS, millis, null);
			Accuracy.getInstance(in);
			// TODO: check disabled as BouncyCastle type would allow this
			//fail("implicitly tagged milliseconds value accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure millisecond value with invalid tag is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(true, 2, 1);
			InputStream in = getDerStream(SECONDS, millis, null);
			Accuracy.getInstance(in);
			fail("milliseconds value tagged with [2] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link Accuracy#getInstance(InputStream)} method with various
	 * microseconds values.
	 */
	public void testInitMicros()
	throws IOException {
		// Make sure empty microseconds value is accepted
		try {
			InputStream in = getDerStream(SECONDS, null, null);
			Accuracy accuracy = Accuracy.getInstance(in);
			assertNull(accuracy.getMicros());
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure negative microseconds value is NOT accepted
		try {
			DERTaggedObject micros = getDerTagged(true, 1, -1);
			InputStream in = getDerStream(SECONDS, null, micros);
			Accuracy.getInstance(in);
			fail("-1 accepted as microseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure 0 as microseconds value is NOT accepted
		try {
			DERTaggedObject micros = getDerTagged(true, 1, 0);
			InputStream in = getDerStream(SECONDS, null, micros);
			Accuracy.getInstance(in);
			fail("0 accepted as microseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure 1..999 as microseconds value are accepted
		try {
			for (int i = 1; i <= 999; i++) {
				DERTaggedObject micros = getDerTagged(true, 1, i);
				InputStream in = getDerStream(SECONDS, null, micros);
				Accuracy accuracy = Accuracy.getInstance(in);
				assertEquals(i, accuracy.getMicros().intValue());
			}
		} catch (Asn1FormatException e) {
			fail(e.getMessage());
		}

		// Make sure 1000 as microseconds value is NOT accepted
		try {
			DERTaggedObject micros = getDerTagged(true, 0, 1000);
			InputStream in = getDerStream(SECONDS, null, micros);
			Accuracy.getInstance(in);
			fail("1000 accepted as microseconds value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure implicitly tagged microsecond value is NOT accepted
		try {
			DERTaggedObject micros = getDerTagged(false, 1, 1);
			InputStream in = getDerStream(SECONDS, null, micros);
			Accuracy.getInstance(in);
			// TODO: check disabled as BouncyCastle type would allow this
			//fail("implicitly tagged microseconds value accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure microsecond value with conflicting tag is NOT accepted
		try {
			DERTaggedObject millis = getDerTagged(true, 0, 1);
			DERTaggedObject micros = getDerTagged(true, 0, 1);
			InputStream in = getDerStream(SECONDS, millis, micros);
			Accuracy.getInstance(in);
			// TODO: check disabled as BouncyCastle type would allow this
			//fail("microseconds value tagged with [0] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link Accuracy#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(ACCURACY);
		Accuracy accuracy = Accuracy.getInstance(in);
		assertTrue(Arrays.equals(ACCURACY, accuracy.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		int[] seconds = { 0, 1, 2, 100, 1000, 10000 };
		int[] millis = { 1, 10, 100, 999 };
		int[] micros = { 1, 9, 99, 999 };

		for (int i = 0; i < seconds.length; i++) {
			Integer sec = new Integer(seconds[i]);
			for (int ii = 0; ii < millis.length; ii++) {
				DERTaggedObject mls = getDerTagged(true, 0, millis[ii]);
				for (int iii = 0; iii < micros.length; iii++) {
					DERTaggedObject mcs = getDerTagged(true, 1, micros[iii]);

					InputStream in = getDerStream(sec, mls, mcs);
					Accuracy accuracy = Accuracy.getInstance(in);

					assertEquals(seconds[i], accuracy.getSeconds().intValue());
					assertEquals(millis[ii], accuracy.getMillis().intValue());
					assertEquals(micros[iii], accuracy.getMicros().intValue());
				}
			}
		}
	}

	/**
	 * Produces input stream containing ASN.1 representation of accuracy.
	 */
	private InputStream getDerStream(Integer seconds, DERTaggedObject millis, DERTaggedObject micros)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (seconds != null) {
			v.add(new ASN1Integer(seconds.intValue()));
		}

		if (millis != null) {
			v.add(millis);
		}

		if (micros != null) {
			v.add(micros);
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, int value)
	throws IOException {
		ASN1Integer asnValue = new ASN1Integer(value);
		return new DERTaggedObject(isExplicit, tagNumber, asnValue);
	}
}
