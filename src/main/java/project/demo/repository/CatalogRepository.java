package project.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.demo.model.Catalog;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Catalog entities
 */
@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Integer> {
    
    /**
     * Find a catalog by name
     * 
     * @param catalogName the name of the catalog
     * @return an Optional containing the catalog if found
     */
    Optional<Catalog> findByCatalogName(String catalogName);
    
    /**
     * Find catalogs by status
     * 
     * @param status the status to search for
     * @return a list of catalogs with the specified status
     */
    List<Catalog> findByStatus(Boolean status);
    
    /**
     * Find catalogs by name containing the given keyword
     * 
     * @param keyword the keyword to search for
     * @return a list of catalogs with names containing the keyword
     */
    List<Catalog> findByCatalogNameContainingIgnoreCase(String keyword);
}
