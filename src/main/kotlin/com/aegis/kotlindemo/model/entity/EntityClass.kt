package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "entity_class")
data class EntityClass(@Id val id: String, val label: String, val moduleId: String, val pid: String,
                       val description: String, val bandFlag: String)

val entityClass = arrayListOf(EntityClass("1", "2", "3", "0", "2","0"))
