package fdo.solver;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleConstraintScore {
    private String name;
    private HardSoftScore score;

    public SimpleConstraintScore(String name, HardSoftScore score) {
        this.name = name;
        this.score = score;
    }

    public static SimpleConstraintScore from(ConstraintAnalysis<HardSoftScore> ca) {
        // use what your Timefold version exposes:
        // - ca.constraintName()
        // - ca.name()
        // - ca.constraintRef().constraintName()
        // pick the one that compiles.
        return new SimpleConstraintScore(ca.constraintName(), ca.score());
    }
}
