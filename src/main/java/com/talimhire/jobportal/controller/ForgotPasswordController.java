package com.talimhire.jobportal.controller;

import com.talimhire.jobportal.DTO.UserDTO;
import com.talimhire.jobportal.entity.PasswordResetToken;
import com.talimhire.jobportal.entity.Users;
import com.talimhire.jobportal.repository.TokenRepository;
import com.talimhire.jobportal.repository.UsersRepository;
import com.talimhire.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class ForgotPasswordController {
    private final UsersRepository usersRepository;
    private final UsersService usersService;
    private final TokenRepository tokenRepository;
    @Autowired
    public ForgotPasswordController(com.talimhire.jobportal.repository.UsersRepository usersRepository, UsersService usersService, TokenRepository tokenRepository) {
        this.usersRepository = usersRepository;
        this.usersService = usersService;
        this.tokenRepository = tokenRepository;
    }
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/forgotPassword")
    public String forgotPassword(){
        return "forgotPassword";
    }
    @PostMapping("/forgotPassword")
    public String forgotPasswordPost(@ModelAttribute UserDTO userDTO){
        String output="";
        Optional<Users> user = usersRepository.findByEmail(userDTO.getEmail());
        if(user.isPresent()){
            Users user1=user.get();
            output = usersService.sendEmail(user1);
        }
        if(output.equals("success")){
            return "redirect:/forgotPassword?success";
        }
        return "redirect:/login?error";
    }
    @GetMapping("/resetPassword/{token}")
    public String resetPasswordForm(@PathVariable String token, Model model) {
        PasswordResetToken reset = tokenRepository.findByToken(token);
        if (reset != null && usersService.hasExpired(reset.getExpiryDateTime())) {
            model.addAttribute("email", reset.getUser().getEmail());
            return "resetPassword";
        }
        return "redirect:/forgotPassword?error";
    }

    @PostMapping("/resetPassword")
    public String passwordResetProcess(@ModelAttribute UserDTO userDTO) {
        Optional<Users> user = usersRepository.findByEmail(userDTO.getEmail());
        if (user.isPresent()) {
            {
                Users user1 = user.get();
                user1.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                usersRepository.save(user1);
            }
        }
        return "redirect:/login";
    }











}
