package com.fixi.fixi.controller.prestador;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.dto.response.AgendamentoSolicitacaoResponseDTO;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.service.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prestadores")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AgendamentoPrestadorController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoRepository agendamentoRepository;

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
            @RequestParam Long idCategoria,
            @RequestParam LocalDate data,
            @RequestParam Periodo periodo,
            @RequestParam String descricaoServico,
            @RequestParam(required = false) Double valorSugerido
    ) {
        return agendamentoService.solicitarAgendamento(
                id,
                clienteId,
                idCategoria,
                data,
                periodo,
                descricaoServico,
                valorSugerido
        );
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
    public ResponseEntity<Map<String, String>> cancelarPorPrestador(
            @PathVariable Long prestadorId,
            @PathVariable Long id
    ) {
        agendamentoService.cancelarAgendamentoPrestador(id, prestadorId);
        return ResponseEntity.ok(Map.of("message", "Agendamento cancelado e e-mail enviado com sucesso."));
    }

    /**
     * Prestador aceita um agendamento pendente
     */
    @PutMapping("/{prestadorId}/agendamentos/{id}/aceitar")
    public ResponseEntity<Map<String, String>> aceitarAgendamento(
            @PathVariable Long prestadorId,
            @PathVariable Long id
    ) {
        agendamentoService.aceitarAgendamento(prestadorId, id);
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Agendamento aceito com sucesso!");
        return ResponseEntity.ok(resposta);
    }

    /**
     * Prestador recusa um agendamento pendente
     */
    @PutMapping("/{prestadorId}/agendamentos/{id}/recusar")
    public ResponseEntity<Void> recusarAgendamento(
            @PathVariable Long prestadorId,
            @PathVariable Long id
    ) {
        agendamentoService.recusarAgendamento(prestadorId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/agendamentos/pendentes")
    public List<AgendamentoSolicitacaoResponseDTO> listarPendentes(@PathVariable Long id) {
        return agendamentoService.listarPendentesPorPrestador(id);
    }

    @GetMapping("/agendamentos/pendentes/{idPrestador}")
    public ResponseEntity<Map<String, Integer>> contarPendentes(@PathVariable Long idPrestador) {
        int quantidade = agendamentoRepository.contarPendentesPorPrestador(idPrestador);
        return ResponseEntity.ok(Map.of("quantidade", quantidade));
    }

    @PostMapping("/forcar-expirados")
    public void forcarExpirados() {
        agendamentoService.atualizarAgendamentosExpirados();
    }

}
