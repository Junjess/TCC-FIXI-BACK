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
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .map(this::toDTOComParidade)
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
                //enviarEmailPrestador(prestador, agendamento);
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível enviar o e-mail: cliente ou agendamento estão nulos."
            );
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
                //enviarEmailCliente(cliente, agendamento);
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível enviar o e-mail: prestador ou agendamento estão nulos."
            );
        }
    }


    /**
     * Método genérico que aplica as regras de cancelamento.
     */
    private void cancelarAgendamento(Long agendamentoId, Long usuarioId, boolean isPrestador) {
        Agendamento ag = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado")
                );

        if (isPrestador && !ag.getPrestador().getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Prestador não autorizado a cancelar este agendamento."
            );
        }

        if (!isPrestador && !ag.getCliente().getId().equals(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cliente não autorizado a cancelar este agendamento."
            );
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
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail.",
                    e
            );
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
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail.",
                    e
            );
        }
    }

    /**
     * Lista a agenda de um prestador em um intervalo de datas.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarPorPrestador(Long prestadorId, LocalDate from, LocalDate to) {
        return agendamentoRepository.findByPrestadorIdAndDataAgendamentoBetween(prestadorId, from, to)
                .stream()
                .map(this::toDTOComParidade)
                .toList();
    }

    /**
     * Solicita um novo agendamento (cliente → prestador).
     */
    @Transactional
    public AgendamentoRespostaDTO solicitarAgendamento(
            Long prestadorId,
            Long clienteId,
            Long idCategoria,
            LocalDate data,
            Periodo periodo,
            String descricaoServico,
            Double valorSugerido
    ) {
        var prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Prestador não encontrado")
                );

        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado")
                );

        var categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada")
                );


        // verifica se o prestador possui a categoria
        boolean prestadorPossuiCategoria = prestador.getCategorias().stream()
                .anyMatch(pc -> pc.getCategoria().getId().equals(idCategoria));
        if (!prestadorPossuiCategoria) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prestador não atende à categoria selecionada."
            );
        }

        // verifica disponibilidade
        boolean prestadorOcupado = agendamentoRepository.existsByPrestadorIdAndDataAgendamentoAndPeriodoAndStatusIn(
                prestadorId,
                data,
                periodo,
                List.of(StatusAgendamento.PENDENTE, StatusAgendamento.ACEITO)
        );
        if (prestadorOcupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Prestador já possui agendamento nesse período."
            );
        }

        Agendamento ag = new Agendamento();
        ag.setPrestador(prestador);
        ag.setCliente(cliente);
        ag.setCategoria(categoria);
        ag.setDataAgendamento(data);
        ag.setPeriodo(periodo);
        ag.setStatus(StatusAgendamento.PENDENTE);
        ag.setDataSolicitacao(LocalDateTime.now());

        //novos campos
        ag.setDescricaoServico(descricaoServico);
        ag.setValorSugerido(valorSugerido);

        var salvo = agendamentoRepository.save(ag);

        return toDTOComParidade(salvo);
    }

    /**
     * Lista apenas agendamentos aceitos de um prestador.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarAceitosPorPrestador(Long prestadorId) {
        return agendamentoRepository.findAceitosByPrestadorId(prestadorId)
                .stream()
                .map(this::toDTOComParidade)
                .toList();
    }

    private AgendamentoRespostaDTO toDTOComParidade(Agendamento ag) {
        var dto = new AgendamentoRespostaDTO();
        dto.setIdAgendamento(ag.getId());

        // Prestador
        var p = ag.getPrestador();
        dto.setIdPrestador(p.getId());
        dto.setNomePrestador(p.getNome());
        dto.setTelefonePrestador(p.getTelefone());
        dto.setCidadePrestador(p.getCidade());
        dto.setEstadoPrestador(p.getEstado());
        if (p.getFoto() != null) {
            dto.setFotoPrestador(Base64.getEncoder().encodeToString(p.getFoto()));
        }

        // Categoria do agendamento (se houver)
        dto.setCategoriaAgendamento(ag.getCategoria() != null ? ag.getCategoria().getNome() : null);

        // Cliente
        var c = ag.getCliente();
        dto.setIdCliente(c.getId());
        dto.setNomeCliente(c.getNome());
        dto.setTelefoneCliente(c.getTelefone());
        dto.setCidadeCliente(c.getCidade());
        dto.setEstadoCliente(c.getEstado());
        if (c.getFoto() != null) {
            dto.setFotoCliente(Base64.getEncoder().encodeToString(c.getFoto()));
        }
        dto.setFotoTipoCliente(c.getFotoTipo());

        // Agendamento
        dto.setData(ag.getDataAgendamento());
        dto.setPeriodo(ag.getPeriodo().name());
        dto.setStatusAgendamento(ag.getStatus().name());
        dto.setCanceladoPor(ag.getCanceladoPor());

        // novos
        dto.setDescricaoServico(ag.getDescricaoServico());
        dto.setValorSugerido(ag.getValorSugerido());

        // ===== regras de avaliação =====
        var clienteParaPrestador = ag.getAvaliacoes().stream()
                .filter(v -> v.getTipo() == AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR)
                .findFirst().orElse(null);

        var prestadorParaCliente = ag.getAvaliacoes().stream()
                .filter(v -> v.getTipo() == AvaliacaoTipo.PRESTADOR_AVALIA_CLIENTE)
                .findFirst().orElse(null);

        boolean clienteFez = (clienteParaPrestador != null);
        boolean prestadorFez = (prestadorParaCliente != null);
        boolean ambas = clienteFez && prestadorFez;

        dto.setAvaliacaoClienteFeita(clienteFez);
        dto.setAvaliacaoPrestadorFeita(prestadorFez);

        if (ambas) {
            dto.setNotaAvaliacaoPrestador(clienteParaPrestador.getNota());
            dto.setComentarioAvaliacaoPrestador(clienteParaPrestador.getDescricao());
            dto.setNotaAvaliacaoCliente(prestadorParaCliente.getNota());
            dto.setComentarioAvaliacaoCliente(prestadorParaCliente.getDescricao());
        } else {
            dto.setNotaAvaliacaoPrestador(null);
            dto.setComentarioAvaliacaoPrestador(null);
            dto.setNotaAvaliacaoCliente(null);
            dto.setComentarioAvaliacaoCliente(null);
        }

        return dto;
    }

    /**
     * Prestador aceita agendamento.
     */
    @Transactional
    public void aceitarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado")
                );

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Prestador não autorizado para esse agendamento."
            );
        }

        agendamento.setStatus(StatusAgendamento.ACEITO);
        agendamentoRepository.save(agendamento);

        Cliente cliente = agendamento.getCliente();
        Prestador prestador = agendamento.getPrestador();

        //enviarEmailAceiteCliente(cliente, prestador, agendamento);
        //enviarEmailAceitePrestador(prestador, cliente, agendamento);
    }

    /**
     * Prestador recusa agendamento.
     */
    @Transactional
    public void recusarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado")
                );

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Prestador não autorizado para esse agendamento."
            );
        }

        agendamento.setStatus(StatusAgendamento.NEGADO);
        agendamentoRepository.save(agendamento);

        //enviarEmailAgendamentoRecusadoCliente(agendamento.getCliente(), agendamento);
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

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void atualizarAgendamentosExpirados() {
        LocalDateTime hoje = LocalDateTime.now();

        List<Agendamento> agendamentos = agendamentoRepository.findAll();

        for (Agendamento ag : agendamentos) {
            LocalDate dataAg = ag.getDataAgendamento();
            LocalDateTime limite;

            if (ag.getPeriodo() == Periodo.MATUTINO) {
                limite = dataAg.atTime(12, 0); // meio-dia
            } else { // VESPERTINO
                limite = dataAg.atTime(18, 0); // 18h
            }
            if (ag.getStatus() == StatusAgendamento.PENDENTE && hoje.isAfter(limite)) {
                ag.setStatus(StatusAgendamento.EXPIRADO);
                agendamentoRepository.save(ag);

                //enviarEmailExpiradoCliente(ag.getCliente(), ag);
                //enviarEmailExpiradoPrestador(ag.getPrestador(), ag);
            }
        }
    }

    public void enviarEmailAgendamentoRecusadoCliente(Cliente cliente, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, sua solicitação de agendamento com o prestador <b>%s</b> foi recusada.</p>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                  <p>Você pode tentar realizar um novo agendamento na plataforma.</p>
                </div>
                """.formatted(cliente.getNome(), agendamento.getPrestador().getNome(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("Agendamento Recusado");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail para o cliente.", e
            );
        }
    }

    public void enviarEmailExpiradoCliente(Cliente cliente, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, seu agendamento com o prestador <b>%s</b> expirou, pois não foi confirmado a tempo.</p>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                  <p>Você pode tentar realizar um novo agendamento na plataforma.</p>
                </div>
                """.formatted(cliente.getNome(), agendamento.getPrestador().getNome(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("Agendamento expirado");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail para o cliente.", e
            );
        }
    }

    public void enviarEmailExpiradoPrestador(Prestador prestador, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, o agendamento solicitado pelo cliente <b>%s</b> expirou, pois não foi aceito dentro do prazo.</p>
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
            helper.setSubject("Agendamento expirado");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail para o prestador.", e
            );
        }
    }

    public void enviarEmailAceiteCliente(Cliente cliente, Prestador prestador, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, seu agendamento com o prestador <b>%s</b> foi <span style="color:green">ACEITO</span>!</p>
                  <p><b>Entre em contato com o prestador para combinar os detalhes:</b></p>
                  <p>📞 Telefone: <b>%s</b></p>
                  <p>📧 E-mail: <b>%s</b></p>
                  <br/>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                  <p> Descrição do serviço: <b>%s</b> </p>
                  <p><i>A comunicação deve ser feita diretamente com o prestador.</i></p>
                </div>
                """.formatted(cliente.getNome(), prestador.getNome(), prestador.getTelefone(), prestador.getEmail(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo(), agendamento.getDescricaoServico());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(cliente.getEmail());
            helper.setSubject("Agendamento aceito pelo prestador");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail de aceite para o cliente.", e
            );
        }
    }

    public void enviarEmailAceitePrestador(Prestador prestador, Cliente cliente, Agendamento agendamento) {
        String html = """
                <div style="font-family:sans-serif">
                  <p>Olá <b>%s</b>, você aceitou o agendamento do cliente <b>%s</b>.</p>
                  <p><b>Entre em contato com o cliente para combinar os detalhes:</b></p>
                  <p>📞 Telefone: <b>%s</b></p>
                  <p>📧 E-mail: <b>%s</b></p>
                  <br/>
                  <p>Detalhes do agendamento:</p>
                  <p> Data: <b>%s</b> </p>
                  <p> Período: <b>%s</b> </p>
                  <p> Descrição do serviço: <b>%s</b> </p>
                  <p><i>A comunicação deve ser feita diretamente com o cliente.</i></p>
                </div>
                """.formatted(prestador.getNome(), cliente.getNome(), cliente.getTelefone(), cliente.getEmail(),
                agendamento.getDataAgendamento(), agendamento.getPeriodo(), agendamento.getDescricaoServico());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(prestador.getEmail());
            helper.setSubject("Confirmação de aceite do agendamento");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Falha ao enviar e-mail de aceite para o prestador.", e
            );
        }
    }

}
