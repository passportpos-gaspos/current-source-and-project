package com.pos.passport.util;

import android.content.Context;

import com.pos.passport.BuildConfig;
import com.pos.passport.R;

/**
 * Created by Kareem on 11/1/2016.
 */

public class Configurations {

    private Context context;

    public Configurations(Context context){
        this.context = context;
    }

    public void setDefaults()
    {
        if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("dev"))
        {
            parser(R.array.config_dev);
        }else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("prod"))
        {
            parser(R.array.config_prod);
        }else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("cumulus_dev"))
        {
            parser(R.array.config_passportpos_dev);
        }else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("cumulus_uat")){
            parser(R.array.config_passportpos_uat);
        }else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("passport")){
            parser(R.array.config_passport);
        }
    }

    private void parser(int resource){
        try {
            String[] resourceParser = context.getResources().getStringArray(resource);
            //Log.e("BaseUrl ","Url call"+resourceParser[0]);
            UrlProvider.setBaseUrl(resourceParser[0]);
            UrlProvider.setBase_inner(resourceParser[1]);
            UrlProvider.setAccess_key(resourceParser[2]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
