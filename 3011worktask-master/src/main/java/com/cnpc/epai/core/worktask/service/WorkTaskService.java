package com.cnpc.epai.core.worktask.service;

import com.cnpc.epai.common.template.service.TreeCondiDto;
import com.cnpc.epai.common.template.service.TreeJsonDto;
import com.cnpc.epai.common.util.BusinessException;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.domain.ProjectUserSoftware;
import com.cnpc.epai.core.worktask.domain.WorkTask;
import com.cnpc.epai.core.worktask.domain.WorkTaskBusinessNode;
import com.cnpc.epai.core.worktask.domain.WorkTaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 任务服务
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Service
public interface WorkTaskService {
    /**
     * 查找所有的任务
     *
     * @param page
     * @return 任务列表
     */
    public Page<WorkTask> findByBsflag(Pageable page,String condition);
    

    public List<WorkTask> findByLevel2List(String condition);

    /**
     * 查找个人所有的任务
     *
     * @param page
     * @return 任务列表
     */
    public Page<WorkTask> findPersonTask(Pageable page,String condition);

    Map<String,Integer> countWorkTask(String projectId);

    /**
     * 根据项目工作室ID查询任务人员
     * @param workroomId
     * @param page
     * @return 任务人员列表
     */
    Page<WorkTask> findByProject(String workroomId, String status, Pageable page);

    @Transactional
    WorkTask createSceneWorktask(WorkTask workTask, String userIds)throws BusinessException;

    //如果是编辑的 需要清空数据
    @Transactional
    WorkTask createWorktask(WorkTask workTask,
                            String userIds,
                            String nodes,
                            String toolIds,
                            String softwareIds) throws BusinessException;

    /**
     * 保存任务
     * @param worktask
     * @param userIds
     * @return 任务
     */
    public WorkTask save(WorkTask worktask, String userIds);

    /**
     * 根据id查找
     * @param id
     * @return 任务
     */
    public WorkTask findById(String id);

    /**
     * 根据条件查询人员分配
     * @param projectId
     * @param userId
     * @param status ，分隔的状态代码
     * @param page 分页
     * @return 人员分配分页
     */
    Page<WorkTask> findByProjectIdAndUserId(String projectId, String userId, String status, Pageable page);

    /**
     * 响应任务
     * @param workTaskId
     * @param opr pass/refuse
     * @param oprContent 响应内容
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String answerAssignment(String workTaskId, String opr,String oprContent) throws BusinessException;

    /**
     *  终止任务
     * @param workTaskId
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String taskOver(String workTaskId) throws BusinessException;

    /**
     *  删除任务 置标志位
     * @param workTaskId
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String deleteTask(String workTaskId) throws BusinessException;

    /**
     * 任务延期
     * @param workTaskId
     * @param endDate 延期至
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String delay(String workTaskId, Date endDate) throws BusinessException;

    /**
     * 催办
     * @param workTaskId
     * @param endDate
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String reminders(String workTaskId, Date endDate) throws BusinessException;

    /**
     * 提交任务 并关联成果
     *
     * @param workTaskId
     * @param treeDataIds
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String submitTask(String workTaskId, String treeDataIds) throws BusinessException;

    /**
     * 重启任务
     * @param workTask
     * @param userIds 分配人员id
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String rebootWorkTask(WorkTask workTask, String userIds) throws BusinessException;

    /**
     * 审核任务
     * @param workTaskId
     * @param oprType 操作类型 pass/refuse
     * @return 操作结果 1：操作成功 非1操作失败
     * @throws BusinessException
     */
    @Transactional
    String audit(String workTaskId, String oprType) throws BusinessException;

    /**
     * 获取任务日志
     * @param workTaskId
     * @param userId
     * @param status 操作类型
     * @return 任务日志列表
     * @throws BusinessException
     */
    List<WorkTaskLog> findWorkTaskLog(String workTaskId, String userId, String status) throws BusinessException;

    /**
     * 人员任务统计
     * @param projectId
     * @return 查询有效的任务数,任务类型
     */
    List<Object> taskProgressStatisticalByCurrentState(String projectId);

    /**
     * 任务进展统计
     * @param projectId
     * @return 任务进展状态Map
     */
    Map<String, List> taskProgressStatisticalByUser(String projectId);

    /**
     * 根据项目ID分组查询任务数量
     * @param projectId
     * @return 任务列表
     */
    Map<String, Object> findCountByProjectId(String projectId);

    List<TreeJsonDto> getToolFullTree(String worktaskId);

    List<ProjectUserSoftware> getSoftwareAccount(String projectId, String satelliteId);

    List getWorkTaskApplicationTree(String id, String eoCode, String showSoftwareAccount, boolean isDesktop);

    List<Map<String,Object>> getWorkTaskApplicationLeaf(String id, String eoCode, String showSoftwareAccount, boolean isDesktop);

    List<Map<String,Object>> getWorkTaskApplication(String id, String eoCode, String showSoftwareAccount, boolean isDesktop);

    List<SrMetaDataset> getDataset(String worktaskId);

    //    @Cacheable(cacheNames = "ProjectFulltree", key = "#p1 + T(com.alibaba.fastjson.JSON).toJSONString(#p0)")
    List<TreeJsonDto> getBusinessNodeFullTree(TreeCondiDto[] treeCategoryPamra,
                                              String taskId ,String dataRegion);

    List<WorkTaskBusinessNode> getNodesByName(String taskId, String name, String type);

    String renameBusinessNode(String businessNodeId, String name);

    List<SrMetaDataset> getDatasetTree(String taskId, String dataRegion);
    
    public String sceneTaskSync(String taskId, Map<String,Object> param) throws Exception;

    String saveFullTreeDataByTask(String taskId,String dataRegion,String nodes) throws Exception;
}
