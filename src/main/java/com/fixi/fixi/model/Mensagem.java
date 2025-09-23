package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id")
    private Conversa conversa;

    @Enumerated(EnumType.STRING)
    private Autor autor;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime dataEnvio = LocalDateTime.now();

}

