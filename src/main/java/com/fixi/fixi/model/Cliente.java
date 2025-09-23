package com.fixi.fixi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Cliente {

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

    @Lob
    private byte[] foto;

    @Column(length = 40)
    private String telefone;
}
