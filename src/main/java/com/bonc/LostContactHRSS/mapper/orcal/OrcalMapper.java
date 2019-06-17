package com.bonc.LostContactHRSS.mapper.orcal;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by malin on 2018/9/11.
 */
@Mapper
@Repository
public interface OrcalMapper {


    //插入发来的任务数据
    void addMessionData(Map data);

    //插入所有复联的电话表
    void addPhoneData(List<Map<String,String>> phoneData);


    //将全部未复联用户插入结果表
    void addResultDataLost(Map<String,String> map);

    //将复联的全部用户插入结果表
    void addResultDataAllUser(Map<String,String> map);

    //将最大通话时长插入结果表
    void addResultDataMaxCallUser(Map<String,String> map);

    //将最长在网时长插入结果表
    void addResultDataMaxOnlineUser(Map<String,String> map);

    //获取短信模板
    String getSMStemplate(String id);
    //获取结果表数据
    List<Map<String,String>> getResultData(String serialid);

    //更新发送状态
    void updateSend(Map map);

    //清空表
    void deleteMession(String serialid);
    void deletePhone(String factTable);
    void deleteResult(String serialid);

    //插入调用借口记录
    void addLog (String sql);
    void addLog2(Map map);

    //是否是存在的任务序列号
    int existSerialid(String serialid);
    //本批任务是否存在重复的身份证
    int existIdno(Map map);
}
