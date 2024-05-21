package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Rede rede = new Rede();
        int quantidadeNos = 0;
        Scanner scanner = new Scanner(System.in);

        while(quantidadeNos <= 0) {
            System.out.print("Digite o número de nós da rede: ");
            quantidadeNos = scanner.nextInt();
        }

        for (int i = 0; i < quantidadeNos; i++) {
            rede.obterNosDoUsuario(rede);
        }
    }
}
