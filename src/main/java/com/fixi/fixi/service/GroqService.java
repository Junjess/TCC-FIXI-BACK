package com.fixi.fixi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GroqService {

    private final WebClient webClient;

    public GroqService(@Value("${GROQ_API_KEY}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String gerarResposta(String mensagemCliente) {
        try {
            String prompt = """
                    Você é uma IA de suporte para um aplicativo de serviços domésticos.
                    Só pode responder perguntas relacionadas a:
                    - ajuda para realizar tarefas de casa, caso haja algum problema,
                    - qual profissional chamar para determinada situação em casa.
                    
                    Se a pergunta do usuário não estiver nesse escopo, responda:
                    "❌ Desculpe, não posso responder a esse tipo de pergunta. O chat é apenas para recomendações relacionadas a serviços domésticos."
                    
                    Pergunta do cliente: %s
                    """.formatted(mensagemCliente);

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "Você é um assistente de serviços domésticos."),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.7
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

            var choices = (java.util.List<Map<String, Object>>) response.get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Erro ao consultar a IA.";
        }
    }
}
