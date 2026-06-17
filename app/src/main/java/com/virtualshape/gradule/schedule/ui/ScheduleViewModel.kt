package com.virtualshape.gradule.schedule.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualshape.gradule.core.util.mapToLessons
import com.virtualshape.gradule.data.remote.ScheduleApi
import com.virtualshape.gradule.schedule.domain.model.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScheduleViewModel : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons
    private val _currentWeekType = MutableStateFlow("0")
    val currentWeekType: StateFlow<String> = _currentWeekType
    public var groupName: String = ""

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://schedule.sfedu.ru/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(ScheduleApi::class.java)

                val weekResponse = api.getCurrentWeek()
                val weekType = weekResponse.week.toString()

                val response = api.getSchedule(116)
                val mapped = mapToLessons(response)

                val groups = api.getGroups()
                groupName = groups.find { it.id == 116 }?.fullName ?: "Unknown"

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    _currentWeekType.value = weekType
                    _lessons.value = mapped
                }
            } catch (e: Exception) {
                Log.e("GraDule", "Error loading schedule", e)
            }
        }
    }
}