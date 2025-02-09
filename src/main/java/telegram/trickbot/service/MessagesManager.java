package telegram.trickbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MessagesManager {
    private static final Logger log = LoggerFactory.getLogger(MessagesManager.class);
    private final TelegramLongPollingBot bot;
    private static final String WELCOME_MESSAGE =
            "Приветствую вас, %s! Нажмите на кнопку чтобы начать. " +
            "Из появившегося списка компаний загадайте одну. " +
                    "Далее три раза подряд укажите в какой стопке она находится.";
    private final List<Integer> botMessageIds;

    public MessagesManager(TelegramLongPollingBot bot) {
        this.bot = bot;
        this.botMessageIds = new ArrayList<>();
    }

    public void addMessageId(int messageId) {
        botMessageIds.add(messageId);
    }

    public void addMessageId(List<Integer> messageIds) {
        botMessageIds.addAll(messageIds);
    }

    public Message sendWelcomeMessage(long chatId, String userName) {
        String text = String.format(WELCOME_MESSAGE, userName);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("🔹 НАЧАТЬ 🔹")
                                .callbackData("start_trick")
                                .build()))
                .build();
        try {
            Message message = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .replyMarkup(keyboard)
                    .disableWebPagePreview(true)
                    .build()
            );
            return message;
        }
        catch (TelegramApiException e) {
            log.error("Error sending welcome message: {}", e.getMessage());
            return null;
        }
    }

    public Message sendTextMessage(long chatId, String text) {
        Message message = null;
        try {
            message = bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending text message: {}", e.getMessage());
        }

        return message;
    }

    public List<Message> sendMediaGroup(long chatId, List<String> pile) {
        List<InputMedia> mediaGroup = new ArrayList<>();
        pile.forEach(photoPath -> {
            InputMediaPhoto photo = new InputMediaPhoto();
            photo.setMedia(new InputFile(new File(photoPath)).getNewMediaFile(), photoPath);
            mediaGroup.add(photo);
        });
        List<Message> messages;
        try {
            messages = bot.execute(SendMediaGroup.builder()
                    .chatId(chatId)
                    .medias(mediaGroup)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending media group: {}", e.getMessage());
            return Collections.emptyList();
        }
        return messages;
    }

    public Message sendPhoto(long chatId, String photoPath, String caption) {
        Message message = null;
        try {
            message = bot.execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(new File(photoPath)))
                    .caption(caption)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending photo: {}", e.getMessage());
        }
        return message;
    }

    public InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("СТОПКА 1").callbackData("1").build());
        row.add(InlineKeyboardButton.builder().text("СТОПКА 2").callbackData("2").build());
        row.add(InlineKeyboardButton.builder().text("СТОПКА 3").callbackData("3").build());
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public Message sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
        try {
            return bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboardMarkup)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending message with keyboard: {}", e.getMessage());
            return null;
        }
    }

    public void deleteMessages(long chatId) {
        botMessageIds.forEach(messageId -> {
            try {
                bot.execute(DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .build());
            }
            catch (TelegramApiException e) {
                log.error("Error deleting message {}: {}", messageId, e.getMessage());
            }
        });
    }

}