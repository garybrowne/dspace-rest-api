/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */


package org.dspace.rest.entities;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.Constants;
import org.sakaiproject.entitybus.entityprovider.annotations.EntityId;
import org.dspace.content.Community;
import org.dspace.core.Context;
import org.sakaiproject.entitybus.exception.EntityException;

import java.sql.SQLException;

/**
 * Entity describing community, basic version
 * @see CommunityEntity
 * @see Community
 * @author Bojan Suzic, bojan.suzic@gmail.com
 */
public class CommunityEntityId {

    @EntityId
    private int id;

    protected CommunityEntityId() {
    }

    public CommunityEntityId(String uid, Context context) {
        if (uid!=null&&!"".equals(uid)) {
            try {

                Community res = Community.find(context, Integer.parseInt(uid));
                // Check authorisation
                AuthorizeManager.authorizeAction(context, res, Constants.READ);

                this.id = res.getID();
                //context.complete();
            } catch (SQLException ex) {
                throw new EntityException("Internal server error", "SQL error", 500);
            } catch (AuthorizeException ex) {
                throw new EntityException("Forbidden", "Forbidden", 403);
            }
        } else {
            throw new EntityException("Bad request", "Value not included", 400);
        }
    }

    public CommunityEntityId(Community community) throws SQLException {
        this.id = community.getID();
    }

    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof CommunityEntityId)) {
            return false;
        } else {
            CommunityEntityId castObj = (CommunityEntityId) obj;
            return (this.id == castObj.id);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }


    @Override
    public String toString() {
        return "id:" + this.id;
    }
}
