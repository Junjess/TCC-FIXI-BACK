package com.fixi.fixi.service;

import com.fixi.fixi.model.Autor;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.model.Conversa;
import com.fixi.fixi.model.Mensagem;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.ConversaRepository;
import com.fixi.fixi.repository.MensagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {
    private final ConversaRepository conversaRepository;
    private final ClienteRepository clienteRepository;
    private final MensagemRepository mensagemRepository;
    private final GroqService groqService;

    public ChatService(ConversaRepository conversaRepository, MensagemRepository mensagemRepository, GroqService groqService,
                       ClienteRepository clienteRepository) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.groqService = groqService;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Conversa criarConversa(Long clienteId) {
        // regra de limite de 10 conversas
        List<Conversa> conversas = conversaRepository.findByCliente_IdOrderByDataInicioAsc(clienteId);
        if (conversas.size() >= 10) {
            // remove a mais antiga
            conversaRepository.delete(conversas.get(0));
        }
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clienteId));

        Conversa conversa = new Conversa();
        conversa.setTitulo("");
        conversa.setCliente(cliente);
        return conversaRepository.save(conversa);
    }

    @Transactional
    public Mensagem salvarMensagem(Long conversaId, Autor autor, String texto) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        // regra de limite de 5 mensagens
        long countMensagens = mensagemRepository.countByConversa_Id(conversaId);
        if (countMensagens >= 5) {
            // cria nova conversa e salva nela
            Conversa nova = criarConversa(conversa.getCliente().getId());
            conversa = nova;
        }

        Mensagem msg = new Mensagem();
        msg.setConversa(conversa);
        msg.setAutor(autor);
        msg.setTexto(texto);
        mensagemRepository.save(msg);

        // Só gera resposta se for cliente
        if (autor == Autor.CLIENTE) {
            String resposta = groqService.gerarResposta(texto);

            // Salvar resposta da IA
            Mensagem msgIA = new Mensagem();
            msgIA.setConversa(conversa);
            msgIA.setAutor(Autor.IA);
            msgIA.setTexto(resposta);
            mensagemRepository.save(msgIA);
        }

        return msg;
    }

    public List<Mensagem> buscarMensagens(Long conversaId) {
        return mensagemRepository.findByConversa_IdOrderByDataEnvioAsc(conversaId);
    }

    @Transactional
    public void excluirConversa(Long conversaId) {
        if (!conversaRepository.existsById(conversaId)) {
            throw new RuntimeException("Conversa não encontrada: " + conversaId);
        }

        // Exclui mensagens primeiro, se não tiver cascade
        mensagemRepository.deleteAllByConversa_Id(conversaId);

        // Depois exclui a conversa
        conversaRepository.deleteById(conversaId);
    }
}
