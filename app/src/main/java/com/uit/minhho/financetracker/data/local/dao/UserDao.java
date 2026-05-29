package com.uit.minhho.financetracker.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.uit.minhho.financetracker.data.local.entity.User;

@Dao
public interface UserDao {
    @Insert
    void registerUser(User user);

    @Query("SELECT * FROM users WHERE (email = :identifier OR phone = :identifier) AND password = :password LIMIT 1")
    User login(String identifier, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);

    @Query("UPDATE users SET password = :newPassword WHERE phone = :phone")
    void updatePasswordByPhone(String phone, String newPassword);
}
