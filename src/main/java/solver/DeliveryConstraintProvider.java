package solver;

import domain.*;
import ai.timefold.solver.core.api.score.stream.*;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

public class DeliveryConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                courierShiftMustCoverOrderTime(factory),
                hotCapacityExceeded(factory),
                coldCapacityExceeded(factory)
        };
    }

    // HARD: courier must be working during order window
    private Constraint courierShiftMustCoverOrderTime(ConstraintFactory factory) {
        return factory.forEach(Order.class)
                .filter(o -> o.getCourierShift() != null)
                .filter(o ->
                        o.getCourierShift().getStartMinute() > o.getEarliestMinute()
                                || o.getCourierShift().getEndMinute() < o.getLatestMinute()
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Courier shift outside order time window");
    }

    // HARD: hot food volume capacity
    private Constraint hotCapacityExceeded(ConstraintFactory factory) {
        return factory.forEach(Order.class)
                .groupBy(
                        Order::getCourierShift,
                        ConstraintCollectors.sum(
                                o -> o.getFoods().stream()
                                        .filter(f -> f.getTemperature() == Food.Temperature.HOT)
                                        .mapToInt(Food::getVolume)
                                        .sum()
                        )
                )
                .filter((shift, volume) -> shift != null && volume > shift.getHotCapacity())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Hot capacity exceeded");
    }

    // HARD: cold food volume capacity
    private Constraint coldCapacityExceeded(ConstraintFactory factory) {
        return factory.forEach(Order.class)
                .groupBy(
                        Order::getCourierShift,
                        ConstraintCollectors.sum(
                                o -> o.getFoods().stream()
                                        .filter(f -> f.getTemperature() == Food.Temperature.COLD)
                                        .mapToInt(Food::getVolume)
                                        .sum()
                        )
                )
                .filter((shift, volume) -> shift != null && volume > shift.getColdCapacity())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Cold capacity exceeded");
    }
}