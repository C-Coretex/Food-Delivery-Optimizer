package fdo.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.fasterxml.jackson.databind.JsonNode;
import fdo.domain.DeliverySolution;
import fdo.domain.Router;
import fdo.domain.Visit;
import fdo.dto.SimpleDeliverySolution;
import fdo.generator.Generator;
import fdo.generator.JsonIO;
import fdo.solver.SimpleIndictmentObject;
import fdo.solver.SimpleScoreAnalysis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Tag(name = "FDO", description = "Service to optimize FDO routes")
@RestController
@Slf4j
@RequestMapping("/fdo")
public class FoodDeliveryOptimizerController {
    private final SolverManager<DeliverySolution, String> solverManager;
    private final SolutionManager<DeliverySolution, HardSoftScore> solutionManager;
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    private Router ghRouter = Router.getDefaultRouterInstance();

    public FoodDeliveryOptimizerController(SolverManager<DeliverySolution, String> solverManager,
                                           SolutionManager<DeliverySolution, HardSoftScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    @Operation(summary = "List the job IDs of all submitted FDOs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = String.class))) })
    @GetMapping
    public Collection<String> list() {
        return jobIdToJob.keySet();
    }

    @Operation(summary = "Submit an FDO to start solving as soon as CPU resources are available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class))) })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String solve(@RequestBody DeliverySolution problem) {
        ghRouter.setDistanceTimeMap(problem.getLocationList());
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofDeliverySolution(problem));
        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId).deliverySolution)
                .withBestSolutionConsumer(solution -> jobIdToJob.put(jobId, Job.ofDeliverySolution(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                    jobIdToJob.put(jobId, Job.ofException(exception));
                    log.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();
        return jobId;
    }

    @Operation(summary = "Submit an FDO (with our custom JSON template) to start solving as soon as CPU resources are available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class))) })
    @PostMapping(
            value = "/solve-custom",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String solveCustom(@RequestBody JsonNode root) {
        DeliverySolution problem = JsonIO.parse_json(root);

        ghRouter.setDistanceTimeMap(problem.getLocationList());

        List<Visit> visits = Generator.VisitGenerator.generateAll(problem);
        problem.setVisitList(visits);

        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofDeliverySolution(problem));

        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId_).deliverySolution)
                .withBestSolutionConsumer(solution ->
                        jobIdToJob.put(jobId, Job.ofDeliverySolution(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                    jobIdToJob.put(jobId_, Job.ofException(exception));
                    log.error("Failed solving jobId ({}).", jobId_, exception);
                })
                .run();

        return jobId;
    }

    @Operation(
            summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The best solution of the FDO so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeliverySolution.class))),
            @ApiResponse(responseCode = "404", description = "No FDO found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an FDO.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleDeliverySolution getDeliverySolution(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId
    ) {
        DeliverySolution solution = getDeliverySolutionAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);

        return SimpleDeliverySolution.from(solution, solverStatus);
    }

    @Operation(
            summary = "Get the score analysis for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The score analysis of the best solution of the FDO so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeliverySolution.class))),
            @ApiResponse(responseCode = "404", description = "No FDO found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an FDO.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/score/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleScoreAnalysis analyze(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId
    ) {
        DeliverySolution deliverySolution = getDeliverySolutionAndCheckForExceptions(jobId);
        ScoreAnalysis<HardSoftScore> analysis = solutionManager.analyze(deliverySolution);

        return SimpleScoreAnalysis.from(analysis);
    }

    @Operation(
            summary = "Get the score indictments for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The score indictments of the best solution of the FDO so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeliverySolution.class))),
            @ApiResponse(responseCode = "404", description = "No FDO found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an FDO.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/indictments/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleIndictmentObject> indictments(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        DeliverySolution deliverySolution= getDeliverySolutionAndCheckForExceptions(jobId);
        return solutionManager.explain(deliverySolution).getIndictmentMap().entrySet().stream()
                .map(entry -> {
                    Indictment<HardSoftScore> indictment = entry.getValue();
                    return
                            new SimpleIndictmentObject(entry.getKey(), // indicted Object
                                    indictment.getScore(),
                                    indictment.getConstraintMatchCount(),
                                    indictment.getConstraintMatchSet());
                }).collect(Collectors.toList());
    }

    private DeliverySolution getDeliverySolutionAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new FoodDeliveryOptimizerSolverException(jobId, HttpStatus.NOT_FOUND, "No FDO found.");
        }
        if (job.exception != null) {
            throw new FoodDeliveryOptimizerSolverException(jobId, job.exception);
        }
        return job.deliverySolution;
    }

    private record Job(DeliverySolution deliverySolution, Throwable exception) {

        static Job ofDeliverySolution(DeliverySolution deliverySolution) {
            return new Job(deliverySolution, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }
}
