package fdo.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import fdo.domain.CourierShift;
import fdo.domain.Visit;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter @Getter

public class SimpleIndictmentObject {
    private String indictedObjectID;
    private String indictedObjectClass;
    private HardSoftScore score;
    private int matchCount;
    private List<SimpleConstraintMatch> constraintMatches = new ArrayList<>();

    public SimpleIndictmentObject(
            Object indictedObject,
            HardSoftScore score,
            int matchCount,
            Set<ConstraintMatch<HardSoftScore>> constraintMatches
    ) {
        if (indictedObject instanceof CourierShift cs) {
            this.indictedObjectID = cs.getId();
        } else if (indictedObject instanceof Visit v) {
            this.indictedObjectID = String.valueOf(v.getId());
        } else {
            this.indictedObjectID = String.valueOf(indictedObject);
        }

        this.indictedObjectClass = indictedObject.getClass().getSimpleName();
        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream()
                .map(SimpleConstraintMatch::new)
                .collect(Collectors.toList());
    }
}
