#!/bin/bash
# ============================================
# 部署脚本：关闭旧服务并部署新文件
# 用法：deploy.sh <主JAR包路径> [附加文件1] [附加文件2] ...
# 说明：
#   $1 = 主JAR包（必须），用于停止旧服务并启动新服务
#   $2... = 附加文件（可选），如配置文件、插件包等，
#           脚本会打印其路径供自定义处理
# ============================================

JAR_FILE="$1"
APP_PORT=8080

echo "[deploy] 参数数量: $#"
echo "[deploy] 所有文件:"
INDEX=0
for FILE in "$@"; do
    INDEX=$((INDEX + 1))
    echo "[deploy]   \$${INDEX} = ${FILE}"
done

if [ -z "$JAR_FILE" ]; then
    echo "[deploy] 错误: 未传入主JAR包路径（第一个参数）"
    exit 1
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "[deploy] 错误: 主JAR文件不存在: $JAR_FILE"
    exit 1
fi

# ---------- 处理附加文件（可选） ----------
if [ $# -gt 1 ]; then
    echo "[deploy] 检测到 $# 个附加文件，可在脚本中自定义复制/解压逻辑"
    # 示例：将附加文件复制到JAR同目录
    # JAR_DIR=$(dirname "$JAR_FILE")
    # for FILE in "${@:2}"; do
    #     cp "$FILE" "$JAR_DIR/"
    #     echo "[deploy] 已复制: $(basename "$FILE") -> $JAR_DIR/"
    # done
fi

# ---------- 停止旧服务 ----------
echo "[deploy] 尝试关闭监听 ${APP_PORT} 端口的进程..."

PID=$(lsof -ti :${APP_PORT} 2>/dev/null)

if [ -n "$PID" ]; then
    echo "[deploy] 找到进程 PID: $PID，正在关闭..."
    kill $PID 2>/dev/null

    # 等待进程退出，最多 10 秒
    for i in $(seq 1 10); do
        if ! kill -0 $PID 2>/dev/null; then
            echo "[deploy] 进程已退出 (耗时 ${i}s)"
            break
        fi
        sleep 1
    done

    # 仍未退出则强制终止
    if kill -0 $PID 2>/dev/null; then
        echo "[deploy] 进程未响应 SIGTERM，强制终止..."
        kill -9 $PID 2>/dev/null
        sleep 1
    fi

    echo "[deploy] 旧服务已停止"
else
    echo "[deploy] 端口 ${APP_PORT} 无进程监听，跳过停止步骤"
fi

# ---------- 启动新服务 ----------
echo "[deploy] 启动主JAR包: $JAR_FILE"
echo "[deploy] 执行命令: java -jar $JAR_FILE"

# 后台启动，控制台输出追加到同目录下的 app.log（用 >> 避免重启覆盖历史日志）
# 应用日志由 logback 落盘并滚动（-DLOG_PATH 指定目录），app.log 仅作 JVM 级启动/崩溃兜底
JAR_DIR=$(dirname "$JAR_FILE")
nohup java -DLOG_PATH="${JAR_DIR}/logs" -jar "$JAR_FILE" >> "${JAR_DIR}/app.log" 2>&1 &

NEW_PID=$!
echo "[deploy] 新服务已启动, PID: $NEW_PID"
echo "[deploy] 日志文件: ${JAR_DIR}/app.log"

exit 0
