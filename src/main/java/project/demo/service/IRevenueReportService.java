package project.demo.service;

import java.time.LocalDate;
import java.util.List;

import project.demo.model.RevenueReport;

public interface IRevenueReportService {
    
    /**
     * Find or create a daily revenue report for a specific date
     */
    RevenueReport findOrCreateDailyReport(LocalDate date);
    
    /**
     * Update report with order data
     */
    void updateReportWithOrder(RevenueReport report, Double orderTotal, Double shippingFee, Double discountAmount);
    
    /**
     * Get revenue reports for a date range
     */
    List<RevenueReport> getReportsForDateRange(LocalDate startDate, LocalDate endDate, String reportType);
    
    /**
     * Get daily reports for a specific month
     */
    List<RevenueReport> getDailyReportsForMonth(int year, int month);
    
    /**
     * Get monthly reports for a specific year
     */
    List<RevenueReport> getMonthlyReportsForYear(int year);
    
    /**
     * Save shipping data for revenue tracking when an order is placed
     */
    void saveShippingData(Integer orderId, Double subtotal, Double shippingFee, Double discountAmount);
}