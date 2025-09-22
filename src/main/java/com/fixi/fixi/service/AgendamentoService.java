package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.dto.response.AgendamentoSolicitacaoResponseDTO;
import com.fixi.fixi.model.*;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final CategoriaRepository categoriaRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@fixi.com}")
    private String from;

    /**
     * Lista agendamentos de um cliente.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findHistoricoByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void cancelarAgendamentoCliente(Long agendamentoId, Long clienteId) {
        cancelarAgendamento(agendamentoId, clienteId, false);

        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId).orElse(null);

        if (cliente != null && agendamento != null) {
            Prestador prestador = agendamento.getPrestador();
            if (prestador != null) {
                System.out.println("📧 Cliente cancelou → enviar e-mail para o prestador");
                enviarEmailPrestador(prestador, agendamento);
            }
        } else {
            System.out.println("⚠️ Não enviou e-mail porque cliente ou agendamento veio null");
        }
    }

    @Transactional
    public void cancelarAgendamentoPrestador(Long agendamentoId, Long prestadorId) {
        cancelarAgendamento(agendamentoId, prestadorId, true);

        Prestador prestador = prestadorRepository.findById(prestadorId).orElse(null);
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId).orElse(null);

        if (prestador != null && agendamento != null) {
            Cliente cliente = agendamento.getCliente();
            if (cliente != null) {
                System.out.println("📧 Prestador cancelou → enviar e-mail para o cliente");
                enviarEmailCliente(cliente, agendamento);
            }
        } else {
            System.out.println("⚠️ Não enviou e-mail porque prestador ou agendamento veio null");
        }
    }


    /**
     * Método genérico que aplica as regras de cancelamento.
     */
    private void cancelarAgendamento(Long agendamentoId, Long usuarioId, boolean isPrestador) {
        Agendamento ag = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (isPrestador && !ag.getPrestador().getId().equals(usuarioId)) {
            throw new RuntimeException("Prestador não autorizado a cancelar este agendamento");
        }
        if (!isPrestador && !ag.getCliente().getId().equals(usuarioId)) {
            throw new RuntimeException("Cliente não autorizado a cancelar este agendamento");
        }

        ag.setStatus(StatusAgendamento.CANCELADO);
        ag.setCanceladoPor(isPrestador ? "PRESTADOR" : "CLIENTE");

        agendamentoRepository.save(ag);
    }

    public void enviarEmailCliente(Cliente cliente, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, o prestador <b>%s</b> cancelou seu agendamento.</p>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                </div>
                """.formatted(cliente.getNome(), agendamento.getPrestador().getNome(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("Agendamento cancelado pelo prestador");
            helper.setText(html, true);
            mailSender.send(msg);
            System.out.println("✅ Email enviado para cliente");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void enviarEmailPrestador(Prestador prestador, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, o cliente <b>%s</b> cancelou o agendamento.</p>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                </div>
                """.formatted(prestador.getNome(), agendamento.getCliente().getNome(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(prestador.getEmail());
            helper.setSubject("Agendamento cancelado pelo cliente");
            helper.setText(html, true);
            mailSender.send(msg);
            System.out.println("✅ Email enviado para prestador");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


    /**
     * Lista a agenda de um prestador em um intervalo de datas.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarPorPrestador(Long prestadorId, LocalDate from, LocalDate to) {
        return agendamentoRepository.findByPrestadorIdAndDataAgendamentoBetween(prestadorId, from, to)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Solicita um novo agendamento (cliente → prestador).
     */
    @Transactional
    public AgendamentoRespostaDTO solicitarAgendamento(
            Long prestadorId,
            Long clienteId,
            String nomeCategoria,
            LocalDate data,
            Periodo periodo,
            String descricaoServico,
            Double valorSugerido
    ) {
        var prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        var categoria = categoriaRepository.findByNome(nomeCategoria);
        if (categoria == null) {
            throw new RuntimeException("Categoria não encontrada");
        }

        // verifica se o prestador possui a categoria
        boolean prestadorPossuiCategoria = prestador.getCategorias().stream()
                .anyMatch(pc -> pc.getCategoria().getNome().equalsIgnoreCase(nomeCategoria));
        if (!prestadorPossuiCategoria) {
            throw new RuntimeException("Prestador não atende a categoria selecionada.");
        }

        // verifica disponibilidade
        boolean prestadorOcupado = agendamentoRepository.existsByPrestadorIdAndDataAgendamentoAndPeriodoAndStatusIn(
                prestadorId,
                data,
                periodo,
                List.of(StatusAgendamento.PENDENTE, StatusAgendamento.ACEITO)
        );
        if (prestadorOcupado) {
            throw new RuntimeException("Prestador já possui agendamento nesse período.");
        }

        Agendamento ag = new Agendamento();
        ag.setPrestador(prestador);
        ag.setCliente(cliente);
        ag.setCategoria(categoria);
        ag.setDataAgendamento(data);
        ag.setPeriodo(periodo);
        ag.setStatus(StatusAgendamento.PENDENTE);
        ag.setDataSolicitacao(LocalDateTime.now());

        // 🔹 novos campos
        ag.setDescricaoServico(descricaoServico);
        ag.setValorSugerido(valorSugerido);

        var salvo = agendamentoRepository.save(ag);

        return toDTO(salvo);
    }

    /**
     * Lista apenas agendamentos aceitos de um prestador.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarAceitosPorPrestador(Long prestadorId) {
        return agendamentoRepository.findAceitosByPrestadorId(prestadorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converte Agendamento → DTO
     */
    private AgendamentoRespostaDTO toDTO(Agendamento agendamento) {
        AgendamentoRespostaDTO dto = new AgendamentoRespostaDTO();
        dto.setIdAgendamento(agendamento.getId());

        // Prestador
        dto.setIdPrestador(agendamento.getPrestador().getId());
        dto.setNomePrestador(agendamento.getPrestador().getNome());
        dto.setTelefonePrestador(agendamento.getPrestador().getTelefone());
        dto.setCidadePrestador(agendamento.getPrestador().getCidade());
        dto.setEstadoPrestador(agendamento.getPrestador().getEstado());

        if (agendamento.getPrestador().getFoto() != null) {
            dto.setFotoPrestador(Base64.getEncoder().encodeToString(agendamento.getPrestador().getFoto()));
        }

        // 🔹 Categoria específica do agendamento
        dto.setCategoriaAgendamento(
                agendamento.getCategoria() != null ? agendamento.getCategoria().getNome() : null
        );

        // Cliente
        dto.setIdCliente(agendamento.getCliente().getId());
        dto.setNomeCliente(agendamento.getCliente().getNome());
        dto.setTelefoneCliente(agendamento.getCliente().getTelefone());
        dto.setCidadeCliente(agendamento.getCliente().getCidade());
        dto.setEstadoCliente(agendamento.getCliente().getEstado());

        if (agendamento.getCliente().getFoto() != null) {
            dto.setFotoCliente(Base64.getEncoder().encodeToString(agendamento.getCliente().getFoto()));
        }

        // Dados do agendamento
        dto.setData(agendamento.getDataAgendamento());
        dto.setPeriodo(agendamento.getPeriodo().toString());
        dto.setStatusAgendamento(agendamento.getStatus().name());
        dto.setAvaliado(agendamento.getAvaliacao() != null);
        dto.setNota(agendamento.getAvaliacao() != null ? agendamento.getAvaliacao().getNota() : null);
        dto.setDescricaoAvaliacao(agendamento.getAvaliacao() != null ? agendamento.getAvaliacao().getDescricao() : null);
        dto.setCanceladoPor(agendamento.getCanceladoPor());

        // 🔹 Novos campos
        dto.setDescricaoServico(agendamento.getDescricaoServico());
        dto.setValorSugerido(agendamento.getValorSugerido());

        return dto;
    }

    /**
     * Prestador aceita agendamento.
     */
    @Transactional
    public void aceitarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new RuntimeException("Prestador não autorizado para esse agendamento.");
        }

        agendamento.setStatus(StatusAgendamento.ACEITO);
        agendamentoRepository.save(agendamento);
    }

    /**
     * Prestador recusa agendamento.
     */
    @Transactional
    public void recusarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new RuntimeException("Prestador não autorizado para esse agendamento.");
        }

        agendamento.setStatus(StatusAgendamento.NEGADO); // 🔹 corrigido
        agendamentoRepository.save(agendamento);
    }

    /**
     * Lista apenas agendamentos pendentes de um prestador (dados do cliente).
     */
    @Transactional
    public List<AgendamentoSolicitacaoResponseDTO> listarPendentesPorPrestador(Long prestadorId) {
        List<Agendamento> agendamentos = agendamentoRepository.findPendentesByPrestadorId(prestadorId);

        return agendamentos.stream().map(a ->
                new AgendamentoSolicitacaoResponseDTO(
                        a.getId(),
                        a.getCliente().getId(),
                        a.getCliente().getNome(),
                        a.getCliente().getTelefone(),
                        a.getCliente().getFoto() != null ? Base64.getEncoder().encodeToString(a.getCliente().getFoto()) : null,
                        a.getDataAgendamento(),
                        a.getPeriodo(),
                        a.getStatus(),
                        a.getDescricaoServico(),
                        a.getValorSugerido()
                )
        ).toList();
    }
}
