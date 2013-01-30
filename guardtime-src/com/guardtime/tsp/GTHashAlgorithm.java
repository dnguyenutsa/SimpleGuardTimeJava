/*
 * $Id: GTHashAlgorithm.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.util.Locale;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;



/**
 * Hash algorithm object used to calculate data hashes.
 * <p>
 * The currently supported algorithms are:
 * <table border="1">
 * <tr><th>Name</th><th>ASN.1 OID</th><th>GTID</th><th>Digest size</th></tr>
 * <tr><td>RIPEMD160</td><td><a href="http://www.oid-info.com/get/1.3.36.3.2.1">1.3.36.3.2.1</a></td><td>2</td><td>20 bytes</td></tr>
 * <tr><td>SHA1</td><td><a href="http://www.oid-info.com/get/1.3.14.3.2.26">1.3.14.3.2.26</a></td><td>0</td><td>20 bytes</td></tr>
 * <tr><td>SHA224</td><td><a href="http://www.oid-info.com/get/2.16.840.1.101.3.4.2.4">2.16.840.1.101.3.4.2.4</a></td><td>3</td><td>28 bytes</td></tr>
 * <tr><td>SHA256</td><td><a href="http://www.oid-info.com/get/2.16.840.1.101.3.4.2.1">2.16.840.1.101.3.4.2.1</a></td><td>1</td><td>32 bytes</td></tr>
 * <tr><td>SHA384</td><td><a href="http://www.oid-info.com/get/2.16.840.1.101.3.4.2.2">2.16.840.1.101.3.4.2.2</a></td><td>4</td><td>48 bytes</td></tr>
 * <tr><td>SHA512</td><td><a href="http://www.oid-info.com/get/2.16.840.1.101.3.4.2.3">2.16.840.1.101.3.4.2.3</a></td><td>5</td><td>64 bytes</td></tr>
 * </table>
 * <p>
 * To create a hash algorithm object, use one of these methods
 * (examples given for SHA-256):
 * <pre>
 * GTHashAlgorithm hashAlg0 = GTHashAlgorithm.SHA256;
 * GTHashAlgorithm hashAlg1 = GTHashAlgorithm.getByName(&quot;SHA256&quot;);
 * GTHashAlgorithm hashAlg2 = GTHashAlgorithm.getByOId(&quot;2.16.840.1.101.3.4.2.1&quot;);
 * GTHashAlgorithm hashAlg3 = GTHashAlgorithm.getByGTId(1);
 * </pre>
 *
 * @see GTDataHash
 *
 * @since 0.1
 */
public final class GTHashAlgorithm {
	public static final GTHashAlgorithm RIPEMD160 = new GTHashAlgorithm("RIPEMD160", TeleTrusTObjectIdentifiers.ripemd160.getId(), 2, 20);
	public static final GTHashAlgorithm SHA1 = new GTHashAlgorithm("SHA1", OIWObjectIdentifiers.idSHA1.getId(), 0, 20);
	public static final GTHashAlgorithm SHA224 = new GTHashAlgorithm("SHA224", NISTObjectIdentifiers.id_sha224.getId(), 3, 28);
	public static final GTHashAlgorithm SHA256 = new GTHashAlgorithm("SHA256", NISTObjectIdentifiers.id_sha256.getId(), 1, 32);
	public static final GTHashAlgorithm SHA384 = new GTHashAlgorithm("SHA384", NISTObjectIdentifiers.id_sha384.getId(), 4, 48);
	public static final GTHashAlgorithm SHA512 = new GTHashAlgorithm("SHA512", NISTObjectIdentifiers.id_sha512.getId(), 5, 64);

	public static final GTHashAlgorithm DEFAULT = SHA256;

	private static final GTHashAlgorithm[] algorithms = { RIPEMD160, SHA1, SHA224, SHA256, SHA384, SHA512 };

	private String name;
	private String oid;
	private int gtid;
	private int hashLength;



	/**
	 * Retrieves hash algorithm with the given name.
	 * <p>
	 * This method is case- and dash-insensitive, e.g. 'SHA256', 'sha256',
	 * 'SHA-256' and 'sha-256' all will be accepted.
	 *
	 * @param name algorithm name.
	 *
	 * @return hash algorithm object with the given name.
	 *
	 * @throws IllegalArgumentException if there is no algorithm with the given name.
	 */
	public static GTHashAlgorithm getByName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("algorithm name is null");
		}

		// SHA256, sha256, SHA-256 and sha-256 all refer to the same algorithm
		name = name.toUpperCase(Locale.ENGLISH).replaceAll("-", "");

		for (int i = 0; i < algorithms.length; i++) {
			if (name.equals(algorithms[i].getName())) {
				return algorithms[i];
			}
		}

		throw new IllegalArgumentException("unsupported algorithm name: " + name);
	}

	/**
	 * Retrieves hash algorithm with the given object identifier (OID).
	 *
	 * @param oid algorithm OID.
	 *
	 * @return hash algorithm object with the given OID.
	 *
	 * @throws IllegalArgumentException if there is no algorithm with the given OID.
	 */
	public static GTHashAlgorithm getByOid(String oid) {
		if (oid == null) {
			throw new IllegalArgumentException("algorithm OID is null");
		}

		for (int i = 0; i < algorithms.length; i++) {
			if (oid.equals(algorithms[i].getOid())) {
				return algorithms[i];
			}
		}

		throw new IllegalArgumentException("unsupported algorithm OID: " + oid);
	}

	/**
	 * Retrieves hash algorithm with the given GuardTime ID (GTID).
	 *
	 * @param gtid algorithm GTID.
	 *
	 * @return hash algorithm object with the given GTID.
	 *
	 * @throws IllegalArgumentException if there is no algorithm with the given GTID.
	 */
	public static GTHashAlgorithm getByGtid(int gtid) {
		for (int i = 0; i < algorithms.length; i++) {
			if (gtid == algorithms[i].getGtid()) {
				return algorithms[i];
			}
		}

		throw new IllegalArgumentException("unsupported algorithm GTID: " + gtid);
	}



	/**
	 * Returns name of this hash algorithm.
	 * <p>
	 * Name is an upper case string with no dashes, e.g. {@code SHA256}.
	 *
	 * @return hash algorithm name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns object identifier (OID) of this hash algorithm.
	 *
	 * @return hash algorithm OID.
	 */
	public String getOid() {
		return oid;
	}

	/**
	 * Returns GuardTime identifier (GTID) of this hash algorithm.
	 *
	 * @return hash algorithm GTID.
	 */
	public int getGtid() {
		return gtid;
	}

	/**
	 * Returns the length (in bytes) of message digest produced by this hash
	 * algorithm.
	 *
	 * @return message digest length in bytes.
	 */
	public int getHashLength() {
		return hashLength;
	}



	/**
	 * Compares this hash algorithm object with other object.
	 * <p>
	 * Two {@code GTHashAlgorithm} objects are considered equal if they both
	 * represent the same algorithm.
	 * <p>
	 * If {@code null} is provided as argument, this method will silently
	 * return {@code false}.
	 *
	 * @param that object to compare this hash algorithm object to.
	 *
	 * @return {@code true} if hash algorithm objects are equal; {@code false}
	 * 			otherwise.
	 *
	 * @since 0.4
	 */
	public boolean equals(Object that) {
		if (that == null || !(that instanceof GTHashAlgorithm)) {
			return false;
		}
		if (this == that) {
			return true; // Both refer to same object
		}

		GTHashAlgorithm otherAlg = (GTHashAlgorithm) that;
		return (gtid == otherAlg.getGtid()); // It is enough to check one parameter only
	}

	/**
	 * Computes hash code of this object.
	 * <p>
	 * Hash code is used in object comparison and has no impact on actual data
	 * hash calculation with this hash algorithm.
	 *
	 * @return hash code of this object.
	 *
	 * @since 0.4
	 */
	public int hashCode() {
		return 31 + gtid; // Why not?
	}



	/**
	 * Class constructor. Used only for initializing the internal static list.
	 * <p>
	 * Creates a new hash algorithm object implementing an algorithm with the given
	 * name, OID, GTID, and hash length.
	 *
	 * @param name algorithm name.
	 * @param oid algorithm OID.
	 * @param gtid algorithm GTID.
	 * @param hashLength hash length.
	 */
	private GTHashAlgorithm(String name, String oid, int gtid, int hashLength) {
		this.name = name;
		this.oid = oid;
		this.gtid = gtid;
		this.hashLength = hashLength;
	}
}
