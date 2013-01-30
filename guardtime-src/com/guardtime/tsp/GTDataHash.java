/*
 * $Id: GTDataHash.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package com.guardtime.tsp;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.guardtime.asn1.MessageImprint;
import com.guardtime.util.Util;



/**
 * Data hash object used as seed when creating and verifying timestamps.
 * <p>
 * To calculate the hash sum of some data, first create a data hash object using
 * the {@link #GTDataHash(GTHashAlgorithm)} constructor and then add data to it
 * by calling one or more of its {@code update()} methods. Do this until all the
 * data has been fed to the hash calculator. You can then retrieve the resulting
 * hash value using the {@link #getHashedMessage()} method, but this 'closes'
 * the hash object and you can't add more data after that.
 *
 * <pre>
 * byte[] data = ...
 * GTDataHash dataHash = new GTDataHash(GTHashAlgorithm.DEFAULT);
 * dataHash.update(data);
 * byte[] hashedMessage = dataHash.getHashedMessage();
 * </pre>
 *
 * <p>
 * To create data hash object from a given hash value, use the
 * {@link #getInstance(GTHashAlgorithm, byte[])} method:
 *
 * <pre>
 * byte[] sha256Value = ...
 * GTDataHash dataHash = GTDataHash.getInstance(GTHashAlgorithm.SHA256, sha256Value);
 * </pre>
 *
 * Note that this data hash object is created 'closed' and can't be updated.
 * <p>
 * Similarly data hash object can be created from data imprint. Data imprint is
 * a byte sequence where the first byte defines hash algorithm used, and
 * remaining bytes contain the hash value.
 *
 * <pre>
 * byte[] dataImprint = ...
 * GTDataHash dataHash = GTDataHash.getInstance(dataImprint);
 * </pre>
 *
 * Note that this data hash object is created 'closed' and can't be updated.
 * <p>
 * To check hash calculator state, use {@link #isClosed()} method.
 * <p>
 * When calculating the hash sum of some data to verify a timestamp, make sure
 * to use the same hash algorithm as when the timestamp was created. To get this
 * algorithm, use {@link GTTimestamp#getHashAlgorithm()}.
 * <p>
 * There is no constructor to create a {@code GTDataHash} object from a
 * {@code MessageDigest} object. This is deliberate to avoid unexpected side
 * effects on the {@code MessageDigest} object. Instead, use the following, more
 * explicit, idiom:
 * <pre>
 * MessageDigest md = ...;
 * GTDataHash dataHash = GTDataHash.getInstance(
 *    GTHashAlgorithm.getByName(md.getAlgorithm()),
 *    md.digest());
 * </pre>
 *
 * @see GTHashAlgorithm
 *
 * @since 0.1
 */
public class GTDataHash {
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private byte[] hashedMessage;
	private int bufferSize;
	private GTHashAlgorithm hashAlgorithm;
	private MessageDigest messageDigest;

	/*
	 * Initializers
	 *
	 * These methods create data hash objects with already calculated
	 * hashed message. Those hashes cannot be updated anymore.
	 */



	/**
	 *  Builds new hash object with the given hash algorithm and hash value.
	 *  <p>
	 *  Hash object created with closed hash calculator and cannot be
	 *  updated.
	 *
	 *  @param hashAlgorithm hash algorithm used to calculate the hash value.
	 *  @param hashedMessage hash value calculated with the hash algorithm.
	 *
	 *  @return newly created hash object.
	 *
	 *  @since 0.4
	 */
	public static GTDataHash getInstance(GTHashAlgorithm hashAlgorithm, byte[] hashedMessage) {
		// `hashedMessage` must be present.
		// Use public constructor to init empty hash with open calculator.
		if (hashedMessage == null) {
			throw new IllegalArgumentException("invalid hashed message: null");
		}

		// Hash algorithm is checked in class constructor.

		return new GTDataHash(hashAlgorithm, hashedMessage);
	}

	/**
	 * Build new hash object with data from the given data imprint.
	 * <p>
	 * Hash object created with closed hash calculator and cannot be updated.
	 * <p>
	 * {@code DataImprint} should not be confused with {@code MessageImprint}.
	 * {@link MessageImprint} is ASN.1 type defined in RFC 3161 with
	 * {@code AlgorithmIdentifier} field as algorithm identifier.
	 * {@code DataImprint} is a type defined by GuardTime with single byte
	 * (GT-ID) as algorithm identifier.
	 *
	 * @param dataImprint
	 *            data imprint.
	 *
	 * @return newly created hash object.
	 *
	 * @since 0.4
	 */
	public static GTDataHash getInstance(byte[] dataImprint) {
		if (dataImprint == null) {
			throw new IllegalArgumentException("invalid data imprint: null");
		} else if (dataImprint.length < 2) {
			throw new IllegalArgumentException("invalid data imprint length: " + dataImprint.length);
		}
		GTHashAlgorithm hashAlg = GTHashAlgorithm.getByGtid(dataImprint[0]);

		// Data hash length is checked in class constructor.

		return new GTDataHash(hashAlg, Util.copyOf(dataImprint, 1, dataImprint.length - 1));
	}



	/*
	 * Class constructor
	 *
	 * Initializes empty hash with open hash calculator.
	 */



	/**
	 * Class constructor. Initializes new empty hash.
	 * <p>
	 * This hash should be updated using one of the {@code update} methods.
	 *
	 * @param hashAlgorithm hash algorithm to hash data with.
	 */
	public GTDataHash(GTHashAlgorithm hashAlgorithm) {
		this(hashAlgorithm, null);
	}



	/*
	 * Buffer operations
	 */



	/**
	 * Returns buffer size, in bytes, used by this hash object for hash
	 * calculation.
	 *
	 * @return buffer size of this hash calculator.
	 *
	 * @since 0.4
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Sets buffer size, in bytes, to use by this hash object for hash
	 * calculation.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @param bufferSize buffer size for this hash calculator.
	 *
	 * @return this hash object with new buffer value set.
	 *
	 * @since 0.4
	 */
	public GTDataHash setBufferSize(int bufferSize) {
		if (bufferSize < 1) {
			throw new IllegalArgumentException("invalid buffer size: " + bufferSize);
		}

		this.bufferSize = bufferSize;

		return this;
	}



	/*
	 * Hash calculator operations
	 */



	/**
	 * Updates this hash calculator with the given data.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @param data byte array to feed to this hash calculator.
	 *
	 * @return this hash object updated with the given data.
	 *
	 * @throws IllegalStateException if hash calculator is closed.
	 */
	public GTDataHash update(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("invalid update data: null");
		}

		// isClosed-check is done in `update(byte[], int, int)`
		return update(data, 0, data.length);
	}

	/**
	 * Updates this hash calculator with part of the given data.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @param data byte array to read data from.
	 * @param offset offset to start reading data from.
	 * @param length number of bytes to read.
	 *
	 * @return this hash object updated with the given data.
	 *
	 * @throws IllegalStateException if hash calculator is closed.
	 */
	public GTDataHash update(byte[] data, int offset, int length) {
		if (data == null) {
			throw new IllegalArgumentException("invalid update data: null");
		} else if (isClosed()) {
			throw new IllegalStateException("hash calculator already closed");
		}

		messageDigest.update(data, offset, length);

		return this;
	}

	/**
	 * Updates this hash calculator with data from the given input stream.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @param in input stream to read data from.
	 *
	 * @return this hash object updated with the given data.
	 *
	 * @throws IOException if stream reading error occurs.
	 * @throws IllegalStateException if hash calculator is closed.
	 *
	 * @since 0.3
	 */
	public GTDataHash update(InputStream in)
	throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid update stream: null");
		}

		// isClosed-check is done in `update(byte[], int, int)`
		byte[] buffer = new byte[bufferSize];
		while (true) {
			int bytesRead = in.read(buffer);
			if (bytesRead == -1) {
				return this;
			}
			update(buffer, 0, bytesRead);
		}
	}

	/**
	 * Updates this hash calculator with part of data in the given input stream.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @param in input stream to read data from.
	 * @param limit number of bytes to read.
	 *
	 * @return this hash object updated with the given data.
	 *
	 * @throws IOException if stream reading error occurs.
	 * @throws IllegalStateException if hash calculator is closed.
	 *
	 * @since 0.4
	 */
	public GTDataHash update(InputStream in, int limit)
	throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid update stream: null");
		}

		// isClosed-check is done in `update(byte[], int, int)`
		byte[] buffer = new byte[bufferSize];
		int remaining = limit;
		while (remaining > 0) {
			int bytesRead = in.read(buffer, 0, Math.min(remaining, bufferSize));
			update(buffer, 0, bytesRead);
			remaining -= bytesRead;
		}

		return this;
	}

	/**
	 * Closes the hash calculator in this hash object.
	 * <p>
	 * Hash value cannot be updated after this method is called.
	 * <p>
	 * Does nothing if calculator is closed already.
	 * <p>
	 * This method returns current hash object and is ready for chaining.
	 *
	 * @return this data hash object with hash calculator closed.
	 *
	 * @since 0.3
	 */
	public GTDataHash close() {
		if (isClosed()) {
			return this;
		}

		hashedMessage = messageDigest.digest();
		messageDigest = null;

		return this;
	}

	/**
	 * Returns hash calculator state (can be 'open' or 'closed').
	 *
	 * @return {@code true} if hash calculator is closed; {@code false}
	 * 			otherwise.
	 *
	 * @since 0.4
	 */
	public boolean isClosed() {
		return (messageDigest == null);
	}



	/*
	 * Property getters and converters
	 */



	/**
	 * Returns the hash algorithm used in this hash object.
	 *
	 * @return hash algorithm.
	 */
	public GTHashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	/**
	 * Returns the hash value calculated in this hash object.
	 * <p>
	 * Hash value cannot be updated after this method is called.
	 *
	 * @return hash value.
	 */
	public byte[] getHashedMessage() {
		close();
		return Util.copyOf(hashedMessage);
	}

	public byte[] toDataImprint() {
		int hashLength = hashAlgorithm.getHashLength();
		byte[] imprint = new byte[1 + hashLength];
		imprint[0] = (byte) hashAlgorithm.getGtid();
		System.arraycopy(getHashedMessage(), 0, imprint, 1, hashLength);
		return imprint;
	}




	/*
	 * Overridden object comparison methods
	 */



	/**
	 * Compares this hash object with other object.
	 * <p>
	 * Two {@code GTDataHash} objects are considered equal if
	 * <ul>
	 * <li>Both hash calculators are closed;
	 * <li>Both hash objects use the same hash algorithm;
	 * <li>Both hash objects contain the same hash value.
	 * </ul>
	 * <p>
	 * If both hashes refer to the same actual object, they are also considered
	 * equal.
	 * <p>
	 * If {@code null} is provided as argument, this method will silently
	 * return {@code false}.
	 * <p>
	 * Only closed hash objects can be equal. If hash calculation is open,
	 * distinct objects will not be equal by any means.
	 *
	 * @param that object to compare this hash object to.
	 *
	 * @return {@code true} if hash objects are equal; {@code false} otherwise.
	 *
	 * @since 0.4
	 */
	public boolean equals(Object that) {
		if (that == null || !(that instanceof GTDataHash)) {
			return false; // Nothing is equal to NULL
		}
		if (this == that) {
			return true; // Both refer to same object
		}

		GTDataHash otherHash = (GTDataHash) that;

		// To compare two hash objects with open calculators,
		// `MessageDigest.equals()` method should be implemented. However,
		// it isn't, so `Object.equals()` will be called. It returns `true`
		// only if both `messageDigests` refer to the same object.
		//
		// This would mean that we have two different GTDataHash wrappers over
		// a single MessageDigest object. One should never do so neither we
		// should allow it.
		if (!isClosed() || !otherHash.isClosed()) {
			return false; // Any of hash calculators is open
		}

		if (!hashAlgorithm.equals(otherHash.hashAlgorithm)) {
			return false; // Hash algorithms are not equal
		}

		if (!Arrays.equals(hashedMessage, otherHash.hashedMessage)) {
			return false; // Hashed messages are not equal
		}

		return true; // All checks passed
	}

	/**
	 * Computes hash code of this object.
	 * <p>
	 * Hash code is used in object comparison and has no impact on actual data
	 * hash calculation in this hash object.
	 *
	 * @return hash code of this object.
	 */
	public int hashCode() {
		int prime = 31;
		int hash = 1;

		hash = hash * prime + hashAlgorithm.hashCode();
		if (isClosed()) {
			hash = hash * prime + hashCode(hashedMessage);
		} else {
			hash = hash * prime + messageDigest.hashCode();
		}

		return hash;
	}



	/*
	 * Common private methods
	 */



	/**
	 * Class constructor.
	 * <p>
	 * Creates new hash object.
	 *
	 * @param hashAlgorithm hash algorithm to use in this hash object.
	 * @param hashedMessage hash value. If set to {@code null}, hash object will
	 * 			be created with open hash calculator; otherwise, hash algorithm
	 * 			and hash value correspondence will be checked.
	 *
	 * @throws RuntimeException if required cryptographic provider is not set.
	 */
	private GTDataHash(GTHashAlgorithm hashAlgorithm, byte[] hashedMessage) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException("invalid hash algorithm: null");
		}

		this.hashAlgorithm = hashAlgorithm;
		this.hashedMessage = hashedMessage;

		if (hashedMessage == null) { // No hashed message -- initialize new digest
			String provider = BouncyCastleProvider.PROVIDER_NAME;
			if (Security.getProvider(provider) == null) {
				Security.addProvider(new BouncyCastleProvider());
			}

			try {
				this.messageDigest = MessageDigest.getInstance(hashAlgorithm.getName(), provider);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Hash algorithm not supported: " + hashAlgorithm.getName());
			} catch (NoSuchProviderException e) {
				throw new RuntimeException("Cryptographic provider not found: " + provider, e);
			}

			setBufferSize(DEFAULT_BUFFER_SIZE);
		} else if (hashAlgorithm.getHashLength() != hashedMessage.length) {
			throw new IllegalArgumentException("hash length does not match with that defined in hash algorithm");
		} else { // Hashed message set -- no digest needed
			this.messageDigest = null;
		}
	}



	/*
	 * Fixes for Java 1.4.2
	 */



	private static int hashCode(byte[] array) {
		if (array == null) {
			return 0;
		}
		int hashCode = 1;
		for (int i = 0; i < array.length; i++) {
			// the hash code value for byte value is its integer value
			hashCode = 31 * hashCode + array[i];
		}
		return hashCode;
	}
}
