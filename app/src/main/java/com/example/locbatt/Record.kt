package com.example.locbatt

class Record(
    val courseId : String,
    val date :String,
    val students : ArrayList<String>
) {
    constructor():this("","",ArrayList())
}