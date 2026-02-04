import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ColorRGB(val r: Int, val g: Int, val b: Int)
class ColorHSL(val h: Int, val s: Int, val l: Int)

class ColorPicker(val container: HTMLElement) {
    private val rSlider = document.getElementById("r-slider") as HTMLInputElement
    private val gSlider = document.getElementById("g-slider") as HTMLInputElement
    private val bSlider = document.getElementById("b-slider") as HTMLInputElement

    private val rNumber = document.getElementById("r-number") as HTMLInputElement
    private val gNumber = document.getElementById("g-number") as HTMLInputElement
    private val bNumber = document.getElementById("b-number") as HTMLInputElement

    private val hSlider = document.getElementById("h-slider") as HTMLInputElement
    private val sSlider = document.getElementById("s-slider") as HTMLInputElement
    private val lSlider = document.getElementById("l-slider") as HTMLInputElement

    private val hNumber = document.getElementById("h-number") as HTMLInputElement
    private val sNumber = document.getElementById("s-number") as HTMLInputElement
    private val lNumber = document.getElementById("l-number") as HTMLInputElement

    private val rgbControls = document.getElementById("rgb-controls") as HTMLDivElement
    private val hslControls = document.getElementById("hsl-controls") as HTMLDivElement
    private val tabRgb = document.getElementById("tab-rgb") as HTMLButtonElement
    private val tabHsl = document.getElementById("tab-hsl") as HTMLButtonElement

    private val colorPreview = document.getElementById("color-preview") as HTMLDivElement

    private val contrastWhite = document.getElementById("contrast-white") as? HTMLSpanElement
    private val contrastBlack = document.getElementById("contrast-black") as? HTMLSpanElement
    private val wcagWhite = document.getElementById("wcag-white") as? HTMLSpanElement
    private val wcagBlack = document.getElementById("wcag-black") as? HTMLSpanElement

    init {
        // Tabs
        tabRgb.onclick = {
            rgbControls.style.display = "block"
            hslControls.style.display = "none"
            tabRgb.classList.add("active")
            tabHsl.classList.remove("active")
        }
        tabHsl.onclick = {
            rgbControls.style.display = "none"
            hslControls.style.display = "block"
            tabHsl.classList.add("active")
            tabRgb.classList.remove("active")
        }

        // RGB Sliders
        rSlider.oninput = { setRGB(rSlider.value.toInt(), gSlider.value.toInt(), bSlider.value.toInt(), from = "slider") }
        gSlider.oninput = { setRGB(rSlider.value.toInt(), gSlider.value.toInt(), bSlider.value.toInt(), from = "slider") }
        bSlider.oninput = { setRGB(rSlider.value.toInt(), gSlider.value.toInt(), bSlider.value.toInt(), from = "slider") }

        // RGB Number inputs
        rNumber.oninput = { setRGB(parseNum(rNumber.value), parseNum(gNumber.value), parseNum(bNumber.value), from = "number") }
        gNumber.oninput = { setRGB(parseNum(rNumber.value), parseNum(gNumber.value), parseNum(bNumber.value), from = "number") }
        bNumber.oninput = { setRGB(parseNum(rNumber.value), parseNum(gNumber.value), parseNum(bNumber.value), from = "number") }

        // HSL Sliders
        hSlider.oninput = { setHSL(hSlider.value.toInt(), sSlider.value.toInt(), lSlider.value.toInt(), from = "slider") }
        sSlider.oninput = { setHSL(hSlider.value.toInt(), sSlider.value.toInt(), lSlider.value.toInt(), from = "slider") }
        lSlider.oninput = { setHSL(hSlider.value.toInt(), sSlider.value.toInt(), lSlider.value.toInt(), from = "slider") }

        // HSL Number inputs
        hNumber.oninput = { setHSL(parseNum(hNumber.value, 360), parseNum(sNumber.value, 100), parseNum(lNumber.value, 100), from = "number") }
        sNumber.oninput = { setHSL(parseNum(hNumber.value, 360), parseNum(sNumber.value, 100), parseNum(lNumber.value, 100), from = "number") }
        lNumber.oninput = { setHSL(parseNum(hNumber.value, 360), parseNum(sNumber.value, 100), parseNum(lNumber.value, 100), from = "number") }

        // Enter key in inputs to trigger Add Color button
        val addColorBtn = document.getElementById("add-color-btn") as? HTMLButtonElement
        val inputs = arrayOf(rNumber, gNumber, bNumber, hNumber, sNumber, lNumber)
        for (el in inputs) {
            el.onkeydown = { ev ->
                if (ev.key == "Enter") {
                    addColorBtn?.click()
                }
            }
        }

        // Keyboard fine adjustments on sliders
        val sliders = arrayOf(rSlider, gSlider, bSlider, hSlider, sSlider, lSlider)
        for (slider in sliders) {
            slider.addEventListener("keydown", { ev ->
                ev as KeyboardEvent
                val step = if (ev.shiftKey) 10 else 1
                val cur = slider.value.toInt()
                val isHSL = slider == hSlider || slider == sSlider || slider == lSlider
                val maxVal = if (slider == hSlider) 360 else if (isHSL) 100 else 255

                when (ev.key) {
                    "ArrowUp", "ArrowRight" -> {
                        val next = (cur + step).coerceIn(0, maxVal)
                        if (isHSL) {
                            setHSL(
                                if (slider == hSlider) next else hSlider.value.toInt(),
                                if (slider == sSlider) next else sSlider.value.toInt(),
                                if (slider == lSlider) next else lSlider.value.toInt(),
                                from = "slider"
                            )
                        } else {
                            setRGB(
                                if (slider == rSlider) next else rSlider.value.toInt(),
                                if (slider == gSlider) next else gSlider.value.toInt(),
                                if (slider == bSlider) next else bSlider.value.toInt(),
                                from = "slider"
                            )
                        }
                        ev.preventDefault()
                    }
                    "ArrowDown", "ArrowLeft" -> {
                        val next = (cur - step).coerceIn(0, maxVal)
                        if (isHSL) {
                            setHSL(
                                if (slider == hSlider) next else hSlider.value.toInt(),
                                if (slider == sSlider) next else sSlider.value.toInt(),
                                if (slider == lSlider) next else lSlider.value.toInt(),
                                from = "slider"
                            )
                        } else {
                            setRGB(
                                if (slider == rSlider) next else rSlider.value.toInt(),
                                if (slider == gSlider) next else gSlider.value.toInt(),
                                if (slider == bSlider) next else bSlider.value.toInt(),
                                from = "slider"
                            )
                        }
                        ev.preventDefault()
                    }
                }
            })
        }

        // Initialize
        setRGB(rSlider.value.toInt(), gSlider.value.toInt(), bSlider.value.toInt(), from = "init")
    }

    private fun parseNum(v: String?, max: Int = 255): Int {
        val n = v?.toIntOrNull() ?: 0
        return n.coerceIn(0, max)
    }

    private fun toHex2(n: Int) = n.coerceIn(0, 255).toString(16).padStart(2, '0')

    private fun normalizeHex(v: String): String? {
        var s = v
        if (s.startsWith("#").not()) s = "#$s"
        if (s.length != 7) return null
        val re = Regex("^#[0-9a-fA-F]{6}$")
        return if (re.matches(s)) s.uppercase() else null
    }

    private fun hexToRgb(hex: String): ColorRGB {
        val r = hex.substring(1, 3).toInt(16)
        val g = hex.substring(3, 5).toInt(16)
        val b = hex.substring(5, 7).toInt(16)
        return ColorRGB(r, g, b)
    }

    private fun rgbToHsl(r: Int, g: Int, b: Int): ColorHSL {
        val rf = r / 255.0
        val gf = g / 255.0
        val bf = b / 255.0
        val max = max(rf, max(gf, bf))
        val min = min(rf, min(gf, bf))
        var h: Double
        var s: Double
        val l = (max + min) / 2.0

        if (max == min) {
            h = 0.0
            s = 0.0
        } else {
            val d = max - min
            s = if (l > 0.5) d / (2.0 - max - min) else d / (max + min)
            h = when (max) {
                rf -> (gf - bf) / d + (if (gf < bf) 6 else 0)
                gf -> (bf - rf) / d + 2
                else -> (rf - gf) / d + 4
            }
            h /= 6.0
        }
        return ColorHSL((h * 360).toInt(), (s * 100).toInt(), (l * 100).toInt())
    }

    private fun hslToRgb(h: Int, s: Int, l: Int): ColorRGB {
        val hf = h / 360.0
        val sf = s / 100.0
        val lf = l / 100.0
        var r: Double
        var g: Double
        var b: Double

        if (s == 0) {
            r = lf; g = lf; b = lf
        } else {
            fun hue2rgb(p: Double, q: Double, t: Double): Double {
                var tt = t
                if (tt < 0) tt += 1.0
                if (tt > 1) tt -= 1.0
                if (tt < 1.0 / 6.0) return p + (q - p) * 6.0 * tt
                if (tt < 1.0 / 2.0) return q
                if (tt < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - tt) * 6.0
                return p
            }

            val q = if (lf < 0.5) lf * (1.0 + sf) else lf + sf - lf * sf
            val p = 2.0 * lf - q
            r = hue2rgb(p, q, hf + 1.0 / 3.0)
            g = hue2rgb(p, q, hf)
            b = hue2rgb(p, q, hf - 1.0 / 3.0)
        }
        return ColorRGB((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun setSliderGradient() {
        val r = rSlider.value.toInt(); val g = gSlider.value.toInt(); val b = bSlider.value.toInt()
        rSlider.style.background = "linear-gradient(to right, rgb(0,$g,$b), rgb(255,$g,$b))"
        gSlider.style.background = "linear-gradient(to right, rgb($r,0,$b), rgb($r,255,$b))"
        bSlider.style.background = "linear-gradient(to right, rgb($r,$g,0), rgb($r,$g,255))"

        val h = hSlider.value.toInt(); val s = sSlider.value.toInt(); val l = lSlider.value.toInt()
        hSlider.style.background = "linear-gradient(to right, #ff0000 0%, #ffff00 17%, #00ff00 33%, #00ffff 50%, #0000ff 67%, #ff00ff 83%, #ff0000 100%)"
        sSlider.style.background = "linear-gradient(to right, hsl($h, 0%, $l%), hsl($h, 100%, $l%))"
        lSlider.style.background = "linear-gradient(to right, hsl($h, $s%, 0%), hsl($h, $s%, 50%), hsl($h, $s%, 100%))"
    }

    fun setColor(hex: String) {
        val norm = normalizeHex(hex) ?: return
        val rgb = hexToRgb(norm)
        setRGB(rgb.r, rgb.g, rgb.b, from = "external")
    }

    private fun setRGB(r: Int, g: Int, b: Int, from: String) {
        val rr = r.coerceIn(0, 255)
        val gg = g.coerceIn(0, 255)
        val bb = b.coerceIn(0, 255)

        if (from != "slider") {
            rSlider.value = rr.toString()
            gSlider.value = gg.toString()
            bSlider.value = bb.toString()
        }
        if (from != "number") {
            rNumber.value = rr.toString()
            gNumber.value = gg.toString()
            bNumber.value = bb.toString()
        }
        
        val hsl = rgbToHsl(rr, gg, bb)
        updateHSLUI(hsl.h, hsl.s, hsl.l)

        updateCommonUI(rr, gg, bb, from)
    }

    private fun setHSL(h: Int, s: Int, l: Int, from: String) {
        val hh = h.coerceIn(0, 360)
        val ss = s.coerceIn(0, 100)
        val ll = l.coerceIn(0, 100)

        if (from != "slider") {
            hSlider.value = hh.toString()
            sSlider.value = ss.toString()
            lSlider.value = ll.toString()
        }
        if (from != "number") {
            hNumber.value = hh.toString()
            sNumber.value = ss.toString()
            lNumber.value = ll.toString()
        }

        val rgb = hslToRgb(hh, ss, ll)
        updateRGBUI(rgb.r, rgb.g, rgb.b)

        updateCommonUI(rgb.r, rgb.g, rgb.b, from)
    }

    private fun updateRGBUI(r: Int, g: Int, b: Int) {
        rSlider.value = r.toString()
        gSlider.value = g.toString()
        bSlider.value = b.toString()
        rNumber.value = r.toString()
        gNumber.value = g.toString()
        bNumber.value = b.toString()
    }

    private fun updateHSLUI(h: Int, s: Int, l: Int) {
        hSlider.value = h.toString()
        sSlider.value = s.toString()
        lSlider.value = l.toString()
        hNumber.value = h.toString()
        sNumber.value = s.toString()
        lNumber.value = l.toString()
    }

    private fun updateCommonUI(r: Int, g: Int, b: Int, from: String) {
        val hex = "#${toHex2(r)}${toHex2(g)}${toHex2(b)}"
        
        colorPreview.style.backgroundColor = hex
        setSliderGradient()
        updateContrastInfo(r, g, b)
        updatePaletteSelection(hex)
    }

    private fun updatePaletteSelection(hex: String) {
        val swatches = container.querySelectorAll(".color-swatch")
        for (i in 0 until swatches.length) {
            val swatch = swatches.item(i) as HTMLElement
            val swatchColor = swatch.style.backgroundColor
            // Note: swatch.style.backgroundColor might be in rgb() format. 
            // However, Main.kt handles the 'selected' class which is more authoritative.
            // We'll just update the border/shadow if it matches exactly.
        }
    }

    private fun updateContrastInfo(r: Int, g: Int, b: Int) {
        fun getLuminance(rv: Int, gv: Int, bv: Int): Double {
            fun adjust(c: Int): Double {
                val v = c / 255.0
                return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
            }
            return adjust(rv) * 0.2126 + adjust(gv) * 0.7152 + adjust(bv) * 0.0722
        }

        val lum = getLuminance(r, g, b)
        val lumWhite = 1.0
        val lumBlack = 0.0

        val ratioWhite = (lumWhite + 0.05) / (lum + 0.05)
        val ratioWhiteRev = (lum + 0.05) / (lumWhite + 0.05)
        val finalWhite = if (ratioWhite > 1) ratioWhite else ratioWhiteRev

        val ratioBlack = (lum + 0.05) / (lumBlack + 0.05)
        val ratioBlackRev = (lumBlack + 0.05) / (lum + 0.05)
        val finalBlack = if (ratioBlack > 1) ratioBlack else ratioBlackRev

        contrastWhite?.textContent = "${((finalWhite * 10).toInt() / 10.0)}:1"
        contrastBlack?.textContent = "${((finalBlack * 10).toInt() / 10.0)}:1"

        updateWCAGBadge(wcagWhite, finalWhite)
        updateWCAGBadge(wcagBlack, finalBlack)
    }

    private fun updateWCAGBadge(el: HTMLSpanElement?, ratio: Double) {
        if (el == null) return
        val status = when {
            ratio >= 7.0 -> "AAA"
            ratio >= 4.5 -> "AA"
            ratio >= 3.0 -> "AA Large"
            else -> "FAIL"
        }
        el.textContent = status
        el.className = if (status == "FAIL") "wcag-fail" else "wcag-pass"
    }

    fun getValue(): String {
        val r = rSlider.value.toInt()
        val g = gSlider.value.toInt()
        val b = bSlider.value.toInt()
        return "#${toHex2(r)}${toHex2(g)}${toHex2(b)}"
    }

    fun updateStyles(uiColor: String) {
        container.style.color = uiColor
        colorPreview.style.borderColor = uiColor

        val labels = container.querySelectorAll(".picker-label")
        for (i in 0 until labels.length) {
            (labels.item(i) as HTMLElement).style.color = uiColor
        }
        
        val numbers = container.querySelectorAll(".picker-number")
        for (i in 0 until numbers.length) {
            (numbers.item(i) as HTMLElement).style.borderColor = uiColor
        }

        val tabs = container.querySelectorAll(".picker-tab")
        for (i in 0 until tabs.length) {
            (tabs.item(i) as HTMLElement).style.color = uiColor
            (tabs.item(i) as HTMLElement).style.borderColor = uiColor
        }

        val groupLabels = container.querySelectorAll(".group-label")
        for (i in 0 until groupLabels.length) {
            (groupLabels.item(i) as HTMLElement).style.color = uiColor
        }
    }
}
