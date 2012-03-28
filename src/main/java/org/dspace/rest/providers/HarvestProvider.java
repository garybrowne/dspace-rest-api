/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.rest.providers;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.rest.entities.HarvestResultsInfoEntity;
import org.dspace.rest.entities.ItemEntity;
import org.dspace.rest.entities.ItemEntityId;
import org.dspace.rest.util.GenComparator;
import org.dspace.rest.util.UserRequestParams;
import org.dspace.search.Harvest;
import org.dspace.search.HarvestedItemInfo;
import org.sakaiproject.entitybus.EntityReference;
import org.sakaiproject.entitybus.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybus.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybus.entityprovider.search.Search;
import org.sakaiproject.entitybus.exception.EntityException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides interface for access to harvesting
 * Enables users to harvest items according to several queries, including
 * data range of publication, status of publication, containing elements etc
 *
 * @author Bojan Suzic, bojan.suzic@gmail.com
 * @see HarvestResultsInfoEntity
 */
public class HarvestProvider extends AbstractBaseProvider implements CoreEntityProvider {

    private static Logger log = Logger.getLogger(UserProvider.class);

    public HarvestProvider(EntityProviderManager entityProviderManager) throws SQLException {
        super(entityProviderManager);
        entityProviderManager.registerEntityProvider(this);
    }

    public String getEntityPrefix() {
        return "harvest";
    }

    public boolean entityExists(String id) {
        return true;
    }

    public Object getEntity(EntityReference reference) {
        log.info(userInfo() + "get_entity:" + reference.getId());
        throw new EntityException("Not Acceptable", "The data is not available", 406);
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        log.info(userInfo() + "get_entities");

        Context context;
        try {
            context = new Context();
        } catch (SQLException ex) {
            throw new EntityException("Internal server error", "SQL error", 500);
        }

        UserRequestParams uparams;
        uparams = refreshParams(context);
        List<Object> entities = new ArrayList<Object>();
        List<HarvestedItemInfo> res = new ArrayList<HarvestedItemInfo>();

        /**
         * check requirement for communities and collections, they should be
         * mutually excluded as underlying architecture accepts searching
         * in only one subject (community or collection)
         */
        try {
            if (_community != null) {
                res = Harvest.harvest(context, _community, _sdate, _edate, _start, _limit, true, true, withdrawn, true);
            } else if (_collection != null) {
                res = Harvest.harvest(context, _collection, _sdate, _edate, _start, _limit, true, true, withdrawn, true);
            } else {
                res = Harvest.harvest(context, null, _sdate, _edate, _start, _limit, true, true, withdrawn, true);
            }
        } catch (ParseException ex) {
            throw new EntityException("Bad request", "Incompatible date format", 400);
        } catch (SQLException sq) {
            throw new EntityException("Internal Server Error", "SQL Problem", 500);
        }

        // check results and add entities
        try {
            entities.add(new HarvestResultsInfoEntity(res.size()));
            for (int x = 0; x < res.size(); x++) {
                entities.add(idOnly ? new ItemEntityId(res.get(x).item) : new ItemEntity(res.get(x).item, uparams));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // sort entities if the full info are requested and there are sorting fields
        if (!idOnly && sortOptions.size() > 0)
            Collections.sort(entities, new GenComparator(sortOptions));

        // format results accordint to _limit, _perpage etc
        removeTrailing(entities);

        return entities;
    }

    /**
     * Returns a Entity object with sample data
     */
    public Object getSampleEntity() {
        return null;
    }
}
