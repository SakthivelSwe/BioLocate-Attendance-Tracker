package com.example.locbatt

class TeacherLocation(
    val courseId : String,
    val longtitude : Double,
    val latitude : Double
) {
    constructor():this("",0.0,0.0)
}