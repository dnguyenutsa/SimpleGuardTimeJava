/*
 * $Id: PublishedDataTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import java.math.BigInteger;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.PublishedData;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link PublishedData} tests.
 */
public class PublishedDataTest
extends TestCase {
	private static final byte[] PUBLISHED_DATA = Base64.decode("MCkCBEt0mgAEIQHyTOwdn+JEv4hg4kgjkHufRhOfCEz/mAyAybGRVXaZyg==");
	private static final BigInteger PUBLICATION_ID = BigInteger.valueOf(1265932800L);
	private static final byte[] PUBLICATION_IMPRINT = Base64.decode("AfJM7B2f4kS/iGDiSCOQe59GE58ITP+YDIDJsZFVdpnK");
	private static final String ENCODED_PUBLICATION = "AAAAAA-CLOSNA-AAPSJT-WB3H7C-IS7YQY-HCJARZ-A647IY-JZ6CCM-76MAZA-GJWGIV-K5UZZI-6KZ36X";



	/**
	 * Tests {@link PublishedData#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			PublishedData.getInstance(null);
			fail("null accepted as published data bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(PUBLISHED_DATA);
			in.skip(1);
			PublishedData.getInstance(in);
			fail("rubbish accepted as published data bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build published data from pre-defined bytes
		InputStream in = new ByteArrayInputStream(PUBLISHED_DATA);
		PublishedData.getInstance(in);

		// Build published data using valid components
		in = getDerStream(PUBLICATION_ID, PUBLICATION_IMPRINT);
		PublishedData.getInstance(in);
	}

	/**
	 * Tests {@link PublishedData#getInstance(InputStream)} method with various
	 * publication IDs.
	 */
	public void testInitPublicationId()
	throws IOException {
		// Make sure empty publication ID is NOT accepted
		try {
			InputStream in = getDerStream(null, PUBLICATION_IMPRINT);
			PublishedData.getInstance(in);
			fail("null accepted as publication ID");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link PublishedData#getInstance(InputStream)} method with various
	 * publication imprints.
	 */
	public void testInitPublicationImprint()
	throws IOException {
		// Make sure empty publication imprint is NOT accepted
		try {
			InputStream in = getDerStream(PUBLICATION_ID, null);
			PublishedData.getInstance(in);
			fail("null accepted as publication imprint");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link PublishedData#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(PUBLISHED_DATA);
		PublishedData publishedData = PublishedData.getInstance(in);
		assertTrue(Arrays.equals(PUBLISHED_DATA, publishedData.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(PUBLISHED_DATA);
		PublishedData publishedData = PublishedData.getInstance(in);

		assertEquals(PUBLICATION_ID.longValue(), publishedData.getPublicationId().longValue());
		assertTrue(Arrays.equals(PUBLICATION_IMPRINT, publishedData.getPublicationImprint()));

		assertEquals(ENCODED_PUBLICATION, publishedData.getEncodedPublication());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		InputStream in = getDerStream(PUBLICATION_ID, PUBLICATION_IMPRINT);
		PublishedData publishedData = PublishedData.getInstance(in);

		assertFalse(publishedData.getDerEncoded() == publishedData.getDerEncoded());

		assertFalse(publishedData.getPublicationImprint() == publishedData.getPublicationImprint());
	}

	/**
	 * Produces input stream containing ASN.1 representation of published data.
	 */
	private InputStream getDerStream(BigInteger publicationId, byte[] publicationImprint)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (publicationId != null) {
			v.add(new ASN1Integer(publicationId));
		}

		if (publicationImprint != null) {
			v.add(new DEROctetString(publicationImprint));
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}
}
