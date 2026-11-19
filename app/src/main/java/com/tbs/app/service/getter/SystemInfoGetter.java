package com.tbs.app.service.getter;

import com.tbs.common.util.base.CollUtil;
import com.tbs.script.api.expressible.IValueGetter;
import com.tbs.script.api.expressible.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * 系统信息取值器。
 * <p>
 * 用于在脚本中按类型读取运行主机（服务器）的系统信息， 返回值统一为字符串，获取失败或类型未知时返回空字符串，不会抛出异常中断脚本。
 */
@Component
public class SystemInfoGetter implements IValueGetter, ApplicationListener<WebServerInitializedEvent> {

    /** Web 容器实际启动端口，-1 表示尚未启动或非 Web 环境 */
    private static volatile int webServerPort = -1;

    @Autowired(required = false)
    private Environment environment;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        webServerPort = event.getWebServer().getPort();
    }

    @Override
    public String getGetterName() {
        return "系统信息";
    }

    @Override
    public String getGetterId() {
        return "SystemInfoGetter";
    }

    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(ParamType.builder().isEnum(true).type("string").required(true).name("系统信息类型").description(
            "选择需要获取的系统信息类型。" +
                "hostname：主机名称；" +
                "hostIp：主机 IP 地址（本机非回环 IPv4）；" +
                "hostMac：主机 MAC 地址（第一块可用网卡，格式 AA:BB:CC:DD:EE:FF）；" +
                "hostport：当前 Web 服务监听端口；" +
                "os：操作系统名称、版本与架构；" +
                "cpu：CPU 核心数与使用率；" +
                "memory：物理内存总量、可用量与已用量。").enumValues(
            List.of(EnumValue.builder().value("hostname").label("主机名称（本机主机名）").build(),
                EnumValue.builder().value("hostIp").label("主机 IP（非回环 IPv4 地址）").build(),
                EnumValue.builder().value("hostMac").label("主机 MAC（第一块可用网卡）").build(),
                EnumValue.builder().value("hostport").label("主机端口（Web 服务监听端口）").build(),
                EnumValue.builder().value("os").label("操作系统（名称/版本/架构）").build(),
                EnumValue.builder().value("cpu").label("CPU 信息（核心数与使用率）").build(),
                EnumValue.builder().value("memory").label("内存信息（总量/可用/已用）").build())).sort(1)
            .ui(ParamUiMeta.builder().inputType("select").build()).build());
    }

    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("string").name("系统信息").description(
            "字符串类型的系统信息，格式随入参类型不同而不同：" +
                "hostname 返回主机名，如 my-server；" +
                "hostIp 返回 IPv4 地址，如 192.168.1.10；" +
                "hostMac 返回大写冒号分隔的 MAC，如 00:1A:2B:3C:4D:5E；" +
                "hostport 返回端口数字字符串，如 8080；" +
                "os 返回操作系统名称、版本与架构，如 Linux 5.15.0 (amd64)；" +
                "cpu 返回核心数与使用率，如 8 核, 使用率 12.3%；" +
                "memory 返回总内存/可用/已用，如 总内存:16.00GB, 可用:8.00GB, 已用:8.00GB。" +
                "获取失败或类型未知时返回空字符串。").sort(1).build();
    }

    @Override
    public String getDescription() {
        return "获取运行主机的系统信息，支持按类型读取：" +
            "主机名称、主机 IP、主机 MAC、服务监听端口、操作系统、CPU 信息、内存信息。" +
            "返回值为字符串，不同信息类型对应不同格式；获取失败或类型未知时返回空字符串，不会中断脚本执行。";
    }

    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        if (CollUtil.isEmpty(paramValues)) {
            return emptyResult();
        }
        ParamValue paramValue = CollUtil.getFirst(paramValues);
        if (paramValue == null) {
            return emptyResult();
        }
        String type = paramValue.getValue();
        if (type == null || type.isBlank()) {
            return emptyResult();
        }
        String value;
        try {
            value = switch (type) {
                case "hostname" -> getHostName();
                case "hostIp" -> getHostIp();
                case "hostMac" -> getHostMac();
                case "hostport" -> getHostPort();
                case "os" -> getOsInfo();
                case "cpu" -> getCpuInfo();
                case "memory" -> getMemoryInfo();
                default -> "";
            };
        } catch (Exception e) {
            // 获取失败时返回空串，避免脚本执行中断
            value = "";
        }
        return ResultValue.builder().type(responseResultType()).value(value).build();
    }

    private ResultValue emptyResult() {
        return ResultValue.builder().type(responseResultType()).value("").build();
    }

    /* ------------------------------ 各项取值实现 ------------------------------ */

    private String getHostName() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            String name = local.getHostName();
            if (name != null && !name.isBlank()) {
                return name;
            }
        } catch (Exception ignored) {
        }
        // 降级：Linux 读 HOSTNAME，Windows 读 COMPUTERNAME
        String env = System.getenv("HOSTNAME");
        if (env == null || env.isBlank()) {
            env = System.getenv("COMPUTERNAME");
        }
        return env == null ? "" : env;
    }

    private String getHostIp() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (!local.isLoopbackAddress() && local instanceof Inet4Address) {
                return local.getHostAddress();
            }
        } catch (Exception ignored) {
        }
        // 降级：遍历网卡找第一个可用的 IPv4 地址
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String getHostMac() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                for (byte b : mac) {
                    sb.append(String.format("%02X:", b));
                }
                return sb.substring(0, sb.length() - 1);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String getHostPort() {
        if (webServerPort > 0) {
            return String.valueOf(webServerPort);
        }
        if (environment != null) {
            String port = environment.getProperty("local.server.port");
            if (port == null || port.isBlank()) {
                port = environment.getProperty("server.port");
            }
            if (port != null && !port.isBlank()) {
                return port;
            }
        }
        return "8080";
    }

    private String getOsInfo() {
        String osName = System.getProperty("os.name", "");
        String osVersion = System.getProperty("os.version", "");
        String osArch = System.getProperty("os.arch", "");
        return osName + " " + osVersion + " (" + osArch + ")";
    }

    private String getCpuInfo() {
        int cores = Runtime.getRuntime().availableProcessors();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            // getCpuLoad 为整机 CPU 使用率，取不到时返回负数
            double load = sunOsBean.getCpuLoad();
            if (load >= 0) {
                return cores + " 核, 使用率 " + String.format("%.1f%%", load * 100);
            }
        }
        return cores + " 核 (" + System.getProperty("os.arch", "") + ")";
    }

    private String getMemoryInfo() {
        long total;
        long free;
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            total = sunOsBean.getTotalMemorySize();
            free = sunOsBean.getFreeMemorySize();
        } else {
            // 降级到 JVM 内存
            Runtime rt = Runtime.getRuntime();
            total = rt.maxMemory();
            free = rt.freeMemory();
        }
        return "总内存:" +
            formatBytes(total) +
            ", 可用:" +
            formatBytes(free) +
            ", 已用:" +
            formatBytes(Math.max(total - free, 0));
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        double v = bytes;
        int idx = -1;
        while (v >= 1024 && idx < units.length - 1) {
            v /= 1024;
            idx++;
        }
        return String.format("%.2f%s", v, units[idx]);
    }
}
