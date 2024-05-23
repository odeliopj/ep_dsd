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
    private List<String> vizinhos;
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

    public void adicionarVizinho(String enderecoPortaVizinho) {
        System.out.println("Tentando adicionar vizinho: " + enderecoPortaVizinho);

        if (!vizinhos.contains(enderecoPortaVizinho))
            vizinhos.add(enderecoPortaVizinho);
    }

    public void listarVizinhos(){
        long numeroDeVizinhos = vizinhos.size();
        System.out.println("Ha " + numeroDeVizinhos + " vizinhos na tabela:");

        if (numeroDeVizinhos > 0) {
            for (int i = 0; i < numeroDeVizinhos; i++) {
                System.out.println("[" + i + "] " + vizinhos.get(i));
            }
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
    public void enviarHello() {
        System.out.println("Escolha o vizinho:");
        listarVizinhos();
        Scanner scanner = new Scanner(System.in);
        int indexNoDestino = scanner.nextInt();
        String enderecoPortaDestino = vizinhos.get(indexNoDestino);
        No noDestino = rede.getNosDaRede().get(enderecoPortaDestino);
        // TO-DO: falta lista de argumentos

        String origem = endereco + ":" + porta;
        int ttl = rede.getTtlPadrao();
        String operacao = "HELLO";
        String mensagem = String.format("%s %d %d %s", origem, numeroSequenciaMsg, ttl, operacao);

        System.out.println("Encaminhando mensagem " + "'" + mensagem + "' " + "para " + noDestino.getEndereco() + ":" + noDestino.getPorta());

        try (Socket socket = criarSocket(noDestino.getEndereco(), noDestino.getPorta())) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem HELLO: " + mensagem);
        }
    }

    /* Métodos de tratamento de mensagens recebidas */
    private void tratarMensagem(String mensagem) {
        List<String> partesMensagem = Arrays.asList(mensagem.split(" "));

        if (partesMensagem.contains("HELLO")) {
            receberHello(partesMensagem.get(0));
        }

        if(partesMensagem.contains("HELLO_OK")) {
            System.out.println("Envio feito com sucesso: " + "Hello");
            numeroSequenciaMsg++;
        }
    }

    public void receberHello(String enderecoPortaOrigem) {
                if(!vizinhos.contains(enderecoPortaOrigem)) {
                    vizinhos.add(enderecoPortaOrigem);
                    System.out.println("Adicionando vizinho na tabela: " + enderecoPortaOrigem);
                }
    }
}
