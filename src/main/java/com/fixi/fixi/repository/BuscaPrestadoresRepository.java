package com.fixi.fixi.repository;

import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BuscaPrestadoresRepository extends JpaRepository<Prestador, Long> {

    @Query("""
        select distinct p
        from Prestador p
        join fetch p.categorias pc
        join fetch pc.categoria c
        where (:estado is null or lower(p.estado) = lower(:estado))
          and (:cidade is null or lower(p.cidade) = lower(:cidade))
          and ( :q is null
                or lower(p.nome) like lower(concat('%', :q, '%'))
                or lower(pc.descricao) like lower(concat('%', :q, '%'))
                or lower(c.nome) like lower(concat('%', :q, '%')) )
          and ( :categoriasVazia = true or c.id in :categorias )
        order by p.nome asc
        """)
    List<Prestador> findFiltradosSemMedia(
            @Param("cidade") String cidade,
            @Param("estado") String estado,
            @Param("q") String q,
            @Param("categorias") List<Long> categorias,
            @Param("categoriasVazia") boolean categoriasVazia
    );

    @Query("""
    select distinct p
    from Prestador p
    left join fetch p.categorias pc
    left join fetch pc.categoria c
    where p.id = :id
    """)
    java.util.Optional<Prestador> findByIdFetchCategorias(@Param("id") Long id);
}
