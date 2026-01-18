package fdo.dto;

import com.graphhopper.util.shapes.GHPoint;
import fdo.domain.Location;
import fdo.domain.Visit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleVisit {

    private Long id;
    private String type;
    private Integer minuteTime;
    private Integer roadTime;

    private String orderId;
    private String restaurantId;
    private String restaurantChainId;

    private Location location;

    private List<GHPoint> pathToNext;

    public SimpleVisit(
            Long id,
            String type,
            Integer minuteTime,
            Integer roadTime,
            String orderId,
            String restaurantId,
            String restaurantChainId,
            Location location,
            List<GHPoint> pathToNext
    ) {
        this.id = id;
        this.type = type;
        this.minuteTime = minuteTime;
        this.roadTime = roadTime;
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.restaurantChainId = restaurantChainId;
        this.location = location;
        this.pathToNext = pathToNext;
    }

    public static SimpleVisit from(Visit visit) {
        return new SimpleVisit(
                visit.getId(),
                visit.getType() != null ? visit.getType().name() : null,
                visit.getMinuteTime(),
                visit.getRoadTime(),
                visit.getOrder() != null ? visit.getOrder().getId() : null,
                visit.getRestaurant() != null ? visit.getRestaurant().getId() : null,
                visit.getRestaurant() != null ? visit.getRestaurant().getChainId() : null,
                visit.getLocation(),
                visit.getPathToNext()
        );
    }
}
