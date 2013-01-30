/*
 * $Id: Util.java 169 2011-03-03 18:45:00Z ahto.truu $
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

package com.guardtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

/**
 * A collection of miscellaneous commonly used utility functions.
 */
public abstract class Util {
	/**
	 * The default buffer size for the data read/copy operations in this class.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8192;



	/**
	 * Creates a copy of the given byte array.
	 *
	 * @param b
	 *            the array to copy.
	 * @return copy of {@code b}, or {@code null} if {@code b} is {@code null}.
	 */
	public static byte[] copyOf(byte[] b) {
		if (b == null) {
			return null;
		}
		return copyOf(b, 0, b.length);
	}

	/**
	 * Creates a copy of a section of the given byte array.
	 *
	 * @param b
	 *            the array to copy.
	 * @param off
	 *            the start offset of the data within {@code b}.
	 * @param len
	 *            the number of bytes to copy.
	 * @return copy of the requested section of {@code b}.
	 * @throws NullPointerException
	 *             if {@code b} is {@code null}.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the half-range {@code [off..off+len)} is not in
	 *             {@code [0..b.length)}.
	 */
	public static byte[] copyOf(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		byte[] copy = new byte[len];
		System.arraycopy(b, off, copy, 0, len);
		return copy;
	}

	/**
	 * Converts {@code value} to two-byte array.
	 * <p>
	 * Bytes are returned in network byte order (ordered from the most to the
	 * least significant byte).
	 *
	 * @param value
	 *            the value to convert.
	 * @return the converted bytes as an array.
	 */
	public static byte[] toByteArray(short value) {
		return new byte[] { (byte) (value >>> 8), (byte) value };
	}

	/**
	 * Converts {@code value} to four-byte array.
	 * <p>
	 * Bytes are returned in network byte order (ordered from the most to the
	 * least significant byte).
	 *
	 * @param value
	 *            the value to convert.
	 * @return the converted bytes as an array.
	 */
	public static byte[] toByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	/**
	 * Converts {@code value} to eight-byte array.
	 * <p>
	 * Bytes are returned in network byte order (ordered from the most to the
	 * least significant byte).
	 *
	 * @param value
	 *            the value to convert.
	 * @return the converted bytes as an array.
	 */
	public static byte[] toByteArray(long value) {
		return new byte[] { (byte) (value >>> 56), (byte) (value >>> 48),
				(byte) (value >>> 40), (byte) (value >>> 32),
				(byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	/**
	 * Converts the first two bytes of {@code b} to a 16-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @return the converted value.
	 */
	public static short toShort(byte[] b) {
		return toShort(b, 0);
	}

	/**
	 * Converts two bytes of {@code b}, starting from {@code offset}, to a
	 * 16-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @param offset
	 *            start offset in the buffer.
	 * @return the converted value.
	 */
	public static short toShort(byte[] b, int offset) {
		return (short) ((b[offset++] << 8) + (b[offset++] & 0xff));
	}

	/**
	 * Converts the first four bytes of {@code b} to a 32-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @return the converted value.
	 */
	public static int toInt(byte[] b) {
		return toInt(b, 0);
	}

	/**
	 * Converts four bytes of {@code b}, starting from {@code offset}, to a
	 * 32-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @param offset
	 *            start offset in the buffer.
	 * @return the converted value.
	 */
	public static int toInt(byte[] b, int offset) {
		return (toShort(b, offset) << 16) + (toShort(b, offset + 2) & 0xffff);
	}

	/**
	 * Converts the first eight bytes of {@code b} to a 64-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @return the converted value.
	 */
	public static long toLong(byte[] b) {
		return toLong(b, 0);
	}

	/**
	 * Converts eight bytes of {@code b}, starting from {@code offset}, to a
	 * 64-bit signed integer.
	 * <p>
	 * Assumes network byte order (ordered from the most to the least
	 * significant byte).
	 *
	 * @param b
	 *            the buffer to read from.
	 * @param offset
	 *            start offset in the buffer.
	 * @return the converted value.
	 */
	public static long toLong(byte[] b, int offset) {
		return ((long) toInt(b, offset) << 32)
				+ (toInt(b, offset + 4) & 0xffffffffL);
	}

	/**
	 * Computes the CRC32 checksum for the given data.
	 * <p>
	 * The checksum is appended to the original data and the result returned in
	 * a newly allocated array.
	 *
	 * @param b
	 *            the data to compute the checksum for.
	 * @return an array containing the original data with the CRC appended.
	 * @throws NullPointerException
	 *             if {@code b} is {@code null}.
	 */
	public static byte[] addCrc32(byte[] b) {
		return addCrc32(b, 0, b.length);
	}

	/**
	 * Computes the CRC32 checksum for {@code len} bytes of the given data,
	 * starting from {@code off}.
	 * <p>
	 * The checksum is appended to the original data and the result returned in
	 * a newly allocated array.
	 *
	 * @param b
	 *            the buffer containing the data to compute the checksum for.
	 * @param off
	 *            start offset in the buffer.
	 * @param len
	 *            number of bytes to include in the checksum.
	 * @return an array containing specified data bytes with CRC appended.
	 * @throws NullPointerException
	 *             if {@code b} is {@code null}.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the half-range {@code [off..off+len)} is not in
	 *             {@code [0..b.length)}.
	 */
	public static byte[] addCrc32(byte[] b, int off, int len) {
		byte[] res = new byte[len + 4];
		CRC32 crc32 = new CRC32();
		crc32.update(b, off, len);
		byte[] crc = toByteArray((int) (crc32.getValue() & 0xffffffffL));
		System.arraycopy(b, off, res, 0, len);
		System.arraycopy(crc, 0, res, len, 4);
		return res;
	}

	/**
	 * Copies all available data from {@code in} to {@code out}.
	 * <p>
	 * Allocates a temporary memory buffer of {@link #DEFAULT_BUFFER_SIZE} bytes
	 * for this.
	 *
	 * @param in
	 *            input stream to copy data from.
	 * @param out
	 *            output stream to copy data to.
	 * @return the number of bytes actually copied.
	 * @throws IOException
	 *             if one is thrown by either {@code in} or {@code out}.
	 */
	public static int copyData(InputStream in, OutputStream out)
	throws IOException {
		return copyData(in, out, -1, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies up to {@code limit} bytes of data from {@code in} to {@code out}.
	 * <p>
	 * May copy less than {@code limit} bytes if {@code in} does not have that
	 * much data available.
	 * <p>
	 * Allocates a temporary memory buffer of {@link #DEFAULT_BUFFER_SIZE} bytes
	 * for this.
	 *
	 * @param in
	 *            input stream to copy data from.
	 * @param out
	 *            output stream to copy data to.
	 * @param limit
	 *            maximum number of bytes to copy.
	 * @return the number of bytes actually copied.
	 * @throws IOException
	 *             if one is thrown by either {@code in} or {@code out}.
	 */
	public static int copyData(InputStream in, OutputStream out, int limit)
	throws IOException {
		return copyData(in, out, limit, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Copies up to {@code limit} bytes of data from {@code in} to {@code out}.
	 * <p>
	 * May copy less than {@code limit} bytes if {@code in} does not have that
	 * much data available.
	 * <p>
	 * Allocates a temporary memory buffer of {@code bufSize} bytes for this.
	 *
	 * @param in
	 *            input stream to copy data from.
	 * @param out
	 *            output stream to copy data to.
	 * @param limit
	 *            maximum number of bytes to copy ({@code -1} to copy all bytes).
	 * @param bufSize
	 *            size of the buffer to allocate (larger buffer may speed up
	 *            the process).
	 * @return the number of bytes actually copied.
	 * @throws IOException
	 *             if one is thrown by either {@code in} or {@code out}.
	 */
	public static int copyData(InputStream in, OutputStream out, int limit,
			int bufSize) throws IOException {
		if (bufSize < 1) {
			throw new IllegalArgumentException("Invalid buffer size: " + bufSize);
		}

		byte buf[] = new byte[bufSize];
		int total = 0;
		while (limit < 0 || total < limit) {
			int maxRead = ((limit < 0) ? buf.length : Math.min(limit - total, buf.length));
			int count = in.read(buf, 0, maxRead);
			if (count < 1) {
				break;
			}
			out.write(buf, 0, count);
			total += count;
		}
		return total;
	}

	/**
	 * Reads all data from the given input stream.
	 *
	 * @param in
	 *            the stream to read from.
	 * @return the data.
	 * @throws IOException
	 *             if there was an error reading from the stream.
	 */
	public static byte[] readAll(InputStream in)
	throws IOException {
		return readAll(in, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Reads all data from the given input stream using a buffer of the given size.
	 *
	 * @param in
	 *            the stream to read from.
	 * @param bufSize
	 *            size of the buffer to use.
	 * @return the data.
	 * @throws IOException
	 *             if there was an error reading from the stream.
	 */
	public static byte[] readAll(InputStream in, int bufSize)
	throws IOException {
		if (bufSize < 1) {
			throw new IllegalArgumentException("Invalid buffer size: " + bufSize);
		}

		byte[] res = new byte[0];
		byte[] buf = new byte[bufSize];

		int bytesRead;
		while ((bytesRead = in.read(buf)) != -1) {
			byte[] tmp = new byte[res.length + bytesRead];
			System.arraycopy(res, 0, tmp, 0, res.length);
			System.arraycopy(buf, 0, tmp, res.length, bytesRead);
			res = tmp;
		}
		in.close();
		return res;
	}

	/**
	 * Computes the greatest common divisor (GCD) of two integers.
	 * <p>
	 * Greatest common divisor is the largest integer that divides both numbers
	 * without remainder.
	 *
	 * @param a
	 *            the first integer.
	 * @param b
	 *            the second integer.
	 * @return the greatest common divisor of {@code a} and {@code b}, or
	 *         {@code 0}, if both {@code a} and {@code b} are {@code 0}.
	 */
	public static int gcd(int a, int b) {
		a = Math.abs(a);
		b = Math.abs(b);
		while (a > 0) {
			int c = b % a;
			b = a;
			a = c;
		}
		return b;
	}

	/**
	 * Computes the least common multiple (LCM) of two integers.
	 * <p>
	 * Least common multiple is the smallest positive integer that can be
	 * divided by both numbers without a remainder.
	 *
	 * @param a
	 *            the first integer.
	 * @param b
	 *            the second integer.
	 * @return the least common multiple of {@code a} and {@code b}, or
	 *         {@code 0}, if either {@code a} or {@code b} is {@code 0}.
	 * @throws ArithmeticException
	 *             when the result is too big to fit into an {@code int}.
	 */
	public static int lcm(int a, int b) {
		if (a == 0 || b == 0) {
			return 0;
		}
		a = Math.abs(a) / gcd(a, b);
		b = Math.abs(b);
		if (a > Integer.MAX_VALUE / b) {
			throw new ArithmeticException("Integer overflow");
		}
		return a * b;
	}

	/*
	 * This is here just to clean up the public JavaDoc.
	 */
	private Util() {
	}
}
