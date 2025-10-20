package com.fixi.fixi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class ViaCepService {

    private static final String URL = "https://viacep.com.br/ws/{cep}/json/";

    public Map<String, Object> buscarCep(String cep) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject(URL, Map.class, cep);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Erro ao consultar o serviço ViaCEP.",
                    e
            );
        }
    }
}
