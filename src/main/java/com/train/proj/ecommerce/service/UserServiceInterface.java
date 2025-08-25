package com.train.proj.ecommerce.service;

import com.train.proj.ecommerce.entity.User;
import java.util.List;

public interface UserServiceInterface {

	void registerUser(User userData);
	boolean loginUser(String username,String password);
	User getUserProfile(int userId);
	void updateUserProfile(int userId,User userData);
	User getUserByUsername(String username);
	void updatePassword(int userId, String currentPassword, String newPassword);
	List<User> getAllUsers();
	void deleteUser(int userId);

}
