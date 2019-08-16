@file:Suppress("DEPRECATION")

package com.aegis.kotlindemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import springfox.documentation.swagger2.annotations.EnableSwagger2
import javax.servlet.MultipartConfigElement

@SpringBootApplication
@EnableSwagger2
class KotlindemoApplication

fun main(args: Array<String>) {
    runApplication<KotlindemoApplication>(*args)
}

@Bean
fun  multipartConfigElement(): MultipartConfigElement {
    val factory = MultipartConfigFactory()
    //文件最大
    factory.setMaxFileSize("100MB") //KB,MB
    /// 设置总上传数据总大小
    factory.setMaxRequestSize("100MB")
    return factory.createMultipartConfig()
}
