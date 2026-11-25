(function () {
  "use strict";

  // =========================================================================
  // 1. VIETNAMESE TRUETYPE / SF PRO HD CANVAS TYPOGRAPHY ENGINE
  // =========================================================================
  var FONT_SPECS = [
    {
      font: "700 23px -apple-system, 'SF Pro Text', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
      condenseX: 0.90,
      ascent: 19,
      descent: 5
    },
    {
      font: "700 25px -apple-system, 'SF Pro Display', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
      condenseX: 0.90,
      ascent: 21,
      descent: 6
    },
    {
      font: "800 46px 'Palatino', 'Palatino Linotype', Georgia, 'Times New Roman', serif",
      condenseX: 1.0,
      ascent: 38,
      descent: 10
    },
    {
      font: "700 19px -apple-system, 'SF Pro Text', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
      condenseX: 1.0,
      ascent: 16,
      descent: 4
    }
  ];

  var probeCanvas = document.createElement("canvas");
  probeCanvas.width = 16;
  probeCanvas.height = 16;
  var probeCtx = probeCanvas.getContext("2d");
  var widthCache = [Object.create(null), Object.create(null), Object.create(null), Object.create(null)];
  var widthCacheCount = [0, 0, 0, 0];

  // Fast offscreen bitmap cache for styled Vietnamese text (avoids per-frame strokeText & gradient creation!)
  var textSpriteCache = new Map();
  var MAX_TEXT_CACHE = 640;

  function rgbHex(rgb) {
    var s = (rgb & 0xffffff).toString(16);
    while (s.length < 6) s = "0" + s;
    return "#" + s;
  }

  function argbCss(argb) {
    var a = (argb >>> 24) & 255;
    if (a === 255) return rgbHex(argb);
    if (a === 0) return "rgba(0,0,0,0)";
    var r = (argb >>> 16) & 255;
    var g = (argb >>> 8) & 255;
    var b = argb & 255;
    return "rgba(" + r + "," + g + "," + b + "," + (a / 255.0).toFixed(3) + ")";
  }

  window.__SQHD_TEXT = {
    argbCss: argbCss,

    setLinGrad2: function (ctx, x1, y1, x2, y2, c0, c1) {
      if (x1 === x2 && y1 === y2) x2 = x1 + 1;
      var g = ctx.createLinearGradient(x1, y1, x2, y2);
      g.addColorStop(0, argbCss(c0));
      g.addColorStop(1, argbCss(c1));
      ctx.fillStyle = g;
      ctx.strokeStyle = g;
    },

    setLinGrad3: function (ctx, x1, y1, x2, y2, s0, c0, s1, c1, s2, c2) {
      if (x1 === x2 && y1 === y2) x2 = x1 + 1;
      var g = ctx.createLinearGradient(x1, y1, x2, y2);
      g.addColorStop(s0, argbCss(c0));
      g.addColorStop(s1, argbCss(c1));
      g.addColorStop(s2, argbCss(c2));
      ctx.fillStyle = g;
      ctx.strokeStyle = g;
    },

    setRadGrad2: function (ctx, cx, cy, r, c0, c1) {
      var rad = r > 0.5 ? r : 0.5;
      var g = ctx.createRadialGradient(cx, cy, 0, cx, cy, rad);
      g.addColorStop(0, argbCss(c0));
      g.addColorStop(1, argbCss(c1));
      ctx.fillStyle = g;
      ctx.strokeStyle = g;
    },

    setRadGrad3: function (ctx, cx, cy, r, s0, c0, s1, c1, s2, c2) {
      var rad = r > 0.5 ? r : 0.5;
      var g = ctx.createRadialGradient(cx, cy, 0, cx, cy, rad);
      g.addColorStop(s0, argbCss(c0));
      g.addColorStop(s1, argbCss(c1));
      g.addColorStop(s2, argbCss(c2));
      ctx.fillStyle = g;
      ctx.strokeStyle = g;
    },

    measureWidth: function (fontKind, text) {
      if (!text) return 0;
      var k = fontKind & 3;
      var cache = widthCache[k];
      var hit = cache[text];
      if (hit !== undefined) return hit;
      var spec = FONT_SPECS[k];
      probeCtx.font = spec.font;
      var w = Math.ceil(probeCtx.measureText(text).width * spec.condenseX);
      if (widthCacheCount[k] < 1536) {
        cache[text] = w;
        widthCacheCount[k]++;
      }
      return w;
    },

    getAscent: function (fontKind) {
      return FONT_SPECS[fontKind & 3].ascent;
    },

    getDescent: function (fontKind) {
      return FONT_SPECS[fontKind & 3].descent;
    },

    drawStyled: function (
      ctx, fontKind, text, drawX, baselineY, scaleX,
      topRgb, botRgb, shadowArgb, outlineArgb, outlineWidth, shadowOx, shadowOy
    ) {
      if (!text) return;
      var k = fontKind & 3;
      var key = k + "|" + topRgb + "|" + botRgb + "|" + shadowArgb + "|" + outlineArgb + "|" +
        Math.round(outlineWidth * 10) + "|" + text;

      var entry = textSpriteCache.get(key);
      if (!entry) {
        var spec = FONT_SPECS[k];
        var condensedW = window.__SQHD_TEXT.measureWidth(k, text);
        var padX = Math.ceil(outlineWidth + Math.abs(shadowOx) + 6);
        var padTop = Math.ceil(spec.ascent * 1.42 + outlineWidth + 6);
        var padBot = Math.ceil(spec.descent * 1.65 + outlineWidth + Math.abs(shadowOy) + 6);
        var bmpW = Math.max(4, condensedW + padX * 2);
        var bmpH = Math.max(4, padTop + padBot);

        var bmp = document.createElement("canvas");
        bmp.width = bmpW;
        bmp.height = bmpH;
        var gCtx = bmp.getContext("2d");
        gCtx.translate(padX, padTop);
        if (spec.condenseX !== 1.0) {
          gCtx.scale(spec.condenseX, 1.0);
        }
        gCtx.font = spec.font;
        gCtx.textBaseline = "alphabetic";
        gCtx.textAlign = "left";

        gCtx.fillStyle = argbCss(shadowArgb);
        gCtx.fillText(text, shadowOx, shadowOy);

        if (outlineWidth > 0.1) {
          gCtx.strokeStyle = argbCss(outlineArgb);
          gCtx.lineWidth = outlineWidth;
          gCtx.lineJoin = "round";
          gCtx.lineCap = "round";
          gCtx.miterLimit = 2;
          gCtx.strokeText(text, 0, 0);
        }

        var grad = gCtx.createLinearGradient(0, -spec.ascent * 0.82, 0, spec.descent * 0.45);
        grad.addColorStop(0, rgbHex(topRgb));
        grad.addColorStop(1, rgbHex(botRgb));
        gCtx.fillStyle = grad;
        gCtx.fillText(text, 0, 0);

        entry = { canvas: bmp, padX: padX, padTop: padTop, w: bmpW, h: bmpH };
        if (textSpriteCache.size >= MAX_TEXT_CACHE) {
          var oldestKey = textSpriteCache.keys().next().value;
          textSpriteCache.delete(oldestKey);
        }
        textSpriteCache.set(key, entry);
      }

      if (scaleX >= 0.999) {
        ctx.drawImage(entry.canvas, Math.round(drawX - entry.padX), Math.round(baselineY - entry.padTop));
      } else {
        ctx.drawImage(
          entry.canvas,
          drawX - entry.padX * scaleX,
          baselineY - entry.padTop,
          entry.w * scaleX,
          entry.h
        );
      }
    }
  };

  // =========================================================================
  // 2. WEBAUDIO 44.1KHZ WAV SFX + POLYPHONIC CỔ PHONG MIDI SEQUENCER
  // =========================================================================
  var AudioCtx = window.AudioContext || window.webkitAudioContext;
  var audioCtx = null;
  var audioUnlocked = false;
  var noiseBuffer = null;
  var sfxBufferCache = Object.create(null);
  var parsedMidiCache = Object.create(null);
  var players = Object.create(null);
  var nextHandle = 1;
  var activeMidiHandle = 0;

  function ensureAudioContext() {
    if (!AudioCtx) return null;
    if (!audioCtx) {
      try {
        audioCtx = new AudioCtx();
      } catch (e) {
        return null;
      }
    }
    if (!noiseBuffer && audioCtx) {
      var len = audioCtx.sampleRate;
      noiseBuffer = audioCtx.createBuffer(1, len, audioCtx.sampleRate);
      var d = noiseBuffer.getChannelData(0);
      for (var i = 0; i < len; ++i) {
        d[i] = Math.random() * 2 - 1;
      }
    }
    return audioCtx;
  }

  function unlockAudioOnGesture() {
    var ctx = ensureAudioContext();
    if (!ctx) return;
    if (ctx.state === "suspended" || ctx.state === "interrupted") {
      ctx.resume().catch(function () {});
    }
    if (!audioUnlocked && ctx.state === "running") {
      audioUnlocked = true;
      try {
        var silent = ctx.createBuffer(1, 1, 22050);
        var src = ctx.createBufferSource();
        src.buffer = silent;
        src.connect(ctx.destination);
        src.start(0);
      } catch (e) {}
    }
    // If a MIDI BGM track was started before iOS unlocked WebAudio, kick its scheduler
    if (activeMidiHandle && players[activeMidiHandle]) {
      var p = players[activeMidiHandle];
      if (p.playing && p.kind === "midi" && ctx.state === "running" && p.waitingForUnlock) {
        p.waitingForUnlock = false;
        p.baseAudioTime = ctx.currentTime + 0.04;
        p.nextNoteIndex = 0;
      }
    }
  }

  ["touchstart", "touchend", "pointerdown", "mousedown", "keydown"].forEach(function (ev) {
    window.addEventListener(ev, unlockAudioOnGesture, { passive: true, capture: true });
  });

  document.addEventListener("visibilitychange", function () {
    if (!audioCtx) return;
    if (document.hidden) {
      if (audioCtx.state === "running") {
        audioCtx.suspend().catch(function () {});
      }
    } else {
      audioCtx.resume().catch(function () {});
    }
  });

  // --- Standard MIDI File (SMF Format 0 & 1) Parser ---
  function parseSmfMidi(bytes) {
    if (!bytes || bytes.length < 18) {
      return { notes: [], duration: 1.0, baseBpm: 120 };
    }
    var u8 = new Uint8Array(bytes.length);
    for (var k = 0; k < bytes.length; ++k) u8[k] = bytes[k] & 255;

    function readU16(pos) {
      return (u8[pos] << 8) | u8[pos + 1];
    }
    function readU32(pos) {
      return ((u8[pos] << 24) >>> 0) + ((u8[pos + 1] << 16) | (u8[pos + 2] << 8) | u8[pos + 3]);
    }

    if (u8[0] !== 0x4d || u8[1] !== 0x54 || u8[2] !== 0x68 || u8[3] !== 0x64) {
      return { notes: [], duration: 1.0, baseBpm: 120 };
    }
    var hdrLen = readU32(4);
    var numTracks = readU16(10);
    var division = readU16(12) || 480;
    var pos = 8 + hdrLen;

    var rawNotes = [];
    var tempoEvents = [{ tick: 0, usPerBeat: 500000 }];
    var maxTick = 0;

    for (var tr = 0; tr < numTracks && pos + 8 <= u8.length; ++tr) {
      if (u8[pos] !== 0x4d || u8[pos + 1] !== 0x54 || u8[pos + 2] !== 0x72 || u8[pos + 3] !== 0x6b) {
        break;
      }
      var trkLen = readU32(pos + 4);
      var p = pos + 8;
      var end = Math.min(u8.length, p + trkLen);
      var tick = 0;
      var runningStatus = 0;
      var chProg = new Uint8Array(16);
      var openNotes = Object.create(null);

      while (p < end) {
        var delta = 0;
        while (p < end) {
          var b = u8[p++];
          delta = (delta << 7) | (b & 0x7f);
          if ((b & 0x80) === 0) break;
        }
        tick += delta;
        if (tick > maxTick) maxTick = tick;
        if (p >= end) break;

        var status = u8[p];
        if (status < 0x80) {
          status = runningStatus;
        } else {
          p++;
          if (status < 0xf0) runningStatus = status;
        }

        if (status === 0xff) {
          var metaType = u8[p++];
          var mLen = 0;
          while (p < end) {
            var mb = u8[p++];
            mLen = (mLen << 7) | (mb & 0x7f);
            if ((mb & 0x80) === 0) break;
          }
          if (metaType === 0x51 && mLen === 3 && p + 3 <= end) {
            var us = (u8[p] << 16) | (u8[p + 1] << 8) | u8[p + 2];
            if (us > 0) tempoEvents.push({ tick: tick, usPerBeat: us });
          }
          p += mLen;
        } else if (status === 0xf0 || status === 0xf7) {
          var sLen = 0;
          while (p < end) {
            var sb = u8[p++];
            sLen = (sLen << 7) | (sb & 0x7f);
            if ((sb & 0x80) === 0) break;
          }
          p += sLen;
        } else {
          var high = status & 0xf0;
          var ch = status & 0x0f;
          if (high === 0x90 || high === 0x80) {
            var note = u8[p++] & 0x7f;
            var vel = u8[p++] & 0x7f;
            var key = (ch << 8) | note;
            if (high === 0x90 && vel > 0) {
              if (openNotes[key]) {
                var prev = openNotes[key];
                rawNotes.push({
                  startTick: prev.tick,
                  endTick: Math.max(prev.tick + 1, tick),
                  midi: note,
                  vel: prev.vel,
                  ch: ch,
                  prog: prev.prog
                });
              }
              openNotes[key] = { tick: tick, vel: vel, prog: chProg[ch] };
            } else if (openNotes[key]) {
              var st = openNotes[key];
              delete openNotes[key];
              rawNotes.push({
                startTick: st.tick,
                endTick: Math.max(st.tick + 1, tick),
                midi: note,
                vel: st.vel,
                ch: ch,
                prog: st.prog
              });
            }
          } else if (high === 0xc0) {
            chProg[ch] = u8[p++] & 0x7f;
          } else if (high === 0xd0) {
            p += 1;
          } else if (high === 0xa0 || high === 0xb0 || high === 0xe0) {
            p += 2;
          }
        }
      }

      for (var remKey in openNotes) {
        var rem = openNotes[remKey];
        rawNotes.push({
          startTick: rem.tick,
          endTick: Math.max(rem.tick + 120, maxTick),
          midi: parseInt(remKey, 10) & 0xff,
          vel: rem.vel,
          ch: (parseInt(remKey, 10) >> 8) & 0x0f,
          prog: rem.prog
        });
      }
      pos = end;
    }

    tempoEvents.sort(function (a, b) {
      return a.tick - b.tick;
    });

    function tickToSec(targetTick) {
      var sec = 0.0;
      var curTick = 0;
      var curUs = tempoEvents[0].usPerBeat;
      for (var i = 1; i < tempoEvents.length; ++i) {
        var te = tempoEvents[i];
        if (te.tick >= targetTick) break;
        sec += ((te.tick - curTick) / division) * (curUs / 1000000.0);
        curTick = te.tick;
        curUs = te.usPerBeat;
      }
      sec += ((targetTick - curTick) / division) * (curUs / 1000000.0);
      return sec;
    }

    var notes = rawNotes.map(function (n) {
      var s0 = tickToSec(n.startTick);
      var s1 = tickToSec(n.endTick);
      return {
        time: s0,
        dur: Math.max(0.06, Math.min(2.8, s1 - s0)),
        midi: n.midi,
        vel: n.vel / 127.0,
        ch: n.ch,
        prog: n.prog
      };
    });
    notes.sort(function (a, b) {
      return a.time - b.time;
    });

    var baseBpm = Math.round(60000000 / tempoEvents[0].usPerBeat);
    var totalDur = Math.max(0.5, tickToSec(maxTick) + 0.15);
    return { notes: notes, duration: totalDur, baseBpm: baseBpm || 120 };
  }

  // --- Polyphonic Cổ Phong Instrument Synthesizer ---
  function triggerMidiNote(ctx, destNode, note, when, speedScale) {
    var dur = Math.max(0.05, note.dur / speedScale);
    var gainVal = Math.min(0.28, (0.06 + note.vel * 0.18));

    // Channel 10 Percussion (Đông Sơn War Drum & Cymbals)
    if (note.ch === 9) {
      if (note.midi <= 40 || note.midi === 45 || note.midi === 47) {
        var drumOsc = ctx.createOscillator();
        var drumGain = ctx.createGain();
        drumOsc.type = "sine";
        var startF = note.midi <= 36 ? 135 : 195;
        drumOsc.frequency.setValueAtTime(startF, when);
        drumOsc.frequency.exponentialRampToValueAtTime(42, when + 0.14);
        drumGain.gain.setValueAtTime(gainVal * 1.35, when);
        drumGain.gain.exponentialRampToValueAtTime(0.001, when + 0.18);
        drumOsc.connect(drumGain);
        drumGain.connect(destNode);
        drumOsc.start(when);
        drumOsc.stop(when + 0.19);
      } else if (noiseBuffer) {
        var nSrc = ctx.createBufferSource();
        nSrc.buffer = noiseBuffer;
        var hp = ctx.createBiquadFilter();
        hp.type = "highpass";
        hp.frequency.value = note.midi >= 49 ? 4200 : 6500;
        var nGain = ctx.createGain();
        var hatDur = note.midi >= 49 ? 0.28 : 0.07;
        nGain.gain.setValueAtTime(gainVal * 0.55, when);
        nGain.gain.exponentialRampToValueAtTime(0.001, when + hatDur);
        nSrc.connect(hp);
        hp.connect(nGain);
        nGain.connect(destNode);
        nSrc.start(when);
        nSrc.stop(when + hatDur + 0.01);
      }
      return;
    }

    var freq = 440.0 * Math.pow(2.0, (note.midi - 69) / 12.0);
    var isFluteOrLead = (note.prog >= 64 && note.prog <= 87) || (note.prog >= 40 && note.prog <= 55);

    var osc1 = ctx.createOscillator();
    var osc2 = ctx.createOscillator();
    var filter = ctx.createBiquadFilter();
    var env = ctx.createGain();

    if (isFluteOrLead) {
      // Bamboo Flute / Silk Strings sustain voice
      osc1.type = "triangle";
      osc2.type = "sine";
      osc1.frequency.setValueAtTime(freq, when);
      osc2.frequency.setValueAtTime(freq * 2.0, when);
      filter.type = "lowpass";
      filter.frequency.setValueAtTime(Math.min(4800, freq * 4.5), when);

      var atk = Math.min(0.035, dur * 0.25);
      env.gain.setValueAtTime(0.0001, when);
      env.gain.linearRampToValueAtTime(gainVal * 0.85, when + atk);
      env.gain.setValueAtTime(gainVal * 0.72, when + Math.max(atk, dur * 0.7));
      env.gain.exponentialRampToValueAtTime(0.0008, when + dur + 0.08);
    } else {
      // Plucked Guzheng / Đàn Tranh & Royal Bronze Harp voice
      osc1.type = "triangle";
      osc2.type = "sawtooth";
      osc1.frequency.setValueAtTime(freq, when);
      osc2.frequency.setValueAtTime(freq, when);
      osc2.detune.setValueAtTime(4.5, when);

      filter.type = "lowpass";
      filter.frequency.setValueAtTime(Math.min(6200, freq * 6.5), when);
      filter.frequency.exponentialRampToValueAtTime(Math.max(380, freq * 1.8), when + Math.min(0.45, dur + 0.12));

      var pluckDur = Math.max(dur, 0.22);
      env.gain.setValueAtTime(0.0001, when);
      env.gain.linearRampToValueAtTime(gainVal, when + 0.008);
      env.gain.exponentialRampToValueAtTime(0.0008, when + pluckDur + 0.10);
      dur = pluckDur;
    }

    var subGain = ctx.createGain();
    subGain.gain.value = isFluteOrLead ? 0.22 : 0.28;

    osc1.connect(filter);
    osc2.connect(subGain);
    subGain.connect(filter);
    filter.connect(env);
    env.connect(destNode);

    osc1.start(when);
    osc2.start(when);
    osc1.stop(when + dur + 0.12);
    osc2.stop(when + dur + 0.12);
  }

  function effectiveGain(volume, muted) {
    if (muted || volume <= 0) return 0.0;
    var norm = Math.max(0, Math.min(100, volume)) / 100.0;
    return norm * norm * 0.9;
  }

  // Global 50ms MIDI lookahead scheduler
  setInterval(function () {
    if (!activeMidiHandle || !audioCtx || audioCtx.state !== "running") return;
    var p = players[activeMidiHandle];
    if (!p || !p.playing || p.kind !== "midi") return;

    var song = p.song;
    if (!song || !song.notes.length) return;

    var speed = p.speedScale || 1.0;
    var scaledDur = song.duration / speed;
    var now = audioCtx.currentTime;
    var horizon = now + 0.14;

    if (p.waitingForUnlock) {
      p.waitingForUnlock = false;
      p.baseAudioTime = now + 0.02;
      p.nextNoteIndex = 0;
    }

    var safety = 0;
    while (p.nextNoteIndex < song.notes.length && safety < 64) {
      var n = song.notes[p.nextNoteIndex];
      var noteWhen = p.baseAudioTime + n.time / speed;
      if (noteWhen > horizon) break;
      if (noteWhen >= now - 0.05) {
        triggerMidiNote(audioCtx, p.gainNode, n, Math.max(now, noteWhen), speed);
      }
      p.nextNoteIndex++;
      safety++;
    }

    if (p.nextNoteIndex >= song.notes.length && now >= p.baseAudioTime + scaledDur) {
      if (p.loopCount === -1 || p.remainingLoops > 1) {
        if (p.remainingLoops > 1) p.remainingLoops--;
        p.baseAudioTime = now + 0.04;
        p.nextNoteIndex = 0;
      } else {
        p.playing = false;
        if (activeMidiHandle === p.handle) activeMidiHandle = 0;
        if (p.onEnded) {
          var cb = p.onEnded;
          p.onEnded = null;
          setTimeout(cb, 0);
        }
      }
    }
  }, 50);

  window.__SQHD_AUDIO = {
    createSfxPlayer: function (clipHint, wavBytes) {
      var id = nextHandle++;
      var key = clipHint || "sfx_" + id;
      players[id] = {
        handle: id,
        kind: "sfx",
        key: key,
        wavBytes: wavBytes,
        playing: false,
        activeSource: null,
        gainNode: null,
        volume: 80,
        muted: false
      };
      var ctx = ensureAudioContext();
      if (ctx && !sfxBufferCache[key] && wavBytes && wavBytes.length > 44) {
        var ab = new ArrayBuffer(wavBytes.length);
        var u8 = new Uint8Array(ab);
        for (var i = 0; i < wavBytes.length; ++i) u8[i] = wavBytes[i] & 255;
        ctx.decodeAudioData(
          ab,
          function (decoded) {
            sfxBufferCache[key] = decoded;
          },
          function () {}
        );
      }
      return id;
    },

    createMidiPlayer: function (trackHint, midiBytes) {
      var id = nextHandle++;
      var key = trackHint || "midi_" + id;
      if (!parsedMidiCache[key]) {
        parsedMidiCache[key] = parseSmfMidi(midiBytes);
      }
      players[id] = {
        handle: id,
        kind: "midi",
        key: key,
        song: parsedMidiCache[key],
        playing: false,
        waitingForUnlock: false,
        gainNode: null,
        volume: 80,
        muted: false,
        loopCount: 1,
        remainingLoops: 1,
        speedScale: 1.0,
        baseAudioTime: 0,
        nextNoteIndex: 0,
        onEnded: null
      };
      return id;
    },

    startPlayer: function (handle, loopCount, volume, muted, tempoMillibeats, onEnded) {
      var p = players[handle];
      if (!p) return;
      var ctx = ensureAudioContext();
      if (!ctx) return;

      p.volume = volume;
      p.muted = muted;

      if (p.kind === "sfx") {
        if (ctx.state !== "running") return;
        var playBuf = function (buf) {
          if (!buf) return;
          try {
            if (p.activeSource) {
              try { p.activeSource.stop(); } catch (e) {}
            }
            var src = ctx.createBufferSource();
            var gain = ctx.createGain();
            gain.gain.value = effectiveGain(p.volume, p.muted);
            src.buffer = buf;
            src.loop = loopCount === -1;
            src.connect(gain);
            gain.connect(ctx.destination);
            p.activeSource = src;
            p.gainNode = gain;
            p.playing = true;
            src.onended = function () {
              p.playing = false;
              if (onEnded) onEnded();
            };
            src.start(0);
          } catch (e) {}
        };

        if (sfxBufferCache[p.key]) {
          playBuf(sfxBufferCache[p.key]);
        } else if (p.wavBytes && p.wavBytes.length > 44) {
          var ab = new ArrayBuffer(p.wavBytes.length);
          var u8 = new Uint8Array(ab);
          for (var i = 0; i < p.wavBytes.length; ++i) u8[i] = p.wavBytes[i] & 255;
          ctx.decodeAudioData(
            ab,
            function (decoded) {
              sfxBufferCache[p.key] = decoded;
              playBuf(decoded);
            },
            function () {}
          );
        }
        return;
      }

      // MIDI BGM or Jingle
      if (activeMidiHandle && activeMidiHandle !== handle && players[activeMidiHandle]) {
        window.__SQHD_AUDIO.stopPlayer(activeMidiHandle);
      }
      if (!p.gainNode) {
        p.gainNode = ctx.createGain();
        p.gainNode.connect(ctx.destination);
      }
      p.gainNode.gain.setValueAtTime(effectiveGain(volume, muted), ctx.currentTime);
      p.loopCount = loopCount;
      p.remainingLoops = loopCount > 0 ? loopCount : 1;
      var reqBpm = tempoMillibeats > 1000 ? tempoMillibeats / 1000.0 : 120.0;
      var baseBpm = (p.song && p.song.baseBpm) ? p.song.baseBpm : 120.0;
      p.speedScale = Math.max(0.65, Math.min(1.65, reqBpm / baseBpm));
      p.nextNoteIndex = 0;
      p.onEnded = onEnded || null;
      p.playing = true;
      activeMidiHandle = handle;

      if (ctx.state === "running") {
        p.waitingForUnlock = false;
        p.baseAudioTime = ctx.currentTime + 0.03;
      } else {
        p.waitingForUnlock = true;
      }
    },

    stopPlayer: function (handle) {
      var p = players[handle];
      if (!p) return;
      p.playing = false;
      p.onEnded = null;
      if (p.kind === "sfx" && p.activeSource) {
        try { p.activeSource.stop(); } catch (e) {}
        p.activeSource = null;
      }
      if (p.kind === "midi") {
        if (p.gainNode && audioCtx) {
          try {
            p.gainNode.disconnect();
          } catch (e) {}
          p.gainNode = null;
        }
        if (activeMidiHandle === handle) activeMidiHandle = 0;
      }
    },

    closePlayer: function (handle) {
      window.__SQHD_AUDIO.stopPlayer(handle);
      delete players[handle];
    },

    updatePlayerVolume: function (handle, volume, muted) {
      var p = players[handle];
      if (!p) return;
      p.volume = volume;
      p.muted = muted;
      if (p.gainNode && audioCtx) {
        p.gainNode.gain.setValueAtTime(effectiveGain(volume, muted), audioCtx.currentTime);
      }
    },

    updatePlayerTempo: function (handle, tempoMillibeats) {
      var p = players[handle];
      if (!p || p.kind !== "midi") return;
      var reqBpm = tempoMillibeats > 1000 ? tempoMillibeats / 1000.0 : 120.0;
      var baseBpm = (p.song && p.song.baseBpm) ? p.song.baseBpm : 120.0;
      p.speedScale = Math.max(0.65, Math.min(1.65, reqBpm / baseBpm));
    },

    resumeIfSuspended: unlockAudioOnGesture
  };
})();
