package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.service.BuscaPrestadoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestadores")
public class BuscaPrestadoresController {

    private final BuscaPrestadoresService buscarService;

    @Autowired
    public BuscaPrestadoresController(BuscaPrestadoresService buscarService) {
        this.buscarService = buscarService;
    }

    @GetMapping("/{idCliente}")
    public List<BuscaPrestadoresRespostaDTO> listarPrestadores(
            @PathVariable Long idCliente,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "categorias") String categoriasCsv,
            @RequestParam(required = false, name = "cidade") String cidade,
            @RequestParam(required = false, name = "estado") String estado
    ) {
        return buscarService.listarPrestadoresFiltrados(idCliente, q, categoriasCsv, cidade, estado);
    }

    @GetMapping("/{id}/detalhes")
    public PrestadorDetalhesResponseDTO buscarPrestadorPorId(@PathVariable Long id) {
        return buscarService.buscarPrestadorPorId(id);
    }
}