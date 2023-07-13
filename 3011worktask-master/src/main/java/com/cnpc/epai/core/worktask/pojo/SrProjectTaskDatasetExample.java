package com.cnpc.epai.core.worktask.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SrProjectTaskDatasetExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SrProjectTaskDatasetExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andTaskDatasetIdIsNull() {
            addCriterion("task_dataset_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdIsNotNull() {
            addCriterion("task_dataset_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdEqualTo(String value) {
            addCriterion("task_dataset_id =", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdNotEqualTo(String value) {
            addCriterion("task_dataset_id <>", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdGreaterThan(String value) {
            addCriterion("task_dataset_id >", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdGreaterThanOrEqualTo(String value) {
            addCriterion("task_dataset_id >=", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdLessThan(String value) {
            addCriterion("task_dataset_id <", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdLessThanOrEqualTo(String value) {
            addCriterion("task_dataset_id <=", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdLike(String value) {
            addCriterion("task_dataset_id like", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdNotLike(String value) {
            addCriterion("task_dataset_id not like", value, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdIn(List<String> values) {
            addCriterion("task_dataset_id in", values, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdNotIn(List<String> values) {
            addCriterion("task_dataset_id not in", values, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdBetween(String value1, String value2) {
            addCriterion("task_dataset_id between", value1, value2, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andTaskDatasetIdNotBetween(String value1, String value2) {
            addCriterion("task_dataset_id not between", value1, value2, "taskDatasetId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdIsNull() {
            addCriterion("workroom_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdIsNotNull() {
            addCriterion("workroom_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdEqualTo(String value) {
            addCriterion("workroom_id =", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdNotEqualTo(String value) {
            addCriterion("workroom_id <>", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdGreaterThan(String value) {
            addCriterion("workroom_id >", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdGreaterThanOrEqualTo(String value) {
            addCriterion("workroom_id >=", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdLessThan(String value) {
            addCriterion("workroom_id <", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdLessThanOrEqualTo(String value) {
            addCriterion("workroom_id <=", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdLike(String value) {
            addCriterion("workroom_id like", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdNotLike(String value) {
            addCriterion("workroom_id not like", value, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdIn(List<String> values) {
            addCriterion("workroom_id in", values, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdNotIn(List<String> values) {
            addCriterion("workroom_id not in", values, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdBetween(String value1, String value2) {
            addCriterion("workroom_id between", value1, value2, "workroomId");
            return (Criteria) this;
        }

        public Criteria andWorkroomIdNotBetween(String value1, String value2) {
            addCriterion("workroom_id not between", value1, value2, "workroomId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(String value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(String value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(String value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(String value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(String value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLike(String value) {
            addCriterion("task_id like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotLike(String value) {
            addCriterion("task_id not like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<String> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<String> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(String value1, String value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(String value1, String value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdIsNull() {
            addCriterion("dataset_id is null");
            return (Criteria) this;
        }

        public Criteria andDatasetIdIsNotNull() {
            addCriterion("dataset_id is not null");
            return (Criteria) this;
        }

        public Criteria andDatasetIdEqualTo(String value) {
            addCriterion("dataset_id =", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdNotEqualTo(String value) {
            addCriterion("dataset_id <>", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdGreaterThan(String value) {
            addCriterion("dataset_id >", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdGreaterThanOrEqualTo(String value) {
            addCriterion("dataset_id >=", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdLessThan(String value) {
            addCriterion("dataset_id <", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdLessThanOrEqualTo(String value) {
            addCriterion("dataset_id <=", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdLike(String value) {
            addCriterion("dataset_id like", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdNotLike(String value) {
            addCriterion("dataset_id not like", value, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdIn(List<String> values) {
            addCriterion("dataset_id in", values, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdNotIn(List<String> values) {
            addCriterion("dataset_id not in", values, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdBetween(String value1, String value2) {
            addCriterion("dataset_id between", value1, value2, "datasetId");
            return (Criteria) this;
        }

        public Criteria andDatasetIdNotBetween(String value1, String value2) {
            addCriterion("dataset_id not between", value1, value2, "datasetId");
            return (Criteria) this;
        }

        public Criteria andSortSequenceIsNull() {
            addCriterion("sort_sequence is null");
            return (Criteria) this;
        }

        public Criteria andSortSequenceIsNotNull() {
            addCriterion("sort_sequence is not null");
            return (Criteria) this;
        }

        public Criteria andSortSequenceEqualTo(Long value) {
            addCriterion("sort_sequence =", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceNotEqualTo(Long value) {
            addCriterion("sort_sequence <>", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceGreaterThan(Long value) {
            addCriterion("sort_sequence >", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceGreaterThanOrEqualTo(Long value) {
            addCriterion("sort_sequence >=", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceLessThan(Long value) {
            addCriterion("sort_sequence <", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceLessThanOrEqualTo(Long value) {
            addCriterion("sort_sequence <=", value, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceIn(List<Long> values) {
            addCriterion("sort_sequence in", values, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceNotIn(List<Long> values) {
            addCriterion("sort_sequence not in", values, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceBetween(Long value1, Long value2) {
            addCriterion("sort_sequence between", value1, value2, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andSortSequenceNotBetween(Long value1, Long value2) {
            addCriterion("sort_sequence not between", value1, value2, "sortSequence");
            return (Criteria) this;
        }

        public Criteria andBsflagIsNull() {
            addCriterion("bsflag is null");
            return (Criteria) this;
        }

        public Criteria andBsflagIsNotNull() {
            addCriterion("bsflag is not null");
            return (Criteria) this;
        }

        public Criteria andBsflagEqualTo(String value) {
            addCriterion("bsflag =", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagNotEqualTo(String value) {
            addCriterion("bsflag <>", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagGreaterThan(String value) {
            addCriterion("bsflag >", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagGreaterThanOrEqualTo(String value) {
            addCriterion("bsflag >=", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagLessThan(String value) {
            addCriterion("bsflag <", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagLessThanOrEqualTo(String value) {
            addCriterion("bsflag <=", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagLike(String value) {
            addCriterion("bsflag like", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagNotLike(String value) {
            addCriterion("bsflag not like", value, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagIn(List<String> values) {
            addCriterion("bsflag in", values, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagNotIn(List<String> values) {
            addCriterion("bsflag not in", values, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagBetween(String value1, String value2) {
            addCriterion("bsflag between", value1, value2, "bsflag");
            return (Criteria) this;
        }

        public Criteria andBsflagNotBetween(String value1, String value2) {
            addCriterion("bsflag not between", value1, value2, "bsflag");
            return (Criteria) this;
        }

        public Criteria andRemarksIsNull() {
            addCriterion("remarks is null");
            return (Criteria) this;
        }

        public Criteria andRemarksIsNotNull() {
            addCriterion("remarks is not null");
            return (Criteria) this;
        }

        public Criteria andRemarksEqualTo(String value) {
            addCriterion("remarks =", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksNotEqualTo(String value) {
            addCriterion("remarks <>", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksGreaterThan(String value) {
            addCriterion("remarks >", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksGreaterThanOrEqualTo(String value) {
            addCriterion("remarks >=", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksLessThan(String value) {
            addCriterion("remarks <", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksLessThanOrEqualTo(String value) {
            addCriterion("remarks <=", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksLike(String value) {
            addCriterion("remarks like", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksNotLike(String value) {
            addCriterion("remarks not like", value, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksIn(List<String> values) {
            addCriterion("remarks in", values, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksNotIn(List<String> values) {
            addCriterion("remarks not in", values, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksBetween(String value1, String value2) {
            addCriterion("remarks between", value1, value2, "remarks");
            return (Criteria) this;
        }

        public Criteria andRemarksNotBetween(String value1, String value2) {
            addCriterion("remarks not between", value1, value2, "remarks");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNull() {
            addCriterion("create_user is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNotNull() {
            addCriterion("create_user is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserEqualTo(String value) {
            addCriterion("create_user =", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotEqualTo(String value) {
            addCriterion("create_user <>", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThan(String value) {
            addCriterion("create_user >", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThanOrEqualTo(String value) {
            addCriterion("create_user >=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThan(String value) {
            addCriterion("create_user <", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThanOrEqualTo(String value) {
            addCriterion("create_user <=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLike(String value) {
            addCriterion("create_user like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotLike(String value) {
            addCriterion("create_user not like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserIn(List<String> values) {
            addCriterion("create_user in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotIn(List<String> values) {
            addCriterion("create_user not in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserBetween(String value1, String value2) {
            addCriterion("create_user between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotBetween(String value1, String value2) {
            addCriterion("create_user not between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNull() {
            addCriterion("create_date is null");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNotNull() {
            addCriterion("create_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreateDateEqualTo(Date value) {
            addCriterion("create_date =", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotEqualTo(Date value) {
            addCriterion("create_date <>", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThan(Date value) {
            addCriterion("create_date >", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThanOrEqualTo(Date value) {
            addCriterion("create_date >=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThan(Date value) {
            addCriterion("create_date <", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThanOrEqualTo(Date value) {
            addCriterion("create_date <=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIn(List<Date> values) {
            addCriterion("create_date in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotIn(List<Date> values) {
            addCriterion("create_date not in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateBetween(Date value1, Date value2) {
            addCriterion("create_date between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotBetween(Date value1, Date value2) {
            addCriterion("create_date not between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNull() {
            addCriterion("update_user is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNotNull() {
            addCriterion("update_user is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserEqualTo(String value) {
            addCriterion("update_user =", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotEqualTo(String value) {
            addCriterion("update_user <>", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThan(String value) {
            addCriterion("update_user >", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThanOrEqualTo(String value) {
            addCriterion("update_user >=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThan(String value) {
            addCriterion("update_user <", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThanOrEqualTo(String value) {
            addCriterion("update_user <=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLike(String value) {
            addCriterion("update_user like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotLike(String value) {
            addCriterion("update_user not like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIn(List<String> values) {
            addCriterion("update_user in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotIn(List<String> values) {
            addCriterion("update_user not in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserBetween(String value1, String value2) {
            addCriterion("update_user between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotBetween(String value1, String value2) {
            addCriterion("update_user not between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateDateIsNull() {
            addCriterion("update_date is null");
            return (Criteria) this;
        }

        public Criteria andUpdateDateIsNotNull() {
            addCriterion("update_date is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateDateEqualTo(Date value) {
            addCriterion("update_date =", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotEqualTo(Date value) {
            addCriterion("update_date <>", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateGreaterThan(Date value) {
            addCriterion("update_date >", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateGreaterThanOrEqualTo(Date value) {
            addCriterion("update_date >=", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateLessThan(Date value) {
            addCriterion("update_date <", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateLessThanOrEqualTo(Date value) {
            addCriterion("update_date <=", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateIn(List<Date> values) {
            addCriterion("update_date in", values, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotIn(List<Date> values) {
            addCriterion("update_date not in", values, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateBetween(Date value1, Date value2) {
            addCriterion("update_date between", value1, value2, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotBetween(Date value1, Date value2) {
            addCriterion("update_date not between", value1, value2, "updateDate");
            return (Criteria) this;
        }

        public Criteria andBusinessIdIsNull() {
            addCriterion("business_id is null");
            return (Criteria) this;
        }

        public Criteria andBusinessIdIsNotNull() {
            addCriterion("business_id is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessIdEqualTo(String value) {
            addCriterion("business_id =", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdNotEqualTo(String value) {
            addCriterion("business_id <>", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdGreaterThan(String value) {
            addCriterion("business_id >", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdGreaterThanOrEqualTo(String value) {
            addCriterion("business_id >=", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdLessThan(String value) {
            addCriterion("business_id <", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdLessThanOrEqualTo(String value) {
            addCriterion("business_id <=", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdLike(String value) {
            addCriterion("business_id like", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdNotLike(String value) {
            addCriterion("business_id not like", value, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdIn(List<String> values) {
            addCriterion("business_id in", values, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdNotIn(List<String> values) {
            addCriterion("business_id not in", values, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdBetween(String value1, String value2) {
            addCriterion("business_id between", value1, value2, "businessId");
            return (Criteria) this;
        }

        public Criteria andBusinessIdNotBetween(String value1, String value2) {
            addCriterion("business_id not between", value1, value2, "businessId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}