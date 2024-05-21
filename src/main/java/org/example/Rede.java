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

    public Rede(){
        this.nosDaRede = new HashMap<>();
    }

    public No criarOuObterNo(String endereco, int porta){
        String chave = endereco + ":" + porta;

        if (!nosDaRede.containsKey(chave)){
            No novoNo = new No(endereco, porta);
            nosDaRede.put(chave, novoNo);
        }

        return nosDaRede.get(chave);
    }

    public void inicializarNo(String enderecoPorta, String caminhoVizinhos, String caminhoChaveValor){
        String[] partes = enderecoPorta.split(":");
        String endereco = partes[0];
        int porta = Integer.parseInt(partes[1]);

        No no = criarOuObterNo(endereco, porta);

        if (caminhoVizinhos != null) {
            lerArquivoVizinhos(no, caminhoVizinhos);
        }

        if (caminhoChaveValor != null) {
            lerArquivoChaveValor(no, caminhoVizinhos);
        }

        System.out.println("Nó " + endereco + ":" + porta + " inicializado com sucesso.");
        System.out.println(no);
    }

    private void lerArquivoVizinhos(No no, String caminhoVizinhos) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoVizinhos))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] vizinhoEnderecoPorta = linha.split(":");
                String vizinhoEndereco = vizinhoEnderecoPorta[0];
                int vizinhoPorta = Integer.parseInt(vizinhoEnderecoPorta[1]);

                No noVizinho = criarOuObterNo(vizinhoEndereco, vizinhoPorta);
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

    public void obterNosDoUsuario(Rede rede){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o: 'endereco:porta + {nome do arquivo de vizinhos} + {nome do arquivo de chaves-valor}': ");
        String dadosNo = scanner.nextLine();

        String[] partes = dadosNo.split(" ");

        if (partes.length < 1 || partes.length > 3) {
            System.err.println("Entrada inválida. Certifique-se de que a entrada contém entre uma e três partes separadas por espaço.");
            return;
        }

        String enderecoPorta = partes[0];
        String caminhoVizinhos = partes.length > 1 ? partes[1] : null;
        String caminhoChaveValor = partes.length > 2 ? partes[2] : null;

        rede.inicializarNo(enderecoPorta, caminhoVizinhos, caminhoChaveValor);
    }
}
