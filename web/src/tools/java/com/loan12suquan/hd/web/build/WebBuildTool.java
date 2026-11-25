package com.loan12suquan.hd.web.build;

import com.loan12suquan.hd.HdGemGenerator;
import com.loan12suquan.hd.desktop.DesktopPlatformBackend;
import com.loan12suquan.hd.desktop.DesktopSurface;
import com.loan12suquan.hd.platform.HdSurface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import org.teavm.diagnostics.Problem;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMProblemRenderer;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

/**
 * Gradle build-time tool that:
 * 1. Compiles {@code com.loan12suquan.hd.web.WebLauncher} + shared HD game code to {@code dist/web/js/classes.js} via TeaVM.
 * 2. Packs all 151 game resources ({@code .png}, {@code .mid}, {@code .amr}) into a single {@code dist/web/assets-pack.json}.
 * 3. Renders crisp Apple Touch Icon ({@code 180x180}) & PWA Icons ({@code 192x192}, {@code 512x512}, {@code 64x64}) for iPhone 17 Home Screen.
 */
public final class WebBuildTool {

    private WebBuildTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("Expected <webClassesPath> <resourcesDir> <distWebDir>");
        }
        String webClassesPath = args[0];
        File resourcesDir = new File(args[1]);
        File distWebDir = new File(args[2]);
        File jsOutDir = new File(distWebDir, "js");
        File iconsOutDir = new File(distWebDir, "icons");
        jsOutDir.mkdirs();
        iconsOutDir.mkdirs();

        compileTeaVmBundle(webClassesPath, jsOutDir);
        generateAssetsPackJson(resourcesDir, new File(distWebDir, "assets-pack.json"));
        generatePwaIcons(iconsOutDir);
    }

    private static void compileTeaVmBundle(String webClassesPath, File jsOutDir) throws Exception {
        System.out.println("[Loan12SuQuanHD-Web] Compiling Java -> JavaScript via TeaVM...");
        String[] pathEntries = webClassesPath.split(File.pathSeparator);
        List<URL> urls = new ArrayList<URL>();
        for (int i = 0; i < pathEntries.length; ++i) {
            if (!pathEntries[i].trim().isEmpty()) {
                File f = new File(pathEntries[i]);
                if (f.exists()) {
                    urls.add(f.toURI().toURL());
                }
            }
        }

        URLClassLoader appClassLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                WebBuildTool.class.getClassLoader());

        TeaVMTool tool = new TeaVMTool();
        tool.setClassLoader(appClassLoader);
        tool.setTargetType(TeaVMTargetType.JAVASCRIPT);
        tool.setTargetDirectory(jsOutDir);
        tool.setTargetFileName("classes.js");
        tool.setMainClass("com.loan12suquan.hd.web.WebLauncher");
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        tool.setObfuscated(false);
        tool.setLog(new ConsoleTeaVMToolLog(false));
        tool.generate();

        List<Problem> severe = tool.getProblemProvider().getSevereProblems();
        if (severe != null && !severe.isEmpty()) {
            TeaVMProblemRenderer.describeProblems(tool.getDependencyInfo().getCallGraph(), tool.getProblemProvider(), new ConsoleTeaVMToolLog(true));
            throw new IllegalStateException("TeaVM compilation failed with " + severe.size() + " severe problem(s)!");
        }
        File outJs = new File(jsOutDir, "classes.js");
        System.out.println("[Loan12SuQuanHD-Web] Generated " + outJs.getAbsolutePath() + " (" + (outJs.length() / 1024) + " KB)");
    }

    private static void generateAssetsPackJson(File resourcesDir, File outJson) throws IOException {
        Path root = resourcesDir.toPath();
        List<Path> files = new ArrayList<Path>();
        Files.walk(root).filter(Files::isRegularFile).forEach(files::add);
        Collections.sort(files);

        StringBuilder imagesJson = new StringBuilder(300_000);
        StringBuilder binaryJson = new StringBuilder(80_000);
        int imgCount = 0;
        int binCount = 0;

        for (Path p : files) {
            String rel = "/" + root.relativize(p).toString().replace('\\', '/');
            if (rel.startsWith("/META-INF/")) {
                continue;
            }
            byte[] bytes = Files.readAllBytes(p);
            String b64 = Base64.getEncoder().encodeToString(bytes);
            String lower = rel.toLowerCase();
            if (lower.endsWith(".png")) {
                if (imgCount > 0) {
                    imagesJson.append(",\n");
                }
                imagesJson.append("    \"").append(escapeJson(rel)).append("\": \"").append(b64).append("\"");
                ++imgCount;
            } else {
                if (binCount > 0) {
                    binaryJson.append(",\n");
                }
                binaryJson.append("    \"").append(escapeJson(rel)).append("\": \"").append(b64).append("\"");
                ++binCount;
            }
        }

        String fullJson = "{\n  \"version\": \"2.0.0-HD\",\n  \"images\": {\n"
                + imagesJson
                + "\n  },\n  \"binary\": {\n"
                + binaryJson
                + "\n  }\n}\n";
        Files.write(outJson.toPath(), fullJson.getBytes(StandardCharsets.UTF_8));
        System.out.println("[Loan12SuQuanHD-Web] Packed " + imgCount + " PNGs + " + binCount + " audio files into "
                + outJson.getName() + " (" + (outJson.length() / 1024) + " KB)");
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void generatePwaIcons(File iconsDir) throws IOException {
        DesktopPlatformBackend.installIfNeeded();
        HdSurface swordSurf = HdGemGenerator.createHdGem("/swordred", null);
        BufferedImage gemImg = ((DesktopSurface) swordSurf).getBufferedImage();

        writeAppIcon(new File(iconsDir, "apple-touch-icon.png"), 180, gemImg, false);
        writeAppIcon(new File(iconsDir, "icon-192.png"), 192, gemImg, true);
        writeAppIcon(new File(iconsDir, "icon-512.png"), 512, gemImg, true);
        writeAppIcon(new File(iconsDir, "favicon.png"), 64, gemImg, true);
        System.out.println("[Loan12SuQuanHD-Web] Generated iPhone 17 Apple Touch Icon & PWA icons in " + iconsDir.getAbsolutePath());
    }

    private static void writeAppIcon(File out, int size, BufferedImage centerGem, boolean roundedCorners) throws IOException {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        float arc = roundedCorners ? size * 0.22f : 0f;
        RoundRectangle2D.Float bgShape = new RoundRectangle2D.Float(0, 0, size, size, arc, arc);
        g2.setPaint(new LinearGradientPaint(
                0, 0, 0, size,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(0x5E1418), new Color(0x2E0B0E), new Color(0x140608)}));
        if (roundedCorners) {
            g2.fill(bgShape);
            g2.setClip(bgShape);
        } else {
            g2.fillRect(0, 0, size, size);
        }

        // Golden imperial radial aura
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(size * 0.5f, size * 0.42f), size * 0.52f,
                new float[]{0f, 0.7f, 1f},
                new Color[]{new Color(255, 215, 95, 115), new Color(190, 95, 25, 40), new Color(0, 0, 0, 0)}));
        g2.fillRect(0, 0, size, size);

        // Ornate burnished gold inner frame
        float m = Math.max(3f, size * 0.055f);
        g2.setPaint(new GradientPaint(0, m, new Color(0xFFF5B8), 0, size - m, new Color(0xA67322)));
        g2.setStroke(new BasicStroke(Math.max(2f, size * 0.028f)));
        g2.draw(new RoundRectangle2D.Float(m, m, size - m * 2f, size - m * 2f, size * 0.16f, size * 0.16f));

        // Center iconic Loạn 12 Sứ Quân Fiery Broadsword Gem
        int gemSize = (int) (size * (size >= 180 ? 0.56f : 0.72f));
        int gx = (size - gemSize) / 2;
        int gy = size >= 180 ? (int) (size * 0.11f) : (size - gemSize) / 2;
        g2.drawImage(centerGem, gx, gy, gemSize, gemSize, null);

        if (size >= 180) {
            Font font = new Font("Serif", Font.BOLD, Math.round(size * 0.145f));
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String label = "12 SỨ QUÂN";
            int tx = (size - fm.stringWidth(label)) / 2;
            int ty = (int) (size * 0.85f);
            g2.setColor(new Color(20, 6, 4, 230));
            g2.drawString(label, tx + 2, ty + 2);
            g2.setPaint(new GradientPaint(0, ty - fm.getAscent(), new Color(0xFFF9C4), 0, ty, new Color(0xE6A12E)));
            g2.drawString(label, tx, ty);
        }

        g2.dispose();
        ImageIO.write(img, "png", out);
    }
}
