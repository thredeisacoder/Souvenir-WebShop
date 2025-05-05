// package project.demo.scheduler;

// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import project.demo.service.IRecommendationService;

// @Component
// public class RecommendationScheduler {

// private final IRecommendationService recommendationService;

// public RecommendationScheduler(IRecommendationService recommendationService)
// {
// this.recommendationService = recommendationService;
// }

// @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
// public void updateCatalogAssociations() {
// recommendationService.generateCatalogAssociations(0.01, 0.3); // 1% support,
// 30% confidence
// }
// }