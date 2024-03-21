package org.acme.authenticationService.dao.web;


import org.acme.authorization.json.UserOnly;

public class BaseModel {

    public UserOnly user;
    public String appName;
    public String search;
    public int page = 1;
    public int size = 1;
    public int totalData = 0;
    public boolean isAlert = false;
    public boolean isAlertSuccess;
    public String alertMessage;

    public BaseModel() {
    }

    public BaseModel(UserOnly user, String appName) {
        this.user = user;
        this.appName = appName;
    }

    public int nextPage(){
        return page+1;
    }

    public int prevPage(){
        return Math.max(1, page -1);
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "user=" + user +
                ", appName='" + appName + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", search='" + search + '\'' +
                ", totalData=" + totalData +
                '}';
    }
}
