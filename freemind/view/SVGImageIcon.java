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
            
            // Set size from configured property, scaled by DPI for sharp rendering
            int targetSize = freemind.main.Resources.getInstance().getIntProperty(freemind.main.FreeMind.RESOURCES_TOOLBAR_ICON_SIZE, 32);
            int dpiScaledSize = (int) (targetSize * freemind.main.Tools.getScalingFactor());
            this.width = dpiScaledSize;
            this.height = dpiScaledSize;
            
            renderImage();
        } catch (Exception e) {
            freemind.main.Resources.getInstance().logException(e);
            // Fallback: try PNG in fallback/ subdirectory
            String pngUrl = url.toString().replace(".svg", ".png").replace("/icons/", "/icons/fallback/");
            try {
                java.net.URL pngFallback = new java.net.URL(pngUrl);
                java.awt.image.BufferedImage pngImg = javax.imageio.ImageIO.read(pngFallback);
                if (pngImg != null) {
                    int targetSize = freemind.main.Resources.getInstance().getIntProperty(freemind.main.FreeMind.RESOURCES_TOOLBAR_ICON_SIZE, 32);
                    this.width = targetSize;
                    this.height = targetSize;
                    this.bufferedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = bufferedImage.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(pngImg, 0, 0, targetSize, targetSize, null);
                    g2.dispose();
                    setImage(bufferedImage);
                    return;
                }
            } catch (Exception ex) {
                // No PNG fallback either
            }
            // Last resort: blank image
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
            try {
                rootNode.paint(g2);
            } catch (Exception e) {
                g2.dispose();
                throw new RuntimeException("SVG rendering failed", e);
            }
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
        // Always use the pre-rendered buffered image for consistent, fast rendering.
        // Live GVT tree rendering causes CMMException with complex SVG gradients.
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, x, y, c);
        }
    }
}
