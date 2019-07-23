package com.aegis.kotlindemo.model.annotator

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "annotation")
data class Annotation(@Id val id: String? = null, val docId: String, val positionList: ArrayList<Position>)

data class Position(val entityId: String, val value: String, val startOffset: Int, val endOffset: Int, @kotlin.jvm.Transient var entity: String? = null)

val annotation = arrayListOf(
        Annotation("1", "2", arrayListOf(
                Position("2", "原告", 1, 10)))
)