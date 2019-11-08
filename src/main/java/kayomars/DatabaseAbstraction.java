package kayomars;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * An object that provides links to a SQLite DB with only the functionality required.
 * 
 */
public class DatabaseAbstraction {
    
    
    private Connection dbConnection = null;
    
    /**
     * Constructor that makes a new instance of a database connection object.
     * @param dbNameWithExt The name of the database file as a String with a.db extension
     */
    public DatabaseAbstraction(String dbNameWithExt) {
        
        try {
            this.dbConnection = DriverManager.getConnection("jdbc:sqlite:" + dbNameWithExt);     
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }

    }
    
    /**
     * Closes the database connection object. Must be called at end of database query.
     */
    public void closeConnection() {
        try {
            this.dbConnection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage()); 
        }
    }
    
    /**
     * Creates a table named allVals in the connected database with "id" as the primary key and "vals" 
     * as associated values.
     */
    private void createTableInDatabase() {
        
        // SQL statement for creating a new table  
        String createTableStr = "CREATE TABLE IF NOT EXISTS allVals (\n"  
                + " id integer PRIMARY KEY,\n"  
                + " vals text \n"  
                + ");"; 
        
        try {
            Statement tableStmt = this.dbConnection.createStatement();
            tableStmt.execute(createTableStr);
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        
    }
    
    /**
     * Inserts a record into the allVals table.
     * @param lineNum is the "id" value of the record
     * @param valToAdd is the "vals" value of the record
     */
    private void insertRecordsInTable(long lineNum, String valToAdd) {
        
        String insertStr = "INSERT INTO allVals(id, vals) VALUES(?,?)";  
        
        try{  
            PreparedStatement stmtToExecute = this.dbConnection.prepareStatement(insertStr);  
            stmtToExecute.setLong(1, lineNum);  
            stmtToExecute.setString(2, valToAdd);  
            stmtToExecute.executeUpdate();  
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
    }
    

    /**
     * Gets a "vals" value from the allVals table.
     * @param lineNum is the "id" value of the record which must be a valid line number
     * @return the "vals" value of the associated "id"
     */
    public String getLineContent(long lineNum) {
        
        String selectStr = "SELECT vals FROM allVals WHERE id=" + String.valueOf(lineNum);  
        
        try {  
            Statement selectStmt = this.dbConnection.createStatement();  
            ResultSet queryRes = selectStmt.executeQuery(selectStr);  
               
            queryRes.next();
            return queryRes.getString("vals");
              
        } catch (SQLException e) {  
            System.out.println(e.getMessage());  
        }  
        
        return "";
    }
    
    
    /**
     * The function prepares the database during startup. It creates the database, creates the table in the database,
     * and inserts records into the table. This must be called during the server initialization process.
     * The lines are 0-indexed. So lines start from number 0.
     * @param fileName is the name of the text file containing lines of data
     * @return the total number of lines that are contained in the text file
     */
    public int prepareDatabase(String fileName) throws IOException {
        
        final BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int numOfLines = 0;
        
        // Add relevant table
        this.createTableInDatabase();
       
        String lineText = reader.readLine();
        
        while (lineText != null) {
            
            this.insertRecordsInTable(numOfLines, lineText);
            numOfLines++;
              
            lineText = reader.readLine();
        }
        
        reader.close();
     
        return numOfLines;
        
    }

}
