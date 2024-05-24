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
            System.err.println("Erro ao criar o socket de escuta: " + e.getMessage());
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
                        receberMensagens(socket, mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }).start();
    }

    /* Métodos de tratamento de mensagens recebidas */
    private void receberMensagens(Socket socket, String mensagem) {
        List<String> partesMensagem = Arrays.asList(mensagem.split(" "));

        if (partesMensagem.contains("HELLO"))
            tratarHello(socket, mensagem);
    }

    public void tratarHello(Socket socket, String mensagem) {
        System.out.println("Mensagem recebida: " + mensagem);
        String[] partesMensagem = mensagem.split(" ");
        String enderecoPortaOrigem = partesMensagem[0];

        if (!vizinhos.contains(enderecoPortaOrigem)) {
            vizinhos.add(enderecoPortaOrigem);
            System.out.println("Adicionando vizinho na tabela: " + enderecoPortaOrigem);
        }
        System.out.println("  Vizinho ja esta na tabela: " + enderecoPortaOrigem);

        // Enviar resposta HELLO_OK na mesma conexão
        String operacao = "HELLO_OK";
        String mensagemResposta = String.format("%s %s", endereco + ":" + porta, operacao);
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(mensagemResposta);
            System.out.println("Envio de HELLO_OK para " + enderecoPortaOrigem + " feito com sucesso.");
        } catch (IOException e) {
            System.err.println("Erro ao enviar resposta HELLO_OK: " + e.getMessage());
        }
    }

    /* Métodos envio de mensagens */
    public void enviarHello() {
        System.out.println("Escolha o vizinho:");
        listarVizinhos();

        Scanner scanner = new Scanner(System.in);
        int indexNoDestino = scanner.nextInt();
        String enderecoPortaDestino = vizinhos.get(indexNoDestino);
        No noDestino = rede.getNosDaRede().get(enderecoPortaDestino);

        String origem = endereco + ":" + porta;
        int ttl = 1;
        String operacao = "HELLO";
        String mensagem = String.format("%s %d %d %s", origem, numeroSequenciaMsg, ttl, operacao);
        String enderecoDestino = noDestino.getEndereco();
        String portaDestino = String.valueOf(noDestino.getPorta());

        System.out.println("Encaminhando mensagem " + "'" + mensagem + "'" + " para " + enderecoDestino + ":" + portaDestino);

        try (Socket socket = criarSocket(noDestino.getEndereco(), noDestino.getPorta())) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem);
            numeroSequenciaMsg++;
            System.out.println("Envio feito com sucesso: " + mensagem);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem HELLO: " + mensagem);
        }
    }
}
