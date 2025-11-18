package com.example.bluetooth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.model.data.MeasurementSession
import com.example.bluetooth.model.export.CsvExporter
import com.example.bluetooth.model.repository.MeasurementRepository
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import androidx.lifecycle.asLiveData

class HistoryViewModel(
    private val repository: MeasurementRepository
) : ViewModel() {

    val allSessions: LiveData<List<MeasurementSession>> =
        repository.allMeasurements.asLiveData()

    private val _selectedSession = MutableLiveData<MeasurementSession?>()
    val selectedSession: LiveData<MeasurementSession?> = _selectedSession

    private val _exportStatus = MutableLiveData<ExportStatus>()
    val exportStatus: LiveData<ExportStatus> = _exportStatus

    fun selectSession(sessionId: String) {
        viewModelScope.launch {
            val session = repository.getMeasurementById(sessionId)
            _selectedSession.value = session
        }
    }

    fun exportSession(sessionId: String) {
        viewModelScope.launch {
            try {
                val session = repository.getMeasurementById(sessionId)
                session?.let {
                    val uri = CsvExporter.exportToCSV(it)
                    _exportStatus.value = ExportStatus.Success(uri)
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(sessionId)
        }
    }
}

private fun Flow.asLiveData(): LiveData<List<MeasurementSession>> {
    return TODO("Provide the return value")
}
