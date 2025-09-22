package com.fixi.fixi.controller;

import com.fixi.fixi.dto.request.LoginRequest;
import com.fixi.fixi.dto.request.PrestadorUpdateDTO;
import com.fixi.fixi.dto.request.RegistroClienteRequest;
import com.fixi.fixi.dto.request.RegistroPrestadorRequest;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.dto.response.UsuarioRespostaDTO;
import com.fixi.fixi.service.AutenticacaoService;
import com.fixi.fixi.service.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins="http://localhost:3000")

public class AutenticacaoController {
    @Autowired
    private AutenticacaoService autenticacaoService;
    @Autowired
    private PrestadorService prestadorService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {this.autenticacaoService = autenticacaoService;}

    @PostMapping("/auth/login/cliente")
    public UsuarioRespostaDTO loginCliente (@RequestBody LoginRequest loginRequest){
        return autenticacaoService.loginCliente(loginRequest);
    }

    @PostMapping("/auth/login/prestador")
    public PrestadorResponseDTO loginPrestador(@RequestBody LoginRequest loginRequest){
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
