package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

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

    @Column(length = 9, nullable = false)
    private String cep;

    @Column(length = 100, nullable = false)
    private String cidade;

    @Column(length = 2, nullable = false)
    private String estado;

    @Column(length = 255)
    private String foto;

    @Column(length = 40)
    private String telefone;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PrestadorCategoria> categorias;
}
