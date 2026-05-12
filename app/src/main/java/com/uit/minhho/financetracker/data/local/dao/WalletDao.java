package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import java.util.List;

@Dao
public interface WalletDao {
    @Insert
    long insert(Wallet wallet);

    @Update
    void update(Wallet wallet);

    @Delete
    void delete(Wallet wallet);

    @Query("SELECT * FROM wallets WHERE isBusiness = :isBusiness")
    LiveData<List<Wallet>> getWallets(boolean isBusiness);

    @Query("SELECT SUM(balance) FROM wallets WHERE isBusiness = :isBusiness")
    LiveData<Double> getTotalBalance(boolean isBusiness);

    @Query("SELECT * FROM wallets WHERE id = :walletId LIMIT 1")
    Wallet getWalletByIdSync(int walletId);

    @Query("SELECT * FROM wallets WHERE isBusiness = :isBusiness ORDER BY id ASC LIMIT 1")
    Wallet getFirstWalletSync(boolean isBusiness);

    @Query("SELECT * FROM wallets WHERE isBusiness = :isBusiness AND balance >= :minBalance ORDER BY balance DESC LIMIT 1")
    Wallet getFirstWalletWithMinBalanceSync(boolean isBusiness, double minBalance);

    @Query("UPDATE wallets SET balance = :newBalance WHERE id = :walletId")
    void updateBalanceById(int walletId, double newBalance);
}
