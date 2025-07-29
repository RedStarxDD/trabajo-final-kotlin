package com.usil.pedidosapp.utils

sealed class ResultUtil<out T> {
    data class Success<out T>(val data:T): ResultUtil<T>()
    data class Error(val message:String, val throwable: Throwable?=null): ResultUtil<Nothing>()
    object Loading: ResultUtil<Nothing>()
}

inline fun <T> ResultUtil<T>.onSuccess(action:(value:T)->Unit): ResultUtil<T> {
    if (this is ResultUtil.Success) action (data)
    return this
}

inline fun <T> ResultUtil<T>.onError(action: (message:String) -> Unit): ResultUtil<T> {
    if(this is ResultUtil.Error) action(message)
    return this
}

fun<T> ResultUtil<T>.getDataOrNull():T?{
    return if(this is ResultUtil.Success) data else null
}

fun <T> ResultUtil<T>.isSuccess():Boolean = this is ResultUtil.Success
fun <T> ResultUtil<T>.isError():Boolean = this is ResultUtil.Error