package domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Getter;

import java.util.List;

@Getter
@PlanningEntity
public class Order {

    private String id;
    private List<Food> foods;
    private int earliestMinute;
    private int latestMinute;
    private Location deliveryLocation;

    @PlanningVariable(valueRangeProviderRefs = "courierRange")
    private CourierShift courierShift;

    public Order() {}

    public Order(String id, int earliestMinute, int latestMinute, List<Food> foods) {
        this.id = id;
        this.earliestMinute = earliestMinute;
        this.latestMinute = latestMinute;
        this.foods = foods;
    }

    public void setCourierShift(CourierShift courierShift) {
        this.courierShift = courierShift;
    }
}