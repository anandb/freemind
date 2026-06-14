/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2006  Christian Foltin <christianfoltin@users.sourceforge.net>
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/*$Id: AllTests.java,v 1.1.2.5 2008/04/18 21:18:27 christianfoltin Exp $*/

package tests.freemind;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import tests.freemind.findreplace.FindTextTests;

/**
 * JUnit 4 suite runner for JUnit 3 compatibility mode.
 * All test classes extend TestCase (JUnit 3 style) but are executed
 * via JUnit 4 runner with junit-vintage-engine on JUnit Platform.
 */
@RunWith(Suite.class)
@SuiteClasses({
	ScriptEditorPanelTest.class,
	Base64Tests.class,
	FindTextTests.class,
	HtmlConversionTests.class,
	MarshallerTests.class,
	SignedScriptTests.class,
	LastStorageManagementTests.class,
	ToolsTests.class,
	ExportTests.class,
	LayoutTests.class,
	LastOpenedTests.class,
	StandaloneMapTests.class,
	CollaborationTests.class,
	CollaborationTestClient.class
})
public class AllTests {
	// JUnit 4 @RunWith + @SuiteClasses replaces the JUnit 3 TestSuite.builder().
	// Individual test classes remain JUnit 3 (extend TestCase) and run
	// via junit-vintage-engine on the JUnit Platform.
}
