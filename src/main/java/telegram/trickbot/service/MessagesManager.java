package telegram.trickbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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
import java.util.List;

@Component
@Slf4j
public class MessagesManager {
    private final TelegramLongPollingBot bot;
    private static final String WELCOME_MESSAGE =
            "Приветствую вас, %s! Нажмите на кнопку чтобы начать. " +
                    "Из появившегося списка компаний загадайте одну. " +
                    "Далее три раза подряд укажите в какой стопке она находится.";

    public MessagesManager(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void sendWelcomeMessage(long chatId, String userName) {
        String text = String.format(WELCOME_MESSAGE, userName);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("🔹 НАЧАТЬ 🔹")
                                .callbackData("start_trick")
                                .build()))
                .build();
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .replyMarkup(keyboard)
                    .disableWebPagePreview(true)
                    .build()
            );
        }
        catch (TelegramApiException e) {
            log.error("Error sending welcome message: {}", e.getMessage());
        }
    }

    public void sendTextMessage(long chatId, String text) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending text message: {}", e.getMessage());
        }
    }

    public void sendMediaGroup(long chatId, List<String> pile) {
        List<InputMedia> mediaGroup = new ArrayList<>();
        pile.forEach(photoPath -> {
            InputMediaPhoto photo = new InputMediaPhoto();
            photo.setMedia(new InputFile(new File(photoPath)).getNewMediaFile(), photoPath);
            mediaGroup.add(photo);
        });

        try {
            bot.execute(SendMediaGroup.builder()
                    .chatId(chatId)
                    .medias(mediaGroup)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending media group: {}", e.getMessage());
        }
    }

    public void sendPhoto(long chatId, String photoPath, String caption) {
        try {
            bot.execute(SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(new File(photoPath)))
                    .caption(caption)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending photo: {}", e.getMessage());
        }
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

    public void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboardMarkup)
                    .build());
        }
        catch (TelegramApiException e) {
            log.error("Error sending message with keyboard: {}", e.getMessage());
        }
    }

}
