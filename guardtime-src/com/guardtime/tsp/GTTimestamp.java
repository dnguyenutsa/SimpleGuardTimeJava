/*
 * $Id: GTTimestamp.java 199 2011-04-19 18:44:19Z ahto.truu $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.CertToken;
import com.guardtime.asn1.CertTokenRequest;
import com.guardtime.asn1.ContentInfo;
import com.guardtime.asn1.PublishedData;
import com.guardtime.asn1.SignedData;
import com.guardtime.asn1.TimeSignature;
import com.guardtime.asn1.TimestampRequest;
import com.guardtime.asn1.TstInfo;
import com.guardtime.util.Base16;



/**
 * Timestamp object.
 * <p>
 * GuardTime timestamps can be either PKI-signed or hash-linked. PKI-signed
 * timestamps can be verified using GuardTime public key certificate and thus
 * are directly usable only until the certificate expires.
 * Hash-linked (extended) timestamps can be
 * verified independently from GuardTime using control publications. There are
 * no time limitations for hash-linked timestamp verification, so those are more
 * suitable for long-term archival.
 * <p>
 * GuardTime timestamps are internally represented in ASN.1 and serialized and
 * stored in DER-encoding. This class is a wrapper to decode, format and
 * otherwise process a timestamp object.
 * <p>
 * A GuardTime timestamp is instantiated by:
 * <ul>
 * <li>creating a new PKI-signed timestamp for the hash value of the data that
 * needs to be timestamped;
 * <li>extending an existing PKI-signed timestamp to create a hash-linked one.
 * <li>loading an existing DER-encoded timestamp from some storage;
 * </ul>
 * <p>
 * To create a new PKI-signed timestamp for a data hash, use:
 * <ol>
 * <li>{@link #composeRequest(GTDataHash)} to prepare a timestamp request
 * that can be sent to a stamping service;
 * <li>{@link GTTimestampResponse#getInstance(byte[])} to
 * create a timestamp response object from the stamping service response;
 * <li>{@link GTTimestampResponse#getTimestamp()} to extract
 * a timestamp out of timestamp response recieved from the stamping service.
 * </ol>
 * <p>
 * To extend a PKI-signed timestamp (link it against publication), use:
 * <ol>
 * <li>{@link #composeExtensionRequest()} to prepare an extension request
 * that can be sent to an extending service;
 * <li>{@link GTCertTokenResponse#getInstance(byte[])} to
 * create an extension response object from the extending service reponse;
 * <li>{@link #extend(GTCertTokenResponse)} to create a hash-linked timestamp
 * out of the extending response received from the extender service.
 * </ol>
 * {@link #isExtended()} shows if timestamp is hash-linked or not.
 * <p>
 * To load an existing DER-encoded timestamp, use one of these methods:
 * <ul>
 * <li>{@link #getInstance(byte[])} or {{@link #getInstance(byte[], int, int)}}
 * to create an instance of {@code GTTimestamp} using bytes of provided array;
 * <li>{@link #getInstance(InputStream)} to create an instance of
 * {@code GTTimestamp} reading bytes from the provided {@code InputStream}.
 * </ul>
 * <p>
 * To save a timestamp, use {@link #getEncoded()} method to produce a byte
 * array suitable for storing and later feeding to one of {@code getInstance}
 * methods.
 * <p>
 * To verify a timestamp, use one of {@code verify} methods:
 * <ul>
 * <li>{@link #verify(GTDataHash, GTPublicationsFile)} can be used with both
 * signed and hash-linked timestamps. Publications file contains all currently
 * valid keys to verify signed timestamps, and all publications to verify
 * hash-linked ones. Publications file will also be verified.
 * <li>{@link #verify(GTDataHash, String)} only makes sense for hash-linked
 * timestamps. Note that timestamp to publication relation is checked here but
 * not publication origin.
 * </ul>
 *
 * @see GTTimestampResponse
 * @see GTCertTokenResponse
 * @see GTPublicationsFile
 *
 * @since 0.1
 */
public class GTTimestamp {
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.accuracy}.
	 * <p>
	 * The value of the property is presented in human-readable format in
	 * seconds, milliseconds, or microseconds, depending on the magnitude.
	 *
	 * @see #getProperty(String)
	 */
	public static final String ACCURACY = "issuer.accuracy";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.messageImprint.hashAlgorithm}.
	 * <p>
	 * The value of the property the identifier of the hash algorithm used to
	 * hash the timestamped datum. It is presented in the ASN.1 OID decimal
	 * notation.
	 *
	 * @see #getProperty(String)
	 */
	public static final String HASH_ALGORITHM = "hashAlgorithm";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.messageImprint.hashedMessage}.
	 * <p>
	 * The value of the property is the hash value of the timestamped datum. It
	 * is presented in hexadecimal notation.
	 *
	 * @see #getProperty(String)
	 */
	public static final String HASHED_MESSAGE = "hashedMessage";
	/**
	 * Name of the property for the history identifier extracted from
	 * {@code timeStampToken.signerInfo.signature.history}.
	 * <p>
	 * This is arguably the most important property of the timestamp as it
	 * represents the time when the timestamp was registered in the GuardTime
	 * calendar. The value is essentially a Unix <code>time_t</code> value,
	 * that is the number of seconds elapsed from 1970-01-01 00:00:00 UTC to the
	 * registration time.
	 *
	 * @see #getProperty(String)
	 * @see #REGISTERED_TIME
	 */
	public static final String HISTORY_ID = "history.id";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.tsa}.
	 * <p>
	 * The value of the property is the name of the service that issued the
	 * timestamp. For GuardTime timestamps, this is the hostname of the gateway.
	 *
	 * @see #getProperty(String)
	 */
	public static final String ISSUER_NAME = "issuer.name";
	/**
	 * Name of the property for the location identifier extracted from
	 * {@code timeStampToken.signerInfo.signature.location}.
	 * <p>
	 * The value of the property is the identifier of the GuardTime gateway that
	 * issued the timestamp. It has generally no meaning for the end users, but
	 * may come in handy in troubleshooting.
	 *
	 * @see #getProperty(String)
	 */
	public static final String LOCATION_ID = "location.id";
	/**
	 * Name of the property for the location name extracted from
	 * {@code timeStampToken.signerInfo.signature.location}.
	 * <p>
	 * The value of the property is the name of the GuardTime gateway that
	 * issued the timestamp. The names form a hierarchical name-space comparable
	 * to DNS names for Internet hosts.
	 * 
	 * @see #getProperty(String)
	 */
	public static final String LOCATION_NAME = "location.name";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.policy}.
	 * <p>
	 * The value of the property is the identifier of the timestamping policy
	 * under which the timestamp was issued. It is presented in the ASN.1 OID
	 * decimal notation.
	 *
	 * @see #getProperty(String)
	 */
	public static final String POLICY_ID = "policy.id";
	/**
	 * Name of the property for the control publication
	 * {@code timeStampToken.signerInfo.signature.publishedData}.
	 * <p>
	 * The value of the property is formatted for human reading and publication
	 * in printed media. The property is {@code null} for unextended timestamps,
	 * as they are not connected to any control publications.
	 *
	 * @see #getProperty(String)
	 */
	public static final String PUBLICATION = "publication.value";
	/**
	 * Name of the property for
	 * {@code timeStampToken.signerInfo.signature.publishedData.publicationIdentifier}.
	 * <p>
	 * The value is essentially a Unix <code>time_t</code> value for the
	 * second when the publication imprint was extracted from the GuardTime
	 * calendar tree.
	 *
	 * @see #getProperty(String)
	 * @see #PUBLICATION_TIME
	 */
	public static final String PUBLICATION_ID = "publication.id";
	/**
	 * Name of the property for
	 * {@code timeStampToken.signerInfo.signature.publishedData.publicationIdentifier}.
	 * <p>
	 * The value is the time when the publication imprint was extracted from the
	 * GuardTime calendar tree, presented as a UTC date-time.
	 *
	 * @see #getProperty(String)
	 * @see #PUBLICATION_ID
	 */
	public static final String PUBLICATION_TIME = "publication.time";
	/**
	 * Name of the property for
	 * {@code timeStampToken.signerInfo.signature.pubReference}.
	 * <p>
	 * The value of the property is a list of bibliographic references to the
	 * printed media where the control publication can be found. The property is
	 * {@code null} for unextended timestamps, as they are not connected to any
	 * control publications.
	 *
	 * @see #getProperty(String)
	 */
	public static final String PUBLICATION_REFERENCES = "publication.references";
	/**
	 * Name of the property for the timestamp registration time (history
	 * identifier) extracted from
	 * {@code timeStampToken.signerInfo.signature.history}.
	 * <p>
	 * Represents the time when the timestamp was registered in the GuardTime
	 * calendar. The value is presented as formatted UTC time.
	 *
	 * @see #getProperty(String)
	 * @see #HISTORY_ID
	 */
	public static final String REGISTERED_TIME = "registeredTime";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.genTime}.
	 * <p>
	 * The value of the property is the time when the timestamping request was
	 * received by the GuardTime gateway, according to the gateway's local
	 * clock. The value is presented as formatted UTC time.
	 *
	 * @see #getProperty(String)
	 */
	public static final String REQUEST_TIME = "issuer.genTime";
	/**
	 * Name of the property for
	 * {@code timeStampToken.content.encapContentInfo.eContent.serialNumber}.
	 * <p>
	 * The value of the property is the serial number of the timestamp.
	 *
	 * @see #getProperty(String)
	 */
	public static final String SERIAL_NUMBER = "issuer.serialNumber";

	private long registeredTime;
	private ContentInfo token;
	private GTDataHash dataHash;
	private Properties properties;
	private SimpleDateFormat dateFormat;
	private GTVerificationResult verificationResult;



	/*
	 * Initializers
	 */



	/**
	 * Creates a new timestamp object from the given byte array containing a
	 * DER-encoded timestamp.
	 * <p>
	 * Timestamp's internal consistency is also checked. If the timestamp has
	 * invalid format or is missing some critical properties, a
	 * {@code GTException} will be thrown.
	 *
	 * @param b DER-encoded timestamp.
	 *
	 * @return timestamp object.
	 *
	 * @throws GTException if timestamp has invalid format.
	 */
	public static GTTimestamp getInstance(byte[] b)
	throws GTException {
		if (b == null) {
			throw new IllegalArgumentException("invalid timestamp data: null");
		}

		return getInstance(b, 0, b.length);
	}

	/**
	 * Creates a new timestamp object from part of the given byte array a
	 * containing DER-encoded timestamp.
	 * <p>
	 * Timestamp's internal consistency is also checked. If the timestamp has
	 * invalid format or is missing some critical properties, a
	 * {@code GTException} will be thrown.
	 *
	 * @param b byte array containing DER-encoded timestamp.
	 * @param offset offset to start reading timestamp from.
	 * @param length length of the timestamp.
	 *
	 * @return timestamp object.
	 *
	 * @throws GTException if timestamp has invalid format.
	 *
	 * @since 0.4
	 */
	public static GTTimestamp getInstance(byte[] b, int offset, int length)
	throws GTException {
		if (b == null) {
			throw new IllegalArgumentException("invalid timestamp data: null");
		}

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b, offset, length);
			return getInstance(in);
		} catch (IOException e) {
			throw new GTException("timestamp has invalid format", e);
		}
	}

	/**
	 * Reads a new timestamp object from the given input stream.
	 * <p>
	 * Timestamp internal consistency is also checked. If encoded timestamp has
	 * invalid format or is missing some critical properties, a
	 * {@code GTException} will be thrown.
	 *
	 * @param in input stream containing DER-encoded timestamp.
	 *
	 * @return timestamp object.
	 *
	 * @throws GTException if timestamp has invalid format.
	 * @throws IOException if stream reading error occurs.
	 *
	 * @since 0.3
	 */
	public static GTTimestamp getInstance(InputStream in)
	throws GTException, IOException {
		if (in == null) {
			throw new IllegalArgumentException("invalid timestamp stream: null");
		}

		try {
			ContentInfo token = ContentInfo.getInstance(in);
			return new GTTimestamp(token);
		} catch (Asn1FormatException e) {
			throw new GTException("timestamp has invalid format", e);
		}
	}



	/*
	 * Request/response methods
	 */



	/**
	 * Composes a timestamp request for the given data hash.
	 * <p>
	 * This request is ready to be transported to the stamping service over
	 * regular HTTP.
	 * <p>
	 * After the request is sent to the stamping service, use
	 * {@link GTTimestampResponse#getInstance(byte[])} to wrap
	 * the received response, and
	 * {@link GTTimestampResponse#getTimestamp()} to extract
	 * timestamp from the response.
	 *
	 * @param dataHash hash value to create the timestamp request for.
	 *
	 * @return timestamp request ready to be sent to the stamping service.
	 *
	 * @see GTTimestampResponse
	 *
	 * @since 0.4
	 */
	public static byte[] composeRequest(GTDataHash dataHash) {
		if (dataHash == null) {
			throw new IllegalArgumentException("invalid data hash: null");
		}

		String algOid = dataHash.getHashAlgorithm().getOid();
		byte[] hashedMessage = dataHash.getHashedMessage();
		TimestampRequest request = TimestampRequest.compose(algOid, hashedMessage);

		return request.getDerEncoded();
	}



	/*
	 * Class constructor and encoder method
	 */



	/**
	 * Class constructor.
	 * <p>
	 * Called by {@code getInstance} methods and
	 * {@link GTTimestampResponse} constructor.
	 *
	 * @throws GTException if timestamp internal structure has invalid format.
	 */
	GTTimestamp(ContentInfo token)
	throws GTException {
		if (token == null) {
			throw new IllegalArgumentException("invalid timestamp token: null");
		}

		this.token = token;

		verificationResult = new GTVerificationResult();

		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		updateProperties();
	}

	/**
	 * Encodes this timestamp using DER.
	 * <p>
	 * Resulting byte array suitable for writing to file, transporting over
	 * network, storing in database, etc.
	 *
	 * @return DER-encoded timestamp.
	 *
	 * @since 0.4
	 */
	public byte[] getEncoded() {
		return token.getDerEncoded();
	}



	/*
	 * Property getters
	 */



	/**
	 * Gets the data hash which this timestamp was created for.
	 *
	 * @return data hash.
	 */
	public GTDataHash getDataHash() {
		return dataHash;
	}

	/**
	 * Gets the algorithm used to hash data which this timestamp was created
	 * for.
	 *
	 * @return hash algorithm.
	 */
	public GTHashAlgorithm getHashAlgorithm() {
		return dataHash.getHashAlgorithm();
	}

	/**
	 * Gets the timestamp property by the given name.
	 * <p>
	 * Property names are defined in {@link GTTimestamp}.
	 *
	 * @param name property name.
	 *
	 * @return property value, or {@code null}, if property is not set.
	 *
	 * @since 0.4
	 */
	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	/**
	 * Gets the time this timestamp was registered at.
	 *
	 * @return timestamp registration time.
	 *
	 * @since 0.4
	 */
	public Date getRegisteredTime() {
		return new Date(registeredTime * 1000);
	}



	/*
	 * Extension methods
	 */



	/**
	 * Composes an extension request for this timestamp.
	 * <p>
	 * This request is ready to be transported to the extending service over
	 * regular HTTP.
	 * <p>
	 * After the request is sent to the extending service, use
	 * {@link GTCertTokenResponse#getInstance(byte[])} to wrap
	 * the received response, and {@link #extend(GTCertTokenResponse)} to extend the
	 * timestamp.
	 *
	 * @return extension request ready to be sent to extension service.
	 *
	 * @see GTCertTokenResponse
	 *
	 * @since 0.4
	 */
	public byte[] composeExtensionRequest() {
		TimeSignature signature = token.getContent().getSignerInfo().getSignature();
		BigInteger publicationId = signature.getPublishedData().getPublicationId();
		HashChain historyChain = HashChain.getHistoryInstance(signature.getHistory());
		BigInteger historyId = historyChain.computeHistoryId(publicationId);
		return CertTokenRequest.compose(historyId).getDerEncoded();
	}

	/**
	 * Extends this timestamp using the given extension reponse.
	 * <p>
	 * You should check response status before trying to extend the timestamp.
	 * If {@link GTCertTokenResponse#getStatusCode()} returns
	 * {@code 0} or {@code 1}, it is likely that timestamp can be extended.
	 * Any other status code means that extension will fail.
	 *
	 * @param response extension response.
	 *
	 * @return extended timestamp.
	 *
	 * @throws GTException if internal structure of extended timestamp has
	 * 			invalid format.
	 *
	 * @see GTCertTokenResponse
	 *
	 * @since 0.4
	 */
	public GTTimestamp extend(GTCertTokenResponse response)
	throws GTException {
		// Make sure response is not null
		if (response == null) {
			throw new IllegalArgumentException("invalid reponse: null");
		}

		// Check response status
		int statusCode = response.getStatusCode();
		if (statusCode < 0 || statusCode > 1) {
			throw new IllegalArgumentException("extending not possible, response error: " + response.getFailCode());
		}

		CertToken certToken = response.getToken();

		// Compare past entries in history chains
		HashChain oldChain = HashChain.getHistoryInstance(token.getContent().getSignerInfo().getSignature().getHistory());
		HashChain newChain = HashChain.getHistoryInstance(certToken.getHistory());
		if (!oldChain.checkPastEntries(newChain)) {
			throw new GTException("past history chains do not match in timestamp and response");
		}

		// Extend timestamp
		try {
			token = token.extend(certToken);
		} catch (Asn1FormatException e) {
			// Failed to extend timestamp
			throw new GTException("extended timestamp has invalid fromat", e);
		}

		// Update properties
		updateProperties();

		// All done
		return this;
	}

	/**
	 * Checks if this timestamp is extended (hash-linked).
	 *
	 * @return {@code true} if timestamp is extended, {@code false} otherwise.
	 */
	public boolean isExtended() {
		return token.isExtended();
	}



	/*
	 * Verification methods
	 */



	/**
	 * Verifies this timestamp with the given data hash and publications file.
	 * Timestamp verification involves these steps:
	 * <ul>
	 * <li>Timestamp's internal consistency and parameters are checked.
	 * <li>Data hash is extracted from the timestamp and compared to the one
	 * provided.
	 * <li>Publications file signature is verified.
	 * <li>If timestamp is extended, publication is extracted, and the publications
	 * file is checked to contain that publication.
	 * <li>If timestamp is signed, public key is extracted from certificate,
	 * and the publications file is checked to contain that public key.
	 * </ul>
	 *
	 * @param dataHash data hash this timestamp was created for.
	 * @param publicationsFile publications file.
	 *
	 * @return timestamp verification result.
	 *
	 * @since 0.4
	 */
	public GTVerificationResult verify(GTDataHash dataHash, GTPublicationsFile publicationsFile) {
		if (publicationsFile == null) {
			throw new IllegalArgumentException("invalid publications file: null");
		}

		// Verify publications file
		verificationResult.update(publicationsFile.verifySignature());
		if (!verificationResult.isValid()) {
			return verificationResult;
		}

		// Extract publication or public key;
		// Check if they are contained in publications file
		String publication = null;
		PublicKey publicKey = null;
		if (isExtended()) {
			// Extract publication
			publication = getProperty(PUBLICATION);

			// Check if publication exists in publications file
			if (!publicationsFile.contains(publication)) {
				// Not critical, go on with verification
				verificationResult.updateErrors(GTVerificationResult.PUBLICATION_FAILURE);
			}
		} else {
			// Extract certificate
			X509Certificate certificate = token.getContent().getCertificate();

			// Extract public key
			publicKey = certificate.getPublicKey();

			// Check if public key exists in publications file
			if (publicationsFile.contains(publicKey)) {
				verificationResult.updateStatus(GTVerificationResult.PUBLICATION_CHECKED);
			} else {
				// Not critical, go on with verification
				verificationResult.updateErrors(GTVerificationResult.PUBLIC_KEY_FAILURE);
			}
		}

		verificationResult.update(verify(dataHash, publication, publicKey));

		return verificationResult;
	}

	/**
	 * Verifies this timestamp with the given data hash and publication.
	 * <p>
	 * Note that this method is applicable to extended timestamps only.
	 * For signed timestamps, use
	 * {@link #verify(GTDataHash, GTPublicationsFile)}.
	 * <p>
	 * Timestamp verification involves these steps:
	 * <ul>
	 * <li>Timestamp's internal consistency and parameters are checked.
	 * <li>Data hash is extracted from the timestamp and compared to the one
	 * provided.
	 * <li>Publication is extracted from the timestamp and checked to match
	 * the provided one.
	 * </ul>
	 *
	 * @param dataHash data hash this timestamp was created for.
	 *
	 * @return timestamp verification result.
	 *
	 * @throws IllegalStateException if this timestamp is not extended.
	 *
	 * @since 0.4
	 */
	public GTVerificationResult verify(GTDataHash dataHash, String publication) {
		if (publication == null) {
			throw new IllegalArgumentException("invalid publication: null");
		} else if (!isExtended()) {
			throw new IllegalStateException("cannot verify against publication: timestamp not extended");
		}

		verificationResult.update(verify(dataHash, publication, null));

		return verificationResult;
	}



	/*
	 * Common private methods
	 */



	/**
	 * Parses timestamp contents and checks vital parameters.
	 * <p>
	 * This method actually performs timestamp syntactic check. This procedure
	 * was previously part of verification, however, it is much more clear to
	 * run it while creating a timestamp, as we have nothing to do with
	 * a timestamp which is syntactically invalid.
	 * <p>
	 * Used by class constructor and {@link #extend(Response)} method.
	 *
	 * @throws GTException is some property is invalid.
	 */
	private void updateProperties()
	throws GTException {
		properties = new Properties();

		// Extract data hash
		String hashAlgOid = token.getContent().getEContent().getMessageImprint().getHashAlgorithm();
		GTHashAlgorithm hashAlgorithm = null;
		try {
			hashAlgorithm = GTHashAlgorithm.getByOid(hashAlgOid);
		} catch (IllegalArgumentException e) {
			throw new GTException("timestamp has invalid format", e);
		}
		byte[] hashedMessage = token.getContent().getEContent().getMessageImprint().getHashedMessage();
		dataHash = GTDataHash.getInstance(hashAlgorithm, hashedMessage);

		properties.setProperty(HASH_ALGORITHM, hashAlgOid);
		properties.setProperty(HASHED_MESSAGE, Base16.encode(hashedMessage));

		// Extract signedData
		SignedData signedData = token.getContent();

		// Extract tstInfo
		TstInfo tstInfo = signedData.getEContent();

		properties.setProperty(POLICY_ID, tstInfo.getPolicy());
		properties.setProperty(SERIAL_NUMBER, tstInfo.getSerialNumber().toString());

		// Request time: UTC (tstInfo.genTime)
		properties.setProperty(REQUEST_TIME, dateFormat.format(tstInfo.getGenTime()));

		// (OPT) Accuracy: microseconds (tstInfo.accuracy.seconds + .millis + .micros)
		String accuracy = tstInfo.getFormattedAccuracy();
		if (accuracy != null) {
			properties.setProperty(ACCURACY, accuracy);
		}

		// (OPT) Issuer name: tstInfo.tsa
		String tsa = tstInfo.getFormattedTsa();
		if (tsa != null) {
			properties.setProperty(ISSUER_NAME, tsa);
		}

		// Extract timeSignature
		TimeSignature timeSignature = signedData.getSignerInfo().getSignature();

		// Registered time (history ID): computed from history chain and publication ID
		BigInteger publicationId = timeSignature.getPublishedData().getPublicationId();
		HashChain historyChain = HashChain.getHistoryInstance(timeSignature.getHistory());
		BigInteger historyId = historyChain.computeHistoryId(publicationId);
		registeredTime = historyId.longValue();
		properties.setProperty(HISTORY_ID, historyId.toString());
		properties.setProperty(REGISTERED_TIME, dateFormat.format(getRegisteredTime()));

		// Location ID and name: computed from location chain
		HashChain locationChain = HashChain.getLocationInstance(timeSignature.getLocation());
		BigInteger locationId = locationChain.computeLocationId();
		properties.setProperty(LOCATION_ID, locationId.toString());
		String locationName = locationChain.extractLocationName();
		if (locationName != null) {
			properties.setProperty(LOCATION_NAME, locationName);
		}

		if (isExtended()) {
			// Extract publishedData
			PublishedData publishedData = timeSignature.getPublishedData();

			// (EXT-ONLY) Publication ID: publishedData.publicationIdentifier
			properties.setProperty(PUBLICATION_ID, publicationId.toString());
			properties.setProperty(PUBLICATION_TIME, dateFormat.format(new Date(publicationId.longValue() * 1000)));

			// (EXT-ONLY) Publication: Base32-encoded publication
			properties.setProperty(PUBLICATION, publishedData.getEncodedPublication());

			// (EXT-ONLY) Publication references: timeSignature.getPubReferences
			List publicationReferences = timeSignature.getPubReferences();
			if (publicationReferences != null) {
				properties.setProperty(PUBLICATION_REFERENCES, publicationReferences.toString());
			}
		}
	}

	/**
	 * Common verification method.
	 *
	 * @param dataHash data hash this timestamp was created for.
	 * @param publication publication this timestamp is linked to.
	 * 			Should be provided for extended timestamps.
	 * 			Could be {@code null} for signed timestamps.
	 * @param publicKey public key to verify time signature with.
	 * 			Should be provided for signed timestamps.
	 * 			Could be {@code null} for extended timestamps.
	 *
	 * @return timestamp verification result.
	 */
	private GTVerificationResult verify(GTDataHash dataHash, String publication, PublicKey publicKey) {
		if (dataHash == null) {
			throw new IllegalArgumentException("invalid data hash: null");
		}

		return Verifier.verify(token, dataHash, publication, publicKey);
	}
}
