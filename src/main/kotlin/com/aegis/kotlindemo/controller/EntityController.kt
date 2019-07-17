package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.entity.*
import com.aegis.kotlindemo.model.result.Result
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.remove
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("entity")
class EntityController (val mongoTemplate: MongoTemplate){

    @ApiOperation("新增或修改实体类", tags = ["新增或修改实体类"])
    @PostMapping("/creatOrUpdateClass")
    fun creatOrUpdateClass(@RequestBody entityList: ArrayList<EntityClass>) {
        mongoTemplate.dropCollection("classes")
        mongoTemplate.insert(entityList, "classes")
    }

    @ApiOperation("删除实体类", tags = ["删除实体类"])
    @DeleteMapping("/deleteClass")
    fun deleteClass() {

    }

    @ApiOperation("查询实体类", tags = ["查询实体类"])
    @GetMapping("getClasses")
    fun getClasses(): Any {
        return mongoTemplate.findAll(EntityClass::class.java,"classes")
}
}