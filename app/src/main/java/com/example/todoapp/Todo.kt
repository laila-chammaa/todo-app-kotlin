package com.example.todoapp

data class Todo (
    val title: String = "",
    val id: String = getRandomString(8),
    var checked: Boolean = false
)

private fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
