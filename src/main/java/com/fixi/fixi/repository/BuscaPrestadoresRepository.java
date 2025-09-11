package com.fixi.fixi.repository;

import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BuscaPrestadoresRepository extends JpaRepository<Prestador, Long> {

    @Query("""
        select p
        from Prestador p
        left join fetch p.categoria c
        where p.estado = :estado
          and ( :q is null
                or lower(p.nome) like lower(concat('%', :q, '%'))
                or lower(p.descricao) like lower(concat('%', :q, '%')) )
          and ( :categoriasVazia = true or c.id in :categorias )
        order by p.nome asc
    """)
    List<Prestador> findFiltradosSemMedia(
            @Param("estado") String estado,
            @Param("q") String q,
            @Param("categorias") List<Long> categorias,
            @Param("categoriasVazia") boolean categoriasVazia
    );
}
