import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;

import domain.*;
import domain.Food.Temperature;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class FoodDeliveryOptimizerApp {

    public static void main(String[] args) {
        // --- Courier shifts ---
        DeliverySolution problem = getSolution();

        SolverFactory<DeliverySolution> solverFactory =
                SolverFactory.createFromXmlResource("solverConfig.xml");

        Solver<DeliverySolution> solver = solverFactory.buildSolver();
        DeliverySolution solution = solver.solve(problem);

        // --- Result ---
        log.info("");
        log.info("Final score: {}", solution.getScore());

        solution.getOrders().forEach(order -> {
            CourierShift shift = order.getCourierShift();
            log.info(
                    "Order {} -> Courier {}",
                    order.getId(),
                    shift != null ? shift.getId() : "UNASSIGNED"
            );
        });
    }

    @NotNull
    private static DeliverySolution getSolution() {
        List<CourierShift> courierShifts = List.of(
                new CourierShift("C1", 10, 5, 480, 240), // 08:00–12:00
                new CourierShift("C2", 6, 10, 540, 300)  // 09:00–14:00
        );

        // --- Foods ---
        Food pizza = new Food("Pizza", 3, 15, 45, Temperature.HOT, 12.0);
        Food salad = new Food("Salad", 2, 5, 30, Temperature.COLD, 7.0);

        // --- Orders ---
        List<Order> orders = List.of(
                new Order("O1", 500, 560, List.of(pizza)),
                new Order("O2", 520, 600, List.of(salad)),
                new Order("O3", 540, 620, List.of(pizza, salad))
        );

        // --- Problem ---
        return new DeliverySolution(courierShifts, orders);
    }
}