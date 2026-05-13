package com.opencode.mobile.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opencode.mobile.utils.LogManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showExportDialog by remember { mutableStateOf(false) }

    // 自动滚动到最新日志
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 过滤按钮
                    FilterMenu(
                        currentFilter = uiState.filterLevel,
                        onFilterSelected = { viewModel.setFilterLevel(it) }
                    )

                    // 导出按钮
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "导出")
                    }

                    // 清除按钮
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "清除")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 状态栏
            if (uiState.exportStatus.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (uiState.exportStatus.contains("成功")) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = uiState.exportStatus,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 日志列表
            if (uiState.logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无日志",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.logs) { entry ->
                        LogEntryItem(entry = entry)
                    }
                }
            }
        }
    }

    // 导出对话框
    if (showExportDialog) {
        ExportDialog(
            path = uiState.exportPath,
            onPathChange = { viewModel.setExportPath(it) },
            onExport = {
                viewModel.exportLogs()
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun FilterMenu(
    currentFilter: LogManager.LogLevel?,
    onFilterSelected: (LogManager.LogLevel?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.FilterList, contentDescription = "过滤")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("全部") },
                onClick = {
                    onFilterSelected(null)
                    expanded = false
                },
                leadingIcon = {
                    if (currentFilter == null) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )

            LogManager.LogLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.name) },
                    onClick = {
                        onFilterSelected(level)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentFilter == level) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LogEntryItem(entry: LogManager.LogEntry) {
    val backgroundColor = when (entry.level) {
        LogManager.LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        LogManager.LogLevel.WARN -> Color(0xFFFFF3CD)
        LogManager.LogLevel.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        LogManager.LogLevel.DEBUG -> Color.Transparent
    }

    val textColor = when (entry.level) {
        LogManager.LogLevel.ERROR -> MaterialTheme.colorScheme.error
        LogManager.LogLevel.WARN -> Color(0xFF856404)
        LogManager.LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogManager.LogLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 级别标签
                Text(
                    text = entry.level.icon,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(textColor, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 时间
                Text(
                    text = entry.formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Tag
                Text(
                    text = entry.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 消息内容
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 异常堆栈
            entry.throwable?.let { throwable ->
                Text(
                    text = throwable.stackTraceToString().take(500),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ExportDialog(
    path: String,
    onPathChange: (String) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出日志") },
        text = {
            Column {
                Text("请输入导出路径：")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = path,
                    onValueChange = onPathChange,
                    placeholder = { Text("/sdcard/Documents/OpenCode/logs/") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onExport) {
                Text("导出")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
