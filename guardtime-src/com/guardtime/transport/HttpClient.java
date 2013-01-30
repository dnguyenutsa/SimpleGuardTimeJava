/*
 * $Id: HttpClient.java 218 2011-09-22 13:07:03Z jaan.oras $
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import com.guardtime.util.Base64;



/**
 * Class to perform non-blocking HTTP queries.
 * <p>
 * To start a client, use
 * <pre>
 * HttpClient client = new HttpClient();
 * client.start();
 * </pre>
 *
 * To handle responses, use {@link ResponseHandler} object.
 *
 * To send a GET request, use {@link #addHttpRequest(URL, long)}
 * method:
 * <pre>
 * ResponseHandler handler = client.addHttpRequest(new URL("http://www.guardtime.com/"), 0);
 * </pre>
 *
 * To send a POST request, use
 * {@link #addHttpRequest(URL, byte[], long)} method:
 * <pre>
 * ResponseHandler handler = client.addHttpRequest(new URL("http://www.guardtime.com/"), "hello".getBytes(), 0);
 * </pre>
 *
 * To receive a HTTP response, use {@link ResponseHandler#receiveResponse(long)}
 * method:
 * <pre>
 * byte[] reponse = handler.receiveResponse(0);
 * </pre>
 *
 * To extract response contents, if any, use
 * {@link #getResponseContents(byte[])} method:
 * <pre>
 * InputStream responseContents = HttpClient.getResponseContents(response);
 * </pre>
 */
public class HttpClient
extends SocketClient {
	private static final String PROTOCOL = "HTTP/1.0";
	private static final String NEWLINE = "\r\n";
	private static final byte[] SEPARATOR = (NEWLINE + NEWLINE).getBytes();

	private String proxyAuth = null;
	private String proxyHost = null;
	private int proxyPort;
	private boolean proxySet = false;



	/**
	 * Extract response contents from this HTTP response.
	 *
	 * @param response HTTP response.
	 *
	 * @return extracted response contents as input stream.
	 */
	public static InputStream getResponseContents(byte[] response)
	throws IOException {
		int pos = find(response, SEPARATOR);
		if (pos == -1) {
			throw new IOException("Malformed HTTP response: no end-of-headers terminator");
		}
		pos += SEPARATOR.length;
		return new ByteArrayInputStream(response, pos, response.length - pos);
	}

	/**
	 * Finds the position of the given pattern in the given buffer.
	 *
	 * This brute-force implementation is inefficient in general, but
	 * we're only using it for a very short pattern and in this case
	 * it is at least good enough, if not even better than the more
	 * advanced ones.
	 *
	 * @param buf buffer to search in.
	 * @param pat pattern to look for.
	 *
	 * @return position of the pattern, or -1 if not found.
	 */
	private static int find(final byte[] buf, final byte[] pat) {
		for (int i = 0; i <= buf.length - pat.length; ++i) {
			boolean found = true;
			for (int j = 0; j < pat.length; ++j) {
				if (buf[i + j] != pat[j]) {
					found = false;
					break;
				}
			}
			if (found) {
				return i;
			}
		}
		return -1;
	}



	/**
	 * Class constructor.
	 * <p>
	 * Instantiating HTTP client may fail if underlying {@link SocketClient}
	 * will fail to initialize.
	 *
	 * @throws IOException if HTTP client fails to initialize.
	 */
	public HttpClient()
	throws IOException {
		super();

		// get proxy configuration from system environment variable
		// and override/set the standard java system properties for proxy
		String webProxy = System.getenv("WEB_PROXY");
		if (webProxy != null) {
			StringTokenizer st = new StringTokenizer(webProxy, ":");
			if (st.countTokens() > 1) {
				System.getProperties().put("proxySet", "true");
				System.getProperties().put("proxyHost", st.nextToken());
				System.getProperties().put("proxyPort", st.nextToken());
			}
		}

		// get proxy authorization parameter from system variable
		// and create HTTP Basic authorization string
		String webProxyAuth = System.getenv("WEB_PROXY_AUTH");
		if (webProxyAuth != null) {
			StringTokenizer st = new StringTokenizer(webProxyAuth, ":");
			if (st.countTokens() > 1) {
				proxyAuth = "Basic " + Base64.encode(webProxyAuth.getBytes());
			}
		}

		// get the proxy configuration from java standard proxy properties
		// that were set by the -Dxxx or set/overridden by system environment
		// parameter "WEB_PROXY" above
		if ("true".equalsIgnoreCase(System.getProperty("proxySet"))) {
			proxyHost = System.getProperty("proxyHost");
			proxyPort = Integer.parseInt(System.getProperty("proxyPort"));
			proxySet = true;
		}
	}

	/**
	 * Sends a GET request to this URL. Response is handled the handler
	 * returned.
	 * 
	 * @param url
	 *            URL.
	 * @param timeout
	 *            transaction timeout, in milliseconds. The transaction will be
	 *            canceled and subsequent calls to
	 *            {@code ResponseHandler.receiveResponse()} will throw
	 *            {@code SocketTimeoutException} when the transaction in not
	 *            completed within the time limit given here.
	 * 
	 * @return response handler.
	 * 
	 * @throws IOException
	 *             if transport error occurred.
	 */
	public ResponseHandler addHttpRequest(URL url, long timeout)
	throws IOException {
		return addHttpRequest(url, null, timeout);
	}

	/**
	 * Sends this data to this URL using POST-request. Response is handled by
	 * the handler returned.
	 * 
	 * @param url
	 *            URL.
	 * @param data
	 *            data to be sent.
	 * @param timeout
	 *            transaction timeout, in milliseconds. The transaction will be
	 *            canceled and subsequent calls to
	 *            {@code ResponseHandler.receiveResponse()} will throw
	 *            {@code SocketTimeoutException} when the transaction in not
	 *            completed within the time limit given here.
	 * 
	 * @return response handler.
	 * 
	 * @throws IOException
	 *             if transport error occurred.
	 */
	public ResponseHandler addHttpRequest(URL url, byte[] data, long timeout)
	throws IOException {
		InetSocketAddress socketAddress;

		if (proxySet) {
			socketAddress = new InetSocketAddress(proxyHost, proxyPort);
		} else {
			int port = url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
			socketAddress = new InetSocketAddress(url.getHost(), port);
		}

		byte[] requestBytes = getRequest(url, data);
		return addRequest(socketAddress, requestBytes, timeout);
	}



	/**
	 * Builds a HTTP request to this URL for this data.
	 * <p>
	 * If data is provided, request type will be 'POST' and data will be sent.
	 * If data is {@code null}, request type will be 'GET'.
	 *
	 * @param url URL.
	 * @param data data to send; may be {@code null}.
	 *
	 * @return HTTP request ready to be sent by one of {@code sendHttpRequest}
	 * 			methods.
	 */
	private byte[] getRequest(URL url, byte[] data) {
								
		StringBuffer sb = new StringBuffer();
				
		// Since we need to support proxy requests, the HTTP RequestURI must be absoluteURI. 
		// But the absolute URI cannot contain userinfo so we need to modify the url parameter
		// The URL class has no methods for removing userinfo, we create new URL from components
		// of the original. 
		URL requestUrl = url;
		try {			
			requestUrl = new URL(url.getProtocol() ,url.getHost() , url.getPort() , url.getFile());
		} catch (MalformedURLException e) {
			// go with original url in this impossible case
		}		
		
		sb.append(data == null ? "GET " : "POST ").append(requestUrl.toString()).append(" ").append(PROTOCOL).append(NEWLINE);
		sb.append("Host: ").append(url.getHost()).append(NEWLINE);
		
		if (proxySet && proxyAuth != null) {
			sb.append("Proxy-Authorization: ").append(proxyAuth).append(NEWLINE);
		}
		
		// basic authentication
		String userInfo = url.getUserInfo();
		if (userInfo!=null && userInfo.contains(":"))
		{
			sb.append("Authorization: ").append("Basic " + Base64.encode(userInfo.getBytes())).append(NEWLINE);
		}
				
		byte[] requestBytes;
		if (data != null) {
			sb.append("Content-Length: ").append(data.length).append(NEWLINE);
			sb.append(NEWLINE);
			byte[] headerBytes = sb.toString().getBytes();
			requestBytes = new byte[headerBytes.length + data.length];
			System.arraycopy(headerBytes, 0, requestBytes, 0, headerBytes.length);
			System.arraycopy(data, 0, requestBytes, headerBytes.length, data.length);
		} else {
			sb.append(NEWLINE);
			requestBytes = sb.toString().getBytes();
		}

		return requestBytes;
	}
}
