package by.antohakon.xmlimportertodb.repository;

import by.antohakon.xmlimportertodb.dto.OfferDto;
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

    public void updateCurrenciesDb(String id, String rate) throws SQLException {


//        String sql = "INSERT INTO currencies (id, rate) " +
//                "VALUES (?, ?) " +
//                "ON CONFLICT (id) DO UPDATE SET " +
//                "rate = EXCLUDED.rate";

        String sql = "INSERT INTO currencies (id, rate) VALUES (?, ?)";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setDouble(2, Double.parseDouble(rate));

        pstmt.executeUpdate();

        pstmt.close();
       // connection.close();
    }

    public void updateCategoriesDb(String id, String parentId, String name) throws SQLException {

//        String sql = "INSERT INTO categories (id, parentId, name) " +
//                "VALUES (?, ?, ?) " +
//                "ON CONFLICT (id) DO UPDATE SET " +
//                "parentId = EXCLUDED.parentId, " +
//                "name = EXCLUDED.name";

        String sql = "INSERT INTO categories (id, parentId, name) VALUES (?, ?, ?)";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, Integer.parseInt(id));
        pstmt.setInt(2, Integer.parseInt(parentId));
        pstmt.setString(3, name);

        pstmt.executeUpdate();

        pstmt.close();
       // connection.close();
    }

    public void updateOffersDb(OfferDto offerDto) throws SQLException {

//        StringBuilder sqlBuilder = new StringBuilder();
//        sqlBuilder.append("INSERT INTO offers (id, available, url, price, currencyId, categoryId, picture, name, vendor, vendorCode, description, param, count) ");
//        sqlBuilder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
//        sqlBuilder.append("ON CONFLICT (id) DO UPDATE SET ");
//        sqlBuilder.append("available = EXCLUDED.available, ");
//        sqlBuilder.append("url = EXCLUDED.url, ");
//        sqlBuilder.append("price = EXCLUDED.price, ");
//        sqlBuilder.append("currencyId = EXCLUDED.currencyId, ");
//        sqlBuilder.append("categoryId = EXCLUDED.categoryId, ");
//        sqlBuilder.append("picture = EXCLUDED.picture, ");
//        sqlBuilder.append("name = EXCLUDED.name, ");
//        sqlBuilder.append("vendor = EXCLUDED.vendor, ");
//        sqlBuilder.append("vendorCode = EXCLUDED.vendorCode, ");
//        sqlBuilder.append("description = EXCLUDED.description, ");
//        sqlBuilder.append("param = EXCLUDED.param, ");
//        sqlBuilder.append("count = EXCLUDED.count");

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO offers (id, available, url, price, currencyId, categoryId, picture, name, vendor, vendorCode, description, param, count) ");
        sqlBuilder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        String sql = sqlBuilder.toString();

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, Integer.parseInt(offerDto.id()));
        pstmt.setBoolean(2, Boolean.parseBoolean(offerDto.available()));
        pstmt.setString(3,offerDto.url());
        pstmt.setInt(4, Integer.parseInt(offerDto.price()));
        pstmt.setString(5,offerDto.currencyId());
        pstmt.setInt(6, Integer.parseInt(offerDto.categoryId()));
        pstmt.setString(7, offerDto.picture());
        pstmt.setString(8, offerDto.name());
        pstmt.setString(9, offerDto.vendor());
        pstmt.setString(10, offerDto.vendorCode());
        pstmt.setString(11, offerDto.description());
        pstmt.setString(12, offerDto.param());
        pstmt.setInt(13, Integer.parseInt(offerDto.count()));

        pstmt.executeUpdate();

        pstmt.close();
      //  connection.close();
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
