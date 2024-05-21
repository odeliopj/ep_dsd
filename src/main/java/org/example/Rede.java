package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class Rede {
    private Map<String, No> nosDaRede;

    public Rede(){
        this.nosDaRede = new HashMap<>();
    }

    public void criarOuObterNo(String endereco, int porta){
        String chave = endereco + ":" + porta;

        if (!nosDaRede.containsKey(chave)){
            No novoNo = new No(endereco, porta);
            nosDaRede.put(chave, novoNo);
        }
    }

    public void inicializarNo(String enderecoPorta, String caminhoVizinhos, String caminhoChavesValor){
        try () {

        } catch () {

        }
    }


}
