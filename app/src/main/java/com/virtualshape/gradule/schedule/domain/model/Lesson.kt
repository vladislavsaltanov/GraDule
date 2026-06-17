package com.virtualshape.gradule.schedule.domain.model

data class Subgroup(
    val teacher: String,
    val room: String,
    val subnum: Int
)

data class Lesson(
    val id: String,
    val subject: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekType: String,
    val subcount: Int,
    val subgroups: List<Subgroup>
)
