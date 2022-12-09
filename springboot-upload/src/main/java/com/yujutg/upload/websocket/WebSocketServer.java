package com.yujutg.upload.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/api/msg/{userId}")
public class WebSocketServer {

    // 对在线用户统计
    private static AtomicInteger onLineCount = new AtomicInteger();
    // 存储socket连接
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String userId = "";

    /**
     * 与前端ws建立连接 ws://localhost/...
     * @param session
     * @param userid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userid){
        this.session = session;
        this.userId = userid;
        if(webSocketMap.containsKey(userid)){
            webSocketMap.remove(userid);
            webSocketMap.put(userid, this);
        }else{
            webSocketMap.put(userid, this);
            onLineCount.incrementAndGet();
        }
        log.info("user: {}, online: {}", userid, onLineCount.get());
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(){
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            onLineCount.decrementAndGet();
        }
        log.info("user logout: {}, online: {}", userId, onLineCount.get());
    }

    /**
     * 出错打印
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        log.error("user error: {}, msg: {}", userId, error.getMessage());
        error.printStackTrace();
    }

    /**
     * 接收消息进行转发
     * @param msg
     * @param session
     */
    @OnMessage
    public void onMessage(String msg, Session session){
        log.info("user: {}, msg: {}", userId, msg);
        if(!StringUtils.isEmpty(msg)){
            JSONObject jsonObject = JSON.parseObject(msg);
            String toUserId = jsonObject.getString("toUserId");
            if(!StringUtils.isEmpty(toUserId) && webSocketMap.containsKey(toUserId)){
                webSocketMap.get(toUserId).sendMessage(msg);
            }else{
                log.error("user is not exist in this server: {}", toUserId);
            }
        }
    }

    public void sendMessage(String msg){
        try {
            this.session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(String userId, String msg){
        log.info("send: {}, msg: {}", userId, msg);
        if(!StringUtils.isEmpty(userId)&&webSocketMap.containsKey(userId)){
            webSocketMap.get(userId).sendMessage(msg);
        }else{
            log.info("user[{}] isn't exist", userId);
        }
    }

}
