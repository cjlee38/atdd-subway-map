package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import wooteco.subway.domain.Line;

@JdbcTest
class LineDaoTest {

    private static final String LINE_NAME = "신분당선";
    private static final String LINE_COLOR = "bg-red-600";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LineDao dao;

    @BeforeEach
    void setUp() {
        dao = new JdbcLineDao(dataSource, jdbcTemplate);
    }

    @Test
    @DisplayName("노선을 저장한다.")
    public void save() {
        // given
        Line Line = new Line(LINE_NAME, LINE_COLOR);
        // when
        final Line saved = dao.save(Line);
        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("중복된 이름을 저장하는 경우 예외를 던진다.")
    public void save_throwsExceptionWithDuplicatedName() {
        // given & when
        dao.save(new Line(LINE_NAME, LINE_COLOR));
        // then
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> dao.save(new Line(LINE_NAME, LINE_COLOR)));
    }

    @Test
    @DisplayName("전체 노선을 조회한다.")
    public void findAll() {
        // given & when
        List<Line> lines = dao.findAll();
        // then
        assertThat(lines).hasSize(0);
    }

    @Test
    @DisplayName("노선을 하나 추가한 뒤, 전체 노선을 조회한다")
    public void findAll_afterSaveOneLine() {
        // given
        dao.save(new Line(LINE_NAME, LINE_COLOR));
        // when
        List<Line> lines = dao.findAll();
        // then
        assertThat(lines).hasSize(1);
    }

    @Test
    @DisplayName("ID 값으로 노선을 조회한다")
    public void findById() {
        // given
        final Line saved = dao.save(new Line(LINE_NAME, LINE_COLOR));
        // when
        final Line found = dao.findById(saved.getId());
        // then
        Assertions.assertAll(
            () -> assertThat(found.getId()).isEqualTo(saved.getId()),
            () -> assertThat(found.getName()).isEqualTo(saved.getName()),
            () -> assertThat(found.getColor()).isEqualTo(saved.getColor())
        );
    }

    @Test
    @DisplayName("존재하지 않는 ID 값으로 노선을 조회하면 예외를 던진다")
    public void findById_invalidID() {
        // given & when
        dao.save(new Line(LINE_NAME, LINE_COLOR));
        // then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> dao.findById(2L));
    }

    @Test
    @DisplayName("노선 정보를 수정한다.")
    public void update() {
        // given & when
        final Line saved = dao.save(new Line(LINE_NAME, LINE_COLOR));
        // then
        assertThatNoException()
            .isThrownBy(() -> dao.update(new Line(saved.getId(), "구분당선", LINE_COLOR)));
    }

    @Test
    @DisplayName("존재하지 않는 ID값을 수정하는 경우 예외를 던진다.")
    public void update_throwsExceptionWithInvalidId() {
        // given
        dao.save(new Line(LINE_NAME, LINE_COLOR));
        // when
        Line updateLine = new Line(100L, "사랑이넘치는", "우테코");
        // then
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> dao.update(updateLine));
    }

    @Test
    @DisplayName("ID값으로 노선을 삭제한다.")
    public void delete() {
        // given & when
        Line saved = dao.save(new Line(LINE_NAME, LINE_COLOR));

        // then
        assertThatNoException()
            .isThrownBy(() -> dao.delete(saved.getId()));
    }

    @Test
    @DisplayName("존재하지않는 ID값을 삭제하는 경우 예외를 던진다.")
    public void delete_throwsExceptionWithInvalidId() {
        // given
        dao.save(new Line(LINE_NAME, LINE_COLOR));
        // when
        Long deleteId = 100L;
        // then
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> dao.delete(deleteId));
    }

    @AfterEach
    void setDown() {
        final String sql = "DELETE FROM line";
        jdbcTemplate.update(sql);
    }
}