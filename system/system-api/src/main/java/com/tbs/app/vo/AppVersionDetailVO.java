package com.tbs.app.vo;

import com.tbs.shared.pojo.entity.version.AppVersionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 应用版本详情视图
 * <p>
 * 聚合版本完整信息与名下安装包列表，供版本详情页使用
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppVersionDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本信息，含更新日志、构建信息等完整字段
     */
    private AppVersionEntity version;

    /**
     * 版本下的安装包列表
     */
    private List<AppPackageVO> packages;
}
