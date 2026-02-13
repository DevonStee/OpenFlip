package com.bokehforu.openflip.core.manager

import org.junit.Test
import kotlin.test.assertEquals

class TimeTest {
    
    @Test
    fun `hourFormatted returns correct 24-hour format`() {
        val time = Time(hour = 13, minute = 30, second = 0, is24Hour = true)
        assertEquals("13", time.hourFormatted)
    }
    
    @Test
    fun `hourFormatted returns correct 12-hour format for PM`() {
        val time = Time(hour = 13, minute = 30, second = 0, is24Hour = false)
        assertEquals("01", time.hourFormatted)
    }
    
    @Test
    fun `hourFormatted returns correct 12-hour format for midnight`() {
        val time = Time(hour = 0, minute = 30, second = 0, is24Hour = false)
        assertEquals("12", time.hourFormatted)
    }
    
    @Test
    fun `hourFormatted returns correct 12-hour format for noon`() {
        val time = Time(hour = 12, minute = 30, second = 0, is24Hour = false)
        assertEquals("12", time.hourFormatted)
    }
    
    @Test
    fun `minuteFormatted pads single digit`() {
        val time = Time(hour = 13, minute = 5, second = 0, is24Hour = true)
        assertEquals("05", time.minuteFormatted)
    }
    
    @Test
    fun `secondFormatted pads single digit`() {
        val time = Time(hour = 13, minute = 30, second = 7, is24Hour = true)
        assertEquals("07", time.secondFormatted)
    }
    
    @Test
    fun `amPm returns AM for morning hours`() {
        val time = Time(hour = 9, minute = 30, second = 0, is24Hour = false)
        assertEquals("AM", time.amPm)
    }
    
    @Test
    fun `amPm returns PM for afternoon hours`() {
        val time = Time(hour = 14, minute = 30, second = 0, is24Hour = false)
        assertEquals("PM", time.amPm)
    }
    
    @Test
    fun `amPm returns AM for midnight`() {
        val time = Time(hour = 0, minute = 30, second = 0, is24Hour = false)
        assertEquals("AM", time.amPm)
    }
    
    @Test
    fun `amPm returns PM for noon`() {
        val time = Time(hour = 12, minute = 30, second = 0, is24Hour = false)
        assertEquals("PM", time.amPm)
    }
}
