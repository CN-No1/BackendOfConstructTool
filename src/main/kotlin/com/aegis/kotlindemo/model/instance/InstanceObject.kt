package com.aegis.kotlindemo.model.instance

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import kotlin.collections.ArrayList

@Document("instance_object")
data class InstanceObject(@Id val id: String? = null, val text: String, val instanceList: ArrayList<Instance>,
                          val moduleId: String, val purpose: String, val status: String, var hashCode: Int,
                          val updateTime: Date)

data class Instance(val domain: String, val rangeList: ArrayList<Range>)

data class Range(val content: String, val relation: String)

val instanceObject = arrayListOf(InstanceObject("", "", arrayListOf(
        Instance("", arrayListOf(
                Range("", "")
        ))), "", "", "", 0, Date()))