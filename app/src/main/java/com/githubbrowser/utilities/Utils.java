package com.githubbrowser.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {

    public static void showToast(String message, Context ctx){
        Toast t1=Toast.makeText(ctx, message, Toast.LENGTH_LONG);
        t1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        t1.show();
    }

    public static boolean hasInternetAccess(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void savePreferences(String key, String value, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void savePreferences(String key, Boolean value, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void savePreferences(String key, int value, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void savePreferencesLong(String key, long value, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void savePreferences(String key, LinkedHashSet<String> value, Context ctx){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getPackageName(), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}

