package org.example;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Setter
@Getter
public class Rede {
    private Map<String, No> nosDaRede;
    private int ttlPadrao;

    public Rede(){
        this.nosDaRede = new HashMap<>();
        this.ttlPadrao = 100;
    }

    public No buscarNo(String enderecoPorta){
        return nosDaRede.get(enderecoPorta);
    }

    public void criarNo(Rede rede){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o: 'endereco:porta + {nome do arquivo de vizinhos} + {nome do arquivo de chaves-valor}': ");
        String dadosNo = scanner.nextLine();

        String[] partesDadosNo = dadosNo.split(" ");

        if (partesDadosNo.length < 1 || partesDadosNo.length > 3) {
            System.err.println("Entrada inválida. Certifique-se de que a entrada contém entre uma e três partes separadas por espaço.");
            return;
        }

        String enderecoPorta = partesDadosNo[0];
        String caminhoVizinhos = partesDadosNo.length > 1 ? partesDadosNo[1] : null;
        String caminhoChaveValor = partesDadosNo.length > 2 ? partesDadosNo[2] : null;

        String[] partesEnderecoPorta = enderecoPorta.split(":");
        String endereco = partesEnderecoPorta[0];
        int porta = Integer.parseInt(partesEnderecoPorta[1]);

        No no = verificarNoExiste(endereco, porta, rede);

        if (caminhoVizinhos != null) {
            lerArquivoVizinhos(no, caminhoVizinhos, rede);
        }

        if (caminhoChaveValor != null) {
            lerArquivoChaveValor(no, caminhoVizinhos);
        }

        System.out.println("Nó " + endereco + ":" + porta + " inicializado com sucesso.");
        System.out.println(no);
    }

    public No verificarNoExiste(String endereco, int porta, Rede rede){
        String chave = endereco + ":" + porta;

        if (!nosDaRede.containsKey(chave)){
            No novoNo = new No(endereco, porta, rede);
            nosDaRede.put(chave, novoNo);
        }

        return nosDaRede.get(chave);
    }

    private void lerArquivoVizinhos(No no, String caminhoVizinhos, Rede rede) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoVizinhos))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] vizinhoEnderecoPorta = linha.split(":");
                String vizinhoEndereco = vizinhoEnderecoPorta[0];
                int vizinhoPorta = Integer.parseInt(vizinhoEnderecoPorta[1]);

                No noVizinho = verificarNoExiste(vizinhoEndereco, vizinhoPorta, rede);
                no.adicionarVizinho(noVizinho);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de vizinhos: " + e.getMessage());
        }
    }

    private void lerArquivoChaveValor(No no, String caminhoChaveValor) {
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
}
