/*
 * $Id: GTCertTokenResponseTest.java 260 2012-02-25 13:04:02Z ahto.truu $
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

import junit.framework.TestCase;

import com.guardtime.tsp.GTCertTokenResponse;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@code com.guardtime.tsp.GTCertTokenResponse} tests.
 */
public class GTCertTokenResponseTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	private static final byte[] TIMESTAMP = Base64.decode("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");
	private static final byte[] EXTENDED_TIMESTAMP = Base64.decode("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");

	private static final String STAMPER_URL = "http://stamper.ee.guardtime.net/gt-signingservice";
	private static final String EXTENDER_URL = "http://verifier.ee.guardtime.net/gt-extendingservice";



	/**
	 * Tests {@code getInstance()} methods.
	 */
	public void testTimestampResponseInit() {
		// Make sure illegal arguments are handled correctly
		try {
			GTCertTokenResponse.getInstance((byte[]) null);
			fail("null accepted as timestamp response bytes");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTCertTokenResponse.getInstance((byte[]) null, 0, 1);
			fail("null accepted as timestamp response bytes");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTCertTokenResponse.getInstance((InputStream) null);
			fail("null accepted as timestamp response stream");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Get cert token response
		byte[] resp = null;
		try {
			GTTimestamp timestamp = GTTimestamp.getInstance(TIMESTAMP);
			byte[] req = timestamp.composeExtensionRequest();
			resp = Helper.sendHttpRequest(EXTENDER_URL, req);
		} catch (GTException e) {
			fail("cannot build timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}

		// Prepare modified data
		byte[] modified = new byte[resp.length + 2];
		modified[0] = 65;
		System.arraycopy(resp, 0, modified, 1, resp.length);
		modified[resp.length + 1] = 66;

		// Build timestamp response object
		try {
			// ... from `byte[]`
			GTCertTokenResponse response = GTCertTokenResponse.getInstance(resp);
			assertNotNull(response);

			// ... from `byte[]` with bounds set
			response = GTCertTokenResponse.getInstance(resp, 0, resp.length);
			assertNotNull(response);

			// ... from part of byte[]
			response = GTCertTokenResponse.getInstance(modified, 1, resp.length);
			assertNotNull(response);

			// ... from InputStream
			InputStream in = new ByteArrayInputStream(resp);
			response = GTCertTokenResponse.getInstance(in);
			assertNotNull(response);
		} catch (GTException e) {
			e.printStackTrace();
			fail("cannot create cert token response: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create cert token response: " + e.getMessage());
		}

		// Try to build cert token response object from invalid data

		// ... from byte[]
		try {
			GTCertTokenResponse.getInstance(modified);
			fail("rubbush accepted as certification token response bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from part of byte[]
		try {
			GTCertTokenResponse.getInstance(modified, 2, modified.length);
			fail("rubbush accepted as certification token response bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from InputStream
		try {
			InputStream in = new ByteArrayInputStream(modified);
			GTCertTokenResponse.getInstance(in);
			fail("rubbush accepted as certification token response stream");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code getStatusCode()} and {@code getFailCode()} methods.
	 */
	public void testCodes() {
		// Build timestamps
		GTTimestamp timestamp = null;
		GTTimestamp extendedTimestamp = null;
		GTTimestamp freshTimestamp = null;
		try {
			timestamp = GTTimestamp.getInstance(TIMESTAMP);
			extendedTimestamp = GTTimestamp.getInstance(EXTENDED_TIMESTAMP);
		} catch (GTException e) {
			fail("cannot build timestamp: " + e.getMessage());
		}

		// Prepare cert token request
		byte[] req = timestamp.composeExtensionRequest();

		// Prepare modified data
		byte[] modified = new byte[req.length + 2];
		modified[0] = 65;
		System.arraycopy(req, 0, modified, 1, req.length);
		modified[req.length + 1] = 66;

		// Prepare fresh timestamp extension request
		byte[] freshExtReq = null;
		try {
			byte[] freshReq = GTTimestamp.composeRequest(DATA_HASH);
			byte[] freshResp = Helper.sendHttpRequest(STAMPER_URL, freshReq);
			freshTimestamp = GTTimestampResponse.getInstance(freshResp).getTimestamp();
			freshExtReq = freshTimestamp.composeExtensionRequest();
		} catch (GTException e) {
			fail("cannot build timestamp: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}

		try {
			// Status 0: Granted
			byte[] resp = Helper.sendHttpRequest(EXTENDER_URL, req);
			GTCertTokenResponse response = GTCertTokenResponse.getInstance(resp);
			assertEquals(0, response.getStatusCode());
			assertEquals(-1, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());

			// Status 0: Granted (extended timestamp can be extended again)
			resp = Helper.sendHttpRequest(EXTENDER_URL, extendedTimestamp.composeExtensionRequest());
			response = GTCertTokenResponse.getInstance(resp);
			assertEquals(0, response.getStatusCode());
			assertEquals(-1, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());

			// Status 2: Rejected; Fail 5: Bad data format
			resp = Helper.sendHttpRequest(EXTENDER_URL, modified);
			response = GTCertTokenResponse.getInstance(resp);
			assertEquals(2, response.getStatusCode());
			assertEquals(5, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());

			// Status 2: Rejected; Fail 100: Too fresh (timestamp too fresh to be extended)
			resp = Helper.sendHttpRequest(EXTENDER_URL, freshExtReq);
			response = GTCertTokenResponse.getInstance(resp);
			assertEquals(2, response.getStatusCode());
			assertEquals(100, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}
	}
}
