package com.fixi.fixi.service;

import com.fixi.fixi.model.*;
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

    public GroqService(@Value("${GROQ_API_KEY}") String apiKey,
                       AgendamentoRepository agendamentoRepository,
                       CategoriaRepository categoriaRepository,
                       PrestadorCategoriaRepository prestadorCategoriaRepository) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.categoriaRepository = categoriaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.prestadorCategoriaRepository = prestadorCategoriaRepository;
    }


    enum Intencao {
        AJUDA_GERAL,
        EMERGENCIA,
        OUTRO
    }

    private Intencao detectarIntencao(String texto) {
        if (texto == null) return Intencao.OUTRO;
        String t = texto.toLowerCase();

        String[] termosEmergencia = {
                "incêndio", "fogo", "choque elétrico", "explosão", "vazamento de gás",
                "cheiro de gás", "desmaio", "urgente", "emergência"
        };
        for (String k : termosEmergencia) {
            if (t.contains(k)) return Intencao.EMERGENCIA;
        }

        String[] termosAjudaGeral = {
                "preciso de ajuda", "preciso de uma ajuda", "estou com um problema",
                "to com um problema", "tô com um problema", "estou tendo um problema",
                "pode me ajudar", "pode ajudar", "ajuda por favor", "ajuda pfv",
                "poderia me ajudar", "socorro", "help"
        };
        for (String k : termosAjudaGeral) {
            if (t.contains(k)) return Intencao.AJUDA_GERAL;
        }

        return Intencao.OUTRO;
    }

    private String respostaEmpaticaPadrao() {
        return "Olá! Sinto muito pelo que você está passando. " +
                "Poderia me enviar mais informações sobre o seu problema " +
                "para que eu possa avaliar e te orientar melhor?\n" +
                "Se puder, descreva:\n" +
                "1) O que aconteceu exatamente;\n" +
                "2) Onde está ocorrendo (ex.: cozinha/banheiro/quarto);\n" +
                "3) Há quanto tempo o problema começou.";
    }

    private String respostaEmergencia() {
        return "⚠️ Situação potencialmente perigosa. Por favor, priorize sua segurança.\n" +
                "• Se houver incêndio, vazamento de gás ou risco de choque elétrico, ligue imediatamente para os serviços de emergência (190/193) e evacue o local.\n" +
                "• Quando estiver seguro, me conte mais detalhes para eu orientar um profissional adequado.";
    }

    private boolean temPistasDomesticas(String texto) {
        if (texto == null) return false;
        String t = texto.toLowerCase();
        String[] pistas = {
                "chuveiro", "tomada", "disjuntor", "lâmpada", "fio", "curto",
                "cano", "vazamento", "ralo", "torneira", "esgoto",
                "parede", "piso", "azulejo", "infiltração",
                "grama", "jardim", "quintal",
                "limpeza", "faxina",
                "roteador", "wi-fi", "wifi", "internet", "computador", "pc",
                "unha", "manicure",
                "aula", "professor", "reforço"
        };
        for (String p : pistas) {
            if (t.contains(p)) return true;
        }
        return false;
    }

    public String gerarResposta(String mensagemCliente) {
        try {
            Intencao intencao = detectarIntencao(mensagemCliente);
            if (intencao == Intencao.EMERGENCIA) {
                return respostaEmergencia();
            }
            if (intencao == Intencao.AJUDA_GERAL) {
                return respostaEmpaticaPadrao();
            }

            String categoriaNome = classificarCategoria(mensagemCliente).trim();

            if ("FORA_DO_ESCOPO".equalsIgnoreCase(categoriaNome)) {
                if (temPistasDomesticas(mensagemCliente)) {
                    return respostaEmpaticaPadrao();
                }
                return "Humm, isso parece estar fora dos serviços que a FIXI oferece no momento. " +
                        "Se for algo como elétrica, hidráulica, limpeza, TI doméstico, jardinagem, entre outros, " +
                        "me conte um pouco mais para eu identificar a categoria certa e te indicar um profissional.";
            }

            Categoria categoria = categoriaRepository.findByNome(categoriaNome);
            if (categoria == null) {
                return "❌ Não encontrei a categoria **" + categoriaNome + "** na plataforma FIXI.";
            }

            List<PrestadorCategoria> prestadorCategorias =
                    prestadorCategoriaRepository.findByCategoriaId(categoria.getId());

            if (prestadorCategorias.isEmpty()) {
                return "❌ No momento não há prestadores cadastrados na categoria **" + categoriaNome +
                        "**. Por favor, tente novamente mais tarde ou escolha outro serviço disponível na plataforma FIXI.";
            }

            Map<Prestador, Double> medias = new HashMap<>();
            for (PrestadorCategoria pc : prestadorCategorias) {
                Prestador prestador = pc.getPrestador();

                List<Agendamento> agendamentos = agendamentoRepository.findHistoricoByClienteId(prestador.getId());

                double somaNotas = 0.0;
                int totalNotas = 0;

                for (Agendamento ag : agendamentos) {
                    if (ag.getAvaliacoes() == null) continue;

                    for (Avaliacao av : ag.getAvaliacoes()) {
                        if (av.getTipo() == AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR && av.getNota() != null) {
                            somaNotas += av.getNota();
                            totalNotas++;
                        }
                    }
                }

                double media = totalNotas > 0 ? somaNotas / totalNotas : 0.0;
                medias.put(prestador, media);
            }

            Prestador melhorPrestador = medias.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(prestadorCategorias.get(0).getPrestador());

            double melhorMedia = medias.getOrDefault(melhorPrestador, 0.0);

            String listaPrestadores = prestadorCategorias.stream()
                    .map(pc -> String.format("%s (%s) → [Ver perfil](https://tcc-fixi-front.vercel.app/prestador/%d)",
                            pc.getPrestador().getNome(),
                            categoriaNome,
                            pc.getPrestador().getId()
                    ))
                    .collect(Collectors.joining("\n"));

            String prompt = """
                Você é uma IA de suporte da FIXI (serviços domésticos).
                Responda SOMENTE sobre serviços residenciais. Não recuse respostas que estejam no escopo.

                Profissionais disponíveis (use só estes para recomendar):
                %s

                Mensagem do cliente: "%s"

                POLÍTICA DE NÃO INVENTAR (obrigatório):
                - Não invente categorias, links, diagnósticos definitivos, marcas/modelos, disponibilidade, prazos ou valores.
                - Se faltar informação, peça de forma objetiva (ex.: CEP/bairro/cidade, ambiente, marca/modelo, fotos).
                - Se pedirem preço/orçamento, explique que o valor depende da avaliação do prestador e sugira abrir um agendamento pelo perfil. Não forneça qualquer estimativa.

                ESTILO (obrigatório):
                - Português do Brasil, cordial e direto.
                - Frases curtas, sem jargões (se necessário, explique em 1 linha).
                - Traga no máximo **3 dicas práticas**, numeradas.
                - Se a descrição estiver incompleta, inclua **2–3 perguntas de esclarecimento** no final.
                - Use **negrito** com moderação; não exagere no Markdown.

                REGRAS DE SEGURANÇA (valem sempre; se alguma dica for potencialmente perigosa para leigos, NÃO inclua):
                - Eletricista: nunca oriente mexer em fiação energizada; peça para desligar o disjuntor antes de qualquer inspeção visual. Não sugerir “pontes” ou gambiarras.
                - Encanador: para vazamento grande, fechar o registro geral. Não misturar produtos químicos de limpeza.
                - Pedreiro: evitar perfurar paredes sem verificar tubulação/eletricidade; usar EPI básico (óculos/luvas) em orientações.
                - Jardineiro: cuidado com ferramentas cortantes e escadas; não operar equipamentos sem experiência.
                - Cozinheiro Privado: atenção a alergias/intolerâncias; higiene e manipulação segura de alimentos (temperaturas/armazenamento).
                - Babá: priorizar segurança da criança; não dar medicamentos sem autorização; manter contatos de emergência.
                - Motorista: sempre usar cinto, respeitar leis; não solicitar documentos sensíveis (CNH/placa completa) no chat.
                - Dog Walker: usar guia adequada; atenção a cães reativos/agressivos; hidratação e horário seguro (calor).
                - Faxineiro: nunca misturar água sanitária (hipoclorito) com amônia/ácidos; ventilar bem o ambiente.
                - Professor Particular: não realizar provas/trabalhos pelo aluno; orientar aprendizado.
                - Manicure/Pedicure: higiene e esterilização; cuidado com corte de cutículas; perguntar sobre alergias.
                - Assistente Virtual: não solicitar senhas/dados sensíveis; orientar organização segura (sem expor PII).
                - Fotógrafo: respeitar privacidade/consentimento; checar autorização para fotos de terceiros/menores.
                - Consultor de TI: nunca pedir senhas; orientar verificação de cabos/reinício de modem/roteador/backups; cuidado com links suspeitos.

                Diretrizes de resposta:
                - Comece com empatia breve se houver frustração/dúvida.
                - Explique em 1–2 frases o que pode estar acontecendo (sem cravar diagnóstico).
                - Liste **3 dicas práticas e seguras** (numeradas).
                - Ao final, recomende o **melhor avaliado** da categoria, com nome e link do perfil (Markdown).

                Formato:
                🚧 Breve explicação.
                1) Dica prática 1
                2) Dica prática 2
                3) Dica prática 3

                ⭐ Recomendação: %s — média %.1f ⭐ — [Ver perfil](https://tcc-fixi-front.vercel.app/prestador/%d)

                Observações finais:
                - Nunca forneça preços/prazos. Oriente o cliente a solicitar orçamento pelo perfil do profissional.
                - Se faltar contexto essencial, peça CEP/bairro/cidade, ambiente (cozinha/banheiro/quarto), marca/modelo (quando aplicável) e fotos apenas se ajudarem de verdade.
                """.formatted(
                    listaPrestadores,
                    mensagemCliente,
                    melhorPrestador.getNome(), melhorMedia, melhorPrestador.getId()
            );

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "Você é um assistente de serviços domésticos da plataforma FIXI. Siga exatamente as políticas e o estilo definidos pelo usuário."),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.35,
                    "max_tokens", 600
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
                Você classifica pedidos de serviços domésticos em UMA das categorias abaixo (responda o nome EXATO da lista):
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

                Regras:
                1) Se a mensagem for claramente sobre um desses serviços, responda apenas o nome exato da categoria.
                2) Se a mensagem for AMBIGUA mas cita algo do lar (ex.: “chuveiro não liga”), escolha a categoria mais provável.
                3) Se for genérica tipo “preciso de ajuda” sem pistas, NÃO classifique aqui (isso já foi tratado antes).
                4) Se não tiver relação com nenhum serviço, responda exatamente: FORA_DO_ESCOPO.

                Exemplos:
                - "Tomadas estão dando choque" -> Eletricista
                - "Cano estourou no banheiro" -> Encanador
                - "Parede com infiltração" -> Pedreiro
                - "Grama do quintal alta" -> Jardineiro
                - "Preciso de alguém para cozinhar almoço" -> Cozinheiro Privado
                - "Cuidar do meu filho à tarde" -> Babá
                - "Levar minha família ao aeroporto" -> Motorista
                - "Passear com meu cachorro" -> Dog Walker
                - "Limpar apartamento amanhã" -> Faxineiro
                - "Aulas de matemática" -> Professor Particular
                - "Fazer unhas e pés" -> Manicure/Pedicure
                - "Organizar planilhas e e-mails" -> Assistente Virtual
                - "Fotos de aniversário" -> Fotógrafo
                - "Wi-Fi cai/roteador/configuração de PC" -> Consultor de TI

                Pedido do cliente: "%s"
                """.formatted(mensagemCliente);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "Você é um classificador objetivo e NUNCA inventa categorias."),
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
