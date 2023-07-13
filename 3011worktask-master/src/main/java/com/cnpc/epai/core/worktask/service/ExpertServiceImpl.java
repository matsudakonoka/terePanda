package com.cnpc.epai.core.worktask.service;

import com.cnpc.epai.core.worktask.domain.ExpertContent;
import com.cnpc.epai.core.worktask.domain.Profession;
import com.cnpc.epai.core.worktask.domain.SrTaskTreeData;
import com.cnpc.epai.core.worktask.repository.ExpertContentRepository;
import com.cnpc.epai.core.worktask.repository.ProfessionRepository;
import com.cnpc.epai.core.worktask.util.RestPageImpl;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.aspectj.weaver.patterns.IfPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.awt.*;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpertServiceImpl implements ExpertService{

    @Autowired
    private ExpertContentRepository contentRepository;

    @Autowired
    private ProfessionRepository professionRepository;

    @Override
    public boolean saveExpertContent(ExpertContent expertContent) {
        contentRepository.save(expertContent);
        return true;
    }

    @Override
    public Page<ExpertContent> getExpertContent(String keyWord, Pageable pageable) {
        Page<ExpertContent> rtn = new RestPageImpl<>();
        Specification<ExpertContent>spec = new Specification<ExpertContent>() {
            @Override
            public Predicate toPredicate(Root<ExpertContent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (keyWord!=null && !keyWord.equals("")){
                    predicates.add(cb.or(cb.like(root.get("expertName"),"%"+keyWord+"%"),cb.like(root.get("expertUnit"),"%"+keyWord+"%"),cb.like(root.get("professionName"),"%"+keyWord+"%")));
                }

                Predicate[] predicateArray = new Predicate[predicates.size()];
                query.where(cb.and(predicates.toArray(predicateArray)));
                query.orderBy(cb.asc(root.get("createDate")));
                predicates.add(cb.equal(root.get("bsflag"),"N"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        rtn = contentRepository.findAll(spec,pageable);
        return rtn;
    }

    @Override
    public Profession saveProfession(Profession profession) {
        if (profession.getId()!=null){
            return professionRepository.save(profession);
        }else {
            List<Profession> list = new ArrayList<>();
            Specification<Profession> spec = new Specification<Profession>() {
                @Override
                public Predicate toPredicate(Root<Profession> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("unitName"),profession.getUnitName()));
                    predicates.add(cb.like(root.get("professionName"),profession.getProfessionName()));
                    predicates.add(cb.equal(root.get("bsflag"),"N"));
                    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            list = professionRepository.findAll(spec);
            if (list == null || list.size()==0){
                return professionRepository.save(profession);
            }
            return null;
        }
    }

    @Override
    public Map<String,Object> getProfession(String unit, String keyWord, String rule,Pageable pageable) {
        Map<String,Object> rtn = new HashMap<>();
//        String[] strings= {"createDate","professionName"};
//        if (rule.equals("asc")){
//            Sort sort = new Sort(Sort.Direction.DESC, "createDate").and(new Sort(Sort.Direction.ASC, "professionName"));
//            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
//        }else if (rule.equals("desc")){
//            Sort sort = new Sort(Sort.Direction.DESC, "createDate").and(new Sort(Sort.Direction.DESC, "professionName"));
//            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
//        }
        Specification<Profession> spec = new Specification<Profession>() {
            @Override
            public Predicate toPredicate(Root<Profession> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if(unit!=null && !unit.equals("")){
                    predicates.add(cb.equal(root.get("unitName"),unit));
                }
                if(keyWord!=null && !keyWord.equals("")){
                    predicates.add(cb.like(root.get("professionName"),"%"+keyWord+"%"));
                }
                predicates.add(cb.equal(root.get("bsflag"),"N"));
                Predicate[] predicateArray = new Predicate[predicates.size()];

                query.where(cb.and(predicates.toArray(predicateArray)));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        List<Profession> list = professionRepository.findAll(spec);
        Integer all = list.size();
        if (rule!=null && !rule.equals("")) {
            if (rule.equals("asc")) {
                Collections.sort(list, new Comparator<Profession>() {
                    @Override
                    public int compare(Profession o1, Profession o2) {
                        String o1Name = o1.getProfessionName().replace("（","");
                        o1Name = o1Name.replace("）","");
                        String newo1 = "";
                        newo1=ToPinyinAndGetFirstChar(o1Name);
                        String newo2 = "";
                        String o2Name = o2.getProfessionName().replace("（","");
                        o2Name = o2Name.replace("）","");
                        newo2=ToPinyinAndGetFirstChar(o2Name);
                        int i = newo1.substring(0, 1).compareTo(newo2.substring(0, 1));
                        return i;
                    }
                });
            } else if (rule.equals("desc")) {
                Collections.sort(list, new Comparator<Profession>() {
                    @Override
                    public int compare(Profession o1, Profession o2) {
                        String o1Name = o1.getProfessionName().replace("（","");
                        o1Name = o1Name.replace("）","");
                        String newo1 = "";
                        newo1=ToPinyinAndGetFirstChar(o1Name);
                        String newo2 = "";
                        String o2Name = o2.getProfessionName().replace("（","");
                        o2Name = o2Name.replace("）","");
                        newo2=ToPinyinAndGetFirstChar(o2Name);
                        int i = newo2.substring(0, 1).compareTo(newo1.substring(0, 1));
                        return i;
                    }
                });
            }
        }else {
            list = list.stream().sorted(Comparator.comparing(Profession::getCreateDate).reversed()).collect(Collectors.toList());
        }
        int strat = pageable.getPageSize()*pageable.getPageNumber();
        int end = pageable.getPageSize()*pageable.getPageNumber()+pageable.getPageSize();
        if (end>list.size()){
            list = list.subList(strat, list.size());
        }else {
            list = list.subList(strat, end);
        }
        rtn.put("content",list);
        rtn.put("number",pageable.getPageNumber());
        rtn.put("size",pageable.getPageSize());
        rtn.put("totalElements",all);
        rtn.put("last",false);
        rtn.put("sort",new ArrayList<>());
        if (all>(pageable.getPageNumber()+1)*pageable.getPageSize()) {
            rtn.put("totalPages", all / pageable.getPageSize()+1);
        }else {
            rtn.put("totalPages", all / pageable.getPageSize());
        }
        rtn.put("first",true);
        rtn.put("numberOfElements",list.size());
        return rtn;
    }

    public static String ToPinyinAndGetFirstChar(String chinese){
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }else{
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr.toUpperCase();
    }

    @Override
    public void delExpertContent(String id) {
        contentRepository.delete(id);
    }

    @Override
    public void delProfession(String id) {
        professionRepository.delete(id);
    }

    @Override
    public List<Map<String,Object>> getExpertProfessionList() {
        Map<String,List<Profession>> map = new HashMap<>();
        List<Profession> list = new ArrayList<>();
        List<Profession> list1 = new ArrayList<>();
        List<Profession> list2 = new ArrayList<>();
        List<Profession> list3 = new ArrayList<>();
        List<Profession> list4 = new ArrayList<>();
        Map<String,Object> map1 = new HashMap<>();
        Map<String,Object> map2 = new HashMap<>();
        Map<String,Object> map3 = new HashMap<>();
        Map<String,Object> map4 = new HashMap<>();
        List<Map<String,Object>> mapList = new ArrayList<>();
        Specification<Profession> spec = new Specification<Profession>() {
            @Override
            public Predicate toPredicate(Root<Profession> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("remarks"),"专家"));
                predicates.add(cb.equal(root.get("bsflag"),"N"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        list = professionRepository.findAll(spec);
        map = list.stream().collect(Collectors.groupingBy(Profession::getUnitName));

        for (String key:map.keySet()){
            if (key.equals("企业专家")){
                list1.addAll(map.get(key));
                map1.put("professionName",key);
                map1.put("children",list1);
                map1.put("id","1");
            }else if (key.equals("地面所")){
                list2.addAll(map.get(key));
                map2.put("professionName",key);
                map2.put("children",list2);
                map2.put("id","2");
            }else if (key.equals("采油气所")){
                list3.addAll(map.get(key));
                map3.put("professionName",key);
                map3.put("children",list3);
                map3.put("id","3");
            }else if (key.equals("钻井所")){
                list4.addAll(map.get(key));
                map4.put("professionName",key);
                map4.put("children",list4);
                map4.put("id","4");
            }
        }
        mapList.add(map1);
        mapList.add(map2);
        mapList.add(map3);
        mapList.add(map4);
        return mapList;
    }
}
