package telegram.trickbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeckManagerTest {
    private DeckManager deckManager;

    @BeforeEach
    void setUp() {
        deckManager = new DeckManager("path/", ".jpg");
    }

    @Test
    void testDeckInitialization() {
        List<String> pile1 = deckManager.getPile1();
        List<String> pile2 = deckManager.getPile2();
        List<String> pile3 = deckManager.getPile3();

        assertEquals(7, pile1.size(), "Pile 1 should have 7 cards");
        assertEquals(7, pile2.size(), "Pile 2 should have 7 cards");
        assertEquals(7, pile3.size(), "Pile 3 should have 7 cards");

        assertEquals(21, pile1.size() + pile2.size() + pile3.size(), "All piles together should have 21 cards");
    }

    @Test
    void testShuffleDeck() {
        List<String> beforeShuffle = List.copyOf(deckManager.getPile1());
        deckManager.shuffleDeck();
        List<String> afterShuffle = deckManager.getPile1();

        assertNotNull(afterShuffle);
        assertEquals(7, afterShuffle.size());
    }

    @Test
    void testUpdateDeck_ChosenPile1() {
        List<String> beforeUpdate = List.copyOf(deckManager.getPile1());

        deckManager.updateDeck(1);

        List<String> afterUpdate = deckManager.getPile1();

        assertNotNull(afterUpdate);
        assertEquals(7, afterUpdate.size());
        assertNotEquals(beforeUpdate, afterUpdate, "Deck should change after update with pile 1");
    }

    @Test
    void testUpdateDeck_ChosenPile2() {
        List<String> beforeUpdate = List.copyOf(deckManager.getPile2());

        deckManager.updateDeck(2);

        List<String> afterUpdate = deckManager.getPile2();

        assertNotNull(afterUpdate);
        assertEquals(7, afterUpdate.size());
        assertNotEquals(beforeUpdate, afterUpdate, "Deck should change after update with pile 2");
    }

    @Test
    void testUpdateDeck_ChosenPile3() {
        List<String> beforeUpdate = List.copyOf(deckManager.getPile3());

        deckManager.updateDeck(3);

        List<String> afterUpdate = deckManager.getPile3();

        assertNotNull(afterUpdate);
        assertEquals(7, afterUpdate.size());
        assertNotEquals(beforeUpdate, afterUpdate, "Deck should change after update with pile 3");
    }

    @Test
    void testGetSelectedCard() {
        String selectedCard = deckManager.getSelectedCard();

        assertNotNull(selectedCard);
        assertTrue(selectedCard.contains("path/"), "Selected card should contain path prefix");
        assertTrue(selectedCard.contains(".jpg"), "Selected card should contain extension suffix");
    }
}
