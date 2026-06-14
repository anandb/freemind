package freemind.view.mindmapview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Drop-in replacement for SimplyHTML's SHTMLPanel using Swing's built-in
 * HTMLEditorKit. Provides the same API surface used by NodeNoteRegistration
 * and EditNodeWYSIWYG.
 */
public class SwingHtmlEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final javax.swing.JEditorPane editorPane;
    private final HTMLEditorKit htmlEditorKit;
    private boolean dirty = false;
    private String originalContent = "";
    private ActionListener hyperlinkHandler;

    public SwingHtmlEditorPanel() {
        super(new BorderLayout());
        htmlEditorKit = new HTMLEditorKit();
        editorPane = new javax.swing.JEditorPane();
        editorPane.setEditorKit(htmlEditorKit);
        editorPane.setContentType("text/html");
        add(editorPane, BorderLayout.CENTER);

        // Track dirty state
        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { markDirty(); }
            public void removeUpdate(DocumentEvent e) { markDirty(); }
            public void changedUpdate(DocumentEvent e) { markDirty(); }
        });

        // Handle hyperlinks via property change (HTMLEditorKit fires this)
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("href".equals(evt.getPropertyName()) && hyperlinkHandler != null) {
                    Object newValue = evt.getNewValue();
                    if (newValue != null) {
                        ActionEvent ae = new ActionEvent(editorPane,
                                ActionEvent.ACTION_PERFORMED, newValue.toString());
                        hyperlinkHandler.actionPerformed(ae);
                    }
                }
            }
        });
    }

    private void markDirty() {
        dirty = true;
    }

    /**
     * Returns the underlying JEditorPane.
     */
    public javax.swing.JEditorPane getEditorPane() {
        return editorPane;
    }

    /**
     * Returns the HTML document.
     */
    public HTMLDocument getDocument() {
        return (HTMLDocument) editorPane.getDocument();
    }

    /**
     * Returns the current HTML content as a string.
     */
    public String getDocumentText() {
        String text = editorPane.getText();
        if (text == null) {
            text = "";
        }
        return text;
    }

    /**
     * Sets the HTML content and resets dirty state.
     */
    public void setCurrentDocumentContent(String html) {
        if (html == null) {
            html = "";
        }
        dirty = false;
        originalContent = html;
        editorPane.setText(html);
        dirty = false;
    }

    /**
     * Returns true if the content has been modified since last set.
     */
    public boolean needsSaving() {
        return dirty;
    }

    /**
     * Sets the hyperlink click handler.
     */
    public void setOpenHyperlinkHandler(ActionListener handler) {
        this.hyperlinkHandler = handler;
    }

    /**
     * Sets the preferred size of the content pane (for dialog sizing).
     */
    public void setContentPanePreferredSize(Dimension size) {
        editorPane.setPreferredSize(size);
    }

    /**
     * Returns the most recently focused component.
     */
    public java.awt.Component getMostRecentFocusOwner() {
        if (editorPane.isFocusOwner()) {
            return editorPane;
        }
        return editorPane;
    }

    /**
     * Returns the caret position in the editor pane.
     */
    public int getCaretPosition() {
        return editorPane.getCaretPosition();
    }
}
