package com.pos.passport.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.model.AdminSetting;
import com.pos.passport.model.Cashier;
import com.pos.passport.model.Category;
import com.pos.passport.model.Customer;
import com.pos.passport.model.ItemButton;
import com.pos.passport.model.LoginCredential;
import com.pos.passport.model.Product;
import com.pos.passport.model.Shift;
import com.pos.passport.model.StoreReceiptHeader;
import com.pos.passport.model.StoreSetting;
import com.pos.passport.model.WebSetting;
import com.pos.passport.util.Dbwork;
import com.pos.passport.util.PrefUtils;
import com.pos.passport.util.RestAgent;
import com.pos.passport.util.UrlProvider;
import com.pos.passport.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/1/15.
 */
public class SendSyncAsyncTask extends AsyncTask<String, String, String> {
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private boolean mIsFullSync;
    public JSONObject mResponseJSONObject;
    private ProductDatabase mDb;
    private boolean mShowMessage = true;
    private boolean mDelete = false;
    private SendSyncListener mCallback;

    public interface SendSyncListener {
        void onFailure(JSONObject error);

        void onSuccess();
    }

    //DbExportImport dbExportImport;
    public SendSyncAsyncTask(Context context, boolean isFullSync) {
        this.mContext = context;
        this.mIsFullSync = isFullSync;
        mDb = ProductDatabase.getInstance(context);
        //dbExportImport=new DbExportImport(mContext);
    }

    public SendSyncAsyncTask(Context context, boolean isFullSync, boolean showMessage) {
        this.mContext = context;
        this.mIsFullSync = isFullSync;
        this.mShowMessage = showMessage;
        mDb = ProductDatabase.getInstance(context);
        //dbExportImport=new DbExportImport(mContext);
    }

    public SendSyncAsyncTask(Context context, boolean isFullSync, boolean showMessage, boolean deletemessage) {
        this.mContext = context;
        this.mIsFullSync = isFullSync;
        this.mShowMessage = showMessage;
        this.mDelete = deletemessage;
        mDb = ProductDatabase.getInstance(context);
    }

    public void setListener(SendSyncListener callback) {
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();

        if (mShowMessage) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String UID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            String version = "";
            try {
                PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                version = pinfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            LoginCredential credential = PrefUtils.getLoginCredential(mContext);
            JSONObject json = new JSONObject();
            json.put("deviceId", credential.getTerminalId());
            json.put("userId", credential.getUserId());
            Log.e("Send data", ">>>" + json.toString());
            publishProgress("Downloading Data");
            try {
                List<RestAgent.Parameter> parameters = new ArrayList<>();
                parameters.add(new RestAgent.Parameter("startSync", json.toString()));
                URL url = new URL(UrlProvider.BASE_URL + UrlProvider.BASE_INNER + UrlProvider.SYNC_URL);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("access-key", UrlProvider.Access_key);
                c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                c.setDoOutput(true);
                OutputStream os = c.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                os.write(json.toString().getBytes("UTF-8"));
                writer.flush();
                writer.close();
                os.close();

                c.connect();
                int length = c.getContentLength();
                InputStream is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1;
                long total = 0;
                StringBuilder data = new StringBuilder();
                while ((len1 = is.read(buffer)) != -1) {
                    data.append(new String(buffer, 0, len1));
                    total += len1;
                    publishProgress(String.format(mContext.getString(R.string.txt_downloading_data_percent), Math.round((length / total * 100))));
                }
                String jsonResult = data.toString();
                mResponseJSONObject = new JSONObject(jsonResult);
                Log.d("Debug", mResponseJSONObject.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (mResponseJSONObject == null)
                return "FAILED";

            String result = "";
            if (mResponseJSONObject != null) {
                String tag = mResponseJSONObject.optString("tag");
                if (tag != null && !tag.isEmpty()) {
                    mCallback.onFailure(mResponseJSONObject);
                    return mResponseJSONObject.optString("error");

                } else {
                    if (mDelete) {
                        Dbwork.exportDb();
                        mDb.clearTable();
                    }
                    if (mResponseJSONObject.has("storeData")) {

                        publishProgress(mContext.getString(R.string.txt_updating_store_setting));
                        JSONObject storeData = new JSONObject();
                        storeData = mResponseJSONObject.getJSONObject("storeData");
                       Log.e("StoreData","StoreData>>>"+mResponseJSONObject.getJSONObject("storeData"));
                        if (storeData.length() > 0) {
                            StoreReceiptHeader storeHeader = new StoreReceiptHeader();
                            storeHeader.setName(storeData.optString("name"));
                            storeHeader.setAddress1(storeData.optString("address1"));
                            storeHeader.setAddress2(storeData.optString("address2"));
                            storeHeader.setEmail(storeData.optString("email"));
                            storeHeader.setPhone(storeData.optString("phone"));
                            storeHeader.setWebsite(storeData.optString("website"));
                            storeHeader.setCurrency(storeData.optString("currancy"));
                            storeHeader.setImage(storeData.optString("image"));
                            storeHeader.setCity(storeData.optString("city"));
                            storeHeader.setState(storeData.optString("state"));

                            if (storeData.has("receipt")) {
                                JSONObject redata = storeData.getJSONObject("receipt");
                                storeHeader.setPrint_sig(redata.optBoolean("printSign"));
                                storeHeader.setCapture_sig(redata.optBoolean("captureSign"));
                                storeHeader.setReceipt_footer(redata.optString("footer"));
                                storeHeader.setReceipt_header(redata.optString("header"));
                            }
                            storeHeader.setHeader_type(StoreSetting.BACK_OFFICE_HEADER);
                            mDb.insertStoreSettings(storeHeader);
                        }
                    }
                    if (mResponseJSONObject.has("taxData")) {
                        JSONArray taxdata = new JSONArray();
                        taxdata = mResponseJSONObject.getJSONArray("taxData");
                        if (taxdata.length() > 0) {
                            for (int i = 0; i < taxdata.length(); i++) {
                                JSONObject taxone = taxdata.getJSONObject(i);
                                //TaxSetting.setTaxname(taxone.getString("taxName"));
                                //TaxSetting.setTaxId(Integer.valueOf(taxone.getString("taxId")));
                                //TaxSetting.setTaxpercent(Float.valueOf(taxone.getString("taxPercent")));
                                mDb.insertTax(Integer.valueOf(taxone.getString("taxId")), taxone.getString("taxName"), Float.valueOf(taxone.getString("taxPercent")));
                            }
                        }
                    }
                    if (mResponseJSONObject.has("merchantData")) {
                        JSONObject mdata = mResponseJSONObject.getJSONObject("merchantData");
                        WebSetting.enabled = mdata.getBoolean("payment");
                        WebSetting.merchantID = mdata.getString("merchantId");
                        WebSetting.webServicePassword = mdata.getString("webservicePassword");
                        WebSetting.hostedMID = mdata.getString("hostedId");
                        WebSetting.hostedPass = mdata.getString("hostedPassword");
                        WebSetting.terminalName = credential.getTerminalName();
                        mDb.insertMercurySettings();
                    }

                    if (mResponseJSONObject.has("departmentData")) {
                        publishProgress(mContext.getString(R.string.txt_updating_departments));
                        JSONArray DepartmentArray = mResponseJSONObject.getJSONArray("departmentData");
                        for (int i = 0; i < DepartmentArray.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_departments) + (int) ((float) i / (float) DepartmentArray.length() * 100f) + "%");
                            JSONObject departmentData = DepartmentArray.getJSONObject(i);
                            Category newprod = new Category();
                            newprod.setId(departmentData.getInt("departmentId"));
                            newprod.setName(departmentData.getString("name"));
                            JSONArray taxarray = new JSONArray();
                            taxarray = departmentData.getJSONArray("tax");
                            if (departmentData.getString("tax").toString().equalsIgnoreCase(null) || departmentData.getString("tax").toString().equalsIgnoreCase("null") || departmentData.getString("tax").toString().equalsIgnoreCase("")) {
                                newprod.setTax(0);
                                newprod.setTaxarray("");
                            } else {
                                newprod.setTax(1);
                                newprod.setTaxarray(departmentData.getJSONArray("tax").toString());
                            }
                            newprod.deleted = false;
                            mDb.insertCat(newprod);
                        }
                    }

                    if (mResponseJSONObject.has("adminData")) {
                        publishProgress(mContext.getString(R.string.txt_updating_admin));
                        JSONObject adminData = mResponseJSONObject.getJSONObject("adminData");
                        Log.e("Admin data",""+adminData);
                        AdminSetting.password = adminData.getString("admin_pin");
                        AdminSetting.userid = adminData.getString("userId");
                        AdminSetting.enabled = adminData.getInt("enabled") != 0;
                        AdminSetting.hint = "";
                        mDb.insertAdminSettings();
                    }
                    if (mResponseJSONObject.has("cashierData")) {
                        publishProgress("Updating Cashiers...");
                        JSONArray DepartmentArray = mResponseJSONObject.getJSONArray("cashierData");
                        for (int i = 0; i < DepartmentArray.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_cashiers) + (int) ((float) i / (float) DepartmentArray.length() * 100f) + "%");
                            JSONObject cashierData = DepartmentArray.getJSONObject(i);
                            Cashier cashier = new Cashier();
                            cashier.id = cashierData.getInt("cashierId");
                            cashier.name = cashierData.getString("name");
                            cashier.email = cashierData.getString("email");
                            cashier.pin = cashierData.getString("pin");
                            cashier.permissionInventory = cashierData.getBoolean("permissionInventory");//cashierData.getInt("permissionInventory") != 0;
                            cashier.permissionSettings = cashierData.getBoolean("permissionSettings");//cashierData.getInt("permissionSettings") != 0;
                            cashier.permissionReports = cashierData.getBoolean("permissionReports");//cashierData.getInt("permissionReports") != 0;
                            cashier.permissionReturn = cashierData.getBoolean("permissionReturn");//cashierData.getInt("permissionReturn") != 0;
                            cashier.permissionPriceModify = cashierData.getBoolean("permissionPriceModify");// cashierData.getInt("permissionPriceModify") != 0;
                            cashier.permissionVoideSale = cashierData.optBoolean("permissionVoideSale");//cashierData.getInt("permissionReturn") != 0;
                            cashier.permissionProcessTender = cashierData.optBoolean("permissionProcessTender");// cashierData.getInt("permissionPriceModify") != 0;

                            boolean admin = cashierData.optBoolean("admin");
                            if (admin)
                                cashier.admin = 1;
                            else
                                cashier.admin = 0;
                            mDb.insertCashier(cashier);
                        }
                    }
                    if (mResponseJSONObject.has("inventoryData")) {
                        JSONArray ProductArray = new JSONArray();
                        ProductArray = mResponseJSONObject.getJSONArray("inventoryData");
                        if (ProductArray.length() > 0) {
                            for (int i = 0; i < ProductArray.length(); i++) {
                                int reorderLevel = 0;
                                int trackable = 0;
                                int taxableget = 0;
                                int isAlcoholic = 0;
                                int isTobaco = 0;
                                publishProgress(mContext.getString(R.string.txt_updating_products) + (int) ((float) i / (float) ProductArray.length() * 100f) + "%");

                                JSONObject productData = ProductArray.getJSONObject(i);
                                Product newprod = new Product();
                                newprod.id = productData.getInt("itemId");
                                newprod.name = productData.getString("name");
                                newprod.cat = productData.getInt("department");
                                if (productData.getString("price").toString().equalsIgnoreCase(null) || productData.getString("price").toString().equalsIgnoreCase("null") || productData.getString("price").toString().equalsIgnoreCase(""))
                                    newprod.price = BigDecimal.ZERO;
                                else
                                    newprod.price = new BigDecimal(productData.getString("price"));

                                if (productData.getString("cost").toString().equalsIgnoreCase(null) || productData.getString("cost").toString().equalsIgnoreCase("null") || productData.getString("cost").toString().equalsIgnoreCase(""))
                                    newprod.cost = BigDecimal.ZERO;
                                else
                                    newprod.cost = new BigDecimal(productData.getString("cost"));

                                if (productData.getString("salePrice").toString().equalsIgnoreCase(null) || productData.getString("salePrice").toString().equalsIgnoreCase("null") || productData.getString("salePrice").toString().equalsIgnoreCase(""))
                                    newprod.salePrice = BigDecimal.ZERO;
                                else
                                    newprod.salePrice = new BigDecimal(productData.getString("salePrice"));
                                newprod.barcode = productData.getString("barcode");
                                newprod.desc = productData.getString("description");
                                if (productData.getString("quantity").toString().equalsIgnoreCase(null) || productData.getString("quantity").toString().equalsIgnoreCase("null") || productData.getString("quantity").toString().equalsIgnoreCase(""))
                                    newprod.onHand = 0;//productData.getInt("quantity");
                                else
                                    newprod.onHand = productData.getInt("quantity");

                                if (productData.getString("price3").toString().equalsIgnoreCase(null) || productData.getString("price3").toString().equalsIgnoreCase("null") || productData.getString("price3").toString().equalsIgnoreCase(""))
                                    newprod.lowAmount = 0;//productData.getInt("price3");
                                else
                                    newprod.lowAmount = productData.getInt("price3");

                                if (productData.getString("saleEndDate").toString().equalsIgnoreCase(null) || productData.getString("saleEndDate").toString().equalsIgnoreCase("null") || productData.getString("saleEndDate").toString().equalsIgnoreCase(""))
                                    newprod.endSale = 0;
                                else
                                    newprod.endSale = productData.getLong("saleEndDate") * 1000;

                                if (productData.getString("saleStartDate").toString().equalsIgnoreCase(null) || productData.getString("saleStartDate").toString().equalsIgnoreCase("null") || productData.getString("saleStartDate").toString().equalsIgnoreCase(""))
                                    newprod.startSale = 0;//productData.getLong("saleStartDate") * 1000;
                                else
                                    newprod.startSale = productData.getLong("saleStartDate") * 1000;

                                newprod.deleted = false;//productData.optInt("deleted") != 0;
                                newprod.track = true;// productData.optInt("track") != 0;
                                newprod.image = productData.getString("image");

                                if (productData.getString("reorderLevel").toString().equalsIgnoreCase(null) || productData.getString("reorderLevel").toString().equalsIgnoreCase("null") || productData.getString("reorderLevel").toString().equalsIgnoreCase("")) {
                                    reorderLevel = 0;
                                } else {
                                    reorderLevel = productData.getInt("reorderLevel");
                                }

                                trackable = productData.getInt("isTrackable");
                                taxableget = productData.getInt("taxable");
                                isAlcoholic = productData.getInt("isAlcoholic");
                                isTobaco = productData.getInt("isTobaco");

                                if (productData.getBoolean("combo")) {
                                    newprod.combo = 1;
                                    JSONArray cdata = productData.getJSONArray("comboItems");
                                    newprod.comboItems = cdata.toString();
                                } else {
                                    newprod.combo = 0;
                                    newprod.comboItems = "";
                                }
                                JSONArray modifierArray = new JSONArray();
                                modifierArray = productData.getJSONArray("modifiers");
                                if (modifierArray.length() > 0) {
                                    newprod.modi_data = modifierArray.toString();
                                } else {
                                    newprod.modi_data = "";
                                }

                                mDb.insertsync(newprod, trackable, reorderLevel, taxableget, isAlcoholic, isTobaco);
                            }
                        }
                    }

                    if (mResponseJSONObject.has("keyboardData")) {
                        publishProgress(mContext.getString(R.string.txt_updating_buttons));

                        JSONObject keyboardData = mResponseJSONObject.getJSONObject("keyboardData");
                        JSONArray ProductArray = keyboardData.getJSONArray("buttons");
                        for (int i = 0; i < ProductArray.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_buttons));
                            JSONObject productData = ProductArray.getJSONObject(i);
                            ItemButton newbutton = new ItemButton();
                            newbutton.id = productData.getInt("id");
                            if (productData.getString("ButtonType").equalsIgnoreCase("Product")) {
                                newbutton.type = ItemButton.TYPE_PRODUCT;
                            }
                            if (productData.getString("ButtonType").equalsIgnoreCase("Folder")) {
                                newbutton.type = ItemButton.TYPE_FOLDER;
                            }
                            if (productData.getString("ButtonType").equalsIgnoreCase("External App")) {
                                newbutton.type = ItemButton.TYPE_APPLINK;
                            }

                            if (!productData.getString("parentId").equals("null"))
                                newbutton.parent = productData.getInt("parentId");

                            if (!productData.getString("productID").equals("null"))
                                newbutton.productID = productData.getInt("productID");

                            if (!productData.optString("department").equals("null"))
                                newbutton.departID = productData.getInt("department");

                            newbutton.order = productData.getInt("position");
                            newbutton.folderName = productData.getString("name");
                            newbutton.deleted = false;
                            newbutton.image = null;
                            newbutton.link = productData.getString("image");
                            if (productData.getString("price").toString().equalsIgnoreCase(null) || productData.getString("price").toString().equalsIgnoreCase("null") || productData.getString("price").toString().equalsIgnoreCase("")) {
                                newbutton.price = "";
                            } else {
                                newbutton.price = productData.getString("price");
                            }

                            if (productData.getString("saleStartDate").toString().equalsIgnoreCase(null) || productData.getString("saleStartDate").toString().equalsIgnoreCase("null") || productData.getString("saleStartDate").toString().equalsIgnoreCase("")) {
                                newbutton.startdate = "0";
                            } else {
                                newbutton.startdate = productData.getString("saleStartDate");
                            }

                            if (productData.getString("saleEndDate").toString().equalsIgnoreCase(null) || productData.getString("saleEndDate").toString().equalsIgnoreCase("null") || productData.getString("saleEndDate").toString().equalsIgnoreCase("")) {
                                newbutton.enddate = "0";
                            } else {
                                newbutton.enddate = productData.getString("saleEndDate");
                            }

                            if (productData.getString("salePrice").toString().equalsIgnoreCase(null) || productData.getString("salePrice").toString().equalsIgnoreCase("null") || productData.getString("salePrice").toString().equalsIgnoreCase("")) {
                                newbutton.saleprice = "0";
                            } else {
                                newbutton.saleprice = productData.getString("salePrice");
                            }

                            if (productData.getString("reorderLevel").toString().equalsIgnoreCase(null) || productData.getString("reorderLevel").toString().equalsIgnoreCase("null") || productData.getString("reorderLevel").toString().equalsIgnoreCase("")) {
                                newbutton.reorderLevel = 0;
                            } else {
                                newbutton.reorderLevel = productData.getInt("reorderLevel");
                            }

                            newbutton.trackable = productData.getInt("isTrackable");
                            mDb.saveButton(newbutton);
                        }
                    }

                    if (mResponseJSONObject.has("days_data")) {
                        publishProgress(mContext.getString(R.string.txt_updating_day_reports));
                        JSONArray DayArray = mResponseJSONObject.getJSONArray("days_data");
                        for (int i = 0; i < DayArray.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_day_reports) + (int) ((float) i / (float) DayArray.length() * 100f) + "%");
                            JSONObject cashierData = DayArray.getJSONObject(i);
                            Shift shift = new Shift();
                            shift.id = cashierData.getInt("day_id");
                            shift.end = cashierData.getLong("end_date");
                            shift.start = cashierData.getLong("start_date");
                            mDb.insertDayShift(shift);
                        }
                    }
                    if (mResponseJSONObject.has("orderStatus")) {
                        publishProgress(mContext.getString(R.string.txt_updating_ostatus));
                        JSONArray DayArray = mResponseJSONObject.getJSONArray("orderStatus");
                        Log.e("orderStatus",""+DayArray.toString());
                        for (int i = 0; i < DayArray.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_ostatus) + (int) ((float) i / (float) DayArray.length() * 100f) + "%");
                            JSONObject ostatus = DayArray.getJSONObject(i);
                            mDb.saveOrderStatus(ostatus);
                        }
                    }

//                    if (mResponseJSONObject.has("orderStatus")) {
//                        publishProgress(mContext.getString(R.string.txt_updating_ostatus));
//                        JSONArray DayArray = mResponseJSONObject.getJSONArray("orderStatus");
//                        for (int i = 0; i < DayArray.length(); i++) {
//                            publishProgress(mContext.getString(R.string.txt_updating_ostatus) + (int) ((float) i / (float) DayArray.length() * 100f) + "%");
//                            JSONObject ostatus = DayArray.getJSONObject(i);
//                            mDb.saveOrderStatus(ostatus);
//                        }
//                    }
                    if (mResponseJSONObject.has("customerData"))
                    {
                        publishProgress(mContext.getString(R.string.txt_updating_cusdata));
                        JSONArray customerData = mResponseJSONObject.getJSONArray("customerData");
                        for (int i = 0; i < customerData.length(); i++) {
                            publishProgress(mContext.getString(R.string.txt_updating_cusdata) + (int) ((float) i / (float) customerData.length() * 100f) + "%");
                            JSONObject cData = customerData.getJSONObject(i);
                            Customer customer = new Customer();
                            customer.setEmail(cData.getString("email").toString().trim());
                            customer.setFName(cData.getString("firstName").toString().trim());
                            customer.setLName(cData.getString("lastName").toString().trim());
                            customer.setPhone(cData.getString("phone").toString().trim());
                            customer.setCid(cData.getInt("customerId"));
                            mDb.insertCustomer(customer);
                        }
                    }
                    if (mResponseJSONObject.has("deviceSetting"))
                    {
                        Log.e("deviceSetting","deviceSetting data>>>"+mResponseJSONObject.get("deviceSetting"));
                        JSONObject deviceSetting = mResponseJSONObject.getJSONObject("deviceSetting");
                        String nvalue=deviceSetting.optString("navigationPane");
                        String svalue=deviceSetting.optString("captureSign");
                        String autoSyncvalue=deviceSetting.optString("autoSync");

                        String mIsCashDrawer=deviceSetting.optString("isCashDrawerOpenOnCredit");
                        String mIsAcceptTips=deviceSetting.optString("isAcceptTips");
                        String mIsAcceptMobileOrders=deviceSetting.optString("isAcceptMobileOrders");
                        //Log.d("navigationPane","navigationPane>>>"+nvalue);
                        //Log.d("captureSign","captureSign>>>"+svalue);
                        //Log.d("autoSyncvalue","autoSyncvalue>>>"+autoSyncvalue);

                        PrefUtils.saveNavigationInfo(mContext,nvalue);
                        PrefUtils.saveAutoSyncInfo(mContext,autoSyncvalue);

                        PrefUtils.saveCashDrawerSyncInfo(mContext,mIsCashDrawer);
                        PrefUtils.saveAcceptTipsSyncInfo(mContext,mIsAcceptTips);
                        PrefUtils.saveAcceptMobileOrdersSyncInfo(mContext,mIsAcceptMobileOrders);

                        if(svalue.length()>0)
                        PrefUtils.saveCaptureSignInfo(mContext,svalue);

                    }
                    result = "success";
                    return result;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "FAILED";
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();

        if (mShowMessage) {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            if (result != null && result.contains("success"))
                Utils.alertBox(mContext, R.string.txt_sync_success, R.string.msg_sync_successfully);
            else {
                if (mDelete)
                    Dbwork.restoreDb();
                Utils.alertBox(mContext, R.string.txt_sync_failed, result);

            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (mShowMessage) {
            mProgressDialog.setMessage(values[0]);
        }
    }

}
