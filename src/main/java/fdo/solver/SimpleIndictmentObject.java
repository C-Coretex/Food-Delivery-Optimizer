package fdo.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;
import fdo.domain.CourierShift;

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

    public SimpleIndictmentObject(Object indictedObject, HardSoftScore score, int matchCount, Set<ConstraintMatch<HardSoftScore>> constraintMatches) {
        this.indictedObjectID = indictedObject instanceof CourierShift ? ((CourierShift) indictedObject).getId() : "0";

        this.indictedObjectClass = indictedObject.getClass().getSimpleName();

        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream().map(SimpleConstraintMatch::new).collect(Collectors.toList());
    }
}
