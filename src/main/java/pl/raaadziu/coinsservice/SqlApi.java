package pl.raaadziu.coinsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SqlApi
{
    private Connection con;
    private Logger logger;

    @Autowired
    public SqlApi(Configuration configuration) throws SqlApiException
    {
        String connectionString = configuration.getSqlString();
        logger = LoggerFactory.getLogger("SqlDbLog");
        logger.info("Try to init sql connection with sql string: " + connectionString);
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionString);
            logger.info("SQL INIT OK");
        }
        catch (SQLException | ClassNotFoundException e) {
            System.err.print(e.getMessage());
            throw new SqlApiException("SQL problem with connection to database :" + e.getMessage());
        }
    }

    private void printHead(ResultSetMetaData md) throws SQLException
    {
        int columns = md.getColumnCount();
        logger.debug("RAW RESPONSE: ");
        StringBuilder columnNames = new StringBuilder();
        for(int i = 1; i <= columns; ++i)
        {
            columnNames.append("[");
            columnNames.append(md.getColumnName(i));
            columnNames.append("] ");
        }
        logger.debug("   " + columnNames.toString());
    }

    List<Map<String, Object>> query(String s) throws SqlApiException
    {
        Statement stmt = null;
        ResultSet rs;
        logger.debug("RAW REQ: " + s);
        try
        {
            // Create and execute an SQL statement that returns some data.
            stmt = con.createStatement();
            boolean gotResults = stmt.execute(s);
            if(!gotResults){
                logger.debug(" RESPONSE: no results");
                return null;
            } else {
                rs = stmt.getResultSet();
            }

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();
            int first = 0;
            while (rs.next())
            {
                first++;
                if (first==1) printHead(md);
                Map<String, Object> row = new HashMap<>(columns);
                StringBuilder data = new StringBuilder();
                for(int i = 1; i <= columns; ++i)
                {
                    row.put(md.getColumnName(i), rs.getObject(i));
                    data.append(rs.getObject(i));
                    data.append("  ");
                }
                logger.debug("   " + data);
                rows.add(row);
            }
            if (first == 0) logger.debug("no records");
            stmt.getMoreResults();
            return rows;
        }
        catch (SQLException e) {
            throw new SqlApiException("SQL Exception ","<" + e.getMessage() + "> with SQL State <" + e.getSQLState() + ">",e.getErrorCode());
        }
        catch (Exception e) {  // other exception
            throw new SqlApiException("SQL Query other error ",e.getMessage(),0);
        }
        finally {
            if (stmt != null)
            {
                try { stmt.close(); }
                catch(SQLException e)
                {
                    throw new SqlApiException("SQL Statement Close error ",e.getMessage(),0);
                }
            }
        }
    }
}

