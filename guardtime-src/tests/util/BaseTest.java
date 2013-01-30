/*
 * $Id: BaseTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package tests.util;

import java.util.Arrays;

import com.guardtime.util.Base16;
import com.guardtime.util.Base32;
import com.guardtime.util.Base64;

import junit.framework.TestCase;



/**
 * JUnit test cases for the RFC 4684 base-X encoder/decoder classes.
 */
public class BaseTest extends TestCase {

	/**
	 * Base-16 test vectors from RFC 4648.
	 */
	private static final String[][] test16 = {
		{"", ""},
		{"f", "66"},
		{"fo", "666F"},
		{"foo", "666F6F"},
		{"foob", "666F6F62"},
		{"fooba", "666F6F6261"},
		{"foobar", "666F6F626172"}
	};

	/**
	 * Tests base-16 encoding.
	 */
	public void testEncode16() {
		for (int i = 0; i < test16.length; i++) {
			assertEquals("Test vector " + i,
					test16[i][1],
					Base16.encode(test16[i][0].getBytes()));
		}
	}

	/**
	 * Tests base-16 decoding.
	 */
	public void testDecode16() {
		for (int i = 0; i < test16.length; i++) {
			assertTrue(Arrays.equals(test16[i][0].getBytes(), Base16.decode(test16[i][1].toUpperCase())));
			assertTrue(Arrays.equals(test16[i][0].getBytes(), Base16.decode(test16[i][1].toLowerCase())));
		}
	}

	/**
	 * Base-32 test vectors from RFC 4648.
	 */
	private static final String[][] test32 = {
		{"", ""},
		{"f", "MY======"},
		{"fo", "MZXQ===="},
		{"foo", "MZXW6==="},
		{"foob", "MZXW6YQ="},
		{"fooba", "MZXW6YTB"},
		{"foobar", "MZXW6YTBOI======"}
	};

	/**
	 * Tests base-32 encoding.
	 */
	public void testEncode32() {
		for (int i = 0; i < test32.length; i++) {
			assertEquals(test32[i][1], Base32.encode(test32[i][0].getBytes()));
		}
	}

	/**
	 * Tests base-32 decoding.
	 */
	public void testDecode32() {
		for (int i = 0; i < test32.length; i++) {
			assertTrue(Arrays.equals(test32[i][0].getBytes(), Base32.decode(test32[i][1].toUpperCase())));
			assertTrue(Arrays.equals(test32[i][0].getBytes(), Base32.decode(test32[i][1].toLowerCase())));
		}
	}

	/**
	 * Base-64 test vectors from RFC 4648.
	 */
	private static final String[][] test64 = {
		{"", ""},
		{"f", "Zg=="},
		{"fo", "Zm8="},
		{"foo", "Zm9v"},
		{"foob", "Zm9vYg=="},
		{"fooba", "Zm9vYmE="},
		{"foobar", "Zm9vYmFy"}
	};

	/**
	 * Tests base-64 encoding.
	 */
	public void testEncode64() {
		for (int i = 0; i < test64.length; i++) {
			assertEquals(test64[i][1], Base64.encode(test64[i][0].getBytes()));
		}
	}

	/**
	 * Tests base-64 decoding.
	 */
	public void testDecode64() {
		for (int i = 0; i < test64.length; i++) {
			assertTrue(Arrays.equals(test64[i][0].getBytes(), Base64.decode(test64[i][1])));
		}
	}

}
