package domain;

import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;
@PlanningSolution
public class DeliverySolution {

    //must be multi-slot
    private List<Restaurant> restaurants;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "courierRange")
    private List<CourierShift> courierShifts;

    private List<Food> Foods;

    @Getter
    @PlanningEntityCollectionProperty
    private List<Order> orders;

    private List<Location> Locations;

    @Setter
    private HardSoftScore score;

    @ProblemFactCollectionProperty
    List<Location> locationList = new ArrayList<>();

    public DeliverySolution() {}

    public DeliverySolution(List<CourierShift> courierShifts, List<Order> orders) {
        this.courierShifts = courierShifts;
        this.orders = orders;
    }

    public List<CourierShift> getCourierShifts() { return courierShifts; }

    @PlanningScore
    public HardSoftScore getScore() { return score; }
}