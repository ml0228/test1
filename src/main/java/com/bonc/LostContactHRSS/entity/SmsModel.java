package com.bonc.LostContactHRSS.entity;

/**
 * @author malin
 */

public class SmsModel {
    /**
     * 用户群id
     */
    private String userGuestId;
    /**
     * 用户群名称
     */
    private String userGuestName;
    /**
     * 部门类型编码 短信 02
     */
    private String departTypeId="38";
    /**
     * 短信内容
     */
    private String message;
    /**
     * 短信类型 0：长-默认，1：短
     */
    private String smsType="0";
    /**
     * 客户号码
     */
    private String deviceNumber;
    /**
     * 外显号码
     */
    private String showNum;
    /**
     * 短信后缀
     */
    private String msgSuffix;

    public String getUserGuestId() {
        return userGuestId;
    }

    public void setUserGuestId(String userGuestId) {
        this.userGuestId = userGuestId;
    }

    public String getUserGuestName() {
        return userGuestName;
    }

    public void setUserGuestName(String userGuestName) {
        this.userGuestName = userGuestName;
    }

    public String getDepartTypeId() {
        return departTypeId;
    }

    public void setDepartTypeId(String departTypeId) {
        this.departTypeId = departTypeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public String getShowNum() {
        return showNum;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public void setShowNum(String showNum) {
        this.showNum = showNum;
    }
    
    public String getmsgSuffix() {
        return msgSuffix;
    }

    public void setMsgSuffix(String msgSuffix) {
        this.msgSuffix = msgSuffix;
    }


    @Override
    public String toString() {
        return "SmsModel{" +
                "userGuestId='" + userGuestId + '\'' +
                ", userGuestName='" + userGuestName + '\'' +
                ", departTypeId='" + departTypeId + '\'' +
                ", message='" + message + '\'' +
                ", smsType='" + smsType + '\'' +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", showNum='" + showNum + '\'' +
                ", msgSuffix='" + msgSuffix + '\'' +
                '}';
    }
}
