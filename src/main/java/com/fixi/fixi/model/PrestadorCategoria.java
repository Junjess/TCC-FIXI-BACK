package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prestador_categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestadorCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestador", nullable = false)
    private Prestador prestador;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(length = 255)
    private String descricao; // descrição personalizada do prestador para essa categoria
}

