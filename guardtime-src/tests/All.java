/*
 * $Id: All.java 169 2011-03-03 18:45:00Z ahto.truu $
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
package tests;

import com.guardtime.util.Log;
import com.guardtime.util.LogLevel;
import com.guardtime.util.LogPrinter;

import tests.asn1.*;
import tests.format.*;
import tests.transport.*;
import tests.tsp.*;
import tests.util.*;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * JUnit test suite to run all our JUnit tests.
 */
public abstract class All {

	/**
	 * Returns a {@code TestSuite} containing all our JUnit tests.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		// whenever you create new test cases, add them here

		// `com.guardtime.asn1.*`
		suite.addTestSuite(AccuracyTest.class);
		suite.addTestSuite(CertTokenRequestTest.class);
		suite.addTestSuite(CertTokenTest.class);
		suite.addTestSuite(ContentInfoTest.class);
		suite.addTestSuite(MessageImprintTest.class);
		suite.addTestSuite(PublishedDataTest.class);
		suite.addTestSuite(SignedDataTest.class);
		suite.addTestSuite(SignerInfoTest.class);
		suite.addTestSuite(SignatureInfoTest.class);
		suite.addTestSuite(TimeSignatureTest.class);
		suite.addTestSuite(TimestampRequestTest.class);
		suite.addTestSuite(TstInfoTest.class);

		// `com.guardtime.tsp.*`
		suite.addTestSuite(GTHashAlgorithmTest.class);
		suite.addTestSuite(GTDataHashTest.class);
		suite.addTestSuite(GTTimestampTest.class);
		suite.addTestSuite(GTTimestampResponseTest.class);
		suite.addTestSuite(GTCertTokenResponseTest.class);
		suite.addTestSuite(GTPublicationsFileTest.class);

		// `com.guardtime.util.*`
		suite.addTestSuite(UtilTest.class);
		suite.addTestSuite(BaseTest.class);
		suite.addTestSuite(LogTest.class);

		// `com.guardtime.transport.*`
		suite.addTestSuite(HttpClientTest.class);
		suite.addTestSuite(HttpStamperTest.class);
		suite.addTestSuite(SimpleHttpStamperTest.class);
		suite.addTestSuite(SocketClientTest.class);

		// `com.guardtime.format.*`
		suite.addTestSuite(FormatTest.class);
		suite.addTestSuite(InsertModeTest.class);

		return suite;
	}

	/**
	 * Entry point for running the full test suite from the command line.
	 * <p>
	 * You may also want to consider using {@code junit.textui.TestRunner}
	 * instead.
	 */
	public static void main(String[] args) {
		Log.addListener(new LogPrinter(System.out), LogLevel.All);
		TestResult res = new TestResult();
		suite().run(res);
		System.out.println(res.runCount() + " tests executed.");
		if (res.failureCount() > 0) {
			System.err.println(res.failureCount() + " tests failed.");
		}
		if (res.errorCount() > 0) {
			System.err.println(res.errorCount() + " internal errors.");
		}
		System.exit(res.failureCount() + res.errorCount());
	}

}
