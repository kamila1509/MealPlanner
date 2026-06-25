package com.kam666.mealplanner.presentation.profile

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kam666.mealplanner.R
import com.kam666.mealplanner.presentation.common.AppLanguage
import com.kam666.mealplanner.presentation.common.LanguageToggle
import com.kam666.mealplanner.presentation.common.SectionLabel
import com.kam666.mealplanner.presentation.common.SurfaceCard
import com.kam666.mealplanner.presentation.theme.CoralPrimary
import java.time.LocalDate

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val language by viewModel.prefs.language.collectAsStateWithLifecycle()
    val darkMode by viewModel.prefs.darkMode.collectAsStateWithLifecycle()
    val defaultServings by viewModel.prefs.defaultServings.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val today = LocalDate.now()
    val backupFileName = "mealplanner_backup_$today.json"

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportToUri(it) } }

    val excelMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    val excelFileName = "recetario_$today.xlsx"
    val exportExcelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(excelMime)
    ) { uri -> uri?.let { viewModel.exportExcelToUri(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirm = true
        }
    }

    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    // Handle backup state messages
    val backupSuccessExport = stringResource(R.string.backup_success)
    val backupSuccessExcel = stringResource(R.string.excel_export_success)
    val backupSuccessImport = stringResource(R.string.restore_success)
    val backupSuccessClear = stringResource(R.string.clear_data_success)
    LaunchedEffect(backupState) {
        val msg = when {
            backupState is BackupUiState.Success && (backupState as BackupUiState.Success).message == "backup_export" -> backupSuccessExport
            backupState is BackupUiState.Success && (backupState as BackupUiState.Success).message == "excel_export" -> backupSuccessExcel
            backupState is BackupUiState.Success && (backupState as BackupUiState.Success).message.startsWith("backup_import") -> backupSuccessImport
            backupState is BackupUiState.Success && (backupState as BackupUiState.Success).message == "clear_data" -> backupSuccessClear
            backupState is BackupUiState.Error -> (backupState as BackupUiState.Error).message
            else -> null
        }
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBackupState()
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text(stringResource(R.string.restore_confirm_title), fontWeight = FontWeight.ExtraBold) },
            text = { Text(stringResource(R.string.restore_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri?.let { viewModel.importFromUri(it) }
                    showRestoreConfirm = false
                }) { Text(stringResource(R.string.action_confirm), color = CoralPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_confirm_title), fontWeight = FontWeight.ExtraBold) },
            text = { Text(stringResource(R.string.clear_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearConfirm = false
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Preferencias
            item {
                SectionLabel(stringResource(R.string.pref_section))
                SurfaceCard {
                    // Idioma
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.label_language), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        LanguageToggle(language = language, onSelect = { viewModel.prefs.setLanguage(it) })
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Modo oscuro
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.dark_mode), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { viewModel.prefs.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary, checkedTrackColor = CoralPrimary.copy(alpha = 0.4f))
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Porciones por defecto
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.default_servings), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (defaultServings > 1) viewModel.prefs.setDefaultServings(defaultServings - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                            }
                            Text("$defaultServings", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.widthIn(min = 24.dp))
                            IconButton(
                                onClick = { viewModel.prefs.setDefaultServings(defaultServings + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                            }
                        }
                    }
                }
            }

            // Mis datos
            item {
                SectionLabel(stringResource(R.string.data_section))
                SurfaceCard {
                    DataRow(
                        label = stringResource(R.string.backup_export),
                        emoji = "↑",
                        enabled = backupState !is BackupUiState.Loading,
                        onClick = { exportLauncher.launch(backupFileName) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DataRow(
                        label = stringResource(R.string.excel_export),
                        emoji = "📊",
                        enabled = backupState !is BackupUiState.Loading,
                        onClick = { exportExcelLauncher.launch(excelFileName) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DataRow(
                        label = stringResource(R.string.backup_import),
                        emoji = "↓",
                        enabled = backupState !is BackupUiState.Loading,
                        onClick = { importLauncher.launch("application/json") }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DataRow(
                        label = stringResource(R.string.clear_data),
                        emoji = "🗑",
                        enabled = backupState !is BackupUiState.Loading,
                        onClick = { showClearConfirm = true },
                        labelColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Acerca de
            item {
                SectionLabel(stringResource(R.string.about_section))
                SurfaceCard {
                    Column(modifier = Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.brand), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text(
                            stringResource(R.string.app_version, versionName),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("© 2026", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (backupState is BackupUiState.Loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = CoralPrimary, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    label: String,
    emoji: String,
    enabled: Boolean,
    onClick: () -> Unit,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = if (enabled) labelColor else labelColor.copy(alpha = 0.4f))
        }
    }
}
