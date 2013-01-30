/*
 * $Id: ResponseHandler.java 208 2011-04-25 11:01:08Z ahto.truu $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;



/**
 * Connection response handler.
 * <p>
 * Used to handle responses from the connections created by {@link SocketClient}
 * and {@link HttpClient}.
 */
public class ResponseHandler {
	// Buffer to collect the response data
	private ByteArrayOutputStream response;
	// Whether we have the complete response
	private boolean complete;
	// The I/O error, if any
	private IOException error;

	/**
	 * Default constructor.
	 */
	public ResponseHandler() {
		response = new ByteArrayOutputStream();
		complete = false;
		error = null;
	}

	/**
	 * Receive response from the handled transaction.
	 * 
	 * @param timeout
	 *            timeout, in milliseconds.
	 * 
	 * @return response bytes, or {@code null} if the timeout specified in this
	 *         method passed before the transaction was completed; in this case
	 *         the transaction remains pending and a later call to
	 *         {@code receiveResponse()} may succeed.
	 * 
	 * @throws SocketTimeoutException
	 *             if the transaction timeout (specified when the request was
	 *             initiated) passed before the transaction was completed; in
	 *             this case the transaction has been canceled and all later
	 *             calls to {@code receiveResponse()} will throw the same
	 *             exception again.
	 * @throws IOException
	 *             if there was an I/O error while performing the transaction;
	 *             in this case the transaction has been canceled and all later
	 *             calls to {@code receiveResponse()} will throw the same
	 *             exception again.
	 */
	public synchronized byte[] receiveResponse(long timeout)
	throws IOException {
		Timeout time = new Timeout(timeout);
		while (true) {
			if (complete) {
				return response.toByteArray();
			}
			if (time.isTimedOut()) {
				return null;
			}
			if (error != null) {
				throw error;
			}
			try {
				wait(time.getRemaining());
			} catch (InterruptedException e) {
				// Nothing here
			}
		}
	}

	/**
	 * Append data to response being received.
	 * 
	 * @param responseBytes
	 *            data to append.
	 */
	synchronized void append(byte[] responseBytes) {
		append(responseBytes, 0, responseBytes.length);
	}

	/**
	 * Append data to response being received.
	 * 
	 * @param responseBytes
	 *            data to append.
	 * @param offset
	 *            the start offset in the data.
	 * @param length
	 *            the number of bytes to append.
	 */
	synchronized void append(byte[] responseBytes, int offset, int length) {
		response.write(responseBytes, offset, length);
	}

	/**
	 * Signal that a complete response has been received.
	 * Wake up threads that are waiting on this response.
	 */
	synchronized void setComplete() {
		this.complete = true;
		notifyAll();
	}

	/**
	 * Signal that an error (including transaction timeout) has occurred.
	 * Wake up threads that are waiting on this response.
	 */
	synchronized void setError(IOException error) {
		this.error = error;
		notifyAll();
	}
}
