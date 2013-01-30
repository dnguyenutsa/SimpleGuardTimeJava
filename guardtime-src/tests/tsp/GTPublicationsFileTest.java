/*
 * $Id: GTPublicationsFileTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package tests.tsp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;

import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;
import com.guardtime.tsp.GTVerificationResult;
import com.guardtime.util.Log;
import com.guardtime.util.Util;



public class GTPublicationsFileTest
extends TestCase {
	private static final GTPublicationsFile PUBLICATIONS_FILE = GTPublicationsFile.getInstance(Helper.getPublicationsFile());



	/**
	 * Tests {@code getInstance()} methods.
	 */
	public void testInit() {
		// Make sure illegal arguments are handled correctly
		try {
			GTPublicationsFile.getInstance((byte[]) null);
			fail("null accepted as publications file bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTPublicationsFile.getInstance((InputStream) null);
			fail("null accepted as publications file stream");
		} catch (IOException e) {
			fail("cannot create publications file: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		GTPublicationsFile publicationsFile = null;

		// Create publications file from byte array
		byte[] resp = Helper.getPublicationsFile();
		publicationsFile = GTPublicationsFile.getInstance(resp);
		assertTrue(Arrays.equals(resp, publicationsFile.getEncoded()));

		// Create publications file from byte array with invalid bounds
		try {
			byte[] modified = Util.copyOf(resp, 1, resp.length - 1);
			publicationsFile = GTPublicationsFile.getInstance(modified);
			fail("rubbish accepted as publications file bytes");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Create publications file from input stream
		try {
			InputStream in = new ByteArrayInputStream(resp);
			publicationsFile = GTPublicationsFile.getInstance(in);
			assertTrue(Arrays.equals(resp, publicationsFile.getEncoded()));
		} catch (IOException e) {
			fail("cannot create publications file: " + e.getMessage());
		}

		// Create publications file from invalid input stream
		try {
			InputStream in = new ByteArrayInputStream(resp);
			in.skip(1);
			publicationsFile = GTPublicationsFile.getInstance(in);
			fail("rubbish accepted as publications file stream");
		} catch (IOException e) {
			fail("cannot create publications file: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}
	}



	/**
	 * Tests {@code publicationId()} and {@code publicationTime()} methods.
	 */
	public void testPublicationId() {
		Date expectedTime, actualTime;
		long expectedId, actualId;
		String publication;

		// Make sure illegal arguments are handled correctly
		try {
			GTPublicationsFile.publicationId(null);
			fail("null accepted as publications ID");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		try {
			GTPublicationsFile.publicationTime(null);
			fail("null accepted as publication time");
		} catch (IllegalArgumentException e) {
			Log.debug("[DBG] (OK) " + e.getMessage());
		}

		// Check first publication
		expectedTime = PUBLICATIONS_FILE.getFirstPublicationTime();
		expectedId = expectedTime.getTime() / 1000;

		publication = PUBLICATIONS_FILE.getPublication(expectedId);

		actualId = GTPublicationsFile.publicationId(publication);
		assertEquals(expectedId, actualId);

		actualTime = GTPublicationsFile.publicationTime(publication);
		assertTrue(expectedTime.equals(actualTime));

		// Check last publication
		expectedTime = PUBLICATIONS_FILE.getLastPublicationTime();
		expectedId = expectedTime.getTime() / 1000;

		publication = PUBLICATIONS_FILE.getPublication(expectedId);

		actualId = GTPublicationsFile.publicationId(publication);
		assertEquals(expectedId, actualId);

		actualTime = GTPublicationsFile.publicationTime(publication);
		assertTrue(expectedTime.equals(actualTime));
	}



	/**
	 * Tests {@code getEncoded()} method.
	 */
	public void testGetEncoded() {
		byte[] resp = Helper.getPublicationsFile();
		GTPublicationsFile publicationsFile = GTPublicationsFile.getInstance(resp);
		assertTrue(Arrays.equals(resp, publicationsFile.getEncoded()));
	}

	/**
	 * Tests {@code contains()} methods.
	 */
	public void testContains() {
		List list = null;
		ListIterator iterator = null;

		// Check `contains(String publication)`
		list = PUBLICATIONS_FILE.getPublicationList();
		assertFalse(list.contains(null));

		iterator = list.listIterator();
		while (iterator.hasNext()) {
			assertTrue(PUBLICATIONS_FILE.contains((String) iterator.next()));
		}

		// Check `contains(PublicKey publicKey)`
		list = PUBLICATIONS_FILE.getPublicKeyList();
		assertFalse(list.contains(null));

		// TODO: positive tests
	}



	/**
	 * Tests {@code getFirstPublicationTime()} and
	 * {@code getlastPublicationTime()} methods.
	 */
	public void testGetFirstLastPublicationTime() {
		Date now = new Date();
		Date firstPublicationTime = PUBLICATIONS_FILE.getFirstPublicationTime();
		Date lastPublicationTime = PUBLICATIONS_FILE.getLastPublicationTime();

		assertTrue(firstPublicationTime.getTime() > 0);
		assertFalse(firstPublicationTime.after(lastPublicationTime));
		assertTrue(lastPublicationTime.before(now));
	}

	/**
	 * Tests {@code getPublication()} and {@code getPublicationList()} methods.
	 */
	public void testGetPublication() {
		List list = PUBLICATIONS_FILE.getPublicationList();
		assertFalse(list.isEmpty());

		ListIterator iterator = list.listIterator();
		while (iterator.hasNext()) {
			String publication = (String) iterator.next();
			assertEquals(publication, PUBLICATIONS_FILE.getPublication(GTPublicationsFile.publicationId(publication)));
			assertEquals(publication, PUBLICATIONS_FILE.getPublication(GTPublicationsFile.publicationTime(publication)));
		}
	}

	/**
	 * Tests {@code getPublicationCount()} method.
	 */
	public void testGetPublicationCount() {
		int expected = PUBLICATIONS_FILE.getPublicationList().size();
		int actual = PUBLICATIONS_FILE.getPublicationCount();

		assertTrue(actual > 0);
		assertEquals(expected, actual);
	}



	/**
	 * Tests {@code getPublicKeyList()} methods.
	 */
	public void testGetPublicKey() {
		// Get public key list
		List list = PUBLICATIONS_FILE.getPublicKeyList();
		assertFalse(list.isEmpty());

		// Extract public key from timestamp
		GTTimestamp timestamp = Helper.getSampleTimestamp();
		PublicKey publicKey = Helper.getCertificate(timestamp).getPublicKey();

		// Check if this public key fingerprint exists in public key list
		ListIterator iterator = list.listIterator();
		while (iterator.hasNext()) {
			GTDataHash pkFingerprint = (GTDataHash) iterator.next();
			GTHashAlgorithm hashAlg = (GTHashAlgorithm) pkFingerprint.getHashAlgorithm();
			GTDataHash myPkFingerprint = new GTDataHash(hashAlg).update(publicKey.getEncoded()).close();
			if (myPkFingerprint.equals(pkFingerprint)) {
				// Matching public key found -- return
				return;
			}
		}

		// Key not found -- this should not happen
		fail("no matching public key found in publications file");
	}

	/**
	 * Tests {@code getPublicationCount()} method.
	 */
	public void testGetPublicKeyCount() {
		int expected = PUBLICATIONS_FILE.getPublicKeyList().size();
		int actual = PUBLICATIONS_FILE.getPublicKeyCount();

		assertTrue(actual > 0);
		assertEquals(expected, actual);
	}



	/**
	 * Tests {@code verifySignature()} method.
	 */
	public void testVerifySignature() {
		byte[] resp = Helper.getPublicationsFile();
		GTPublicationsFile publicationsFile = GTPublicationsFile.getInstance(resp);

		GTVerificationResult result = publicationsFile.verifySignature();
		assertTrue(result.isValid());
		assertTrue(result.hasStatus(GTVerificationResult.PUBFILE_SIGNATURE_VERIFIED));

		// Get default key store path
		StringBuffer sb = new StringBuffer(System.getProperty("java.home"));
		sb.append(File.separator).append("lib");
		sb.append(File.separator).append("security");
		sb.append(File.separator).append("cacerts");
		String keyStorePath = sb.toString();

		result = publicationsFile.verifySignature(keyStorePath);
		assertTrue(result.isValid());
		assertTrue(result.hasStatus(GTVerificationResult.PUBFILE_SIGNATURE_VERIFIED));
	}
}
