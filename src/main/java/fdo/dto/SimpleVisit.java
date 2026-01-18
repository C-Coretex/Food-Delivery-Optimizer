package fdo.dto;

import fdo.domain.Location;
import fdo.domain.Visit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleVisit {

    private Long id;
    private String type;
    private Integer minuteTime;
    private Integer roadTime;

    private String orderId;
    private String restaurantName;

    private Location location;

    public SimpleVisit(
            Long id,
            String type,
            Integer minuteTime,
            Integer roadTime,
            String orderId,
            String restaurantName,
            Location location
    ) {
        this.id = id;
        this.type = type;
        this.minuteTime = minuteTime;
        this.roadTime = roadTime;
        this.orderId = orderId;
        this.restaurantName = restaurantName;
        this.location = location;
    }

    public static SimpleVisit from(Visit visit) {
        return new SimpleVisit(
                visit.getId(),
                visit.getType() != null ? visit.getType().name() : null,
                visit.getMinuteTime(),
                visit.getRoadTime(),
                visit.getOrder() != null ? visit.getOrder().getId() : null,
                visit.getRestaurant() != null ? visit.getRestaurant().getId() : null,
                visit.getLocation()
        );
    }
}
