package org.example;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.io.*;
import java.net.*;

@Getter
@Setter
public class No {
    private String endereco;
    private int porta;
    private List<No> vizinhos;
    private Map<String, String> chaveValor;
    private ServerSocket serverSocket;
    private int numeroSequenciaMsg;

    public No(String endereco, int porta) {
        this.endereco = endereco;
        this.porta = porta;
        this.vizinhos = new ArrayList<>();
        this.chaveValor = new HashMap<>();
        this.numeroSequenciaMsg = 0;
        try {
            this.serverSocket = new ServerSocket(porta, 50, InetAddress.getByName(endereco));
        } catch (IOException e) {
            System.err.println("Erro ao criar o socket: " + e.getMessage());
        }
    }

    public void criarSocket() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(endereco, porta));
        System.out.println("Nó " + endereco + ":" + porta + " iniciado em " + endereco + ":" + porta);
        escutarConexoes();
    }

    public void adicionarChaveValor(String chave, String valor) {
        this.chaveValor.put(chave, valor);
    }

    public String buscarChave(String chave) {
        return chaveValor.get(chave);
    }

    public void adicionarVizinho(No vizinho) {
        System.out.println("Tentando adicionar vizinho: " + vizinho.getEndereco() + ":" + vizinho.getPorta());

        try (Socket socket = new Socket(vizinho.getEndereco(), vizinho.getPorta())) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("HELLO");

            this.vizinhos.add(vizinho);

            System.out.println("Vizinho adicionado com sucesso: "  + vizinho.getEndereco() + ":" + vizinho.getPorta());
        } catch (IOException e) {
            System.err.println("Erro ao adicionar vizinho: " + e.getMessage());
        }
    }

    private void escutarConexoes() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String mensagem = in.readLine();

                    if (mensagem != null) {
                        tratarMensagem(socket, mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }).start();
    }

    private void tratarMensagem(Socket socket, String mensagem) {
        String[] partes = mensagem.split(" ");
        String tipoMensagem = partes[0];

        if ("HELLO".equals(tipoMensagem)) {
            String origin = partes[1];
            System.out.println("Mensagem HELLO recebida de " + origin);
            receberHello(origin);
        }
    }

    public void receberHello(String origin) {
        String[] parts = origin.split(":");
        String endereco = parts[0];
        int porta = Integer.parseInt(parts[1]);
        No transmissor = new No(endereco, porta);
        adicionarVizinho(transmissor);
    }
}
