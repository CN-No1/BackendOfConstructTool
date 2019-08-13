package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("tree")
data class Tree(@Id val id: String? = null, val moduleId: String, val name: String, val deleteFlag: Int = 0)

val tree = arrayListOf(Tree("1","2", "2",0))