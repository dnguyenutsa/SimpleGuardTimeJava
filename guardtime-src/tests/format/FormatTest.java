/*
 * $Id: FormatTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package tests.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import junit.framework.TestCase;

import com.guardtime.format.Format;
import com.guardtime.format.FormatException;
import com.guardtime.format.FormatSpi;
import com.guardtime.format.InsertMode;
import com.guardtime.util.Log;



/**
 * {@link Format} tests.
 */
public class FormatTest
extends TestCase {
	/**
	 * Tests {@link Format#getProvider(String)} method.
	 */
	public void testGetProvider() {
		// Make sure illegal arguments are handled correctly
		try {
			Format.getProvider(null);
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		DummyFormatProvider dfp = new DummyFormatProvider();

		// Register provider, make sure correct one is returned
		Format.setProvider(DummyFormatProvider.MIME_TYPE, dfp);
		assertEquals(dfp, Format.getProvider(DummyFormatProvider.MIME_TYPE));

		// Unregister provider
		Format.removeProvider(DummyFormatProvider.MIME_TYPE);

		// Make sure NULL is returned if provider is not registered
		assertNull(Format.getProvider(DummyFormatProvider.MIME_TYPE));
	}

	/**
	 * Tests {@link Format#setProvider(String, FormatSpi)} method.
	 */
	public void testSetProvider() {
		// Make sure illegal arguments are handled correctly
		try {
			Format.setProvider(null, new DummyFormatProvider());
			fail("null MIME type accepted");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			Format.setProvider(DummyFormatProvider.MIME_TYPE, null);
			fail("null provider accepted");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			Format.setProvider(null, null);
			fail("null MIME type and provider accepted");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		DummyFormatProvider dfp1 = new DummyFormatProvider();
		DummyFormatProvider dfp2 = new DummyFormatProvider();

		// Register first provider for this MIME type
		assertNull(Format.setProvider(DummyFormatProvider.MIME_TYPE, dfp1));
		assertEquals(dfp1, Format.getProvider(DummyFormatProvider.MIME_TYPE));

		// Register second provider, make sure previous one is removed
		assertEquals(dfp1, Format.setProvider(DummyFormatProvider.MIME_TYPE, dfp2));
		assertEquals(dfp2, Format.getProvider(DummyFormatProvider.MIME_TYPE));

		// Unregister provider
		Format.removeProvider(DummyFormatProvider.MIME_TYPE);
	}

	/**
	 * Tests {@link Format#removeProvider(FormatSpi)} and
	 * {@link Format#removeProvider(String)} methods.
	 */
	public void testRemoveProvider() {
		// Make sure illegal arguments are handled correctly
		try {
			Format.removeProvider((FormatSpi) null);
			fail("null provider accepted");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			Format.removeProvider((String) null);
			fail("null MIME type accepted");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		DummyFormatProvider dfp = new DummyFormatProvider();

		// Make sure nothing is removed if it wasn't previously registered
		assertNull(Format.getProvider(DummyFormatProvider.MIME_TYPE));
		assertNull(Format.removeProvider(DummyFormatProvider.MIME_TYPE));
		assertEquals(0, Format.removeProvider(dfp));

		// Register provider for two MIME types, make sure it gets removed
		Format.setProvider("image/foo", dfp);
		Format.setProvider("image/bar", dfp);

		assertEquals(dfp, Format.removeProvider("image/foo"));
		assertNull(Format.getProvider("image/foo"));
		assertEquals(dfp, Format.getProvider("image/bar"));

		assertEquals(dfp, Format.removeProvider("image/bar"));
		assertNull(Format.getProvider("image/foo"));
		assertNull(Format.getProvider("image/bar"));

		// Once again with other `removeProvider(FormatSpi)` method
		Format.setProvider("image/foo", dfp);
		Format.setProvider("image/bar", dfp);

		assertEquals(2, Format.removeProvider(dfp));
		assertNull(Format.getProvider("image/foo"));
		assertNull(Format.getProvider("image/bar"));
	}
}

// Also tests FormatSpi by implementing it (:
class DummyFormatProvider
implements FormatSpi {
	static final String MIME_TYPE = "type/format";

	public void extractData(InputStream in, int limit, OutputStream out) {}

	public List extractTimestamps(InputStream in)
	throws IOException, FormatException { return null; }

	public void insertTimestamp(InputStream in, byte[] ts, OutputStream out, InsertMode mode)
	throws IOException, FormatException {}

	public void removeTimestamp(InputStream in, OutputStream out)
	throws IOException, FormatException {}
}