package com.coco.domain

import com.coco.domain.utils.StringUtils.newBase32Random

fun genString(len: Int = 8) = newBase32Random(len)
