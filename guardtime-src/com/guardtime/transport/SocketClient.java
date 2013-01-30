/*
 * $Id: SocketClient.java 219 2011-09-28 10:20:22Z ahto.truu $
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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



/**
 * Class to perform non-blocking data transfer via sockets.
 */
public class SocketClient
implements Runnable {
	// The selector we'll be monitoring
	private Selector selector;

	// Channel registration requests
	private Set pendingRequests;

	// Maps a SocketChannel to a ByteBuffer with data to be written
	private Map pendingData;

	// Maps a SocketChannel to a ResponseHandler for receiving data
	private Map responseHandlers;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer;

	/**
	 * Default constructor.
	 * <p>
	 * Fails if a selector cannot be created.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public SocketClient()
	throws IOException {
		selector = Selector.open();
		pendingRequests = new HashSet();
		pendingData = Collections.synchronizedMap(new HashMap());
		responseHandlers = Collections.synchronizedMap(new HashMap());
		readBuffer = ByteBuffer.allocate(8192);
	}

	/**
	 * Starts a worker thread for this socket client.
	 */
	public void start() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * The worker thread, basically an event loop.
	 */
	public void run() {
		while (true) {
			// Process any pending channel registration requests
			synchronized (pendingRequests) {
				Iterator requests = pendingRequests.iterator();
				while (requests.hasNext()) {
					Request request = (Request) requests.next();
					request.process();
				}
				pendingRequests.clear();
			}

			// Look for timeouts
			long next = 0;
			Iterator keys = selector.keys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				Timeout timeout = (Timeout) key.attachment();
				if (timeout.isTimedOut()) {
					SocketChannel channel = (SocketChannel) key.channel();
					try {
						int ops = key.interestOps();
						if (ops == SelectionKey.OP_CONNECT) {
							cleanup(channel, new SocketTimeoutException("Connect timeout"));
						} else if (ops == SelectionKey.OP_WRITE) {
							cleanup(channel, new SocketTimeoutException("Write timeout"));
						} else if (ops == SelectionKey.OP_READ) {
							cleanup(channel, new SocketTimeoutException("Read timeout"));
						} else {
							cleanup(channel, new SocketTimeoutException("Unknown timeout"));
						}
					} catch (CancelledKeyException x) {
						// Nothing here, already canceled
					}
				} else {
					long left = timeout.getRemaining();
					if (next > left && left > 0 || next == 0) {
						next = left;
					}
				}
			}

			// Wait for an event from one of the registered channels
			try {
				selector.select(next);
			} catch (IOException x) {
				// We don't have anyone to report this to...
				x.printStackTrace();
			}

			// Iterate over the available events
			keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				keys.remove();
				if (key.isValid()) {
					if (key.isConnectable()) {
						finishConnection(key);
					} else if (key.isWritable()) {
						write(key);
					} else if (key.isReadable()) {
						read(key);
					}
				}
			}
		}
	}

	/**
	 * Adds a request to be sent to the given address.
	 * The request will be processed asynchronously.
	 * Response is handled by the handler returned.
	 *
	 * @param address socket address.
	 * @param data data to send.
	 * @param timeout transaction timeout, in milliseconds.
	 *
	 * @return response handler.
	 *
	 * @throws IOException if transport error occurred.
	 */
	public ResponseHandler addRequest(InetSocketAddress address, byte[] data, long timeout)
	throws IOException {
		// Create a non-blocking socket channel and initiate connecting
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		try {
			channel.connect(address);
		} catch (UnsupportedAddressTypeException x) {
			// There's no IOException(Throwable) constructor in JDK1.5
			IOException xx = new IOException();
			xx.initCause(x);
			throw xx;
		} catch (UnresolvedAddressException x) {
			// There's no IOException(Throwable) constructor in JDK1.5
			IOException xx = new IOException();
			xx.initCause(x);
			throw xx;
		}

		// Register the response handler
		ResponseHandler handler = new ResponseHandler();
		responseHandlers.put(channel, handler);

		// Queue data to be written
		pendingData.put(channel, ByteBuffer.wrap(data));

		// Queue a request for channel registration
		// Can't register directly, as channel.register() would block here
		synchronized (pendingRequests) {
			Request request = new Request(channel, selector, timeout);
			pendingRequests.add(request);
		}

		// Wake up worker thread so it can process the registration
		selector.wakeup();

		return handler;
	}

	/**
	 * Finishes the connect operation defined by the given key.
	 * 
	 * @param key
	 *            key defining the connection.
	 */
	private void finishConnection(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();

		// Finish connecting
		try {
			channel.finishConnect();
		} catch (IOException x) {
			cleanup(channel, x);
			return;
		}

		// When connected, register for writing to this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	/**
	 * Writes previously queued data to the connection defined by the given key.
	 * 
	 * @param key
	 *            key defining the connection to write to.
	 */
	private void write(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) pendingData.get(channel);

		// Write data from the buffer
		try {
			channel.write(buffer);
		} catch (IOException x) {
			cleanup(channel, x);
			return;
		}

		// When all data written, register for reading from this channel
		if (buffer.remaining() == 0) {
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	/**
	 * Reads data from connection defined by the given key.
	 * 
	 * @param key
	 *            key defining the connection to read from.
	 */
	private void read(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		ResponseHandler handler = (ResponseHandler) responseHandlers.get(channel);

		// Reset read buffer for new data
		readBuffer.clear();

		// Read from the channel
		int numRead = -1;
		try {
			numRead = channel.read(readBuffer);
		} catch (IOException x) {
			cleanup(channel, x);
			return;
		}

		// Hand the data over to the client
		if (numRead > 0) {
			handler.append(readBuffer.array(), 0, numRead);
		} else {
			// Connection closed cleanly
			cleanup(channel, null);
		}
	}

	/**
	 * Cleans up all internal resources when we're done with a channel.
	 * 
	 * @param channel
	 *            the channel to close.
	 * @param error
	 *            the error to report back to clients; {@code null} if the
	 *            transaction was completed successfully.
	 */
	private void cleanup(SocketChannel channel, IOException error) {
		try {
			channel.close();
		} catch (IOException e) {
			// Do not overwrite the original error, if there was one
			if (error != null) {
				error = e;
			}
		}

		pendingData.remove(channel);
		ResponseHandler handler = (ResponseHandler) responseHandlers.remove(channel);

		if (error == null) {
			handler.setComplete();
		} else {
			handler.setError(error);
		}
	}



	/**
	 * Request for adding a Channel to a Selector.
	 */
	private class Request {
		private SocketChannel channel;
		private Selector selector;
		private long timeout;

		public Request(SocketChannel channel, Selector selector, long timeout) {
			this.channel = channel;
			this.selector = selector;
			this.timeout = timeout;
		}

		public void process() {
			try {
				channel.register(selector, SelectionKey.OP_CONNECT, new Timeout(timeout));
			} catch (IOException x) {
				cleanup(channel, x);
			}
		}
	}
}

/**
 * Timeout.
 */
class Timeout {
	private final boolean finite;
	private final long finish;

	public Timeout(long timeout) {
		this.finite = timeout > 0;
		this.finish = System.currentTimeMillis() + timeout;
	}

	public boolean isTimedOut() {
		return finite && finish <= System.currentTimeMillis();
	}

	public long getRemaining() {
		return finite ? Math.max(1, finish - System.currentTimeMillis()) : 0;
	}
}
