package com.pos.passport.model;

import com.pos.passport.interfaces.ItemOpen;

import java.io.Serializable;

public class OpenorderData implements ItemOpen, Serializable
{

	public int _id;
	public int orderId;
	public String orderPaidDate;
	public float orderSubTotal;
	public float orderTax;
	public float orderTip;
	public float orderServiceCharge;
	public float orderTotal;
	public String zoneID;
	public String paymentAuth;
	public String orderType;
	public int token;
	public String paymentSource;
	public String section;
	public String rowid;
	public String customerName;
	public String seat;
	public String orderStatus;
	public int orderStatusId;
	public String orderItems;

	public OpenorderData(int _id, int orderId, String orderPaidDate, float orderSubTotal, float orderTax, float orderTip, float orderServiceCharge, float orderTotal, String zoneID, String paymentAuth, String orderType, int token, String paymentSource, String section, String rowid, String customerName, String seat, String orderStatus, int orderStatusId, String orderItems) {
		this._id = _id;
		this.orderId = orderId;
		this.orderPaidDate = orderPaidDate;
		this.orderSubTotal = orderSubTotal;
		this.orderTax = orderTax;
		this.orderTip = orderTip;
		this.orderServiceCharge = orderServiceCharge;
		this.orderTotal = orderTotal;
		this.zoneID = zoneID;
		this.paymentAuth = paymentAuth;
		this.orderType = orderType;
		this.token = token;
		this.paymentSource = paymentSource;
		this.section = section;
		this.rowid = rowid;
		this.seat = seat;
		this.orderStatus = orderStatus;
		this.orderStatusId = orderStatusId;
		this.orderItems = orderItems;
		this.customerName=customerName;
	}

	public OpenorderData() {
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getOrderPaidDate() {
		return orderPaidDate;
	}

	public void setOrderPaidDate(String orderPaidDate) {
		this.orderPaidDate = orderPaidDate;
	}

	public float getOrderSubTotal() {
		return orderSubTotal;
	}

	public void setOrderSubTotal(float orderSubTotal) {
		this.orderSubTotal = orderSubTotal;
	}

	public float getOrderTax() {
		return orderTax;
	}

	public void setOrderTax(float orderTax) {
		this.orderTax = orderTax;
	}

	public float getOrderTip() {
		return orderTip;
	}

	public void setOrderTip(float orderTip) {
		this.orderTip = orderTip;
	}

	public float getOrderServiceCharge() {
		return orderServiceCharge;
	}

	public void setOrderServiceCharge(float orderServiceCharge) {
		this.orderServiceCharge = orderServiceCharge;
	}

	public float getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(float orderTotal) {
		this.orderTotal = orderTotal;
	}

	public String getZoneID() {
		return zoneID;
	}

	public void setZoneID(String zoneID) {
		this.zoneID = zoneID;
	}

	public String getPaymentAuth() {
		return paymentAuth;
	}

	public void setPaymentAuth(String paymentAuth) {
		this.paymentAuth = paymentAuth;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public String getPaymentSource() {
		return paymentSource;
	}

	public void setPaymentSource(String paymentSource) {
		this.paymentSource = paymentSource;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getRowid() {
		return rowid;
	}

	public void setRowid(String rowid) {
		this.rowid = rowid;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public int getOrderStatusId() {
		return orderStatusId;
	}

	public void setOrderStatusId(int orderStatusId) {
		this.orderStatusId = orderStatusId;
	}

	public String getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(String orderItems) {
		this.orderItems = orderItems;
	}

	@Override
	public boolean isSection() {
		return false;
	}

}
