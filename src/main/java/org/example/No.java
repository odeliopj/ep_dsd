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
    private int numMsgsVistasFlooding;
    private int numMsgsVistasRandomWalk;
    private int numMsgsVistasBP;
    private List<Integer> numHopsValFloodingList;
    private List<Integer> numHopsValRandomWalkList;
    private List<Integer> numHopsValBPList;
    private List<InfoMsgBP> infoMsgVistaListBP;

    public No(String endereco, int porta, Rede rede) {
        this.endereco = endereco;
        this.porta = porta;
        this.vizinhosList = new ArrayList<>();
        this.msgsVistasList = new ArrayList<>();
        this.chaveValorMap = new HashMap<>();
        this.numeroSequenciaMsg = 0;
        this.rede = rede;
        this.numMsgsVistasFlooding = 0;
        this.numMsgsVistasRandomWalk = 0;
        this.numMsgsVistasBP = 0;
        this.numHopsValFloodingList = new ArrayList<>();
        this.numHopsValRandomWalkList = new ArrayList<>();
        this.numHopsValBPList = new ArrayList<>();
        this.infoMsgVistaListBP = new ArrayList<>();
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

    public void listarVizinhos(){
        long numeroDeVizinhos = vizinhosList.size();
        System.out.println("Ha " + numeroDeVizinhos + " vizinhos na tabela:");

        if (numeroDeVizinhos > 0) {
            for (int i = 0; i < numeroDeVizinhos; i++) {
                System.out.println("[" + i + "] " + vizinhosList.get(i));
            }
        }

        Servicos.executarMenu = true;
    }

    /** Métodos criação sockets **/
    public Socket criarSocket(String enderecoDestino, int portaDestino) throws IOException {
        return new Socket(enderecoDestino, portaDestino);
    }

    private void escutarConexoes() {
        new Thread(() -> {
            while (Servicos.escutarConexoes) {
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

        /* HELLO */
        if (partesMensagem.contains("HELLO"))
            processarMsgHello(socketRecebimento, mensagem);

        else if (partesMensagem.contains("HELLO_OK")) {
            System.out.println("  OK - Recebida com sucesso: " + mensagem);
            Servicos.executarMenu = true;
            Servicos.escutarConexoes = false;
        }

        /* FLOODING */
        else if (partesMensagem.contains("SEARCH_FL")) {
            numMsgsVistasFlooding++;
            processarMsgFlooding(socketRecebimento, mensagem);
        }

        else if (partesMensagem.contains("FL_OK"))
            System.out.println("  OK - Recebida com sucesso: " + "'" + mensagem + "'");

        else if (partesMensagem.contains("VAL_FL")) {
            adicionarNumSaltos(partesMensagem);
            System.out.println("Chave encontrada! Chave: " + partesMensagem.get(4) + " >" + " Valor: " + partesMensagem.get(5));
            Servicos.exibirEstatisticas(rede);
            Servicos.executarMenu = true;
            Servicos.escutarConexoes = false;
        }

        /* RANDOM WALK */
        else if (partesMensagem.contains("SEARCH_RW")) {
            numMsgsVistasRandomWalk++;
            processarMsgRandomWalk(socketRecebimento, mensagem);
        }

        else if (partesMensagem.contains("VAL_RW")) {
            adicionarNumSaltos(partesMensagem);
            System.out.println("Chave encontrada! Chave: " + partesMensagem.get(4) + " >" + " Valor: " + partesMensagem.get(5));
            Servicos.exibirEstatisticas(rede);
            Servicos.executarMenu = true;
            Servicos.escutarConexoes = false;
        }

        /* BUSCA PROFUNDIDADE */
        else if (partesMensagem.contains("SEARCH_BP")) {
            numMsgsVistasBP++;
            processarMsgBuscaProfundidade(socketRecebimento, mensagem);
        }

        else if (partesMensagem.contains("VAL_BP")) {
            adicionarNumSaltos(partesMensagem);
            System.out.println("Chave encontrada! Chave: " + partesMensagem.get(4) + " >" + " Valor: " + partesMensagem.get(5));
            Servicos.exibirEstatisticas(rede);
            Servicos.executarMenu = true;
            Servicos.escutarConexoes = false;
        }
    }

    void adicionarNumSaltos(List<String> partesMensagem){
        String mode = partesMensagem.get(3);
        int hopsCount = Integer.parseInt(partesMensagem.get(6));

        if (mode.equals("VAL_FL"))
            numHopsValFloodingList.add(hopsCount);

        if (mode.equals("VAL_RW"))
            numHopsValRandomWalkList.add(hopsCount);

        if (mode.equals("VAL_BP"))
            numHopsValBPList.add(hopsCount);
    }

/***************************************************************************************************************************************************/
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
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(mensagem);
            System.out.println("  Envio feito com sucesso: " + "'" + mensagem + "'");
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
        System.out.println("Mensagem recebida: " + "'" + mensagem + "'");
        String[] partesMensagem = mensagem.split(" ");
        String enderecoPortaOrigem = partesMensagem[0];

        if (!vizinhosList.contains(enderecoPortaOrigem)) {
            vizinhosList.add(enderecoPortaOrigem);
            System.out.println("Adicionando vizinho na tabela: " + enderecoPortaOrigem);
        }
        System.out.println("  Vizinho ja esta na tabela: " + enderecoPortaOrigem);

        // Enviar confirmação HELLO_OK na mesma conexão
        String operacao = "HELLO_OK";
        String mensagemResposta = String.format("%s %s", endereco + ":" + porta, operacao);
        try (PrintWriter out = new PrintWriter(socketRecebimento.getOutputStream(), true)) {
            out.println(mensagemResposta);
        } catch (IOException e) {
            System.err.println("Erro ao enviar resposta HELLO_OK: " + e.getMessage());
        }
    }

/***************************************************************************************************************************************************/
    /** Métodos FLOODING **/
    public void iniciarSearchFlooding() {
        System.out.print("Digite a chave a ser buscada: ");
        Scanner scannerFlooding = new Scanner(System.in);
        String chaveBuscada = scannerFlooding.nextLine();

        if (chaveValorMap.containsKey(chaveBuscada)){
            System.out.println("Valor na tabela local!");
            System.out.println("  Chave: " + chaveBuscada + " >" + " Valor: " + chaveValorMap.get(chaveBuscada));
            Servicos.executarMenu = true;
        } else {
            String enderecoPortaOrigem = endereco + ":" + porta;
            numeroSequenciaMsg++;
            String seqNum = String.valueOf(numeroSequenciaMsg);
            String ttl = String.valueOf(rede.getTtlPadrao());
            String searchMode = "SEARCH_FL";
            String lastHopPort = String.valueOf(porta);
            String hopCount =  String.valueOf(0);    // será incrementado no método enviarMsg()
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

            System.out.println("Encaminhando mensagem " + "'" + msgFloodingAjustada + "'" + " para " + enderecoDestino + ":" + portaDestino);

            try (Socket socket = criarSocket(enderecoDestino, portaDestino)) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(msgFloodingAjustada);
                System.out.println("  Envio feito com sucesso: "+ "'" + msgFloodingAjustada + "'");
                out.flush();
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem Busca Flooding: " + msgFlooding);
            }
        });
    }

    public void processarMsgFlooding(Socket socket, String mensagem){
        List<String> partesMsgFlooding = Arrays.asList(mensagem.split(" "));
        String enderecoPortaOrigem = partesMsgFlooding.get(0);
        String enderecoOrigem = enderecoPortaOrigem.split(":")[0];
        int portaOrigem = Integer.parseInt(enderecoPortaOrigem.split(":")[1]);
        String numSeqMsg = partesMsgFlooding.get(1);
        String chaveBuscada = partesMsgFlooding.get(5);

        // Enviar confirmação FL_OK na mesma conexão
        String operacao = "FL_OK";
        String mensagemResposta = String.format("%s %s", endereco + ":" + porta, operacao);
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(mensagemResposta);
        } catch (IOException e) {
            System.err.println("Erro ao enviar resposta" + operacao + ":" + e.getMessage());
        }

        // decrementar TTL
        int ttl = Integer.parseInt(partesMsgFlooding.get(2));
        ttl--;
        partesMsgFlooding.set(2, String.valueOf(ttl));

        if (msgsVistasList.contains(enderecoOrigem + ":" + numSeqMsg)) {
            System.out.println("Flooding: Mensagem repetida");
            Servicos.executarMenu = true;
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

            System.out.println("ENCONTRADO - Encaminhando mensagem " + "'" + msgValorEncontrado + "'" + " para " + enderecoPortaOrigem);

            try (Socket socketEnvioEncontrado = criarSocket(enderecoOrigem, portaOrigem)) {
                PrintWriter out = new PrintWriter(socketEnvioEncontrado.getOutputStream(), true);
                out.println(msgValorEncontrado);
                System.out.println("  Envio feito com sucesso: "+ "'" + msgValorEncontrado + "'");
                out.flush();
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem de ENCONTRADO: " + msgValorEncontrado);
            }
            return;
        }

        // descartar mensagem se TTL = 0
        if (ttl == 0) {
            System.out.println("TTL igual a zero, descartando mensagem");
            Servicos.executarMenu = true;
            return;
        }

        // reenviar msg para os nós vizinhos (com exceção do nó de origem da msg)
        String msgFloodingAjustada = String.join(" ", partesMsgFlooding);
        List<String> nosDestino = new ArrayList<>();
        vizinhosList.forEach(vizinho -> {
            if (!vizinho.equals(enderecoPortaOrigem)) // retira o nó de origem da msg da lista de envio da msg
                nosDestino.add(vizinho);
        });

        enviarMsgFloodingVizinhos(msgFloodingAjustada, nosDestino);
    }

/***************************************************************************************************************************************************/
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

        // ajustar LAST_HOP_PORT
        partesMsgFlooding.set(4, String.valueOf(porta));

        // enviar msg
        String enderecoDestino = noEscolhidoEnderecoPorta.split(":")[0];
        int portaDestino =  Integer.parseInt(noEscolhidoEnderecoPorta.split(":")[1]);
        try (Socket socket = criarSocket(enderecoDestino, portaDestino)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msgRandomWalkAjustada);
            out.flush();
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
        else if (ttl == 0) {
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

/***************************************************************************************************************************************************/
    /** Métodos BUSCA EM PROFUNDIDADE **/
    public void iniciarSearchBuscaProfundidade(){
        System.out.print("Digite a chave a ser buscada: ");
        Scanner scannerFlooding = new Scanner(System.in);
        String chaveBuscada = scannerFlooding.nextLine();

        // verificar tabela local de chave-valor
        if (chaveValorMap.containsKey(chaveBuscada)){
            System.out.println("Valor na tabela local!");
            System.out.println("      Chave: " + chaveBuscada + " >" + " Valor: " + chaveValorMap.get(chaveBuscada));
        } else {
            String enderecoPortaAtual = endereco + ":" + porta;
            String seqNum = String.valueOf(numeroSequenciaMsg);
            String ttl = String.valueOf(rede.getTtlPadrao());
            String searchMode = "SEARCH_BP";
            String lastHopPort = String.valueOf(porta);
            String hopCount =  String.valueOf(0);
            String msgBuscaProfundidade = String.format("%s %s %s %s %s %s %s", enderecoPortaAtual, seqNum, ttl, searchMode, lastHopPort, chaveBuscada, hopCount);

            // set 'vizinhosCandidatosList' para primeira msg
            List<String> vizinhosCandidatosList = new ArrayList<>();
            vizinhosCandidatosList.addAll(vizinhosList);
            InfoMsgBP msgVista = new InfoMsgBP();
            msgVista.setVizinhosCandidatosList(vizinhosCandidatosList);
            infoMsgVistaListBP.add(msgVista);

            // seleciona um nó aleatório da lista de vizinhos, e set vizinhoAtivo
            String noVizinhoAtivo = vizinhosCandidatosList.get(new Random().nextInt(vizinhosCandidatosList.size()));
            vizinhosCandidatosList.remove(noVizinhoAtivo);

            // adicionar info da primeira msg na List de InfoMsgBP deste nó
            infoMsgVistaListBP.get(0).setIdentificacaoMsg(enderecoPortaAtual + "-" + numeroSequenciaMsg);
            infoMsgVistaListBP.get(0).setNoMae(enderecoPortaAtual);
            infoMsgVistaListBP.get(0).setNoFilho(noVizinhoAtivo);
            infoMsgVistaListBP.get(0).getVizinhosCandidatosList().remove(noVizinhoAtivo);

            System.out.println("Busca em profundidade iniciada");
            enviarMsgBuscaProfundidade(msgBuscaProfundidade, noVizinhoAtivo);
        }
    }

    public void enviarMsgBuscaProfundidade(String msgBuscaProfundidade, String noEscolhidoEnderecoPorta) {
        // ajustar msg incrementando valores antes do envio
        String msgBuscaProfundidadeAjustada = incrementarHopPortAndNumSeqBP(msgBuscaProfundidade);

        // enviar msg
        String enderecoDestino = noEscolhidoEnderecoPorta.split(":")[0];
        int portaDestino =  Integer.parseInt(noEscolhidoEnderecoPorta.split(":")[1]);
        try (Socket socket = criarSocket(enderecoDestino, portaDestino)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msgBuscaProfundidadeAjustada);
            out.flush();
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem Busca em Profundidade: " + msgBuscaProfundidadeAjustada);
        }
    }

    public void processarMsgBuscaProfundidade(Socket socket, String mensagem){
        List<String> partesMsgBuscaProfundidade = Arrays.asList(mensagem.split(" "));
        String chaveBuscada = partesMsgBuscaProfundidade.get(5);

        // verificar tabela local de chave-valor
        if (chaveValorMap.containsKey(chaveBuscada)) {
            System.out.println("Chave encontrada!");

            List<String> partesMsgChaveEncontrada = new ArrayList<>();
            partesMsgChaveEncontrada.add(endereco + ":" + porta);
            partesMsgChaveEncontrada.add(String.valueOf(numeroSequenciaMsg));
            partesMsgChaveEncontrada.add(String.valueOf(rede.getTtlPadrao()));
            partesMsgChaveEncontrada.add("VAL_BP");
            partesMsgChaveEncontrada.add(String.valueOf(porta));
            partesMsgChaveEncontrada.add(chaveBuscada);
            partesMsgChaveEncontrada.add("1");
            String msgValorEncontrado = String.join(" ", partesMsgChaveEncontrada);

            String enderecoPortaOriginal = partesMsgBuscaProfundidade.get(0);
            String enderecoOriginal = enderecoPortaOriginal.split(":")[0];
            int portaOriginal = Integer.parseInt(enderecoPortaOriginal.split(":")[1]);

            try (Socket socketEnvio = criarSocket(enderecoOriginal, portaOriginal)) {
                PrintWriter out = new PrintWriter(socketEnvio.getOutputStream(), true);
                out.println(msgValorEncontrado);
                out.flush();
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem Busca em Profundidade: " + msgValorEncontrado);
            }
        }

        // decrementar TTL
        int ttl = Integer.parseInt(partesMsgBuscaProfundidade.get(2));
        ttl--;
        partesMsgBuscaProfundidade.set(2, String.valueOf(ttl));

        // descartar mensagem se TTL = 0
        if (ttl == 0) {
            System.out.println("TTL igual a zero, descartando mensagem");
            return;
        }

        // checar se já foram inicializados: 'nó mãe' e 'lista de vizinhos candidatos'
        String identificacaoMsgRecebida = partesMsgBuscaProfundidade.get(0) + "-" + partesMsgBuscaProfundidade.get(1);
        String noAnterior = endereco + ":" + partesMsgBuscaProfundidade.get(4);
        Optional<InfoMsgBP> optionalInfoMsg = existeInfoMsgVistaBP(infoMsgVistaListBP, identificacaoMsgRecebida);

        if (optionalInfoMsg.isEmpty()) {
            InfoMsgBP infoMsgVista = new InfoMsgBP();
            infoMsgVista.setIdentificacaoMsg(identificacaoMsgRecebida);
            infoMsgVista.setNoMae(noAnterior);
            infoMsgVista.setVizinhosCandidatosList(new ArrayList<>(vizinhosList));

            infoMsgVistaListBP.add(infoMsgVista);
        }

        // remove nó que enviou a mensagem, caso esteja na lista de vizinhos candidatos
        ajustarVizinhosCandidatosBP(infoMsgVistaListBP, identificacaoMsgRecebida, noAnterior);

        // formatar msg para envio
        String msgBuscaProfundidade =  String.join(" ", partesMsgBuscaProfundidade);

        /* Condições de Parada */
        optionalInfoMsg = existeInfoMsgVistaBP(infoMsgVistaListBP, identificacaoMsgRecebida);
        InfoMsgBP infoMsgAtual = optionalInfoMsg.orElseThrow(() -> new RuntimeException("InfoMsg not found"));
        String noMae = infoMsgAtual.getNoMae();
        String noVizinhoAtivo = infoMsgAtual.getNoFilho();
        List<String> vizinhosCandidatosList = infoMsgAtual.getVizinhosCandidatosList();

        // 1) Se a execução do algoritmo chegou ao fim
        if (noMae.equals(endereco + ":" + porta) &&  vizinhosCandidatosList.isEmpty() && noVizinhoAtivo.equals(noAnterior)) {
            System.out.println("BP: Nao foi possivel localizar a chave: " + chaveBuscada);
            return;
        }

        // 2) Se um ciclo foi detectado
        else if (noVizinhoAtivo != null  && !noVizinhoAtivo.equals(noAnterior)){
            System.out.println("BP: ciclo detectado, devolvendo a mensagem...");
            enviarMsgBuscaProfundidade(msgBuscaProfundidade, noAnterior);
            return;
        }

        // 3) Se todos os vizinhos já foram consultados
        else if (vizinhosCandidatosList.isEmpty()){
            System.out.println("BP: nenhum vizinho encontrou a chave, retrocedendo..." );
            enviarMsgBuscaProfundidade(msgBuscaProfundidade, noMae);
            return;
        }

        else {
            // sorteia próximo 'noAtivo'
            String proxNoVizinhoAtivo = vizinhosCandidatosList.get(new Random().nextInt(vizinhosCandidatosList.size()));

            // set o 'noAtivo' nas informações da msg atual
            infoMsgAtual.setNoFilho(proxNoVizinhoAtivo);

            // remove nó sorteado dos vizinhosCandidatos
            ajustarVizinhosCandidatosBP(infoMsgVistaListBP, identificacaoMsgRecebida, proxNoVizinhoAtivo);

            // enviar msg
            enviarMsgBuscaProfundidade(msgBuscaProfundidade, proxNoVizinhoAtivo);
        }
    }

    /* Métodos auxiliares */
    private Optional<InfoMsgBP> existeInfoMsgVistaBP(List<InfoMsgBP> msgsVistasInfoListBP, String identificacaoMsgRecebida) {
        for (InfoMsgBP infoMsg : msgsVistasInfoListBP) {
            if (infoMsg.getIdentificacaoMsg().equals(identificacaoMsgRecebida)) {
                return Optional.of(infoMsg);
            }
        }

        return Optional.empty();
    }

    private static void ajustarVizinhosCandidatosBP(List<InfoMsgBP> msgsVistasInfoListBP, String identificacaoMsgRecebida, String noAnterior) {
        for (InfoMsgBP infoMsg : msgsVistasInfoListBP){
            if (infoMsg.getIdentificacaoMsg().equals(identificacaoMsgRecebida)){
                infoMsg.getVizinhosCandidatosList().remove(noAnterior);
                return;
            }
        }
    }

    private String incrementarHopPortAndNumSeqBP(String msg){
        // incrementar HOP_COUNT
        List<String> partesMsgBP = Arrays.asList(msg.split(" "));
        int hopCount = Integer.parseInt(partesMsgBP.get(6));
        hopCount++;
        partesMsgBP.set(6, String.valueOf(hopCount));

        // ajustar LAST_HOP_PORT
        partesMsgBP.set(4, String.valueOf(porta));

        // ajustar NUM_SEQ
        numeroSequenciaMsg++;
        partesMsgBP.set(1, String.valueOf(numeroSequenciaMsg));

        return String.join(" ", partesMsgBP);
    }
}
