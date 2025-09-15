package com.fixi.fixi.controller.prestador;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prestadores")
@CrossOrigin(origins = "http://localhost:3000")
public class AgendamentoPrestadorController {

    private final AgendamentoService agendamentoService;

    public AgendamentoPrestadorController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Lista agenda de um prestador em um intervalo de datas.
     */
    @GetMapping("/{id}/agenda")
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
    @PostMapping("/{id}/agendamentos")
    public AgendamentoRespostaDTO solicitarAgendamento(
            @PathVariable Long id,
            @RequestParam Long clienteId,
            @RequestParam LocalDate data,
            @RequestParam Periodo periodo
    ) {
        return agendamentoService.solicitarAgendamento(id, clienteId, data, periodo);
    }

    /**
     * Lista apenas agendamentos ACEITOS de um prestador.
     * Exemplo: GET /prestadores/5/agendamentos/aceitos
     */
    @GetMapping("/{id}/agendamentos/aceitos")
    public List<AgendamentoRespostaDTO> listarAceitos(@PathVariable Long id) {
        return agendamentoService.listarAceitosPorPrestador(id);
    }

    /**
     * Prestador cancela um agendamento
     */
    @PutMapping("/{prestadorId}/agendamentos/{id}/cancelar")
    public ResponseEntity<Void> cancelarPorPrestador(
            @PathVariable Long prestadorId,
            @PathVariable Long id
    ) {
        agendamentoService.cancelarAgendamentoPrestador(id, prestadorId);
        return ResponseEntity.noContent().build();
    }
}
