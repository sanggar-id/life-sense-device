package app.bbr.minardebug

import app.bbr.minardebug.data.LifeSenseRepository
import app.bbr.minardebug.data.LifeSenseRepositoryImpl
import app.bbr.minardebug.data.SharedPreferencesRepository
import app.bbr.minardebug.data.SharedPreferencesRepositoryImpl

object Dependencies {

    private fun sharedPrefRepository(): SharedPreferencesRepository {
        return SharedPreferencesRepositoryImpl()
    }

    fun lifeSenseRepository(): LifeSenseRepository {
        return LifeSenseRepositoryImpl(
            sharedPrefRepository()
        )
    }
}
