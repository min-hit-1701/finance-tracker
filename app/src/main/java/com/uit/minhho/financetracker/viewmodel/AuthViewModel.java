package com.uit.minhho.financetracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.uit.minhho.financetracker.data.local.entity.User;
import com.uit.minhho.financetracker.data.repository.AppRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AppRepository repository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    public void register(String name, String email, String password) {
        User newUser = new User(name, email, password);
        repository.registerUser(newUser);
    }

    public void login(String email, String password) {
        // In a real app, this should be asynchronous
        new Thread(() -> {
            User user = repository.login(email, password);
            if (user != null) {
                userLiveData.postValue(user);
            } else {
                errorLiveData.postValue("Email hoặc mật khẩu không chính xác");
            }
        }).start();
    }
}
