package com.boostcamp.planj.data.repository

import com.boostcamp.planj.data.db.AppDatabase
import com.boostcamp.planj.data.model.Schedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val db : AppDatabase
) : MainRepository {

    override fun insertSchedule(schedule: Schedule) {
        TODO("Not yet implemented")
    }

    override fun getSchedules(): Flow<List<Schedule>> {
        TODO("Not yet")
    }
}