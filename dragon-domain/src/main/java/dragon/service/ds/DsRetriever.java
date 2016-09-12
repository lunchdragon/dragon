package dragon.service.ds;

import dragon.model.food.Restaurant;

import java.util.List;

/**
 * Created by lin.cheng on 7/21/15.
 */
public interface DsRetriever {
    Restaurant addByBid(Long gid, String bid)throws Exception;
    List<Restaurant> searchAndImport(Long gid)throws Exception;
}
