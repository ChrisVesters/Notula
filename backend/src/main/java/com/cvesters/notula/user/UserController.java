package com.cvesters.notula.user;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cvesters.notula.user.dto.CreateUserDto;
import com.cvesters.notula.user.dto.UserDto;


@RestController
@RequestMapping("/users")
public class UserController {
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserDto request) {
		return ResponseEntity.ok(new UserDto());

	}
	
}
