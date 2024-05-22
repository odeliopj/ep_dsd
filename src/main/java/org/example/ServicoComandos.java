package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public final class ServicoComandos {

    public static void listarVizinhos(No noEscolhido){
        long numeroDeVizinhos = 0;
        numeroDeVizinhos = noEscolhido.getVizinhos().size();
        System.out.println("Ha " + numeroDeVizinhos + " vizinhos na tabela:");

        for (int i = 0; i < numeroDeVizinhos; i++) {
            No vizinho = noEscolhido.getVizinhos().get(i);
            System.out.println("[" + i + "] " + vizinho.getEndereco() + ":" + vizinho.getPorta());
        }
    }

    public static void enviarHello(No noEscolhido) {
        try (Socket socket = new Socket(noEscolhido.getEndereco(), noEscolhido.getPorta())) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("HELLO " + noEscolhido.getEndereco() + ":" + noEscolhido.getPorta()); // Enviar a mensagem HELLO com o endereço de origem

            System.out.println("Mensagem HELLO enviada para " + noEscolhido.getEndereco() + ":" + noEscolhido.getPorta());
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem HELLO: " + e.getMessage());
        }
    }

    public static void alterarValorPadraoTTL(Rede rede) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite novo valor de TTL: ");
        int novoValorTTL = scanner.nextInt();

        rede.setTtlPadrao(novoValorTTL);
        System.out.println("Valor de TTL alterado para " + novoValorTTL);
    }
}
