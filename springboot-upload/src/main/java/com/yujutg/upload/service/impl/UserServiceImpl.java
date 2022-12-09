package com.yujutg.upload.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yujutg.upload.entity.Users;
import com.yujutg.upload.mapper.UserMapper;
import com.yujutg.upload.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void truncateUser() {
        userMapper.truncateUser();
    }

    @Async("asyncServiceExecutor")
    @Override
    public Future<List<Users>> saveBatch(List<Users> users, String userName, int batchSize) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try{
            int BATCH_LENGTH = batchSize;
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            int len = (users.size() / BATCH_LENGTH)+1;
            for (int i = 0; i < len; i++) {
                List<Users> collect = users.stream().skip((long) i * BATCH_LENGTH).limit(BATCH_LENGTH).collect(Collectors.toList());
                if(collect.size()>0)
                    mapper.insertBatchByUser(collect);
            }
            sqlSession.commit();
            stopWatch.stop();
            log.info("thread: {}, time: {}s", Thread.currentThread().getName(), stopWatch.getTotalTimeMillis());
            return new AsyncResult<>(null);
        } catch (Exception e){
            sqlSession.rollback();
            stopWatch.stop();
            e.printStackTrace();
            log.info("thread: {}, time: {}s ,rollback: {}", Thread.currentThread().getName(), stopWatch.getTotalTimeMillis(), e.getMessage());
        }finally {
            sqlSession.close();
        }
        return new AsyncResult<>(users);
    }
}
