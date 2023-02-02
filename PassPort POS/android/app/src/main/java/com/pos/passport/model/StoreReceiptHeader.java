package com.pos.passport.model;

/**
 * Created by Kareem on 5/4/2016.
 */
public class StoreReceiptHeader {

    private String name = "";
    private String address1 = "";
    private String address2 = "";
    private String phone = "";
    private String email = "";
    private String website = "";
    private String currency = "$";
    private boolean capture_sig = false;
    private boolean print_sig = false;
    private String receipt_header = "";
    private String receipt_footer = "";
    private String image = "";
    private int header_type = StoreSetting.BACK_OFFICE_HEADER;

    private String city = "";
    private String state = "";

    public int getHeader_type() {
        return header_type;
    }

    public void setHeader_type(int header_type) {
        this.header_type = header_type;
    }

    public String getReceipt_header() {
        return receipt_header;
    }

    public void setReceipt_header(String receipt_header) {
        this.receipt_header = receipt_header;
    }

    public String getReceipt_footer() {
        return receipt_footer;
    }

    public void setReceipt_footer(String receipt_footer) {
        this.receipt_footer = receipt_footer;
    }

    public boolean isCapture_sig() {
        return capture_sig;
    }

    public void setCapture_sig(boolean capture_sig) {
        this.capture_sig = capture_sig;
    }

    public boolean isPrint_sig() {
        return print_sig;
    }

    public void setPrint_sig(boolean print_sig) {
        this.print_sig = print_sig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
