#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

uniform samplerExternalOES uCameraTex;
uniform sampler2D uPrevLagTex; // Ping-pong FBO for Vidicon camera tube persistence
uniform float uTime;
uniform vec2 uResolution;
uniform float uZoom;           // Camcorder optical/digital zoom (1.0 .. 4.0)

// Signal & VHS Parameters (AnalogTV 3.0 Pipeline)
uniform int uStandard;         // 0 = NTSC, 1 = PAL, 2 = SECAM, 3 = Mono 405
uniform float uChromaBleed;    // VHS 629kHz colour-under lowpass smearing (0..1)
uniform float uTapeNoise;      // FM noise triangle & snow (0..1)
uniform float uFlagging;       // Top-of-frame H-PLL tension bend (0..1)
uniform float uTracking;       // Head-switch & pause noise bar position (0..1)
uniform float uPauseBar;       // 1.0 when paused / searching (FM azimuth noise bar)
uniform float uMainsHum;       // 0 = Off, 1 = 50Hz, 2 = 60Hz hum bar amplitude
uniform float uTubeLag;        // Vidicon / Orthicon temporal persistence (0..0.92)
uniform float uTubeReg;        // 3-tube camera R/B prism registration offset
uniform float uGhosting;       // RF multipath echo strength
uniform float uCurvature;      // CRT barrel distortion
uniform float uScanlines;      // CRT scanline & shadow mask intensity

// AnalogCam Live Stompbox Pedals
uniform float uStompScramble;  // SSAVI sinusoidal H-oscillator hunt + polarity invert
uniform float uStompImpulse;   // Broadband RF ignition spikes
uniform float uDegaussPhase;   // 1.8s PTC coil tangential Lorentz swirl

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
    // 1. CRT Barrel Distortion & Camcorder Zoom Rocker
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

    // 2. Flagging + SSAVI Scrambler + Capstan Wow/Flutter + Head-Switch Tear
    float topDecay = exp(-tc.y * 8.5);
    float flagShift = uFlagging * 0.045 * topDecay * sin(uTime * 14.0 + tc.y * 6.0);
    float ssaviWave = uStompScramble * 0.065 * sin(tc.y * 22.0 + uTime * 11.0);
    float flutter = 0.0018 * uChromaBleed * sin(tc.y * 45.0 + uTime * 7.5);
    float headSwitch = step(0.965, tc.y) * (0.025 * sin(uTime * 30.0 + tc.y * 200.0));
    tc.x = fract(tc.x + flagShift + ssaviWave + flutter + headSwitch);

    // 3. Camera Tube Registration & Vidicon Temporal Lag
    float regOffset = uTubeReg * 0.008;
    float rSample = texture(uCameraTex, tc + vec2(regOffset, -regOffset * 0.4)).r;
    float gSample = texture(uCameraTex, tc).g;
    float bSample = texture(uCameraTex, tc - vec2(regOffset, regOffset * 0.4)).b;
    vec3 prevLag = texture(uPrevLagTex, vTexCoord).rgb;
    vec3 tubeRgb = max(vec3(rSample, gSample, bSample), prevLag * uTubeLag);

    // 4. Composite Encode/Decode & VHS Colour-Under Chroma Smear (~400 kHz lowpass)
    vec3 yiq = rgb2yiq(tubeRgb);
    float luma = yiq.x;

    if (uGhosting > 0.01) {
        vec3 ghostRgb = texture(uCameraTex, clamp(tc - vec2(0.035, 0.0), 0.0, 1.0)).rgb;
        vec3 ghostYiq = rgb2yiq(ghostRgb);
        luma = mix(luma, luma + ghostYiq.x * 0.35, uGhosting);
    }

    vec2 iqAccum = vec2(0.0);
    float weightSum = 0.0;
    float spread = (0.002 + uChromaBleed * 0.016);
    for (int i = 0; i < 9; i++) {
        float w = exp(-float(i) * 0.35);
        vec2 sampleUv = clamp(tc - vec2(float(i) * spread / 9.0, 0.0), 0.0, 1.0);
        vec3 sYiq = rgb2yiq(texture(uCameraTex, sampleUv).rgb);
        iqAccum += sYiq.yz * w;
        weightSum += w;
    }
    vec2 chroma = iqAccum / weightSum;

    // Subcarrier Dot Crawl
    float chromaEdge = length(chroma - yiq.yz);
    float subPhase = tc.x * 320.0 + scanY * 3.14159 + uTime * 18.0;
    luma += chromaEdge * 0.35 * sin(subPhase);

    if (uStandard == 1) {
        float alt = mod(scanY, 2.0) * 2.0 - 1.0;
        chroma.y *= (1.0 + 0.18 * alt * uChromaBleed);
    } else if (uStandard == 2) {
        float fire = chromaEdge * sin(tc.x * 90.0 + uTime * 20.0);
        chroma.x += fire * 0.45;
    } else if (uStandard == 3) {
        chroma = vec2(0.0);
    }

    // SSAVI Field Polarity Inversion
    if (uStompScramble > 0.5 && mod(floor(uTime * 15.0), 2.0) < 1.0) {
        luma = 1.0 - luma;
        chroma = -chroma * 1.3;
    }

    // 5. FM Tape Noise Triangle + Impulse Spikes + Oxide Dropouts
    float n = hash12(vec2(tc.x * 480.0 + uTime * 13.7, scanY));
    luma += (n - 0.5) * uTapeNoise * (0.4 + 0.6 * luma);

    float dropSeed = hash12(vec2(floor(tc.x * 18.0), scanY + floor(uTime * 24.0) * 19.0));
    if (dropSeed > (1.0 - (uTapeNoise * 0.018 + uStompImpulse * 0.04))) {
        luma = 0.96;
        chroma = vec2(0.0);
    }

    // 6. Pause / Search Helical-Scan Azimuth Noise Bar
    if (uPauseBar > 0.01) {
        float barCenter = mix(0.18, 0.82, uTracking);
        float barMask = smoothstep(0.08, 0.01, abs(tc.y - barCenter)) * uPauseBar;
        if (barMask > 0.01) {
            float barNoise = hash12(vec2(tc.x * 300.0, scanY + uTime * 50.0));
            luma = mix(luma, barNoise, barMask * 0.88);
            chroma *= (1.0 - barMask);
        }
    }

    // 7. Mains Hum Bars (50/60 Hz beat roll)
    if (uMainsHum > 0.01) {
        luma += sin(tc.y * 6.28318 * 1.5 - uTime * 2.4) * 0.08 * uMainsHum;
    }

    vec3 outRgb = yiq2rgb(vec3(luma, chroma));

    // 8. Degauss Coil Triad Swirl
    if (uDegaussPhase > 0.01) {
        float d = length(uv);
        float angle = sin(d * 16.0 - uDegaussPhase * 14.0) * uDegaussPhase * 2.5;
        float ca = cos(angle);
        float sa = sin(angle);
        outRgb = abs(vec3(
            outRgb.r * ca - outRgb.g * sa,
            outRgb.g * ca + outRgb.b * sa,
            outRgb.b * ca - outRgb.r * sa
        ));
    }

    // 9. CRT Scanlines & Aperture Grille
    float scanlineMask = 1.0 - uScanlines * 0.35 * (0.5 - 0.5 * cos(tc.y * lines * 6.28318));
    float triad = mod(floor(tc.x * uResolution.x), 3.0);
    vec3 phosphorMask = vec3(
        triad == 0.0 ? 1.08 : 0.92,
        triad == 1.0 ? 1.08 : 0.92,
        triad == 2.0 ? 1.08 : 0.92
    );
    outRgb *= scanlineMask * mix(vec3(1.0), phosphorMask, uScanlines * 0.5);

    fragColor = vec4(outRgb, 1.0);
}
