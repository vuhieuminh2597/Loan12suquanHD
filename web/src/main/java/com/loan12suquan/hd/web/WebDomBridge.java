package com.loan12suquan.hd.web;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * High-performance zero-copy TeaVM {@code @JSBody} bridge to HTML5 Canvas2D, TypedArrays,
 * WebAudio, LocalStorage, and iPhone 17 touch/ProMotion VSync events.
 */
public final class WebDomBridge {

    @JSFunctor
    public interface JsRunnable extends JSObject {
        void run();
    }

    @JSFunctor
    public interface FrameCallback extends JSObject {
        void onFrame(double timestampMs);
    }

    @JSFunctor
    public interface PointerCallback extends JSObject {
        void onPointer(int kind, int logicalX, int logicalY, int wheelDelta);
    }

    @JSFunctor
    public interface KeyCallback extends JSObject {
        void onKey(int kind, int midpKey, int typedChar);
    }

    @JSFunctor
    public interface UiCommandCallback extends JSObject {
        String onCommand(String cmd, String payload);
    }

    private WebDomBridge() {
    }

    @JSBody(params = {"width", "height", "fillWhite"}, script =
            "var c = document.createElement('canvas');" +
            "c.width = width; c.height = height;" +
            "var ctx = c.getContext('2d');" +
            "if (fillWhite) { ctx.fillStyle = '#ffffff'; ctx.fillRect(0, 0, width, height); }" +
            "return c;")
    public static native JSObject createCanvasElement(int width, int height, boolean fillWhite);

    @JSBody(params = {"id"}, script = "return document.getElementById(id);")
    public static native JSObject getCanvasById(String id);

    @JSBody(params = {"canvas"}, script = "return canvas.width | 0;")
    public static native int getCanvasWidth(JSObject canvas);

    @JSBody(params = {"canvas"}, script = "return canvas.height | 0;")
    public static native int getCanvasHeight(JSObject canvas);

    @JSBody(params = {"canvas"}, script =
            "var ctx = canvas.getContext('2d');" +
            "ctx.imageSmoothingEnabled = true;" +
            "return ctx;")
    public static native JSObject getContext2D(JSObject canvas);

    /**
     * Reads {@code w * h} pixels from {@code canvas} into an unwrapped JS {@link Int32Array}
     * in {@code 0xAARRGGBB} order (avoids TeaVM's pass-by-value {@code int[]} wrapper bug!).
     */
    @JSBody(params = {"canvas", "x", "y", "w", "h"}, script =
            "var ctx = canvas.getContext('2d');" +
            "var img = ctx.getImageData(x, y, w, h);" +
            "var d = img.data;" +
            "var len = (w * h) | 0;" +
            "var out = new Int32Array(len);" +
            "var p = 0;" +
            "for (var i = 0; i < len; ++i) {" +
            "  out[i] = ((d[p + 3] << 24) | (d[p] << 16) | (d[p + 1] << 8) | d[p + 2]) | 0;" +
            "  p += 4;" +
            "}" +
            "return out;")
    public static native Int32Array readCanvasArgbTyped(JSObject canvas, int x, int y, int w, int h);

    /**
     * Writes {@code w * h} {@code 0xAARRGGBB} pixels from an unwrapped {@link Int32Array} onto {@code canvas}.
     */
    @JSBody(params = {"canvas", "x", "y", "w", "h", "srcArgb"}, script =
            "var ctx = canvas.getContext('2d');" +
            "var img = ctx.createImageData(w, h);" +
            "var d = img.data;" +
            "var len = (w * h) | 0;" +
            "var p = 0;" +
            "for (var i = 0; i < len; ++i) {" +
            "  var c = srcArgb[i];" +
            "  d[p] = (c >>> 16) & 255;" +
            "  d[p + 1] = (c >>> 8) & 255;" +
            "  d[p + 2] = c & 255;" +
            "  d[p + 3] = (c >>> 24) & 255;" +
            "  p += 4;" +
            "}" +
            "ctx.putImageData(img, x, y);")
    public static native void writeCanvasArgbTyped(
            JSObject canvas, int x, int y, int w, int h, Int32Array srcArgb);

    // --- Path2D Wrapper ---
    @JSBody(script = "return new Path2D();")
    public static native JSObject createPath2D();

    @JSBody(params = {"p", "x", "y"}, script = "p.moveTo(x, y);")
    public static native void pathMoveTo(JSObject p, double x, double y);

    @JSBody(params = {"p", "x", "y"}, script = "p.lineTo(x, y);")
    public static native void pathLineTo(JSObject p, double x, double y);

    @JSBody(params = {"p", "cx", "cy", "x", "y"}, script = "p.quadraticCurveTo(cx, cy, x, y);")
    public static native void pathQuadTo(JSObject p, double cx, double cy, double x, double y);

    @JSBody(params = {"p", "c1x", "c1y", "c2x", "c2y", "x", "y"}, script = "p.bezierCurveTo(c1x, c1y, c2x, c2y, x, y);")
    public static native void pathCurveTo(JSObject p, double c1x, double c1y, double c2x, double c2y, double x, double y);

    @JSBody(params = {"p", "cx", "cy", "r", "startRad", "endRad", "ccw"}, script = "p.arc(cx, cy, r, startRad, endRad, ccw);")
    public static native void pathArc(JSObject p, double cx, double cy, double r, double startRad, double endRad, boolean ccw);

    @JSBody(params = {"p"}, script = "p.closePath();")
    public static native void pathClose(JSObject p);

    // --- CanvasRenderingContext2D Operations ---
    @JSBody(params = {"ctx"}, script = "ctx.save();")
    public static native void ctxSave(JSObject ctx);

    @JSBody(params = {"ctx"}, script = "ctx.restore();")
    public static native void ctxRestore(JSObject ctx);

    @JSBody(params = {"ctx", "tx", "ty"}, script = "ctx.setTransform(1, 0, 0, 1, tx, ty);")
    public static native void ctxSetTranslateTransform(JSObject ctx, int tx, int ty);

    @JSBody(params = {"ctx", "tx", "ty"}, script = "ctx.translate(tx, ty);")
    public static native void ctxTranslate(JSObject ctx, double tx, double ty);

    @JSBody(params = {"ctx", "rad"}, script = "ctx.rotate(rad);")
    public static native void ctxRotate(JSObject ctx, double rad);

    @JSBody(params = {"ctx", "sx", "sy"}, script = "ctx.scale(sx, sy);")
    public static native void ctxScale(JSObject ctx, double sx, double sy);

    @JSBody(params = {"ctx", "x", "y", "w", "h"}, script =
            "ctx.beginPath(); ctx.rect(x, y, w < 0 ? 0 : w, h < 0 ? 0 : h); ctx.clip();")
    public static native void ctxClipRect(JSObject ctx, double x, double y, double w, double h);

    @JSBody(params = {"ctx", "x", "y", "w", "h"}, script =
            "ctx.beginPath();" +
            "ctx.ellipse(x + w * 0.5, y + h * 0.5, Math.max(0.1, w * 0.5), Math.max(0.1, h * 0.5), 0, 0, Math.PI * 2);" +
            "ctx.clip();")
    public static native void ctxClipOval(JSObject ctx, double x, double y, double w, double h);

    @JSBody(params = {"ctx", "p"}, script = "ctx.clip(p);")
    public static native void ctxClipPath(JSObject ctx, JSObject p);

    @JSBody(params = {"ctx", "css"}, script = "ctx.fillStyle = css; ctx.strokeStyle = css;")
    public static native void ctxSetCssPaint(JSObject ctx, String css);

    // Zero-allocation primitive 2-stop & 3-stop gradients (avoids otji_JS_wrap array cloning on every button/bar!)
    @JSBody(params = {"ctx", "x1", "y1", "x2", "y2", "c0", "c1"}, script =
            "window.__SQHD_TEXT.setLinGrad2(ctx, x1, y1, x2, y2, c0, c1);")
    public static native void ctxSetLinearGradient2(
            JSObject ctx, double x1, double y1, double x2, double y2, int c0, int c1);

    @JSBody(params = {"ctx", "x1", "y1", "x2", "y2", "s0", "c0", "s1", "c1", "s2", "c2"}, script =
            "window.__SQHD_TEXT.setLinGrad3(ctx, x1, y1, x2, y2, s0, c0, s1, c1, s2, c2);")
    public static native void ctxSetLinearGradient3(
            JSObject ctx, double x1, double y1, double x2, double y2,
            float s0, int c0, float s1, int c1, float s2, int c2);

    @JSBody(params = {"ctx", "x1", "y1", "x2", "y2", "stops", "argb"}, script =
            "if (x1 === x2 && y1 === y2) { x2 = x1 + 1; }" +
            "var g = ctx.createLinearGradient(x1, y1, x2, y2);" +
            "for (var i = 0; i < stops.length; ++i) {" +
            "  var c = argb[i];" +
            "  g.addColorStop(stops[i], window.__SQHD_TEXT.argbCss(c));" +
            "}" +
            "ctx.fillStyle = g; ctx.strokeStyle = g;")
    public static native void ctxSetLinearGradientN(
            JSObject ctx, double x1, double y1, double x2, double y2, float[] stops, int[] argb);

    @JSBody(params = {"ctx", "cx", "cy", "r", "c0", "c1"}, script =
            "window.__SQHD_TEXT.setRadGrad2(ctx, cx, cy, r, c0, c1);")
    public static native void ctxSetRadialGradient2(
            JSObject ctx, double cx, double cy, double r, int c0, int c1);

    @JSBody(params = {"ctx", "cx", "cy", "r", "s0", "c0", "s1", "c1", "s2", "c2"}, script =
            "window.__SQHD_TEXT.setRadGrad3(ctx, cx, cy, r, s0, c0, s1, c1, s2, c2);")
    public static native void ctxSetRadialGradient3(
            JSObject ctx, double cx, double cy, double r,
            float s0, int c0, float s1, int c1, float s2, int c2);

    @JSBody(params = {"ctx", "cx", "cy", "r", "stops", "argb"}, script =
            "var rad = r > 0.5 ? r : 0.5;" +
            "var g = ctx.createRadialGradient(cx, cy, 0, cx, cy, rad);" +
            "for (var i = 0; i < stops.length; ++i) {" +
            "  g.addColorStop(stops[i], window.__SQHD_TEXT.argbCss(argb[i]));" +
            "}" +
            "ctx.fillStyle = g; ctx.strokeStyle = g;")
    public static native void ctxSetRadialGradientN(
            JSObject ctx, double cx, double cy, double r, float[] stops, int[] argb);

    @JSBody(params = {"ctx", "width", "round"}, script =
            "ctx.lineWidth = width;" +
            "ctx.lineCap = round ? 'round' : 'butt';" +
            "ctx.lineJoin = round ? 'round' : 'miter';")
    public static native void ctxSetStroke(JSObject ctx, float width, boolean round);

    @JSBody(params = {"ctx", "x", "y", "w", "h"}, script = "ctx.fillRect(x, y, w, h);")
    public static native void ctxFillRect(JSObject ctx, double x, double y, double w, double h);

    @JSBody(params = {"ctx", "x", "y", "w", "h"}, script = "ctx.strokeRect(x, y, w, h);")
    public static native void ctxDrawRect(JSObject ctx, double x, double y, double w, double h);

    @JSBody(params = {"ctx", "x", "y", "w", "h", "arcDiameter", "fill"}, script =
            "if (w <= 0 || h <= 0) return;" +
            "var r = Math.max(0, Math.min(Math.min(w, h) * 0.5, arcDiameter * 0.5));" +
            "ctx.beginPath();" +
            "if (ctx.roundRect) { ctx.roundRect(x, y, w, h, r); } else {" +
            "  ctx.moveTo(x + r, y);" +
            "  ctx.arcTo(x + w, y, x + w, y + h, r);" +
            "  ctx.arcTo(x + w, y + h, x, y + h, r);" +
            "  ctx.arcTo(x, y + h, x, y, r);" +
            "  ctx.arcTo(x, y, x + w, y, r);" +
            "  ctx.closePath();" +
            "}" +
            "if (fill) ctx.fill(); else ctx.stroke();")
    public static native void ctxRoundRect(
            JSObject ctx, double x, double y, double w, double h, double arcDiameter, boolean fill);

    @JSBody(params = {"ctx", "x", "y", "w", "h", "fill"}, script =
            "if (w <= 0 || h <= 0) return;" +
            "ctx.beginPath();" +
            "ctx.ellipse(x + w * 0.5, y + h * 0.5, w * 0.5, h * 0.5, 0, 0, Math.PI * 2);" +
            "if (fill) ctx.fill(); else ctx.stroke();")
    public static native void ctxOval(JSObject ctx, double x, double y, double w, double h, boolean fill);

    @JSBody(params = {"ctx", "x1", "y1", "x2", "y2"}, script =
            "ctx.beginPath(); ctx.moveTo(x1, y1); ctx.lineTo(x2, y2); ctx.stroke();")
    public static native void ctxDrawLine(JSObject ctx, double x1, double y1, double x2, double y2);

    @JSBody(params = {"ctx", "x", "y", "w", "h", "startDeg", "arcDeg", "pie"}, script =
            "if (w <= 0 || h <= 0) return;" +
            "var cx = x + w * 0.5; var cy = y + h * 0.5;" +
            "var rx = w * 0.5; var ry = h * 0.5;" +
            "var sRad = -startDeg * Math.PI / 180.0;" +
            "var eRad = -(startDeg + arcDeg) * Math.PI / 180.0;" +
            "var ccw = arcDeg > 0;" +
            "ctx.beginPath();" +
            "if (pie) ctx.moveTo(cx, cy);" +
            "ctx.ellipse(cx, cy, rx, ry, 0, sRad, eRad, ccw);" +
            "if (pie) { ctx.closePath(); ctx.fill(); } else { ctx.stroke(); }")
    public static native void ctxArc(
            JSObject ctx, double x, double y, double w, double h, double startDeg, double arcDeg, boolean pie);

    @JSBody(params = {"ctx", "x0", "y0", "x1", "y1", "x2", "y2"}, script =
            "ctx.beginPath(); ctx.moveTo(x0, y0); ctx.lineTo(x1, y1); ctx.lineTo(x2, y2); ctx.closePath(); ctx.fill();")
    public static native void ctxFillTriangle(JSObject ctx, int x0, int y0, int x1, int y1, int x2, int y2);

    @JSBody(params = {"ctx", "xs", "ys", "count"}, script =
            "if (count < 3) return;" +
            "ctx.beginPath(); ctx.moveTo(xs[0], ys[0]);" +
            "for (var i = 1; i < count; ++i) ctx.lineTo(xs[i], ys[i]);" +
            "ctx.closePath(); ctx.fill();")
    public static native void ctxFillPolygon(JSObject ctx, int[] xs, int[] ys, int count);

    @JSBody(params = {"ctx", "p"}, script = "ctx.fill(p);")
    public static native void ctxFillPath(JSObject ctx, JSObject p);

    @JSBody(params = {"ctx", "p"}, script = "ctx.stroke(p);")
    public static native void ctxDrawPath(JSObject ctx, JSObject p);

    @JSBody(params = {"ctx", "srcCanvas", "dx", "dy", "dw", "dh"}, script =
            "ctx.drawImage(srcCanvas, dx, dy, dw, dh);")
    public static native void ctxDrawSurface(JSObject ctx, JSObject srcCanvas, double dx, double dy, double dw, double dh);

    /**
     * Hardware-accelerated 8-mode MIDP {@code Sprite.TRANS_*} region blitter on GPU Canvas2D
     * (eliminates all per-frame {@code cropAndTransform} pixel-array & {@code <canvas>} allocations!).
     */
    @JSBody(params = {"ctx", "srcCanvas", "sx", "sy", "sw", "sh", "dx", "dy", "dw", "dh", "transform"}, script =
            "if (transform === 0) {" +
            "  ctx.drawImage(srcCanvas, sx, sy, sw, sh, dx, dy, dw, dh);" +
            "  return;" +
            "}" +
            "ctx.save();" +
            "switch (transform) {" +
            "  case 1: ctx.translate(dx, dy + dh); ctx.scale(1, -1); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, dw, dh); break;" +
            "  case 2: ctx.translate(dx + dw, dy); ctx.scale(-1, 1); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, dw, dh); break;" +
            "  case 3: ctx.translate(dx + dw, dy + dh); ctx.scale(-1, -1); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, dw, dh); break;" +
            "  case 4: ctx.transform(0, 1, 1, 0, dx, dy); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, sw, sh); break;" +
            "  case 5: ctx.transform(0, 1, -1, 0, dx + dw, dy); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, sw, sh); break;" +
            "  case 6: ctx.transform(0, -1, 1, 0, dx, dy + dh); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, sw, sh); break;" +
            "  case 7: ctx.transform(0, -1, -1, 0, dx + dw, dy + dh); ctx.drawImage(srcCanvas, sx, sy, sw, sh, 0, 0, sw, sh); break;" +
            "  default: ctx.drawImage(srcCanvas, sx, sy, sw, sh, dx, dy, dw, dh); break;" +
            "}" +
            "ctx.restore();")
    public static native void ctxDrawSurfaceRegionTransformed(
            JSObject ctx, JSObject srcCanvas,
            int sx, int sy, int sw, int sh,
            int dx, int dy, int dw, int dh,
            int transform);

    @JSBody(params = {"fontKind", "text"}, script =
            "return window.__SQHD_TEXT.measureWidth(fontKind, text) | 0;")
    public static native int measureTextWidth(int fontKind, String text);

    @JSBody(params = {"fontKind"}, script = "return window.__SQHD_TEXT.getAscent(fontKind) | 0;")
    public static native int getFontAscent(int fontKind);

    @JSBody(params = {"fontKind"}, script = "return window.__SQHD_TEXT.getDescent(fontKind) | 0;")
    public static native int getFontDescent(int fontKind);

    @JSBody(params = {
            "ctx", "fontKind", "text", "drawX", "baselineY", "scaleX",
            "topRgb", "botRgb", "shadowArgb", "outlineArgb", "outlineWidth", "shadowOx", "shadowOy"
    }, script =
            "window.__SQHD_TEXT.drawStyled(ctx, fontKind, text, drawX, baselineY, scaleX," +
            " topRgb, botRgb, shadowArgb, outlineArgb, outlineWidth, shadowOx, shadowOy);")
    public static native void drawStyledText(
            JSObject ctx,
            int fontKind,
            String text,
            double drawX,
            double baselineY,
            double scaleX,
            int topRgb,
            int botRgb,
            int shadowArgb,
            int outlineArgb,
            float outlineWidth,
            double shadowOx,
            double shadowOy);

    // --- Preloaded Assets, Storage & Timers ---
    @JSBody(params = {"path"}, script =
            "if (!window.__SQHD_ASSETS || !window.__SQHD_ASSETS.images) return false;" +
            "return !!(window.__SQHD_ASSETS.images[path] || window.__SQHD_ASSETS.images[path.toLowerCase()]);")
    public static native boolean hasPreloadedImage(String path);

    @JSBody(params = {"path"}, script =
            "if (!window.__SQHD_ASSETS || !window.__SQHD_ASSETS.images) return null;" +
            "return window.__SQHD_ASSETS.images[path] || window.__SQHD_ASSETS.images[path.toLowerCase()] || null;")
    public static native JSObject getPreloadedImageCanvas(String path);

    @JSBody(params = {"path"}, script =
            "if (!window.__SQHD_ASSETS || !window.__SQHD_ASSETS.binary) return null;" +
            "var bin = window.__SQHD_ASSETS.binary[path] || window.__SQHD_ASSETS.binary[path.toLowerCase()];" +
            "return bin ? new Int8Array(bin.buffer, bin.byteOffset, bin.byteLength) : null;")
    public static native Int8Array getPreloadedBinaryTyped(String path);

    @JSBody(params = {"key"}, script =
            "try {" +
            "  var b64 = window.localStorage.getItem('sqhd_rms_' + key);" +
            "  if (!b64) return null;" +
            "  var raw = window.atob(b64);" +
            "  var out = new Int8Array(raw.length);" +
            "  for (var i = 0; i < raw.length; ++i) out[i] = raw.charCodeAt(i) & 255;" +
            "  return out;" +
            "} catch (e) { return null; }")
    public static native Int8Array loadLocalStorageBytesTyped(String key);

    @JSBody(params = {"key", "data"}, script =
            "try {" +
            "  var s = '';" +
            "  for (var i = 0; i < data.length; ++i) s += String.fromCharCode(data[i] & 255);" +
            "  window.localStorage.setItem('sqhd_rms_' + key, window.btoa(s));" +
            "} catch (e) {}")
    public static native void saveLocalStorageBytesTyped(String key, Int8Array data);

    @JSBody(params = {"key"}, script =
            "try { window.localStorage.removeItem('sqhd_rms_' + key); } catch (e) {}")
    public static native void deleteLocalStorageBytes(String key);

    @JSBody(params = {"delayMs", "cb"}, script = "window.setTimeout(function() { cb(); }, delayMs);")
    public static native void setTimeout(int delayMs, JsRunnable cb);

    @JSBody(params = {"cb"}, script = "window.requestAnimationFrame(function(ts) { cb(ts); });")
    public static native void requestAnimationFrame(FrameCallback cb);

    @JSBody(params = {"pointerCb", "keyCb", "cmdCb"}, script =
            "if (window.__SQHD_BOOT) window.__SQHD_BOOT.bindRuntime(pointerCb, keyCb, cmdCb);")
    public static native void bindRuntimeCallbacks(
            PointerCallback pointerCb, KeyCallback keyCb, UiCommandCallback cmdCb);

    @JSBody(params = {"hx", "hy"}, script =
            "if (window.__SQHD_BOOT && window.__SQHD_BOOT.spawnSparkBurst) window.__SQHD_BOOT.spawnSparkBurst(hx, hy);")
    public static native void spawnSparkBurst(int hx, int hy);

    @JSBody(params = {"dtSec"}, script =
            "if (window.__SQHD_BOOT && window.__SQHD_BOOT.stepFx) window.__SQHD_BOOT.stepFx(dtSec);")
    public static native void stepFxOverlay(double dtSec);

    // --- WebAudio Synth & MIDI Sequencer Bridge ---
    @JSBody(params = {"clipHint", "wavBytes"}, script =
            "return window.__SQHD_AUDIO.createSfxPlayer(clipHint, wavBytes);")
    public static native int createWebSfxPlayerTyped(String clipHint, Int8Array wavBytes);

    @JSBody(params = {"trackHint", "midiBytes"}, script =
            "return window.__SQHD_AUDIO.createMidiPlayer(trackHint, midiBytes);")
    public static native int createWebMidiPlayerTyped(String trackHint, Int8Array midiBytes);

    @JSBody(params = {"handle", "loopCount", "volume", "muted", "tempo", "onEnded"}, script =
            "window.__SQHD_AUDIO.startPlayer(handle, loopCount, volume, muted, tempo, function() { onEnded(); });")
    public static native void startWebPlayer(
            int handle, int loopCount, int volume, boolean muted, int tempo, JsRunnable onEnded);

    @JSBody(params = {"handle"}, script = "window.__SQHD_AUDIO.stopPlayer(handle);")
    public static native void stopWebPlayer(int handle);

    @JSBody(params = {"handle"}, script = "window.__SQHD_AUDIO.closePlayer(handle);")
    public static native void closeWebPlayer(int handle);

    @JSBody(params = {"handle", "volume", "muted"}, script =
            "window.__SQHD_AUDIO.updatePlayerVolume(handle, volume, muted);")
    public static native void updateWebPlayerVolume(int handle, int volume, boolean muted);

    @JSBody(params = {"handle", "tempo"}, script =
            "window.__SQHD_AUDIO.updatePlayerTempo(handle, tempo);")
    public static native void updateWebPlayerTempo(int handle, int tempo);
}
