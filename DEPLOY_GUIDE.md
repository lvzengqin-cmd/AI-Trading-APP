# 事件AI量化APP - 部署指南

## 🚀 三种构建方式

### 方式一：GitHub Actions自动构建（推荐 ⭐）

**优点**: 无需配置环境，自动构建，可下载APK

**步骤**:

1. **创建GitHub仓库**
   ```bash
   cd shijian-ai-android-new
   git init
   git add .
   git commit -m "Initial commit"
   ```

2. **推送到GitHub**
   ```bash
   git remote add origin https://github.com/你的用户名/shijian-ai-android.git
   git branch -M main
   git push -u origin main
   ```

3. **触发构建**
   - 访问GitHub仓库页面
   - 点击 Actions 标签
   - 选择 "Build APK" 工作流
   - 点击 "Run workflow"

4. **下载APK**
   - 等待构建完成（约5-10分钟）
   - 在Actions页面找到最新运行
   - 下载Artifacts中的APK文件

---

### 方式二：Docker本地构建

**要求**: 已安装Docker

**步骤**:

1. **进入项目目录**
   ```bash
   cd shijian-ai-android-new
   ```

2. **运行构建脚本**
   ```bash
   chmod +x build.sh
   ./build.sh
   ```

3. **获取APK**
   - 构建完成后APK在 `output/` 目录

---

### 方式三：Android Studio构建

**要求**: 安装Android Studio

**步骤**:

1. **打开项目**
   - 启动Android Studio
   - 选择 "Open an existing project"
   - 选择 `shijian-ai-android-new` 目录

2. **等待同步**
   - 等待Gradle同步完成
   - 如果提示更新，点击Update

3. **构建APK**
   - 菜单: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或使用快捷键: Ctrl+F9 (Windows/Linux) / Cmd+F9 (Mac)

4. **找到APK**
   - 构建完成后点击右下角提示
   - 或在 `app/build/outputs/apk/debug/` 目录

---

## 📱 安装APK

### 方法1：ADB安装（推荐）

```bash
# 连接手机，开启USB调试
adb devices

# 安装APK
adb install app-debug.apk

# 如果已安装，覆盖安装
adb install -r app-debug.apk
```

### 方法2：直接安装

1. 将APK传输到手机
2. 在手机上点击APK文件
3. 允许安装未知来源应用
4. 完成安装

---

## ⚙️ 使用说明

### 首次使用

1. **开启无障碍权限**
   - 安装后打开APP
   - 点击"去开启权限"
   - 找到"事件AI量化"服务并开启

2. **注册/登录**
   - 注册新账号
   - 复制设备ID联系客服开通会员

3. **设置交易**
   - 设置交易金额（默认20 USDT）
   - 开启自动交易
   - 开启自动亮屏（建议）

4. **准备币安APP**
   - 确保币安APP已安装并登录
   - 进入期权交易页面
   - 选择BTCUSDT交易对

---

## 🔧 常见问题

### Q: 无法安装APK
A: 检查手机是否允许安装未知来源应用

### Q: 无障碍权限自动关闭
A: 将APP加入电池优化白名单，锁定后台

### Q: 无法自动点击
A: 确保币安APP在正确的交易页面

### Q: 信号接收不到
A: 检查网络连接，联系客服检查服务器状态

---

## 📞 技术支持

- **客服QQ**: 1306353623
- **官网**: https://www.yucebot.com

---

**最后更新**: 2026-03-22
