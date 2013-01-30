/*
 * $Id: GTDataHashTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package tests.tsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.util.Base16;
import com.guardtime.util.Log;



/**
 * {@code com.guardtime.tsp.GTDataHash} tests.
 */
public class GTDataHashTest
extends TestCase {
	private static final String DATA_STR = "Sample Data";
	private static final byte[] DATA = DATA_STR.getBytes();

	private static final GTHashAlgorithm[] HASH_ALGS = {
		GTHashAlgorithm.RIPEMD160,
		GTHashAlgorithm.SHA1,
		GTHashAlgorithm.SHA224,
		GTHashAlgorithm.SHA256,
		GTHashAlgorithm.SHA384,
		GTHashAlgorithm.SHA512
	};

	// Use tools/compute-hashes.py, Luke
	private static final byte[][] HASHED_MESSAGES = {
		Base16.decode("d49ce1cca2b20464bc6f9d4e63f8e39c36c52101"), // RIPEMD160,
		Base16.decode("93cc16cad7601675158b9331513b5740a09166e4"), // SHA1
		Base16.decode("c32d343fdb85522a32134432ed37fb0aa8c46e1de02202d716645b46"), // SHA224
		Base16.decode("f4c51197ed3863a9ecba56580b7bc9c8df9e9352788b8b9af9185304b6a43c18"), // SHA256
		Base16.decode("90a271a3855e687c73d6399a88abf8be063eb9398710923f234478dc22387185e4e7b73485915e03bfdaaa289614873d"), // SHA384
		Base16.decode("afd3b92453b2095f238c3149fa53f2aee2a388dd5649751c4239dcaa2e9732a8a838ec4f6a0ed8806b5bd537c33403b2da8a552096317f35ea40ef608d9b2814") // SHA512
	};



	/**
	 * Runs all tests with all hash algorithms provided.
	 */
	public void testWithAllAlgorithms() {
		for (int i = 0; i < HASH_ALGS.length; i++) {
			testDataHashInit(HASH_ALGS[i], HASHED_MESSAGES[i]);
			testDataHashEquals(HASH_ALGS[i], HASHED_MESSAGES[i]);

			testDataHashAlgorithm(HASH_ALGS[i], HASHED_MESSAGES[i]);
			testDataHashFormat(HASH_ALGS[i], HASHED_MESSAGES[i]);

			testDataHashState(HASH_ALGS[i], HASHED_MESSAGES[i]);

			testDataHashByteArrayUpdate(HASH_ALGS[i], HASHED_MESSAGES[i]);
			try {
				testDataHashStreamUpdate(HASH_ALGS[i], HASHED_MESSAGES[i]);
			} catch (IOException e) {
				fail(e.getMessage());
			}

			testDataHashBuffer(HASH_ALGS[i], HASHED_MESSAGES[i]);
		}
	}



	/**
	 * Tests {@code getInstance()} methods and class constructors.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashInit(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure illegal arguments are handled correctly
		try {
			GTDataHash.getInstance(null);
			fail("null accepted as data imprint");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTDataHash.getInstance(hashAlg, null);
			fail("null accepted as hashed message");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTDataHash.getInstance(null, hashedMessage);
			fail("null accepted as hash algorithm");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTDataHash.getInstance(null, null);
			fail("null's accepted as both hash algorithm and hashed message");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			new GTDataHash(null);
			fail("null accepted as hash algorithm");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Construct hash object with this hashed message
		GTDataHash dataHash = GTDataHash.getInstance(hashAlg, hashedMessage);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Rebuild data imprint manually and check if correct value is returned
		byte[] dataImprint = new byte[hashAlg.getHashLength() + 1];
		dataImprint[0] = (byte) hashAlg.getGtid();
		System.arraycopy(hashedMessage, 0, dataImprint, 1, hashedMessage.length);
		assertTrue(Arrays.equals(dataImprint, dataHash.toDataImprint()));

		// Construct hash object from imprint, make sure both hashes are equal
		GTDataHash dataHashFromImprint = GTDataHash.getInstance(dataImprint);
		assertTrue(dataHashFromImprint.equals(dataHash));

		// Construct empty hash and update it
		assertTrue(new GTDataHash(hashAlg).update(DATA).close().equals(dataHash));
	}

	/**
	 * Tests {@code equals()} and {@code hashCode()} methods.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashEquals(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		GTDataHash aHash = GTDataHash.getInstance(hashAlg, hashedMessage);
		GTDataHash anotherHash = new GTDataHash(hashAlg).update(DATA);

		// Make sure none of the hashes is equal to NULL
		assertFalse(aHash.equals(null));
		assertFalse(anotherHash.equals(null));

		// Make sure comparison to the same object returns TRUE
		assertTrue(aHash.equals(aHash));
		assertEquals(aHash.hashCode(), aHash.hashCode());
		assertTrue(anotherHash.equals(anotherHash));
		assertEquals(anotherHash.hashCode(), anotherHash.hashCode());

		// Make sure data hashes are not equal if calculator states vary
		assertFalse(aHash.equals(anotherHash));
		assertFalse(anotherHash.equals(aHash));

		// Make sure data hashes are NOT equal if both calculators are open
		GTDataHash yetAnotherHash = new GTDataHash(hashAlg).update(DATA);
		assertFalse(yetAnotherHash.equals(anotherHash));
		assertFalse(anotherHash.equals(yetAnotherHash));

		// Make sure data hashes are equal if both calculators are closed
		anotherHash.close();
		assertTrue(aHash.equals(anotherHash));
		assertTrue(anotherHash.equals(aHash));
		assertEquals(aHash.hashCode(), anotherHash.hashCode());

		// Make sure hashes with different algorithms are not equal
		for (int i = 0; i < HASH_ALGS.length; i++) {
			// Pick different algorithm
			if (!HASH_ALGS[i].equals(hashAlg)) {
				GTDataHash alienHash = GTDataHash.getInstance(HASH_ALGS[i], HASHED_MESSAGES[i]);
				assertFalse(alienHash.equals(aHash));
			}
		}
	}

	/**
	 * Tests {@code getHashAlgorithm()} method.
	 *
	 * @param hashAlg
	 */
	private void testDataHashAlgorithm(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure created data hash returns proper algorithm
		GTDataHash aHash = new GTDataHash(hashAlg);
		assertTrue(aHash.getHashAlgorithm().equals(hashAlg));

		// Make sure algorithm is not changed during update
		aHash.update(DATA);
		assertTrue(aHash.getHashAlgorithm().equals(hashAlg));

		// Make sure algorithm is available after hash calculation is closed
		aHash.close();
		assertTrue(aHash.getHashAlgorithm().equals(hashAlg));

		// Make sure instantiated data hash returns proper hash algorithm
		GTDataHash anotherHash = GTDataHash.getInstance(hashAlg, hashedMessage);
		assertTrue(anotherHash.getHashAlgorithm().equals(hashAlg));
	}

	/**
	 * Tests if proper combinations of hash algorithm and hashed message
	 * are accepted. Also tests data imprint formats accepted.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashFormat(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure hash length is checked
		for (int i = 0; i < HASH_ALGS.length; i++) {
			// Pick different hash length
			if (HASH_ALGS[i].getHashLength() != hashAlg.getHashLength()) {
				// Make sure hashed message with invalid length is NOT accepted
				try {
					GTDataHash.getInstance(hashAlg, HASHED_MESSAGES[i]);
					fail("data hash of invalid length accepted.");
				} catch (IllegalArgumentException e) {
					Log.debug("[DBG] (OK) " + e.getMessage());
				}

				// Make sure data imprint with invalid length is NOT accepted
				try {
					byte[] dataImprint = new byte[HASHED_MESSAGES[i].length + 1];
					dataImprint[0] = (byte) hashAlg.getGtid();
					System.arraycopy(HASHED_MESSAGES[i], 0, dataImprint, 1, HASHED_MESSAGES[i].length);
					GTDataHash.getInstance(dataImprint);
					fail("data imprint of invalid length accepted.");
				} catch (IllegalArgumentException e) {
					Log.debug("[DBG] (OK) " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Tests {@code update(byte[])} and {@code update(byte[], int, int)}
	 * methods.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashByteArrayUpdate(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure illegal arguments are handled correctly
		try {
			new GTDataHash(hashAlg).update((byte[]) null);
			fail("null-array accepted as hash update argument");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			new GTDataHash(hashAlg).update((byte[]) null, 0, 1);
			fail("null-array accepted as hash update argument");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Prepare common variables
		byte[] modifiedData = ("o_O" + DATA_STR + ".").getBytes();
		int offset = 3;
		GTDataHash dataHash;

		// Hashing entire array -- equal hashes
		dataHash = new GTDataHash(hashAlg).update(DATA);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing modified array -- different hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing entire array with bounds provided -- equal hashes
		dataHash = new GTDataHash(hashAlg).update(DATA, 0, DATA.length);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing modified array with bounds provided -- different hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData, 0, modifiedData.length);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Correct array length provided in update() -- equal hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData, offset, DATA.length);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect array length provided in update() -- different hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData, offset, DATA.length + 1);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect array offset provided in update() -- different hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData, offset - 1, DATA.length);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect array bounds provided in update() -- different hashes
		dataHash = new GTDataHash(hashAlg).update(modifiedData, offset - 1, DATA.length + 1);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));
	}

	/**
	 * Tests {@code update(InputStream)} and {@code update(InputStream, int)}
	 * methods.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 * @throws IOException
	 */
	public void testDataHashStreamUpdate(GTHashAlgorithm hashAlg, byte[] hashedMessage)
	throws IOException {
		// Make sure NULL-arguments are handled correctly
		try {
			new GTDataHash(hashAlg).update((InputStream) null);
			fail("null-stream accepted as hash update argument");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			new GTDataHash(hashAlg).update((InputStream) null, 1);
			fail("null-stream accepted as hash update argument");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Prepare common variables
		byte[] modifiedData = ("o_O" + DATA_STR + ".").getBytes();
		int offset = 3;
		GTDataHash dataHash;
		ByteArrayInputStream in;

		// Hashing entire data -- equal hashes
		in = new ByteArrayInputStream(DATA);
		dataHash = new GTDataHash(hashAlg).update(in);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing modified data -- different hashes
		in = new ByteArrayInputStream(modifiedData);
		dataHash = new GTDataHash(hashAlg).update(in);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing entire data with bounds provided -- equal hashes
		in = new ByteArrayInputStream(DATA);
		dataHash = new GTDataHash(hashAlg).update(in, in.available());
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Hashing modified data with bounds provided -- different hashes
		in = new ByteArrayInputStream(modifiedData);
		dataHash = new GTDataHash(hashAlg).update(in, in.available());
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Correct data bounds provided -- equal hashes
		in = new ByteArrayInputStream(modifiedData);
		in.skip(offset);
		dataHash = new GTDataHash(hashAlg).update(in, DATA.length);
		assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect data length provided -- different hashes
		in = new ByteArrayInputStream(modifiedData);
		in.skip(offset);
		dataHash = new GTDataHash(hashAlg).update(in, DATA.length + 1);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect data start point provided -- different hashes
		in = new ByteArrayInputStream(modifiedData);
		in.skip(offset - 1);
		dataHash = new GTDataHash(hashAlg).update(in, DATA.length);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));

		// Incorrect data bounds provided -- different hashes
		in = new ByteArrayInputStream(modifiedData);
		in.skip(offset - 1);
		dataHash = new GTDataHash(hashAlg).update(in, DATA.length + 1);
		assertFalse(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));
	}

	/**
	 * Tests data hash calculator states, namely {@code close()} and
	 * {@code isClosed()} method in different conditions.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashState(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure data hash is created with open hash calculator
		GTDataHash aHash = new GTDataHash(hashAlg);
		assertFalse(aHash.isClosed());

		// Make sure update() works and does NOT close hash calculator
		try {
			aHash.update("some data".getBytes());
		} catch (Exception e) {
			fail("could not update open data hash: " + e.getMessage());
		}
		try {
			aHash.update("some more data".getBytes());
		} catch (Exception e) {
			fail("could not update open data hash for the second time: " + e.getMessage());
		}
		assertFalse(aHash.isClosed());

		// Make sure close() closes hash calculator
		aHash.close();
		assertTrue(aHash.isClosed());

		// Make sure update() is not working any more
		try {
			aHash.update("even more data".getBytes());
			fail("managed to update closed data hash");
		} catch (Exception e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure getHashedMessage() closes hash calculator
		GTDataHash anotherHash = new GTDataHash(hashAlg).update(DATA);
		anotherHash.getHashedMessage();
		assertTrue(anotherHash.isClosed());

		// Make sure data hash created from hashed message has closed hash calculator
		GTDataHash yetAnotherHash = GTDataHash.getInstance(hashAlg, hashedMessage);
		assertTrue(yetAnotherHash.isClosed());

		// Make sure empty hash does NOT return as hashed message
		GTDataHash emptyHash = new GTDataHash(hashAlg);
		assertNotNull(emptyHash.getHashedMessage());
	}

	/**
	 * Tests data hash update buffer, namely {@code getBuffer()} and
	 * {@code setBuffer()} methods.
	 *
	 * @param hashAlg
	 * @param hashedMessage
	 */
	private void testDataHashBuffer(GTHashAlgorithm hashAlg, byte[] hashedMessage) {
		// Make sure newly created data hash has default positive buffer value
		assertTrue(0 < new GTDataHash(hashAlg).getBufferSize());

		// Make sure negative and zero buffer values are NOT accepted
		int[] invalidBuffers = { Integer.MIN_VALUE, -1, 0 };
		for (int j = 0; j < invalidBuffers.length; j++) {
			try {
				new GTDataHash(hashAlg).setBufferSize(invalidBuffers[j]);
				fail(invalidBuffers[j] + " accepted as buffer size");
			} catch(IllegalArgumentException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}

		// Make sure positive buffer values are accepted
		int[] validBuffers = { 1, Short.MAX_VALUE, Integer.MAX_VALUE };
		for (int j = 0; j < validBuffers.length; j++) {
			new GTDataHash(hashAlg).setBufferSize(validBuffers[j]);
		}

		// Test hash calculations with different buffer sizes
		byte[] data = DATA_STR.getBytes();

		// Buffer sizes being tested are 1 .. len + 2
		for (int i = 1; i <= data.length + 2; i++) {
			GTDataHash dataHash = new GTDataHash(hashAlg).setBufferSize(i);
			assertEquals(i, dataHash.getBufferSize());

			try {
				dataHash.update(new ByteArrayInputStream(data));
			} catch (IOException e) {
				fail("update failed with buffer size " + i);
			}
			assertTrue(Arrays.equals(hashedMessage, dataHash.getHashedMessage()));
		}
	}
}
