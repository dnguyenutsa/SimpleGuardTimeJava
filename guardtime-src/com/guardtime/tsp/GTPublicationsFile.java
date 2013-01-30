/*
 * $Id: GTPublicationsFile.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sun.security.pkcs.PKCS7;
import sun.security.pkcs.ParsingException;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;

import com.guardtime.util.Base32;
import com.guardtime.util.Util;


/**
 * Publications file object used in timestamp verification.
 * <p>
 * Publications file contains all publications released, and hashes of all
 * public keys used to sign timestamps.
 * <p>
 * To instantiate publications file object, use {@link #getInstance(byte[])} or
 * {@link #getInstance(InputStream)} methods. Note than only basic syntax check
 * is performed when creating publications file. To verify its signature, use
 * {@link #verifySignature()} method.
 * <p>
 * Publications file should <b>not</b> be trusted unless its signature is
 * verified successfully.
 *
 * @since 0.1
 */
public final class GTPublicationsFile {
	private static final int VERSION = 1;
	private static final int VERSION_POS = 0;

	private static final int PUBLICATION_BLOCK_BEGIN_POS = 10;
	private static final int PUBLICATION_CELL_SIZE_POS = 14;
	private static final int PUBLICATION_COUNT_POS = 16;

	private static final int PUBLIC_KEY_BLOCK_BEGIN_POS = 20;
	private static final int PUBLIC_KEY_CELL_SIZE_POS = 24;
	private static final int PUBLIC_KEY_COUNT_POS = 26;

	private static final int PUBLICATION_REFERENCES_BLOCK_BEGIN_POS = 28;

	private static final int SIGNATURE_BLOCK_BEGIN_POS = 32;

	private static final int HEADER_SIZE = 36;
	private static final int TIME_SIZE = 8;



	private byte[] content;
	private boolean isSignatureVerified;
	private GTVerificationResult verificationResult;

	private int publicationBlockBegin;
	private short publicationCellSize;
	private int publicationCount;

	private int publicKeyBlockBegin;
	private short publicKeyCellSize;
	private short publicKeyCount;

	private int publicationReferenceBlockBegin;

	private int signatureBlockBegin;



	/*
	 * Initializers
	 */



	/**
	 * Creates a new publications file object from the given byte array.
	 *
	 * @param b byte array containing encoded publications file.
	 *
	 * @return newly created publications file object.
	 *
	 * @throws IllegalArgumentException if publications file has invalid format.
	 */
	public static GTPublicationsFile getInstance(byte[] b) {
		if (b == null) {
			throw new IllegalArgumentException("invalid publications file: null");
		}

		return new GTPublicationsFile(b);
	}

	/**
	 * Creates a new publications file object from the given input stream.
	 *
	 * @param in input stream containing encoded publications file.
	 *
	 * @return newly created publications file object.
	 *
	 * @throws IOException if stream reading error occurs.
	 *
	 * @since 0.4
	 */
	public static GTPublicationsFile getInstance(InputStream in)
	throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid publications file stream: null");
		}

		return new GTPublicationsFile(Util.readAll(in));
	}



	/*
	 * Publication parsers
	 */



	/**
	 * Extracts publication ID from the given encoded publication.
	 * <p>
	 * Publication ID is UNIX time (the number of seconds since midnight
	 * 1970-01-01 UTC) of the moment when publication was released.
	 *
	 * @param publicationString Base32-encoded publication.
	 *
	 * @return publication ID.
	 *
	 * @see #publicationTime(String)
	 *
	 * @since 0.4
	 */
	// v0.3 had `BigInteger getPublicationId()`
	public static long publicationId(String publicationString) {
		if (publicationString == null) {
			throw new IllegalArgumentException("invalid publication: null");
		}

		byte[] b = Base32.decode(publicationString);
		return Util.toLong(b); // First 8 bytes
	}

	/**
	 * Extracts publication time from the given encoded publication.
	 * <p>
	 * Publication time is the moment when publication was released.
	 *
	 * @param publicationString Base32-encoded publication.
	 *
	 * @return publication time.
	 *
	 * @see #publicationId(String)
	 *
	 * @since 0.4
	 */
	// v0.3 had `Date getPublicationTime()`
	public static Date publicationTime(String publicationString) {
		return new Date(publicationId(publicationString) * 1000);
	}



	/*
	 * Encoding methods
	 */



	/**
	 * Returns encoded publications file.
	 * <p>
	 * Resulting byte array suitable for writing to file, transporting over
	 * network, storing in database, etc.
	 *
	 * @return encoded publications file.
	 *
	 * @since 0.4
	 */
	public byte[] getEncoded() {
		return Util.copyOf(content);
	}



	/*
	 * Property getters: publications
	 */



	/**
	 * Check if this publications file contains the given publication.
	 *
	 * @param publicationString Base32-encoded publication.
	 *
	 * @return {@code true}, if this publications file contains the
	 * 			publication; {@code false} otherwise.
	 *
	 * @since 0.4
	 */
	public boolean contains(String publicationString) {
		if (publicationString == null) {
			throw new IllegalArgumentException("invalid publication: null");
		}

		return (publicationString.equals(getPublication(publicationId(publicationString))));
	}

	/**
	 * Returns the earliest publication time.
	 *
	 * @return first publication time.
	 *
	 * @since 0.4
	 */
	public Date getFirstPublicationTime() {
		long publicationId = Util.toLong(content, publicationBlockBegin);
		return new Date(publicationId * 1000);
	}

	/**
	 * Returns the latest publications time.
	 *
	 * @return last publication time.
	 *
	 * @since 0.4
	 */
	public Date getLastPublicationTime() {
		int offset = publicationCellSize * (publicationCount - 1);
		long publicationId = Util.toLong(content, publicationBlockBegin + offset);
		return new Date(publicationId * 1000);
	}

	/**
	 * Retrieves publication, if any, by the given publication ID.
	 *
	 * @param publicationId publication ID.
	 *
	 * @return Base32-encoded publication, if found; {@code null} otherwise.
	 *
	 * @see #getPublication(Date)
	 *
	 * @since 0.4
	 */
	public String getPublication(long publicationId) {
		// Calculate publication cell offset
		int low = 0;
		int high = publicationCount - 1;

		while (low <= high) {
			int index = low + ((high - low) / 2); // Start from the middle
			int offset = publicationBlockBegin + (index * publicationCellSize);
			long myPublicationId = Util.toLong(content, offset);

			if (myPublicationId > publicationId) { // Search before that
				high = index - 1;
			} else if (myPublicationId < publicationId) { // Search after that
				low = index + 1;
			} else { // Found it!
				return getEncodedPublication(offset);
			}
		}

		// Publication not found
		return null;
	}

	/**
	 * Retrieves publication, if any, by the given publication time.
	 *
	 * @param publicationTime publication time.
	 *
	 * @return Base32-encoded publication, if found; {@code null} otherwise.
	 *
	 * @see #getPublication(long)
	 *
	 * @since 0.4
	 */
	public String getPublication(Date publicationTime) {
		return getPublication(publicationTime.getTime() / 1000);
	}

	/**
	 * Returns publication count in this publications file.
	 *
	 * @return publications count in this publications file.
	 */
	public int getPublicationCount() {
		return publicationCount;
	}

	/**
	 * Returns list of all publications contained in this publications file.
	 *
	 * @return publications list of this publications file.
	 *
	 * @since 0.4
	 */
	public List getPublicationList() {
		List list = new ArrayList(publicationCount);
		int offset = publicationBlockBegin;

		for (int i = 0; i < publicationCount; i++) {
			list.add(getEncodedPublication(offset));
			offset += publicationCellSize;
		}

		return list;
	}



	/**
	 * Extracts encoded publication from the given offset.
	 *
	 * @param offset offset to start reading publication from.
	 *
	 * @return Base32-encoded publication.
	 */
	private String getEncodedPublication(int offset) {
		GTHashAlgorithm hashAlg = GTHashAlgorithm.getByGtid(content[offset + TIME_SIZE]);
		int length = TIME_SIZE + 1 + hashAlg.getHashLength();
		return Base32.encodeWithDashes(Util.addCrc32(content, offset, length));
	}



	/*
	 * Property getters: public keys
	 */



	/**
	 * Check if this publications file contains hash of the given public key.
	 *
	 * @param publicKey public key.
	 *
	 * @return {@code true}, if this publications file contains the
	 * 			public key; {@code false} otherwise.
	 *
	 * @since 0.4
	 */
	public boolean contains(PublicKey publicKey) {
		if (publicKey == null) {
			throw new IllegalArgumentException("invalid public key: null");
		}

		// Get public key bytes to be fed to hash calculator
		byte[] publicKeyBytes = publicKey.getEncoded();

		// Init hash algorithm and data hash used to calculate key fingerprint
		GTHashAlgorithm lastHashAlg = null;
		GTDataHash publicKeyHash = null;

		// Iterate through contained key fingerprints, search for matches
		int offset = publicKeyBlockBegin;
		for (int i = 0; i < publicKeyCount; i++) {
			// Extract next public key fingerprint
			GTDataHash currentHash = getPublicKeyHash(offset);

			// Recalculate provided public key hash with new algorithm, if needed
			GTHashAlgorithm hashAlg = (GTHashAlgorithm) currentHash.getHashAlgorithm();
			if (hashAlg != lastHashAlg) {
				publicKeyHash = new GTDataHash(hashAlg).update(publicKeyBytes).close();
				lastHashAlg = hashAlg;
			}

			// Compare calculated public key hash with that contained in
			// publications file
			if (currentHash.equals(publicKeyHash)) { // Found it!
				return true;
			}

			// Move cell start pointer to next public key cell
			offset += publicKeyCellSize;
		}

		// No public key fingerprint found
		return false;
	}

	/**
	 * Returns public key hash count in this publications file.
	 *
	 * @return public key hash count in this publications file.
	 *
	 * @since 0.4
	 */
	public int getPublicKeyCount() {
		return publicKeyCount;
	}

	/**
	 * Returns list of all public key hashes contained in this publications
	 * file.
	 *
	 * @return public key hash list of this publications file.
	 */
	public List getPublicKeyList() {
		List list = new ArrayList(publicKeyCount);
		int offset = publicKeyBlockBegin;

		for (int i = 0; i < publicKeyCount; i++) {
			list.add(getPublicKeyHash(offset));
			offset += publicKeyCellSize;
		}

		return list;
	}



	/**
	 * Extracts the public key hash from the given offset.
	 *
	 * @param offset offset to start reading public key hash from.
	 *
	 * @return data hash object containing public key hash.
	 */
	private GTDataHash getPublicKeyHash(int offset) {
		offset += TIME_SIZE;
		GTHashAlgorithm hashAlg = GTHashAlgorithm.getByGtid(content[offset]);
		int length = hashAlg.getHashLength();
		byte[] pkf = new byte[length];
		System.arraycopy(content, offset + 1, pkf, 0, length);
		return GTDataHash.getInstance(hashAlg, pkf);
	}



	/*
	 * Signature and verification related methods
	 */



	/**
	 * Verifies publications file signature, verifying signature certificates
	 * against default key store.
	 * <p>
	 * Default key store resides at {@code java.home/security/cacerts}.
	 *
	 * @return Publications file verification result.
	 *
	 * @since 0.4
	 */
	public GTVerificationResult verifySignature() {
		// Get default key store path
		StringBuffer sb = new StringBuffer(System.getProperty("java.home"));
		sb.append(File.separator).append("lib");
		sb.append(File.separator).append("security");
		sb.append(File.separator).append("cacerts");
		String keyStorePath = sb.toString();

		return verifySignature(keyStorePath);
	}

	/**
	 * Verifies publications file signature, verifying signature certificates
	 * against the given key store.
	 *
	 * @return Publications file verification result.
	 *
	 * @since 0.4
	 */
	public GTVerificationResult verifySignature(String keyStorePath) {
		// Return result if signature was verified before
		if (isSignatureVerified) {
			return verificationResult;
		}

		// Extract signature
		byte[] signature = new byte[content.length - signatureBlockBegin];
		System.arraycopy(content, signatureBlockBegin, signature, 0, signature.length);

		// Verify PKCS7 file
		try {
			PKCS7 pkcs7 = new PKCS7(signature);
			verifyPkcs7(pkcs7, keyStorePath);
		} catch (ParsingException e) {
			verificationResult.updateErrors(GTVerificationResult.PUBFILE_SIGNATURE_FAILURE);
			return verificationResult;
		} catch (GTException e) {
			verificationResult.updateErrors(GTVerificationResult.PUBFILE_SIGNATURE_FAILURE);
			return verificationResult;
		}

		// All checks passed
		isSignatureVerified = true;
		verificationResult.updateStatus(GTVerificationResult.PUBFILE_SIGNATURE_VERIFIED);

		return verificationResult;
	}

	/**
	 * Verifies PKCS7 object (checks signed data).
	 *
	 * @param pkcs7 PKCS7 object.
	 * @param keyStorePath trusted keystore path
	 *
	 * @throws GTException if any of the checks fails.
	 */
	private void verifyPkcs7(PKCS7 pkcs7, String keyStorePath)
	throws GTException {
		try {
			// Check algorithm
			AlgorithmId[] algorithmIds = pkcs7.getDigestAlgorithmIds();
			if (algorithmIds == null || algorithmIds.length != 1) {
				throw new GTException("publications file signature algorithm check failed");
			}
			String algOid = algorithmIds[0].getOID().toString();
			if (!algOid.equals(GTHashAlgorithm.SHA256.getOid())) {
				throw new GTException("unsupported publications file signature algorithm: " + algOid);
			}

			// Verify signature, and get signers
			byte[] data = new byte[signatureBlockBegin];
			System.arraycopy(content, 0, data, 0, data.length);
			SignerInfo[] signerInfos = pkcs7.verify(data);

			if (signerInfos == null) {
				throw new GTException("publication file is not signed");
			}

			if (signerInfos.length != 1) {
				throw new GTException("more than one signers found, only one (GuardTime) is expected");
			}

			verifySigner(signerInfos[0], pkcs7, keyStorePath);

		} catch (NoSuchAlgorithmException e) {
			throw new GTException("unsupported publications file signature algorithm", e);
		} catch (SignatureException e) {
			throw new GTException("publications file signature verification failed", e);
		}
	}

	/**
	 * Verifies that the signer of the message is GuardTime, and that the
	 * certificate chain is traceable back to trusted root certificate.
	 *
	 *
	 * @param signerInfo signer info
	 * @param pkcs7 pkcs7 message object
	 * @param keyStorePath trusted keystore path
	 *
	 * @throws GTException if validation fails
	 */
	private void verifySigner(SignerInfo signerInfo, PKCS7 pkcs7, String keyStorePath) throws GTException {
		try
		{
			// verify that certificate's subject is GuardTime
			String subjectDn = signerInfo.getCertificate(pkcs7).getSubjectDN().toString();
			if (subjectDn.indexOf("EMAILADDRESS=publications@guardtime.com") == -1) {
				throw new GTException("publication file is not signed by guardtime");
			}

			// form certificate chain from message
			List certChain = signerInfo.getCertificateChain(pkcs7);

			// Build certificate path
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			CertPath certPath = cf.generateCertPath(certChain);

			// Load key store for root certificates
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(keyStorePath), null);

			// Set validation parameters
			PKIXParameters params = new PKIXParameters(keyStore);
			params.setRevocationEnabled(false);

			// Validate certificate path
			CertPathValidator validator = CertPathValidator.getInstance("PKIX");
			validator.validate(certPath, params);

		} catch (CertificateException e) {
			throw new GTException("certificate verification failed", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new GTException("certificate verification failed", e);
		} catch (IOException e) {
			throw new GTException("certificate verification failed", e);
		} catch (KeyStoreException e) {
			throw new GTException("certificate verification failed", e);
		} catch (NoSuchAlgorithmException e) {
			throw new GTException("certificate verification failed", e);
		} catch (CertPathValidatorException e) {
			throw new GTException("certificate verification failed", e);
		}
	}



	/*
	 * Class constructor
	 */
	/**
	 * Creates new publications file object from byte array containing
	 * publications file bytes.
	 *
	 * @param publicationFileBytes publications file.
	 *
	 * @return Publications file object created from bytes provided.
	 */
	private GTPublicationsFile(byte[] b) {
		content = b;
		isSignatureVerified = false;

		// Get header
		if (content.length < HEADER_SIZE) {
			throw new IllegalArgumentException("invalid publications file length: " + content.length);
		}

		// Get and check version
		int version = Util.toShort(content, VERSION_POS);
		if (version != VERSION) {
			throw new IllegalArgumentException("unsupported publications file version: " + version);
		}

		// Get and check publication block params
		publicationBlockBegin = Util.toInt(content, PUBLICATION_BLOCK_BEGIN_POS);
		if (publicationBlockBegin != HEADER_SIZE) {
			throw new IllegalArgumentException("invalid publications block offset: " + publicationBlockBegin);
		}
		publicationCellSize = Util.toShort(content, PUBLICATION_CELL_SIZE_POS);
		publicationCount = Util.toInt(content, PUBLICATION_COUNT_POS);

		// Get and check public key block params
		publicKeyBlockBegin = Util.toInt(content, PUBLIC_KEY_BLOCK_BEGIN_POS);
		if (publicKeyBlockBegin != publicationBlockBegin + (publicationCellSize * publicationCount)) {
			throw new IllegalArgumentException("invalid publications block offset: " + publicKeyBlockBegin);
		}
		publicKeyCellSize = Util.toShort(content, PUBLIC_KEY_CELL_SIZE_POS);
		publicKeyCount = Util.toShort(content, PUBLIC_KEY_COUNT_POS);

		// Get and check publication references' block params
		publicationReferenceBlockBegin = Util.toInt(content, PUBLICATION_REFERENCES_BLOCK_BEGIN_POS);
		if (publicationReferenceBlockBegin >= content.length) {
			throw new IllegalArgumentException("invalid publication reference block offset: " + publicationReferenceBlockBegin);
		}

		// Get and check signature block params
		signatureBlockBegin = Util.toInt(content, SIGNATURE_BLOCK_BEGIN_POS);
		if (signatureBlockBegin >= content.length) {
			throw new IllegalArgumentException("invalid signature block offset: " + signatureBlockBegin);
		}

		// Init verification result
		verificationResult = new GTVerificationResult();
	}
}
