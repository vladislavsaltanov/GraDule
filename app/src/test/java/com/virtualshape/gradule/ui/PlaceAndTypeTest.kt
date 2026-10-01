package com.virtualshape.gradule.ui

import androidx.compose.ui.text.font.FontWeight
import com.virtualshape.gradule.domain.schedule.LessonType
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceAndTypeTest {
    @Test
    fun separatorsMatchFigmaAndRemoteRoomsStayUnprefixed() {
        val rendered = placeAndType("120", "", LessonType.LECTURE)
        assertEquals("ауд. 120 • лекция", rendered.text)
        val separator = rendered.spanStyles.single()
        assertEquals(8, separator.start)
        assertEquals(11, separator.end)
        assertEquals(FontWeight.Normal, separator.item.fontWeight)

        assertEquals("Онлайн", placeAndType("Онлайн", "", LessonType.UNKNOWN).text)
    }
}
