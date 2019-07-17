package com.aegis.kotlindemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableSwagger2
class KotlindemoApplication

fun main(args: Array<String>) {
    runApplication<KotlindemoApplication>(*args)
}
