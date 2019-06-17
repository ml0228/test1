package com.bonc.LostContactHRSS.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.LostContactHRSS.controller.LostContactHrssController;
import com.bonc.LostContactHRSS.entity.SmsModel;
import com.bonc.LostContactHRSS.mapper.orcal.OrcalMapper;
import com.bonc.LostContactHRSS.mapper.xcloud.XcloudMapper;
import com.bonc.LostContactHRSS.util.DateUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by malin on 2018/9/7.
 * 该线程用来发送短信并记录结果
 */
@Scope("prototype")
@Component
public class ThreadSendMessage extends Thread{
    private Logger log = Logger.getLogger(ThreadSendMessage.class.getName());
    private OrcalMapper orcalMapper;

    private XcloudMapper xcloudMapper;

    private DateUtil dateUtil;



    private HashMap<String,String> dataMap;
    private String serialid;
    private String templateid;
    private String type;
    private String list;
    private String db_schema_epm_test;




    public ThreadSendMessage(HashMap<String,String> data,DateUtil dateUtil,XcloudMapper xcloudMapper,OrcalMapper orcalMapper,String db_schema_epm_test){

        this.dataMap = data;
        this.serialid = data.get("serialid");
        this.templateid = data.get("templateid");
        this.type = data.get("type");
        this.list = data.get("list");

        this.dateUtil = dateUtil;
        this.orcalMapper = orcalMapper;
        this.xcloudMapper = xcloudMapper;
        this.db_schema_epm_test = db_schema_epm_test;


    }
    @Transactional
    @Override
    public void run(){
        log.info("启动线程发送短信");
        /**
         * 1、数据详情插入orcal
         */

        log.info("将数据插入orcal任务表");
        addorcalHrssMession();


        /**
         * 2、生成事实表
         */

        log.info("将身份证id注入行云生成事实表");
        String factTable = createFactTable();

        /**
         * 复联数据返回复联表
         */

        log.info("复联出全部数据返回复联表");
        addContactTable(factTable);

        /**
         * 生成结果表最终数据
         */

        log.info("根据需求类型，将需要发送的数据导入结果表");
        addResult(factTable);

        /**
         * 发送短信
         */

        sendMessage();

        /**
         * 短信完成后导入行云
         */

        Boolean aBoolean = addXcloudLog();

        /**
         * 导入行云成功后清空表
         *
         */

        if(aBoolean){
            //插入日志表成功后删除 任务表，复联表，结果表对应的数据
            try{
                clearTable(factTable);
                log.info("orcal数据清除成功");
                log.info("-----------------流程执行完成-----------");

            }catch(Exception e){
                log.warning("orcal数据清除失败");
                e.printStackTrace();
            }

        }else {
            log.warning("本批数据执行有问题，请检查，数据任务流水号"+this.serialid);
        }

        log.info("线程执行结束");

    }

    /**
     * 吧来的信息插入任务表
     */
    public void addorcalHrssMession(){
        JSONArray jsonArray = JSON.parseArray(list);
        for (int i = 0 ; i < jsonArray.size() ; i++){
            //取出list里面每个单位的内容 插入数据库
            JSONObject personData = (JSONObject) jsonArray.get(i);
            String id = personData.getString("id");
            String idno = personData.getString("idno");
            String name = personData.getString("name");
            String variables = personData.getString("variables");
            Map<String,String> map = new HashMap<String,String>();


            /*map.put("dateBaseName",dateBaseUtil.jzyx_eam);*/
            map.put("serialid",this.serialid);
            map.put("templateid",this.templateid);
            map.put("type",this.type);
            map.put("id",id);
            map.put("idno",idno);
            map.put("name",name);
            map.put("variables",variables);
            map.put("time",dateUtil.getNowTime());

            if(orcalMapper.existIdno(map)!=0){
                continue;
            }else {
                orcalMapper.addMessionData(map);
            }

        }

    }

    /**
     * 生成事实码表并插入数据 用来join 出复联用户
     */

    public String createFactTable(){
        //生成事实表名称
        String factTableName = "TABLE_IMPORT_HRSS_"+dateUtil.getDateFlag();
        //生成事实表
        createTable(factTableName);
        //事实表插入数据
        /**
         *
         */
        String sql = "insert into "+factTableName+
                " {select distinct (idnomd5) from "+db_schema_epm_test+".lost_contact_hrss_mession where serialid='"+this.serialid+"' }@jingzhunhua";
        xcloudMapper.useXcloud(sql);
        return factTableName;
    }

    /**
     * 执行复联用户，将数据插入orcal复联表
     */
    public List<Map<String,String>> addContactTable(String factTableName){
        // 1、获取当前最大月份账期

        String MaxMonthSql = getLMaxMonth();
        List<Map<String, String>> maps = xcloudMapper.useXcloud(MaxMonthSql);

        String monthMax = maps.get(0).get("MONTHID");
        /*monthMax = "201808";*/


        // 2、复联所有数据插入复联表
        String sqlAllData = "SELECT  DISTINCT(a.device_number) AS phoneNum ,a.JF_TIMES as callTime , a.INNET_MONTHS_NEW as onlineTime ,f.fieldname AS idNo, '"+factTableName+"' as factTableName FROM DM_V_D_CUS_ALL_BASIC a JOIN "+factTableName+" f ON a.cert_no = f.fieldname where a.month_id = '"+monthMax+"' and a.is_nomal_open = 1";

        //行云取别名无效
        List<Map<String, String>> allDataList = xcloudMapper.useXcloud(sqlAllData);
        if (allDataList.size()>0){
            orcalMapper.addPhoneData(allDataList);
        }else {
            log.info("-------------无复联的用户-----------------");
        }

        return null;
    }

    /**
     * 根据要求将最终结果存入结果表备用
     */
    public void addResult(String factTableName){
        /**
         * 插入未复联用户
         */

        Map<String,String> map = new HashMap<String,String>();
        map.put("serialid",serialid);
        map.put("facttablename",factTableName);
        orcalMapper.addResultDataLost(map);

        /**
         * 插入复联用户
         */
        if(type.equals("0")){
            orcalMapper.addResultDataAllUser(map);
        }else if(type.equals("1")){
            orcalMapper.addResultDataMaxOnlineUser(map);
        }else if(type.equals("2")){
            orcalMapper.addResultDataMaxCallUser(map);
        }else {
            orcalMapper.addResultDataAllUser(map);
        }

    }

    /**
     * 复联用户发短信,成功后更新发送记录
     */
    public void sendMessage(){
        String smStemplate = orcalMapper.getSMStemplate(this.templateid);
        SmsModel smsModel = new SmsModel();
        smsModel.setDepartTypeId("38");
        smsModel.setSmsType("0");
        smsModel.setShowNum("10655191012333");
        smsModel.setMsgSuffix("");

        //从结果表取出已经复联数据准备发送短信
        List<Map<String,String>> getResultData = orcalMapper.getResultData(this.serialid);

        for(int i = 0 ; i <getResultData.size();i++ ){
            //取出其中一条数据
            Map<String, String> oneDataMap = getResultData.get(i);

            String variables = oneDataMap.get("VARIABLES");
            String phone_number = oneDataMap.get("PHONE_NUMBER");
            String serialid = oneDataMap.get("SERIALID");
            String message_number = oneDataMap.get("MESSAGE_NUMBER");//短信流水号，可能用来做参数
            String timeflag = dateUtil.getNowTime();

            //根据参数拿到短信内容
            String SMS = getMessage(variables, smStemplate);
            //封装参数
            smsModel.setDeviceNumber(phone_number);
            smsModel.setMessage(SMS);
            smsModel.setUserGuestId(timeflag);//id存入时间戳
            smsModel.setUserGuestName(serialid);//存入任务id



             Boolean isSend = sendMessage(smsModel);
            oneDataMap.put("SEND_TIME",timeflag);
             if(isSend){
                 oneDataMap.put("IS_SEND_MESSAGE","1");
                 orcalMapper.updateSend(oneDataMap);
             }else {
                 oneDataMap.put("IS_SEND_MESSAGE","0");
                 orcalMapper.updateSend(oneDataMap);
             }
        }

    }

    /**
     * 短信发送完毕后将本批数据传送到行云日志表
     */
    public Boolean addXcloudLog(){
        /**
         * 1、创建分区
         */

        dateUtil.getDateFlag();
        String sql1 = "ALTER TABLE JINGZHUNHUA.API_LOSTCONTACT_HRSS_LOG\n" +
                "ADD PARTITION a"+dateUtil.getDateFlag()+"(SERIALID = '"+serialid+"')";


        /**
         * 导入数据
         */
        String sql2 = "INSERT\n" +
                "    INTO\n" +
                "        JINGZHUNHUA.API_LOSTCONTACT_HRSS_LOG(\n" +
                "            SERIALID ,\n" +
                "            TEMPLATEID ,\n" +
                "            TYPE ,\n" +
                "            MESSAGE_NUMBER ,\n" +
                "            PHONE_NUMBER ,\n" +
                "            SEND_TIME ,\n" +
                "            IDNOMD5 ,\n" +
                "            NAME ,\n" +
                "            VARIABLES ,\n" +
                "            IS_CONTACT ,\n" +
                "            IS_SEND_MESSAGE ,\n" +
                "            FACT_TABLE_NAME\n" +
                "        ) PARTITION\n" +
                "            ON(\n" +
                "            SERIALID = '"+this.serialid+"'" +
                "        ) {select\n" +
                "         serialid ,\n" +
                "        templateid ,\n" +
                "        type ,\n" +
                "        message_number ,\n" +
                "        phone_number ,\n" +
                "        send_time ,\n" +
                "        idnomd5 ,\n" +
                "        name ,\n" +
                "        variables ,\n" +
                "        is_contact ,\n" +
                "        is_send_message ,\n" +
                "        fact_table_name\n" +
                "    FROM\n" +
                "        "+db_schema_epm_test+".LOST_CONTACT_HRSS_RESULT\n" +
                "    WHERE\n" +
                "        SERIALID = '"+this.serialid+"' " +
                "        \t }@jingzhunhua\n";
        try{
            xcloudMapper.useXcloud(sql1);
            xcloudMapper.useXcloud(sql2);
            return true;
        }catch (Exception e ){
            log.warning("插入行云出错");
            return false;
        }
    }

    /**
     * 步骤结束后清空本次使用的数据  包括 1、任务表，2、复联表  3、结果表
     */
    public void clearTable(String factTable){
        orcalMapper.deleteMession(serialid);
        orcalMapper.deletePhone(factTable);
        orcalMapper.deleteResult(serialid);

    }



    /**
     * 获取短信发送内容
     * @param variables 变量格式 json {key1:xxx ,ke2:xxx}   smStemplate 短信模板 您好，key1  xxxxx key2
     * @return
     */
    public String getMessage(String variables,String smStemplate){

        JSONObject variableJson = JSON.parseObject(variables);


        for (int i = 1 ; i <= variableJson.size() ; i++){
            smStemplate = smStemplate.replace("key" + i, variableJson.getString("key" + i));
        }
        return smStemplate;
    }

    public Boolean sendMessage(SmsModel sms){
        Boolean flag  = false;
        String smsUrl = "http://10.191.19.82:8082/daohangsms/sendSms";

        String smsModelStr=JSON.toJSONString(sms);
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("smsModelStr",smsModelStr);


        RestTemplate rt = new RestTemplate();
        String result;
        try{
            log.info("准备发送短信"+paramMap.toString());
            result = rt.postForObject(smsUrl,paramMap, String.class);
        }catch (Exception e){
            result="{\"status\":\"失败\",\"code\":\"-1\",\"msg\":\"无法调用短信接口\"}";
            e.printStackTrace();
            log.warning("无法调用短信接口");

        }
        log.info(result);

        JSONObject jsonObject = JSON.parseObject(result);
        String code = jsonObject.getString("code");
        if(code.equals("000")){
            flag =true;
        }else {
            flag = false;
        }

        return flag;
    }


    /**
     * 获得当前最大的日期
     * @return
     */
    public String getLMaxMonth(){
        return "select max(month_id) as monthid from jingzhunhua.DM_V_D_CUS_ALL_BASIC LIMIT(1, 1)";
    }


    /**
     * 生成码表（事实表，仅包含身份证id）
     * @param tableName
     *
     */
    public void createTable(String tableName ){
        //1创建事实表
        String sql = "create /*+type(dimension)*/  table "+tableName+" (FIELDNAME varchar(70))";
        xcloudMapper.useXcloud(sql);
    }


}
