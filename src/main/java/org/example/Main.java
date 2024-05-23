package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Rede rede = new Rede();

        Servicos.criarNosRede(rede);

        Servicos.menuComandos(rede);
    }
}
