package com.pengzhaopeng.qc_logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pengzhaopeng.utils.ParseJsonData;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * description
 * author 鹏鹏鹏先森
 * date 2020/5/20 23:44
 * Version 1.0
 */
@RestController
@Slf4j
public class LoggerController {

    @GetMapping("testDemo")
    public String testDemo() {
        return "狗子";
    }

    @PostMapping("/log")
    public String doLog(@RequestParam("logString") String logString) {

        //补充时间戳
        JSONObject jsonObject = JSON.parseObject(logString);
        jsonObject.put("ts",System.currentTimeMillis());
        //落盘
        String jsonString = jsonObject.toJSONString();
        log.info(jsonObject.toJSONString());

        return "success";
    }
}
