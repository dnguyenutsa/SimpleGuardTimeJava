/*
 * $Id: SimpleHttpStamperTest.java 260 2012-02-25 13:04:02Z ahto.truu $
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
package tests.transport;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import com.guardtime.transport.SimpleHttpStamper;
import com.guardtime.transport.HttpVerificationResult;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTVerificationResult;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link SimpleHttpTimestamper} tests.
 */
public class SimpleHttpStamperTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	static final byte[] TIMESTAMP = Base64.decode("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");
	static final byte[] EXTENDED_TIMESTAMP = Base64.decode("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");
	static final String PUBLICATION = "AAAAAA-CLPCHI-AAPZUV-RPPRDK-ZZJHXR-DVO2VF-EXJD4C-MU6W3G-IIGWAH-S34OC2-MUTEOK-DZNQUW";

	private static final String STAMPER_URL = "http://stamper.guardtime.net/gt-signingservice";
	private static final String VERIFIER_URL = "http://verifier.guardtime.net/gt-extendingservice";
	private static final String PUBFILE_URL = "http://verify.guardtime.com/gt-controlpublications.bin";



	/**
	 * Tests {@link SimpleHttpStamper#create(GTDataHash, String)} and
	 * {@link SimpleHttpStamper#create(GTDataHash, java.net.URL)} methods.
	 */
	public void testCreate() {
		try {
			GTTimestamp ts1 = SimpleHttpStamper.create(DATA_HASH, STAMPER_URL);
			assertEquals(DATA_HASH, ts1.getDataHash());

			URL url = new URL(STAMPER_URL);
			GTTimestamp ts2 = SimpleHttpStamper.create(DATA_HASH, url);
			assertEquals(DATA_HASH, ts2.getDataHash());
		} catch (GTException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests
	 * {@link SimpleHttpStamper#extend(com.guardtime.tsp.GTTimestamp, String)} and
	 * {@link SimpleHttpStamper#extend(com.guardtime.tsp.GTTimestamp, java.net.URL)}
	 * methods.
	 *
	 */
	public void testExtend() {
		// Make sure extending of extended timestamp will fail
		try {
			GTTimestamp exts = GTTimestamp.getInstance(EXTENDED_TIMESTAMP);
			SimpleHttpStamper.extend(exts, VERIFIER_URL);
		} catch (IllegalStateException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (GTException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Extend timestamp
		try {
			GTTimestamp ts1 = GTTimestamp.getInstance(TIMESTAMP);
			SimpleHttpStamper.extend(ts1, VERIFIER_URL);
			assertTrue(ts1.isExtended());

			URL url = new URL(VERIFIER_URL);
			GTTimestamp ts2 = GTTimestamp.getInstance(TIMESTAMP);
			ts2 = SimpleHttpStamper.extend(ts2, url);
			assertTrue(ts2.isExtended());

			assertTrue(ts1.getDataHash().equals(ts2.getDataHash()));
		} catch (GTException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests
	 * {@link SimpleHttpStamper#verify(com.guardtime.tsp.GTTimestamp, GTDataHash, String, String, com.guardtime.tsp.GTPublicationsFile)}
	 * and
	 * {@link SimpleHttpStamper#verify(com.guardtime.tsp.GTTimestamp, GTDataHash, java.net.URL, String, com.guardtime.tsp.GTPublicationsFile)}
	 * methods.
	 */
	public void testVerify() {
		try {
			GTTimestamp ts = GTTimestamp.getInstance(TIMESTAMP);
			GTTimestamp ts2 = GTTimestamp.getInstance(TIMESTAMP);
			GTTimestamp exts = GTTimestamp.getInstance(EXTENDED_TIMESTAMP);
			URL url = new URL(VERIFIER_URL);
			GTPublicationsFile pf = SimpleHttpStamper.getPublicationsFile(PUBFILE_URL);
			HttpVerificationResult result = null;

			// Verify signed timestamp without extending
			result = SimpleHttpStamper.verify(ts, DATA_HASH, (String) null, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertFalse(ts.isExtended());

			result = SimpleHttpStamper.verify(ts, DATA_HASH, (URL) null, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertFalse(ts.isExtended());

			// Verify signed timestamp, try to extend
			result = SimpleHttpStamper.verify(ts, DATA_HASH, VERIFIER_URL, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts.isExtended());

			result = SimpleHttpStamper.verify(ts2, DATA_HASH, url, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts2.isExtended());

			// Verify extended timestamp without extending
			result = SimpleHttpStamper.verify(exts, DATA_HASH, (String) null, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts.isExtended()); // Just in case

			result = SimpleHttpStamper.verify(exts, DATA_HASH, (URL) null, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts.isExtended()); // Just in case

			// Verify signed timestamp, try to extend
			result = SimpleHttpStamper.verify(exts, DATA_HASH, VERIFIER_URL, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts.isExtended()); // Just in case

			result = SimpleHttpStamper.verify(exts, DATA_HASH, url, null, pf);
			assertTrue(result.isValid());
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.DATA_HASH_CHECKED));
			assertTrue(result.getGtResult().hasStatus(GTVerificationResult.PUBLICATION_CHECKED));
			assertTrue(ts.isExtended()); // Just in case
		} catch (GTException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests {@link SimpleHttpStamper#getPublicationsFile(String)} and
	 * {@link SimpleHttpStamper#getPublicationsFile(java.net.URL)} methods.
	 */
	public void testGetPublicationsFile() {
		try {
			GTPublicationsFile pf1 = SimpleHttpStamper.getPublicationsFile(PUBFILE_URL);
			assertNotNull(pf1);

			URL url = new URL(PUBFILE_URL);
			GTPublicationsFile pf2 = SimpleHttpStamper.getPublicationsFile(url);
			assertNotNull(pf2);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
