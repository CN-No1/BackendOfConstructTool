package com.aegis.kotlindemo.model.entity

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "data_type")
data class DataType(val id: String, val label: String)

val dataType = arrayListOf(
        DataType("1", "string")
)