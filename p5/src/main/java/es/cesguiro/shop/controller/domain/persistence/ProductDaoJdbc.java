package es.cesguiro.shop.controller.domain.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

/**
 * Implementación JDBC del DAO de productos: consulta con PreparedStatement,
 * mapea el ResultSet a su entidad y devuelve la lista de resultados.
 * La paginación se resuelve con LIMIT y OFFSET a mano (sin Spring Data).
 */
@Repository
public class ProductDaoJdbc implements ProductDao {

    private static final String SELECT_BY_ID =
            "SELECT id, catalog_id, name, description, price, stock FROM products WHERE id = ?";
    private static final String SELECT_PAGE_BY_CATALOG_ID =
            "SELECT id, catalog_id, name, description, price, stock "
                    + "FROM products WHERE catalog_id = ? ORDER BY id LIMIT ? OFFSET ?";
    private static final String SELECT_COUNT_BY_CATALOG_ID =
            "SELECT COUNT(*) FROM products WHERE catalog_id = ?";
    private static final String INSERT =
            "INSERT INTO products (catalog_id, name, description, price, stock) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE products SET catalog_id = ?, name = ?, description = ?, price = ?, stock = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM products WHERE id = ?";

    private final DataSource dataSource;

    public ProductDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<ProductEntity> getById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el producto " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProductEntity> getPageByCatalogId(Long catalogId, int limit, int offset) {
        List<ProductEntity> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_PAGE_BY_CATALOG_ID)) {

            statement.setLong(1, catalogId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los productos del catálogo " + catalogId, e);
        }
        return products;
    }

    @Override
    public long countByCatalogId(Long catalogId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COUNT_BY_CATALOG_ID)) {

            statement.setLong(1, catalogId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar los productos del catálogo " + catalogId, e);
        }
        return 0;
    }

    @Override
    public Long save(ProductEntity product) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, product.getCatalogId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStock());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el producto " + product.getName(), e);
        }
        throw new RuntimeException("No se ha podido obtener el id generado del producto");
    }

    @Override
    public void update(ProductEntity product) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setLong(1, product.getCatalogId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStock());
            statement.setLong(6, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al modificar el producto " + product.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar el producto " + id, e);
        }
    }

    private ProductEntity mapRow(ResultSet resultSet) throws SQLException {
        return new ProductEntity(
                resultSet.getLong("id"),
                resultSet.getLong("catalog_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock")
        );
    }
}
