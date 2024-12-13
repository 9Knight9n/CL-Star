import com.google.common.collect.Streams;
import de.learnlib.api.oracle.MembershipOracle;

import java.util.*;
import java.util.stream.Stream;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.util.automata.conformance.WMethodTestsIterator;
import net.automatalib.words.Word;

public class WMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D> extends AbstractTestWordDCEQOracle<A, I, D> {
    private final int lookahead;
    private final int expectedSize;

    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead) {
        this(sulOracle, lookahead, 0);
    }

    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize) {
        this(sulOracle, lookahead, expectedSize, 1);
    }

    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize, int batchSize) {
        super(sulOracle, batchSize);
        this.lookahead = lookahead;
        this.expectedSize = expectedSize;
    }

    public Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs,List<A> listOfHypothesises, List<Collection<? extends I>> listOfInputs) {
        return Streams.stream(new WDCMethodTestsIterator(hypothesis, inputs, listOfHypothesises, listOfInputs, Math.max(this.lookahead, this.expectedSize - hypothesis.size())));
    }

    public Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return Streams.stream(new WMethodTestsIterator(hypothesis, inputs, Math.max(this.lookahead, this.expectedSize - hypothesis.size())));
    }
}