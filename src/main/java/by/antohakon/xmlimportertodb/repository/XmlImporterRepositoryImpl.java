package by.antohakon.xmlimportertodb.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
@Slf4j
public class XmlImporterRepositoryImpl implements XmlImporterRepository {

    private final String url;
    private final String username;
    private final String password;
    private final String driver;
    private Connection connection;

    public XmlImporterRepositoryImpl(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            log.info("СОЕДИНЕНИЕ НОРМ ANTOXA");
        } catch (ClassNotFoundException | SQLException e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public List<String> getAllTableNames() {
        return List.of();
    }

    @Override
    public String getTableDDL(String tableName) {
        return "";
    }

    @Override
    public void updateAllTables() {

    }

    @Override
    public void updateTableByName(String tableName) {

    }

    @Override
    public List<String> getColumnNames() {
        return List.of();
    }

    @Override
    public boolean isColumnId(String tablName, String columnName) {
        return false;
    }

    @Override
    public String getDDlChange(String tableName) {
        return "";
    }
}
