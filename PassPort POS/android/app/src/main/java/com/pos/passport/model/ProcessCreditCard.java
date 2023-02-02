package com.pos.passport.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;

/**
 * Created by Kareem on 1/19/2016.
 */
@Root(name="ProcessCreditCard")
@Default(DefaultType.FIELD)
@Namespace(reference = "http://TPISoft.com/SmartPayments/", prefix = "")
public class ProcessCreditCard {

    @Element(name = "UserName")
    private String userName="";
    @Element(name="Password")
    private String password;
    @Element(name="MerchantID")
    private String merchantID="";
    @Element(name="TransType")
    private String transType="";
    @Element(name = "Amount")
    private String amount="";
    @Element(name = "CardNum")
    private String cardNum ="";
    @Element(required = true)
    private String ExpDate = "";
    @Element(name="PNRef")
    private String pnRef = "";
    @Element(name = "MagData")
    private String magData = "";
    @Element(name= "NameOnCard")
    private String nameOnCard = "";
    @Element(name="InvNum")
    private String invNum = "";
    @Element(name="Zip")
    private String zip = "";
    @Element(name="Street")
    private String street = "";
    @Element(name="CVNum")
    private String cvNum = "";
    @Element(name="ExtData")
    private String extData="";

    // Bridgepay is throwing error if child elements are included in path ExtData

    /*@Path("ExtData")
    @Element(name="SecurityInfo")
    private String securityInfo;
    @Path("ExtData")
    @Element(name="Track1")
    private String track1;
    @Path("ExtData")
    @Element(name="Track2")
    private String track2;
    @Path("ExtData")
    @Element(name="SecureFormat")
    private String secureFormat = "MagneSafeV1";*/

    public String getExtData() {
        return extData;
    }

    public void setExtData(String ksn, String track1, String track2) {
        String data = "<SecurityInfo>%1s</SecurityInfo>"
                + "<Track1>%2s</Track1>"
                + "<Track2>%3s</Track2>"
                + "<SecureFormat>MagneSafeV1</SecureFormat>";
        this.extData = String.format(data, new Object[] {ksn, track1, track2} );
    }

    public String getMagData() {
        return magData;
    }

    public void setMagData(String magData) {
        this.magData = magData;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public String getInvNum() {
        return invNum;
    }

    public void setInvNum(String invNum) {
        this.invNum = invNum;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCvNum() {
        return cvNum;
    }

    public void setCvNum(String cvNum) {
        this.cvNum = cvNum;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String expDate) {
        this.ExpDate = expDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String Password) {
        this.password = Password;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String MerchantID) {
        this.merchantID = MerchantID;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String Amount) {
        this.amount = Amount;
    }

    public String getPNRef() {
        return pnRef;
    }

    public void setPNRef(String PNRef) {
        this.pnRef = PNRef;
    }

    public String generateXmlString(){
        StringWriter sw = new StringWriter();
        try {
            Serializer serializer = new Persister();
            serializer.write(this, sw);
        }catch (Exception e){
            e.printStackTrace();
        }
        return sw.toString();
    }
}
