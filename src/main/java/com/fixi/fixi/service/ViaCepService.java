package com.fixi.fixi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ViaCepService {

    private static final String URL = "https://viacep.com.br/ws/{cep}/json/";

    public Map<String, Object> buscarCep(String cep) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject(URL, Map.class, cep);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar ViaCEP", e);
        }
    }
}
