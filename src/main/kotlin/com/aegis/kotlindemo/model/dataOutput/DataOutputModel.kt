package com.aegis.kotlindemo.model.dataOutput

data class DataOutputModel(val id: String, val text: String)

data class InstanceOutputModel(val text: String, val instanceList: ArrayList<Instance>, val annotationList: ArrayList<Annotation>)

data class Instance(val domain: String, val rangeList: ArrayList<Range>)

data class Range(val content: String, val relation: String, val role: String)

data class Annotation(val entity: String, val value: String, val startOffset: Int, val endOffset: Int)

val instanceOutputModel = arrayListOf<InstanceOutputModel>()