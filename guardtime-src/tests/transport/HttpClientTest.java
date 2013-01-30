/*
 * $Id: HttpClientTest.java 260 2012-02-25 13:04:02Z ahto.truu $
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

import com.guardtime.transport.HttpClient;
import com.guardtime.transport.ResponseHandler;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.util.Log;



/**
 * {@link HttpClient} tests.
 */
public class HttpClientTest
extends TestCase {
	private static final byte[] DATA = "Tere\n".getBytes();
	private static final GTHashAlgorithm HASH_ALG = GTHashAlgorithm.SHA256;
	private static final GTDataHash DATA_HASH = new GTDataHash(HASH_ALG).update(DATA).close();

	private static final String STAMPER_URL = "http://stamper.guardtime.net/gt-signingservice";
	private static final String PUBFILE_URL = "http://verify.guardtime.com/gt-controlpublications.bin";



	/**
	 * Tests {@link HttpClient#start()}
	 */
	public void testStart() {
		HttpClient httpClient = null;
		URL url = null;

		// Init HTTP client
		try {
			url = new URL(PUBFILE_URL);
			httpClient = new HttpClient();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Make sure client is NOT running yet
		try {
			httpClient.addHttpRequest(url, 0);
		} catch (IllegalStateException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Start client, make sure it starts
		httpClient.start();
		try {
			httpClient.addHttpRequest(url, 0);
		} catch(IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests {@link HttpClient#addHttpRequest(URL, long)},
	 * {@link HttpClient#addHttpRequest(URL, byte[], long)},
	 * {@link ResponseHandler#receiveResponse(long)} and
	 * {@link HttpClient#getResponseContents(byte[])} methods.
	 */
	public void testSendRequest() {
		HttpClient httpClient = null;
		URL url = null;

		// Init and start HTTP client
		try {
			httpClient = new HttpClient();
			httpClient.start();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Send GET request
		try {
			url = new URL(PUBFILE_URL);
			ResponseHandler responseHandler = httpClient.addHttpRequest(url, 0);
			byte[] response = responseHandler.receiveResponse(0);
			String res = new String(response);
			assertTrue(res.startsWith("HTTP/1.0 200 OK") || res.startsWith("HTTP/1.1 200 OK"));

			// Check response contents
			GTPublicationsFile.getInstance(HttpClient.getResponseContents(response));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Send POST request
		try {
			byte[] request = GTTimestamp.composeRequest(DATA_HASH);
			url = new URL(STAMPER_URL);
			ResponseHandler responseHandler = httpClient.addHttpRequest(url, request, 0);
			byte[] response = responseHandler.receiveResponse(0);
			String res = new String(response);
			assertTrue(res.startsWith("HTTP/1.0 200 OK") || res.startsWith("HTTP/1.1 200 OK"));

			// Check response contents
			GTTimestampResponse.getInstance(HttpClient.getResponseContents(response));
		} catch (GTException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test multiple parallel requests;
	 */
	public void testMultipleRequests() {
		int n = 5;

		ResponseHandler[] responseHandlers = new ResponseHandler[n];

		try {
			for (int i = 0; i < n; i++) {
				// Init and start HTTP client
				HttpClient httpClient = new HttpClient();
				httpClient.start();

				// Send request
				responseHandlers[i] = httpClient.addHttpRequest(new URL(PUBFILE_URL), 0);

				Log.debug("Sent request " + i);
			}

			for (int i = n - 1; i >= 0; i--) {
				// Receive response
				byte[] response = responseHandlers[i].receiveResponse(0);
				String res = new String(response);
				assertTrue(res.startsWith("HTTP/1.0 200 OK") || res.startsWith("HTTP/1.1 200 OK"));

				Log.debug("Received response " + i);
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
