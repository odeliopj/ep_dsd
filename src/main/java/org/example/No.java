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
    private List<String> vizinhosList;
    private List<String> msgsVistasList;
    private Map<String, String> chaveValorMap;
    private ServerSocket serverSocketEscuta;
    private int numeroSequenciaMsg;
    private Rede rede;

    public No(String endereco, int porta, Rede rede) {
        this.endereco = endereco;
        this.porta = porta;
        this.vizinhosList = new ArrayList<>();
        this.msgsVistasList = new ArrayList<>();
        this.chaveValorMap = new HashMap<>();
        this.numeroSequenciaMsg = 0;
        this.rede = rede;
        try {
            this.serverSocketEscuta = new ServerSocket(porta, 50, InetAddress.getByName(endereco));
            escutarConexoes();
        } catch (IOException e) {
            System.err.println("Erro ao criar o socket de escuta: " + e.getMessage());
        }
    }

    public void adicionarChaveValor(String chave, String valor) {
        this.chaveValorMap.put(chave, valor);
    }

    public void adicionarVizinho(String enderecoPortaVizinho) {
        System.out.println("Tentando adicionar vizinho: " + enderecoPortaVizinho);

        if (!vizinhosList.contains(enderecoPortaVizinho))
            vizinhosList.add(enderecoPortaVizinho);
    }

    public void listarVizinhos(){
        long numeroDeVizinhos = vizinhosList.size();
        System.out.println("Ha " + numeroDeVizinhos + " vizinhos na tabela:");

        if (numeroDeVizinhos > 0) {
            for (int i = 0; i < numeroDeVizinhos; i++) {
                System.out.println("[" + i + "] " + vizinhosList.get(i));
            }
        }
    }

    /** Métodos criação sockets **/
    public Socket criarSocket(String enderecoDestino, int portaDestino) throws IOException {
        return new Socket(enderecoDestino, portaDestino);
    }

    private void escutarConexoes() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socketRecebimento = serverSocketEscuta.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socketRecebimento.getInputStream()));
                    String mensagem = in.readLine();

                    if (mensagem != null) {
                        receberMensagens(socketRecebimento, mensagem);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }).start();
    }

    private void receberMensagens(Socket socketRecebimento, String mensagem) {
        List<String> partesMensagem = Arrays.asList(mensagem.split(" "));

        if (partesMensagem.contains("HELLO"))
            processarMsgHello(socketRecebimento, mensagem);
        if (partesMensagem.contains("HELLO_OK"))
            System.out.println("Mensagem de resposta HELLO_OK recebida com sucesso!");

        if (partesMensagem.contains("SEARCH_FL"))
            processarMsgFlooding(socketRecebimento, mensagem);
        if (partesMensagem.contains("VAL_FL"))
            System.out.println("Valor encontrado! Chave: " + partesMensagem.get(4) + " >" + " Valor: " + partesMensagem.get(5));

        if (partesMensagem.contains("SEARCH_RW"))
            processarMsgRandomWalk(socketRecebimento, mensagem);
        if (partesMensagem.contains("VAL_RW"))
            System.out.println("Valor encontrado! Chave: " + partesMensagem.get(4) + " >" + " Valor: " + partesMensagem.get(5));
    }


    /** Métodos HELLO **/
    public void enviarHello() {
        System.out.println("Escolha o vizinho:");
        listarVizinhos();

        Scanner scanner = new Scanner(System.in);
        int indexNoDestino = scanner.nextInt();
        String enderecoPortaDestino = vizinhosList.get(indexNoDestino);
        No noDestino = rede.getNosDaRede().get(enderecoPortaDestino);

        String origem = endereco + ":" + porta;
        int ttl = 1;
        numeroSequenciaMsg++;
        String operacao = "HELLO";
        String mensagem = String.format("%s %d %d %s", origem, numeroSequenciaMsg, ttl, operacao);
        String enderecoDestino = noDestino.getEndereco();
        String portaDestino = String.valueOf(noDestino.getPorta());

        System.out.println("Encaminhando mensagem " + "'" + mensagem + "'" + " para " + enderecoDestino + ":" + portaDestino);

        try (Socket socket = criarSocket(noDestino.getEndereco(), noDestino.getPorta())) {
            System.out.println("Envio feito com sucesso: " + mensagem);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem);
            out.flush();

            // Wait for the response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
                receberMensagens(socket, resposta);
            }
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem HELLO: " + mensagem);
        }
    }

    public void processarMsgHello(Socket socketRecebimento, String mensagem) {
        System.out.println("Mensagem recebida: " + mensagem);
        String[] partesMensagem = mensagem.split(" ");
        String enderecoPortaOrigem = partesMensagem[0];

        if (!vizinhosList.contains(enderecoPortaOrigem)) {
            vizinhosList.add(enderecoPortaOrigem);
            System.out.println("Adicionando vizinho na tabela: " + enderecoPortaOrigem);
        }
        System.out.println("  Vizinho ja esta na tabela: " + enderecoPortaOrigem);

        // Enviar resposta HELLO_OK na mesma conexão
        String operacao = "HELLO_OK";
        String mensagemResposta = String.format("%s %s", endereco + ":" + porta, operacao);
        try (PrintWriter out = new PrintWriter(socketRecebimento.getOutputStream(), true)) {
            System.out.println("\nEnvio de HELLO_OK para " + enderecoPortaOrigem + " feito com sucesso.");
            out.println(mensagemResposta);
        } catch (IOException e) {
            System.err.println("Erro ao enviar resposta HELLO_OK: " + e.getMessage());
        }
    }

    /** Métodos FLOODING **/
    public void iniciarSearchFlooding() {
        System.out.print("Digite a chave a ser buscada: ");
        Scanner scannerFlooding = new Scanner(System.in);
        String chaveBuscada = scannerFlooding.nextLine();

        if (chaveValorMap.containsKey(chaveBuscada)){
            System.out.println("Valor na tabela local!");
            System.out.println("      Chave: " + chaveBuscada + " Valor: " + chaveValorMap.get(chaveBuscada));
        } else {
            String enderecoPortaOrigem = endereco + ":" + porta;
            numeroSequenciaMsg++;
            String seqNum = String.valueOf(numeroSequenciaMsg);
            String ttl = String.valueOf(rede.getTtlPadrao());
            String searchMode = "SEARCH_FL";
            String lastHopPort = String.valueOf(porta);
            String hopCount =  String.valueOf(0);
            String msgFlooding = String.format("%s %s %s %s %s %s %s", enderecoPortaOrigem, seqNum, ttl, searchMode, lastHopPort, chaveBuscada, hopCount);

            msgsVistasList.add(endereco + ":" + numeroSequenciaMsg);

            System.out.println("Busca flooding iniciada");
            enviarMsgFloodingVizinhos(msgFlooding, vizinhosList);
        }
    }

    public void enviarMsgFloodingVizinhos(String msgFlooding, List<String> nosDeDestino) {
        // incrementar HOP_COUNT
        List<String> partesMsgFlooding = Arrays.asList(msgFlooding.split(" "));
        int hopCount = Integer.parseInt(partesMsgFlooding.get(6));
        hopCount++;
        partesMsgFlooding.set(6, String.valueOf(hopCount));
        String msgFloodingAjustada = String.join(" ", partesMsgFlooding);

        // enviar msgs
        nosDeDestino.forEach(vizinho -> {
            String enderecoDestino = vizinho.split(":")[0];
            int portaDestino =  Integer.parseInt(vizinho.split(":")[1]);

            try (Socket socket = criarSocket(enderecoDestino, portaDestino)) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(msgFloodingAjustada);
                out.flush();

                // aguardar resposta
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String resposta = in.readLine();
                if (resposta != null) {
                    receberMensagens(socket, resposta);
                }
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem Busca Flooding: " + msgFlooding);
            }
        });
    }

    public void processarMsgFlooding(Socket socket, String mensagem){
        List<String> partesMsgFlooding = Arrays.asList(mensagem.split(" "));
        String enderecoPortaOrigem = partesMsgFlooding.get(0);
        String enderecoOrigem = enderecoPortaOrigem.split(":")[0];
        String numSeqMsg = partesMsgFlooding.get(1);
        String chaveBuscada = partesMsgFlooding.get(5);

        // decrementar TTL
        int ttl = Integer.parseInt(partesMsgFlooding.get(2));
        ttl--;
        partesMsgFlooding.set(2, String.valueOf(ttl));

        if (msgsVistasList.contains(enderecoOrigem + ":" + numSeqMsg)) {
            System.out.println("Flooding: Mensagem repetida");
            return;
        }

        msgsVistasList.add(enderecoOrigem + ":" + numSeqMsg);

        // verificar tabela local de chave-valor
        if (chaveValorMap.containsKey(chaveBuscada)) {
            partesMsgFlooding.set(0, endereco + ":" + porta);
            numeroSequenciaMsg++;
            partesMsgFlooding.set(1, String.valueOf(numeroSequenciaMsg));
            partesMsgFlooding.set(2, String.valueOf(ttl));
            partesMsgFlooding.set(3, "VAL_FL");
            partesMsgFlooding.set(4, chaveBuscada);
            partesMsgFlooding.set(5, chaveValorMap.get(chaveBuscada));
            String msgValorEncontrado = String.join(" ", partesMsgFlooding);

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                System.out.println("Envio de 'chave-valor' para " + enderecoPortaOrigem + " feito com sucesso.");
                out.println(msgValorEncontrado);
            } catch (IOException e) {
                System.err.println("Erro ao enviar resposta FLOODING: " + e.getMessage());
            }
            return;
        }

        // descartar mensagem se TTL = 0
        if (ttl == 0) {
            System.out.println("TTL igual a zero, descartando mensagem");
            return;
        }

        // re-enviar msg para os nós vizinhos (com exceção do nó de origem da msg)
        String msgFloodingAjustada = String.join(" ", partesMsgFlooding);
        List<String> nosDestino = new ArrayList<>();
        vizinhosList.forEach(vizinho -> {
            if (!vizinho.equals(enderecoPortaOrigem)) // retira o nó de origem da msg da lista de envio da msg
                nosDestino.add(vizinho);
        });

        enviarMsgFloodingVizinhos(msgFloodingAjustada, nosDestino);
    }

    /** Métodos RANDOM WALK **/
    public void iniciarSearchRandomWalk(){
        System.out.print("Digite a chave a ser buscada: ");
        Scanner scannerFlooding = new Scanner(System.in);
        String chaveBuscada = scannerFlooding.nextLine();

        // verificar tabela local de chave-valor
        if (chaveValorMap.containsKey(chaveBuscada)){
            System.out.println("Valor na tabela local!");
            System.out.println("      Chave: " + chaveBuscada + " >" + " Valor: " + chaveValorMap.get(chaveBuscada));
        } else {
            String enderecoPortaOrigem = endereco + ":" + porta;
            numeroSequenciaMsg++;
            String seqNum = String.valueOf(numeroSequenciaMsg);
            String ttl = String.valueOf(rede.getTtlPadrao());
            String searchMode = "SEARCH_FL";
            String lastHopPort = String.valueOf(porta);
            String hopCount =  String.valueOf(0);
            String msgRandomWalk = String.format("%s %s %s %s %s %s %s", enderecoPortaOrigem, seqNum, ttl, searchMode, lastHopPort, chaveBuscada, hopCount);

            // seleciona um nó aleatório da lista de vizinhos
            String noEscolhidoEnderecoPorta = vizinhosList.get(new Random().nextInt(vizinhosList.size()));

            System.out.println("Busca random walk iniciada");
            enviarMsgRandomWalk(msgRandomWalk, noEscolhidoEnderecoPorta);
        }
    }

    public void enviarMsgRandomWalk(String msgRandomWalk, String noEscolhidoEnderecoPorta){
        // incrementar HOP_COUNT
        List<String> partesMsgFlooding = Arrays.asList(msgRandomWalk.split(" "));
        int hopCount = Integer.parseInt(partesMsgFlooding.get(6));
        hopCount++;
        partesMsgFlooding.set(6, String.valueOf(hopCount));
        String msgRandomWalkAjustada = String.join(" ", partesMsgFlooding);

        // enviar msg
        String enderecoDestino = noEscolhidoEnderecoPorta.split(":")[0];
        int portaDestino =  Integer.parseInt(noEscolhidoEnderecoPorta.split(":")[1]);
        try (Socket socket = criarSocket(enderecoDestino, portaDestino)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msgRandomWalkAjustada);
            out.flush();

            // aguardar resposta
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resposta = in.readLine();
            if (resposta != null) {
                receberMensagens(socket, resposta);
            }
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem Busca Flooding: " + msgRandomWalkAjustada);
        }
    }

    public void processarMsgRandomWalk(Socket socket, String mensagem){
        List<String> partesMsgRandomWalk = Arrays.asList(mensagem.split(" "));
        String chaveBuscada = partesMsgRandomWalk.get(5);

        // decrementar TTL
        int ttl = Integer.parseInt(partesMsgRandomWalk.get(2));
        ttl--;
        partesMsgRandomWalk.set(2, String.valueOf(ttl));

        // verificar tabela local de chave-valor
        if (chaveValorMap.containsKey(chaveBuscada)) {
            partesMsgRandomWalk.set(0, endereco + ":" + porta);
            numeroSequenciaMsg++;
            partesMsgRandomWalk.set(1, String.valueOf(numeroSequenciaMsg));
            partesMsgRandomWalk.set(2, String.valueOf(ttl));
            partesMsgRandomWalk.set(3, "VAL_RW");
            partesMsgRandomWalk.set(4, chaveBuscada);
            partesMsgRandomWalk.set(5, chaveValorMap.get(chaveBuscada));
            String msgValorEncontrado = String.join(" ", partesMsgRandomWalk);

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                System.out.println("Envio de 'chave-valor' para " + partesMsgRandomWalk + " feito com sucesso.");
                out.println(msgValorEncontrado);
            } catch (IOException e) {
                System.err.println("Erro ao enviar resposta FLOODING: " + e.getMessage());
            }
            return;
        }

        // descartar mensagem se TTL = 0
        if (ttl == 0) {
            System.out.println("TTL igual a zero, descartando mensagem");
            return;
        }

        // seleciona um nó aleatório - salto anterior com baixa prioridade
        List<String> nosDestinoAjustado = new ArrayList<>();
        String lastHop = partesMsgRandomWalk.get(4);

        vizinhosList.forEach(vizinho -> {
            String portaVizinho = vizinho.split(":")[1];

            if (!portaVizinho.equals(lastHop)) // excluir o nó de origem
                nosDestinoAjustado.add(vizinho);
        });

        String noEscolhidoEnderecoPorta =  endereco + ":" + lastHop; // se não houver vizinhos para além do nó de origem, enviar para o nó de origem
        if (!nosDestinoAjustado.isEmpty())
            noEscolhidoEnderecoPorta = nosDestinoAjustado.get(new Random().nextInt(nosDestinoAjustado.size()));

        // enviar msg
        String msgRandomWalk =  String.join(" ", partesMsgRandomWalk);
        enviarMsgRandomWalk(msgRandomWalk, noEscolhidoEnderecoPorta);
    }

}
