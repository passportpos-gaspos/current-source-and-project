package com.pos.passport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Kareem on 5/30/2016.
 */
public class ItemsSold {
    private String id;
    private String name;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal cost = BigDecimal.ZERO;
    private BigDecimal voidAmount = BigDecimal.ZERO;
    private BigDecimal returnAmount = BigDecimal.ZERO;

    public ItemsSold(String id, String name, BigDecimal price){
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public ItemsSold(String id, String name, BigDecimal price,BigDecimal quantity, BigDecimal cost){
        this.id = id;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.quantity = quantity;
    }

    public ItemsSold(){
    }

    public BigDecimal getVoidAmount() {
        return voidAmount;
    }

    public void setVoidAmount(BigDecimal voidAmount) {
        this.voidAmount = voidAmount;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj){
        ItemsSold itemsSold = (ItemsSold) obj;
        return this.id.equals(itemsSold.id);
    }

    @Override
    public int hashCode(){
        return id.hashCode();
    }

    public BigDecimal getMargin(){
        return this.getPrice().subtract(this.getCost());
    }

    public BigDecimal getMarginPercentage(){
        return getMargin().divide(this.getPrice(), 2, RoundingMode.HALF_UP);
    }

}
