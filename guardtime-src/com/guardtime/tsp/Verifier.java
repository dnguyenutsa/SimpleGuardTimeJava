/*
 * $Id: Verifier.java 217 2011-09-15 20:02:06Z ahto.truu $
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

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.guardtime.asn1.ContentInfo;
import com.guardtime.asn1.MessageImprint;
import com.guardtime.asn1.SignatureInfo;
import com.guardtime.asn1.SignedData;
import com.guardtime.asn1.SignerInfo;
import com.guardtime.asn1.TimeSignature;
import com.guardtime.util.Base32;



abstract class Verifier {
	static GTVerificationResult verify(ContentInfo contentInfo, GTDataHash dataHash, String publication, PublicKey publicKey) {
		GTVerificationResult result = new GTVerificationResult();

		// Extract needed structures
		SignedData signedData = contentInfo.getContent();
		SignerInfo signerInfo = signedData.getSignerInfo();
		TimeSignature timeSignature = signerInfo.getSignature();

		// Set status bits for time signature components
		if (timeSignature.getPubReferences() != null) {
			result.updateStatus(GTVerificationResult.PUBLICATION_REFERENCE_PRESENT);
		}
		if (timeSignature.getPkSignature() != null) {
			result.updateStatus(GTVerificationResult.PUBLIC_KEY_SIGNATURE_PRESENT);
		}

		// Basic syntax check is done in timestamp constructor

		// Check data hash
		MessageImprint messageImprint = signedData.getEContent().getMessageImprint();
		result.update(checkDataHash(messageImprint, dataHash));
		if (!result.isValid()) {
			return result;
		}

		// Check the message-digest signed attribute
		byte[] eContent = signedData.getEContent().getDerEncoded();
		GTHashAlgorithm digestAlg = GTHashAlgorithm.getByOid(signerInfo.getDigestAlgorithm());
		byte[] messageDigest = signerInfo.getMessageDigest();
		result.update(verifyMessageDigest(messageDigest, digestAlg, eContent));

		// Verify time signature (hash chains)
		byte[] signedAttrs = signerInfo.getEncodedSignedAttrs();
		if (signedAttrs == null) {
			throw new IllegalArgumentException("invalid signed attrs: null");
		}
		result.update(verifyHashChains(timeSignature, digestAlg, signedAttrs));
		if (!result.isValid()) {
			return result;
		}

		// If timestamp is extended, verify publication.
		// Else, verify certificate and public key signature.
		if (timeSignature.isExtended()) {
			result.update(verifyPublication(timeSignature, publication));
		} else {
			// Extract certificate bytes
			X509Certificate certificate = contentInfo.getContent().getCertificate();

			// Get history time
			BigInteger publicationId = timeSignature.getPublishedData().getPublicationId();
			HashChain historyChain = HashChain.getHistoryInstance(timeSignature.getHistory());
			BigInteger historyId = historyChain.computeHistoryId(publicationId);
			Date historyTime = new Date(historyId.longValue() * 1000);

			// Verify certificate
			result.update(verifyCertificate(certificate, publicKey, historyTime));
			if (!result.isValid()) {
				return result;
			}

			// Verify public key signature
			result.update(verifyPkSignature(timeSignature, publicKey));
			if (!result.isValid()) {
				return result;
			}
		}

		// All done
		return result;
	}



	/*
	 * Data hash checks
	 */


	private static GTVerificationResult checkDataHash(MessageImprint messageImprint, GTDataHash dataHash) {
		GTVerificationResult result = new GTVerificationResult();

		// Check arguments
		if (messageImprint == null) {
			throw new IllegalArgumentException("message imprint is null");
		} else if (dataHash == null) {
			return result;
		}

		// Compare hashes
		if (!dataHash.getHashAlgorithm().getOid().equals(messageImprint.getHashAlgorithm())) {
			result.updateErrors(GTVerificationResult.WRONG_DOCUMENT_FAILURE);
		} else if (!Arrays.equals(dataHash.getHashedMessage(), messageImprint.getHashedMessage())) {
			result.updateErrors(GTVerificationResult.WRONG_DOCUMENT_FAILURE);
		}

		// Mark check as passed
		result.updateStatus(GTVerificationResult.DATA_HASH_CHECKED);

		return result;
	}



	/*
	 * Certificate checks (signed timestamps only)
	 */



	private static GTVerificationResult verifyCertificate(X509Certificate certificate, PublicKey publicKey, Date historyTime) {
		GTVerificationResult result = new GTVerificationResult();

		// Check arguments
		if (certificate == null) {
			throw new IllegalArgumentException("certificate is null");
		} else if (publicKey == null) {
			throw new IllegalArgumentException("public key is null");
		} else if (historyTime == null) {
			throw new IllegalArgumentException("history time is null");
		}


		try {
			// Check if certificate is valid now
			// This is NOT needed, we are happy just if public key hash exists in publications file.
			//certificate.checkValidity();

			// Check if certificate was valid when timestamp was created
			// This is also NOT needed, for the same reason as above
			//certificate.checkValidity(historyTime);

			// Verify certificate against public key
			certificate.verify(publicKey);

			// No tracing of the certificate chain to a root,
			// as the GuardTime timestamp token signing keys
			// are not maintained using public PKI. Instead,
			// the valid keys are listed in the GuardTime
			// publications file.
		} catch (CertificateException e) {
			result.updateErrors(GTVerificationResult.CERTIFICATE_FAILURE);
		} catch (InvalidKeyException e) {
			result.updateErrors(GTVerificationResult.CERTIFICATE_FAILURE);
		} catch (NoSuchAlgorithmException e) {
			result.updateErrors(GTVerificationResult.CERTIFICATE_FAILURE);
		} catch (NoSuchProviderException e) {
			result.updateErrors(GTVerificationResult.CERTIFICATE_FAILURE);
		} catch (SignatureException e) {
			result.updateErrors(GTVerificationResult.CERTIFICATE_FAILURE);
		}

		return result;
	}



	/*
	 * Embedded TSTInfo checks
	 */



	private static GTVerificationResult verifyMessageDigest(byte[] messageDigest, GTHashAlgorithm digestAlg, byte[] eContent) {
		GTVerificationResult result = new GTVerificationResult();

		GTDataHash hash = new GTDataHash(digestAlg);
		hash.update(eContent);
		if (!Arrays.equals(hash.getHashedMessage(), messageDigest)) {
			result.updateErrors(GTVerificationResult.HASHCHAIN_VERIFICATION_FAILURE);
		}

		return result;
	}



	/*
	 * Time signature checks
	 */



	private static GTVerificationResult verifyHashChains(TimeSignature timeSignature, GTHashAlgorithm digestAlg, byte[] signedAttrs) {
		GTVerificationResult result = new GTVerificationResult();

		// Init chains
		byte[] locationChainBytes = timeSignature.getLocation();
		byte[] historyChainBytes = timeSignature.getHistory();

		HashChain locationChain = null;
		HashChain historyChain = null;

		try {
			locationChain = HashChain.getLocationInstance(locationChainBytes);
			historyChain = HashChain.getHistoryInstance(historyChainBytes);
		} catch (IllegalArgumentException e) {
			result.updateErrors(GTVerificationResult.SYNTACTIC_CHECK_FAILURE);
			return result;
		}

		// Check publication imprint
		byte[] publicationImprint = timeSignature.getPublishedData().getPublicationImprint();
		GTHashAlgorithm publicationImprintAlg = GTHashAlgorithm.getByGtid(publicationImprint[0]);
		if (publicationImprintAlg.getHashLength() + 1 != publicationImprint.length) {
			result.updateErrors(GTVerificationResult.SYNTACTIC_CHECK_FAILURE);
			return result;
		}

		// Get input
		byte[] input = new GTDataHash(digestAlg).update(signedAttrs).toDataImprint();

		// Calculate output
		byte[] locationOutput = locationChain.computeOutput(input);
		byte[] historyOutput = historyChain.computeOutput(locationOutput);
		byte[] output = new GTDataHash(publicationImprintAlg).update(historyOutput).toDataImprint();

		// Compare imprints
		if (!Arrays.equals(output, publicationImprint)) {
			result.updateErrors(GTVerificationResult.HASHCHAIN_VERIFICATION_FAILURE);
		}

		return result;
	}

	private static GTVerificationResult verifyPublication(TimeSignature timeSignature, String publication) {
		GTVerificationResult result = new GTVerificationResult();

		// Check arguments
		if (publication == null) {
			return result;
		}

		// Decode publications
		byte[] expected = Base32.decode(publication);
		byte[] actual = Base32.decode(timeSignature.getPublishedData().getEncodedPublication());

		// Compare publications
		if (!Arrays.equals(expected, actual)) {
			result.updateErrors(GTVerificationResult.PUBLICATION_FAILURE);
			return result;
		}

		// Mark check as passed
		result.updateStatus(GTVerificationResult.PUBLICATION_CHECKED);

		return result;
	}

	private static GTVerificationResult verifyPkSignature(TimeSignature timeSignature, PublicKey publicKey) {
		GTVerificationResult result = new GTVerificationResult();

		// Check arguments
		if (publicKey == null) {
			return result;
		}

		// Set BouncyCastle provider
		String provider = BouncyCastleProvider.PROVIDER_NAME;
		if (Security.getProvider(provider) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		try {
			// Create and initialize PK signature
			SignatureInfo pkSignature = timeSignature.getPkSignature();
			Signature signature = Signature.getInstance(pkSignature.getSignatureAlgorithm(), provider);
			signature.initVerify(publicKey);
			signature.update(timeSignature.getPublishedData().getDerEncoded());

			// Verify PK signature
			if (!signature.verify(pkSignature.getSignatureValue())) {
				result.updateErrors(GTVerificationResult.PUBLIC_KEY_SIGNATURE_FAILURE);
			}
		} catch (SignatureException e) {
			result.updateErrors(GTVerificationResult.PUBLIC_KEY_SIGNATURE_FAILURE);
		} catch (NoSuchProviderException e) {
			result.updateErrors(GTVerificationResult.PUBLIC_KEY_SIGNATURE_FAILURE);
		} catch (NoSuchAlgorithmException e) {
			result.updateErrors(GTVerificationResult.PUBLIC_KEY_SIGNATURE_FAILURE);
		} catch (InvalidKeyException e) {
			result.updateErrors(GTVerificationResult.PUBLIC_KEY_SIGNATURE_FAILURE);
		}

		return result;
	}
}
