package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.request.AvaliacaoRequest;
import com.fixi.fixi.dto.response.AvaliacaoDirecionalResponseDTO;
import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.service.AvaliacaoPlataformaService;
import com.fixi.fixi.service.AvaliacaoService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final AvaliacaoPlataformaService avaliacaoPlataformaService;

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> salvar(@RequestBody AvaliacaoRequest dto) {
        return ResponseEntity.ok(avaliacaoService.salvarAvaliacao(dto));
    }

    // Cliente -> Prestador explícito
    @PostMapping("/cliente-para-prestador")
    public ResponseEntity<AvaliacaoResponseDTO> salvarClienteParaPrestador(@RequestBody AvaliacaoRequest dto) {
        return ResponseEntity.ok(avaliacaoService.salvarAvaliacaoClienteParaPrestador(dto));
    }

    // Prestador -> Cliente
    @PostMapping("/prestador-para-cliente")
    public ResponseEntity<AvaliacaoDirecionalResponseDTO> salvarPrestadorParaCliente(@RequestBody AvaliacaoRequest dto) {
        return ResponseEntity.ok(avaliacaoService.salvarAvaliacaoPrestadorParaCliente(dto));
    }

    //  Avaliações recebidas pelo prestador (clientes avaliaram prestador)
    @GetMapping("/prestador/{id}")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorPrestador(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoesPorPrestador(id));
    }

    // Avaliações recebidas pelo cliente (prestadores avaliaram cliente)
    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<AvaliacaoDirecionalResponseDTO>> listarPorCliente(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoesPorCliente(id));
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAvaliacoes(@PathVariable Long id) {
        try {
            List<AvaliacaoResponseDTO> avaliacoes = avaliacaoService.listarAvaliacoesPorPrestador(id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            //  Cabeçalho
            Paragraph titulo = new Paragraph("Relatório de Avaliações dos Clientes");
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            //  Introdução
            document.add(new Paragraph(
                    "Este relatório apresenta todas as avaliações recebidas diretamente dos clientes.\n" +
                            "Cada registro contém o nome do cliente, a nota atribuída e possíveis comentários adicionais.\n\n"
            ));

            //  Tabela
            PdfPTable table = new PdfPTable(3); // 3 colunas
            table.setWidthPercentage(100);
            table.addCell("Cliente");
            table.addCell("Nota");
            table.addCell("Comentário");

            for (AvaliacaoResponseDTO av : avaliacoes) {
                table.addCell(av.getClienteNome());
                table.addCell(String.valueOf(av.getNota()));
                table.addCell(av.getDescricao() != null ? av.getDescricao() : "-");
            }

            document.add(table);

            //  Conclusão
            document.add(new Paragraph("\nResumo:"));
            document.add(new Paragraph(
                    "Foram registradas " + avaliacoes.size() + " avaliações até o momento.\n" +
                            "Acompanhar o feedback dos clientes ajuda a identificar pontos fortes e oportunidades de melhoria."
            ));

            document.close();
            byte[] pdfBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=avaliacoes-clientes.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/plataforma/{prestadorId}")
    public ResponseEntity<List<AvaliacaoPlataforma>> listarAvaliacoesIA(@PathVariable Long prestadorId) {
        return ResponseEntity.ok(avaliacaoPlataformaService.listarAvaliacoesIA(prestadorId));
    }

    @GetMapping("/plataforma/{prestadorId}/desempenho")
    public ResponseEntity<Map<String, Object>> desempenhoGeral(@PathVariable Long prestadorId) {
        Map<String, Object> resultado = new HashMap<>();

        // avaliações da IA
        List<AvaliacaoPlataforma> ia = avaliacaoPlataformaService.listarAvaliacoesIA(prestadorId);
        resultado.put("avaliacoesPlataforma", ia);

        // avaliações dos clientes
        List<AvaliacaoResponseDTO> clientes = avaliacaoService.listarAvaliacoesPorPrestador(prestadorId);
        resultado.put("avaliacoesClientes", clientes);

        return ResponseEntity.ok(resultado);
    }
}
