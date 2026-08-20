package com.tbs.updater.demo;

import com.tbs.updater.api.AutoUpdater;
import com.tbs.updater.api.UpdateListener;
import com.tbs.updater.config.UpdaterConfig;
import com.tbs.updater.exception.UpdateException;
import com.tbs.updater.model.UpdateInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 基于 Swing 的应用自动更新器 GUI
 * <p>
 * 功能：
 * 1. 启动时按 appKey 选择/新建应用配置（updater-{appKey}.properties）
 * 2. 版本检查与对比展示
 * 3. 下载进度与速度实时显示
 * 4. 部署/启动脚本执行状态
 * 5. 运行时配置编辑（版本号独立回写各自配置文件）
 * 6. 下载完成后自动打开文件（可选）
 * 7. 带时间戳的操作日志
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class UpdaterGuiDemo extends JFrame {

    /**
     * 单实例锁文件名（位于 JAR 包所在目录）
     */
    private static final String INSTANCE_LOCK_FILE = ".updater.lock";

    /**
     * 配置文件命名前缀与后缀（形如 updater-{appKey}.properties）
     */
    private static final String PROFILE_FILE_PREFIX = "updater-";
    private static final String PROFILE_FILE_SUFFIX = ".properties";

    /**
     * 单实例锁通道（静态引用防止被 GC 释放导致锁失效）
     */
    private static FileChannel instanceLockChannel;

    /**
     * 单实例文件锁
     */
    private static FileLock instanceLock;

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
     * 切换应用按钮（运行时切换到其他 appKey 配置）
     */
    private final JButton switchAppButton;

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
     * 当前配置对应的持久化文件（updater-{appKey}.properties）
     */
    private File currentConfigFile;

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
     * 检查到待安装更新时暂存的更新信息，用户确认后用于安装
     */
    private volatile UpdateInfo pendingInfo;

    /**
     * 速度估算：约 1 秒前的采样点（时间戳，已下载字节数）
     */
    private final long[] speedSnapshot = new long[2];

    /**
     * 日志里程碑：上次输出进度日志的百分比档位（25/50/75）
     */
    private volatile int lastLoggedMilestone = 0;

    /**
     * 本次流程是否已进入安装阶段（用于区分“已是最新”与“安装完成”两种成功）
     */
    private volatile boolean installCompleted = false;

    /**
     * 当前待安装更新是否为服务端强制更新（强制更新不可跳过、不可退出）
     */
    private volatile boolean forceUpdate = false;

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

        JPanel optionsPanel = new JPanel(new BorderLayout());
        autoOpenCheckbox = new JCheckBox("下载完成后自动打开文件");
        autoOpenCheckbox.setFont(SMALL_FONT);
        optionsPanel.add(autoOpenCheckbox, BorderLayout.WEST);

        JButton clearLogButton = new JButton("清空日志");
        clearLogButton.setFont(SMALL_FONT);
        clearLogButton.addActionListener(e -> logArea.setText(""));
        optionsPanel.add(clearLogButton, BorderLayout.EAST);
        controlPanel.add(optionsPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        switchAppButton = new JButton("切换应用");
        switchAppButton.setFont(UI_FONT);
        switchAppButton.setPreferredSize(new Dimension(90, 32));
        switchAppButton.addActionListener(e -> switchApp());
        buttonPanel.add(switchAppButton);

        configButton = new JButton("配置");
        configButton.setFont(UI_FONT);
        configButton.setPreferredSize(new Dimension(80, 32));
        configButton.addActionListener(e -> showConfigDialog());
        buttonPanel.add(configButton);

        startButton = new JButton("检查更新");
        startButton.setFont(UI_FONT);
        startButton.setPreferredSize(new Dimension(120, 32));
        startButton.addActionListener(e -> onMainButtonClick());
        buttonPanel.add(startButton);

        controlPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.PAGE_END);

        setContentPane(mainPanel);

        /*
         * 窗口关闭时若正在更新需用户确认，避免误关中断部署
         */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (forceUpdate) {
                    JOptionPane.showMessageDialog(UpdaterGuiDemo.this,
                        "本次更新由服务端强制要求，无法退出，请先完成安装。", "无法退出", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (updating && !confirmCloseWhileUpdating()) {
                    return;
                }
                updating = false;
                dispose();
                System.exit(0);
            }
        });

        initUpdater();
    }

    /**
     * 初始化更新器配置
     * <p>
     * 启动时必须确定一份应用配置文件（按 appKey 命名），
     * 用户未选择或取消时退出程序。
     * </p>
     */
    private void initUpdater() {
        try {
            File configFile = showAppSelectorDialog(false);
            if (configFile == null) {
                /*
                 * 用户未选择任何配置（退出或关闭选择对话框），程序无法工作，直接退出
                 */
                dispose();
                System.exit(0);
                return;
            }
            applyConfigFile(configFile);
        } catch (Exception e) {
            appendLog("[初始化] 失败: " + e.getMessage());
            setStatus("配置加载失败: " + e.getMessage(), COLOR_ERROR);
            startButton.setEnabled(false);
        }
    }

    /**
     * 运行时切换到其他应用配置
     * <p>
     * 复用启动时的选择对话框，用户取消时保持当前配置不变；
     * 更新进行中禁止切换，避免中断下载或部署。
     * </p>
     */
    private void switchApp() {
        if (updating) {
            return;
        }
        File configFile = showAppSelectorDialog(true);
        if (configFile == null) {
            return;
        }
        if (configFile.equals(currentConfigFile)) {
            appendLog("[切换] 所选应用与当前一致，未执行切换");
            return;
        }
        try {
            applyConfigFile(configFile);
        } catch (Exception e) {
            appendLog("[切换] 失败: " + e.getMessage());
            setStatus("切换失败: " + e.getMessage(), COLOR_ERROR);
            JOptionPane.showMessageDialog(this, "配置加载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载指定配置文件并重建更新器
     * <p>
     * 启动与运行时切换共用：重置待安装暂存与强制更新状态，刷新界面展示。
     * </p>
     *
     * @param configFile 待加载的配置文件
     * @throws Exception 配置文件解析失败时抛出
     */
    private void applyConfigFile(File configFile) throws Exception {
        currentConfigFile = configFile;
        currentConfig = UpdaterConfig.fromFile(configFile.getAbsolutePath());
        updater = new AutoUpdater(currentConfig, new GuiUpdateListener());
        pendingInfo = null;
        forceUpdate = false;
        resetButtons();
        appendLog("[初始化] 配置加载完成: " + configFile.getName());
        appendLog("[初始化] 服务端: " + currentConfig.buildCheckUrl());
        appendLog("[初始化] appKey: " + currentConfig.getAppKey());
        appendLog("[初始化] platform: " + currentConfig.getPlatform());
        appendLog("[初始化] 当前版本: " + currentConfig.getCurrentVersionCode());
        appendLog("[初始化] 部署脚本: " + (currentConfig.getDeployScript() != null ? currentConfig.getDeployScript() : "未配置（跳过）"));
        appendLog("[初始化] 启动脚本: " + (currentConfig.getStartScript() != null ? currentConfig.getStartScript() : "未配置（跳过）"));
        versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode() + "  |  最新版本: 未检查");
        setStatus("当前应用: " + currentConfig.getAppKey(), COLOR_SUCCESS);
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
     * 应用配置选择对话框
     * <p>
     * 列出 JAR 目录下所有 updater-{appKey}.properties 配置文件，
     * 用户选择其一，或新建一份配置。启动时（cancelable=false）取消返回 null
     * 由调用方退出程序；运行时切换（cancelable=true）取消返回 null 保持当前配置。
     * </p>
     *
     * @param cancelable 是否可取消（运行时切换为true，首次选择为false）
     * @return 选定或新建的配置文件，用户取消时返回null
     */
    private File showAppSelectorDialog(boolean cancelable) {
        while (true) {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (String appKey : listAppConfigKeys()) {
                model.addElement(appKey);
            }
            JList<String> appList = new JList<>(model);
            appList.setFont(UI_FONT);
            appList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            if (!model.isEmpty()) {
                appList.setSelectedIndex(0);
            }
            JScrollPane listScroll = new JScrollPane(appList);
            listScroll.setPreferredSize(new Dimension(360, 180));

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            JLabel hint = new JLabel(
                "<html>请选择要使用的应用配置（按 appKey 区分），<br>每个应用独立保存配置与版本号。</html>");
            hint.setFont(UI_FONT);
            panel.add(hint, BorderLayout.NORTH);
            panel.add(listScroll, BorderLayout.CENTER);

            Object[] options = cancelable
                ? new Object[] {"打开", "新建", "删除", "取消"}
                : new Object[] {"打开", "新建", "删除", "退出"};
            int choice = JOptionPane.showOptionDialog(this, panel,
                cancelable ? "切换应用" : "选择应用", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == 3 || choice == JOptionPane.CLOSED_OPTION) {
                return null;
            }
            String selected = appList.getSelectedValue();
            if (choice == 0) {
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "请先选择要打开的应用配置，或点击「新建」创建", "提示",
                        JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                File configFile = resolveAppConfigFile(selected);
                if (!configFile.exists()) {
                    JOptionPane.showMessageDialog(this, "配置文件不存在: " + configFile.getName(), "错误",
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return configFile;
            }
            if (choice == 1) {
                File created = createNewAppConfig();
                if (created != null) {
                    return created;
                }
                continue;
            }
            if (choice == 2) {
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "请先选择要删除的应用配置", "提示", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                deleteAppConfig(selected);
            }
        }
    }

    /**
     * 新建应用配置向导
     * <p>
     * 引导用户填写服务端地址与应用标识，支持先「测试连接」验证再保存；
     * 配置以 updater-{appKey}.properties 落盘。取消时返回 null，回到选择对话框。
     * </p>
     *
     * @return 新建并保存成功的配置文件，取消时返回null
     */
    private File createNewAppConfig() {
        appendLog("[初始化] 新建应用配置");

        JTextField hostField = new JTextField("192.168.80.32", 20);
        JTextField portField = new JTextField("8082", 6);
        JTextField appKeyField = new JTextField("", 20);
        JTextField platformField = new JTextField("android", 8);
        JTextField versionField = new JTextField("0", 6);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        form.setBorder(BorderFactory.createTitledBorder("服务端与应用信息"));
        form.add(new JLabel("服务端主机:"));
        form.add(hostField);
        form.add(new JLabel("端口:"));
        form.add(portField);
        form.add(new JLabel("应用标识(appKey):"));
        form.add(appKeyField);
        form.add(new JLabel("平台:"));
        form.add(platformField);
        form.add(new JLabel("当前版本号:"));
        form.add(versionField);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel hint = new JLabel(
            "<html>appKey 决定配置文件名（updater-{appKey}.properties），<br>同一 appKey 已存在时将覆盖。建议先「测试连接」验证。</html>");
        hint.setFont(UI_FONT);
        panel.add(hint, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);

        Object[] options = {"测试连接", "确定", "取消"};
        while (true) {
            int choice = JOptionPane.showOptionDialog(this, panel, "新建应用配置", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                return null;
            }
            UpdaterConfig config;
            try {
                config = new UpdaterConfig.Builder()
                    .serverHost(hostField.getText().trim())
                    .serverPort(Integer.parseInt(portField.getText().trim()))
                    .appKey(appKeyField.getText().trim())
                    .platform(platformField.getText().trim())
                    .currentVersionCode(Integer.parseInt(versionField.getText().trim()))
                    .build();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "配置无效，请检查输入: " + e.getMessage(), "错误",
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            String appKey = config.getAppKey();
            if (appKey == null || appKey.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "appKey 不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (appKey.contains("/") || appKey.contains("\\")) {
                JOptionPane.showMessageDialog(this, "appKey 不能包含路径分隔符", "错误", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (choice == 0) {
                testServerConnection(config);
                continue;
            }
            File configFile = resolveAppConfigFile(appKey);
            if (configFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                    "应用「" + appKey + "」的配置已存在，是否覆盖？", "确认覆盖",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.OK_OPTION) {
                    continue;
                }
            }
            writeConfigToFile(config, configFile);
            appendLog("[初始化] 新配置已保存: " + configFile.getName());
            return configFile;
        }
    }

    /**
     * 测试服务端连接
     * <p>
     * 复用类库的检查接口完整验证连通性与接口契约，结果以对话框反馈。
     * </p>
     *
     * @param config 待验证的配置
     */
    private void testServerConnection(UpdaterConfig config) {
        new Thread(() -> {
            boolean ok;
            String message;
            try {
                UpdateInfo info = new AutoUpdater(config, new UpdateListener() {
                }).checkForUpdate();
                ok = true;
                message = "连接成功，服务端响应正常（最新版本: " + info.getLatestVersionCode() + "）";
            } catch (Exception e) {
                ok = false;
                message = "连接失败: " + e.getMessage();
            }
            final boolean success = ok;
            final String text = message;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(UpdaterGuiDemo.this, text, "测试连接",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
        }, "updater-conn-test-thread").start();
    }

    /**
     * 主按钮点击入口
     * <p>
     * 已检查到待安装更新时直接进入安装，否则发起检查。
     * </p>
     */
    private void onMainButtonClick() {
        if (pendingInfo != null) {
            showUpdateConfirmDialog(pendingInfo);
        } else {
            startCheck();
        }
    }

    /**
     * 检查阶段：仅向服务端查询最新版本，不自动下载安装
     */
    private void startCheck() {
        if (updating) {
            return;
        }
        updating = true;
        installCompleted = false;
        pendingInfo = null;
        downloadedFiles = new ArrayList<>();
        startButton.setEnabled(false);
        startButton.setText("检查中...");
        configButton.setEnabled(false);
        switchAppButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("检查中");
        speedLabel.setText("");
        setStatus("正在检查更新...", COLOR_WORKING);

        new Thread(() -> {
            try {
                updater.checkForUpdate();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("检查失败: " + e.getMessage(), COLOR_ERROR);
                    appendLog("[异常] " + e.getMessage());
                    resetButtons();
                });
            } finally {
                updating = false;
            }
        }, "updater-check-thread").start();
    }

    /**
     * 安装阶段：用户在确认对话框同意后执行下载、部署与启动
     *
     * @param info 检查阶段获得的更新信息
     */
    private void startInstall(UpdateInfo info) {
        if (updating) {
            return;
        }
        updating = true;
        installCompleted = true;
        downloadedFiles = new ArrayList<>();
        startButton.setEnabled(false);
        startButton.setText("更新中...");
        configButton.setEnabled(false);
        switchAppButton.setEnabled(false);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString("0%");
        speedLabel.setText("");
        setStatus("正在下载更新...", COLOR_WORKING);

        new Thread(() -> {
            try {
                updater.installUpdate(info);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("更新失败: " + e.getMessage(), COLOR_ERROR);
                    appendLog("[异常] " + e.getMessage());
                    resetButtons();
                });
            } finally {
                updating = false;
            }
        }, "updater-install-thread").start();
    }

    /**
     * 弹出更新确认对话框（Sparkle风格）
     * <p>
     * 展示新版本号、待下载文件数与发行说明，由用户决定是否立即安装。
     * </p>
     *
     * @param info 检查阶段获得的更新信息
     */
    private void showUpdateConfirmDialog(UpdateInfo info) {
        String latestVer = info.getLatestVersionName() != null
                ? info.getLatestVersionName() : info.getLatestVersionCode();
        boolean force = isForceUpdate(info);

        while (true) {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel summary = new JLabel("<html>发现新版本 <b>" +
                latestVer +
                "</b>（当前 " +
                currentConfig.getCurrentVersionCode() +
                "），共 " +
                info.getFiles().size() +
                " 个文件待下载</html>");
            summary.setFont(UI_FONT);
            panel.add(summary, BorderLayout.NORTH);

            if (info.getReleaseNote() != null && !info.getReleaseNote().trim().isEmpty()) {
                JTextArea noteArea = new JTextArea(info.getReleaseNote());
                noteArea.setEditable(false);
                noteArea.setLineWrap(true);
                noteArea.setWrapStyleWord(true);
                noteArea.setFont(UI_FONT);
                JScrollPane noteScroll = new JScrollPane(noteArea);
                noteScroll.setPreferredSize(new Dimension(400, 150));
                noteScroll.setBorder(BorderFactory.createTitledBorder("发行说明"));
                panel.add(noteScroll, BorderLayout.CENTER);
            }

            if (isForceUpdate(info)) {
                JLabel forceLabel = new JLabel("本次更新由服务端强制要求，无法跳过");
                forceLabel.setFont(SMALL_FONT);
                forceLabel.setForeground(COLOR_ERROR);
                panel.add(forceLabel, BorderLayout.SOUTH);
            } else {
                String actionText = formatAction(info.getAction());
                if (actionText != null) {
                    JLabel actionLabel = new JLabel("服务端动作: " + actionText);
                    actionLabel.setFont(SMALL_FONT);
                    actionLabel.setForeground(COLOR_WORKING);
                    panel.add(actionLabel, BorderLayout.SOUTH);
                }
            }

            Object[] options = force ? new Object[] {"立即安装"} : new Object[] {"立即安装", "稍后"};
            int choice = JOptionPane.showOptionDialog(this, panel, "发现新版本", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (choice == 0) {
                startInstall(info);
                return;
            }
            if (force) {
                /*
                 * 强制更新下关闭对话框或任何非安装操作都重新弹出确认，确保无法跳过
                 */
                appendLog("[检查] 强制更新不可跳过，重新弹出确认");
                continue;
            }
            appendLog("[检查] 用户选择稍后安装，可点击「立即更新」继续");
            startButton.setText("立即更新");
            startButton.setEnabled(true);
            configButton.setEnabled(true);
            return;
        }
    }

    /**
     * 判断更新信息是否为服务端强制更新
     * <p>
     * 服务端契约：action为AppVersionCompatibilityActionEnum的编码（FORCE=1/RECOMMEND=2/NOTIFY=3），
     * 未命中兼容策略时为null。此处同时兼容字符串标识形态，容错其他服务端实现。
     * </p>
     *
     * @param info 服务端返回的更新信息
     * @return true表示强制更新
     */
    private static boolean isForceUpdate(UpdateInfo info) {
        if (info == null || info.getAction() == null || info.getAction().trim().isEmpty()) {
            return false;
        }
        String action = info.getAction().trim();
        return "1".equals(action) || "force".equalsIgnoreCase(action);
    }

    /**
     * 将服务端动作编码转为用户可读文案
     * <p>
     * 对齐AppVersionCompatibilityActionEnum：1=强制更新，2=推荐更新，3=仅提示。
     * </p>
     *
     * @param action 服务端返回的动作字段原文
     * @return 可读文案，无法识别时返回原文，为空时返回null
     */
    private static String formatAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            return null;
        }
        switch (action.trim()) {
            case "1":
            case "force":
                return "强制更新";
            case "2":
            case "recommend":
                return "推荐更新";
            case "3":
            case "notify":
                return "仅提示";
            default:
                return action;
        }
    }

    /**
     * 更新进行中关闭窗口的确认提示
     *
     * @return true表示用户确认关闭，false表示取消
     */
    private boolean confirmCloseWhileUpdating() {
        int choice = JOptionPane.showConfirmDialog(this,
                "正在执行更新，关闭可能中断部署过程，确定关闭吗？",
                "确认关闭", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.OK_OPTION;
    }

    /**
     * 显示配置编辑对话框
     * <p>
     * 按服务端/应用/本地三组分区展示，路径类字段提供文件浏览器选择，避免手工输入出错。
     * </p>
     */
    private void showConfigDialog() {
        JTextField hostField = new JTextField(currentConfig != null ? currentConfig.getServerHost() : "", 18);
        JTextField portField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getServerPort()) : "8082", 6);
        JTextField appKeyField = new JTextField(currentConfig != null ? currentConfig.getAppKey() : "", 18);
        JTextField platformField = new JTextField(currentConfig != null ? currentConfig.getPlatform() : "android", 8);
        JTextField versionField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getCurrentVersionCode()) : "0", 6);
        JTextField downloadDirField = new JTextField(currentConfig != null ? currentConfig.getDownloadDir() : "", 18);
        JTextField deployScriptField = new JTextField(currentConfig != null ? currentConfig.getDeployScript() : "", 18);
        JTextField startScriptField = new JTextField(currentConfig != null ? currentConfig.getStartScript() : "", 18);
        JTextField timeoutField = new JTextField(currentConfig != null ? String.valueOf(currentConfig.getTimeoutSeconds()) : "30", 6);
        JCheckBox tryAllUrlsBox = new JCheckBox("", currentConfig == null || currentConfig.isTryAllUrls());

        JPanel serverGroup = new JPanel(new GridLayout(0, 2, 8, 6));
        serverGroup.setBorder(BorderFactory.createTitledBorder("服务端"));
        serverGroup.add(new JLabel("主机地址:"));
        serverGroup.add(hostField);
        serverGroup.add(new JLabel("端口:"));
        serverGroup.add(portField);

        JPanel appGroup = new JPanel(new GridLayout(0, 2, 8, 6));
        appGroup.setBorder(BorderFactory.createTitledBorder("应用"));
        appGroup.add(new JLabel("应用标识(appKey):"));
        appGroup.add(appKeyField);
        appGroup.add(new JLabel("平台:"));
        appGroup.add(platformField);
        appGroup.add(new JLabel("当前版本号:"));
        appGroup.add(versionField);

        JPanel localGroup = new JPanel(new GridBagLayout());
        localGroup.setBorder(BorderFactory.createTitledBorder("本地环境"));
        addPathRow(localGroup, 0, "下载目录:", downloadDirField,
                createBrowseButton(downloadDirField, true, null));
        addPathRow(localGroup, 1, "部署脚本:", deployScriptField,
                createBrowseButton(deployScriptField, false, "部署脚本"));
        addPathRow(localGroup, 2, "启动脚本:", startScriptField,
                createBrowseButton(startScriptField, false, "启动脚本"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 0, 3, 8);
        localGroup.add(new JLabel("超时时间(秒):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        localGroup.add(timeoutField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        localGroup.add(new JLabel("失败尝试其他地址:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        localGroup.add(tryAllUrlsBox, gbc);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(serverGroup);
        panel.add(Box.createVerticalStrut(8));
        panel.add(appGroup);
        panel.add(Box.createVerticalStrut(8));
        panel.add(localGroup);

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
                saveCurrentConfig();
                setStatus("配置已更新", COLOR_IDLE);
                resetButtons();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "配置无效: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 在GridBagLayout面板中添加一行路径字段（标签+输入框+浏览按钮）
     *
     * @param panel       目标面板
     * @param row         行号
     * @param label       字段标签文本
     * @param field       路径输入框
     * @param browseButton 浏览按钮
     */
    private void addPathRow(JPanel panel, int row, String label,
                            JTextField field, JButton browseButton) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(3, 0, 3, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(browseButton, gbc);
    }

    /**
     * 创建路径浏览按钮
     * <p>
     * directoryOnly为true时弹出目录选择器（用于下载目录），
     * 否则弹出文件选择器并过滤脚本后缀（sh/bat/cmd）。
     * </p>
     *
     * @param field         回填选择结果的路径输入框
     * @param directoryOnly 是否仅选择目录
     * @param description   文件过滤描述，仅文件模式使用
     * @return 浏览按钮
     */
    private JButton createBrowseButton(JTextField field, boolean directoryOnly, String description) {
        JButton button = new JButton("浏览...");
        button.setFont(SMALL_FONT);
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (directoryOnly) {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("选择下载目录");
            } else {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileNameExtensionFilter(
                        description + " (*.sh, *.bash, *.bat, *.cmd)", "sh", "bash", "bat", "cmd"));
                chooser.setDialogTitle("选择" + description);
            }
            String current = field.getText().trim();
            if (!current.isEmpty()) {
                File currentFile = new File(current);
                if (currentFile.exists()) {
                    chooser.setSelectedFile(currentFile);
                }
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                field.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        return button;
    }

    /**
     * 恢复按钮状态并清理待安装更新暂存
     */
    private void resetButtons() {
        pendingInfo = null;
        forceUpdate = false;
        startButton.setEnabled(true);
        startButton.setText("检查更新");
        configButton.setEnabled(true);
        switchAppButton.setEnabled(true);
    }

    /**
     * 解析 JAR 包所在目录
     * <p>
     * 将配置文件、锁文件、版本文件等运行时文件统一锚定到 JAR 包目录，
     * 避免双击启动或从其他目录启动时文件散落在不相关目录。
     * IDE 开发环境（CodeSource 为 classes 目录）或解析失败时回退到 JVM 工作目录。
     * </p>
     *
     * @return JAR 包所在目录，回退时为 JVM 工作目录
     */
    private static File resolveJarDir() {
        try {
            URL location = UpdaterGuiDemo.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(location.toURI());
            if (file.isFile()) {
                return file.getParentFile();
            }
        } catch (Exception e) {
            /*
             * CodeSource 解析失败时回退到 JVM 工作目录
             */
        }
        return new File(System.getProperty("user.dir"));
    }

    /**
     * 保存当前配置到其对应的持久化文件（updater-{appKey}.properties）
     * <p>
     * 若 appKey 被修改则落盘到新 appKey 对应的文件，旧文件保留；写入失败仅记录日志不阻断主流程。
     * </p>
     */
    private void saveCurrentConfig() {
        if (currentConfig == null) {
            return;
        }
        File target = resolveAppConfigFile(currentConfig.getAppKey());
        writeConfigToFile(currentConfig, target);
        if (currentConfigFile != null && !target.equals(currentConfigFile)) {
            appendLog("[配置] appKey 已变更，配置已保存到新文件: " + target.getName());
        }
        currentConfigFile = target;
    }

    /**
     * 列出 JAR 目录下所有应用配置对应的 appKey（从 updater-{appKey}.properties 文件名提取）
     *
     * @return 按名称排序的 appKey 列表
     */
    private List<String> listAppConfigKeys() {
        List<String> keys = new ArrayList<>();
        File[] files = resolveJarDir().listFiles(
            (dir, name) -> name.startsWith(PROFILE_FILE_PREFIX) && name.endsWith(PROFILE_FILE_SUFFIX));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                keys.add(name.substring(PROFILE_FILE_PREFIX.length(), name.length() - PROFILE_FILE_SUFFIX.length()));
            }
        }
        Collections.sort(keys);
        return keys;
    }

    /**
     * 解析指定 appKey 对应的配置文件
     *
     * @param appKey 应用标识
     * @return 配置文件 updater-{appKey}.properties
     */
    private static File resolveAppConfigFile(String appKey) {
        return new File(resolveJarDir(), PROFILE_FILE_PREFIX + appKey + PROFILE_FILE_SUFFIX);
    }

    /**
     * 删除指定 appKey 的应用配置（需用户确认）
     *
     * @param appKey 应用标识
     */
    private void deleteAppConfig(String appKey) {
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除应用配置「" + appKey + "」吗？", "确认删除",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }
        File configFile = resolveAppConfigFile(appKey);
        if (configFile.delete()) {
            appendLog("[配置] 已删除: " + configFile.getName());
        } else {
            JOptionPane.showMessageDialog(this, "删除失败: " + configFile.getName(), "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 将配置写入指定文件（UTF-8 编码，支持中文值）
     *
     * @param config     待写入的配置
     * @param configFile 目标文件
     */
    private void writeConfigToFile(UpdaterConfig config, File configFile) {
        Properties props = new Properties();
        props.setProperty(UpdaterConfig.KEY_SERVER_HOST, config.getServerHost());
        props.setProperty(UpdaterConfig.KEY_SERVER_PORT, String.valueOf(config.getServerPort()));
        props.setProperty(UpdaterConfig.KEY_SERVER_CHECK_PATH, config.getCheckPath());
        props.setProperty(UpdaterConfig.KEY_APP_KEY, config.getAppKey());
        props.setProperty(UpdaterConfig.KEY_APP_PLATFORM, config.getPlatform());
        props.setProperty(UpdaterConfig.KEY_APP_CURRENT_VERSION_CODE, String.valueOf(config.getCurrentVersionCode()));
        props.setProperty(UpdaterConfig.KEY_UPDATE_TIMEOUT_SECONDS, String.valueOf(config.getTimeoutSeconds()));
        props.setProperty(UpdaterConfig.KEY_UPDATE_TRY_ALL_URLS, String.valueOf(config.isTryAllUrls()));
        if (config.getDownloadDir() != null) {
            props.setProperty(UpdaterConfig.KEY_LOCAL_DOWNLOAD_DIR, config.getDownloadDir());
        }
        if (config.getDeployScript() != null) {
            props.setProperty(UpdaterConfig.KEY_LOCAL_DEPLOY_SCRIPT, config.getDeployScript());
        }
        if (config.getStartScript() != null) {
            props.setProperty(UpdaterConfig.KEY_LOCAL_START_SCRIPT, config.getStartScript());
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            props.store(writer, "更新器配置（由 GUI 保存，请勿手动修改格式）");
            appendLog("[配置] 已写入: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            appendLog("[配置] 写入失败: " + e.getMessage());
        }
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
                progressBar.setIndeterminate(false);
                if (info.isNeedUpdate()) {
                    setStatus("发现新版本: " + latestVer, COLOR_WORKING);
                    appendLog("[检查] 发现新版本: " + latestVer + " (versionCode=" + info.getLatestVersionCode() + ")");
                    int fileCount = info.getFiles().size();
                    appendLog("[检查] 待下载文件: " + fileCount + "个");
                    if (info.getDownloadUrls().isEmpty()) {
                        appendLog("[警告] 服务端未返回下载地址");
                    }
                    /*
                     * 两段式交互：暂存更新信息并弹出确认对话框，用户同意后才开始安装
                     */
                    pendingInfo = info;
                    forceUpdate = isForceUpdate(info);
                    if (forceUpdate) {
                        appendLog("[检查] 服务端要求强制更新，本次不可跳过");
                    }
                    showUpdateConfirmDialog(info);
                } else {
                    setStatus("当前已是最新版本", COLOR_SUCCESS);
                    appendLog("[检查] 已是最新版本");
                    resetButtons();
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
                /*
                 * 重置速度采样点与日志里程碑，避免跨文件污染估算
                 */
                speedSnapshot[0] = 0;
                speedSnapshot[1] = 0;
                lastLoggedMilestone = 0;
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
            });
        }

        @Override
        public void onDownloadProgressDetail(long downloadedBytes, long totalBytes, int percent) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(percent);
                progressBar.setString(percent + "%");
                /*
                 * 基于约1秒前的采样点估算实时速度与剩余时间
                 */
                long now = System.currentTimeMillis();
                long prevTime = speedSnapshot[0];
                long prevBytes = speedSnapshot[1];
                if (prevTime <= 0) {
                    speedSnapshot[0] = now;
                    speedSnapshot[1] = downloadedBytes;
                } else if (now - prevTime >= 800) {
                    double bytesPerSecond = (downloadedBytes - prevBytes) * 1000.0 / (now - prevTime);
                    speedSnapshot[0] = now;
                    speedSnapshot[1] = downloadedBytes;
                    if (bytesPerSecond > 0) {
                        String speedText = formatSpeed((long) bytesPerSecond, 1000);
                        if (totalBytes > 0 && downloadedBytes < totalBytes) {
                            long remainSeconds = (long) ((totalBytes - downloadedBytes) / bytesPerSecond);
                            speedText += " | 剩" + formatDuration(remainSeconds);
                        }
                        speedLabel.setText(speedText);
                    }
                }
                /*
                 * 日志降噪：仅在25/50/75里程碑输出，避免刷屏
                 */
                int milestone = percent / 25 * 25;
                if (milestone > 0 && milestone < 100 && milestone > lastLoggedMilestone) {
                    lastLoggedMilestone = milestone;
                    appendLog("[下载] 进度: " + milestone + "%");
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
                speedLabel.setText("");
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
                 * 更新成功后类库已将新版本号写入全局版本文件，
                 * 读取后回写到当前 appKey 对应的配置文件，
                 * 保证每个 appKey 独立记录版本且下次启动不重复检测更新
                 */
                int newVersion = UpdaterConfig.readLocalVersion();
                if (newVersion > currentConfig.getCurrentVersionCode()) {
                    try {
                        currentConfig = rebuildWithVersion(currentConfig, newVersion);
                        updater = new AutoUpdater(currentConfig, new GuiUpdateListener());
                        saveCurrentConfig();
                        appendLog("[成功] 版本号已回写配置文件: " + newVersion);
                    } catch (UpdateException e) {
                        appendLog("[成功] 版本同步失败: " + e.getMessage());
                    }
                }
                versionLabel.setText("当前版本: " + currentConfig.getCurrentVersionCode()
                        + "  |  最新版本: 已同步");
                resetButtons();
                /*
                 * 安装完成后给出明确反馈，避免用户仅凭状态栏无法确认结果
                 */
                if (installCompleted) {
                    JOptionPane.showMessageDialog(UpdaterGuiDemo.this,
                            "更新已完成，当前版本: " + currentConfig.getCurrentVersionCode(),
                            "更新成功", JOptionPane.INFORMATION_MESSAGE);
                }
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
     * 格式化剩余时间
     *
     * @param seconds 剩余秒数
     * @return 可读的时长字符串（如 3秒、1分20秒）
     */
    private static String formatDuration(long seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        if (seconds < 60) {
            return seconds + "秒";
        }
        long minutes = seconds / 60;
        long remainSeconds = seconds % 60;
        if (minutes < 60) {
            return minutes + "分" + remainSeconds + "秒";
        }
        return minutes / 60 + "小时" + minutes % 60 + "分";
    }

    /**
     * 启动 GUI
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        if (!acquireInstanceLock()) {
            JOptionPane.showMessageDialog(null, "更新器已在运行中，请勿重复启动。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        /*
         * 将本地版本文件锚定到 JAR 包所在目录，不再跟随启动时的工作目录
         */
        UpdaterConfig.setLocalVersionDir(resolveJarDir().getAbsolutePath());
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

    /**
     * 获取单实例文件锁
     * <p>
     * 基于 JAR 包所在目录下的锁文件防止重复启动多个更新器实例，
     * JVM 退出时锁自动释放。锁机制自身异常时不阻止启动。
     * </p>
     *
     * @return true表示获取成功（当前为唯一实例），false表示已有实例在运行
     */
    private static boolean acquireInstanceLock() {
        try {
            File lockFile = new File(resolveJarDir(), INSTANCE_LOCK_FILE);
            instanceLockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            instanceLock = instanceLockChannel.tryLock();
            return instanceLock != null;
        } catch (Exception e) {
            return true;
        }
    }
}
