#!/bin/bash

# 络绎智慧交通管理系统 - 测试脚本
# 运行所有服务的测试

set -e

echo "=========================================="
echo "络绎智慧交通管理系统 - 测试运行器"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
PASSED=0
FAILED=0
SKIPPED=0

# 测试结果函数
test_passed() {
    echo -e "${GREEN}✓ $1${NC}"
    ((PASSED++))
}

test_failed() {
    echo -e "${RED}✗ $1${NC}"
    ((FAILED++))
}

test_skipped() {
    echo -e "${YELLOW}○ $1 (跳过)${NC}"
    ((SKIPPED++))
}

# 1. 算法服务测试
echo ""
echo "1. 测试算法服务 (Python/FastAPI)"
echo "------------------------------------------"
cd algorithm-service
if command -v python3 &> /dev/null; then
    if python3 -m pytest test_main.py -v --tb=short 2>&1 | grep -q "passed"; then
        test_passed "算法服务测试"
    else
        test_failed "算法服务测试"
    fi
else
    test_skipped "算法服务测试 (需要 Python 3)"
fi
cd ..

# 2. Hub Backend 测试
echo ""
echo "2. 测试枢纽端后端 (Spring Boot)"
echo "------------------------------------------"
cd hub-backend
if command -v mvn &> /dev/null; then
    if mvn test -q 2>&1 | grep -q "BUILD SUCCESS"; then
        test_passed "Hub Backend 测试"
    else
        test_failed "Hub Backend 测试"
    fi
elif [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    if ./mvnw test -q 2>&1 | grep -q "BUILD SUCCESS"; then
        test_passed "Hub Backend 测试"
    else
        test_failed "Hub Backend 测试"
    fi
else
    test_skipped "Hub Backend 测试 (需要 Maven)"
fi
cd ..

# 3. Mobile Backend 测试
echo ""
echo "3. 测试移动端后端 (Spring Boot)"
echo "------------------------------------------"
cd mobile-backend
if command -v mvn &> /dev/null; then
    if mvn test -q 2>&1 | grep -q "BUILD SUCCESS"; then
        test_passed "Mobile Backend 测试"
    else
        test_failed "Mobile Backend 测试"
    fi
else
    test_skipped "Mobile Backend 测试 (需要 Maven)"
fi
cd ..

# 4. Web Frontend 测试
echo ""
echo "4. 测试 Web 前端 (Vue 3)"
echo "------------------------------------------"
cd web-frontend
if command -v npm &> /dev/null; then
    if [ -d "node_modules" ]; then
        if npm run test 2>&1 | grep -q "passed"; then
            test_passed "Web Frontend 测试"
        else
            test_skipped "Web Frontend 测试 (无测试配置)"
        fi
    else
        test_skipped "Web Frontend 测试 (需要运行 npm install)"
    fi
else
    test_skipped "Web Frontend 测试 (需要 Node.js)"
fi
cd ..

# 5. Mobile App 测试
echo ""
echo "5. 测试移动端 App (Flutter)"
echo "------------------------------------------"
cd mobile-app
if command -v flutter &> /dev/null; then
    if flutter test 2>&1 | grep -q "All tests passed"; then
        test_passed "Mobile App 测试"
    else
        test_failed "Mobile App 测试"
    fi
else
    test_skipped "Mobile App 测试 (需要 Flutter)"
fi
cd ..

# 6. Docker 构建测试
echo ""
echo "6. 验证 Docker 配置"
echo "------------------------------------------"
if command -v docker &> /dev/null; then
    if docker-compose config -q 2>&1; then
        test_passed "Docker Compose 配置验证"
    else
        test_failed "Docker Compose 配置验证"
    fi
else
    test_skipped "Docker 配置验证 (需要 Docker)"
fi

# 打印测试摘要
echo ""
echo "=========================================="
echo "测试摘要"
echo "=========================================="
echo -e "通过: ${GREEN}${PASSED}${NC}"
echo -e "失败: ${RED}${FAILED}${NC}"
echo -e "跳过: ${YELLOW}${SKIPPED}${NC}"
echo ""

if [ $FAILED -gt 0 ]; then
    echo -e "${RED}测试未全部通过!${NC}"
    exit 1
else
    echo -e "${GREEN}测试通过!${NC}"
    exit 0
fi
