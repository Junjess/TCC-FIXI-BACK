package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.service.BuscaPrestadoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/prestadores")
public class BuscaPrestadoresController{
    @Autowired
    private final BuscaPrestadoresService buscarService;

    public BuscaPrestadoresController(BuscaPrestadoresService buscarService) {
        this.buscarService = buscarService;
    }

    // 🔹 GET /prestadores
    @GetMapping("/{idCliente}")
    public List<BuscaPrestadoresRespostaDTO> listarPrestadores(
            @PathVariable Long idCliente,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "categorias") List<Long> categoriasIds
    ) {
        return buscarService.listarPrestadoresFiltrados(idCliente, q, categoriasIds);
    }
}
