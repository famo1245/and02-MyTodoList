package com.boostcamp.planj.data.model

import com.google.gson.annotations.SerializedName

data class DeleteScheduleBody(
    @SerializedName("scheduleUuid") val scheduleUuid: String
)
