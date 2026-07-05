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
/*$Id: FreeMindMainMock.java,v 1.1.2.16 2009/03/29 19:37:23 christianfoltin Exp $*/

package tests.freemind;

import java.awt.Container;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import freemind.controller.Controller;
import freemind.controller.MenuBar;
import freemind.main.FreeMindMain;
import freemind.main.FreeMindStarter;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.view.mindmapview.MapView;
import java.util.ArrayList;

public class FreeMindMainMock implements FreeMindMain {

	private final Properties mProperties;

	public FreeMindMainMock() {

		super();
		mProperties = new FreeMindStarter().readDefaultPreferences();
		Resources.createInstance(this);

	}

	@Override
	public JFrame getJFrame() {
		return null;
	}

	@Override
	public boolean isApplet() {
		return false;
	}

	@Override
	public MapView getView() {
		return null;
	}

	@Override
	public void setView(MapView view) {
	}

	@Override
	public Controller getController() {
		return null;
	}

	@Override
	public void setWaitingCursor(boolean waiting) {
	}

	@Override
	public File getPatternsFile() {
		return null;
	}

	@Override
	public MenuBar getFreeMindMenuBar() {
		return null;
	}

	@Override
	public ResourceBundle getResources() {
		return null;
	}

	@Override
	public String getResourceString(String key) {
		return key;
	}

	@Override
	public String getResourceString(String key, String resource) {
		return key;
	}

	@Override
	public Container getContentPane() {
		return null;
	}

	@Override
	public void out(String msg) {
	}

	@Override
	public void err(String msg) {
	}

	@Override
	public void openDocument(URL location) throws Exception {
	}

	@Override
	public void repaint() {
	}

	@Override
	public URL getResource(String name) {
		return ClassLoader.getSystemResource(name);
	}

	@Override
	public int getIntProperty(String key, int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}

	@Override
	public Properties getProperties() {
		return mProperties;
	}

	@Override
	public String getProperty(String key) {
		return mProperties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
	}

	@Override
	public void saveProperties(boolean pIsShutdown) {
	}

	@Override
	public String getFreemindDirectory() {
		return ".";
	}

	@Override
	public JLayeredPane getLayeredPane() {
		return null;
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public int getWinHeight() {
		return 0;
	}

	@Override
	public int getWinWidth() {
		return 0;
	}

	@Override
	public int getWinState() {
		return 0;
	}

	@Override
	public int getWinX() {
		return 0;
	}

	@Override
	public int getWinY() {
		return 0;
	}

	@Override
	public VersionInformation getFreemindVersion() {
		return new VersionInformation(1, 0, 0, FreeMindMain.VERSION_TYPE_ALPHA,
				42);
	}

	@Override
	public Logger getLogger(String forClass) {
		return java.util.logging.Logger.getLogger(forClass);
	}

	@Override
	public ClassLoader getFreeMindClassLoader() {
		ClassLoader classLoader = this.getClass().getClassLoader();
		try {
			return new URLClassLoader(new URL[] { Tools.fileToUrl(new File(
					getFreemindBaseDir())) }, classLoader);
		} catch (MalformedURLException e) {
			freemind.main.Resources.getInstance().logException(e);
			return classLoader;
		}
	}

	@Override
	public String getFreemindBaseDir() {
		return ".";
	}

	@Override
	public String getAdjustableProperty(String pLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultProperty(String pKey, String pValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSplitPane insertComponentIntoSplitPane(JComponent pParameter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSplitPane() {
		// TODO Auto-generated method stub

	}

	@Override
	public JComponent getContentComponent() {
		return null;
	}

	@Override
	public JScrollPane getScrollPane() {
		return null;
	}

	@Override
	public void registerStartupDoneListener(
			StartupDoneListener pStartupDoneListener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see freemind.main.FreeMindMain#getLoggerList()
	 */
	@Override
	public List getLoggerList() {
		return new ArrayList<>();
	}
}
