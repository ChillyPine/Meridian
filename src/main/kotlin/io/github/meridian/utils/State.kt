package io.github.meridian.utils

import io.github.meridian.Meridian
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

val userIGN: String? get() = Meridian.mc.user?.name
val userUUID: UUID? get() = Meridian.mc.user?.profileId
/**
 * Alternatively, we can use the below one-liner to get both values.
 * val gameProfile: com.mojang.authlib.GameProfile? get() = Meridian.mc.gameProfile
 * // gameProfile?.name  and  gameProfile?.id
*/
val DEV_UUIDS = setOf(
    UUID.fromString("e6d1d332-8c7c-4bcc-9a22-69f369a16fc8"), // Dawn
    UUID.fromString("3575d27b-2197-4a55-b6ae-60ebf17842bf") // Chilly
)
val isDeveloper: Boolean get() = userUUID in DEV_UUIDS

interface State<T> {
    var value: T
    fun listen(cb: (v: T) -> Unit)
    fun <R> map(transform: (v: T) -> R): State<R>
    fun <O, R> zip(other: State<O>, transform: (a: T, b: O) -> R): State<R>
}

open class BasicState<T>(initial: T) : State<T> {
    private val ref = AtomicReference(initial)
    private val listeners = CopyOnWriteArrayList<(v: T) -> Unit>()

    override var value: T
        get() = ref.get()
        set(value) {
            if (ref.getAndSet(value) != value) listeners.forEach { it(value) }
        }

    override fun listen(cb: (v: T) -> Unit) {
        listeners.add(cb)
    }

    override fun <R> map(transform: (v: T) -> R) = UnaryDerivedState(this, transform)

    override fun <O, R> zip(other: State<O>, transform: (a: T, b: O) -> R) =
        BinaryDerivedState(this, other, transform)
}

class UnaryDerivedState<T, R>(
    base: State<T>,
    private val transform: (v: T) -> R
) : BasicState<R>(transform(base.value)) {
    override var value: R
        get() = super.value
        set(value) {
            throw UnsupportedOperationException("derived state is read-only")
        }

    init {
        base.listen { super.value = transform(it) }
    }
}

class BinaryDerivedState<T1, T2, R>(
    private val base1: State<T1>,
    private val base2: State<T2>,
    private val transform: (a: T1, b: T2) -> R
) : BasicState<R>(transform(base1.value, base2.value)) {
    override var value: R
        get() = super.value
        set(value) {
            throw UnsupportedOperationException("derived state is read-only")
        }

    init {
        base1.listen { super.value = transform(it, base2.value) }
        base2.listen { super.value = transform(base1.value, it) }
    }
}
