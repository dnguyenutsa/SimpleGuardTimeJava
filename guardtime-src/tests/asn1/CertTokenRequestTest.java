/*
 * $Id: CertTokenRequestTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.math.BigInteger;
import java.util.Arrays;

import junit.framework.TestCase;

import com.guardtime.asn1.CertTokenRequest;
import com.guardtime.asn1.TimestampRequest;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link CertTokenRequest} tests.
 */
public class CertTokenRequestTest
extends TestCase {
	private static final byte[] CERT_TOKEN_REQUEST = Base64.decode("MAkCAQECBEt4joA=");
	private static final BigInteger HISTORY_ID = BigInteger.valueOf(1266192000L);



	/**
	 * Tests {@link CertTokenRequest#compose(BigInteger)} method.
	 */
	public void testCompose() {
		// Make sure illegal arguments are handled correctly
		try {
			CertTokenRequest.compose(null);
			fail("null accepted as history identifier");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build request from valid components
		CertTokenRequest.compose(HISTORY_ID);
	}

	/**
	 * Tests {@link TimestampRequest#getDerEncoded()} method.
	 */
	public void testGetDerEncoded() {
		CertTokenRequest request = CertTokenRequest.compose(HISTORY_ID);
		assertTrue(Arrays.equals(CERT_TOKEN_REQUEST, request.getDerEncoded()));
	}
}
