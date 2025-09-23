package com.fixi.fixi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column
    private LocalDateTime dataInicio = LocalDateTime.now();

    @Column
    private String titulo;

    @OneToMany(mappedBy = "conversa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagens;

}


