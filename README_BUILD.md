# 事件AI量化 - Android APP

## 📱 项目概述

AI量化交易辅助APP，用于接收服务器信号并通过无障碍服务自动执行币安交易。

## ✅ 项目完成状态

### 已完成模块

| 模块 | 状态 | 说明 |
|------|------|------|
| **用户系统** | ✅ 完成 | 登录/注册/设备ID绑定 |
| **主界面** | ✅ 完成 | 状态显示/交易设置/日志 |
| **无障碍服务** | ✅ 完成 | TradingService自动交易核心 |
| **信号接收** | ✅ 完成 | HTTP API获取信号历史 |
| **UI界面** | ✅ 完成 | 登录页/主页面/弹窗/列表 |
| **权限管理** | ✅ 完成 | 无障碍/电池优化/悬浮窗 |

### 项目结构

```
shijian-ai-android-new/
├── app/
│   ├── src/main/
│   │   ├── java/com/shijian/aitrading/
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt          # 主界面
│   │   │   │   ├── LoginActivity.kt         # 登录界面
│   │   │   │   └── adapter/                 # 列表适配器
│   │   │   ├── service/
│   │   │   │   └── TradingService.kt        # 自动交易服务
│   │   │   └── utils/
│   │   │       ├── Config.kt                # 配置文件
│   │   │       ├── HttpUtil.kt              # 网络请求
│   │   │       └── PreferenceManager.kt     # 本地存储
│   │   ├── res/
│   │   │   ├── layout/                      # 界面布局
│   │   │   ├── drawable/                    # 图形资源
│   │   │   ├── values/                      # 字符串/颜色/主题
│   │   │   └── xml/                         # 配置文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── Dockerfile                               # Docker构建配置
├── docker-compose.yml                       # Docker Compose配置
└── build.sh                                 # 一键构建脚本

```

## 🚀 构建方式

### 方式一：Docker构建（推荐）

```bash
# 1. 进入项目目录
cd shijian-ai-android-new

# 2. 运行构建脚本
chmod +x build.sh
./build.sh

# 3. 构建完成后，APK在 output/ 目录
```

### 方式二：手动Docker构建

```bash
# 使用Docker Compose
docker-compose up --build

# 或使用Docker命令
docker build -t shijian-ai-builder .
docker run --rm -v "$(pwd)/output:/output" shijian-ai-builder
```

### 方式三：Android Studio

1. 打开项目目录 `shijian-ai-android-new`
2. 等待Gradle同步完成
3. 点击 Build → Generate Signed Bundle/APK
4. 选择 APK，配置签名（或选择debug签名）
5. 构建完成后APK在 `app/build/outputs/apk/release/`

## 📋 功能说明

### 核心功能

1. **用户系统**
   - 用户名/密码注册登录
   - 设备ID绑定（用于会员验证）
   - JWT Token认证

2. **交易设置**
   - 设置交易金额（USDT）
   - 自动交易开关
   - 自动亮屏开关

3. **自动交易**
   - 基于无障碍服务模拟点击
   - 支持做多/做空双向交易
   - 自动打开币安APP并执行交易

4. **信号系统**
   - 接收服务器交易信号
   - 显示信号历史记录
   - 实时日志输出

### 配置信息

**服务器地址**: `https://shijian-ai-backend.onrender.com`

**客服QQ**: 1306353623

**官网**: https://www.yucebot.com

## ⚠️ 使用须知

1. **无障碍权限**：必须开启无障碍权限才能自动交易
2. **币安APP**：需要安装币安APP并登录
3. **会员限制**：部分功能需要开通会员
4. **风险提示**：自动交易有风险，请谨慎使用

## 🔧 技术栈

- **语言**: Kotlin
- **最小SDK**: 26 (Android 8.0)
- **目标SDK**: 34 (Android 14)
- **架构**: 单Activity + Service
- **网络**: 原生HttpURLConnection + Coroutines

## 📄 许可证

本项目仅供学习交流使用。

---

**构建日期**: 2026-03-22  
**版本**: 1.0.0
