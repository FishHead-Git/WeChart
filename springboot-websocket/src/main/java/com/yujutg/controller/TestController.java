package com.yujutg.controller;

import com.alibaba.fastjson.JSON;
import com.yujutg.dto.RequestDTO;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class TestController {

    @GetMapping("/")
    public String toIndex(){
        return "notice";
    }

    @ResponseBody
    @PostMapping("/send")
    public Map<String, Object> sendMsg(@RequestBody RequestDTO dto){
        HashMap<String, Object> res = new HashMap<>();
        if(dto!=null&&
                !StringUtils.isEmpty(dto.getFromUser())&&
                !StringUtils.isEmpty(dto.getToUserId())&&
                !StringUtils.isEmpty(dto.getContentText())){
            res.put("code", 200);
            WebSocketServer.sendInfo(dto.getToUserId(), JSON.toJSONString(dto));
        }else{
            res.put("code", 400);
        }
        return res;
    }
}
