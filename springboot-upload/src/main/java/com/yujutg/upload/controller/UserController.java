package com.yujutg.upload.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.cache.selector.SimpleReadCacheSelector;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yujutg.upload.dto.RequestDTO;
import com.yujutg.upload.entity.APIResult;
import com.yujutg.upload.entity.PageDTO;
import com.yujutg.upload.entity.Users;
import com.yujutg.upload.service.UserService;
import com.yujutg.upload.service.impl.UploadExcelListener;
import com.yujutg.upload.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${uploadFilePath}")
    public static String FILE_PATH;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketServer webSocketServer;

    @PostMapping("/")
    public APIResult getList(@RequestBody PageDTO pageDTO){
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        if(pageDTO.getPageCondition()!=null && !"".equals(pageDTO.getPageCondition())){
            wrapper.eq("user_name", pageDTO.getPageCondition());
        }
        return APIResult.SUCCESS(userService.page(new Page<Users>(pageDTO.getPageNo(), pageDTO.getPageSize()), wrapper));
    }

    @PostMapping("/send")
    public APIResult sendMsg(@RequestBody RequestDTO dto){
        if(dto!=null&&
                !StringUtils.isEmpty(dto.getFromUser())&&
                !StringUtils.isEmpty(dto.getToUserId())&&
                !StringUtils.isEmpty(dto.getContentText())){
            webSocketServer.sendInfo(dto.getToUserId(), JSON.toJSONString(dto));
            return APIResult.SUCCESS();
        }else{
            return APIResult.FAIL();
        }
    }

    @PostMapping("/upload")
    public APIResult uploadAllFile(MultipartFile file, HttpServletResponse response) {
        if(file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) return APIResult.FAIL();
        log.info("uploadAllFile-> name: {}, size: {}", file.getName(), file.getSize());
        try {
            EasyExcel.read(file.getInputStream(),
                    Users.class,
                    new UploadExcelListener<Users>(
                            ()->{return null;},
                            (maps, tList)->null,
                            (vos, user)->userService.saveBatch(vos, "user",1000),
                            (delVos, errVos)->{},
                            (errVos)->{},
                            new HashMap<>(),
                            10000,
                            "user",
                            webSocketServer
                    ))
                    .readCacheSelector(new SimpleReadCacheSelector(5, 20))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            log.error("read Excel: {}", e.getMessage());
            response.reset();
            return APIResult.FAIL();
        }
        return APIResult.SUCCESS();
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response){
        String path = "User"+ UUID.randomUUID().toString().replace("-", "") +".xlsx";
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + path);
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), Users.class)
                    .registerConverter(new LongStringConverter())
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("user")
                    .doWrite(userService.list());
        } catch (Exception e) {
            // 重置response
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            try {
                response.getWriter().println(new Gson().toJson(map));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
