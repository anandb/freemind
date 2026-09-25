/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
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
/*$Id: MindMapToolBar.java,v 1.12.18.1.12.5 2009/07/04 20:38:27 christianfoltin Exp $*/

package freemind.modes.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import freemind.controller.Controller;
import freemind.controller.FreeMindToolBar;
import freemind.modes.mindmapmode.actions.IconAction;
import freemind.modes.MindIcon;
import freemind.controller.StructuredMenuHolder;
import freemind.controller.filter.DefaultFilter;
import freemind.controller.filter.Filter;
import freemind.controller.filter.condition.Condition;
import freemind.controller.filter.condition.IconContainedCondition;
import freemind.controller.filter.condition.IconNotContainedCondition;
import freemind.controller.filter.condition.NoFilteringCondition;
import freemind.modes.MindMap;
import freemind.controller.ZoomListener;
import freemind.controller.color.ColorPair;
import freemind.controller.color.JColorCombo;
import freemind.main.FreeMind;
import freemind.main.Resources;
import freemind.main.Tools;
import freemind.modes.MindMapNode;
import freemind.view.ImageFactory;
import freemind.view.mindmapview.MapView;

@SuppressWarnings("serial")
public class MindMapToolBar extends FreeMindToolBar implements ZoomListener {

	/**
	 * A combo box that doesn't fill the complete screen.
	 * See http://stackoverflow.com/questions/13345640/does-anyone-know-how-to-layout-a-jtoolbar-that-doest-move-or-re-size-any-compon
	 */
	private final class FreeMindComboBox extends JComboBox<String> {

		public FreeMindComboBox(String[] pItems) {
			super(pItems);
		}

		public java.awt.Dimension getMaximumSize() {
			return getPreferredSize();
		}
	}

	private static final String[] sizes = { "8", "10", "12", "14", "16", "18",
			"20", "24", "28" };

	/** Preference key storing the selected filter-status state. */
	private static final String FILTER_STATUS_PROPERTY = "filter_status_state";
	private static final String FILTER_STATUS_ALL = "all";
	private static final String FILTER_STATUS_COMPLETE = "complete";
	private static final String FILTER_STATUS_PENDING = "pending";

	private MindMapController c;
	private JComboBox<String> fonts, size;
	private JAutoScrollBarPane iconToolBarScrollPane;
	private JToolBar iconToolBar;
	private javax.swing.JTextField searchBox;
	private JToolBar removeToolBar;
	private Action filterAction;
	/** Currently applied filter-state (all/complete/pending). */
	private String filterStatus = FILTER_STATUS_ALL;
	private boolean fontSize_IgnoreChangeEvent = false;
	private boolean fontFamily_IgnoreChangeEvent = false;
	private boolean color_IgnoreChangeEvent = false;
	private ItemListener fontsListener;
	private ItemListener sizeListener;
	private JComboBox<String> zoom;
	private String userDefinedZoom;
	private JColorCombo colorCombo;
	private int userDefinedCounter = 1;

	protected static java.util.logging.Logger logger = null;
	
	public MindMapToolBar(MindMapController controller) {
		super();
		this.c = controller;
		if (logger == null) {
			logger = freemind.main.Resources.getInstance().getLogger(
					this.getClass().getName());
		}
		this.setRollover(true);
		fonts = new FreeMindComboBox(Tools.getAvailableFonts());
		fonts.setFocusable(false);
		size = new FreeMindComboBox(sizes);
		size.setFocusable(false);
		
		String osName = System.getProperty("os.name").toLowerCase();
		final String placeholderText = (osName.contains("mac") || osName.contains("darwin")) ? "CMD + L" : "CTRL + L";

		searchBox = new javax.swing.JTextField() {
			@Override
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				if (getText().isEmpty()) {
					java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
					try {
						g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						g2d.setColor(new java.awt.Color(128, 128, 128));
						g2d.setFont(getFont());
						java.awt.Insets insets = getInsets();
						int x = (insets != null) ? insets.left + 2 : 5;
						java.awt.FontMetrics fm = g2d.getFontMetrics();
						int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
						g2d.drawString(placeholderText, x, y);
					} finally {
						g2d.dispose();
					}
				}
			}
		};
		searchBox.setToolTipText("Search icons (" + placeholderText + ")");
		searchBox.putClientProperty("JTextField.placeholderText", placeholderText);
		javax.swing.UIManager.put("Component.placeholderForeground", new java.awt.Color(128, 128, 128));
		searchBox.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { filterIcons(searchBox.getText()); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { filterIcons(searchBox.getText()); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { filterIcons(searchBox.getText()); }
		});
		searchBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP ||
					e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
					e.consume();
					boolean focused = false;
					for (java.awt.Component comp : removeToolBar.getComponents()) {
						if (comp.isVisible() && comp instanceof AbstractButton) {
							comp.requestFocusInWindow();
							focused = true;
							break;
						}
					}
					if (!focused) {
						for (java.awt.Component comp : iconToolBar.getComponents()) {
							if (comp.isVisible() && comp instanceof AbstractButton) {
								comp.requestFocusInWindow();
								break;
							}
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					for (java.awt.Component comp : iconToolBar.getComponents()) {
						if (comp.isVisible() && comp instanceof javax.swing.JButton) {
							((javax.swing.JButton)comp).doClick();
							c.focusMapView();
							break;
						}
					}
				}
			}
		});

		iconToolBar = new FreeMindToolBar();
		iconToolBarScrollPane = new JAutoScrollBarPane(iconToolBar);
		removeToolBar = new FreeMindToolBar();
		removeToolBar.setFloatable(false);
		removeToolBar.setBorderPainted(false);
		String iconBarPosition = getController().getProperty(FreeMind.ICON_BAR_POSITION);
		if ("top".equals(iconBarPosition)) {
			iconToolBar.setOrientation(JToolBar.HORIZONTAL);
			int rows = getController().getIntProperty(FreeMind.ICON_BAR_ROW_AMOUNT, 1);
			iconToolBar.setLayout(new GridLayout(rows, 0));
			iconToolBarScrollPane.getHorizontalScrollBar().setUnitIncrement(100);
		} else {
			iconToolBar.setOrientation(JToolBar.VERTICAL);
			int cols = getController().getIntProperty(FreeMind.ICON_BAR_COLUMN_AMOUNT, 1);
			iconToolBar.setLayout(new GridLayout(0, cols));
			iconToolBarScrollPane.getVerticalScrollBar().setUnitIncrement(100);
		}
		fontsListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				// TODO: this is super-dirty, why doesn't the toolbar know the
				// model?
				if (fontFamily_IgnoreChangeEvent) {
					// fc, 27.8.2004: I don't understand, why the ignore type is
					// resetted here.
					// let's see: fontFamily_IgnoreChangeEvent = false;
					return;
				}
				fontFamily_IgnoreChangeEvent = true;
				c.fontFamily.actionPerformed((String) e.getItem());
				fontFamily_IgnoreChangeEvent = false;
			}
		};
		fonts.addItemListener(fontsListener);
		sizeListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// System.err.println("ce:"+e);
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				// change the font size
				// TODO: this is super-dirty, why doesn't the toolbar know the
				// model?
				if (fontSize_IgnoreChangeEvent) {
					// fc, 27.8.2004: I don't understand, why the ignore type is
					// resetted here.
					// let's see: fontSize_IgnoreChangeEvent = false;
					return;
				}
				// call action:
				c.fontSize.actionPerformed((String) e.getItem());
			}
		};
		size.addItemListener(sizeListener);
		userDefinedZoom = controller.getText("user_defined_zoom");

		zoom = new FreeMindComboBox(controller.getController().getZooms());
		zoom.setSelectedItem("150%");
		zoom.addItem(userDefinedZoom);
		// Focus fix.
		zoom.setFocusable(false);
		zoom.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// todo: dialog with user zoom value, if user zoom is chosen.
				// change proposed by dimitri:
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setZoomByItem(e.getItem());
				}
			}
		});
		
		colorCombo = new JColorCombo();
		colorCombo.setFocusable(false);
		colorCombo.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(color_IgnoreChangeEvent){
					return;
				}
				if (e.getStateChange() == ItemEvent.SELECTED) {
					color_IgnoreChangeEvent = true;
					setFontColorByItem((ColorPair) e.getItem());
					color_IgnoreChangeEvent = false;
				}
			}

		});
	}

	private void setZoomByItem(Object item) {
		if (((String) item).equals(userDefinedZoom))
			return;
		String dirty = (String) item;
		String cleaned = dirty.substring(0, dirty.length() - 1);
		// change representation ("125" to 1.25)
		float zoomValue = Float.parseFloat(cleaned) / 100F; // nothing to do...
		// remove '%' sign
		getController().setZoom(zoomValue);
	}

	private void setFontColorByItem(ColorPair pItem) {
		for (MindMapNode node : c.getSelecteds()) {
			c.setNodeColor(node, pItem.color);
		}
	}
	

	
	protected Controller getController() {
		return c.getController();
	}
	
	public void update(StructuredMenuHolder holder) {
		this.removeAll();
		holder.updateMenus(this, "mindmapmode_toolbar/");
		
		addIcon("images/list-add-font.svg");
		fonts.setMaximumRowCount(30);
		add(fonts);

//		size.setEditor(new BasicComboBoxEditor());
//		size.setEditable(true);
		addIcon("images/format-font-size-more.png");
		add(size);
		JLabel label = addIcon("images/format-text-color.png");
		label.setToolTipText(Resources.getInstance().getText("mindmapmode_toolbar_font_color"));
		add(colorCombo);
		add(Box.createHorizontalGlue());
		addIcon("images/page-zoom.svg");
		add(zoom);
		
		// remove tool bar (always visible, outside scroll pane)
		removeToolBar.removeAll();
		removeToolBar.add(c.removeLastIconAction);
		removeToolBar.add(c.removeAllIconsAction);
		removeToolBar.add(createFilterAction());
		removeToolBar.addSeparator();
		removeToolBar.revalidate();
		// icon tool bar (inside scroll pane, filterable)
		iconToolBar.removeAll();
		for (int i = 0; i < c.iconActions.size(); ++i) {
			iconToolBar.add((Action) c.iconActions.get(i));
		}
		configureToolbarButtons(removeToolBar);
		configureToolbarButtons(iconToolBar);
		autoSizeGrid();
		iconToolBar.revalidate();
		iconToolBarScrollPane.revalidate();
	}

	private Action createFilterAction() {
		if (filterAction == null) {
			filterAction = new AbstractAction() {
				private static final long serialVersionUID = 1L;
				{
					putValue(Action.NAME, Resources.getInstance()
							.getText("filter_status_button_tooltip"));
					putValue(Action.SMALL_ICON, ImageFactory.getInstance()
							.createIcon("images/funnel.svg"));
					putValue(Action.SHORT_DESCRIPTION, Resources.getInstance()
							.getText("filter_status_button_tooltip"));
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					Component anchor = (e.getSource() instanceof Component)
							? (Component) e.getSource() : MindMapToolBar.this.removeToolBar;
					showFilterMenu(anchor);
				}
			};
		}
		return filterAction;
	}

	private void showFilterMenu(Component anchor) {
		JPopupMenu menu = new JPopupMenu();
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem allItem = addFilterMenuItem(menu, group, "filter_status_all",
				FILTER_STATUS_ALL, NoFilteringCondition.createCondition());
		JRadioButtonMenuItem completeItem = addFilterMenuItem(menu, group, "filter_status_complete",
				FILTER_STATUS_COMPLETE, new IconContainedCondition("button_ok"));
		JRadioButtonMenuItem pendingItem = addFilterMenuItem(menu, group, "filter_status_pending",
				FILTER_STATUS_PENDING, new IconNotContainedCondition("button_ok"));
		// Select the previously applied state ('All' is the default).
		JRadioButtonMenuItem selectedItem = allItem;
		if (FILTER_STATUS_COMPLETE.equals(filterStatus)) {
			selectedItem = completeItem;
		} else if (FILTER_STATUS_PENDING.equals(filterStatus)) {
			selectedItem = pendingItem;
		}
		selectedItem.setSelected(true);
		menu.show(anchor, 0, anchor.getHeight());
	}

	private JRadioButtonMenuItem addFilterMenuItem(JPopupMenu menu, ButtonGroup group,
			String textKey, String status, Condition condition) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				Resources.getInstance().getText(textKey));
		item.addActionListener(e -> {
			filterStatus = status;
			getController().setProperty(FILTER_STATUS_PROPERTY, status);
			applyFilter(condition);
		});
		group.add(item);
		menu.add(item);
		return item;
	}

	/**
	 * Re-applies the filter-state saved in the preferences at startup.
	 */
	private void applySavedFilter() {
		String saved = getController().getProperty(FILTER_STATUS_PROPERTY);
		if (saved == null) {
			return;
		}
		switch (saved) {
		case FILTER_STATUS_COMPLETE:
			filterStatus = FILTER_STATUS_COMPLETE;
			applyFilter(new IconContainedCondition("button_ok"));
			break;
		case FILTER_STATUS_PENDING:
			filterStatus = FILTER_STATUS_PENDING;
			applyFilter(new IconNotContainedCondition("button_ok"));
			break;
		default:
			filterStatus = FILTER_STATUS_ALL;
			break;
		}
	}

	private void applyFilter(Condition condition) {
		Controller controller = getController();
		MindMap map = controller.getModel();
		if (map == null) {
			return;
		}
		// show ancestors so that the path from a visible node up to the root
		// is always displayed.
		Filter filter = new DefaultFilter(condition, true, false);
		map.setFilter(filter);
		filter.applyFilter(controller);
		controller.getModeController().refreshMap();
		DefaultFilter.selectVisibleNode(controller.getView());
	}

	private void configureToolbarButtons(JToolBar toolBar) {
		int iconSize = getController().getIntProperty(FreeMind.RESOURCES_TOOLBAR_ICON_SIZE, 48);
		for (int i = 0; i < toolBar.getComponentCount(); i++) {
			java.awt.Component comp = toolBar.getComponent(i);
			if (comp instanceof AbstractButton) {
				final AbstractButton btn = (AbstractButton) comp;
				btn.setFocusable(true);
				// Make grid cells square
				btn.setPreferredSize(new java.awt.Dimension(iconSize, iconSize));
				
				// Keep track of original visual states
				final javax.swing.border.Border originalBorder = btn.getBorder();
				final boolean originalBorderPainted = btn.isBorderPainted();
				final boolean originalContentAreaFilled = btn.isContentAreaFilled();
				final Color originalBackground = btn.getBackground();
				final boolean originalOpaque = btn.isOpaque();

				// Remove existing focus listeners if any
				for (java.awt.event.FocusListener l : btn.getFocusListeners()) {
					btn.removeFocusListener(l);
				}

				btn.addFocusListener(new java.awt.event.FocusListener() {
					public void focusGained(java.awt.event.FocusEvent e) {
						btn.setBorderPainted(true);
						Color focusColor = javax.swing.UIManager.getColor("Component.focusColor");
						if (focusColor == null) {
							focusColor = new Color(26, 115, 232); // Google Blue / elegant blue
						}
						btn.setBorder(javax.swing.BorderFactory.createLineBorder(focusColor, 1));
						
						// Semi-transparent background highlight
						Color highlightColor = new Color(
								focusColor.getRed(), 
								focusColor.getGreen(), 
								focusColor.getBlue(), 
								40);
						btn.setBackground(highlightColor);
						btn.setContentAreaFilled(true);
						btn.setOpaque(false);
						btn.repaint();
					}

					public void focusLost(java.awt.event.FocusEvent e) {
						btn.setBorderPainted(originalBorderPainted);
						btn.setBorder(originalBorder);
						btn.setContentAreaFilled(originalContentAreaFilled);
						btn.setBackground(originalBackground);
						btn.setOpaque(originalOpaque);
						btn.repaint();
					}
				});

				// Remove existing key listeners if any
				for (java.awt.event.KeyListener l : btn.getKeyListeners()) {
					btn.removeKeyListener(l);
				}

				btn.addKeyListener(new java.awt.event.KeyAdapter() {
					@Override
					public void keyPressed(java.awt.event.KeyEvent e) {
						int keyCode = e.getKeyCode();
						if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP ||
							keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
							e.consume();
							handleArrowKeyNavigation(btn, toolBar, keyCode);
						} else if (keyCode == KeyEvent.VK_ENTER) {
							e.consume();
							btn.doClick();
							c.focusMapView();
						}
					}

					@Override
					public void keyTyped(java.awt.event.KeyEvent e) {
						if (e.isControlDown() || e.isAltDown() || e.isMetaDown()) {
							return;
						}
						char c = e.getKeyChar();
						if (c != java.awt.event.KeyEvent.CHAR_UNDEFINED && c != '\n' && c != '\t' && c != '\u001b') {
							searchBox.requestFocusInWindow();
							if (c == '\b') { // backspace
								String text = searchBox.getText();
								if (text.length() > 0) {
									searchBox.setText(text.substring(0, text.length() - 1));
								}
							} else {
								searchBox.setText(searchBox.getText() + c);
							}
							searchBox.setCaretPosition(searchBox.getText().length());
							e.consume();
						}
					}
				});
			}
		}
	}

	private void autoSizeGrid() {
		int gap = 16;
		String iconBarPosition = getController().getProperty(FreeMind.ICON_BAR_POSITION);
		if ("top".equals(iconBarPosition)) {
			int rows = getController().getIntProperty(FreeMind.ICON_BAR_ROW_AMOUNT, 1);
			iconToolBar.setLayout(new GridLayout(rows, 0, gap, gap));
		} else {
			int cols = getController().getIntProperty(FreeMind.ICON_BAR_COLUMN_AMOUNT, 1);
			iconToolBar.setLayout(new GridLayout(0, cols, gap, gap));
		}
	}

	private java.util.List<AbstractButton> getVisibleButtons(JToolBar toolBar) {
		java.util.List<AbstractButton> list = new java.util.ArrayList<>();
		for (Component comp : toolBar.getComponents()) {
			if (comp.isVisible() && comp instanceof AbstractButton) {
				list.add((AbstractButton) comp);
			}
		}
		return list;
	}

	private void handleArrowKeyNavigation(AbstractButton btn, JToolBar toolBar, int keyCode) {
		java.util.List<AbstractButton> removeButtons = getVisibleButtons(removeToolBar);
		java.util.List<AbstractButton> iconButtons = getVisibleButtons(iconToolBar);
		
		if (toolBar == removeToolBar) {
			int index = removeButtons.indexOf(btn);
			if (index == -1) return;
			
			if (keyCode == KeyEvent.VK_LEFT) {
				if (index > 0) {
					removeButtons.get(index - 1).requestFocusInWindow();
				}
			} else if (keyCode == KeyEvent.VK_RIGHT) {
				if (index < removeButtons.size() - 1) {
					removeButtons.get(index + 1).requestFocusInWindow();
				}
			} else if (keyCode == KeyEvent.VK_UP) {
				focusSearchBox();
			} else if (keyCode == KeyEvent.VK_DOWN) {
				if (!iconButtons.isEmpty()) {
					iconButtons.get(0).requestFocusInWindow();
				}
			}
		} else if (toolBar == iconToolBar) {
			int index = iconButtons.indexOf(btn);
			if (index == -1) return;
			
			Component[] comps = iconToolBar.getComponents();
			int fullIndex = java.util.Arrays.asList(comps).indexOf(btn);
			if (fullIndex == -1) return;

			String iconBarPosition = getController().getProperty(FreeMind.ICON_BAR_POSITION);
			int cols = 1;
			int rows = 1;
			int totalCount = comps.length;
			if ("top".equals(iconBarPosition)) {
				rows = getController().getIntProperty(FreeMind.ICON_BAR_ROW_AMOUNT, 1);
				cols = (totalCount + rows - 1) / rows;
			} else {
				cols = getController().getIntProperty(FreeMind.ICON_BAR_COLUMN_AMOUNT, 1);
				rows = (totalCount + cols - 1) / cols;
			}
			if (cols < 1) cols = 1;
			if (rows < 1) rows = 1;

			if (keyCode == KeyEvent.VK_LEFT) {
				if (isVisibleButton(comps, fullIndex - 1)) {
					comps[fullIndex - 1].requestFocusInWindow();
				} else {
					if (index > 0) {
						iconButtons.get(index - 1).requestFocusInWindow();
					} else {
						if (!removeButtons.isEmpty()) {
							removeButtons.get(0).requestFocusInWindow();
						} else {
							focusSearchBox();
						}
					}
				}
			} else if (keyCode == KeyEvent.VK_RIGHT) {
				if (isVisibleButton(comps, fullIndex + 1)) {
					comps[fullIndex + 1].requestFocusInWindow();
				} else {
					if (index < iconButtons.size() - 1) {
						iconButtons.get(index + 1).requestFocusInWindow();
					}
				}
			} else if (keyCode == KeyEvent.VK_UP) {
				int targetFullIndex = fullIndex - cols;
				if (isVisibleButton(comps, targetFullIndex)) {
					comps[targetFullIndex].requestFocusInWindow();
				} else {
					// Fallback to previous active icon
					if (index > 0) {
						iconButtons.get(index - 1).requestFocusInWindow();
					} else {
						if (!removeButtons.isEmpty()) {
							removeButtons.get(0).requestFocusInWindow();
						} else {
							focusSearchBox();
						}
					}
				}
			} else if (keyCode == KeyEvent.VK_DOWN) {
				int targetFullIndex = fullIndex + cols;
				if (isVisibleButton(comps, targetFullIndex)) {
					comps[targetFullIndex].requestFocusInWindow();
				} else {
					// Fallback to next active icon
					if (index < iconButtons.size() - 1) {
						iconButtons.get(index + 1).requestFocusInWindow();
					}
				}
			}
		}
	}

	private boolean isVisibleButton(Component[] comps, int idx) {
		if (idx >= 0 && idx < comps.length) {
			Component comp = comps[idx];
			return comp.isVisible() && comp instanceof AbstractButton;
		}
		return false;
	}

	public JLabel addIcon(String iconPath) {
		add(new JToolBar.Separator());
		JLabel label = new JLabel(ImageFactory.getInstance().createIcon(iconPath));
		label.setText(" ");
		add(label);
//		add(new JToolBar.Separator());
		return label;
	}

	// Daniel Polansky: both the following methods trigger item listeners above.
	// Those listeners obtain two events: first DESELECTED and then
	// SELECTED. Both events are to be ignored - we don't want to update
	// a node with its own font. The item listeners should react only
	// to a user change, not to our change.

	public void selectFontSize(String fontSize) // (DiPo)
	{
		fontSize_IgnoreChangeEvent = true;
		size.setSelectedItem(fontSize);
		fontSize_IgnoreChangeEvent = false;
	}

	/**
	 * Cached left toolbar panel. It must be created only once: the caller
	 * (Controller) asks for it repeatedly (on mode change, on visibility toggle)
	 * and every {@code new} panel would re-parent the search box into a detached
	 * container, which breaks {@link #focusSearchBox()}.
	 */
	private JPanel leftToolBar;

	Component getLeftToolBar() {
		if (leftToolBar != null) {
			return leftToolBar;
		}
		JPanel panel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(searchBox, BorderLayout.NORTH);
		topPanel.add(removeToolBar, BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(iconToolBarScrollPane, BorderLayout.CENTER);
		leftToolBar = panel;
		return panel;
	}

	public void focusSearchBox() {
		if (searchBox == null) {
			return;
		}
		// Make sure the box is really attached and showing, otherwise
		// requestFocusInWindow() is a no-op.
		java.awt.Container parent = searchBox.getParent();
		if (parent != null && !parent.isShowing()) {
			parent.setVisible(true);
		}
		if (!searchBox.isShowing()) {
			return;
		}
		searchBox.requestFocusInWindow();
		searchBox.requestFocus();
		searchBox.selectAll();
	}

	public void selectFontName(String fontName) // (DiPo)
	{
		if (fontFamily_IgnoreChangeEvent) {
			return;
		}
		fontFamily_IgnoreChangeEvent = true;
		fonts.setEditable(true);
		fonts.setSelectedItem(fontName);
		fonts.setEditable(false);
		fontFamily_IgnoreChangeEvent = false;
	}

	void setAllActions(boolean enabled) {
		fonts.setEnabled(enabled);
		size.setEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see freemind.controller.ZoomListener#setZoom(float)
	 */
	public void setZoom(float f) {
		logger.fine("setZoomComboBox is called with " + f + ".");
		String toBeFound = getItemForZoom(f);
		for (int i = 0; i < zoom.getItemCount(); ++i) {
			if (toBeFound.equals((String) zoom.getItemAt(i))) {
				// found
				zoom.setSelectedItem(toBeFound);
				return;
			}
		}
		zoom.setSelectedItem(userDefinedZoom);
		
	}
	
	private String getItemForZoom(float f) {
		return (int) (f * 100F) + "%";
	}

	public void startup() {
		getController().registerZoomListener(this);
		applySavedFilter();
	}
		
	public void shutdown() {
		getController().deregisterZoomListener(this);
	}

	void filterIcons(String searchText) {
		Component[] comps = iconToolBar.getComponents();
		String lowerSearch = (searchText == null) ? "" : searchText.trim().toLowerCase();
		boolean emptySearch = lowerSearch.isEmpty();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof AbstractButton) {
				AbstractButton btn = (AbstractButton) comps[i];
				String desc = btn.getToolTipText();
				// Also match against icon file name (e.g. "help", "hourglass")
				String iconName = null;
				String tags = null;
				if (btn.getAction() instanceof IconAction) {
					MindIcon mindIcon = ((IconAction) btn.getAction()).getMindIcon();
					iconName = mindIcon.getName();
					tags = mindIcon.getTags();
				}
				boolean match = emptySearch
						|| (desc != null && desc.toLowerCase().contains(lowerSearch))
						|| (iconName != null && iconName.contains(lowerSearch))
						|| (tags != null && tags.toLowerCase().contains(lowerSearch));
				comps[i].setVisible(match);
			}
		}
		autoSizeGrid();
		iconToolBar.revalidate();
		iconToolBarScrollPane.revalidate();
		iconToolBar.repaint();
	}

	void focusFirstVisibleIcon() {
		Component[] comps = iconToolBar.getComponents();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof AbstractButton && comps[i].isVisible()) {
				comps[i].requestFocusInWindow();
				return;
			}
		}
	}

	public void selectColor(Color pColor) {
		if(pColor == null){
			pColor = MapView.standardNodeTextColor;
		}
		color_IgnoreChangeEvent = true;
		for (int i = 0; i < colorCombo.getModel().getSize(); i++) {
			ColorPair pair = colorCombo.getModel().getElementAt(i);
			if(pair.color.equals(pColor)){
				colorCombo.setSelectedIndex(i);
				color_IgnoreChangeEvent = false;
				return;
			}
		}
		// new color. add it to the combo box:
		ColorPair pair = new ColorPair(pColor, "user" + userDefinedCounter,
				Resources.getInstance().format(
						"mindmapmode_toolbar_font_color_user_defined",
						new Object[] { userDefinedCounter }));
		userDefinedCounter++;
		colorCombo.addItem(pair);
		colorCombo.setSelectedItem(pair);
		color_IgnoreChangeEvent = false;
	}
}
