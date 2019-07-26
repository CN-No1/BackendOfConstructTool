package com.aegis.kotlindemo.model.nlu

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("NLU_entity")
data class NLUEntity(@Id val id: String? = null, val content: String, val moduleId: String,
                    val status: String, val AnnotationList:ArrayList<Annotation>)

data class Annotation(val entityId: String, val value: String, val startOffset: Int, val endOffset: Int)

val nluEntity = arrayListOf(NLUEntity("1","2","3","0", arrayListOf(
        Annotation("1","2",0,3)
)))