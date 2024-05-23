package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Rede rede = new Rede();
        int quantidadeNos = 0;
        int comandoEscolhido = -1;
        String enderecoPortaNoEscolhido = null;
        Scanner scanner = new Scanner(System.in);

        while(quantidadeNos <= 0) {
            System.out.print("Digite o número de nós que a rede terá: ");
            quantidadeNos = scanner.nextInt();
        }

        for (int i = 0; i < quantidadeNos; i++) {
            Scanner scannerNos = new Scanner(System.in);
            System.out.print("[" + i + "] " + "Digite o: 'endereco:porta + {nome do arquivo de vizinhos} + {nome do arquivo de chaves-valor}': ");
            String dadosNovoNo = scannerNos.nextLine();

            Servicos.criarNo(dadosNovoNo, rede);
        }

        Servicos.menuComandos(rede);
    }
}
