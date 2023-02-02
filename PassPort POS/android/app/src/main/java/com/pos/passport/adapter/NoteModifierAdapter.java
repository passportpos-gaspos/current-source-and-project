package com.pos.passport.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.passport.R;
import com.pos.passport.model.Product;
import com.pos.passport.util.Utils;

import java.util.List;

/**
 * Created by imd-macmini on 11/14/16.
 */
public class NoteModifierAdapter extends BaseAdapter
{
    private Context context;
    private List<Product> mItemnote;
    private SetOnItemClick mListener;
    public NoteModifierAdapter(Context context, List<Product> itemnote)
    {
        this.context = context;
        this.mItemnote = itemnote;
    }
    public interface SetOnItemClick{
        void onItemClick(int posg);
    }
    public void setListener(SetOnItemClick listener){
        mListener = listener;
    }
    @Override
    public int getCount() {
        return mItemnote.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemnote.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        ViewHolder holder;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.view_notemodifier_row, parent, false);
            Utils.setTypeFace(Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) row);
            holder = new ViewHolder();
            holder.titlemsg = (TextView) row.findViewById(R.id.txt_note_msg);
            holder.img_close = (ImageView) row.findViewById(R.id.img_close);

            row.setTag(holder);
        } else
        {
            holder = (ViewHolder) row.getTag();
        }

        holder.titlemsg.setText(mItemnote.get(position).name + "( "+mItemnote.get(position).price+" )");
        holder.img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mListener.onItemClick(position);
            }
        });

        return row;
    }
    static class ViewHolder
    {
        TextView titlemsg;
        ImageView img_close;
    }
}
