import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.math.roundToInt

fun main() {
    val colors = listOf(
        "black",
        "white",
        "red",
        "green",
        "blue"
    )
    var currentIndex = 0
    var intensity = 100 // percent 0..100

    fun baseRgb(name: String): Triple<Int, Int, Int> = when (name) {
        "white" -> Triple(255, 255, 255)
        "red" -> Triple(255, 0, 0)
        "green" -> Triple(0, 255, 0)
        "blue" -> Triple(0, 0, 255)
        else -> Triple(0, 0, 0) // black or unknown
    }

    window.onload = {
        val body = document.body
        if (body != null) {
            val hint = document.getElementById("fullscreen-hint") as? HTMLDivElement
            val fullScreenBtn = document.getElementById("fullscreen-btn") as? HTMLButtonElement
            val enterIcon = fullScreenBtn?.querySelector(".enter-icon") as? HTMLElement
            val exitIcon = fullScreenBtn?.querySelector(".exit-icon") as? HTMLElement
            val slider = document.getElementById("intensity-slider") as? HTMLInputElement
            val sliderLabel = document.getElementById("intensity-label") as? HTMLDivElement
            val sliderContainer = document.getElementById("intensity-container") as? HTMLDivElement

            fun updateFullscreenIcons() {
                val isFullscreen = document.asDynamic().fullscreenElement != null ||
                        document.asDynamic().webkitFullscreenElement != null ||
                        document.asDynamic().mozFullScreenElement != null ||
                        document.asDynamic().msFullscreenElement != null

                if (isFullscreen) {
                    enterIcon?.style?.display = "none"
                    exitIcon?.style?.display = "block"
                } else {
                    enterIcon?.style?.display = "block"
                    exitIcon?.style?.display = "none"
                }
            }

            fun applyColor() {
                val (r0, g0, b0) = baseRgb(colors[currentIndex])
                val f = intensity.coerceIn(0, 100) / 100.0
                val r = (r0 * f).roundToInt()
                val g = (g0 * f).roundToInt()
                val b = (b0 * f).roundToInt()
                body.style.backgroundColor = "rgb(${r}, ${g}, ${b})"

                // compute luminance to select contrasting UI color
                val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
                val uiColor = if (luminance > 128) "black" else "white"
                body.style.color = uiColor
                fullScreenBtn?.style?.color = uiColor
                fullScreenBtn?.style?.borderColor = uiColor
                sliderContainer?.style?.color = uiColor
            }

            document.addEventListener("fullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("webkitfullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("mozfullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("MSFullscreenChange", { updateFullscreenIcons() })

            body.addEventListener("click", {
                currentIndex = (currentIndex + 1) % colors.size
                applyColor()

                // Hide hint after first click
                hint?.style?.display = "none"
            })

            // Prevent slider interactions from cycling the color
            sliderContainer?.addEventListener("click", { event ->
                event.stopPropagation()
            })
            slider?.addEventListener("click", { event ->
                event.stopPropagation()
            })
            slider?.addEventListener("input", {
                val value = slider.value?.toIntOrNull() ?: 100
                intensity = value
                sliderLabel?.textContent = "Intensity: ${value}%"
                applyColor()
            })

            fullScreenBtn?.onclick = { event ->
                event.stopPropagation() // Prevent cycling color when clicking the button

                val isFullscreen = document.asDynamic().fullscreenElement != null ||
                        document.asDynamic().webkitFullscreenElement != null ||
                        document.asDynamic().mozFullScreenElement != null ||
                        document.asDynamic().msFullscreenElement != null

                if (isFullscreen) {
                    val exitFullscreen = document.asDynamic().exitFullscreen ?:
                                         document.asDynamic().mozCancelFullScreen ?:
                                         document.asDynamic().webkitExitFullscreen ?:
                                         document.asDynamic().msExitFullscreen

                    if (exitFullscreen != null) {
                        exitFullscreen.call(document)
                    }
                } else {
                    val docEl = document.documentElement
                    if (docEl != null) {
                        val requestFullscreen = docEl.asDynamic().requestFullscreen ?:
                                                docEl.asDynamic().mozRequestFullScreen ?:
                                                docEl.asDynamic().webkitRequestFullscreen ?:
                                                docEl.asDynamic().msRequestFullscreen

                        if (requestFullscreen != null) {
                            requestFullscreen.call(docEl)
                        }
                    }
                }
            }

            // Initialize
            applyColor()
            sliderLabel?.textContent = "Intensity: ${intensity}%"
        }
    }
}
