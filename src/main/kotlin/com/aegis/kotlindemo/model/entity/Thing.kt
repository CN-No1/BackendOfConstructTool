package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Thing(@Id val id :String? = null, val name: String)

val thing = arrayListOf(
        Thing("1","道路交通")
)