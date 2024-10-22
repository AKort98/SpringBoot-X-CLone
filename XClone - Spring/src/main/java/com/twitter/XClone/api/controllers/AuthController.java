package com.twitter.XClone.api.controllers;


import com.twitter.XClone.api.model.*;
import com.twitter.XClone.exceptions.*;
import com.twitter.XClone.model.Friendship;
import com.twitter.XClone.model.LocalUser;
import com.twitter.XClone.model.Tweet;
import com.twitter.XClone.model.UserLikes;
import com.twitter.XClone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<HashMap> registerUser(@Valid @RequestBody RegistrationBody registrationBody) {
        try {
            userService.registerUser(registrationBody);
            HashMap<String, String> response = new HashMap<>();
            response.put("message", "User created");
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExists e) {
            HashMap<String, String> response = new HashMap<>();
            response.put("message", "User exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse();
        BindingResult bindingResult = ex.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            validationErrorResponse.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseBody> loginUser(@Valid @RequestBody LoginBody body) {
        try {
            String jwt = userService.loginUser(body);
            return buildResponse(HttpStatus.OK, jwt, true, "Log-in successful");
        } catch (UserDoesNotExistException e) {
            return buildResponse(HttpStatus.FORBIDDEN, null, false, "User does not exist");
        } catch (UserIsNotVerifiedException e) {
            return buildResponse(HttpStatus.FORBIDDEN, null, false, "User is not verified");
        } catch (IncorrectPasswordException e) {
            return buildResponse(HttpStatus.FORBIDDEN, null, false, "Incorrect Password");
        }
    }

    private ResponseEntity<LoginResponseBody> buildResponse(HttpStatus status, String token, boolean success, String message) {
        LoginResponseBody body = new LoginResponseBody();
        body.setJWTToken(token);
        body.setSuccess(success);
        body.setMessage(message);
        return ResponseEntity.status(status).body(body);
    }

    @PostMapping("/verify")
    public ResponseEntity verifyUserEmail(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
    @GetMapping("/me")
    public ResponseEntity getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


    @PostMapping("/forgot")
    public ResponseEntity forgotPassword(@RequestParam String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok().build();
        } catch (EmailNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reset")
    public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordBody body) {
        try {
            userService.resetPassword(body);
            return ResponseEntity.ok().build();
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


}
