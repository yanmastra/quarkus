package org.acme.javaFundamen;

import org.acme.javaFundamen.socket.Diesel;

public class Main {
    public static void main(String[] args) {
        Diesel diesel = new Diesel();
        int result = diesel.run(999, "manusia");
        System.out.println("hasil putaran: "+result);
    }
}
