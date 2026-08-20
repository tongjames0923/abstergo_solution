package com.tbs.updater.demo;

import com.tbs.updater.api.AutoUpdater;
import com.tbs.updater.api.UpdateListener;
import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.model.UpdateInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 基于 Swing 的应用自动更新器 GUI
 * <p>
 * 功能：
 * 1. 版本检查与对比展示
 * 2. 下载进度与速度实时显示
 * 3. 部署/启动脚本执行状态
 * 4. 运行时配置编辑
 * 5. 下载完成后自动打开文件（可选）
 * 6. 带时间戳的操作日志
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdaterGuiDemo extends JFrame {

    /**
     * 配置文件路径系统属性键名
     */
    private static final String CONFIG_FILE_PROPERTY = "updater.config.file";

    /**
     * classpath 内默认配置文件名
     */
    private static final String CLASSPATH_CONFIG = "updater.properties";

    /**
     * 时间戳格式
     */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * 字体
     */
    private static final Font UI_FONT = new Font("Microsoft YaHei", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);

    /**
     * 状态颜色
     */
    private static final Color COLOR_IDLE = new Color(100, 100, 100);
    private static final Color COLOR_WORKING = new Color(33, 150, 243);
    private static final Color COLOR_SUCCESS = new Color(76, 175, 80);
    private static final Color COLOR_ERROR = new Color(244, 67, 54);

    /**
     * 状态标签
     */
    private final JLabel statusLabel;

    /**
     * 版本信息标签
     */
    private final JLabel versionLabel;

    /**
     * 进度条
     */
    private final JProgressBar progressBar;

    /**
     * 下载速度标签
     */
    private final JLabel speedLabel;

    /**
     * 日志文本区
     */
    private final JTextArea logArea;

    /**
     * 开始更新按钮
     */
    private final JButton startButton;

    /**
     * 配置按钮
     */
    private final JButton configButton;

    /**
     * 下载完成后自动打开文件复选框
     */
    private final JCheckBox autoOpenCheckbox;

    /**
     * 更新器实例
     */
    private AutoUpdater updater;

    /**
     * 当前加载的配置
     */
    private UpdaterConfig currentConfig;

    /**
     * 下载开始时间（用于计算速度）
     */
    private volatile long downloadStartTime;

    /**
     * 下载完成后的文件列表
     */
    private volatile List<File> downloadedFiles = new ArrayList<>();

    /**
     * 是否正在执行更新
     */
    private volatile boolean updating = false;

    /**
     * 构造更新器 GUI
     */
    public UpdaterGuiDemo() {
        super("应用自动更新器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 520);
        setMinimumSize(new Dimension(480, 420));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        /*
         * 顶部：标题与版本信息
         */
        JPanel headerPanel = new JPanel(new BorderLayout(5, 3));
        JLabel titleLabel = new JLabel("应用更新器");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        versionLabel = new JLabel("当前版本: -  |  最新版本: 未检查");
        versionLabel.setFont(SMALL_FONT);
        versionLabel.setForeground(COLOR_IDLE);
        headerPanel.add(versionLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        /*
         * 中部上：状态与进度区域
         */
        JPanel statusPanel = new JPanel(new BorderLayout(5, 8));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        statusLabel = new JLabel("就绪，点击「检查更新」开始");
        statusLabel.setFont(UI_FONT);
        statusLabel.setForeground(COLOR_IDLE);
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel progressPanel = new JPanel(new BorderLayout(8, 0));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("等待中");
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressPanel.add(progressBar, BorderLayout.CENTER);

        speedLabel = new JLabel("");
        speedLabel.setFont(SMALL_FONT);
        speedLabel.setForeground(COLOR_IDLE);
        speedLabel.setPreferredSize(new Dimension(120, 22));
        progressPanel.add(speedLabel, BorderLayout.EAST);

        statusPanel.add(progressPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.PAGE_START);

        /*
         * 中部：日志区域
         */
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("操作日志"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.CENTER);

        /*
         * 底部：控制区域
         */
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        autoOpenCheckbox = new JCheckBox("下载完成后自动打开文件");
        autoOpenCheckbox.setFont(SMALL_FONT);
        optionsPanel.add(autoOpenCheckbox);
        controlPanel.add(optionsPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        configButton = new JButton("配置");
        configButton.setFont(UI_FONT);
        configButton.setPreferredSize(new Dimension(80, 32));
        configButton.addActionListener(e -> showConfigDialog());
        buttonPanel.add(configButton);

        startButton = new JButton("检查更新");
        startButton.setFont(UI_FONT);
        startButton.setPreferredSize(new Dimension(120, 32));
        startButton.addActionListener(e -> startUpdate());
        buttonPanel.add(startButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.PAGE_END);

        setContentPane(mainPanel);

        /*
         * 窗口关闭时中断更新线程
         */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                updating = false;
            }
        });

        initUpdater();
    }

    /**
     * 初始化更新器配置
     */
    private void initUpdater() {
        try {
            currentConfig = loadConfig();
            /*
             * 检查本地已安装版本文件，若存在则覆盖 currentVersionCode
             */
            int localVersion = UpdaterConfig.readLocalVersion(currentConfig.getDownloadDir());
            if (localVersion > currentConfig.getCurrentVersionCode()) {
                appendLog("[初始化] 检测到本地版本记录: " + localVersion + "，覆盖配置文件中的版本号");
                currentConfig = rebuildWithVersion(currentConfig, localVersion);
            }
            updater = new AutoUpdater(currentConfig, new GuiUpdateListener());
            appendLog("[初始化] 配置加载完成");
            appendLog("[初始化] 服务端: " + currentConfig.buildCheckUrl());
            appendLog("[初始化] appKey: " + currentConfig.getAppKey());
            appendLog("[初始化] platform: " + currentConfig.getPlatform());
            appendLog("[初始化] 当前版本: " + currentConfig.getCurrentVersionCode());
            appendLog("[初始化] 部署脚本: " + (currentConfig.getDeployScript() != null ? currentConfig.getDeployScript() : "未配置（跳过）"));
            appendLog("[初始化] 启动脚本: " + (currentConfig.getStartScript() != null ? currentConfig.getStartScript() : "未配置（跳过）"));
            versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode() + "  |  最新版本: 未检查");
        } catch (Exception e) {
            appendLog("[初始化] 失败: " + e.getMessage());
            setStatus("配置加载失败: " + e.getMessage(), COLOR_ERROR);
            startButton.setEnabled(false);
        }
    }

    /**
     * 基于已有配置重建并指定新版本号
     *
     * @param source  原始配置
     * @param version 新版本号
     * @return 新版本号替换后的配置实例
     */
    private static UpdaterConfig rebuildWithVersion(UpdaterConfig source, int version) {
        return new UpdaterConfig.Builder()
                .serverHost(source.getServerHost())
                .serverPort(source.getServerPort())
                .checkPath(source.getCheckPath())
                .appKey(source.getAppKey())
                .platform(source.getPlatform())
                .currentVersionCode(version)
                .downloadDir(source.getDownloadDir())
                .deployScript(source.getDeployScript())
                .startScript(source.getStartScript())
                .timeoutSeconds(source.getTimeoutSeconds())
                .tryAllUrls(source.isTryAllUrls())
                .build();
    }

    /**
     * 按优先级加载配置
     *
     * @return 更新器配置实例
     * @throws Exception 所有加载途径均失败时抛出
     */
    private UpdaterConfig loadConfig() throws Exception {
        /*
         * 优先级1：系统属性指定的外部配置文件
         */
        String externalPath = System.getProperty(CONFIG_FILE_PROPERTY);
        if (externalPath != null && !externalPath.trim().isEmpty()) {
            File externalFile = new File(externalPath.trim());
            if (externalFile.exists() && externalFile.isFile()) {
                appendLog("[配置] 从外部文件加载: " + externalFile.getAbsolutePath());
                return UpdaterConfig.fromFile(externalFile.getAbsolutePath());
            }
            appendLog("[配置] 外部文件不存在: " + externalPath);
        }

        /*
         * 优先级2：classpath 下的 updater.properties
         */
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CLASSPATH_CONFIG)) {
            if (in != null) {
                Properties props = new Properties();
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                appendLog("[配置] 从 classpath 加载: " + CLASSPATH_CONFIG);
                return UpdaterConfig.fromProperties(props);
            }
        } catch (IOException e) {
            appendLog("[配置] classpath 加载失败: " + e.getMessage());
        }

        /*
         * 优先级3：硬编码默认值
         */
        appendLog("[配置] 使用内置默认值");
        return new UpdaterConfig.Builder()
                .serverHost("192.168.80.32")
                .serverPort(8082)
                .appKey("兴昊生产扫码入库App")
                .platform("android")
                .currentVersionCode(0)
                .build();
    }

    /**
     * 开始更新流程
     */
    private void startUpdate() {
        if (updating) {
            return;
        }
        updating = true;
        downloadedFiles = new ArrayList<>();
        startButton.setEnabled(false);
        startButton.setText("更新中...");
        configButton.setEnabled(false);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString("0%");
        speedLabel.setText("");
        setStatus("正在检查更新...", COLOR_WORKING);

        new Thread(() -> {
            try {
                updater.autoUpdate();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("更新失败: " + e.getMessage(), COLOR_ERROR);
                    appendLog("[异常] " + e.getMessage());
                    resetButtons();
                });
            } finally {
                updating = false;
            }
        }, "updater-thread").start();
    }

    /**
     * 显示配置编辑对话框
     */
    private void showConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField hostField = new JTextField(currentConfig != null ? currentConfig.getServerHost() : "", 20);
        JTextField portField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getServerPort()) : "8082", 6);
        JTextField appKeyField = new JTextField(currentConfig != null ? currentConfig.getAppKey() : "", 20);
        JTextField platformField = new JTextField(currentConfig != null ? currentConfig.getPlatform() : "android", 10);
        JTextField versionField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getCurrentVersionCode()) : "0", 6);
        JTextField downloadDirField = new JTextField(currentConfig != null ? currentConfig.getDownloadDir() : "", 20);
        JTextField deployScriptField = new JTextField(currentConfig != null ? currentConfig.getDeployScript() : "", 20);
        JTextField startScriptField = new JTextField(currentConfig != null ? currentConfig.getStartScript() : "", 20);
        JTextField timeoutField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getTimeoutSeconds()) : "30", 6);
        JCheckBox tryAllUrlsBox = new JCheckBox("", currentConfig == null || currentConfig.isTryAllUrls());

        panel.add(new JLabel("服务端主机:"));
        panel.add(hostField);
        panel.add(new JLabel("服务端端口:"));
        panel.add(portField);
        panel.add(new JLabel("应用标识:"));
        panel.add(appKeyField);
        panel.add(new JLabel("平台:"));
        panel.add(platformField);
        panel.add(new JLabel("当前版本号:"));
        panel.add(versionField);
        panel.add(new JLabel("下载目录:"));
        panel.add(downloadDirField);
        panel.add(new JLabel("部署脚本路径:"));
        panel.add(deployScriptField);
        panel.add(new JLabel("启动脚本路径:"));
        panel.add(startScriptField);
        panel.add(new JLabel("超时时间(秒):"));
        panel.add(timeoutField);
        panel.add(new JLabel("失败尝试其他地址:"));
        panel.add(tryAllUrlsBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "更新器配置",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String downloadDirText = downloadDirField.getText().trim();
                currentConfig = new UpdaterConfig.Builder()
                        .serverHost(hostField.getText().trim())
                        .serverPort(Integer.parseInt(portField.getText().trim()))
                        .appKey(appKeyField.getText().trim())
                        .platform(platformField.getText().trim())
                        .currentVersionCode(Integer.parseInt(versionField.getText().trim()))
                        .downloadDir(downloadDirText.isEmpty() ? null : downloadDirText)
                        .deployScript(deployScriptField.getText().trim().isEmpty() ? null : deployScriptField.getText().trim())
                        .startScript(startScriptField.getText().trim().isEmpty() ? null : startScriptField.getText().trim())
                        .timeoutSeconds(Integer.parseInt(timeoutField.getText().trim()))
                        .tryAllUrls(tryAllUrlsBox.isSelected())
                        .build();
                updater = new AutoUpdater(currentConfig, new GuiUpdateListener());
                appendLog("[配置] 配置已更新，服务端: " + currentConfig.buildCheckUrl());
                if (currentConfig.getDeployScript() != null) {
                    appendLog("[配置] 部署脚本: " + currentConfig.getDeployScript());
                } else {
                    appendLog("[配置] 部署脚本: 未配置（跳过）");
                }
                if (currentConfig.getStartScript() != null) {
                    appendLog("[配置] 启动脚本: " + currentConfig.getStartScript());
                } else {
                    appendLog("[配置] 启动脚本: 未配置（跳过）");
                }
                versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode() + "  |  最新版本: 未检查");
                setStatus("配置已更新", COLOR_IDLE);
                resetButtons();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "配置无效: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 恢复按钮状态
     */
    private void resetButtons() {
        startButton.setEnabled(true);
        startButton.setText("检查更新");
        configButton.setEnabled(true);
    }

    /**
     * 设置状态标签文本与颜色
     *
     * @param text  状态文本
     * @param color 颜色
     */
    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    /**
     * 追加带时间戳的日志
     *
     * @param log 日志内容
     */
    private void appendLog(String log) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = TIME_FORMAT.format(new Date());
            logArea.append("[" + timestamp + "] " + log + "\n");
        });
    }

    /**
     * 尝试用系统默认程序打开文件
     *
     * @param file 要打开的文件
     */
    private void openFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            Desktop.getDesktop().open(file);
            appendLog("[打开] 已打开文件: " + file.getName());
        } catch (IOException e) {
            appendLog("[打开] 无法打开文件: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            appendLog("[打开] 当前系统不支持自动打开文件");
        }
    }

    /**
     * GUI 更新监听器实现
     */
    private class GuiUpdateListener implements UpdateListener {

        @Override
        public void onCheckStart() {
            SwingUtilities.invokeLater(() -> {
                setStatus("正在检查更新...", COLOR_WORKING);
                appendLog("[检查] 开始检查更新");
            });
        }

        @Override
        public void onCheckComplete(UpdateInfo info) {
            SwingUtilities.invokeLater(() -> {
                String latestVer = info.getLatestVersionName() != null ? info.getLatestVersionName() : info.getLatestVersionCode();
                versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode()
                        + "  |  最新版本: " + latestVer);
                if (info.isNeedUpdate()) {
                    setStatus("发现新版本: " + latestVer, COLOR_WORKING);
                    appendLog("[检查] 发现新版本: " + latestVer + " (versionCode=" + info.getLatestVersionCode() + ")");
                    if (info.getReleaseNote() != null && !info.getReleaseNote().isEmpty()) {
                        appendLog("[日志] " + info.getReleaseNote());
                    }
                    if (info.getAction() != null) {
                        appendLog("[动作] " + info.getAction());
                    }
                    int fileCount = info.getFiles().size();
                    appendLog("[检查] 待下载文件: " + fileCount + "个");
                    if (info.getDownloadUrls().isEmpty()) {
                        appendLog("[警告] 服务端未返回下载地址");
                    }
                } else {
                    setStatus("当前已是最新版本", COLOR_SUCCESS);
                    appendLog("[检查] 已是最新版本");
                }
            });
        }

        @Override
        public void onDownloadFileStart(int fileIndex, int totalFiles, String url) {
            SwingUtilities.invokeLater(() -> {
                String fileLabel = "(" + (fileIndex + 1) + "/" + totalFiles + ")";
                setStatus("正在下载文件 " + fileLabel + "...", COLOR_WORKING);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                progressBar.setString("0%");
                speedLabel.setText("");
                downloadStartTime = System.currentTimeMillis();
                appendLog("[下载] 文件 " + fileLabel);
            });
        }

        @Override
        public void onDownloadStart(String url) {
            SwingUtilities.invokeLater(() -> {
                String shortUrl = url.length() > 70 ? url.substring(0, 70) + "..." : url;
                appendLog("[下载] 地址: " + shortUrl);
            });
        }

        @Override
        public void onDownloadProgress(int progress) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");
                /*
                 * 估算下载速度
                 */
                long elapsed = System.currentTimeMillis() - downloadStartTime;
                if (elapsed > 1000 && progress > 0) {
                    appendLog("[下载] 进度: " + progress + "%");
                }
            });
        }

        @Override
        public void onDownloadComplete(File file) {
            SwingUtilities.invokeLater(() -> {
                long elapsed = System.currentTimeMillis() - downloadStartTime;
                String sizeInfo = formatFileSize(file.length());
                String speedInfo = elapsed > 0 ? formatSpeed(file.length(), elapsed) : "";
                appendLog("[下载] 完成: " + file.getName() + " (" + sizeInfo + ", " + speedInfo + ")");
            });
        }

        @Override
        public void onAllDownloadsComplete(List<File> files) {
            SwingUtilities.invokeLater(() -> {
                downloadedFiles = new ArrayList<>(files);
                long totalSize = files.stream().mapToLong(File::length).sum();
                String sizeInfo = formatFileSize(totalSize);
                setStatus("全部下载完成: " + files.size() + "个文件 (" + sizeInfo + ")", COLOR_SUCCESS);
                progressBar.setValue(100);
                progressBar.setString("100%");
                appendLog("[下载] 全部完成，共 " + files.size() + " 个文件，总大小: " + sizeInfo);
                for (int i = 0; i < files.size(); i++) {
                    appendLog("[下载]   [" + (i + 1) + "] " + files.get(i).getAbsolutePath());
                }
                if (autoOpenCheckbox.isSelected() && !files.isEmpty()) {
                    openFile(files.get(0));
                }
            });
        }

        @Override
        public void onDeployStart() {
            SwingUtilities.invokeLater(() -> {
                setStatus("正在执行部署脚本...", COLOR_WORKING);
                progressBar.setIndeterminate(true);
                progressBar.setString("执行中");
                appendLog("[部署] 开始执行部署脚本");
                if (downloadedFiles != null && !downloadedFiles.isEmpty()) {
                    appendLog("[部署] 传入文件: " + downloadedFiles.size() + "个");
                    for (int i = 0; i < downloadedFiles.size(); i++) {
                        appendLog("[部署]   $" + (i + 1) + " = " + downloadedFiles.get(i).getAbsolutePath());
                    }
                } else {
                    appendLog("[部署] 警告: downloadedFiles 为空");
                }
            });
        }

        @Override
        public void onDeployComplete() {
            SwingUtilities.invokeLater(() -> appendLog("[部署] 完成"));
        }

        @Override
        public void onStartScriptStart() {
            SwingUtilities.invokeLater(() -> {
                setStatus("正在启动应用...", COLOR_WORKING);
                appendLog("[启动] 开始执行启动脚本");
            });
        }

        @Override
        public void onStartScriptComplete() {
            SwingUtilities.invokeLater(() -> appendLog("[启动] 完成"));
        }

        @Override
        public void onSuccess() {
            SwingUtilities.invokeLater(() -> {
                setStatus("更新流程完成", COLOR_SUCCESS);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                progressBar.setString("完成");
                appendLog("[成功] 更新流程完成");
                /*
                 * 更新成功后从本地版本文件同步版本号并刷新显示
                 */
                int localVersion = UpdaterConfig.readLocalVersion(currentConfig.getDownloadDir());
                if (localVersion > currentConfig.getCurrentVersionCode()) {
                    try {
                        currentConfig = rebuildWithVersion(currentConfig, localVersion);
                        updater = new AutoUpdater(currentConfig, new GuiUpdateListener());
                        appendLog("[成功] 本地版本已同步: " + localVersion);
                    } catch (UpdateException e) {
                        appendLog("[成功] 版本同步失败: " + e.getMessage());
                    }
                }
                versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode()
                        + "  |  最新版本: 已同步");
                resetButtons();
            });
        }

        @Override
        public void onError(String errMsg) {
            SwingUtilities.invokeLater(() -> {
                setStatus("更新失败", COLOR_ERROR);
                progressBar.setIndeterminate(false);
                progressBar.setString("失败");
                appendLog("[错误] " + errMsg);
                resetButtons();
                JOptionPane.showMessageDialog(UpdaterGuiDemo.this,
                        "更新失败:\n" + errMsg,
                        "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    /**
     * 格式化文件大小
     *
     * @param bytes 字节数
     * @return 可读的文件大小字符串
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化下载速度
     *
     * @param bytes      下载字节数
     * @param elapsedMs  耗时毫秒数
     * @return 可读的速度字符串
     */
    private static String formatSpeed(long bytes, long elapsedMs) {
        double seconds = elapsedMs / 1000.0;
        if (seconds <= 0) {
            return "";
        }
        double bytesPerSecond = bytes / seconds;
        if (bytesPerSecond < 1024) {
            return String.format("%.0f B/s", bytesPerSecond);
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.1f KB/s", bytesPerSecond / 1024);
        } else {
            return String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024));
        }
    }

    /**
     * 启动 GUI
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            /*
             * 使用默认外观
             */
        }

        SwingUtilities.invokeLater(() -> {
            UpdaterGuiDemo demo = new UpdaterGuiDemo();
            demo.setVisible(true);
        });
    }
}
