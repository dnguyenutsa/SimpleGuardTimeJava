/*
 * $Id: TimeSignatureTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.TimeSignature;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link TimeSignature} tests.
 */
public class TimeSignatureTest
extends TestCase {
	private static final byte[] TIME_SIGNATURE = Base64.decode("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");
	private static final byte[] EXTENDED_TIME_SIGNATURE = Base64.decode("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");
	private static final byte[] LOCATION = Base64.decode("AgACAgICAgICAgICAgICAgICAgICAgICAgACAAAAAAAAAAAAAAAAAAAAAAAAAAADAgACAAAAAAAAAAAAAAAAAAAAAAAAAAAEAgACAAAAAAAAAAAAAAAAAAAAAAAAAAAFAgECAAAAAAAAAAAAAAAAAAAAAAAAAAAGAgECAAAAAAAAAAAAAAAAAAAAAAAAAAAHAgECAAAAAAAAAAAAAAAAAAAAAAAAAAAIAgECAAAAAAAAAAAAAAAAAAAAAAAAAAAJAgECAAAAAAAAAAAAAAAAAAAAAAAAAAAKAgACAAAAAAAAAAAAAAAAAAAAAAAAAAALAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAATAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAUAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVAQABvMYEOA3gY5Ddv8t3V8oyr4XFYDv8vTBNZefhUQk88+cWAQABdA6T5EUUhIpCBHHsJme3ZrLys3GR+cJiZ03vO3F9ja8XAQEB3CGqolQC5Yvb7MSkQdoHfpbMq+4eYm1qmvV8WANaJC0YAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAZAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAbAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAdAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEeAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAnAQAB15cTWEtAVIEMCApzOit/Ukl6SMWLUe/QzZcGNJ5gU08oAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAApAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAqAQABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAArAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAsAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAtAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAuAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAvAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEzAQABggcEYF/RGxHpewGpxeRRrhV2Y6B8aicWx+DMucCBPac8AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA9AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA+AQAB5aUYmtObpuG7RQmfhEuvB6tUXNVqgr2XxBmPXQjqp2E/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBAQABNmMih4893/Qyk8Ckklm+XiykzGgRNbmAUO7bL5yBqPhCAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABDAQABAsuBwU5pIKPKFYOhxzqNnBNW6YXKvEcr4HpXd+Uvjan/");
	private static final byte[] HISTORY = Base64.decode("AQABs3oP8CjvYkjE1AVBYuVNxiFXmIyRqLOqnvzQnfMTnij/AQABfrrLclB5s/B+Z6uFhdvFAHSPPg6ShNc1PTA1LH745Zn/AQAB2WphXffcpgRN63Ka/zjyJ85gt4KFPdVWKJc4qR9h9Zr/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABpqnsC8pxBFiu2lQ4PYODHOoIn3nr3O1suylqWwDdHlb/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABa34e3rx8xwkwULN7K0f8N+/aIXbjqeZxcQCwEvrRb7T/AQABLL7IpYAOHGWZxGLPznET7LxvXFGHdVC6wkWY6z8t2KX/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQAB2JaKdodLcPfjefAccJozquHoeVwbu/TvyxTnjvvl4X//AQABdgTDojjrv8k8yuVoaLT14EqyaG1+d4kqFfDh+qk146P/AQABGf/07pUKZHFQP3T8mFFvvpJk+RMq0efqBuGE6CbXuIH/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABGeX9zdGy9XH03N62fQgZE+L36er6oECzgj9oHNkEcr7/AQABfIfIdjOoihaFkqH13DKTGpIx3fSEizgxWXHNLNNYvOf/AQABKV4lLgIwqfTrpiSKzlO1Ue51kk8lavxIx5HD6hDZXw//AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABG8JJVaLoqrqfkEky5OjUeUJS6xZt22LC0Jsi6C1JUDz/AQABsXaQ3QyISMeCoTdrNZ5fmbewaQgz4DI7Vsf6zeKdjm3/AQABFsLUXys9fMUU//9iQwMbTGwrUL4AmZRDOrGBWWIFFW//AQABZ2LCFin0ZnYb/Le2FAB9dQZa1iufx/NypKsKn4jMX+3/AQAByAF8RzeggmB3ya3Udtr4Ov4fShYo2l7LLaZZoq6h42n/AQABCZE/ez9vAEHYgXbeiO1RdGJUeT97j0HnwjFq6ZuGRw7/AQABu0T9NqXzze57XG3zpgmKCeNTM1tgKfFHdQJYin43vgD/");
	private static final byte[] EXTENDED_HISTORY = Base64.decode("AQABs3oP8CjvYkjE1AVBYuVNxiFXmIyRqLOqnvzQnfMTnij/AQABfrrLclB5s/B+Z6uFhdvFAHSPPg6ShNc1PTA1LH745Zn/AQAB2WphXffcpgRN63Ka/zjyJ85gt4KFPdVWKJc4qR9h9Zr/AQEB1vCNM8En2zgLInbPYat5eVmYa5mvvq/NuyqVB9KKHHn/AQEBsqzA640oFT7BX1SOodyt9VGnCKHkImlSfLLbSyjSBy7/AQABpqnsC8pxBFiu2lQ4PYODHOoIn3nr3O1suylqWwDdHlb/AQEB7c4HecnzTb0NoDltsN+rIuH+6jmHTwJuRUSCyvCvfvL/AQEBpTAw9YyQdBQe/pFcg7G5rXiYXp503QcOg2Afy+K52BX/AQABa34e3rx8xwkwULN7K0f8N+/aIXbjqeZxcQCwEvrRb7T/AQABLL7IpYAOHGWZxGLPznET7LxvXFGHdVC6wkWY6z8t2KX/AQEBW9UbPoh8eqLGKCnpjgcmm82phcYh7c03BSZKwFSXThT/AQAB2JaKdodLcPfjefAccJozquHoeVwbu/TvyxTnjvvl4X//AQABdgTDojjrv8k8yuVoaLT14EqyaG1+d4kqFfDh+qk146P/AQABGf/07pUKZHFQP3T8mFFvvpJk+RMq0efqBuGE6CbXuIH/AQEBz6S2wNJlcDUeWNSJdzK7Rz2XJOsEpy8Y0dJF9u1i8Ub/AQABGeX9zdGy9XH03N62fQgZE+L36er6oECzgj9oHNkEcr7/AQABfIfIdjOoihaFkqH13DKTGpIx3fSEizgxWXHNLNNYvOf/AQABKV4lLgIwqfTrpiSKzlO1Ue51kk8lavxIx5HD6hDZXw//AQEBLU/2FNksvSN2m7C38KWf/KsqY3QbhTOkkzTUwQZV3XT/AQEBxQ0EaJ1MMAYEbEzZ4tItIINHuqkqVE5+EH3CKvbvciz/AQABG8JJVaLoqrqfkEky5OjUeUJS6xZt22LC0Jsi6C1JUDz/AQABsXaQ3QyISMeCoTdrNZ5fmbewaQgz4DI7Vsf6zeKdjm3/AQABFsLUXys9fMUU//9iQwMbTGwrUL4AmZRDOrGBWWIFFW//AQEBRuFJ6KpeBB5rXcxhPN7V5OPOWrcMXc/X/Ip8U62lIz3/AQABZ2LCFin0ZnYb/Le2FAB9dQZa1iufx/NypKsKn4jMX+3/AQAByAF8RzeggmB3ya3Udtr4Ov4fShYo2l7LLaZZoq6h42n/AQABCZE/ez9vAEHYgXbeiO1RdGJUeT97j0HnwjFq6ZuGRw7/AQABu0T9NqXzze57XG3zpgmKCeNTM1tgKfFHdQJYin43vgD/");
	private static final byte[] PUBLISHED_DATA = Base64.decode("MCkCBEt0mgAEIQHyTOwdn+JEv4hg4kgjkHufRhOfCEz/mAyAybGRVXaZyg==");
	private static final byte[] EXTENDED_PUBLISHED_DATA = Base64.decode("MCkCBEvGVwAEIQGrBLWkD9UETsMl5Vg3OfERkfMHO4Xhn58jviIneAH8Hg==");
	private static final byte[] PK_SIGNATURE = Base64.decode("MIIBEzANBgkqhkiG9w0BAQsFAASCAQCEMPSumzNjbH+2rlA85au9KGTYZz5aP5FV8uWyunIsZBURo6YhWteKnWmVooQ9QsUAfXae+MJg6MzDOvB5+j2IwEEcsmh90R6K/ZHMmPmEYRCx9FvgY9u0IVsTsggD0Mrc7TMgrsGUgPfQct1Ro53RBX8TlTuBaY8k+HDkK0yDkzEpd0mlRz1UkXvokTSwQFr15G8zvHhE2nRNhjPckXlIGOVhNRDg2zQl2TZO3Retr/5yH3gbhyOIgDT+S1/NwrKEpIH+TfCtqkLfdFaaOU+sTpLxLWGXybG5Dv9ajDdqYa3NDwRz/6Yt8bNbZvpCu/m/mBrWtHJdABn0kf61ld71");
	private static final byte[][] PUB_REFERENCES = { "bar".getBytes(), "foo".getBytes(), "xyzzy".getBytes() }; // Strings here should be sorted alphabetically!



	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			TimeSignature.getInstance(null);
			fail("null accepted as time signature bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(TIME_SIGNATURE);
			in.skip(1);
			TimeSignature.getInstance(in);
			fail("rubbish accepted as time signature bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build time signature from pre-defined bytes
		InputStream in = new ByteArrayInputStream(TIME_SIGNATURE);
		TimeSignature.getInstance(in);

		in = new ByteArrayInputStream(EXTENDED_TIME_SIGNATURE);
		TimeSignature.getInstance(in);

		// Build time signature using valid components
		in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, null, null);
		TimeSignature.getInstance(in);
	}

	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method with various
	 * location values.
	 */
	public void testInitLocation()
	throws IOException {
		// Make sure empty location is NOT accepted
		try {
			InputStream in = getDerStream(null, HISTORY, PUBLISHED_DATA, null, null);
			TimeSignature.getInstance(in);
			fail("null accepted as location value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method with various
	 * history values.
	 */
	public void testInitHistory()
	throws IOException {
		// Make sure empty history is NOT accepted
		try {
			InputStream in = getDerStream(LOCATION, null, PUBLISHED_DATA, null, null);
			TimeSignature.getInstance(in);
			fail("null accepted as history value");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method with various
	 * published data values.
	 */
	public void testInitPublishedData()
	throws IOException {
		// Make sure empty published data is NOT accepted
		try {
			InputStream in = getDerStream(LOCATION, HISTORY, null, null, null);
			TimeSignature.getInstance(in);
			fail("null accepted as published data");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method with various
	 * PK signature values.
	 */
	public void testInitPkSignature()
	throws Asn1FormatException, IOException {
		// Make sure empty PK signature is accepted
		InputStream in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, null, null);
		TimeSignature.getInstance(in);

		// Make sure explicitly tagged PK signature is NOT accepted
		try {
			DERTaggedObject pkSignature = getDerTagged(true, 0, PK_SIGNATURE);
			in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, pkSignature, null);
			TimeSignature.getInstance(in);
			fail("explicitly tagged PK signature accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure PK signature with invalid tag is NOT accepted
		try {
			DERTaggedObject pkSignature = getDerTagged(false, 2, PK_SIGNATURE);
			in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, pkSignature, null);
			TimeSignature.getInstance(in);
			fail("PK signature tagged with [2] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TimeSignature#getInstance(InputStream)} method with various
	 * publication references.
	 */
	public void testInitPubReferences()
	throws Asn1FormatException, IOException {
		// Make sure empty publication references are accepted
		InputStream in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, null, null);
		TimeSignature.getInstance(in);

		// Make sure explicitly tagged publication references are NOT accepted
		try {
			DERTaggedObject pubReferences = getDerTagged(true, 1, PUB_REFERENCES);
			in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, null, pubReferences);
			TimeSignature.getInstance(in);
			fail("explicitly tagged publication references accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure publication references with conflicting tag are NOT accepted
		try {
			DERTaggedObject pkSignature = getDerTagged(false, 0, PK_SIGNATURE);
			DERTaggedObject pubReferences = getDerTagged(false, 0, PUB_REFERENCES);
			in = getDerStream(LOCATION, HISTORY, PUBLISHED_DATA, pkSignature, pubReferences);
			TimeSignature.getInstance(in);
			fail("publication references tagged with [0] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link TimeSignature#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(TIME_SIGNATURE);
		TimeSignature signature = TimeSignature.getInstance(in);
		assertTrue(Arrays.equals(TIME_SIGNATURE, signature.getDerEncoded()));

		in = new ByteArrayInputStream(EXTENDED_TIME_SIGNATURE);
		signature = TimeSignature.getInstance(in);
		assertTrue(Arrays.equals(EXTENDED_TIME_SIGNATURE, signature.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(TIME_SIGNATURE);
		TimeSignature signature = TimeSignature.getInstance(in);

		assertTrue(Arrays.equals(LOCATION, signature.getLocation()));
		assertTrue(Arrays.equals(HISTORY, signature.getHistory()));
		assertTrue(Arrays.equals(PUBLISHED_DATA, signature.getPublishedData().getDerEncoded()));
		assertTrue(Arrays.equals(PK_SIGNATURE, signature.getPkSignature().getDerEncoded()));
		assertNull(signature.getPubReferences());

		DERTaggedObject pubReferences = getDerTagged(false, 1, PUB_REFERENCES);
		in = getDerStream(LOCATION, EXTENDED_HISTORY, EXTENDED_PUBLISHED_DATA, null, pubReferences);
		signature = TimeSignature.getInstance(in);

		assertTrue(Arrays.equals(LOCATION, signature.getLocation()));
		assertTrue(Arrays.equals(EXTENDED_HISTORY, signature.getHistory()));
		assertTrue(Arrays.equals(EXTENDED_PUBLISHED_DATA, signature.getPublishedData().getDerEncoded()));
		assertNull(signature.getPkSignature());

		List list = signature.getPubReferences();
		assertEquals(PUB_REFERENCES.length, list.size());
		for (int i = 0; i < PUB_REFERENCES.length; i++) {
			assertTrue(Arrays.equals(PUB_REFERENCES[i], (byte[]) list.get(i)));
		}
	}

	/**
	 * Tests {@link TimeSignature#isExtended()} method.
	 */
	public void testExtend()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(TIME_SIGNATURE);
		TimeSignature signature = TimeSignature.getInstance(in);
		assertFalse(signature.isExtended());

		in = new ByteArrayInputStream(EXTENDED_TIME_SIGNATURE);
		signature = TimeSignature.getInstance(in);
		assertTrue(signature.isExtended());
	}

	/**
	 * Tests if return values are immutable.
	 */
	public void testIsImmutable()
	throws Asn1FormatException, IOException {
		// Both pkSignature and pubReferences are present
		// This signature has no practical sense, however,
		// is fine to extract all possible fields.
		DERTaggedObject pkSignature = getDerTagged(false, 0, PK_SIGNATURE);
		DERTaggedObject pubReferences = getDerTagged(false, 1, PUB_REFERENCES);
		InputStream in = getDerStream(LOCATION, EXTENDED_HISTORY, EXTENDED_PUBLISHED_DATA, pkSignature, pubReferences);
		TimeSignature signature = TimeSignature.getInstance(in);

		assertFalse(signature.getDerEncoded() == signature.getDerEncoded());

		assertFalse(signature.getLocation() == signature.getLocation());

		assertFalse(signature.getHistory() == signature.getHistory());

		try {
			signature.getPubReferences().clear();
			fail("Modifiable list returned as publication references");
		} catch (UnsupportedOperationException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Produces input stream containing ASN.1 representation of time signature.
	 */
	private InputStream getDerStream(
			byte[] location, byte[] history, byte[] publishedData,
			DERTaggedObject pkSignature, DERTaggedObject pubReferences)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (location != null) {
			v.add(new DEROctetString(location));
		}

		if (history != null) {
			v.add(new DEROctetString(history));
		}

		if (publishedData != null) {
			v.add(new ASN1InputStream(publishedData).readObject());
		}

		if (pkSignature != null) {
			v.add(pkSignature);
		}

		if (pubReferences != null) {
			v.add(pubReferences);
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[] data)
	throws IOException {
		ASN1Object pkSignature = new ASN1InputStream(data).readObject();
		return new DERTaggedObject(isExplicit, tagNumber, pkSignature);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[][] pubReferences)
	throws IOException {
		DEROctetString[] derRefs = new DEROctetString[pubReferences.length];
		for (int i = 0; i < pubReferences.length; i++) {
			derRefs[i] = new DEROctetString(pubReferences[i]);
		}
		return new DERTaggedObject(isExplicit, tagNumber, new DERSet(derRefs));
	}
}
