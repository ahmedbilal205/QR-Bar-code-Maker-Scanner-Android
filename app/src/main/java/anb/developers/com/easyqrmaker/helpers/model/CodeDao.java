package anb.developers.com.easyqrmaker.helpers.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Flowable;
import anb.developers.com.easyqrmaker.helpers.constant.TableNames;
import anb.developers.com.easyqrmaker.helpers.util.database.BaseDao;

@Dao

public interface CodeDao extends BaseDao<Code> {
    @Query("SELECT * FROM " + TableNames.CODES)
    Flowable<List<Code>> getAllFlowableCodes();

}
