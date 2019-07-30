package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.annotator.Annotation
import com.aegis.kotlindemo.model.annotator.Doc
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.entity.Module
import com.aegis.kotlindemo.model.nlu.NLUEntity
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import java.io.File
import java.lang.Exception

@RestController
@RequestMapping("annotation")
class AnnotationController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("根据id查询文本")
    @GetMapping("getDocById")
    fun getDocById(id: String): Result<Doc?> {
        val res = mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`(id)), Doc::class.java)
        return Result<Doc?>(0).setData(res)
    }

    @ApiOperation("根据条件分页查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?, pageable: Pageable): Result<PageImpl<NLUEntity>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        val query = Query.query(criteria).with(pageable)
        val res = mongoTemplate.find(query, NLUEntity::class.java)
        for (item in res) {
            item.moduleName = mongoTemplate.findOne(
                    Query.query(Criteria.where("_id").`is`(item.moduleId)), Module::class.java)!!.name
        }
        res.map { annotation ->
            annotation.annotationList.map {
                val queryEntity = Query.query(Criteria.where("id").`is`(it.entityId))
                it.entity = mongoTemplate.findOne(queryEntity, EntityClass::class.java)!!.label
            }
        }
        val count = mongoTemplate.count(query, NLUEntity::class.java)
        val page = PageImpl<NLUEntity>(res, pageable, count)
        return Result<PageImpl<NLUEntity>?>(0).setData(page)
    }

    @ApiOperation("创建标注信息")
    @PostMapping("createOrUpdateAnnotation")
    fun createOrUpdateAnnotation(@RequestBody nluEntity: NLUEntity): Result<Int?> {
        val query = Query.query(Criteria.where("id").`is`(nluEntity.id))
        val update = Update.update("annotationList", nluEntity.annotationList)
        mongoTemplate.upsert(query, update, NLUEntity::class.java)
        val flag = if (nluEntity.annotationList.isNotEmpty()) "1" else "0"
        val updateDocStatus = Update.update("status", flag)
        mongoTemplate.upsert(query, updateDocStatus, NLUEntity::class.java)
        return Result(0, "success")
    }

    @ApiOperation("新增NLU文档")
    @PostMapping("createNLUDoc")
    fun createNLUDoc(@RequestBody nluDoc: NLUEntity): Result<Int>? {
        mongoTemplate.insert(nluDoc, "NLU_entity")
        return Result(0, "success")
    }

    @ApiOperation("删除NLU文档")
    @DeleteMapping("deleteNLUDoc")
    fun deleteNLUDoc(id: String): Result<Int?> {
        return try {
            mongoTemplate.remove(Query.query(Criteria.where("id").`is`(id)), NLUEntity::class.java)
            Result(0)
        } catch (e: Exception) {
            Result(500, e.toString())
        }
    }

    @ApiOperation("解析Json文件")
    @PostMapping("parseJson")
    fun parseJson() {
        val fileName = """D:\traffic_nlu_data.json"""
        val file = File(fileName)
        val content = file.readText()
        val jsonObject = JsonParser().parse(content).asJsonObject.get("qa_nlu_data").asJsonObject.get("common_examples").asJsonArray
        jsonObject.map {
            val text = it.asJsonObject.get("text").asString
            val nluDoc = NLUEntity(null, text, "5d2fe2f28eb1330dcc8f46bd", "0", ArrayList())
            mongoTemplate.insert(nluDoc, "NLU_entity")
        }
    }
}