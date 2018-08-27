/*******************************************************************************
 * Copyright (c) 2017 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev <simeon.danailov.andreev@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.events;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests() {
		this(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());

		suite.addTest(BuildProjectFromMultipleJobsTest.suite());

		return suite;
	}
}
