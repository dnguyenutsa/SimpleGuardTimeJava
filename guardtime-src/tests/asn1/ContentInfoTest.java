/*
 * $Id: ContentInfoTest.java 268 2012-08-27 18:31:08Z ahto.truu $
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

import junit.framework.TestCase;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import com.guardtime.asn1.Asn1FormatException;
import com.guardtime.asn1.CertToken;
import com.guardtime.asn1.ContentInfo;
import com.guardtime.util.Base64;
import com.guardtime.util.Log;



/**
 * {@link ContentInfo} tests.
 */
public class ContentInfoTest
extends TestCase {
	private static final byte[] CONTENT_INFO = Base64.decode("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");
	private static final String CONTENT_TYPE = "1.2.840.113549.1.7.2";
	private static final byte[] SIGNED_DATA = Base64.decode("MIIONAIBAzELMAkGBSskAwIBBQAwfgYLKoZIhvcNAQkQAQSgbwRtMGsCAQEGCysGAQQBgdlcAgEBMDEwDQYJYIZIAWUDBAIBBQAEIAAZap90D/GUJFCQZXCqmIwwHist0XoohhX3UitkvZrwAhBLc7pfAAIAAQADAAAAAAglGA8yMDEwMDIxMTA4MDU1MVowAwIBAaCCAsQwggLAMIIBqAIBATANBgkqhkiG9w0BAQsFADAmMQ0wCwYDVQQDEwRUU0EyMRUwEwYDVQQKEwxHdWFyZFRpbWUgQVMwHhcNMDkwNDAzMTEwNTUzWhcNMTAwNTAzMTEwNTUzWjAmMQ0wCwYDVQQDEwRUU0EyMRUwEwYDVQQKEwxHdWFyZFRpbWUgQVMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCus2JidivZ9YSHM1QrwBurAK51gsB/WNJz/+zwZH3a0Wn5IKQvTx6rB6uULIy5TPeK8qczZPSt4r9wk8iWUqMorexUQakqXQhpv8kRH74j1ZlDKk6gVn6UN5Apj6B3dkg3gXoU8JHyoF9dTgLf60J6sRpVhG/5mPnZjPtzQNxlLinSZ5jFQe2RBo+WdGPn3ojm62r4UtaeFfeck4WnWHRP03zEO8ltkd+4QNyZ/E0xeR4vtBaB/4Dw3/RBqCOlNc0PVqUVCbll6v7uVX88W5GrRDI3rHzhcjik/nVbC2WRsRhu6JmTN8lfKpHkh2WDrFiH6Hq+7VZxxighKOEBq7CpAgMAii0wDQYJKoZIhvcNAQELBQADggEBAF8yxI4bR9JhzQ6HZIJD5ha0v+cvjNOL8xvTp8lZyGv2uB1pusn4J45mdJy9Rwf3tviHFvnZfstx30t4AItgqAyZENiPiC1wd4OZjx29ICdqgBt9I5i8tB89QITthJIJb38ZvsaTDbgw2SuJ2i3qxnXOcuqSVPLpSGCxiro2fxX428tPOLkGkOpMZGzlGTD17EBLfOQRnR+vHdmxqzvwu9gXHx0WudpX2FaAdwJL/MMoj/VizQcW0xBVanDthJ3LqNV/anqf2mUE1athe/C2PRzBrAN7O0EuGs7qTDaAs/r1U/4CrupTyA2AJj1rNAhJBfRjiaVVzPGZChbmKjbqogoxggrYMIIK1AIBATArMCYxDTALBgNVBAMTBFRTQTIxFTATBgNVBAoTDEd1YXJkVGltZSBBUwIBATAJBgUrJAMCAQUAoEEwGgYJKoZIhvcNAQkDMQ0GCyqGSIb3DQEJEAEEMCMGCSqGSIb3DQEJBDEWBBR0QI8IDFiMPNKwhSezN/BouPTDpjAOBgorBgEEAYHZXAQBBQAEggpCMIIKPgSCBUwCAAICAgICAgICAgICAgICAgICAgICAgICAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAMCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAQCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAUCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAYCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAcCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAgCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAkCAQIAAAAAAAAAAAAAAAAAAAAAAAAAAAoCAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABMBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABQBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABUBAAG8xgQ4DeBjkN2/y3dXyjKvhcVgO/y9ME1l5+FRCTzz5xYBAAF0DpPkRRSEikIEcewmZ7dmsvKzcZH5wmJnTe87cX2NrxcBAQHcIaqiVALli9vsxKRB2gd+lsyr7h5ibWqa9XxYA1okLRgBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABkBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABoBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB0BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAR4BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACcBAAHXlxNYS0BUgQwICnM6K39SSXpIxYtR79DNlwY0nmBTTygBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACkBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACoBAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACsBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC0BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC4BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC8BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBATMBAAGCBwRgX9EbEel7AanF5FGuFXZjoHxqJxbH4My5wIE9pzwBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD0BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD4BAAHlpRia05um4btFCZ+ES68Hq1Rc1WqCvZfEGY9dCOqnYT8BAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEABAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEEBAAE2YyKHjz3f9DKTwKSSWb5eLKTMaBE1uYBQ7tsvnIGo+EIBAQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEMBAAECy4HBTmkgo8oVg6HHOo2cE1bphcq8Ryvgeld35S+Nqf8EggOoAQABs3oP8CjvYkjE1AVBYuVNxiFXmIyRqLOqnvzQnfMTnij/AQABfrrLclB5s/B+Z6uFhdvFAHSPPg6ShNc1PTA1LH745Zn/AQAB2WphXffcpgRN63Ka/zjyJ85gt4KFPdVWKJc4qR9h9Zr/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABpqnsC8pxBFiu2lQ4PYODHOoIn3nr3O1suylqWwDdHlb/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABa34e3rx8xwkwULN7K0f8N+/aIXbjqeZxcQCwEvrRb7T/AQABLL7IpYAOHGWZxGLPznET7LxvXFGHdVC6wkWY6z8t2KX/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQAB2JaKdodLcPfjefAccJozquHoeVwbu/TvyxTnjvvl4X//AQABdgTDojjrv8k8yuVoaLT14EqyaG1+d4kqFfDh+qk146P/AQABGf/07pUKZHFQP3T8mFFvvpJk+RMq0efqBuGE6CbXuIH/AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABGeX9zdGy9XH03N62fQgZE+L36er6oECzgj9oHNkEcr7/AQABfIfIdjOoihaFkqH13DKTGpIx3fSEizgxWXHNLNNYvOf/AQABKV4lLgIwqfTrpiSKzlO1Ue51kk8lavxIx5HD6hDZXw//AQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/AQABG8JJVaLoqrqfkEky5OjUeUJS6xZt22LC0Jsi6C1JUDz/AQABsXaQ3QyISMeCoTdrNZ5fmbewaQgz4DI7Vsf6zeKdjm3/AQABFsLUXys9fMUU//9iQwMbTGwrUL4AmZRDOrGBWWIFFW//AQABZ2LCFin0ZnYb/Le2FAB9dQZa1iufx/NypKsKn4jMX+3/AQAByAF8RzeggmB3ya3Udtr4Ov4fShYo2l7LLaZZoq6h42n/AQABCZE/ez9vAEHYgXbeiO1RdGJUeT97j0HnwjFq6ZuGRw7/AQABu0T9NqXzze57XG3zpgmKCeNTM1tgKfFHdQJYin43vgD/MCkCBEt0mgAEIQHyTOwdn+JEv4hg4kgjkHufRhOfCEz/mAyAybGRVXaZyqCCARMwDQYJKoZIhvcNAQELBQAEggEAhDD0rpszY2x/tq5QPOWrvShk2Gc+Wj+RVfLlsrpyLGQVEaOmIVrXip1plaKEPULFAH12nvjCYOjMwzrwefo9iMBBHLJofdEeiv2RzJj5hGEQsfRb4GPbtCFbE7IIA9DK3O0zIK7BlID30HLdUaOd0QV/E5U7gWmPJPhw5CtMg5MxKXdJpUc9VJF76JE0sEBa9eRvM7x4RNp0TYYz3JF5SBjlYTUQ4Ns0Jdk2Tt0Xra/+ch94G4cjiIA0/ktfzcKyhKSB/k3wrapC33RWmjlPrE6S8S1hl8mxuQ7/Wow3amGtzQ8Ec/+mLfGzW2b6Qrv5v5ga1rRyXQAZ9JH+tZXe9Q==");
	private static final byte[] CERT_TOKEN = Base64.decode("MIIEJAIBAQSCA/ABAAGzeg/wKO9iSMTUBUFi5U3GIVeYjJGos6qe/NCd8xOeKP8BAAF+ustyUHmz8H5nq4WF28UAdI8+DpKE1zU9MDUsfvjlmf8BAAHZamFd99ymBE3rcpr/OPInzmC3goU91VYolzipH2H1mv8BAQHW8I0zwSfbOAsids9hq3l5WZhrma++r827KpUH0oocef8BAQGyrMDrjSgVPsFfVI6h3K31UacIoeQiaVJ8sttLKNIHLv8BAAGmqewLynEEWK7aVDg9g4Mc6gifeevc7Wy7KWpbAN0eVv8BAQHtzgd5yfNNvQ2gOW2w36si4f7qOYdPAm5FRILK8K9+8v8BAQGlMDD1jJB0FB7+kVyDsbmteJhennTdBw6DYB/L4rnYFf8BAAFrfh7evHzHCTBQs3srR/w379ohduOp5nFxALAS+tFvtP8BAAEsvsilgA4cZZnEYs/OcRPsvG9cUYd1ULrCRZjrPy3Ypf8BAQFb1Rs+iHx6osYoKemOByabzamFxiHtzTcFJkrAVJdOFP8BAAHYlop2h0tw9+N58BxwmjOq4eh5XBu79O/LFOeO++Xhf/8BAAF2BMOiOOu/yTzK5WhotPXgSrJobX53iSoV8OH6qTXjo/8BAAEZ//TulQpkcVA/dPyYUW++kmT5EyrR5+oG4YToJte4gf8BAQHPpLbA0mVwNR5Y1Il3MrtHPZck6wSnLxjR0kX27WLxRv8BAAEZ5f3N0bL1cfTc3rZ9CBkT4vfp6vqgQLOCP2gc2QRyvv8BAAF8h8h2M6iKFoWSofXcMpMakjHd9ISLODFZcc0s01i85/8BAAEpXiUuAjCp9OumJIrOU7VR7nWSTyVq/EjHkcPqENlfD/8BAQEtT/YU2Sy9I3absLfwpZ/8qypjdBuFM6STNNTBBlXddP8BAQHFDQRonUwwBgRsTNni0i0gg0e6qSpUTn4QfcIq9u9yLP8BAAEbwklVouiqup+QSTLk6NR5QlLrFm3bYsLQmyLoLUlQPP8BAAGxdpDdDIhIx4KhN2s1nl+Zt7BpCDPgMjtWx/rN4p2Obf8BAAEWwtRfKz18xRT//2JDAxtMbCtQvgCZlEM6sYFZYgUVb/8BAQFG4Unoql4EHmtdzGE83tXk485atwxdz9f8inxTraUjPf8BAAFnYsIWKfRmdhv8t7YUAH11BlrWK5/H83KkqwqfiMxf7f8BAAHIAXxHN6CCYHfJrdR22vg6/h9KFijaXsstplmirqHjaf8BAAEJkT97P28AQdiBdt6I7VF0YlR5P3uPQefCMWrpm4ZHDv8BAAG7RP02pfPN7ntcbfOmCYoJ41MzW2Ap8Ud1AliKfje+AP8wKQIES8ZXAAQhAasEtaQP1QROwyXlWDc58RGR8wc7heGfnyO+Iid4AfweMQA=");



	/**
	 * Tests {@link ContentInfo} constant field values.
	 */
	public void testConstants() {
		assertEquals(CONTENT_TYPE, ContentInfo.CONTENT_TYPE);
	}

	/**
	 * Tests {@link ContentInfo#getInstance(InputStream)} method.
	 */
	public void testInit()
	throws Asn1FormatException, IOException {
		// Make sure illegal arguments are handled correctly
		try {
			ContentInfo.getInstance(null);
			fail("null accepted as content info bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			InputStream in = new ByteArrayInputStream(CONTENT_INFO);
			in.skip(1);
			ContentInfo.getInstance(in);
			fail("rubbish accepted as content info bytes");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Build content info from pre-defined bytes
		InputStream in = new ByteArrayInputStream(CONTENT_INFO);
		ContentInfo.getInstance(in);

		// Build content info using valid components
		DERTaggedObject content = getDerTagged(true, 0, SIGNED_DATA);
		in = getDerStream(CONTENT_TYPE, content);
		ContentInfo.getInstance(in);
	}

	/**
	 * Tests {@link ContentInfo#getInstance(InputStream)} method with various
	 * content types.
	 */
	public void testInitContentType()
	throws IOException {
		DERTaggedObject content = getDerTagged(true, 0, SIGNED_DATA);

		// Make sure empty content type is NOT accepted
		try {
			InputStream in = getDerStream(null, content);
			ContentInfo.getInstance(in);
			fail("null accepted as content type");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure invalid content type is NOT accepted
		try {
			InputStream in = getDerStream("1.2.840.113549.1.7.3", content);
			ContentInfo.getInstance(in);
			fail("1.2.840.113549.1.7.3 accepted as content type");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link ContentInfo#getInstance(InputStream)} method with various
	 * content values.
	 */
	public void testInitContent()
	throws IOException {
		// Make sure empty content value is NOT accepted
		try {
			InputStream in = getDerStream(CONTENT_TYPE, null);
			ContentInfo.getInstance(in);
			fail("null accepted as content");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure invalid content value is NOT accepted
		try {
			// Using `contentInfo` instead of `signedData`
			DERTaggedObject content = getDerTagged(true, 0, CONTENT_INFO);
			InputStream in = getDerStream(CONTENT_TYPE, content);
			ContentInfo.getInstance(in);
			fail("rubbish accepted as content");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure implicitly tagged content is NOT accepted
		try {
			DERTaggedObject content = getDerTagged(false, 0, SIGNED_DATA);
			InputStream in = getDerStream(CONTENT_TYPE, content);
			ContentInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("implicitly tagged content accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Make sure content with invalid tag is NOT accepted
		try {
			DERTaggedObject content = getDerTagged(true, 1, SIGNED_DATA);
			InputStream in = getDerStream(CONTENT_TYPE, content);
			ContentInfo.getInstance(in);
			//TODO: re-enable check when we upgrade the BC libraries
			//http://www.bouncycastle.org/jira/browse/BJA-259
			//fail("content tagged with [1] accepted");
		} catch (Asn1FormatException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}

	/**
	 * Tests {@link ContentInfo#getDerEncoded()} method.
	 */
	public void testGetDerEncoded()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(CONTENT_INFO);
		ContentInfo contentInfo = ContentInfo.getInstance(in);
		assertTrue(Arrays.equals(CONTENT_INFO, contentInfo.getDerEncoded()));
	}

	/**
	 * Tests property getters.
	 */
	public void testGetParams()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(CONTENT_INFO);
		ContentInfo contentInfo = ContentInfo.getInstance(in);

		assertEquals(ContentInfo.CONTENT_TYPE, contentInfo.getContentType());

		ASN1Object signedData = new ASN1InputStream(contentInfo.getContent().getDerEncoded()).readObject();
		assertTrue(Arrays.equals(signedData.getEncoded(ASN1Encoding.DER), contentInfo.getContent().getDerEncoded()));
	}

	/**
	 * Tests {@link ContentInfo#isExtended()} and
	 * {@link ContentInfo#extend(CertToken)} methods.
	 */
	public void testExtend()
	throws Asn1FormatException, IOException {
		InputStream in = new ByteArrayInputStream(CONTENT_INFO);
		ContentInfo contentInfo = ContentInfo.getInstance(in);

		// Make sure created content info is NOT extended
		assertFalse(contentInfo.isExtended());

		// Make sure illegal arguments are handled correctly
		try {
			contentInfo.extend(null);
			fail("null accepted as cert token");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Create extended content info
		in = new ByteArrayInputStream(CERT_TOKEN);
		CertToken certToken = CertToken.getInstance(in);
		ContentInfo extendedContentInfo = contentInfo.extend(certToken);

		// Make sure created content info is extended;
		// also make sure initial content info is NOT extended
		assertFalse(contentInfo.isExtended());
		assertTrue(extendedContentInfo.isExtended());
	}

	/**
	 * Produces input stream containing ASN.1 representation of content info.
	 */
	private InputStream getDerStream(String contentType, DERTaggedObject content)
	throws IOException {
		ASN1EncodableVector v = new ASN1EncodableVector();

		if (contentType != null) {
			v.add(new ASN1ObjectIdentifier(contentType));
		}

		if (content != null) {
			v.add(content);
		}

		byte[] der = new DERSequence(v).getEncoded(ASN1Encoding.DER);

		return new ByteArrayInputStream(der);
	}

	/**
	 * Produces ASN.1 tagged object.
	 */
	private DERTaggedObject getDerTagged(boolean isExplicit, int tagNumber, byte[] data)
	throws IOException {
		ASN1Object signedData = new ASN1InputStream(data).readObject();
		return new DERTaggedObject(isExplicit, tagNumber, signedData);
	}
}
