package project.demo.service.implement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import project.demo.exception.CatalogException;
import project.demo.exception.ResourceNotFoundException;
import project.demo.model.Catalog;
import project.demo.model.Product;
import project.demo.repository.CatalogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CatalogServiceImplTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Catalog testCatalog;
    private List<Product> products;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test catalog
        testCatalog = new Catalog();
        testCatalog.setCatalogId(1);
        testCatalog.setCatalogName("Test Catalog");
        testCatalog.setCatalogDescription("Test Description");
        testCatalog.setStatus(true);

        // Setup empty products list
        products = new ArrayList<>();
        testCatalog.setProducts(products);
    }

    @Test
    void findById_ExistingCatalog_ReturnsCatalog() {
        // Arrange
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));

        // Act
        Catalog result = catalogService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCatalog.getCatalogId(), result.getCatalogId());
        verify(catalogRepository, times(1)).findById(1);
    }

    @Test
    void findById_NonExistingCatalog_ThrowsException() {
        // Arrange
        when(catalogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> catalogService.findById(999));
        verify(catalogRepository, times(1)).findById(999);
    }

    @Test
    void findAll_ReturnsCatalogs() {
        // Arrange
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);
        when(catalogRepository.findAll()).thenReturn(catalogs);

        // Act
        List<Catalog> result = catalogService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCatalog.getCatalogId(), result.get(0).getCatalogId());
        verify(catalogRepository, times(1)).findAll();
    }

    @Test
    void findAllActive_ReturnsActiveCatalogs() {
        // Arrange
        List<Catalog> catalogs = new ArrayList<>();
        catalogs.add(testCatalog);
        when(catalogRepository.findByStatus(true)).thenReturn(catalogs);

        // Act
        List<Catalog> result = catalogService.findAllActive();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCatalog.getCatalogId(), result.get(0).getCatalogId());
        verify(catalogRepository, times(1)).findByStatus(true);
    }

    @Test
    void save_ValidCatalog_SavesCatalog() {
        // Arrange
        Catalog newCatalog = new Catalog();
        newCatalog.setCatalogName("New Catalog");
        newCatalog.setCatalogDescription("New Description");
        
        when(catalogRepository.findByCatalogName("New Catalog")).thenReturn(Optional.empty());
        when(catalogRepository.save(any(Catalog.class))).thenReturn(newCatalog);

        // Act
        Catalog result = catalogService.save(newCatalog);

        // Assert
        assertNotNull(result);
        assertEquals(newCatalog.getCatalogName(), result.getCatalogName());
        assertTrue(result.getStatus()); // Default status should be true
        verify(catalogRepository, times(1)).findByCatalogName("New Catalog");
        verify(catalogRepository, times(1)).save(newCatalog);
    }

    @Test
    void save_NullCatalogName_ThrowsException() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setCatalogDescription("Test Description");

        // Act & Assert
        assertThrows(CatalogException.class, () -> catalogService.save(catalog));
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void save_DuplicateCatalogName_ThrowsException() {
        // Arrange
        Catalog newCatalog = new Catalog();
        newCatalog.setCatalogName("Test Catalog");
        newCatalog.setCatalogDescription("New Description");
        
        when(catalogRepository.findByCatalogName("Test Catalog")).thenReturn(Optional.of(testCatalog));

        // Act & Assert
        assertThrows(CatalogException.class, () -> catalogService.save(newCatalog));
        verify(catalogRepository, times(1)).findByCatalogName("Test Catalog");
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void update_ValidCatalog_UpdatesCatalog() {
        // Arrange
        Catalog updatedCatalog = new Catalog();
        updatedCatalog.setCatalogId(1);
        updatedCatalog.setCatalogName("Updated Catalog");
        updatedCatalog.setCatalogDescription("Updated Description");
        updatedCatalog.setStatus(true);
        
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.findByCatalogName("Updated Catalog")).thenReturn(Optional.empty());
        when(catalogRepository.save(any(Catalog.class))).thenReturn(updatedCatalog);

        // Act
        Catalog result = catalogService.update(updatedCatalog);

        // Assert
        assertNotNull(result);
        assertEquals(updatedCatalog.getCatalogName(), result.getCatalogName());
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).findByCatalogName("Updated Catalog");
        verify(catalogRepository, times(1)).save(updatedCatalog);
    }

    @Test
    void update_NonExistingCatalog_ThrowsException() {
        // Arrange
        Catalog catalog = new Catalog();
        catalog.setCatalogId(999);
        catalog.setCatalogName("Updated Catalog");
        
        when(catalogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> catalogService.update(catalog));
        verify(catalogRepository, times(1)).findById(999);
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void update_DuplicateCatalogName_ThrowsException() {
        // Arrange
        Catalog anotherCatalog = new Catalog();
        anotherCatalog.setCatalogId(2);
        anotherCatalog.setCatalogName("Another Catalog");
        
        Catalog updatedCatalog = new Catalog();
        updatedCatalog.setCatalogId(1);
        updatedCatalog.setCatalogName("Another Catalog");
        
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.findByCatalogName("Another Catalog")).thenReturn(Optional.of(anotherCatalog));

        // Act & Assert
        assertThrows(CatalogException.class, () -> catalogService.update(updatedCatalog));
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).findByCatalogName("Another Catalog");
        verify(catalogRepository, never()).save(any(Catalog.class));
    }

    @Test
    void delete_ExistingCatalog_DeletesCatalog() {
        // Arrange
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));
        doNothing().when(catalogRepository).deleteById(1);

        // Act
        catalogService.delete(1);

        // Assert
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_NonExistingCatalog_ThrowsException() {
        // Arrange
        when(catalogRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> catalogService.delete(999));
        verify(catalogRepository, times(1)).findById(999);
        verify(catalogRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_CatalogWithProducts_ThrowsException() {
        // Arrange
        // Add a product to the catalog
        Product product = new Product();
        product.setProductId(1);
        products.add(product);
        testCatalog.setProducts(products);
        
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));

        // Act & Assert
        assertThrows(CatalogException.class, () -> catalogService.delete(1));
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, never()).deleteById(anyInt());
    }

    @Test
    void activate_ExistingCatalog_ActivatesCatalog() {
        // Arrange
        testCatalog.setStatus(false);
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(testCatalog);

        // Act
        catalogService.activate(1);

        // Assert
        assertTrue(testCatalog.getStatus());
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).save(testCatalog);
    }

    @Test
    void deactivate_ExistingCatalog_DeactivatesCatalog() {
        // Arrange
        when(catalogRepository.findById(1)).thenReturn(Optional.of(testCatalog));
        when(catalogRepository.save(any(Catalog.class))).thenReturn(testCatalog);

        // Act
        catalogService.deactivate(1);

        // Assert
        assertFalse(testCatalog.getStatus());
        verify(catalogRepository, times(1)).findById(1);
        verify(catalogRepository, times(1)).save(testCatalog);
    }
}
