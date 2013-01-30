/*
 * $Id: GTVerificationResult.java 169 2011-03-03 18:45:00Z ahto.truu $
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
 * Timestamp and/or publications file verification result.
 *
 * @see com.guardtime.tsp.VerificationResult
 *
 * @since 0.4
 */
public final class GTVerificationResult
extends VerificationResult {
	/*
	 * Status codes
	 */



	/**
	 * Short-term RSA signature was present in the timestamp.
	 */
	public static final int PUBLIC_KEY_SIGNATURE_PRESENT = 1;

	/**
	 * Long-term publication reference was present in the timestamp.
	 */
	public static final int PUBLICATION_REFERENCE_PRESENT = 2;

	/**
	 * Timestamp was checked against the data hash.
	 */
	public static final int DATA_HASH_CHECKED = 16;

	/**
	 * Timestamp was checked against control publication.
	 */
	public static final int PUBLICATION_CHECKED = 32;

	/**
	 * Publications file signature verified
	 */
	public static final int PUBFILE_SIGNATURE_VERIFIED = 64;



	/*
	 * Error codes
	 */



	/**
	 * Timestamp has invalid syntax.
	 */
	public static final int SYNTACTIC_CHECK_FAILURE = 1;

	/**
	 * Hash chain computation result does not match the publication imprint.
	 */
	public static final int HASHCHAIN_VERIFICATION_FAILURE = 2;

	/**
	 * Signed data structure is incorrectly composed, i.e. wrong data is signed
	 * or the signature does not match the public key in the timestamp.
	 */
	public static final int PUBLIC_KEY_SIGNATURE_FAILURE = 16;

	/**
	 * Public key of signed timestamp is not found among published ones.
	 */
	public static final int PUBLIC_KEY_FAILURE = 64;

	/**
	 * Timestamp does not match with the document it is claimed to belong to.
	 */
	public static final int WRONG_DOCUMENT_FAILURE = 128;

	/**
	 * Publications file is inconsistent with the corresponding data in
	 * timestamp -- publication identifiers or published hash values do not
	 * match.
	 */
	public static final int PUBLICATION_FAILURE = 256;

	/**
	 * Signed data certificate validation failed.
	 */
	public static final int CERTIFICATE_FAILURE = 512;

	/**
	 * Technical failure occurred while verifying timestamp.
	 */
	public static final int TECH_FAILURE = 1024;

	/**
	 * Publications file signature failure
	 */
	public static final int PUBFILE_SIGNATURE_FAILURE = 2048;



	/**
	 * Updates status and errors in current verification result with status and
	 * error codes from provided verification result.
	 *
	 * @param otherResult verification result.
	 */
	void update(GTVerificationResult otherResult) {
		updateStatus(otherResult.getStatusCode());
		updateErrors(otherResult.getErrorCode());
	}
}
