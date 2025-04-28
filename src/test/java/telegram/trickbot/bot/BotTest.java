package telegram.trickbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.objects.*;
import telegram.trickbot.service.DeckManager;
import telegram.trickbot.service.MessagesManager;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotTest {

    @Mock
    private DeckManager deckManager;

    @Mock
    private MessagesManager messagesManager;

    @InjectMocks
    private Bot bot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bot = new Bot("dummy-token", deckManager);
        injectBotUsername(bot, "dummyBot");
    }

    private void injectBotUsername(Bot bot, String username) {
        try {
            var field = Bot.class.getDeclaredField("botUsername");
            field.setAccessible(true);
            field.set(bot, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetBotUsername() {
        assertThat(bot.getBotUsername()).isEqualTo("dummyBot");
    }

    @Test
    void testOnUpdateReceived_TextMessage_Start() {
        ReflectionTestUtils.setField(bot, "messageSender", messagesManager);

        Update update = mockUpdateWithText("/start", 123L, "TestUser");

        bot.onUpdateReceived(update);

        verify(messagesManager).sendWelcomeMessage(123L, "TestUser");
    }

    @Test
    void testOnUpdateReceived_TextMessage_NonStart() {
        Update update = mockUpdateWithText("Hello world", 123L, "TestUser");

        bot.onUpdateReceived(update);

        verify(messagesManager, never()).sendWelcomeMessage(anyLong(), anyString());
    }

    @Test
    void testOnUpdateReceived_Callback_StartTrick() {
        Update update = mockUpdateWithCallback("start_trick", 123L);

        bot.onUpdateReceived(update);

        verify(deckManager).shuffleDeck();
    }

    @Test
    void testOnUpdateReceived_Callback_ChoosePile() {
        Update update = mockUpdateWithCallback("2", 123L);

        bot.onUpdateReceived(update);

        verify(deckManager).updateDeck(2);
    }

    @Test
    void testStartTrick() {
        bot.startTrick(321L);

        verify(deckManager).shuffleDeck();
    }

    @Test
    void testPerformTrick() {
        ReflectionTestUtils.setField(bot, "chatId", 123L);
        ReflectionTestUtils.setField(bot, "messageSender", messagesManager);

        bot.performTrick();

        verify(messagesManager, times(1)).sendTextMessage(eq(123L), contains("СТОПКА 1"));
        verify(messagesManager, times(1)).sendTextMessage(eq(123L), contains("СТОПКА 2"));
        verify(messagesManager, times(1)).sendTextMessage(eq(123L), contains("СТОПКА 3"));
        verify(messagesManager, times(3)).sendMediaGroup(eq(123L), anyList());
        verify(messagesManager, times(1)).sendMessageWithKeyboard(eq(123L), contains("В какой стопке"), any());
    }

    @Test
    void testHandleButtonPress_PerformMoreTricks() {
        ReflectionTestUtils.setField(bot, "chatId", 456L);
        ReflectionTestUtils.setField(bot, "step", 0);
        ReflectionTestUtils.setField(bot, "messageSender", messagesManager);

        bot.handleButtonPress("1"); // step 1
        bot.handleButtonPress("2"); // step 2

        verify(deckManager, times(2)).updateDeck(anyInt());
        verify(messagesManager, atLeastOnce()).sendTextMessage(eq(456L), anyString());
    }

    @Test
    void testHandleButtonPress_ShowEleventhCard_AfterThreeSteps() {
        ReflectionTestUtils.setField(bot, "chatId", 456L);
        ReflectionTestUtils.setField(bot, "step", 2); // чтобы сразу показать карточку
        ReflectionTestUtils.setField(bot, "messageSender", messagesManager);

        when(deckManager.getSelectedCard()).thenReturn("photo_url");

        bot.handleButtonPress("3"); // третий шаг

        verify(deckManager, times(1)).updateDeck(anyInt());
        verify(messagesManager, times(1)).sendPhoto(eq(456L), eq("photo_url"), contains("Компания"));
    }

    @Test
    void testShowEleventhCard() {
        when(deckManager.getSelectedCard()).thenReturn("photo_url");

        ReflectionTestUtils.setField(bot, "chatId", 123L);
        ReflectionTestUtils.setField(bot, "step", 2);

        bot.handleButtonPress("1");

        verify(deckManager).getSelectedCard();
    }

    @Test
    void testSleepHandlesException() {
        Thread.currentThread().interrupt();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bot.sleep(10));
        assertThat(exception.getMessage()).isNotEmpty();
    }

    private Update mockUpdateWithText(String text, long chatId, String username) {
        Update update = new Update();
        Message message = mock(Message.class);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(chatId);
        User user = new User();
        user.setFirstName(username);
        when(message.getFrom()).thenReturn(user);
        update.setMessage(message);
        return update;
    }

    private Update mockUpdateWithCallback(String data, long chatId) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        callbackQuery.setData(data);
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);
        return update;
    }
}
