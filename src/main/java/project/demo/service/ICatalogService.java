package project.demo.service;

import project.demo.model.Catalog;

import java.util.List;

/**
 * Service interface for managing Catalog entities
 */
public interface ICatalogService {
    
    /**
     * Find a catalog by its ID
     * 
     * @param catalogId the ID of the catalog to find
     * @return the catalog if found
     */
    Catalog findById(Integer catalogId);
    
    /**
     * Get all catalogs
     * 
     * @return a list of all catalogs
     */
    List<Catalog> findAll();
    
    /**
     * Get all active catalogs
     * 
     * @return a list of all active catalogs
     */
    List<Catalog> findAllActive();
    
    /**
     * Save a new catalog
     * 
     * @param catalog the catalog to save
     * @return the saved catalog
     */
    Catalog save(Catalog catalog);
    
    /**
     * Update an existing catalog
     * 
     * @param catalog the catalog with updated information
     * @return the updated catalog
     */
    Catalog update(Catalog catalog);
    
    /**
     * Delete a catalog
     * 
     * @param catalogId the ID of the catalog to delete
     */
    void delete(Integer catalogId);
    
    /**
     * Activate a catalog
     * 
     * @param catalogId the ID of the catalog to activate
     */
    void activate(Integer catalogId);
    
    /**
     * Deactivate a catalog
     * 
     * @param catalogId the ID of the catalog to deactivate
     */
    void deactivate(Integer catalogId);
}
