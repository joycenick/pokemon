package com.example.pokmon.model

data class Item (
    val id: Int,
    val name: String,
    val url: String,
    val type: String? = null
)
