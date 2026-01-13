package restaurant.controller;

import restaurant.model.User;
import restaurant.repository.UserRepository;

public class LoginController {
    private final UserRepository userRepository = new UserRepository();

    public User login(String username, String password) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found!"));

        if (!user.getPassword().equals(password)) {
            throw new Exception("Invalid password!");
        }
        return user;
    }
}