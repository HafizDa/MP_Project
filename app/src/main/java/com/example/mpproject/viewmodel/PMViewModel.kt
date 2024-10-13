package com.example.mpproject.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpproject.db.PMDatabase
import com.example.mpproject.db.ParliamentMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class PMViewModel(context: Context) : ViewModel() {
    private val dao = PMDatabase.getInstance(context).parliamentMemberDao()
    val members: Flow<List<ParliamentMember>> = dao.getAll()
    lateinit var member: Flow<ParliamentMember>
        private set
    lateinit var nextMember: Flow<ParliamentMember>
        private set
    lateinit var previousMember: Flow<ParliamentMember>
        private set

    fun updateMember(updatedMember: ParliamentMember?) {
        viewModelScope.launch {
            if (updatedMember != null) {
                dao.update(updatedMember)
                member = members.transform {
                    emit(updatedMember)
                }
            }
        }
    }

    fun setMember(hetekaId: Int?) {
        member = members.transform {
            if (hetekaId == null) {
                emit(it.first())
            } else {
                emit(it.first { it.hetekaId == hetekaId })
            }
        }

        nextMember = members.transform {
            val idx = (it.indexOfFirst { it.hetekaId == member.first().hetekaId } + 1) % it.size
            emit(it[idx])
        }

        previousMember = members.transform {
            val idx = (it.indexOfFirst { it.hetekaId == member.first().hetekaId } - 1 + it.size) % it.size
            emit(it[idx])
        }
    }
}