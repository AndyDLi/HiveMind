package com.andydli.hivemind.mapper;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;

@Component
public class UserMapper {
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toEntity(UserRegistrationDTO userRegistrationDTO) {
        if (userRegistrationDTO == null) {
            return null;
        }

        User user = new User();
        user.setFirstName(userRegistrationDTO.firstName());
        user.setLastName(userRegistrationDTO.lastName());
        return user;
    }
}