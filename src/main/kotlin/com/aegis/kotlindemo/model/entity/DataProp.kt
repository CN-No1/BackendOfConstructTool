package com.aegis.kotlindemo.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "data_prop")
data class DataProp(@Id val id: String? = null, val moduleId: String, val dataPropList: ArrayList<DataPropNode>)

data class DataPropNode(val id: String, val label: String, val children: ArrayList<DataPropNode>? = null,
                        val entityClass: ArrayList<EntityClass>, val dataType: String)

val dataProp = arrayListOf(
        DataProp("1", "2",
                arrayListOf(
                        DataPropNode("2", "姓名", null, arrayListOf(
                                EntityClass("1", "2", "2", "0", "2","0")
                        ), "string")
                )
        )
)