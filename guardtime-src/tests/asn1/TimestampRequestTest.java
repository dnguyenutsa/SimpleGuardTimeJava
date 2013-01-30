/*
 * $Id: TimestampRequestTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.util.Arrays;

import junit.framework.TestCase;

import com.guardtime.asn1.TimestampRequest;
import com.guardtime.util.Base16;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link TimestampRequest} tests.
 */
public class TimestampRequestTest
extends TestCase {
	private static final byte[] TIMESTAMP_REQUEST = Base64.decode("MDQCAQEwLzALBglghkgBZQMEAgEEIAAZap90D/GUJFCQZXCqmIwwHist0XoohhX3UitkvZrw");
	private static final String HASH_ALGORITHM = "2.16.840.1.101.3.4.2.1"; // SHA256
	private static final byte[] HASHED_MESSAGE = Base16.decode("00196a9f740ff1942450906570aa988c301e2b2dd17a288615f7522b64bd9af0"); // sha256("Tere\n")



	/**
	 * Tests {@link TimestampRequest#compose(String, byte[])} method.
	 */
	public void testCompose() {
		// Make sure illegal arguments are handled correctly
		try {
			TimestampRequest.compose(null, HASHED_MESSAGE);
			fail("null accepted as hash algorithm");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			TimestampRequest.compose("rubbish", HASHED_MESSAGE);
			fail("rubbish accepted as hash algorithm");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			TimestampRequest.compose(HASH_ALGORITHM, null);
			fail("null accepted as hashed message");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build timestamp request from valid components
		TimestampRequest.compose(HASH_ALGORITHM, HASHED_MESSAGE);
	}

	/**
	 * Tests {@link TimestampRequest#getDerEncoded()} method.
	 */
	public void testGetDerEncoded() {
		TimestampRequest request = TimestampRequest.compose(HASH_ALGORITHM, HASHED_MESSAGE);
		assertTrue(Arrays.equals(TIMESTAMP_REQUEST, request.getDerEncoded()));
	}
}
