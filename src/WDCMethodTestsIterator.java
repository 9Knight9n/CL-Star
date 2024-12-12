import java.util.*;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.collections.AbstractThreeLevelIterator;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.collections.ReusableIterator;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import java.util.stream.*;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class WDCMethodTestsIterator<I> extends AbstractThreeLevelIterator<List<I>, Word<I>, Word<I>, Word<I>> {
    private final Iterable<Word<I>> prefixes;
    private final Iterable<Word<I>> suffixes;
    private final WordBuilder<I> wordBuilder = new WordBuilder();

    public WDCMethodTestsIterator(Collection<? extends I> inputs, List<UniversalDeterministicAutomaton<?, I, ?, ?, ?>> ListOfAutomatons, List<Collection<? extends I>> listOfInputs, int maxDepth) {
        super(CollectionsUtil.<I>allTuples(inputs, 0, maxDepth).iterator());
        List<Iterable<Word<I>>> listOfPrefixes = new ArrayList<>();
        List<Iterable<Word<I>>> listOfSuffixes = new ArrayList<>();

        for (UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton : ListOfAutomatons ){
            listOfPrefixes.add(new ReusableIterator(Covers.transitionCoverIterator(automaton, inputs), new ArrayList(automaton.size() * inputs.size())));
            Iterator<Word<I>> characterizingSet = CharacterizingSets.characterizingSetIterator(automaton, inputs);
            if (!characterizingSet.hasNext()) {
                listOfSuffixes.add(Collections.singletonList(Word.epsilon()));
            } else {
                listOfSuffixes.add(new ReusableIterator(characterizingSet));
            }
        }

        this.prefixes = () -> listOfPrefixes.stream()
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .iterator();
        this.suffixes = () -> listOfSuffixes.stream()
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .iterator();
    }

    protected Iterator<Word<I>> l2Iterator(List<I> l1Object) {
        return this.prefixes.iterator();
    }

    protected Iterator<Word<I>> l3Iterator(List<I> l1Object, Word<I> l2Object) {
        return this.suffixes.iterator();
    }

    protected Word<I> combine(List<I> middle, Word<I> prefix, Word<I> suffix) {
        this.wordBuilder.ensureAdditionalCapacity(prefix.size() + middle.size() + suffix.size());
        Word<I> word = this.wordBuilder.append(prefix).append(middle).append(suffix).toWord();
        this.wordBuilder.clear();
        return word;
    }
}