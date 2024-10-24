package com.coco.domain.utils

inline fun <K : Comparable<K>, T : Any> List<T>.findByBinarySearch(
    key: K,
    crossinline selector: (T) -> K,
): T? = this.getOrNull(this.binarySearchBy(key) { selector(it) })
