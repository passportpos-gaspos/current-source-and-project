package com.pos.passport.model;

import org.simpleframework.xml.Root;

/**
 * Created by Kareem on 1/19/2016.
 */

@Root(name="response")
public class Response {
    private int result = -1;
    private String respMSG;
    private String message;
    private String message1;
    private String message2;
    private String authCode;
    private String PNRef;
    private String hostCode;
    private String hostURL;
    private String receiptURL;
    private String getAVSResult;
    private String getAVSResultTXT;
    private String getStreetMatchTXT;
    private String getZipMatchTXT;
    private String getCVResult;
    private String getCVResultTXT;
    private String getGetOrigResult;
    private String getCommercialCard;
    private String workingKey;
    private String keyPointer;
    private String invNum;
    private String cardType;
    private String extData;
    private String theResponseXmlStr;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getRespMSG() {
        return respMSG;
    }

    public void setRespMSG(String respMSG) {
        this.respMSG = respMSG;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage1() {
        return message1;
    }

    public void setMessage1(String message1) {
        this.message1 = message1;
    }

    public String getMessage2() {
        return message2;
    }

    public void setMessage2(String message2) {
        this.message2 = message2;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getPNRef() {
        return PNRef;
    }

    public void setPNRef(String PNRef) {
        this.PNRef = PNRef;
    }

    public String getHostCode() {
        return hostCode;
    }

    public void setHostCode(String hostCode) {
        this.hostCode = hostCode;
    }

    public String getHostURL() {
        return hostURL;
    }

    public void setHostURL(String hostURL) {
        this.hostURL = hostURL;
    }

    public String getReceiptURL() {
        return receiptURL;
    }

    public void setReceiptURL(String receiptURL) {
        this.receiptURL = receiptURL;
    }

    public String getGetAVSResult() {
        return getAVSResult;
    }

    public void setGetAVSResult(String getAVSResult) {
        this.getAVSResult = getAVSResult;
    }

    public String getGetAVSResultTXT() {
        return getAVSResultTXT;
    }

    public void setGetAVSResultTXT(String getAVSResultTXT) {
        this.getAVSResultTXT = getAVSResultTXT;
    }

    public String getGetStreetMatchTXT() {
        return getStreetMatchTXT;
    }

    public void setGetStreetMatchTXT(String getStreetMatchTXT) {
        this.getStreetMatchTXT = getStreetMatchTXT;
    }

    public String getGetZipMatchTXT() {
        return getZipMatchTXT;
    }

    public void setGetZipMatchTXT(String getZipMatchTXT) {
        this.getZipMatchTXT = getZipMatchTXT;
    }

    public String getGetCVResult() {
        return getCVResult;
    }

    public void setGetCVResult(String getCVResult) {
        this.getCVResult = getCVResult;
    }

    public String getGetCVResultTXT() {
        return getCVResultTXT;
    }

    public void setGetCVResultTXT(String getCVResultTXT) {
        this.getCVResultTXT = getCVResultTXT;
    }

    public String getGetGetOrigResult() {
        return getGetOrigResult;
    }

    public void setGetGetOrigResult(String getGetOrigResult) {
        this.getGetOrigResult = getGetOrigResult;
    }

    public String getGetCommercialCard() {
        return getCommercialCard;
    }

    public void setGetCommercialCard(String getCommercialCard) {
        this.getCommercialCard = getCommercialCard;
    }

    public String getWorkingKey() {
        return workingKey;
    }

    public void setWorkingKey(String workingKey) {
        this.workingKey = workingKey;
    }

    public String getKeyPointer() {
        return keyPointer;
    }

    public void setKeyPointer(String keyPointer) {
        this.keyPointer = keyPointer;
    }

    public String getInvNum() {
        return invNum;
    }

    public void setInvNum(String invNum) {
        this.invNum = invNum;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public String getTheResponseXmlStr() {
        return theResponseXmlStr;
    }

    public void setTheResponseXmlStr(String theResponseXmlStr) {
        this.theResponseXmlStr = theResponseXmlStr;
    }
}
