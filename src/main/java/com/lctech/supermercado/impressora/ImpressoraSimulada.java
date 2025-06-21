package com.lctech.supermercado.impressora;

import java.io.PrintWriter;
import java.io.IOException;

public class ImpressoraSimulada implements ImpressoraService {

    @Override
    public void imprimir(String conteudo) {
        System.out.println(">>> Simulando impressão do cupom:\n");
        System.out.println(conteudo);

        try (PrintWriter out = new PrintWriter("cupom_simulado.txt")) {
            out.println(conteudo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
