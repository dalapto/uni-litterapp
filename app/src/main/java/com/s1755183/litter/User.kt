package com.s1755183.litter

data class User(
        val id: String = "",
        val name: String = "",
        val pickup_range: Double = 0.5,
        val my_messages: List<String>? = null,
        val kept_messages: List<String>? = null
            )

