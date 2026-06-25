package com.kam666.mealplanner.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kam666.mealplanner.data.local.MealPlannerDatabase
import com.kam666.mealplanner.domain.usecase.backup.ExportBackupUseCase
import com.kam666.mealplanner.domain.usecase.backup.ExportExcelUseCase
import com.kam666.mealplanner.domain.usecase.backup.ImportBackupUseCase
import com.kam666.mealplanner.presentation.common.AppLanguage
import com.kam666.mealplanner.presentation.common.AppPreferencesHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Loading : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val prefs: AppPreferencesHolder,
    private val exportBackup: ExportBackupUseCase,
    private val exportExcel: ExportExcelUseCase,
    private val importBackup: ImportBackupUseCase,
    private val database: MealPlannerDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _backupState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    fun exportToUri(uri: Uri) {
        _backupState.value = BackupUiState.Loading
        viewModelScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) { exportBackup() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray())
                    } ?: error("No se pudo abrir el archivo")
                }
            }.fold(
                onSuccess = { _backupState.value = BackupUiState.Success("backup_export") },
                onFailure = { e -> _backupState.value = BackupUiState.Error(e.message ?: "Error") }
            )
        }
    }

    fun importFromUri(uri: Uri) {
        _backupState.value = BackupUiState.Loading
        viewModelScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: error("No se pudo leer el archivo")
                }
                withContext(Dispatchers.IO) { importBackup(json) }
            }.fold(
                onSuccess = { count -> _backupState.value = BackupUiState.Success("backup_import:$count") },
                onFailure = { e -> _backupState.value = BackupUiState.Error(e.message ?: "Error") }
            )
        }
    }

    fun clearAllData() {
        _backupState.value = BackupUiState.Loading
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { database.clearAllTables() } }.fold(
                onSuccess = { _backupState.value = BackupUiState.Success("clear_data") },
                onFailure = { e -> _backupState.value = BackupUiState.Error(e.message ?: "Error") }
            )
        }
    }

    fun exportExcelToUri(uri: Uri) {
        _backupState.value = BackupUiState.Loading
        viewModelScope.launch {
            runCatching {
                val bytes = withContext(Dispatchers.IO) { exportExcel() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(bytes)
                    } ?: error("No se pudo abrir el archivo")
                }
            }.fold(
                onSuccess = { _backupState.value = BackupUiState.Success("excel_export") },
                onFailure = { e -> _backupState.value = BackupUiState.Error(e.message ?: "Error") }
            )
        }
    }

    fun clearBackupState() { _backupState.value = BackupUiState.Idle }
}
