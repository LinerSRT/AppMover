package com.liner.appmover;

public class AppHolder {
    private String appName;
    private String appPackageName;
    private String appSourceDir;
    private boolean movedToSystem;
    private String systemPath;
    private boolean isSelected;

    public AppHolder(String appName, String appPackageName, String appSourceDir, boolean movedToSystem, boolean isSelected) {
        this.appName = appName;
        this.appPackageName = appPackageName;
        this.appSourceDir = appSourceDir;
        this.movedToSystem = movedToSystem;
        this.isSelected = isSelected;
        this.systemPath = "/system/priv-app/"+appPackageName+"/"+appPackageName+".apk";
    }

    String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    String getAppSourceDir() {
        return appSourceDir;
    }

    public void setAppSourceDir(String appSourceDir) {
        this.appSourceDir = appSourceDir;
    }

    boolean isMovedToSystem() {
        return movedToSystem;
    }

    void setMovedToSystem(boolean movedToSystem) {
        this.movedToSystem = movedToSystem;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "AppHolder{" +
                "appName='" + appName + '\'' +
                ", appPackageName='" + appPackageName + '\'' +
                ", appSourceDir='" + appSourceDir + '\'' +
                ", movedToSystem=" + movedToSystem +
                ", systemPath='" + systemPath + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
