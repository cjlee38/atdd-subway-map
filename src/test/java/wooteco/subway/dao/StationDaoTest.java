package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import wooteco.subway.domain.Station;

@JdbcTest
class StationDaoTest {

    private static final String STATION_NAME = "청구역";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private StationDao dao;

    @BeforeEach
    void setUp() {
        dao = new JdbcStationDao(dataSource, jdbcTemplate);
    }

    @Test
    @DisplayName("역을 저장한다.")
    public void save() {
        // given
        Station station = new Station("청구역");
        // when
        final Optional<Station> saved = dao.save(station);
        // then
        assertThat(saved).isPresent();
    }

    @Test
    @DisplayName("중복된 이름을 저장하는 경우 빈 Optional을 돌려준다.")
    public void save_throwsExceptionWithDuplicatedName() {
        // given
        final Optional<Station> saved = dao.save(new Station("청구역"));
        // when
        final Optional<Station> duplicated = dao.save(new Station("청구역"));
        // then
        assertThat(duplicated).isEmpty();
    }

    @Test
    @DisplayName("역 목록을 불러온다.")
    public void findAll() {
        // given & when
        final List<Station> stations = dao.findAll();
        // then
        assertThat(stations).hasSize(0);
    }

    @Test
    @DisplayName("역을 하나 추가한 뒤, 역 목록을 불러온다.")
    public void findAll_afterSaveOneStation() {
        // given
        dao.save(new Station(STATION_NAME));
        // when
        final List<Station> stations = dao.findAll();
        // then
        assertThat(stations).hasSize(1);
    }

    @Test
    @DisplayName("ID값으로 역을 삭제한다.")
    public void deleteById() {
        // given
        final Station saved = dao.save(new Station(STATION_NAME)).orElseThrow(IllegalStateException::new);
        final Long id = saved.getId();
        // when
        final boolean isDeleted = dao.deleteById(id);
        // then
        assertThat(isDeleted).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 역을 삭제할 수 없다.")
    public void deleteById_doesNotExist() {
        // given
        final long id = 1L;

        // when
        final boolean isDeleted = dao.deleteById(id);

        // then
        assertThat(isDeleted).isFalse();
    }

    @AfterEach
    void setDown() {
        jdbcTemplate.update("DELETE FROM station");
    }
}