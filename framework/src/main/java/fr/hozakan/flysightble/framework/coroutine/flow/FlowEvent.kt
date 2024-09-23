/**
 * SPDX-FileCopyrightText: Copyright (c) 2024 Qorvo, Inc.
 * SPDX-License-Identifier: LicenseRef-QORVO-2
 */

package com.qorvo.uwbtestapp.framework.coroutines.flow

/**
 * Used as a wrapper for data that is exposed via a Flow that represents an event.
 * An event should be dealt with only one time, so next collector to collect the flow won't have to deal with it.
 * This is useful when you want to display a snackbar/toast or any widget that should react on events
 */
class FlowEvent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

fun <T> T.asEvent(): FlowEvent<T> = FlowEvent(this)