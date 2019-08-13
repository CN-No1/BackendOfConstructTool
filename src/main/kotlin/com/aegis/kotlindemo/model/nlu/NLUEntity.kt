package com.aegis.kotlindemo.model.nlu

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("NLU_entity")
data class NLUEntity(@Id val id: String? = null, val content: String, val moduleId: String,
                     val status: String, var hashCode: Int, val annotationList: ArrayList<Annotation>,
                     @Transient var moduleName: String? = null)

data class Annotation(val entityId: String, val value: String, val startOffset: Int, val endOffset: Int,
                      @kotlin.jvm.Transient var entity: String? = null)

val nluEntity = arrayListOf(NLUEntity("1", "2", "3", "0", 1, arrayListOf(
        Annotation("1", "2", 0, 3)
)))