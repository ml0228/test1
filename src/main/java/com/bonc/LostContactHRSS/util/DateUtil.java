package com.bonc.LostContactHRSS.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by malin on 2018/9/12.
 */
@Component
public class DateUtil {

    static  SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    public String getDateYesterday(){

        Calendar calendar = Calendar.getInstance();//此时打印它获取的是系统当前时间
        calendar.add(Calendar.DATE, -1);    //得到前一天
        Date time = calendar.getTime();
        String date = sdf.format(time);
        return date;
    }

    public  String getNowTime(){

        Date date = new Date();

        String dateString = sdf.format(date);
        return dateString;
    }

    public  String getDateFlag(){
        Date date = new Date();
        long time = date.getTime();
        return time+"";
    }
}
