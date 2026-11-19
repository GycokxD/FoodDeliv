package univ.fooddeliv.service;

import univ.fooddeliv.dto.AuthDto;
import univ.fooddeliv.dto.ProfileUpdateDto;
import univ.fooddeliv.dto.UserDto;
import univ.fooddeliv.exceptions.UserAlreadyExistsException;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.User;

import java.util.List;

public interface UserService {
    User registerUser(AuthDto authDto) throws UserAlreadyExistsException;

    UserDto getCurrentUserDetails(String email);

    User findByEmail(String email);

    List<Order> getUserOrders(String email);

    void updateProfile(String email, ProfileUpdateDto profileUpdateDto);
}
