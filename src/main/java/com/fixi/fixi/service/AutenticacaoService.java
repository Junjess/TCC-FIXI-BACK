package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.LoginRequest;
import com.fixi.fixi.dto.request.RegistroClienteRequest;
import com.fixi.fixi.dto.request.RegistroPrestadorRequest;
import com.fixi.fixi.dto.response.UsuarioRespostaDTO;
import com.fixi.fixi.model.Categoria;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticacaoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;


    public AutenticacaoService(ClienteRepository clienteRepository, PrestadorRepository prestadorRepository, CategoriaRepository categoriaRepository) {
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public UsuarioRespostaDTO loginCliente(LoginRequest loginRequest) {
        Cliente cliente = clienteRepository.findByEmail(loginRequest.getEmail());

        if (cliente == null) {
            throw new RuntimeException("Cliente não encontrado");
        }

        // Verifica senha usando BCrypt
        if (!BCrypt.checkpw(loginRequest.getSenha(), cliente.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        // Retorna DTO de resposta
        return new UsuarioRespostaDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail()

        );
    }

    public UsuarioRespostaDTO loginPrestador(LoginRequest loginRequest) {
        Prestador prestador = prestadorRepository.findByEmail(loginRequest.getEmail());

        if (prestador == null) {
            throw new RuntimeException("Prestador não encontrado");
        }

        // Verifica senha usando BCrypt
        if (!BCrypt.checkpw(loginRequest.getSenha(), prestador.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }

        // Retorna DTO de resposta
        return new UsuarioRespostaDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getEmail()
        );
    }

    public UsuarioRespostaDTO cadastroCliente(RegistroClienteRequest cadastroRequest) {
        Optional<Cliente> clienteExistente = Optional.ofNullable(clienteRepository.findByEmail(cadastroRequest.getEmail()));
        if (clienteExistente.isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        //Cria o cliente a partir do request
        Cliente cliente = new Cliente();
        cliente.setNome(cadastroRequest.getNome());
        cliente.setEmail(cadastroRequest.getEmail());

        // Criptografa a senha
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        cliente.setSenha(encoder.encode(cadastroRequest.getSenha()));

        cliente.setTelefone(cadastroRequest.getTelefone());
        cliente.setCidade(cadastroRequest.getCidade());
        cliente.setEstado(cadastroRequest.getEstado());

        //Salva no banco
        cliente = clienteRepository.save(cliente);

        //Retorna DTO de resposta
        return new UsuarioRespostaDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail()
        );
    }

    public UsuarioRespostaDTO cadastroPrestador(RegistroPrestadorRequest cadastroRequest) {
        Optional<Prestador> prestadorExistente = Optional.ofNullable(prestadorRepository.findByEmail(cadastroRequest.getEmail()));
        if (prestadorExistente.isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        //Cria o prestador a partir do request
        Prestador prestador = new Prestador();
        prestador.setNome(cadastroRequest.getNome());
        prestador.setEmail(cadastroRequest.getEmail());

        Categoria categoria = categoriaRepository.findByNome(cadastroRequest.getTipoServico());

        // Criptografa a senha
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        prestador.setSenha(encoder.encode(cadastroRequest.getSenha()));

        prestador.setTelefone(cadastroRequest.getTelefone());
        prestador.setCidade(cadastroRequest.getCidade());
        prestador.setEstado(cadastroRequest.getEstado());
        prestador.setCategoria(categoria);
        prestador.setDescricao(cadastroRequest.getDescricao());

        //Salva no banco
        prestador = prestadorRepository.save(prestador);

        //Retorna DTO de resposta
        return new UsuarioRespostaDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getEmail()
        );
    }
}