package com.pos.passport.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Kareem on 4/30/2016.
 */
public class Modifier implements Serializable{

    public String name  = "";
    public String title="";
    public String desc = "";
    public String type="";
    public int cat;
    public int id;
    public int combo_id;
    public int quantity = 1;
    public String barcode = "";
    public BigDecimal cost = BigDecimal.ZERO;
    public BigDecimal price = BigDecimal.ZERO;
    public boolean deleted = false;
    public int combo_check = 0;

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getCombo_id() {
        return combo_id;
    }

    public String getDesc() {
        return desc;
    }

    public int getCat() {
        return cat;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isDeleted() {
        return deleted;
    }

}
