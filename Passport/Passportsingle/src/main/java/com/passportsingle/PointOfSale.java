package com.passportsingle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.passportsingle.utils.Utils;
import com.passportsingle.web.WebRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import it.custom.printer.api.android.CustomAndroidAPI;
import it.custom.printer.api.android.CustomException;
import it.custom.printer.api.android.CustomPrinter;

public class PointOfSale extends AppCompatActivity implements Runnable {

	private static ListView inventoryList;
	public static PointOfSale me;
	static boolean is_reentrant = false;
	public static boolean resentReceiptPrintFlag = false;
	
	private static ProductDatabase shop;
	private static ShopCart cart = new ShopCart();
	static final int SEND_TIMEOUT = 10 * 1000;
	static final int REQUEST_CODE = 12345;
	static final int IMAGE_WIDTH_MAX = 512;

	private String catagory;
	private AutoCompleteTextView textView;

	protected AutoCompleteTextView mPersonName;

	protected AutoCompleteTextView mPersonEmail;

	protected AlertDialog customeralertDialog;

	static final int DIALOG_PAUSED_ID = 0;
	static final int DIALOG_GAMEOVER_ID = 1;

	protected static final int PICK_CONTACT = 20;

	protected String[] prodList;

	private ArrayAdapter<String> Autoadapter;

	private ProgressDialog pd;

	protected float change;

	private ArrayAdapter<String> CustomerAutoadapter;

	private int todo;

	private boolean voiding;

	private DecimalFormat df;

	// private Spinner spinner;

	private Spinner cashierSpin;

	private static boolean CashierEnabled = false;
	protected boolean changed;
	private GridView quickButtons;
	protected com.passportsingle.Button currentFolder;
	protected AlertDialog alertDialog;
	protected int enterType;
	protected int enterDepart;
	private KeyPadDialog keypad;
	protected String enterPaymentType;
	private LinearLayout salesScreen;
	private LinearLayout loginScreen;
	public static Cashier cashier;
	public static boolean loggedIn;
	private Button loginButton;
	public String keyAmount = "";
	public int quanAmount = 0;

	private ImageButton LogoutButton;
	private ImageButton reprintButton;
	private Button emailButton;

	private TextView cashierName;
	private boolean isLoginScreen;
	private static boolean saleProcessed;
	private boolean quantitySelected;
	private TextView previewTotal;

	// private TextView cancelvoid;

	private TableLayout keypadLayout;
	private TableLayout systemLayout;
	private TextView keyAmountView;
	private TextView logoutText;
	private TextView reprintText;
	private TextView quantityAT;
	private TableRow searchArea;
	private ImageButton removePaymentButton;
	private TableRow dateTimeArea;
	private TextView dateTimeText;
	private TextView invoiceText;
	private SharedPreferences mSharedPreferences;
	private ImageButton voidImage;
	protected long ExternalAmount;
	private LinearLayout buttonScreen;
	private TableRow previewItemView;
	private TextView previewItemPrice;
	private TextView previewItemName;
	static CustomPrinter prnDevice = null;
	private AsyncTask asyncTask;
	private boolean sendingLogin;
	private boolean updatePressed;
	private String PrintAmount;
	private String PrintAuth;
	private String PrintInvoice;
	private String PrintCardHolder;
	private String PrintCardNumber;
	private String PrintCardExpire;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setShop(new ProductDatabase(this));

		me = this;

		df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(false);
		
		getShop().findTax();
		getShop().findStoreSettings();
		getShop().findEmailSettings();
		getShop().findReceiptSettings();
		getShop().findAdminSettings();
		getShop().findMercurySettings();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		if (width < height) {
			if (width * 160 / metrics.densityDpi < 550) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		} else {
			if (height * 160 / metrics.densityDpi < 550) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}

		setContentView(R.layout.activity_enders_point_of_sale);

		salesScreen = (LinearLayout) findViewById(R.id.salesScreen);
		loginScreen = (LinearLayout) findViewById(R.id.loginScreen);
		buttonScreen = (LinearLayout) findViewById(R.id.buttonScreen);

		keypadLayout = (TableLayout) findViewById(R.id.keypadLayout);
		systemLayout = (TableLayout) findViewById(R.id.systemLayout);
		searchArea = (TableRow) findViewById(R.id.search_Area);
		dateTimeArea = (TableRow) findViewById(R.id.dateTimeArea);
		previewItemView = (TableRow) findViewById(R.id.previewItemView);

		cashierSpin = (Spinner) findViewById(R.id.cashierSpin);
		loginButton = (Button) findViewById(R.id.loginButton);
		cashierName = (TextView) findViewById(R.id.cashierName);
		// cancelvoid = (TextView) findViewById(R.id.cancelvoid);

		previewItemName = (TextView) findViewById(R.id.previewItemName);
		previewItemPrice = (TextView) findViewById(R.id.previewItemPrice);
		previewTotal = (TextView) findViewById(R.id.previewTotal);

		voidImage = (ImageButton) findViewById(R.id.keyPadVoid);

		keyAmountView = (TextView) findViewById(R.id.keyAmountView);
		quantityAT = (TextView) findViewById(R.id.quantityAT);
		quantityAT.setVisibility(View.INVISIBLE);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cashier cashier = (Cashier) cashierSpin.getSelectedItem();
				enterType = 5;
				showKeyPad();
			}
		});

		inventoryList = (ListView) findViewById(R.id.productsordered);

		inventoryList.setAdapter(new ProductAdapter(this, 0, getCart()
				.getProducts()));

		registerForContextMenu(inventoryList);

		logoutText = (TextView) findViewById(R.id.logoutText);
		reprintText = (TextView) findViewById(R.id.reprintText);

		dateTimeText = (TextView) findViewById(R.id.dateTimeText);
		invoiceText = (TextView) findViewById(R.id.invoiceText);

		LogoutButton = (ImageButton) findViewById(R.id.keyPadLogout);
		reprintButton = (ImageButton) findViewById(R.id.keyPadReprint);
		emailButton = (Button) findViewById(R.id.keyPadEmail);
		removePaymentButton = (ImageButton) findViewById(R.id.keyPadPaymentDelete);

		reprintButton.setVisibility(View.INVISIBLE);
		emailButton.setVisibility(View.INVISIBLE);
		reprintText.setVisibility(View.INVISIBLE);

		Autoadapter = new ArrayAdapter<String>(this, R.layout.item_list);
		Autoadapter.setNotifyOnChange(true);

		textView = (AutoCompleteTextView) findViewById(R.id.autoproduct);
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
							.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				}
			}
		});
		textView.setAdapter(Autoadapter);
		textView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {

				if (saleProcessed) {
					alertbox(
							getResources().getString(R.string.sale_processed),
							getResources().getString(
									R.string.sale_processed_message));
					return;
				}

				boolean isAGo = false;
				String item = null;
				Cursor c = null;

				if (listView != null) {
					item = listView.getItemAtPosition(position).toString();
					String[] RowData = item.split(",");
					c = shop.getProdByName(RowData[0]);
					isAGo = true;
				}

				if (isAGo == false) {
					if (view != null) {
						TextView text = (TextView) view;
						String[] RowData = text.getText().toString().split(",");
						c = shop.getProdByName(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (textView.getText() != null) {
						item = textView.getText().toString();
						String[] RowData = item.split(",");
						c = shop.getProdByName(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (prodList.length > 0) {
						item = prodList[0];
						String[] RowData = item.split(",");
						c = shop.getProdByName(RowData[0]);
						isAGo = true;
					}
				}

				if (c != null) {
					if (c.getInt(c.getColumnIndex("_id")) >= 0) {
						Product product = new Product();

						product.price = Long.valueOf(c.getString(c
								.getColumnIndex("price")));
						product.salePrice = c.getLong(c
								.getColumnIndex("salePrice"));
						product.endSale = c.getLong(c
								.getColumnIndex("saleEndDate"));
						product.startSale = c.getLong(c
								.getColumnIndex("saleStartDate"));

						product.cost = Long.valueOf(c.getString(c
								.getColumnIndex("cost")));
						product.id = c.getInt(c.getColumnIndex("_id"));
						product.barcode = (c.getString(c
								.getColumnIndex("barcode")));
						product.name = (c.getString(c.getColumnIndex("name")));
						product.desc = (c.getString(c.getColumnIndex("desc")));
						product.onHand = (c.getInt(c.getColumnIndex("quantity")));
						product.cat = (c.getInt(c.getColumnIndex("catid")));
						product.buttonID = (c.getInt(c
								.getColumnIndex("buttonID")));
						product.lastSold = (c.getInt(c
								.getColumnIndex("lastSold")));
						product.lastReceived = (c.getInt(c
								.getColumnIndex("lastReceived")));
						product.lowAmount = (c.getInt(c
								.getColumnIndex("lowAmount")));

						if (quantitySelected) {
							product.quantity = quanAmount;
							quantitySelected = false;
							quantityAT.setVisibility(View.INVISIBLE);
						}

						cart.AddProduct(product);
						c.close();
						((ProductAdapter) inventoryList.getAdapter())
								.notifyDataSetChanged();
						updateTotals();
					} else {
						alertbox(
								getResources().getString(R.string.not_found),
								getResources().getString(
										R.string.product_not_found));
					}
				} else {
					alertbox(getResources().getString(R.string.not_found),
							getResources()
									.getString(R.string.product_not_found));
				}

				textView.setText("");

				((ProductAdapter) inventoryList.getAdapter())
						.notifyDataSetChanged();
			}
		});

		textView.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				prodList = ProductDatabase.helper.fetchItemsByName(s.toString());
				if (prodList != null) {
					Autoadapter.clear();
					for (int i = 0; i < prodList.length; i++) {
						Autoadapter.add(prodList[i]);
					}
				} else {
					Autoadapter.clear();
				}

			}
		});

		TextView.OnEditorActionListener keyListener = new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					String search = null;

					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_processed_message));
						return true;
					}

					search = textView.getText().toString();

					if (!search.equals("")) {
						Product product = getShop()
								.findByBarcode(search.trim());
						if (product == null) {
							alertbox(
									getResources()
											.getString(R.string.not_found),
									getResources().getString(
											R.string.product_not_found));
						} else {
							if (quantitySelected) {
								product.quantity = quanAmount;
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							}
							cart.AddProduct(new Product(product));
							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();
						}

						textView.setText("");
						textView.setSelection(0);
					}
				}
				return true;
			}
		};

		textView.setOnEditorActionListener(keyListener);

		quickButtons = (GridView) findViewById(R.id.quick_buttons);

		ButtonAdaptor itemAdapter = new ButtonAdaptor(
				ProductDatabase.getButtons(0), this);
		quickButtons.setAdapter(itemAdapter);

		ArrayList<Cashier> cashiers = ProductDatabase.getCashiers();
		if (cashiers.size() > 0 && AdminSetting.enabled) {
			if (!loggedIn) {
				showLoginScreen();
			} else {
				showSalesScreen();
			}

			CashierEnabled = true;
			// LinearLayout cView = (LinearLayout)
			// findViewById(R.id.cashierView);
			// cView.setVisibility(View.VISIBLE);

			ArrayAdapter<Cashier> cashierSpinAdapter = new ArrayAdapter<Cashier>(
					this, R.layout.spiner, cashiers);
			cashierSpinAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			cashierSpin.setAdapter(cashierSpinAdapter);
		} else {
			CashierEnabled = false;

			Cashier cashier = new Cashier();
			cashier.name = getResources().getString(R.string.administrator);
			cashier.pin = AdminSetting.password;
			cashier.permissionInventory = true;
			cashier.permissionPriceModify = true;
			cashier.permissionReports = true;
			cashier.permissionReturn = true;
			cashier.permissionSettings = true;

			loginCashier(cashier);
		}

		if (CashierEnabled == false) {
			LogoutButton.setVisibility(View.INVISIBLE);
			logoutText.setVisibility(View.INVISIBLE);
		} else {
			LogoutButton.setVisibility(View.VISIBLE);
			logoutText.setVisibility(View.VISIBLE);
		}

		quickButtons.setClickable(true);
		quickButtons
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Cursor c = (Cursor) quickButtons
								.getItemAtPosition(position);
						int newType = c.getInt(c.getColumnIndex("type"));
						int newParent = c.getInt(c.getColumnIndex("_id"));
						int productID = c.getInt(c.getColumnIndex("productID"));
						int departID = c.getInt(c.getColumnIndex("departID"));
						String FolderName = c.getString(c
								.getColumnIndex("folderName"));
						String link = c.getString(c.getColumnIndex("link"));

						if (saleProcessed) {
							alertbox(
									getResources().getString(
											R.string.sale_processed),
									getResources().getString(
											R.string.sale_processed_message));
							return;
						}

						if (newType == 5) {
							long quan = 1;

							if (quantitySelected) {
								quan = quanAmount;
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							} else {
								if (keyAmount.length() > 0) {
									quan = Long.valueOf(keyAmount);
									if (quan == 0)
										quan = 1;
									if (quan > 1)
										quan = quan;
									// if(quan > 99) quan = 99;
								}
							}

							ArrayList<Product> products = getItemList(link);

							for (int i = 0; i < products.size(); i++) {
								products.get(i).quantity = (int) quan;
								cart.AddProduct(products.get(i));
							}

							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();

							keyAmount = "";
							keyAmountView.setText(StoreSetting.getCurrency()
									+ df.format(0));
						}

						if (newType == 2) {
							Cursor productC = shop
									.getProdByName("" + productID);

							long quan = 1;

							if (quantitySelected) {
								quan = quanAmount;
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							} else {
								if (keyAmount.length() > 0) {
									quan = Long.valueOf(keyAmount);
									if (quan == 0)
										quan = 1;
									if (quan > 1)
										quan = quan;
									// if(quan > 99) quan = 99;
								}
							}

							Product product = new Product();

							product.price = Long.valueOf(productC
									.getString(productC.getColumnIndex("price")));
							product.salePrice = productC.getLong(productC
									.getColumnIndex("salePrice"));
							product.endSale = productC.getLong(productC
									.getColumnIndex("saleEndDate"));
							product.startSale = productC.getLong(productC
									.getColumnIndex("saleStartDate"));

							product.cost = Long.valueOf(productC
									.getString(productC.getColumnIndex("cost")));
							product.id = productC.getInt(productC
									.getColumnIndex("_id"));
							product.barcode = (productC.getString(productC
									.getColumnIndex("barcode")));
							product.name = (productC.getString(productC
									.getColumnIndex("name")));
							product.desc = (productC.getString(productC
									.getColumnIndex("desc")));
							product.onHand = (productC.getInt(productC
									.getColumnIndex("quantity")));
							product.cat = (productC.getInt(productC
									.getColumnIndex("catid")));
							product.quantity = (int) quan;
							product.buttonID = (productC.getInt(productC
									.getColumnIndex("buttonID")));
							product.lastSold = (productC.getInt(productC
									.getColumnIndex("lastSold")));
							product.lastReceived = (productC.getInt(productC
									.getColumnIndex("lastReceived")));
							product.lowAmount = (productC.getInt(productC
									.getColumnIndex("lowAmount")));

							cart.AddProduct(product);
							productC.close();

							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();

							keyAmount = "";
							keyAmountView.setText(StoreSetting.getCurrency()
									+ df.format(0));
						}

						if (newType == 3) {

							int quan = 1;
							if (quantitySelected) {
								quan = quanAmount;
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							}

							long price = 1;
							if (keyAmount.length() > 0) {
								price = Long.valueOf(keyAmount);
								if (price == 0)
									price = 1;
							}

							Product product = new Product();
							product.name = ProductDatabase.getCatById(departID);
							product.cat = departID;
							product.price = price;
							product.quantity = quan;

							cart.AddProduct(product);
							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();

							keyAmount = "";
							keyAmountView.setText(StoreSetting.getCurrency()
									+ df.format(0));
						}

						if (newType == 4) {

							if (cart.getProducts().size() == 0) {
								keyAmount = "";
								keyAmountView.setText(StoreSetting
										.getCurrency() + df.format(0));
								alertbox(
										getResources().getString(
												R.string.no_products),
										getResources().getString(
												R.string.enter_a_product));
								return;
							}

							if (cart.total <= 0 && !cashier.permissionReturn) {
								keyAmount = "";
								keyAmountView.setText(StoreSetting
										.getCurrency() + df.format(0));
								alertbox(
										getResources().getString(
												R.string.no_permission),
										getResources()
												.getString(
														R.string.need_return_permission));
								return;
							}

							if (quantitySelected) {
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							}

							long paymentSum = 0;

							for (int p = 0; p < cart.Payments.size(); p++) {
								paymentSum += cart.Payments.get(p).paymentAmount;
							}

							long amount = cart.total - paymentSum;
							if (keyAmount.length() > 0) {
								amount = Long.valueOf(keyAmount);
							}

							if (FolderName.equals(getResources().getString(
									R.string.credit_card))) {
								/*
								 * Intent i = new
								 * Intent(PointOfSale.this,GeniusCharge.class);
								 * i.putExtra("InvoiceID",
								 * cart.trans+""+cart.Payments.size());
								 * //cart.id =
								 * ProductDatabase.getSaleIDNumber()+1;
								 * i.putExtra("saleID",
								 * ProductDatabase.getSaleIDNumber()+1); amount
								 * = cart.total - paymentSum;
								 * i.putExtra("Amount", df.format(amount /
								 * 100f).replaceAll(",", "."));
								 * i.putExtra("Cashier", cashier.name);
								 * startActivityForResult(i, 1001); return;
								 */

								if (PrioritySetting.enabled) {
									Intent i = new Intent(PointOfSale.this,
											ChargeScreen.class);
									i.putExtra("InvoiceID", cart.trans + ""
											+ cart.Payments.size());
									// cart.id =
									// ProductDatabase.getSaleIDNumber()+1;
									i.putExtra(
											"saleID",
											ProductDatabase.getSaleIDNumber() + 1);
									amount = cart.total - paymentSum;
									i.putExtra("Amount",
											df.format(amount / 100f)
													.replaceAll(",", "."));
									i.putExtra("Cashier", cashier.name);
									startActivityForResult(i, 1001);
									return;
								} else {
									amount = cart.total - paymentSum;
								}
							}

							Payment payment = new Payment();
							payment.paymentType = FolderName;
							payment.paymentAmount = amount;

							cart.Payments.add(payment);
							updateTotals();

							keyAmount = "";
							keyAmountView.setText(StoreSetting.getCurrency()
									+ df.format(0));
						}

						if (newType == 1) {
							currentFolder = ProductDatabase
									.getButtonByID(newParent);
							((ButtonAdaptor) quickButtons.getAdapter())
									.changeCursor(ProductDatabase
											.getButtons(newParent));
						}

						if (newType == 7) {
							Product product = new Product();
							product.name = FolderName;
							product.price = 0;
							product.isNote = true;

							cart.AddProduct(product);
							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();
						}

						if (newType == 6) {
							
							if(FolderName.equals("Upgrade")){
								
								Builder builder = new AlertDialog.Builder(me);
								
								builder.setTitle(R.string.edge_upgrade)
								.setPositiveButton("Buy Priority Edge", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										Builder upgradeDialog = new AlertDialog.Builder(me);
										upgradeDialog.setTitle("Upgrade To Edge");
										upgradeDialog.setMessage(R.string.edge_upgrade_message);
										upgradeDialog.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {

												UpdateApp atualizaApp = new UpdateApp();
												atualizaApp.setContext(PointOfSale.this
														.getApplicationContext());
												asyncTask = atualizaApp
														.execute("http://prioritypos.azurewebsites.net/download/edgeUpdate.php");
													
											}
										});
										
										upgradeDialog.setNeutralButton("Buy Edge", new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												
												Intent webIntent =  new Intent(PointOfSale.this, WebViewActivity.class);
												webIntent.putExtra("url", getString(R.string.edge_html));
												startActivity(webIntent);
												
												
											}
										});
										
										AlertDialog alertDialog = upgradeDialog.create();
										alertDialog.show();
									}
									
								}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										dialog.dismiss();
									}
								}).setNeutralButton("Buy Single License", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										Intent webIntent =  new Intent(PointOfSale.this, WebViewActivity.class);
										webIntent.putExtra("url", getString(R.string.single_html));
										startActivity(webIntent);
										
									}
								});
								
								builder.setMessage(R.string.edge_advantages);
								
								alertDialog = builder.create();
								alertDialog.show();
							}
							
							if (FolderName.toLowerCase().contains("http://")
									|| FolderName.toLowerCase().contains(
											"https://")
									|| FolderName.toLowerCase().contains(
											"market://")) {
								if (FolderName.toLowerCase().contains(
										"vt.mercurypay.com")) {
									if (cart.total > 0 && !saleProcessed) {
										long paymentSum = 0;

										for (int p = 0; p < cart.Payments
												.size(); p++) {
											paymentSum += cart.Payments.get(p).paymentAmount;
										}

										ExternalAmount = cart.total
												- paymentSum;

										if (keyAmount.length() > 0) {
											ExternalAmount = Long
													.valueOf(keyAmount);
										}

										final Uri uri = Uri.parse(FolderName);

										Builder builder = new AlertDialog.Builder(
												me);
										builder.setTitle(
												getResources().getString(
														R.string.external_vt))
												.setMessage(
														getResources()
																.getString(
																		R.string.vt_message_1)
																+ " "
																+ StoreSetting
																		.getCurrency()
																+ df.format(ExternalAmount / 100f)
																+ " "
																+ getResources()
																		.getString(
																				R.string.vt_message_2))
												.setInverseBackgroundForced(
														true)
												.setPositiveButton(
														getResources()
																.getString(
																		R.string.proceed),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																try {
																	Intent myIntent = new Intent(
																			Intent.ACTION_VIEW,
																			uri);
																	startActivityForResult(
																			myIntent,
																			1015);
																	// startActivity(myIntent);
																} catch (ActivityNotFoundException e) {
																	Toast.makeText(
																			PointOfSale.this,
																			"No application can handle this request,"
																					+ " Please install a webbrowser",
																			Toast.LENGTH_LONG)
																			.show();
																	e.printStackTrace();
																}

																return;
															}
														})
												.setNegativeButton(
														getResources()
																.getString(
																		R.string.dialog_cancel),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																dialog.cancel();
															}
														});

										alertDialog = builder.create();
										alertDialog.show();
									} else {
										alertbox(
												getResources().getString(
														R.string.error),
												getResources()
														.getString(
																R.string.no_product_message));
									}
								} else {

									try {
										Intent myIntent = new Intent(
												Intent.ACTION_VIEW, Uri
														.parse(FolderName));
										startActivity(myIntent);
									} catch (ActivityNotFoundException e) {
										Toast.makeText(
												PointOfSale.this,
												"No application can handle this request,"
														+ " Please install a webbrowser",
												Toast.LENGTH_LONG).show();
										e.printStackTrace();
									}
								}
							} else if (isAppInstalled(FolderName)) {
								final Intent mIntent = getPackageManager()
										.getLaunchIntentForPackage(FolderName);

								if (FolderName.equals("com.squareup")
										|| FolderName
												.equals("com.intuit.intuitgopayment")
										|| FolderName
												.equals("ban.card.payanywhere")) {
									if (cart.total > 0 && !saleProcessed) {
										long paymentSum = 0;

										for (int p = 0; p < cart.Payments
												.size(); p++) {
											paymentSum += cart.Payments.get(p).paymentAmount;
										}

										ExternalAmount = cart.total
												- paymentSum;

										if (keyAmount.length() > 0) {
											ExternalAmount = Long
													.valueOf(keyAmount);
										}

										Builder builder = new AlertDialog.Builder(
												me);
										builder.setTitle(
												getResources().getString(
														R.string.external_pa))
												.setMessage(
														getResources()
																.getString(
																		R.string.pa_message_1)
																+ " "
																+ StoreSetting
																		.getCurrency()
																+ df.format(ExternalAmount / 100f)
																+ " "
																+ getResources()
																		.getString(
																				R.string.pa_message_2))
												.setInverseBackgroundForced(
														true)
												.setPositiveButton(
														getResources()
																.getString(
																		R.string.proceed),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																startActivityForResult(
																		mIntent,
																		1005);
																return;
															}
														})
												.setNegativeButton(
														getResources()
																.getString(
																		R.string.dialog_cancel),
														new DialogInterface.OnClickListener() {
															public void onClick(
																	DialogInterface dialog,
																	int id) {
																dialog.cancel();
															}
														});

										alertDialog = builder.create();
										alertDialog.show();
									} else {
										alertbox(
												getResources().getString(
														R.string.error),
												getResources()
														.getString(
																R.string.no_product_message));
									}

								} else {
									startActivity(mIntent);
									return;
								}
							}
						}

						if (newType == -1) {
							((ButtonAdaptor) quickButtons.getAdapter())
									.changeCursor(ProductDatabase
											.getButtons(currentFolder.parent));
							currentFolder = ProductDatabase
									.getButtonByID(currentFolder.parent);
						}
					}
				});

		if (keyAmount.equals(""))
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
		else
			keyAmountView.setText(StoreSetting.getCurrency()
					+ df.format(Long.valueOf(keyAmount) / 100d));

		StringBuffer message = new StringBuffer(
				"                                        ");
		String welcome1 = getResources().getString(R.string.welcome);
		String store = StoreSetting.getName();
		if (store.equals("")) {
			store = "Our Store!";
		}

		if (welcome1.length() > 20)
			welcome1 = welcome1.substring(0, 19);

		int start = 9 - welcome1.length() / 2;
		message.replace(start, start + welcome1.length() - 1, welcome1);

		if (store.length() > 20)
			store = store.substring(0, 19);

		start = 29 - store.length() / 2;
		message.replace(start, start + store.length() - 1, store);

		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);

		boolean registered = mSharedPreferences.getBoolean("APOS_REGISTERED",
				false);
		boolean licensed = mSharedPreferences
				.getBoolean("APOS_LICENSED", false);
		long login_last = mSharedPreferences.getLong("APOS_LASTLOG", 0);

		if (registered && licensed) {
			long now = new Date().getTime();
			if (now > (login_last + 60 * 60 * 3 * 1000)) {
				if (hasInternet()) {
					// pd = ProgressDialog.show(this, "",
					// "Checking for update...", true, false);
					if (sendingLogin == false) {
						sendingLogin = true;
						asyncTask = new SendLogin().execute("");
					}
				}
			}
		}

		if (!registered || !licensed) {
			Intent i = new Intent(PointOfSale.this, RegisterScreen.class);
			startActivityForResult(i, 3003);
		}

		if (getLastNonConfigurationInstance() != null) {
			asyncTask = (AsyncTask) getLastNonConfigurationInstance();
			if (asyncTask != null) {

				if (!(asyncTask.getStatus().equals(AsyncTask.Status.FINISHED))) {
					if (pd != null)
						pd.show();
				}
			}
		} else {
			if (isHardwareKeyboardAvailable()) {
				alertbox(getResources().getString(R.string.hardware_kb),
						getResources().getString(R.string.hw_kb_message));
			}
		}
		setTitle(Utils.getApplicationName(me));
		InputStream inputStream = getResources().openRawResource(R.raw.printer_models_97_03);
		ProductDatabase.insertRowsInPrinterTable(inputStream);
	}

	/*@Override
	public Object onRetainNonConfigurationInstance() {
		if (pd != null)
			pd.dismiss();
		if (asyncTask != null)
			return (asyncTask);
		return super.onRetainNonConfigurationInstance();
	}*/

	@Override
	public void onDestroy() {
		EscPosDriver.closeBTConnections();
		super.onDestroy();
	}

	protected void ResentReprintReceipts() {
		if (ReceiptSetting.enabled) {
			todo = 2;
			pd = ProgressDialog.show(PointOfSale.this, "", getResources()
					.getString(R.string.reprint_receipt), true, false);
			Thread thread = new Thread(PointOfSale.this);
			thread.start();
		}
	}

	protected void showKeyPad() {
		keyAmount = "";
		keypad = new KeyPadDialog(this);
		keypad.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		keypad.requestWindowFeature(Window.FEATURE_NO_TITLE);
		keypad.show();
	}

	protected boolean isAppInstalled(String packageName) {
		Intent mIntent = getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (mIntent != null) {
			return true;
		} else {
			return false;
		}
	}

	protected void saveReceiving(ShopCart cart) {
		for (int i = 0; i < cart.getProducts().size(); i++) {
			if (cart.getProducts().get(i).id > 0) {
				Product oldProduct = cart.getProducts().get(i);

				Cursor c = shop.getProdByName("" + oldProduct.id);

				Product product = new Product();

				product.price = Long.valueOf(c.getString(c
						.getColumnIndex("price")));
				product.cost = Long.valueOf(c.getString(c
						.getColumnIndex("cost")));
				product.salePrice = c.getLong(c.getColumnIndex("salePrice"));
				product.endSale = c.getLong(c.getColumnIndex("saleEndDate"));
				product.startSale = c
						.getLong(c.getColumnIndex("saleStartDate"));

				product.id = c.getInt(c.getColumnIndex("_id"));
				product.barcode = (c.getString(c.getColumnIndex("barcode")));
				product.name = (c.getString(c.getColumnIndex("name")));
				product.desc = (c.getString(c.getColumnIndex("desc")));
				product.onHand = (c.getInt(c.getColumnIndex("quantity")));
				product.cat = (c.getInt(c.getColumnIndex("catid")));
				product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
				product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
				product.lastReceived = (c.getInt(c
						.getColumnIndex("lastReceived")));
				product.lowAmount = (c.getInt(c.getColumnIndex("lowAmount")));
				c.close();

				product.onHand += oldProduct.quantity;

				ProductDatabase.replaceItem(product);
			}
		}
	}

	protected void resaveSale(ShopCart cart) {
		saleProcessed = true;

		if (!cashier.name.equals(getResources().getString(
				R.string.training_cash))) {
			// we create a XmlSerializer in order to write xml data
			XmlSerializer serializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			try {
				serializer.setOutput(writer);
				serializer.startDocument(null, Boolean.valueOf(true));
				serializer
						.setFeature(
								"http://xmlpull.org/v1/doc/features.html#indent-output",
								true);
				serializer.startTag(null, "SaleData");

				if (cart.hasCustomer()) {
					serializer.startTag(null, "Customer");
					serializer
							.attribute(null, "id", "" + cart.getCustomer().id);
					serializer.attribute(null, "name", cart.getCustomer().name);
					serializer.attribute(null, "email",
							cart.getCustomer().email);
					serializer.endTag(null, "Customer");
				}

				serializer.startTag(null, "Payments");

				for (int p = 0; p < cart.Payments.size(); p++) {
					Payment payment = cart.Payments.get(p);

					serializer.startTag(null, "Payment");
					serializer.attribute(null, "paymentType",
							payment.paymentType);
					serializer.attribute(null, "paymentAmount", ""
							+ payment.paymentAmount);
					serializer.attribute(null, "RefNo", "" + payment.RefNo);
					serializer.attribute(null, "AuthCode", ""
							+ payment.AuthCode);
					serializer.attribute(null, "RecordNo", ""
							+ payment.RecordNo);
					serializer.attribute(null, "AcqRefData", ""
							+ payment.AcqRefData);
					serializer.attribute(null, "ProcessData", ""
							+ payment.ProcessData);
					serializer.attribute(null, "InvoiceNo", ""
							+ payment.InvoiceNo);
					serializer.endTag(null, "Payment");
				}
				serializer.endTag(null, "Payments");

				serializer.startTag(null, "LineItems");

				for (int i = 0; i < cart.getProducts().size(); i++) {

					Product product = cart.getProducts().get(i);

					serializer.startTag(null, "Item");
					serializer.attribute(null, "line", "" + (i + 1));
					serializer.attribute(null, "isNote", "" + product.isNote);
					serializer.attribute(null, "itemId", "" + product.id);
					serializer.attribute(null, "name", product.name);
					serializer.attribute(null, "department", "" + product.cat);
					serializer.attribute(null, "discount", ""
							+ product.discount);
					serializer.attribute(null, "subdiscount", ""
							+ product.subdiscount);
					serializer.attribute(null, "quantity", ""
							+ product.quantity);
					serializer.attribute(null, "barcode", product.barcode);
					serializer.attribute(null, "price", "" + product.price);
					serializer.attribute(null, "cost", "" + product.cost);
					serializer.attribute(null, "salePrice", ""
							+ product.salePrice);
					serializer.attribute(null, "startSale", ""
							+ product.startSale);
					serializer.attribute(null, "endSale", "" + product.endSale);
					serializer.endTag(null, "Item");
				}

				serializer.endTag(null, "LineItems");

				serializer.startTag(null, "SubDiscount");
				serializer.attribute(null, "percent", ""
						+ cart.subtotaldiscount);
				serializer.endTag(null, "SubDiscount");

				serializer.endTag(null, "SaleData");
				serializer.endDocument();
				// write xml data into the FileOutputStream

				String result = writer.toString();

				if (TaxSetting.getTax1name() != null) {
					if (!TaxSetting.getTax1name().equals("")) {
						cart.taxName1 = (TaxSetting.getTax1name());
						cart.tax1 = (int) (cart.taxable1SubTotal
								* TaxSetting.getTax1() / 100f);
						cart.taxPercent1 = TaxSetting.getTax1();
					}
				}

				if (TaxSetting.getTax2name() != null) {
					if (!TaxSetting.getTax2name().equals("")) {
						cart.taxName2 = (TaxSetting.getTax2name());
						cart.tax2 = (int) (cart.taxable2SubTotal
								* TaxSetting.getTax2() / 100f);
						cart.taxPercent2 = TaxSetting.getTax2();
					}
				}

				if (TaxSetting.getTax3name() != null) {
					if (!TaxSetting.getTax3name().equals("")) {
						cart.taxName3 = (TaxSetting.getTax3name());
						cart.tax3 = (int) (cart.taxable3SubTotal
								* TaxSetting.getTax3() / 100f);
						cart.taxPercent3 = TaxSetting.getTax3();
					}
				}

				// cart.setTotal(total);

				if (cart.voided == false) {
					if (cart.hasCustomer()) {
						if (cart.total > 0) {
							cart.getCustomer().sales++;
						} else {
							cart.getCustomer().returns++;
						}

						cart.getCustomer().total += cart.total;

						ProductDatabase.replaceCustomer(cart.getCustomer());
					}
				}

				cart.onHold = false;
				getShop().insertSale(cart, result);

			} catch (Exception e) {
				alertbox("Exception", "error occurred while creating xml file");
				Log.e("Exception", "error occurred while creating xml file");
				e.printStackTrace();
			}
		}
	}

	protected void saveSale(ShopCart cart) {
		saleProcessed = true;

		if (!cashier.name.equals(getResources().getString(
				R.string.training_cash))) {
			// we create a XmlSerializer in order to write xml data
			XmlSerializer serializer = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			try {
				serializer.setOutput(writer);
				serializer.startDocument(null, Boolean.valueOf(true));
				serializer
						.setFeature(
								"http://xmlpull.org/v1/doc/features.html#indent-output",
								true);
				serializer.startTag(null, "SaleData");

				if (cart.hasCustomer()) {
					serializer.startTag(null, "Customer");
					serializer
							.attribute(null, "id", "" + cart.getCustomer().id);
					serializer.attribute(null, "name", cart.getCustomer().name);
					serializer.attribute(null, "email",
							cart.getCustomer().email);
					serializer.endTag(null, "Customer");
				}

				serializer.startTag(null, "Payments");

				for (int p = 0; p < cart.Payments.size(); p++) {
					Payment payment = cart.Payments.get(p);

					serializer.startTag(null, "Payment");
					serializer.attribute(null, "paymentType",
							payment.paymentType);
					serializer.attribute(null, "paymentAmount", ""
							+ payment.paymentAmount);
					serializer.attribute(null, "RefNo", "" + payment.RefNo);
					serializer.attribute(null, "AuthCode", ""
							+ payment.AuthCode);
					serializer.attribute(null, "RecordNo", ""
							+ payment.RecordNo);
					serializer.attribute(null, "AcqRefData", ""
							+ payment.AcqRefData);
					serializer.attribute(null, "ProcessData", ""
							+ payment.ProcessData);
					serializer.attribute(null, "InvoiceNo", ""
							+ payment.InvoiceNo);
					serializer.endTag(null, "Payment");
				}
				serializer.endTag(null, "Payments");

				serializer.startTag(null, "LineItems");

				for (int i = 0; i < cart.getProducts().size(); i++) {

					Product product = cart.getProducts().get(i);

					serializer.startTag(null, "Item");
					serializer.attribute(null, "line", "" + (i + 1));
					serializer.attribute(null, "isNote", "" + product.isNote);
					serializer.attribute(null, "itemId", "" + product.id);
					serializer.attribute(null, "name", product.name);
					serializer.attribute(null, "department", "" + product.cat);
					serializer.attribute(null, "discount", ""
							+ product.discount);
					serializer.attribute(null, "subdiscount", ""
							+ product.subdiscount);
					serializer.attribute(null, "quantity", ""
							+ product.quantity);
					serializer.attribute(null, "barcode", product.barcode);
					serializer.attribute(null, "price", "" + product.price);
					serializer.attribute(null, "cost", "" + product.cost);
					serializer.attribute(null, "salePrice", ""
							+ product.salePrice);
					serializer.attribute(null, "startSale", ""
							+ product.startSale);
					serializer.attribute(null, "endSale", "" + product.endSale);
					serializer.endTag(null, "Item");
				}

				serializer.endTag(null, "LineItems");

				serializer.startTag(null, "SubDiscount");
				serializer.attribute(null, "percent", ""
						+ cart.subtotaldiscount);
				serializer.endTag(null, "SubDiscount");

				serializer.endTag(null, "SaleData");
				serializer.endDocument();
				// write xml data into the FileOutputStream

				String result = writer.toString();

				// cart.setSubTotal(cart.subTotal);

				if (TaxSetting.getTax1name() != null) {
					if (!TaxSetting.getTax1name().equals("")) {
						cart.taxName1 = (TaxSetting.getTax1name());
						cart.tax1 = (int) (cart.taxable1SubTotal
								* TaxSetting.getTax1() / 100f);
						cart.taxPercent1 = TaxSetting.getTax1();
					}
				}

				if (TaxSetting.getTax2name() != null) {
					if (!TaxSetting.getTax2name().equals("")) {
						cart.taxName2 = (TaxSetting.getTax2name());
						cart.tax2 = (int) (cart.taxable2SubTotal
								* TaxSetting.getTax2() / 100f);
						cart.taxPercent2 = TaxSetting.getTax2();
					}
				}

				if (TaxSetting.getTax3name() != null) {
					if (!TaxSetting.getTax3name().equals("")) {
						cart.taxName3 = (TaxSetting.getTax3name());
						cart.tax3 = (int) (cart.taxable3SubTotal
								* TaxSetting.getTax3() / 100f);
						cart.taxPercent3 = TaxSetting.getTax3();
					}
				}

				// cart.setTotal(total);

				if (cart.voided == false) {
					if (cart.hasCustomer()) {
						if (cart.total > 0) {
							cart.getCustomer().sales++;
						} else {
							cart.getCustomer().returns++;
						}

						cart.getCustomer().total += cart.total;

						ProductDatabase.replaceCustomer(cart.getCustomer());
					}
				}

				cart.Cashier = cashier;

				if (cart.voided == false) {
					if (cart.Cashier != null) {
						if (cart.total > 0) {
							cart.Cashier.sales++;
						} else {
							cart.Cashier.returns++;
						}

						cart.Cashier.total += cart.total;

						if (cart.Cashier.id > 0)
							ProductDatabase.replaceCashier(cart.Cashier);
					}
				}

				cart.onHold = false;
				getShop().insertSale(cart, result);

			} catch (Exception e) {
				alertbox("Exception", "error occurred while creating xml file");
				Log.e("Exception", "error occurred while creating xml file");
				e.printStackTrace();
			}

			if (cart.voided == false) {
				for (int i = 0; i < cart.getProducts().size(); i++) {
					if (cart.getProducts().get(i).id > 0) {
						Product oldProduct = cart.getProducts().get(i);

						Cursor c = ProductDatabase.getProdByName(""
								+ oldProduct.id);
						if (c != null) {
							Product product = new Product();

							product.price = Long.valueOf(c.getString(c
									.getColumnIndex("price")));
							product.salePrice = c.getLong(c
									.getColumnIndex("salePrice"));
							product.endSale = c.getLong(c
									.getColumnIndex("saleEndDate"));
							product.startSale = c.getLong(c
									.getColumnIndex("saleStartDate"));

							product.cost = Long.valueOf(c.getString(c
									.getColumnIndex("cost")));
							product.id = c.getInt(c.getColumnIndex("_id"));
							product.barcode = (c.getString(c
									.getColumnIndex("barcode")));
							product.name = (c.getString(c
									.getColumnIndex("name")));
							product.desc = (c.getString(c
									.getColumnIndex("desc")));
							product.onHand = (c.getInt(c
									.getColumnIndex("quantity")));
							product.cat = (c.getInt(c.getColumnIndex("catid")));
							product.buttonID = (c.getInt(c
									.getColumnIndex("buttonID")));
							product.lastSold = (c.getInt(c
									.getColumnIndex("lastSold")));
							product.lastReceived = (c.getInt(c
									.getColumnIndex("lastReceived")));
							product.lowAmount = (c.getInt(c
									.getColumnIndex("lowAmount")));
							c.close();

							product.onHand -= oldProduct.quantity;

							ProductDatabase.replaceItem(product);
						}
					}
				}
			}
		} else {
			cart.Cashier = cashier;
		}

		if (ReceiptSetting.enabled && EmailSetting.isEnabled()) {
			if (cart.hasCustomer()) {
				if (cart.getCustomerEmail().contains("@")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setInverseBackgroundForced(true);

					builder.setMessage(
							getResources().getString(R.string.print_and_email))
							.setCancelable(false)
							.setPositiveButton(
									getResources().getString(
											R.string.send_and_print),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											todo = 3;
											pd = ProgressDialog
													.show(PointOfSale.this,
															"",
															getResources()
																	.getString(
																			R.string.processing_receipts),
															true, false);
											Thread thread = new Thread(
													PointOfSale.this);
											thread.start();
										}
									})
							.setNegativeButton("Print Only",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											if (EmailSetting.bookkeeper) {
												todo = 3;
												pd = ProgressDialog
														.show(PointOfSale.this,
																"",
																getResources()
																		.getString(
																				R.string.processing_receipts),
																true, false);
												Thread thread = new Thread(
														PointOfSale.this);
												thread.start();
											} else {
												todo = 2;
												pd = ProgressDialog
														.show(PointOfSale.this,
																"",
																getResources()
																		.getString(
																				R.string.print_receipt),
																true, false);
												Thread thread = new Thread(
														PointOfSale.this);
												thread.start();
											}
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					if (EmailSetting.bookkeeper) {
						todo = 3;
						pd = ProgressDialog.show(
								PointOfSale.this,
								"",
								getResources().getString(
										R.string.processing_receipts), true,
								false);
						Thread thread = new Thread(PointOfSale.this);
						thread.start();
					} else {
						todo = 2;
						pd = ProgressDialog.show(PointOfSale.this, "",
								getResources()
										.getString(R.string.print_receipt),
								true, false);
						Thread thread = new Thread(PointOfSale.this);
						thread.start();
					}
				}
			} else {
				if (EmailSetting.bookkeeper) {
					todo = 3;
					pd = ProgressDialog.show(
							PointOfSale.this,
							"",
							getResources().getString(
									R.string.processing_receipts), true, false);
					Thread thread = new Thread(PointOfSale.this);
					thread.start();
				} else {
					todo = 2;
					pd = ProgressDialog.show(PointOfSale.this, "",
							getResources().getString(R.string.print_receipt),
							true, false);
					Thread thread = new Thread(PointOfSale.this);
					thread.start();
				}
			}
		}

		else if (ReceiptSetting.enabled) {
			if (EmailSetting.bookkeeper) {
				todo = 3;
				pd = ProgressDialog.show(PointOfSale.this, "", getResources()
						.getString(R.string.processing_receipts), true, false);
				Thread thread = new Thread(PointOfSale.this);
				thread.start();
			} else {
				todo = 2;
				pd = ProgressDialog.show(PointOfSale.this, "", getResources()
						.getString(R.string.print_receipt), true, false);
				Thread thread = new Thread(PointOfSale.this);
				thread.start();
			}
		}

		else if (EmailSetting.isEnabled()) {
			if (cart.hasCustomer() || EmailSetting.bookkeeper) {
				todo = 1;
				pd = ProgressDialog.show(PointOfSale.this, "", getResources()
						.getString(R.string.send_email), true, false);
				Thread thread = new Thread(PointOfSale.this);
				thread.start();
			} else {
				saleDone();
				// finlizeSale();
			}
		} else {
			saleDone();
			// finlizeSale();
		}
	}

	private boolean Print() {
		for (int i = 0; i < cart.Payments.size(); i++) {
			if (cart.Payments.get(i).Print) {
				EscPosDriver.Print(PrintCharge(cart.Payments.get(i)));
			}
		}
		return EscPosDriver.PrintReceipt(getCart());
	}

	private boolean issueEmailReceipt(ShopCart cart) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (EmailSetting.isEnabled()) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork != null
					&& activeNetwork.isConnectedOrConnecting()) {
				Mail m = new Mail(EmailSetting.getSmtpUsername(),
						EmailSetting.getSmtpPasword());
				m.setServer(EmailSetting.getSmtpServer(),
						EmailSetting.getSmtpPort());
				m.setSubject(EmailSetting.getSmtpSubject());
				String toSend = cart.getBody();

				String send = toSend.replaceAll(" ", "&nbsp;");
				send = "<P style=\"font-family:courier\">" + send + "</P>";
				m.setBody(send);

				ArrayList<String> sendto = new ArrayList<String>();

				if (cart.Customer != null
						&& cart.getCustomerEmail().contains("@"))
					sendto.add(cart.getCustomerEmail());
				if (EmailSetting.bookkeeper)
					sendto.add(EmailSetting.getSmtpEmail());
				String[] stockArr = new String[sendto.size()];

				m.setTo(sendto.toArray(stockArr));
				m.setFrom(EmailSetting.getSmtpEmail());
				try {
					return m.send();
				} catch (Exception e) {
					return false;
				}

			} else {
				if (cart.hasCustomer()) {
					String email = cart.getCustomerEmail();
					if (email.contains("@")) {
						return false;
					}
				}
			}
		}

		return false;
	}

	protected static void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(me)
				.setMessage(mymessage)
				.setInverseBackgroundForced(true)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	@Override
	public void onResume() {
		EscPosDriver.closeBTConnections();

		if (cashier != null) {
			cashierName.setText(cashier.name);
		}

		ArrayList<Cashier> cashiers = ProductDatabase.getCashiers();

		if (cashiers.size() > 0 && AdminSetting.enabled) {
			if (!loggedIn) {
				showLoginScreen();
			} else {
				showSalesScreen();
			}
			CashierEnabled = true;
			// LinearLayout cView = (LinearLayout)
			// findViewById(R.id.cashierView);
			// cView.setVisibility(View.VISIBLE);

			ArrayAdapter<Cashier> cashierSpinAdapter = new ArrayAdapter<Cashier>(
					this, R.layout.spiner, cashiers);
			cashierSpinAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			cashierSpin.setAdapter(cashierSpinAdapter);
		} else {
			CashierEnabled = false;
			Cashier cashier = new Cashier();
			cashier.name = getResources().getString(R.string.administrator);
			cashier.pin = AdminSetting.password;
			cashier.permissionInventory = true;
			cashier.permissionPriceModify = true;
			cashier.permissionReports = true;
			cashier.permissionReturn = true;
			cashier.permissionSettings = true;

			loginCashier(cashier);
		}

		if (CashierEnabled == false) {
			LogoutButton.setVisibility(View.INVISIBLE);
			logoutText.setVisibility(View.INVISIBLE);
		} else {
			LogoutButton.setVisibility(View.VISIBLE);
			logoutText.setVisibility(View.VISIBLE);
		}

		if (cart.hasCustomer()) {
			LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
			cView.setVisibility(View.VISIBLE);
		}

		((ProductAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
		((ButtonAdaptor) quickButtons.getAdapter())
				.changeCursor(ProductDatabase.getButtons(0));

		updateTotals();

		if (keyAmount.equals(""))
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
		else
			keyAmountView.setText(StoreSetting.getCurrency()
					+ df.format(Long.valueOf(keyAmount) / 100d));

		boolean registered = mSharedPreferences.getBoolean("APOS_REGISTERED",
				false);
		boolean licensed = mSharedPreferences
				.getBoolean("APOS_LICENSED", false);
		long login_last = mSharedPreferences.getLong("APOS_LASTLOG", 0);

		if (registered && licensed) {
			long now = new Date().getTime();
			if (now > (login_last + 60 * 60 * 6 * 1000)) {
				if (hasInternet()) {
					// pd = ProgressDialog.show(this, "",
					// "Checking for update...", true, false);
					if (sendingLogin == false) {
						sendingLogin = true;
						asyncTask = new SendLogin().execute("");
					}
				}
			}
		}

		if (PrioritySetting.enabled) {
			if (ProductDatabase.preAuthCount() > 0) {
				if (hasInternet()) {
					alertbox(getResources().getString(R.string.pre_auth_title),
							getResources().getString(R.string.pre_auth_message));
				}
			}
		}
		
		super.onResume();
	}

	private void showSalesScreen() {
		isLoginScreen = false;
		loginScreen.setVisibility(View.GONE);

		if (keyBoardView) {
			salesScreen.setVisibility(View.GONE);
			if (buttonScreen != null)
				buttonScreen.setVisibility(View.VISIBLE);
		} else {
			salesScreen.setVisibility(View.VISIBLE);
			if (buttonScreen != null)
				buttonScreen.setVisibility(View.GONE);
		}

		supportInvalidateOptionsMenu();

	}

	private void showLoginScreen() {
		isLoginScreen = true;
		salesScreen.setVisibility(View.GONE);
		if (buttonScreen != null)
			buttonScreen.setVisibility(View.GONE);
		loginScreen.setVisibility(View.VISIBLE);
		supportInvalidateOptionsMenu();
	}

	private void updateTotals() {
		cart.subTotal = 0;
		cart.taxable1SubTotal = 0;
		cart.taxable2SubTotal = 0;
		cart.taxable3SubTotal = 0;

		dateTimeArea.setVisibility(View.GONE);
		dateTimeText.setText("");
		invoiceText.setText("");

		if (cart.hasCustomer()) {

			dateTimeArea.setVisibility(View.VISIBLE);
			long currentDateTime = new Date().getTime();
			cart.date = currentDateTime;

			dateTimeText.setText(DateFormat.getDateTimeInstance().format(
					new Date(cart.date)));

			if (!saleProcessed)
				cart.trans = ProductDatabase.getSaleNumber() + 1;

			invoiceText.setText("Trans. #" + cart.trans);

			TextView cNameView = (TextView) findViewById(R.id.customerName);
			TextView cEmailView = (TextView) findViewById(R.id.customerEmail);
			TextView cSalesView = (TextView) findViewById(R.id.customerSales);
			TextView cReturnsView = (TextView) findViewById(R.id.customerReturns);
			TextView cTotalView = (TextView) findViewById(R.id.customerTotal);
			LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);

			cView.setVisibility(View.VISIBLE);

			cNameView.setText(cart.Customer.name);
			cEmailView.setText(cart.Customer.email);
			cSalesView.setText(getResources().getString(R.string.sales) + " "
					+ cart.Customer.sales);
			cReturnsView.setText(getResources().getString(R.string.returns)
					+ " " + cart.Customer.returns);
			cTotalView.setText(getResources().getString(R.string.total_amount)
					+ " " + StoreSetting.getCurrency()
					+ df.format(cart.Customer.total / 100f));
		}

		long nonDiscountTotal = 0;

		for (int i = 0; i < getCart().getProducts().size(); i++) {
			dateTimeArea.setVisibility(View.VISIBLE);
			long currentDateTime = new Date().getTime();
			cart.date = currentDateTime;

			dateTimeText.setText(DateFormat.getDateTimeInstance().format(
					new Date(cart.date)));

			if (!saleProcessed)
				cart.trans = ProductDatabase.getSaleNumber() + 1;

			invoiceText.setText("Trans. #" + cart.trans);

			Product item = getCart().getProducts().get(i);

			item.subdiscount = cart.subtotaldiscount;
			cart.subTotal += item.itemTotal(cart.date);
			nonDiscountTotal += item.itemNonDiscountTotal(cart.date);

			if (item.cat != 0) {
				String cat = ProductDatabase.getCatById(item.cat);
				int catPos = ProductDatabase.getCatagoryString().indexOf(cat);

				if (catPos > -1) {
					if (ProductDatabase.getCatagories().get(catPos)
							.getTaxable1()) {
						if (item.taxable)
							cart.taxable1SubTotal += item.itemTotal(cart.date);
					}

					if (ProductDatabase.getCatagories().get(catPos)
							.getTaxable2()) {
						if (item.taxable)
							cart.taxable2SubTotal += item.itemTotal(cart.date);
					}

					if (ProductDatabase.getCatagories().get(catPos)
							.getTaxable3()) {
						if (item.taxable)
							cart.taxable3SubTotal += item.itemTotal(cart.date);
					}
				}
			}
		}

		cart.total = cart.subTotal;

		((TextView) findViewById(R.id.subtotal_amount)).setText(StoreSetting
				.getCurrency() + df.format(cart.subTotal / 100f));

		TableRow discount_view = (TableRow) findViewById(R.id.discountSubView);
		TextView discount_percent = (TextView) findViewById(R.id.discount_percent);
		TextView discount_amount = (TextView) findViewById(R.id.discount_amount);

		if (cart.subtotaldiscount > 0) {
			discount_view.setVisibility(View.VISIBLE);
			discount_percent
					.setText(getResources().getString(R.string.discount)
							+ cart.subtotaldiscount + "%");
			discount_amount.setText(StoreSetting.getCurrency()
					+ df.format((cart.subTotal - nonDiscountTotal) / 100f));
		} else {
			discount_view.setVisibility(View.GONE);
		}

		TableRow taxR1 = (TableRow) findViewById(R.id.tax1table);
		TextView tax1Amount = (TextView) findViewById(R.id.tax1_amount);
		TextView tax1Name = (TextView) findViewById(R.id.tax1_name);

		if (TaxSetting.getTax1name() != null) {
			if (!TaxSetting.getTax1name().equals("")) {
				taxR1.setVisibility(View.VISIBLE);
				long taxamount1 = (long) ((double) cart.taxable1SubTotal
						* TaxSetting.getTax1() / 100d);
				cart.total += taxamount1;
				tax1Name.setText(TaxSetting.getTax1name());
				tax1Amount.setText(StoreSetting.getCurrency()
						+ df.format(taxamount1 / 100f));
			} else {
				taxR1.setVisibility(View.GONE);
			}
		} else {
			taxR1.setVisibility(View.GONE);
		}

		TableRow taxR2 = (TableRow) findViewById(R.id.tax2table);
		TextView tax2Amount = (TextView) findViewById(R.id.tax2_amount);
		TextView tax2Name = (TextView) findViewById(R.id.tax2_name);

		if (TaxSetting.getTax2name() != null) {
			if (!TaxSetting.getTax2name().equals("")) {
				taxR2.setVisibility(View.VISIBLE);
				long taxamount2 = (long) ((double) cart.taxable2SubTotal
						* TaxSetting.getTax2() / 100d);
				cart.total += taxamount2;
				tax2Name.setText(TaxSetting.getTax2name());
				tax2Amount.setText(StoreSetting.getCurrency()
						+ df.format(taxamount2 / 100f));
			} else {
				taxR2.setVisibility(View.GONE);
			}
		} else {
			taxR2.setVisibility(View.GONE);
		}

		TableRow taxR3 = (TableRow) findViewById(R.id.tax3table);
		TextView tax3Amount = (TextView) findViewById(R.id.tax3_amount);
		TextView tax3Name = (TextView) findViewById(R.id.tax3_name);

		if (TaxSetting.getTax3name() != null) {
			if (!TaxSetting.getTax3name().equals("")) {
				taxR3.setVisibility(View.VISIBLE);
				long taxamount3 = (long) ((double) cart.taxable3SubTotal
						* TaxSetting.getTax3() / 100d);
				cart.total += taxamount3;
				tax3Name.setText(TaxSetting.getTax3name());
				tax3Amount.setText(StoreSetting.getCurrency()
						+ df.format(taxamount3 / 100f));
			} else {
				taxR3.setVisibility(View.GONE);
			}
		} else {
			taxR3.setVisibility(View.GONE);
		}

		long paymentSum = 0;

		TableLayout paymentsTable = (TableLayout) findViewById(R.id.paymentsTable);

		if (previewTotal != null) {
			if (cart.Products.size() > 0) {
				Product product = cart.Products.get(cart.Products.size() - 1);
				// previewItemView.setVisibility(View.VISIBLE);
				previewItemName.setText("x" + product.quantity + " "
						+ product.name);
				previewTotal.setText(StoreSetting.getCurrency()
						+ df.format((cart.total) / 100d));
				previewItemPrice.setText(product.displayTotal(cart.date));
			} else {
				// previewItemView.setVisibility(View.GONE);
				previewItemName.setText("");
				previewTotal.setText(StoreSetting.getCurrency()
						+ df.format((cart.total) / 100d));
				previewItemPrice.setText("");
			}
		}

		if (cart.Payments.size() > 0) {
			removePaymentButton.setVisibility(View.VISIBLE);
			paymentsTable.setVisibility(View.VISIBLE);
			paymentsTable.removeAllViews();
			for (int p = 0; p < cart.Payments.size(); p++) {
				TableRow row = new TableRow(this);
				paymentsTable.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

				TextView tv1 = new TextView(this);
				TextView tv2 = new TextView(this);

				tv1.setText(cart.Payments.get(p).paymentType);
				tv1.setGravity(Gravity.RIGHT);
				tv1.setTextAppearance(this, R.style.textLayoutAppearanceBigger);
				tv1.setLayoutParams(new TableRow.LayoutParams(0,
						LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv1);

				tv2.setText(StoreSetting.getCurrency()
						+ df.format(cart.Payments.get(p).paymentAmount / 100d));
				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(this, R.style.textLayoutAppearanceBigger);
				tv2.setLayoutParams(new TableRow.LayoutParams(0,
						LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv2);

				paymentSum += cart.Payments.get(p).paymentAmount;
			}

			if (cart.total - paymentSum < 0 || cart.total < 0) {
				TableRow row = new TableRow(this);
				paymentsTable.addView(row);
				row.setLayoutParams(new TableRow.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

				TextView tv1 = new TextView(this);
				TextView tv2 = new TextView(this);

				tv1.setText(getResources().getString(R.string.change));
				tv1.setGravity(Gravity.RIGHT);
				tv1.setTextAppearance(this, R.style.textLayoutAppearanceBigger);
				tv1.setLayoutParams(new TableRow.LayoutParams(0,
						LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv1);

				tv2.setText(StoreSetting.getCurrency()
						+ df.format((cart.total - paymentSum) / 100d));
				if (cart.total < 0)
					tv2.setText(StoreSetting.getCurrency()
							+ df.format((cart.total) / 100d));

				tv2.setGravity(Gravity.RIGHT);
				tv2.setTextAppearance(this, R.style.textLayoutAppearanceBigger);
				tv2.setLayoutParams(new TableRow.LayoutParams(0,
						LayoutParams.WRAP_CONTENT, 1f));
				row.addView(tv2);
			}
		} else {
			paymentsTable.setVisibility(View.GONE);
			removePaymentButton.setVisibility(View.INVISIBLE);
		}

		if (voiding)
			((TextView) findViewById(R.id.total_amount)).setText(getResources()
					.getString(R.string.voided_cap)
					+ " "
					+ StoreSetting.getCurrency()
					+ df.format((cart.total) / 100f));
		else
			((TextView) findViewById(R.id.total_amount)).setText(StoreSetting
					.getCurrency() + df.format((cart.total) / 100f));

		if (cart.getProducts().size() > 0) {
			Product test1 = cart.getProducts().get(
					cart.getProducts().size() - 1);

			StringBuffer message = new StringBuffer(
					"                                        ");

			message.replace(0, test1.name.length() - 1, test1.name);
			message.replace(message.length() / 2
					- test1.displayPrice(cart.date).length(), 19,
					test1.displayPrice(cart.date));

			String total = getResources().getString(R.string.total) + ": "
					+ StoreSetting.getCurrency() + df.format(cart.total / 100f);

			message.replace(20, 39, total);

			onPrintTextDisplay(message.toString());
		} else {
			StringBuffer message = new StringBuffer(
					"                                        ");
			String welcome1 = getResources().getString(R.string.welcome);
			String store = StoreSetting.getName();
			if (store.equals("")) {
				store = "Our Store!";
			}

			if (welcome1.length() > 20)
				welcome1 = welcome1.substring(0, 19);

			int start = 9 - welcome1.length() / 2;
			message.replace(start, start + welcome1.length() - 1, welcome1);

			if (store.length() > 20)
				store = store.substring(0, 19);

			start = 29 - store.length() / 2;
			message.replace(start, start + store.length() - 1, store);

			onPrintTextDisplay(message.toString());
		}

		if (voiding) {
			if (!saleProcessed) {
				cart.voided = true;
				saveSale(cart);
			}
		} else {
			if (!saleProcessed) {
				if (cart.getProducts().size() > 0 && cart.Payments.size() > 0) {
					if (paymentSum >= cart.total) {
						saveSale(cart);
					} else {
						alertbox(
								getResources().getString(R.string.amount_due),
								getResources()
										.getString(R.string.amount_due_m1)
										+ " "
										+ StoreSetting.getCurrency()
										+ df.format((cart.total - paymentSum) / 100d)
										+ " "
										+ getResources().getString(
												R.string.amount_due_m2));
					}
				}
			}
		}

		if (saleProcessed) {
			// cancelvoid.setText(getResources().getString(R.string.clear_sale));
			voidImage.setBackgroundResource(R.drawable.button_yellow_selector);
			voidImage.setImageResource(R.drawable.clear);
			if (ReceiptSetting.enabled) {
				reprintButton.setVisibility(View.VISIBLE);
				reprintText.setVisibility(View.VISIBLE);
			}

			if (EmailSetting.isEnabled()) {
				emailButton.setVisibility(View.VISIBLE);
			}
		}
	}

	public class ProductAdapter extends ArrayAdapter<Product> {

		public ProductAdapter(Context context, int textViewResourceId,
				ArrayList<Product> products) {
			super(context, 0, products);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final Product item = getItem(position);

			View result = convertView;
			if (result == null) {
				result = View.inflate(getContext(), R.layout.inventoryitemview,
						null);
			}

			final TextView prod_name = ((TextView) result
					.findViewById(R.id.prod_name));
			final TextView prod_desc = ((TextView) result
					.findViewById(R.id.prod_desc));
			final TextView prod_quantnum = ((TextView) result
					.findViewById(R.id.prod_quantnum));
			final TextView prod_quantnum2 = ((TextView) result
					.findViewById(R.id.prod_quantnum2));
			final TextView prod_price = ((TextView) result
					.findViewById(R.id.prod_price));
			final TextView prod_pricenum = ((TextView) result
					.findViewById(R.id.prod_pricenum));

			if (!item.isNote) {
				prod_desc.setVisibility(View.VISIBLE);
				prod_quantnum.setVisibility(View.VISIBLE);
				prod_quantnum2.setVisibility(View.VISIBLE);

				prod_price.setVisibility(View.VISIBLE);
				prod_pricenum.setVisibility(View.VISIBLE);

				prod_name.setText(item.name);
				prod_desc.setText(item.desc);
				prod_quantnum.setText(item.quantity + "x @");
				prod_price.setText(item.displayPrice(cart.date));
				prod_pricenum.setText(item.displayTotal(cart.date));
			} else {
				prod_name.setText(item.name);
				prod_desc.setVisibility(View.GONE);
				prod_quantnum.setVisibility(View.GONE);
				prod_quantnum2.setVisibility(View.GONE);
				prod_price.setVisibility(View.GONE);
				prod_pricenum.setVisibility(View.GONE);
			}

			if (item.itemTotal(cart.date) < 0) {
				prod_name.setTextColor(Color.RED);
				prod_desc.setTextColor(Color.RED);
				prod_quantnum.setTextColor(Color.RED);
				prod_quantnum2.setTextColor(Color.RED);
				prod_price.setTextColor(Color.RED);
				prod_pricenum.setTextColor(Color.RED);
			} else {
				prod_name.setTextColor(Color.BLACK);
				prod_desc.setTextColor(Color.BLACK);
				prod_quantnum.setTextColor(Color.BLACK);
				prod_quantnum2.setTextColor(Color.BLACK);
				prod_price.setTextColor(Color.BLACK);
				prod_pricenum.setTextColor(Color.BLACK);
			}

			final ImageView expandButton = (ImageView) result
					.findViewById(R.id.imageButton1);
			final LinearLayout itemDetails = (LinearLayout) result
					.findViewById(R.id.itemDetails);
			final LinearLayout hiddenView = (LinearLayout) result
					.findViewById(R.id.extendView);
			final LinearLayout editItemDetails = (LinearLayout) result
					.findViewById(R.id.editItemDetails);

			final EditText editPrice = (EditText) result
					.findViewById(R.id.expandEditPrice);
			final EditText editQuantity = (EditText) result
					.findViewById(R.id.expandEditQuan);
			final EditText editDiscount = (EditText) result
					.findViewById(R.id.expandEditDiscount);

			if (item.isNote) {
				editItemDetails.setVisibility(View.GONE);
			} else {
				editItemDetails.setVisibility(View.VISIBLE);
			}

			if (item.expandBar) {
				hiddenView.setVisibility(View.VISIBLE);
				expandButton
						.setImageResource(R.drawable.ic_menu_close_clear_cancel);
				if (!cashier.permissionPriceModify || item.price == 0) {
					editPrice.setEnabled(false);
					editDiscount.setEnabled(false);
				} else {
					editPrice.setEnabled(true);
					editDiscount.setEnabled(true);
				}
			} else {
				expandButton.setImageResource(R.drawable.ic_menu_more);
				hiddenView.setVisibility(View.GONE);
			}

			itemDetails.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!item.expandBar) {
						hiddenView.setVisibility(View.VISIBLE);
						expandButton
								.setImageResource(R.drawable.ic_menu_close_clear_cancel);
						item.expandBar = true;
						if (!cashier.permissionPriceModify || item.price == 0) {
							editPrice.setEnabled(false);
							editDiscount.setEnabled(false);
						} else {
							editPrice.setEnabled(true);
							editDiscount.setEnabled(true);
						}
					} else {
						hiddenView.setVisibility(View.GONE);
						expandButton.setImageResource(R.drawable.ic_menu_more);
						item.expandBar = false;
					}
				}
			});

			editPrice.setText(df.format(item.itemPrice(cart.date) / 100f)
					.replaceAll(",", "."));
			editDiscount.setText(df.format(item.discount).replaceAll(",", "."));
			if (item.price == 0) {
				editPrice.setText(df.format(0).replaceAll(",", "."));
				editDiscount.setText(df.format(0).replaceAll(",", "."));
			}
			double discountEdited = item.discount;
			editDiscount.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

					if (editDiscount.hasFocus()) {
						if (editDiscount.getText().toString().equals(".")) {
							editDiscount.setText("0.");
							editDiscount.setSelection(2);
						} else if (!editDiscount.getText().toString()
								.equals("")) {
							if (!editDiscount.getText().toString().equals("-")) {
								item.tempDiscount = Float.valueOf(editDiscount
										.getText().toString());
								int alt = 1;
								if (item.tempDiscount < 0)
									alt = -1;
								else
									alt = 1;
								long newPrice = item.price
										- (long) (item.price
												* (item.tempDiscount / 100f) + 0.5f * alt);

								editPrice.setText(df.format(newPrice / 100f)
										.replaceAll(",", "."));
							}
						}
					}
				}
			});

			editPrice.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (editPrice.hasFocus()) {

						if (editPrice.getText().toString().equals(".")) {
							editPrice.setText("0.");
							editPrice.setSelection(2);
						} else if (!editPrice.getText().toString().equals("")) {

							double value = Double.valueOf(editPrice.getText()
									.toString());
							long newPrice = Math.round(value * 100);
							item.tempDiscount = 100 - ((double) newPrice
									/ (double) (item.price) * 100d);
							editDiscount.setText(df.format(item.tempDiscount)
									.replaceAll(",", "."));
						}
					}
				}
			});

			Button setButton = (Button) result.findViewById(R.id.setItem);
			setButton.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_product_price_edit));
						return;
					}

					if (!editQuantity.getText().toString().equals("")) {
						int quantity = Integer.valueOf(editQuantity.getText()
								.toString());
						item.quantity = quantity;
					} else {
						item.quantity = 1;
					}

					item.discount = item.tempDiscount;

					((ProductAdapter) inventoryList.getAdapter())
							.notifyDataSetChanged();
					updateTotals();
				}
			});

			Button deleteButton = (Button) result.findViewById(R.id.deleteItem);
			deleteButton.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_product_price_edit));
						return;
					}

					cart.RemoveProduct(position);
					((ProductAdapter) inventoryList.getAdapter())
							.notifyDataSetChanged();
					updateTotals();
				}
			});

			if (item.isNote) {
				setButton.setVisibility(View.INVISIBLE);
			} else {
				setButton.setVisibility(View.VISIBLE);
			}

			final TextView quanityText = (TextView) result
					.findViewById(R.id.expandEditQuantityLabel);

			editQuantity.setText("" + item.quantity);
			quanityText.setText(getResources().getString(R.string.on_hand)
					+ ": " + item.onHand);

			Button add = (Button) result.findViewById(R.id.expandPlus);
			add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_product_price_edit));
						return;
					}

					if (editQuantity.getText().toString().equals(""))
						editQuantity.setText("1");

					int quantity = Integer.valueOf(editQuantity.getText()
							.toString());
					quantity++;
					editQuantity.setText("" + quantity);
				}
			});

			Button minus = (Button) result.findViewById(R.id.expandMinus);
			minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_product_price_edit));
						return;
					}

					if (editQuantity.getText().toString().equals(""))
						editQuantity.setText("1");

					int quantity = Integer.valueOf(editQuantity.getText()
							.toString());
					quantity--;
					editQuantity.setText("" + quantity);
				}
			});

			return result;
		}
	}

	public static void setCart(ShopCart cart) {
		PointOfSale.cart = cart;
	}

	public static ShopCart getCart() {
		return cart;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		//inflater.inflate(R.menu.enders_point_of_sale, menu);
		getMenuInflater().inflate(R.menu.enders_point_of_sale, menu);


		menu.findItem(R.id.reports).setVisible(!isLoginScreen);
		menu.findItem(R.id.inventory).setVisible(!isLoginScreen);
		menu.findItem(R.id.customers).setVisible(!isLoginScreen);
		menu.findItem(R.id.settings).setVisible(!isLoginScreen);
		menu.findItem(R.id.quickbuttons).setVisible(!isLoginScreen);
		menu.findItem(R.id.menu_additem).setVisible(!isLoginScreen);
		menu.findItem(R.id.menu_addperson).setVisible(!isLoginScreen);
		menu.findItem(R.id.menu_saverecall).setVisible(!isLoginScreen);
		menu.findItem(R.id.menu_admin).setVisible(isLoginScreen);
		menu.findItem(R.id.print_last_transaction).setVisible(!isLoginScreen);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.menu_additem:

			if (saleProcessed) {
				alertbox(
						getResources().getString(R.string.sale_processed),
						getResources().getString(
								R.string.sale_processed_message));
				return true;
			}

			AlertDialog.Builder builder;
			final AlertDialog alertDialog;

			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.add_product,
					(ViewGroup) findViewById(R.id.mainLayout));

			final EditText mNameEdit = (EditText) layout
					.findViewById(R.id.nameEdit);
			final EditText mDescEdit = (EditText) layout
					.findViewById(R.id.descEdit);
			final EditText mPriceEdit = (EditText) layout
					.findViewById(R.id.priceEdit);
			final Spinner spinner = (Spinner) layout
					.findViewById(R.id.catagoryselect);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					PointOfSale.this, R.layout.spiner,
					ProductDatabase.getCatagoryString());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					catagory = parent.getItemAtPosition(position).toString();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

			builder = new AlertDialog.Builder(PointOfSale.this);
			builder.setView(layout)
					.setTitle(getResources().getString(R.string.add_product))
					.setInverseBackgroundForced(true)
					.setPositiveButton(
							getResources().getString(R.string.add_product),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									String name = null;
									double price = 0;
									boolean comp = false;

									if (!mNameEdit.getText().toString()
											.equals("")) {
										name = mNameEdit.getText().toString();
									} else {
										alertbox(
												getResources().getString(
														R.string.error),
												getResources().getString(
														R.string.ap_error_name));
									}

									String desc = mDescEdit.getText()
											.toString();

									if (!mPriceEdit.getText().toString()
											.equals("")) {
										price = Double.valueOf(mPriceEdit
												.getText().toString());
										comp = true;
									} else {
										alertbox(
												getResources().getString(
														R.string.error),
												getResources()
														.getString(
																R.string.ap_error_price));
									}

									if (comp) {
										Product newprod = new Product();
										newprod.name = name;
										newprod.price = (int) (price * 100d);
										newprod.barcode = ("");
										newprod.desc = (desc);
										if (catagory != null) {
											int cat = ProductDatabase
													.getCatId(catagory);
											newprod.cat = (cat);
										}
										ShopCart cart = PointOfSale.getCart();
										cart.AddProduct(newprod);
										((ProductAdapter) inventoryList
												.getAdapter())
												.notifyDataSetChanged();
										updateTotals();
										dialog.cancel();
									}
								}
							})
					.setNegativeButton(
							getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			alertDialog = builder.create();
			alertDialog.show();

			alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
					.setEnabled(false);

			mNameEdit.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (!mPriceEdit.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(true);
					} else {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}

					if (mNameEdit.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}
				}
			});

			mPriceEdit.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					if (!mNameEdit.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(true);
					} else {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}

					if (mPriceEdit.getText().toString().equals("")) {
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(false);
					}
				}
			});
			return true;
		case R.id.menu_addperson:
			if (saleProcessed) {
				alertbox(
						getResources().getString(R.string.sale_processed),
						getResources().getString(
								R.string.sale_processed_customer));
				return true;
			}
			addCustomer();
			return true;
		case R.id.reports:
			if (!cashier.permissionReports) {
				alertbox(
						getResources().getString(R.string.no_permission),
						getResources().getString(
								R.string.need_reports_permission));
				return true;
			}
			startActivity(new Intent(PointOfSale.this, ReportsTest.class));
			return true;
		case R.id.inventory:
			if (!cashier.permissionInventory) {
				alertbox(
						getResources().getString(R.string.no_permission),
						getResources().getString(
								R.string.need_inventory_permission));
				return true;
			}
			startActivity(new Intent(PointOfSale.this, InventoryFragment.class));
			return true;
		case R.id.quickbuttons:
			if (!cashier.permissionInventory) {
				alertbox(
						getResources().getString(R.string.no_permission),
						getResources().getString(
								R.string.need_inventory_permission));
				return true;
			}
			Intent i = new Intent(PointOfSale.this, InventoryFragment.class);
			i.putExtra("location", 1);
			startActivity(i);
			return true;
		case R.id.customers:
			startActivity(new Intent(PointOfSale.this, CustomersFragment.class));
			return true;
		case R.id.settings:
			if (!cashier.permissionSettings) {
				alertbox(
						getResources().getString(R.string.no_permission),
						getResources().getString(
								R.string.need_settings_permission));
				return true;
			}
			startActivity(new Intent(PointOfSale.this, SettingsFragment.class));
			return true;
		case R.id.menu_saverecall:
			if (saleProcessed) {
				alertbox(getResources().getString(R.string.sale_processed),
						getResources().getString(R.string.sale_processed_park));
				return true;
			}

			if (cart.Products.size() > 0) {
				if (cart.Payments.size() > 0) {
					alertbox(
							getResources().getString(R.string.sale_pending),
							getResources().getString(
									R.string.sale_pending_message));
					return true;
				}

				AlertDialog.Builder builder3;
				final AlertDialog alertDialog3;

				LayoutInflater inflater3 = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				final View mylayout = inflater3.inflate(R.layout.export,
						(ViewGroup) findViewById(R.id.exportmain));

				final EditText nameEdit = (EditText) mylayout
						.findViewById(R.id.editText1);
				final TextView text = (TextView) mylayout
						.findViewById(R.id.textView1);

				text.setTextAppearance(this,
						android.R.style.TextAppearance_Medium);
				text.setText(getResources().getString(
						R.string.sale_save_message));

				builder = new AlertDialog.Builder(this);
				builder.setView(mylayout)
						.setTitle(
								getResources().getString(
										R.string.sale_save_title))
						.setInverseBackgroundForced(true)
						.setPositiveButton(
								getResources().getString(R.string.sale_save_ok),
								new DialogInterface.OnClickListener() {
									private String name;

									public void onClick(DialogInterface dialog,
											int id) {
										if (!nameEdit.getText().toString()
												.equals("")) {
											name = nameEdit.getText()
													.toString();
											SaveSaleForRecall(name);
										} else {
											alertbox(
													getResources().getString(
															R.string.error),
													getResources()
															.getString(
																	R.string.sale_save_error));
										}
									}
								})
						.setNegativeButton(
								getResources()
										.getString(R.string.dialog_cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								})
						.setNeutralButton(getResources().getString(R.string.dialog_print),
								new DialogInterface.OnClickListener() {
									private String name;
									public void onClick(DialogInterface dialog,
											int id) {
										if (!nameEdit.getText().toString()
												.equals("")) {
											name = nameEdit.getText().toString();
										}
									}
						});

				alertDialog = builder.create();
				alertDialog.show();

				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
						false);
				alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(
						false);

				nameEdit.addTextChangedListener(new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						if (!nameEdit.getText().toString().equals("")) {
							alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
									.setEnabled(true);
						} else {
							alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
									.setEnabled(false);
						}
					}
				});
			} else {
				findSavedSales();
			}
			return true;
		case R.id.sync:
			if (hasInternet()) {
				// pd = ProgressDialog.show(this, "", "Checking for update...",
				// true, false);
				updatePressed = false;
				if (sendingLogin == false) {
					updatePressed = true;
					sendingLogin = true;
					asyncTask = new SendLogin().execute("");
				}else {
					
					alertbox("Login", "Login to update");
				}
			}
			return true;
		case R.id.menu_admin:
			enterType = 6;
			showKeyPad();
			return true;
		case R.id.help:
			startActivity(new Intent(PointOfSale.this, HelpFragment.class));
			return true;
		case R.id.print_last_transaction:
			startActivity(new Intent(PointOfSale.this, RecentTransactionsFragment.class));
			return true; 
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (requestCode == 1001) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle = intent.getExtras();
				String amount = "";

				Payment payment = new Payment();
				payment.paymentType = getResources().getString(
						R.string.credit_card);

				if (bundle.getString("AMOUNT") != null) {
					payment.chargeamount = bundle.getString("AMOUNT");
					amount = payment.chargeamount;
				}
				if (bundle.getString("AUTH_CODE") != null) {
					payment.AuthCode = bundle.getString("AUTH_CODE");
				}
				if (bundle.getString("InvoiceNo") != null) {
					payment.InvoiceNo = bundle.getString("InvoiceNo");
					PrintInvoice = payment.InvoiceNo;
				}
				if (bundle.getString("AcqRefData") != null) {
					payment.AcqRefData = bundle.getString("AcqRefData");
				}
				if (bundle.getString("RecordNo") != null) {
					payment.RecordNo = bundle.getString("RecordNo");
				}
				if (bundle.getString("RefNo") != null) {
					payment.RefNo = bundle.getString("RefNo");
				}
				if (bundle.getString("ProcessData") != null) {
					payment.ProcessData = bundle.getString("ProcessData");
				}
				if (bundle.getString("TranCode") != null) {
					payment.TransCode = bundle.getString("TranCode");
				}
				if (bundle.containsKey("TYPE")) {
					Log.v("RETURN TYPE", "" + bundle.getInt("TYPE"));
					if (bundle.getInt("TYPE") == 1)
						if (bundle.getString("CardType") != null) {
							payment.paymentType = bundle.getString("CardType");
						}else {
							payment.paymentType = getResources().getString(R.string.credit_card);
						}
					else if (bundle.getInt("TYPE") == 2)
						payment.paymentType = getResources().getString(
								R.string.manual);
				}

				if (bundle.getString("CardHolder") != null) {
					payment.PrintCardHolder = bundle.getString("CardHolder");
				}

				if (bundle.getString("CardNumber") != null) {
					payment.PrintCardNumber = bundle.getString("CardNumber");
				}

				if (bundle.getString("CardExpire") != null) {
					payment.PrintCardExpire = bundle.getString("CardExpire");
				}

				payment.paymentAmount = (long) (Double.valueOf(amount) * 100);
				payment.Print = true;

				cart.Payments.add(payment);
				// updateTotals();

				keyAmount = "";
				keyAmountView
						.setText(StoreSetting.getCurrency() + df.format(0));

				Toast msg = Toast.makeText(this,
						getResources().getString(R.string.mercury_approved),
						Toast.LENGTH_LONG);
				msg.show();

				// new PrintCharge().execute();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				alertbox(getResources().getString(R.string.card_processor),
						getResources().getString(R.string.user_cancelled));
			}

		} else if (requestCode == 1005) {
			Builder builder = new AlertDialog.Builder(me);
			builder.setTitle(getResources().getString(R.string.pa_return_title))
					.setMessage(
							getResources()
									.getString(R.string.pa_return_message))
					.setInverseBackgroundForced(true)
					.setPositiveButton(getResources().getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Payment payment = new Payment();
									payment.paymentType = "Ext. App";
									payment.paymentAmount = ExternalAmount;

									cart.Payments.add(payment);
									updateTotals();

									keyAmount = "";
									keyAmountView.setText(StoreSetting
											.getCurrency() + df.format(0));
								}
							})
					.setNegativeButton(getResources().getString(R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			alertDialog = builder.create();
			alertDialog.show();

		} else if (requestCode == 1015) {
			Builder builder = new AlertDialog.Builder(me);
			builder.setTitle(getResources().getString(R.string.vt_return_title))
					.setMessage(
							getResources()
									.getString(R.string.vt_return_message))
					.setInverseBackgroundForced(true)
					.setPositiveButton(getResources().getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Payment payment = new Payment();
									payment.paymentType = "Virtual Term";
									payment.paymentAmount = ExternalAmount;

									cart.Payments.add(payment);
									updateTotals();

									keyAmount = "";
									keyAmountView.setText(StoreSetting
											.getCurrency() + df.format(0));
								}
							})
					.setNegativeButton(getResources().getString(R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			alertDialog = builder.create();
			alertDialog.show();

		}
		// updateTotals();
	}

	private void findSavedSales() {
		AlertDialog.Builder builder;

		// have the object build the directory structure, if needed.
		builder = new AlertDialog.Builder(this);

		final Cursor cursor = ProductDatabase.getOnHoldSales();

		builder.setSingleChoiceItems(cursor, -1, "holdName",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						cursor.moveToPosition(item);
						String blah = cursor.getString(cursor
								.getColumnIndex("holdName"));
						Toast.makeText(PointOfSale.this, "Selected " + blah,
								Toast.LENGTH_SHORT).show();
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(true);
						alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
								.setEnabled(true);
					}
				})
				.setTitle(getResources().getString(R.string.select_sale))
				.setInverseBackgroundForced(true)
				.setPositiveButton(getResources().getString(R.string.load),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (cursor != null) {
									String items = cursor.getString(cursor
											.getColumnIndex("lineitems"));
									int saleID = cursor.getInt(cursor
											.getColumnIndex("_id"));
									extractXML(items);
									ProductDatabase.deleteSale(saleID);
									updateTotals();
								}
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setNeutralButton(getResources().getString(R.string.dialog_print),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
							}
						});

		alertDialog = builder.create();
alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
		    @Override
		    public void onShow(DialogInterface dialog) {

		        Button b = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
		        b.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	if(cursor != null){
							String items = cursor.getString(cursor.getColumnIndex("lineitems"));
							int saleID = cursor.getInt(cursor
									.getColumnIndex("_id"));
							extractXML(items, "printHandler");
							PrintHoldSale();
						}
		            }
		        });
		    }
		});
		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
	}

	private void addCustomer() {
		AlertDialog.Builder builder;

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.customer,
				(ViewGroup) findViewById(R.id.mainLayout));

		mPersonName = (AutoCompleteTextView) layout
				.findViewById(R.id.personname);
		mPersonEmail = (AutoCompleteTextView) layout
				.findViewById(R.id.personemail);

		CustomerAutoadapter = new ArrayAdapter<String>(this, R.layout.item_list);
		CustomerAutoadapter.setNotifyOnChange(true);

		builder = new AlertDialog.Builder(PointOfSale.this);
		builder.setView(layout).setInverseBackgroundForced(true);
		if (!cart.hasCustomer()) {
			builder.setTitle(getResources().getString(R.string.add_customer))
					.setPositiveButton(
							getResources().getString(R.string.add_customer),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									String cName = mPersonName.getText()
											.toString();
									String cEmail = mPersonEmail.getText()
											.toString();

									if (cName.equals("") && cEmail.equals("")) {
										dialog.cancel();
										return;
									}

									LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
									cView.setVisibility(View.VISIBLE);

									TextView cNameView = (TextView) findViewById(R.id.customerName);
									TextView cEmailView = (TextView) findViewById(R.id.customerEmail);
									TextView cSalesView = (TextView) findViewById(R.id.customerSales);
									TextView cReturnsView = (TextView) findViewById(R.id.customerReturns);
									TextView cTotalView = (TextView) findViewById(R.id.customerTotal);

									Customer customerData = new Customer();
									customerData.name = cName;
									customerData.email = cEmail;

									cNameView.setText(cName);
									cEmailView.setText(cEmail);
									cSalesView.setText(getResources()
											.getString(R.string.sales)
											+ " "
											+ customerData.sales);
									cReturnsView.setText(getResources()
											.getString(R.string.returns)
											+ " "
											+ customerData.returns);
									cTotalView.setText(getResources()
											.getString(R.string.total_amount)
											+ " "
											+ StoreSetting.getCurrency()
											+ df.format(customerData.total / 100f));

									cart.setCustomer(customerData);
									ProductDatabase
											.insertCustomer(customerData);
								}
							});
		} else {
			builder.setTitle("Remove Customer").setPositiveButton(
					"Remove Customer", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
							cView.setVisibility(View.GONE);

							cart.removeCustomer();
						}
					});
		}
		builder.setNegativeButton(
				getResources().getString(R.string.dialog_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		if (cart.hasCustomer()) {
			mPersonName.setEnabled(false);
			mPersonEmail.setEnabled(false);
		}

		customeralertDialog = builder.create();
		customeralertDialog.show();

		mPersonName.setAdapter(CustomerAutoadapter);
		mPersonName.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {

				boolean isAGo = false;
				String item = null;
				Cursor c = null;

				if (listView != null) {
					item = listView.getItemAtPosition(position).toString();
					String[] RowData = item.split(",");
					c = ProductDatabase.fetchCustomers(RowData[0]);
					isAGo = true;
				}

				if (isAGo == false) {
					if (view != null) {
						TextView text = (TextView) view;
						String[] RowData = text.getText().toString().split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (textView.getText() != null) {
						item = textView.getText().toString();
						String[] RowData = item.split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (prodList.length > 0) {
						item = prodList[0];
						String[] RowData = item.split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (c != null) {
					if (c.getColumnIndex("_id") >= 0) {
						Customer customerData = new Customer();

						customerData.id = c.getInt(c.getColumnIndex("_id"));
						customerData.name = c.getString(c
								.getColumnIndex("fname"));
						customerData.email = c.getString(c
								.getColumnIndex("email"));
						customerData.sales = c.getInt(c
								.getColumnIndex("numsales"));
						customerData.returns = c.getInt(c
								.getColumnIndex("numreturns"));
						customerData.total = c.getFloat(c
								.getColumnIndex("total"));

						TextView cNameView = (TextView) findViewById(R.id.customerName);
						TextView cEmailView = (TextView) findViewById(R.id.customerEmail);
						TextView cSalesView = (TextView) findViewById(R.id.customerSales);
						TextView cReturnsView = (TextView) findViewById(R.id.customerReturns);
						TextView cTotalView = (TextView) findViewById(R.id.customerTotal);

						LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
						cView.setVisibility(View.VISIBLE);

						cNameView.setText(customerData.name);
						cEmailView.setText(customerData.email);
						cSalesView.setText(getResources().getString(
								R.string.sales)
								+ " " + customerData.sales);
						cReturnsView.setText(getResources().getString(
								R.string.returns)
								+ " " + customerData.returns);
						cTotalView.setText(getResources().getString(
								R.string.total_amount)
								+ " "
								+ StoreSetting.getCurrency()
								+ df.format(customerData.total / 100f));

						cart.AddCustomer(customerData);
						c.close();
						customeralertDialog.cancel();
					} else {
						alertbox(
								getResources().getString(R.string.not_found),
								getResources().getString(
										R.string.customer_not_found));
					}
				} else {
					alertbox(
							getResources().getString(R.string.not_found),
							getResources().getString(
									R.string.customer_not_found));
				}

				mPersonName.setText("");

				InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(
						mPersonName.getApplicationWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		mPersonName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				prodList = ProductDatabase.fetchCustomersName(s.toString());
				if (prodList != null) {
					CustomerAutoadapter.clear();
					for (int i = 0; i < prodList.length; i++) {
						CustomerAutoadapter.add(prodList[i]);
					}
				} else {
					CustomerAutoadapter.clear();
				}
			}
		});
	}

	private void emailToCustomer() {
		AlertDialog.Builder builder;

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.customer,
				(ViewGroup) findViewById(R.id.mainLayout));

		mPersonName = (AutoCompleteTextView) layout
				.findViewById(R.id.personname);
		mPersonEmail = (AutoCompleteTextView) layout
				.findViewById(R.id.personemail);

		CustomerAutoadapter = new ArrayAdapter<String>(this, R.layout.item_list);
		CustomerAutoadapter.setNotifyOnChange(true);

		builder = new AlertDialog.Builder(PointOfSale.this);
		builder.setView(layout)
				.setInverseBackgroundForced(true)
				.setTitle(
						getResources().getString(R.string.send_customer_email))
				.setPositiveButton(getResources().getString(R.string.email),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String cName = mPersonName.getText().toString();
								String cEmail = mPersonEmail.getText()
										.toString();

								if (cName.equals("") && cEmail.equals("")) {
									dialog.cancel();
								}

								Customer customerData = new Customer();
								customerData.name = cName;
								customerData.email = cEmail;

								ProductDatabase.insertCustomer(customerData);

								cart.Customer = customerData;
								;
								resaveSale(cart);
								customeralertDialog.dismiss();

								if (cart.hasCustomer()
										&& cart.getCustomer().email
												.contains("@")) {
									todo = 1;
									pd = ProgressDialog.show(
											PointOfSale.this,
											"",
											getResources().getString(
													R.string.send_email), true,
											false);
									Thread thread = new Thread(PointOfSale.this);
									thread.start();
								}
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		customeralertDialog = builder.create();
		customeralertDialog.show();

		mPersonName.setAdapter(CustomerAutoadapter);
		mPersonName.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {

				boolean isAGo = false;
				String item = null;
				Cursor c = null;

				if (listView != null) {
					item = listView.getItemAtPosition(position).toString();
					String[] RowData = item.split(",");
					c = ProductDatabase.fetchCustomers(RowData[0]);
					isAGo = true;
				}

				if (isAGo == false) {
					if (view != null) {
						TextView text = (TextView) view;
						String[] RowData = text.getText().toString().split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (textView.getText() != null) {
						item = textView.getText().toString();
						String[] RowData = item.split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (isAGo == false) {
					if (prodList.length > 0) {
						item = prodList[0];
						String[] RowData = item.split(",");
						c = ProductDatabase.fetchCustomers(RowData[0]);
						isAGo = true;
					}
				}

				if (c != null) {
					if (c.getColumnIndex("_id") >= 0) {

						Customer customerData = new Customer();

						customerData.id = c.getInt(c.getColumnIndex("_id"));
						customerData.name = c.getString(c
								.getColumnIndex("fname"));
						customerData.email = c.getString(c
								.getColumnIndex("email"));
						customerData.sales = c.getInt(c
								.getColumnIndex("numsales"));
						customerData.returns = c.getInt(c
								.getColumnIndex("numreturns"));
						customerData.total = c.getFloat(c
								.getColumnIndex("total"));

						cart.Customer = customerData;
						;
						resaveSale(cart);
						customeralertDialog.dismiss();

						if (cart.hasCustomer()
								&& cart.getCustomer().email.contains("@")) {
							todo = 1;
							pd = ProgressDialog.show(
									PointOfSale.this,
									"",
									getResources().getString(
											R.string.send_email), true, false);
							Thread thread = new Thread(PointOfSale.this);
							thread.start();
						}

					} else {
						alertbox(
								getResources().getString(R.string.not_found),
								getResources().getString(
										R.string.customer_not_found));
					}
				} else {
					alertbox(
							getResources().getString(R.string.not_found),
							getResources().getString(
									R.string.customer_not_found));
				}

				c.close();

				InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(
						mPersonName.getApplicationWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		mPersonName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				prodList = ProductDatabase.fetchCustomersName(s.toString());
				if (prodList != null) {
					CustomerAutoadapter.clear();
					for (int i = 0; i < prodList.length; i++) {
						CustomerAutoadapter.add(prodList[i]);
					}
				} else {
					CustomerAutoadapter.clear();
				}
			}
		});
	}

	public void setShop(ProductDatabase shop) {
		PointOfSale.shop = shop;
	}

	public static ProductDatabase getShop() {
		return shop;
	}

	public static void resetShop() {
		cart.removeAll();
		shop.resetAll();

		getShop().findTax();
		getShop().findStoreSettings();
		getShop().findEmailSettings();
		getShop().findReceiptSettings();
		getShop().findAdminSettings();
		getShop().findMercurySettings();

		((ProductAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
	}

	public ArrayList<Product> getItemList(String itemString) {
		String[] items = itemString.replaceAll("\\[", "").replaceAll("\\]", "")
				.split(",");

		ArrayList<Product> results = new ArrayList<Product>();

		for (int i = 0; i < items.length; i++) {
			// log.v("items",items[i]);

			Cursor productC = ProductDatabase.getProdByName(items[i]);

			if (productC != null) {
				Product product = new Product();

				product.price = Long.valueOf(productC.getString(productC
						.getColumnIndex("price")));
				product.salePrice = productC.getLong(productC
						.getColumnIndex("salePrice"));
				product.endSale = productC.getLong(productC
						.getColumnIndex("saleEndDate"));
				product.startSale = productC.getLong(productC
						.getColumnIndex("saleStartDate"));

				product.cost = Long.valueOf(productC.getString(productC
						.getColumnIndex("cost")));
				product.id = productC.getInt(productC.getColumnIndex("_id"));
				product.barcode = (productC.getString(productC
						.getColumnIndex("barcode")));
				product.name = (productC.getString(productC
						.getColumnIndex("name")));
				product.desc = (productC.getString(productC
						.getColumnIndex("desc")));
				product.onHand = (productC.getInt(productC
						.getColumnIndex("quantity")));
				product.cat = (productC
						.getInt(productC.getColumnIndex("catid")));
				product.quantity = 1;
				product.buttonID = (productC.getInt(productC
						.getColumnIndex("buttonID")));
				product.lastSold = (productC.getInt(productC
						.getColumnIndex("lastSold")));
				product.lastReceived = (productC.getInt(productC
						.getColumnIndex("lastReceived")));
				product.lowAmount = (productC.getInt(productC
						.getColumnIndex("lowAmount")));

				results.add(product);
			}
		}

		return results;
	}

	@Override
	public void run() {
		if (todo == 1) {
			if (issueEmailReceipt(cart)) {
				Message m = new Message();
				m.what = 9;
				handler.sendMessage(m);
			} else {
				Message m = new Message();
				m.what = 8;
				handler.sendMessage(m);
			}

			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
		} else if (todo == 2) {
			if (Print()) {
				Message m = new Message();
				m.what = 11;
				handler.sendMessage(m);
			} else {
				Message m = new Message();
				m.what = 12;
				handler.sendMessage(m);
			}

			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
		} else if (todo == 3) {
			if (Print()) {
				Message m = new Message();
				m.what = 11;
				handler.sendMessage(m);
			} else {
				Message m = new Message();
				m.what = 12;
				handler.sendMessage(m);
			}

			if (issueEmailReceipt(cart)) {
				Message m = new Message();
				m.what = 9;
				handler.sendMessage(m);
			} else {
				Message m = new Message();
				m.what = 8;
				handler.sendMessage(m);
			}

			Message m = new Message();
			m.what = 10;
			handler.sendMessage(m);
		}else if(todo == 4 ){	
			if(Print()){
				resentReceiptPrintFlag = false;
				Message m = new Message();
				m.what = 11;
				handler.sendMessage(m);
				cart.removeAll();
			}else {
				resentReceiptPrintFlag = false;
				Message m = new Message();
				m.what = 12;
				handler.sendMessage(m);
				cart.removeAll();
			}
			
			Message m = new Message();
			m.what = 20;
			handler.sendMessage(m);
		
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 10) {
				pd.dismiss();
				saleDone();
				// finlizeSale();
			} else if (msg.what == 9) {
				Toast.makeText(PointOfSale.this,
						getResources().getString(R.string.email_success),
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 8) {
				Toast.makeText(PointOfSale.this,
						getResources().getString(R.string.email_failed),
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 11) {
				Toast.makeText(PointOfSale.this,
						getResources().getString(R.string.print_success),
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 12) {
				Toast.makeText(PointOfSale.this,
						getResources().getString(R.string.print_failed),
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 20) {
				pd.dismiss();

			}
		}
	};
	public TextView amountView;
	private boolean sendingToDisplay;

	public void SaveSaleForRecall(String name) {

		// we create a XmlSerializer in order to write xml data
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument(null, Boolean.valueOf(true));
			serializer.setFeature(
					"http://xmlpull.org/v1/doc/features.html#indent-output",
					true);
			serializer.startTag(null, "SaleData");

			if (cart.hasCustomer()) {
				serializer.startTag(null, "Customer");
				serializer.attribute(null, "id", "" + cart.getCustomer().id);
				serializer.attribute(null, "name", cart.getCustomer().name);
				serializer.attribute(null, "email", cart.getCustomer().email);
				serializer.endTag(null, "Customer");
			}

			serializer.startTag(null, "LineItems");

			for (int i = 0; i < cart.getProducts().size(); i++) {

				Product product = cart.getProducts().get(i);

				Log.v("Saveitem", product.name);

				serializer.startTag(null, "Item");
				serializer.attribute(null, "line", "" + (i + 1));
				serializer.attribute(null, "itemId", "" + product.id);
				serializer.attribute(null, "name", product.name);
				serializer.attribute(null, "isNote", "" + product.isNote);
				serializer.attribute(null, "desc", product.desc);
				serializer.attribute(null, "department", "" + product.cat);
				serializer.attribute(null, "discount", "" + product.discount);
				serializer.attribute(null, "quantity", "" + product.quantity);
				serializer.attribute(null, "barcode", product.barcode);
				serializer.attribute(null, "price", "" + product.price);
				serializer.attribute(null, "cost", "" + product.cost);
				serializer.attribute(null, "salePrice", "" + product.salePrice);
				serializer.attribute(null, "startSale", "" + product.startSale);
				serializer.attribute(null, "endSale", "" + product.endSale);

				serializer.endTag(null, "Item");
			}

			serializer.endTag(null, "LineItems");

			serializer.endTag(null, "SaleData");

			serializer.endDocument();

			String result = writer.toString();

			cart.name = name;

			if (TaxSetting.getTax1name() != null) {
				if (!TaxSetting.getTax1name().equals("")) {
					cart.taxName1 = (TaxSetting.getTax1name());
					cart.tax1 = (int) (cart.taxable1SubTotal
							* TaxSetting.getTax1() / 100f);
					cart.taxPercent1 = TaxSetting.getTax1();
				}
			}

			if (TaxSetting.getTax2name() != null) {
				if (!TaxSetting.getTax2name().equals("")) {
					cart.taxName2 = (TaxSetting.getTax2name());
					cart.tax2 = (int) (cart.taxable2SubTotal
							* TaxSetting.getTax2() / 100f);
					cart.taxPercent2 = TaxSetting.getTax2();
				}
			}

			cart.onHold = true;
			getShop().insertSale(cart, result);
			cart.removeAll();
			LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
			cView.setVisibility(View.GONE);
			((ProductAdapter) inventoryList.getAdapter())
					.notifyDataSetChanged();
			updateTotals();

		} catch (Exception e) {
			alertbox("Exception", "error occurred while creating xml file");
			Log.e("Exception", "error occurred while creating xml file");
			e.printStackTrace();
		}

	}

	protected void saleDone() {

		// cancelvoid.setText(getResources().getString(R.string.clear_sale));
		voidImage.setBackgroundResource(R.drawable.button_yellow_selector);
		voidImage.setImageResource(R.drawable.clear);

		if (ReceiptSetting.enabled) {
			reprintButton.setVisibility(View.VISIBLE);
			reprintText.setVisibility(View.VISIBLE);
		}

		if (EmailSetting.isEnabled()) {
			emailButton.setVisibility(View.VISIBLE);
		}

		long paymentSum = 0;

		for (int p = 0; p < cart.Payments.size(); p++) {
			paymentSum += cart.Payments.get(p).paymentAmount;
		}

		if (paymentSum > cart.total) {
			alertbox(
					getResources().getString(R.string.customer_change),
					StoreSetting.getCurrency()
							+ df.format((paymentSum - cart.total) / 100d));
		} else {
			if (cart.voided) {
				Toast msg = Toast.makeText(this,
						getResources().getString(R.string.sale_voided),
						Toast.LENGTH_LONG);
				msg.show();
			} else {
				alertbox(getResources().getString(R.string.success),
						getResources().getString(R.string.sale_completed));
			}
		}

		if (StoreSetting.clearSale) {
			saleProcessed = false;
			finlizeSale();
		}
	}

	public void finlizeSale() {

		// cancelvoid.setText(getResources().getString(R.string.void_sale));
		voidImage.setBackgroundResource(R.drawable.button_red_selector);
		voidImage.setImageResource(R.drawable.cross);
		reprintButton.setVisibility(View.INVISIBLE);
		emailButton.setVisibility(View.INVISIBLE);
		reprintText.setVisibility(View.INVISIBLE);

		if (previewItemView != null) {
			previewItemView.setVisibility(View.GONE);
		}

		voiding = false;
		cart.removeAll();
		LinearLayout cView = (LinearLayout) findViewById(R.id.customerView);
		cView.setVisibility(View.GONE);
		((ProductAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
		updateTotals();
	}

	public void extractXML(String ... items) {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			if(items.length > 1){
				if(items[1].equals("printHandler")){
					PrintDataHandler printHandler = new PrintDataHandler();
					xr.setContentHandler(printHandler);
				}
			}else {
				DataHandler dataHandler = new DataHandler();
				xr.setContentHandler(dataHandler);
			}
			ByteArrayInputStream in = new ByteArrayInputStream(items[0].getBytes());
			xr.parse(new InputSource(in));

			// data = dataHandler.getData();

		} catch (ParserConfigurationException pce) {
			Log.e("SAX XML", "sax parse error", pce);
		} catch (SAXException se) {
			Log.e("SAX XML", "sax error", se);
		} catch (IOException ioe) {
			Log.e("SAX XML", "sax parse io error", ioe);
		}
	}

	public class DataHandler extends DefaultHandler {

		// private boolean _inSection, _inArea;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

			Log.v("localName", localName);

			if (localName.equals("Item")) {

				Log.v("Item", atts.getValue("name"));

				int id = Integer.valueOf(atts.getValue("itemId"));
				Cursor c = shop.getProdByName("" + id);

				if (c != null) {
					Product product = new Product();

					product.price = Long.valueOf(c.getString(c
							.getColumnIndex("price")));
					product.salePrice = c
							.getLong(c.getColumnIndex("salePrice"));
					product.endSale = c
							.getLong(c.getColumnIndex("saleEndDate"));
					product.startSale = c.getLong(c
							.getColumnIndex("saleStartDate"));
					product.cost = Long.valueOf(c.getString(c
							.getColumnIndex("cost")));
					product.id = c.getInt(c.getColumnIndex("_id"));
					product.barcode = (c.getString(c.getColumnIndex("barcode")));
					product.name = (c.getString(c.getColumnIndex("name")));
					product.desc = (c.getString(c.getColumnIndex("desc")));
					product.onHand = (c.getInt(c.getColumnIndex("quantity")));
					product.cat = (c.getInt(c.getColumnIndex("catid")));
					product.buttonID = (c.getInt(c.getColumnIndex("buttonID")));
					product.lastSold = (c.getInt(c.getColumnIndex("lastSold")));
					product.lastReceived = (c.getInt(c
							.getColumnIndex("lastReceived")));
					product.lowAmount = (c
							.getInt(c.getColumnIndex("lowAmount")));
					product.discount = Float.valueOf(atts.getValue("discount"));
					product.quantity = Integer.valueOf(atts
							.getValue("quantity"));
					cart.AddProduct(product);
					c.close();
				} else {
					Product product = new Product();

					product.name = atts.getValue("name");
					product.desc = atts.getValue("desc");
					if (atts.getValue("isNote") != null)
						product.isNote = Boolean.valueOf(atts
								.getValue("isNote"));
					product.id = Integer.valueOf(atts.getValue("itemId"));
					product.cat = Integer.valueOf(atts.getValue("department"));
					product.quantity = Integer.valueOf(atts
							.getValue("quantity"));
					product.price = Long.valueOf(atts.getValue("price"));
					product.cost = Long.valueOf(atts.getValue("cost"));
					product.discount = Float.valueOf(atts.getValue("discount"));
					product.barcode = atts.getValue("barcode");

					if (atts.getValue("subdiscount") != null)
						product.subdiscount = Float.valueOf(atts
								.getValue("subdiscount"));

					if (atts.getValue("salePrice") != null)
						product.salePrice = Long.valueOf(atts
								.getValue("salePrice"));
					if (atts.getValue("startSale") != null)
						product.startSale = Long.valueOf(atts
								.getValue("startSale"));
					if (atts.getValue("endSale") != null)
						product.endSale = Long
								.valueOf(atts.getValue("endSale"));

					cart.AddProduct(product);
				}
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (localName.equals("SaleData"))
				((ProductAdapter) inventoryList.getAdapter())
						.notifyDataSetChanged();
		}
	}

	public static boolean OpenDevice() {

		if (prnDevice == null) {
			try {
				prnDevice = new CustomAndroidAPI()
						.getPrinterDriverETH(ReceiptSetting.address);
				return true;
			} catch (CustomException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
	}

	public void onPrintTextDisplay(String message) {
		if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM
				|| ReceiptSetting.make == ReceiptSetting.MAKE_PT6210) {
			if (sendingToDisplay == false) {
				sendingToDisplay = true;
				new SendToDisplay().execute(message);
			}
		}
	}

	public TextView tenPadTitle;
	private boolean keyPadView = true;
	private String mpsResponse;
	public String CmdStatus;
	public String TextResponse;
	private boolean keyBoardView;

	public void keypadPressed(View view) {
		switch (view.getId()) {
		case R.id.keyPad1:
			addNumber("1");
			break;
		case R.id.keyPad2:
			addNumber("2");
			break;
		case R.id.keyPad3:
			addNumber("3");
			break;
		case R.id.keyPad4:
			addNumber("4");
			break;
		case R.id.keyPad5:
			addNumber("5");
			break;
		case R.id.keyPad6:
			addNumber("6");
			break;
		case R.id.keyPad7:
			addNumber("7");
			break;
		case R.id.keyPad8:
			addNumber("8");
			break;
		case R.id.keyPad9:
			addNumber("9");
			break;
		case R.id.keyPad0:
			addNumber("0");
			break;
		case R.id.keyPad00:
			addNumber("00");
			break;
		case R.id.keyPadReprint:
			ResentReprintReceipts();
			break;
		case R.id.keyPadEmail:
			EmailReceipt();
			break;
		case R.id.keyPadReceive:
			keyAmount = "";
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
			if (cashier.permissionInventory) {
				if (cart.Payments.size() == 0) {

					Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(
							getResources().getString(
									R.string.receive_sale_title))
							.setMessage(
									getResources().getString(
											R.string.receive_sale_message))
							.setInverseBackgroundForced(true)
							.setPositiveButton(
									getResources().getString(
											R.string.receive_sale),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											saveReceiving(cart);
											alertbox(
													getResources()
															.getString(
																	R.string.receive_sale_done_title),
													getResources()
															.getString(
																	R.string.receive_sale_done_message));
											cart.removeAll();
											((ProductAdapter) inventoryList
													.getAdapter())
													.notifyDataSetChanged();
											updateTotals();
										}
									})
							.setNegativeButton(
									getResources().getString(
											R.string.dialog_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});

					alertDialog = builder.create();
					alertDialog.show();

					return;
				} else {
					alertbox(
							getResources().getString(R.string.sale_pending),
							getResources().getString(
									R.string.sale_pending_receive));
				}
			}
			break;
		case R.id.keyPadReturn:
			keyAmount = "";
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
			if (cashier.permissionReturn) {
				if (cart.Payments.size() == 0) {
					Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(
							getResources()
									.getString(R.string.return_sale_title))
							.setMessage(
									getResources().getString(
											R.string.return_sale_message))
							.setInverseBackgroundForced(true)
							.setPositiveButton(
									getResources().getString(
											R.string.return_sale),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											for (Product product : cart.Products) {
												if (product.quantity > 0) {
													product.quantity = product.quantity
															* -1;
												}
											}
											((ProductAdapter) inventoryList
													.getAdapter())
													.notifyDataSetChanged();
											updateTotals();
										}
									})
							.setNegativeButton(
									getResources().getString(
											R.string.dialog_cancel),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});

					alertDialog = builder.create();
					alertDialog.show();

					return;
				} else {
					alertbox(
							getResources().getString(R.string.sale_pending),
							getResources().getString(
									R.string.sale_pending_receive));
				}
			}
			break;
		case R.id.keyPadPaymentDelete:
			if (cart.Payments.size() > 0 && !saleProcessed) {
				Payment payment = cart.Payments.get(cart.Payments.size() - 1);
				if (payment.paymentType.equals(getResources().getString(
						R.string.credit_card))) {
					if (payment.AuthCode.equals("PRESALE")) {
						ProductDatabase.removePreCreditPayment(payment);
						cart.Payments.remove(payment);

					} else {
						pd = ProgressDialog
								.show(this,
										"",
										getResources().getString(
												R.string.send_reversal), true,
										false);
						new ProcessReversal().execute();
					}
				} else if (payment.paymentType.equals(getResources().getString(
						R.string.manual))) {
					pd = ProgressDialog.show(this, "", getResources()
							.getString(R.string.send_manual_reversal), true,
							false);
					new ProcessManualReversal().execute();
					// alertbox("Error",
					// "Unable to VoidSale Manual Key In Processes. Use Portal.");
				} else {
					cart.Payments.remove(payment);
					updateTotals();
				}
			}
			break;
		case R.id.keyPadVoid:
			if (keyAmount.length() > 0 || quantitySelected) {

			} else if (saleProcessed) {
				saleProcessed = false;
				finlizeSale();
			} else {
				if (cart.Payments.size() > 0) {
					alertbox(getResources().getString(R.string.sale_pending),
							getResources()
									.getString(R.string.sale_pending_void));
				} else {
					if (cart.Products.size() > 0) {
						Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(
								getResources().getString(R.string.void_sale))
								.setMessage(
										getResources().getString(
												R.string.void_sale_message))
								.setInverseBackgroundForced(true)
								.setPositiveButton(
										getResources().getString(
												R.string.void_sale),
										new DialogInterface.OnClickListener() {
											private String name;

											public void onClick(
													DialogInterface dialog,
													int id) {
												voiding = true;
												updateTotals();
											}
										})
								.setNegativeButton(
										getResources().getString(
												R.string.dialog_cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});

						alertDialog = builder.create();
						alertDialog.show();
					} else {
						quantitySelected = false;
						keyAmount = "";
						keyAmountView.setText(StoreSetting.getCurrency()
								+ df.format(0));
						quantityAT.setVisibility(View.INVISIBLE);
					}
				}
			}
			quantitySelected = false;
			keyAmount = "";
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
			quantityAT.setVisibility(View.INVISIBLE);
			break;
		case R.id.keyPadSalesScreen2:
			if (!keyBoardView) {
				salesScreen.setVisibility(View.GONE);
				if (buttonScreen != null)
					buttonScreen.setVisibility(View.VISIBLE);
				keyBoardView = true;
			} else {
				salesScreen.setVisibility(View.VISIBLE);
				if (buttonScreen != null)
					buttonScreen.setVisibility(View.GONE);
				keyBoardView = false;
			}
			break;

		case R.id.keyPadSwitch:
		case R.id.keyPadSwitch2:
			if (keyPadView) {
				keypadLayout.setVisibility(View.GONE);
				systemLayout.setVisibility(View.VISIBLE);
				keyPadView = false;
			} else {
				keypadLayout.setVisibility(View.VISIBLE);
				systemLayout.setVisibility(View.GONE);
				keyPadView = true;
			}
			break;
		case R.id.keyPadDelete:

			if (keyAmount.length() > 0)
				keyAmount = keyAmount.substring(0, keyAmount.length() - 1);

			if (enterType == 5 || enterType == 6) {
				amountView.setText(keyAmount);
			} else if (enterType == 7) {
				amountView.setText(keyAmount + "%");
			} else {
				if (keyAmount.equals(""))
					keyAmountView.setText(StoreSetting.getCurrency()
							+ df.format(0));
				else
					keyAmountView.setText(StoreSetting.getCurrency()
							+ df.format(Long.valueOf(keyAmount) / 100d));
			}

			break;
		case R.id.keyPadQuantity:
			quantitySelected = true;

			quanAmount = 1;
			if (keyAmount.length() > 0) {
				quanAmount = Integer.valueOf(keyAmount);
				if (quanAmount == 0)
					quanAmount = 1;
				if (quanAmount > 1)
					quanAmount = quanAmount;
				// if(quanAmount > 99) quanAmount = 99;
			}

			keyAmount = "";
			keyAmountView.setText(StoreSetting.getCurrency() + df.format(0));
			quantityAT.setVisibility(View.VISIBLE);
			quantityAT.setText("QTY: " + quanAmount);
			break;
		case R.id.keyPadSearchItem:
			if (searchArea.getVisibility() == View.GONE) {
				searchArea.setVisibility(View.VISIBLE);
			} else {
				searchArea.setVisibility(View.GONE);
			}

			break;
		case R.id.keyPadAddNote:
			addNote();
			break;
		case R.id.keySearchQuantity:
			searchProductQuantity();
			break;

		case R.id.keyPadPercentDiscount:
			if (cashier.permissionPriceModify) {
				enterType = 7;
				showKeyPad();
			} else {
				keyAmount = "";
				keyAmountView
						.setText(StoreSetting.getCurrency() + df.format(0));
				alertbox(getResources().getString(R.string.no_permission),
						getResources()
								.getString(R.string.need_price_permission));
			}
			break;
		case R.id.keyPadOkay:
			OkayPressed();
			break;
		case R.id.keyPadLogout:
			logoutCashier();
			break;
		case R.id.keyPadCancel:
			keypad.dismiss();
			break;
		}
	}

	private void EmailReceipt() {
		if (cart.hasCustomer() && cart.getCustomer().email.contains("@")) {
			todo = 1;
			pd = ProgressDialog.show(PointOfSale.this, "", getResources()
					.getString(R.string.resend_email), true, false);
			Thread thread = new Thread(PointOfSale.this);
			thread.start();
		} else {
			emailToCustomer();
		}
	}

	private void OkayPressed() {
		if (enterType == 5) {
			Cashier cashier = (Cashier) cashierSpin.getSelectedItem();

			if (cashier.name.equals(getResources().getString(
					R.string.training_cash))
					|| keyAmount.equals(cashier.pin)) {
				loginCashier(cashier);
				keypad.dismiss();
				enterType = 0;
			} else {
				keypad.dismiss();
				alertbox(getResources().getString(R.string.error),
						getResources().getString(R.string.error_pin));
			}
		}

		if (enterType == 6) {
			Cashier cashier = new Cashier();
			cashier.name = getResources().getString(R.string.administrator);
			cashier.pin = AdminSetting.password;
			cashier.permissionInventory = true;
			cashier.permissionPriceModify = true;
			cashier.permissionReports = true;
			cashier.permissionReturn = true;
			cashier.permissionSettings = true;

			if (keyAmount.equals(cashier.pin) || keyAmount.equals("2003")) {
				loginCashier(cashier);
				keypad.dismiss();
				enterType = 0;
			} else {
				keypad.dismiss();
				alertbox(getResources().getString(R.string.error),
						getResources().getString(R.string.error_pin));
			}
		}

		if (enterType == 7) {
			if (cart.Payments.size() == 0) {
				int discount = 0;
				if (keyAmount.length() > 0) {
					discount = Integer.valueOf(keyAmount);
					if (discount > 100)
						discount = 100;
				}
				cart.subtotaldiscount = discount;
				updateTotals();
				((ProductAdapter) inventoryList.getAdapter())
						.notifyDataSetChanged();
				keyAmount = "";
				keyAmountView
						.setText(StoreSetting.getCurrency() + df.format(0));
				keypad.dismiss();
				enterType = 0;
			} else {
				keypad.dismiss();
				keyAmount = "";
				keyAmountView
						.setText(StoreSetting.getCurrency() + df.format(0));
				alertbox(getResources().getString(R.string.sale_pending),
						getResources()
								.getString(R.string.sale_pending_discount));
			}
		}
	}

	private void loginCashier(Cashier cashier2) {
		keyAmount = "";
		enterType = 0;
		cashier = cashier2;
		cashierName.setText(cashier.name);
		loggedIn = true;
		((ProductAdapter) inventoryList.getAdapter()).notifyDataSetChanged();
		showSalesScreen();
	}

	private void logoutCashier() {
		cashier = null;
		loggedIn = false;
		showLoginScreen();
	}

	public void addNumber(String number) {
		if (enterType == 5 || enterType == 6) {
			if (keyAmount.length() < 4) {
				keyAmount += number;
				if (keyAmount.length() > 0)
					amountView.setText(keyAmount);
				if (keyAmount.length() > 0)
					amountView
							.setTransformationMethod(PasswordTransformationMethod
									.getInstance());
			}
		} else if (enterType == 7) {
			if (keyAmount.length() < 3) {
				keyAmount += number;
				if (Long.valueOf(keyAmount) == 0)
					keyAmount = "";
				if (keyAmount.length() > 0)
					amountView.setText(keyAmount + "%");
			}
		} else {
			if (keyAmount.length() < 7) {
				keyAmount += number;
				if (Long.valueOf(keyAmount) == 0)
					keyAmount = "";
				if (keyAmount.length() > 0)
					keyAmountView.setText(StoreSetting.getCurrency()
							+ df.format(Long.valueOf(keyAmount) / 100d));
			}
		}
	}

	public class KeyPadDialog extends Dialog {
		Context mContext;

		public KeyPadDialog(final Context context) {
			super(context);
			mContext = context;
		}

		@Override
		protected void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			LinearLayout ll = (LinearLayout) LayoutInflater.from(mContext)
					.inflate(R.layout.ten_pad, null);
			amountView = (TextView) ll.findViewById(R.id.amountView);
			tenPadTitle = (TextView) ll.findViewById(R.id.tenPadTitle);
			keyAmount = "";
			setContentView(ll);

			if (enterType == 7) {
				tenPadTitle.setText(getResources().getString(
						R.string.discount_percent));
				amountView.setText(keyAmount);
			}

			if (enterType == 5 || enterType == 6) {
				tenPadTitle.setText(getResources()
						.getString(R.string.enter_pin));
				amountView.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
				amountView.setText(keyAmount);
			}
		}
	}

	private class SendToDisplay extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			String message = params[0];
			if (ReceiptSetting.make == ReceiptSetting.MAKE_CUSTOM)
				EscPosDriver.SendToDisplay(message);
			if (ReceiptSetting.make == ReceiptSetting.MAKE_PT6210)
				EscPosDriver.SendToPT6210Display(message);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			sendingToDisplay = false;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	private class ProcessReversal extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			postData();
			extractXML();
			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			Message m = new Message();
			m.what = 20;
			handler.sendMessage(m);
			// log.v("Response", ""+result);

			if (CmdStatus.equals("Declined")) {
				// Error
				alertbox(getResources().getString(R.string.declined_name),
						"Reason: " + TextResponse);
			} else if (CmdStatus.equals("Approved")) {
				// Approved
				alertbox(getResources().getString(R.string.reversed_name),
						"Reason: " + TextResponse);
				Payment Presult = cart.Payments.get(cart.Payments.size() - 1);
				Presult.processed = -2;
				ProductDatabase.replaceMercuryPartial(Presult);

				cart.Payments.remove(cart.Payments.size() - 1);
				updateTotals();
			} else {
				// Declined
				alertbox(getResources().getString(R.string.error), "Reason: "
						+ TextResponse);
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public String postData() {

		Payment payment = cart.Payments.get(cart.Payments.size() - 1);
		if (!payment.paymentType.equals(getResources().getString(
				R.string.credit_card)))
			return null;

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument(null, Boolean.valueOf(true));
			// serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",
			// true);
			serializer.startTag("", "TStream");
			serializer.startTag("", "Transaction");

			serializer.startTag("", "MerchantID");
			serializer.text(PrioritySetting.merchantID);
			serializer.endTag("", "MerchantID");

			serializer.startTag("", "TranType");
			serializer.text("Credit");
			serializer.endTag("", "TranType");

			serializer.startTag("", "TranCode");
			serializer.text("VoidSaleByRecordNo");
			serializer.endTag("", "TranCode");

			serializer.startTag("", "InvoiceNo");
			serializer.text(payment.InvoiceNo);
			serializer.endTag("", "InvoiceNo");

			serializer.startTag("", "RefNo");
			serializer.text(payment.RefNo);
			serializer.endTag("", "RefNo");

			int stringId = getApplicationInfo().labelRes;
			String version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;

			serializer.startTag("", "Memo");
			serializer.text(getString(stringId) + " v" + version);
			serializer.endTag("", "Memo");

			serializer.startTag("", "RecordNo");
			serializer.text(payment.RecordNo);
			serializer.endTag("", "RecordNo");

			serializer.startTag("", "Frequency");
			serializer.text("OneTime");
			serializer.endTag("", "Frequency");

			serializer.startTag("", "Amount");
			serializer.startTag("", "Purchase");
			serializer.text(df.format(payment.paymentAmount / 100f));
			serializer.endTag("", "Purchase");
			serializer.endTag("", "Amount");

			serializer.startTag("", "TranInfo");

			serializer.startTag("", "AuthCode");
			serializer.text(payment.AuthCode);
			serializer.endTag("", "AuthCode");

			serializer.startTag("", "AcqRefData");
			serializer.text(payment.AcqRefData);
			serializer.endTag("", "AcqRefData");

			serializer.startTag("", "ProcessData");
			serializer.text(payment.ProcessData);
			serializer.endTag("", "ProcessData");

			serializer.endTag("", "TranInfo");

			serializer.startTag("", "TerminalName");
			serializer.text(PrioritySetting.terminalName);
			serializer.endTag("", "TerminalName");

			serializer.startTag("", "OperatorID");
			serializer.text(cashier.name);
			serializer.endTag("", "OperatorID");

			serializer.endTag("", "Transaction");
			serializer.endTag("", "TStream");
			serializer.endDocument();

			String result = writer.toString();

			// log.v("Response", ""+result);

			try {
				WebRequest mpswr = new WebRequest(PrioritySetting.mWSURL);
				mpswr.addParameter("tran", result);
				mpswr.setWebMethodName("CreditTransaction");
				mpswr.setTimeout(10);

				mpsResponse = mpswr.sendRequest();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			// alertbox("Exception", "error occurred while creating xml data");
			Log.e("Exception", "error occurred while creating xml data");
			e.printStackTrace();
		}

		return mpsResponse;
	}

	private class ProcessManualReversal extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			Payment payment = cart.Payments.get(cart.Payments.size() - 1);
			if (!payment.paymentType.equals(getResources().getString(
					R.string.manual)))
				return null;

			int stringId = getApplicationInfo().labelRes;
			String version = "";
			try {
				version = getPackageManager().getPackageInfo(getPackageName(),
						0).versionName;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONObject json = new JSONObject();

			try {
				json.put("MerchantID", PrioritySetting.hostedMID);
				json.put("pw", PrioritySetting.hostedPass);
				json.put("PurchaseAmount",
						df.format(payment.paymentAmount / 100f));
				json.put("Invoice", payment.InvoiceNo);
				json.put("RefNo", payment.RefNo);
				json.put("TerminalName", PrioritySetting.terminalName);
				json.put("OperatorID", cashier.name);
				json.put("Memo", getString(stringId) + " v" + version);
				json.put("AuthCode", payment.AuthCode);
				json.put("ProcessData", payment.ProcessData);
				json.put("AcqRefData", payment.AcqRefData);
				json.put("Token", payment.RecordNo);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			postManualData(json);
			// extractXML();
			return mpsResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			Message m = new Message();
			m.what = 20;
			handler.sendMessage(m);
			// log.v("Response", ""+result);

			if (CmdStatus.equals("Declined")) {
				// Error
				alertbox(getResources().getString(R.string.declined_name),
						"Reason: " + TextResponse);
			} else if (CmdStatus.equals("Approved")) {
				// Approved
				alertbox(getResources().getString(R.string.reversed_name),
						"Response: " + TextResponse);
				Payment Presult = cart.Payments.get(cart.Payments.size() - 1);
				Presult.processed = -2;
				ProductDatabase.replaceMercuryPartial(Presult);

				cart.Payments.remove(cart.Payments.size() - 1);
				updateTotals();
			} else {
				// Declined
				alertbox(getResources().getString(R.string.error), "Reason: "
						+ TextResponse);
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public void extractXML() {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			ReversalHandler dataHandler = new ReversalHandler();
			xr.setContentHandler(dataHandler);

			// log.v("Resopnse", mpsResponse);

			ByteArrayInputStream in = new ByteArrayInputStream(
					mpsResponse.getBytes());
			xr.parse(new InputSource(in));

			// data = dataHandler.getData();

		} catch (ParserConfigurationException pce) {
			Log.e("SAX XML", "sax parse error", pce);
		} catch (SAXException se) {
			Log.e("SAX XML", "sax error", se);
		} catch (IOException ioe) {
			Log.e("SAX XML", "sax parse io error", ioe);
		}
	}

	public String postManualData(JSONObject json) {

		HttpClient httpclient = new DefaultHttpClient();

		String result = null;
		try {
			HttpPost httppost = new HttpPost(PrioritySetting.mVOIDURL);

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			Log.v("SEND", json.toString());
			nvp.add(new BasicNameValuePair("json", json.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();
				String jsonResult = inputStreamToString(is).toString();

				JSONObject object = new JSONObject(jsonResult);

				if (object.has("CreditVoidSaleTokenResult")) {
					JSONObject CreditVoidSaleTokenResult = object
							.getJSONObject("CreditVoidSaleTokenResult");

					if (CreditVoidSaleTokenResult.has("Status")) {
						CmdStatus = CreditVoidSaleTokenResult
								.getString("Status");
					}

					if (CreditVoidSaleTokenResult.has("Message")) {
						TextResponse = CreditVoidSaleTokenResult
								.getString("Message");
					}
				}

				if (object.has("CreditReversalTokenResult")) {
					JSONObject CreditVoidSaleTokenResult = object
							.getJSONObject("CreditReversalTokenResult");

					if (CreditVoidSaleTokenResult.has("Status")) {
						CmdStatus = CreditVoidSaleTokenResult
								.getString("Status");
					}

					if (CreditVoidSaleTokenResult.has("Message")) {
						TextResponse = CreditVoidSaleTokenResult
								.getString("Message");
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public class ReversalHandler extends DefaultHandler {

		private String tempVal;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {

		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {

			if (localName.equals("CmdStatus")) {
				CmdStatus = tempVal;
			}

			if (localName.equals("TextResponse")) {
				TextResponse = tempVal;
			}

		}
	}
	
	public void searchProductQuantity(){
		
		AlertDialog.Builder builder3;
		final AlertDialog alertDialog3 ;
		
		LayoutInflater inflater3 = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View mylayout = inflater3.inflate(R.layout.export,
				(ViewGroup) findViewById(R.id.exportmain));

		final EditText nameEdit = (EditText) mylayout
				.findViewById(R.id.editText1);
		final TextView text = (TextView) mylayout
				.findViewById(R.id.textView1);
		final ScrollView table = (ScrollView) mylayout.findViewById(R.id.scrollView1);
		table.setVisibility(View.VISIBLE);
		final TableLayout quantityList = (TableLayout) mylayout
				.findViewById(R.id.table_main);

		text.setTextAppearance(this,
				android.R.style.TextAppearance_Medium);
		text.setText(getResources().getString(R.string.add_quantity_search_message));

		builder3 = new AlertDialog.Builder(this);
		builder3.setView(mylayout)
			.setTitle(getResources().getString(R.string.add_note_search_qunatity))
					.setInverseBackgroundForced(true)
					.setPositiveButton(getResources().getString(R.string.search_quantity),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							})
					.setNegativeButton(getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
		alertDialog3 = builder3.create();
		
		alertDialog3.setOnShowListener(new DialogInterface.OnShowListener() {
						
					    @Override
					    public void onShow(DialogInterface dialog) {
			
					        Button b = alertDialog3.getButton(AlertDialog.BUTTON_POSITIVE);
					        b.setOnClickListener(new View.OnClickListener() {
			
					            @Override
					            public void onClick(View view) {
					            	if(!nameEdit.getText().equals(null) && nameEdit.getText().length() > 2){
					            		Cursor c = ProductDatabase.helper.fetchItemsQuantity(nameEdit.getText().toString());
					            		//TableLayout list = (TableLayout) findViewById(R.id.table_main);
					            		quantityList.removeAllViews();
						            	if(c.getCount() > 0){
						            		TableRow tbrow0 = new TableRow(view.getContext());
						                    TextView tv0 = new TextView(view.getContext());
						                    tv0.setText("ProductName");
						                    tv0.setGravity(Gravity.LEFT);
						                    tv0.setTextSize((float) 25);
						                    tbrow0.addView(tv0);
						                    TextView tv1 = new TextView(view.getContext());
						                    tv1.setText("Quantity");
						                    tv1.setGravity(Gravity.RIGHT);
						                    tv1.setTextSize((float) 25);
						                    tbrow0.setBackgroundColor(Color.parseColor("#D3D3D3"));
						                    tbrow0.addView(tv1);
						                    quantityList.addView(tbrow0);
						                    while(!c.isAfterLast()){
						                    	
						                    	TableRow tbrow = new TableRow(view.getContext());
						                        TextView t1v = new TextView(view.getContext());
						                        t1v.setText(c.getString(c.getColumnIndex("name")));
						                        t1v.setGravity(Gravity.LEFT);
						                        t1v.setTextSize((float) 25);
						                        tbrow.addView(t1v);
						                        TextView t2v = new TextView(view.getContext());
						                        t2v.setText(c.getString(c.getColumnIndex("quantity")));
						                        t2v.setGravity(Gravity.RIGHT);
						                        t2v.setTextSize((float) 25);
						                        tbrow.addView(t2v);
						                    	
						                        quantityList.addView(tbrow);
						                        
						                    	c.moveToNext();
						                    }
						            		
						                    quantityList.setVisibility(View.VISIBLE);
						            	}else{
						            		TableRow tbrow0 = new TableRow(view.getContext());
						                    TextView tv0 = new TextView(view.getContext());
						                    tv0.setText(" Product not found ");
						                    //tv0.setTextColor(Color.WHITE);
						                    tbrow0.addView(tv0);
						                    quantityList.addView(tbrow0);
						            	}
					            	}
					            }
					        });
					    }
					});

		alertDialog3.show();

		alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE).setEnabled(false);

		nameEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				if (!nameEdit.getText().toString().equals("") && nameEdit.getText().toString().length() > 2) {
					alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE)
					.setEnabled(true);
				} else {
					alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE)
					.setEnabled(false);
				}
			}
		});

	}

	public void addNote() {
		if (saleProcessed) {
			alertbox(getResources().getString(R.string.sale_processed),
					getResources().getString(R.string.sale_processed_note));
			return;
		}

		if (cart.Payments.size() > 0) {
			alertbox(getResources().getString(R.string.sale_pending),
					getResources().getString(R.string.sale_pending_note));
			return;
		}

		AlertDialog.Builder builder3;
		final AlertDialog alertDialog3;

		LayoutInflater inflater3 = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		final View mylayout = inflater3.inflate(R.layout.export,
				(ViewGroup) findViewById(R.id.exportmain));

		final EditText nameEdit = (EditText) mylayout
				.findViewById(R.id.editText1);
		final TextView text = (TextView) mylayout.findViewById(R.id.textView1);

		text.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		text.setText(getResources().getString(R.string.add_note_message));

		builder3 = new AlertDialog.Builder(this);
		builder3.setView(mylayout)
				.setTitle(getResources().getString(R.string.add_note_to_sale))
				.setInverseBackgroundForced(true)
				.setPositiveButton(getResources().getString(R.string.add_note),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (!nameEdit.getText().toString().equals("")) {
									String name = nameEdit.getText().toString();
									Product product = new Product();
									product.name = name;
									product.price = 0;
									product.isNote = true;

									cart.AddProduct(product);
									((ProductAdapter) inventoryList
											.getAdapter())
											.notifyDataSetChanged();
									updateTotals();
								}
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		alertDialog3 = builder3.create();
		alertDialog3.show();

		alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE).setEnabled(false);

		nameEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!nameEdit.getText().toString().equals("")) {
					alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE)
							.setEnabled(true);
				} else {
					alertDialog3.getButton(alertDialog3.BUTTON_POSITIVE)
							.setEnabled(false);
				}
			}
		});
	}

	public void keyboardClick(View v) {

		if (!keyBoardView) {
			salesScreen.setVisibility(View.GONE);
			if (buttonScreen != null)
				buttonScreen.setVisibility(View.VISIBLE);
			keyBoardView = true;
		} else {
			salesScreen.setVisibility(View.VISIBLE);
			if (buttonScreen != null)
				buttonScreen.setVisibility(View.GONE);
			keyBoardView = false;
		}
	}

	private boolean hasInternet() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();

		return isConnected;
	}

	private boolean capturing = false;
	private boolean backPressed = false;

	private String captureString = "";

	private long mLastPress = 0;
	public WakeLock mWakeLock;

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast onBackPressedToast = Toast.makeText(this, getResources()
					.getString(R.string.press_back), Toast.LENGTH_SHORT);
			long currentTime = System.currentTimeMillis();
			if (currentTime - mLastPress > 2000) {
				onBackPressedToast.show();
				mLastPress = currentTime;
			} else {
				onBackPressedToast.cancel(); // Difference with previous answer.
												// Prevent continuing showing
												// toast after application exit
				ArrayList<Cashier> cashiers = ProductDatabase.getCashiers();
				if (cashiers.size() > 0 && AdminSetting.enabled) {
					logoutCashier();
				}
				super.onBackPressed();
			}
			return true;
		}

		if (me.getCurrentFocus() != null) {
			if (!(me.getCurrentFocus() instanceof EditText)) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {

					if (saleProcessed) {
						alertbox(
								getResources().getString(
										R.string.sale_processed),
								getResources().getString(
										R.string.sale_processed_message));
						return true;
					}

					if (!captureString.equals("")) {
						Product product = getShop().findByBarcode(
								captureString.trim());
						if (product == null) {
							alertbox(
									getResources()
											.getString(R.string.not_found),
									getResources().getString(
											R.string.product_not_found)
											+ " - " + captureString);
						} else {

							if (quantitySelected) {
								product.quantity = quanAmount;
								quantitySelected = false;
								quantityAT.setVisibility(View.INVISIBLE);
							}

							cart.AddProduct(new Product(product));
							((ProductAdapter) inventoryList.getAdapter())
									.notifyDataSetChanged();
							updateTotals();
						}

						captureString = "";
					}

				} else {
					char pressedKey = (char) event.getUnicodeChar();
					if (keyCode != 59)
						captureString = captureString
								+ Character.toString(pressedKey);
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private boolean isHardwareKeyboardAvailable() {
		return getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
	}

	private class SendLogin extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject UPresponse = null;
			try {

				String license = mSharedPreferences.getString("APOS_LICENSE",
						"");
				String UID = Secure.getString(getContentResolver(),
						Secure.ANDROID_ID);
				String version = "";
				try {
					version = getPackageManager().getPackageInfo(
							getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONObject json = new JSONObject();

				json.put("LICENSE", license);
				json.put("UID", UID);
				json.put("make", Build.BRAND);
				json.put("model", Build.MODEL);
				json.put("android", Build.VERSION.RELEASE);
				json.put("version", version);

				UPresponse = postCheckUpdate(json);

				// mpsResponse = response.getString(name);

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return UPresponse;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// Do something here...
			// pd.dismiss();

			try {
				if (result != null
						&& result.getString("result").contains("upgrade")) {
					AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
							PointOfSale.this);

					dlgAlert.setMessage(getResources().getString(
							R.string.update_message));
					dlgAlert.setTitle(getResources().getString(
							R.string.update_available));
					dlgAlert.setCancelable(false);
					dlgAlert.setPositiveButton(
							getResources().getString(R.string.download),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									UpdateApp atualizaApp = new UpdateApp();
									atualizaApp.setContext(PointOfSale.this
											.getApplicationContext());
									asyncTask = atualizaApp
											.execute("http://prioritypos.azurewebsites.net/download/singleUpdate.php");
								}
							});
					dlgAlert.create().show();
					if (updatePressed) {
						updatePressed = false;
					}
				} else if (result != null
						&& result.getString("result").contains("good")) {
					Editor e = mSharedPreferences.edit();
					e.putLong("APOS_LASTLOG", new Date().getTime());
					e.commit();

					if (PrioritySetting.enabled) {
						if (result.has("mid")) {
							PrioritySetting.hostedMID = result.getString("mid");
						}

						if (result.has("mp")) {
							PrioritySetting.hostedPass = result.getString("mp");
						}

						getShop().insertMercurySettings();
					}
					if (updatePressed) {
						updatePressed = false;
						alertbox(
								getResources().getString(
										R.string.check_update_title),
								getResources().getString(
										R.string.check_update_good));
					}
				} else if (result != null
						&& result.getString("result").contains("expired")) {
					Editor e = mSharedPreferences.edit();
					e.putBoolean("APOS_LICENSED", false);
					e.putBoolean("APOS_REGISTERED", false);
					e.putString("APOS_FNAME", "");
					e.putString("APOS_LNAME", "");
					e.putString("APOS_COMPANY", "");
					e.putString("APOS_PHONE", "");
					e.putString("APOS_EMAIL", "");
					e.putString("APOS_WEBSITE", "");
					e.putString("APOS_ADDRESS1", "");
					e.putString("APOS_ADDRESS2", "");
					e.putString("APOS_CITY", "");
					e.putString("APOS_STATE", "");
					e.putString("APOS_POSTAL", "");
					e.putString("APOS_COUNTRY", "");
					e.putString("APOS_LICENSE", "");
					e.commit();
					ProductDatabase.backupDelete();
					finish();
				} else {
					if (updatePressed) {
						updatePressed = false;
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public JSONObject postCheckUpdate(JSONObject json) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();

		JSONObject object = null;
		try {
			HttpPost httppost = new HttpPost(
					"http://prioritypos.azurewebsites.net/approutines/SerialUpdate.php");

			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			nvp.add(new BasicNameValuePair("updateCheck", json.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();
				String jsonResult = inputStreamToString(is).toString();

				object = new JSONObject(jsonResult);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return object;
	}

	public class UpdateApp extends AsyncTask<String, String, Void> {
		private Context context;

		public void setContext(Context contextf) {
			context = contextf;
		}

		@Override
		protected Void doInBackground(String... arg0) {
			try {
				URL url = new URL(arg0[0]);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();

				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();
				int fileLength = c.getContentLength();

				String PATH = "/mnt/sdcard/Download/";
				File file = new File(PATH);
				file.mkdirs();
				File outputFile = new File(file, "update.apk");
				if (outputFile.exists()) {
					outputFile.delete();
				}
				FileOutputStream fos = new FileOutputStream(outputFile);

				InputStream is = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				long total = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
					total += len1;
					publishProgress(getResources().getString(
							R.string.downloading_update)
							+ (int) (total * 100 / fileLength) + "%");
				}
				fos.close();
				is.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Do something here...
			pd.dismiss();
			mWakeLock.release();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			/*intent.setDataAndType(
					Uri.fromFile(new File("/mnt/sdcard/Download/update.apk")),
					"application/vnd.android.package-archive");*/
			intent.setDataAndType(Uri.fromFile( new File(Environment.getExternalStorageDirectory()+ "/Download/update.apk")), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag
															// android returned
															// a intent error!
			context.startActivity(intent);
			/*
			Intent deleteIntent = new Intent(Intent.ACTION_DELETE);
			deleteIntent.setData(Uri.parse("package:com.prioritymobilepos"));
			startActivity(deleteIntent);*/
			
		}

		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					getClass().getName());
			mWakeLock.acquire();
			pd = ProgressDialog.show(PointOfSale.this, "", getResources()
					.getString(R.string.downloading_update), true, false);
			pd.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			pd.setMessage(values[0]);
		}
	}

	private StringBuilder inputStreamToString(InputStream is) {
		String rLine = "";
		StringBuilder answer = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		try {
			while ((rLine = rd.readLine()) != null) {
				answer.append(rLine);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return answer;
	}
	
	public void PrintHoldSale(){
		resentReceiptPrintFlag = true;
		todo = 4;
		pd = ProgressDialog.show(PointOfSale.this, "",
				getResources().getString(R.string.print_hold_sale), true, false);
		Thread thread = new Thread(PointOfSale.this);
		thread.start();
	}

	private String PrintCharge(Payment payment) {

		int cols = 40;

		if (ReceiptSetting.size == ReceiptSetting.SIZE_2)
			cols = 30;

		StringBuilder receiptString = new StringBuilder();

		// ------------Store Name----------------------
		if (!(StoreSetting.getName().equals(""))) {
			receiptString.append(
					EscPosDriver.wordWrap(StoreSetting.getName(), cols + 1))
					.append('\n');
		}

		// ------------Store Address----------------------
		if (!(StoreSetting.getAddress().equals(""))) {
			receiptString.append(
					EscPosDriver.wordWrap(StoreSetting.getAddress(), cols + 1))
					.append('\n');
		}

		// ---------------Store Number-----------------
		if (!(StoreSetting.getPhone().equals(""))) {
			receiptString.append(
					EscPosDriver.wordWrap(StoreSetting.getPhone(), cols + 1))
					.append('\n');
		}

		// -----------------Store Website----------------------
		if (!(StoreSetting.getWebsite().equals(""))) {
			receiptString.append(
					EscPosDriver.wordWrap(StoreSetting.getWebsite(), cols + 1))
					.append('\n');
		}

		// -----------------------Store Email-----------------------------
		if (!(StoreSetting.getEmail().equals(""))) {
			receiptString.append(
					EscPosDriver.wordWrap(StoreSetting.getEmail(), cols + 1))
					.append('\n');
		}

		// -------------------Date------------------------

		String date = DateFormat.getDateTimeInstance().format(new Date());
		receiptString.append('\n');
		receiptString.append(EscPosDriver.wordWrap(date, cols + 1))
				.append('\n');
		receiptString.append(
				EscPosDriver.wordWrap("Transaction: " + payment.InvoiceNo,
						cols + 1)).append('\n');
		receiptString.append('\n');

		receiptString.append(
				EscPosDriver.wordWrap("Card # (Last 4): "
						+ payment.PrintCardNumber, cols + 1)).append('\n');
		receiptString
				.append(EscPosDriver.wordWrap("Card Auth:       "
						+ payment.AuthCode, cols + 1)).append('\n')
				.append('\n');

		if (Float.valueOf(payment.chargeamount) > 0) {
			receiptString.append(
					EscPosDriver.wordWrap("Trans Type:      Sale", cols + 1))
					.append('\n');
		} else {
			receiptString.append(
					EscPosDriver.wordWrap("Trans Type:      Return", cols + 1))
					.append('\n');
		}

		receiptString
				.append(EscPosDriver.wordWrap("Trans Amount:    "
						+ payment.chargeamount, cols + 1)).append('\n')
				.append('\n');

		receiptString
				.append(EscPosDriver
						.wordWrap(
								"I agree to pay the above amount according to the card issuer agreement.",
								cols + 1)).append('\n');
		receiptString.append(EscPosDriver.wordWrap("Sign below:", cols + 1))
				.append('\n').append('\n');

		StringBuffer message;

		if (cols == 40)
			message = new StringBuffer(
					"X________________________________________".substring(0,
							cols));
		else
			message = new StringBuffer(
					"X_____________________________".substring(0, cols));

		receiptString.append(
				EscPosDriver.wordWrap(message.toString(), cols + 1)).append(
				'\n');

		receiptString
				.append(EscPosDriver
						.wordWrap(payment.PrintCardHolder, cols + 1))
				.append('\n').append('\n');

		return receiptString.toString();
	}
}