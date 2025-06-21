package com.lctech.supermercado.device;

import com.fazecast.jSerialComm.*;
import java.util.regex.*;

public class BalancaController {

    // 🔒 Singleton
    private static BalancaController instanciaUnica;

    private SerialPort serialPort;
    private double pesoLido = 0;
    private boolean pesoJaCapturado = false;
    private final StringBuilder bufferLeitura = new StringBuilder();

    // 🔒 Construtor privado
    private BalancaController() {
        iniciarListener();
    }

    public static BalancaController getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new BalancaController();
        }
        return instanciaUnica;
    }

    public void resetarLeitura() {
        pesoLido = 0;
        pesoJaCapturado = false;
        bufferLeitura.setLength(0);
        iniciarListener();
        System.out.println("🔁 Leitura reiniciada e listener ativo");
    }

    private void configurarPortaSerial(String porta) {
        if (serialPort != null && serialPort.isOpen()) {
            System.out.println("⚠️ Porta " + porta + " já está aberta");
            return;
        }

        serialPort = SerialPort.getCommPort(porta);
        serialPort.setComPortParameters(9600, 8, 1, 0);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        if (!serialPort.openPort()) {
            System.out.println("❌ Não foi possível abrir a porta " + porta);
        } else {
            System.out.println("✅ Porta " + porta + " aberta com sucesso");
        }
    }

    private void iniciarListener() {
        if (serialPort == null || !serialPort.isOpen()) return;

        serialPort.addDataListener(new SerialPortDataListener() {

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;

                byte[] buffer = new byte[serialPort.bytesAvailable()];
                int bytesLidos = serialPort.readBytes(buffer, buffer.length);

                if (bytesLidos > 0) {
                    String dados = new String(buffer, 0, bytesLidos);
                    bufferLeitura.append(dados);

                    if (dados.contains("\r") || dados.contains("\n")) {
                        String respostaCompleta = bufferLeitura.toString().trim();
                        bufferLeitura.setLength(0);

                        if (!pesoJaCapturado) {
                            System.out.println("📥 Dados recebidos completos: [" + respostaCompleta + "]");
                        }

                        Pattern pattern = Pattern.compile("(\\d+\\.\\d+)");
                        Matcher matcher = pattern.matcher(respostaCompleta);
                        if (matcher.find() && !pesoJaCapturado) {
                            String pesoStr = matcher.group(1);
                            try {
                                pesoLido = Double.parseDouble(pesoStr);
                                pesoJaCapturado = true;
                                System.out.println("✅ Peso detectado: " + pesoLido + " kg");

                                // 🔇 Remove o listener após captura bem-sucedida
                                serialPort.removeDataListener();
                                System.out.println("🛑 Listener desativado após leitura");

                            } catch (NumberFormatException e) {
                                System.out.println("Erro ao converter peso: " + pesoStr);
                            }
                        }
                    }
                }
            }
        });
    }

    public double obterPesoLido() {
        return pesoLido;
    }
}
