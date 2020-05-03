package com.liner.appmover;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppHelper {
    private Context context;
    private List<AppHolder> appHolderList;


    public AppHelper(Context context) {
        this.context = context;
        getAppList();
    }


    List<AppHolder> getAppList() {
        appHolderList = loadAppList();
        if (appHolderList == null || appHolderList.size() <= 0) {
            appHolderList = fillAppList();
        }
        return appHolderList;
    }

    private List<AppHolder> fillAppList() {
        PackageManager packageManager = context.getPackageManager();
        appHolderList = new ArrayList<>();
        for (ApplicationInfo packageInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (packageInfo.sourceDir.contains("/data/app/")) {
                appHolderList.add(new AppHolder(getApplicationName(packageInfo.packageName), packageInfo.packageName, packageInfo.sourceDir, false, false));
            }
        }
        Collections.sort(appHolderList, new Comparator<AppHolder>() {
            Collator collator = Collator.getInstance(context.getResources().getConfiguration().getLocales().get(0));

            @Override
            public int compare(AppHolder appName1, AppHolder appName2) {
                return collator.compare(appName1.getAppName(), appName2.getAppName());
            }
        });
        saveAppList(appHolderList);
        return appHolderList;
    }

    boolean isAppInstalled(String packageName){
        PackageManager packageManager = context.getPackageManager();
        appHolderList = new ArrayList<>();
        for (ApplicationInfo packageInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (packageInfo.sourceDir.contains("/data/app/"))
                if (packageName.equals(packageInfo.packageName))
                    return true;
        }
        return false;
    }


    boolean searchAPK(File file, String packageName){
        File[] fileList = file.listFiles();
        if(fileList != null) {
            for (File item : file.listFiles()) {
                if (item.isDirectory()) {
                    searchAPK(item, packageName);
                } else {
                    if(item.getName().equals(packageName)){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    void saveAppList(List<AppHolder> appList) {
        try {
            File path = new File(context.getApplicationInfo().dataDir);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, "data_file.lsr");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(new Gson().toJson(appList).getBytes());
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private List<AppHolder> loadAppList() {
        File path = new File(context.getApplicationInfo().dataDir);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, "data_file.lsr");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert in != null;
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Type listType = new TypeToken<ArrayList<AppHolder>>() {
        }.getType();

        return new Gson().fromJson(new String(bytes), listType);
    }

    private String getApplicationName(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
    }

    static Drawable getApplicationIcon(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Drawable appIcon;
        try {
            appIcon = packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            appIcon = context.getDrawable(R.drawable.blank_app);
        }
        return appIcon;
    }
}
