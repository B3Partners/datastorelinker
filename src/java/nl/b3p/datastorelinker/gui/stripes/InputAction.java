/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.datastorelinker.gui.stripes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.util.Log;
import nl.b3p.commons.jpa.JpaUtilServlet;
import nl.b3p.commons.stripes.Transactional;
import nl.b3p.datastorelinker.entity.Database;
import nl.b3p.datastorelinker.entity.Inout;
import nl.b3p.datastorelinker.util.DefaultErrorResolution;
import nl.b3p.geotools.data.linker.DataStoreLinker;
import nl.b3p.geotools.data.linker.util.DataStoreUtil;
import nl.b3p.geotools.data.linker.util.DataTypeList;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureCollection;
import org.hibernate.Session;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 *
 * @author Erik van de Pol
 */
@Transactional
public class InputAction extends DefaultAction {
    private final static Log log = Log.getInstance(InputAction.class);

    private final static String LIST_JSP = "/WEB-INF/jsp/main/input/list.jsp";
    private final static String TABLE_LIST_JSP = "/WEB-INF/jsp/main/input/table/list.jsp";
    private final static String CREATE_DATABASE_JSP = "/WEB-INF/jsp/main/input/database/create.jsp";
    private final static String CREATE_FILE_JSP = "/WEB-INF/jsp/main/input/file/create.jsp";
    private final static String EXAMPLE_RECORD_JSP = "/WEB-INF/jsp/main/actions/exampleRecord.jsp";
    private final static String ADMIN_JSP = "/WEB-INF/jsp/management/inputAdmin.jsp";

    private List<Inout> inputs;
    private Long selectedInputId;

    private List<Database> databases;
    private Long selectedDatabaseId;

    private String selectedFilePath;
    //private String selectedFileDirectory;

    private List<String> tables;
    private List<String> failedTables;
    private String selectedTable;

    private List<String> columnNames;
    private List<Object> recordValues;

    @DefaultHandler
    public Resolution admin() {
        list();
        return new ForwardResolution(ADMIN_JSP);
    }

    public Resolution list() {
        inputs = findDBInputs();

        log.debug(inputs);

        return new ForwardResolution(LIST_JSP);
    }

    public static List<Inout> findDBInputs() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        return session.getNamedQuery("Inout.findAllOfDataType")
                .setParameter("typeName", Inout.Type.INPUT)
                .setParameter("datatypeName", Inout.Datatype.DATABASE)
                .list();
    }

    public Resolution delete() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        session.delete(session.get(Inout.class, selectedInputId));

        return list();
    }

    public Resolution update() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        Inout input = (Inout)session.get(Inout.class, selectedInputId);
        selectedTable = input.getTableName();

        switch(input.getDatatype()) {
            case DATABASE:
                selectedDatabaseId = input.getDatabase().getId();
                return createDatabaseInput();
            /*case FILE:
                selectedFileId = input.getFile().getId();
                selectedFileDirectory = input.getFile().getDirectory();
                return createFileInput();*/
            default:
                log.error("Unknown input type.");
                return null;
        }
    }

    public Resolution createDatabaseInput() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        databases = session.getNamedQuery("Database.find")
                .setParameter("typeInout", Database.TypeInout.INPUT)
                .list();
        
        return new ForwardResolution(CREATE_DATABASE_JSP);
    }

    public Resolution createFileInput() {
        return new ForwardResolution(CREATE_FILE_JSP);
    }

    public Resolution createDatabaseInputComplete() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        Database selectedDatabase = (Database)session.get(Database.class, selectedDatabaseId);

        Inout dbInput = null;
        if (selectedInputId == null)
            dbInput = new Inout();
        else
            dbInput = (Inout)session.get(Inout.class, selectedInputId);

        dbInput.setType(Inout.Type.INPUT);
        dbInput.setDatatype(Inout.Datatype.DATABASE);
        dbInput.setDatabase(selectedDatabase);
        dbInput.setTableName(selectedTable);
        String name = selectedDatabase.getName();
        if (selectedTable != null && !selectedTable.equals(""))
            name += " (" + selectedTable + ")";
        dbInput.setName(name);

        if (selectedInputId == null)
            selectedInputId = (Long)session.save(dbInput);

        return list();
    }

    /*public Resolution createFileInputComplete() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        File selectedFile = (File)session.get(nl.b3p.datastorelinker.entity.File.class, selectedFileId);

        Inout fileInput = null;
        if (selectedInputId == null)
            fileInput = new Inout();
        else
            fileInput = (Inout)session.get(Inout.class, selectedInputId);

        fileInput.setType(Inout.Type.INPUT);
        fileInput.setDatatype(Inout.Datatype.FILE);
        fileInput.setFile(selectedFile);
        fileInput.setTableName(selectedTable);

        FileAction fileAction = new FileAction();
        fileAction.setContext(getContext());
        String name;
        try {
            name = fileAction.getFileNameRelativeToUploadDirPP(selectedFile);
        } catch (IOException ex) {
            log.error(ex);
            name = selectedFile.getName();
        }
        if (selectedTable != null && !selectedTable.equals(""))
            name += " (" + selectedTable + ")";
        fileInput.setName(name);

        if (selectedInputId == null)
            selectedInputId = (Long)session.save(fileInput);

        return list();
    }*/

    public Resolution createTablesList() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        if (selectedInputId != null) {
            Inout input = (Inout)session.get(Inout.class, selectedInputId);
            // only prefill selected table if we have saved this db with the input we are editing
            if (selectedDatabaseId.equals(input.getDatabase().getId()))
                selectedTable = input.getTableName();
        }

        Database selectedDatabase = (Database)session.get(Database.class, selectedDatabaseId);

        try {
            DataTypeList dataTypeList = DataStoreUtil.getDataTypeList(selectedDatabase.toMap());

            if (dataTypeList != null) {

                tables = dataTypeList.getGood();
                failedTables = dataTypeList.getBad();

                return new ForwardResolution(TABLE_LIST_JSP);
            } else {
                throw new Exception("Error getting datatypes from DataStore.");
            }
        } catch(Exception e) {
            String tablesError = "Fout bij ophalen tabellen. ";
            log.error(tablesError + e.getMessage());
            return new DefaultErrorResolution(tablesError);
        }
    }

    public Resolution getExampleRecord() {
        EntityManager em = JpaUtilServlet.getThreadEntityManager();
        Session session = (Session)em.getDelegate();

        Inout input = (Inout)session.get(Inout.class, selectedInputId);

        try {
            DataStore ds = null;
            String tableName = null;
            if (input.getDatabase() != null) {
                ds = DataStoreLinker.openDataStore(input.getDatabase());
                tableName = input.getTableName();
            } else if (input.getFile() != null) {
                ds = DataStoreLinker.openDataStore(input.getFile());
            } else {
                Exception ex = new Exception("unsupported input type.");
                log.error(ex);
                throw ex;
            }
            SimpleFeature feature = getExampleFeature(ds, tableName);

            columnNames = new ArrayList<String>();
            for (AttributeDescriptor desc : feature.getFeatureType().getAttributeDescriptors()) {
                String col = desc.getLocalName();
                String type = desc.getType().getBinding().getSimpleName();
                columnNames.add(col + "(" + type + ")");
            }
            recordValues = feature.getAttributes();
        } catch (Exception e) {
            log.error(e);
            return new DefaultErrorResolution(e.getMessage());
        }
        return new ForwardResolution(EXAMPLE_RECORD_JSP);
    }

    private SimpleFeature getExampleFeature(DataStore ds, String tableName) throws Exception {
        //log.debug((Object[])ds.getTypeNames());
        if (tableName == null) {
            if (ds.getTypeNames().length == 0)
                throw new IllegalArgumentException("no typeNames");
            tableName = ds.getTypeNames()[0];
        }

        FeatureCollection<SimpleFeatureType, SimpleFeature> fc =
                ds.getFeatureSource(tableName).getFeatures();

        Iterator<SimpleFeature> iterator = fc.iterator();
        try {
            if (iterator.hasNext())
                return iterator.next();
        } finally {
            fc.close(iterator);
        }

        throw new Exception("Geen features gevonden.");
    }

    public List<Inout> getInputs() {
        return inputs;
    }

    public void setInputs(List<Inout> inputs) {
        this.inputs = inputs;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public Long getSelectedDatabaseId() {
        return selectedDatabaseId;
    }

    public void setSelectedDatabaseId(Long selectedDatabaseId) {
        this.selectedDatabaseId = selectedDatabaseId;
    }

    public String getSelectedFilePath() {
        return selectedFilePath;
    }

    public void setSelectedFilePath(String selectedFilePath) {
        this.selectedFilePath = selectedFilePath;
    }

    public Long getSelectedInputId() {
        return selectedInputId;
    }

    public void setSelectedInputId(Long selectedInputId) {
        this.selectedInputId = selectedInputId;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public String getSelectedTable() {
        return selectedTable;
    }

    public void setSelectedTable(String selectedTable) {
        this.selectedTable = selectedTable;
    }

    public List<String> getFailedTables() {
        return failedTables;
    }

    public void setFailedTables(List<String> failedTables) {
        this.failedTables = failedTables;
    }

    public String getUploadDirectory() {
        return getContext().getServletContext().getInitParameter("uploadDirectory");
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Object> getRecordValues() {
        return recordValues;
    }

    public void setRecordValues(List<Object> recordValues) {
        this.recordValues = recordValues;
    }

}
