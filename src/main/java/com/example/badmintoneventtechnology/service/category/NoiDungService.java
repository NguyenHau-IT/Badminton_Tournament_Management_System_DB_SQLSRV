package com.example.badmintoneventtechnology.service.category;

import com.example.badmintoneventtechnology.model.category.NoiDung;
import com.example.badmintoneventtechnology.repository.category.NoiDungRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class NoiDungService {
    private final NoiDungRepository repository;

    public NoiDungService(NoiDungRepository repository) {
        this.repository = repository;
    }

    public NoiDung createNoiDung(NoiDung noiDung) throws SQLException {
        return repository.create(noiDung);
    }

    public List<NoiDung> getAllNoiDung() throws SQLException {
        return repository.findAll();
    }

    public Optional<NoiDung> getNoiDungById(Integer id) throws SQLException {
        return repository.findById(id);
    }

    public boolean updateNoiDung(NoiDung noiDung) throws SQLException {
        return repository.update(noiDung);
    }

    public boolean deleteNoiDung(Integer id) throws SQLException {
        return repository.delete(id);
    }
}
