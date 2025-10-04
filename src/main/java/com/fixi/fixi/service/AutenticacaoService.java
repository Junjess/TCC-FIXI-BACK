package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.LoginRequest;
import com.fixi.fixi.dto.request.RegistroClienteRequest;
import com.fixi.fixi.dto.request.RegistroPrestadorRequest;
import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.LoginResponseDTO;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.dto.response.UsuarioRespostaDTO;
import com.fixi.fixi.model.Categoria;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.model.PrestadorCategoria;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.PrestadorCategoriaRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import com.fixi.fixi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutenticacaoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ViaCepService viaCepService; // 🔹 serviço para buscar cidade/estado pelo CEP

    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository;
    @Autowired
    private PrestadorService prestadorService;

    public LoginResponseDTO<UsuarioRespostaDTO> loginCliente(LoginRequest loginRequest) {
        Cliente cliente = clienteRepository.findByEmail(loginRequest.getEmail());

        if (cliente == null) throw new RuntimeException("Cliente não encontrado");
        if (!BCrypt.checkpw(loginRequest.getSenha(), cliente.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        UsuarioRespostaDTO usuarioDTO = new UsuarioRespostaDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getFoto() != null ? Base64.getEncoder().encodeToString(cliente.getFoto()) : null
        );

        String token = JwtUtil.generateToken(String.valueOf(cliente.getId()), "CLIENTE");

        return new LoginResponseDTO<>(token, usuarioDTO);
    }

    public LoginResponseDTO<PrestadorResponseDTO> loginPrestador(LoginRequest loginRequest) {
        Prestador prestador = prestadorRepository.findByEmail(loginRequest.getEmail());

        if (prestador == null) throw new RuntimeException("Prestador não encontrado");
        if (!BCrypt.checkpw(loginRequest.getSenha(), prestador.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        PrestadorResponseDTO usuarioDTO = prestadorService.toDTO(prestador);


        String token = JwtUtil.generateToken(String.valueOf(prestador.getId()), "PRESTADOR");

        return new LoginResponseDTO<>(token, usuarioDTO);
    }

    public UsuarioRespostaDTO cadastroCliente(RegistroClienteRequest cadastroRequest) {
        Optional<Cliente> clienteExistente =
                Optional.ofNullable(clienteRepository.findByEmail(cadastroRequest.getEmail()));
        if (clienteExistente.isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Busca cidade e estado via ViaCEP
        Map<String, Object> dadosCep = viaCepService.buscarCep(cadastroRequest.getCep());
        if (dadosCep == null || dadosCep.containsKey("erro")) {
            throw new RuntimeException("CEP inválido ou não encontrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(cadastroRequest.getNome());
        cliente.setEmail(cadastroRequest.getEmail());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        cliente.setSenha(encoder.encode(cadastroRequest.getSenha()));

        cliente.setTelefone(cadastroRequest.getTelefone());
        cliente.setCep(cadastroRequest.getCep());
        cliente.setCidade((String) dadosCep.get("localidade"));
        cliente.setEstado((String) dadosCep.get("uf"));

        cliente = clienteRepository.save(cliente);

        return new UsuarioRespostaDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                Arrays.toString(cliente.getFoto())
        );
    }

    public UsuarioRespostaDTO cadastroPrestador(RegistroPrestadorRequest cadastroRequest) {
        Optional<Prestador> prestadorExistente =
                Optional.ofNullable(prestadorRepository.findByEmail(cadastroRequest.getEmail()));
        if (prestadorExistente.isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criar prestador
        Prestador prestador = new Prestador();
        prestador.setNome(cadastroRequest.getNome());
        prestador.setEmail(cadastroRequest.getEmail());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        prestador.setSenha(encoder.encode(cadastroRequest.getSenha()));

        prestador.setTelefone(cadastroRequest.getTelefone());
        prestador.setCep(cadastroRequest.getCep());

        Map<String, Object> dadosCep = viaCepService.buscarCep(cadastroRequest.getCep());
        prestador.setCidade((String) dadosCep.get("localidade"));
        prestador.setEstado((String) dadosCep.get("uf"));

        prestador = prestadorRepository.save(prestador);

        for (Long idCategoria : cadastroRequest.getCategoriasIds()) {
            Categoria categoria = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + idCategoria));

            PrestadorCategoria pc = new PrestadorCategoria();
            pc.setPrestador(prestador);
            pc.setCategoria(categoria);
            pc.setDescricao(null);

            prestadorCategoriaRepository.save(pc);
        }

        String fotoBase64 = prestador.getFoto() != null
                ? Base64.getEncoder().encodeToString(prestador.getFoto())
                : null;

        return new UsuarioRespostaDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getEmail(),
                fotoBase64
        );
    }
}
