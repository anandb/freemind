/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2015 Christian Foltin, Joerg Mueller, Daniel Polansky, Dimitri Polivaev and others.
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
 */

package freemind.view;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.awt.Image;

import javax.swing.ImageIcon;

import freemind.main.Resources;
import freemind.main.Tools;

/**
 * @author foltin
 * @date 24.05.2015
 */
public class ImageFactory {
	private static ImageFactory mInstance = null;

	public static ImageFactory getInstance() {
		if (mInstance == null) {
			mInstance = new ImageFactory();
		}
		return mInstance;
	}

	private ImageIcon scaleToConfiguredSize(ImageIcon icon) {
		if (icon == null) return null;
		int targetSize = Resources.getInstance().getIntProperty(freemind.main.FreeMind.RESOURCES_TOOLBAR_ICON_SIZE, 32);
		if (icon.getIconWidth() != targetSize || icon.getIconHeight() != targetSize) {
			Image img = icon.getImage();
			if (img != null) {
				Image scaledImg = img.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
				icon.setImage(scaledImg);
			}
		}
		return icon;
	}

	public ImageIcon createIcon(URL pUrl){
		if (pUrl == null) return null;
		if (pUrl.getPath().toLowerCase().endsWith(".svg")) {
			return new SVGImageIcon(pUrl);
		}
		boolean isToolbarOrBubbleIcon = false;
		if (pUrl != null) {
			String path = pUrl.getPath().toLowerCase();
			if (path.contains("images/icons/") || path.contains("/icons/")) {
				isToolbarOrBubbleIcon = true;
			}
		}
		if(Tools.getScalingFactorPlain()==100){
			ImageIcon icon = createUnscaledIcon(pUrl);
			if (isToolbarOrBubbleIcon) {
				scaleToConfiguredSize(icon);
			}
			return icon;
		}
		ScalableImageIcon icon = new ScalableImageIcon(pUrl);
		if (isToolbarOrBubbleIcon) {
			scaleToConfiguredSize(icon);
		}
		icon.setScale(Tools.getScalingFactor());
		return icon;
	}

	/**
	 * All icons directly displayed in the mindmap view are scaled by the zoom.
	 */
	public ImageIcon createUnscaledIcon(URL pResource) {
		if (pResource == null) return null;
		if (pResource.getPath().toLowerCase().endsWith(".svg")) {
			return new SVGImageIcon(pResource);
		}
		ImageIcon icon = new ImageIcon(pResource);
		boolean isToolbarOrBubbleIcon = false;
		if (pResource != null) {
			String path = pResource.getPath().toLowerCase();
			if (path.contains("images/icons/") || path.contains("/icons/")) {
				isToolbarOrBubbleIcon = true;
			}
		}
		if (isToolbarOrBubbleIcon) {
			scaleToConfiguredSize(icon);
		}
		return icon;
	}

	/**
	 * @param pString
	 * @return
	 */
	public ImageIcon createIcon(String pFilePath) {
		if(Tools.getScalingFactorPlain()==200){
			// test for existence  of a scaled icon:
			if(pFilePath.endsWith(".png")){
				try {
					URL url = Resources.getInstance().getResource(pFilePath.replaceAll(".png$", "_32.png"));
					URLConnection connection = url.openConnection();
					if(connection.getContentLength()>0){
						return createUnscaledIcon(url);
					}
				} catch (IOException e) {
					freemind.main.Resources.getInstance().logException(e);
				}
			}
		}
		return createIcon(Resources.getInstance().getResource(pFilePath));
	}
}
