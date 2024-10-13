package com.example.mpproject.db

import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mpproject.PMApplication
import com.example.mpproject.network.NetworkAPI
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// 02.10.2024 by Hafiz
object DBSynchronizer {
    fun start() {
        val uploadRequest = PeriodicWorkRequestBuilder<DBWorker>(15, TimeUnit.MINUTES).build()
        val workManager = WorkManager.getInstance(PMApplication.appContext)
        workManager.enqueue(uploadRequest)
    }
}

// 02.10.2024 by Hafiz
class DBWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    @OptIn(DelicateCoroutinesApi::class)
    override fun doWork(): Result {
        val db = PMDatabase.getInstance(applicationContext)
        val dao = db.parliamentMemberDao()

        try {
            GlobalScope.launch {
                var parliamentMembers: List<ParliamentMember>? = NetworkAPI.apiService.loadMainData()?.execute()?.body()
                if (parliamentMembers == null) {
                    throw Exception("Failed to fetch main data")
                }

                val extraData = NetworkAPI.apiService.loadExtraData()?.execute()?.body()
                    ?: throw Exception("Failed to fetch extra data")

                // Add extra data to parliament members' list
                parliamentMembers = parliamentMembers.map { memberData1: ParliamentMember ->
                    val memberData2 = extraData.find { it.hetekaId == memberData1.hetekaId }
                    if (memberData2 != null) {
                        return@map memberData1.copy(
                            twitter = memberData2.twitter,
                            bornYear = memberData2.bornYear,
                            constituency = memberData2.constituency
                        )
                    } else {
                        return@map memberData1
                    }
                }

                dao.insertAll(parliamentMembers)
                Log.d("DB", "Data synchronized with remote server")
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}