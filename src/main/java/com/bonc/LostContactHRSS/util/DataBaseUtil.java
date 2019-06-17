package com.bonc.LostContactHRSS.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.csvreader.CsvWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by malin on 2018/9/10.
 */
@Component
public class DataBaseUtil {

    //正式测试切换
    public String jzyx_eam = "jzyx_eam";



    final  String  url = "D:\\";

    /**
     *
     */

/*    HRSS_RESULT_*/

    public  String getCreateTableSql(String tableName){
        return "CREATE TABLE "+tableName+" (ID varchar(70),IDNO varchar(50),INSERT_TIME VARCHAR(50),NUM varchar(50), STATUS varchar(5))";
    }

    /**
     * 將 list 數據放入csv中
     */
    public  String createCsv(List<String[]> dataList,String fileName){
        String csvFilePath = url+fileName+".csv";
        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
            // 写内容
            for(String[] strings :dataList ){
                csvWriter.writeRecord(strings);
            }

            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {

            e.printStackTrace();
        }
        return csvFilePath;

    }






    /*
      测试环境
     */

/*    *//*        *//**//**
 * 1、接收信息，插入到任务表
 *//*

        String[] mession = {serialid,templateid,type,""};
        List<String[]> messionData = new ArrayList<>();
        messionData.add(mession);
        //生成csv文件
        String messionPath = dataBaseUtil.createCsv(messionData, "M" + serialid);
        String insertMessionSql = dataBaseUtil.getInsertSql("JINGZHUNHUA.API_LOSTCONTACT_HRSS", messionPath);
        xcloudMapper.addMission(insertMessionSql);

        *//**
 * 2、生成临时文件    生成失联复联对照表
 *//*
        List<String[]> idNoList = new ArrayList<String[]>();
        JSONArray personArray = JSON.parseArray(list);
        for(int i = 0 ;i<personArray.size();i++){
            JSONObject aPerson = personArray.getJSONObject(0);
            String[] item = {aPerson.getString("idno")};
            idNoList.add(item);
        }
        //生成csv文件
        String tempCsv = dataBaseUtil.createCsv(idNoList, "TEMP" + serialid);
        //清空对照表并插入数据*/
}
