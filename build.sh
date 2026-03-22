#!/bin/bash
# Build script for Shijian AI Trading App

set -e

echo "========================================"
echo "  事件AI量化 - APK构建脚本"
echo "========================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker未安装"
    echo "请先安装Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

echo "✅ Docker已安装"

# Create output directory
mkdir -p output

# Build using Docker
echo ""
echo "📦 开始构建APK..."
echo "这可能需要10-20分钟，请耐心等待"
echo ""

if command -v docker-compose &> /dev/null; then
    docker-compose up --build
else
    docker build -t shijian-ai-builder . && docker run --rm -v "$(pwd)/output:/output" shijian-ai-builder
fi

echo ""
echo "========================================"
if [ -f "output/app-release.apk" ] || [ -f "output/app-debug.apk" ]; then
    echo "✅ 构建成功!"
    echo ""
    echo "APK文件位置:"
    ls -lh output/*.apk
    echo ""
    echo "安装命令:"
    echo "  adb install output/app-release.apk"
else
    echo "⚠️ 构建可能未完成，请检查output目录"
    ls -la output/
fi
echo "========================================"
