package wooteco.subway.domain.section;

import java.util.Objects;

import wooteco.subway.domain.property.Distance;
import wooteco.subway.domain.station.Station;
import wooteco.subway.util.Id;

public class Section {

    @Id
    private final Long id;
    private final Station upStation;
    private final Station downStation;
    private final Distance distance;

    public Section(Long id, Station upStation, Station downStation, Distance distance) {
        this.id = id;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public Section(Station upStation, Station downStation, Distance distance) {
        this(null, upStation, downStation, distance);
    }

    public boolean isDividable(Section other) {
        return isOneStationMatch(other) && this.distance.isLongerThan(other.distance);
    }

    private boolean isOneStationMatch(Section other) {
        return this.upStation.equals(other.upStation) ^ this.downStation.equals(other.downStation);
    }

    public Section divide(Section other) {
        if (isUpStationConnected(other)) {
            return new Section(other.downStation, downStation, distance.subtract(other.distance));
        }
        return new Section(upStation, other.upStation, distance.subtract(other.distance));
    }

    private boolean isUpStationConnected(Section other) {
        return this.upStation.equals(other.upStation);
    }

    public boolean isAnyIdMatch(Long stationId) {
        return upStation.getId().equals(stationId) || downStation.getId().equals(stationId);
    }

    public Section merge(Section other) {
        return new Section(id, upStation, other.downStation, distance.plus(other.distance));
    }

    public Long getId() {
        return id;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public Distance getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Section section = (Section)o;
        return Objects.equals(id, section.id) && Objects.equals(upStation, section.upStation)
            && Objects.equals(downStation, section.downStation) && Objects.equals(distance,
            section.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, upStation, downStation, distance);
    }
}
