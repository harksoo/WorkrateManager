package com.sovate.workratemanager.bean;

/**
 * Created by harks on 2016-03-05.
 */
public class ActivityDevice {

    String name = "";
    String mac = "";
    String alias = "";
    String description = "";

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
