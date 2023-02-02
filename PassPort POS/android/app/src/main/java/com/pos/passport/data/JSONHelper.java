package com.pos.passport.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.model.Cart;
import com.pos.passport.model.Customer;
import com.pos.passport.model.Payment;
import com.pos.passport.model.Product;
import com.pos.passport.model.TaxArray;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kareem on 5/28/2016.
 */
public class JSONHelper {

    public static JSONArray toJSONProductArray(Context context, Cart cart) {

        JSONArray jsonProducts = new JSONArray();

        try {
            int modi_l = 1;
            int combo_l = 1;
            int combo_id = 0;
            for (int i = 0; i < cart.getProducts().size(); i++)
            {
                List<String> posget=new ArrayList<>();
                List<String> posget_itemid=new ArrayList<>();
                JSONObject jsonProduct = new JSONObject();
                Product product = cart.getProducts().get(i);
                ArrayList<Product> modifiers = cart.getProducts().get(i).modifiers;
                JSONArray jsonModifiers = new JSONArray();
                JSONArray jsoncombifiers = new JSONArray();

                for (int j = 0; j < modifiers.size(); j++)
                {
                    JSONObject jsonModifier = new JSONObject();

                    Product modifier = modifiers.get(j);
                    if (modifier.type_check == 0) {
                        //jsonModifier.put("line", j + 1);
                        jsonModifier.put("line", modi_l);
                        jsonModifier.put("id", modifier.id);
                        jsonModifier.put("name", modifier.name);
                        jsonModifier.put("price", modifier.price);
                        jsonModifier.put("cost", modifier.cost);
                        jsonModifier.put("department", modifier.cat);
                        jsonModifier.put("type", modifier.modifierType);
                        jsonModifier.put("quantity", modifier.quantity);
                        jsonModifiers.put(jsonModifier);
                        modi_l = modi_l + 1;

                    }
                }

                JSONObject obj_item = new JSONObject();
                JSONArray array_item = new JSONArray();
                for (int j = 0; j < modifiers.size(); j++)
                {

                    Product modifier = modifiers.get(j);
                    if (modifier.type_check == 1)
                    {
                        if (jsoncombifiers.length() > 0)
                        {

                            //for(int jc=0;jc<jsoncombifiers.length();jc++)
                            if(posget.contains(""+modifier.combo_id))
                            {
                                int index1 = posget.indexOf("" + modifier.combo_id);

                                if(posget_itemid.contains(""+modifier.id))
                                {
                                    Log.v("if show", "if show");
                                }
                                else
                                {

                                    int index = posget.indexOf("" + modifier.combo_id);

                                    JSONObject mainobj_item = jsoncombifiers.getJSONObject(index);
                                    //if (modifier.combo_id == mainobj_item.getInt("id"))
                                    //{
                                    obj_item = new JSONObject();
                                    obj_item.put("itemId", modifier.id);
                                    obj_item.put("image", "");
                                    obj_item.put("description", modifier.desc);
                                    obj_item.put("name", modifier.name);
                                    obj_item.put("price", modifier.price);
                                    obj_item.put("cost", modifier.cost);
                                    obj_item.put("departmentId", modifier.cat);
                                    obj_item.put("barcode", modifier.barcode);
                                    obj_item.put("quantity", modifier.quantity);
                                    mainobj_item.getJSONArray("items").put(obj_item);
                                    posget_itemid.add(""+modifier.id);
                                    //}
                                }
                           }
                            else
                            {

                                JSONObject mainobj_item1 = new JSONObject();
                                mainobj_item1.put("line", combo_l);
                                mainobj_item1.put("id", modifier.combo_id);
                                mainobj_item1.put("name", modifier.comboname);
                                array_item = new JSONArray();
                                obj_item = new JSONObject();
                                obj_item.put("itemId", modifier.id);
                                obj_item.put("image", "");
                                obj_item.put("description", modifier.desc);
                                obj_item.put("name", modifier.name);
                                obj_item.put("price", modifier.price);
                                obj_item.put("cost", modifier.cost);
                                obj_item.put("departmentId", modifier.cat);
                                obj_item.put("barcode", modifier.barcode);
                                obj_item.put("quantity", modifier.quantity);
                                array_item.put(obj_item);
                                mainobj_item1.put("items", array_item);
                                jsoncombifiers.put(mainobj_item1);
                                combo_l = combo_l + 1;
                                posget.add(""+modifier.combo_id);
                                posget_itemid.add(""+modifier.id);
                            }
                        }
                        else
                        {

                            JSONObject mainobj_item = new JSONObject();
                            mainobj_item.put("line", combo_l);
                            mainobj_item.put("id", modifier.combo_id);
                            mainobj_item.put("name", modifier.comboname);
                            array_item = new JSONArray();
                            obj_item = new JSONObject();
                            obj_item.put("itemId", modifier.id);
                            obj_item.put("image", "");
                            obj_item.put("description", modifier.desc);
                            obj_item.put("name", modifier.name);
                            obj_item.put("price", modifier.price);
                            obj_item.put("cost", modifier.cost);
                            obj_item.put("departmentId", modifier.cat);
                            obj_item.put("barcode", modifier.barcode);
                            obj_item.put("quantity", modifier.quantity);
                            array_item.put(obj_item);
                            mainobj_item.put("items", array_item);
                            jsoncombifiers.put(mainobj_item);
                            combo_l=combo_l+1;
                            posget.add(""+modifier.combo_id);
                            posget_itemid.add(""+modifier.id);
                        }

                    }
                }

                jsonProduct.put("line", i + 1);
                jsonProduct.put("id", product.id);
                jsonProduct.put("isNote", product.isNote);
                jsonProduct.put("itemId", product.id);
                jsonProduct.put("name", product.name);
                jsonProduct.put("department", product.cat);
                jsonProduct.put("quantity", product.quantity);
                jsonProduct.put("barcode", product.barcode);
                jsonProduct.put("price", product.price);
                jsonProduct.put("cost", product.cost);
                jsonProduct.put("salePrice", product.salePrice);
                jsonProduct.put("startSale", product.startSale);
                jsonProduct.put("endSale", product.endSale);
                jsonProduct.put("discountName", product.discountName);
                jsonProduct.put("discountAmount", product.discountAmount);
                jsonProduct.put("modifierType", product.modifierType);
                jsonProduct.put("taxable", product.taxable);
                //jsonProduct.put("total", product.totalPrice(cart.mDate));
                jsonProduct.put("total", product.displayPriceNew(cart.mDate));
                jsonProduct.put("modifiers", jsonModifiers);
                jsonProduct.put("comboItems", jsoncombifiers);
                jsonProduct.put("isAlcoholic", product.isAlcoholic);
                jsonProduct.put("isTobaco", product.isTobaco);

                jsonProducts.put(jsonProduct);
            }
        } catch (JSONException e) {
            Utils.alertBox(context, R.string.txt_exception, R.string.msg_creating_json_failed);
            e.printStackTrace();
        }
        return jsonProducts;
    }

    public static JSONArray toJSONTaxArray(Context context, Cart cart) {

        JSONArray jsonProducts = new JSONArray();
        try {
            ArrayList<TaxArray> getTaxarraydata = new ArrayList<>();
            getTaxarraydata = cart.getTaxarray();
           // Log.e("Texarraydata","at helper >>>>"+getTaxarraydata.toString());
            if (getTaxarraydata.size() > 0) {
                for (int i = 0; i < getTaxarraydata.size(); i++) {
                    JSONObject jsonProduct = new JSONObject();
                    TaxArray getTaxarray = getTaxarraydata.get(i);
                    jsonProduct.put("name", getTaxarray.taxname);
                   // Log.e("Amount inner","inner amount>>>"+getTaxarray.amoutn);
                    List<BigDecimal> amoutn=getTaxarray.amoutn;
                    BigDecimal amountfinal=BigDecimal.ZERO;
                    for(int aa=0;aa<amoutn.size();aa++)
                    {
                        amountfinal=amountfinal.add(amoutn.get(aa));
                    }
                    //Log.e("Amount final",">>"+amountfinal);
                    jsonProduct.put("amount",Utils.formatCartTotal(amountfinal));// getTaxarray.amoutn);
                    jsonProducts.put(jsonProduct);
                }
            }
        } catch (JSONException e) {
            Utils.alertBox(context, R.string.txt_exception, R.string.msg_creating_json_failed);
            e.printStackTrace();
        }
        return jsonProducts;
    }

    /*ArrayList<TaxArray> getTaxarray =new ArrayList<>();
    getTaxarray=mCart.getTaxarray();
    if(getTaxarray.size()>0)
    {
        for (int t=0;t<getTaxarray.size();t++)
        {
            if(getTaxarray.get(t).getTaxId() == Integer.parseInt(taxdata.getString("taxId"))) {
                break;
            }
            else
            {
                TaxArray data=new TaxArray();
                data.taxId=Integer.parseInt(taxdata.getString("taxId"));
                data.taxname=taxdata.getString("taxName");
                data.taxpercent=Float.parseFloat(taxdata.getString("taxPercent"));
                getTaxarray.add(data);
            }
        }
    }*/
    public static JSONObject toJSONcustomer(Context context, Cart cart) {

        JSONObject jsonProducts = new JSONObject();
        try {
            if (cart.hasCustomer()) {
                Customer customer = cart.getCustomer();
                jsonProducts.put("id", "" + customer.id);
                jsonProducts.put("firstName", "" + customer.fName);
                jsonProducts.put("lastName", "" + customer.lName);
                jsonProducts.put("email", "" + customer.email);
                jsonProducts.put("phone", "");
            }
            /*else
            {
                jsonProducts.put("id", "");
                jsonProducts.put("firstName", "");
                jsonProducts.put("lastName", "");
                jsonProducts.put("email", "");
                jsonProducts.put("phone", "");
            }*/

        } catch (JSONException e) {
            Utils.alertBox(context, R.string.txt_exception, R.string.msg_creating_json_failed);
            e.printStackTrace();
        }
        return jsonProducts;
    }

    public static JSONArray toJSONPaymentArray(Context context, Cart cart, @Nullable String gatewayId) {
        JSONArray jsonPayments = new JSONArray();
        try {
            for (int p = 0; p < cart.mPayments.size(); p++) {
                JSONObject jsonPayment = new JSONObject();
                Payment payment = cart.mPayments.get(p);

                if (gatewayId != null)
                    payment.gatewayId = gatewayId;

                jsonPayment.put("paymentType", payment.paymentType);
                jsonPayment.put("paymentAmount", payment.paymentAmount);
                jsonPayment.put("gatewayId", payment.gatewayId);
                jsonPayment.put("refNo", payment.refNo);
                jsonPayment.put("authCode", payment.authCode);
                jsonPayment.put("recordNo", payment.recordNo);
                jsonPayment.put("acqRefData", payment.acqRefData);
                jsonPayment.put("processData", payment.processData);
                jsonPayment.put("invoiceNo", payment.invoiceNo);
                //jsonPayment.put("signImage", Base64.encodeToString(payment.signImage.getBytes("UTF-8"), Base64.DEFAULT));
                jsonPayment.put("signImage", payment.signImage);
                jsonPayment.put("payload", payment.payload);
                jsonPayment.put("response", payment.response);
                jsonPayment.put("cardType", payment.cardType);
                jsonPayment.put("lastFour", payment.lastFour);
                jsonPayment.put("tipAmount", payment.tipAmount.toString());
                //Log.e("PAYMENT", jsonPayment.toString());
                jsonPayments.put(jsonPayment);
            }

        } catch (JSONException e) {
            Utils.alertBox(context, R.string.txt_exception, R.string.msg_creating_json_failed);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.e("Helper Payment len", "Helper payment len>>" + jsonPayments.length());
        //Log.e("Payment ", "payment>>" + jsonPayments.toString());
        return jsonPayments;
    }
}
