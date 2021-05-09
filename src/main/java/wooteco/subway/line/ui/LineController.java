package wooteco.subway.line.ui;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wooteco.subway.line.domain.Line;
import wooteco.subway.line.domain.section.Section;
import wooteco.subway.line.domain.section.Sections;
import wooteco.subway.line.domain.value.LineColor;
import wooteco.subway.line.domain.value.LineId;
import wooteco.subway.line.domain.value.LineName;
import wooteco.subway.line.service.LineService;
import wooteco.subway.line.ui.dto.LineRequest;
import wooteco.subway.line.ui.dto.LineResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("lines")
public class LineController {

    private final LineService lineService;

    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineResponse> createNewLine(@RequestBody LineRequest lineRequest) throws URISyntaxException {
        final Section section = new Section(
                lineRequest.getUpStationId(),
                lineRequest.getDownStationId(),
                lineRequest.getDistance()
        );

        final Sections sections = new Sections(Collections.singletonList(section));

        final Line line = new Line(
                new LineName(lineRequest.getName()),
                new LineColor(lineRequest.getColor()),
                sections
        );

        final Line savedLine = lineService.save(line);

        return ResponseEntity
                .created(URI.create("/lines/" + savedLine.getLineId()))
                .body(
                        new LineResponse(
                                savedLine.getLineId(),
                                savedLine.getLineName(),
                                savedLine.getLineColor()
                        )
                );
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LineResponse>> allLines() {
        final List<LineResponse> lineResponses = lineService.allLines().stream()
                .map(line ->
                        new LineResponse(
                                line.getLineId(),
                                line.getLineName(),
                                line.getLineColor()
                        )
                ).collect(toList());

        return ResponseEntity.ok(lineResponses);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineResponse> findById(@PathVariable Long id) {
        final Line line = lineService.findById(id);

        return ResponseEntity.ok(
                new LineResponse(
                        line.getLineId(),
                        line.getLineName(),
                        line.getLineColor()
                )
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> modifyById(@PathVariable Long id, @RequestBody LineRequest lineRequest) {
        final Line line = new Line(id, lineRequest.getName(), lineRequest.getColor());

        lineService.update(line);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        lineService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> duplicationKeyExceptionHandle(Exception e) {
        return ResponseEntity.badRequest().body("동일한 라인을 등록할 수 없습니다");
    }

    @ExceptionHandler(DataAccessException.class)
    private ResponseEntity<String> handleDatabaseExceptions(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandle(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
