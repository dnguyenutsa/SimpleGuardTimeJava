/*
 * $Id: SocketClientTest.java 244 2011-11-25 22:20:48Z ahto.truu $
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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import com.guardtime.transport.ResponseHandler;
import com.guardtime.transport.SocketClient;
import com.guardtime.util.Log;



/**
 * {@link SocketClient} tests.
 */
public class SocketClientTest
extends TestCase {
	private static final String PUBFILE_ADDR = "verify.guardtime.com";
	static final int PORT = 80;
	static final byte[] REQUEST = ("GET /gt-controlpublications.bin HTTP/1.0\r\nHost: " + PUBFILE_ADDR + "\r\n\r\n").getBytes();



	/**
	 * Tests {@link SocketClient#start()}
	 */
	public void testStart() {
		SocketClient socketClient = null;
		InetSocketAddress address = new InetSocketAddress(PUBFILE_ADDR, PORT);

		// Init HTTP client
		try {
			socketClient = new SocketClient();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Start client, make sure it starts
		socketClient.start();
		try {
			socketClient.addRequest(address, REQUEST, 0);
		} catch(IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests
	 * {@link SocketClient#sendRequest(InetSocketAddress, byte[], ResponseHandler, int)}
	 * and {@link ResponseHandler#receiveResponse()} methods.
	 */
	public void testSendRequest() {
		SocketClient socketClient = null;

		// Init and start HTTP client
		try {
			socketClient = new SocketClient();
			socketClient.start();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		InetSocketAddress address = new InetSocketAddress(PUBFILE_ADDR, PORT);

		// Send request
		try {
			ResponseHandler responseHandler = socketClient.addRequest(address, REQUEST, 0);
			byte[] response = responseHandler.receiveResponse(0);
			assertTrue(new String(response).startsWith("HTTP/1.0 200 OK"));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Test transaction timeout
		try {
			ResponseHandler responseHandler = socketClient.addRequest(address, REQUEST, 1); // 1 ms
			responseHandler.receiveResponse(0);
			fail("Transaction timeout not respected");
		} catch (SocketTimeoutException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Test response polling timeout
		try {
			ResponseHandler responseHandler = socketClient.addRequest(address, REQUEST, 0);
			byte[] response = responseHandler.receiveResponse(1); // 1 ms
			assertNull("Read timeout not respected", response);
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
			// Init and start HTTP client
			SocketClient socketClient = new SocketClient();
			socketClient.start();

			for (int i = 0; i < n; i++) {
				// Send request
				InetSocketAddress address = new InetSocketAddress(PUBFILE_ADDR, PORT);
				responseHandlers[i] = socketClient.addRequest(address, REQUEST, 0);

				Log.debug("Sent request " + i);
			}

			for (int i = n - 1; i >= 0; i--) {
				// Receive response
				byte[] response = responseHandlers[i].receiveResponse(0);
				assertTrue(new String(response).startsWith("HTTP/1.0 200 OK"));

				Log.debug("Received response " + i);
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
