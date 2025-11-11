package com.isam.service;


import com.isam.model.Categoria;
import com.isam.model.Producto;
import com.isam.repository.CategoriaRepository;
import com.isam.repository.OfertaRepository;
import com.isam.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
