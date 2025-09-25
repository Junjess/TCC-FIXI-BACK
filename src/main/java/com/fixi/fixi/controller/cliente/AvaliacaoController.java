package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.request.AvaliacaoRequest;
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

    @GetMapping("/prestador/{id}")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorPrestador(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoesPorPrestador(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAvaliacoes(@PathVariable Long id) {
        try {
            List<AvaliacaoResponseDTO> avaliacoes = avaliacaoService.listarAvaliacoesPorPrestador(id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            document.add(new Paragraph("Relatório de Avaliações dos Clientes\n\n"));
            // Tabela
            PdfPTable table = new PdfPTable(3);
            // 3 colunas
            table.addCell("Cliente");
            table.addCell("Nota");
            table.addCell("Comentário");

            for (AvaliacaoResponseDTO av : avaliacoes) {
                table.addCell(av.getClienteNome());
                table.addCell(String.valueOf(av.getNota()));
                table.addCell(av.getDescricao() != null ? av.getDescricao() : "");
            }

            document.add(table);
            document.close();
            byte[] pdfBytes = baos.toByteArray();

            return ResponseEntity.ok().header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=avaliacoes-clientes.pdf")
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
