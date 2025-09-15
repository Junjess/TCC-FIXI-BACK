package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:3000")
public class AgendamentoClienteController {

    private final AgendamentoService agendamentoService;

    public AgendamentoClienteController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Histórico de agendamentos de um cliente
     */
    @GetMapping("/{id}/agendamentos")
    public List<AgendamentoRespostaDTO> listarAgendamentosCliente(@PathVariable("id") Long clienteId) {
        return agendamentoService.listarPorCliente(clienteId);
    }

    /**
     * Cliente cancela um agendamento
     */
    @PutMapping("/{clienteId}/agendamentos/{id}/cancelar")
    public ResponseEntity<Void> cancelarAgendamentoPorCliente(
            @PathVariable Long clienteId,
            @PathVariable Long id
    ) {
        agendamentoService.cancelarAgendamentoCliente(id, clienteId);
        return ResponseEntity.noContent().build();
    }
}
