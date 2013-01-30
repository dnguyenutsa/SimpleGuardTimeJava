/*
 * $Id: HttpStamperTest.java 260 2012-02-25 13:04:02Z ahto.truu $
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
import java.net.SocketTimeoutException;
import java.net.URL;

import junit.framework.TestCase;

import com.guardtime.transport.HttpStamper;
import com.guardtime.transport.ResponseHandler;
import com.guardtime.tsp.GTCertTokenResponse;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link HttpTimestamper} tests.
 */
public class HttpStamperTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	static final byte[] TIMESTAMP = Base64.decode("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");

	private static final String STAMPER_URL = "http://stamper.guardtime.net/gt-signingservice";
	private static final String VERIFIER_URL = "http://verifier.guardtime.net/gt-extendingservice";



	/**
	 * Tests {@link HttpStamper#addTimestampRequest(GTDataHash, URL, long)}
	 * method.
	 */
	public void testAddTimestampRequest()
	throws GTException, IOException {
		URL url = new URL(STAMPER_URL);
		HttpStamper stamper = HttpStamper.getInstance();
		ResponseHandler handler = stamper.addTimestampRequest(DATA_HASH, url, 0);
		assertNotNull(handler);
	}

	/**
	 * Tests {@link HttpStamper#addExtensionRequest(GTTimestamp, URL, long)}
	 * method.
	 */
	public void testAddExtensionRequest()
	throws GTException, IOException {
		URL url = new URL(VERIFIER_URL);
		GTTimestamp timestamp = GTTimestamp.getInstance(TIMESTAMP);
		HttpStamper stamper = HttpStamper.getInstance();
		ResponseHandler handler = stamper.addExtensionRequest(timestamp, url, 0);
		assertNotNull(handler);
	}

	/**
	 * Tests {@link HttpStamper#receiveTimestampResponse(ResponseHandler, long)}
	 * method.
	 */
	public void testRecieveTimestampResponse()
	throws GTException, IOException {
		URL url = new URL(STAMPER_URL);
		HttpStamper stamper = HttpStamper.getInstance();

		// Test transaction timeout
		try {
			ResponseHandler handler = stamper.addTimestampRequest(DATA_HASH, url, 1); // 1 ms
			HttpStamper.receiveTimestampResponse(handler, 0);
			fail("Transaction timeout not respected");
		} catch (SocketTimeoutException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Test response polling timeout
		{
			ResponseHandler handler = stamper.addTimestampRequest(DATA_HASH, url, 0);
			GTTimestampResponse response = HttpStamper.receiveTimestampResponse(handler, 1); // 1 ms
			assertNull("Polling timeout not respected", response);
		}

		// Test blocking use
		{
			ResponseHandler handler = stamper.addTimestampRequest(DATA_HASH, url, 0);
			GTTimestampResponse response = HttpStamper.receiveTimestampResponse(handler, 0);
			assertEquals(0, response.getStatusCode());
		}
	}

	/**
	 * Tests {@link HttpStamper#receiveTimestampResponse(ResponseHandler, long)}
	 * method.
	 */
	public void testRecieveExtensionResponse()
	throws GTException, IOException {
		URL url = new URL(VERIFIER_URL);
		GTTimestamp timestamp = GTTimestamp.getInstance(TIMESTAMP);
		HttpStamper stamper = HttpStamper.getInstance();

		// Test transaction timeout
		try {
			ResponseHandler handler = stamper.addExtensionRequest(timestamp, url, 1); // 1 ms
			HttpStamper.receiveExtensionResponse(handler, 0);
			fail("Transaction timeout not respected");
		} catch (SocketTimeoutException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Test response polling timeout
		{
			ResponseHandler handler = stamper.addExtensionRequest(timestamp, url, 0);
			GTCertTokenResponse response = HttpStamper.receiveExtensionResponse(handler, 1); // 1 ms
			assertNull("Polling timeout not respected", response);
		}

		// Test blocking use
		{
			ResponseHandler handler = stamper.addExtensionRequest(timestamp, url, 0);
			GTCertTokenResponse response = HttpStamper.receiveExtensionResponse(handler, 0);
			assertEquals(0, response.getStatusCode());
		}
	}
}
