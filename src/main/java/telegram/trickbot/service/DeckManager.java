package telegram.trickbot.service;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class DeckManager {
    private static final int DECK_SIZE = 21;
    private static final int PILE_SIZE = 7;
    private static final int FIRST_CARD = 0;
    private static final int FOURTEENTH_CARD = 14;
    private static final int SELECTED_CARD = 10;
    private List<String> deck;
    private static final String PICTURES_PATH = "C:\\Users\\Administrator\\Desktop\\TrickBot\\images\\";
    private static final String PICTURES_EXTENSION = ".png";

    public DeckManager() {
        initializeDeck();
    }

    private void initializeDeck() {
        deck = new ArrayList<>();
        IntStream.rangeClosed(1, DECK_SIZE).forEach(i -> deck.add(PICTURES_PATH + i + PICTURES_EXTENSION));
        log.info("Deck initialized: {}", deck);
        shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
        log.info("Deck shuffled: {}", deck);
    }

    public List<String> getPile1() {
        List<String> pile1 = new ArrayList<>(deck.subList(FIRST_CARD, PILE_SIZE));
        log.info("Pile 1: {}", pile1);
        return pile1;
    }

    public List<String> getPile2() {
        List<String> pile2 = new ArrayList<>(deck.subList(PILE_SIZE, FOURTEENTH_CARD));
        log.info("Pile 2: {}", pile2);
        return pile2;
    }

    public List<String> getPile3() {
        List<String> pile3 = new ArrayList<>(deck.subList(FOURTEENTH_CARD, DECK_SIZE));
        log.info("Pile 3: {}", pile3);
        return pile3;
    }

    public void updateDeck(int chosenPile) {
        log.info("chosen pile: {}", chosenPile);
        List<String> pile1 = getPile1();
        List<String> pile2 = getPile2();
        List<String> pile3 = getPile3();
        List<String> newDeck = new ArrayList<>();

        switch (chosenPile) {
            case 1:
                newDeck.addAll(pile2);
                newDeck.addAll(pile1);
                newDeck.addAll(pile3);
                break;
            case 2:
                newDeck.addAll(pile1);
                newDeck.addAll(pile2);
                newDeck.addAll(pile3);
                break;
            case 3:
                newDeck.addAll(pile1);
                newDeck.addAll(pile3);
                newDeck.addAll(pile2);
                break;
        }

        Arrays.asList(pile1, pile2, pile3).forEach(List::clear);

        IntStream.range(FIRST_CARD, PILE_SIZE)
                 .forEach(j -> {
            pile1.add(newDeck.get(j * 3));
            pile2.add(newDeck.get(j * 3 + 1));
            pile3.add(newDeck.get(j * 3 + 2));
        });

        deck.clear();
        deck.addAll(pile1);
        deck.addAll(pile2);
        deck.addAll(pile3);
        log.info("Updated deck: {}", deck);
    }

    public String getSelectedCard() {
        return deck.get(SELECTED_CARD);
    }

}