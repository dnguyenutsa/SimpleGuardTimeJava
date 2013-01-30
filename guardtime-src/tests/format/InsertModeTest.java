/*
 * $Id: InsertModeTest.java 169 2011-03-03 18:45:00Z ahto.truu $
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

import com.guardtime.format.InsertMode;

import junit.framework.TestCase;



/**
 * {@link InsertMode} tests.
 */
public class InsertModeTest
extends TestCase {
	public void testInsertModeValues() {
		assertFalse(InsertMode.Append.toString().length() == 0);
		assertFalse(InsertMode.Append.equals(InsertMode.Insert));
		assertFalse(InsertMode.Append.toString().equals(InsertMode.Insert.toString()));
		assertFalse(InsertMode.Append.equals(InsertMode.Replace));
		assertFalse(InsertMode.Append.toString().equals(InsertMode.Replace.toString()));

		assertFalse(InsertMode.Insert.toString().length() == 0);
		assertFalse(InsertMode.Insert.equals(InsertMode.Append));
		assertFalse(InsertMode.Insert.toString().equals(InsertMode.Append.toString()));
		assertFalse(InsertMode.Insert.equals(InsertMode.Replace));
		assertFalse(InsertMode.Insert.toString().equals(InsertMode.Replace.toString()));

		assertFalse(InsertMode.Replace.toString().length() == 0);
		assertFalse(InsertMode.Replace.equals(InsertMode.Append));
		assertFalse(InsertMode.Replace.toString().equals(InsertMode.Append.toString()));
		assertFalse(InsertMode.Replace.equals(InsertMode.Insert));
		assertFalse(InsertMode.Replace.toString().equals(InsertMode.Insert.toString()));
	}
}
