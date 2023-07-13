package com.cnpc.epai.core.worktask.service;

import com.alibaba.fastjson.JSONObject;
import com.cnpc.epai.common.util.ThreadContext;
import com.cnpc.epai.core.worktask.domain.Template;
import com.cnpc.epai.core.worktask.repository.TemplateRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
public class TemplateServiceImpl implements TemplateService{

    @Autowired
    TemplateRepository templateRepo;


    @Autowired
    private RestTemplate restTemplate;



    @Override
    public List<Template> getAll(String templateId,String templateName, Date startTime, Date endTime) {
        List<Template> rtnList = new ArrayList<>();
        Specification<Template> spec = new Specification<Template>() {
            @Override
            public Predicate toPredicate(Root<Template> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(StringUtils.isNotBlank(templateId)){
                    predicates.add(criteriaBuilder.equal(root.get("id"), templateId));
                }
                if(StringUtils.isNotBlank(templateName)){
                    predicates.add(criteriaBuilder.like(root.get("templateName"), "%"+templateName+"%"));
                }
                if (startTime!=null && endTime!=null){
                    predicates.add(criteriaBuilder.between(root.get("createDate"), startTime, endTime));
                }
                predicates.add(criteriaBuilder.equal(root.get("bsflag"), "N"));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        ThreadContext.putContext("currentSchema", "epai_crpadmin");
        rtnList = templateRepo.findAll(spec);
        for (Template template : rtnList){
            if (template.getDiscoverOrgs()!=null && template.getDiscoverOrgs()!="") {
                List<String> s = Arrays.asList(template.getDiscoverOrgs().split(","));
                template.setDiscoverOrgsIds(s);
            }
        }
        return rtnList;
    }

    @Override
    public Template saveTemplate(Template template) {
        if (template.getDiscoverOrgsIds()!=null && template.getDiscoverOrgsIds().size()>0){
            String dis = "";
            for (int i = 0;i<template.getDiscoverOrgsIds().size();i++){
                if (i==template.getDiscoverOrgsIds().size()-1){
                    dis = dis+template.getDiscoverOrgsIds().get(i);
                }else {
                    dis = dis+template.getDiscoverOrgsIds()+",";
                }
            }
            template.setDiscoverOrgs(dis);
        }
        Template template1 =templateRepo.save(template);
        template1 = templateRepo.getOne(template1.getId());
        return template1;
    }


}
