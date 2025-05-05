// package project.demo.repository;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
// import project.demo.model.CatalogAssociation;

// import java.util.List;

// @Repository
// public interface CatalogAssociationRepository extends
// JpaRepository<CatalogAssociation, Integer> {

// List<CatalogAssociation>
// findByAntecedentCatalogIdOrderByConfidenceDesc(Integer catalogId);

// List<CatalogAssociation>
// findByConfidenceGreaterThanOrderByConfidenceDesc(Double minConfidence);
// }