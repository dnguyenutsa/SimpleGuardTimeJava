/*
 * $Id: GTHashAlgorithmTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import junit.framework.TestCase;

import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.util.Log;



/**
 * {@code com.guardtime.tsp.GTHashAlgorithm} tests.
 */
public class GTHashAlgorithmTest
extends TestCase {
	private String[] algNames = {
			"SHA1",
			"SHA256",
			"RIPEMD160",
			"SHA224",
			"SHA384",
			"SHA512"
	};

	private String[] algOids = {
			"1.3.14.3.2.26", // SHA1
			"2.16.840.1.101.3.4.2.1", // SHA256
			"1.3.36.3.2.1", // RIPEMD160
			"2.16.840.1.101.3.4.2.4", // SHA224
			"2.16.840.1.101.3.4.2.2", // SHA384
			"2.16.840.1.101.3.4.2.3" // SHA512
	};

	private int[] algHashLengths = { 20, 32, 20, 28, 48, 64 };



	/**
	 * Tests predefined hash algorithms.
	 */
	public void testHashAlgorithmConstants() {
		assertTrue(GTHashAlgorithm.getByName("RIPEMD160").equals(GTHashAlgorithm.RIPEMD160));
		assertTrue(GTHashAlgorithm.getByName("SHA1").equals(GTHashAlgorithm.SHA1));
		assertTrue(GTHashAlgorithm.getByName("SHA224").equals(GTHashAlgorithm.SHA224));
		assertTrue(GTHashAlgorithm.getByName("SHA256").equals(GTHashAlgorithm.SHA256));
		assertTrue(GTHashAlgorithm.getByName("SHA384").equals(GTHashAlgorithm.SHA384));
		assertTrue(GTHashAlgorithm.getByName("SHA512").equals(GTHashAlgorithm.SHA512));
	}

	/**
	 * Runs all remaining tests with every algorithm.
	 */
	public void testAllAlgorithms() {
		for (int i = 0; i < algNames.length; i++) {
			GTHashAlgorithm alg = GTHashAlgorithm.getByGtid(i);
			assertTrue(i == alg.getGtid());

			testHashAlgorithmInit(alg);
			testhashAlgorithmEquals(alg);
			testHashAlgorithmGetters(alg);
		}
	}



	/**
	 * Tests hash algorithm init methods.
	 *
	 * @param alg
	 */
	private void testHashAlgorithmInit(GTHashAlgorithm alg) {
		// Make sure illegal arguments are handled correctly
		try {
			GTHashAlgorithm.getByName(null);
			fail("null accepted as hash algorithm name");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTHashAlgorithm.getByName("TinkyWinky");
			fail("rubbish accepted as hash algorithm name");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTHashAlgorithm.getByOid(null);
			fail("null accepted as hash algorithm OID");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTHashAlgorithm.getByOid("o_O");
			fail("rubbish accepted as hash algorithm OID");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		int[] invalidGtids = { -1, 6, Integer.MAX_VALUE + 1 };
		for (int i = 0; i < invalidGtids.length; i++) {
			try {
				GTHashAlgorithm.getByGtid(invalidGtids[i]);
				fail("invalid hash algorithm GTID accepted: " + invalidGtids[i]);
			} catch (IllegalArgumentException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}

		// Make sure proper GTID-OID-Name relations are implemented
		assertTrue(alg.equals(GTHashAlgorithm.getByOid(algOids[alg.getGtid()])));
		assertTrue(alg.equals(GTHashAlgorithm.getByName(algNames[alg.getGtid()])));
	}

	/**
	 * Tests {@code equals()} and {@code hashCode()} methods.
	 *
	 * @param alg
	 */
	private void testhashAlgorithmEquals(GTHashAlgorithm alg) {
		// Make sure algorithm is not equal to NULL
		assertFalse(alg.equals(null));

		// Make sure comparison to the same object returns TRUE
		assertTrue(alg.equals(alg));

		for (int j = 0; j < algNames.length; j++) {
			GTHashAlgorithm other = GTHashAlgorithm.getByName(algNames[j]);

			// Make sure algorithms with different OIDs are not equal
			if (!other.getOid().equals(alg.getOid())) {
				assertFalse(other.equals(alg));
			}

			// Make sure algorithms with different names are not equal
			if (!other.getName().equals(alg.getName())) {
				assertFalse(other.equals(alg));
			}

			// Make sure algorithms with different GTIDs are not equal
			if (j != alg.getGtid()) {
				assertFalse(other.equals(alg));
			}
		}
	}

	/**
	 * Tests property getters, namely {@code getName()}, {@code getOid()}
	 * and {@code getHashLength()}.
	 *
	 * {@code getOid()} method is tested in {@link #testAllAlgorithms()}.
	 *
	 * @param alg
	 */
	private void testHashAlgorithmGetters(GTHashAlgorithm alg) {
		// Make sure proper values are returned
		assertTrue(alg.getName().equals(algNames[alg.getGtid()]));
		assertTrue(alg.getOid().equals(algOids[alg.getGtid()]));
		assertTrue(alg.getHashLength() == algHashLengths[alg.getGtid()]);
	}
}
