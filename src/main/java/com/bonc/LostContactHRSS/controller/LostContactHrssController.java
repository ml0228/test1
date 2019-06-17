package com.bonc.LostContactHRSS.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.LostContactHRSS.service.LostContactHrssServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * Created by malin on 2018/7/20.
 */

@RestController
public class LostContactHrssController{
    @Autowired
    private LostContactHrssServicesImpl lostContactHrssServices;

    private Logger log = Logger.getLogger(LostContactHrssController.class.getName());

    //接口一
    @RequestMapping(value = "/U01",method = RequestMethod.POST)
    public JSONObject U01(@RequestBody JSONObject U01){
        log.info("接收到参数，准备调用service");

        String serialid = U01.getString("serialid").trim();
        String templateid = U01.getString("templateid").trim();
        String type = U01.getString("type").trim();
        JSONArray listJson = U01.getJSONArray("list");
        log.info("转换list成功");
        String list = listJson.toJSONString();

        log.info("打印参数  serialid："+serialid+" templateid:"+templateid+"  type:"+type+"  list:"+list);

        JSONObject jsonResult = lostContactHrssServices.main_Hrss(serialid, templateid, type, list);
        log.info("返回参数，接口执行结束");
        return jsonResult;
    }

    //接口二
    @RequestMapping(value = "/U02",method = RequestMethod.POST)
    public JSONObject U02(@RequestBody JSONObject U02){

        String serialid = U02.getString("serialid");
        JSONObject jsonObject = lostContactHrssServices.lostIdno(serialid);
        return jsonObject;
    }

    //接口三
    @RequestMapping(value = "/U03",method = RequestMethod.POST)
    public JSONObject U03(@RequestBody JSONObject U03){
        String serialid = U03.getString("serialid");
        JSONObject jsonObject = lostContactHrssServices.contactIdno(serialid);
        return jsonObject;
    }



    /**
     * 测试用例
     * @return
     */
    @RequestMapping(value = "/addTest", method = RequestMethod.GET)
    public String test2(){

        return "------------访问成功------------";
    }


    /**
     * 最开始用的
     * application/x-www-form-urlencoded
     * 格式请求，不好用
     */

    //接口一
 /*   @RequestMapping(value = "/U01",method = RequestMethod.POST)
    public JSONObject U01(@RequestParam("serialid") String serialid, @RequestParam("templateid") String templateid, @RequestParam("type") String type, @RequestParam("list") String list){
        log.info("接收到参数，准备调用service");
        JSONObject jsonResult = lostContactHrssServices.main_Hrss(serialid, templateid, type, list);
        log.info("返回参数，接口执行结束");
        return jsonResult;
    }

    //接口二
    @RequestMapping(value = "/U02",method = RequestMethod.POST)
    public JSONObject U02(@RequestParam("serialid") String serialid){

        JSONObject jsonObject = lostContactHrssServices.lostIdno(serialid);
        return jsonObject;
    }

    //接口三
    @RequestMapping(value = "/U03",method = RequestMethod.POST)
    public JSONObject U03(@RequestParam("serialid") String serialid){
        JSONObject jsonObject = lostContactHrssServices.contactIdno(serialid);
        return jsonObject;
    }*/

}
