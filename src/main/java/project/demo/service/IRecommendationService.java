// package project.demo.service;

// import project.demo.model.Catalog;
// import project.demo.model.CatalogAssociation;

// import java.util.List;
// import java.util.Map;
// import java.util.Set;

// public interface IRecommendationService {

// /**
// * Generate catalog associations using the Apriori algorithm
// *
// * @param minSupport minimum support threshold
// * @param minConfidence minimum confidence threshold
// */
// void generateCatalogAssociations(double minSupport, double minConfidence);

// /**
// * Get recommended catalogs based on a given catalog
// *
// * @param catalogId the ID of the catalog
// * @param limit maximum number of recommendations
// * @return list of recommended catalogs
// */
// List<Catalog> getRecommendedCatalogs(Integer catalogId, int limit);

// /**
// * Get all catalog associations with confidence above threshold
// *
// * @param minConfidence minimum confidence threshold
// * @return list of catalog associations
// */
// List<CatalogAssociation> getCatalogAssociations(double minConfidence);
// }