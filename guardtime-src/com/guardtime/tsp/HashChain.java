/*
 * $Id: HashChain.java 222 2011-09-28 20:01:17Z ahto.truu $
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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;



/**
 * Hash chain object used to link timestamps to publications.
 * <p>
 * Hash chain is a sequence of entries (links), each containing a hash value,
 * direction and level of this computation step.
 * <p>
 * Hash chain entries are presented as {@link HashEntry} objects.
 *
 * @since 0.1
 */
final class HashChain {
	private ArrayList entries;



	/**
	 * Builds a new location hash chain object out of these hash chain bytes.
	 * <p>
	 * Hash chain bytes is a binary representation of a hash chain extracted
	 * from the timestamp.
	 *
	 * @param chainBytes hash chain bytes.
	 *
	 * @return hash chain object.
	 *
	 * @throws IllegalArgumentException if hash chain has invalid format.
	 */
	static HashChain getLocationInstance(byte[] chainBytes) {
		return new HashChain(chainBytes, true);
	}

	/**
	 * Builds a new history hash chain object out of these hash chain bytes.
	 * <p>
	 * Hash chain bytes is a binary representation of a hash chain extracted
	 * from the timestamp.
	 *
	 * @param chainBytes hash chain bytes.
	 *
	 * @return hash chain object.
	 *
	 * @throws IllegalArgumentException if hash chain has invalid format.
	 */
	static HashChain getHistoryInstance(byte[] chainBytes) {
		return new HashChain(chainBytes, false);
	}



	/**
	 * Checks that past entries match in this history chain and the one
	 * provided.
	 * <p>
	 * Past history chain entries are those which have a {@code 0}-direction.
	 *
	 * @param otherChain history chain to compare this chain entries with.
	 *
	 * @return {@code true}, if all past entries match; {@code false} otherwise.
	 */
	boolean checkPastEntries(HashChain otherChain) {
		Iterator iterator = entries.iterator();
		Iterator otherIterator = otherChain.entries.iterator();

		// Past entries are those which have a 0-direction.
		while (iterator.hasNext()) {
			// Get next 0-direction entry from current chain
			HashEntry entry = null;
			do {
				entry = (HashEntry) iterator.next();
			} while (iterator.hasNext() && entry.getDirection() == 1);

			// No past entries in other chain available to match this one
			if (entry.getDirection() == 0 && !otherIterator.hasNext()) {
				return false;
			}

			// Get next 0-direction entry from other chain
			HashEntry otherEntry = null;
			do {
				otherEntry = (HashEntry) otherIterator.next();
			} while (otherIterator.hasNext() && otherEntry.getDirection() == 1);

			if (entry.getDirection() == 1 && otherEntry.getDirection() == 1) {
				// No more 0-direction entries to compare.
				// This may happen if both chains contain some 1-direction
				// entries in the end.
				return true;
			} else if (!entry.equals(otherEntry)) {
				return false;
			}
		}

		return true; // All 0-direction entries match.
	}

	/**
	 * Computes the result of passing the given input data through this hash chain.
	 *
	 * @param input chain input: bytes to compute chain output for.
	 *
	 * @return chain output: final hash value.
	 */
	byte[] computeOutput(byte[] input) {
		if (input == null) {
			throw new IllegalArgumentException("invalid chain input: null");
		}

		byte[] output = input;

		Iterator iterator = entries.iterator();
		while (iterator.hasNext()) {
			output = ((HashEntry) iterator.next()).computeOutput(output);
		}

		return output;
	}

	// Skip: machine bits + slot bits
	private static final int TOP_SKIP = 3 + 3;
	private static final int NATIONAL_SKIP = 3 + 2;
	private static final int STATE_SKIP = 2 + 2;

	// Level: depth + machine bits + slot bits - 2
	private static final int HASHER = 80;
	private static final int TOP_LEVEL = 60 + TOP_SKIP - 2;
	private static final int NATIONAL_LEVEL = 39 + NATIONAL_SKIP - 2;
	private static final int STATE_LEVEL = 19 + STATE_SKIP - 2;

	/**
	 * Computes location ID of this chain.
	 * <p>
	 * This computation is only sensible for location chains.
	 *
	 * @return location ID computed on this location chain.
	 */
	BigInteger computeLocationId() {
		HashEntry entry = null;
		BigInteger locationId = BigInteger.ZERO;
		int i = entries.size();

		// Skip hasher ID
		while (i > 0 && (entry = (HashEntry) entries.get(--i)).getLevel() > HASHER) {
			// Nothing here, just skipping
		}
		++i; // The previous loop goes one too far

		// Calculate national ID
		int idNational = 0;
		while (i > 0 && (entry = (HashEntry) entries.get(--i)).getLevel() > TOP_LEVEL) {
			idNational = 2 * idNational + (1 - entry.getDirection());
		}

		// Append national ID to location ID
		locationId = locationId.shiftLeft(16).or(BigInteger.valueOf(idNational));

		// Skip machine and slot bits of top-level aggregator
		i -= TOP_SKIP;

		// Calculate state ID
		int idState = 0;
		while (i > 0 && (entry = (HashEntry) entries.get(--i)).getLevel() > NATIONAL_LEVEL) {
			idState = 2 * idState + (1 - entry.getDirection());
		}

		// Append state ID to location ID
		locationId = locationId.shiftLeft(16).or(BigInteger.valueOf(idState));

		// Skip machine and slot bits of national-level aggregator
		i -= NATIONAL_SKIP;

		// Calculate local ID
		int idLocal = 0;
		while (i > 0 && (entry = (HashEntry) entries.get(--i)).getLevel() > STATE_LEVEL) {
			idLocal = 2 * idLocal + (1 - entry.getDirection());
		}

		// Append local ID to location ID
		locationId = locationId.shiftLeft(16).or(BigInteger.valueOf(idLocal));

		// Skip machine and slot bits of state-level aggregator
		i -= STATE_SKIP;

		// Calculate client ID, skipping the name step if there is one
		boolean hasClientName = (i > 0 && checkName((HashEntry) entries.get(0)) != null);
		int idClient = 0;
		while (i > (hasClientName ? 1 : 0)) {
			entry = (HashEntry) entries.get(--i);
			idClient = 2 * idClient + (1 - entry.getDirection());
		}

		// Append client ID to location ID
		locationId = locationId.shiftLeft(16).or(BigInteger.valueOf(idClient));

		// All done
		return locationId;
	}

	/**
	 * Checks whether the given hash chain entry embeds a name. Returns the
	 * name if it does, or {@code null} otherwise.
	 */
	private String checkName(HashEntry entry) {
		if (entry == null) {
			// no step
			return null;
		}
		if (entry.getDirection() != 1) {
			// sibling not on the right
			return null;
		}
		if (!GTHashAlgorithm.SHA224.equals(entry.getSiblingHash().getHashAlgorithm())) {
			// sibling not SHA-224
			return null;
		}
		byte[] hash = entry.getSiblingHash().getHashedMessage();
		if (hash[0] != 0) {
			// first byte of sibling hash value not the tag value 0
			return null;
		}
		if (hash[1] + 2 > hash.length) {
			// second byte of sibling hash value not a valid name length
			return null;
		}
		for (int i = 2 + hash[1]; i < hash.length; ++i) {
			if (hash[i] != 0) {
				// name not properly padded
				return null;
			}
		}
		try {
			return new String(hash, 2, hash[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static final String NAME_SEPARATOR = " : ";

	/**
	 * Extracts location name from this chain.
	 * <p>
	 * This computation is only sensible for location chains.
	 *
	 * @return location name extracted form this location chain.
	 */
	String extractLocationName() {
		int i = entries.size();

		// Skip hasher and national ID and top-level machine and slot bits
		while (i > 0 && ((HashEntry) entries.get(--i)).getLevel() > TOP_LEVEL) {
			// nothing here
		}
		i -= TOP_SKIP;
		
		// This might be the national aggregator name
		String nationalName = (i < 0 ? null : checkName((HashEntry) entries.get(i)));

		// Skip state ID and national machine and slot bits
		while (i > 0 && ((HashEntry) entries.get(--i)).getLevel() > NATIONAL_LEVEL) {
			// noting here
		}
		i -= NATIONAL_SKIP;

		// This might be the state aggregator name
		String stateName = (i < 0 ? null : checkName((HashEntry) entries.get(i)));

		// Skip local ID and state machine and slot bits
		while (i > 0 && ((HashEntry) entries.get(--i)).getLevel() > STATE_LEVEL) {
			// nothing here
		}
		i -= STATE_SKIP;

		// This might be the local aggregator name
		String localName = (i < 0 ? null : checkName((HashEntry) entries.get(i)));

		// The last entry might be the client name
		String clientName = (i <= 0 ? null : checkName((HashEntry) entries.get(0)));

		// Collect the result
		if (nationalName == null && stateName == null && localName == null &&
				clientName == null) {
			return null;
		}
		final BigInteger id = computeLocationId();
		final BigInteger mask = BigInteger.valueOf(0xffff);
		StringBuilder locationName = new StringBuilder();
		if (nationalName == null) {
			locationName.append('[').append(id.shiftRight(48).and(mask)).append(']');
		} else {
			locationName.append(nationalName);
		}
		locationName.append(NAME_SEPARATOR);
		if (stateName == null) {
			locationName.append('[').append(id.shiftRight(32).and(mask)).append(']');
		} else {
			locationName.append(stateName);
		}
		locationName.append(NAME_SEPARATOR);
		if (localName == null) {
			locationName.append('[').append(id.shiftRight(16).and(mask)).append(']');
		} else {
			locationName.append(localName);
		}
		if (clientName != null) {
			locationName.append(NAME_SEPARATOR);
			locationName.append(clientName);
		}
		return locationName.toString();
	}

	/**
	 * Computes history ID of this chain, that is the number of seconds from
	 * 1970-01-01 00:00:00 UTC to the time corresponding to the starting
	 * position of this hash chain in the GuardTime calendar tree.
	 * <p>
	 * This computation is only sensible for history chains.
	 *
	 * @param publicationId history ID corresponding to the root of the
	 * calendar tree from which the history hash chain was extracted.
	 *
	 * @return location ID computed on this location chain.
	 */
	BigInteger computeHistoryId(BigInteger publicationId) {
		int i;
		long N = publicationId.longValue() + 1;
		int m = bitCount(N); // Number of 1-bits in N

		// Shape of a hash chain is represented as `hashChainShape` together
		// with `hashChainLen`.
		int hashChainLen = entries.size();

		// Find how many topmost bits of `hashChainShape` are 0-bits
		long hashChainDirs = 0;
		i = hashChainLen;
		while (i > 0) {
			--i;
			hashChainDirs <<= 1;
			HashEntry entry = (HashEntry) entries.get(i);
			if (entry.getDirection() == 1) {
				hashChainDirs ^= 1;
			}
		}
		// Get leading zeros within `hashChainLen` number of last bits
		int z = numberOfLeadingZeros(hashChainDirs) - (64 - hashChainLen);

		// Delete topmost bits of `hashChainShape` and least significant 1-bits
		// of N
		int count;
		if (z + 1 > m) {
			hashChainLen -= m - 1;
			count = 1;
		} else {
			hashChainLen -= z + 1;
			count = m - z;
		}

		long mask = 1;
		i = 0;
		while (i < count && N > 0) {
			if ((N & mask) == mask) {
				N ^= mask;
				++i;
			}
			mask <<= 1;
		}

		// Flip all bits of sNum
		hashChainDirs = ~hashChainDirs;

		// Convert `hashChainDirs` to long
		i = 0;
		mask = 1;
		long n = 0;

		while (i < hashChainLen) {
			if ((hashChainDirs & mask) == mask) {
				n += mask;
			}
			mask <<= 1;
			i++;
		}

		return BigInteger.valueOf(n + N);
	}



	/*
	 * Common private methods
	 */



	/**
	 * Class constructor.
	 * <p>
	 * Parses hash chain bytes and creates a new hash chain object.
	 *
	 * @param chainBytes hash chain bytes.
	 * @param checkLevel whether to check that level bytes are in increasing order.
	 */
	private HashChain(byte[] chainBytes, boolean checkLevel) {
		if (chainBytes == null) {
			throw new IllegalArgumentException("invalid hash chain: null");
		}

		int previousLevel = -1;
		entries = new ArrayList();
		try {
			for (int pos = 0; pos < chainBytes.length; ) {
				// [0] -- hash algorithm
				GTHashAlgorithm hashAlg = GTHashAlgorithm.getByGtid(chainBytes[pos++]);

				// [1] -- direction
				int dir = chainBytes[pos++];
				if (dir != 0 && dir != 1) {
					throw new IllegalArgumentException("invalid hash step direction: " + dir);
				}

				// [2 .. size - 2] -- sibling data imprint
				GTHashAlgorithm siblingHashAlg = GTHashAlgorithm.getByGtid(chainBytes[pos++]);
				int hashLength = siblingHashAlg.getHashLength();
				byte[] hashBytes = new byte[hashLength];
				System.arraycopy(chainBytes, pos, hashBytes, 0, hashLength);
				GTDataHash dataHash = GTDataHash.getInstance(siblingHashAlg, hashBytes);
				pos += hashLength;

				// [size - 1] -- level
				int level = (int) chainBytes[pos++] & 0xFF; // unsigned byte
				if (checkLevel && level <= previousLevel) {
					throw new IllegalArgumentException("invalid hash step level: " + level);
				}
				previousLevel = level;

				// add the entry to the list
				entries.add(new HashEntry(hashAlg, dir, dataHash, level));
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("hash chain has invalid format: " + e.getMessage());
		}
	}



	/*
	 * Fixes for Java 1.4.2
	 */

	private static int numberOfLeadingZeros(long lng) {
		lng |= lng >> 1;
		lng |= lng >> 2;
		lng |= lng >> 4;
		lng |= lng >> 8;
		lng |= lng >> 16;
		lng |= lng >> 32;
		return bitCount(~lng);
	}

	private static int bitCount(long lng) {
		lng = (lng & 0x5555555555555555L) + ((lng >> 1) & 0x5555555555555555L);
		lng = (lng & 0x3333333333333333L) + ((lng >> 2) & 0x3333333333333333L);
		// adjust for 64-bit integer
		int i = (int) ((lng >>> 32) + lng);
		i = (i & 0x0F0F0F0F) + ((i >> 4) & 0x0F0F0F0F);
		i = (i & 0x00FF00FF) + ((i >> 8) & 0x00FF00FF);
		i = (i & 0x0000FFFF) + ((i >> 16) & 0x0000FFFF);
		return i;
	}
}
