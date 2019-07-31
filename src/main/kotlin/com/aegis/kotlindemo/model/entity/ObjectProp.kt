package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "object_prop")
data class ObjectProp(@Id val id: String? = null, val moduleId: String, val objectPropList: ArrayList<ObjectPropNode>)

data class ObjectPropNode(val id: String, val label: String, val children: ArrayList<ObjectPropNode>? = null, val relation: ArrayList<Relation>)

data class Relation(val domain: String, val range: String)

val objectProp = arrayListOf(
        ObjectProp("1", "2",
                arrayListOf(
                        ObjectPropNode("2", "姓", null, arrayListOf(
                                Relation("1", "2"))
                        ))
        )
)