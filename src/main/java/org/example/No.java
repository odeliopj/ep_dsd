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
    private ServerSocket serverSocketEscuta;
    private int numeroSequenciaMsg;
    private Rede rede;

    public No(String endereco, int porta, Rede rede) {
        this.endereco = endereco;
        this.porta = porta;
        this.vizinhos = new ArrayList<>();
        this.chaveValor = new HashMap<>();
        this.numeroSequenciaMsg = 1;
        this.rede = rede;
        try {
            this.serverSocketEscuta = new ServerSocket(porta, 50, InetAddress.getByName(endereco));
            escutarConexoes();
        } catch (IOException e) {
            System.err.println("Erro ao criar o socket: " + e.getMessage());
        }
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

    public void listarVizinhos(){
        long numeroDeVizinhos = 0;
        numeroDeVizinhos = vizinhos.size();
        System.out.println("Ha " + numeroDeVizinhos + " vizinhos na tabela:");

        for (int i = 0; i < numeroDeVizinhos; i++) {
            No vizinho = vizinhos.get(i);
            System.out.println("[" + i + "] " + vizinho.getEndereco() + ":" + vizinho.getPorta());
        }
    }

    /* Métodos criação sockets */
    public Socket criarSocket(String enderecoDestino, int portaDestino) throws IOException {
        return new Socket(enderecoDestino, portaDestino);
    }

    private void escutarConexoes() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocketEscuta.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String mensagem = in.readLine();

                    if (mensagem != null) {
                        tratarMensagem(mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }).start();
    }

    /* Métodos envio de mensagens */
    public void enviarHello(Rede rede) {
        System.out.println("Escolha o vizinho:");
        listarVizinhos();
        Scanner scanner = new Scanner(System.in);
        int indexNoDestino = scanner.nextInt();
        No noDestino = vizinhos.get(indexNoDestino);

        String origem = endereco + ":" + porta;
        int ttl = rede.getTtlPadrao();
        String operacao = "HELLO";
        String mensagem = String.format("%s %d %d %s", origem, numeroSequenciaMsg, ttl, operacao);

        System.out.println("Encaminhando mensagem " + "'" + mensagem + "' " + "para " + noDestino.getPorta());
        try (Socket socket = criarSocket(noDestino.getEndereco(), noDestino.getPorta())) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem);

            System.out.println("Envio feito com sucesso: " + mensagem);
            numeroSequenciaMsg++;
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem HELLO: " + mensagem);
        }
    }

    /* Métodos de tratamento de mensagens recebidas */
    private void tratarMensagem(String mensagem) {
        List<String> partesMensagem = Arrays.asList(mensagem.split(" "));

        if (partesMensagem.contains("HELLO")) {
            receberHello(partesMensagem);
        }
    }

    public void receberHello(List<String> partesMensagem) {
        if(partesMensagem.size() >= 2) {
            String noOrigem = partesMensagem.get(0);
            String[] enderecoPorta = noOrigem.split(":");
            String endereco = enderecoPorta[0];
            int porta = Integer.parseInt(enderecoPorta[1]);

            No transmissor = new No(endereco, porta, rede);
            adicionarVizinho(transmissor);
        }
    }

}
