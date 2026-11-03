package es.cesguiro.shop.persistence;

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
 * Implementación JDBC del DAO de catálogos: consulta con PreparedStatement,
 * mapea el ResultSet a su entidad y devuelve la lista de resultados.
 */
@Repository
public class CatalogDaoJdbc implements CatalogDao {

    private static final String SELECT_ALL =
            "SELECT id, name, description FROM catalogs ORDER BY id";
    private static final String SELECT_BY_ID =
            "SELECT id, name, description FROM catalogs WHERE id = ?";
    private static final String SELECT_BY_NAME =
            "SELECT id, name, description FROM catalogs WHERE name = ?";
    private static final String INSERT =
            "INSERT INTO catalogs (name, description) VALUES (?, ?)";
    private static final String UPDATE =
            "UPDATE catalogs SET name = ?, description = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM catalogs WHERE id = ?";

    private final DataSource dataSource;

    public CatalogDaoJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CatalogEntity> getAll() {
        List<CatalogEntity> catalogs = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                catalogs.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los catálogos", e);
        }
        return catalogs;
    }

    @Override
    public Optional<CatalogEntity> getById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el catálogo " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CatalogEntity> findByName(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {

            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el catálogo " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(CatalogEntity catalog) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, catalog.getName());
            statement.setString(2, catalog.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el catálogo " + catalog.getName(), e);
        }
        throw new RuntimeException("No se ha podido obtener el id generado del catálogo");
    }

    @Override
    public void update(CatalogEntity catalog) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, catalog.getName());
            statement.setString(2, catalog.getDescription());
            statement.setLong(3, catalog.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al modificar el catálogo " + catalog.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar el catálogo " + id, e);
        }
    }

    private CatalogEntity mapRow(ResultSet resultSet) throws SQLException {
        return new CatalogEntity(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
