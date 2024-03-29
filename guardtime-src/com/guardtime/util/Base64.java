/*
 * $Id: Base64.java 169 2011-03-03 18:45:00Z ahto.truu $
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

/**
 * <a target="_blank" href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a>
 * base-64 encoding/decoding.
 *
 * @since 0.3
 */
public abstract class Base64 {

	/**
	 * The encoder/decoder instance.
	 */
	private static BaseX inst = new BaseX(
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
			true, '=');

	/**
	 * Encodes the given bytes into a base-64 string.
	 *
	 * @param in
	 *            the bytes to encode.
	 * @return the base-64 string.
	 */
	public static String encode(byte[] in) {
		return encode(in, 0, in.length);
	}

	/**
	 * Encodes the given bytes into a base-64 string.
	 *
	 * @param in
	 *            an array containing the bytes to encode.
	 * @param off
	 *            the start offset of the data within {@code in}.
	 * @param len
	 *            the number of bytes to encode.
	 * @return the base-64 string.
	 */
	public static String encode(byte[] in, int off, int len) {
		if (in == null) {
			return null;
		}
		return inst.encode(in, off, len, null, 0).toString();
	}

	/**
	 * Decodes the given base-64 string into bytes. Any non-base-64 characters
	 * are silently ignored.
	 *
	 * @param in
	 *            the base-64 string to decode.
	 * @return the decoded bytes.
	 */
	public static byte[] decode(String in) {
		if (in == null) {
			return null;
		}
		return inst.decode(in);
	}

	/*
	 * This is here just to clean up the public JavaDoc.
	 */
	private Base64() {
	}

}
