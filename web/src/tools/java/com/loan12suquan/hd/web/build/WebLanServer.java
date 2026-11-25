package com.loan12suquan.hd.web.build;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Zero-dependency static HTTP server bound to {@code 0.0.0.0} so the user can test
 * <em>Loạn 12 Sứ Quân HD</em> directly on an <b>iPhone 17</b> over the same Wi-Fi LAN
 * as well as on {@code http://localhost:8080}.
 */
public final class WebLanServer {

    private WebLanServer() {
    }

    public static void main(String[] args) throws Exception {
        File webRoot = args.length > 0 ? new File(args[0]) : new File("dist/web");
        int preferredPort = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        if (!webRoot.exists() || !new File(webRoot, "index.html").exists()) {
            throw new IllegalStateException("Web distribution not found at: " + webRoot.getAbsolutePath());
        }

        final File canonicalRoot = webRoot.getCanonicalFile();
        HttpServer server = null;
        int boundPort = preferredPort;
        for (int p = preferredPort; p < preferredPort + 15; ++p) {
            try {
                server = HttpServer.create(new InetSocketAddress("0.0.0.0", p), 0);
                boundPort = p;
                break;
            } catch (IOException busy) {
                if (p == preferredPort + 14) {
                    throw busy;
                }
            }
        }

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange ex) throws IOException {
                String method = ex.getRequestMethod();
                if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                    ex.sendResponseHeaders(405, -1);
                    ex.close();
                    return;
                }

                String rawPath = ex.getRequestURI().getPath();
                if (rawPath == null || rawPath.isEmpty() || "/".equals(rawPath)) {
                    rawPath = "/index.html";
                }
                File target = new File(canonicalRoot, rawPath.substring(1)).getCanonicalFile();
                if (!target.getPath().startsWith(canonicalRoot.getPath()) || !target.isFile()) {
                    byte[] msg = "404 Not Found".getBytes("UTF-8");
                    ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                    ex.sendResponseHeaders(404, msg.length);
                    try (OutputStream os = ex.getResponseBody()) {
                        os.write(msg);
                    }
                    return;
                }

                byte[] bytes = Files.readAllBytes(target.toPath());
                ex.getResponseHeaders().set("Content-Type", guessMimeType(target.getName()));
                ex.getResponseHeaders().set("Cache-Control", "no-cache");
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

                if ("HEAD".equalsIgnoreCase(method)) {
                    ex.sendResponseHeaders(200, -1);
                    ex.close();
                    return;
                }

                ex.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        List<String> lanIps = detectLanIpv4Addresses();
        System.out.println("======================================================================");
        System.out.println("  LOAN 12 SU QUAN HD — WEB & IPHONE 17 PWA SERVER IS RUNNING!");
        System.out.println("======================================================================");
        System.out.println("  Thu muc Web : " + canonicalRoot.getAbsolutePath());
        System.out.println("  Tren PC nay : http://localhost:" + boundPort);
        if (!lanIps.isEmpty()) {
            System.out.println("  Tren iPhone 17 (cung mang Wi-Fi, mo Safari va nhap dia chi):");
            for (String ip : lanIps) {
                System.out.println("    ->  http://" + ip + ":" + boundPort);
            }
        } else {
            System.out.println("  Tren iPhone 17: dung IP Wi-Fi cua PC kem cong :" + boundPort);
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.println("  Nhan Ctrl+C (hoac nut Stop trong IntelliJ) de dung may chu.");
        System.out.println("======================================================================");

        Thread.currentThread().join();
    }

    private static List<String> detectLanIpv4Addresses() {
        List<String> result = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces != null && ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                String disp = (ni.getDisplayName() + " " + ni.getName()).toLowerCase();
                if (disp.contains("wsl") || disp.contains("hyper-v") || disp.contains("vmware") || disp.contains("virtualbox")) {
                    continue;
                }
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        result.add(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static String guessMimeType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".webmanifest")) return "application/manifest+json; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
