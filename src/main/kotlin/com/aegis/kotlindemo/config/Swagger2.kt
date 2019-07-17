package com.aegis.business.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.service.ApiInfo
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
open class Swagger2 {
  @Bean
  open fun createRestApi(): Docket {
    return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.aegis.kotlindemo"))
            .paths(PathSelectors.any())
            .build()
  }

  private fun apiInfo(): ApiInfo {
    return ApiInfoBuilder()
            .title("标注api")
            .description("标注api")
            .version("1.0")
            .build()
  }

}