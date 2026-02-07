export class ColorPicker {
    constructor(container) {
        this.container = container;
        this.rSlider = document.getElementById("r-slider");
        this.gSlider = document.getElementById("g-slider");
        this.bSlider = document.getElementById("b-slider");

        this.rNumber = document.getElementById("r-number");
        this.gNumber = document.getElementById("g-number");
        this.bNumber = document.getElementById("b-number");

        this.hSlider = document.getElementById("h-slider");
        this.sSlider = document.getElementById("s-slider");
        this.lSlider = document.getElementById("l-slider");

        this.hNumber = document.getElementById("h-number");
        this.sNumber = document.getElementById("s-number");
        this.lNumber = document.getElementById("l-number");

        this.rgbControls = document.getElementById("rgb-controls");
        this.hslControls = document.getElementById("hsl-controls");
        this.tabRgb = document.getElementById("tab-rgb");
        this.tabHsl = document.getElementById("tab-hsl");

        this.colorPreview = document.getElementById("color-preview");

        this.contrastWhite = document.getElementById("contrast-white");
        this.contrastBlack = document.getElementById("contrast-black");
        this.wcagWhite = document.getElementById("wcag-white");
        this.wcagBlack = document.getElementById("wcag-black");

        this.init();
    }

    init() {
        // Tabs
        this.tabRgb.onclick = () => {
            this.rgbControls.style.display = "block";
            this.hslControls.style.display = "none";
            this.tabRgb.classList.add("active");
            this.tabHsl.classList.remove("active");
        };
        this.tabHsl.onclick = () => {
            this.rgbControls.style.display = "none";
            this.hslControls.style.display = "block";
            this.tabHsl.classList.add("active");
            this.tabRgb.classList.remove("active");
        };

        // RGB Sliders
        this.rSlider.oninput = () => this.setRGB(parseInt(this.rSlider.value), parseInt(this.gSlider.value), parseInt(this.bSlider.value), "slider");
        this.gSlider.oninput = () => this.setRGB(parseInt(this.rSlider.value), parseInt(this.gSlider.value), parseInt(this.bSlider.value), "slider");
        this.bSlider.oninput = () => this.setRGB(parseInt(this.rSlider.value), parseInt(this.gSlider.value), parseInt(this.bSlider.value), "slider");

        // RGB Number inputs
        this.rNumber.oninput = () => this.setRGB(this.parseNum(this.rNumber.value), this.parseNum(this.gNumber.value), this.parseNum(this.bNumber.value), "number");
        this.gNumber.oninput = () => this.setRGB(this.parseNum(this.rNumber.value), this.parseNum(this.gNumber.value), this.parseNum(this.bNumber.value), "number");
        this.bNumber.oninput = () => this.setRGB(this.parseNum(this.rNumber.value), this.parseNum(this.gNumber.value), this.parseNum(this.bNumber.value), "number");

        // HSL Sliders
        this.hSlider.oninput = () => this.setHSL(parseInt(this.hSlider.value), parseInt(this.sSlider.value), parseInt(this.lSlider.value), "slider");
        this.sSlider.oninput = () => this.setHSL(parseInt(this.hSlider.value), parseInt(this.sSlider.value), parseInt(this.lSlider.value), "slider");
        this.lSlider.oninput = () => this.setHSL(parseInt(this.hSlider.value), parseInt(this.sSlider.value), parseInt(this.lSlider.value), "slider");

        // HSL Number inputs
        this.hNumber.oninput = () => this.setHSL(this.parseNum(this.hNumber.value, 360), this.parseNum(this.sNumber.value, 100), this.parseNum(this.lNumber.value, 100), "number");
        this.sNumber.oninput = () => this.setHSL(this.parseNum(this.hNumber.value, 360), this.parseNum(this.sNumber.value, 100), this.parseNum(this.lNumber.value, 100), "number");
        this.lNumber.oninput = () => this.setHSL(this.parseNum(this.hNumber.value, 360), this.parseNum(this.sNumber.value, 100), this.parseNum(this.lNumber.value, 100), "number");

        // Enter key in inputs to trigger Add Color button
        const addColorBtn = document.getElementById("add-color-btn");
        const inputs = [this.rNumber, this.gNumber, this.bNumber, this.hNumber, this.sNumber, this.lNumber];
        for (const el of inputs) {
            el.onkeydown = (ev) => {
                if (ev.key === "Enter") {
                    addColorBtn?.click();
                }
            };
        }

        // Keyboard fine adjustments on sliders
        const sliders = [this.rSlider, this.gSlider, this.bSlider, this.hSlider, this.sSlider, this.lSlider];
        for (const slider of sliders) {
            slider.addEventListener("keydown", (ev) => {
                const step = ev.shiftKey ? 10 : 1;
                const cur = parseInt(slider.value);
                const isHSL = slider === this.hSlider || slider === this.sSlider || slider === this.lSlider;
                const maxVal = slider === this.hSlider ? 360 : (isHSL ? 100 : 255);

                if (ev.key === "ArrowUp" || ev.key === "ArrowRight") {
                    const next = Math.min(cur + step, maxVal);
                    if (isHSL) {
                        this.setHSL(
                            slider === this.hSlider ? next : parseInt(this.hSlider.value),
                            slider === this.sSlider ? next : parseInt(this.sSlider.value),
                            slider === this.lSlider ? next : parseInt(this.lSlider.value),
                            "slider"
                        );
                    } else {
                        this.setRGB(
                            slider === this.rSlider ? next : parseInt(this.rSlider.value),
                            slider === this.gSlider ? next : parseInt(this.gSlider.value),
                            slider === this.bSlider ? next : parseInt(this.bSlider.value),
                            "slider"
                        );
                    }
                    ev.preventDefault();
                } else if (ev.key === "ArrowDown" || ev.key === "ArrowLeft") {
                    const next = Math.max(cur - step, 0);
                    if (isHSL) {
                        this.setHSL(
                            slider === this.hSlider ? next : parseInt(this.hSlider.value),
                            slider === this.sSlider ? next : parseInt(this.sSlider.value),
                            slider === this.lSlider ? next : parseInt(this.lSlider.value),
                            "slider"
                        );
                    } else {
                        this.setRGB(
                            slider === this.rSlider ? next : parseInt(this.rSlider.value),
                            slider === this.gSlider ? next : parseInt(this.gSlider.value),
                            slider === this.bSlider ? next : parseInt(this.bSlider.value),
                            "slider"
                        );
                    }
                    ev.preventDefault();
                }
            });
        }

        // Initialize
        this.setRGB(parseInt(this.rSlider.value), parseInt(this.gSlider.value), parseInt(this.bSlider.value), "init");
    }

    parseNum(v, max = 255) {
        const n = parseInt(v) || 0;
        return Math.max(0, Math.min(n, max));
    }

    toHex2(n) {
        return Math.max(0, Math.min(n, 255)).toString(16).padStart(2, '0');
    }

    normalizeHex(v) {
        let s = v;
        if (!s.startsWith("#")) s = "#" + s;
        if (s.length !== 7) return null;
        for (let i = 1; i < 7; i++) {
            const ch = s[i];
            const isHex = (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
            if (!isHex) return null;
        }
        return s.toUpperCase();
    }

    hexToRgb(hex) {
        const r = parseInt(hex.substring(1, 3), 16);
        const g = parseInt(hex.substring(3, 5), 16);
        const b = parseInt(hex.substring(5, 7), 16);
        return { r, g, b };
    }

    rgbToHsl(r, g, b) {
        const rf = r / 255.0;
        const gf = g / 255.0;
        const bf = b / 255.0;
        const max = Math.max(rf, gf, bf);
        const min = Math.min(rf, gf, bf);
        let h, s, l = (max + min) / 2.0;

        if (max === min) {
            h = 0.0;
            s = 0.0;
        } else {
            const d = max - min;
            s = l > 0.5 ? d / (2.0 - max - min) : d / (max + min);
            switch (max) {
                case rf: h = (gf - bf) / d + (gf < bf ? 6 : 0); break;
                case gf: h = (bf - rf) / d + 2; break;
                case bf: h = (rf - gf) / d + 4; break;
            }
            h /= 6.0;
        }
        return { h: Math.round(h * 360), s: Math.round(s * 100), l: Math.round(l * 100) };
    }

    hslToRgb(h, s, l) {
        const hf = h / 360.0;
        const sf = s / 100.0;
        const lf = l / 100.0;
        let r, g, b;

        if (s === 0) {
            r = g = b = lf;
        } else {
            const hue2rgb = (p, q, t) => {
                if (t < 0) t += 1.0;
                if (t > 1) t -= 1.0;
                if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
                if (t < 1.0 / 2.0) return q;
                if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
                return p;
            };

            const q = lf < 0.5 ? lf * (1.0 + sf) : lf + sf - lf * sf;
            const p = 2.0 * lf - q;
            r = hue2rgb(p, q, hf + 1.0 / 3.0);
            g = hue2rgb(p, q, hf);
            b = hue2rgb(p, q, hf - 1.0 / 3.0);
        }
        return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) };
    }

    setSliderGradient() {
        const r = parseInt(this.rSlider.value);
        const g = parseInt(this.gSlider.value);
        const b = parseInt(this.bSlider.value);
        this.rSlider.style.background = `linear-gradient(to right, rgb(0,${g},${b}), rgb(255,${g},${b}))`;
        this.gSlider.style.background = `linear-gradient(to right, rgb(${r},0,${b}), rgb(${r},255,${b}))`;
        this.bSlider.style.background = `linear-gradient(to right, rgb(${r},${g},0), rgb(${r},${g},255))`;

        const h = parseInt(this.hSlider.value);
        const s = parseInt(this.sSlider.value);
        const l = parseInt(this.lSlider.value);
        this.hSlider.style.background = "linear-gradient(to right, #ff0000 0%, #ffff00 17%, #00ff00 33%, #00ffff 50%, #0000ff 67%, #ff00ff 83%, #ff0000 100%)";
        this.sSlider.style.background = `linear-gradient(to right, hsl(${h}, 0%, ${l}%), hsl(${h}, 100%, ${l}%))`;
        this.lSlider.style.background = `linear-gradient(to right, hsl(${h}, ${s}%, 0%), hsl(${h}, ${s}%, 50%), hsl(${h}, ${s}%, 100%))`;
    }

    setColor(hex) {
        const norm = this.normalizeHex(hex);
        if (!norm) return;
        const rgb = this.hexToRgb(norm);
        this.setRGB(rgb.r, rgb.g, rgb.b, "external");
    }

    setRGB(r, g, b, from) {
        const rr = Math.max(0, Math.min(r, 255));
        const gg = Math.max(0, Math.min(g, 255));
        const bb = Math.max(0, Math.min(b, 255));

        if (from !== "slider") {
            this.rSlider.value = rr;
            this.gSlider.value = gg;
            this.bSlider.value = bb;
        }
        if (from !== "number") {
            this.rNumber.value = rr;
            this.gNumber.value = gg;
            this.bNumber.value = bb;
        }

        const hsl = this.rgbToHsl(rr, gg, bb);
        this.updateHSLUI(hsl.h, hsl.s, hsl.l);
        this.updateCommonUI(rr, gg, bb, from);
    }

    setHSL(h, s, l, from) {
        const hh = Math.max(0, Math.min(h, 360));
        const ss = Math.max(0, Math.min(s, 100));
        const ll = Math.max(0, Math.min(l, 100));

        if (from !== "slider") {
            this.hSlider.value = hh;
            this.sSlider.value = ss;
            this.lSlider.value = ll;
        }
        if (from !== "number") {
            this.hNumber.value = hh;
            this.sNumber.value = ss;
            this.lNumber.value = ll;
        }

        const rgb = this.hslToRgb(hh, ss, ll);
        this.updateRGBUI(rgb.r, rgb.g, rgb.b);
        this.updateCommonUI(rgb.r, rgb.g, rgb.b, from);
    }

    updateRGBUI(r, g, b) {
        this.rSlider.value = r;
        this.gSlider.value = g;
        this.bSlider.value = b;
        this.rNumber.value = r;
        this.gNumber.value = g;
        this.bNumber.value = b;
    }

    updateHSLUI(h, s, l) {
        this.hSlider.value = h;
        this.sSlider.value = s;
        this.lSlider.value = l;
        this.hNumber.value = h;
        this.sNumber.value = s;
        this.lNumber.value = l;
    }

    updateCommonUI(r, g, b, from) {
        const hex = "#" + this.toHex2(r) + this.toHex2(g) + this.toHex2(b);
        this.colorPreview.style.backgroundColor = hex;
        this.setSliderGradient();
        this.updateContrastInfo(r, g, b);
        this.updatePaletteSelection(hex);
    }

    updatePaletteSelection(hex) {
        // Authoritative selection is handled in main.js
    }

    updateContrastInfo(r, g, b) {
        const getLuminance = (rv, gv, bv) => {
            const adjust = (c) => {
                const v = c / 255.0;
                return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
            };
            return adjust(rv) * 0.2126 + adjust(gv) * 0.7152 + adjust(bv) * 0.0722;
        };

        const lum = getLuminance(r, g, b);
        const finalWhite = Math.max((1.0 + 0.05) / (lum + 0.05), (lum + 0.05) / (1.0 + 0.05));
        const finalBlack = Math.max((lum + 0.05) / (0.0 + 0.05), (0.0 + 0.05) / (lum + 0.05));

        if (this.contrastWhite) this.contrastWhite.textContent = (Math.floor(finalWhite * 10) / 10) + ":1";
        if (this.contrastBlack) this.contrastBlack.textContent = (Math.floor(finalBlack * 10) / 10) + ":1";

        this.updateWCAGBadge(this.wcagWhite, finalWhite);
        this.updateWCAGBadge(this.wcagBlack, finalBlack);
    }

    updateWCAGBadge(el, ratio) {
        if (!el) return;
        let status = "FAIL";
        if (ratio >= 7.0) status = "AAA";
        else if (ratio >= 4.5) status = "AA";
        else if (ratio >= 3.0) status = "AA Large";

        el.textContent = status;
        el.className = status === "FAIL" ? "wcag-fail" : "wcag-pass";
    }

    getValue() {
        const r = parseInt(this.rSlider.value);
        const g = parseInt(this.gSlider.value);
        const b = parseInt(this.bSlider.value);
        return "#" + this.toHex2(r) + this.toHex2(g) + this.toHex2(b);
    }

    updateStyles(uiColor) {
        this.container.style.color = uiColor;
        this.colorPreview.style.borderColor = uiColor;
        this.container.querySelectorAll(".picker-label").forEach(el => el.style.color = uiColor);
        this.container.querySelectorAll(".picker-number").forEach(el => el.style.borderColor = uiColor);
        this.container.querySelectorAll(".picker-tab").forEach(el => {
            el.style.color = uiColor;
            el.style.borderColor = uiColor;
        });
        this.container.querySelectorAll(".group-label").forEach(el => el.style.color = uiColor);
    }
}
