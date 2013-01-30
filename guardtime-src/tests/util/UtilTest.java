/*
 * $Id: UtilTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import com.guardtime.util.Util;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * JUnit test cases for the utility functions.
 */
public class UtilTest extends TestCase {

	/**
	 * A common data string for various test cases.
	 * <p>
	 * In case you care about parity or even division, note that the length is
	 * 21=3*7 characters.
	 */
	private static final String testString = "This is a test string";

	/**
	 * Tests {@link Util#copyOf(byte[])} and
	 * {@link Util#copyOf(byte[], int, int)}.
	 */
	public void testCopyOf() {
		byte[] data = testString.getBytes();
		assertTrue(Arrays.equals(data, Util.copyOf(data)));
		int len = data.length / 2;
		for (int i = 0; i + len <= data.length; ++i) {
			assertTrue(Arrays.equals(testString.substring(i, i + len).getBytes(), Util.copyOf(data, i, len)));
		}
	}

	/**
	 * {@code short} test data.
	 */
	private static short[] testShort = {
		(short) 0x1234, // all bytes different
		(short) 0x0000, // all zero bits
		(short) 0xffff, // all one bits
		(short) 0x00ff, // 0th byte used
		(short) 0xff00, // 1st byte used
		(short) 0x7fff, // maximal positive value
		(short) 0x8000, // minimal negative value
	};

	/**
	 * Tests conversions between byte arrays and {@code short}s.
	 */
	public void testByteArrayShort() {
		for (int i = 0; i < testShort.length; i++) {
			assertEquals("Test case " + i,
					testShort[i], Util.toShort(Util.toByteArray(testShort[i])));
		}
	}

	/**
	 * {@code int} test data.
	 */
	private static int[] testInt = {
		0x12345678, // all bytes different
		0x00000000, // all zero bits
		0xffffffff, // all one bits
		0x000000ff, // 0th byte used
		0x0000ff00, // 1st byte used
		0x00ff0000, // 2nd byte used
		0xff000000, // 3rd byte used
		0x7fffffff, // maximal positive value
		0x80000000, // minimal negative value
	};

	/**
	 * Tests conversions between byte arrays and {@code int}s.
	 */
	public void testByteArrayInt() {
		for (int i = 0; i < testInt.length; i++) {
			assertEquals("Test case " + i,
					testInt[i], Util.toInt(Util.toByteArray(testInt[i])));
		}
	}

	/**
	 * {@code long} test data.
	 */
	private static long[] testLong = {
		0x1234567890abcdefL, // all bytes different
		0x0000000000000000L, // all zero bits
		0xffffffffffffffffL, // all one bits
		0x00000000000000ffL, // 0th byte used
		0x000000000000ff00L, // 1st byte used
		0x0000000000ff0000L, // 2nd byte used
		0x00000000ff000000L, // 3rd byte used
		0x000000ff00000000L, // 4th byte used
		0x0000ff0000000000L, // 5th byte used
		0x00ff000000000000L, // 6th byte used
		0xff00000000000000L, // 7th byte used
		0x7fffffffffffffffL, // maximal positive value
		0x8000000000000000L, // minimal negative value
	};

	/**
	 * Tests conversions between byte arrays and {@code long}s.
	 */
	public void testByteArrayLong() {
		for (int i = 0; i < testLong.length; i++) {
			assertEquals("Test case " + i,
					testLong[i], Util.toLong(Util.toByteArray(testLong[i])));
		}
	}

	/**
	 * CRC32 test vectors.
	 */
	private static final String[][] testCrc32 = {
		{"", "0"},
		{"a", "e8b7be43"},
		{"abc", "352441c2"},
		{"message digest", "20159d7f"},
		{"abcdefghijklmnopqrstuvwxyz", "4c2750bd"},
		{"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "1fc2e6d2"},
		{"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "7ca94a72"},
		{"123456789", "cbf43926"},
	};

	/**
	 * Tests {@link Util#addCrc32(byte[])}
	 */
	public void testAddCrc32() {
		for (int i = 0; i < testCrc32.length; i++) {
			byte[] data = Util.addCrc32(testCrc32[i][0].getBytes());
			assertTrue(Arrays.equals(testCrc32[i][0].getBytes(), Util.copyOf(data, 0, data.length - 4)));
			assertEquals(testCrc32[i][1], Integer.toHexString(Util.toInt(Util.copyOf(data, data.length - 4, 4))));
		}
	}

	/**
	 * Tests {@code copyData} methods.
	 */
	public void testCopyData()
	throws IOException {
		byte[] data = testString.getBytes();
		int[] limits = {1, data.length / 2, data.length * 2};
		int[] buffers = {1, data.length / 3, data.length * 3};
		int[] invalidBuffers = {-1, 0};
		ByteArrayInputStream in;
		ByteArrayOutputStream out;
		int bytesCopied;

		// Make sure non-positive buffer size values are not accepted
		for (int i = 0; i < invalidBuffers.length; i++) {
			try {
				in = new ByteArrayInputStream(data);
				out = new ByteArrayOutputStream();
				Util.copyData(in, out, 1, invalidBuffers[i]);
				fail("Negative limit accepted, this should not happen");
			} catch (IllegalArgumentException e) {
				// Exception thrown -- OK
			}
		}

		// Test with default limit and buffer size
		in = new ByteArrayInputStream(data);
		out = new ByteArrayOutputStream();
		bytesCopied = Util.copyData(in, out);
		assertEquals(data.length, bytesCopied);
		assertTrue(Arrays.equals(data, out.toByteArray()));

		// Test with various limits and various buffer size values
		for (int i = 0; i < limits.length; i++) {
			// ... with default buffer size value
			in = new ByteArrayInputStream(data);
			out = new ByteArrayOutputStream();
			bytesCopied = Util.copyData(in, out, limits[i]);
			assertTrue(limits[i] >= bytesCopied);
			assertTrue(Arrays.equals(Util.copyOf(data, 0, bytesCopied), out.toByteArray()));

			// ... with other buffer size value
			for (int j = 0; j < buffers.length; j++) {
				in = new ByteArrayInputStream(data);
				out = new ByteArrayOutputStream();
				bytesCopied = Util.copyData(in, out, limits[i], buffers[j]);
				assertTrue(limits[i] >= bytesCopied);
				assertTrue(Arrays.equals(Util.copyOf(data, 0, bytesCopied), out.toByteArray()));
			}
		}
	}

	/**
	 * Tests {@code readAll} methods.
	 */
	public void testReadAll()
	throws IOException {
		byte[] data = testString.getBytes();
		int[] buffers = {1, data.length / 3, data.length * 3};
		int[] invalidBuffers = {-1, 0};
		ByteArrayInputStream in;

		// Make sure non-positive buffer size values are not accepted
		for (int i = 0; i < invalidBuffers.length; i++) {
			in = new ByteArrayInputStream(data);
			try {
				Util.readAll(in, invalidBuffers[i]);
				fail("Non-positive buffer size accepted, this should not happen");
			} catch (IllegalArgumentException e) {
				// Exception thrown -- OK
			}
		}

		// Test with default buffer size
		in = new ByteArrayInputStream(data);
		assertTrue(Arrays.equals(data, Util.readAll(in)));

		// Test with various buffer values
		for (int i = 0; i < buffers.length; i++) {
			in = new ByteArrayInputStream(data);
			assertTrue(Arrays.equals(data, Util.readAll(in, buffers[i])));
		}
	}

	/**
	 * Tests {@link Util#gcd(int, int)}.
	 */
	public void testGcd() {
		assertEquals(0, Util.gcd(0, 0));
		assertEquals(3 * 5, Util.gcd(0, 3 * 5));
		assertEquals(3 * 5, Util.gcd(3 * 5, 0));
		assertEquals(1, Util.gcd(1, 3 * 5));
		assertEquals(1, Util.gcd(3 * 5, 1));
		assertEquals(1, Util.gcd(2 * 7, 3 * 5));
		assertEquals(1, Util.gcd(3 * 5, 2 * 7));
		assertEquals(3 * 5, Util.gcd(2 * 3 * 5, 3 * 5 * 7));
		assertEquals(3 * 5, Util.gcd(3 * 5 * 7, 2 * 3 * 5));
		assertEquals(3 * 5, Util.gcd(3 * 5, 3 * 5));
		assertEquals(3 * 5, Util.gcd(-3 * 5, 3 * 5));
		assertEquals(3 * 5, Util.gcd(3 * 5, -3 * 5));
		assertEquals(3 * 5, Util.gcd(-3 * 5, -3 * 5));
	}

	/**
	 * Tests {@link Util#lcm(int, int)}.
	 */
	public void testLcm() {
		assertEquals(0, Util.lcm(0, 0));
		assertEquals(0, Util.lcm(0, 3 * 5));
		assertEquals(0, Util.lcm(3 * 5, 0));
		assertEquals(3 * 5, Util.lcm(1, 3 * 5));
		assertEquals(3 * 5, Util.lcm(3 * 5, 1));
		assertEquals(2 * 3 * 5 * 7, Util.lcm(2 * 7, 3 * 5));
		assertEquals(2 * 3 * 5 * 7, Util.lcm(3 * 5, 2 * 7));
		assertEquals(2 * 3 * 5 * 7, Util.lcm(2 * 3 * 5, 3 * 5 * 7));
		assertEquals(2 * 3 * 5 * 7, Util.lcm(3 * 5 * 7, 2 * 3 * 5));
		assertEquals(3 * 5, Util.lcm(3 * 5, 3 * 5));
		assertEquals(3 * 5, Util.lcm(-3 * 5, 3 * 5));
		assertEquals(3 * 5, Util.lcm(3 * 5, -3 * 5));
		assertEquals(3 * 5, Util.lcm(-3 * 5, -3 * 5));
		try {
			Util.lcm(2 * 3 * 5 * 7 * 11, 13 * 17 * 19 * 23 * 29);
			fail();
		} catch (ArithmeticException x) {
			// nothing here
		}
		try {
			Util.lcm(13 * 17 * 19 * 23 * 29, 2 * 3 * 5 * 7 * 11);
			fail();
		} catch (ArithmeticException x) {
			// nothing here
		}
	}

}
