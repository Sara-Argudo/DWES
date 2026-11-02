package es.cesguiro.shop.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

/**
 * Implementación JDBC del DAO: consulta con PreparedStatement,
 * mapea el ResultSet a su entidad y devuelve la lista de resultados.
 */
@Repository
public class ProductDaoJdbc implements ProductDao {

    private static final String SELECT_ALL =
            "SELECT id, catalog_id, name, description, price, stock FROM products ORDER BY id";

    private final DataSource dataSource;

    public ProductDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ProductEntity> getAll() {
        List<ProductEntity> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los productos", e);
        }
        return products;
    }

    private ProductEntity mapRow(ResultSet resultSet) throws SQLException {
        return new ProductEntity(
                resultSet.getLong("id"),
                resultSet.getLong("catalog_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                BigDecimal.valueOf(resultSet.getDouble("price")),
                resultSet.getInt("stock")
        );
    }
}
