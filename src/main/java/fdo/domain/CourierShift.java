package fdo.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@PlanningEntity
@Getter
@Setter
public class CourierShift {

    @PlanningId
    private String id;

    private int hotCapacity;
    private int coldCapacity;

    @PlanningListVariable(valueRangeProviderRefs = "visitList", allowsUnassignedValues = true)
    List<Visit> visits = new ArrayList<>();

    //public CourierShift(String c2, int i, int i1) {}
    public CourierShift() {
        // Required by Timefold
    }
    public boolean isUsed() {
        return !visits.isEmpty();
    }

    public CourierShift(String id, int hotCapacity, int coldCapacity) {
        this.id = id;
        this.hotCapacity = hotCapacity;
        this.coldCapacity = coldCapacity;
    }

    //public int getEndMinute() { return startMinute + durationMinutes; }
    public Integer getEndMinute() {
        return visits.stream()
                .map(Visit::getMinuteTime)
                .filter(t -> t != null)
                .max(Integer::compareTo)
                .orElse(null);
    }

    public Integer getStartMinute() {
        return visits.stream()
                .map(Visit::getMinuteTime)
                .filter(t -> t != null)
                .min(Integer::compareTo)
                .orElse(null);
    }

    public int getDurationMinutes() {
        Integer start = getStartMinute();
        Integer end = getEndMinute();
        if (start == null || end == null) return 0;
        return end - start;
    }

    public boolean isAnyCapacityExceeded() {
        int currentHotVolume = 0;
        int currentColdVolume = 0;

        int hotCap = this.getHotCapacity();
        int coldCap = this.getColdCapacity();

        // 1. Sort visits chronologically
        List<Visit> sortedVisits = this.getVisits().stream()
                .sorted(Comparator.comparingInt(Visit::getMinuteTime))
                .toList();

        // 2. Single-pass evaluation
        for (Visit visit : sortedVisits) {
            List<Food> foods = visit.getOrder().getFoods();

            // Calculate volumes for this specific visit
            int hotChange = foods.stream()
                    .filter(f -> f.getTemperature() == Food.Temperature.HOT)
                    .mapToInt(Food::getVolume).sum();

            int coldChange = foods.stream()
                    .filter(f -> f.getTemperature() == Food.Temperature.COLD)
                    .mapToInt(Food::getVolume).sum();

            // 3. Update load based on visit type
            if (visit.getType() == Visit.VisitType.RESTAURANT) {
                currentHotVolume += hotChange;
                currentColdVolume += coldChange;
            } else {
                currentHotVolume -= hotChange;
                currentColdVolume -= coldChange;
            }

            // 4. Check for violations in either compartment
            if (currentHotVolume > hotCap || currentColdVolume > coldCap) {
                return true;
            }
        }

        return false;
    }
}