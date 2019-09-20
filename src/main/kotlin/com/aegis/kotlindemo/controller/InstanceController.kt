package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.correlation.Correlation
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.instance.InstanceObject
import com.aegis.kotlindemo.model.nlu.Purpose
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.bson.types.ObjectId
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@RestController
@RequestMapping("instance")
class InstanceController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("修改实例对象")
    @PostMapping("updateInstance")
    fun updateInstance(@RequestBody instanceObject: InstanceObject): Result<Int> {
        val query = Query.query(Criteria.where("id").`is`(instanceObject.id))
        val flag = if (instanceObject.instanceList.isEmpty()) "0" else "1"
        val correlationList = ArrayList<Correlation>()
        val update = Update()
        deleteCorrelation(instanceObject.id as String)
        if (flag == "1") {
            instanceObject.instanceList.map {
                val queryEntity = Query.query(Criteria.where("id").`is`(it.domain))
                it.instanceName = mongoTemplate.findOne(queryEntity, EntityClass::class.java)!!.label
                // 绑定实例与实体类
                val correlation = Correlation(null, instanceObject.id, it.domain)
                correlationList.add(correlation)
                it.rangeList.map { range ->
                    if (!range.relation.isNullOrEmpty()) {
                        val correlation2 = Correlation(null, instanceObject.id, range.relation!!)
                        correlationList.add(correlation2)
                    }
                }
            }
            mongoTemplate.insert(correlationList, Correlation::class.java)
        }
        update.set("instanceList", instanceObject.instanceList)
        update.set("status", if (instanceObject.status == "2") "2" else flag)
        mongoTemplate.updateFirst(query, update, instanceObject::class.java)
        return Result(0)
    }

    fun deleteCorrelation(id: String) {
        // 找出已绑定的实体类，全部移除
        val queryCorrelation = Query()
        queryCorrelation.addCriteria(Criteria.where("objectId").`is`(id))
//        val res = mongoTemplate.find(queryCorrelation, Correlation::class.java)
        mongoTemplate.remove(queryCorrelation, Correlation::class.java)
    }

    @ApiOperation("根据条件分页查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?, docContent: String?, hashCode: Int?, pageable: Pageable): Result<PageImpl<InstanceObject>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        if (!docContent.isNullOrBlank()) {
            val pattern = Pattern.compile("^.*$docContent.*$", Pattern.CASE_INSENSITIVE)
            criteria.and("text").regex(pattern)
        }
        if (hashCode != 0) criteria.and("hashCode").`is`(hashCode)
        val query = Query.query(criteria).with(pageable)
        val res = mongoTemplate.find(query, InstanceObject::class.java)
        res.map { annotation ->
            annotation.annotationList?.map {
                val queryEntity = Query.query(Criteria.where("id").`is`(it.entityId))
                it.entity = mongoTemplate.findOne(queryEntity, EntityClass::class.java)!!.label
            }
        }
        val count = mongoTemplate.count(query, InstanceObject::class.java)
        val page = PageImpl<InstanceObject>(res, pageable, count)
        return Result<PageImpl<InstanceObject>?>(0).setData(page)
    }

//    @ApiOperation("修改实例id")
//    @GetMapping("updateInstanceId")
//    fun updateInstanceId() {
//        val res = mongoTemplate.findAll(InstanceObject::class.java)
//        mongoTemplate.dropCollection("instance_object")
//        res.map {
//            it.id = null
//            mongoTemplate.insert(it,"instance_object")
//        }
//    }
}