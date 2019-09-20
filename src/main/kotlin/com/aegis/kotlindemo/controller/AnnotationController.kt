package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.correlation.Correlation
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.entity.Module
import com.aegis.kotlindemo.model.instance.Instance
import com.aegis.kotlindemo.model.instance.InstanceObject
import com.aegis.kotlindemo.model.nlu.Annotation
import com.aegis.kotlindemo.model.nlu.NLUEntity
import com.aegis.kotlindemo.model.nlu.Purpose
import com.aegis.kotlindemo.model.nlu.nluEntity
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.bson.types.ObjectId
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.remove
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import java.util.regex.Pattern.CASE_INSENSITIVE


@RestController
@RequestMapping("annotation")
class AnnotationController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("根据条件分页查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?, purpose: String?, docContent: String?, hashCode: Int?, pageable: Pageable): Result<PageImpl<NLUEntity>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        if (!purpose.isNullOrBlank()) criteria.and("purpose").`is`(purpose)
        if (!docContent.isNullOrBlank()) {
            val pattern = Pattern.compile("^.*$docContent.*$", CASE_INSENSITIVE)
            criteria.and("content").regex(pattern)
        }
        if (hashCode != 0) criteria.and("hashCode").`is`(hashCode)
        val query = Query.query(criteria).with(pageable)
        val res = mongoTemplate.find(query, NLUEntity::class.java)
        for (item in res) {
            item.moduleName = mongoTemplate.findOne(
                    Query.query(Criteria.where("_id").`is`(item.moduleId)), Module::class.java)!!.name
        }
        res.map { annotation ->
            annotation.annotationList.map {
                val queryEntity = Query.query(Criteria.where("id").`is`(it.entityId))
                val entity = mongoTemplate.findOne(queryEntity, EntityClass::class.java)
                if (entity != null) {
                    it.entity = entity.label
                }
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
        val correlationList = ArrayList<Correlation>()
        update.set("annotationList", nluEntity.annotationList)
        deleteCorrelation(nluEntity.id as String)
        nluEntity.annotationList.map {
            val queryEntity = Query.query(Criteria.where("id").`is`(it.entityId))
            val updateBandFlag = Update()
            updateBandFlag.set("bandFlag", "1")
            mongoTemplate.updateFirst(queryEntity, updateBandFlag, EntityClass::class.java)
            // 绑定nlu与实体类
            val correlation = Correlation(null, nluEntity.id, it.entityId)
            correlationList.add(correlation)
        }
        mongoTemplate.insert(correlationList, Correlation::class.java)
        nluEntity.intention?.let { update.set("intention", it) }
        mongoTemplate.upsert(query, update, NLUEntity::class.java)
        val flag = if (nluEntity.annotationList.isEmpty() && nluEntity.intention!!.isEmpty()) "0" else "1"
        val updateDocStatus = Update.update("status", flag)
        mongoTemplate.upsert(query, updateDocStatus, NLUEntity::class.java)
        // 更新实例图对象
        val queryInstance = Query.query(Criteria.where("hashCode").`is`(nluEntity.hashCode))
        val updateInstance = Update()
        updateInstance.set("instanceList", arrayListOf<Instance>())
        updateInstance.set("status", "0")
        updateInstance.set("updateTime", Date())
        updateInstance.set("annotationList", nluEntity.annotationList)
        mongoTemplate.updateFirst(queryInstance, updateInstance, InstanceObject::class.java)
        // 删除实例id与实体id绑定
        val instanceId = mongoTemplate.findOne(queryInstance, InstanceObject::class.java)!!.id as String
        deleteCorrelation(instanceId)
        return Result(0, "success")
    }

    fun deleteCorrelation(id: String) {
        // 找出已绑定的实体类，全部移除
        val queryCorrelation = Query()
        queryCorrelation.addCriteria(Criteria.where("objectId").`is`(id))
//        val res = mongoTemplate.find(queryCorrelation, Correlation::class.java)
        mongoTemplate.remove(queryCorrelation, Correlation::class.java)
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
    fun parseJson(@RequestParam("file") file: MultipartFile) {
        val inputStream = file.inputStream
        val tempFile = File.createTempFile("temp", ".json")
        org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, tempFile)
        val content = tempFile.readText()
        val jsonObject = JsonParser().parse(content).asJsonObject.get("idList").asJsonArray
        jsonObject.map {
            val hashCode = mongoTemplate.findOne(Query.query(Criteria.where("id").`is`(it.asString)), NLUEntity::class.java)!!.hashCode
            val queryInstance = Query.query(Criteria.where("hashCode").`is`(hashCode))
            val updateInstance = Update()
            updateInstance.set("status", "2")
            mongoTemplate.updateFirst(queryInstance, updateInstance, InstanceObject::class.java)
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
            val instanceObject = InstanceObject(null, nluDoc.content, arrayListOf(),
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

    @ApiOperation("初始化关联表")
    @GetMapping("initCorrelation")
    fun initCorrelation() {
        val nluList = mongoTemplate.findAll(NLUEntity::class.java)
        val instanceObjectList = mongoTemplate.findAll(InstanceObject::class.java)
        val correlationList = ArrayList<Correlation>()
        nluList.map {
            it.annotationList.map { annotation ->
                val correlation = Correlation(null, it.id!!, annotation.entityId)
                correlationList.add(correlation)
            }
            it.intention?.map { entityClass ->
                val correlation = Correlation(null, it.id!!, entityClass.id!!)
                correlationList.add(correlation)
            }
        }
        instanceObjectList.map { instanceObject ->
            instanceObject.instanceList.map { instance ->
                val correlation = Correlation(null, instanceObject.id!!, instance.domain)
                correlationList.add(correlation)
                instance.rangeList.map { range ->
                    if (!range.relation.isNullOrEmpty()) {
                        val correlation2 = Correlation(null, instanceObject.id, range.relation!!)
                        correlationList.add(correlation2)
                    }
                }
            }
        }
        mongoTemplate.insert(correlationList, Correlation::class.java)
    }

}