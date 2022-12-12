package com.yujutg.upload.mapper;

import com.yujutg.upload.entity.Users;
import com.yujutg.upload.service.UserService;
import com.yujutg.upload.utils.PasswordEncoderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserMapperTest {

    private static TreeMap<String, Object> timeMap = new TreeMap<>();
    private LinkedList<Users> userList = new LinkedList<>();

    @Autowired
    private UserService userService;

    // {10000-1000=[19015, 20041, 20064, 22606, 25417], 10000-2000=[23234, 21035, 21668, 21277, 23552], 20000-1000=[16638, 19192, 21810, 23766, 20956], 20000-2000=[20115, 19844, 22814, 18980, 24198]}
    @BeforeEach
    void setUp() {
        userList = new LinkedList<>();
        int size = 500000;
        for (int i = 0; i < size; i++) {
            Users users = new Users();
            users.setId(Long.valueOf(i));
            users.setUsername("user"+i);
            users.setPassword(PasswordEncoderUtils.encoder("user"+i));
            String email = UUID.randomUUID().toString().replace("-", "");
            users.setEmail(email.substring(0, 7)+"@"+email.substring(7,10)+"."+email.substring(10, 13));
            users.setMoney(BigDecimal.ZERO);
            userList.add(users);
        }
        userService.truncateUser();
        log.info("init...");
    }

    @AfterEach
    void tearDown() {
        log.info("{}", timeMap);
    }

    @AfterAll
    public static void after(){
        log.info("{}", timeMap);
    }

    @RepeatedTest(1)
    void insertBatchByUserThousand() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Future<List<Users>>> resList = new LinkedList<>();
        int everyBatch = 1000, batch = 10000;
        for (int i = 0; i < userList.size() / batch; i++) {
            List<Users> collect = userList.stream().skip(i * batch).limit(batch).collect(Collectors.toList());
            if(collect.size()>0)
                resList.add(userService.saveBatch(collect, "fishhead", everyBatch));
        }

        final Iterator<Future<List<Users>>> iterator = resList.iterator();
        while(iterator.hasNext()){
            try {
                iterator.next().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopWatch.stop();
        LinkedList orDefault = (LinkedList)timeMap.getOrDefault(batch + "-" + everyBatch, new LinkedList<Long>());
        orDefault.add(stopWatch.getTotalTimeMillis());
        timeMap.put(batch + "-" + everyBatch, orDefault);
    }

    @RepeatedTest(1)
    void insertBatchByUserThousand2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Future<List<Users>>> resList = new LinkedList<>();
        int everyBatch = 1000, batch = 20000;
        for (int i = 0; i < userList.size() / batch; i++) {
            List<Users> collect = userList.stream().skip(i * batch).limit(batch).collect(Collectors.toList());
            if(collect.size()>0)
                resList.add(userService.saveBatch(collect, "fishhead", everyBatch));
        }

        final Iterator<Future<List<Users>>> iterator = resList.iterator();
        while(iterator.hasNext()){
            try {
                iterator.next().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopWatch.stop();
        LinkedList orDefault = (LinkedList)timeMap.getOrDefault(batch + "-" + everyBatch, new LinkedList<Long>());
        orDefault.add(stopWatch.getTotalTimeMillis());
        timeMap.put(batch + "-" + everyBatch, orDefault);
    }

    @RepeatedTest(1)
    void insertBatchByUserThousand3() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Future<List<Users>>> resList = new LinkedList<>();
        int everyBatch = 2000, batch = 10000;
        for (int i = 0; i < userList.size() / batch; i++) {
            List<Users> collect = userList.stream().skip(i * batch).limit(batch).collect(Collectors.toList());
            if(collect.size()>0)
                resList.add(userService.saveBatch(collect, "fishhead", everyBatch));
        }

        final Iterator<Future<List<Users>>> iterator = resList.iterator();
        while(iterator.hasNext()){
            try {
                iterator.next().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopWatch.stop();
        LinkedList orDefault = (LinkedList)timeMap.getOrDefault(batch + "-" + everyBatch, new LinkedList<Long>());
        orDefault.add(stopWatch.getTotalTimeMillis());
        timeMap.put(batch + "-" + everyBatch, orDefault);
    }

    @RepeatedTest(1)
    void insertBatchByUserThousand4() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Future<List<Users>>> resList = new LinkedList<>();
        int everyBatch = 2000, batch = 20000;
        for (int i = 0; i < userList.size() / batch; i++) {
            List<Users> collect = userList.stream().skip(i * batch).limit(batch).collect(Collectors.toList());
            if(collect.size()>0)
                resList.add(userService.saveBatch(collect, "fishhead", everyBatch));
        }

        final Iterator<Future<List<Users>>> iterator = resList.iterator();
        while(iterator.hasNext()){
            try {
                iterator.next().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopWatch.stop();
        LinkedList orDefault = (LinkedList)timeMap.getOrDefault(batch + "-" + everyBatch, new LinkedList<Long>());
        orDefault.add(stopWatch.getTotalTimeMillis());
        timeMap.put(batch + "-" + everyBatch, orDefault);
    }
}