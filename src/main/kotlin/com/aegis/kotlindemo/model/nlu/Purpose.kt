package com.aegis.kotlindemo.model.nlu

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("purpose")
data class Purpose (@Id val id: String? = null, val moduleId: String, val name: String)

val purpose = arrayListOf(Purpose("1","2","2"))