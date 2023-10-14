package cc.hidev.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import cc.hidev.todolist.user.UserModel;
import cc.hidev.todolist.user.UserRepositoryInterface;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepositoryInterface userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/") == true) {
            this.tasksPath(request, response, filterChain);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private void tasksPath(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String authorization = request.getHeader("Authorization");
        String basic = authorization.substring("Basic".length()).trim();
        String[] credentials = new String(Base64.getDecoder().decode(basic)).split(":");
        String username = credentials[0];
        String password = credentials[1];

        UserModel user = this.userRepository.findByUsername(username);
        if (user == null) {
            response.sendError(401);
        } else {
            Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());
            if (!result.verified) {
                response.sendError(401);
            }

            request.setAttribute("userId", user.getId());
        }

        filterChain.doFilter(request, response);
    }

}
