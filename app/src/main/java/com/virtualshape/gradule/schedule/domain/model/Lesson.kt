package com.virtualshape.gradule.schedule.domain.model

data class Lesson(
    val id: String,
    val subject: String,
    val teacher: String,
    val room: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekType: String
)