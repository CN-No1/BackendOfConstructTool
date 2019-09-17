package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.entity.*
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import kotlin.collections.ArrayList

@RestController
@RequestMapping("entity")
class EntityController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("查询实体类")
    @GetMapping("getClasses")
    fun getClasses(treeId: String): Result<ArrayList<EntityClass>?> {
        val query = Query()
        query.with(Sort(Sort.Direction.ASC, "index"))
        query.addCriteria(Criteria.where("treeId").`is`(treeId))
        val res = mongoTemplate.find(
                query, EntityClass::class.java)
        return Result<ArrayList<EntityClass>?>(0).setData(ArrayList(res))
    }

    @ApiOperation("通过id查询实体类")
    @GetMapping("getClassesById")
    fun getClassesById(id: String): Result<EntityClass?> {
        val query = Query()
        query.addCriteria(Criteria.where("id").`is`(id))
        val res = mongoTemplate.findOne(
                query, EntityClass::class.java)
        return Result<EntityClass?>(0).setData(res)
    }

    @ApiOperation("查询模块")
    @GetMapping("getModule")
    fun getModule(): Result<ArrayList<Module>> {
        val res = mongoTemplate.find(Query.query(Criteria.where("deleteFlag").`is`(0)), Module::class.java)
        return Result<ArrayList<Module>>(0).setData(ArrayList(res))
    }

    @ApiOperation("查询树类型")
    @GetMapping("getTreeType")
    fun getTreeType(): Result<ArrayList<TreeType>> {
        val res = mongoTemplate.findAll(TreeType::class.java)
        return Result<ArrayList<TreeType>>(0).setData(ArrayList(res))
    }

    @ApiOperation("查询树")
    @GetMapping("getTree")
    fun getTree(moduleId: String, treeType: String?): Result<ArrayList<Tree>> {
        val criteria = Criteria()
        criteria.and("deleteFlag").`is`(0)
        criteria.and("moduleId").`is`(moduleId)
        if (!treeType.isNullOrBlank()) criteria.and("treeType").`is`(treeType)
        val query = Query.query(criteria)
        val res = mongoTemplate.find(query, Tree::class.java)
        return Result<ArrayList<Tree>>(0).setData(ArrayList(res))
    }

    @ApiOperation("查询数据属性")
    @GetMapping("getDataProp")
    fun getDataProp(id: String): Result<DataProp?> {
        val res = mongoTemplate.findOne(
                Query.query(Criteria.where("treeId").`is`(id)), DataProp::class.java)
        return Result<DataProp?>(0).setData(res)
    }

    @ApiOperation("查询对象属性")
    @GetMapping("getObjectProp")
    fun getObjectProp(id: String): Result<ObjectProp?> {
        val res = mongoTemplate.findOne(
                Query.query(Criteria.where("treeId").`is`(id)), ObjectProp::class.java)
        return Result<ObjectProp?>(0).setData(res)
    }

    @ApiOperation("查询数据类型")
    @GetMapping("getDataType")
    fun getDataType(): Result<ArrayList<DataType>?> {
        val res = mongoTemplate.findAll(DataType::class.java)
        return Result<ArrayList<DataType>?>(0).setData(ArrayList(res))
    }

    @ApiOperation("新增或修改模块")
    @PostMapping("createOrUpdateModule")
    fun createOrUpdateModule(@RequestBody module: Module): Result<Int?> {
        return if (module.id.isNullOrBlank()) {
            val query = Query.query(Criteria.where("name").`is`(module.name).and("deleteFlag").`is`(0))
            val res = mongoTemplate.find(query, Module::class.java)
            if (res.isEmpty()) {
                mongoTemplate.insert(module, "module")
                Result(0)
            } else {
                Result(500, "模块已存在！请勿重复添加！")
            }
        } else {
            val update = Update()
            update.set("name", module.name)
            val query = Query.query(Criteria.where("id").`is`(module.id))
            mongoTemplate.updateFirst(query, update, Module::class.java)
            Result(0)
        }
    }

    @ApiOperation("删除模块")
    @DeleteMapping("deleteModule")
    fun deleteModule(id: String): Result<Int?> {
        return try {
            val treeSet = mongoTemplate.find(Query.query(Criteria.where("moduleId").`is`(id)), Tree::class.java)
            if (treeSet.isNotEmpty()) {
                Result(500, "该模块下有树，无法删除！")
            } else {
                val update = Update()
                update.set("deleteFlag", 1)
                val query = Query.query(Criteria.where("id").`is`(id))
                mongoTemplate.updateFirst(query, update, Module::class.java)
                Result(0)
            }
        } catch (e: Exception) {
            Result(500, e.toString())
        }
    }

    @ApiOperation("新增或修改树")
    @PostMapping("createOrUpdateTree")
    fun createOrUpdateTree(@RequestBody tree: Tree): Result<Int?> {
        return if (tree.id.isNullOrBlank()) {
            mongoTemplate.insert(tree, "tree")
            Result(0)
        } else {
            val update = Update()
            update.set("name", tree.name)
            tree.treeType?.let { update.set("treeType", it) }
            val query = Query.query(Criteria.where("id").`is`(tree.id))
            mongoTemplate.updateFirst(query, update, Tree::class.java)
            Result(0)
        }
    }

    @ApiOperation("新增树类型")
    @PostMapping("createTreeType")
    fun createTreeType(@RequestBody treeType: TreeType): Result<Int?> {
        mongoTemplate.insert(treeType, "tree_type")
        return Result(0)
    }

    @ApiOperation("删除树")
    @DeleteMapping("deleteTree")
    fun deleteTree(id: String): Result<Int?> {
        val query = Query.query((Criteria.where("treeId").`is`(id)))
        val res = mongoTemplate.find(query, EntityClass::class.java)
        return if (res.isNotEmpty()) {
            Result(500, "此树非空，禁止删除！")
        } else {
            mongoTemplate.remove(Query.query((Criteria.where("id").`is`(id))), Tree::class.java)
            Result(0)
        }
    }

    @ApiOperation("删除树类型")
    @DeleteMapping("deleteTreeType")
    fun deleteTreeType(id: String): Result<Int?> {
        mongoTemplate.remove(Query.query((Criteria.where("id").`is`(id))), TreeType::class.java)
        return Result(0)
    }

    @ApiOperation("新增或修改实体类")
    @PostMapping("creatOrUpdateClass")
    fun creatOrUpdateClass(@RequestBody entityClass: List<EntityClass>): Result<Int?> {
        var index = 0
        for (it in entityClass) {
            val query = Query.query(Criteria.where("id").`is`(it.id))
            val update = Update()
            update.set("label", it.label)
            update.set("treeId", it.treeId)
            update.set("pid", it.pid)
            update.set("description", it.description)
            update.set("bandFlag", it.bandFlag)
            update.set("propList", it.propList)
            update.set("index", index)
            mongoTemplate.upsert(query, update, EntityClass::class.java)
            index += 1
        }
        return Result(0, "success")
    }

    @ApiOperation("删除实体类")
    @DeleteMapping("deleteClass")
    fun deleteClass(id: String): Result<Int?> {
        return try {
            val query = Query.query((Criteria.where("id").`is`(id)))
            val queryRes = mongoTemplate.findOne(query, EntityClass::class.java)
            if (queryRes != null) {
                if ("1" == queryRes.bandFlag) {
                    Result(500, "该实体类已被绑定，无法删除！")
                } else {
                    mongoTemplate.remove(query, EntityClass::class.java)
                    Result(0)
                }
            } else {
                Result(0)
            }
        } catch (e: Exception) {
            Result(500, e.toString())
        }
    }

    @ApiOperation("新增或修改数据属性")
    @PostMapping("creatOrUpdateDataProp")
    fun creatOrUpdateDataProp(@RequestBody dataProp: DataProp): Result<Int?> {
        val query = Query.query(Criteria.where("treeId").`is`(dataProp.treeId))
        val update = Update.update("dataPropList", dataProp.dataPropList)
        mongoTemplate.upsert(query, update, DataProp::class.java)
        return Result(0, "success")
    }

    @ApiOperation("新增或修改对象属性")
    @PostMapping("creatOrUpdateObjectProp")
    fun creatOrUpdateObjectProp(@RequestBody objectProp: ObjectProp): Result<Int?> {
        val query = Query.query(Criteria.where("treeId").`is`(objectProp.treeId))
        val update = Update.update("objectPropList", objectProp.objectPropList)
        mongoTemplate.upsert(query, update, ObjectProp::class.java)
        return Result(0, "success")
    }

    @ApiOperation("新增或修改数据类型")
    @PostMapping("creatOrUpdateDataType")
    fun creatOrUpdateDataType(@RequestBody dataType: ArrayList<DataType>): Result<Int?> {
        mongoTemplate.dropCollection("data_type")
        mongoTemplate.insert(dataType, "data_type")
        return Result(0, "success")
    }

    @ApiOperation("解析Json文件")
    @PostMapping("parseJson")
    fun parseJson(@RequestParam("file") file: MultipartFile,
                  @RequestParam("treeId") treeId: String) {
        val inputStream = file.inputStream
        val tempFile = File.createTempFile("temp", ".json")
        org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, tempFile)
        val content = tempFile.readText()
        val jsonObject = JsonParser().parse(content).asJsonObject.get("entityList").asJsonArray
        jsonObject.map {
            val label = it.asJsonObject.get("label").asString
            val entity = EntityClass(null, treeId, label, "0", "", "0",
                    arrayListOf())
            mongoTemplate.insert(entity, "entity_class")
        }
        tempFile.delete()
    }

}