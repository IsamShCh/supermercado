package com.isam.service;

import com.isam.dto.producto.CrearProductoDto;
import com.isam.model.Categoria;
import com.isam.model.Producto;
import com.isam.repository.CategoriaRepository;
import com.isam.repository.OfertaRepository;
import com.isam.repository.ProductoRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CatalogoService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private OfertaRepository ofertaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    public CatalogoService() {
    }

    /**
     * Creates a product from DTO, handling category lookup and entity creation
     * This method contains the business logic for product creation
     */
    @Transactional
    public Producto crearProducto(CrearProductoDto dto) {
        System.out.println("DEBUG: Service creating product from DTO: " + dto);
        System.out.println("DEBUG: DTO PoliticaRotacion: " + dto.getPoliticaRotacion());
        System.out.println("DEBUG: DTO UnidadMedida: " + dto.getUnidadMedida());

        // Create the base product entity
        Producto producto = new Producto();
        
        // Map basic properties
        producto.setSku(dto.getSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecioVenta(dto.getPrecioVenta());
        producto.setCaduca(dto.getCaduca());
        producto.setEsGranel(dto.getEsGranel());
        producto.setPoliticaRotacion(dto.getPoliticaRotacion());
        producto.setUnidadMedida(dto.getUnidadMedida());
        producto.setEan(dto.getEan());
        producto.setPlu(dto.getPlu());
        
        System.out.println("DEBUG: After mapping - Entity PoliticaRotacion: " + producto.getPoliticaRotacion());
        System.out.println("DEBUG: After mapping - Entity UnidadMedida: " + producto.getUnidadMedida());
        
        // Handle etiquetas
        if (dto.getEtiquetas() != null && !dto.getEtiquetas().isEmpty()) {
            String etiquetasString = String.join(",", dto.getEtiquetas());
            producto.setEtiquetas(etiquetasString);
            System.out.println("DEBUG: Etiquetas set: " + etiquetasString);
        }
        
        // Handle categoria lookup - this is the Service layer responsibility
        if (dto.getIdCategoria() != null && dto.getIdCategoria() > 0) {
            System.out.println("DEBUG: Looking up category with ID: " + dto.getIdCategoria());
            Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.getIdCategoria());
            if (categoriaOpt.isPresent()) {
                producto.setCategoria(categoriaOpt.get());
                System.out.println("DEBUG: Category found and set: " + categoriaOpt.get().getNombreCategoria());
            } else {
                System.err.println("ERROR: Category not found for ID: " + dto.getIdCategoria());
                throw new RuntimeException("Category not found with ID: " + dto.getIdCategoria());
            }
        } else {
            System.out.println("DEBUG: No category ID provided");
        }

        // Save the product
        System.out.println("DEBUG: Before saving - Entity PoliticaRotacion: " + producto.getPoliticaRotacion());
        System.out.println("DEBUG: Before saving - Entity UnidadMedida: " + producto.getUnidadMedida());
        
        Producto savedProduct = productoRepository.save(producto);
        
        // Ensure categoria is loaded before returning (prevent LazyInitializationException)
        if (savedProduct.getCategoria() != null) {
            System.out.println("DEBUG: Is categoria proxy initialized before access: " + Hibernate.isInitialized(savedProduct.getCategoria()));
            // Access categoria properties to force loading within the transaction
            String categoriaNombre = savedProduct.getCategoria().getNombreCategoria();
            System.out.println("DEBUG: Category eagerly loaded: " + categoriaNombre);
        }
        
        System.out.println("DEBUG: After saving - Entity PoliticaRotacion: " + savedProduct.getPoliticaRotacion());
        System.out.println("DEBUG: After saving - Entity UnidadMedida: " + savedProduct.getUnidadMedida());
        System.out.println("DEBUG: Product saved successfully with category: " +
                          (savedProduct.getCategoria() != null ? savedProduct.getCategoria().getNombreCategoria() : "null"));

        return savedProduct;
    }

    /**
     * Legacy method - kept for backward compatibility
     * Consider deprecating this in favor of the DTO-based method
     */
    public Producto crearProducto(Producto productoEntity) {
        // Guardar el producto
        productoRepository.save(productoEntity);
        return productoEntity;
    }

    public Producto consultarProducto(String sku){
        Producto productoEntity = productoRepository.findBySku(sku).get(); // TODO - hacer que tire una excepción
        return productoEntity;
    }

    public Categoria crearCategoria(Categoria categoria){
        Categoria categoriaRespEntity = categoriaRepository.save(categoria);
        return categoriaRespEntity;
    }
}
