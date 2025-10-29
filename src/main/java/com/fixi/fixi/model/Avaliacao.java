package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "avaliacao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agendamento_tipo",
                columnNames = {"agendamento_id", "tipo"}
        ),
        indexes = {
                @Index(name = "idx_avaliacao_agendamento", columnList = "agendamento_id"),
                @Index(name = "idx_avaliacao_tipo", columnList = "tipo")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(nullable = false)
    private Double nota; // 0 a 5 (ex.: 4.5)

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AvaliacaoTipo tipo;
}
