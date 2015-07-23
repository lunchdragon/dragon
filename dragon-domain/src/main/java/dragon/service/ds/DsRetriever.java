package dragon.service.ds;

import dragon.model.food.Restaurant;

/**
 * Created by lin.cheng on 7/21/15.
 */
public interface DsRetriever {
    Restaurant addByBid(Long gid, String bid);
    int searchAndImport(Long gid);
}
