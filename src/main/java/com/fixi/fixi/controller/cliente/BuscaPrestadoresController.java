package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.service.BuscaPrestadoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/prestadores")
public class BuscaPrestadoresController {

    private final BuscaPrestadoresService buscarService;

    @Autowired
    public BuscaPrestadoresController(BuscaPrestadoresService buscarService) {
        this.buscarService = buscarService;
    }

    // GET /prestadores/{idCliente}?q=xxx&categorias=1,2&cidade=Florianópolis&estado=SC
    @GetMapping("/{idCliente}")
    public List<BuscaPrestadoresRespostaDTO> listarPrestadores(
            @PathVariable Long idCliente,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "categorias") List<Long> categoriasIds,
            @RequestParam(required = false, name = "cidade") String cidade,
            @RequestParam(required = false, name = "estado") String estado
    ) {
        return buscarService.listarPrestadoresFiltrados(idCliente, q, categoriasIds, cidade, estado);
    }

    @GetMapping("/{id}/detalhes")
    public PrestadorDetalhesResponseDTO buscarPrestadorPorId(@PathVariable Long id) {
        return buscarService.buscarPrestadorPorId(id);
    }
}