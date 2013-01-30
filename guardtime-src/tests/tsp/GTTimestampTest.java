/*
 * $Id: GTTimestampTest.java 212 2011-06-01 12:07:40Z ahto.truu $
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
package tests.tsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import com.guardtime.tsp.GTCertTokenResponse;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.tsp.GTVerificationResult;
import com.guardtime.util.Base16;
import com.guardtime.util.Log;



/**
 * {@code com.guardtime.tsp.GTTimestamp} tests.
 */
public class GTTimestampTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	private static final String PROPERTY_ACCURACY = "1s";
	private static final String PROPERTY_HISTORY_ID = "1265875751";
	//	private static final String PROPERTY_ISSUER_NAME = null; // Not set in this timestamp
	private static final String PROPERTY_LOCATION_ID = "562954248585231";
	// private static final String PROPERTY_LOCATION_NAME = null; // Not set in this timestamp
	private static final String PROPERTY_POLICY_ID = "1.3.6.1.4.1.27868.2.1.1";
	private static final String PROPERTY_PUBLICATION = "AAAAAA-CLPCHI-AAPZUV-RPPRDK-ZZJHXR-DVO2VF-EXJD4C-MU6W3G-IIGWAH-S34OC2-MUTEOK-DZNQUW";
	private static final String PROPERTY_PUBLICATION_ID = "1266192000";
	private static final String PROPERTY_PUBLICATION_TIME = "2010-02-15 00:00:00 UTC";
	//	private static final String PROPERTY_PUBLICATION_REFERENCES = null; // Not set in this timestamp
	private static final String PROPERTY_REGISTERED_TIME = "2010-02-11 08:09:11 UTC";
	private static final String PROPERTY_REQUEST_TIME = "2010-02-11 08:05:51 UTC";
	private static final String PROPERTY_SERIAL_NUMBER = "100292993877464331580964279931289733157";



	/**
	 * Tests predefined timestamp field names.
	 */
	public void testConstants() {
		assertTrue(GTTimestamp.ACCURACY.equals("issuer.accuracy"));
		assertTrue(GTTimestamp.HASH_ALGORITHM.equals("hashAlgorithm"));
		assertTrue(GTTimestamp.HASHED_MESSAGE.equals("hashedMessage"));
		assertTrue(GTTimestamp.HISTORY_ID.equals("history.id"));
		assertTrue(GTTimestamp.ISSUER_NAME.equals("issuer.name"));
		assertTrue(GTTimestamp.LOCATION_ID.equals("location.id"));
		assertTrue(GTTimestamp.LOCATION_NAME.equals("location.name"));
		assertTrue(GTTimestamp.POLICY_ID.equals("policy.id"));
		assertTrue(GTTimestamp.PUBLICATION.equals("publication.value"));
		assertTrue(GTTimestamp.PUBLICATION_ID.equals("publication.id"));
		assertTrue(GTTimestamp.PUBLICATION_TIME.equals("publication.time"));
		assertTrue(GTTimestamp.PUBLICATION_REFERENCES.equals("publication.references"));
		assertTrue(GTTimestamp.REGISTERED_TIME.equals("registeredTime"));
		assertTrue(GTTimestamp.REQUEST_TIME.equals("issuer.genTime"));
		assertTrue(GTTimestamp.SERIAL_NUMBER.equals("issuer.serialNumber"));
	}

	/**
	 * Tests {@code getInstance()} methods.
	 */
	public void testInit() {
		// Make sure illegal arguments are handled correctly
		try {
			GTTimestamp.getInstance((byte[]) null);
			fail("null accepted as timestamp bytes");
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTTimestamp.getInstance(null, 0, 1);
			fail("null accepted as timestamp bytes");
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTTimestamp.getInstance((InputStream) null);
			fail("null accepted as timestamp stream");
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		byte[] timestampBytes = Helper.TIMESTAMP;
		byte[] modifiedTimestampBytes = Helper.getModifiedData(Helper.TIMESTAMP);

		// Build timestamp object
		try {
			// ... from byte[]
			GTTimestamp aTimestamp = GTTimestamp.getInstance(Helper.TIMESTAMP);

			// ... from byte[] with bounds set
			GTTimestamp anotherTimestamp = GTTimestamp.getInstance(timestampBytes, 0, timestampBytes.length);
			assertTrue(Arrays.equals(aTimestamp.getEncoded(), anotherTimestamp.getEncoded()));

			// ... from part of byte[]
			anotherTimestamp = GTTimestamp.getInstance(modifiedTimestampBytes, 1, timestampBytes.length);
			assertTrue(Arrays.equals(aTimestamp.getEncoded(), anotherTimestamp.getEncoded()));

			// ... from InputStream
			InputStream in = new ByteArrayInputStream(timestampBytes);
			anotherTimestamp = GTTimestamp.getInstance(in);
			assertTrue(Arrays.equals(aTimestamp.getEncoded(), anotherTimestamp.getEncoded()));
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp: " + e.getMessage());
		}

		// Try to build timestamp object from invalid data

		// ... from byte[]
		try {
			GTTimestamp.getInstance(modifiedTimestampBytes);
			fail("rubbish accepted as timestamp bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from part of byte[]
		try {
			GTTimestamp.getInstance(modifiedTimestampBytes, 2, modifiedTimestampBytes.length - 2);
			fail("rubbish accepted as timestamp bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from InputStream
		try {
			InputStream in = new ByteArrayInputStream(modifiedTimestampBytes);
			GTTimestamp.getInstance(in);
			fail("rubbish accepted as timestamp bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code getEncoded()} method.
	 */
	public void testGetEncoded() {
		try {
			GTTimestamp timestamp = GTTimestamp.getInstance(Helper.TIMESTAMP);
			assertTrue(Arrays.equals(Helper.TIMESTAMP, timestamp.getEncoded()));

			GTTimestamp extendedTimestamp = GTTimestamp.getInstance(Helper.EXTENDED_TIMESTAMP);
			assertTrue(Arrays.equals(Helper.EXTENDED_TIMESTAMP, extendedTimestamp.getEncoded()));
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code getDataHash()}, {@code getHashAlgorithm()} and
	 * {@code getToken()} methods.
	 */
	public void testPropertyGetters() {
		try {
			testPropertyGetters(GTTimestamp.getInstance(Helper.TIMESTAMP));
			testPropertyGetters(GTTimestamp.getInstance(Helper.EXTENDED_TIMESTAMP));
		} catch (GTException e) {
			fail("cannot create timestamp: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code composeRequest()} method.
	 *
	 * Also tests {@code getHashAlgorithm()} and {@code getHashedMessage()}.
	 */
	public void testTimestamping() {
		try {
			byte[] req = GTTimestamp.composeRequest(DATA_HASH);
			byte[] resp = Helper.sendHttpRequest(Helper.STAMPER_URL, req);
			GTTimestampResponse response = GTTimestampResponse.getInstance(resp);
			GTTimestamp timestamp = response.getTimestamp();

			// Make sure data hash is intact
			assertTrue(timestamp.getDataHash().equals(DATA_HASH));
		} catch (GTException e) {
			e.printStackTrace();
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code composeExtensionRequest()}, {@code extend} and
	 * {@code isExtended()} methods.
	 *
	 * Also tests {@code getHashAlgorithm()} and {@code getHashedMessage()}.
	 */
	public void testExtending() {
		try {
			GTTimestamp timestamp = GTTimestamp.getInstance(Helper.TIMESTAMP);
			assertFalse(timestamp.isExtended());

			byte[] req = timestamp.composeExtensionRequest();
			byte[] resp = Helper.sendHttpRequest(Helper.EXTENDER_URL, req);
			GTCertTokenResponse response = GTCertTokenResponse.getInstance(resp);
			timestamp = timestamp.extend(response);
			assertTrue(timestamp.isExtended());

			// Make sure data hash and registered time are intact
			assertTrue(timestamp.getDataHash().equals(DATA_HASH));
			assertEquals(timestamp.getProperty(GTTimestamp.REGISTERED_TIME), PROPERTY_REGISTERED_TIME);
		} catch (GTException e) {
			fail("cannot extend timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}

		try {
			GTTimestamp timestamp = GTTimestamp.getInstance(Helper.EXTENDED_TIMESTAMP);
			assertTrue(timestamp.isExtended());

			byte[] req = timestamp.composeExtensionRequest();
			byte[] resp = Helper.sendHttpRequest(Helper.EXTENDER_URL, req);
			GTCertTokenResponse response = GTCertTokenResponse.getInstance(resp);
			timestamp = timestamp.extend(response);
			assertTrue(timestamp.isExtended());

			// Make sure data hash and registered time are intact
			assertTrue(timestamp.getDataHash().equals(DATA_HASH));
			assertEquals(timestamp.getProperty(GTTimestamp.REGISTERED_TIME), PROPERTY_REGISTERED_TIME);
		} catch (GTException e) {
			fail("cannot extend timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code verify()} methods.
	 */
	public void testVerify() {
		testVerify(Helper.getSampleTimestamp());
		testVerify(Helper.getSampleExtendedTimestamp());
	}

	/**
	 * Tests {@code getDataHash()}, {@code getHashAlgorithm()} and
	 * {@code getToken()} methods.
	 *
	 * @param timestamp
	 */
	private void testPropertyGetters(GTTimestamp timestamp) {
		// Direct properties
		assertTrue(timestamp.getHashAlgorithm().equals(HASH_ALG));
		assertTrue(timestamp.getDataHash().equals(DATA_HASH));

		Date registeredTime = new Date(Long.valueOf(PROPERTY_HISTORY_ID).longValue() * 1000);
		assertEquals(registeredTime, timestamp.getRegisteredTime());

		// Common properties
		assertEquals(PROPERTY_ACCURACY, timestamp.getProperty(GTTimestamp.ACCURACY));
		assertEquals(HASH_ALG.getOid(), timestamp.getProperty(GTTimestamp.HASH_ALGORITHM));
		assertEquals(Base16.encode(DATA_HASH.getHashedMessage()), timestamp.getProperty(GTTimestamp.HASHED_MESSAGE));
		assertEquals(PROPERTY_HISTORY_ID, timestamp.getProperty(GTTimestamp.HISTORY_ID));
		assertNull(timestamp.getProperty(GTTimestamp.ISSUER_NAME));
		assertEquals(PROPERTY_LOCATION_ID, timestamp.getProperty(GTTimestamp.LOCATION_ID));
		// TODO: test location name, too
		assertEquals(PROPERTY_POLICY_ID, timestamp.getProperty(GTTimestamp.POLICY_ID));
		assertEquals(PROPERTY_REGISTERED_TIME, timestamp.getProperty(GTTimestamp.REGISTERED_TIME));
		assertEquals(PROPERTY_REQUEST_TIME, timestamp.getProperty(GTTimestamp.REQUEST_TIME));
		assertEquals(PROPERTY_SERIAL_NUMBER, timestamp.getProperty(GTTimestamp.SERIAL_NUMBER));

		if (timestamp.isExtended()) {
			assertEquals(PROPERTY_PUBLICATION, timestamp.getProperty(GTTimestamp.PUBLICATION));
			assertEquals(PROPERTY_PUBLICATION_ID, timestamp.getProperty(GTTimestamp.PUBLICATION_ID));
			assertEquals(PROPERTY_PUBLICATION_TIME, timestamp.getProperty(GTTimestamp.PUBLICATION_TIME));
			assertNotNull(timestamp.getProperty(GTTimestamp.PUBLICATION_REFERENCES));
		} else {
			assertNull(timestamp.getProperty(GTTimestamp.PUBLICATION));
			assertNull(timestamp.getProperty(GTTimestamp.PUBLICATION_ID));
			assertNull(timestamp.getProperty(GTTimestamp.PUBLICATION_TIME));
			assertNull(timestamp.getProperty(GTTimestamp.PUBLICATION_REFERENCES));
		}
	}

	/**
	 * Helper method to test {@code verify()} methods.
	 *
	 * @param timestamp
	 */
	private void testVerify(GTTimestamp timestamp) {
		GTVerificationResult result = null;

		// Get publications file
		byte[] resp = Helper.getPublicationsFile();
		GTPublicationsFile publicationsFile = GTPublicationsFile.getInstance(resp);
		result = publicationsFile.verifySignature();
		if (!result.isValid()) {
			fail("cannot verify publications file signature: error " + result.getErrorCode());
		}

		// Make sure illegal arguments are handled correctly
		try {
			timestamp.verify((GTDataHash) null, publicationsFile);
			fail("null accepted as data hash");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			timestamp.verify(DATA_HASH, (GTPublicationsFile) null);
			fail("null accepted as publications file");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			timestamp.verify((GTDataHash) null, (GTPublicationsFile) null);
			fail("null accepted as both data hash and publications file");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Verify timestamp with publications file
		result = timestamp.verify(DATA_HASH, publicationsFile);
		assertTrue(result.isValid());
		assertTrue((result.getStatusCode() & GTVerificationResult.DATA_HASH_CHECKED) > 0);
		assertTrue((result.getStatusCode() & GTVerificationResult.PUBLICATION_CHECKED) > 0);

		if (timestamp.isExtended()) {
			// Make sure NULL is NOT accepted
			try {
				timestamp.verify(DATA_HASH, (String) null);
				fail("null accepted as publication for extended timestamp");
			} catch (IllegalArgumentException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}

			// Verify extended timestamp with publication
			result = timestamp.verify(DATA_HASH, Helper.PUBLICATION);
			assertTrue(result.isValid());
			assertTrue((result.getStatusCode() & GTVerificationResult.DATA_HASH_CHECKED) > 0);
			assertTrue((result.getStatusCode() & GTVerificationResult.PUBLICATION_CHECKED) > 0);
		} else {
			// Make sure NULL is NOT accepted
			try {
				timestamp.verify(DATA_HASH, (String) null);
				fail("null accepted as publication for signed timestamp");
			} catch (IllegalArgumentException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			} catch (IllegalStateException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}

			// Make sure publication is NOT accepted for signed timestamp
			try {
				timestamp.verify(DATA_HASH, Helper.PUBLICATION);
				fail("`verify(DataHash, String)` call succeeded for signed timestamp");
			} catch (IllegalStateException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}
}
