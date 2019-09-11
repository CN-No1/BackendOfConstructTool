package com.aegis.kotlindemo.model.instance

import com.aegis.kotlindemo.model.nlu.Annotation
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import kotlin.collections.ArrayList

@Document("instance_object")
data class InstanceObject(@Id val id: String? = null, val text: String, val instanceList: ArrayList<Instance>,
                          val moduleId: String, val status: String, var hashCode: Int, val updateTime: Date,
                          val annotationList: ArrayList<Annotation>? = arrayListOf())

data class Instance(var instanceName: String, val domain: String, val rangeList: ArrayList<Range>)

data class Range(val content: String, val relation: String?, val role: String, val status: Boolean)

val instanceObject = arrayListOf(InstanceObject("", "", arrayListOf(
        Instance("", "", arrayListOf(
                Range("", "", "", false)
        ))), "", "", 0, Date()))