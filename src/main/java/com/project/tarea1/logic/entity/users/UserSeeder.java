package com.project.tarea1.logic.entity.users;

import com.project.tarea1.logic.entity.rols.Role;
import com.project.tarea1.logic.entity.rols.RoleRepository;
import com.project.tarea1.logic.entity.rols.RoleEnum;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createDefaultUser();
    }

    private void createDefaultUser() {
        User defaultUser = new User();
        defaultUser.setName("Cami");
        defaultUser.setLastname("Morales");
        defaultUser.setEmail("camimorales@gmail.com");
        defaultUser.setPassword("user123");

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);
        Optional<User> optionalUser = userRepository.findByEmail(defaultUser.getEmail());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = new User();
        user.setName(defaultUser.getName());
        user.setLastname(defaultUser.getLastname());
        user.setEmail(defaultUser.getEmail());
        user.setPassword(passwordEncoder.encode(defaultUser.getPassword()));
        user.setRole(optionalRole.get());

        userRepository.save(user);
    }
}
