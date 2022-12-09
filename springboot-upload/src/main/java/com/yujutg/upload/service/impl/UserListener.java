package com.yujutg.upload.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.yujutg.upload.entity.Users;
import com.yujutg.upload.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class UserListener extends AnalysisEventListener<Users> {

    private UserService userService;
    private LinkedList<Users> users;
    private LinkedList<Users> errors;
    private final static int BATCH_LENGTH = 5000;
    private String userName;

    public UserListener(UserService userService, String userName) {
        this.userService = userService;
        this.userName = userName;
        this.users = new LinkedList<>();
        this.errors = new LinkedList<>();
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        log.error("read Excel: {}", exception.getMessage());
    }

    @Override
    public void invoke(Users user, AnalysisContext analysisContext) {
        if(!hasNullColumns(user)){
            users.add(user);
        }
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
//        log.info("hasNext: {}", context.readRowHolder().getCurrentRowAnalysisResult());
        return super.hasNext(context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("success: {}", users.size());
        doSave();
    }

    private void doSave() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        userService.truncateUser();

        List<Future<List<Users>>> errorList = new LinkedList<>();
        int len = (users.size() / BATCH_LENGTH)+1;
        for (int i = 0; i < len; i++) {
            List<Users> collect = users.stream().skip((long) i * BATCH_LENGTH).limit(BATCH_LENGTH).collect(Collectors.toList());
            if(collect.size()>0)
                errorList.add(userService.saveBatch(collect, userName, 1000));
        }

        final Iterator<Future<List<Users>>> iterator = errorList.iterator();
        while(iterator.hasNext()){
            try {
                final List<Users> users = iterator.next().get();
                if(users!=null) errors.addAll(users);
                // 可以统计上传进度
            } catch (Exception e) {
//                e.printStackTrace();
                log.error("future error: {}", e.getMessage());
            }
        }
        stopWatch.stop();
        log.info("finish: {}, errorVos: {}", stopWatch.getTotalTimeMillis()/1000.0, errors.size());
    }

    private Boolean hasNullColumns(Users user){
        return user==null ||
                user.getId() == null ||
                StringUtils.isEmpty(user.getUsername()) ||
                StringUtils.isEmpty(user.getPassword());
    }
}
