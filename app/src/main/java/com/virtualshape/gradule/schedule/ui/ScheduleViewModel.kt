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
                val response = api.getSchedule(116)
                val mapped = mapToLessons(response)

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    _lessons.value = mapped
                }
            } catch (e: Exception) {
                Log.e("GraDule", "Error loading schedule", e)
            }
        }
    }
}