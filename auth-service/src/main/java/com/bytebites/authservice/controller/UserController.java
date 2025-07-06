package com.bytebites.authservice.controller;


import com.bytebites.authservice.model.User;
import com.bytebites.authservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/api/users")
@RestController
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        System.out.println("All users");
        return ResponseEntity.ok("All users");
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println(user);
        return ResponseEntity.ok(userService.createUser(user));
    }
}