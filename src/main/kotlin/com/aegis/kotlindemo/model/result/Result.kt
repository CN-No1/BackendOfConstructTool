package com.aegis.kotlindemo.model.result

import java.io.Serializable

open class Result<T> : Serializable {
    private var code = 0
    private var message = ""
    private var data: T? = null

    constructor(code: Int) {
        this.code = code
    }

    constructor(message: String) {
        this.message= message
    }

    constructor(code: Int, message: String) {
        this.code = code
        this.message = message
    }

    fun setCode(code: Int): Result<T> {
        this.code = code
        return this
    }

    fun setMessage(message: String): Result<T> {
        this.message = message
        return this
    }

    fun setData(data: T): Result<T> {
        this.data = data
        return this
    }

    fun getCode(): Int {
        return code
    }

    fun getMessage(): String {
        return message
    }

    fun getData(): T? {
        return data
    }
}
