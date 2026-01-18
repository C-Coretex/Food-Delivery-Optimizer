package fdo.dto;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import fdo.domain.DeliverySolution;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleDeliverySolution {
    private SolverStatus solverStatus;
    private HardSoftScore score;
    private List<SimpleCourierShift> courierShifts;

    public SimpleDeliverySolution(
            SolverStatus solverStatus,
            HardSoftScore score,
            List<SimpleCourierShift> courierShifts
    ) {
        this.solverStatus = solverStatus;
        this.score = score;
        this.courierShifts = courierShifts;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public List<SimpleCourierShift> getCourierShifts() {
        return courierShifts;
    }

    public static SimpleDeliverySolution from(
            DeliverySolution solution,
            SolverStatus solverStatus
    ) {
        return new SimpleDeliverySolution(
                solverStatus,
                solution.getScore(),
                solution.getCourierShifts().stream()
                        .map(SimpleCourierShift::from)
                        .toList()
        );
    }
}
