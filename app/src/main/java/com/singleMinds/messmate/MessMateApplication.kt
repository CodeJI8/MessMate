package com.singleminds.messmate

import android.app.Application
import com.singleminds.messmate.data.local.MessMateDatabase
import com.singleminds.messmate.data.repository.MessMateRepository

class MessMateApplication : Application() {
    val database: MessMateDatabase by lazy { MessMateDatabase.getInstance(this) }
    val repository: MessMateRepository by lazy { MessMateRepository(database) }
}
