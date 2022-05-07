package wooteco.subway.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ReflectionUtils;

import wooteco.subway.domain.Station;
import wooteco.subway.exception.RowDuplicatedException;
import wooteco.subway.exception.RowNotFoundException;

public class MemoryStationDao implements StationDao {
    private Long seq = 0L;
    private List<Station> stations = new ArrayList<>();

    @Override
    public Station save(Station station) {
        validateDistinct(station);
        Station persistStation = createNewObject(station);
        stations.add(persistStation);
        return persistStation;
    }

    private void validateDistinct(Station otherStation) {
        boolean isDuplicated = stations.stream()
            .anyMatch(station -> station.hasSameNameWith(otherStation));
        if (isDuplicated) {
            throw new RowDuplicatedException("이미 존재하는 역 이름입니다.");
        }
    }

    @Override
    public List<Station> findAll() {
        return stations;
    }

    private Station createNewObject(Station station) {
        Field field = ReflectionUtils.findField(Station.class, "id");
        field.setAccessible(true);
        ReflectionUtils.setField(field, station, ++seq);
        return station;
    }

    @Override
    public void deleteById(Long id) {
        final boolean isRemoved = stations.removeIf(station -> station.getId().equals(id));
        validateRemoved(isRemoved);
    }

    private void validateRemoved(boolean isRemoved) {
        if (!isRemoved) {
            throw new RowNotFoundException("삭제하고자 하는 역이 존재하지 않습니다.");
        }
    }
}
