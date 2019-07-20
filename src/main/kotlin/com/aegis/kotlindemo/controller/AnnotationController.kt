package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.annotator.Annotation
import com.aegis.kotlindemo.model.annotator.Doc
import com.aegis.kotlindemo.model.result.Result
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("annotation")
class AnnotationController (val mongoTemplate: MongoTemplate) {

    @ApiOperation("查询标注信息")
    @GetMapping("getAnnotation")
    fun getAnnotation(id: String): Result<Annotation?> {
        val res = mongoTemplate.findOne(Query.query(Criteria.where("docId").`is`("id")),Annotation::class.java)
        return Result<Annotation?>(0).setData(res)
    }

    @ApiOperation("根据id查询文本内容")
    @GetMapping("getDocById")
    fun getDocById(id: String): Result<Doc?> {
        val res = mongoTemplate.findOne(Query.query(Criteria.where("_id").`is`("id")),Doc::class.java)
        return Result<Doc?>(0).setData(res)
    }

    @ApiOperation("根据条件查询文本内容")
    @GetMapping("getDocByParam")
    fun getDocByParam(moduleId: String?, status: String?): Result<ArrayList<Doc>?> {
        val res = mongoTemplate.find(Query.query(Criteria.where("moduleId").`is`(moduleId).and("status").`is`(status)),Doc::class.java)
        return Result<ArrayList<Doc>?>(0).setData(ArrayList(res) )
    }

    @ApiOperation("创建标注信息")
    @PostMapping("createOrUpdateAnnotation")
    fun createOrUpdateAnnotation(annotation: Annotation): Result<Int?>{
        val query = Query.query(Criteria.where("docId").`is`(annotation.docId))
        val update = Update.update("positionList",annotation.positionList)
        mongoTemplate.upsert(query,update,Annotation::class.java,"annotation")
        return Result(0,"success")
    }
}