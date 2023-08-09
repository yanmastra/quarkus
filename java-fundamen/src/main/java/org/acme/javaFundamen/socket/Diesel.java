package org.acme.javaFundamen.socket;

public class Diesel {
    private Rantai rantai = null;
    private int speed = 1;

    public int run(int putaran, String namaMesin) {
        System.out.printf("input puratan: %s, mesin: %s%n", putaran, namaMesin);
        rantai = RantaiFactory.getRantai(namaMesin);
        int result = 0;
        for (int i=0; i<putaran; i = i+speed) {
            result += rantai.putar();
        }
        return result;
    }

}
