import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CharacterizingSetCalculator {

    public static void main(String[] args) {
        try {
            // Load the Mealy machine from the sample_fsm.dot file
            File fsmFile = new File("data/sample_fsm.dot");
            CompactMealy<String, Word<String>> mealyMachine = Utils.getInstance().loadMealyMachineFromDot(fsmFile);

            // Get the input alphabet
            Alphabet<String> alphabet = mealyMachine.getInputAlphabet();

            // Calculate the Characterizing Sets
            Iterator<Word<String>> characterizingSetIterator = CharacterizingSets.characterizingSetIterator(mealyMachine, alphabet);

            // Convert the iterator to a list for easier processing
            List<Word<String>> characterizingSets = StreamSupport.stream(
                    ((Iterable<Word<String>>) () -> characterizingSetIterator).spliterator(), false)
                    .collect(Collectors.toList());

            // Print the Characterizing Sets
            System.out.println("Characterizing Sets:");
            for (Word<String> word : characterizingSets) {
                System.out.println(word);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}