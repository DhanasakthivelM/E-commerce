package com.train.proj.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.train.proj.ecommerce.entity.User;
import com.train.proj.ecommerce.entity.User.Role;
import com.train.proj.ecommerce.exception.InvalidPasswordException;
import com.train.proj.ecommerce.exception.UserAlreadyExistsException;
import com.train.proj.ecommerce.exception.UserNotFoundException;
import com.train.proj.ecommerce.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements UserServiceInterface {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Autowired
	private UserRepository userRepository;
	@Override
	public void registerUser(User userData) {
		// Check if username already exists
		if (userRepository.findByUsername(userData.getUsername()) != null) {
			throw new UserAlreadyExistsException("Username already exists: " + userData.getUsername());
		}
		
		// Check if email already exists
		if (userRepository.findByEmail(userData.getEmail()) != null) {
			throw new UserAlreadyExistsException("Email already exists: " + userData.getEmail());
		}
		
		userData.setRole(Role.CUSTOMER);
		String userpassword=userData.getPassword();
		userData.setPassword(encoder.encode(userpassword));
		userRepository.save(userData);
		
	}

	@Override
	public boolean loginUser(String username, String password) {
		
		User user=userRepository.findByUsername(username);
		if(user !=null)
		{
			return encoder.matches(password,user.getPassword());
		}
		
		return false;
	}

	@Override
	public User getUserProfile(int userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
	}

	@Override
	public void updateUserProfile(int userId, User userData) {
		
		User existingUser =userRepository.findById(userId).orElse(null);
		
		if (existingUser != null) {
	        existingUser.setEmail(userData.getEmail());
	        existingUser.setUsername(userData.getUsername());
	        
	        userRepository.save(existingUser);
	    }
		
	}

	@Override
	public User getUserByUsername(String username) {
		
		User user=userRepository.findByUsername(username);
		return user;
	}

	@Override
	public void updatePassword(int userId, String currentPassword, String newPassword) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
		
		// Verify current password
		if (!encoder.matches(currentPassword, user.getPassword())) {
			throw new InvalidPasswordException("Current password is incorrect");
		}
		
		// Update password
		user.setPassword(encoder.encode(newPassword));
		userRepository.save(user);
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public void deleteUser(int userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException("User not found with id: " + userId);
		}
		userRepository.deleteById(userId);
	}

}
