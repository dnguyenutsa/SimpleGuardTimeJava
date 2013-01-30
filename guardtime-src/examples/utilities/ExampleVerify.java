package examples.utilities;

/*
 * $Id: ExampleVerify.java 190 2011-03-22 10:12:13Z ahto.truu $
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
import java.io.FileInputStream;

import com.guardtime.transport.SimpleHttpStamper;
import com.guardtime.transport.HttpVerificationResult;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTPublicationsFile;
import com.guardtime.tsp.GTTimestamp;



/**
 * An example showing how to verify timestamps using GuardTime client SDK.
 */
public class ExampleVerify {

	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.out.println("Usage: java ExampleVerify dataInFile tsInFile pubUrl <extUrl | ->");
			return;
		}
		if (args[3].equals("-")) {
			args[3] = null;
		}

		// Load timestamp
		FileInputStream tsInFile = new FileInputStream(args[1]);
		GTTimestamp ts = GTTimestamp.getInstance(tsInFile);
		tsInFile.close();

		// Compute data hash
		FileInputStream dataInFile = new FileInputStream(args[0]);
		GTDataHash dataHash = new GTDataHash(ts.getHashAlgorithm());
		dataHash.update(dataInFile).close();
		dataInFile.close();

		// Download publications file
		GTPublicationsFile publicationsFile = SimpleHttpStamper.getPublicationsFile(args[2]);

		// Verify timestamp
		HttpVerificationResult res = SimpleHttpStamper.verify(ts, dataHash, args[3], null, publicationsFile);
		System.out.println(res.isValid() ? "Timestamp valid" : "Timestamp NOT valid");
	}
}
