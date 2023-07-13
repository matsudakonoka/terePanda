package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.ProjectUserSoftware;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface ProjectUserSoftwareRepository extends StringIdRepository<ProjectUserSoftware> {
    /**
     * 查询当前项目中分配给成员的软件信息
     * @param satelliteId 卫星端id
     * @param projectId 项目id
     * @param softwareId 软件id
     * @param bsflag N有效 Y无效
     * @return 软件信息列表
     */
    List<ProjectUserSoftware> findBySatelliteIdAndProjectIdAndSoftwareIdAndBsflag(String satelliteId,
                                                                                  String projectId,
                                                                                  String softwareId,
                                                                                  String bsflag);

    /**
     * 获取用户在工作室下，所推送的该款软件都有哪些配置账号
     * @param satelliteId 卫星端id
     * @param projectId 项目id
     * @param userId 用户id
     * @param bsflag N有效 Y无效
     * @return 软件信息列表
     */
    List<ProjectUserSoftware> findBySatelliteIdAndProjectIdAndUserIdAndBsflag(String satelliteId,
                                                                              String projectId,
                                                                              String userId,
                                                                              String bsflag);

}
