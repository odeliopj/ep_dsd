package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Servicos {
    public static boolean executarMenu = true;
    public static boolean escutarConexoes = true;;

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
            lerArquivoChaveValor(novoNo, caminhoChaveValor);

        System.out.println("Nó " + enderecoCriarNo + ":" + portaCriarNo + " criado com sucesso.");
    }

    private static void lerArquivoVizinhos(No no, String caminhoVizinhos) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoVizinhos))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                no.getVizinhosList().add(linha);
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
                if(chaveValor.length == 2) {
                    String chave = chaveValor[0];
                    String valor = chaveValor[1];
                    no.adicionarChaveValor(chave, valor);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de pares chave-valor: " + e.getMessage());
        }
    }

    public static void criarNosRede(Rede rede) {
        int quantidadeNos = 0;
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
    }

    public static void menuComandos(Rede rede) {
        int comandoEscolhido = -1;
        String enderecoPortaNoEscolhido = null;
        No noEscolhidoOrigem = null;

        while (executarMenu) {
            executarMenu = false;

            System.out.println("""
                \nEscolha o comando:

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
                    noEscolhidoOrigem.iniciarSearchFlooding();
                    break;
                case 3:
                    noEscolhidoOrigem.iniciarSearchRandomWalk();
                    break;
                case 4:
                    noEscolhidoOrigem.iniciarSearchBuscaProfundidade();
                    break;
                case 5:
                    exibirEstatisticas(rede);
                    break;
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

    public static void arquivosDeTeste(Rede rede) {
        criarNo("127.0.0.1:5001 topologias/topologia_ciclo_3/1.txt topologias/topologia_ciclo_3/1-values.txt", rede);
        criarNo("127.0.0.1:5002 topologias/topologia_ciclo_3/2.txt topologias/topologia_ciclo_3/2-values.txt", rede);
        criarNo("127.0.0.1:5003 topologias/topologia_ciclo_3/3.txt topologias/topologia_ciclo_3/3-values.txt", rede);
    }

    public static void exibirEstatisticas(Rede rede) {
        int totalMsgsVistasFlooding = 0;
        int totalHopsFlooding = 0;
        List<Integer> mediasPorNoFlooding = new ArrayList<>();
        int mediaSaltosFlooding = 0;

        int totalMsgsVistasRandomWalk = 0;
        int totalHopsRandomWalk = 0;
        List<Integer> mediasPorNoRandomWalk = new ArrayList<>();
        int mediaSaltosRandomWalk = 0;

        int totalMsgsVistasBP = 0;
        int totalHopsBP = 0;
        List<Integer> mediasPorNoBP = new ArrayList<>();
        int mediaSaltosBP = 0;

        for (No no : rede.getNosDaRede().values()) {
            totalMsgsVistasFlooding += no.getNumMsgsVistasFlooding();
            totalHopsFlooding = no.getNumHopsValFloodingList().stream().mapToInt(Integer::intValue).sum();
            if (!no.getNumHopsValFloodingList().isEmpty()) {
                mediasPorNoFlooding.add(totalHopsFlooding / no.getNumHopsValFloodingList().size());
            }

            totalMsgsVistasRandomWalk += no.getNumMsgsVistasRandomWalk();
            totalHopsRandomWalk = no.getNumHopsValRandomWalkList().stream().mapToInt(Integer::intValue).sum();
            if (!no.getNumHopsValRandomWalkList().isEmpty()) {
                mediasPorNoRandomWalk.add(totalHopsRandomWalk / no.getNumHopsValRandomWalkList().size());
            }

            totalMsgsVistasBP += no.getNumMsgsVistasBP();
            totalHopsBP = no.getNumHopsValBPList().stream().mapToInt(Integer::intValue).sum();
            if (!no.getNumHopsValBPList().isEmpty()) {
                mediasPorNoBP.add(totalHopsBP / no.getNumHopsValBPList().size());
            }
        }

        if(!mediasPorNoFlooding.isEmpty())
            mediaSaltosFlooding = mediasPorNoFlooding.stream().mapToInt(Integer::intValue).sum() / mediasPorNoFlooding.size();

        if(!mediasPorNoRandomWalk.isEmpty())
            mediaSaltosRandomWalk = mediasPorNoRandomWalk.stream().mapToInt(Integer::intValue).sum() / mediasPorNoRandomWalk.size();

        if(!mediasPorNoBP.isEmpty())
            mediaSaltosBP = mediasPorNoBP.stream().mapToInt(Integer::intValue).sum() / mediasPorNoBP.size();

        System.out.println("Estatisticas");

        System.out.println("  Media de saltos ate encontrar destino por flooding: " + mediaSaltosFlooding);
        System.out.println("  Total de mensagens de flooding vistas: " + totalMsgsVistasFlooding);

        System.out.println("\n  Media de saltos ate encontrar destino por random walk: " + mediaSaltosRandomWalk);
        System.out.println("  Total de mensagens de random walk vistas: " + totalMsgsVistasRandomWalk);

        System.out.println("\n  Media de saltos ate encontrar destino por BP: " + mediaSaltosBP);
        System.out.println("  Total de mensagens de BP vistas: " + totalMsgsVistasBP);
    }
}
