/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2006 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitri Polivaev and others.
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
/*
 * Created on 06.05.2005
 *
 */
package freemind.controller.filter.condition;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import freemind.main.Resources;
import freemind.modes.MindIcon;

;

/**
 * @author dimitri 06.05.2005
 */
public class ConditionRenderer implements ListCellRenderer<Object> {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null)
			return new JLabel(Resources.getInstance().getResourceString(
					"filter_no_filtering"));
		JComponent component;
		if (value instanceof MindIcon) {
			component = new JLabel(((MindIcon) value).getIcon());
		} else if (value instanceof Condition) {
			Condition cond = (Condition) value;
			component = cond.getListCellRendererComponent();
		} else {
			component = new JLabel(value.toString());
		}
		// Use the look and feel's colours instead of hard-coded ones, so that
		// the renderer also works with dark themes. Otherwise an opaque white
		// cell would be painted underneath the light foreground colour used by
		// such themes, making the condition text unreadable.
		component.setOpaque(true);
		if (isSelected) {
			component.setBackground(selectionBackground(list));
			setForegroundDeep(component, selectionForeground(list));
		} else {
			component.setBackground(background(list));
			setForegroundDeep(component, foreground(list));
		}
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		return component;
	}

	private static Color background(JList<?> list) {
		return list != null ? list.getBackground() : UIManager
				.getColor("List.background");
	}

	private static Color selectionBackground(JList<?> list) {
		return list != null ? list.getSelectionBackground() : UIManager
				.getColor("List.selectionBackground");
	}

	private static Color foreground(JList<?> list) {
		return list != null ? list.getForeground() : UIManager
				.getColor("List.foreground");
	}

	private static Color selectionForeground(JList<?> list) {
		return list != null ? list.getSelectionForeground() : UIManager
				.getColor("List.selectionForeground");
	}

	/**
	 * Sets the foreground colour on the component and all of its children.
	 * Condition renderers are composite components (see {@link JCondition})
	 * whose child labels do not inherit the foreground of their parent.
	 */
	private static void setForegroundDeep(Component component, Color foreground) {
		if (foreground != null) {
			component.setForeground(foreground);
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				setForegroundDeep(child, foreground);
			}
		}
	}

}
