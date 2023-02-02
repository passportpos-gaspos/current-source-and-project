package com.pos.passport.service;

/**
 * Created by imd-macmini on 9/1/16.
 */

public class MyFirebaseMessagingService /*extends FirebaseMessagingService*/ {

    /*private static final String TAG = "MyFirebaseMsgService";
    //DbExportImport dbExportImport;
    *//**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     *//*
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //dbExportImport=new DbExportImport(this);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            JSONObject dataget = new JSONObject(remoteMessage.getData());

            if (dataget.has("sync"))
            {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.SyncBroadcastReceiver.PROCESS_SYNC_RESPONSE);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                this.sendBroadcast(broadcastIntent);
            } else
            {
                if (CallClassMethod() == 0)
                {
                    sendNotification(dataget);
                } else
                {
                    try {
                        setCallbacks();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    *//**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     *//*
    private void sendNotification(JSONObject messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.top_icon)
                .setContentTitle(messageBody.optString("title"))
                .setContentText(messageBody.optString("message"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }

    public int CallClassMethod() {
        //Log.e("Call method","Callmethod");
        int show_ = 0;
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).topActivity.toString().equalsIgnoreCase("ComponentInfo{com.pos.cumulus/com.pos.cumulus.activity.MainActivity}")) {
                show_ = 1;
            }
        }
        return show_;
    }


    public void setCallbacks() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.OrderUpdate.PROCESS_RESPONSE_1);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("update", "0");
        this.sendBroadcast(broadcastIntent);
    }
    synchronized void callSync()
    {
        synchronized(this)
        {
            if (Utils.hasInternet(this))
            {
                //SendSyncAsyncTask sendSyncTask = new SendSyncAsyncTask(MainActivity.this, true)

                    SendSyncAsyncTask sendSyncTask = new SendSyncAsyncTask(this, true, true, true) {
                        @Override
                        protected void onPostExecute(String result)
                        {
                            super.onPostExecute(result);
                            if (result != null && result.contains("success"))
                            {
                                if (CallClassMethod() != 0)
                                {
                                    Log.e("Open activity", "Main activity open");
                                    try
                                    {
                                        setCallbacks();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                    };
                    sendSyncTask.execute();

            } else {
                Utils.alertBox(this, getString(R.string.txt_no_network), getString(R.string.msg_unable_to_download_data));
            }
        }
    }*/
}