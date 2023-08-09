package org.acme.javaFundamen.socket;

public interface Rantai {
    int putar();

    default int getar() {
        int putar = putar();
        return (int) (putar * 0.5);
    }
}
