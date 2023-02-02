package com.pos.passport.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.interfaces.QueueInterface;
import com.pos.passport.model.Category;
import com.pos.passport.model.Product;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 10/19/15.
 */
public class NoteFragmentNew extends DialogFragment {
    @IntDef({ NOTE_SCOPE_ALL, NOTE_SCOPE_ITEM, NOTE_SCOPE_ADD_ITEM })
    @Retention(RetentionPolicy.SOURCE)
    public @interface NoteScope {}

    public final static int NOTE_SCOPE_ITEM = 0;
    public final static int NOTE_SCOPE_ALL = 1;
    public final static int NOTE_SCOPE_ADD_ITEM = 2;

    private final static String TAG_DIALOG_FRAGMENT = "tag_dialog_fragment";

    private int mNoteScope;
    private List<Product> mProducts;
    private List<Category> mCategories;
    private int mSelected;
    private String mNote;
    private BigDecimal mPrice;

    private Spinner mItemSpinner;
    private EditText mNoteEditText;
    private Button mOkButton;
    private Button mCancelButton;
    private Button mPriceButton;

    private NoteListener mNoteListener;
    private QueueInterface mCallback;

    public interface NoteListener {
        void onNote(int position, String note, BigDecimal amount);
        void onDelete();
    }

    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelected = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private TextWatcher mNoteWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                mOkButton.setEnabled(false);
            } else {
                mOkButton.setEnabled(true);
            }
        }
    };

    private View.OnClickListener mPriceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TenPadDialogFragment fragment = TenPadDialogFragment.newInstance(TenPadDialogFragment.TEN_PAD_TYPE_PRICE, mPrice);
            fragment.setPriceListener(new TenPadDialogFragment.PriceListener() {
                @Override
                public void onSetPrice(BigDecimal amount) {
                    mPrice = amount;
                    mPriceButton.setText(DecimalFormat.getCurrencyInstance().format(amount.divide(Consts.HUNDRED)));
                }
            });
            fragment.show(getFragmentManager(), TAG_DIALOG_FRAGMENT);
        }
    };

    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NoteValidator validator = validate();
            if (validator.resId != 0) {
                Utils.alertBox(getActivity(), R.string.txt_add_note, validator.resId);
                validator.view.requestFocus();
                return;
            }
            Utils.dismissKeyboard(v);
            mNoteListener.onNote(mSelected - 1, mNoteEditText.getText().toString().trim(), mPrice);
            dismiss();
        }
    };

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //mCallback.onChangeFragment(MainActivity.FRAGMENT_BUTTONS);
            Utils.dismissKeyboard(v);
            dismiss();
        }
    };

    public static NoteFragmentNew newInstance(@NoteScope int noteScope, ArrayList<Product> products, int selected) {
        NoteFragmentNew f = new NoteFragmentNew();

        Bundle args = new Bundle();
        args.putInt("noteScope", noteScope);
        args.putSerializable("products", products);
        args.putInt("selected", selected);
        f.setArguments(args);

        return f;
    }

    public static NoteFragmentNew newInstance(@NoteScope int noteScope, ArrayList<Category> categories) {
        NoteFragmentNew f = new NoteFragmentNew();

        Bundle args = new Bundle();
        args.putInt("noteScope", noteScope);
        args.putSerializable("categories", categories);
        args.putInt("selected", 0);
        f.setArguments(args);

        return f;
    }

    public static NoteFragmentNew newInstance(@NoteScope int noteScope, String note, BigDecimal price) {
        NoteFragmentNew f = new NoteFragmentNew();
        Bundle args = new Bundle();
        args.putInt("noteScope", noteScope);
        args.putString("note", note);
        args.putSerializable("price", price);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try {
            mCallback = (QueueInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement QueueInterface");
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_note, container, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) v);
        mNoteScope = getArguments().getInt("noteScope");
        ArrayList<Product> products = (ArrayList<Product>)getArguments().getSerializable("products");
        if (products != null)
            mProducts = (ArrayList<Product>)products.clone();
        ArrayList<Category> categories = (ArrayList<Category>) getArguments().getSerializable("categories");
        if(categories !=null){
            mCategories = (ArrayList<Category>) categories.clone();
        }
        mSelected = getArguments().getInt("selected");
        mPrice = (BigDecimal)getArguments().getSerializable("price");
        if (mPrice == null)
            mPrice = BigDecimal.ZERO;
        mNote = getArguments().getString("note");
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindUIElements(view);
        setUpSpinner();
        setUpListeners();
        setUIs();
    }

    private void bindUIElements(View v) {
        mItemSpinner = (Spinner) v.findViewById(R.id.item_spinner);
        mNoteEditText = (EditText) v.findViewById(R.id.note_edit_text);
        mPriceButton = (Button)v.findViewById(R.id.price_button);
        mOkButton = (Button) v.findViewById(R.id.note_ok_button);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
    }

    private void setUpSpinner() {
        if (mNoteScope == NOTE_SCOPE_ALL) {
            if (mProducts == null)
                mProducts = new ArrayList<>();
            Product product = new Product();
            product.name = getString(R.string.txt_all_items);
            mProducts.add(0, product);
        }
        if (mProducts != null) {
            ItemAdapter adapter = new ItemAdapter(getActivity(), R.layout.view_spinner_item, mProducts);
            mItemSpinner.setAdapter(adapter);
            if (mNoteScope == NOTE_SCOPE_ITEM) {
                mItemSpinner.setSelection(mSelected);
                mItemSpinner.setEnabled(false);
            }
        } else if(mCategories != null){
            CategoryAdapter adapter = new CategoryAdapter(getActivity(), R.layout.view_spinner_item, mCategories);
            mItemSpinner.setAdapter(adapter);
            if (mNoteScope == NOTE_SCOPE_ADD_ITEM) {
                mItemSpinner.setSelection(mSelected);
            }
        }
        else {
            mItemSpinner.setVisibility(View.GONE);
        }
    }

    private void setUpListeners() {
        mItemSpinner.setOnItemSelectedListener(mItemSelectedListener);
        mNoteEditText.addTextChangedListener(mNoteWatcher);
        mPriceButton.setOnClickListener(mPriceClickListener);
        mOkButton.setOnClickListener(mOkClickListener);
        mCancelButton.setOnClickListener(mCancelClickListener);
    }

    private void setUIs() {
        if (mProducts == null)
            mItemSpinner.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(mNote)) {
            mNoteEditText.setText(mNote);
            mNoteEditText.setSelection(mNote.length());
        }
        if (mPrice != null && mPrice.compareTo(BigDecimal.ZERO) != 0)
            mPriceButton.setText(DecimalFormat.getCurrencyInstance().format(mPrice.divide(Consts.HUNDRED)));

        if (TextUtils.isEmpty(mNote))
            mOkButton.setEnabled(false);
        if(mCategories != null)
            mItemSpinner.setVisibility(View.VISIBLE);
    }

    private class NoteValidator {
        private View view;
        private @StringRes int resId;
    }

    private NoteValidator validate() {
        NoteValidator validator = new NoteValidator();

        if (TextUtils.isEmpty(mNoteEditText.getText().toString().trim())) {
            validator.view = mNoteEditText;
            validator.resId = R.string.msg_add_note;
        }

        return validator;
    }

    public void setNoteListener(NoteListener l) {
        mNoteListener = l;
    }

    private class CategoryAdapter extends ArrayAdapter<Category>{
        private Context context;
        private @LayoutRes int resource;

        public CategoryAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mCategories != null) {
                Category category = mCategories.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(category.getName());
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mCategories != null) {
                Category category = mCategories.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(category.getName());
            }

            return view;
        }
    }

    private class ItemAdapter extends ArrayAdapter<Product> {
        private Context context;
        private @LayoutRes int resource;

        public ItemAdapter(Context context, int resource, List<Product> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mProducts != null) {
                Product product = mProducts.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(product.name);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(resource, parent, false);
            }

            if (mProducts != null) {
                Product product = mProducts.get(position);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(product.name);
            }

            return view;
        }
    }
}
