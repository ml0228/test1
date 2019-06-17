package com.bonc.LostContactHRSS.mapper.xcloud;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by malin on 2018/7/2.
 */
@Mapper
@Repository
public interface XcloudMapper {
    /**
     * 由于使用sql拼接，一条sql可以解决所有问题
     * @param sql
     */

    //万用查询sql
    List<Map<String,String>> useXcloud(String sql);

    //万用插入sql
    void addXcloud(String sql);


    //-----------------------------------

    //每次来任务插入一条任务信息
    void addMission (String sql);

    //生成一张临时表
    void createTempTable(String sql);

    //查询到复联到的用户
    List<Map> queryUnicom(String sql);

    //创建结果表
    void createResultTable(String sql);

    //插入结果数据
    void addMissionResult(String sql);

    //清空对照表
    void truncateTable(String sql);

    //删除表
    void deleteTable(String sql);

    //获得复联后的所有手机号List
    List<Map<String,String>> getExistsNumber(String sql);

    //获得所有未复联用户的数据
    List<Map<String,String>> getNotExistsIdNo(String sql);



}
