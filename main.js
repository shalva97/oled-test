import { ColorPicker } from './ColorPicker.js';

let colors = ['#000000', '#FFFFFF', '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#00FFFF', '#FF00FF', 'random'];
const mandatoryColors = ["#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF"];

// Load persisted data
const storage = window.localStorage;
const savedColors = storage.getItem("oled-test-colors");
if (savedColors) {
    try {
        const resultList = [];
        // Always start with mandatory colors
        mandatoryColors.forEach(c => resultList.push(c));

        const jsParts = savedColors.split(",");
        jsParts.forEach(p => {
            if (p.trim() && p !== "random") {
                if (!mandatoryColors.includes(p)) {
                    resultList.push(p);
                }
            }
        });

        // Always push "random" at the end
        resultList.push("random");

        if (resultList.length > 0) {
            colors = resultList;
        }
    } catch (e) { console.error("Failed to load saved colors", e); }
}

let currentIndex = 0;
const savedIndex = parseInt(storage.getItem("oled-test-index"));
if (!isNaN(savedIndex) && savedIndex < colors.length) {
    currentIndex = savedIndex;
}

function persist() {
    storage.setItem("oled-test-colors", colors.join(","));
    storage.setItem("oled-test-index", currentIndex.toString());
}

function parseColor(color) {
    if (color.startsWith("#")) {
        const hex = color.substring(1);
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);
        return { r, g, b };
    }
    const names = {
        white: { r: 255, g: 255, b: 255 },
        black: { r: 0, g: 0, b: 0 },
        red: { r: 255, g: 0, b: 0 },
        green: { r: 0, g: 255, b: 0 },
        blue: { r: 0, g: 0, b: 255 },
        yellow: { r: 255, g: 255, b: 0 },
        cyan: { r: 0, g: 255, b: 255 },
        magenta: { r: 255, g: 0, b: 255 }
    };
    return names[color] || { r: 0, g: 0, b: 0 };
}

window.onload = () => {
    const body = document.body;
    const hint = document.getElementById("fullscreen-hint");
    const fullScreenBtn = document.getElementById("fullscreen-btn");
    const enterIcon = fullScreenBtn?.querySelector(".enter-icon");
    const exitIcon = fullScreenBtn?.querySelector(".exit-icon");
    const colorListContainer = document.getElementById("color-list");
    const colorSelectorContainer = document.getElementById("color-selector-container");
    const addColorBtn = document.getElementById("add-color-btn");
    const customColorPickerContainer = document.getElementById("custom-color-picker-container");
    const activeHexDisplay = document.getElementById("active-hex");
    const activeRgbDisplay = document.getElementById("active-rgb");
    const activeColorInfo = document.getElementById("active-color-info");
    const copyInfoBtn = document.getElementById("copy-info-btn");
    const prevBtn = document.getElementById("prev-btn");
    const nextBtn = document.getElementById("next-btn");
    const helpBtn = document.getElementById("help-btn");
    const helpOverlay = document.getElementById("help-overlay");
    const helpCloseBtn = helpOverlay?.querySelector(".help-close-btn");
    const settingsBtn = document.getElementById("settings-btn");
    const settingsOverlay = document.getElementById("settings-overlay");
    const settingsCloseBtn = settingsOverlay?.querySelector(".settings-close-btn");
    const resetColorsBtn = document.getElementById("reset-colors-btn");

    const colorPicker = customColorPickerContainer ? new ColorPicker(customColorPickerContainer) : null;

    let isUiHidden = false;

    function toggleUi() {
        isUiHidden = !isUiHidden;
        if (isUiHidden) {
            body.classList.add("ui-hidden");
        } else {
            body.classList.remove("ui-hidden");
        }
    }

    function updateFullscreenIcons() {
        const isFullscreen = !!(document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement);
        if (enterIcon) enterIcon.style.display = isFullscreen ? "none" : "block";
        if (exitIcon) exitIcon.style.display = isFullscreen ? "block" : "none";
    }

    function toHex2(v) {
        return v.toString(16).padStart(2, '0').toUpperCase();
    }

    function applyColor() {
        let colorStr = colors[currentIndex];
        let r, g, b;

        if (colorStr === "random") {
            r = Math.floor(Math.random() * 256);
            g = Math.floor(Math.random() * 256);
            b = Math.floor(Math.random() * 256);
            colorStr = `#${toHex2(r)}${toHex2(g)}${toHex2(b)}`;
        } else {
            const rgb = parseColor(colorStr);
            r = rgb.r;
            g = rgb.g;
            b = rgb.b;
        }

        body.style.backgroundColor = `rgb(${r}, ${g}, ${b})`;

        if (activeHexDisplay) activeHexDisplay.textContent = `#${toHex2(r)}${toHex2(g)}${toHex2(b)}`;
        if (activeRgbDisplay) activeRgbDisplay.textContent = `${r}, ${g}, ${b}`;

        const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        const uiColor = luminance > 128 ? "black" : "white";
        body.style.color = uiColor;

        const themedElements = [
            fullScreenBtn, colorSelectorContainer, addColorBtn, activeColorInfo,
            prevBtn, nextBtn, helpBtn, settingsBtn
        ];
        themedElements.forEach(el => {
            if (el) {
                el.style.color = uiColor;
                if (el.style.borderColor !== undefined) el.style.borderColor = uiColor;
            }
        });

        if (activeColorInfo) activeColorInfo.style.borderColor = uiColor === "white" ? "rgba(255,255,255,0.2)" : "rgba(0,0,0,0.2)";
        if (copyInfoBtn) copyInfoBtn.style.borderColor = uiColor === "white" ? "rgba(255,255,255,0.2)" : "rgba(0,0,0,0.2)";

        colorPicker?.updateStyles(uiColor);
        colorPicker?.setColor(colorStr);

        // Update selected icon in the list
        const swatches = document.querySelectorAll(".color-swatch");
        swatches.forEach((swatch, i) => {
            const swatchColor = colors[i];
            if (i === currentIndex) {
                swatch.classList.add("selected");
            } else {
                swatch.classList.remove("selected");
            }

            if (swatchColor === "random") {
                swatch.innerHTML = `<svg viewBox="0 0 24 24"><path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M7,7c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S6.45,7,7,7z M7,15c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S6.45,15,7,15z M12,11c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S11.45,11,12,11z M17,7c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S16.45,7,17,7z M17,15c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S16.45,15,17,15z"/></svg>`;
            } else {
                swatch.innerHTML = "";
            }
            swatch.style.color = uiColor;
        });
    }

    function renderColorList(lastAddedIndex = null) {
        if (!colorListContainer) return;
        colorListContainer.innerHTML = "";
        colors.forEach((color, index) => {
            const swatch = document.createElement("div");
            swatch.className = "color-swatch";
            if (index === lastAddedIndex) {
                swatch.classList.add("swatch-new");
            }
            if (color === "random") {
                swatch.classList.add("swatch-random");
                swatch.title = "Random Color";
                swatch.innerHTML = `<svg viewBox="0 0 24 24"><path d="M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M7,7c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S6.45,7,7,7z M7,15c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S6.45,15,7,15z M12,11c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S11.45,11,12,11z M17,7c0.55,0,1,0.45,1,1s-0.45,1-1,1s-1-0.45-1-1S16.45,7,17,7z M17,15c0.55,0,1,0.45,1,1s-0.45,1-1,1 s-1-0.45-1-1S16.45,15,17,15z"/></svg>`;
            } else {
                swatch.style.backgroundColor = color;
                swatch.title = color.charAt(0).toUpperCase() + color.slice(1);
            }

            if (index >= 5 && color !== "random") {
                const removeBtn = document.createElement("div");
                removeBtn.className = "remove-btn";
                removeBtn.textContent = "×";
                removeBtn.title = "Remove Color";
                removeBtn.onclick = (ev) => {
                    ev.stopPropagation();
                    colors.splice(index, 1);
                    if (currentIndex >= colors.length) {
                        currentIndex = colors.length - 1;
                    }
                    renderColorList();
                    applyColor();
                    persist();
                };
                swatch.appendChild(removeBtn);
            }

            swatch.onclick = (event) => {
                event.stopPropagation();
                currentIndex = index;
                applyColor();
                persist();
                if (hint) hint.style.display = "none";
            };
            colorListContainer.appendChild(swatch);
        });
    }

    ["fullscreenchange", "webkitfullscreenchange", "mozfullscreenchange", "MSFullscreenChange"].forEach(evt => {
        document.addEventListener(evt, updateFullscreenIcons);
    });

    if (copyInfoBtn) {
        copyInfoBtn.onclick = (event) => {
            event.stopPropagation();
            const hex = activeHexDisplay?.textContent || "";
            const rgb = activeRgbDisplay?.textContent || "";
            const textToCopy = `Hex: ${hex}, RGB: (${rgb})`;
            window.navigator.clipboard.writeText(textToCopy).then(() => {
                const oldText = copyInfoBtn.textContent;
                copyInfoBtn.textContent = "Copied!";
                window.setTimeout(() => { copyInfoBtn.textContent = oldText; }, 1500);
            });
        };
    }

    if (prevBtn) {
        prevBtn.onclick = (event) => {
            event.stopPropagation();
            currentIndex = (currentIndex - 1 + colors.length) % colors.length;
            applyColor();
            if (hint) hint.style.display = "none";
        };
    }

    if (nextBtn) {
        nextBtn.onclick = (event) => {
            event.stopPropagation();
            body.click();
        };
    }

    if (helpBtn) {
        helpBtn.onclick = (event) => {
            event.stopPropagation();
            if (helpOverlay) helpOverlay.style.display = "flex";
        };
    }

    if (helpCloseBtn) {
        helpCloseBtn.onclick = (event) => {
            event.stopPropagation();
            if (helpOverlay) helpOverlay.style.display = "none";
        };
    }

    if (helpOverlay) {
        helpOverlay.onclick = (event) => {
            event.stopPropagation();
            helpOverlay.style.display = "none";
        };
        helpOverlay.querySelector(".help-content")?.addEventListener("click", e => e.stopPropagation());
    }

    if (settingsBtn) {
        settingsBtn.onclick = (event) => {
            event.stopPropagation();
            if (settingsOverlay) settingsOverlay.style.display = "flex";
        };
    }

    if (settingsCloseBtn) {
        settingsCloseBtn.onclick = (event) => {
            event.stopPropagation();
            if (settingsOverlay) settingsOverlay.style.display = "none";
        };
    }

    if (settingsOverlay) {
        settingsOverlay.onclick = (event) => {
            event.stopPropagation();
            settingsOverlay.style.display = "none";
        };
        settingsOverlay.querySelector(".settings-content")?.addEventListener("click", e => e.stopPropagation());
    }

    if (resetColorsBtn) {
        resetColorsBtn.onclick = (event) => {
            event.stopPropagation();
            if (window.confirm("Are you sure you want to reset the color list?")) {
                colors = [...mandatoryColors, "random"];
                currentIndex = 0;
                renderColorList();
                applyColor();
                persist();
                if (settingsOverlay) settingsOverlay.style.display = "none";
            }
        };
    }

    body.addEventListener("click", () => {
        if (isUiHidden) {
            toggleUi();
        } else {
            const currentColor = colors[currentIndex];
            if (currentColor !== "random") {
                currentIndex = (currentIndex + 1) % colors.length;
            }
            applyColor();
            persist();
        }
        if (hint) hint.style.display = "none";
    });

    window.addEventListener("keydown", (event) => {
        const key = event.key.toLowerCase();
        if (event.target.tagName === "INPUT") return;

        switch (key) {
            case "escape":
                if (helpOverlay) helpOverlay.style.display = "none";
                if (settingsOverlay) settingsOverlay.style.display = "none";
                break;
            case "f": fullScreenBtn?.click(); break;
            case "h": toggleUi(); break;
            case "?": if (!isUiHidden) helpBtn?.click(); break;
            case "c": case " ": case "arrowright": case "n":
                if (isUiHidden) {
                    const currentColor = colors[currentIndex];
                    if (currentColor !== "random") {
                        currentIndex = (currentIndex + 1) % colors.length;
                    }
                    applyColor();
                    persist();
                } else {
                    body.click();
                }
                break;
            case "arrowleft": case "p":
                currentIndex = (currentIndex - 1 + colors.length) % colors.length;
                applyColor();
                persist();
                if (hint) hint.style.display = "none";
                break;
            case "arrowup":
                currentIndex = Math.max(currentIndex - 4, 0);
                applyColor();
                persist();
                if (hint) hint.style.display = "none";
                break;
            case "arrowdown":
                currentIndex = Math.min(currentIndex + 4, colors.length - 1);
                applyColor();
                persist();
                if (hint) hint.style.display = "none";
                break;
            case "r":
                const randomIdx = colors.indexOf("random");
                if (randomIdx !== -1) {
                    currentIndex = randomIdx;
                    applyColor();
                    persist();
                }
                break;
            default:
                const digit = parseInt(key);
                if (!isNaN(digit) && digit >= 1 && digit <= 9) {
                    const colorIdx = digit - 1;
                    if (colorIdx < colors.length) {
                        currentIndex = colorIdx;
                        applyColor();
                        persist();
                        if (hint) hint.style.display = "none";
                    }
                }
        }
    });

    colorSelectorContainer?.addEventListener("click", e => e.stopPropagation());

    if (addColorBtn) {
        addColorBtn.onclick = (event) => {
            event.stopPropagation();
            const newColor = colorPicker?.getValue();
            if (newColor) {
                const existingIdx = colors.findIndex(c => c.toUpperCase() === newColor.toUpperCase());
                if (existingIdx !== -1) {
                    currentIndex = existingIdx;
                } else {
                    const randomIdx = colors.indexOf("random");
                    if (randomIdx !== -1) {
                        colors.splice(randomIdx, 0, newColor);
                        currentIndex = randomIdx;
                    } else {
                        colors.push(newColor);
                        currentIndex = colors.length - 1;
                    }
                    renderColorList(currentIndex);
                    persist();
                }
                applyColor();
            }
        };
    }

    if (fullScreenBtn) {
        fullScreenBtn.onclick = (event) => {
            event.stopPropagation();
            const isFullscreen = !!(document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement);
            if (isFullscreen) {
                const exitFullscreen = document.exitFullscreen || document.mozCancelFullScreen || document.webkitExitFullscreen || document.msExitFullscreen;
                exitFullscreen?.call(document);
            } else {
                const docEl = document.documentElement;
                const requestFullscreen = docEl.requestFullscreen || docEl.mozRequestFullScreen || docEl.webkitRequestFullscreen || docEl.msRequestFullscreen;
                requestFullscreen?.call(docEl);
            }
        };
    }

    renderColorList();
    applyColor();
};
