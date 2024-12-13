import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestWordDCEQOracle<A extends Output<I, D>, I, D> implements EquivalenceOracle<A, I, D> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestWordDCEQOracle.class);
    private final MembershipOracle<I, D> membershipOracle;
    private final int batchSize;

    public AbstractTestWordDCEQOracle(MembershipOracle<I, D> membershipOracle) {
        this(membershipOracle, 1);
    }

    public AbstractTestWordDCEQOracle(MembershipOracle<I, D> membershipOracle, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        this.membershipOracle = membershipOracle;
        this.batchSize = batchSize;
    }

    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        if (inputs.isEmpty()) {
            LOGGER.warn("Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        } else {
            Stream<Word<I>> testWordStream = this.generateTestWords(hypothesis, inputs);
            Stream<DefaultQuery<I, D>> queryStream = testWordStream.map(DefaultQuery::new);
            Stream<DefaultQuery<I, D>> answeredQueryStream = this.answerQueries(queryStream);
            Stream<DefaultQuery<I, D>> ceStream = answeredQueryStream.filter((query) -> {
                D hypOutput = hypothesis.computeOutput(query.getInput());
                return !Objects.equals(hypOutput, query.getOutput());
            });
            return (DefaultQuery)ceStream.findFirst().orElse((DefaultQuery<I, D>) null);
        }
    }

    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs, List<A> listOfHypothesis, List<Collection<? extends I>> listOfInputs) {
        if (inputs.isEmpty()) {
            LOGGER.warn("Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        } else {
            Stream<Word<I>> testWordStream = this.generateTestWords(hypothesis, inputs, listOfHypothesis, listOfInputs);
            Stream<DefaultQuery<I, D>> queryStream = testWordStream.map(DefaultQuery::new);
            Stream<DefaultQuery<I, D>> answeredQueryStream = this.answerQueries(queryStream);
            Stream<DefaultQuery<I, D>> ceStream = answeredQueryStream.filter((query) -> {
                D hypOutput = hypothesis.computeOutput(query.getInput());
                return !Objects.equals(hypOutput, query.getOutput());
            });
            return (DefaultQuery)ceStream.findFirst().orElse((DefaultQuery<I, D>) null);
        }
    }

    protected abstract Stream<Word<I>> generateTestWords(A var1, Collection<? extends I> var2, List<A> var3, List<Collection<? extends I>> var4);
    protected abstract Stream<Word<I>> generateTestWords(A var1, Collection<? extends I> var2   );

    private Stream<DefaultQuery<I, D>> answerQueries(Stream<DefaultQuery<I, D>> stream) {
        if (this.isBatched()) {
            return Streams.stream(
                Iterators.partition(stream.iterator(), this.batchSize))
                .peek(batch -> this.membershipOracle.processQueries(batch))
                .flatMap(Collection::stream);
        } else {
            return stream.peek(this.membershipOracle::processQuery);
        }
    }

    private boolean isBatched() {
        return this.batchSize > 1;
    }
}
