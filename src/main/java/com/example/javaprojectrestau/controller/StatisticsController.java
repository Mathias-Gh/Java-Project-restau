package com.example.javaprojectrestau.controller;

import com.example.javaprojectrestau.model.Order;
import com.example.javaprojectrestau.model.OrderItem;
import com.example.javaprojectrestau.model.Dish;
import com.example.javaprojectrestau.model.Table;
import com.example.javaprojectrestau.service.OrderService;
import com.example.javaprojectrestau.service.DishService;
import com.example.javaprojectrestau.service.TableService;
import com.example.javaprojectrestau.service.FinancialService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> chartTypeComboBox;
    
    // Graphiques
    @FXML private StackPane financialChartContainer;
    @FXML private BarChart<String, Number> revenueExpenseBarChart;
    @FXML private CategoryAxis revExpXAxis;
    @FXML private NumberAxis revExpYAxis;
    
    @FXML private LineChart<String, Number> ordersLineChart;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;
    
    @FXML private PieChart orderStatusPieChart;
    @FXML private BarChart<String, Number> dishPopularityBarChart;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> tableOccupancyBarChart;
    @FXML private BarChart<String, Number> tableRevenueBarChart;
    
    // Labels des indicateurs
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label profitMarginLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgOrderValueLabel;
    @FXML private Label topDishLabel;
    @FXML private Label peakTimeLabel;
    @FXML private Label topCategoryLabel;
    @FXML private Label avgDishPriceLabel;
    @FXML private Label mostProfitableDishLabel;
    @FXML private Label mostOccupiedTableLabel;
    @FXML private Label mostProfitableTableLabel;
    @FXML private Label avgOccupationTimeLabel;
    
    private final OrderService orderService = new OrderService();
    private final DishService dishService = new DishService();
    private final TableService tableService = new TableService();
    private final FinancialService financialService = new FinancialService();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser la période par défaut (30 derniers jours)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        
        // Initialiser les types de graphiques
        chartTypeComboBox.setItems(FXCollections.observableArrayList(
            "Barres", "Lignes", "Camembert"
        ));
        chartTypeComboBox.setValue("Barres");
        chartTypeComboBox.setOnAction(e -> updateCharts());
        
        // Charger les données initiales
        loadAllData();
    }
    
    @FXML
    private void handleApplyFilter() {
        loadAllData();
    }
    
    @FXML
    private void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données statistiques");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV", "*.csv")
        );
        fileChooser.setInitialFileName("statistiques_restaurant_" + 
                                     LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        
        Stage stage = (Stage) financialChartContainer.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // En-tête
                writer.write("Catégorie,Métrique,Valeur\n");
                
                // Finances
                writer.write("Finances,Revenus totaux," + totalRevenueLabel.getText() + "\n");
                writer.write("Finances,Dépenses totales," + totalExpensesLabel.getText() + "\n");
                writer.write("Finances,Profit net," + netProfitLabel.getText() + "\n");
                writer.write("Finances,Marge bénéficiaire," + profitMarginLabel.getText() + "\n");
                
                // Commandes
                writer.write("Commandes,Nombre total," + totalOrdersLabel.getText() + "\n");
                writer.write("Commandes,Valeur moyenne," + avgOrderValueLabel.getText() + "\n");
                writer.write("Commandes,Plat le plus commandé," + topDishLabel.getText() + "\n");
                writer.write("Commandes,Heure de pointe," + peakTimeLabel.getText() + "\n");
                
                // Plats & Catégories
                writer.write("Plats,Catégorie populaire," + topCategoryLabel.getText() + "\n");
                writer.write("Plats,Prix moyen," + avgDishPriceLabel.getText() + "\n");
                writer.write("Plats,Plat le plus rentable," + mostProfitableDishLabel.getText() + "\n");
                
                // Tables
                writer.write("Tables,Table la plus occupée," + mostOccupiedTableLabel.getText() + "\n");
                writer.write("Tables,Table la plus rentable," + mostProfitableTableLabel.getText() + "\n");
                writer.write("Tables,Durée moyenne d'occupation," + avgOccupationTimeLabel.getText() + "\n");
                
                showAlert("Exportation réussie", "Les données statistiques ont été exportées avec succès dans " + file.getName());
            } catch (IOException e) {
                showAlert("Erreur d'exportation", "Une erreur s'est produite lors de l'exportation des données: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void loadAllData() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert("Erreur de date", "Veuillez sélectionner une période valide.");
            return;
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        // Charger toutes les données nécessaires pour les graphiques
        List<Order> orders = orderService.getAllOrders().stream()
                .filter(order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    return orderTime != null && 
                           !orderTime.isBefore(startDateTime) && 
                           orderTime.isBefore(endDateTime);
                })
                .collect(Collectors.toList());
        
        // Mise à jour des graphiques
        updateFinancialCharts(orders, startDateTime, endDateTime);
        updateOrdersCharts(orders);
        updateDishAndCategoryCharts(orders);
        updateTableCharts(orders);
        
        // Mise à jour des indicateurs de performance clés
        updateKeyPerformanceIndicators(orders, startDateTime, endDateTime);
    }
    
    private void updateCharts() {
        // Recharge les données avec le type de graphique sélectionné
        loadAllData();
    }
    
    private void updateFinancialCharts(List<Order> orders, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Préparer les données pour le graphique des revenus vs dépenses
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");
        
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Dépenses");
        
        // Organiser les données par mois ou par semaine selon la durée de la période
        Map<String, BigDecimal> revenueByPeriod = new LinkedHashMap<>();
        Map<String, BigDecimal> expensesByPeriod = new LinkedHashMap<>();
        
        // Exemple: agrégation par jour
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Calculer les revenus par jour à partir des commandes
        for (Order order : orders) {
            String dateKey = order.getOrderTime().format(formatter);
            BigDecimal orderTotal = order.getTotalPrice();
            
            revenueByPeriod.merge(dateKey, orderTotal, BigDecimal::add);
        }
        
        // Simuler des dépenses (ceci serait normalement chargé depuis une base de données)
        // Dans une implémentation réelle, utilisez financialService.getExpensesByPeriod()
        Random random = new Random(123); // seed fixe pour reproducibilité
        for (String date : revenueByPeriod.keySet()) {
            // Simuler des dépenses aléatoires entre 40% et 70% des revenus
            BigDecimal revenue = revenueByPeriod.get(date);
            double expenseRatio = 0.4 + (random.nextDouble() * 0.3);
            BigDecimal expense = revenue.multiply(BigDecimal.valueOf(expenseRatio))
                    .setScale(2, RoundingMode.HALF_UP);
            
            expensesByPeriod.put(date, expense);
        }
        
        // Remplir les séries avec les données
        for (String date : revenueByPeriod.keySet()) {
            revenueSeries.getData().add(new XYChart.Data<>(date, revenueByPeriod.get(date)));
            
            BigDecimal expense = expensesByPeriod.getOrDefault(date, BigDecimal.ZERO);
            expensesSeries.getData().add(new XYChart.Data<>(date, expense));
        }
        
        // Mettre à jour le graphique
        revenueExpenseBarChart.getData().clear();
        revenueExpenseBarChart.getData().addAll(revenueSeries, expensesSeries);
    }
    
    private void updateOrdersCharts(List<Order> orders) {
        // 1. Graphique linéaire du nombre de commandes par jour
        XYChart.Series<String, Number> orderCountSeries = new XYChart.Series<>();
        orderCountSeries.setName("Nombre de commandes");
        
        // Organiser les commandes par jour
        Map<String, Long> ordersPerDay = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getOrderTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM")),
                    Collectors.counting()
                ));
        
        // Trier par date
        Map<String, Long> sortedOrdersPerDay = new TreeMap<>(ordersPerDay);
        
        for (Map.Entry<String, Long> entry : sortedOrdersPerDay.entrySet()) {
            orderCountSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        ordersLineChart.getData().clear();
        ordersLineChart.getData().add(orderCountSeries);
        
        // 2. Graphique circulaire pour les statuts des commandes
        orderStatusPieChart.getData().clear();
        
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : ordersByStatus.entrySet()) {
            String statusLabel;
            switch (entry.getKey()) {
                case "EN_ATTENTE": 
                    statusLabel = "En attente";
                    break;
                case "PREPAREE": 
                    statusLabel = "Préparée";
                    break;
                case "ANNULEE": 
                    statusLabel = "Annulée";
                    break;
                default: 
                    statusLabel = entry.getKey();
            }
            
            PieChart.Data slice = new PieChart.Data(statusLabel, entry.getValue());
            orderStatusPieChart.getData().add(slice);
        }
    }
    
    private void updateDishAndCategoryCharts(List<Order> orders) {
        // 1. Popularité des plats (top 10)
        Map<String, Integer> dishCounts = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String dishName = item.getDishName();
                dishCounts.merge(dishName, item.getQuantity(), Integer::sum);
            }
        }
        
        // Trier et prendre les 10 premiers
        List<Map.Entry<String, Integer>> topDishes = dishCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        XYChart.Series<String, Number> dishSeries = new XYChart.Series<>();
        dishSeries.setName("Quantité vendue");
        
        for (Map.Entry<String, Integer> entry : topDishes) {
            dishSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        dishPopularityBarChart.getData().clear();
        dishPopularityBarChart.getData().add(dishSeries);
        
        // 2. Ventes par catégorie (graphique circulaire)
        categoryPieChart.getData().clear();
        
        // Charger tous les plats pour avoir leurs catégories
        List<Dish> allDishes = dishService.getAllDishes();
        Map<Long, String> dishCategories = allDishes.stream()
                .collect(Collectors.toMap(Dish::getId, Dish::getCategory));
        
        // Compter les ventes par catégorie
        Map<String, BigDecimal> salesByCategory = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String category = dishCategories.getOrDefault(item.getDishId(), "Autre");
                BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                salesByCategory.merge(category, totalPrice, BigDecimal::add);
            }
        }
        
        for (Map.Entry<String, BigDecimal> entry : salesByCategory.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey(), entry.getValue().doubleValue()
            );
            categoryPieChart.getData().add(slice);
        }
    }
    
    private void updateTableCharts(List<Order> orders) {
        // 1. Taux d'occupation des tables
        List<Table> allTables = tableService.getAllTables();
        Map<Long, String> tableNumbers = allTables.stream()
                .collect(Collectors.toMap(Table::getId, Table::getNumero));
        
        // Compter le nombre de commandes par table
        Map<Long, Integer> orderCountByTable = new HashMap<>();
        
        for (Order order : orders) {
            Long tableId = order.getTableId();
            if (tableId != null) {
                orderCountByTable.merge(tableId, 1, Integer::sum);
            }
        }
        
        XYChart.Series<String, Number> occupancySeries = new XYChart.Series<>();
        occupancySeries.setName("Taux d'occupation (%)");
        
        // Calculer un taux d'occupation relatif
        int maxOrders = orderCountByTable.values().stream().max(Integer::compare).orElse(1);
        
        for (Table table : allTables) {
            int orderCount = orderCountByTable.getOrDefault(table.getId(), 0);
            // Calcul d'un pourcentage relatif
            double occupancyRate = (double) orderCount / maxOrders * 100;
            
            occupancySeries.getData().add(new XYChart.Data<>(
                table.getNumero(), occupancyRate
            ));
        }
        
        tableOccupancyBarChart.getData().clear();
        tableOccupancyBarChart.getData().add(occupancySeries);
        
        // 2. Revenus par table
        Map<Long, BigDecimal> revenueByTable = new HashMap<>();
        
        for (Order order : orders) {
            Long tableId = order.getTableId();
            if (tableId != null) {
                BigDecimal orderTotal = order.getTotalPrice();
                revenueByTable.merge(tableId, orderTotal, BigDecimal::add);
            }
        }
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus (€)");
        
        for (Table table : allTables) {
            BigDecimal revenue = revenueByTable.getOrDefault(table.getId(), BigDecimal.ZERO);
            
            revenueSeries.getData().add(new XYChart.Data<>(
                table.getNumero(), revenue
            ));
        }
        
        tableRevenueBarChart.getData().clear();
        tableRevenueBarChart.getData().add(revenueSeries);
    }
    
    private void updateKeyPerformanceIndicators(List<Order> orders, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Finances
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Simulation de dépenses (dans une implémentation réelle, ce serait chargé depuis la base de données)
        BigDecimal totalExpenses = totalRevenue.multiply(BigDecimal.valueOf(0.65))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        totalRevenueLabel.setText(formatCurrency(totalRevenue));
        totalExpensesLabel.setText(formatCurrency(totalExpenses));
        netProfitLabel.setText(formatCurrency(netProfit));
        profitMarginLabel.setText(profitMargin.setScale(1, RoundingMode.HALF_UP) + "%");
        
        // Commandes
        int totalOrders = orders.size();
        
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        avgOrderValueLabel.setText(formatCurrency(avgOrderValue));
        
        // Plat le plus commandé
        Map<String, Integer> dishCounts = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                dishCounts.merge(item.getDishName(), item.getQuantity(), Integer::sum);
            }
        }
        
        // Trouver le plat le plus commandé
        String topDish = dishCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        
        topDishLabel.setText(topDish);
        
        // Heure de pointe (basée sur l'heure de la commande)
        Map<Integer, Integer> ordersByHour = new HashMap<>();
        
        for (Order order : orders) {
            int hour = order.getOrderTime().getHour();
            ordersByHour.merge(hour, 1, Integer::sum);
        }
        
        int peakHour = ordersByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
        
        peakTimeLabel.setText(peakHour != -1 ? peakHour + "h" : "-");
        
        // Autres indicateurs de plat et catégorie
        List<Dish> allDishes = dishService.getAllDishes();
        
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                final Long dishId = item.getDishId();
                allDishes.stream()
                        .filter(d -> d.getId().equals(dishId))
                        .findFirst()
                        .ifPresent(dish -> {
                            String category = dish.getCategory();
                            categoryCounts.merge(category, item.getQuantity(), Integer::sum);
                        });
            }
        }
        
        String topCategory = categoryCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        
        BigDecimal avgDishPrice = allDishes.isEmpty() ? BigDecimal.ZERO :
                allDishes.stream()
                        .map(Dish::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allDishes.size()), 2, RoundingMode.HALF_UP);
        
        // Simplification pour le plat le plus rentable (normalement il faudrait les coûts)
        String mostProfitableDish = allDishes.stream()
                .max(Comparator.comparing(Dish::getPrice))
                .map(Dish::getName)
                .orElse("-");
        
        topCategoryLabel.setText(topCategory);
        avgDishPriceLabel.setText(formatCurrency(avgDishPrice));
        mostProfitableDishLabel.setText(mostProfitableDish);
        
        // Tables
        List<Table> allTables = tableService.getAllTables();
        
        // Table la plus occupée
        Map<Long, Integer> tableOccupation = new HashMap<>();
        for (Order order : orders) {
            Long tableId = order.getTableId();
            if (tableId != null) {
                tableOccupation.merge(tableId, 1, Integer::sum);
            }
        }
        
        String mostOccupiedTable = "-";
        if (!tableOccupation.isEmpty()) {
            Long mostOccupiedTableId = tableOccupation.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
                    
            if (mostOccupiedTableId != null) {
                mostOccupiedTable = allTables.stream()
                        .filter(t -> t.getId().equals(mostOccupiedTableId))
                        .findFirst()
                        .map(Table::getNumero)
                        .orElse("-");
            }
        }
        
        mostOccupiedTableLabel.setText(mostOccupiedTable);
        
        // Table la plus rentable
        Map<Long, BigDecimal> revenueByTable = new HashMap<>();
        
        for (Order order : orders) {
            Long tableId = order.getTableId();
            if (tableId != null) {
                revenueByTable.merge(tableId, order.getTotalPrice(), BigDecimal::add);
            }
        }
        
        String mostProfitableTable = "-";
        if (!revenueByTable.isEmpty()) {
            Long mostProfitableTableId = revenueByTable.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
                    
            if (mostProfitableTableId != null) {
                mostProfitableTable = allTables.stream()
                        .filter(t -> t.getId().equals(mostProfitableTableId))
                        .findFirst()
                        .map(Table::getNumero)
                        .orElse("-");
            }
        }
        
        mostProfitableTableLabel.setText(mostProfitableTable);
        
        // Durée moyenne d'occupation (simplifiée pour cette démo)
        avgOccupationTimeLabel.setText("45 min");
    }
    
    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP) + " €";
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer le style au dialogue
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/com/example/javaprojectrestau/styles/dark-theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }
}
