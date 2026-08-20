package com.tbs.updater.impl;

import com.tbs.updater.exception.UpdateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 外部脚本执行组件
 * <p>
 * 基于ProcessBuilder执行部署脚本，工作目录为脚本所在目录，
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
     * 默认脚本执行超时时间（秒）
     * <p>
     * 超时后强制终止脚本进程，避免脚本卡死导致更新流程永久阻塞。
     * </p>
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 120;

    /**
     * 执行外部脚本并等待结束（默认超时120秒）
     *
     * @param scriptPath 脚本文件路径
     * @param args       传递给脚本的参数列表
     * @return 脚本进程退出码
     * @throws UpdateException 脚本不存在、不可执行、启动失败、超时、读取输出失败或被中断时抛出SCRIPT_ERROR
     */
    public int execute(String scriptPath, String... args) throws UpdateException {
        return execute(DEFAULT_TIMEOUT_SECONDS, scriptPath, args);
    }

    /**
     * 执行外部脚本并等待结束
     * <p>
     * 注意：若脚本内部启动常驻进程且不退出，本方法会在超时后强制终止，
     * 此类脚本应自行将服务守护化（如nohup、systemd、start命令）后立即返回，
     * 或改用 {@link #executeAsync(String, String...)} 只启动不等待。
     * </p>
     *
     * @param timeoutSeconds 超时时间（秒），超时后强制终止脚本进程
     * @param scriptPath     脚本文件路径
     * @param args           传递给脚本的参数列表
     * @return 脚本进程退出码
     * @throws UpdateException 脚本不存在、不可执行、启动失败、超时或被中断时抛出SCRIPT_ERROR
     */
    public int execute(int timeoutSeconds, String scriptPath, String... args) throws UpdateException {
        Process process = startProcess(scriptPath, args);
        try {
            startOutputDrain(process);
            /*
             * 带超时等待，避免脚本卡死导致更新流程永久阻塞
             */
            boolean exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                        "脚本执行超时(" + timeoutSeconds + "秒)，已强制终止: " + scriptPath);
            }
            return process.exitValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "脚本执行被中断: " + scriptPath, e);
        } finally {
            process.destroy();
        }
    }

    /**
     * 启动外部脚本后立即返回，不等待脚本结束
     * <p>
     * 适用于部署脚本等会派生常驻服务进程的场景：Windows 上通过 start 命令
     * 拉起的服务会继承输出管道并持续运行，等待其结束会永久阻塞更新流程。
     * 脚本输出仍由后台线程实时透传到控制台，但无法感知脚本执行结果，
     * 是否成功由调用方自行确认。
     * </p>
     *
     * @param scriptPath 脚本文件路径
     * @param args       传递给脚本的参数列表
     * @throws UpdateException 脚本不存在、不可执行或启动失败时抛出SCRIPT_ERROR
     */
    public void executeAsync(String scriptPath, String... args) throws UpdateException {
        Process process = startProcess(scriptPath, args);
        startOutputDrain(process);
        System.out.println(OUTPUT_PREFIX + "脚本已启动，不等待其结束: " + scriptPath);
    }

    /**
     * 校验脚本并启动进程
     * <p>
     * 根据脚本扩展名决定执行方式：
     * .sh/.bash 脚本显式用 bash 执行，避免 macOS 等平台直接执行时参数丢失；
     * .bat/.cmd 脚本用 cmd /c 执行，确保 Windows 上参数正确传递；
     * 其他脚本直接执行（依赖 shebang 或 OS 关联）。
     * </p>
     *
     * @param scriptPath 脚本文件路径
     * @param args       传递给脚本的参数列表
     * @return 已启动的脚本进程
     * @throws UpdateException 脚本不存在、不可执行或启动失败时抛出SCRIPT_ERROR
     */
    private Process startProcess(String scriptPath, String[] args) throws UpdateException {
        File script = new File(scriptPath);
        if (!script.exists() || !script.isFile()) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR, "脚本文件不存在: " + scriptPath);
        }
        if (!script.canExecute()) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR, "脚本文件不可执行: " + scriptPath);
        }
        List<String> command = new ArrayList<>();
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
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new UpdateException(UpdateException.ErrorType.SCRIPT_ERROR,
                    "脚本启动失败: " + scriptPath, e);
        }
    }

    /**
     * 启动守护线程在后台读取脚本输出
     * <p>
     * 后台读取避免同步读阻塞主流程：若脚本派生的常驻子进程继承并持有输出管道，
     * 管道永不关闭，同步读取将永久阻塞。
     * </p>
     *
     * @param process 脚本进程
     */
    private void startOutputDrain(Process process) {
        Charset outputCharset = Charset.defaultCharset();
        Thread outputThread = new Thread(
                () -> drainOutput(process.getInputStream(), outputCharset), "script-output-reader");
        outputThread.setDaemon(true);
        outputThread.start();
    }

    /**
     * 逐行读取脚本输出并透传到控制台
     * <p>
     * 持续读取直到进程关闭输出流，避免输出缓冲区写满导致脚本阻塞。
     * 使用系统默认编码读取：Windows 上 cmd.exe 管道输出为 GBK，
     * Linux/macOS 上 bash 输出为 UTF-8，与各自系统默认编码一致。
     * 读取异常仅记录日志，不影响主流程的超时判定。
     * </p>
     *
     * @param inputStream 脚本进程输出流
     * @param charset     输出解码字符集
     */
    private void drainOutput(InputStream inputStream, Charset charset) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(OUTPUT_PREFIX + line);
            }
        } catch (IOException e) {
            System.err.println(OUTPUT_PREFIX + "读取脚本输出异常: " + e.getMessage());
        }
    }
}
