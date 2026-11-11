package com.mvron.booking.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mvron.booking.infrastructure.persistence.AppUserEntity;
import com.mvron.booking.infrastructure.persistence.SpringDataAppUserRepository;

@Service 
public class AppUserDetailsService implements UserDetailsService {
    private final SpringDataAppUserRepository userRepository;

    public AppUserDetailsService(SpringDataAppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override 
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " +email));

        return User.withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(user.getRole())
                    .build();
    }


}
