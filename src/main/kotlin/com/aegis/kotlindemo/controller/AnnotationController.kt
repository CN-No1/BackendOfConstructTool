package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.annotator.Annotation
import com.aegis.kotlindemo.model.annotator.Doc
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.entity.EntityClassNode
import com.aegis.kotlindemo.model.entity.Module
import com.aegis.kotlindemo.model.result.Result
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("annotation")
class AnnotationController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("查询标注信息")
    @GetMapping("getAnnotation")
    fun getAnnotation(id: String): Result<Annotation?> {
        val res = mongoTemplate.findOne(
                Query.query(Criteria.where("docId").`is`(id)), Annotation::class.java)
        if (res != null) {
            for (item in res.positionList) {
                val criteria = Criteria()
                criteria.and("entityList").elemMatch(Criteria.where("_id").`is`(item.entityId))
                val temp = mongoTemplate.findOne(Query.query(criteria), EntityClass::class.java)!!.entityList
                item.entity = findLabel(temp, item.entityId)
            }
        }
        return Result<Annotation?>(0).setData(res)
    }

    fun findLabel(list: ArrayList<EntityClassNode>?, id: String): String {
        var res = ""
        var children: ArrayList<EntityClassNode>? = arrayListOf()
        if (list != null) {
            for (item in list) {
                if (item.id == id) {
                    res = item.label
                } else children = item.children
            }
        }
        return if (!res.isBlank()) res else findLabel(children, id)
    }

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
        val updateDocStatus = Update.update("status", "1")
        mongoTemplate.upsert(queryDocById, updateDocStatus, Doc::class.java)
        return Result(0, "success")
    }
}