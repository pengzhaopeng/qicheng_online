package com.pengzhaopeng.qc_logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pengzhaopeng.constans.GmallConstant;
import com.pengzhaopeng.utils.ParseJsonData;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

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
        log.info(jsonString);
        //推送到kafka
        if ("startup".equals(jsonObject.getString("type"))) {
            kafkaTemplate.send(GmallConstant.KAFKA_STARTUP,jsonString);
        }else{
            kafkaTemplate.send(GmallConstant.KAFKA_EVENT,jsonString);
        }

        return "success";
    }
}
