package project.demo.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.demo.exception.CatalogException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Catalog;
import project.demo.repository.CatalogRepository;
import project.demo.service.ICatalogService;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the ICatalogService interface for managing Catalog entities
 */
@Service
public class CatalogServiceImpl implements ICatalogService {

    private final CatalogRepository catalogRepository;

    public CatalogServiceImpl(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog findById(Integer catalogId) {
        return catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("CATALOG_NOT_FOUND", 
                        "Catalog not found with ID: " + catalogId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Catalog> findAll() {
        return catalogRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Catalog> findAllActive() {
        return catalogRepository.findByStatus(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Catalog save(Catalog catalog) {
        // Validate catalog
        validateCatalog(catalog);
        
        // Check for duplicate name
        Optional<Catalog> existingCatalog = catalogRepository.findByCatalogName(catalog.getCatalogName());
        if (existingCatalog.isPresent()) {
            throw CatalogException.duplicateName(catalog.getCatalogName());
        }
        
        // Set default status if not provided
        if (catalog.getStatus() == null) {
            catalog.setStatus(true); // Active by default
        }
        
        return catalogRepository.save(catalog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Catalog update(Catalog catalog) {
        // Check if catalog exists
        if (catalog.getCatalogId() == null) {
            throw CatalogException.missingRequiredField("Catalog ID");
        }
        
        Catalog existingCatalog = findById(catalog.getCatalogId());
        
        // Validate catalog
        validateCatalog(catalog);
        
        // Check for duplicate name (excluding the current catalog)
        Optional<Catalog> duplicateCatalog = catalogRepository.findByCatalogName(catalog.getCatalogName());
        if (duplicateCatalog.isPresent() && !duplicateCatalog.get().getCatalogId().equals(catalog.getCatalogId())) {
            throw CatalogException.duplicateName(catalog.getCatalogName());
        }
        
        return catalogRepository.save(catalog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Integer catalogId) {
        // Check if catalog exists
        Catalog catalog = findById(catalogId);
        
        // Check if catalog has products
        if (catalog.getProducts() != null && !catalog.getProducts().isEmpty()) {
            throw CatalogException.cannotDelete(catalogId, "Catalog contains products");
        }
        
        catalogRepository.deleteById(catalogId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void activate(Integer catalogId) {
        // Check if catalog exists
        Catalog catalog = findById(catalogId);
        
        // Update status
        catalog.setStatus(true);
        catalogRepository.save(catalog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deactivate(Integer catalogId) {
        // Check if catalog exists
        Catalog catalog = findById(catalogId);
        
        // Update status
        catalog.setStatus(false);
        catalogRepository.save(catalog);
    }
    
    /**
     * Validates a catalog
     * 
     * @param catalog the catalog to validate
     * @throws CatalogException if validation fails
     */
    private void validateCatalog(Catalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog cannot be null");
        }
        
        if (catalog.getCatalogName() == null || catalog.getCatalogName().trim().isEmpty()) {
            throw CatalogException.missingRequiredField("Catalog name");
        }
    }
}
