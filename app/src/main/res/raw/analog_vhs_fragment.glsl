#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

uniform samplerExternalOES uCameraTex;
uniform sampler2D uOsdTex;      // Pre-encoder Fujitsu MB88303 OSD character layer
uniform float uTime;
uniform vec2 uResolution;
uniform float uZoom;

// Orientation & Source Routing
uniform int uSensorRot;         // 0=0, 1=90 CW (default rear cam), 2=180, 3=270 CW
uniform int uMirrorX;           // 1=front selfie mirror, 0=normal
uniform int uSourceMode;        // 0=Camera/Video, 1=SMPTE Test Card, 2='85 Outrun Synth

// Broadcast & VCR Hardware Switches
uniform int uStandard;          // 0=NTSC, 1=PAL, 2=SECAM, 3=405-Line Mono
uniform int uTubeMode;          // 0=Vidicon, 1=Plumbicon, 2=Orthicon, 3=Early CCD
uniform int uPhosphorMode;      // 0=P22 Colour, 1=P31 Green, 2=P4 White, 3=P3 Amber
uniform int uMaskMode;          // 0=Aperture Grille, 1=Shadow Mask, 2=Slot Mask
uniform int uIsPxl2000;         // 1=Fisher-Price PXL-2000 120x90 mode
uniform int uTransportMode;     // 0=Play, 1=Pause, 2=Cue x5, 3=Rev x5
uniform float uTbcAtten;        // 0.18 when 4fsc TBC locked, 1.0 when raw CVBS

// Analog Signal Sliders (0..1)
uniform float uChromaBleed;
uniform float uFlagging;
uniform float uTapeNoise;
uniform float uFlutter;
uniform float uTracking;
uniform float uEdgeRinging;
uniform float uDotCrawl;
uniform float uSnow;
uniform float uGhosting;
uniform float uHanover;
uniform float uVHold;
uniform float uMainsHum;
uniform float uTubeLag;
uniform float uTubeReg;
uniform float uScanlines;
uniform float uCurvature;
uniform float uBloom;
uniform float uCrtContrast;

// Stompboxes & Overlays
uniform float uStompScramble;
uniform float uStompImpulse;
uniform float uDegaussPhase;
uniform float uShutdownProg;    // 0=On, 0..1=CRT EHT vertical/horizontal collapse
uniform float uWipePos;         // <0 = disabled, 0.05..0.95 = Before/After split wipe

in vec2 vTexCoord;
out vec4 fragColor;

float hash11(float n) {
    return fract(sin(n * 127.1 + 311.7) * 43758.5453123);
}

float hash12(vec2 p) {
    vec3 p3  = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise1D(float x) {
    float i = floor(x);
    float f = fract(x);
    float u = f * f * (3.0 - 2.0 * f);
    return mix(hash11(i), hash11(i + 1.0), u) * 2.0 - 1.0;
}

vec3 rgb2yiq(vec3 c) {
    return vec3(
        0.299 * c.r + 0.587 * c.g + 0.114 * c.b,
        0.596 * c.r - 0.274 * c.g - 0.322 * c.b,
        0.211 * c.r - 0.523 * c.g + 0.312 * c.b
    );
}

vec3 yiq2rgb(vec3 yiq) {
    return clamp(vec3(
        yiq.x + 0.956 * yiq.y + 0.621 * yiq.z,
        yiq.x - 0.272 * yiq.y - 0.647 * yiq.z,
        yiq.x - 1.106 * yiq.y + 1.703 * yiq.z
    ), 0.0, 1.0);
}

// Procedural SMPTE Test Card + Multiburst Frequency Grating
vec3 evalTestCard(vec2 uv, float t) {
    if (uv.y < 0.62) {
        int b = int(clamp(floor(uv.x * 7.0), 0.0, 6.0));
        if (b == 0) return vec3(0.82, 0.82, 0.82);
        if (b == 1) return vec3(0.85, 0.85, 0.05);
        if (b == 2) return vec3(0.05, 0.85, 0.85);
        if (b == 3) return vec3(0.05, 0.82, 0.05);
        if (b == 4) return vec3(0.82, 0.05, 0.82);
        if (b == 5) return vec3(0.85, 0.05, 0.05);
        return vec3(0.05, 0.05, 0.85);
    } else if (uv.y < 0.70) {
        int b = int(clamp(floor(uv.x * 7.0), 0.0, 6.0));
        if (b == 0) return vec3(0.05, 0.05, 0.85);
        if (b == 2) return vec3(0.82, 0.05, 0.82);
        if (b == 4) return vec3(0.05, 0.85, 0.85);
        if (b == 6) return vec3(0.82, 0.82, 0.82);
        return vec3(0.08, 0.08, 0.08);
    } else {
        if (uv.x < 0.60) {
            float freq = mix(40.0, 260.0, uv.x / 0.60);
            float v = 0.5 + 0.45 * sin(uv.x * freq + t * 2.0);
            return vec3(v);
        } else if (uv.x < 0.78) {
            return vec3(0.95);
        } else {
            return vec3(0.04);
        }
    }
}

// Procedural 1985 Synthwave Outrun Night Drive
vec3 evalSynthwave(vec2 uv, float t) {
    if (uv.y < 0.56) {
        vec3 sky = mix(vec3(0.04, 0.02, 0.12), vec3(0.75, 0.10, 0.38), uv.y / 0.56);
        vec2 sc = (uv - vec2(0.5, 0.36)) * vec2(1.33, 1.0);
        float d = length(sc);
        if (d < 0.22) {
            float stripe = step(0.15, fract(uv.y * 28.0 - t * 0.4));
            vec3 sunCol = mix(vec3(1.0, 0.95, 0.45), vec3(0.92, 0.12, 0.35), (uv.y - 0.14) / 0.42);
            if (uv.y < 0.34 || stripe > 0.5) sky = sunCol;
        }
        return sky;
    } else {
        float gy = (uv.y - 0.56) / 0.44;
        float persp = (uv.x - 0.5) / max(0.06, gy);
        float gridX = abs(fract(persp * 1.6 + 0.5) - 0.5);
        float gridY = abs(fract(1.2 / max(0.05, gy) - t * 2.4) - 0.5);
        float line = smoothstep(0.06, 0.01, min(gridX, gridY * 0.6));
        return mix(vec3(0.04, 0.02, 0.09), vec3(0.05, 0.85, 0.95), line);
    }
}

// Samples upright camera/video texture + pre-encoder OSD character layer
vec3 sampleRawSource(vec2 screenUv) {
    vec2 uv = clamp(screenUv, 0.001, 0.999);
    vec3 baseRgb;
    if (uSourceMode == 1) {
        baseRgb = evalTestCard(uv, uTime);
    } else if (uSourceMode == 2) {
        baseRgb = evalSynthwave(uv, uTime);
    } else {
        vec2 cuv = uv;
        if (uMirrorX == 1) {
            cuv.x = 1.0 - cuv.x;
        }
        vec2 rotUv;
        if (uSensorRot == 1) {
            // 90 deg clockwise upright correction for Android rear camera in portrait
            rotUv = vec2(cuv.y, 1.0 - cuv.x);
        } else if (uSensorRot == 2) {
            rotUv = vec2(1.0 - cuv.x, 1.0 - cuv.y);
        } else if (uSensorRot == 3) {
            rotUv = vec2(1.0 - cuv.y, cuv.x);
        } else {
            rotUv = cuv;
        }
        baseRgb = texture(uCameraTex, clamp(rotUv, 0.001, 0.999)).rgb;
    }

    // Inject Fujitsu MB88303 OSD BEFORE the composite/VHS encoder so OSD gets authentic bleed & scanlines!
    vec4 osd = texture(uOsdTex, uv);
    return mix(baseRgb, osd.rgb, osd.a);
}

void main() {
    // CRT Power Shutdown Collapse (Vertical squeeze -> Horizontal line -> Center P22 Spot)
    if (uShutdownProg > 0.01) {
        vec2 c = abs(vTexCoord * 2.0 - 1.0);
        float vLim = max(0.012, 1.0 - uShutdownProg * 1.5);
        float hLim = uShutdownProg > 0.55 ? max(0.02, 1.0 - (uShutdownProg - 0.55) * 2.1) : 1.0;
        if (c.y > vLim || c.x > hLim) {
            fragColor = vec4(0.015, 0.02, 0.03, 1.0);
            return;
        }
    }

    // 1. Faceplate Barrel Curvature & Camcorder Zoom Rocker
    vec2 normUv = vTexCoord * 2.0 - 1.0;
    float r2 = dot(normUv, normUv);
    float curveAmt = uCurvature * 0.34;
    vec2 curvedUv = normUv * (1.0 + curveAmt * r2);
    if (abs(curvedUv.x) > 1.01 || abs(curvedUv.y) > 1.01) {
        fragColor = vec4(0.02, 0.024, 0.035, 1.0);
        return;
    }

    vec2 baseTc = (curvedUv / max(1.0, uZoom)) * 0.5 + 0.5;

    // Interactive Before/After Split-Screen Wipe (Left = Clean Original, Right = AnalogTV 3.0 Engine)
    if (uWipePos > 0.0 && vTexCoord.x < uWipePos) {
        if (abs(vTexCoord.x - uWipePos) < 0.004) {
            fragColor = vec4(0.96, 0.62, 0.04, 1.0);
            return;
        }
        vec3 cleanCol = sampleRawSource(baseTc);
        fragColor = vec4(cleanCol, 1.0);
        return;
    }

    float lines = (uIsPxl2000 == 1) ? 90.0 : ((uStandard == 1 || uStandard == 2) ? 625.0 : (uStandard == 3 ? 405.0 : 525.0));
    vec2 tc = baseTc;
    if (uIsPxl2000 == 1) {
        tc = floor(tc * vec2(120.0, 90.0)) / vec2(120.0, 90.0);
    }

    // V-Hold Vertical Roll
    if (uVHold > 0.01) {
        tc.y = fract(tc.y + uTime * uVHold * 1.4);
    }

    float scanY = floor(tc.y * lines);
    float fieldId = floor(uTime * 30.0);
    float fieldParity = mod(fieldId, 2.0);
    vec2 px = vec2(1.0 / 360.0, 1.0 / 270.0);

    // 2. Flagging (Top 20 lines exponential H-PLL pull) + Per-Scanline TBC Jitter (NO artificial wavy sine)
    float topDecay = exp(-tc.y * 16.0);
    float flagShift = uFlagging * 0.022 * uTbcAtten * topDecay * (0.8 + 0.2 * noise1D(uTime * 3.0));
    float lineJitter = (noise1D(scanY * 1.93 + fieldId * 7.1) * 0.6 + noise1D(scanY * 0.44 - uTime * 9.0) * 0.4);
    float flutterShift = lineJitter * (0.0003 + uFlutter * 0.0032) * uTbcAtten;

    // 3. Helical-Scan Pause / Cue / Rev Azimuth Noise Bar & Head-Switching Tear
    float azimuthBar = 0.0;
    if (uTransportMode == 1) {
        float barCenter = mix(0.18, 0.82, uTracking) + 0.015 * noise1D(uTime * 4.5);
        float dist = abs(tc.y - barCenter);
        if (dist < 0.095) {
            azimuthBar = pow(max(0.0, 1.0 - dist / 0.095), 1.3);
        }
    } else if (uTransportMode == 2 || uTransportMode == 3) {
        float dir = (uTransportMode == 2) ? 1.0 : -1.0;
        float stripe = sin(tc.y * 15.7 - dir * uTime * 14.0);
        if (stripe > 0.52) {
            azimuthBar = (stripe - 0.52) * 2.08;
        }
    }

    // Bottom 6-scanline rotary head-switching crossover in VBI
    float hsStart = 0.968 - 0.015 * (uTracking - 0.5);
    float hsEnv = step(hsStart, tc.y) * exp(-(tc.y - hsStart) * 55.0);
    float headShift = hsEnv * (0.018 + 0.025 * uChromaBleed) * cos(fieldId * 2.1 + scanY * 1.7);
    float fmBarPull = (azimuthBar > 0.05) ? azimuthBar * 0.028 * noise1D(scanY * 0.7 + uTime * 40.0) : 0.0;
    float ssaviShift = uStompScramble * 0.055 * sin(scanY * 0.14 + uTime * 12.0);

    vec2 warpedTc = vec2(clamp(tc.x + flagShift + flutterShift + headShift + fmBarPull + ssaviShift, 0.001, 0.999), tc.y);

    // 4. Camera Tube Physics: 3-Tube R/B Prism Registration, Vidicon Warm Highlight Glow, Orthicon Halo
    float regOff = uTubeReg * 0.009;
    vec3 srcCenter = sampleRawSource(warpedTc);
    float rTube = (uTubeReg > 0.01) ? sampleRawSource(warpedTc + vec2(regOff, 0.0)).r : srcCenter.r;
    float gTube = srcCenter.g;
    float bTube = (uTubeReg > 0.01) ? sampleRawSource(warpedTc - vec2(regOff, 0.0)).b : srcCenter.b;
    vec3 tubeRgb = vec3(rTube, gTube, bTube);

    // Vidicon / Tube horizontal highlight smear & warm comet persistence
    if (uTubeLag > 0.02) {
        vec3 trail1 = sampleRawSource(warpedTc - vec2(px.x * 3.0, 0.0));
        vec3 trail2 = sampleRawSource(warpedTc - vec2(px.x * 7.0, 0.0));
        vec3 trail3 = sampleRawSource(warpedTc - vec2(px.x * 12.0, 0.0));
        vec3 maxTrail = max(trail1 * 0.92, max(trail2 * 0.80, trail3 * 0.68));
        float trailLum = dot(maxTrail, vec3(0.299, 0.587, 0.114));
        if (trailLum > 0.62) {
            float boost = (trailLum - 0.62) * 2.4 * uTubeLag;
            // Warm Sb2S3 Vidicon spectral retention (Red & Amber persist longer than Blue)
            tubeRgb = max(tubeRgb, maxTrail * vec3(1.0, 0.92, 0.78) * min(1.0, 0.55 + boost));
        }
    }

    // Image Orthicon dark halation ring around extreme highlights
    if (uTubeMode == 2) {
        float nbLum = max(
            dot(sampleRawSource(warpedTc - vec2(px.x * 5.0, 0.0)), vec3(0.299, 0.587, 0.114)),
            dot(sampleRawSource(warpedTc + vec2(px.x * 5.0, 0.0)), vec3(0.299, 0.587, 0.114))
        );
        float cLum = dot(tubeRgb, vec3(0.299, 0.587, 0.114));
        if (nbLum > 0.82 && cLum < 0.70) {
            tubeRgb *= 0.52;
        }
    }

    // 5. YIQ Composite Baseband + 2nd-Order Luma IF Filter + 2 MHz Peaking / Overshoot Ringing
    vec3 yiqCenter = rgb2yiq(tubeRgb);
    vec3 yiqLeft1  = rgb2yiq(sampleRawSource(warpedTc - vec2(px.x * 1.3, 0.0)));
    vec3 yiqLeft2  = rgb2yiq(sampleRawSource(warpedTc - vec2(px.x * 2.8, 0.0)));
    vec3 yiqRight1 = rgb2yiq(sampleRawSource(warpedTc + vec2(px.x * 1.3, 0.0)));

    float dY = yiqCenter.x - yiqLeft1.x;
    float lumaIir = 0.52 * yiqCenter.x + 0.28 * yiqLeft1.x + 0.20 * yiqRight1.x;
    float overshoot = (yiqLeft1.x - yiqLeft2.x) * (0.35 + uEdgeRinging * 1.15) - dY * (0.30 + uEdgeRinging * 0.55);
    float yVal = clamp(lumaIir + overshoot, 0.0, 1.0);

    // Multipath RF Ghosting (Delayed attenuated composite echo)
    if (uGhosting > 0.02) {
        float ghostShift = (6.0 + uGhosting * 18.0) * px.x;
        float ghostY = dot(sampleRawSource(warpedTc - vec2(ghostShift, 0.0)), vec3(0.299, 0.587, 0.114));
        yVal = mix(yVal, yVal + (ghostY - 0.5) * 0.45, uGhosting);
    }

    // 6. Causal 629 kHz VHS Colour-Under Smear + Rightward Chroma Group-Delay Shift + 1H Comb Filter
    float chromaDelay = (2.5 + uChromaBleed * 4.5) * px.x;
    float chromaSpread = (1.2 + uChromaBleed * 3.8) * px.x;
    vec2 iqCurr = vec2(0.0);
    vec2 iqPrevLine = vec2(0.0);
    float wSum = 0.0;
    for (int k = -4; k <= 4; k++) {
        float fk = float(k);
        float w = exp(-0.16 * fk * fk);
        vec2 cUv = clamp(vec2(warpedTc.x - chromaDelay + fk * chromaSpread, warpedTc.y), 0.001, 0.999);
        vec2 pUv = clamp(vec2(warpedTc.x - chromaDelay + fk * chromaSpread, warpedTc.y - px.y * 1.8), 0.001, 0.999);
        iqCurr += rgb2yiq(sampleRawSource(cUv)).yz * w;
        iqPrevLine += rgb2yiq(sampleRawSource(pUv)).yz * w;
        wSum += w;
    }
    iqCurr /= wSum;
    iqPrevLine /= wSum;

    // 1H Vertical Chroma Comb Filter (NTSC / PAL Hanover Bars / SECAM Fire)
    float palAlt = mod(scanY, 2.0) * 2.0 - 1.0;
    vec2 chroma;
    if (uStandard == 2) {
        // SECAM sequential Db/Dr line reuse + FM Cloche fire streaks on sharp transitions
        chroma = (mod(scanY, 2.0) < 1.0) ? vec2(iqCurr.x, iqPrevLine.y) : vec2(iqPrevLine.x, iqCurr.y);
        float dChroma = length(iqCurr - yiqCenter.yz);
        if (dChroma > 0.04) {
            chroma.x += palAlt * dChroma * uHanover * 1.25;
        }
    } else if (uStandard == 1) {
        // PAL 1H delay-line averaging + Hanover bar phase error
        chroma.x = 0.55 * iqCurr.x + 0.45 * iqPrevLine.x;
        chroma.y = (0.55 * iqCurr.y + 0.45 * iqPrevLine.y) * (1.0 + palAlt * uHanover * 0.36);
    } else {
        // NTSC + VHS vertical 2H comb blend
        float vBlend = 0.32 * uChromaBleed;
        chroma = mix(iqCurr, iqPrevLine, vBlend);
    }

    // Boost analog VHS chroma saturation so colors feel warm and vibrant like the web version!
    float colourKiller = (uSnow > 0.82) ? max(0.0, 1.0 - (uSnow - 0.82) * 5.5) : 1.0;
    bool isMono = (uStandard == 3 || uIsPxl2000 == 1);
    chroma = isMono ? vec2(0.0) : chroma * (1.28 + uCrtContrast * 0.25) * colourKiller;

    // 7. True Subcarrier Modulation Cross-Colour (Rainbow Moire) & Cross-Luma Dot Crawl
    float chromaEdge = length(yiqCenter.yz - iqCurr);
    if (!isMono && uDotCrawl > 0.02) {
        float scFreq = (uStandard == 1) ? 283.75 * 6.28318 : 227.5 * 6.28318;
        float scPhase = warpedTc.x * scFreq + scanY * 3.14159 + fieldParity * 3.14159 + floor(uTime * 15.0) * 1.57;
        float scCos = cos(scPhase);
        float scSin = sin(scPhase);
        if (chromaEdge > 0.04) {
            yVal += scCos * chromaEdge * uDotCrawl * 0.45;
        }
        if (abs(dY) > 0.07 && uStandard != 2) {
            float ccGain = (uStandard == 1) ? uDotCrawl * 0.25 : uDotCrawl * 0.45;
            chroma.x += dY * scCos * ccGain;
            chroma.y += dY * scSin * ccGain;
        }
    }

    // 8. Authentic Sparse Horizontal Oxide Dropout Streaks (1-2 random lines per frame, NO regular dash grid!)
    float effDrop = clamp(uTapeNoise + uStompImpulse * 0.65, 0.0, 1.0);
    if (effDrop > 0.02) {
        float lineSeed = hash12(vec2(scanY, fieldId * 1.37));
        // Only ~1.5% of scanlines at moderate tapeNoise trigger a dropout streak
        if (lineSeed > (1.0 - effDrop * 0.032)) {
            float dropStart = hash12(vec2(scanY * 3.1, fieldId * 7.3)) * 0.82;
            float dropLen = 0.04 + hash12(vec2(scanY * 5.7, fieldId * 2.9)) * 0.14;
            if (warpedTc.x >= dropStart && warpedTc.x <= dropStart + dropLen) {
                float tail = 1.0 - (warpedTc.x - dropStart) / dropLen;
                bool isBlack = hash11(scanY + fieldId) < 0.25;
                yVal = isBlack ? yVal * (1.0 - tail * 0.85) : min(1.0, yVal + 0.92 * tail);
                chroma = iqPrevLine; // VHS DOC 1H line substitution
            }
        }
    }

    // Helical-Scan Pause / Cue / Rev Azimuth Bar FM zero-crossing noise
    if (azimuthBar > 0.01) {
        float fmGrain = hash12(vec2(floor(warpedTc.x * 280.0), scanY + fieldId * 19.0));
        float fmSpike = step(1.0 - azimuthBar * 0.22, hash12(vec2(floor(warpedTc.x * 45.0), scanY + fieldId)));
        float barLuma = mix(fmGrain, 0.96, fmSpike);
        yVal = mix(yVal, barLuma, azimuthBar * 0.78);
        chroma *= (1.0 - azimuthBar * 0.92);
    }

    // Head-switching bright FM phase spike at bottom 2 lines
    if (hsEnv > 0.25) {
        float hsNoise = hash12(vec2(floor(warpedTc.x * 200.0), scanY + fieldId));
        yVal = mix(yVal, hsNoise, hsEnv * 0.55);
    }

    // Mains Hum Bar (50/60 Hz rolling bar)
    if (uMainsHum > 0.01) {
        yVal += sin(warpedTc.y * 6.28318 - uTime * 3.2) * 0.075 * uMainsHum;
    }

    // Multi-Octave RF Snow + FM Demodulator Noise Triangle
    if (uSnow > 0.01 || uTapeNoise > 0.01) {
        float highlightWt = 0.35 + 0.65 * yVal;
        float rfNoise = (hash12(vec2(floor(warpedTc.x * 360.0), scanY + fieldId * 29.0)) - 0.5) * 2.0;
        float noiseAmp = (uSnow * 0.26 + uTapeNoise * 0.08) * highlightWt;
        yVal += rfNoise * noiseAmp;
    }

    // 9. Vintage CRT S-Curve Contrast & Warm Analog Tone Mapping
    float sCurveY = smoothstep(0.0, 1.0, yVal);
    yVal = mix(yVal, sCurveY, uCrtContrast * 0.68);
    yVal = clamp(0.02 + yVal * (0.96 + uCrtContrast * 0.10), 0.02, 1.0);

    vec3 rgb = yiq2rgb(vec3(yVal, chroma));

    // 10. 2D Spatial Glass Halation & Phosphor Bloom (samples surrounding bright highlights!)
    if (uBloom > 0.01) {
        vec3 b1 = sampleRawSource(warpedTc + vec2(px.x * 6.0, px.y * 4.0));
        vec3 b2 = sampleRawSource(warpedTc + vec2(-px.x * 6.0, px.y * 4.0));
        vec3 b3 = sampleRawSource(warpedTc + vec2(px.x * 6.0, -px.y * 4.0));
        vec3 b4 = sampleRawSource(warpedTc + vec2(-px.x * 6.0, -px.y * 4.0));
        vec3 bloomAvg = (b1 + b2 + b3 + b4) * 0.25;
        float bloomLum = dot(bloomAvg, vec3(0.299, 0.587, 0.114));
        if (bloomLum > 0.52) {
            float bWeight = (bloomLum - 0.52) * 1.85 * uBloom;
            rgb += bloomAvg * vec3(1.0, 0.92, 0.80) * bWeight * 0.55;
        }
    }

    // 11. CRT Phosphor Chemistry (P22 Consumer Colour, P31 Green, P4 White, P3 Amber)
    if (uPhosphorMode != 0) {
        float mono = dot(rgb, vec3(0.2126, 0.7152, 0.0722));
        if (uPhosphorMode == 1) {
            rgb = vec3(mono * 0.16, mono * 1.08, mono * 0.30);
        } else if (uPhosphorMode == 2) {
            rgb = vec3(mono * 0.94, mono * 0.98, mono * 1.05);
        } else if (uPhosphorMode == 3) {
            rgb = vec3(mono * 1.12, mono * 0.68, mono * 0.07);
        }
    }

    // 12. Degauss Coil Triad Swirl
    if (uDegaussPhase > 0.01 && uPhosphorMode == 0) {
        float dist = length(normUv);
        float angle = sin(dist * 18.0 - uDegaussPhase * 16.0) * uDegaussPhase * 2.5;
        float ca = cos(angle);
        float sa = sin(angle);
        rgb = abs(vec3(
            rgb.r * ca - rgb.g * sa,
            rgb.g * ca + rgb.b * sa,
            rgb.b * ca - rgb.r * sa
        ));
    }

    // 13. Dynamic EHT Beam-Width Scanlines & Sub-Pixel RGB Phosphor Mask (Aperture Grille / Shadow / Slot)
    float normLuma = clamp(yVal, 0.0, 1.0);
    float beamBloom = normLuma * normLuma * 0.62;
    float scanWave = 0.5 - 0.5 * cos(warpedTc.y * 270.0 * 6.28318);
    float scanAtten = 1.0 - uScanlines * 0.46 * scanWave * (1.0 - beamBloom) + uScanlines * 0.09 * (1.0 - scanWave);

    vec3 maskMul = vec3(scanAtten);
    if (uScanlines > 0.05) {
        float mStr = uScanlines * 0.26 * (1.0 - normLuma * 0.32);
        float screenX = floor(vTexCoord.x * 480.0);
        float screenY = floor(vTexCoord.y * 360.0);
        float triad = mod(screenX + ((uMaskMode == 1) ? mod(screenY, 2.0) * 1.5 : 0.0), 3.0);
        if (triad < 1.0) {
            maskMul += vec3(mStr, -mStr * 0.32, -mStr * 0.32);
        } else if (triad < 2.0) {
            maskMul += vec3(-mStr * 0.32, mStr, -mStr * 0.32);
        } else {
            maskMul += vec3(-mStr * 0.32, -mStr * 0.32, mStr);
        }
        // Trinitron damper wires or Slot Mask bridges
        if (uMaskMode == 0 && (abs(vTexCoord.y - 0.33) < 0.002 || abs(vTexCoord.y - 0.67) < 0.002)) {
            maskMul *= 0.82;
        } else if (uMaskMode == 2 && mod(screenY + mod(screenX, 2.0), 3.0) < 1.0) {
            maskMul *= (1.0 - uScanlines * 0.14);
        }
    }
    rgb *= maskMul;

    // 14. Curved CRT Bezel Vignette
    if (curveAmt > 0.01) {
        float vig = max(0.50, 1.0 - r2 * curveAmt * 0.85);
        rgb *= vig;
    }

    if (uShutdownProg > 0.01) {
        rgb *= (1.0 + uShutdownProg * 2.5);
    }

    fragColor = vec4(clamp(rgb, 0.0, 1.0), 1.0);
}