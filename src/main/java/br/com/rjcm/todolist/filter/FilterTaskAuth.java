package br.com.rjcm.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.rjcm.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {

    var serverletPath = request.getServletPath();

    if (serverletPath.equals("/tasks/")) {
      var authorization = request.getHeader("Authorization");

      var encodedAuth = authorization.substring("Basic".length()).trim();
      byte[] decodedAuth = Base64.getDecoder().decode(encodedAuth);
      var stringAuth = new String(decodedAuth);

      String[] credentials = stringAuth.split(":");
      String username = credentials[0];
      String password = credentials[1];

      // validar usuario
      var user = this.userRepository.findByUsername(username);

      if (user == null) {
        response.sendError(401);
      } else {
        var passwordVerify = BCrypt
          .verifyer()
          .verify(password.toCharArray(), user.getPassword());

        if (passwordVerify.verified) {
          filterChain.doFilter(request, response);
        } else {
          response.sendError(401);
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
