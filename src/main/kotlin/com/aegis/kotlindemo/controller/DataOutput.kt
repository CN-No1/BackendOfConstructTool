package com.aegis.kotlindemo.controller

import com.aegis.kotlindemo.model.dataOutput.*
import com.aegis.kotlindemo.model.dataOutput.Annotation
import com.aegis.kotlindemo.model.entity.EntityClass
import com.aegis.kotlindemo.model.instance.InstanceObject
import com.aegis.kotlindemo.model.nlu.NLUEntity
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.util.ArrayList
import com.google.gson.Gson
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query


@RestController
@RequestMapping("dataOutput")
class DataOutput(val mongoTemplate: MongoTemplate) {

    @ApiOperation("实例数据导出")
    @GetMapping("getInstanceData")
    fun getInstanceData() {
        val filePath = "D://instanceData.json"
        val instances = mongoTemplate.findAll(InstanceObject::class.java)
        val instanceList = ArrayList<Instance>()
        val annotationList = ArrayList<Annotation>()
        val rangeList = ArrayList<Range>()
        instances.map {
            instanceList.clear()
            annotationList.clear()
            it.instanceList.map { instance ->
                val domain = mongoTemplate.findOne(Query.query(Criteria.where("id").`is`(instance.domain)), EntityClass::class.java)!!.label
                rangeList.clear()
                instance.rangeList.map { range ->
                    val relation = mongoTemplate.findOne(Query.query(Criteria.where("id").`is`(range.relation)), EntityClass::class.java)
                    if (relation!=null){
                        val rangeObj = Range(range.content, relation.label, range.role)
                        rangeList.add(rangeObj)
                    }
                }
                val instanceObj = Instance(domain, rangeList)
                instanceList.add(instanceObj)
            }
            it.annotationList!!.map { annotation ->
                val entity = mongoTemplate.findOne(Query.query(Criteria.where("id").`is`(annotation.entityId)), EntityClass::class.java)!!.label
                val annotationObj = Annotation(entity, annotation.value, annotation.startOffset, annotation.endOffset)
                annotationList.add(annotationObj)
            }
            val instanceOutput = InstanceOutputModel(it.text, instanceList, annotationList)
            //把文本写入文件
            File(filePath).appendText(Gson().toJson(instanceOutput))
        }
    }

    @ApiOperation("文本数据导出")
    @GetMapping("getDocData")
    fun getDocData() {
        val instanceList = mongoTemplate.findAll(NLUEntity::class.java)
        val outputList = arrayListOf<DataOutputModel>()
        instanceList.map {
            val annotationList = ArrayList<Annotation>()
            val intentionList = ArrayList<String>()
            it.annotationList.map {
                val entity = mongoTemplate.findOne(Query.query(Criteria.where("id").`is`(it.entityId)), EntityClass::class.java)!!.label
                val annotation = Annotation(entity, it.value, it.startOffset, it.endOffset)
                annotationList.add(annotation)
            }
            it.intention!!.map {
                intentionList.add(it.label)
            }
            val data = it.id?.let { it1 -> DataOutputModel(it1, it.content, annotationList, intentionList) }
            if (data != null) {
                outputList.add(data)
            }
        }
        val str = Gson().toJson(outputList)
        //把文本写入文件
        val filePath = "D://data.json"
        File(filePath).writeText(str)
    }
}