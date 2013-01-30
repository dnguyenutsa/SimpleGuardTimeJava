/*
 * $Id: SimpleHttpStamper.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package com.guardtime.transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.guardtime.tsp.GTCertTokenResponse;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;
import com.guardtime.tsp.GTVerificationResult;



/**
 * A collection of static methods for basic operations with timestamps.
 * <p>
 * Methods of this class use built-in HTTP transport tools, so you don't need to
 * worry about data transport either.
 * <p>
 * To create a timestamp, use the {@link #create(GTDataHash, URL)} method.
 * Provide the hash value calculated from the data you want to timestamp and the
 * URL of the stamping service you want to use. See
 * {@link com.guardtime.tsp.GTDataHash} documentation for details on how to
 * calculate data hash.
 * <p>
 * To extend a timestamp, use the {@link #extend(GTTimestamp, URL)} method.
 * Provide the timestamp you want to extend and the URL of the extender service
 * you want to use.
 * <p>
 * To verify a timestamp, use the
 * {@link #verify(GTTimestamp, GTDataHash, URL, String, GTPublicationsFile)}
 * method. Provide the timestamp you want to verify, the hash value calculated
 * from the data the timestamp is supposed to be created from, and either
 * the publication string or the publications file.
 * <p>
 * Note that both {@link #create(GTDataHash, URL)} and
 * {@link #extend(GTTimestamp, URL)} methods also perform basic verification
 * of the newly acquired timestamps. To get more info on verification, see
 * {@link com.guardtime.tsp.GTTimestamp#verify(GTDataHash, String)},
 * {@link com.guardtime.tsp.GTTimestamp#verify(GTDataHash, GTPublicationsFile)}
 * and {@link com.guardtime.tsp.GTVerificationResult}.
 * <p>
 * To get more control on over the timestamping process, use methods of
 * {@link com.guardtime.tsp.GTTimestamp},
 * {@link com.guardtime.tsp.GTTimestampResponse} and
 * {@link com.guardtime.tsp.GTCertTokenResponse}. They provide tools for
 * creating timestamp and extension requests, decoding service responses and
 * extracting basic info from timestamps. Note that you will need to provide
 * transport tools in that case, as {@link com.guardtime.tsp.GTTimestamp}
 * does not have any built-in transport functionality.
 *
 * @see com.guardtime.transport.HttpStamper
 *
 * @since 0.4
 */
public class SimpleHttpStamper {
	/**
	 * Creates timestamp for this hash value using this stamping service URL.
	 *
	 * @param dataHash data hash to create timestamp for.
	 * @param stamperUrl stamping service URL.
	 *
	 * @return a newly created short-term (signed) timestamp.
	 *
	 * @throws GTException if timestamp creation fails.
	 * @throws IOException if transport error occurs.
	 * @throws IlelgalArgumentException if URL has invalid format.
	 *
	 * @see #create(GTDataHash, URL)
	 */
	public static GTTimestamp create(GTDataHash dataHash, String stamperUrl)
	throws GTException, IOException {
		try {
			return create(dataHash, new URL(stamperUrl));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid URL: " + stamperUrl);
		}
	}

	/**
	 * Creates timestamp for this hash value using this stamping service URL.
	 * <p>
	 * Exceptions thrown by this method usually indicate problems with the
	 * network connection to the stamper or the stamping service itself.
	 *
	 * @param dataHash data hash to create timestamp for.
	 * @param stamperUrl stamping service URL.
	 *
	 * @return a newly created short-term (signed) timestamp.
	 *
	 * @throws GTException if timestamp creation fails.
	 * @throws IOException if transport error occurs.
	 *
	 * @since 0.4
	 */
	public static GTTimestamp create(GTDataHash dataHash, URL stamperUrl)
	throws GTException, IOException {
		HttpStamper stamper = HttpStamper.getInstance();

		ResponseHandler handler = stamper.addTimestampRequest(dataHash, stamperUrl, 0);

		GTTimestampResponse response = HttpStamper.receiveTimestampResponse(handler, 0);

		int statusCode = response.getStatusCode();
		if (statusCode != 0 && statusCode != 1) {
			throw new GTException("service returned error " + response.getFailCode() + ": " + response.getFailMessage());
		}

		return response.getTimestamp();
	}

	/**
	 * Extends this timestamp using this extension service URL.
	 *
	 * @param timestamp timestamp to be extended.
	 * @param verifierUrl extension service URL.
	 *
	 * @return the newly created long-term (extended) timestamp.
	 *
	 * @throws GTException if timestamp extension fails.
	 * @throws IOException if transport error occurs.
	 * @throws IlelgalArgumentException if URL has invalid format.
	 *
	 * @see #extend(GTTimestamp, URL)
	 */
	public static GTTimestamp extend(GTTimestamp timestamp, String verifierUrl)
	throws GTException, IOException {
		try {
			return extend(timestamp, new URL(verifierUrl));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid URL: " + verifierUrl);
		}
	}

	/**
	 * Extends this timestamp using this extension service URL.
	 * <p>
	 * Exceptions thrown by this method usually indicate problems with the
	 * network connection to the extender or the extension service itself.
	 *
	 * @param timestamp timestamp to be extended.
	 * @param verifierUrl extension service URL.
	 *
	 * @return the newly created long-term (extended) timestamp.
	 *
	 * @throws GTException if timestamp extension fails.
	 * @throws IOException if transport error occurs.
	 *
	 * @since 0.4
	 */
	public static GTTimestamp extend(GTTimestamp timestamp, URL verifierUrl)
	throws GTException, IOException {
		HttpStamper stamper = HttpStamper.getInstance();

		ResponseHandler handler = stamper.addExtensionRequest(timestamp, verifierUrl, 0);

		GTCertTokenResponse response = HttpStamper.receiveExtensionResponse(handler, 0);

		int statusCode = response.getStatusCode();
		if (statusCode != 0 && statusCode != 1) {
			throw new GTException("service returned error " + response.getFailCode() + ": " + response.getFailMessage());
		}

		return timestamp.extend(response);
	}

	/**
	 * Downloads publications file from this URL.
	 *
	 * @param url publications file URL.
	 *
	 * @return publications file.
	 *
	 * @throws IlelgalArgumentException if URL has invalid format.
	 *
	 * @see #getPublicationsFile(URL)
	 */
	public static GTPublicationsFile getPublicationsFile(String url)
	throws IOException {
		try {
			return getPublicationsFile(new URL(url));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid URL: " + url);
		}
	}

	/**
	 * Downloads publications file from this URL.
	 * <p>
	 * Note that only basic syntax check of loaded publications file is
	 * performed. It is <b>strongly</b> recommended to verify publications file
	 * signature, too -- see {@link GTPublicationsFile#verifySignature()}.
	 *
	 * @param url publications file URL.
	 *
	 * @return publications file.
	 *
	 * @since 0.4
	 */
	public static GTPublicationsFile getPublicationsFile(URL url)
	throws IOException {
		HttpStamper stamper = HttpStamper.getInstance();

		ResponseHandler handler = stamper.addPublicationFileRequest(url, 0);

		InputStream in = HttpClient.getResponseContents(handler.receiveResponse(0));
		return GTPublicationsFile.getInstance(in);
	}

	/**
	 * Verifies this timestamp against this data hash, verification service
	 * at this URL, and either this publication or this publications file.
	 *
	 * @param timestamp timestamp to be verified.
	 * @param dataHash data hash to verify timestamp against.
	 * @param verifierUrl verification service URL. If this is {@code null},
	 * 			extending will not be attempted.
	 * @param publication control publication to verify timestamp against.
	 * 			Ignored for signed timestamps. If set for extended timestamps,
	 * 			publications file will be ignored.
	 * @param publicationsFile publications file to verify timestamp against.
	 * 			Should be set for signed timestamps or for extended timestamps
	 * 			if {@code publication} is not provided.
	 *
	 * @return timestamp verification result.
	 *
	 * @throws IlelgalArgumentException if URL has invalid format.
	 *
	 * @see #verify(GTTimestamp, GTDataHash, URL, String, GTPublicationsFile)
	 */
	public static HttpVerificationResult verify(GTTimestamp timestamp,
			GTDataHash dataHash, String verifierUrl,
			String publication, GTPublicationsFile publicationsFile) {
		try {
			URL url = ((verifierUrl == null) ? null : new URL(verifierUrl));
			return verify(timestamp, dataHash, url, publication, publicationsFile);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid URL: " + verifierUrl);
		}
	}

	/**
	 * Verifies this timestamp against this data hash, verification service
	 * at this URL, and either this publication or this publications file.
	 * <p>
	 * If timestamp is not extended, automatic extension will be attempted.
	 * If it is succeeded, this extended timestamp is verified.
	 * <p>
	 * If extension will fail due to 'time criteria', e.g. timestamp is too
	 * new or too old for this verifier, provided signed timestamp will be
	 * verified. If some other service error occurs, {@code GTException} will
	 * be thrown containing service error message.
	 * <p>
	 * To disable automatic extension attempts, set {@code verifierUrl} to
	 * {@code null}.
	 * <p>
	 * At least one of {@code publication} and {@code publicationsFile}
	 * should be set.
	 * <p>
	 * Note that verifying against publication will only work with extended
	 * timestamps. For signed ones, publications file should be provided.
	 * <p>
	 * To obtain publications file, use {@link #getPublicationsFile(URL)} or
	 * one of {@link GTPublicationsFile#getInstance} methods.
	 *
	 * @param timestamp timestamp to be verified.
	 * @param dataHash data hash to verify timestamp against.
	 * @param verifierUrl verification service URL. If this is {@code null},
	 * 			extending will not be attempted.
	 * @param publication control publication to verify timestamp against.
	 * 			Ignored for signed timestamps. If set for extended timestamps,
	 * 			publications file will be ignored.
	 * @param publicationsFile publications file to verify timestamp against.
	 * 			Should be set for signed timestamps or for extended timestamps
	 * 			if {@code publication} is not provided.
	 *
	 * @return timestamp verification result.
	 *
	 * @since 0.4
	 */
	public static HttpVerificationResult verify(GTTimestamp timestamp,
			GTDataHash dataHash, URL verifierUrl,
			String publication, GTPublicationsFile publicationsFile) {
		// Check arguments
		if (dataHash == null) {
			throw new IllegalArgumentException("invalid data hash: null");
		}
		if (publicationsFile == null && publication == null) {
			throw new IllegalArgumentException("invalid publications file: null");
		}

		// Check timestamp syntax: done already while constructing timestamp
		// Check publications file signature: done in GTTimestamp.verify()

		HttpVerificationResult result = new HttpVerificationResult();

		// Check if the timestamp could be extended
		boolean isExtendable = (!timestamp.isExtended() && verifierUrl != null);
		if (isExtendable && publicationsFile != null) {
			// Last publication time check is done to avoid unnecessary extension
			// requests that are guaranteed to fail if timestamp is too fresh.
			long registeredTime = timestamp.getRegisteredTime().getTime() / 1000;
			long lastPublicationTime = publicationsFile.getLastPublicationTime().getTime() / 1000;
			isExtendable = (registeredTime <= lastPublicationTime);
		}
		if (isExtendable) {
			result.updateStatus(HttpVerificationResult.EXTENSION_ATTEMPTED);
			// Receive extension response
			GTCertTokenResponse response = null;
			try {
				HttpStamper stamper = HttpStamper.getInstance();
				ResponseHandler handler = stamper.addExtensionRequest(timestamp, verifierUrl, 0);
				response = HttpStamper.receiveExtensionResponse(handler, 0);
			} catch (GTException e) {
				// If response has invalid format, quit verification.
				result.updateStatus(HttpVerificationResult.RESPONSE_FORMAT_FAILURE);
				return result;
			} catch (IOException e) {
				// If service cannot be reached, go on with signed timestamp
				// verification.
				result.updateStatus(HttpVerificationResult.SERVICE_UNREACHABLE_FAILURE);
			}

			// Set status code
			int statusCode = ((response == null) ? -1 : response.getStatusCode());
			if (statusCode == 0) {
				result.updateStatus(HttpVerificationResult.TIMESTAMP_GRANTED);
			} else if (statusCode == 1) {
				result.updateStatus(HttpVerificationResult.TIMESTAMP_GRANTED_WITH_MODS);
			} else if (statusCode == 2) {
				result.updateStatus(HttpVerificationResult.TIMESTAMP_REJECTED);
			} else if (statusCode == 3) {
				result.updateStatus(HttpVerificationResult.TIMESTAMP_WAITING);
			} else if (statusCode == 4) {
				result.updateStatus(HttpVerificationResult.REVOCATION_WARNING);
			} else if (statusCode == 5) {
				result.updateStatus(HttpVerificationResult.REVOCATION_NOTIFICATION);
			}

			// Check response status
			//
			// Last publication time defined in publications file may be
			// different from calendar data that verifier currently contains.
			// This may happen if calendar update process in this verifier
			// and publications file generation procedure are out of sync.
			// This situation is very unlikely to happen and last publication
			// time check against publications file will almost always be
			// sufficient, however, one cannot be 100% sure, so response status
			// check is also performed.
			if (statusCode == 0 || statusCode == 1) {
				// Extend timestamp
				try {
					timestamp.extend(response);
					result.updateStatus(HttpVerificationResult.TIMESTAMP_EXTENDED);
				} catch (GTException e) {
					// Received extended timestamp has invalid format
					GTVerificationResult gtResult = new GTVerificationResult();
					gtResult.updateErrors(GTVerificationResult.SYNTACTIC_CHECK_FAILURE);
					result.setGtResult(gtResult);
					return result;
				}

				// Verify extended timestamp
				GTVerificationResult gtResult = timestamp.verify(dataHash, publicationsFile);
				result.setGtResult(gtResult);
			} else if (statusCode > 1) {
				// Set fail code
				int failCode = response.getFailCode();
				if (failCode == 0) {
					result.updateErrors(HttpVerificationResult.INVALID_ALGORITHM_FAILURE);
				} else if (failCode == 2) {
					result.updateErrors(HttpVerificationResult.INVALID_REQUEST_FAILURE);
				} else if (failCode == 5) {
					result.updateErrors(HttpVerificationResult.INVALID_DATA_FORMAT_FAILURE);
				} else if (failCode == 14) {
					result.updateErrors(HttpVerificationResult.TIME_NOT_AVAILBLE_FAILURE);
				} else if (failCode == 15) {
					result.updateErrors(HttpVerificationResult.UNACCEPTED_POLICY_FAILURE);
				} else if (failCode == 16) {
					result.updateErrors(HttpVerificationResult.UNACCEPTED_EXTENSION_FAILURE);
				} else if (failCode == 17) {
					result.updateErrors(HttpVerificationResult.ADDITIONAL_INFO_NOT_AVAILABLE_FAILURE);
				} else if (failCode == 25) {
					result.updateErrors(HttpVerificationResult.SYSTEM_FAILURE);
				} else if (failCode == 100) {
					// Timestamp too new: publication not released yet.
					result.updateErrors(HttpVerificationResult.TIMESTAMP_TOO_NEW_FAILURE);
				} else if (failCode == 101) {
					// Timestamp too old: verifier history starts after this
					// timestamp was created; probably wrong verifier used.
					result.updateErrors(HttpVerificationResult.TIMESTAMP_TOO_OLD_FAILURE);
				}

				// If extension failure is either '100: timestamp too new' or
				// '101: timestamp too old', verification will go on with signed
				// timestamp. On any other failure, verification is considered
				// failed as well.
				if (failCode != 100 && failCode != 101) {
					return result;
				}
			}
		}

		GTVerificationResult gtResult = timestamp.verify(dataHash, publicationsFile);
		result.setGtResult(gtResult);

		return result;
	}
}
