package com.aegis.kotlindemo.model.annotator

import org.springframework.data.mongodb.core.mapping.Document

@Document("doc")
data class Doc(val id: String? = null, val content: String)

val doc = arrayListOf(
        Doc("1","原告福州市顺辉运输有限公司诉称，2013年10月1日，原告的驾驶员杨海军驾驶闽A×××××号重型半挂牵引车，在324国道被告管辖路段发生交通事故。")
)