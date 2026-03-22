#!/bin/bash
# 项目检查和构建辅助脚本

echo "========================================"
echo "  事件AI量化APP - 项目检查"
echo "========================================"
echo ""

# 检查项目结构
echo "📁 检查项目结构..."
REQUIRED_FILES=(
    "app/src/main/java/com/shijian/aitrading/ui/MainActivity.kt"
    "app/src/main/java/com/shijian/aitrading/ui/LoginActivity.kt"
    "app/src/main/java/com/shijian/aitrading/service/TradingService.kt"
    "app/src/main/java/com/shijian/aitrading/utils/Config.kt"
    "app/src/main/java/com/shijian/aitrading/utils/HttpUtil.kt"
    "app/src/main/java/com/shijian/aitrading/utils/PreferenceManager.kt"
    "app/src/main/res/layout/activity_main.xml"
    "app/src/main/res/layout/activity_login.xml"
    "app/src/main/AndroidManifest.xml"
)

ALL_PRESENT=true
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ $file - 缺失"
        ALL_PRESENT=false
    fi
done

echo ""

# 检查关键权限
echo "🔐 检查Android权限配置..."
if grep -q "android.permission.BIND_ACCESSIBILITY_SERVICE" app/src/main/AndroidManifest.xml; then
    echo "  ✅ 无障碍服务权限已配置"
else
    echo "  ❌ 无障碍服务权限缺失"
fi

if grep -q "android.permission.FOREGROUND_SERVICE" app/src/main/AndroidManifest.xml; then
    echo "  ✅ 前台服务权限已配置"
else
    echo "  ❌ 前台服务权限缺失"
fi

echo ""

# 检查资源文件
echo "🎨 检查资源文件..."
DRAWABLE_COUNT=$(find app/src/main/res/drawable -name "*.xml" 2>/dev/null | wc -l)
LAYOUT_COUNT=$(find app/src/main/res/layout -name "*.xml" 2>/dev/null | wc -l)
echo "  📄 Drawable资源: $DRAWABLE_COUNT 个"
echo "  📄 Layout布局: $LAYOUT_COUNT 个"

echo ""

# 统计代码行数
echo "📊 代码统计..."
KT_LINES=$(find app/src/main/java -name "*.kt" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
XML_LINES=$(find app/src/main/res -name "*.xml" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
echo "  📝 Kotlin代码: $KT_LINES 行"
echo "  📝 XML资源: $XML_LINES 行"

echo ""

# 检查构建配置
echo "🔧 检查构建配置..."
if [ -f "build.gradle" ]; then
    echo "  ✅ 根目录build.gradle存在"
else
    echo "  ❌ 根目录build.gradle缺失"
fi

if [ -f "app/build.gradle" ]; then
    echo "  ✅ app/build.gradle存在"
else
    echo "  ❌ app/build.gradle缺失"
fi

if [ -f "Dockerfile" ]; then
    echo "  ✅ Dockerfile存在（用于Docker构建）"
else
    echo "  ⚠️ Dockerfile缺失"
fi

echo ""
echo "========================================"

if [ "$ALL_PRESENT" = true ]; then
    echo "✅ 项目结构完整"
    echo ""
    echo "构建方式:"
    echo "  1. Docker构建: ./build.sh"
    echo "  2. Android Studio: 打开项目直接构建"
else
    echo "⚠️ 项目有缺失文件，请检查"
fi

echo "========================================"
