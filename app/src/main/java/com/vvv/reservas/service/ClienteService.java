package com.vvv.reservas.service;

import com.vvv.reservas.dto.ClienteSignupForm;
import com.vvv.reservas.model.entity.Cliente;
import com.vvv.reservas.model.entity.Perfil;
import com.vvv.reservas.model.entity.Usuario;
import com.vvv.reservas.repository.ClienteRepository;
import com.vvv.reservas.repository.PerfilRepository;
import com.vvv.reservas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository,
                          UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente cadastrar(ClienteSignupForm form) {
        if (clienteRepository.findByCpf(form.getCpf()).isPresent()) {
            throw new RegraNegocioException("Já existe um cliente cadastrado com este CPF.");
        }
        if (usuarioRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
        }

        // 1. Criar o Usuário com perfil CLIENTE
        Perfil perfilCliente = perfilRepository.findByNome("CLIENTE")
                .orElseThrow(() -> new RegraNegocioException("Perfil CLIENTE não encontrado no sistema."));

        Usuario usuario = new Usuario();
        usuario.setEmail(form.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        usuario.setAtivo(true);
        usuario.getPerfis().add(perfilCliente);
        usuario = usuarioRepository.save(usuario);

        // 2. Criar o Cliente
        Cliente cliente = new Cliente();
        cliente.setCodigo(gerarCodigo());
        cliente.setNome(form.getNome());
        cliente.setCpf(form.getCpf());
        cliente.setEmail(form.getEmail());
        cliente.setTelefone(form.getTelefone());
        cliente.setUsuario(usuario);
        cliente.setAtivo(true);

        return clienteRepository.save(cliente);
    }

    private String gerarCodigo() {
        return "CLI" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }
}
