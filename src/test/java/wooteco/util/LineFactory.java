package wooteco.util;

import wooteco.subway.line.domain.Line;
import wooteco.subway.line.domain.section.Section;
import wooteco.subway.line.domain.section.Sections;
import wooteco.subway.line.domain.value.LineColor;
import wooteco.subway.line.domain.value.LineId;
import wooteco.subway.line.domain.value.LineName;

import java.util.List;

public class LineFactory {

    public static Line create(Long id, String name, String color, List<Section> sections) {
        return new Line(
                new LineId(id),
                new LineName(name),
                new LineColor(color),
                new Sections(sections)
        );
    }

    public static Line create(String name, String color, List<Section> sections) {
        return create(null, name, color, sections);
    }

}
