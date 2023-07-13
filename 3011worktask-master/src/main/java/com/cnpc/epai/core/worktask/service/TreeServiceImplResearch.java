package com.cnpc.epai.core.worktask.service;

import com.cnpc.epai.common.template.service.TreeJsonDto;
import com.cnpc.epai.common.template.service.TreeServiceImpl;
import com.cnpc.epai.core.dataset.domain.SrMetaDataset;
import com.cnpc.epai.core.worktask.repository.DataSetRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * @Description: 树服务抽象类
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
public abstract class TreeServiceImplResearch extends TreeServiceImpl {

    @Autowired
    DataSetRepository dataSetRepository;

    private List<SrMetaDataset> datasetList;

    /**
     * 获取数据集的父节点
     * @param node
     * @return
     */
    public SrMetaDataset findRoot(SrMetaDataset node){
        if (StringUtils.isEmpty(node.getPDatasetId())) {
            return node;
        }
        for (SrMetaDataset d : datasetList) {
            if (node.getPDatasetId().equals(d.getId())) {
                List<SrMetaDataset> children = new ArrayList<>();
                children.add(node);
                d.setChildren(children);
                return findRoot(d);
            }
        }
        return null;
    }

    /**
     * 获取数据集
     * @param id
     * @return
     */
    public SrMetaDataset findOne(String id){
        for (SrMetaDataset d : datasetList){
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    /**
     * 生成数据集列表
     */
    public void genDatasetList(String dataRegion){
        datasetList = dataSetRepository.findAll(dataRegion);
    }

    /**
     * 对叶子节点（数据集）查询根数据集，并将根数据集挂在原有叶子节点的位置，
     * 并带上原有节点，保留数据集路径轨迹。
     * @param list
     */
    public void genDataSetParent(List<TreeJsonDto> list){
        Iterator<TreeJsonDto> it = list.iterator();
        while (it.hasNext()){
            TreeJsonDto dto = it.next();
            if (dto.getChildren() != null && !dto.getChildren().isEmpty()){
                genDataSetParent(dto.getChildren());
            }else if (dto.getAttributes() != null && dto.getAttributes() instanceof Map) {
                Map<String,Object> map = (Map<String, Object>) dto.getAttributes();
                //判断当前节点是否为数据集类型,是叶子节点，并且Attributes中的isElement为true为数据集
                if ((boolean)map.get("isElement")) {
                    SrMetaDataset currentDataSet = findOne(dto.getId());
                    //从服务中未取到数据集 删除掉此节点
                    if ( currentDataSet == null ){
                        it.remove();
                        continue;
                    }

                    SrMetaDataset dataset = findRoot(currentDataSet);
                    if ( dataset == null ){
                        it.remove();
                        continue;
                    }
                    TreeJsonDto dtoRoot = new TreeJsonDto();
                    parseDataSet(dtoRoot,dataset,dto);
                    TreeJsonDto sameTreeNode = findSameDataSetTreeNode(dtoRoot,list);
                    if (sameTreeNode == null) {
                        BeanUtils.copyProperties(dtoRoot,dto);
                    } else if (sameTreeNode.getChildren() == null) {
                        BeanUtils.copyProperties(dtoRoot,dto);
                    }else{
                        sameTreeNode.getChildren().addAll(dtoRoot.getChildren());
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * 查找兄弟节点中是否存在同一个数据集
     * @param source
     * @param list
     * @return
     */
    private TreeJsonDto findSameDataSetTreeNode(TreeJsonDto source,List<TreeJsonDto> list){
        for (TreeJsonDto dto : list){
            if (dto.getId().equals(source.getId())) {
                return dto;
            }
        }
        return null;
    }

    /**
     * 将数据集对象转换成树结构
     *
     * @param dto 树节点
     * @param dataset 数据集对象
     * @param leaf 当前叶子节点对象
     */
    private void parseDataSet(TreeJsonDto dto, SrMetaDataset dataset,TreeJsonDto leaf){
        Map<String,Object> attribute = null;
        Map<String,Object> element =  new HashMap();
        if (dataset.getChildren() != null && !(dataset.getChildren().isEmpty())) {
            dto.setState("open");
            dto.setText(dataset.getName());
            dto.setId(dataset.getId());
            dto.setChecked(false);
            dto.setIconCls(dataset.getIconCls());


            element.put("eoCode",dataset.getEoCode());
            element.put("id",dataset.getId());
            element.put("name",dataset.getName());
            element.put("datasetType",dataset.getDatasetType());
            element.put("iconCls",dataset.getIconCls());

            element.put("isBaseDataset",dataset.getIsBaseDataset());
            element.put("isDisplay",dataset.getIsDisplay());
            element.put("isValid",dataset.getIsValid());
            element.put("pDatasetId",dataset.getPDatasetId());
            element.put("sortingCondition",dataset.getSortingCondition());
            element.put("treeId",dataset.getTreeId());
            element.put("type",dataset.getType());
            element.put("isDrillDown",dataset.getIsDrillDown());

            attribute = new HashMap();
            attribute.put("isDataSet",true);
            attribute.put("element",element);
            dto.setAttributes(attribute);

            //实际只循环一次，数据集只包含某个节点的溯源路径节点
            for(SrMetaDataset d: dataset.getChildren()){
                TreeJsonDto currentDto = new TreeJsonDto();
                List<TreeJsonDto> dtoList = new ArrayList<>();
                dtoList.add(currentDto);
                dto.setChildren(dtoList);
                parseDataSet(currentDto,d,leaf);
            }
        }else{
            leaf.setIconCls(dataset.getIconCls());
            BeanUtils.copyProperties(leaf,dto);

            if (dto.getAttributes() instanceof Map) {
                attribute = (Map<String, Object>) dto.getAttributes();
                attribute.put("isDataSet",true);
            }
        }
    }

}
