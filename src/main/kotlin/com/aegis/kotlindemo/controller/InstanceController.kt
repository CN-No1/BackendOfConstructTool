package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.instance.InstanceObject
import com.aegis.kotlindemo.model.nlu.NLUEntity
import com.aegis.kotlindemo.model.nlu.Purpose
import com.aegis.kotlindemo.model.result.Result
import com.google.gson.JsonParser
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@RestController
@RequestMapping("instance")
class InstanceController(val mongoTemplate: MongoTemplate) {

    @ApiOperation("新增或修改实例对象")
    @PostMapping("createOrUpdateInstanceObject")
    fun createOrUpdateInstanceObject(@RequestBody instanceObject: InstanceObject): Result<Int> {
        return if (instanceObject.id.isNullOrBlank()) {
            mongoTemplate.insert(instanceObject, "instance_object")
            Result(0)
        } else {
            val update = Update()
            val query = Query.query(Criteria.where("id").`is`(instanceObject.id))
            mongoTemplate.updateFirst(query, update, instanceObject::class.java)
            Result(0)
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
            val instanceObject = InstanceObject(null, text, ArrayList(), newModuleId, newPurpose, "0",
                    hashCode, Date())
            mongoTemplate.insert(instanceObject, "instance_object")
        }
        tempFile.delete()
    }

}