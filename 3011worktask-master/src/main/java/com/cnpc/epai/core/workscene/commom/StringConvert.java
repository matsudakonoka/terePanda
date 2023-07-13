package com.cnpc.epai.core.workscene.commom;

import com.cnpc.epai.core.workscene.entity.WorkNode;
import com.cnpc.epai.core.workscene.pojo.vo.Facade;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;

public class StringConvert {
    public static String convertToString(Facade[] facades) {
        int size = facades.length;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                stringBuilder.append(facades[i].id());
            } else {
                stringBuilder.append(",").append(facades[i].id());
            }
        }
        return stringBuilder.toString();
    }


    public static Set<String> convertToSet(String str) {
        String[] split = str.split(",");
        int size = split.length;
        Set<String> res = new HashSet<>();
        for (int i = 0; i < size; i++) {
            res.add(split[i]);
        }
        return res;
    }

    public static List<String> convertToList(String str) {
        String[] split = str.split(",");
        int size = split.length;
        List<String> res = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            res.add(split[i]);
        }
        return res;
    }

    public static String convertToString(Collection<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                stringBuilder.append(item);
                first = false;
            } else {
                stringBuilder.append(",").append(item);
            }
        }
        return stringBuilder.toString();
    }

    public static String convertEntityToString(List<WorkNode> treeNodes) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (WorkNode item : treeNodes) {
            if (first) {
                stringBuilder.append(item.getTreeNodeId());
                first = false;
            } else {
                stringBuilder.append(",").append(item.getTreeNodeId());
            }
        }
        return stringBuilder.toString();
    }

}
