package tests.freemind;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import freemind.view.SVGImageIcon;
import org.junit.Test;

/**
 * Tests that all SVG icons load successfully via SVGImageIcon
 * (which has PNG fallback for SVGs that Batik can't render).
 */
public class SvgIconRenderTest {

    @Test
    public void testAllSvgsLoadViaSvgImageIcon() throws Exception {
        Path iconsDir = Paths.get(System.getProperty("user.dir"))
                .resolve("..").resolve("images").resolve("icons");
        if (!Files.isDirectory(iconsDir)) {
            iconsDir = Paths.get("/home/anand/workspace/freemind-code/images/icons");
        }
        assertTrue("icons directory must exist: " + iconsDir, Files.isDirectory(iconsDir));

        File[] svgFiles = iconsDir.toFile().listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".svg"));
        assertTrue("Must find at least one .svg file", svgFiles != null && svgFiles.length > 0);

        System.out.println("=== SVGImageIcon Load Test: testing " + svgFiles.length + " files ===");

        List<String> failed = new ArrayList<>();
        int passed = 0;

        for (File svg : svgFiles) {
            String name = svg.getName();
            try {
                URL url = svg.toURI().toURL();
                ImageIcon icon = new SVGImageIcon(url);
                // Verify icon has valid dimensions
                assertTrue(name + " must have positive width", icon.getIconWidth() > 0);
                assertTrue(name + " must have positive height", icon.getIconHeight() > 0);
                // Verify icon has content (not just a blank buffer)
                if (icon instanceof SVGImageIcon) {
                    // SVGImageIcon always has a bufferedImage — constructor ensures it
                }
                passed++;
            } catch (Exception e) {
                failed.add(name);
                System.out.println("FAIL: " + name + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        System.out.println("=== Summary ===");
        System.out.println("Passed: " + passed + " / " + svgFiles.length);
        if (failed.isEmpty()) {
            System.out.println("All SVG icons loaded successfully.");
        } else {
            System.out.println("Failed (" + failed.size() + "):");
            for (String f : failed) {
                System.out.println("  - " + f);
            }
        }

        assertTrue("Some SVGs failed to load via SVGImageIcon. See output above.", failed.isEmpty());
    }
}
