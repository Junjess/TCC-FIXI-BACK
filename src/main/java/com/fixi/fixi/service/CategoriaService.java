package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.CategoriaResponseDTO;
import com.fixi.fixi.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repo;

    public CategoriaService(CategoriaRepository repo) {
        this.repo = repo;
    }

    public List<CategoriaResponseDTO> listar() {
        return repo.findAllByOrderByNomeAsc()
                .stream()
                .map(CategoriaResponseDTO::from)
                .toList();
    }
}
