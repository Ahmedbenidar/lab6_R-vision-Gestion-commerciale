package org.example.service;

import java.io.Serializable;
import java.util.List;
import org.example.dao.GenericDAO;

/**
 * Couche metier entre le DAO et les controleurs.
 * Elle centralise les validations pour ne pas les repeter partout.
 */
public class CrudService<T, ID extends Serializable> {

    private final GenericDAO<T, ID> dao;

    public CrudService(Class<T> entityClass) {
        this.dao = new GenericDAO<T, ID>(entityClass);
    }

    protected GenericDAO<T, ID> dao() {
        return dao;
    }

    public T getById(ID id) {
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public Long count() {
        return dao.count();
    }

    public List<T> getPage(int numeroPage, int taillePage) {
        return dao.findPage(numeroPage, taillePage);
    }

    public void create(T entity) {
        verifier(entity);
        dao.create(entity);
    }

    public T update(T entity) {
        verifier(entity);
        return dao.update(entity);
    }

    public void delete(T entity) {
        verifier(entity);
        dao.delete(entity);
    }

    private void verifier(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entite ne doit pas etre nulle");
        }
    }
}
