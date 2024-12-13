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

    public WDCMethodTestsIterator(UniversalDeterministicAutomaton<?, I, ?, ?, ?> hypothesis, Collection<? extends I> allInputs, List<UniversalDeterministicAutomaton<?, I, ?, ?, ?>> ListOfAutomatons, List<Collection<? extends I>> listOfInputs, int maxDepth) {
        super(CollectionsUtil.<I>allTuples(allInputs, 0, maxDepth).iterator());
        List<Iterable<Word<I>>> listOfPrefixes = new ArrayList<>();
        List<Iterable<Word<I>>> listOfSuffixes = new ArrayList<>();

//        System.out.println("Inputs: " + allInputs);
//
//        // Log the list of automatons
//        System.out.println("ListOfAutomatons: " + ListOfAutomatons);
//
//        // Log the list of inputs
//        System.out.println("ListOfInputs: " + listOfInputs);
//
//        // Log the max depth
//        System.out.println("MaxDepth: " + maxDepth);

        for (int index = 0; index < ListOfAutomatons.size(); index++){
            UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton = ListOfAutomatons.get(index);
            Collection<? extends I> inputs = listOfInputs.get(index);
            
//                    // Log each automaton and its inputs
//            System.out.println("Iteration " + index + ":");
//            System.out.println("  Automaton: " + automaton);
//            System.out.println("  Automaton Inputs: " + inputs);
//
//            // Log the size of the automaton and inputs
//            System.out.println("  Automaton Size: " + automaton.size());
//            System.out.println("  Inputs Size: " + inputs.size());


            listOfPrefixes.add(new ReusableIterator(Covers.transitionCoverIterator(automaton, inputs), new ArrayList(automaton.size() * inputs.size())));
            Iterator<Word<I>> characterizingSet = CharacterizingSets.characterizingSetIterator(automaton, inputs);
            
//            System.out.println("  Characterizing Set has elements: " + characterizingSet.hasNext());

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
        // System.out.println("new method prefixes: " + this.prefixes);
        // System.out.println("new method suffixes: " + this.suffixes);

        // Iterator<Word<I>> characterizingSet2 = CharacterizingSets.characterizingSetIterator(hypothesis, allInputs);
        // System.out.println("new method prefixes: " + new ReusableIterator(Covers.transitionCoverIterator(hypothesis, allInputs), new ArrayList(hypothesis.size() * allInputs.size())));
        // if (!characterizingSet2.hasNext()) {
        //     System.out.println("new method suffixes: " + Collections.singletonList(Word.epsilon()));
        // } else {
        //     System.out.println("new method suffixes: " + new ReusableIterator(characterizingSet2));
        // }
        
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