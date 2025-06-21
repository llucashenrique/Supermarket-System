package com.lctech.supermercado.gui;

import com.lctech.supermercado.service.OrderService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

@Controller
public class SalesAnalyticsController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private BarChart<String, Number> monthlySalesChart;
    @FXML private PieChart paymentRangeChart;
    @FXML private Label totalOrdersLabel;
    @FXML private ComboBox<Integer> yearComboBox;

    @FXML
    private Label totalSalesLabel;

    @Autowired
    private OrderService orderService;

    @FXML
    private DatePicker monthlyYearPicker;

    private int selectedYear = LocalDate.now().getYear(); // ano atual por padrão

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        monthlyYearPicker.setValue(LocalDate.now()); // só o ano será usado
        updateMonthlyChart(LocalDate.now().getYear());
    }

    private void updateMonthlyChart(int year) {
        monthlySalesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Vendas Mensais " + year);

        for (int month = 1; month <= 12; month++) {
            double total = orderService.getMonthlyTotalByMonthAndYear(month, year);
            XYChart.Data<String, Number> data = new XYChart.Data<>(getMonthName(month), total);
            series.getData().add(data);
        }

        monthlySalesChart.getData().add(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip("Total: R$ " + String.format("%.2f", data.getYValue().doubleValue()));
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.seconds(5));
                tooltip.setShowDuration(Duration.seconds(10));
                tooltip.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ffffff; -fx-text-fill: #000000;");

                Node node = data.getNode();
                if (node != null) {
                    Tooltip.install(node, tooltip);
                    node.setOnMouseEntered(e -> tooltip.show(node, e.getScreenX(), e.getScreenY()));
                    node.setOnMouseExited(e -> tooltip.hide());
                } else {
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip.install(newNode, tooltip);
                            newNode.setOnMouseEntered(e -> tooltip.show(newNode, e.getScreenX(), e.getScreenY()));
                            newNode.setOnMouseExited(e -> tooltip.hide());
                        }
                    });
                }
            }
        });
    }


    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Fev";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "Mai";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Set";
            case 10 -> "Out";
            case 11 -> "Nov";
            case 12 -> "Dez";
            default -> "";
        };
    }

    @FXML
    private void handleGenerateMonthlyChart() {
        LocalDate selectedDate = monthlyYearPicker.getValue();
        if (selectedDate == null) {
            showAlert("Erro", "Selecione uma data para obter o ano.");
            return;
        }

        int year = selectedDate.getYear();
        updateMonthlyChart(year);
    }


    @FXML
    private void handleGeneratePaymentChart() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || end.isBefore(start)) {
            showAlert("Alerta de Data", "Selecione um intervalo de datas válido.");
            return;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();

        paymentRangeChart.getData().clear();

        double dinheiro = orderService.getTotalByPaymentTypeAndDate("DINHEIRO", startDateTime, endDateTime);
        double credito = orderService.getTotalByPaymentTypeAndDate("CREDITO", startDateTime, endDateTime);
        double debito = orderService.getTotalByPaymentTypeAndDate("DEBITO", startDateTime, endDateTime);
        double pix = orderService.getTotalByPaymentTypeAndDate("PIX", startDateTime, endDateTime);

        double total = dinheiro + credito + debito + pix;
        long totalPedidos = orderService.countOrdersByDateRange(startDateTime, endDateTime);

        totalOrdersLabel.setText("Total de pedidos: " + totalPedidos);
        totalSalesLabel.setText(String.format("R$ %.2f", total));

        addPaymentData("Dinheiro", dinheiro, total);
        addPaymentData("Crédito", credito, total);
        addPaymentData("Débito", debito, total);
        addPaymentData("Pix", pix, total);
    }



    private void addPaymentData(String label, double value, double total) {
        if (value > 0) {
            PieChart.Data slice = new PieChart.Data(label, value);
            paymentRangeChart.getData().add(slice);

            Platform.runLater(() -> {
                Node node = slice.getNode();
                Tooltip tooltip = new Tooltip(String.format("%s: %.2f%% (R$ %.2f)", label, (value / total) * 100, value));
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.seconds(5));
                tooltip.setShowDuration(Duration.seconds(10));
                tooltip.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ffffff; -fx-text-fill: #000000;");

                if (node != null) {
                    Tooltip.install(node, tooltip);
                    node.setOnMouseEntered(e -> tooltip.show(node, e.getScreenX(), e.getScreenY()));
                    node.setOnMouseExited(e -> tooltip.hide());
                } else {
                    slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip.install(newNode, tooltip);
                            newNode.setOnMouseEntered(e -> tooltip.show(newNode, e.getScreenX(), e.getScreenY()));
                            newNode.setOnMouseExited(e -> tooltip.hide());
                        }
                    });
                }
            });
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
