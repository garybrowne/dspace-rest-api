package org.dspace.rest.content;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.workflow.WorkflowItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContentHelper {
    //EPerson
    public static int countItemsEPerson(Context context) throws SQLException {
        int itemcount = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM eperson";

            statement = context.getDBConnection().prepareStatement(query);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                itemcount = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqle) {
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return itemcount;
    }

    public static EPerson[] findAllEPerson(Context context, int offset, int limit) throws SQLException {

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT * FROM eperson ORDER BY eperson_id");

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name"))) {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0) {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") rec ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0) {
                queryBuf.append("WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0) {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0) {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        } else {
            if (limit > 0) {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0) {
                queryBuf.append(" OFFSET ? ");
            }
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[]{};
        if (limit > 0 && offset > 0) {
            paramArr = new Object[]{limit, offset};
        } else if (limit > 0) {
            paramArr = new Object[]{limit};
        } else if (offset > 0) {
            paramArr = new Object[]{offset};
        }

        TableRowIterator rows = DatabaseManager.query(context, queryBuf.toString(), paramArr);

        try {
            List<TableRow> epeopleRows = rows.toList();

            EPerson[] epeople = new EPerson[epeopleRows.size()];

            for (int i = 0; i < epeopleRows.size(); i++) {
                TableRow row = epeopleRows.get(i);

                // First check the cache
                EPerson fromCache = (EPerson) context.fromCache(EPerson.class, row.getIntColumn("eperson_id"));

                if (fromCache != null) {
                    epeople[i] = fromCache;
                } else {
                    epeople[i] = new EPerson(context, row);
                }
            }

            return epeople;
        } finally {
            if (rows != null) {
                rows.close();
            }
        }
    }

    public static int countItemsSubmitters(Context context, String q) throws SQLException {

        int itemcount = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String param = "%" + q.toLowerCase() + "%";

            String query = "SELECT COUNT(*) FROM (" +
                    "SELECT DISTINCT eperson.email, eperson.* FROM workflowitem, item, eperson " +
                    "WHERE workflowitem.item_id = item.item_id AND item.submitter_id = eperson.eperson_id AND " +
                    "(LOWER(firstname) LIKE LOWER(?) OR LOWER(lastname) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)))";

            statement = context.getDBConnection().prepareStatement(query);
            statement.setString(1, param);
            statement.setString(2, param);
            statement.setString(3, param);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                itemcount = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqle) {
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return itemcount;
    }

    public static EPerson[] findAllSubmitters(Context context, String query, int offset, int limit, String orderby) throws SQLException {

        String params = "%" + query.toLowerCase() + "%";
        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT DISTINCT eperson.email em, eperson.* FROM workflowitem, item, eperson WHERE workflowitem.item_id = item.item_id AND item.submitter_id = eperson.eperson_id AND ");
        queryBuf.append("(LOWER(firstname) LIKE LOWER(?) OR LOWER(lastname) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) " + (!"".equals(orderby) ? "ORDER BY " + orderby : ""));

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name"))) {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0) {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") rec ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0) {
                queryBuf.append("WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0) {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0) {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        } else {
            if (limit > 0) {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0) {
                queryBuf.append(" OFFSET ? ");
            }
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[]{params, params, params};
        if (limit > 0 && offset > 0) {
            paramArr = new Object[]{params, params, params, limit, offset};
        } else if (limit > 0) {
            paramArr = new Object[]{params, params, params, limit};
        } else if (offset > 0) {
            paramArr = new Object[]{params, params, params, offset};
        }

        // Get all the epeople that match the query
        TableRowIterator rows = DatabaseManager.query(context, queryBuf.toString(), paramArr);
        try {
            List<TableRow> epeopleRows = rows.toList();
            EPerson[] epeople = new EPerson[epeopleRows.size()];

            for (int i = 0; i < epeopleRows.size(); i++) {
                TableRow row = epeopleRows.get(i);

                // First check the cache
                EPerson fromCache = (EPerson) context.fromCache(EPerson.class, row.getIntColumn("eperson_id"));

                if (fromCache != null) {
                    epeople[i] = fromCache;
                } else {
                    epeople[i] = new EPerson(context, row);
                }
            }

            return epeople;
        } finally {
            if (rows != null) {
                rows.close();
            }
        }
    }

    public static int countItemsCollection(Context context) throws SQLException {
        int itemcount = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM collection";

            statement = context.getDBConnection().prepareStatement(query);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                itemcount = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqle) {
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return itemcount;
    }

    public static Collection[] findAllCollection(Context context, int offset, int limit) throws SQLException {

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT * FROM collection ORDER BY name");

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name"))) {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0) {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") rec ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0) {
                queryBuf.append("WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0) {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0) {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        } else {
            if (limit > 0) {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0) {
                queryBuf.append(" OFFSET ? ");
            }
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[]{};
        if (limit > 0 && offset > 0) {
            paramArr = new Object[]{limit, offset};
        } else if (limit > 0) {
            paramArr = new Object[]{limit};
        } else if (offset > 0) {
            paramArr = new Object[]{offset};
        }

        TableRowIterator tri = DatabaseManager.query(context, queryBuf.toString(), paramArr);

        List<Collection> collections = new ArrayList<Collection>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();

                // First check the cache
                Collection fromCache = (Collection) context.fromCache(Collection.class, row.getIntColumn("collection_id"));

                if (fromCache != null) {
                    collections.add(fromCache);
                } else {
                    collections.add(new Collection(context, row));
                }
            }
        } finally {
            // close the TableRowIterator to free up resources
            if (tri != null) {
                tri.close();
            }
        }

        return collections.toArray(new Collection[collections.size()]);
    }

    public static int countItemsItem(Context context, int collectionID) throws SQLException {
        int itemcount = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM collection2item, item WHERE "
                    + "collection2item.collection_id =  ? "
                    + "AND collection2item.item_id = item.item_id ";

            statement = context.getDBConnection().prepareStatement(query);
            statement.setInt(1, collectionID);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                itemcount = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqle) {
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return itemcount;
    }

    public static Item[] findAllItem(Context context, int collectionID, int offset, int limit) throws SQLException {

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT item.* FROM item, collection2item " +
                "WHERE item.item_id=collection2item.item_id " +
                "AND collection2item.collection_id = ? " +
                "order by item.item_id desc");

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name"))) {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0) {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") rec ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0) {
                queryBuf.append("WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0) {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0) {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        } else {
            if (limit > 0) {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0) {
                queryBuf.append(" OFFSET ? ");
            }
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[]{collectionID};
        if (limit > 0 && offset > 0) {
            paramArr = new Object[]{collectionID, limit, offset};
        } else if (limit > 0) {
            paramArr = new Object[]{collectionID, limit};
        } else if (offset > 0) {
            paramArr = new Object[]{collectionID, offset};
        }

        TableRowIterator tri = DatabaseManager.query(context, queryBuf.toString(), paramArr);

        List<Item> items = new ArrayList<Item>();

        try {
            while (tri.hasNext()) {
                TableRow row = tri.next();

                // First check the cache
                Item fromCache = (Item) context.fromCache(Item.class, row.getIntColumn("item_id"));

                if (fromCache != null) {
                    items.add(fromCache);
                } else {
                    items.add(new Item(context, row));
                }
            }
        } finally {
            if (tri != null) {
                tri.close();
            }
        }

        return items.toArray(new Item[items.size()]);
    }

    public static int countItemsWorkflow(Context c, String reviewerStr, String submitterStr, String[] fields, String status) throws SQLException {
        int itemcount = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM (\n" +
                    fillSqlWorkflow(c, reviewerStr, submitterStr, fields, status) +
                    ") t";

            statement = c.getDBConnection().prepareStatement(query);

            rs = statement.executeQuery();
            if (rs != null) {
                rs.next();
                itemcount = rs.getInt(1);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqle) {
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return itemcount;
    }

    public static WorkflowItem[] findAllWorkflow(Context c, String reviewerStr, String submitterStr, String[] fields,
                                                 String status, int offset, int limit) throws SQLException {
        List<WorkflowItem> wfItems = new ArrayList<WorkflowItem>();

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("SELECT * FROM (\n")
                .append(fillSqlWorkflow(c, reviewerStr, submitterStr, fields, status))
                .append(") t ORDER BY workflow_id");

        // Add offset and limit restrictions - Oracle requires special code
        if ("oracle".equals(ConfigurationManager.getProperty("db.name"))) {
            // First prepare the query to generate row numbers
            if (limit > 0 || offset > 0) {
                queryBuf.insert(0, "SELECT /*+ FIRST_ROWS(n) */ rec.*, ROWNUM rnum  FROM (");
                queryBuf.append(") ");
            }

            // Restrict the number of rows returned based on the limit
            if (limit > 0) {
                queryBuf.append("rec WHERE rownum<=? ");
                // If we also have an offset, then convert the limit into the maximum row number
                if (offset > 0) {
                    limit += offset;
                }
            }

            // Return only the records after the specified offset (row number)
            if (offset > 0) {
                queryBuf.insert(0, "SELECT * FROM (");
                queryBuf.append(") WHERE rnum>?");
            }
        } else {
            if (limit > 0) {
                queryBuf.append(" LIMIT ? ");
            }

            if (offset > 0) {
                queryBuf.append(" OFFSET ? ");
            }
        }

        // Create the parameter array, including limit and offset if part of the query
        Object[] paramArr = new Object[]{};
        if (limit > 0 && offset > 0) {
            paramArr = new Object[]{limit, offset};
        } else if (limit > 0) {
            paramArr = new Object[]{limit};
        } else if (offset > 0) {
            paramArr = new Object[]{offset};
        }

        TableRowIterator tri = DatabaseManager.query(c, queryBuf.toString(), paramArr);

        try {
            // make a list of workflow items
            while (tri.hasNext()) {
                TableRow row = tri.next();
                WorkflowItem wi = new WorkflowItem(c, row);
                wfItems.add(wi);
            }
        } finally {
            if (tri != null) {
                tri.close();
            }
        }

        return wfItems.toArray(new WorkflowItem[wfItems.size()]);
    }

    private static String fillSqlWorkflow(Context c, String reviewerStr, String submitterStr, String[] fields, String status) {
        int epid = 0;
        EPerson ep = c.getCurrentUser();
        if (ep != null) {
            epid = ep.getID();
        }

        int own;
        int submit;
        int pool;

        int reviewer = parseEPersonInt(c, reviewerStr);
        int submitter = parseEPersonInt(c, submitterStr);
        if (reviewer > 0 || submitter > 0) {
            pool = 0;
            own = epid > 0 ? reviewer > 0 ? reviewer : 0 : 0;
            submit = epid > 0 ? submitter > 0 ? submitter : 0 : 0;
        } else {
            pool = own = submit = epid;
        }

        String insql;
        if (fields != null && fields.length > 0) {
            List<String[]> l = new ArrayList<String[]>();
            for (String field : fields) {
                String[] kv = new String[4];
                String[] fkv = field.split("\\:");
                if (fkv.length > 0) {
                    String[] ks = fkv[0].split("\\.");
                    for (int i = 0; i < ks.length; i++) {
                        kv[i] = ks[i];
                    }
                }
                if (fkv.length > 1) {
                    kv[3] = fkv[1];
                }
                l.add(kv);
            }

            insql = "(SELECT DISTINCT mv.item_id FROM metadataschemaregistry msr, metadatafieldregistry mfr, metadatavalue mv \n" +
                    "WHERE msr.metadata_schema_id = mfr.metadata_schema_id AND mfr.metadata_field_id = mv.metadata_field_id \n" +
                    "AND ( \n";
            for (int i = 0; i < l.size(); i++) {
                String[] kv = l.get(i);
                insql = insql + "msr.short_id " + (kv[0] == null ? "is null\n" : "= '" + kv[0] + "'\n");
                insql = insql + "AND mfr.element " + (kv[1] == null ? "is null\n" : "= '" + kv[1] + "'\n");
                insql = insql + "AND mfr.qualifier " + (kv[2] == null ? "is null\n" : "= '" + kv[2] + "'\n");
                insql = insql + "AND LOWER(mv.text_value) " + (kv[3] == null ? "is null\n" : "LIKE '%" + kv[3].toLowerCase() + "%'\n");
                if (i != l.size() - 1) {
                    insql = insql + "OR\n";
                }
            }
            insql = insql + ")) its";
        } else {
            insql = "item its";
        }

        boolean reviewInd = false;
        boolean poolInd = false;

        int submitinstatus = 0;
        if (epid > 0) {
            if (status != null && !"".equals(status)) {
                own = pool = submit = 0;
                submitinstatus = submitter > 0 ? submitter : 0;
                if ("pool".equals(status)) {
                    poolInd = true;
                } else if ("review".equals(status)) {
                    reviewInd = true;
                }
            }
        }


        String sql = "SELECT workflowitem.* FROM workflowitem, item, " + insql + " WHERE workflowitem.item_id = its.item_id" + (reviewInd ? (submitinstatus > 0 ? (" AND item.submitter_id=" + submitinstatus) : "") + " AND workflowitem.owner>0" : " AND workflowitem.owner=" + own) + " AND its.item_id=item.item_id \n" +
                "UNION\n" +
                "SELECT workflowitem.* FROM workflowitem, item, taskListItem, " + insql + " WHERE workflowitem.item_id = its.item_id" + (poolInd ? submitinstatus > 0 ? (" AND item.submitter_id=" + submitinstatus) : "" : " AND tasklistitem.eperson_id=" + pool) + " AND tasklistitem.workflow_id=workflowitem.workflow_id AND its.item_id=item.item_id \n" +
                "UNION\n" +
                "SELECT workflowitem.* FROM workflowitem, item, " + insql + " WHERE workflowitem.item_id=its.item_id AND item.submitter_id=" + submit + " AND its.item_id=item.item_id \n";
        return sql;
    }

    private static int parseEPersonInt(Context c, String str) {
        if (str != null && !"".equals(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }

            try {
                EPerson ePerson = EPerson.findByEmail(c, str);
                if (ePerson != null) {
                    return ePerson.getID();
                }
            } catch (SQLException ee) {
            } catch (AuthorizeException e1) {
            }
        }
        return 0;
    }
}