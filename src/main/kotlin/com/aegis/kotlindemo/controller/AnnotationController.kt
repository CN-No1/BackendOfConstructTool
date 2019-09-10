package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.annotator.Doc
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.entity.Module
import com.aegis.kotlindemo.model.instance.Instance
import com.aegis.kotlindemo.model.instance.InstanceObject
import com.aegis.kotlindemo.model.instance.Range
import com.aegis.kotlindemo.model.nlu.Annotation
import com.aegis.kotlindemo.model.nlu.NLUEntity
import com.aegis.kotlindemo.model.nlu.Purpose
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
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import java.util.regex.Pattern.CASE_INSENSITIVE
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.validation.Validator.criteria


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
    fun getDocByParam(moduleId: String?, status: String?, purpose: String?, docContent: String?, pageable: Pageable): Result<PageImpl<NLUEntity>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        if (!purpose.isNullOrBlank()) criteria.and("purpose").`is`(purpose)
        if (!docContent.isNullOrBlank()) {
            val pattern = Pattern.compile("^.*$docContent.*$", CASE_INSENSITIVE)
            criteria.and("content").regex(pattern)
        }
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
        val update = Update()
        update.set("annotationList", nluEntity.annotationList)
        nluEntity.annotationList.map {
            val queryEntity = Query.query(Criteria.where("id").`is`(it.entityId))
            val updateBandFlag = Update()
            updateBandFlag.set("bandFlag","1")
            mongoTemplate.updateFirst(queryEntity,updateBandFlag,EntityClass::class.java)
        }
        nluEntity.intention?.let { update.set("intention", it) }
        mongoTemplate.upsert(query, update, NLUEntity::class.java)
        val flag = if (nluEntity.annotationList.isEmpty() && nluEntity.intention!!.isEmpty()) "0" else "1"
        val updateDocStatus = Update.update("status", flag)
        mongoTemplate.upsert(query, updateDocStatus, NLUEntity::class.java)
        // 删除并重新导入实例图对象
        val queryInstance = Query.query(Criteria.where("hashCode").`is`(nluEntity.hashCode))
        mongoTemplate.remove(queryInstance, InstanceObject::class.java)
        val instanceObject = InstanceObject(nluEntity.id, nluEntity.content, arrayListOf(),
                nluEntity.moduleId, "0", nluEntity.hashCode, Date(), nluEntity.annotationList)
        if (flag === "1") {
            mongoTemplate.insert(arrayListOf(instanceObject), InstanceObject::class.java)
        }
        return Result(0, "success")
    }

    @ApiOperation("新增NLU文档")
    @PostMapping("createNLUDoc")
    fun createNLUDoc(@RequestBody nluDoc: NLUEntity): Result<Int>? {
        nluDoc.hashCode = nluDoc.content.hashCode()
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
    fun parseJson(@RequestParam("file") file: MultipartFile,
                  @RequestParam("newModuleId") newModuleId: String,
                  @RequestParam("newPurpose") newPurpose: String) {
        val query = Query.query(Criteria.where("moduleId").`is`(newModuleId)
                .and("name").`is`(newPurpose))
        val res = mongoTemplate.find(query, Purpose::class.java)
        if (res.isEmpty()) {
            mongoTemplate.insert(Purpose(null, newModuleId, newPurpose), "purpose")
        }
        val inputStream = file.inputStream
        val tempFile = File.createTempFile("temp", ".json")
        org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, tempFile)
        val content = tempFile.readText()
        val jsonObject = JsonParser().parse(content).asJsonObject.get("docList").asJsonArray
        jsonObject.map {
            val text = it.asJsonObject.get("text").asString
            val hashCode = text.hashCode()
            val nluDoc = NLUEntity(null, text, newModuleId, newPurpose, "0",
                    hashCode, ArrayList(), Date(), null)
            mongoTemplate.insert(nluDoc, "NLU_entity")
        }
        tempFile.delete()
    }


    @ApiOperation("解析Json文件")
    @PostMapping("parseJson2")
    fun parseJson2(@RequestParam("file") file: MultipartFile) {
        val inputStream = file.inputStream
        val tempFile = File.createTempFile("temp", ".json")
        org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, tempFile)
        val content = tempFile.readText()
        val jsonObject = JsonParser().parse(content).asJsonObject.get("docList").asJsonArray
        jsonObject.map {
            val text = it.asJsonObject.get("text").asString
            val intent = it.asJsonObject.get("intent").asString
            val instance = mongoTemplate.findOne(
                    Query.query(Criteria.where("label").`is`(intent)), EntityClass::class.java)!!
            val hashCode = text.hashCode()
            val annotationList = arrayListOf<Annotation>()
            if (it.asJsonObject.has("entities")) {
                val update = Update()
                update.set("bandFlag", "1")
                val temArr = it.asJsonObject.get("entities").asJsonArray
                annotationList.clear()
                if (temArr.size() != 0) {
                    temArr.map { item ->
                        val entity = item.asJsonObject.get("entity").asString
                        val queryEntity = Query.query(Criteria.where("label").`is`(entity))
                        val entityClass = mongoTemplate.findOne(queryEntity, EntityClass::class.java)
                        var entityId = ""
                        if (entityClass != null) {
                            entityId = entityClass.id!!
                            mongoTemplate.updateFirst(queryEntity, update, EntityClass::class.java)
                        } else {
                            println("不存在$entity")
                        }
                        val annotation = Annotation(entityId, item.asJsonObject.get("value").asString,
                                item.asJsonObject.get("start").asInt, item.asJsonObject.get("end").asInt)
                        annotationList.add(annotation)
                    }
                }
            }
            val nluDoc = NLUEntity(null, text, "5d4d34110b5f5a2d7ce2cca1", "nlu", "1",
                    hashCode, annotationList, Date(), arrayListOf(instance))
            mongoTemplate.insert(nluDoc, "NLU_entity")
            val instanceObject = InstanceObject(nluDoc.id, nluDoc.content, arrayListOf(),
                    nluDoc.moduleId, "0", nluDoc.hashCode, Date(), nluDoc.annotationList)
            mongoTemplate.insert(arrayListOf(instanceObject), InstanceObject::class.java)
        }
        tempFile.delete()
    }

    @ApiOperation("获取用途")
    @GetMapping("getPurpose")
    fun getPurpose(): Result<List<String>> {
        val res = mongoTemplate.findDistinct(
                "purpose", NLUEntity::class.java, String::class.java)
        return Result<List<String>>(0).setData(res)
    }
}