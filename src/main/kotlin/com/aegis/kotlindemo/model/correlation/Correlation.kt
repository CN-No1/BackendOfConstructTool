package com.aegis.kotlindemo.model.correlation

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("correlation")
data class Correlation(@Id val id: String? = null, val objectId: String, val entityId: String)

data class CorrelationObject(val id: String, val text: String, val hashCode: Int)

val correlation = arrayListOf(Correlation("","",""))