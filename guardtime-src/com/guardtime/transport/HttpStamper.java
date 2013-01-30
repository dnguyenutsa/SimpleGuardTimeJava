/*
 * $Id: HttpStamper.java 208 2011-04-25 11:01:08Z ahto.truu $
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
import java.net.URL;

import com.guardtime.tsp.GTCertTokenResponse;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTTimestampResponse;



/**
 * A collection of static methods for transport operations with timestamps.
 * <p>
 * This class gives you more control over transport routines, splitting request
 * queuing and response handling between different methods.
 * <p>
 * For example, to create a timestamp, you could use something like
 * <code>
 *     HttpStamper stamper = HttpStamper.getInstance();
 *     ResponseHandler handler = stamper.addTimestampRequest(dataHash, stamperUrl, 0);
 *     GTTimestampResponse response = HttpStamper.receiveTimestampResponse(handler, 0);
 *     int statusCode = response.getStatusCode();
 *     if (statusCode != 0 && statusCode != 1) {
 *         throw new GTException("service returned error " + response.getFailCode());
 *     }
 *     GTTimestamp timestamp = response.getTimestamp();
 * </code>
 *
 * {@code }
 *
 * @see com.guardtime.transport.SimpleHttpStamper
 *
 * @since 0.4
 */
public class HttpStamper {
	private static HttpStamper INSTANCE = null;
	private HttpClient httpClient;



	/**
	 * Returns the singleton instance of stamper.
	 * <p>
	 * Stamper is created with transport client launched already, so you can
	 * start using it right away.
	 *
	 * @return instance of stamper object.
	 *
	 * @throws IOException if transport client cannot be started.
	 */
	public static synchronized HttpStamper getInstance()
	throws IOException
	{
		if (INSTANCE == null) {
			INSTANCE = new HttpStamper();
		}
		return INSTANCE;
	}



	/**
	 * Extracts timestamp response from this response handler.
	 * <p>
	 * This method will throw {@link java.net.SocketTimeoutException} if
	 * the transaction has timed out.
	 * <p>
	 * This method will return {@code null} if {@code timeout} passes and
	 * the transaction is still pending.
	 * <p>
	 * You should necessarily check response status by calling
	 * {@link com.guardtime.tsp.GTTimestampResponse#getStatusCode()} method.
	 * <p>
	 * You can then retrieve a timestamp by calling
	 * {@link com.guardtime.tsp.GTTimestampResponse#getTimestamp()} method.
	 *
	 * @param handler response handler, e.g. as returned by
	 * 			{@link #addTimestampRequest(GTDataHash, URL, long)}.
	 * @param timeout the time to wait for response, in milliseconds.
	 *
	 * @return timestamp response.
	 *
	 * @throws GTException if timestamp response has invalid format.
	 * @throws IOException if transport IO error occurs.
	 */
	public static GTTimestampResponse receiveTimestampResponse(ResponseHandler handler, long timeout)
	throws GTException, IOException {
		byte[] response = handler.receiveResponse(timeout);
		if (response == null) {
			return null;
		}
		return GTTimestampResponse.getInstance(HttpClient.getResponseContents(response));
	}

	/**
	 * Extracts timestamp extension response from this response handler.
 	 * <p>
	 * This method will throw {@link java.net.SocketTimeoutException} if
	 * the transaction has timed out.
	 * <p>
	 * This method will return {@code null} if {@code timeout} passes and
	 * the transaction is still pending.
	 * <p>
	 * You should necessarily check response status by calling
	 * {@link com.guardtime.tsp.GTCertTokenResponse#getStatusCode()} method.
	 * <p>
	 * You can then extend a timestamp by calling
	 * {@link com.guardtime.tsp.GTTimestamp#extend(GTCertTokenResponse)} method.
	 *
	 * @param handler response handler, e.g. as returned by
	 * 			{@link #addExtensionRequest(GTTimestamp, URL, long)}.
	 * @param timeout the time to wait for response, in milliseconds.
	 *
	 * @return timestamp extension response.
	 *
	 * @throws GTException if timestamp extension response has invalid format.
	 * @throws IOException if transport IO error occurs.
	 */
	public static GTCertTokenResponse receiveExtensionResponse(ResponseHandler handler, long timeout)
	throws GTException, IOException {
		byte[] response = handler.receiveResponse(timeout);
		if (response == null) {
			return null;
		}
		return GTCertTokenResponse.getInstance(HttpClient.getResponseContents(response));
	}



	/**
	 * Adds timestamp request to the queue.
	 * <p>
	 * This request will be processed as soon as possible, and this response
	 * handler will receive the response.
	 * <p>
	 * Actual response can then be retrieved by calling
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * method.
	 * <p>
	 * Note that this method will not handle network timeout directly. Instead,
	 * exception will be thrown by
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * or {@link #receiveTimestampResponse(ResponseHandler, long)} that uses it.
	 *
	 * @param dataHash data hash to retrieve timestamp for.
	 * @param stamperUrl stamping service URL.
	 * @param timeout stamper service transaction timeout.
	 *
	 * @return response handler.
	 *
	 * @throws IOException if transport IO error occurs.
	 */
	public ResponseHandler addTimestampRequest(GTDataHash dataHash, URL stamperUrl, long timeout)
	throws IOException {
		byte[] requestBytes = GTTimestamp.composeRequest(dataHash);
		return httpClient.addHttpRequest(stamperUrl, requestBytes, timeout);
	}

	/**
	 * Adds timestamp extension request to the queue.
	 * <p>
	 * This request will be processed as soon as possible, and this response
	 * handler will receive the response.
	 * <p>
	 * Actual response can then be retrieved by calling
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * method.
	 * <p>
	 * Note that this method will not handle network timeout directly. Instead,
	 * exception will be thrown by
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * or {@link #receiveTimestampResponse(ResponseHandler, long)} that uses it.
	 *
	 * @param timestamp timestamp to extend.
	 * @param verifierUrl extension service URL.
	 * @param timeout extension service transaction timeout.
	 *
	 * @return reponse handler.
	 *
	 * @throws IOException if transport IO error occurs.
	 */
	public ResponseHandler addExtensionRequest(GTTimestamp timestamp, URL verifierUrl, long timeout)
	throws IOException {
		byte[] requestBytes = timestamp.composeExtensionRequest();
		return httpClient.addHttpRequest(verifierUrl, requestBytes, timeout);
	}

	/**
	 * Adds publication file download request to the queue.
	 * <p>
	 * This request will be processed as soon as possible, and this response
	 * handler will receive the response.
	 * <p>
	 * Actual response can then be retrieved by calling
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * method.
	 * <p>
	 * Note that this method will not handle network timeout directly. Instead,
	 * exception will be thrown by
	 * {@link com.guardtime.transport.ResponseHandler#receiveResponse(long)}
	 * or {@link #receiveTimestampResponse(ResponseHandler, long)} that uses it.
	 *
	 * @param publicationFileUrl publication file URL.
	 * @param timeout network transaction timeout.
	 *
	 * @return response handler.
	 *
	 * @throws IOException if transport IO error occurs.
	 */
	public ResponseHandler addPublicationFileRequest(URL publicationFileUrl, long timeout)
	throws IOException {
		return httpClient.addHttpRequest(publicationFileUrl, timeout);
	}



	/**
	 * Class constructor.
	 *
	 * @throws IOException
	 */
	private HttpStamper()
	throws IOException {
		httpClient = new HttpClient();
		httpClient.start();
	}
}
