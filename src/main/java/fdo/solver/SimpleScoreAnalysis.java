package fdo.solver;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SimpleScoreAnalysis {
    private HardSoftScore score;
    private List<SimpleConstraintScore> constraints;

    public SimpleScoreAnalysis(HardSoftScore score, List<SimpleConstraintScore> constraints) {
        this.score = score;
        this.constraints = constraints;
    }

    public static SimpleScoreAnalysis from(ScoreAnalysis<HardSoftScore> analysis) {
        List<SimpleConstraintScore> constraints = analysis.constraintAnalyses().stream()
                .map(SimpleConstraintScore::from)
                .toList();

        return new SimpleScoreAnalysis(analysis.score(), constraints);
    }
}
