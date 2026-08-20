@echo off
chcp 65001 >nul 2>&1
rem ============================================
rem 部署脚本（Windows）：关闭旧服务并部署新文件
rem 用法：deploy.bat <主JAR包路径> [附加文件1] [附加文件2] ...
rem 说明：
rem   %1 = 主JAR包（必须），用于停止旧服务并启动新服务
rem   %2... = 附加文件（可选），如配置文件、插件包等
rem ============================================

set "JAR_FILE=%~1"
set APP_PORT=8080

echo [deploy] 参数数量: %*
echo [deploy] 所有文件:

set INDEX=0
:LOOP_ARGS
if "%~1"=="" goto END_ARGS
set /a INDEX+=1
call echo [deploy]   %%%INDEX% = %%~1
shift
goto LOOP_ARGS
:END_ARGS

rem 恢复参数，重新取 %1 作为 JAR_FILE
rem 因为 shift 已经移动了参数，需要从环境变量获取
if "%JAR_FILE%"=="" (
    echo [deploy] 错误: 未传入主JAR包路径（第一个参数）
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo [deploy] 错误: 主JAR文件不存在: %JAR_FILE%
    exit /b 1
)

rem ---------- 处理附加文件（可选） ----------
if %INDEX% GTR 1 (
    echo [deploy] 检测到 %INDEX% 个文件，可在脚本中自定义复制/解压逻辑
    rem 示例：将附加文件复制到JAR同目录
    rem for %%F in (%*) do (
    rem     copy "%%~F" "%~dp1" >nul
    rem     echo [deploy] 已复制: %%~nxF -^> %~dp1
    rem )
)

rem ---------- 停止旧服务 ----------
echo [deploy] 尝试关闭监听 %APP_PORT% 端口的进程...

rem 使用 netstat 查找监听该端口的进程 PID
set "PID="
for /f "tokens=5" %%a in ('netstat -aon ^| findstr "LISTENING" ^| findstr ":%APP_PORT% "') do (
    set "PID=%%a"
)

if defined PID (
    echo [deploy] 找到进程 PID: %PID%，正在关闭...
    taskkill /PID %PID% /F >nul 2>&1
    if %errorlevel% equ 0 (
        echo [deploy] 旧服务已停止
    ) else (
        echo [deploy] 警告: 关闭进程失败，可能需要管理员权限
    )
) else (
    echo [deploy] 端口 %APP_PORT% 无进程监听，跳过停止步骤
)

rem ---------- 启动新服务 ----------
echo [deploy] 启动主JAR包: %JAR_FILE%
echo [deploy] 执行命令: java -jar "%JAR_FILE%"

rem 新窗口启动（可见控制台），日志同时输出到同目录下的 app.log
rem -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 确保中文日志不乱码
set "JAR_DIR=%~dp1"
start "App-%APP_PORT%" java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "%JAR_FILE%" > "%JAR_DIR%app.log" 2>&1

echo [deploy] 新服务已启动（控制台窗口已打开）
echo [deploy] 日志文件: %JAR_DIR%app.log

exit /b 0
