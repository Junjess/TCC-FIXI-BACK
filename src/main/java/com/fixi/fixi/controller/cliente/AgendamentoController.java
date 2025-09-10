package com.fixi.fixi.controller.cliente;


import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins="http://localhost:3000")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {this.agendamentoService = agendamentoService;}

    @GetMapping("/cliente/{id}/agendamentos")
    public List<AgendamentoRespostaDTO> listarAgendamentosCliente(@PathVariable("id") Long clienteId) {
        return agendamentoService.listarPorCliente(clienteId);
    }

    @DeleteMapping("/clientes/{clienteId}/agendamentos/{id}/cancelar")
    public ResponseEntity<Void> cancelarAgendamento(
            @PathVariable Long clienteId,
            @PathVariable Long id) {

        agendamentoService.cancelarAgendamento(id, clienteId);
        return ResponseEntity.noContent().build();
    }

}
