package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.uit.minhho.financetracker.data.local.entity.BusinessEntity;

import java.util.List;

@Dao
public interface BusinessDao {
    @Insert
    long insert(BusinessEntity business);

    @Update
    void update(BusinessEntity business);

    @Delete
    void delete(BusinessEntity business);

    @Query("SELECT * FROM businesses ORDER BY id DESC")
    LiveData<List<BusinessEntity>> getBusinesses();
}
