package univ.fooddeliv.service.impl;

import univ.fooddeliv.dto.AuthDto;
import univ.fooddeliv.dto.ProfileUpdateDto;
import univ.fooddeliv.dto.UserDto;
import univ.fooddeliv.exceptions.UserAlreadyExistsException;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.Role;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.RoleRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(AuthDto authDto) throws UserAlreadyExistsException {
        if (userRepository.existsByEmail(authDto.getEmail())) {
            throw new UserAlreadyExistsException("Этот email уже зарегистрирован");
        }

        if (!authDto.getPassword().equals(authDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        Role userRole = roleRepository.findByRole("CUSTOMER").orElse(null);

        User user = User.builder()
                .email(authDto.getEmail())
                .password(passwordEncoder.encode(authDto.getPassword()))
                .name(authDto.getName())
                .role(userRole)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public UserDto getCurrentUserDetails(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return convertToUserDto(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    @Override
    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getOrders().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void updateProfile(String email, ProfileUpdateDto profileUpdateDto) {
        User user = findByEmail(email);
        user.setName(profileUpdateDto.getName());
        user.setPhone(profileUpdateDto.getPhone());

        String newPassword = profileUpdateDto.getNewPassword() == null ? "" : profileUpdateDto.getNewPassword();
        String confirmPassword = profileUpdateDto.getConfirmPassword() == null ? "" : profileUpdateDto.getConfirmPassword();
        boolean changingPassword = !newPassword.isBlank() || !confirmPassword.isBlank();
        if (changingPassword) {
            String currentPassword = profileUpdateDto.getCurrentPassword() == null ? "" : profileUpdateDto.getCurrentPassword();
            if (currentPassword.isBlank() || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Неверный текущий пароль");
            }
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("Новый пароль должен быть не короче 6 символов");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    private UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().getRole())
                .build();
    }
}
