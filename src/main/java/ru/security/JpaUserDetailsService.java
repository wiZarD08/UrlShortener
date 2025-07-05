package ru.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.model.User;
import ru.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) throw new UsernameNotFoundException("Username not found");
        else {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User
                    (user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        }
    }
}
