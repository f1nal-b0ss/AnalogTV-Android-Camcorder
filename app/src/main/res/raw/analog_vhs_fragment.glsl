#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

uniform samplerExternalOES uCameraTex;
uniform float uTime;
uniform vec2 uResolution;
uniform float uZoom;

// Authentic AnalogTV 3.2 VHS & Broadcast Signal Parameters
uniform int uStandard;         // 0 = NTSC, 1 = PAL, 2 = SECAM, 3 = Mono 405
uniform float uChromaBleed;    // 629 kHz colour-under Gaussian LPF + 1H comb filter
uniform float uTapeNoise;      // FM noise + multi-pixel horizontal white/black dropouts
uniform float uFlagging;       // Top-of-frame exponential H-PLL skew pull-in
uniform float uTracking;       // Head-switch & pause noise bar position (0..1)
uniform float uPauseBar;       // Stationary helical-scan drum azimuth bar
uniform float uMainsHum;       // 50/60 Hz AC power supply hum bar
uniform float uTubeLag;        // Vidicon highlight persistence / glow
uniform float uTubeReg;        // 3-tube camera R/B registration offset + edge ringing
uniform float uGhosting;       // RF multipath ghost + per-line TBC H-sync jitter
uniform float uCurvature;      // CRT barrel curvature
uniform float uScanlines;      // CRT aperture grille & scanlines

// AnalogCam Stompbox Pedals
uniform float uStompScramble;  // SSAVI scrambler
uniform float uStompImpulse;   // Ignition spark impulse noise
uniform float uDegaussPhase;   // PTC coil degauss swirl

in vec2 vTexCoord;
out vec4 fragColor;

float hash12(vec2 p) {
    vec3 p3  = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
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

void main() {
    vec2 uv = vTexCoord * 2.0 - 1.0;
    float r2 = dot(uv, uv);
    uv *= 1.0 + uCurvature * r2;
    vec2 tc = (uv / max(1.0, uZoom)) * 0.5 + 0.5;
    if (tc.x < 0.0 || tc.x > 1.0 || tc.y < 0.0 || tc.y > 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    float lines = (uStandard == 1 || uStandard == 2) ? 625.0 : (uStandard == 3 ? 405.0 : 525.0);
    float scanY = floor(tc.y * lines);
    float fieldId = floor(uTime * 30.0);
    vec2 px = 1.0 / max(uResolution, vec2(320.0, 240.0));

    // 1. Quasi-rigid scanline geometry: exponential top flagging + discrete per-line H-sync jitter (NO wavy sine distortion)
    float topDecay = exp(-tc.y * 14.0);
    float flagTear = uFlagging * 0.018 * topDecay * (0.65 + 0.35 * (hash12(vec2(fieldId, 1.0)) - 0.5));
    float lineNoise = (hash12(vec2(scanY, fieldId)) - 0.5) * 2.0;
    float adjLineNoise = (hash12(vec2(scanY - 1.0, fieldId)) - 0.5) * 2.0;
    float hSyncJitter = (0.65 * lineNoise + 0.35 * adjLineNoise) * (0.00025 + uGhosting * 0.0018);

    // 2. Bottom 6-scanline rotary head-switching point (exponential PLL pull-in)
    float hsStart = 0.968 - 0.015 * abs(uTracking - 0.5);
    float hsEnv = step(hsStart, tc.y) * exp(-(tc.y - hsStart) * 55.0);
    float headSwitchPull = hsEnv * (0.016 + 0.025 * abs(uTracking - 0.5)) * (0.8 + 0.2 * hash12(vec2(scanY, fieldId)));

    float ssaviShift = uStompScramble * 0.045 * step(0.5, hash12(vec2(floor(scanY * 0.12), fieldId)));
    tc.x = clamp(tc.x + flagTear + hSyncJitter + headSwitchPull + ssaviShift, 0.001, 0.999);

    // 3. Camera tube registration + luma pre-emphasis ringing (crisp dark-then-bright right edge halo)
    float regShift = uTubeReg * 0.004;
    vec3 centerRgb = vec3(
        texture(uCameraTex, tc + vec2(regShift, 0.0)).r,
        texture(uCameraTex, tc).g,
        texture(uCameraTex, tc - vec2(regShift, 0.0)).b
    );
    vec3 yiqCenter = rgb2yiq(centerRgb);
    float yLeft  = dot(texture(uCameraTex, clamp(tc - vec2(px.x * 1.5, 0.0), 0.0, 1.0)).rgb, vec3(0.299, 0.587, 0.114));
    float yLeft2 = dot(texture(uCameraTex, clamp(tc - vec2(px.x * 3.5, 0.0), 0.0, 1.0)).rgb, vec3(0.299, 0.587, 0.114));
    float yRight = dot(texture(uCameraTex, clamp(tc + vec2(px.x * 1.5, 0.0), 0.0, 1.0)).rgb, vec3(0.299, 0.587, 0.114));

    float lumaBlur = 0.50 * yiqCenter.x + 0.25 * yLeft + 0.25 * yRight;
    float edgeStep = yiqCenter.x - yLeft;
    float overshoot = (yLeft - yLeft2) * 0.38 - edgeStep * 0.45;
    float luma = clamp(lumaBlur + overshoot * (0.25 + uTubeLag * 0.45), 0.0, 1.0);

    // 4. VHS 629 kHz Colour-Under 9-tap Gaussian LPF + 1H Delay Line Comb Filter
    float chromaDelay = (0.0025 + uChromaBleed * 0.0065);
    float chromaStep  = (0.0012 + uChromaBleed * 0.0032);
    vec2 iqCurr = vec2(0.0);
    vec2 iqPrevLine = vec2(0.0);
    float wSum = 0.0;
    for (int k = -4; k <= 4; k++) {
        float fk = float(k);
        float w = exp(-0.18 * fk * fk);
        vec2 cUv = clamp(vec2(tc.x - chromaDelay + fk * chromaStep, tc.y), 0.0, 1.0);
        vec2 pUv = clamp(vec2(tc.x - chromaDelay + fk * chromaStep, tc.y - px.y * 2.0), 0.0, 1.0);
        iqCurr += rgb2yiq(texture(uCameraTex, cUv).rgb).yz * w;
        iqPrevLine += rgb2yiq(texture(uCameraTex, pUv).rgb).yz * w;
        wSum += w;
    }
    vec2 chroma = 0.55 * (iqCurr / wSum) + 0.45 * (iqPrevLine / wSum);

    // Subcarrier dot crawl on sharp chroma transitions
    float chromaEdge = length(chroma - yiqCenter.yz);
    float subPhase = tc.x * 340.0 + scanY * 3.14159 + fieldId * 1.57;
    luma += chromaEdge * 0.22 * sin(subPhase);

    if (uStandard == 3) {
        chroma = vec2(0.0);
    }

    // 5. High-frequency FM luma grain + horizontal oxide dropout streaks with exponential tail
    float fmGrain = (hash12(vec2(floor(tc.x * 360.0), scanY + fieldId * 17.0)) - 0.5) * uTapeNoise * 0.14;
    luma += fmGrain;

    float segX = floor(tc.x * 24.0);
    float dropSeed = hash12(vec2(segX, scanY + fieldId * 31.0));
    float dropThresh = 1.0 - (uTapeNoise * 0.008 + uStompImpulse * 0.035);
    if (dropSeed > dropThresh) {
        float segFrac = fract(tc.x * 24.0);
        float dropEnv = exp(-segFrac * 3.5);
        luma = mix(luma, 0.95, dropEnv);
        chroma *= (1.0 - dropEnv);
    }

    // Head-switch bright FM phase spike on bottom 2 lines
    if (hsEnv > 0.25) {
        float hsSpike = hash12(vec2(floor(tc.x * 180.0), scanY + fieldId)) * hsEnv * 0.45;
        luma = clamp(luma + hsSpike, 0.0, 1.0);
    }

    // 6. Pause / Search Helical-Scan Azimuth Noise Bar
    if (uPauseBar > 0.01) {
        float barCenter = mix(0.18, 0.82, uTracking);
        float barMask = smoothstep(0.075, 0.01, abs(tc.y - barCenter)) * uPauseBar;
        if (barMask > 0.01) {
            float barNoise = hash12(vec2(floor(tc.x * 260.0), scanY + fieldId * 13.0));
            luma = mix(luma, barNoise, barMask * 0.85);
            chroma *= (1.0 - barMask);
        }
    }

    // 7. Mains Hum Bar
    if (uMainsHum > 0.01) {
        luma += sin(tc.y * 6.28318 - uTime * 2.2) * 0.06 * uMainsHum;
    }

    // 8. Tape black pedestal lift & highlight knee compression
    luma = clamp(0.035 + luma * 0.93, 0.035, 0.97);
    vec3 outRgb = yiq2rgb(vec3(luma, chroma));

    // 9. Subtle CRT scanline & triad mask
    float scanlineMask = 1.0 - uScanlines * 0.22 * (0.5 - 0.5 * cos(tc.y * lines * 6.28318));
    outRgb *= scanlineMask;

    fragColor = vec4(outRgb, 1.0);
}