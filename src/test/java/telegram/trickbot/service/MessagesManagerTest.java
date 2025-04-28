package telegram.trickbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagesManagerTest {
    private TelegramLongPollingBot bot;
    private MessagesManager messagesManager;

    @BeforeEach
    void setUp() {
        bot = mock(TelegramLongPollingBot.class);
        messagesManager = new MessagesManager(bot);
    }

    @Test
    void testSendWelcomeMessage_success() throws TelegramApiException {
        messagesManager.sendWelcomeMessage(123L, "TestUser");

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendWelcomeMessage_exception() throws TelegramApiException {
        doThrow(new TelegramApiException("test")).when(bot).execute(any(SendMessage.class));

        assertDoesNotThrow(() -> messagesManager.sendWelcomeMessage(123L, "TestUser"));

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendTextMessage_success() throws TelegramApiException {
        messagesManager.sendTextMessage(456L, "Hello");

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendTextMessage_exception() throws TelegramApiException {
        doThrow(new TelegramApiException("test")).when(bot).execute(any(SendMessage.class));

        assertDoesNotThrow(() -> messagesManager.sendTextMessage(456L, "Hello"));

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendPhoto_success() throws TelegramApiException {
        messagesManager.sendPhoto(789L, "path/to/photo.jpg", "Caption");

        verify(bot, times(1)).execute(any(SendPhoto.class));
    }

    @Test
    void testSendPhoto_exception() throws TelegramApiException {
        doThrow(new TelegramApiException("test")).when(bot).execute(any(SendPhoto.class));

        assertDoesNotThrow(() -> messagesManager.sendPhoto(789L, "path/to/photo.jpg", "Caption"));

        verify(bot, times(1)).execute(any(SendPhoto.class));
    }

    @Test
    void testSendMediaGroup_success() throws TelegramApiException {
        messagesManager.sendMediaGroup(321L, List.of("photo1.jpg", "photo2.jpg"));

        verify(bot, times(1)).execute(any(SendMediaGroup.class));
    }

    @Test
    void testSendMediaGroup_exception() throws TelegramApiException {
        doThrow(new TelegramApiException("test")).when(bot).execute(any(SendMediaGroup.class));

        assertDoesNotThrow(() -> messagesManager.sendMediaGroup(321L, List.of("photo1.jpg", "photo2.jpg")));

        verify(bot, times(1)).execute(any(SendMediaGroup.class));
    }

    @Test
    void testSendMessageWithKeyboard_success() throws TelegramApiException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        messagesManager.sendMessageWithKeyboard(555L, "Choose", keyboard);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendMessageWithKeyboard_exception() throws TelegramApiException {
        doThrow(new TelegramApiException("test")).when(bot).execute(any(SendMessage.class));
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        assertDoesNotThrow(() -> messagesManager.sendMessageWithKeyboard(555L, "Choose", keyboard));

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testCreateInlineKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = messagesManager.createInlineKeyboard();

        assertNotNull(keyboardMarkup);
        assertEquals(1, keyboardMarkup.getKeyboard().size());
        assertEquals(3, keyboardMarkup.getKeyboard().get(0).size());
    }
}
