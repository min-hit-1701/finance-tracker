package com.uit.minhho.financetracker.viewmodel; // Bạn nhớ check xem dòng package này có giống file cũ không nhé

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.uit.minhho.financetracker.data.repository.AppRepository;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import java.util.List;

public class WalletViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<Wallet>> personalWallets;

    public WalletViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        // Truyền "false" vào vì bạn đang làm Backend cho Personal (Cá nhân) chứ không phải Business
        personalWallets = repository.getWallets(false);
    }

    // Hàm lấy danh sách ví cá nhân cho Giao diện
    public LiveData<List<Wallet>> getPersonalWallets() {
        return personalWallets;
    }

    public void refreshPersonalWallets() {
        repository.refreshWallets(false);
    }

    public LiveData<Double> getTotalBalance(boolean isBusiness) {
        return repository.getTotalBalance(isBusiness);
    }

    // Hàm thêm một cái ví mới (Gọi sang hàm insert chuẩn có chạy ngầm của AppRepository)
    public void insert(Wallet wallet) {
        repository.insertWallet(wallet);
    }

    public void insert(Wallet wallet, AppRepository.OperationCallback callback) {
        repository.insertWallet(wallet, callback);
    }

    public void delete(Wallet wallet) {
        repository.deleteWallet(wallet);
    }

    public void delete(Wallet wallet, AppRepository.OperationCallback callback) {
        repository.deleteWallet(wallet, callback);
    }
}
