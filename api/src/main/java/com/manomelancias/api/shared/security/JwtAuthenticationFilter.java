package com.manomelancias.api.shared.security;

import com.manomelancias.api.usuario.Usuario;
import com.manomelancias.api.usuario.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Autentica a requisição a partir de um Bearer token JWT. Reconsulta o
 * usuário no banco a cada request (em vez de confiar só nas claims) para que
 * inativar/excluir um usuário revogue o acesso dele imediatamente, e não só
 * quando o token expirar — e para que uma troca de papel valha na hora.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.validarEExtrairClaims(token);
                UUID usuarioId = UUID.fromString(claims.getSubject());
                Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

                if (usuario != null && Boolean.TRUE.equals(usuario.getAtivo())) {
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name()));
                    var authentication = new UsernamePasswordAuthenticationToken(usuarioId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
