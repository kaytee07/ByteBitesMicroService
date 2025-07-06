package com.bytebites.authservice.service;
import com.bytebites.authservice.Repository.UserRepository;
import com.bytebites.authservice.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;



@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user){
        return userRepository.save(user);
    }
}