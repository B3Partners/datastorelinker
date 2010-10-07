/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.datastorelinker.gui.stripes;

import java.util.List;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;
import nl.b3p.commons.jpa.JpaUtilServlet;
import nl.b3p.commons.stripes.Transactional;
import nl.b3p.datastorelinker.entity.Database;
import nl.b3p.datastorelinker.json.SuccessMessage;
import nl.b3p.datastorelinker.json.JSONErrorResolution;
import nl.b3p.datastorelinker.json.JSONResolution;
import nl.b3p.geotools.data.linker.DataStoreLinker;
import org.geotools.data.DataStore;
import org.hibernate.Session;

/**
 *
 * @author Erik van de Pol
 */
@Transactional
public class DatabaseAction extends DefaultAction {

    private Log log = Log.getInstance(DatabaseAction.class);

    private Boolean admin;

    private List<Database> databases;
    private Database selectedDatabase;
    protected Long selectedDatabaseId;
    // PostGIS specific:
    private Database.Type dbType;
    private String host;
    private String databaseName;
    private String username;
    private String password;
    private Integer port;
    private String schema;
    // Oracle specific (plus above):
    private String instance;
    private String alias;
    // MS Access specific:
    private String url;
    private String srs;
    private String colX;
    private String colY;


    @DefaultHandler
    public Resolution admin() {
        setAdmin(true);
        list();
        return new ForwardResolution(getAdminJsp());
    }

    protected String getAdminJsp() {
        return "/WEB-INF/jsp/management/databaseAdmin.jsp";
    }

    protected String getCreateJsp() {
        return "/WEB-INF/jsp/main/database/create.jsp";
    }

    protected String getListJsp() {
        return "/WEB-INF/jsp/main/database/list.jsp";
    }

    @DontValidate
    public Resolution create() {
        return new ForwardResolution(getCreateJsp());
    }

    // Always returns input databases! Should be overridden if necessary
    public Resolution list() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session) em.getDelegate();

        databases = session.getNamedQuery("Database.find")
                .setParameter("typeInout", Database.TypeInout.INPUT)
                .list();

        return new ForwardResolution(getListJsp());
    }

    public Resolution delete() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session) em.getDelegate();

        session.delete(session.get(Database.class, selectedDatabaseId));

        return list();
    }

    public Resolution update() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session) em.getDelegate();

        selectedDatabase = (Database) session.get(Database.class, selectedDatabaseId);

        return create();
    }

    public Resolution createComplete() {
        Database db = saveDatabase(Database.TypeInout.INPUT);
        selectedDatabaseId = db.getId();

        return list();
    }

    protected Database saveDatabase(Database.TypeInout typeInout) {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session) em.getDelegate();

        Database database = getDatabase(false);
        database.setTypeInout(typeInout);

        // TODO: wat als DB met ongeveer zelfde inhoud al aanwezig is? waarschuwing? Custom naamgeving issue eerst oplossen hiervoor
        if (selectedDatabaseId == null) {
            session.save(database);
        }
        
        return database;
    }

    protected Database getDatabase(boolean alwaysCreateNewDB) {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session) em.getDelegate();

        Database database;
        if (selectedDatabaseId == null || alwaysCreateNewDB) {
            database = new Database();
        } else {
            database = (Database) session.get(Database.class, selectedDatabaseId);
            database.reset();
        }

        database.setType(dbType);

        switch (dbType) {
            case ORACLE:
                database.setHost(host);
                database.setDatabaseName(databaseName);
                database.setUsername(username);
                database.setPassword(password);
                database.setPort(port);
                database.setSchema(schema);
                database.setInstance(instance);
                database.setAlias(alias);
                break;
            case MSACCESS:
                database.setUrl(url);
                database.setSrs(srs);
                database.setColX(colX);
                database.setColY(colY);
                break;
            case POSTGIS:
                database.setHost(host);
                database.setDatabaseName(databaseName);
                database.setUsername(username);
                database.setPassword(password);
                database.setPort(port);
                database.setSchema(schema);
                break;
            default:
                log.error("Unsupported database type");
                return null;
        }
        return database;
    }

    public Resolution testConnection() {
        DataStore dataStore = null;
        try {
            dataStore = DataStoreLinker.openDataStore(getDatabase(true));
        } catch (Exception e) {
            return new JSONErrorResolution(e.getMessage(), "Databaseconnectie fout");
        } finally {
            if (dataStore != null)
                dataStore.dispose();
        }
        return new JSONResolution(new SuccessMessage());
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public Database.Type getDbType() {
        return dbType;
    }

    public void setDbType(Database.Type dbType) {
        this.dbType = dbType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public String getColX() {
        return colX;
    }

    public void setColX(String colX) {
        this.colX = colX;
    }

    public String getColY() {
        return colY;
    }

    public void setColY(String colY) {
        this.colY = colY;
    }

    public Long getSelectedDatabaseId() {
        return selectedDatabaseId;
    }

    public void setSelectedDatabaseId(Long selectedDatabaseId) {
        this.selectedDatabaseId = selectedDatabaseId;
    }

    public Database getSelectedDatabase() {
        return selectedDatabase;
    }

    public void setSelectedDatabase(Database selectedDatabase) {
        this.selectedDatabase = selectedDatabase;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

}
