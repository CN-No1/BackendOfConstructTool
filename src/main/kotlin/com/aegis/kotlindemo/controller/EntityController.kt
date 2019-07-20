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

    @ApiOperation("查询实体类")
    @GetMapping("getClasses")
    fun getClasses(id: String): Result<EntityClass?> {
        val res =  mongoTemplate.findOne(Query.query(Criteria.where("thingId").`is`(id)),EntityClass::class.java)
        return Result<EntityClass?>(0).setData(res)
    }

    @ApiOperation("查询thing")
    @GetMapping("queryThing")
    fun getThing(): Result<ArrayList<Thing>>{
        val res = mongoTemplate.findAll(Thing::class.java,"thing")
        return Result<ArrayList<Thing>>(0).setData(ArrayList(res))
    }

    @ApiOperation("查询数据属性")
    @GetMapping("getDataProp")
    fun getDataProp(id: String): Result<DataProp?> {
        val res =  mongoTemplate.findOne(Query.query(Criteria.where("thingId").`is`(id)),DataProp::class.java)
        return Result<DataProp?>(0).setData(res)
    }

    @ApiOperation("查询对象属性")
    @GetMapping("getObjectProp")
    fun getObjectProp(id: String): Result<ObjectProp?> {
        val res =  mongoTemplate.findOne(Query.query(Criteria.where("thingId").`is`(id)),ObjectProp::class.java)
        return Result<ObjectProp?>(0).setData(res)
    }

    @ApiOperation("查询数据类型")
    @GetMapping("getDataType")
    fun getDataType(): Result<ArrayList<DataType>?> {
        val res =  mongoTemplate.findAll(DataType::class.java,"data_type")
        return Result<ArrayList<DataType>?>(0).setData(ArrayList(res) )
    }


    @ApiOperation("新增或修改实体类")
    @PostMapping("creatOrUpdateClass")
    fun creatOrUpdateClass(@RequestBody entityClass: EntityClass): Result<Int?> {
        val query = Query.query(Criteria.where("thingId").`is`(entityClass.thingId))
        val update = Update.update("entityList",entityClass.entityList)
        mongoTemplate.upsert(query,update,EntityClass::class.java,"entity_class")
        return Result(0,"success")
    }

    @ApiOperation("新增或修改数据属性")
    @PostMapping("creatOrUpdateDataProp")
    fun creatOrUpdateDataProp(@RequestBody dataProp: DataProp): Result<Int?> {
        val query = Query.query(Criteria.where("thingId").`is`(dataProp.thingId))
        val update = Update.update("dataPropList",dataProp.dataPropList)
        mongoTemplate.upsert(query,update,DataProp::class.java,"data_prop")
        return Result(0,"success")
    }

    @ApiOperation("新增或修改对象属性")
    @PostMapping("creatOrUpdateObjectProp")
    fun creatOrUpdateObjectProp(@RequestBody objectProp: ObjectProp): Result<Int?> {
        val query = Query.query(Criteria.where("thingId").`is`(objectProp.thingId))
        val update = Update.update("objectPropList",objectProp.objectPropList)
        mongoTemplate.upsert(query,update,ObjectProp::class.java,"object_prop")
        return Result(0,"success")
    }

    @ApiOperation("新增或修改数据类型")
    @PostMapping("creatOrUpdateDataType")
    fun creatOrUpdateDataType(@RequestBody dataType: ArrayList<DataType>): Result<Int?> {
        mongoTemplate.dropCollection("data_type")
        mongoTemplate.insert(dataType,"data_type")
        return Result(0,"success")
    }

}