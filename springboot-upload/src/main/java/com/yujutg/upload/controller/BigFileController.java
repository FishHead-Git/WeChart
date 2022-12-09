package com.yujutg.upload.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.cache.selector.SimpleReadCacheSelector;
import com.alibaba.fastjson.JSON;
import com.yujutg.upload.entity.APIResult;
import com.yujutg.upload.entity.JsonResult;
import com.yujutg.upload.entity.MultipartFileParam;
import com.yujutg.upload.entity.Users;
import com.yujutg.upload.service.UserService;
import com.yujutg.upload.service.impl.UploadExcelListener;
import com.yujutg.upload.utils.PasswordEncoderUtils;
import com.yujutg.upload.websocket.WebSocketServer;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;

@Controller
@RequestMapping(value = "/bigfile")
public class BigFileController {

    private Logger logger = LoggerFactory.getLogger(BigFileController.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${uploadFilePath}")
    private String fileStorePath;

    /**
     * @Title: 判断文件是否上传过，是否存在分片，断点续传
     * @MethodName:  checkBigFile
     * @param fileMd5
     * @Return JsonResult
     * @Exception
     * @Description:
     *  文件已存在，下标为-1
     *  文件没有上传过，下标为零
     *  文件上传中断过，返回当前已经上传到的下标
     */
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult checkBigFile(String fileMd5) {

        JsonResult jr =  new JsonResult();
        // 秒传
        File mergeMd5Dir = new File(fileStorePath + "/" + "merge"+ "/" + fileMd5);
        if(mergeMd5Dir.exists()){
            mergeMd5Dir.mkdirs();
            jr.setResultCode(-1);//文件已存在，下标为-1
            return jr;
        }

        // 读取目录里的所有文件
        File dir = new File(fileStorePath + "/" + fileMd5);
        File[] childs = dir.listFiles();
        if(childs==null){
            jr.setResultCode(0);//文件没有上传过，下标为零
        }else{
            jr.setResultCode(childs.length-1);//文件上传中断过，返回当前已经上传到的下标
        }
        return  jr;
    }
    /**
     * 上传文件
     *
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public void filewebUpload(MultipartFileParam param, HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        // 文件名
        String fileName = param.getName();
        // 文件每次分片的下标
        int chunkIndex = param.getChunk();
        if (isMultipart) {
            File file = new File(fileStorePath + "/" + param.getMd5());
            if (!file.exists()) {
                file.mkdir();
            }
            File chunkFile = new File(
                    fileStorePath + "/" +  param.getMd5() + "/" + chunkIndex);
            try{
                FileUtils.copyInputStreamToFile(param.getFile().getInputStream(), chunkFile);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        logger.info("文件-:{}的小标-:{},上传成功",fileName,chunkIndex);
        return;
    }

    /**
     * 分片上传成功之后，合并文件
     * @param request
     * @return
     */
    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    @ResponseBody
    public APIResult filewebMerge(HttpServletRequest request, HttpServletResponse response) {
        FileChannel outChannel = null;
        try {
            String fileName = request.getParameter("fileName");
            String fileMd5 = request.getParameter("fileMd5");
            // 读取目录里的所有文件
            File dir = new File(fileStorePath + "/" + fileMd5);
            File[] childs = dir.listFiles();
            if(Objects.isNull(childs)|| childs.length==0){
                return null;
            }
            // 转成集合，便于排序
            List<File> fileList = new ArrayList<File>(Arrays.asList(childs));
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                        return -1;
                    }
                    return 1;
                }
            });
            // 合并后的文件
            File outputFile = new File(fileStorePath + "/" + "merge"+ "/" + fileMd5 + "/" + fileName);
            // 创建文件
            if(!outputFile.exists()){
                File mergeMd5Dir = new File(fileStorePath + "/" + "merge"+ "/" + fileMd5);
                if(!mergeMd5Dir.exists()){
                    mergeMd5Dir.mkdirs();
                }
                outputFile.createNewFile();
            }
            outChannel = new FileOutputStream(outputFile).getChannel();
            FileChannel inChannel = null;
            try {
                for (File file : fileList) {
                    inChannel = new FileInputStream(file).getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inChannel.close();
                    // 删除分片
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
                //发生异常，文件合并失败 ，删除创建的文件
                outputFile.delete();
                dir.delete();//删除文件夹
            }finally {
                if(inChannel!=null){
                    inChannel.close();
                }
            }
            dir.delete(); //删除分片所在的文件夹

            // 进行读取Excel操作


        } catch (IOException e) {
            e.printStackTrace();
            response.reset();
            return APIResult.FAIL();
        }finally {
            try {
                if(outChannel!=null){
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return APIResult.SUCCESS();
    }

    /**
     * 分片上传成功之后，合并文件
     * @param request
     * @return
     */
    @RequestMapping(value = "/mergeExcel", method = RequestMethod.POST)
    @ResponseBody
    public APIResult filewebMergeAndExcel(HttpServletRequest request, HttpServletResponse response) {
        FileChannel outChannel = null;
        try {
            String fileName = request.getParameter("fileName");
            String fileMd5 = request.getParameter("fileMd5");
            // 读取目录里的所有文件
            File dir = new File(fileStorePath + "/" + fileMd5);

            // 合并后的文件
            File outputFile = new File(fileStorePath + "/" + "merge"+ "/" + fileMd5 + "/" + fileName);
            File[] childs = dir.listFiles();
            if(Objects.isNull(childs)|| childs.length==0){
                if(!outputFile.exists()){
                    return APIResult.FAIL("file is not exist");
                }
            }else{
                // 转成集合，便于排序
                List<File> fileList = new ArrayList<File>(Arrays.asList(childs));
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                            return -1;
                        }
                        return 1;
                    }
                });
                // 创建文件
                if(!outputFile.exists()){
                    File mergeMd5Dir = new File(fileStorePath + "/" + "merge"+ "/" + fileMd5);
                    if(!mergeMd5Dir.exists()){
                        mergeMd5Dir.mkdirs();
                    }
                    outputFile.createNewFile();
                }
                outChannel = new FileOutputStream(outputFile).getChannel();
                FileChannel inChannel = null;
                try {
                    for (File file : fileList) {
                        inChannel = new FileInputStream(file).getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                        inChannel.close();
                        // 删除分片
                        file.delete();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    //发生异常，文件合并失败 ，删除创建的文件
                    outputFile.delete();
                    dir.delete();//删除文件夹
                }finally {
                    if(inChannel!=null){
                        inChannel.close();
                    }
                }
                dir.delete(); //删除分片所在的文件夹
            }

            // 进行读取Excel操作
            try {
                EasyExcel.read(outputFile,
                        Users.class,
                        new UploadExcelListener<Users>(
                                ()->{userService.truncateUser();return null;},
                                (maps, tList)-> {
                                    Users users = tList.pollLast();
                                    if(StringUtils.isEmpty(users.getUsername())||StringUtils.isEmpty(users.getPassword())){
                                        return users;
                                    }
                                    // 设置密码加密
                                    users.setPassword(PasswordEncoderUtils.encoder(users.getPassword()));
                                    tList.add(users);
                                    return null;
                                },
                                (vos, user)->userService.saveBatch(vos, "fishhead",1000),
                                (delVos, errVos)->{},
                                (errVos)->{
                                    webSocketServer.sendInfo("fishhead", JSON.toJSONString(APIResult.FAIL("有未通过校验数据",errVos)));
                                },
                                new HashMap<>(),
                                10000,
                                "fishhead",
                                webSocketServer
                        ))
                        .readCacheSelector(new SimpleReadCacheSelector(5, 20))
                        .sheet()
                        .doRead();
            } catch (Exception e) {
                e.printStackTrace();
                response.reset();
                return APIResult.FAIL();
            }

        } catch (IOException e) {
            e.printStackTrace();
            response.reset();
            return APIResult.FAIL();
        }finally {
            try {
                if(outChannel!=null){
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return APIResult.SUCCESS();
    }


    @PostMapping("/uploadAll")
    @ResponseBody
    public APIResult uploadCheckTime(MultipartFile file, HttpServletResponse response) {
        webSocketServer.sendInfo("fishhead",JSON.toJSONString(APIResult.SUCCESS(50)));
        logger.info("文件-:{}的大小-:{},上传成功",file.getOriginalFilename(),file.getSize());
        return APIResult.SUCCESS();
    }

    /**
     * 普通上传Excel文件处理
     * @param file
     * @param response
     * @return
     */
    @PostMapping("/uploadExcel")
    @ResponseBody
    public APIResult uploadAllFile(MultipartFile file, HttpServletResponse response) {
        webSocketServer.sendInfo("fishhead",JSON.toJSONString(APIResult.SUCCESS("read",1)));
        if(file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) return APIResult.FAIL();
        try {
            EasyExcel.read(file.getInputStream(),
                    Users.class,
                    new UploadExcelListener<Users>(
                            ()->{userService.truncateUser();return null;},
                            (maps, tList)-> {
                                Users users = tList.pollLast();
                                if(StringUtils.isEmpty(users.getUsername())||StringUtils.isEmpty(users.getPassword())){
                                    return users;
                                }
                                // 设置密码加密
                                users.setPassword(PasswordEncoderUtils.encoder(users.getPassword()));
                                tList.add(users);
                                return null;
                            },
                            (vos, user)->userService.saveBatch(vos, "fishhead",1000),
                            (delVos, errVos)->{},
                            (errVos)->{
                                webSocketServer.sendInfo("fishhead", JSON.toJSONString(APIResult.FAIL("有未通过校验数据",errVos)));
                            },
                            new HashMap<>(),
                            10000,
                            "fishhead",
                            webSocketServer
                    ))
                    .readCacheSelector(new SimpleReadCacheSelector(5, 20))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            return APIResult.FAIL();
        }
        return APIResult.SUCCESS();
    }
}