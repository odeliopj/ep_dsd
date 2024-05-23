package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Rede rede = new Rede();
        int quantidadeNos = 0;
        int comandoEscolhido = -1;
        No noEscolhidoOrigem = null;
        boolean sairPrograma = false;
        String enderecoPortaNoEscolhido = null;
        Scanner scanner = new Scanner(System.in);

        while(quantidadeNos <= 0) {
            System.out.print("Digite o número de nós da rede: ");
            quantidadeNos = scanner.nextInt();
        }

        for (int i = 0; i < quantidadeNos; i++) {
            rede.criarNo(rede);
        }

        while (!sairPrograma){
            System.out.println("""
                \n
                Escolha o comando:

                [0] Listar vizinhos
                [1] HELLO
                [2] SEARCH (flooding)
                [3] SEARCH (random walk)
                [4] SEARCH (busca em profundidade)
                [5] Estatisticas
                [6] Alterar valor padrao de TTL
                [9] Sair;
             """);
            System.out.print("Digite o comando: ");
            comandoEscolhido = scanner.nextInt();

            if (comandoEscolhido != 9) {
                scanner = new Scanner(System.in);
                System.out.print("Qual no sera utilizado para executar os comandos (digite 'endereco:porta'): ");
                enderecoPortaNoEscolhido = scanner.nextLine();
                noEscolhidoOrigem = rede.buscarNo(enderecoPortaNoEscolhido);
            }

            switch(comandoEscolhido){
                case 0:
                    noEscolhidoOrigem.listarVizinhos();
                    break;
                case 1:
                    noEscolhidoOrigem.enviarHello(rede);
                    break;
                case 2:
//                    ServicoComandos.enviarSearchFlooding(noEscolhidoOrigem);
//                    break;
                case 3:
//                    ServicoComandos.enviarSearchRandomWalk(noEscolhidoOrigem);
//                    break;
                case 4:
//                    ServicoComandos.enviarSearchBuscaEmProfundidade(noEscolhidoOrigem);
//                    break;
                case 5:
//                    ServicoComandos.exibirEstatisticas(noEscolhidoOrigem);
//                    break;
                case 6:
                    ServicoComandos.alterarValorPadraoTTL(rede);
                    break;
                case 9:
                    System.out.println("Programa encerrado");
                    sairPrograma = true;
                    break;
                default:
                    System.out.println("Comando inválido.");
            }
        }
    }
}
