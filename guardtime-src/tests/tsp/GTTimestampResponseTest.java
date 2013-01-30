/*
 * $Id: GTTimestampResponseTest.java 260 2012-02-25 13:04:02Z ahto.truu $
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

import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.util.Log;



/**
 * {@code com.guardtime.tsp.GTTimestampResponse} tests.
 */
public class GTTimestampResponseTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	private static final String STAMPER_URL = "http://stamper.guardtime.net/gt-signingservice";



	/**
	 * Tests {@code getInstance()} methods.
	 */
	public void testTimestampResponseInit() {
		// Make sure illegal arguments are handled correctly
		try {
			GTTimestampResponse.getInstance((byte[]) null);
			fail("null accepted as timestamp response bytes");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTTimestampResponse.getInstance((byte[]) null, 0, 1);
			fail("null accepted as timestamp response bytes");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTTimestampResponse.getInstance((InputStream) null);
			fail("null accepted as timestamp response bytes");
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Get timestamp response
		byte[] resp = null;
		try {
			byte[] req = GTTimestamp.composeRequest(DATA_HASH);
			resp = Helper.sendHttpRequest(STAMPER_URL, req);
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
			GTTimestampResponse response = GTTimestampResponse.getInstance(resp);
			assertNotNull(response);

			// ... from `byte[]` with bounds set
			response = GTTimestampResponse.getInstance(resp, 0, resp.length);
			assertNotNull(response);

			// ... from part of byte[]
			response = GTTimestampResponse.getInstance(modified, 1, resp.length);
			assertNotNull(response);

			// ... from InputStream
			InputStream in = new ByteArrayInputStream(resp);
			response = GTTimestampResponse.getInstance(in);
			assertNotNull(response);
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		}

		// Try to build timestamp response object from invalid data

		// ... from byte[]
		try {
			GTTimestampResponse.getInstance(modified);
			fail("rubbish accepted as timestamp response bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from part of byte[]
		try {
			GTTimestampResponse.getInstance(modified, 2, modified.length);
			fail("rubbish accepted as timestamp response bytes");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// ... from InputStream
		try {
			InputStream in = new ByteArrayInputStream(modified);
			GTTimestampResponse.getInstance(in);
			fail("rubbish accepted as timestamp response stream");
		} catch (GTException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code getTimestamp()} method.
	 */
	public void testGetTimestamp() {
		try {
			// Get timestamp response
			byte[] req = GTTimestamp.composeRequest(DATA_HASH);
			byte[] resp = Helper.sendHttpRequest(STAMPER_URL, req);

			// Build timestamp response object
			GTTimestampResponse response = GTTimestampResponse.getInstance(resp);

			// Get timestamp
			assertNotNull(response.getTimestamp());
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}
	}

	/**
	 * Tests {@code getStatusCode()} and {@code getFailCode()} methods.
	 */
	public void testCodes() {
		byte[] req = GTTimestamp.composeRequest(DATA_HASH);

		// Prepare modified data
		byte[] modified = new byte[req.length + 2];
		modified[0] = 65;
		System.arraycopy(req, 0, modified, 1, req.length);
		modified[req.length + 1] = 66;

		try {
			// Status 0: Granted
			byte[] resp = Helper.sendHttpRequest(STAMPER_URL, req);
			GTTimestampResponse response = GTTimestampResponse.getInstance(resp);
			assertEquals(0, response.getStatusCode());
			assertEquals(-1, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());
			assertNotNull(response.getTimestamp());

			// Status 2: Rejected; Fail 5: Bad data format
			resp = Helper.sendHttpRequest(STAMPER_URL, modified);
			response = GTTimestampResponse.getInstance(resp);
			assertEquals(2, response.getStatusCode());
			assertEquals(5, response.getFailCode());
			Log.debug("[DBG] status: " + response.getStatusMessage() + ", fail: " + response.getFailMessage());
			assertNull(response.getTimestamp());
		} catch (GTException e) {
			fail("cannot create timestamp response: " + e.getMessage());
		} catch (IOException e) {
			fail("network error: " + e.getMessage());
		}
	}
}
