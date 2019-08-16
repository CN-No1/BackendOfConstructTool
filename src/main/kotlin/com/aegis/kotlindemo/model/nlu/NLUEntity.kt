package com.aegis.kotlindemo.model.nlu

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.File
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

@Document("NLU_entity")
data class NLUEntity(@Id val id: String? = null, val content: String, val moduleId: String, val purpose: String,
                     val status: String, var hashCode: Int, val annotationList: ArrayList<Annotation>,
                     val updateTime: Date, @Transient var moduleName: String? = null)

data class Annotation(val entityId: String, val value: String, val startOffset: Int, val endOffset: Int,
                      @kotlin.jvm.Transient var entity: String? = null)

data class UploadObj(val file: File, val moduleId: String, val purpose: String)

val nluEntity = arrayListOf(NLUEntity("1", "2", "3", "3", "0", 1,
        arrayListOf(Annotation("1", "2", 0, 3)), Date()
))