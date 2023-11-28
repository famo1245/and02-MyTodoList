package com.boostcamp.planj.data.repository

import com.boostcamp.planj.data.model.Category
import com.boostcamp.planj.data.model.PatchCategoryResponse
import com.boostcamp.planj.data.model.PatchScheduleBody
import com.boostcamp.planj.data.model.PatchScheduleResponse
import com.boostcamp.planj.data.model.PostCategoryBody
import com.boostcamp.planj.data.model.PostCategoryResponse
import com.boostcamp.planj.data.model.PostScheduleResponse
import com.boostcamp.planj.data.model.Schedule
import com.boostcamp.planj.data.model.User
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    fun getSchedules(): Flow<List<Schedule>>

    suspend fun insertSchedule(schedule: Schedule)

    suspend fun deleteSchedule(schedule: Schedule)

    suspend fun deleteScheduleUsingId(id: String)

    fun getCategories(): Flow<List<String>>

    fun getAllCategories(): Flow<List<Category>>

    suspend fun insertCategory(category: Category)

    suspend fun deleteCategory(category: Category)

    suspend fun updateCategory(category: Category)

    fun getWeekSchedule(): Flow<List<Schedule>>

    fun getCategoryTitleSchedule(title: String): Flow<List<Schedule>>

    suspend fun insertUser(email: String)

    suspend fun deleteUser(email: String)

    fun getAllUser(): Flow<List<User>>

    fun searchSchedule(input: String): Flow<List<Schedule>>

    fun postCategory(postCategoryBody: PostCategoryBody): Flow<PostCategoryResponse>

    fun postSchedule(categoryId: String, title: String, endTime: String): Flow<PostScheduleResponse>
    fun getUser(): Flow<String>

    fun getCategory(categoryName: String): Category

    suspend fun deleteScheduleApi(scheduleUuid: String)

    suspend fun deleteCategoryApi(categoryUuid: String)

    suspend fun updateSchedule(schedule: Schedule)

    suspend fun updateScheduleUsingCategory(categoryNameBefore: String, categoryAfter: String)

    fun patchSchedule(patchScheduleBody: PatchScheduleBody): Flow<PatchScheduleResponse>

    suspend fun deleteScheduleUsingCategoryName(categoryName: String)

    suspend fun updateCategoryApi(
        categoryUuid: String,
        categoryName: String
    ): Flow<PatchCategoryResponse>
}