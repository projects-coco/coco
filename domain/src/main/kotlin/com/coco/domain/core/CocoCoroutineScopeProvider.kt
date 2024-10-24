package com.coco.domain.core

import kotlinx.coroutines.CoroutineScope

interface CocoCoroutineScopeProvider {
    fun provide(): CoroutineScope
}
