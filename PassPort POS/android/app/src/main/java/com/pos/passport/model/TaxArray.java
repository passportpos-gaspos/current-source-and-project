package com.pos.passport.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TaxArray implements Serializable {
    public String taxname;
    public int taxId;
    public float taxpercent;
    public List<BigDecimal> amoutn = new ArrayList<>();
    public List<String> pid=new ArrayList<>();
    public List<String> quntity=new ArrayList<>();

    public String getTaxname() {
        return taxname;
    }

    public int getTaxId() {
        return taxId;
    }

    public List<BigDecimal> getAmoutn() {
        return amoutn;
    }

   // public void setAmoutn(BigDecimal amoutn) {
   //     this.amoutn = amoutn;
   // }

    public List<String> getPid() {
        return pid;
    }

    public float getTaxpercent() {
        return taxpercent;
    }

    public List<String> getQuntity() {
        return quntity;
    }
}
