/*
 * $Id: CertTokenTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
package tests.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.CertToken;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link CertToken} tests.
 */
public class CertTokenTest
extends TestCase {
	private static final byte[] CERT_TOKEN = Base64.decode("MIID7QIBAQSCA6gBAAGzeg/wKO9iSMTUBUFi5U3GIVeYjJGos6qe/NCd8xOeKP8BAAF+ustyUHmz8H5nq4WF28UAdI8+DpKE1zU9MDUsfvjlmf8BAAHZamFd99ymBE3rcpr/OPInzmC3goU91VYolzipH2H1mv8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAAGmqewLynEEWK7aVDg9g4Mc6gifeevc7Wy7KWpbAN0eVv8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAAFrfh7evHzHCTBQs3srR/w379ohduOp5nFxALAS+tFvtP8BAAEsvsilgA4cZZnEYs/OcRPsvG9cUYd1ULrCRZjrPy3Ypf8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAAHYlop2h0tw9+N58BxwmjOq4eh5XBu79O/LFOeO++Xhf/8BAAF2BMOiOOu/yTzK5WhotPXgSrJobX53iSoV8OH6qTXjo/8BAAEZ//TulQpkcVA/dPyYUW++kmT5EyrR5+oG4YToJte4gf8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAAEZ5f3N0bL1cfTc3rZ9CBkT4vfp6vqgQLOCP2gc2QRyvv8BAAF8h8h2M6iKFoWSofXcMpMakjHd9ISLODFZcc0s01i85/8BAAEpXiUuAjCp9OumJIrOU7VR7nWSTyVq/EjHkcPqENlfD/8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP8BAAEbwklVouiqup+QSTLk6NR5QlLrFm3bYsLQmyLoLUlQPP8BAAGxdpDdDIhIx4KhN2s1nl+Zt7BpCDPgMjtWx/rN4p2Obf8BAAEWwtRfKz18xRT//2JDAxtMbCtQvgCZlEM6sYFZYgUVb/8BAAFnYsIWKfRmdhv8t7YUAH11BlrWK5/H83KkqwqfiMxf7f8BAAHIAXxHN6CCYHfJrdR22vg6/h9KFijaXsstplmirqHjaf8BAAEJkT97P28AQdiBdt6I7VF0YlR5P3uPQefCMWrpm4ZHDv8BAAG7RP02pfPN7ntcbfOmCYoJ41MzW2Ap8Ud1AliKfje+AP8wKQIES3SaAAQhAfJM7B2f4kS/iGDiSCOQe59GE58ITP+YDIDJsZFVdpnKMREEA2JhcgQDZm9vBAV4eXp6eQ==");
	private static final Integer VERSION = new Integer(1);
	private static final byte[] HISTORY = Base64.decode("AQABs3oP8CjvYkjE1AVBYuVNxiFXmIyRqLOqnvzQnfMTnij/AQABfrrLclB5s/B+Z6uFhdvFAHSPPg6ShNc1PTA1LH745Zn/AQAB2WphXffcpgRN63Ka/zjyJ85gt4KFPdVWKJc4qR9h9Zr/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABpqnsC8pxBFiu2lQ4PYODHOoIn3nr3O1suylqWwDdHlb/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABa34e3rx8xwkwULN7K0f8N+/aIXbjqeZxcQCwEvrRb7T/AQABLL7IpYAOHGWZxGLPznET7LxvXFGHdVC6wkWY6z8t2KX/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQAB2JaKdodLcPfjefAccJozquHoeVwbu/TvyxTnjvvl4X//AQABdgTDojjrv8k8yuVoaLT14EqyaG1+d4kqFfDh+qk146P/AQABGf/07pUKZHFQP3T8mFFvvpJk+RMq0efqBuGE6CbXuIH/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABGeX9zdGy9XH03N62fQgZE+L36er6oECzgj9oHNkEcr7/AQABfIfIdjOoihaFkqH13DKTGpIx3fSEizgxWXHNLNNYvOf/AQABKV4lLgIwqfTrpiSKzlO1Ue51kk8lavxIx5HD6hDZXw//AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABG8JJVaLoqrqfkEky5OjUeUJS6xZt22LC0Jsi6C1JUDz/AQABsXaQ3QyISMeCoTdrNZ5fmbewaQgz4DI7Vsf6zeKdjm3/AQABFsLUXys9fMUU//9iQwMbTGwrUL4AmZRDOrGBWWIFFW//AQABZ2LCFin0ZnYb/Le2FAB9dQZa1iufx/NypKsKn4jMX+3/AQAByAF8RzeggmB3ya3Udtr4Ov4fShYo2l7LLaZZoq6h42n/AQABCZE/ez9vAEHYgXbeiO1RdGJUeT97j0HnwjFq6ZuGRw7/AQABu0T9NqXzze57XG3zpgmKCeNTM1tgKfFHdQJYin43vgD/");
	private static final byte[] PUBLISHED_DATA = Base64.decode("MCkCBEt0mgAEIQHyTOwdn+JEv4hg4kgjkHufRhOfCEz/mAyAybGRVXaZyg==");
	private static final byte[][] PUB_REFERENCES = { "bar".getBytes(), "foo".getBytes(), "xyzzy".getBytes() }; // Strings here should be sorted alphabetically!



	/**
	 * Tests {@link CertToken#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			CertToken.getInstance(null);
			fail("null accepted as cert token bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(CERT_TOKEN);
			in.skip(1);
			CertToken.getInstance(in);
			fail("rubbish accepted as cert token bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build signature info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(CERT_TOKEN);
		CertToken.getInstance(in);

		// Build signature info using valid components
		in = getDerStream(VERSION, HISTORY, PUBLISHED_DATA, PUB_REFERENCES);
		CertToken.getInstance(in);
	}

	/**
	 * Tests {@link CertToken#getInstance(InputStream)} method with various
	 * version numbers.
	 */
	public void testInitVersion()
	throws IOException {
		// Make sure invalid version numbers are NOT accepted
		Integer[] invalidVersions = { null, Integer.valueOf("0"), Integer.valueOf("2") };
		for (int i = 0; i < invalidVersions.length; i++) {
			try {
				InputStream in = getDerStream(invalidVersions[i], HISTORY, PUBLISHED_DATA, PUB_REFERENCES);
				CertToken.getInstance(in);
				fail(invalidVersions[i] + " accepted as version");
			} catch (Asn1FormatException e) {
				Log.debug("[DBG] (OK) " + e.getMessage());
			}
		}
	}

	/**
	 * Tests {@link CertToken#getInstance(InputStream)} method with various
	 * history values.
	 */
	public void testInitHistory()
	throws IOException {
		// Make sure empty history is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, null, PUBLISHED_DATA, PUB_REFERENCES);
			CertToken.getInstance(in);
			fail("null accepted as history value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link CertToken#getInstance(InputStream)} method with various
	 * published data values.
	 */
	public void testInitPublishedData()
	throws IOException {
		// Make sure empty published data is NOT accepted
		try {
			InputStream in = getDerStream(VERSION, HISTORY, null, PUB_REFERENCES);
			CertToken.getInstance(in);
			fail("null accepted as published data");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link CertToken#getInstance(InputStream)} method with various
	 * publication references.
	 */
	public void testInitPubReferences()
	throws IOException {
		// Make sure empty publication references are NOT accepted
		try {
			InputStream in = getDerStream(VERSION, HISTORY, PUBLISHED_DATA, null);
			CertToken.getInstance(in);
			fail("null accepted as publication references");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link CertToken#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(CERT_TOKEN);
		CertToken certToken = CertToken.getInstance(in);
		assertTrue(Arrays.equals(CERT_TOKEN, certToken.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(CERT_TOKEN);
		CertToken certToken = CertToken.getInstance(in);

		assertEquals(VERSION.intValue(), certToken.getVersion());
		assertTrue(Arrays.equals(HISTORY, certToken.getHistory()));
		assertTrue(Arrays.equals(PUBLISHED_DATA, certToken.getPublishedData().getDerEncoded()));

		List list = certToken.getPubReferences();
		assertEquals(PUB_REFERENCES.length, list.size());
		for (int i = 0; i < PUB_REFERENCES.length; i++) {
			assertTrue(Arrays.equals(PUB_REFERENCES[i], (byte[]) list.get(i)));
		}

		assertNull(certToken.getEncodedExtensions());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		InputStream in = getDerStream(VERSION, HISTORY, PUBLISHED_DATA, PUB_REFERENCES);
		CertToken certToken = CertToken.getInstance(in);

		assertFalse(certToken.getDerEncoded() == certToken.getDerEncoded());

		assertFalse(certToken.getHistory() == certToken.getHistory());

		try {
			certToken.getPubReferences().clear();
			fail("Modifiable list returned as publication references");
		} catch (UnsupportedOperationException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// TODO: check disabled as GuardTime is not currently using this field.
		//try {
		//	certToken.getExtensions().clear();
		//	fail("Modifiable extension list returned");
		//} catch (UnsupportedOperationException e) {
		//	Log.debug("[DBG] (OK) " + e.getMessage());
		//}
	}

	/**
	 * Produces input stream containing ASN.1 representation of signature info.
	 */
	private InputStream getDerStream(Integer version, byte[] history, byte[] publishedData, byte[][] pubReferences)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (version != null) {
			v.add(new ASN1Integer(version.intValue()));
		}

		if (history != null) {
			v.add(new DEROctetString(history));
		}

		if (publishedData != null) {
			v.add(new ASN1InputStream(publishedData).readObject());
		}

		if (pubReferences != null) {
			DEROctetString[] derRefs = new DEROctetString[pubReferences.length];
			for (int i = 0; i < pubReferences.length; i++) {
				derRefs[i] = new DEROctetString(pubReferences[i]);
			}
			v.add(new DERSet(derRefs));
		}

		// Extensions skipped -- see CertToken code for comments

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}
}
