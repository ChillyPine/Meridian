package io.github.meridian.utils

import io.github.meridian.Meridian.mc

// Since 26.3 (SDL), charTyped only fires while SDL text input is started. Vanilla
// starts it for EditBox/MultiLineEditBox only, so every custom text widget must
// report its own focus changes. Stopping is owner-checked, and Gui.setScreen stops
// text input on every screen change, so a widget that never unfocuses can't leak it.
fun setTextInputFocus(owner: Any, focused: Boolean) {
    mc.textInputManager().onTextInputFocusChange(owner, focused)
}
