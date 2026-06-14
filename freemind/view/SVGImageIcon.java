package freemind.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import javax.swing.ImageIcon;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

public class SVGImageIcon extends ImageIcon {
    private GraphicsNode rootNode;
    private Rectangle2D bounds;
    private int width;
    private int height;
    private BufferedImage bufferedImage;

    public SVGImageIcon(URL url) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            SVGDocument doc;
            InputStream in = url.openStream();
            try {
                doc = f.createSVGDocument(url.toString(), in);
            } finally {
                in.close();
            }
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            this.rootNode = builder.build(ctx, doc);
            this.bounds = rootNode.getPrimitiveBounds();
            
            // Set size from configured property
            int targetSize = freemind.main.Resources.getInstance().getIntProperty(freemind.main.FreeMind.RESOURCES_TOOLBAR_ICON_SIZE, 32);
            this.width = targetSize;
            this.height = targetSize;
            
            renderImage();
        } catch (Exception e) {
            freemind.main.Resources.getInstance().logException(e);
            // Fallback: blank image
            this.width = 16;
            this.height = 16;
            this.bufferedImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            setImage(bufferedImage);
        }
    }

    private void renderImage() {
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        if (bounds != null) {
            double scaleX = width / bounds.getWidth();
            double scaleY = height / bounds.getHeight();
            g2.scale(scaleX, scaleY);
            g2.translate(-bounds.getX(), -bounds.getY());
        }
        
        if (rootNode != null) {
            rootNode.paint(g2);
        }
        g2.dispose();
        setImage(bufferedImage);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if (rootNode != null && bounds != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            
            double scaleX = (double) width / bounds.getWidth();
            double scaleY = (double) height / bounds.getHeight();
            g2.scale(scaleX, scaleY);
            g2.translate(-bounds.getX(), -bounds.getY());
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            rootNode.paint(g2);
            g2.dispose();
        } else {
            g.drawImage(bufferedImage, x, y, c);
        }
    }
}
