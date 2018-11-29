package com.cartlc.tracker.model.event

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class LiveEvent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Executes function exactly once.
     */
    fun executeIfNotHandled(func: ((T) -> Unit)) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            func(content)
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}