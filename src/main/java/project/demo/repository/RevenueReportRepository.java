package project.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.demo.model.RevenueReport;

@Repository
public interface RevenueReportRepository extends JpaRepository<RevenueReport, Integer> {
    
    // Find report by date and type
    Optional<RevenueReport> findByReportDateAndReportType(LocalDate reportDate, String reportType);
    
    // Find reports in date range
    List<RevenueReport> findByReportDateBetweenAndReportType(LocalDate startDate, LocalDate endDate, String reportType);
    
    // Get daily reports for current month
    @Query("SELECT r FROM RevenueReport r WHERE YEAR(r.reportDate) = :year AND MONTH(r.reportDate) = :month AND r.reportType = 'DAILY'")
    List<RevenueReport> findDailyReportsForMonth(@Param("year") int year, @Param("month") int month);
    
    // Get monthly reports for current year
    @Query("SELECT r FROM RevenueReport r WHERE YEAR(r.reportDate) = :year AND r.reportType = 'MONTHLY'")
    List<RevenueReport> findMonthlyReportsForYear(@Param("year") int year);
}