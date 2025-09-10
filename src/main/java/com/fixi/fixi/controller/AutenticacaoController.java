package com.fixi.fixi.controller;

import com.fixi.fixi.dto.request.LoginRequest;
import com.fixi.fixi.dto.request.RegistroClienteRequest;
import com.fixi.fixi.dto.request.RegistroPrestadorRequest;
import com.fixi.fixi.dto.response.UsuarioRespostaDTO;
import com.fixi.fixi.service.AutenticacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins="http://localhost:3000")

public class AutenticacaoController {
    @Autowired
    private AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {this.autenticacaoService = autenticacaoService;}

    @PostMapping("/auth/login/cliente")
    public UsuarioRespostaDTO loginCliente (@RequestBody LoginRequest loginRequest){
        return autenticacaoService.loginCliente(loginRequest);
    }

    @PostMapping("/auth/login/prestador")
    public UsuarioRespostaDTO loginPrestador (@RequestBody LoginRequest loginRequest){
        return autenticacaoService.loginPrestador(loginRequest);
    }

    @PostMapping("/auth/cadastro/cliente")
    public UsuarioRespostaDTO cadastroCliente (@RequestBody RegistroClienteRequest registroClienteRequest){
        return autenticacaoService.cadastroCliente(registroClienteRequest);
    }

    @PostMapping("/auth/cadastro/prestador")
    public UsuarioRespostaDTO cadastroPrestador (@RequestBody RegistroPrestadorRequest registroPrestadorRequest){
        return autenticacaoService.cadastroPrestador(registroPrestadorRequest);
    }
}
