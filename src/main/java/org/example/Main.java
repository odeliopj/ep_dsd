package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Rede rede = new Rede();
        int quantidadeNos = 0;
        int comandoEscolhido = -1;
        boolean sairPrograma = false;
        No noEscolhido = null;
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
            //scanner.nextLine();

            scanner = new Scanner(System.in);
            System.out.print("Qual no sera utilizado para os comandos (digite 'endereco:porta'): ");
            enderecoPortaNoEscolhido = scanner.nextLine();
            noEscolhido = rede.buscarNo(enderecoPortaNoEscolhido);

            switch(comandoEscolhido){
                case 0:
                    ServicoComandos.listarVizinhos(noEscolhido);
                    break;
                case 1:
                    ServicoComandos.enviarHello(noEscolhido);
                    break;
                case 2:
//                    ServicoComandos.enviarSearchFlooding(noEscolhido);
//                    break;
                case 3:
//                    ServicoComandos.enviarSearchRandomWalk(noEscolhido);
//                    break;
                case 4:
//                    ServicoComandos.enviarSearchBuscaEmProfundidade(noEscolhido);
//                    break;
                case 5:
//                    ServicoComandos.exibirEstatisticas(noEscolhido);
//                    break;
                case 6:
                    ServicoComandos.alterarValorPadraoTTL(rede);
                    break;
                case 9:
                    sairPrograma = true;
                    break;
                default:
                    System.out.println("Comando inválido.");
            }
        }
    }
}
