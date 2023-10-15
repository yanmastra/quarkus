package org.acme.authenticationService.dao.web;

public class ErrorModel extends BaseModel{
    public int status;
    public String msg1;
    public String msg2;

    public ErrorModel() {
    }

    public ErrorModel(int status, String msg1) {
        this.status = status;
        this.msg1 = msg1;
    }


}
