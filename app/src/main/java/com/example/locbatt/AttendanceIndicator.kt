package com.example.locbatt

class AttendanceIndicator (
    val courseId : String,
    val status : Boolean
    ) {
    constructor():this("",false)
}