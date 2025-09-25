package com.fixi.fixi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "avaliacao_plataforma")
public class AvaliacaoPlataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    @JsonIgnore
    private Prestador prestador;


    // Critérios
    @Column(name = "tempo_plataforma", nullable = false)
    private Double tempoPlataforma; // nota normalizada de 0 a 5

    @Column(name = "taxa_aceitacao", nullable = false)
    private Double taxaAceitacao; // nota normalizada de 0 a 5

    @Column(name = "taxa_cancelamento", nullable = false)
    private Double taxaCancelamento; // nota normalizada de 0 a 5

    @Column(name = "avaliacao_ia", nullable = false)
    private Double avaliacaoIa; // nota 0-5 da IA

    // Nota final calculada
    @Column(name = "nota_final", nullable = false)
    private Double notaFinal;

    // Período de referência (avaliação mensal)
    @Column(name = "periodo_referencia", nullable = false)
    private LocalDate periodoReferencia;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao = LocalDateTime.now();

    // Método para calcular a nota final com pesos
    public void calcularNotaFinal() {
        if (tempoPlataforma != null && taxaAceitacao != null
                && taxaCancelamento != null && avaliacaoIa != null) {

            this.notaFinal =
                    (tempoPlataforma * 2 +
                            taxaAceitacao * 3 +
                            taxaCancelamento * 3 +
                            avaliacaoIa * 2) / 10.0;
        }
    }
}
