package com.bonc.LostContactHRSS.util;

import com.bonc.LostContactHRSS.mapper.xcloud.XcloudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by malin on 2018/9/26.
 */
@Component
public class LostContantUtil {

@Autowired
private XcloudMapper xcloudMapper;


    /**
     * 生成失联复联用户群的SQL
     * 2017年6月13日  修改字段  innet_months_split 为 innet_months_new
     * 2017年7月3日   增加复联字段 MAC、IDFA、IMEI
     * 2018年4月27日  对复联全部的进行剔重（解决复联全部时查询多条重复号码问题）
     * 2018年8月22日 cjx不知道改了什么东西
     * @param tablename
     * @return
     */
    public String getContentSql(String tablename,String dimeName,String factTable,String contactType ,String monthId){
        String sql = "";
        //根据cubeid找到大宽表
        //  2017-07-24 制定事实表为basic宽表
//		String factTable = (String) daoHelper.queryForObject("stockmarketing.usergroup.getFactTable", cubeId);

        // 2017-07-24 增加匹配有效用户字段  is_nomal_open（1-有效，0-无效）
        if("01".equals(contactType)){//复联--在网时长最长的

            sql = "select * from ( select ROW_NUMBER() over( PARTITION BY t."+dimeName+" ORDER BY device_number desc) as rn, "
                    + "device_number,prov_id,area_id,user_id,cust_name as surname,cust_sex as sex,t."+dimeName+",kuhu_id,num1,num2,num3  from ";
            sql += " ( select device_number,prov_id,area_id,user_id,cust_name,cust_sex,"+dimeName+",innet_months_new  from "+factTable+" where month_id='"+monthId+"' and is_nomal_open=1) t "
                    + "join ( select MAX(innet_months_new) as innet_months_new, "+dimeName+",MAX(kuhu_id) as kuhu_id,MAX(num1) as num1,MAX(num2) as num2,MAX(num3) as num3 "
                    + " from "+factTable+" t join "+tablename+" a on t."+dimeName+"=a.FIELDNAME and t.month_id='"+monthId+"' where t.is_nomal_open=1 group by "+dimeName+" ) m "
                    + " on t."+dimeName+"=m."+dimeName+"  where t.innet_months_new=m.innet_months_new ) where rn=1";

        }else if("02".equals(contactType)){//复联--通话时长最长的

            sql = "select * from ( select ROW_NUMBER() over( PARTITION BY t."+dimeName+" ORDER BY device_number desc) as rn, "
                    + "device_number,prov_id,area_id,user_id,cust_name as surname,cust_sex as sex,t."+dimeName+",kuhu_id,num1,num2,num3  from ";
            sql += " ( select device_number,prov_id,area_id,user_id,cust_name,cust_sex,"+dimeName+",jf_times  from "+factTable+" where month_id='"+monthId+"' and is_nomal_open=1) t "
                    + "join ( select MAX(jf_times) as jf_times, "+dimeName+",MAX(kuhu_id) as kuhu_id,MAX(num1) as num1,MAX(num2) as num2,MAX(num3) as num3 "
                    + " from "+factTable+" t join "+tablename+" a on t."+dimeName+"=a.FIELDNAME and t.month_id='"+monthId+"' where t.is_nomal_open=1 group by "+dimeName+" ) m "
                    + " on t."+dimeName+"=m."+dimeName+" where t.jf_times=m.jf_times ) where rn=1";

        }else{//复联全部

			/*sql = "select * from ( select ROW_NUMBER() over( PARTITION BY t.mac ORDER BY device_number desc) as rn, "
					+ "device_number,prov_id,area_id,user_id,t."+dimeName+",kuhu_id,num1,num2,num3  from ";*/
            sql = "select distinct (device_number),prov_id,area_id,user_id,cust_name as surname,cust_sex as sex,"+dimeName+",kuhu_id,num1,num2,num3  from  "
                    +factTable+" t join ( SELECT FIELDNAME,max(kuhu_id) as kuhu_id,max(num1) as num1,max(num2) as num2,max(num3) as num3 FROM "
                    +tablename+" group by FIELDNAME) a on t."+dimeName+"=a.FIELDNAME and t.month_id='"+monthId+"' where t.is_nomal_open=1 ";

        }

        System.out.println(sql);



        return sql;
    }

    /**
     * 未复联用户sql
     */
    public String getLostSql(String tablename,String dimeName,String factTable,String monthId){
        return "select kuhu_id from "+tablename+" a left join "+factTable+" t  on a.FIELDNAME=t."+dimeName+" "
                + "AND t.month_id ='"+monthId+"' where t."+dimeName+" is NULL";
    }

    /**
     * 得到最大的复联月份
     */

    public String getLMaxMonth(){
        return "select max(month_id)  from jingzhunhua.DIM_MONTH_USERTOOL";
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
    public void addFactTable(String tableName,String columnName){
        //2插入数据
        String sql = "insert into "+tableName+"" +
                "{select "+columnName+" from jzyx_label.dim_jzyx_data_ods where table_name='DM_V_D_17782' and ord>149}@odba_jzyx;";

    }

}
