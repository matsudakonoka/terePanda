package com.cnpc.epai.core.worktask.repository;


import com.cnpc.epai.common.template.repository.StringIdRepository;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SrTaskTreeDataRepository extends JpaRepository<SrTaskTreeData, String>, JpaSpecificationExecutor<SrTaskTreeData> {
    @Query(value = "select * from SR_TASK_TREE_DATA where node_ID = ?1 and OBJECT_ID <> ?2 and bsflag = 'N'",nativeQuery = true)
    List<SrTaskTreeData> getAllByNodeId(String nodeId,String objectId);

    @Query(value = "select * from SR_TASK_TREE_DATA where node_ID = ?1 and OBJECT_ID = ?2 and bsflag = 'N'",nativeQuery = true)
    List<SrTaskTreeData> getAllByNodeIdAndNodeId(String nodeId, String wellId);

    @Query(value = "select * from SR_TASK_TREE_DATA where WORK_ID = ?1 and NODE_ID = ?2 and bsflag = 'N'",nativeQuery = true)
    List<SrTaskTreeData> getAllObjectByUser(String workId,String nodeId);

    @Query(value = "select * from SR_TASK_TREE_DATA where OBJECT_NAME like CONCAT('%',?1,'%') and DATASET_ID = ?2 and bsflag = 'N'",nativeQuery = true)
    List<SrTaskTreeData> findByobjectName(String objectName, String datasetId);

    @Query(value = "select * from SR_TASK_TREE_DATA where WORK_ID = ?1 and DATASET_ID = ?2 and OBJECT_NAME like CONCAT('%',?2,'%') and bsflag = 'N'",nativeQuery = true)
    List<SrTaskTreeData> getSrTreeData(String workId, String dataSetId, String key);

    @Query(value = "select * from SR_TASK_TREE_DATA where WORK_TREE_DATA_ID = ?1 and bsflag = 'N'",nativeQuery = true)
    SrTaskTreeData findOne(String id);
}
