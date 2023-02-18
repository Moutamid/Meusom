package com.moutamid.meusom.utilis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Utils {
    private static final String PACKAGE_NAME = "dev.moutamid.meusom";

    private SharedPreferences sharedPreferences;

    public void removeSharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public String getStoredString(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(name, "Error");
    }

    public void storeString(Context context, String name, String object) {
        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(name, object).apply();
    }

    public void storeBoolean(Context context1, String name, boolean value) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(name, value).apply();
    }

    public boolean getStoredBoolean(Context context1, String name) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(name, false);
    }

    public void storeInteger(Context context1, String name, int value) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(name, value).apply();
    }

    public int getStoredInteger(Context context1, String name) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(name, 0);
    }

    public int getAdsInteger(Context context1, String name) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(name, 5);
    }

    public void storeFloat(Context context1, String name, float value) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putFloat(name, value).apply();
    }

    public float getStoredFloat(Context context1, String name) {
        sharedPreferences = context1.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(name, 0);
    }

    //    public void storeArrayList(Context context, String name, ArrayList<String> arrayList) {
//        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor edit = sharedPreferences.edit();
//        Set<String> set = new HashSet<>(arrayList);
//        edit.putStringSet(name, set);
//        edit.apply();
//    }
//
//    public ArrayList<String> getStoredArrayList(Context context, String name) {
//        sharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
//        Set<String> defaultSet = new HashSet<>();
//        defaultSet.add("Error");
//        Set<String> set = sharedPreferences.getStringSet(name, defaultSet);
//        return new ArrayList<>(set);
//    }

    public boolean fileExists(String name) {
        String path_save_vid = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp3";

        } else {
            path_save_vid =
                    Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp3";
        }
        File file = new File(path_save_vid);
        return file.exists();

    }

    public boolean videoExists(String name) {
        String path_save_vid = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp4";

        } else {
            path_save_vid =
                    Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp4";
        }
        File file = new File(path_save_vid);
        return file.exists();

    }

    public String getSongPath(String name) {
        String path_save_vid = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp3";

        } else {
            path_save_vid =
                    Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp3";

        }
        return path_save_vid;
    }

    public String getVideoPath(String name) {
        String path_save_vid = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp4";

        } else {
            path_save_vid =
                    Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            File.separator
                            + "Meusom."
                            + File.separator
                            + name
                            + ".mp4";

        }
        return path_save_vid;
    }

    public String getPath() {
        String path_save_vid = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment
                            .getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) +
                            File.separator
                            + "Meusom."
                            + File.separator;

        } else {
            path_save_vid =
                    Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            File.separator
                            + "Meusom."
                            + File.separator;

        }
        return path_save_vid;
    }

    public long getRandomNmbr(int length) {
        return new Random().nextInt(length) + 1;
    }

    public void changeLanguage(Context context, String languageToLoad) {
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    public void showDialog(Context context, String title, String message, String positiveBtnName, String negativeBtnName, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener, boolean cancellable) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtnName, positiveListener)
                .setNegativeButton(negativeBtnName, negativeListener)
                .setCancelable(cancellable)
                .show();
    }

    public String getLastSunday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -(cal.get(Calendar.DAY_OF_WEEK) - 1));
//        Toast.makeText(context(), "" + cal.get(Calendar.DATE), Toast.LENGTH_SHORT).show();
//        Toast.makeText(context(), cal.getTime().toString(), Toast.LENGTH_SHORT).show();
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date);
    }

    public boolean isTodaySunday() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public String getDate() {

        try {

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";

    }

}
