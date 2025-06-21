package com.lctech.supermercado.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class PedidoWrapper {
    private final Order pedido;
    private final BooleanProperty selecionado = new SimpleBooleanProperty(false);

    public PedidoWrapper(Order pedido) {
        this.pedido = pedido;
    }

    public BooleanProperty selecionadoProperty() {
        return selecionado;
    }

    public boolean isSelecionado() {
        return selecionado.get();
    }

    public Order getPedido() {
        return pedido;
    }
}

