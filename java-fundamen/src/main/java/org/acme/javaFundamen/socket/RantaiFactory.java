package org.acme.javaFundamen.socket;

public class RantaiFactory {

    public static Rantai getRantai(String namaMesin) {
        if ("daging".equals(namaMesin)) return new PenggilingDaging();
        else if ("kelapa".equals(namaMesin)) return new PenggilingKelapa();
        else if ("genset".equals(namaMesin)) return new Genset();
        else if ("manusia".equals(namaMesin)) return new Rantai() {
            @Override
            public int putar() {
                return 999;
            }
        };
        else throw new RuntimeException("Mesin tidak diketahui");
    }
}
