package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.annotator.Annotation
import com.aegis.kotlindemo.model.annotator.Doc
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.entity.Module
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import java.io.File
import java.lang.Exception

@RestController
@RequestMapping("annotation")
class AnnotationController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("查询标注信息")
    @GetMapping("getAnnotation")
    fun getAnnotation(id: String): Result<Annotation?> {
        val res = mongoTemplate.findOne(
                Query.query(Criteria.where("docId").`is`(id)), Annotation::class.java)
//        if (res != null) {
//            for (item in res.positionList) {
//                val criteria = Criteria()
//                criteria.and("entityList").elemMatch(Criteria.where("id").`is`(item.entityId))
//                val temp = mongoTemplate.findOne(Query.query(criteria), EntityClass::class.java)!!.entityList
//                item.entity = findLabel(temp, item.entityId)
//            }
//        }
        return Result<Annotation?>(0).setData(res)
    }
//
//    fun findLabel(list: ArrayList<EntityClassNode>?, id: String): String {
//        if (list != null) {
//            for (item in list) {
//                if (item.id == id) {
//                    return item.label
//                } else return findLabel(item.children,id)
//            }
//        }
//    }

    @ApiOperation("根据id查询文本")
    @GetMapping("getDocById")
    fun getDocById(id: String): Result<Doc?> {
        val res = mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`(id)), Doc::class.java)
        return Result<Doc?>(0).setData(res)
    }

    @ApiOperation("根据条件分页查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?, pageable: Pageable): Result<PageImpl<Doc>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        val query = Query.query(criteria).with(pageable)
        val res = mongoTemplate.find(query, Doc::class.java)
        for (item in res) {
            item.moduleName = mongoTemplate.findOne(
                    Query.query(Criteria.where("_id").`is`(item.moduleId)), Module::class.java)!!.name
        }
        val count = mongoTemplate.count(query, Doc::class.java)
        val page = PageImpl<Doc>(res, pageable, count)
        return Result<PageImpl<Doc>?>(0).setData(page)
    }

    @ApiOperation("创建标注信息")
    @PostMapping("createOrUpdateAnnotation")
    fun createOrUpdateAnnotation(@RequestBody annotation: Annotation): Result<Int?> {
        val query = Query.query(Criteria.where("docId").`is`(annotation.docId))
        val update = Update.update("positionList", annotation.positionList)
        mongoTemplate.upsert(query, update, Annotation::class.java)
        val queryDocById = Query.query(Criteria.where("_id").`is`(annotation.docId))
        val flag = if (annotation.positionList.isNotEmpty()) "1" else "0"
        val updateDocStatus = Update.update("status", flag)
        mongoTemplate.upsert(queryDocById, updateDocStatus, Doc::class.java)
        return Result(0, "success")
    }

    @ApiOperation("新增文档")
    @PostMapping("createDoc")
    fun createDoc(@RequestBody doc: Doc): Result<Int>? {
        mongoTemplate.insert(doc, "doc")
        return Result(0, "success")
    }

    @ApiOperation("删除文档")
    @DeleteMapping("deleteDoc")
    fun deleteDoc(id: String): Result<Int?> {
        return try {
            mongoTemplate.remove(Query.query(Criteria.where("id").`is`(id)), Doc::class.java)
            mongoTemplate.remove(Query.query(Criteria.where("docId").`is`(id)), Annotation::class.java)
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
            val doc = Doc(null, "5d2fe2f28eb1330dcc8f46bd", "0", text)
            mongoTemplate.insert(doc, "doc")
        }
    }
}