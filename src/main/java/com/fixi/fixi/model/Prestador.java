package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prestador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 255)
    private String foto;

    @Column(length = 40)
    private String telefone;

    @Column(length = 40)
    private String descricao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}