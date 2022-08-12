package com.ssafy.colors.controller;

import com.ssafy.colors.request.LoginReq;
import com.ssafy.colors.service.AuthService;
import com.ssafy.colors.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    JWTUtil jwtUtil;
    // Login
    @PostMapping("/login")
    public ResponseEntity<HashMap> login(@RequestBody LoginReq user){
        String result = "fail";
        HashMap<String, Object> resultmap = new HashMap<>();
        if(authService.login(user)){
            result = "success";
            resultmap.put("access-token", jwtUtil.createAccessToken(user.getUserId()));
        }
        resultmap.put("message",result);

        return new ResponseEntity<>(resultmap, HttpStatus.OK);
    }
}
