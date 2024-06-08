package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InfoMsgBP {
    private String identificacaoMsg;
    private String noMae;
    private String noFilho;
    private List<String> vizinhosCandidatosList;
}
