package com.cnpc.epai.core.worktask.repository;

import com.cnpc.epai.common.template.repository.TreeRepository;
import com.cnpc.epai.core.worktask.domain.WorkTask;
import com.cnpc.epai.core.worktask.domain.WorkTaskBusinessNode;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * @author 王淼
 * @Title: A6
 * @Package com.cnpc.epai.core.worktask.repository
 * @Description: 功能描述
 * @date 18:20 2018/7/11
 * {修改记录：修改人、修改时间、修改内容等}
 */
@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Repository
public interface WorkTaskBusinessNodeRepository extends TreeRepository<WorkTaskBusinessNode> {

    /**
     * 根据项目id查询
     * @param taskId
     * @return 关联关系列表
     */
    List<WorkTaskBusinessNode> findByTaskId(String taskId);

    /**
     * 查询根业务节点
     * @param taskId
     * @return 根节点列表
     */
    @Query("select a from WorkTaskBusinessNode a where a.parent is null and a.taskId = :taskId order by a.showOrder")
    List<WorkTaskBusinessNode> findBusinessNodeByTaskIdAndParentIsNull(@Param("taskId") String taskId);

    /**
     * 根据父节点id查询业务节点
     * @param elementid
     * @return
     */
    @Override
    @Query("select e from WorkTaskBusinessNode e ,e.elements ele where ele.id =:elementid")
    List<WorkTaskBusinessNode> getElementParent(@Param("elementid") String elementid);

    /**
     * 查询节点列表
     * @param name 名称
     * @return 节点列表
     */
    List<WorkTaskBusinessNode> findByTaskIdAndName(String taskId, String name);

    /**
     * 查询节点列表
     * @param name 名称
     * @return 节点列表
     */
    List<WorkTaskBusinessNode> findByTaskIdAndNameLike(String taskId, String name);
}
