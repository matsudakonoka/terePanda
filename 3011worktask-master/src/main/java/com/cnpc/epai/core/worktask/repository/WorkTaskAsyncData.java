//package com.cnpc.epai.core.worktask.repository;
//
//import com.cnpc.epai.core.worktask.domain.WorkTask;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
////@EnableAsync
//@Service
//public class WorkTaskAsyncData {
//    @Autowired
//    DataSetRepository dataSetRepository;
////    @Async
////    public void sycIndexThread(String taskId, Map<String, Object> param, WorkTask task, String token) {
////        try {
////            dataSetRepository.syncIndex(task.getProjectId(), taskId, param, token);
////        }catch (Exception e){
////            e.printStackTrace();
////        }
////    }
//    public void sycIndexThread(String taskId, Map<String, Object> param, WorkTask task, String token) {
//        Thread t = new Thread(new Runnable(){
//            public void run(){
//                try{
//                    dataSetRepository.syncIndex(task.getProjectId(),taskId,param,token);
//
//                }catch(Exception e){
//                }
//            }
//        });
//        t.start();
//    }
//}
