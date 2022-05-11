package wooteco.subway.dao;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import wooteco.subway.entity.LineEntity;

@Repository
public class JdbcLineDao implements LineDao {

    private static final RowMapper<LineEntity> LINE_ROW_MAPPER = (resultSet, rowNum) -> new LineEntity(
        resultSet.getLong("id"),
        resultSet.getString("name"),
        resultSet.getString("color")
    );

    private final SimpleJdbcInsert jdbcInsert;
    private final JdbcTemplate jdbcTemplate;

    public JdbcLineDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("line")
            .usingGeneratedKeyColumns("id");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LineEntity save(LineEntity entity) {
        final SqlParameterSource param = new BeanPropertySqlParameterSource(entity);
        final Long id = jdbcInsert.executeAndReturnKey(param).longValue();
        return new LineEntity(id, entity.getName(), entity.getColor());
    }

    @Override
    public List<LineEntity> findAll() {
        final String sql = "SELECT * FROM line";
        return jdbcTemplate.query(sql, LINE_ROW_MAPPER);
    }

    @Override
    public Optional<LineEntity> findById(Long id) {
        final String sql = "SELECT * FROM line WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, LINE_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LineEntity> findByName(String name) {
        final String sql = "SELECT * FROM line WHERE name = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, LINE_ROW_MAPPER, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean update(LineEntity entity) {
        final String sql = "UPDATE line SET name = ?, color = ? WHERE id = ?";
        final int updatedCount = jdbcTemplate.update(sql, entity.getName(), entity.getColor(), entity.getId());
        return isUpdated(updatedCount);
    }

    private boolean isUpdated(int updatedCount) {
        return updatedCount == 1;
    }

    @Override
    public boolean delete(Long id) {
        final String sql = "DELETE FROM line WHERE id = ?";
        final int deletedCount = jdbcTemplate.update(sql, id);
        return isUpdated(deletedCount);
    }
}
