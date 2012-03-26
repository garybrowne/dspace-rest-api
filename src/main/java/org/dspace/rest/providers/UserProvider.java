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
import org.dspace.eperson.EPerson;
import org.dspace.rest.content.ContentHelper;
import org.dspace.rest.entities.UserEntity;
import org.dspace.rest.util.UserRequestParams;
import org.sakaiproject.entitybus.EntityReference;
import org.sakaiproject.entitybus.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybus.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybus.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybus.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybus.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybus.entityprovider.search.Search;
import org.sakaiproject.entitybus.exception.EntityException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserProvider extends AbstractBaseProvider implements CoreEntityProvider, Createable, Updateable, Deleteable {

    private static Logger log = Logger.getLogger(UserProvider.class);

    public UserProvider(EntityProviderManager entityProviderManager) throws SQLException, NoSuchMethodException {
        super(entityProviderManager);
        entityProviderManager.registerEntityProvider(this);
        processedEntity = UserEntity.class;
        func2actionMapGET.put("groups", "groups");
        func2actionMapPOST.put("create", "");
        inputParamsPOST.put("create", new String[]{"email", "firstName", "lastName"});
        func2actionMapPUT.put("edit", "");
        func2actionMapDELETE.put("remove", "");
        func2actionMapPOST.put("authenticate", "authenticate");
        inputParamsPOST.put("authenticate", new String[]{"email","password"});
        entityConstructor = processedEntity.getDeclaredConstructor();
        initMappings(processedEntity);
    }

    public String getEntityPrefix() {
        return "users";
    }

    public boolean entityExists(String id) {
        log.info(userInfo() + "user_exists:" + id);

        if ("authenticate".equals(id)) {
            return true;
        }
        Context context = null;
        try {
            context = new Context();

            refreshParams(context);

            EPerson ePerson = EPerson.find(context, Integer.parseInt(id));
            return ePerson != null ? true : false;
        } catch (SQLException ex) {
            throw new EntityException("Internal server error", "SQL error", 500);
        } catch (NumberFormatException ex) {
            throw new EntityException("Bad request", "Could not parse input", 400);
        } finally {
            removeConn(context);
        }
    }

    public Object getEntity(EntityReference reference) {
        log.info(userInfo() + "get_user:" + reference.getId());
        String segments[] = {};

        if (reqStor.getStoredValue("pathInfo") != null) {
            segments = reqStor.getStoredValue("pathInfo").toString().split("/");
        }

        if (segments.length > 3) {
            return super.getEntity(reference);
        }

        Context context = null;
        try {
            context = new Context();

            UserRequestParams uparams = refreshParams(context);
            if (entityExists(reference.getId())) {
                return new UserEntity(reference.getId(), context, uparams);
            }
        } catch (SQLException ex) {
            throw new EntityException("Internal server error", "SQL error", 500);
        } finally {
            removeConn(context);
        }
        throw new IllegalArgumentException("Invalid id:" + reference.getId());
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        log.info(userInfo() + "list_users:");

        Context context = null;
        try {
            context = new Context();

            UserRequestParams uparams = refreshParams(context);
            List<Object> entities = new ArrayList<Object>();
            entities.add("count:"+ContentHelper.countItemsEPerson(context));
            EPerson[] ePersons = ContentHelper.findAllEPerson(context, _start, _limit);
            for (EPerson c : ePersons) {
                entities.add(new UserEntity(c, context, uparams));
            }

            return entities;
        } catch (SQLException ex) {
            throw new EntityException("Internal server error", "SQL error", 500);
        } finally {
            removeConn(context);
        }
    }

    public Object getSampleEntity() {
        return new UserEntity();
    }
}
