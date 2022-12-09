package com.yujutg.upload.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {


    @GetMapping("/")
    public String toIndex(){
        return "index";
    }

    @GetMapping("/bigFile")
    public String toBigFile(){
        return "bigFile";
    }
    @GetMapping("/notice")
    public String toNotice(){
        return "notice";
    }

}
