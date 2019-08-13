package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "data_type")
data class DataType(@Id val id: String? = null, val label: String)

val dataType = arrayListOf(
        DataType("1", "string")
)