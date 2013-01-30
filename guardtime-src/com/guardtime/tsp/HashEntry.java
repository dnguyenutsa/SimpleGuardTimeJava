/*
 * $Id: HashEntry.java 199 2011-04-19 18:44:19Z ahto.truu $
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

/**
 * Hash chain single entry object.
 * <p>
 * Hash chain entry contains all data needed to perform the current step in
 * hash chain output computation.
 *
 * @since 0.4
 */
final class HashEntry {
	private GTHashAlgorithm hashAlgorithm;
	private int direction;
	private GTDataHash siblingHash;
	private int level;



	/**
	 * Compares this hash entry object with other object.
	 * <p>
	 * Two {@code HashEntry} objects are considered equal if they both
	 * represent the same has entry, so they hash algorithms are equal,
	 * directions are equal, sibling hash values are equal and levels are
	 * equal.
	 * <p>
	 * If {@code null} is provided as argument, this method will silently
	 * return {@code false}.
	 *
	 * @param that object to compare this hash entry object to.
	 *
	 * @return {@code true} if hash algorithm objects are equal;
	 *          {@code false} otherwise.
	 */
	public boolean equals(Object that) {
		if (that == null || !(that instanceof HashEntry)) {
			return false;
		}
		if (this == that) {
			return true; // Both refer to same object
		}

		HashEntry otherEntry = (HashEntry) that;

		if (!hashAlgorithm.equals(otherEntry.hashAlgorithm)) {
			return false;
		}

		if (direction != otherEntry.direction) {
			return false;
		}

		if (!siblingHash.equals(otherEntry.siblingHash)) {
			return false;
		}

		if (level != otherEntry.level) {
			return false;
		}

		return true; // All checks passed
	}

	/**
	 * Computes hash code of this object.
	 * <p>
	 * Hash code is used in object comparison and has no impact on actual
	 * data hash value in this hash object.
	 *
	 * @return hash code of this object.
	 */
	public int hashCode() {
		int prime = 31;
		int hash = 1;

		hash = hash * prime + hashAlgorithm.hashCode();
		hash = hash * prime + direction;
		hash = hash * prime + siblingHash.hashCode();
		hash = hash * prime + level;

		return hash;
	}



	/**
	 * Class constructor.
	 */
	HashEntry(GTHashAlgorithm alg, int dir, GTDataHash sib, int lev) {
		hashAlgorithm = alg;
		direction = dir;
		siblingHash = sib;
		level = lev;
	}



	byte[] computeOutput(byte[] input) {
		GTDataHash hash = new GTDataHash(hashAlgorithm).update(input);
		byte[] imprint1 = hash.toDataImprint();
		byte[] imprint2 = siblingHash.toDataImprint();
		byte[] output = new byte[imprint1.length + imprint2.length + 1];
		if (direction == 0) {
			System.arraycopy(imprint2, 0, output, 0, imprint2.length);
			System.arraycopy(imprint1, 0, output, imprint2.length, imprint1.length);
		} else {
			System.arraycopy(imprint1, 0, output, 0, imprint1.length);
			System.arraycopy(imprint2, 0, output, imprint1.length, imprint2.length);
		}
		output[imprint1.length + imprint2.length] = (byte) level;

		return output;
	}

	int getDirection() {
		return direction;
	}

	int getLevel() {
		return level;
	}

	GTDataHash getSiblingHash() {
		return siblingHash;
	}
}
