package com.example.opengledit.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Ext {

    suspend fun <T> io(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO) {
        block.invoke(this)
    }

    suspend fun <T> ui(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main) {
        block.invoke(this)
    }
}