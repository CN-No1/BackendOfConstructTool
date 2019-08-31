package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.instance.InstanceObject
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
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@RestController
@RequestMapping("instance")
class InstanceController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("修改实例对象")
    @PostMapping("updateInstance")
    fun updateInstance(@RequestBody instanceObject: InstanceObject): Result<Int> {
        val update = Update()
        update.set("instanceList", instanceObject.instanceList)
        val query = Query.query(Criteria.where("id").`is`(instanceObject.id))
        mongoTemplate.updateFirst(query, update, instanceObject::class.java)
        return Result(0)
    }

    @ApiOperation("根据条件分页查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?, docContent: String?, pageable: Pageable): Result<PageImpl<InstanceObject>?> {
        val criteria = Criteria()
        if (!moduleId.isNullOrBlank()) criteria.and("moduleId").`is`(moduleId)
        if (!status.isNullOrBlank()) criteria.and("status").`is`(status)
        if (!docContent.isNullOrBlank()) {
            val pattern = Pattern.compile("^.*$docContent.*$", Pattern.CASE_INSENSITIVE)
            criteria.and("content").regex(pattern)
        }
        val query = Query.query(criteria).with(pageable)
        val res = mongoTemplate.find(query, InstanceObject::class.java)
        val count = mongoTemplate.count(query, InstanceObject::class.java)
        val page = PageImpl<InstanceObject>(res, pageable, count)
        return Result<PageImpl<InstanceObject>?>(0).setData(page)
    }
}