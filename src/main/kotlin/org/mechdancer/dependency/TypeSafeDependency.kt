package org.mechdancer.dependency

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Dependency declaration
 *
 * It holds the type [T] and the reference of the [Component].
 * The implementation is thread-safe.
 */
sealed class TypeSafeDependency<T : Component>(
    val type: KClass<T>,
    private val predicate: (T) -> Boolean
) {
    private val _field = AtomicReference<T?>(null)

    /**
     * Try to set [value]
     *
     * Fail if unable to cast [value] to desired type or the predication fails
     */
    fun set(value: Component): T? =
        _field.updateAndGet {
            type.safeCast(value)?.takeIf(predicate) ?: it
        }

    /**
     * Try to get the value
     */
    open val field: T? get() = _field.get()

    /**
     * Weak dependency with type [T]
     */
    class WeakDependency<T : Component>(type: KClass<T>, predicate: (T) -> Boolean) :
        TypeSafeDependency<T>(type, predicate)

    /**
     * Strict dependency with type [T]
     */
    class Dependency<T : Component>(type: KClass<T>, predicate: (T) -> Boolean) :
        TypeSafeDependency<T>(type, predicate) {
        /**
         * Try to get the value
         * @throws ComponentNotExistException if unable to get
         */
        override val field: T get() = super.field ?: throw ComponentNotExistException(type)
    }

    override fun equals(other: Any?) = this === other || (other as? TypeSafeDependency<*>)?.type == type
    override fun hashCode() = type.hashCode()
}
