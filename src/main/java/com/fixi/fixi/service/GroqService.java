package com.fixi.fixi.service;

import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Categoria;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.model.PrestadorCategoria;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.PrestadorCategoriaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroqService {

    private final WebClient webClient;
    private final CategoriaRepository categoriaRepository;
    private final PrestadorCategoriaRepository prestadorCategoriaRepository;
    private final AgendamentoRepository agendamentoRepository;

    public GroqService(@Value("${GROQ_API_KEY}") String apiKey , AgendamentoRepository agendamentoRepository, CategoriaRepository categoriaRepository, PrestadorCategoriaRepository prestadorCategoriaRepository) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.categoriaRepository = categoriaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.prestadorCategoriaRepository = prestadorCategoriaRepository;
    }

    public String gerarResposta(String mensagemCliente) {
        try {
            // Classifica a categoria
            String categoriaNome = classificarCategoria(mensagemCliente).trim();

            // Fora do escopo
            if ("FORA_DO_ESCOPO".equalsIgnoreCase(categoriaNome)) {
                return "❌ Não posso fornecer informações que não sejam relacionadas aos serviços da plataforma FIXI. "
                        + "Se precisar de ajuda com serviços domésticos, estou à disposição para recomendar um profissional qualificado.";
            }

            // Busca a categoria
            Categoria categoria = categoriaRepository.findByNome(categoriaNome);
            if (categoria == null) {
                return "❌ Não encontrei a categoria **" + categoriaNome + "** na plataforma FIXI.";
            }

            // Busca os prestadores da categoria
            List<PrestadorCategoria> prestadorCategorias =
                    prestadorCategoriaRepository.findByCategoriaId(categoria.getId());

            if (prestadorCategorias.isEmpty()) {
                return "❌ No momento não há prestadores cadastrados na categoria **" + categoriaNome +
                        "**. Por favor, tente novamente mais tarde ou escolha outro serviço disponível na plataforma FIXI.";
            }

            //Calcula a média de avaliações de cada prestador
            Map<Prestador, Double> medias = new HashMap<>();

            for (PrestadorCategoria pc : prestadorCategorias) {
                Prestador prestador = pc.getPrestador();

                // busca agendamentos do prestador que possuem avaliação
                List<Agendamento> agendamentos = agendamentoRepository.findHistoricoByClienteId(prestador.getId());

                double somaNotas = 0.0;
                int totalNotas = 0;

                for (Agendamento ag : agendamentos) {
                    if (ag.getAvaliacao() != null && ag.getAvaliacao().getNota() != null) {
                        somaNotas += ag.getAvaliacao().getNota();
                        totalNotas++;
                    }
                }

                double media = totalNotas > 0 ? somaNotas / totalNotas : 0.0;
                medias.put(prestador, media);
            }

            //Seleciona o prestador com maior média
            Prestador melhorPrestador = medias.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(prestadorCategorias.get(0).getPrestador());

            double melhorMedia = medias.getOrDefault(melhorPrestador, 0.0);

            // Monta lista de prestadores
            String listaPrestadores = prestadorCategorias.stream()
                    .map(pc -> String.format("%s (%s) → [Ver perfil](http://localhost:3000/prestador/%d)",
                            pc.getPrestador().getNome(),
                            categoriaNome,
                            pc.getPrestador().getId()
                    ))
                    .collect(Collectors.joining("\n"));

            // Destaque do melhor avaliado
            String destaque = """
                ⭐ **Melhor avaliado nesta categoria:** %s  
                Média de avaliações: %.1f ⭐  
                [Ver perfil](http://localhost:3000/prestador/%d)
                """.formatted(melhorPrestador.getNome(), melhorMedia, melhorPrestador.getId());

            // Prompt final
            String prompt = """
                Você é uma IA de suporte para o aplicativo de serviços domésticos FIXI.
                Só pode responder perguntas relacionadas a serviços da plataforma.
                
                Profissionais disponíveis no sistema (use apenas estes para recomendar):
                %s
                
                O cliente perguntou: "%s"
                
                Monte uma resposta em português, seguindo este formato:
                🚧 Texto introdutório explicando o problema.
                🚧 Liste **3 dicas práticas** que o cliente pode tentar resolver ou mitigar o problema.
                🚧 No final, recomende o **melhor avaliado** da categoria, incluindo nome, especialidade e link do perfil.
                
                ⚠️ Sempre inclua o link no formato Markdown: [Ver perfil](URL).
                Use Markdown para formatar em **negrito** e listas numeradas.
                
                %s
                """.formatted(listaPrestadores, mensagemCliente, destaque);

            // Chamada à IA
            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "Você é um assistente de serviços domésticos da plataforma FIXI."),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.4
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("choices") == null) {
                return "⚠️ Não foi possível obter resposta da IA no momento.";
            }

            var choices = (List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Erro ao consultar a IA.";
        }
    }

    private String classificarCategoria(String mensagemCliente) {
        String promptCategoria = """
                Você é uma IA que classifica pedidos de serviços domésticos.
                O cliente vai descrever um problema. Sua tarefa é escolher a categoria mais adequada
                entre esta lista (responda exatamente como está escrito):
                
                - Eletricista
                - Encanador
                - Pedreiro
                - Jardineiro
                - Cozinheiro Privado
                - Babá
                - Motorista
                - Dog Walker
                - Faxineiro
                - Professor Particular
                - Manicure/Pedicure
                - Assistente Virtual
                - Fotógrafo
                - Consultor de TI
                
                ⚠️ Se o pedido do cliente não tiver relação com nenhum desses serviços, responda exatamente:
                FORA_DO_ESCOPO
                
                Pedido do cliente: "%s"
                """.formatted(mensagemCliente);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "Você é um classificador de categorias."),
                        Map.of("role", "user", "content", promptCategoria)
                },
                "temperature", 0.0
        );

        Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("choices") == null) {
            return "FORA_DO_ESCOPO";
        }

        var choices = (java.util.List<Map<String, Object>>) response.get("choices");
        var message = (Map<String, Object>) choices.get(0).get("message");
        return ((String) message.get("content")).trim();
    }

    public Double avaliarComentariosPrestador(List<String> comentarios) {
        if (comentarios == null || comentarios.isEmpty()) {
            return 0.0;
        }

        String prompt = """
        Você é um avaliador imparcial.
        Analise os seguintes comentários de clientes sobre um prestador:

        %s

        Com base neles, atribua uma nota única de **0 a 5** que represente a satisfação média geral.
        Retorne **apenas o número**, sem texto extra.
        """.formatted(String.join("\n", comentarios));

        try {
            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "Você é um avaliador de qualidade de prestadores."),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.0
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("choices") == null) {
                return 0.0;
            }

            var choices = (java.util.List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            String content = ((String) message.get("content")).trim();

            // Extrai número (0–5) da resposta
            return parseNota(content);

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private Double parseNota(String resposta) {
        try {
            String apenasNumero = resposta.replaceAll("[^0-9.]", "");
            double valor = Double.parseDouble(apenasNumero);
            return Math.max(0.0, Math.min(5.0, valor)); // garante entre 0 e 5
        } catch (Exception e) {
            return 0.0;
        }
    }

}
