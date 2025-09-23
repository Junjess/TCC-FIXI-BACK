package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.ConversaDTO;
import com.fixi.fixi.dto.response.MensagemDTO;
import com.fixi.fixi.model.Autor;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.model.Conversa;
import com.fixi.fixi.model.Mensagem;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.ConversaRepository;
import com.fixi.fixi.repository.MensagemRepository;
import com.fixi.fixi.service.GroqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final ClienteRepository clienteRepository;
    private final GroqService groqService;

    // 🔹 Buscar as últimas 10 conversas do cliente
    @GetMapping("/conversas/{clienteId}")
    public List<ConversaDTO> listarConversas(@PathVariable Long clienteId) {
        return conversaRepository.findTop10ByCliente_IdOrderByDataInicioDesc(clienteId)
                .stream()
                .map(ConversaDTO::fromEntity)
                .toList();
    }

    // 🔹 Buscar mensagens de uma conversa
    @GetMapping("/mensagens/{conversaId}")
    public List<MensagemDTO> listarMensagens(@PathVariable Long conversaId) {
        return mensagemRepository.findByConversa_IdOrderByDataEnvioAsc(conversaId)
                .stream()
                .map(MensagemDTO::fromEntity)
                .toList();
    }

    // 🔹 Criar nova conversa
    @PostMapping("/conversa/{clienteId}")
    public ConversaDTO criarConversa(@PathVariable Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Conversa conversa = new Conversa();
        conversa.setCliente(cliente);
        conversa.setTitulo("Nova conversa");
        conversa.setDataInicio(LocalDateTime.now());

        return ConversaDTO.fromEntity(conversaRepository.save(conversa));
    }

    // 🔹 Salvar mensagem (cliente + IA)
    @PostMapping("/mensagem/{conversaId}")
    public Map<String, Object> salvarMensagem(@PathVariable Long conversaId, @RequestBody MensagemDTO dto) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        // 1️⃣ Salva mensagem do cliente
        Mensagem mensagemCliente = new Mensagem();
        mensagemCliente.setAutor(dto.getAutor());
        mensagemCliente.setTexto(dto.getTexto());
        mensagemCliente.setConversa(conversa);
        mensagemCliente.setDataEnvio(LocalDateTime.now());
        mensagemRepository.save(mensagemCliente);

        // 2️⃣ Chama GroqService
        String resposta = groqService.gerarResposta(dto.getTexto());

        // 3️⃣ Salva resposta da IA
        Mensagem mensagemIA = new Mensagem();
        mensagemIA.setAutor(Autor.IA);
        mensagemIA.setTexto(resposta);
        mensagemIA.setConversa(conversa);
        mensagemIA.setDataEnvio(LocalDateTime.now());
        mensagemRepository.save(mensagemIA);

        // 4️⃣ Retorna no formato esperado pelo front
        Map<String, Object> response = new HashMap<>();
        response.put("conversaId", conversa.getId());
        response.put("mensagem", MensagemDTO.fromEntity(mensagemCliente));
        response.put("respostaIA", MensagemDTO.fromEntity(mensagemIA));

        return response;
    }

    // 🔹 Atualizar título da conversa
    @PutMapping("/conversa/{conversaId}/titulo")
    public ConversaDTO atualizarTitulo(@PathVariable Long conversaId, @RequestBody Map<String, String> body) {
        String novoTitulo = body.get("titulo");

        if (novoTitulo == null || novoTitulo.trim().isEmpty()) {
            throw new RuntimeException("O título não pode ser vazio");
        }

        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        conversa.setTitulo(novoTitulo.trim());
        return ConversaDTO.fromEntity(conversaRepository.save(conversa));
    }
}
