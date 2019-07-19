package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.entity.*
import com.aegis.kotlindemo.model.result.Result
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("entity")
class EntityController (val mongoTemplate: MongoTemplate){

    @ApiOperation("新增或修改实体类", tags = ["新增或修改实体类"])
    @PostMapping("creatOrUpdateClass")
    fun creatOrUpdateClass(@RequestBody entityClass: EntityClass): Result<Int?> {
        val query = Query.query(Criteria.where("thingId").`is`(entityClass.thingId))
        val update = Update.update("entityList",entityClass.entityList)
        mongoTemplate.upsert(query,update,EntityClass::class.java)
        return Result(0,"success")
    }

    @ApiOperation("查询实体类", tags = ["查询实体类"])
    @GetMapping("getClasses")
    fun getClasses(id: String): Result<EntityClass?> {
        val res =  mongoTemplate.findOne(Query.query(Criteria.where("thingId").`is`(id)),EntityClass::class.java)
        return Result<EntityClass?>(0).setData(res)
    }

    @ApiOperation("查询thing")
    @GetMapping("queryThing")
    fun queryThing(): Result<ArrayList<Thing>>{
        val res = mongoTemplate.findAll(Thing::class.java,"thing")
        return Result<ArrayList<Thing>>(0).setData(ArrayList(res))
    }
}