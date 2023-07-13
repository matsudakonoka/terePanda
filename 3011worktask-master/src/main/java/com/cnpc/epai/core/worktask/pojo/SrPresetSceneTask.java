package com.cnpc.epai.core.worktask.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class SrPresetSceneTask {
    private String presetSceneTaskId;

    private String taskId;

    private String sceneId;

    private String workroomId;

    private String remarks;

    private String bsflag;

    private String createUser;

    private Date createDate;

    private String updateUser;

    private Date updateDate;

}