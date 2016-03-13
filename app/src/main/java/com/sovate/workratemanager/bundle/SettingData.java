package com.sovate.workratemanager.bundle;

import java.io.Serializable;

/**
 * Created by harksoo on 2016-03-09.
 */
public class SettingData implements Serializable {

    String serverUrl = "";

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

}
