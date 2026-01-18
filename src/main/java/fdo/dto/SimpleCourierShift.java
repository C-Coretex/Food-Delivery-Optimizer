package fdo.dto;

import fdo.domain.CourierShift;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleCourierShift {
    private String id;
    private int hotCapacity;
    private int coldCapacity;
    private List<SimpleVisit> visits;

    public SimpleCourierShift(
            String id,
            int hotCapacity,
            int coldCapacity,
            List<SimpleVisit> visits
    ) {
        this.id = id;
        this.hotCapacity = hotCapacity;
        this.coldCapacity = coldCapacity;
        this.visits = visits;
    }

    public static SimpleCourierShift from(CourierShift shift) {
        return new SimpleCourierShift(
                shift.getId(),
                shift.getHotCapacity(),
                shift.getColdCapacity(),
                shift.getVisits().stream()
                        .map(SimpleVisit::from)
                        .toList()
        );
    }
}
