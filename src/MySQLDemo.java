/**
 * <strong>MySQLDemo.java</strong> - description
 *
 *         Based on original code from: http://www.ccs.neu.edu/home/kathleen/classes/cs3200/JDBCtutorial.pdf
 * @author Peter K. Johnson - <a href="http://WebExplorations.com"
 *         target="_blank"> http://WebExplorations.com</a><br >
 *         Written: Oct 27, 2014<br >
 *         Revised: Oct 31, 2014
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This class demonstrates how to connect to MySQL and run some basic commands.
 * 
 * In order to use this, you have to download the Connector/J driver and add
 * its .jar file to your build path.  You can find it here:
 * http://dev.mysql.com/downloads/connector/j/
 * 
 * ERROR MESSAGES?
 * ERROR: Could not connect to the databasecom.mysql.jdbc.exceptions.jdbc4.
 * CommunicationsException: Communications link failure
 * Solution: Make sure MAMP/WAMP is running ;-)
 * 
 * ERROR: java.sql.SQLException: No suitable driver found for jdbc:mysql://localhost:3306/
 * The JDBC Driver is not on the path (Eclipse is not aware of it.)
 * To add it to your class path:i
 * 1. Right click on your project
 * 2. Go to Build Path -> Add External Archives...
 * 3. Select the file mysql-connector-java-5.1.24-bin.jar
 *    NOTE: If you have a different version of the .jar file, the name may be a little different.
 *
 * ERROR: java.sql.SQLException: Access denied for user 'userName'@'localhost' (using password: YES)
 * The userID and password are wrong.
 * The user name and password are both "root" or "root" "" depending on which LAMP stack you are running.
 * You can change these setting below.
 */
public class MySQLDemo 
{
    // Set up two constants to be used in this test environment.
    private final static String DBF_NAME = "javasql"; 
    private final static String TABLE_NAME = "customer";

    // The mySQL username
    private final String userName = "root";

    // The mySQL password (may be empty "" )
    private final String password = "root";

    // Name of the computer running mySQL
    private final String serverName = "localhost";

    // Port of the MySQL server (default is 3306 or 8889 on MAMP)
    private final int portNumber = 8889;


    public static void main(String[] args) 
    {      
        // Simulate data input by user, stored in an array
        String[ ] dataInput = {"May", "999 West Avenue", "Laurel", "TN", "22245"};
        MySQLDemo app = new MySQLDemo();
        
        app.createTable(TABLE_NAME);
        app.insertData(dataInput, TABLE_NAME);
        app.showTable(TABLE_NAME);
        // Simulate new data input by user
        dataInput[0] = "Chris";
        dataInput[1] = "111 One Ave NE";
        dataInput[2] = "Princeton";
        dataInput[3] = "CA";
        dataInput[4] = "99987";
        app.update(dataInput, 5    , TABLE_NAME);  
        app.showTable(TABLE_NAME);
        app.delete(4,TABLE_NAME);
        app.showTable(TABLE_NAME);
        
        //app.dropTable(TABLE_NAME);
    } // end of main( )
    
    /**
     * createTable - create a new table in the database
     *               if one already exists no action is taken
     * @param tableName
     */
    public void createTable(String tableName) 
    {
        Connection conn = null;
        String sql = "";
        ResultSet rs = null;
        boolean createTable = true;
        
        // Connect to MySQL
        try 
        {
           conn = this.getConnection();
        } 
        catch (SQLException e) 
        {
            System.out.println("ERROR: Could not connect to the database");
            e.printStackTrace();
            return;
        }

        // Create a table
        try 
        {
           // Check to see if a table already exists
           DatabaseMetaData meta = conn.getMetaData();
           rs = meta.getTables(null, null, "%", null);
           // Does the table already exist?
           // Loop through looking for the table name
           while (rs.next()) 
           {
              if(rs.getString(3).equals(tableName))
              {
                 createTable=false;
                 break;
              }
            }  

           if(createTable)
           {
              String createString =
                "CREATE TABLE " + tableName + " ( " +
                "id INTEGER NOT NULL AUTO_INCREMENT, " +
                "name varchar(40) NOT NULL, " +
                "street varchar(40) NOT NULL, " +
                "city varchar(20) NOT NULL, " +
                "state char(2) NOT NULL, " +
                "postalcode char(5), " +
                "PRIMARY KEY (id))";
              this.executeUpdate(conn, createString);
              System.out.println("Created table named:" + tableName);
            }
            else // table already exists
            {
               System.out.println(tableName + " already exists. No action taken.");           
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("ERROR: Could not create the table named: " + tableName);
            e.printStackTrace();
            return;
        }
        // release the resources
        finally { releaseResource(rs, null, conn); }
    } // end of createTable( )

 
    /**
     * delete( ) - remove a record based on id
     * @param thisID - the id of the record to be removed
     * @param thisTable
     */
    public void delete(int thisID, String thisTable)
    {
       Connection conn = null;
       String sql = "";
       
       try 
       {
          conn = this.getConnection();
       } catch (SQLException e) {
          System.out.println("ERROR: Could not connect to the database");
          e.printStackTrace();
       }
       
       // Delete a record
       try 
       {
          /* REFERENCE SQL:
           *  sql = "DELETE FROM customer WHERE id = 15";
           */
           sql = "DELETE FROM " + thisTable + " WHERE id = " + thisID;
           this.executeUpdate(conn, sql);
        } 
        catch (SQLException e) 
        {             
           System.out.println("ERROR: Could delete the record using this SQL: " + sql);
           e.printStackTrace();
        }
       // Release the resources
       finally { releaseResource(null, null, conn); }
    }// end of delete( )

    /**
     * dropTable - removes the specific table from the database
     * @param tableName
     */
    public void dropTable(String tableName)
    {
       String sql = "";
       Connection conn = null;
       try 
       {
          conn = this.getConnection();
       } 
       catch (SQLException e) 
       {
          System.out.println("ERROR: Could not connect to the database");
          e.printStackTrace();
       }
       try 
       {
          sql = "DROP TABLE " + tableName;
          this.executeUpdate(conn, sql);
          System.out.println("Dropped the table named:" + tableName);
       } 
       catch (SQLException e) 
       {
          System.out.println("ERROR: Could not drop the table using this SQL: " + sql);
          e.printStackTrace();
          return;
      }
       finally { releaseResource(null,null, conn);}
    } // end of dropTable( )   

    
    /**
     * insertData - inserts data from the array into the designated table
     * @param dataArray  - Must be in the indexed order:<br>
     *                     0-name 1-street 2-city 3-state 4-postalcode
     * @param thisTable
     */
    public void insertData(String[ ] dataArray, String thisTable)
    {
       Connection conn = null;
       String sql = "";
       try 
       {
          conn = this.getConnection();
       } catch (SQLException e) {
          System.out.println("ERROR: Could not connect to the database");
          e.printStackTrace();
       }
       
       // Insert the data  
       try 
       {
          /* REFERENCE SQL:
           *  sql = "INSERT INTO customer (name, street, city, state, postalcode)"
           *    + "VALUES ('Tom B. Erichsen','Skagen 21','Stavanger','MN','4006')";
           */
           sql = "INSERT INTO " + thisTable + " (name, street, city, state, postalcode) VALUES("
             + "'" + dataArray[0] + "', "
             + "'" + dataArray[1] + "', "
             + "'" + dataArray[2] + "', "
             + "'" + dataArray[3] + "', "
             + "'" + dataArray[4] + "')";
           this.executeUpdate(conn, sql);
           System.out.println("Inserted a record:" + dataArray[0] + " " + dataArray[1]);
        } 
        catch (SQLException e) 
        {             
           System.out.println("ERROR: Could not insert the data using this SQL: " + sql);
           e.printStackTrace();
        }
       // Release the resources
       finally { releaseResource(null, null, conn); }
    }// end of insertData( )
 

    /**
     * showTable( ) - display the contents of the designated table
     * @param tableName
     */
    public void showTable(String tableName)
    {
       String sql = "";
       Statement stmt = null;
       ResultSet rs = null;
       int id = 0;
       String name = "";
       String street = "";
       String city = "";
       String state = "";
       String postalcode = "";
       
       // Connect to MySQL
       Connection conn = null;
       try {
          conn = this.getConnection();
       } 
       catch (SQLException e) 
       {
          System.out.println("ERROR: Could not connect to the database");
             e.printStackTrace();
       }
       
        // Select the data
        try 
        {        
           sql = "SELECT * FROM customer";
           // Run the SQL and save in Result Set
           stmt = conn.createStatement( );
           rs = stmt.executeQuery(sql);
           System.out.println("\nID\tNAME\t\tSTREET\t\t\tCITY - STATE - ZIP");
           System.out.println("**************************************************************************");
           while (rs.next()) 
           {
              id = rs.getInt("id");
              name       = rs.getString("name");
              street     = rs.getString("street");
              city       = rs.getString("city");
              state      = rs.getString("state");
              postalcode = rs.getString("postalcode");
              System.out.printf("%d\t%s\t\t%s\t\t%s, %s %s.\n", 
                    id, name, street, city, state, postalcode);
            }
          } 
          catch (SQLException e) 
          {             
             System.out.println("ERROR: Could not SELECT data using this SQL: " + sql);
                e.printStackTrace();
          }
          // Release the resources
          finally { releaseResource(rs, stmt, conn); }
    } // end of showTable( )
    
    
    /**
     * update( ) - update a specific record using the contents of the array based on a specific ID
     * @param dataArray Must be in the indexed order:<br>
     *                  0-name 1-street 2-city 3-state 4-postalcode
     * @param thisID
     * @param thisTable
     */
    public void update(String[ ] dataArray, int thisID, String thisTable)
    {
       Connection conn = null;
       String sql = "";
       
       try 
       {
          conn = this.getConnection();
       } 
       catch (SQLException e) 
       {
          System.out.println("ERROR: Could not connect to the database");
          e.printStackTrace();
       }
       
       // Update a record
       try 
       {
          /* REFERENCE SQL:            
           * sql = "UPDATE Customer "
           *      + "SET name='Alicia', street='333 Happy Ave', "
           *      + "city='Bangor', state='ME', postalcode='12333' "
           *      + "WHERE id=30";
           */
           sql = "UPDATE " + thisTable
                 + " SET name='"  + dataArray[0] + "', "
                 + "street='"     + dataArray[1] + "', "
                 + "city='"       + dataArray[2] + "', "
                 + "state='"      + dataArray[3] + "', "
                 + "postalcode='" + dataArray[4] + "' "
                 + "WHERE id=" + thisID;
           this.executeUpdate(conn, sql);
        } 
        catch (SQLException e) 
        {             
           System.out.println("ERROR: Could not update the record using this SQL: " + sql);
           e.printStackTrace();
        }
       // Release the resources
       finally { releaseResource(null, null, conn); }
    }// end of insertData( )
    

    /* ******************************
     * HEAVY LIFTING - Common to the CRUD methods.
     *********************************/
     /**
     * getConnection( ) - Get a new database connection
     * 
     * @return Connection
     * @throws SQLException
     */
     public Connection getConnection() throws SQLException 
     {
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);

       conn = DriverManager.getConnection("jdbc:mysql://"
                + this.serverName + ":" + this.portNumber + "/" + DBF_NAME,
                connectionProps);
 
        return conn;
     }
    
    
    /**
     * executeUpdate( ) - Used to run a SQL command which does NOT return a resultSet:
     * CREATE/INSERT/UPDATE/DELETE/DROP
     * 
     * @throws SQLException If something goes wrong
     * @return boolean if command was successful or not
     */
     public boolean executeUpdate(Connection conn, String command) throws SQLException 
     {
        Statement stmt = null;
        try 
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(command); // This will throw a SQLException if it fails
            return true;
        } 
        finally 
        {
            // This will run whether we throw an exception or not
            if (stmt != null) { stmt.close(); }
        }
    } // end of executeUpdate( )
 
    
    /**
     * executeQuery - Run a SQL command which returns a result set:
     * SELECT
     * 
     * @throws SQLException If something goes wrong
     * @return ResultSet containing data from the table
     */
    public ResultSet executeQuery(Connection conn, String command) throws SQLException 
    {
       ResultSet rs; 
       Statement stmt = null;
        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(command); // This will throw a SQLException if it fails
            return rs;
        } 
        finally 
        {
            // This will run whether we throw an exception or not
            if (stmt != null) { stmt.close(); }           
        }
    } // end of executeQuery( )
    
    
    /**
     * releaseResource( ) - Free up the system resources that were opened.
     *                      If not used,  a null will be passed in for that parameter.
     * @param rs - Resultset
     * @param ps - Statement
     * @param conn - Connection
     */
    public void releaseResource(ResultSet rs, Statement ps, Connection conn )
    {
       if (rs != null) 
       {
          try { rs.close(); } 
          catch (SQLException e) { /* ignored */}
       }
       if (ps != null) 
       {
          try { ps.close(); } 
          catch (SQLException e) { /* ignored */}
       }
       if (conn != null) 
       {
          try { conn.close();}
          catch (SQLException e) { /* ignored */}
       }
    } // end of releaseResource( )
}
