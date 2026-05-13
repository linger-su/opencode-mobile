# OpenCode Mobile

OpenCode Mobile 是一个基于 OpenCode 架构的 Android AI 助手应用，支持通过文字指令控制手机功能。

## 功能特性

- **多模型支持**: 支持 OpenAI、Claude、Ollama 等多种 LLM 提供商
- **设备控制**: 通过文字指令控制手机功能
- **PC 联动**: 可连接 PC 端 OpenCode 服务器
- **无障碍服务**: 使用 Android Accessibility Service 实现自动化操作
- **Shizuku 支持**: 通过 ADB 执行高级操作

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 推荐 Android 12+ 以获得最佳体验

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/opencode-mobile.git
cd opencode-mobile
```

### 2. 使用 Android Studio 打开项目

1. 打开 Android Studio
2. 选择 "Open an existing project"
3. 选择项目目录

### 3. 配置 API Key

1. 打开应用
2. 进入设置页面
3. 选择模型提供商
4. 输入 API Key

### 4. 启用无障碍服务

1. 进入设置 -> 设备控制
2. 点击 "无障碍服务"
3. 找到 "OpenCode Mobile" 并启用

### 5. (可选) 安装 Shizuku

1. 从 Google Play 或 GitHub 安装 Shizuku
2. 启动 Shizuku 服务
3. 在 OpenCode Mobile 中授权

## 使用示例

### 基本对话

```
用户: 你好
助手: 你好！有什么我可以帮助你的吗？
```

### 设备控制指令

```
用户: 打开微信
助手: 已打开微信

用户: 设置亮度为 50%
助手: 已设置亮度为 50%

用户: 截屏
助手: 已截屏，保存至: /sdcard/Pictures/Screenshots/screenshot_xxx.png
```

### 支持的指令

| 类型 | 指令示例 |
|------|---------|
| 应用控制 | 打开微信、关闭抖音 |
| 系统设置 | 设置亮度为 50%、关闭 WiFi |
| 媒体控制 | 播放音乐、下一首 |
| 通知管理 | 查看通知、清除通知 |
| 屏幕操作 | 截屏、录屏、锁屏 |

## 架构说明

```
opencode-mobile/
├── app/                    # 主应用模块
│   ├── data/              # 数据层
│   │   ├── api/           # 网络 API
│   │   ├── database/      # Room 数据库
│   │   └── repository/    # 仓库实现
│   ├── domain/            # 领域层
│   │   ├── model/         # 数据模型
│   │   ├── provider/      # LLM Provider
│   │   ├── parser/        # 指令解析
│   │   └── controller/    # 设备控制
│   ├── ui/                # UI 层
│   │   ├── chat/          # 聊天界面
│   │   ├── settings/      # 设置界面
│   │   └── components/    # 通用组件
│   └── service/           # 系统服务
│       ├── accessibility/ # 无障碍服务
│       └── notification/  # 通知服务
└── core/                  # 核心模块
```

## 开发计划

- [x] 基础框架搭建
- [x] LLM Provider 抽象层
- [x] 基础 Chat UI
- [x] 指令解析系统
- [x] Accessibility Service
- [ ] Shizuku 集成
- [ ] PC 连接功能
- [ ] 语音输入/输出
- [ ] 快捷指令
- [ ] 小组件

## 许可证

MIT License

## 致谢

- [OpenCode](https://opencode.ai) - 原始项目架构
- [Android Jetpack](https://developer.android.com/jetpack) - Android 开发组件
- [Hilt](https://dagger.dev/hilt/) - 依赖注入
- [Shizuku](https://github.com/RikkaApps/Shizuku) - ADB 权限管理
