package com.aegis.kotlindemo.model.result

data class Result(val code: Int, val message: String) {
    constructor(status: Result) : this(status.code, status.message)
}