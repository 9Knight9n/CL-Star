import java.util.*;
import java.util.stream.*;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.collections.AbstractThreeLevelIterator;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.collections.ReusableIterator;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class WDCMethodTestsIterator<I> extends AbstractThreeLevelIterator<List<I>, Word<I>, Word<I>, Word<I>> {
    private final Iterable<Word<I>> prefixes;
    private final Iterable<Word<I>> suffixes;
    private final WordBuilder<I> wordBuilder;

    public WDCMethodTestsIterator(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton, Collection<? extends I> inputs, int maxDepth) {
        super(CollectionsUtil.<I>allTuples(inputs, 0, maxDepth).iterator());
//        this.logHypothesis(automaton, inputs);
//        System.out.println("----------------------------------");
        this.wordBuilder = new WordBuilder<>(maxDepth * 2); // Preallocate buffer size
        this.prefixes = new ReusableIterator(Covers.transitionCoverIterator(automaton, inputs), new ArrayList(automaton.size() * inputs.size()));
        Iterator<Word<I>> characterizingSet = CharacterizingSets.characterizingSetIterator(automaton, inputs);
        if (!characterizingSet.hasNext()) {
            this.suffixes = Collections.singletonList(Word.epsilon());
        } else {
            this.suffixes = new ReusableIterator(characterizingSet);
        }
//        for (Word<I> prefix : prefixes) {
//            System.out.println("Prefix: " + prefix);
//        }
//        for (Word<I> suffix : suffixes) {
//            System.out.println("Suffix: " + suffix);
//        }
    }

    public WDCMethodTestsIterator(UniversalDeterministicAutomaton<?, I, ?, ?, ?> hypothesis,
                                  Collection<? extends I> allInputs,
                                  List<UniversalDeterministicAutomaton<?, I, ?, ?, ?>> listOfAutomatons,
                                  List<Collection<? extends I>> listOfInputs,
                                  int maxDepth) {
        super(CollectionsUtil.<I>allTuples(allInputs, 0, maxDepth).iterator());

        boolean nonEmptySuffix = false;

        this.wordBuilder = new WordBuilder<>(maxDepth * 2); // Preallocate buffer size

        List<List<Word<I>>> allPrefixes = new ArrayList<>();
        List<List<Word<I>>> allSuffixes = new ArrayList<>();

        List<Word<I>> currentPrefixes = new LinkedList<>();
        currentPrefixes.add(Word.epsilon());

        for (int index = 0; index < listOfAutomatons.size(); index++) {
            UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton = listOfAutomatons.get(index);
            Collection<? extends I> inputs = listOfInputs.get(index);
//            this.logHypothesis(automaton, inputs);
//            System.out.println("----------------------------------");

            List<Word<I>> prefixList = new ArrayList<>();
            Covers.stateCoverIterator(automaton, inputs).forEachRemaining(prefixList::add);
            allPrefixes.add(prefixList);

            List<Word<I>> suffixList = new ArrayList<>();
            CharacterizingSets.characterizingSetIterator(automaton, inputs).forEachRemaining(suffixList::add);
            if (!nonEmptySuffix && !suffixList.isEmpty()) {
                nonEmptySuffix = true;
            }
            allSuffixes.add(suffixList);

//            for (Word<I> prefix : prefixList) {
//                System.out.println("items on prefixList: " + prefix);
//            }
//            System.out.println("----------------------------------");
        }

        for (List<Word<I>> prefixList : allPrefixes) {
            List<Word<I>> tempPrefixes = new ArrayList<>(prefixList.size() * currentPrefixes.size());
            for (Word<I> word1 : prefixList) {
                for (Word<I> word2 : currentPrefixes) {
                    wordBuilder.clear();
                    tempPrefixes.add(wordBuilder.append(word1).append(word2).toWord());
                }
            }
            currentPrefixes = tempPrefixes;
        }

        if (!nonEmptySuffix)
        {
            List<Word<I>> suffixList = new ArrayList<>();
            suffixList.add(Word.epsilon());
            allSuffixes.add(suffixList);
        }

        this.prefixes = currentPrefixes;
        this.suffixes = allSuffixes.stream().flatMap(Collection::stream).collect(Collectors.toList());
//        for (Word<I> prefix : prefixes) {
//            System.out.println("Prefix: " + prefix);
//        }
//        for (Word<I> suffix : suffixes) {
//            System.out.println("Suffix: " + suffix);
//        }
    }

    @Override
    protected Iterator<Word<I>> l2Iterator(List<I> l1Object) {
        return this.prefixes.iterator();
    }

    @Override
    protected Iterator<Word<I>> l3Iterator(List<I> l1Object, Word<I> l2Object) {
        return this.suffixes.iterator();
    }

    @Override
    protected Word<I> combine(List<I> middle, Word<I> prefix, Word<I> suffix) {
        this.wordBuilder.ensureAdditionalCapacity(prefix.size() + middle.size() + suffix.size());
        Word<I> word = this.wordBuilder.append(prefix).append(middle).append(suffix).toWord();
        this.wordBuilder.clear();
        return word;
    }

    public <S, T> void logHypothesis(UniversalDeterministicAutomaton<S, I, T, ?, ?> hypothesis, Collection<? extends I> inputs) {
        // Log states
        System.out.println("States:");
        for (S state : hypothesis.getStates()) {
            System.out.println("State: " + state);
        }

        // Log transitions
        System.out.println("Transitions:");
        for (S state : hypothesis.getStates()) {
            for (I input : inputs) {
                T transition = hypothesis.getTransition(state, input);
                if (transition != null) {
                    S successor = hypothesis.getSuccessor(transition);
                    System.out.println("Transition: " + state + " --" + input + "--> " + successor);
                }
            }
        }
    }
}
