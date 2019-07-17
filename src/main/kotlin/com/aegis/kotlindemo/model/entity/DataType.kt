package com.aegis.kotlindemo.model.entity

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "data_type")
data class DataType (val name: String, val type: String)

val dataType = arrayListOf(
        DataType("name","string")
)