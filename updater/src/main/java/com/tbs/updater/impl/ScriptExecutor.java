package com.tbs.updater.impl;

import com.tbs.updater.exception.UpdateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 外部脚本执行组件
 * <p>
 * 基于ProcessBuilder执行部署/启动脚本，工作目录为脚本所在目录，
 * 标准错误合并到标准输出并实时透传到控制台。
 * 无共享可变状态，可安全复用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ScriptExecutor {

    /**
     * 脚本输出透传前缀
     */
    private static final String OUTPUT_PREFIX = "[script] ";

    /**
     * 执行外部脚本并等待结束
     * <p>
     * 注意：若脚本内部启动常驻进程且不退出，本方法会一直阻塞等待，
     * 此类脚本应自行将服务守护化（如nohup、systemd）后立即返回。
     * </p>
     *
     * @param scriptPath 脚本文件路径
     * @param args       传递给脚本的参数列表
     * @return 脚本进程退出码
     * @throws UpdateException 脚本不存在、不可执行、启动失败、读取输出失败或被中断时抛出SCRIPT_ERROR
     */
    public int execute(String scriptPath, String... args) throws UpdateException {
        File script = new File(scriptPath);
        if (!script.exists() || !script.isFile()) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR, "脚本文件不存在: " + scriptPath);
        }
        if (!script.canExecute()) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR, "脚本文件不可执行: " + scriptPath);
        }
        List<String> command = new ArrayList<>();
        /*
         * 根据脚本扩展名决定执行方式：
         * .sh/.bash 脚本显式用 bash 执行，避免 macOS 等平台直接执行时参数丢失
         * .bat/.cmd 脚本用 cmd /c 执行，确保 Windows 上参数正确传递
         * 其他脚本直接执行（依赖 shebang 或 OS 关联）
         */
        String absolutePath = script.getAbsolutePath();
        if (absolutePath.endsWith(".sh") || absolutePath.endsWith(".bash")) {
            command.add("bash");
        } else if (absolutePath.endsWith(".bat") || absolutePath.endsWith(".cmd")) {
            command.add("cmd");
            command.add("/c");
        }
        command.add(absolutePath);
        if (args != null) {
            Collections.addAll(command, args);
        }
        System.out.println(OUTPUT_PREFIX + "执行命令: " + command);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        File parentDir = script.getParentFile();
        if (parentDir != null) {
            processBuilder.directory(parentDir);
        }
        processBuilder.redirectErrorStream(true);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "脚本启动失败: " + scriptPath, e);
        }
        try {
            drainOutput(process);
            return process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "脚本执行被中断: " + scriptPath, e);
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "读取脚本输出失败: " + scriptPath, e);
        } finally {
            process.destroy();
        }
    }

    /**
     * 逐行读取脚本输出并透传到控制台
     * <p>
     * 持续读取直到进程关闭输出流，避免输出缓冲区写满导致进程阻塞。
     * 使用系统默认编码读取：Windows 上 cmd.exe 管道输出为 GBK，
     * Linux/macOS 上 bash 输出为 UTF-8，与各自系统默认编码一致。
     * </p>
     *
     * @param process 脚本进程
     * @throws IOException 读取输出流失败时抛出
     */
    private void drainOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(OUTPUT_PREFIX + line);
            }
        }
    }
}
