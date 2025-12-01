package com.jotadev.mediflow.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AppEvents {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events

    fun emit(event: String) {
        _events.tryEmit(event)
    }
}