package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.uit.minhho.financetracker.data.local.entity.Partner;
import java.util.List;

@Dao
public interface PartnerDao {
    @Insert
    long insert(Partner partner);

    @Update
    void update(Partner partner);

    @Delete
    void delete(Partner partner);

    @Query("SELECT * FROM partners WHERE isBusiness = :isBusiness ORDER BY name ASC")
    LiveData<List<Partner>> getPartners(boolean isBusiness);

    @Query("SELECT * FROM partners WHERE type = :type AND isBusiness = :isBusiness ORDER BY name ASC")
    LiveData<List<Partner>> getPartnersByType(String type, boolean isBusiness);

    @Query("SELECT * FROM partners WHERE name = :name AND isBusiness = :isBusiness LIMIT 1")
    Partner getPartnerByNameSync(String name, boolean isBusiness);
}
