/*
 * $Id: Format.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Singleton that manages the list of providers available for integrating
 * timestamps into various document formats.
 * <p>
 * The formats are designated by their MIME types. Note that, per <a
 * target="_blank" href="http://www.ietf.org/rfc/rfc1341.txt">RFC 1341</a>, the
 * MIME type names are case-insensitive.
 *
 * @since 0.4
 */
public abstract class Format {
	/**
	 * The mapping from MIME types to corresponding providers.
	 */
	private static Map providers = new HashMap();



	/**
	 * Retrieves the provider for the given format.
	 *
	 * @param type MIME type for which a provider is requested.
	 * @return provider for the given format, or {@code null} if none is
	 * 			available.
	 */
	public static synchronized FormatSpi getProvider(String type) {
		if (type == null) {
			throw new IllegalArgumentException("invalid MIME type: null");
		}

		return (FormatSpi) providers.get(type.toLowerCase());
	}

	/**
	 * Registers a new provider for the given format.
	 *
	 * @param type MIME type for which the provider is to be registered.
	 * @param spi provider object. One object can be registered for several
	 * 			types simultaneously.
	 * @return previous provider registered for the given type, or {@code null}
	 * 			if there was none.
	 */
	public static synchronized FormatSpi setProvider(String type, FormatSpi spi) {
		if (type == null) {
			throw new IllegalArgumentException("invalid MIME type: null");
		} else if (spi == null) {
			throw new IllegalArgumentException("invalid provider: null");
		}

		return (FormatSpi) providers.put(type.toLowerCase(), spi);
	}

	/**
	 * Unregisters the current provider for the given format.
	 *
	 * @param type MIME type whose provider is to be removed.
	 * @return the removed provider, or {@code null} if there was none.
	 */
	public static synchronized FormatSpi removeProvider(String type) {
		if (type == null) {
			throw new IllegalArgumentException("invalid MIME type: null");
		}

		return (FormatSpi) providers.remove(type.toLowerCase());
	}

	/**
	 * Unregisters the given provider from all types for which it is currently
	 * registered.
	 *
	 * @param spi provider to remove.
	 * @return the number of registrations removed.
	 */
	public static synchronized int removeProvider(FormatSpi spi) {
		if (spi == null) {
			throw new IllegalArgumentException("invalid provider: null");
		}

		int count = 0;
		for (Iterator it = providers.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			if (spi == e.getValue()) {
				it.remove();
				++count;
			}
		}
		return count;
	}



	/*
	 * This is here just to clean up the public JavaDoc.
	 */
	private Format() {}
}
