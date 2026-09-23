/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006  Joerg Mueller, Daniel Polansky, Dimitri Polivaev, Christian Foltin and others.
 *
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
 *
 * Created on 06.07.2006
 */
/*$Id: FreeMindStarter.java,v 1.1.2.11 2009/03/29 19:37:23 christianfoltin Exp $*/
package freemind.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * This class should check the java version and start freemind. In order to be
 * able to check, it must be startable with java versions < 1.4. We have
 * therefore a section in the build.xml that explicitly compiles this class for
 * java 1.1 compatibility. Currently, it is unclear, if this works as expected.
 * But in any case, almost no dependencies to other FreeMind sources should be
 * used here.
 * 
 * @author foltin
 * 
 */
public class FreeMindStarter {
	/** Doubled variable on purpose. See header of this class. */
	static final String JAVA_VERSION = System.getProperty("java.version");

	   public static void main(String[] args) {
        try {
            FreeMindStarter starter = new FreeMindStarter();            
            Properties defaultPreferences = starter.readDefaultPreferences();
            starter.createUserDirectory(defaultPreferences);
            Properties userPreferences = starter.readUsersPreferences(defaultPreferences);
            starter.setDefaultLocale(userPreferences);
            
            // workaround for java bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7075600
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
            FreeMind.main(args, defaultPreferences, userPreferences, starter.getUserPreferencesFile(defaultPreferences));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "freemind.main.FreeMind can't be started: " + e.getLocalizedMessage() + "\n" + Tools.getStacktrace(e),
                    "Startup problem", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

	private void createUserDirectory(Properties pDefaultProperties) {
		File userPropertiesFolder = new File(
				getFreeMindDirectory(pDefaultProperties));
		try {
			// create user directory:
			if (!userPropertiesFolder.exists()) {
				userPropertiesFolder.mkdir();
			}
		} catch (Exception e) {
			// exception is logged to console as we don't have a logger
			e.printStackTrace();
			System.err.println("Cannot create folder for user properties and logging: '"
							+ userPropertiesFolder.getAbsolutePath() + "'");

		}
	}

	/**
	 * @param pProperties
	 */
	private void setDefaultLocale(Properties pProperties) {
		String lang = pProperties.getProperty(FreeMindCommon.RESOURCE_LANGUAGE);
		if (lang == null) {
			return;
		}
		Locale localeDef = null;
		switch (lang.length()) {
		case 2:
			localeDef = new Locale(lang);
			break;
		case 5:
			localeDef = new Locale(lang.substring(0, 1), lang.substring(3, 4));
			break;
		default:
			return;
		}
		Locale.setDefault(localeDef);
	}

	/**
	 * Detect and set sun.java2d.uiScale BEFORE any AWT/Swing initialization.
	 * Priority: explicit JVM property > scaling_factor_property > Xft.dpi auto-detect.
	 * This fixes mouse pointer drift on HiDPI displays where the Java2D
	 * rendering pipeline needs to know the display scale factor upfront.
	 */
	private void initHiDpiScaling(Properties userPreferences) {
		// 1. If already set via -D flag, respect it
		String existing = System.getProperty("sun.java2d.uiScale");
		if (existing != null) {
			System.out.println("HiDPI: sun.java2d.uiScale already set to " + existing);
			return;
		}

		float scale = 0f;

		// 2. Check scaling_factor_property (user preference, e.g. 150 = 1.5x)
		String scaleProp = userPreferences.getProperty("scaling_factor_property");
		if (scaleProp != null) {
			try {
				int pct = Integer.parseInt(scaleProp.trim());
				if (pct > 0 && pct != 100) {
					scale = pct / 100f;
					System.out.println("HiDPI: using scaling_factor_property=" + pct + " -> uiScale=" + scale);
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		// 3. Auto-detect from Xft.dpi (set by GNOME/KDE/Xresources)
		if (scale <= 0f) {
			scale = detectXftDpiScale();
		}

		if (scale > 0f && Math.abs(scale - 1.0f) > 0.01f) {
			System.setProperty("sun.java2d.uiScale", String.valueOf(scale));
			System.out.println("HiDPI: set sun.java2d.uiScale=" + scale);
		}
	}

	/**
	 * Read Xft.dpi from X resource database and compute scale relative to 96 DPI.
	 * Returns 0 if detection fails.
	 */
	private float detectXftDpiScale() {
		try {
			Process proc = new ProcessBuilder("xrdb", "-query")
					.redirectErrorStream(true)
					.start();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(proc.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("Xft.dpi:")) {
						String val = line.substring("Xft.dpi:".length()).trim();
						float dpi = Float.parseFloat(val);
						if (dpi > 0) {
							float detected = dpi / 96f;
							System.out.println("HiDPI: Xft.dpi=" + dpi
									+ " -> detected scale=" + detected);
							// Only apply if meaningfully different from 1.0
							if (Math.abs(detected - 1.0f) > 0.01f) {
								return detected;
							}
						}
						break;
					}
				}
			}
			proc.waitFor();
		} catch (Exception e) {
			// xrdb not available or failed — not a problem
		}
		return 0f;
	}

	private Properties readUsersPreferences(Properties defaultPreferences) {
		Properties auto = null;
		auto = new Properties(defaultPreferences);
		try {
			InputStream in = null;
			File autoPropertiesFile = getUserPreferencesFile(defaultPreferences);
			if (autoPropertiesFile.exists()) {
				in = new FileInputStream(autoPropertiesFile);
				auto.load(in);
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return auto;
	}

	private File getUserPreferencesFile(Properties defaultPreferences) {
		if (defaultPreferences == null) {
			System.err.println("Panic! Error while loading default properties.");
			System.exit(1);
		}
		String freemindDirectory = getFreeMindDirectory(defaultPreferences);
		File userPropertiesFolder = new File(freemindDirectory);
		File autoPropertiesFile = new File(userPropertiesFolder,
				defaultPreferences.getProperty("autoproperties"));
		return autoPropertiesFile;
	}

	private String getFreeMindDirectory(Properties defaultPreferences) {
		return System.getProperty("user.home") + File.separator
				+ defaultPreferences.getProperty("properties_folder");
	}

	public Properties readDefaultPreferences() {
		String propsLoc = "freemind.properties";
		URL defaultPropsURL =
				this.getClass().getClassLoader().getResource(propsLoc);
		Properties props = new Properties();
		try {
			InputStream in = defaultPropsURL.openStream();
			props.load(in);
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Panic! Error while loading default properties.");
		}
		return props;
	}
	
	public static class ProxyAuthenticator extends Authenticator {

	    private String user, password;

	    public ProxyAuthenticator(String user, String password) {
	        this.user = user;
	        this.password = password;
	    }

	    protected PasswordAuthentication getPasswordAuthentication() {
	        return new PasswordAuthentication(user, password.toCharArray());
	    }
	}
}