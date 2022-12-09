package com.yujutg.upload.service.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.yujutg.upload.entity.APIResult;
import com.yujutg.upload.websocket.WebSocketServer;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class UploadExcelListener<T> extends AnalysisEventListener<T> {

    private LinkedList<T> tVos;
    private LinkedList<T> errors;
    private int BATCH_LENGTH = 10000;
    private HashMap<String, Object> maps;
    private String userName;
    private Supplier<List<T>> beforAction;
    private Consumer<List<T>> errorAction;
    private BiFunction<HashMap<String, Object>, LinkedList<T>, T> invokeHandler;
    private BiFunction<List<T>, String, Future<List<T>>> writeHandler;
    private BiConsumer<List<T>, List<T>> afterAction;
    private WebSocketServer server;

    /**
     * 公用上传Excel方法
     * @param beforAction       前置操作
     * @param invokeHandler     判断是否记入
     * @param writeHandler      写入操作
     * @param afterAction       后置操作
     * @param errorAction       错误操作
     * @param maps              预先加载数据
     * @param batchSize         分批调用写入操作
     * @param userName          socket的连接人名称
     */
    public UploadExcelListener(Supplier<List<T>> beforAction,
                               BiFunction<HashMap<String, Object>, LinkedList<T>, T> invokeHandler,
                               BiFunction<List<T>, String, Future<List<T>>> writeHandler,
                               BiConsumer<List<T>, List<T>> afterAction,
                               Consumer<List<T>> errorAction,
                               HashMap<String, Object> maps,
                               int batchSize,
                               String userName,
                               WebSocketServer server) {
        this.userName = userName;
        this.beforAction = beforAction;
        this.invokeHandler = invokeHandler;
        this.writeHandler = writeHandler;
        this.afterAction = afterAction;
        this.errorAction = errorAction;
        this.BATCH_LENGTH = batchSize;
        this.server = server;
        this.tVos = new LinkedList<>();
        this.errors = new LinkedList<>();
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
//        log.error("read Excel: {}", exception.getMessage());
    }

    @Override
    public void invoke(T vo, AnalysisContext analysisContext) {
        tVos.add(vo);
        T t = invokeHandler.apply(maps, tVos);
        if(t!=null) errors.add(vo);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
//        log.info("success: {}, error: {}", tVos.size(), errors.size());
        if(errors.size()>0){
            errorAction.accept(errors);
        }else
            doSave();
    }

    private void doSave() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<T> deleteVos = beforAction.get();

        List<Future<List<T>>> resList = new LinkedList<>();
        int len = (tVos.size() / BATCH_LENGTH)+1;
        for (int i = 0; i < len; i++) {
            List<T> collect = tVos.stream().skip((long) i * BATCH_LENGTH).limit(BATCH_LENGTH).collect(Collectors.toList());
            if(collect.size()>0)
                resList.add(writeHandler.apply(collect, userName));
        }

        final Iterator<Future<List<T>>> iterator = resList.iterator();
        List<T> err = null;
        for(int i=0;iterator.hasNext();i++){
            try {
                err = iterator.next().get();
                if(err!=null) errors.addAll(err);
                // 可以统计上传进度
                server.sendInfo(userName, JSON.toJSONString(APIResult.SUCCESS("process", ((double)i/resList.size()))));
            } catch (Exception e) {
                e.printStackTrace();
//                log.error("future error: {}", e.getMessage());
            }
        }
        stopWatch.stop();
//        log.info("finish: {}, errorVos: {}", stopWatch.getTotalTimeMillis()/1000.0, errors.size());

        afterAction.accept(deleteVos, errors);
    }
}