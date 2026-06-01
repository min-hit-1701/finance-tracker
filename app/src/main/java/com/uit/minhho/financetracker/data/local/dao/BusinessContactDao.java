package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.uit.minhho.financetracker.data.local.entity.BusinessContact;

import java.util.List;

@Dao
public interface BusinessContactDao {
    @Insert
    long insert(BusinessContact contact);

    @Update
    void update(BusinessContact contact);

    @Delete
    void delete(BusinessContact contact);

    @Query("SELECT * FROM business_contacts ORDER BY name ASC")
    LiveData<List<BusinessContact>> getAllContacts();

    @Query("SELECT * FROM business_contacts ORDER BY name ASC")
    List<BusinessContact> getAllContactsSync();

    @Query("SELECT * FROM business_contacts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    List<BusinessContact> searchContactsSync(String query);
}
