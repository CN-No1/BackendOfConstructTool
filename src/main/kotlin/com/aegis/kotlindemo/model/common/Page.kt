package com.aegis.kotlindemo.model.common

import org.springframework.data.domain.Page

data class Page<T>(val totalPage: Long, val totalCount: Long, val data: ArrayList<T>)