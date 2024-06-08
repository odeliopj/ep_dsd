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
    private Map<String, No> nosDaRede; // chave = "endereço:porta"
    private int ttlPadrao;

    public Rede(){
        this.nosDaRede = new HashMap<>();
        this.ttlPadrao = 100;
    }
}
