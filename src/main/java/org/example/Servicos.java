package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public final class Servicos {

    public static void alterarValorPadraoTTL(Rede rede) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite novo valor de TTL: ");
        int novoValorTTL = scanner.nextInt();

        rede.setTtlPadrao(novoValorTTL);
        System.out.println("Valor de TTL alterado para " + novoValorTTL);
    }

    public static void criarNo(String dadosNo, Rede rede){
        String[] partesDadosNo = dadosNo.split(" ");
        String enderecoPorta = partesDadosNo[0];
        String caminhoVizinhos = partesDadosNo.length > 1 ? partesDadosNo[1] : null;
        String caminhoChaveValor = partesDadosNo.length > 2 ? partesDadosNo[2] : null;

        String[] partesEnderecoPorta = enderecoPorta.split(":");
        String enderecoCriarNo = partesEnderecoPorta[0];
        int portaCriarNo = Integer.parseInt(partesEnderecoPorta[1]);

        No novoNo = new No(enderecoCriarNo, portaCriarNo, rede);
        rede.getNosDaRede().put(enderecoPorta, novoNo);

        if (caminhoVizinhos != null)
            lerArquivoVizinhos(novoNo, caminhoVizinhos);

        if (caminhoChaveValor != null)
            lerArquivoChaveValor(novoNo, caminhoVizinhos);

        System.out.println("Nó " + enderecoCriarNo + ":" + portaCriarNo + " criado com sucesso.");
    }

    private static void lerArquivoVizinhos(No no, String caminhoVizinhos) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoVizinhos))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                no.getVizinhos().add(linha);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de vizinhos: " + e.getMessage());
        }
    }

    private static void lerArquivoChaveValor(No no, String caminhoChaveValor) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoChaveValor))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] chaveValor = linha.split(" ");
                String chave = chaveValor[0];
                String valor = chaveValor[1];
                no.adicionarChaveValor(chave, valor);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de pares chave-valor: " + e.getMessage());
        }
    }

    public static void menuComandos(Rede rede) {
        boolean executarMenu = true;
        int comandoEscolhido = -1;
        String enderecoPortaNoEscolhido = null;
        No noEscolhidoOrigem = null;

        while (executarMenu) {
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

            while (comandoEscolhido < 0 || comandoEscolhido > 9) {
                Scanner scannerComando = new Scanner(System.in);
                System.out.print("Digite o comando: ");
                comandoEscolhido = scannerComando.nextInt();
            }

            if (comandoEscolhido != 9) {
                Scanner scannerNo = new Scanner(System.in);
                System.out.print("Qual no sera utilizado para executar os comandos (digite 'endereco:porta'): ");
                enderecoPortaNoEscolhido = scannerNo.nextLine();
                noEscolhidoOrigem = rede.getNosDaRede().get(enderecoPortaNoEscolhido);
            }

            switch (comandoEscolhido) {
                case 0:
                    noEscolhidoOrigem.listarVizinhos();
                    break;
                case 1:
                    noEscolhidoOrigem.enviarHello();
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
                    Servicos.alterarValorPadraoTTL(rede);
                    break;
                case 9:
                    System.out.println("Programa encerrado");
                    executarMenu = false;
                    break;
                default:
                    System.out.println("Comando inválido.");
            }

            noEscolhidoOrigem = null;
            comandoEscolhido = -1;
        }
    }
}