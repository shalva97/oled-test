import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import kotlin.math.roundToInt

/*
    OLED Test App — Main entry

    Notes for future maintainers (Kotlin/JS specifics):
    - We intentionally avoid Kotlin stdlib collections and regex in hot paths because some
      runtimes mangle iterator/size/regex methods, causing "is not a function" crashes.
    - Wherever mutation or array length is needed, we store colors as a native JS array
      (via js("[]") or js("['#000', ...]")) and use .length/push/splice through dynamic.
    - Keep loops index-based and prefer primitive operations over high-level helpers to
      maximize runtime stability across bundlers and browsers.

    UX overview:
    - Centered color picker (RGB/HSL) + Add-to-List, below it the Saved Colors grid.
    - Keyboard shortcuts: C/Space/N next, P/Left prev, Up/Down grid jump, R random,
      H hide/show UI, F fullscreen, ? help. ESC closes overlays.
    - Mandatory colors are pinned at the start and non-removable: Black, White, R, G, B.
*/
fun main() {
    var colors = js("['#000000', '#FFFFFF', '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#00FFFF', '#FF00FF', 'random']")
    
    val mandatoryColors = arrayOf("#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF")

    // Load persisted data
    val storage = window.localStorage
    val savedColors = storage.getItem("oled-test-colors")
    if (savedColors != null) {
        try {
            val resultList = js("[]")
            // Always start with mandatory colors
            for (c in mandatoryColors) {
                resultList.push(c)
            }

            val jsParts = savedColors.asDynamic().split(",")
            val len = (jsParts.length as Int)
            for (i in 0 until len) {
                val p = jsParts[i] as String
                if (p.isNotBlank() && p != "random") {
                    // Check if already in mandatory list to avoid duplicates
                    var isMandatory = false
                    for (mc in mandatoryColors) {
                        if (mc == p) {
                            isMandatory = true
                            break
                        }
                    }
                    if (!isMandatory) {
                        resultList.push(p)
                    }
                }
            }
            
            // Always push "random" at the end
            resultList.push("random")

            if ((resultList.length as Int) > 0) {
                colors = resultList
            }
        } catch (e: Exception) {}
    }

    var currentIndex = 0
    val savedIndex = storage.getItem("oled-test-index")?.toIntOrNull()
    if (savedIndex != null && savedIndex < (colors.length as Int)) {
        currentIndex = savedIndex
    }

    fun persist() {
        val colorsList = mutableListOf<String>()
        val len = (colors.length as Int)
        for (i in 0 until len) {
            colorsList.add(colors[i] as String)
        }
        storage.setItem("oled-test-colors", colorsList.joinToString(","))
        storage.setItem("oled-test-index", currentIndex.toString())
    }

    fun parseColor(color: String): ColorRGB {
        return when {
            color.startsWith("#") -> {
                val hex = color.removePrefix("#")
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                ColorRGB(r, g, b)
            }
            color == "white" -> ColorRGB(255, 255, 255)
            color == "black" -> ColorRGB(0, 0, 0)
            color == "red" -> ColorRGB(255, 0, 0)
            color == "green" -> ColorRGB(0, 255, 0)
            color == "blue" -> ColorRGB(0, 0, 255)
            color == "yellow" -> ColorRGB(255, 255, 0)
            color == "cyan" -> ColorRGB(0, 255, 255)
            color == "magenta" -> ColorRGB(255, 0, 255)
            else -> ColorRGB(0, 0, 0) // unknown
        }
    }

    window.onload = {
        val body = document.body
        if (body != null) {
            val hint = document.getElementById("fullscreen-hint") as? HTMLDivElement
            val fullScreenBtn = document.getElementById("fullscreen-btn") as? HTMLButtonElement
            val enterIcon = fullScreenBtn?.querySelector(".enter-icon") as? HTMLElement
            val exitIcon = fullScreenBtn?.querySelector(".exit-icon") as? HTMLElement
            val colorListContainer = document.getElementById("color-list") as? HTMLDivElement
            val colorSelectorContainer = document.getElementById("color-selector-container") as? HTMLDivElement
            val addColorBtn = document.getElementById("add-color-btn") as? HTMLButtonElement
            val customColorPickerContainer = document.getElementById("custom-color-picker-container") as? HTMLDivElement
            val activeHexDisplay = document.getElementById("active-hex") as? HTMLElement
            val activeRgbDisplay = document.getElementById("active-rgb") as? HTMLElement
            val activeColorInfo = document.getElementById("active-color-info") as? HTMLDivElement
            val copyInfoBtn = document.getElementById("copy-info-btn") as? HTMLButtonElement
            val prevBtn = document.getElementById("prev-btn") as? HTMLButtonElement
            val nextBtn = document.getElementById("next-btn") as? HTMLButtonElement
            val helpBtn = document.getElementById("help-btn") as? HTMLButtonElement
            val helpOverlay = document.getElementById("help-overlay") as? HTMLDivElement
            val helpCloseBtn = helpOverlay?.querySelector(".help-close-btn") as? HTMLButtonElement
            val settingsBtn = document.getElementById("settings-btn") as? HTMLButtonElement
            val settingsOverlay = document.getElementById("settings-overlay") as? HTMLDivElement
            val settingsCloseBtn = settingsOverlay?.querySelector(".settings-close-btn") as? HTMLButtonElement
            val resetColorsBtn = document.getElementById("reset-colors-btn") as? HTMLButtonElement

            val colorPicker = customColorPickerContainer?.let { ColorPicker(it) }

            var isUiHidden = false

            fun toggleUi() {
                isUiHidden = !isUiHidden
                if (isUiHidden) {
                    body.classList.add("ui-hidden")
                } else {
                    body.classList.remove("ui-hidden")
                }
            }

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

            fun toHex2(v: Int) = v.toString(16).padStart(2, '0').uppercase()

            fun applyColor() {
                val len = (colors.length as Int)
                var colorStr = colors[currentIndex] as String
                
                val r: Int
                val g: Int
                val b: Int

                if (colorStr == "random") {
                    r = js("Math.floor(Math.random() * 256)") as Int
                    g = js("Math.floor(Math.random() * 256)") as Int
                    b = js("Math.floor(Math.random() * 256)") as Int
                    colorStr = "#${toHex2(r)}${toHex2(g)}${toHex2(b)}"
                } else {
                    val rgb0 = parseColor(colorStr)
                    r = rgb0.r
                    g = rgb0.g
                    b = rgb0.b
                }
                
                body.style.backgroundColor = "rgb(${r}, ${g}, ${b})"

                // Update active color info
                activeHexDisplay?.textContent = "#${toHex2(r)}${toHex2(g)}${toHex2(b)}"
                activeRgbDisplay?.textContent = "$r, $g, $b"

                // compute luminance to select contrasting UI color
                val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
                val uiColor = if (luminance > 128) "black" else "white"
                body.style.color = uiColor
                fullScreenBtn?.style?.color = uiColor
                fullScreenBtn?.style?.borderColor = uiColor
                colorSelectorContainer?.style?.color = uiColor
                addColorBtn?.style?.color = uiColor
                addColorBtn?.style?.borderColor = uiColor
                activeColorInfo?.style?.color = uiColor
                activeColorInfo?.style?.borderColor = uiColor.let { 
                    if (it == "white") "rgba(255,255,255,0.2)" else "rgba(0,0,0,0.2)"
                }
                copyInfoBtn?.style?.borderColor = uiColor.let {
                    if (it == "white") "rgba(255,255,255,0.2)" else "rgba(0,0,0,0.2)"
                }
                prevBtn?.style?.color = uiColor
                nextBtn?.style?.color = uiColor
                helpBtn?.style?.color = uiColor
                helpBtn?.style?.borderColor = uiColor
                settingsBtn?.style?.color = uiColor
                settingsBtn?.style?.borderColor = uiColor
                colorPicker?.updateStyles(uiColor)
                colorPicker?.setColor(colorStr)

                // Update selected icon in the list
                val swatches = document.querySelectorAll(".color-swatch")
                for (i in 0 until swatches.length) {
                    val swatch = swatches.item(i) as HTMLElement
                    val swatchColor = colors[i] as String
                    if (i == currentIndex) {
                        swatch.classList.add("selected")
                    } else {
                        swatch.classList.remove("selected")
                    }
                    
                    // Maintain dice icon for random swatch, or handle checkmark for others
                    if (swatchColor == "random") {
                        swatch.innerHTML = """<svg viewBox="0 0 24 24"><path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M7,7c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S6.45,7,7,7z M7,15c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S6.45,15,7,15z M12,11c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S11.45,11,12,11z M17,7c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S16.45,7,17,7z M17,15c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S16.45,15,17,15z"/></svg>"""
                    } else {
                        swatch.innerHTML = "" // Clear for normal swatches (CSS handles checkmark)
                    }
                    
                    swatch.style.color = uiColor // Icon color
                }
            }

            fun renderColorList(lastAddedIndex: Int? = null) {
                if (colorListContainer == null) return
                colorListContainer.innerHTML = ""
                val len = (colors.length as Int)
                for (index in 0 until len) {
                    val color = colors[index] as String
                    val swatch = document.createElement("div") as HTMLDivElement
                    swatch.className = "color-swatch"
                    if (index == lastAddedIndex) {
                        swatch.classList.add("swatch-new")
                    }
                    if (color == "random") {
                        swatch.classList.add("swatch-random")
                        swatch.title = "Random Color"
                        swatch.innerHTML = """<svg viewBox="0 0 24 24"><path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M7,7c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S6.45,7,7,7z M7,15c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S6.45,15,7,15z M12,11c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S11.45,11,12,11z M17,7c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S16.45,7,17,7z M17,15c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S16.45,15,17,15z"/></svg>"""
                    } else {
                        swatch.style.backgroundColor = color
                        swatch.title = color.replaceFirstChar { it.uppercase() }
                    }
                    
                    if (index >= 5 && color != "random") {
                        val removeBtn = document.createElement("div") as HTMLDivElement
                        removeBtn.className = "remove-btn"
                        removeBtn.textContent = "×"
                        removeBtn.title = "Remove Color"
                        removeBtn.onclick = { ev ->
                            ev.stopPropagation()
                            colors.splice(index, 1)
                            val newLen = (colors.length as Int)
                            if (currentIndex >= newLen) {
                                currentIndex = newLen - 1
                            }
                            renderColorList()
                            applyColor()
                            persist()
                        }
                        swatch.appendChild(removeBtn)
                    }

                    swatch.onclick = { event ->
                        event.stopPropagation()
                        currentIndex = index
                        applyColor()
                        persist()
                        hint?.style?.display = "none"
                    }
                    colorListContainer.appendChild(swatch)
                }
            }

            document.addEventListener("fullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("webkitfullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("mozfullscreenchange", { updateFullscreenIcons() })
            document.addEventListener("MSFullscreenChange", { updateFullscreenIcons() })

            copyInfoBtn?.onclick = { event ->
                event.stopPropagation()
                val hex = activeHexDisplay?.textContent ?: ""
                val rgb = activeRgbDisplay?.textContent ?: ""
                val textToCopy = "Hex: $hex, RGB: ($rgb)"
                window.navigator.clipboard.writeText(textToCopy).then {
                    val oldText = copyInfoBtn.textContent
                    copyInfoBtn.textContent = "Copied!"
                    window.setTimeout({ copyInfoBtn.textContent = oldText }, 1500)
                }
            }

            prevBtn?.onclick = { event ->
                event.stopPropagation()
                val len = (colors.length as Int)
                currentIndex = (currentIndex - 1 + len) % len
                applyColor()
                hint?.style?.display = "none"
            }

            nextBtn?.onclick = { event ->
                event.stopPropagation()
                body.click()
            }

            helpBtn?.onclick = { event ->
                event.stopPropagation()
                helpOverlay?.style?.display = "flex"
            }

            helpCloseBtn?.onclick = { event ->
                event.stopPropagation()
                helpOverlay?.style?.display = "none"
            }

            helpOverlay?.onclick = { event ->
                event.stopPropagation()
                helpOverlay.style.display = "none"
            }
            
            settingsBtn?.onclick = { event ->
                event.stopPropagation()
                settingsOverlay?.style?.display = "flex"
            }

            settingsCloseBtn?.onclick = { event ->
                event.stopPropagation()
                settingsOverlay?.style?.display = "none"
            }

            settingsOverlay?.onclick = { event ->
                event.stopPropagation()
                settingsOverlay.style.display = "none"
            }

            resetColorsBtn?.onclick = { event ->
                event.stopPropagation()
                if (window.confirm("Are you sure you want to reset the color list? This will remove all your custom colors but keep the standard test colors.")) {
                    // Create a new JS array with only mandatory colors and "random"
                    val resetList = js("[]")
                    for (c in mandatoryColors) {
                        resetList.push(c)
                    }
                    resetList.push("random")
                    
                    colors = resetList
                    currentIndex = 0
                    renderColorList()
                    applyColor()
                    persist()
                    
                    settingsOverlay?.style?.display = "none"
                }
            }
            
            helpOverlay?.querySelector(".help-content")?.addEventListener("click", { it.stopPropagation() })
            settingsOverlay?.querySelector(".settings-content")?.addEventListener("click", { it.stopPropagation() })

            body.addEventListener("click", {
                val len = (colors.length as Int)
                if (isUiHidden) {
                    toggleUi()
                } else {
                    val currentColor = colors[currentIndex] as String
                    if (currentColor == "random") {
                        // Stay on random, just re-apply for a new color
                    } else {
                        currentIndex = (currentIndex + 1) % len
                    }
                    applyColor()
                    persist()
                }

                // Hide hint after first click
                hint?.style?.display = "none"
            })

            window.addEventListener("keydown", { event ->
                val keyEvent = event as org.w3c.dom.events.KeyboardEvent
                val key = keyEvent.key.lowercase()

                // If typing in an input, don't trigger shortcuts
                val target = event.target as? HTMLElement
                if (target?.tagName == "INPUT") return@addEventListener

                when (key) {
                    "escape" -> {
                        helpOverlay?.style?.display = "none"
                        settingsOverlay?.style?.display = "none"
                    }
                    "f" -> fullScreenBtn?.click()
                    "h" -> toggleUi()
                    "?" -> if (!isUiHidden) helpBtn?.click()
                    "c", " ", "arrowright", "n" -> {
                        if (isUiHidden) {
                            val len = (colors.length as Int)
                            val currentColor = colors[currentIndex] as String
                            if (currentColor == "random") {
                                // Stay on random
                            } else {
                                currentIndex = (currentIndex + 1) % len
                            }
                            applyColor()
                            persist()
                        } else {
                            body.click()
                        }
                    }
                    "arrowleft", "p" -> {
                        val len = (colors.length as Int)
                        currentIndex = (currentIndex - 1 + len) % len
                        applyColor()
                        persist()
                        hint?.style?.display = "none"
                    }
                    "arrowup" -> {
                        currentIndex = (currentIndex - 4).coerceAtLeast(0)
                        applyColor()
                        persist()
                        hint?.style?.display = "none"
                    }
                    "arrowdown" -> {
                        val len = (colors.length as Int)
                        currentIndex = (currentIndex + 4).coerceAtMost(len - 1)
                        applyColor()
                        persist()
                        hint?.style?.display = "none"
                    }
                    "r" -> {
                        // Find index of "random" or just show a random color
                        var randomIdx = -1
                        val len = (colors.length as Int)
                        for (i in 0 until len) {
                            if (colors[i] == "random") {
                                randomIdx = i
                                break
                            }
                        }
                        
                        if (randomIdx != -1) {
                            currentIndex = randomIdx
                            applyColor()
                            persist()
                        }
                    }
                    else -> {
                        // Direct color selection with 1-9 keys
                        val digit = key.toIntOrNull()
                        if (digit != null && digit >= 1 && digit <= 9) {
                            val colorIdx = digit - 1
                            val len = (colors.length as Int)
                            if (colorIdx < len) {
                                currentIndex = colorIdx
                                applyColor()
                                persist()
                                hint?.style?.display = "none"
                            }
                        }
                    }
                }
            })

            // Prevent interactions from cycling the color
            colorSelectorContainer?.addEventListener("click", { event -> event.stopPropagation() })

            addColorBtn?.onclick = { event ->
                event.stopPropagation()
                val newColor = colorPicker?.getValue()
                if (newColor != null) {
                    var exists = false
                    val len = (colors.length as Int)
                    for (i in 0 until len) {
                        val colorAt = colors[i] as String
                        if (colorAt.uppercase() == newColor.uppercase()) {
                            exists = true
                            currentIndex = i
                            break
                        }
                    }
                    
                    if (!exists) {
                        // Insert before "random" which should be at the end
                        var randomIdx = -1
                        val currentLen = (colors.length as Int)
                        for (i in 0 until currentLen) {
                            if (colors[i] == "random") {
                                randomIdx = i
                                break
                            }
                        }
                        
                        if (randomIdx != -1) {
                            colors.splice(randomIdx, 0, newColor)
                            currentIndex = randomIdx
                        } else {
                            colors.push(newColor)
                            currentIndex = (colors.length as Int) - 1
                        }
                        
                        renderColorList(currentIndex)
                        persist()
                    }
                    applyColor()
                }
            }


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
            renderColorList()
            applyColor()
        }
    }
}
