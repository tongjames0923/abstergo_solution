package com.tbs.app.remote.company;

import com.tbs.app.vo.RemoteCompanyInfoVO;

import java.util.List;

/**
 * 远程公司服务接口
 *
 * @author abstergo
 */
public interface ICompanyRemoteService {

    /**
     * 根据公司ID获取公司信息
     *
     * @param companyId 公司ID
     * @return 公司信息
     */
    RemoteCompanyInfoVO getCompanyInfoById(Long companyId);

    /**
     * 根据公司名称模糊查询公司信息列表
     *
     * @param companyName 公司名称（支持模糊查询）
     * @return 公司信息列表
     */
    List<RemoteCompanyInfoVO> getCompanyInfoByName(String companyName);

    /**
     * 根据公司税号获取公司信息
     *
     * @param taxNumber 公司税号
     * @return 公司信息
     */
    RemoteCompanyInfoVO getCompanyInfoByTaxNumber(String taxNumber);

    /**
     * 获取指定公司的子公司列表
     *
     * @param parentId 父公司ID
     * @return 子公司信息列表
     */
    List<RemoteCompanyInfoVO> getChildCompanies(Long parentId);
}
