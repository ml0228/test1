package com.bonc.LostContactHRSS.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.LostContactHRSS.mapper.orcal.OrcalMapper;
import com.bonc.LostContactHRSS.mapper.xcloud.XcloudMapper;
import com.bonc.LostContactHRSS.thread.ThreadSendMessage;
import com.bonc.LostContactHRSS.util.DataBaseUtil;
import com.bonc.LostContactHRSS.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by malin on 2018/9/7.
 */
@Service
public class LostContactHrssServicesImpl {

    private Logger log = Logger.getLogger(LostContactHrssServicesImpl.class.getName());

    @Autowired
    private OrcalMapper orcalMapper;
    @Autowired
    private XcloudMapper xcloudMapper;
    @Autowired
    private DateUtil dateUtil;

    @Value("${db_schema_epm_test}")
    private String db_schema_epm_test;

    @Value("${is_test}")
    private String is_test;


    //
    public JSONObject main_Hrss(String serialid,String templateid,String type,String list){


        log.info("进入service方法");
        log.info("打印参数  serialid："+serialid+" templateid:"+templateid+"  type:"+type+"  list:"+list);
        JSONObject jsonResult = new JSONObject();
        JSONArray jsonArray;
        //数据校验
        int i= 0;
        try{
            i = orcalMapper.existSerialid(serialid);
        }catch (Exception e){
            e.printStackTrace();
            log.warning("调用orcal失败");
        }

        if(serialid.isEmpty()||templateid.isEmpty()||type.isEmpty()||list.isEmpty()||i>0){
            jsonResult.put("status","02");
            jsonResult.put("statusDesc","参数格式不正确(有参数为空或者任务流水号重复)");
            log.info("参数格式不正确(有参数为空或者任务流水号重复)");
            return jsonResult;
        }else {

            try{
                jsonArray = JSON.parseArray(list);
            }catch (Exception e){
                jsonResult.put("status","02");
                jsonResult.put("statusDesc","参数格式不正确（包含的list不是标准的json格式）");
                log.info("参数格式不正确(包含的list不是标准的json格式)");
                return jsonResult;
            }

        }

        log.info("参数校验基本完成");



        HashMap<String,String> dataMap = new HashMap<>();
        dataMap.put("serialid",serialid.trim());
        dataMap.put("templateid",templateid.trim());
        dataMap.put("type",type.trim());
        dataMap.put("list",list.trim());

/*----------------------------------------------------测试用模块-----------------------------*/

        log.info("准备校验是否是测试");
        log.info("校验值："+is_test);
        if(is_test.equals("1")){
            //如果本次在测试，调用测试模块
            log.info("判定为测试模式");
            boolean test = testModel(list);
            if(!test){
                jsonResult.put("status","02");
                jsonResult.put("statusDesc","参数格式不正确（测试时包含非法的身份证加密）");
                log.info("参数格式不正确（测试时包含非法的身份证加密）");
                return jsonResult;
            }

        }
/*----------------------------------------------------测试用模块-----------------------------*/

        log.info("接收信息，插入到行云任务表");

        /**
         * 1、接收信息，插入到任务表
         *
         */
        String sql = "insert into JINGZHUNHUA.API_LOSTCONTACT_HRSS " +
                "SELECT '"+serialid+"','"+templateid+"','"+type+"','"+dateUtil.getNowTime()+"'" +
                "FROM API_LOSTCONTACT_HRSS LIMIT(1, 1)";

        String sql2 = "insert into lost_contact_hrss_log values('"+serialid+"','"+templateid+"','"+type+"','"+dateUtil.getNowTime()+"')";
        Map<String,String> map = new HashMap<String,String>();
        map.put("serialid",serialid);
        map.put("templateid",templateid);
        map.put("type",type);
        map.put("time",dateUtil.getNowTime());

        log.info("准备记录插入行云");
        orcalMapper.addLog2(map);

        log.info("行云插入成功，准备插入orcal");
        xcloudMapper.useXcloud(sql);
        log.info("插入成功");

        jsonResult.put("status","00");
        jsonResult.put("statusDesc","接收成功");


        try{
            log.info("使用线程完成短信发送");
            //使用线程完成短信发送
            Thread t1 = new Thread(new ThreadSendMessage(dataMap,dateUtil,xcloudMapper,orcalMapper,db_schema_epm_test));
            t1.start();
        }catch (Exception e){
            jsonResult.put("status","01");
            jsonResult.put("statusDesc","服务端错误");
        }



        return jsonResult;
    }




    /**
     * 查询未复联用户
     * {"mismatch":[ "210610199701055125"]}
     */
    public JSONObject lostIdno(String serialid){

        /**
         * 查询出未复联的用户的身份证加密
         *
         */

        JSONObject resultJson = new JSONObject();
        JSONArray idnos = new JSONArray();



        String sql = "select idnomd5 from API_LOSTCONTACT_HRSS_LOG where SERIALID = '"+serialid+"' and is_contact = '0'";
        List<Map<String, String>> maps = xcloudMapper.useXcloud(sql);
        for(int i = 0 ;i< maps.size() ; i++){
            idnos.add(maps.get(i).get("IDNOMD5"));
        }
        resultJson.put("mismatch",idnos);

        return resultJson;
    }

    /**
     * 查询出已复联的用户的身份证加密
     * 返回参数短信流水号，身份证加密，状态，状态放入list   ，整体返回一个jsonArray
     */
    public JSONObject contactIdno(String serialid){


        JSONObject resultJson = new JSONObject();
        JSONArray listArray = new JSONArray();



        String sql = "select * from API_LOSTCONTACT_HRSS_LOG where SERIALID = '"+ serialid +"'  and is_contact = '1' order by idnomd5";

        List<Map<String, String>> maps = xcloudMapper.useXcloud(sql);

        String tempIdnomd5 = "";
        JSONObject jsonItem = new JSONObject();
        JSONArray statusList = new JSONArray();

        for(int i = 0 ;i < maps.size() ; i++){
            String idnomd5 = maps.get(i).get("IDNOMD5");
            String is_send_message = maps.get(i).get("IS_SEND_MESSAGE");
            if(!idnomd5.equals(tempIdnomd5)){
                tempIdnomd5 = idnomd5;
                if(i!=0){
                    int size = statusList.size();
                    jsonItem.put("num",size+"");
                    jsonItem.put("status",statusList);
                    listArray.add(jsonItem);
                }


                String message_number = maps.get(i).get("MESSAGE_NUMBER");

                jsonItem = new JSONObject();
                statusList = new JSONArray();

               jsonItem.put("id",message_number);
               jsonItem.put("idno",idnomd5);

               if(is_send_message.equals("1")){
                   statusList.add("SUCCESS");
               }else {
                   statusList.add("FAILED");
               }

                //最后一次将数据put至 listArray
                if(i==maps.size()-1){
                    int size = statusList.size();
                    jsonItem.put("num",size+"");
                    jsonItem.put("status",statusList);
                    listArray.add(jsonItem);
                }

            }else {
                if(is_send_message.equals("1")){
                    statusList.add("SUCCESS");
                }else {
                    statusList.add("FAILED");
                }
                //最后一次将数据put至 listArraay
                if(i==maps.size()-1){
                    int size = statusList.size();
                    jsonItem.put("num",size+"");
                    jsonItem.put("status",statusList);
                    listArray.add(jsonItem);
                }

            }
        }
        //装完list 将list放入jsonobject
        resultJson.put("list",listArray);

        return resultJson;
    }

    public boolean testModel(String list){
        //添加测试环境可以使用的身份证加密
        log.info("进入测试模块校验方法");
        Set<String> set = new HashSet<String>();
/*
        set.add("00001975F964B966F16D286879C869BB");
        set.add("00003122DD0A8551BE09B3F9F60D069D");
        set.add("00005AD9855741CA28AC9E23CEF7EA62");*/

//马林，王楠楠， 杨春
        set.add("D4654A07C5DC5E04F093D6126BAE4B4E");
        set.add("5404787323061D07262383D8FB9756FA");
        set.add("1E14141F8F85A8EFDFBA7C13E80C7B46");

//甲方提供的10个idno
        set.add("2FF4FA58AC10D42637F4472F55D064D7");
        set.add("70DBA070149D77A1B189B4387D51A6C4");
        set.add("510AD02780DF707BADC2A28E09620A09");
        set.add("8B2FA97FCF2EE929559E399793104969");
        set.add("5FBB306C627873876FC7ADE72C7582ED");
        set.add("BE50CE012D48F86F51E41B1F4B04F816");
        set.add("98398C1C17F8827B2DBC16A6E8CA360E");
        set.add("77ECF4D5CE4EB8152ECC3D9DE4277691");
        set.add("3E5C7840172BCE369C6D10BABB127BDE");
        set.add("A0942BBA5FEF10CB61DF8B4888B89FC0");


        JSONArray jsonArraytest = JSON.parseArray(list);
        log.info("测试方法转换jsonarray成功");

        for(int i = 0 ; i < jsonArraytest.size() ; i++){
            JSONObject json = (JSONObject)jsonArraytest.get(i);

            if (json.getString("idno").length()>15){
                boolean contains = set.contains(json.getString("idno"));
                if (!contains){
                    return false;
                }
            }
        }
        log.info("测试模块结束");
        return true;
    }

}
