package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("tree_type")
data class TreeType(@Id val id: String? = null, val label: String)

val treeType = arrayListOf(TreeType("",""))