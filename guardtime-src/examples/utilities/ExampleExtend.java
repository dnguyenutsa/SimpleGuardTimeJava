package examples.utilities;

/*
 * $Id: ExampleExtend.java 190 2011-03-22 10:12:13Z ahto.truu $
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
import java.io.FileOutputStream;

import com.guardtime.transport.SimpleHttpStamper;
import com.guardtime.tsp.GTTimestamp;



/**
 * An example showing how to extend timestamps using GuardTime client SDK.
 */
public class ExampleExtend {

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.out.println("Usage: java ExampleExtend tsInFile tsOutFile tsaUrl");
			return;
		}

		// Load timestamp
		FileInputStream tsInFile = new FileInputStream(args[0]);
		GTTimestamp ts = GTTimestamp.getInstance(tsInFile);
		tsInFile.close();

		// Extend timestamp
		SimpleHttpStamper.extend(ts, args[2]);

		// Save extended timestamp
		if (ts.isExtended()) {
			FileOutputStream tsOutFile = new FileOutputStream(args[1]);
			tsOutFile.write(ts.getEncoded());
			tsOutFile.close();
		}
	}
}
