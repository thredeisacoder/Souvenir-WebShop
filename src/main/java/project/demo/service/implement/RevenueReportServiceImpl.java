package project.demo.service.implement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.demo.model.Order;
import project.demo.model.RevenueReport;
import project.demo.repository.OrderRepository;
import project.demo.repository.RevenueReportRepository;
import project.demo.service.IRevenueReportService;

@Service
public class RevenueReportServiceImpl implements IRevenueReportService {

    private final RevenueReportRepository revenueReportRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public RevenueReportServiceImpl(RevenueReportRepository revenueReportRepository, OrderRepository orderRepository) {
        this.revenueReportRepository = revenueReportRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public RevenueReport findOrCreateDailyReport(LocalDate date) {
        Optional<RevenueReport> existingReport = revenueReportRepository.findByReportDateAndReportType(date, "DAILY");
        
        if (existingReport.isPresent()) {
            return existingReport.get();
        } else {
            RevenueReport newReport = new RevenueReport(date, "DAILY");
            newReport.setStartDate(date);
            newReport.setEndDate(date);
            newReport.setTotalOrders(0);
            return revenueReportRepository.save(newReport);
        }
    }

    @Override
    @Transactional
    public void updateReportWithOrder(RevenueReport report, Double orderTotal, Double shippingFee, Double discountAmount) {
        if (report != null) {
            report.addOrder(orderTotal, shippingFee, discountAmount);
            
            // Calculate total profit (simplified - in real application this would involve more complex calculations)
            // For now we'll just assume profit is 30% of net revenue
            if (report.getNetRevenue() != null) {
                BigDecimal profitMargin = new BigDecimal("0.3"); // 30% profit margin
                BigDecimal profit = report.getNetRevenue().multiply(profitMargin);
                report.setTotalProfit(profit);
            }
            
            // Update the timestamp
            report.setUpdatedAt(LocalDateTime.now());
            
            revenueReportRepository.save(report);
        }
    }

    @Override
    public List<RevenueReport> getReportsForDateRange(LocalDate startDate, LocalDate endDate, String reportType) {
        return revenueReportRepository.findByReportDateBetweenAndReportType(startDate, endDate, reportType);
    }

    @Override
    public List<RevenueReport> getDailyReportsForMonth(int year, int month) {
        return revenueReportRepository.findDailyReportsForMonth(year, month);
    }

    @Override
    public List<RevenueReport> getMonthlyReportsForYear(int year) {
        return revenueReportRepository.findMonthlyReportsForYear(year);
    }

    @Override
    @Transactional
    public void saveShippingData(Integer orderId, Double subtotal, Double shippingFee, Double discountAmount) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            
            // Find or create daily report
            RevenueReport dailyReport = findOrCreateDailyReport(orderDate);
            
            // Update the report with order data
            updateReportWithOrder(dailyReport, subtotal, shippingFee, discountAmount);
            
            // Link order to the report
            if (dailyReport.getOrderId() == null) {
                dailyReport.setOrderId(orderId);
                revenueReportRepository.save(dailyReport);
            }
        }
    }
}