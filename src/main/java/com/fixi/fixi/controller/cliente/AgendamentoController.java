package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Histórico de agendamentos de um cliente, incluindo avaliações.
     */
    @GetMapping("/clientes/{id}/agendamentos")
    public List<AgendamentoRespostaDTO> listarAgendamentosCliente(@PathVariable("id") Long clienteId) {
        return agendamentoService.listarPorCliente(clienteId);
    }

    /**
     * Cancela um agendamento de um cliente.
     */
    @DeleteMapping("/clientes/{clienteId}/agendamentos/{id}/cancelar")
    public ResponseEntity<Void> cancelarAgendamento(
            @PathVariable Long clienteId,
            @PathVariable Long id
    ) {
        agendamentoService.cancelarAgendamento(id, clienteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista agenda de um prestador em um intervalo de datas.
     */
    @GetMapping("/prestadores/{id}/agenda")
    public List<AgendamentoRespostaDTO> listarAgendaPrestador(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return agendamentoService.listarPorPrestador(id, from, to);
    }

    /**
     * Solicita um novo agendamento entre cliente e prestador.
     */
    @PostMapping("/prestadores/{id}/agendamentos")
    public AgendamentoRespostaDTO solicitarAgendamento(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestParam LocalDate data,
            @RequestParam Periodo periodo
    ) {
        return agendamentoService.solicitarAgendamento(id, clienteId, data, periodo);
    }
}
