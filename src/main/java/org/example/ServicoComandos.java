package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public final class ServicoComandos {

    public static void alterarValorPadraoTTL(Rede rede) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite novo valor de TTL: ");
        int novoValorTTL = scanner.nextInt();

        rede.setTtlPadrao(novoValorTTL);
        System.out.println("Valor de TTL alterado para " + novoValorTTL);
    }
}
