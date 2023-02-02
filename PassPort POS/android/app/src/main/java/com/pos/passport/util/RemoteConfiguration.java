package com.pos.passport.util;

public class RemoteConfiguration {

    /*private static RemoteConfiguration instance = null;

    public FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    long cacheExpiration = 3600;

    protected RemoteConfiguration(){

    }

    public static RemoteConfiguration getInstance(){
        if(instance == null) {
            instance = new RemoteConfiguration();
        }
        return instance;
    }

    public void setDefaults(){
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("dev"))
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_dev_defaults);
        else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("prod"))
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_prod_defaults);
        else
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_dev_defaults);


        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        setRemoteConfig();
    }

    public void fetchRemoteConfig() {

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("dev"))
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_dev_defaults);
        else if(BuildConfig.ENVIRONMENT.equalsIgnoreCase("prod"))
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_prod_defaults);
        else
            mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_dev_defaults);


        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Log.e("remote config", "Firebase remote config fetch failed");
                        }
                    }
                });

        setRemoteConfig();
    }

    public void fetch(){
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Remote config", "Fetch Succeeded");
                        // Once the config is successfully fetched it must be activated before newly fetched values are returned.
                        mFirebaseRemoteConfig.activateFetched();
                        setRemoteConfig();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("Remote config", "Fetch failed");
                    }
                });
    }
    public void setRemoteConfig()
    {
        //Log.e("setRemoteConfig","setRemoteConfig on changing url");
        UrlProvider.setBaseUrl(mFirebaseRemoteConfig.getString(Consts.CONFIG_BASE_URL));
        UrlProvider.setAccess_key(mFirebaseRemoteConfig.getString(Consts.CONFIG_ACCESS_KEY));

        //Log.e("setRemoteConfig","inner>>"+mFirebaseRemoteConfig.getString(Consts.CONFIG_BASE_INNER));
        UrlProvider.setBase_inner(mFirebaseRemoteConfig.getString(Consts.CONFIG_BASE_INNER));
    }*/
}
