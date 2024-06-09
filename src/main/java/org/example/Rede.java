package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
public class Rede {
    private Map<String, No> nosDaRede; // chave = "endereço:porta"
    private int ttlPadrao;
    private int numSaltosChaveEncontrada;
    private List<Integer> numHopsPorBuscaFloodingList; // armazena o número de hops de cada busca
    private List<Integer> numHopsPorBuscaRwList;
    private List<Integer> numHopsPorBuscaBpList;

    public Rede(){
        this.nosDaRede = new HashMap<>();
        this.ttlPadrao = 100;
        this.numHopsPorBuscaFloodingList = new ArrayList<>();
        this.numHopsPorBuscaRwList = new ArrayList<>();
        this.numHopsPorBuscaBpList = new ArrayList<>();
    }
}
