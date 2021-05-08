package wooteco.subway.line;


import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.web.dto.LineResponse;

@DisplayName("노선 인수 테스트")
public class LineAcceptanceTest extends AcceptanceTest {

    private static final Map<String, String> DATA1 = new HashMap<>();
    private static final Map<String, String> DATA2 = new HashMap<>();
    private static final Map<String, String> DATA_FOR_UPDATE = new HashMap<>();
    private static final Map<String, String> DATA_EMPTY_STRING = new HashMap<>();
    private static final Map<String, String> DATA_NULL = new HashMap<>();

    private static final String LINES_PATH = "/lines/";
    private static final String LOCATION = "Location";
    private static final String NAME = "name";
    private static final String COLOR = "color";
    private static final long INVALID_ID = Long.MAX_VALUE;

    static {
        put(DATA1, "신분당선", "bg-red-600");
        put(DATA2, "2호선", "bg-green-600");
        put(DATA_FOR_UPDATE, "수정된 이름", "수정된 색");
        put(DATA_EMPTY_STRING, "", "");
        put(DATA_NULL, null, null);
    }

    private static void put(Map<String, String> data, String name, String color) {
        data.put(NAME, name);
        data.put(COLOR, color);
    }

    @Test
    @DisplayName("노선 생성")
    void create() {
        // when
        ExtractableResponse<Response> response = postLine(DATA1);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        LineResponse lineResponse = response.body().jsonPath().getObject(".", LineResponse.class);
        assertLineResponse(lineResponse, DATA1);
    }

    @Test
    @DisplayName("중복이름 노선 생성불가")
    void createFail_duplicatedName() {
        // when
        ExtractableResponse<Response> response1 = postLine(DATA1);
        ExtractableResponse<Response> response2 = postLine(DATA1);

        // then
        assertThat(response1.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response2.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("name,color 빈 문자열: 노선 생성불가")
    void createFail_emptyString() {
        // when
        ExtractableResponse<Response> response = postLine(DATA_EMPTY_STRING);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("name,color null: 노선 생성불가")
    void createFail_null() {
        // when
        ExtractableResponse<Response> response = postLine(DATA_NULL);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("노선 목록 조회")
    void listLines() {
        /// given
        ExtractableResponse<Response> postResponse1 = postLine(DATA1);
        ExtractableResponse<Response> postResponse2 = postLine(DATA2);

        // when
        ExtractableResponse<Response> listResponse = listLine();

        // then
        assertThat(listResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<LineResponse> expectedLines = toLineDtos(postResponse1, postResponse2);
        List<LineResponse> results = listResponse.jsonPath().getList(".", LineResponse.class);

        assertThat(results).containsAll(expectedLines);
    }

    private List<LineResponse> toLineDtos(ExtractableResponse<Response>... responses) {
        return Arrays.stream(responses)
                .map(response -> response.jsonPath().getObject(".", LineResponse.class))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("노선 수정")
    void updateLine() {
        // given
        ExtractableResponse<Response> postResponse = postLine(DATA1);

        // when
        String uri = postResponse.header(LOCATION);
        ExtractableResponse<Response> updateResponse = putLine(DATA_FOR_UPDATE, uri);

        // then
        assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

        LineResponse lineResponse = getLine(uri).jsonPath()
                .getObject(".", LineResponse.class);
        assertLineResponse(lineResponse, DATA_FOR_UPDATE);
    }

    @Test
    @DisplayName("이름에 빈 문자열: 노선 수정불가")
    void updateFail_emptyString() {
        // given
        ExtractableResponse<Response> response1 = postLine(DATA1);
        String uri = response1.header(LOCATION);

        // when
        ExtractableResponse<Response> putResponse = putLine(DATA_EMPTY_STRING, uri);

        // then
        assertThat(putResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("이름에 null: 노선 수정불가")
    void updateFail_null() {
        // given
        ExtractableResponse<Response> response1 = postLine(DATA1);
        String uri = response1.header(LOCATION);

        // when
        ExtractableResponse<Response> putResponse = putLine(DATA_NULL, uri);

        // then
        assertThat(putResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("존재하지 않는 노선 수정불가")
    void updateLineByInvalidId() {
        // when
        ExtractableResponse<Response> response = putLine(DATA_FOR_UPDATE, LINES_PATH + INVALID_ID);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("노선 삭제")
    void deleteLine() {
        // given
        ExtractableResponse<Response> postResponse1 = postLine(DATA1);
        ExtractableResponse<Response> postResponse2 = postLine(DATA2);
        String uri = postResponse1.header(LOCATION);

        // when
        ExtractableResponse<Response> response = deleteLine(uri);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        List<LineResponse> lineResponses = listLine().jsonPath()
                .getList(".", LineResponse.class);
        assertThat(lineResponses.size()).isEqualTo(1);
        assertLineResponse(lineResponses.get(0), DATA2);
    }

    @Test
    @DisplayName("존재하지 않는 노선 삭제불가")
    void deleteLineByInvalidId() {
        // when
        ExtractableResponse<Response> response = deleteLine(LINES_PATH + INVALID_ID);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private ExtractableResponse<Response> postLine(Map<String, String> data) {
        return getRequestSpecification()
                .body(data)
                .post(LINES_PATH)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> listLine() {
        return getRequestSpecification()
                .get(LINES_PATH)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> getLine(String path) {
        return getRequestSpecification()
                .get(path)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> putLine(Map<String, String> data, String path) {
        return getRequestSpecification()
                .body(data)
                .put(path)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> deleteLine(String path) {
        return getRequestSpecification()
                .delete(path)
                .then().log().all()
                .extract();
    }

    private RequestSpecification getRequestSpecification() {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    private void assertLineResponse(LineResponse result, Map<String, String> expected) {
        assertThat(result.getName()).isEqualTo(expected.get(NAME));
        assertThat(result.getColor()).isEqualTo(expected.get(COLOR));
    }
}