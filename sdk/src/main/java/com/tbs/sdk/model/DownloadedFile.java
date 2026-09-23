package com.tbs.sdk.model;

import java.io.File;
import lombok.Data;

/**
 * 下载完成文件
 * <p>
 * 记录单次下载的源地址与本地落盘位置，供调用方定位安装包。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class DownloadedFile {

    /**
     * 下载源地址
     */
    private String url;

    /**
     * 本地文件
     */
    private File file;

    /**
     * 构造下载结果
     *
     * @param url  下载源地址
     * @param file 本地文件
     * @return 下载结果实例
     */
    public static DownloadedFile of(String url, File file) {
        DownloadedFile result = new DownloadedFile();
        result.setUrl(url);
        result.setFile(file);
        return result;
    }
}
