package com.example.javaprojectrestau.service;

import com.example.javaprojectrestau.dao.ExpenseDAO;
import com.example.javaprojectrestau.model.Expense;
import com.example.javaprojectrestau.model.Order;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour gérer les finances
 */
public class FinanceService {
    
    private final ExpenseDAO expenseDAO;
    private final OrderService orderService;
    
    public FinanceService() {
        this.expenseDAO = new ExpenseDAO();
        this.orderService = new OrderService();
        
        // S'assurer que les tables existent
        this.expenseDAO.createTablesIfNotExist();
    }
    
    /**
     * Récupère toutes les dépenses
     */
    public List<Expense> getAllExpenses() {
        return expenseDAO.findAll();
    }
    
    /**
     * Récupère les dépenses pour une période donnée
     */
    public List<Expense> getExpensesByDateRange(LocalDateTime start, LocalDateTime end) {
        return expenseDAO.findByDateRange(start, end);
    }
    
    /**
     * Sauvegarde une dépense
     */
    public Expense saveExpense(Expense expense) {
        return expenseDAO.save(expense);
    }
    
    /**
     * Supprime une dépense
     */
    public boolean deleteExpense(Long id) {
        return expenseDAO.delete(id);
    }
    
    /**
     * Calcule le total des dépenses
     */
    public BigDecimal calculateTotalExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calcule le total des recettes
     */
    public BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calcule le profit (recettes - dépenses)
     */
    public BigDecimal calculateProfit(BigDecimal revenue, BigDecimal expenses) {
        return revenue.subtract(expenses);
    }
    
    /**
     * Récupère les commandes terminées pour une période donnée
     */
    public List<Order> getCompletedOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderService.getCompletedOrders().stream()
                .filter(order -> {
                    LocalDateTime orderTime = order.getOrderTime();
                    return orderTime.isAfter(start) && orderTime.isBefore(end);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Génère un rapport financier PDF avec PDFBox (compatible avec les modules Java)
     */
    public boolean generateFinancialReport(String filePath, LocalDateTime start, LocalDateTime end) {
        List<Expense> expenses = getExpensesByDateRange(start, end);
        List<Order> completedOrders = getCompletedOrdersByDateRange(start, end);
        
        BigDecimal totalExpenses = calculateTotalExpenses(expenses);
        BigDecimal totalRevenue = calculateTotalRevenue(completedOrders);
        BigDecimal profit = calculateProfit(totalRevenue, totalExpenses);
        
        // Regrouper les dépenses par catégorie
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory() != null ? expense.getCategory() : "Non catégorisé";
            BigDecimal currentTotal = expensesByCategory.getOrDefault(category, BigDecimal.ZERO);
            expensesByCategory.put(category, currentTotal.add(expense.getAmount()));
        }
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // Traiter la première page avec les informations de base
            writeSummaryPage(document, page, start, end, totalRevenue, totalExpenses, profit, expensesByCategory);
            
            // Si des dépenses sont disponibles, écrire une page de détails des dépenses
            if (!expenses.isEmpty()) {
                PDPage detailsPage = new PDPage(PDRectangle.A4);
                document.addPage(detailsPage);
                writeExpensesPage(document, detailsPage, expenses);
            }
            
            // Ajouter une page pour les détails des recettes
            if (!completedOrders.isEmpty()) {
                PDPage revenuePage = new PDPage(PDRectangle.A4);
                document.addPage(revenuePage);
                writeRevenuePage(document, revenuePage, completedOrders);
            }
            
            document.save(filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du rapport PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Écrit la page de résumé du rapport
     */
    private void writeSummaryPage(PDDocument document, PDPage page, LocalDateTime start, LocalDateTime end, 
                               BigDecimal totalRevenue, BigDecimal totalExpenses, BigDecimal profit, 
                               Map<String, BigDecimal> expensesByCategory) throws IOException {
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Titre
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Rapport Financier");
            contentStream.endText();
            
            yPosition -= leading;
            
            // Période
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Période: du " + start.format(formatter) + " au " + end.format(formatter));
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Résumé financier
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Résumé Financier");
            contentStream.endText();
            
            yPosition -= leading;
            
            // Total des recettes
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Total des recettes: " + totalRevenue + " €");
            contentStream.endText();
            
            yPosition -= leading;
            
            // Total des dépenses
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Total des dépenses: " + totalExpenses + " €");
            contentStream.endText();
            
            yPosition -= leading;
            
            // Profit
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Profit: " + profit + " €");
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Dépenses par catégorie
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Dépenses par Catégorie");
            contentStream.endText();
            
            yPosition -= leading;
            
            // Liste des catégories
            for (Map.Entry<String, BigDecimal> entry : expensesByCategory.entrySet()) {
                // Vérifier s'il reste de l'espace sur la page
                if (yPosition < margin) {
                    break; // Arrêter si plus d'espace (les détails iront sur la page suivante)
                }
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(entry.getKey() + ": " + entry.getValue() + " €");
                contentStream.endText();
                
                yPosition -= leading;
            }
            
            // Ajouter une note pour les détails
            if (yPosition > margin * 2) {
                yPosition -= leading * 2;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("* Voir pages suivantes pour le détail des dépenses et recettes");
                contentStream.endText();
            }
        }
    }
    
    /**
     * Écrit la page de détails des dépenses
     */
    private void writeExpensesPage(PDDocument document, PDPage page, List<Expense> expenses) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Titre de la page
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Détail des Dépenses");
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Titres des colonnes
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize - 2);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(String.format("%-5s %-30s %-10s %-20s %-15s", "ID", "Nom", "Montant", "Date", "Catégorie"));
            contentStream.endText();
            
            yPosition -= leading;
            
            // Données des dépenses
            int itemsPerPage = 25;  // Estimation du nombre d'éléments par page
            int itemsOnCurrentPage = 0;
            
            for (Expense expense : expenses) {
                // Vérifier s'il faut passer à une nouvelle page
                if (itemsOnCurrentPage >= itemsPerPage || yPosition < margin + 30) {
                    // Fermer le flux actuel
                    contentStream.close();
                    
                    // Créer une nouvelle page
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    
                    // Initialiser un nouveau flux et repositionner y
                    PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);
                    yPosition = newPage.getMediaBox().getHeight() - margin;
                    
                    // Écrire l'en-tête de la nouvelle page
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText("Détail des Dépenses (suite)");
                    newContentStream.endText();
                    
                    yPosition -= leading * 2;
                    
                    // Titres des colonnes sur la nouvelle page
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize - 2);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText(String.format("%-5s %-30s %-10s %-20s %-15s", "ID", "Nom", "Montant", "Date", "Catégorie"));
                    newContentStream.endText();
                    
                    yPosition -= leading;
                    itemsOnCurrentPage = 0;
                    
                    // Écrire l'élément courant avec le nouveau flux
                    String row = String.format("%-5s %-30s %-10s %-20s %-15s", 
                        expense.getId(), 
                        truncateString(expense.getName(), 28),
                        expense.getAmount() + "€", 
                        expense.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        expense.getCategory() != null ? truncateString(expense.getCategory(), 13) : "");
                    
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA, fontSize - 2);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText(row);
                    newContentStream.endText();
                    
                    yPosition -= leading;
                    itemsOnCurrentPage++;
                    
                    // Continuer avec le nouveau flux
                    try {
                        contentStream.close(); // Fermer proprement l'ancien flux
                    } catch (Exception e) {
                        // Ignorer si déjà fermé
                    }
                    
                    return; // Terminer cette méthode et laisser l'appelant gérer la suite
                }
                
                // Ajouter l'élément à la page courante
                String row = String.format("%-5s %-30s %-10s %-20s %-15s", 
                    expense.getId(), 
                    truncateString(expense.getName(), 28),
                    expense.getAmount() + "€", 
                    expense.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    expense.getCategory() != null ? truncateString(expense.getCategory(), 13) : "");
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize - 2);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(row);
                contentStream.endText();
                
                yPosition -= leading;
                itemsOnCurrentPage++;
            }
        }
    }
    
    /**
     * Écrit la page des détails des recettes (commandes)
     */
    private void writeRevenuePage(PDDocument document, PDPage page, List<Order> orders) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Titre de la page
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Détail des Recettes");
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Titres des colonnes
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize - 2);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(String.format("%-5s %-20s %-10s %-20s", 
                                             "ID", "Date", "Montant", "Statut"));
            contentStream.endText();
            
            yPosition -= leading;
            
            // Données des commandes
            int itemsPerPage = 25;  // Estimation du nombre d'éléments par page
            int itemsOnCurrentPage = 0;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Order order : orders) {
                // Vérifier s'il faut passer à une nouvelle page
                if (itemsOnCurrentPage >= itemsPerPage || yPosition < margin + 30) {
                    // Fermer le flux actuel
                    contentStream.close();
                    
                    // Créer une nouvelle page
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    
                    // Initialiser un nouveau flux et repositionner y
                    PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);
                    yPosition = newPage.getMediaBox().getHeight() - margin;
                    
                    // Écrire l'en-tête de la nouvelle page
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText("Détail des Recettes (suite)");
                    newContentStream.endText();
                    
                    yPosition -= leading * 2;
                    
                    // Titres des colonnes sur la nouvelle page
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize - 2);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText(String.format("%-5s %-20s %-10s %-20s", 
                                                    "ID", "Date", "Montant", "Statut"));
                    newContentStream.endText();
                    
                    yPosition -= leading;
                    itemsOnCurrentPage = 0;
                    
                    // Écrire l'élément courant avec le nouveau flux
                    String row = String.format("%-5s %-20s %-10s %-20s", 
                        order.getId(), 
                        order.getOrderTime().format(dateFormatter),
                        order.getTotalPrice() + "€", 
                        order.getStatus());
                    
                    newContentStream.beginText();
                    newContentStream.setFont(PDType1Font.HELVETICA, fontSize - 2);
                    newContentStream.newLineAtOffset(margin, yPosition);
                    newContentStream.showText(row);
                    newContentStream.endText();
                    
                    yPosition -= leading;
                    itemsOnCurrentPage++;
                    
                    // Continuer avec le nouveau flux
                    try {
                        contentStream.close(); // Fermer proprement l'ancien flux
                    } catch (Exception e) {
                        // Ignorer si déjà fermé
                    }
                    
                    return; // Terminer cette méthode et laisser l'appelant gérer la suite
                }
                
                // Ajouter l'élément à la page courante
                String row = String.format("%-5s %-20s %-10s %-20s", 
                    order.getId(), 
                    order.getOrderTime().format(dateFormatter),
                    order.getTotalPrice() + "€", 
                    order.getStatus());
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize - 2);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(row);
                contentStream.endText();
                
                yPosition -= leading;
                itemsOnCurrentPage++;
            }
            
            // Ajouter un résumé des commandes par jour si l'espace le permet
            if (yPosition > margin * 4) {
                yPosition -= leading * 2;
                
                // Titre du résumé journalier
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Résumé des recettes par jour");
                contentStream.endText();
                
                yPosition -= leading * 1.5f;
                
                // Regrouper les commandes par jour
                Map<LocalDateTime, BigDecimal> revenueByDay = new HashMap<>();
                for (Order order : orders) {
                    LocalDateTime orderDate = order.getOrderTime().toLocalDate().atStartOfDay();
                    BigDecimal currentTotal = revenueByDay.getOrDefault(orderDate, BigDecimal.ZERO);
                    revenueByDay.put(orderDate, currentTotal.add(order.getTotalPrice()));
                }
                
                // Afficher les totaux par jour
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Map.Entry<LocalDateTime, BigDecimal> entry : revenueByDay.entrySet()) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(entry.getKey().format(dayFormatter) + ": " + entry.getValue() + " €");
                    contentStream.endText();
                    
                    yPosition -= leading;
                    if (yPosition < margin) break;
                }
            }
        }
    }
    
    /**
     * Tronque une chaîne si elle est trop longue
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
