package io.github.meridian.utils

abstract class Toggleable {
    @Volatile private var attached = false

    protected abstract fun add()
    protected abstract fun remove()

    private fun update(active: Boolean) {
        if (active) {
            if (!attached) { add(); attached = true }
        } else {
            if (attached) { remove(); attached = false }
        }
    }

    fun bind(state: State<Boolean>) = apply {
        state.listen(::update)
        update(state.value)
    }
}
