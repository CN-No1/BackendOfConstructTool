package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("module")
data class Module(@Id val id: String? = null, val name: String, val deleteFlag: Int = 0)

val module = arrayListOf(
        Module("1", "道路交通",0)
)