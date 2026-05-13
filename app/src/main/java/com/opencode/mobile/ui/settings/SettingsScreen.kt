package com.opencode.mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 页面可见时刷新状态
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // AI 模型设置
            SettingsSection(title = "AI 模型") {
                // Provider 选择
                var expanded by remember { mutableStateOf(false) }
                val providers = listOf("OpenAI", "Claude", "Ollama", "自定义")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.provider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("模型提供商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    viewModel.setProvider(provider)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // API Key
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = { viewModel.setApiKey(it) },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Base URL
                OutlinedTextField(
                    value = uiState.baseUrl,
                    onValueChange = { viewModel.setBaseUrl(it) },
                    label = { Text("Base URL (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://api.openai.com/v1/") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 模型选择
                OutlinedTextField(
                    value = uiState.model,
                    onValueChange = { viewModel.setModel(it) },
                    label = { Text("模型") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("gpt-3.5-turbo") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PC 连接设置
            SettingsSection(title = "PC 连接") {
                OutlinedTextField(
                    value = uiState.pcServerUrl,
                    onValueChange = { viewModel.setPcServerUrl(it) },
                    label = { Text("PC 服务器地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("http://192.168.1.100:4096") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.testConnection() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("测试连接")
                }

                if (uiState.connectionStatus.isNotEmpty()) {
                    Text(
                        text = uiState.connectionStatus,
                        color = if (uiState.connectionStatus.contains("成功")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 设备控制设置
            SettingsSection(title = "设备控制") {
                // 无障碍服务状态
                SettingsItem(
                    title = "无障碍服务",
                    subtitle = if (uiState.isAccessibilityEnabled) "已启用" else "未启用",
                    onClick = { viewModel.openAccessibilitySettings() }
                )

                // Shizuku 状态
                SettingsItem(
                    title = "Shizuku (ADB)",
                    subtitle = if (uiState.isShizukuAvailable) "已连接" else "未连接",
                    onClick = { viewModel.checkShizuku() }
                )

                // 权限设置
                SettingsItem(
                    title = "权限管理",
                    subtitle = "管理应用权限",
                    onClick = { viewModel.openPermissionSettings() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 关于
            SettingsSection(title = "关于") {
                SettingsItem(
                    title = "版本",
                    subtitle = "1.0.0"
                )

                SettingsItem(
                    title = "开源许可",
                    subtitle = "MIT License"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
