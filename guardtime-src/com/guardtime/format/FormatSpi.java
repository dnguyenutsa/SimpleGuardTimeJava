/*
 * $Id: FormatSpi.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package com.guardtime.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;



/**
 * This is the interface all format providers are expected to implement.
 * <p>
 * To facilitate processing of documents that are too big to fit into RAM, both
 * the documents and data should be presented as streams that could be consumed
 * and produced incrementally.
 * <p>
 * In addition to the methods specified in this interface, format providers are
 * encouraged to provide two public static methods:
 * <ul>
 * <li>{@code register()} to create an instance of the provider class and add
 * it as the provider for the appropriate document type(s) by calling
 * {@link Format#setProvider(String, FormatSpi)};
 * <li>{@code unregister()} to undo the effects of {@code register()}.
 * </ul>
 *
 * @since 0.4
 */
public interface FormatSpi {
	/**
	 * Extracts all the timestamps from the given document and returns them in a
	 * list ordered from the innermost (oldest) to the outermost (newest).
	 *
	 * @param in input stream containing document to extract timestamps from.
	 *
	 * @return a list of {@code byte[]} objects, each containing a timestamp.
	 * 			This list may be empty, but not {@code null}.
	 *
	 * @throws IOException if stream reading error occurs.
	 * @throws FormatException if data retrieved from {@code in} has invalid
	 * 			format.
	 */
	public List extractTimestamps(InputStream in)
	throws IOException, FormatException;

	/**
	 * Extracts from the given document the data (to be) protected by the indicated
	 * timestamp.
	 * <p>
	 * The intention for timestamp generation is to filter out any transient
	 * data that should not be timestamped and possibly normalize or
	 * canonicalize the rest.
	 * <p>
	 * The intention for timestamp verification is to retrieve the exact same
	 * sequence of bytes as for the timestamping (assuming the content of the
	 * document has not been modified since timestamping, of course).
	 * <p>
	 * The index of the timestamp for which the data is to be extracted is given
	 * by the parameter {@code pos}. The value must be at least 0 and at most
	 * equal to the length of the list returned by
	 * {@link #extractTimestamps(InputStream)} for the same document.
	 * <p>
	 * If the value of {@code pos} is strictly less than the length of the list,
	 * the intention is to retrieve the data covered by the timestamp in the
	 * given position in the list. That data should include the body of the
	 * document and all the preceding timestamps.
	 * <p>
	 * If the value of {@code pos} is equal to the length of the list, the
	 * intention is to retrieve the data for appending a new timestamp covering
	 * everything so far. That data should include the body of the document and
	 * all existing timestamps.
	 *
	 * @param in input stream containing document to extract data from.
	 * @param pos index of the timestamp that covers this data.
	 * @param out output stream to receive extracted data.
	 *
	 * @throws IOException if stream reading or writing error occurs.
	 * @throws FormatException if data retrieved from {@code in} has invalid
	 * 			format.
	 */
	public void extractData(InputStream in, int pos, OutputStream out)
	throws IOException, FormatException;

	/**
	 * Adds the given timestamp to the given document.
	 * <p>
	 * Insertion mode {@link InsertMode#Insert} indicates that client code
	 * believes it is adding the first timestamp to a document that is initially
	 * not timestamped. In this context it is considered an error if the
	 * document already contains a timestamp, and {@code IllegalStateException}
	 * should be thrown in this case.
	 * <p>
	 * Insertion mode {@link InsertMode#Replace} indicates the client code wants
	 * to replace an existing timestamp (most likely to replace an unextended
	 * timestamp with an extended one). There is no way to indicate which
	 * timestamp to replace (in case the document has several), because it only
	 * ever makes sense to replace the last one. In this context it is
	 * considered an error if the document does not contain any timestamps, and
	 * {@code IllegalStateException} should be thrown in this case.
	 * <p>
	 * Insertion mode {@link InsertMode#Append} indicates the client code wants
	 * to add a new timestamp (presumably covering the body of the document as
	 * well as all existing timestamps) to the document. If the document does
	 * not contain any existing timestamps, the behavior in this mode should be
	 * the same as if the mode {@code InsertMode.Insert} had been specified.
	 *
	 * @param in input stream containing document to add this timestamp to.
	 * @param ts timestamp to insert into this document.
	 * @param out output stream to receive the modified document.
	 * @param mode insertion mode.
	 *
	 * @throws IOException if stream reading or writing error occurs.
	 * @throws FormatException if data retrieved from {@code in} has invalid
	 * 			format.
	 * @throws IllegalStateException if insertion mode {@code InsertMode.Insert}
	 * 			was specified to update a document that is already timestamped,
	 * 			or if insertion mode {@code InsertMode.Replace} was specified to
	 * 			update a document that is not yet timestamped.
	 */
	public void insertTimestamp(InputStream in, byte[] ts, OutputStream out, InsertMode mode)
	throws IOException, FormatException, IllegalStateException;

	/**
	 * Removes a timestamp from the given document.
	 * <p>
	 * There is no way to indicate which timestamp to remove in case the
	 * document has several, because it only ever makes sense to remove the
	 * outermost (newest) one.
	 * <p>
	 * Removal of a timestamp from a document should be an exceptional operation
	 * only performed when there's a specific reason to do so. Therefore it is
	 * considered an error when removal is attempted on a document that does not
	 * have any timestamps, and {@code IllegalStateException} should be thrown
	 * in this case.
	 *
	 * @param in input stream containing document to remove timestamp from.
	 * @param out output stream to receive the modified document.
	 *
	 * @throws IOException if stream reading or writing error occurs.
	 * @throws FormatException if data retrieved from {@code in} has invalid
	 * 			format.
	 * @throws IllegalStateException if this document does not contain any
	 * 			timestamps.
	 */
	public void removeTimestamp(InputStream in, OutputStream out)
	throws IOException, FormatException, IllegalStateException;
}
